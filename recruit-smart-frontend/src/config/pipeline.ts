import type { ApplicationStatus } from '@/types/candidate'
import type { HrStatusTone } from '@/types/hr'
import type { PipelineStageKey, ScreeningDecision } from '@/types/pipeline'

export interface PipelineStageDefinition {
  key: PipelineStageKey
  label: string
  description: string
  statuses: ApplicationStatus[]
}

export const pipelineStages: PipelineStageDefinition[] = [
  { key: 'NEW', label: '新投递', description: '等待 HR 开始筛选', statuses: ['SUBMITTED'] },
  {
    key: 'SCREENING',
    label: 'HR 筛选',
    description: '通过、拒绝或保留待定',
    statuses: ['SCREENING'],
  },
  {
    key: 'INTERVIEW',
    label: '面试',
    description: '初筛通过与面试进行中',
    statuses: ['SCREEN_PASSED', 'INTERVIEWING'],
  },
  { key: 'OFFER', label: 'Offer', description: '等待候选人决定', statuses: ['OFFERED'] },
  { key: 'HIRED', label: '已录用', description: '已接受并进入入职', statuses: ['HIRED'] },
  {
    key: 'CLOSED',
    label: '已结束',
    description: '拒绝、撤回或流程终止',
    statuses: ['SCREEN_REJECT', 'REJECTED', 'WITHDRAWN'],
  },
]

export const defaultPipelineStageKey: PipelineStageKey = 'NEW'

export const applicationStatusOptions: Array<{ label: string; value: ApplicationStatus }> = [
  { label: '已投递', value: 'SUBMITTED' },
  { label: '筛选中', value: 'SCREENING' },
  { label: '初筛通过', value: 'SCREEN_PASSED' },
  { label: '初筛未通过', value: 'SCREEN_REJECT' },
  { label: '面试中', value: 'INTERVIEWING' },
  { label: 'Offer 阶段', value: 'OFFERED' },
  { label: '已入职', value: 'HIRED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已撤回', value: 'WITHDRAWN' },
]

export const pipelineRejectReasonOptions = [
  { label: '核心技能不匹配', value: 'SKILL_NOT_MATCH' },
  { label: '经验年限不符合', value: 'EXPERIENCE_NOT_MATCH' },
  { label: '岗位方向不匹配', value: 'ROLE_NOT_MATCH' },
  { label: '候选人主动终止', value: 'CANDIDATE_WITHDRAW' },
  { label: '其他业务原因', value: 'OTHER' },
]

export const screeningDecisionCopy: Record<
  ScreeningDecision,
  { title: string; confirmLabel: string; description: string }
> = {
  PASS: {
    title: '确认初筛通过',
    confirmLabel: '确认通过',
    description: '通过后进入面试准备阶段，后续仍需由 HR 安排面试。',
  },
  REJECT: {
    title: '确认初筛未通过',
    confirmLabel: '确认拒绝',
    description: '该操作会结束当前投递流程，必须记录可审计的业务原因。',
  },
  PENDING: {
    title: '保留为待定',
    confirmLabel: '确认待定',
    description: '候选人继续停留在筛选阶段，请说明需要补充核实的信息。',
  },
}

export function getApplicationStatusText(status: ApplicationStatus) {
  return applicationStatusOptions.find((option) => option.value === status)?.label ?? status
}

export function getPipelineStageKey(status: ApplicationStatus): PipelineStageKey {
  return pipelineStages.find((stage) => stage.statuses.includes(status))?.key ?? 'CLOSED'
}

export function getPipelineStage(key: PipelineStageKey) {
  return pipelineStages.find((stage) => stage.key === key) ?? pipelineStages[0]!
}

export function isPipelineStageKey(value: string): value is PipelineStageKey {
  return pipelineStages.some((stage) => stage.key === value)
}

export function getApplicationStatusTone(status: ApplicationStatus): HrStatusTone {
  if (status === 'HIRED' || status === 'OFFERED') return 'success'
  if (status === 'SCREEN_REJECT' || status === 'REJECTED') return 'danger'
  if (status === 'SCREEN_PASSED' || status === 'INTERVIEWING') return 'info'
  if (status === 'WITHDRAWN') return 'neutral'
  return 'warning'
}

export function isTerminalApplicationStatus(status: ApplicationStatus) {
  return ['SCREEN_REJECT', 'REJECTED', 'HIRED', 'WITHDRAWN'].includes(status)
}

export function getScreeningDecisionStatus(decision: ScreeningDecision): ApplicationStatus {
  if (decision === 'PASS') return 'SCREEN_PASSED'
  if (decision === 'REJECT') return 'SCREEN_REJECT'
  return 'SCREENING'
}
