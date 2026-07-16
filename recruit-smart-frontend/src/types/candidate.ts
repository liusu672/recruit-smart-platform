import type { PagedData } from '@/types/api'

export type CandidateStatus = 'AVAILABLE' | 'INTERVIEWING' | 'HIRED'
export type CandidateSource = 'SELF_REGISTER' | 'HR_IMPORT' | 'ONLINE'
export type ApplicationStatus =
  | 'SUBMITTED'
  | 'SCREENING'
  | 'SCREEN_PASSED'
  | 'SCREEN_REJECT'
  | 'INTERVIEWING'
  | 'OFFERED'
  | 'HIRED'
  | 'REJECTED'
  | 'WITHDRAWN'

export interface CandidateSummary {
  id: number
  userId: number | null
  name: string
  gender: string | null
  phone: string | null
  email: string | null
  education: string | null
  school: string | null
  major: string | null
  yearsOfExperience: number
  currentStatus: CandidateStatus
  currentStatusText: string
  source: CandidateSource
  sourceText: string
  resumeCount: number
  latestApplicationStatus: ApplicationStatus | null
  latestApplicationStatusText: string | null
  latestJobTitle: string | null
  latestMatchScore: number | null
  lastActivityAt: string | null
  duplicateRisk: boolean
}

export interface CandidateResume {
  id: number
  resumeName: string
  fileUrl: string | null
  fileType: string | null
  parsedContent: string | null
  skills: string[]
  projectExperience: string | null
  workExperience: string | null
  isDefault: boolean
  updatedAt: string | null
}

export interface CandidateAiMatch {
  score: number
  level: 'HIGH' | 'MEDIUM' | 'LOW'
  summary: string
  highlights: string[]
  risks: string[]
  suggestion: string
  modelName: string
  generatedAt: string
}

export interface CandidateApplication {
  id: number
  jobId: number
  jobTitle: string
  resumeId: number
  status: ApplicationStatus
  statusText: string
  source: string
  allowAdjustment: boolean
  hrNote: string | null
  appliedAt: string
  aiMatch: CandidateAiMatch | null
}

export interface CandidateDetail extends CandidateSummary {
  resumes: CandidateResume[]
  applications: CandidateApplication[]
}

export interface CandidatePageResponse {
  total: number
  records: CandidateSummary[]
}

export interface CandidateQuery {
  keyword: string
  education: string
  school: string
  yearsOfExperienceMin: number | null
  currentStatus: CandidateStatus | ''
  page: number
  pageSize: number
}

export interface CandidateCreateRequest {
  name: string
  gender: string
  phone: string
  email: string
  education: string
  school: string
  major: string
  yearsOfExperience: number
  currentStatus: CandidateStatus
  source: CandidateSource
}

export type CandidatePagedData = PagedData<CandidateSummary>
