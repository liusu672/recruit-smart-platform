import { http, unwrapResult, unwrapVoidResult } from '@/api/http'
import type { Result } from '@/types/api'
import type {
  PipelineApplication,
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

// 后端目前只有投递、面试和 Offer 实体；以下聚合接口待业务服务实现后再核对字段。
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

export function getPipelineApplication(id: number): Promise<PipelineApplication> {
  return unwrapResult(http.get<Result<PipelineApplication>>(`/applications/${id}/pipeline`))
}

export function updateApplicationStatus(id: number, data: PipelineStatusUpdateRequest) {
  return unwrapVoidResult(http.put<Result<null>>(`/applications/${id}/status`, data))
}

export function reviewApplication(id: number, data: PipelineReviewRequest) {
  return unwrapVoidResult(http.put<Result<null>>(`/applications/${id}/screening`, data))
}
