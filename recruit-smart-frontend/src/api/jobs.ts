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
