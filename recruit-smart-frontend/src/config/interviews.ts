import type { ApplicationStatus } from '@/types/candidate'
import type {
  InterviewFeedbackState,
  InterviewMethod,
  InterviewScoreItem,
  InterviewRound,
  InterviewStatus,
  InterviewSuggestion,
} from '@/types/interview'

export const interviewStatusOptions: Array<{ label: string; value: InterviewStatus }> = [
  { label: '待预约', value: 'ASSIGNED' },
  { label: '待面试', value: 'SCHEDULED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELED' },
  { label: '需复试', value: 'REINTERVIEW' },
]

export const interviewMethodOptions: Array<{ label: string; value: InterviewMethod }> = [
  { label: '线上面试', value: 'ONLINE' },
  { label: '线下面试', value: 'OFFLINE' },
  { label: '电话面试', value: 'PHONE' },
]

export const interviewRoundOptions: Array<{ label: string; value: InterviewRound }> = [
  { label: '一面', value: 'FIRST' },
  { label: '二面', value: 'SECOND' },
  { label: 'HR 面', value: 'HR' },
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

type InterviewScoreCriterion = Omit<InterviewScoreItem, 'score' | 'evidence'>

export const interviewScorecardTemplates: Record<
  InterviewRound,
  InterviewScoreCriterion[]
> = {
  FIRST: [
    {
      key: 'resume-verification',
      label: '简历与经历真实性',
      description: '候选人描述的职责、技术细节与成果是否真实且前后一致。',
    },
    {
      key: 'fundamentals',
      label: '核心基础能力',
      description: '是否掌握岗位要求的基础知识、常用方法与基本实践。',
    },
    {
      key: 'basic-problem-solving',
      label: '基础问题解决',
      description: '能否理解问题、拆解步骤并给出清晰可执行的处理思路。',
    },
    {
      key: 'communication-motivation',
      label: '沟通与岗位动机',
      description: '表达是否清晰，求职动机与岗位方向是否基本匹配。',
    },
  ],
  SECOND: [
    {
      key: 'professional-depth',
      label: '专业深度',
      description: '能否深入解释关键原理、边界条件与实际应用经验。',
    },
    {
      key: 'solution-design',
      label: '方案设计与取舍',
      description: '能否针对复杂场景设计方案并说明成本、风险与取舍依据。',
    },
    {
      key: 'ownership-results',
      label: '项目主导与结果',
      description: '是否真正承担关键责任，并能用事实说明推进过程和最终结果。',
    },
    {
      key: 'collaboration-influence',
      label: '协作与影响力',
      description: '能否处理分歧、推动跨角色协作并对团队结果产生积极影响。',
    },
  ],
  HR: [
    {
      key: 'career-motivation',
      label: '求职动机',
      description: '离职原因、求职诉求与选择本岗位的动机是否合理一致。',
    },
    {
      key: 'stability-planning',
      label: '稳定性与职业规划',
      description: '职业规划是否清晰，与岗位发展路径和预期任职周期是否匹配。',
    },
    {
      key: 'values-fit',
      label: '价值观与团队匹配',
      description: '工作方式、责任意识和协作理念是否与团队要求相符。',
    },
    {
      key: 'offer-risk',
      label: '薪资、到岗与录用风险',
      description: '薪资期望、到岗时间及其他可能影响录用或入职的风险是否可控。',
    },
  ],
}

export function getDefaultInterviewScorecard(round: InterviewRound) {
  return interviewScorecardTemplates[round]
}

export function getInterviewRoundText(round: InterviewRound) {
  if (round === 'SECOND') return '二面'
  if (round === 'HR') return 'HR 面'
  return '一面'
}

export function getInterviewArrangementText(round: InterviewRound) {
  return `\u5b89\u6392${getInterviewRoundText(round).replace(/\s+/g, '')}`
}

interface NextInterviewRoundContext {
  applicationStatus: ApplicationStatus
  currentRound: InterviewRound | null | undefined
  interviewStatus: InterviewStatus | null | undefined
  feedbackState: InterviewFeedbackState | null | undefined
  requiredInterviewRounds: number
}

const orderedInterviewRounds: InterviewRound[] = ['FIRST', 'SECOND', 'HR']

export function getNextSchedulableInterviewRound(context: NextInterviewRoundContext) {
  const requiredRounds = Math.min(Math.max(context.requiredInterviewRounds, 1), 3)
  if (!['SCREEN_PASSED', 'INTERVIEWING'].includes(context.applicationStatus)) return null

  if (!context.currentRound) return orderedInterviewRounds[0] ?? null

  const currentIndex = orderedInterviewRounds.indexOf(context.currentRound)
  if (currentIndex < 0 || currentIndex >= requiredRounds) return null

  if (context.interviewStatus === 'CANCELED') return context.currentRound
  if (
    context.interviewStatus !== 'COMPLETED' ||
    context.feedbackState !== 'SUBMITTED' ||
    currentIndex + 1 >= requiredRounds
  ) {
    return null
  }

  return orderedInterviewRounds[currentIndex + 1] ?? null
}

export function getInterviewMethodText(method: InterviewMethod | null) {
  if (!method) return '待确认'
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
