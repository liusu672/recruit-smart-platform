import type { PagedData } from '@/types/api'
import type { ApplicationStatus } from '@/types/candidate'
import type {
  InterviewFeedbackState,
  InterviewMethod,
  InterviewRound,
  InterviewStatus,
} from '@/types/interview'

export type PipelineViewMode = 'BOARD' | 'TABLE'
export type PipelineStageKey = 'NEW' | 'SCREENING' | 'INTERVIEW' | 'OFFER' | 'HIRED' | 'CLOSED'
export type ScreeningDecision = 'PASS' | 'REJECT' | 'PENDING'

export interface PipelineInterviewSummary {
  id: number
  round: InterviewRound
  roundText: string
  interviewerName: string
  interviewerId: number
  interviewTime: string | null
  method: InterviewMethod | null
  methodText: string
  location: string | null
  status: InterviewStatus
  statusText: string
  assignedAt: string | null
  scheduledAt: string | null
  feedbackState?: InterviewFeedbackState | null
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

export interface AiMatchSummary {
  matchScore: number
  recommendLevel: string
  recommendReason: string
  highlightSummary: string
  riskSummary: string
  modelName: string
  generatedAt: string
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

export interface PipelineApplicationSummary {
  id: number
  candidateId: number
  candidateName: string
  education: string | null
  yearsOfExperience: number
  jobId: number
  jobTitle: string
  department: string
  status: ApplicationStatus
  statusText: string
  matchScore: number | null
  recommendLevel: string | null
  ownerId: number | null
  ownerName: string | null
  source: string
  sourceText: string
  reviewDecision: ScreeningDecision | null
  appliedAt: string
  lastActivityAt: string
}

export interface PipelineApplicationDetail extends PipelineApplicationSummary {
  candidatePhone: string | null
  candidateEmail: string | null
  school: string | null
  resumeId: number
  resumeName: string | null
  resumeFileType: string | null
  allowAdjustment: boolean
  adjustedJobId: number | null
  hrNote: string | null
  rejectReasonCode: string | null
  rejectReason: string | null
  reviewedAt: string | null
  requiredInterviewRounds?: number | null
  aiMatch: AiMatchSummary | null
  interview: PipelineInterviewSummary | null
  offer: PipelineOfferSummary | null
  timeline: PipelineTimelineEvent[]
}

export interface PipelinePageResponse {
  total: number
  records: PipelineApplicationSummary[]
}

export interface PipelineStageCount {
  stage: PipelineStageKey
  count: number
}

export interface PipelineQuery {
  keyword: string
  jobId: number | null
  stage?: PipelineStageKey | ''
  status: ApplicationStatus | ''
  page: number
  pageSize: number
}

export interface PipelineReviewRequest {
  decision: ScreeningDecision
  rejectReasonCode: string
  note: string
}

export interface PipelineRejectRequest {
  reasonCode: string
  reason: string
}

export interface PipelineStatusUpdateRequest {
  status: ApplicationStatus
}

export type PipelinePagedData = PagedData<PipelineApplicationSummary>
