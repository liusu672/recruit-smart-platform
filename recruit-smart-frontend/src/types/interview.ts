import type { PagedData } from '@/types/api'

export type InterviewRound = 'FIRST' | 'SECOND' | 'HR'
export type InterviewMethod = 'ONLINE' | 'OFFLINE' | 'PHONE'
export type InterviewStatus = 'ASSIGNED' | 'SCHEDULED' | 'COMPLETED' | 'CANCELED' | 'REINTERVIEW'
export type InterviewSuggestion = 'PASS' | 'REJECT' | 'PENDING'
export type InterviewFeedbackState = 'EMPTY' | 'DRAFT' | 'SUBMITTED'

export interface InterviewerOption {
  id: number
  username: string
  realName: string
}

export interface InterviewTaskSummary {
  id: number
  applicationId: number
  candidateId: number
  candidateName: string
  jobTitle: string
  department: string
  round: InterviewRound
  roundText: string
  interviewTime: string | null
  method: InterviewMethod | null
  methodText: string
  location: string | null
  status: InterviewStatus
  statusText: string
  interviewerId: number
  interviewerName: string
  assignedAt: string | null
  scheduledAt: string | null
  feedbackState: InterviewFeedbackState
  feedbackStateText: string
}

export interface InterviewCandidateBrief {
  education: string | null
  school: string | null
  yearsOfExperience: number
  resumeName: string
  skills: string[]
  workExperience: string | null
  projectExperience: string | null
  matchScore: number | null
  matchSummary: string | null
  riskPoints: string[]
}

export interface InterviewScoreItem {
  key: string
  label: string
  description: string
  score: number | null
  evidence: string
}

export interface InterviewQuestion {
  id: string
  category: string
  question: string
  source: 'JOB' | 'RESUME' | 'RISK' | 'MANUAL'
}

export interface InterviewFeedback {
  id: number | null
  interviewId: number
  interviewerId: number
  score: number | null
  comment: string
  suggestion: InterviewSuggestion | null
  aiSummary: string | null
  state: InterviewFeedbackState
  submittedAt: string | null
}

export interface InterviewWorkspace extends InterviewTaskSummary {
  candidateBrief: InterviewCandidateBrief
  scorecard: InterviewScoreItem[]
  questions: InterviewQuestion[]
  feedback: InterviewFeedback
}

export interface InterviewTaskQuery {
  keyword: string
  status: InterviewStatus | ''
  feedbackState: InterviewFeedbackState | ''
  page: number
  pageSize: number
}

export interface InterviewTaskPageResponse {
  total: number
  records: InterviewTaskSummary[]
}

export interface InterviewFeedbackRequest {
  scorecard: InterviewScoreItem[]
  score: number | null
  comment: string
  suggestion: InterviewSuggestion | null
  interviewerId: number
}

export interface InterviewAssignmentRequest {
  applicationId: number
  interviewerId: number
  round: InterviewRound
}

export interface InterviewUpdateRequest {
  interviewerId: number
}

export interface InterviewScheduleRequest {
  interviewTime: string
  method: InterviewMethod
  location: string
}

export interface InterviewQuestionRequest {
  focus: string
  jobId?: number
  candidateId?: number
  resumeId?: number
  jobTitle?: string
  responsibilities?: string
  requirements?: string
  resumeText?: string
  skills?: string
  projectExperience?: string
  workExperience?: string
}

export type InterviewTaskPagedData = PagedData<InterviewTaskSummary>
