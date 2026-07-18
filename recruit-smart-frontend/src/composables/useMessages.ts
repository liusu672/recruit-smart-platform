import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

import {
  getConversationMessages,
  getConversations,
  getUnreadMessageCount,
  markConversationRead,
  sendConversationMessage,
  subscribeToMessageStream,
} from '@/api/messages'

const streamStatus = ref<'idle' | 'connected' | 'reconnecting'>('idle')
const streamError = ref<Error | null>(null)
const streamFailureCount = ref(0)
const streamLastSyncedAt = ref<string | null>(null)
let retryStreamHandler: (() => void) | null = null

export function useMessages() {
  const queryClient = useQueryClient()
  const selectedConversationId = ref<number | null>(null)
  const conversationPage = ref(1)
  const conversationPageSize = ref(20)
  const messagePage = ref(1)
  const messagePageSize = ref(50)

  const conversationsQuery = useQuery({
    queryKey: computed(() => [
      'message-conversations',
      conversationPage.value,
      conversationPageSize.value,
    ]),
    queryFn: () =>
      getConversations({ page: conversationPage.value, pageSize: conversationPageSize.value }),
  })

  const messagesQuery = useQuery({
    queryKey: computed(() => [
      'conversation-messages',
      selectedConversationId.value,
      messagePage.value,
      messagePageSize.value,
    ]),
    enabled: computed(() => selectedConversationId.value !== null),
    queryFn: () => {
      const id = selectedConversationId.value
      if (id === null) throw new Error('尚未选择消息会话')
      return getConversationMessages(id, {
        page: messagePage.value,
        pageSize: messagePageSize.value,
      })
    },
  })

  const sendMutation = useMutation({
    mutationFn: ({ conversationId, content }: { conversationId: number; content: string }) =>
      sendConversationMessage(conversationId, content),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['conversation-messages'] }),
        queryClient.invalidateQueries({ queryKey: ['message-conversations'] }),
        queryClient.invalidateQueries({ queryKey: ['message-unread-count'] }),
      ])
    },
  })

  const markReadMutation = useMutation({
    mutationFn: (conversationId: number) => markConversationRead(conversationId),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['conversation-messages'] }),
        queryClient.invalidateQueries({ queryKey: ['message-conversations'] }),
        queryClient.invalidateQueries({ queryKey: ['message-unread-count'] }),
      ])
    },
  })

  function selectConversation(id: number) {
    selectedConversationId.value = id
    messagePage.value = 1
    void markReadMutation.mutateAsync(id)
  }

  return {
    selectedConversationId,
    conversationPage,
    conversationPageSize,
    messagePage,
    messagePageSize,
    conversationsQuery,
    messagesQuery,
    sendMutation,
    markReadMutation,
    streamStatus,
    streamError,
    streamFailureCount,
    streamLastSyncedAt,
    retryStream: () => retryStreamHandler?.(),
    selectConversation,
  }
}

export function useMessageNotifications() {
  const queryClient = useQueryClient()
  const unreadQuery = useQuery({
    queryKey: ['message-unread-count'],
    queryFn: getUnreadMessageCount,
    refetchInterval: 30_000,
  })
  let streamController: AbortController | null = null

  function startStream() {
    streamController?.abort()
    streamController = new AbortController()
    const controller = streamController
    streamStatus.value = 'idle'

    void subscribeToMessageStream({
      signal: controller.signal,
      onStatus(status) {
        streamStatus.value = status
        if (status === 'connected') {
          streamError.value = null
          streamFailureCount.value = 0
          streamLastSyncedAt.value = new Date().toISOString()
        }
      },
      onEvent(event) {
        streamLastSyncedAt.value =
          event.type === 'message-updated' && event.changedAt
            ? event.changedAt
            : new Date().toISOString()
        void queryClient.invalidateQueries({ queryKey: ['message-conversations'] })
        void queryClient.invalidateQueries({ queryKey: ['conversation-messages'] })
        void queryClient.invalidateQueries({ queryKey: ['message-unread-count'] })
      },
      onError(error) {
        streamFailureCount.value += 1
        streamError.value = error
      },
    }).catch((error: unknown) => {
      if (!controller.signal.aborted) {
        streamStatus.value = 'reconnecting'
        streamFailureCount.value += 1
        streamError.value = error instanceof Error ? error : new Error('消息实时连接失败')
      }
    })
  }

  onMounted(() => {
    retryStreamHandler = startStream
    startStream()
  })

  onBeforeUnmount(() => {
    streamController?.abort()
    streamController = null
    if (retryStreamHandler === startStream) retryStreamHandler = null
    streamStatus.value = 'idle'
    streamError.value = null
    streamFailureCount.value = 0
  })
  return {
    unreadQuery,
    streamStatus,
    streamError,
    failureCount: streamFailureCount,
    lastSyncedAt: streamLastSyncedAt,
    retryStream: startStream,
  }
}
