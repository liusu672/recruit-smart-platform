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

  const unreadQuery = useQuery({
    queryKey: ['message-unread-count'],
    queryFn: getUnreadMessageCount,
    refetchInterval: 30_000,
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
    unreadQuery,
    sendMutation,
    markReadMutation,
    streamStatus,
    streamError,
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
  const streamController = new AbortController()

  onMounted(() => {
    void subscribeToMessageStream({
      signal: streamController.signal,
      onStatus(status) {
        streamStatus.value = status
        if (status === 'connected') streamError.value = null
      },
      onEvent() {
        void queryClient.invalidateQueries({ queryKey: ['message-conversations'] })
        void queryClient.invalidateQueries({ queryKey: ['conversation-messages'] })
        void queryClient.invalidateQueries({ queryKey: ['message-unread-count'] })
      },
      onError(error) {
        streamError.value = error
      },
    }).catch((error: unknown) => {
      if (!streamController.signal.aborted) {
        streamStatus.value = 'reconnecting'
        streamError.value = error instanceof Error ? error : new Error('消息实时连接失败')
      }
    })
  })

  onBeforeUnmount(() => {
    streamController.abort()
    streamStatus.value = 'idle'
    streamError.value = null
  })
  return { unreadQuery, streamStatus, streamError }
}
