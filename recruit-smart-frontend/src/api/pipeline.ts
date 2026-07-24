import { http, unwrapResult, unwrapVoidResult } from '@/api/http'
import { pipelineStages } from '@/config/pipeline'
import type { Result } from '@/types/api'
import type {
  AiMatchSummary,
  PipelineApplicationDetail,
  PipelineApplicationSummary,
  PipelinePagedData,
  PipelinePageResponse,
  PipelineQuery,
  PipelineRejectRequest,
  PipelineReviewRequest,
  PipelineStageCount,
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

const PIPELINE_POOL_PAGE_SIZE = 100

type PipelinePoolQuery = Pick<PipelineQuery, 'keyword' | 'jobId'>

function getPipelinePoolParams(query: PipelinePoolQuery) {
  return {
    ...(query.keyword ? { keyword: query.keyword } : {}),
    ...(query.jobId !== null ? { jobId: query.jobId } : {}),
  }
}

async function getPipelineApplicationPool(
  query: PipelinePoolQuery,
): Promise<PipelineApplicationSummary[]> {
  const params = getPipelinePoolParams(query)
  const firstPage = await unwrapResult(
    http.get<Result<PipelinePageResponse>>('/applications/pipeline', {
      params: { pageNum: 1, pageSize: PIPELINE_POOL_PAGE_SIZE, ...params },
    }),
  )
  const remainingPageCount = Math.max(0, Math.ceil(firstPage.total / PIPELINE_POOL_PAGE_SIZE) - 1)
  if (remainingPageCount === 0) return firstPage.records

  const remainingPages = await Promise.all(
    Array.from({ length: remainingPageCount }, (_, index) =>
      unwrapResult(
        http.get<Result<PipelinePageResponse>>('/applications/pipeline', {
          params: {
            pageNum: index + 2,
            pageSize: PIPELINE_POOL_PAGE_SIZE,
            ...params,
          },
        }),
      ),
    ),
  )
  return [firstPage, ...remainingPages].flatMap((page) => page.records)
}

function filterPipelineApplicationPool(
  records: PipelineApplicationSummary[],
  query: Pick<PipelineQuery, 'stage' | 'status'>,
) {
  if (query.status) return records.filter((application) => application.status === query.status)
  if (!query.stage) return records

  const stage = pipelineStages.find((item) => item.key === query.stage)
  if (!stage) return records
  return records.filter((application) => stage.statuses.includes(application.status))
}

export async function getPipelineApplications(query: PipelineQuery): Promise<PipelinePagedData> {
  const pool = await getPipelineApplicationPool(query)
  const filtered = filterPipelineApplicationPool(pool, query)
  const start = (query.page - 1) * query.pageSize
  return {
    items: filtered.slice(start, start + query.pageSize),
    page: query.page,
    pageSize: query.pageSize,
    total: filtered.length,
  }
}

export async function getPipelineStageCounts(
  query: PipelinePoolQuery,
): Promise<PipelineStageCount[]> {
  const pool = await getPipelineApplicationPool(query)
  return pipelineStages.map((stage) => ({
    stage: stage.key,
    count: pool.filter((application) => stage.statuses.includes(application.status)).length,
  }))
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

export function rejectApplication(id: number, data: PipelineRejectRequest) {
  return unwrapVoidResult(http.put<Result<null>>(`/applications/${id}/reject`, data))
}

export function generateApplicationAiMatch(id: number): Promise<AiMatchSummary> {
  return unwrapResult(http.post<Result<AiMatchSummary>>(`/applications/${id}/ai-match`))
}
