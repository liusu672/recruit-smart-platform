import { beforeEach, describe, expect, it, vi } from 'vitest'

import { sendToolChatMessage } from '@/api/aiAssistant'
import { http } from '@/api/http'
import { generateInterviewQuestions } from '@/api/interviews'

describe('HR AI assistant API contract', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('sends Tool Chat messages as raw-string AI service requests', async () => {
    const post = vi.spyOn(http, 'post').mockResolvedValue({ data: '2026-07-22' })

    const result = await sendToolChatMessage('今天几号？')

    expect(post).toHaveBeenCalledWith('/ai/tool-chat', { message: '今天几号？' })
    expect(post).not.toHaveBeenCalledWith('/interviews/20/ai-questions', expect.anything())
    expect(result).toBe('2026-07-22')
  })

  it('keeps interview follow-up generation on the Biz aggregate endpoint', async () => {
    const post = vi.spyOn(http, 'post').mockResolvedValue({
      data: {
        code: 200,
        message: 'success',
        data: {
          category: 'Java后端',
          questions: ['请说明 Java 并发排查思路'],
        },
      },
    })

    await generateInterviewQuestions(20, { focus: 'Java 并发' })

    expect(post).toHaveBeenCalledWith('/interviews/20/ai-questions', { focus: 'Java 并发' })
    expect(post).not.toHaveBeenCalledWith('/ai/tool-chat', expect.anything())
  })
})
