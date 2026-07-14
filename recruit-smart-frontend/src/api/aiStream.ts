import { fetchEventSource } from '@microsoft/fetch-event-source'

import { useSessionStore } from '@/stores/session'
import type { AiStreamEvent } from '@/types/ai'

interface AiStreamOptions {
  path: string
  body: unknown
  signal: AbortSignal
  onEvent: (event: AiStreamEvent) => void
  onError?: (error: Error) => void
}

export async function startAiStream(options: AiStreamOptions) {
  const session = useSessionStore()

  // fetch-event-source 支持 POST、JWT 请求头、停止生成和错误重试处理。
  await fetchEventSource(`/api${options.path}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(session.token ? { Authorization: `${session.tokenType} ${session.token}` } : {}),
    },
    body: JSON.stringify(options.body),
    signal: options.signal,
    onmessage(message) {
      if (!message.data) return
      // 后端应输出类型化 AI 生命周期事件，而不是无约束的自由文本。
      options.onEvent(JSON.parse(message.data) as AiStreamEvent)
    },
    onerror(error) {
      const normalizedError = error instanceof Error ? error : new Error('AI 流式请求失败')
      options.onError?.(normalizedError)
      throw normalizedError
    },
  })
}
