import type { JobPosition } from '@/types/job'
import type { InterviewMethod, InterviewStatus } from '@/types/interview'

export interface PageResponse<T> {
  total: number
  records: T[]
}

export interface CandidateProfile {
  id: number
  name: string
  gender: string | null
  age: number | null
  phone: string | null
  email: string | null
  education: string | null
  school: string | null
  major: string | null
  yearsOfExperience: number
  currentStatus: string | null
  source: string | null
  hasAccount: boolean
  createdAt: string | null
  updatedAt: string | null
  resumes: ResumeSummary[]
}

export interface CandidateProfileUpdate {
  gender: string
  age: number | null
  education: string
  school: string
  major: string
  yearsOfExperience: number
}

export interface ResumeSummary {
  id: number
  resumeName: string
  fileUrl: string | null
  fileType: string | null
  isDefault: number
  createdAt: string | null
  parseStatus: string | null
  parseStatusText: string | null
}

export interface CandidateApplication {
  id: number
  jobId: number
  jobTitle: string
  department: string
  resumeId: number
  resumeName: string
  status: string
  statusText: string
  allowAdjustment: number
  adjustedJobId: number | null
  appliedAt: string
}

export interface PortalInterview {
  id: number
  applicationId: number
  jobId: number
  jobTitle: string
  candidateId: number
  candidateName: string
  interviewerId: number
  interviewerName: string
  round: string
  roundText: string
  interviewTime: string | null
  method: InterviewMethod | null
  methodText: string
  location: string | null
  status: InterviewStatus
  statusText: string
  assignedAt: string | null
  scheduledAt: string | null
}

export interface CandidateOffer {
  id: number
  applicationId: number
  jobId: number
  jobTitle: string
  department: string
  salary: number
  entryDate: string
  workLocation: string
  status: string
  statusText: string
  sentAt: string | null
  acceptedAt: string | null
}

export interface CandidateOnboarding {
  id: number
  offerId: number
  applicationId: number
  jobId: number
  jobTitle: string
  department: string
  entryDate: string
  status: string
  statusText: string
  currentStep: string
  materialStatus: string
  materialStatusText: string
  completedAt: string | null
  createdAt: string
}

export interface CandidatePortalSnapshot {
  jobs: JobPosition[]
  resumes: ResumeSummary[]
  applications: CandidateApplication[]
  interviews: PortalInterview[]
  offers: CandidateOffer[]
  onboardings: CandidateOnboarding[]
}
