import { http } from '@/api/http'

export interface ToolChatRequest {
  message: string
}

export async function sendToolChatMessage(message: string): Promise<string> {
  const response = await http.post<string>('/ai/tool-chat', { message } satisfies ToolChatRequest)
  return response.data
}
