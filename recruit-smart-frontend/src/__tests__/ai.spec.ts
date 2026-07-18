import { describe, expect, it } from 'vitest'

import { adaptFeedbackSummary, adaptTurnoverRisk } from '@/api/ai'
import { adaptInterviewQuestions } from '@/api/interviews'

describe('raw AI response adapters', () => {
  it('filters malformed interview questions and supplies a category fallback', () => {
    expect(adaptInterviewQuestions(7, { questions: ['说明项目取舍', null, 3] })).toEqual([
      expect.objectContaining({ id: '7-ai-0', category: 'AI 追问', question: '说明项目取舍' }),
    ])
  })

  it('normalizes feedback arrays without overwriting original feedback fields', () => {
    expect(
      adaptFeedbackSummary({ summary: '表达清晰', advantages: ['结构化'], risks: '未知' }),
    ).toEqual({ summary: '表达清晰', advantages: ['结构化'], risks: [], suggestion: '' })
  })

  it('bounds risk scores and rejects unknown levels', () => {
    expect(adaptTurnoverRisk({ riskLevel: 'UNKNOWN', riskScore: 120 })).toMatchObject({
      riskLevel: 'MEDIUM',
      riskScore: 100,
      riskReasons: [],
      suggestions: [],
    })
  })
})
