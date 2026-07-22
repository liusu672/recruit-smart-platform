import { http } from '@/api/http'
import { startAiStream } from '@/api/aiStream'
import type { AiStreamEvent } from '@/types/ai'

export interface ToolChatRequest {
  message: string
}

export interface ToolChatStreamOptions {
  message: string
  signal: AbortSignal
  onEvent: (event: AiStreamEvent) => void
  onError?: (error: Error) => void
}

export async function sendToolChatMessage(message: string): Promise<string> {
  const response = await http.post<string>('/ai/tool-chat', { message } satisfies ToolChatRequest)
  return response.data
}

export function streamToolChatMessage(options: ToolChatStreamOptions) {
  const streamOptions = {
    path: '/ai/tool-chat/stream',
    body: { message: options.message } satisfies ToolChatRequest,
    signal: options.signal,
    onEvent: options.onEvent,
  }

  return startAiStream(
    options.onError ? { ...streamOptions, onError: options.onError } : streamOptions,
  )
}
