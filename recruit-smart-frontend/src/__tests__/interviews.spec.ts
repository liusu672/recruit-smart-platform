import { describe, expect, it } from 'vitest'

import { adaptInterviewTaskPage } from '@/api/interviews'
import {
  applyDemoInterviewDraft,
  applyDemoInterviewSubmit,
  getDemoInterviewPage,
  initialDemoInterviews,
} from '@/config/demoInterviews'
import { calculateInterviewScore } from '@/config/interviews'

describe('interview API adaptation', () => {
  it('maps task records to the shared pagination contract', () => {
    const interview = initialDemoInterviews[0]!
    const page = adaptInterviewTaskPage({ total: 1, records: [interview] }, 2, 8)

    expect(page).toEqual({ items: [interview], page: 2, pageSize: 8, total: 1 })
  })
})

describe('interview workspace demo operations', () => {
  it('filters tasks by keyword, interview status and feedback state', () => {
    const page = getDemoInterviewPage(initialDemoInterviews, {
      keyword: '陈思悦',
      status: 'COMPLETED',
      feedbackState: 'SUBMITTED',
      page: 1,
      pageSize: 8,
    })

    expect(page.total).toBe(1)
    expect(page.items[0]?.candidateName).toBe('陈思悦')
  })

  it('calculates a 100-point score only after every criterion is rated', () => {
    expect(calculateInterviewScore([4, 3, 3, 2])).toBe(75)
    expect(calculateInterviewScore([4, null, 3, 2])).toBeNull()
  })

  it('saves incomplete work as a draft without producing an AI summary', () => {
    const interview = structuredClone(initialDemoInterviews[0]!)
    const updated = applyDemoInterviewDraft(interview, {
      scorecard: interview.scorecard,
      score: null,
      comment: '继续核实消息一致性。',
      suggestion: null,
      interviewerId: 3,
    })

    expect(updated.feedbackState).toBe('DRAFT')
    expect(updated.feedback.comment).toBe('继续核实消息一致性。')
    expect(updated.feedback.aiSummary).toBeNull()
  })

  it('requires evidence before submitting human feedback', () => {
    const interview = structuredClone(initialDemoInterviews[1]!)

    expect(() =>
      applyDemoInterviewSubmit(interview, {
        scorecard: interview.scorecard,
        score: null,
        comment: '',
        suggestion: null,
        interviewerId: 3,
      }),
    ).toThrow('请完成所有评分并填写评价证据')
  })

  it('locks submitted feedback while keeping the AI summary separate', () => {
    const interview = structuredClone(initialDemoInterviews[0]!)
    const scorecard = interview.scorecard.map((item, index) => ({
      ...item,
      score: index === 0 ? 4 : 3,
      evidence: item.evidence || '候选人提供了可复核的项目事实。',
    }))
    const updated = applyDemoInterviewSubmit(
      interview,
      {
        scorecard,
        score: 81,
        comment: '原始面试官评价。',
        suggestion: 'PASS',
        interviewerId: 3,
      },
      '2026-07-15T16:30:00',
    )

    expect(updated).toMatchObject({
      status: 'COMPLETED',
      feedbackState: 'SUBMITTED',
      feedback: {
        comment: '原始面试官评价。',
        suggestion: 'PASS',
        aiSummary: null,
      },
    })
    expect(() =>
      applyDemoInterviewDraft(updated, {
        scorecard,
        score: 81,
        comment: '尝试覆盖',
        suggestion: 'REJECT',
        interviewerId: 3,
      }),
    ).toThrow('已提交的反馈不能覆盖')
  })
})
