import type {
  InterviewFeedbackState,
  InterviewScoreItem,
  InterviewScoreOption,
  InterviewTaskSummary,
  InterviewerTaskStage,
} from '@/types/interview'

export const interviewScoreOptions: InterviewScoreOption[] = [
  { value: 1, label: '不符合', description: '没有提供满足岗位要求的有效证据。' },
  { value: 2, label: '部分符合', description: '具备部分能力，但关键经验或深度仍需补充。' },
  { value: 3, label: '符合', description: '提供了能够满足岗位要求的清晰证据。' },
  { value: 4, label: '超出预期', description: '能力深度、影响范围或结果明显高于岗位要求。' },
]

export function getInterviewerTaskStage(
  task: Pick<InterviewTaskSummary, 'status' | 'feedbackState'>,
): InterviewerTaskStage {
  if (task.status === 'CANCELED') return 'CANCELED'
  if (task.status === 'REINTERVIEW') return 'REINTERVIEW'
  if (task.status === 'ASSIGNED') return 'SCHEDULE'
  if (task.status === 'SCHEDULED') return 'ATTEND'
  return task.feedbackState === 'SUBMITTED' ? 'SUBMITTED' : 'FEEDBACK'
}

export function getInterviewerStageText(stage: InterviewerTaskStage) {
  const labels: Record<InterviewerTaskStage, string> = {
    SCHEDULE: '确认安排',
    ATTEND: '待完成面试',
    FEEDBACK: '填写反馈',
    SUBMITTED: '反馈已提交',
    CANCELED: '已取消',
    REINTERVIEW: '需要复试',
  }
  return labels[stage]
}

export function getInterviewerStageTone(stage: InterviewerTaskStage) {
  if (stage === 'SUBMITTED') return 'success'
  if (stage === 'FEEDBACK' || stage === 'REINTERVIEW') return 'warning'
  if (stage === 'CANCELED') return 'neutral'
  return 'info'
}

export function getMissingFeedbackCount(
  scorecard: InterviewScoreItem[],
  comment: string,
  suggestion: string | null,
) {
  const missingCriteria = scorecard.filter(
    (item) => item.score === null || !item.evidence.trim(),
  ).length
  return missingCriteria + (comment.trim() ? 0 : 1) + (suggestion ? 0 : 1)
}

export function isHttpMeetingLocation(value: string | null | undefined) {
  if (!value) return false
  try {
    const url = new URL(value)
    return url.protocol === 'http:' || url.protocol === 'https:'
  } catch {
    return false
  }
}

export function getInterviewerPriorityTask(input: {
  feedbackEmpty: InterviewTaskSummary[]
  feedbackDraft: InterviewTaskSummary[]
  scheduled: InterviewTaskSummary[]
  assigned: InterviewTaskSummary[]
  now?: Date
}) {
  const pendingFeedback = [...input.feedbackDraft, ...input.feedbackEmpty]
  if (pendingFeedback[0]) return pendingFeedback[0]

  const now = input.now ?? new Date()
  const scheduled = [...input.scheduled].sort((a, b) => {
    const left = a.interviewTime ? new Date(a.interviewTime).getTime() : Number.MAX_SAFE_INTEGER
    const right = b.interviewTime ? new Date(b.interviewTime).getTime() : Number.MAX_SAFE_INTEGER
    return left - right
  })
  const elapsed = scheduled.find(
    (item) => item.interviewTime && new Date(item.interviewTime).getTime() <= now.getTime(),
  )
  if (elapsed) return elapsed
  if (scheduled[0]) return scheduled[0]
  return input.assigned[0] ?? null
}

export function hasPendingFeedbackState(state: InterviewFeedbackState) {
  return state === 'EMPTY' || state === 'DRAFT'
}
