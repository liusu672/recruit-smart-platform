import { describe, expect, it } from 'vitest'

import { adaptInterviewTaskPage } from '@/api/interviews'
import {
  applyDemoInterviewCompletion,
  applyDemoInterviewDraft,
  applyDemoInterviewSchedule,
  applyDemoInterviewSubmit,
  getDemoInterviewPage,
  initialDemoInterviews,
} from '@/config/demoInterviews'
import { calculateInterviewScore, getInterviewStatusText } from '@/config/interviews'
import {
  getInterviewerPriorityTask,
  getInterviewerTaskStage,
  getMissingFeedbackCount,
  isHttpMeetingLocation,
} from '@/config/interviewer'

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

  it('schedules an assigned task before allowing it to be completed', () => {
    const assigned = structuredClone(initialDemoInterviews[0]!)
    assigned.status = 'ASSIGNED'
    assigned.statusText = getInterviewStatusText('ASSIGNED')
    assigned.interviewTime = null
    assigned.method = null
    assigned.methodText = '待确认'
    assigned.location = null
    assigned.scheduledAt = null

    expect(
      getDemoInterviewPage([assigned], {
        keyword: '',
        status: 'ASSIGNED',
        feedbackState: '',
        page: 1,
        pageSize: 8,
      }).total,
    ).toBe(1)

    const scheduled = applyDemoInterviewSchedule(
      assigned,
      {
        interviewTime: '2026-07-20T14:00:00',
        method: 'ONLINE',
        location: '腾讯会议 123456',
      },
      '2026-07-17T10:00:00',
    )
    expect(scheduled).toMatchObject({
      status: 'SCHEDULED',
      interviewTime: '2026-07-20T14:00:00',
      scheduledAt: '2026-07-17T10:00:00',
    })

    expect(applyDemoInterviewCompletion(scheduled)).toMatchObject({
      status: 'COMPLETED',
      statusText: '已完成',
    })
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
    const interview = structuredClone(initialDemoInterviews[2]!)
    interview.feedbackState = 'EMPTY'
    interview.feedbackStateText = '未填写'
    interview.feedback = {
      ...interview.feedback,
      id: null,
      state: 'EMPTY',
      score: null,
      comment: '',
      suggestion: null,
      submittedAt: null,
    }

    expect(() =>
      applyDemoInterviewSubmit(interview, {
        scorecard: interview.scorecard.map((item, index) =>
          index === 0 ? { ...item, score: null, evidence: '' } : item,
        ),
        score: null,
        comment: '',
        suggestion: null,
        interviewerId: 3,
      }),
    ).toThrow('请完成所有评分并填写评价证据')
  })

  it('locks submitted feedback while keeping the AI summary separate', () => {
    const interview = applyDemoInterviewCompletion(structuredClone(initialDemoInterviews[0]!))
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

describe('interviewer task experience helpers', () => {
  it('keeps interview and feedback states as separate task stages', () => {
    expect(getInterviewerTaskStage({ status: 'ASSIGNED', feedbackState: 'EMPTY' })).toBe('SCHEDULE')
    expect(getInterviewerTaskStage({ status: 'SCHEDULED', feedbackState: 'DRAFT' })).toBe('ATTEND')
    expect(getInterviewerTaskStage({ status: 'COMPLETED', feedbackState: 'DRAFT' })).toBe(
      'FEEDBACK',
    )
    expect(getInterviewerTaskStage({ status: 'COMPLETED', feedbackState: 'SUBMITTED' })).toBe(
      'SUBMITTED',
    )
  })

  it('prioritizes unfinished feedback before scheduled and assigned tasks', () => {
    const draft = initialDemoInterviews[0]!
    const submitted = initialDemoInterviews[2]!
    const assigned = { ...draft, id: 999, status: 'ASSIGNED' as const }
    const priority = getInterviewerPriorityTask({
      feedbackDraft: [{ ...draft, status: 'COMPLETED' }],
      feedbackEmpty: [],
      scheduled: [submitted],
      assigned: [assigned],
      now: new Date('2026-07-18T12:00:00'),
    })

    expect(priority?.id).toBe(draft.id)
  })

  it('counts every missing scorecard and summary requirement', () => {
    const scorecard = initialDemoInterviews[0]!.scorecard.map((item) => ({
      ...item,
      score: null,
      evidence: '',
    }))
    expect(getMissingFeedbackCount(scorecard, '', null)).toBe(scorecard.length + 2)
  })

  it('only treats HTTP and HTTPS locations as meeting links', () => {
    expect(isHttpMeetingLocation('https://meeting.example.com/room')).toBe(true)
    expect(isHttpMeetingLocation('http://localhost/room')).toBe(true)
    expect(isHttpMeetingLocation('武汉研发中心 3A')).toBe(false)
    expect(isHttpMeetingLocation('javascript:alert(1)')).toBe(false)
  })
})
