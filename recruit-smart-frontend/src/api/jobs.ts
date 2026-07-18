import { http, unwrapResult, unwrapVoidResult } from '@/api/http'
import type { Result } from '@/types/api'
import type {
  JobCreateRequest,
  JobPageResponse,
  JobPagedData,
  JobPosition,
  JobQuery,
  JobUpdateRequest,
} from '@/types/job'

export function adaptJobPage(
  source: JobPageResponse,
  page: number,
  pageSize: number,
): JobPagedData {
  return {
    items: source.records,
    page,
    pageSize,
    total: source.total,
  }
}

export async function getJobs(query: JobQuery): Promise<JobPagedData> {
  const params = {
    pageNum: query.page,
    pageSize: query.pageSize,
    ...(query.keyword ? { keyword: query.keyword } : {}),
    ...(query.department ? { department: query.department } : {}),
    ...(query.status ? { status: query.status } : {}),
  }
  const page = await unwrapResult(http.get<Result<JobPageResponse>>('/jobs', { params }))
  return adaptJobPage(page, query.page, query.pageSize)
}

export function getJobById(id: number): Promise<JobPosition> {
  return unwrapResult(http.get<Result<JobPosition>>(`/jobs/${id}`))
}

export function getOpenJobById(id: number): Promise<JobPosition> {
  return unwrapResult(http.get<Result<JobPosition>>(`/jobs/open/${id}`))
}

export interface JobApplicationRecord {
  id: number
  jobId: number
  jobTitle: string
  candidateId: number
  candidateName: string
  gender: string | null
  age: number | null
  education: string | null
  school: string | null
  major: string | null
  yearsOfExperience: number | null
  resumeId: number
  resumeName: string | null
  resumeFileType: string | null
  status: string
  statusText: string
  allowAdjustment: number
  adjustedJobId: number | null
  appliedAt: string
  reviewedAt: string | null
}

export interface JobApplicationPageResponse {
  total: number
  records: JobApplicationRecord[]
}

export function getJobApplications(
  jobId: number,
  params: { page: number; pageSize: number; status?: string; candidateKeyword?: string },
) {
  return unwrapResult(
    http.get<Result<JobApplicationPageResponse>>(`/jobs/${jobId}/applications`, {
      params: {
        pageNum: params.page,
        pageSize: params.pageSize,
        ...(params.status ? { status: params.status } : {}),
        ...(params.candidateKeyword ? { candidateKeyword: params.candidateKeyword } : {}),
      },
    }),
  )
}

export function createJob(data: JobCreateRequest): Promise<number> {
  return unwrapResult(http.post<Result<number>>('/jobs', data))
}

export function updateJob(id: number, data: JobUpdateRequest): Promise<void> {
  return unwrapVoidResult(http.put<Result<null>>(`/jobs/${id}`, data))
}

export function publishJob(id: number): Promise<void> {
  return unwrapVoidResult(http.put<Result<null>>(`/jobs/${id}/publish`))
}

export function pauseJob(id: number): Promise<void> {
  return unwrapVoidResult(http.put<Result<null>>(`/jobs/${id}/pause`))
}

export function resumeJob(id: number): Promise<void> {
  return unwrapVoidResult(http.put<Result<null>>(`/jobs/${id}/resume`))
}

export function closeJob(id: number): Promise<void> {
  return unwrapVoidResult(http.put<Result<null>>(`/jobs/${id}/close`))
}

export function parseSalaryRange(value: string | null): [number, number] {
  if (!value) return [0, 0]

  const [minimum = Number.NaN, maximum = Number.NaN] = value
    .split('-')
    .map((item) => Number(item.trim()))
  return [Number.isFinite(minimum) ? minimum : 0, Number.isFinite(maximum) ? maximum : 0]
}
