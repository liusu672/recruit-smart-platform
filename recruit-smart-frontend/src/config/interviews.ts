import type {
  InterviewFeedbackState,
  InterviewMethod,
  InterviewRound,
  InterviewStatus,
  InterviewSuggestion,
} from '@/types/interview'

export const interviewStatusOptions: Array<{ label: string; value: InterviewStatus }> = [
  { label: '待面试', value: 'SCHEDULED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELED' },
  { label: '需复试', value: 'REINTERVIEW' },
]

export const interviewFeedbackStateOptions: Array<{
  label: string
  value: InterviewFeedbackState
}> = [
  { label: '未填写', value: 'EMPTY' },
  { label: '草稿', value: 'DRAFT' },
  { label: '已提交', value: 'SUBMITTED' },
]

export const interviewSuggestionOptions: Array<{
  label: string
  value: InterviewSuggestion
}> = [
  { label: '建议通过', value: 'PASS' },
  { label: '建议待定', value: 'PENDING' },
  { label: '建议不通过', value: 'REJECT' },
]

export const defaultInterviewScorecard = [
  {
    key: 'professional',
    label: '专业能力',
    description: '是否具备岗位要求的核心知识、方法与实践深度。',
  },
  {
    key: 'problem-solving',
    label: '问题解决',
    description: '能否拆解问题、说明判断依据并形成可执行方案。',
  },
  {
    key: 'collaboration',
    label: '协作与影响力',
    description: '能否清晰沟通、推动协作并对结果负责。',
  },
  {
    key: 'reflection',
    label: '结果与复盘',
    description: '能否量化结果、识别不足并沉淀后续改进。',
  },
]

export function getInterviewRoundText(round: InterviewRound) {
  if (round === 'SECOND') return '二面'
  if (round === 'HR') return 'HR 面'
  return '一面'
}

export function getInterviewMethodText(method: InterviewMethod) {
  if (method === 'OFFLINE') return '线下面试'
  if (method === 'PHONE') return '电话面试'
  return '线上面试'
}

export function getInterviewStatusText(status: InterviewStatus) {
  return interviewStatusOptions.find((item) => item.value === status)?.label ?? status
}

export function getFeedbackStateText(state: InterviewFeedbackState) {
  return interviewFeedbackStateOptions.find((item) => item.value === state)?.label ?? state
}

export function getInterviewStatusTone(status: InterviewStatus) {
  if (status === 'COMPLETED') return 'success'
  if (status === 'CANCELED') return 'danger'
  if (status === 'REINTERVIEW') return 'warning'
  return 'info'
}

export function getFeedbackStateTone(state: InterviewFeedbackState) {
  if (state === 'SUBMITTED') return 'success'
  if (state === 'DRAFT') return 'warning'
  return 'danger'
}

export function calculateInterviewScore(scores: Array<number | null>) {
  if (scores.some((score) => score === null)) return null
  const total = scores.reduce<number>((sum, score) => sum + (score ?? 0), 0)
  return Math.round((total / (scores.length * 4)) * 100)
}
