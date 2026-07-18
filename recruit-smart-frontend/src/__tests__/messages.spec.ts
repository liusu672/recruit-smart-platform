import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, afterEach, describe, expect, it, vi } from 'vitest'
import { fetchEventSource } from '@microsoft/fetch-event-source'

import {
  getConversationMessages,
  getConversations,
  markConversationRead,
  subscribeToMessageStream,
} from '@/api/messages'
import { http } from '@/api/http'
import { useSessionStore } from '@/stores/session'

vi.mock('@microsoft/fetch-event-source', () => ({ fetchEventSource: vi.fn() }))

describe('message API contracts', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    sessionStorage.clear()
  })
  afterEach(() => vi.restoreAllMocks())

  it('maps conversation and message pagination to the frontend contract', async () => {
    const get = vi.spyOn(http, 'get').mockResolvedValue({
      data: { code: 200, message: 'success', data: { total: 3, records: [{ id: 1 }] } },
    } as never)

    await expect(getConversations({ page: 2, pageSize: 20 })).resolves.toMatchObject({
      page: 2,
      pageSize: 20,
      total: 3,
    })
    await getConversationMessages(9, { page: 1, pageSize: 50 })
    expect(get).toHaveBeenNthCalledWith(2, '/messages/conversations/9/messages', {
      params: { pageNum: 1, pageSize: 50 },
    })
  })

  it('uses an empty request body for mark-read action endpoints', async () => {
    const put = vi.spyOn(http, 'put').mockResolvedValue({
      data: { code: 200, message: 'success', data: null },
    } as never)
    await markConversationRead(9)
    expect(put).toHaveBeenCalledWith('/messages/conversations/9/read')
  })

  it('carries JWT and maps SSE updates while exposing reconnect state', async () => {
    vi.mocked(fetchEventSource).mockResolvedValue(undefined)
    useSessionStore().setSession({
      token: 'token-1',
      tokenType: 'Bearer',
      user: { id: '1', username: 'hr01', name: 'HR', role: 'HR', status: 1 },
    })
    const events: string[] = []
    const statuses: string[] = []
    const controller = new AbortController()
    await subscribeToMessageStream({
      signal: controller.signal,
      onEvent: (event) => events.push(event.type),
      onStatus: (status) => statuses.push(status),
    })

    const options = vi.mocked(fetchEventSource).mock.calls[0]?.[1]
    expect(options?.headers).toMatchObject({ Authorization: 'Bearer token-1' })
    await options?.onopen?.(new Response(null, { status: 200 }))
    options?.onmessage?.({ event: 'message', data: '{"changedAt":"2026-07-18"}', id: '' })
    options?.onclose?.()
    expect(events).toEqual(['message-updated'])
    expect(statuses).toEqual(['connected', 'reconnecting'])
  })
})
