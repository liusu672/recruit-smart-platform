export type DashboardTaskType = 'SCREENING' | 'INTERVIEW_FEEDBACK' | 'OFFER' | 'ONBOARDING'

export interface DashboardMetrics {
  pendingScreening: number
  pendingFeedback: number
  activeOffers: number
  reviewingOnboardings: number
}

export interface DashboardTask {
  type: DashboardTaskType
  relatedId: number
  applicationId: number | null
  candidateId: number | null
  candidateName: string | null
  jobId: number | null
  jobTitle: string | null
  title: string
  status: string
  statusText: string
  occurredAt: string | null
}

export interface DashboardOverview {
  metrics: DashboardMetrics
  tasks: DashboardTask[]
}
