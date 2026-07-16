import { http, unwrapResult } from '@/api/http'
import type { Result } from '@/types/api'
import type {
  CandidateCreateRequest,
  CandidateDetail,
  CandidatePagedData,
  CandidatePageResponse,
  CandidateQuery,
} from '@/types/candidate'

export function adaptCandidatePage(
  source: CandidatePageResponse,
  page: number,
  pageSize: number,
): CandidatePagedData {
  return {
    items: source.records,
    page,
    pageSize,
    total: source.total,
  }
}

// 该聚合接口尚待后端实现；类型来自 Candidate、Resume、JobApplication 与 AI 匹配实体。
export async function getCandidates(query: CandidateQuery): Promise<CandidatePagedData> {
  const params = {
    pageNum: query.page,
    pageSize: query.pageSize,
    ...(query.keyword ? { keyword: query.keyword } : {}),
    ...(query.education ? { education: query.education } : {}),
    ...(query.school ? { school: query.school } : {}),
    ...(query.yearsOfExperienceMin !== null
      ? { yearsOfExperienceMin: query.yearsOfExperienceMin }
      : {}),
    ...(query.currentStatus ? { currentStatus: query.currentStatus } : {}),
  }
  const result = await unwrapResult(
    http.get<Result<CandidatePageResponse>>('/candidates', { params }),
  )
  return adaptCandidatePage(result, query.page, query.pageSize)
}

export function getCandidateById(id: number): Promise<CandidateDetail> {
  return unwrapResult(http.get<Result<CandidateDetail>>(`/candidates/${id}`))
}

export function createCandidate(data: CandidateCreateRequest): Promise<number> {
  return unwrapResult(http.post<Result<number>>('/candidates', data))
}
