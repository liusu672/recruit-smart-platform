import type { PagedData } from '@/types/api'
import type { ApplicationStatus, CandidateAiMatch } from '@/types/candidate'

export type PipelineViewMode = 'BOARD' | 'TABLE'
export type PipelineStageKey = 'NEW' | 'SCREENING' | 'INTERVIEW' | 'OFFER' | 'HIRED' | 'CLOSED'
export type ScreeningDecision = 'PASS' | 'REJECT' | 'PENDING'

export interface PipelineInterviewSummary {
  id: number
  round: string
  interviewerName: string
  interviewTime: string
  method: string
  location: string | null
  status: string
  statusText: string
  feedbackScore: number | null
  feedbackSuggestion: string | null
}

export interface PipelineOfferSummary {
  id: number
  salary: number | null
  entryDate: string | null
  workLocation: string | null
  status: string
  statusText: string
}

export interface PipelineTimelineEvent {
  id: string
  title: string
  description: string
  actorName: string
  occurredAt: string
  source: 'BUSINESS' | 'AI'
  relatedObject: string
}

export interface PipelineApplication {
  id: number
  candidateId: number
  candidateName: string
  candidatePhone: string | null
  education: string | null
  school: string | null
  yearsOfExperience: number
  resumeId: number
  resumeName: string
  jobId: number
  jobTitle: string
  department: string
  status: ApplicationStatus
  statusText: string
  source: string
  sourceText: string
  allowAdjustment: boolean
  ownerName: string | null
  appliedAt: string
  lastActivityAt: string
  hrNote: string | null
  rejectReasonCode: string | null
  rejectReason: string | null
  reviewDecision: ScreeningDecision | null
  aiMatch: CandidateAiMatch | null
  interview: PipelineInterviewSummary | null
  offer: PipelineOfferSummary | null
  timeline: PipelineTimelineEvent[]
}

export interface PipelinePageResponse {
  total: number
  records: PipelineApplication[]
}

export interface PipelineQuery {
  keyword: string
  jobId: number | null
  status: ApplicationStatus | ''
  page: number
  pageSize: number
}

export interface PipelineReviewRequest {
  decision: ScreeningDecision
  rejectReasonCode: string
  note: string
  reviewedBy: number
}

export interface PipelineStatusUpdateRequest {
  toStatus: ApplicationStatus
  note: string
  operatorId: number
}

export type PipelinePagedData = PagedData<PipelineApplication>
