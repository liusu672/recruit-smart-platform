import type { PagedData } from '@/types/api'

export type JobStatus = 'DRAFT' | 'OPEN' | 'PAUSED' | 'CLOSED'

export interface JobPosition {
  id: number
  title: string
  department: string
  location: string | null
  jobType: string | null
  salaryRange: string | null
  headcount: number
  experienceRequirement: string | null
  educationRequirement: string | null
  description: string | null
  requirement: string | null
  status: JobStatus
  statusText: string
  createdAt: string | null
  updatedAt: string | null
}

export interface JobPageResponse {
  total: number
  records: JobPosition[]
}

export interface JobQuery {
  keyword: string
  department: string
  status: JobStatus | ''
  page: number
  pageSize: number
}

export interface JobCreateRequest {
  title: string
  department: string
  location: string
  salaryMin: number
  salaryMax: number
  headcount: number
  responsibilities: string
  requirements: string
}

export type JobUpdateRequest = JobCreateRequest

export interface JobUpdateCommand {
  id: number
  data: JobUpdateRequest
}

export type JobPagedData = PagedData<JobPosition>

export interface JobFormValue extends JobUpdateRequest {
  id: number | null
}
