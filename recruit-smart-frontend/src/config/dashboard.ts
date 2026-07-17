import type { DashboardMetrics, DashboardTask, DashboardTaskType } from '@/types/dashboard'

export type DashboardTaskTone = 'danger' | 'info' | 'warning'

export interface DashboardMetricCard {
  key: keyof DashboardMetrics
  label: string
  value: number
  status: string
}

export const emptyDashboardMetrics: DashboardMetrics = {
  pendingScreening: 0,
  pendingFeedback: 0,
  activeOffers: 0,
  reviewingOnboardings: 0,
}

const dashboardMetricDefinitions: Array<Pick<DashboardMetricCard, 'key' | 'label'>> = [
  { key: 'pendingScreening', label: '待筛选投递' },
  { key: 'pendingFeedback', label: '待补充反馈' },
  { key: 'activeOffers', label: '进行中 Offer' },
  { key: 'reviewingOnboardings', label: '入职材料审核' },
]

export const dashboardTaskRoutes: Record<DashboardTaskType, string> = {
  SCREENING: '/hr/pipeline',
  INTERVIEW_FEEDBACK: '/hr/interviews',
  OFFER: '/hr/offers',
  ONBOARDING: '/hr/onboardings',
}

export function normalizeDashboardMetrics(metrics?: DashboardMetrics | null): DashboardMetrics {
  return {
    pendingScreening: metrics?.pendingScreening ?? 0,
    pendingFeedback: metrics?.pendingFeedback ?? 0,
    activeOffers: metrics?.activeOffers ?? 0,
    reviewingOnboardings: metrics?.reviewingOnboardings ?? 0,
  }
}

export function buildDashboardMetricCards(
  metrics?: DashboardMetrics | null,
): DashboardMetricCard[] {
  const normalized = normalizeDashboardMetrics(metrics)
  return dashboardMetricDefinitions.map((definition) => {
    const value = normalized[definition.key]
    return {
      ...definition,
      value,
      status: value > 0 ? '需要及时处理' : '暂无待处理',
    }
  })
}

export function getDashboardTaskRoute(type: DashboardTaskType): string {
  return dashboardTaskRoutes[type]
}

export function getDashboardTaskTone(type: DashboardTaskType): DashboardTaskTone {
  if (type === 'INTERVIEW_FEEDBACK') return 'danger'
  if (type === 'OFFER') return 'info'
  return 'warning'
}

export function formatDashboardOccurredAt(value: string | null): string {
  if (!value) return '时间待定'
  return value.replace('T', ' ').slice(0, 16)
}

export function getDashboardTaskMeta(task: DashboardTask): string {
  const parts = [
    task.candidateName ? `候选人：${task.candidateName}` : '',
    task.jobTitle ? `职位：${task.jobTitle}` : '',
    `时间：${formatDashboardOccurredAt(task.occurredAt)}`,
  ].filter(Boolean)

  return parts.join(' · ')
}

export function getDashboardTaskKey(task: DashboardTask): string {
  return `${task.type}-${task.relatedId}-${task.occurredAt ?? 'pending'}`
}
