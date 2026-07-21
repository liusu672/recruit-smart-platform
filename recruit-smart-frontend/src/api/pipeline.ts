import { http, unwrapResult, unwrapVoidResult } from '@/api/http'
import type { Result } from '@/types/api'
import type {
  PipelineApplicationDetail,
  AiMatchSummary,
  PipelinePagedData,
  PipelinePageResponse,
  PipelineQuery,
  PipelineReviewRequest,
  PipelineStatusUpdateRequest,
} from '@/types/pipeline'

export function adaptPipelinePage(
  source: PipelinePageResponse,
  page: number,
  pageSize: number,
): PipelinePagedData {
  return {
    items: source.records,
    page,
    pageSize,
    total: source.total,
  }
}

export async function getPipelineApplications(query: PipelineQuery): Promise<PipelinePagedData> {
  const params = {
    pageNum: query.page,
    pageSize: query.pageSize,
    ...(query.keyword ? { keyword: query.keyword } : {}),
    ...(query.jobId !== null ? { jobId: query.jobId } : {}),
    ...(query.status ? { status: query.status } : {}),
  }
  const result = await unwrapResult(
    http.get<Result<PipelinePageResponse>>('/applications/pipeline', { params }),
  )
  return adaptPipelinePage(result, query.page, query.pageSize)
}

export function getPipelineApplication(id: number): Promise<PipelineApplicationDetail> {
  return unwrapResult(http.get<Result<PipelineApplicationDetail>>(`/applications/${id}/pipeline`))
}

export function updateApplicationStatus(id: number, data: PipelineStatusUpdateRequest) {
  return unwrapVoidResult(http.put<Result<null>>(`/applications/${id}/status`, data))
}

export function reviewApplication(id: number, data: PipelineReviewRequest) {
  return unwrapVoidResult(http.put<Result<null>>(`/applications/${id}/screening`, data))
}

export function generateApplicationAiMatch(id: number): Promise<AiMatchSummary> {
  return unwrapResult(
    http.post<Result<AiMatchSummary>>(`/applications/${id}/ai-match`),
  )
}
