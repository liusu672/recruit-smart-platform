import type { PagedData } from '@/types/api'

export type InterviewRound = 'FIRST' | 'SECOND' | 'HR'
export type InterviewMethod = 'ONLINE' | 'OFFLINE' | 'PHONE'
export type InterviewStatus = 'SCHEDULED' | 'COMPLETED' | 'CANCELED' | 'REINTERVIEW'
export type InterviewSuggestion = 'PASS' | 'REJECT' | 'PENDING'
export type InterviewFeedbackState = 'EMPTY' | 'DRAFT' | 'SUBMITTED'

export interface InterviewTaskSummary {
  id: number
  applicationId: number
  candidateId: number
  candidateName: string
  jobTitle: string
  department: string
  round: InterviewRound
  roundText: string
  interviewTime: string
  method: InterviewMethod
  methodText: string
  location: string | null
  status: InterviewStatus
  statusText: string
  interviewerId: number
  interviewerName: string
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

export interface InterviewQuestionRequest {
  focus: string
}

export type InterviewTaskPagedData = PagedData<InterviewTaskSummary>
