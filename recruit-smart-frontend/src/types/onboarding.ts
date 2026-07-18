import type { PagedData } from '@/types/api'

export type OnboardingStatus = 'PENDING' | 'REVIEWING' | 'APPROVED' | 'ONBOARDED' | 'CANCELED'
export type MaterialStatus = 'PENDING' | 'REVIEWING' | 'APPROVED' | 'REJECTED'

export interface OnboardingTimelineEvent {
  id: string
  title: string
  description: string
  actorName: string
  occurredAt: string
}

export interface OnboardingRecord {
  id: number
  offerId: number
  candidateId: number
  candidateName: string
  candidatePhone: string | null
  candidateEmail: string | null
  jobTitle: string
  department: string
  entryDate: string
  status: OnboardingStatus
  statusText: string
  currentStep: string
  materialStatus: MaterialStatus
  materialStatusText: string
  remark: string | null
  completedAt: string | null
  createdAt: string
  updatedAt: string
  timeline: OnboardingTimelineEvent[]
}

export interface OnboardingQuery {
  keyword: string
  status: OnboardingStatus | ''
  page: number
  pageSize: number
}

export interface OnboardingPageResponse {
  total: number
  records: OnboardingRecord[]
}

export interface OnboardingActionRequest {
  note: string
}

export interface MaterialReviewRequest extends OnboardingActionRequest {
  decision: 'APPROVE' | 'REJECT'
}

export interface CancelOnboardingRequest {
  reason: string
}

export type OnboardingPagedData = PagedData<OnboardingRecord>
