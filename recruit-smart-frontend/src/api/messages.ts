import { fetchEventSource } from '@microsoft/fetch-event-source'

import { http, unwrapResult, unwrapVoidResult } from '@/api/http'
import type { Result } from '@/types/api'
import type {
  MessageConversation,
  MessageConversationPagedData,
  MessagePageQuery,
  MessageRecord,
  MessageRecordPagedData,
  MessageRealtimeEvent,
} from '@/types/message'
import { useSessionStore } from '@/stores/session'

interface BackendPage<T> {
  total: number
  records: T[]
}

function adaptPage<T>(source: BackendPage<T>, query: MessagePageQuery) {
  return {
    items: source.records,
    page: query.page,
    pageSize: query.pageSize,
    total: source.total,
  }
}

export function getConversations(query: MessagePageQuery): Promise<MessageConversationPagedData> {
  return unwrapResult(
    http.get<Result<BackendPage<MessageConversation>>>('/messages/conversations', {
      params: { pageNum: query.page, pageSize: query.pageSize },
    }),
  ).then((page) => adaptPage(page, query))
}

export function getConversationMessages(
  conversationId: number,
  query: MessagePageQuery,
): Promise<MessageRecordPagedData> {
  return unwrapResult(
    http.get<Result<BackendPage<MessageRecord>>>(
      `/messages/conversations/${conversationId}/messages`,
      { params: { pageNum: query.page, pageSize: query.pageSize } },
    ),
  ).then((page) => adaptPage(page, query))
}

export function getOrCreateConversation(applicationId: number): Promise<number> {
  return unwrapResult(http.post<Result<number>>('/messages/conversations', { applicationId }))
}

export function sendConversationMessage(conversationId: number, content: string): Promise<number> {
  return unwrapResult(
    http.post<Result<number>>(`/messages/conversations/${conversationId}/messages`, { content }),
  )
}

export function markConversationRead(conversationId: number): Promise<void> {
  return unwrapVoidResult(http.put<Result<null>>(`/messages/conversations/${conversationId}/read`))
}

export function getUnreadMessageCount(): Promise<number> {
  return unwrapResult(http.get<Result<number>>('/messages/unread-count'))
}

export interface MessageStreamOptions {
  signal: AbortSignal
  onEvent: (event: MessageRealtimeEvent) => void
  onStatus?: (status: 'connected' | 'reconnecting') => void
  onError?: (error: Error) => void
}

export async function subscribeToMessageStream(options: MessageStreamOptions) {
  const session = useSessionStore()

  await fetchEventSource('/api/messages/stream', {
    method: 'GET',
    headers: {
      ...(session.token ? { Authorization: `${session.tokenType} ${session.token}` } : {}),
    },
    signal: options.signal,
    async onopen(response) {
      if (!response.ok) throw new Error(`消息实时连接失败（${response.status}）`)
      options.onStatus?.('connected')
    },
    onmessage(message) {
      if (!message.data) return
      const payload = JSON.parse(message.data) as { connected?: boolean; changedAt?: string }
      const event: MessageRealtimeEvent =
        message.event === 'connected'
          ? { type: 'connected' }
          : {
              type: 'message-updated',
              ...(payload.changedAt ? { changedAt: payload.changedAt } : {}),
            }
      options.onEvent(event)
    },
    onclose() {
      options.onStatus?.('reconnecting')
    },
    onerror(error) {
      const normalized = error instanceof Error ? error : new Error('消息实时连接失败')
      options.onStatus?.('reconnecting')
      options.onError?.(normalized)
      // 不抛出错误，让 fetch-event-source 按退避策略自动重连。
    },
  })
}
