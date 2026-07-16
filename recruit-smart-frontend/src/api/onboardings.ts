import { http, unwrapResult, unwrapVoidResult } from '@/api/http'
import type { Result } from '@/types/api'
import type {
  CompleteOnboardingRequest,
  MaterialReviewRequest,
  OnboardingActionRequest,
  OnboardingPageResponse,
  OnboardingPagedData,
  OnboardingQuery,
  OnboardingRecord,
} from '@/types/onboarding'

export function adaptOnboardingPage(
  source: OnboardingPageResponse,
  page: number,
  pageSize: number,
): OnboardingPagedData {
  return { items: source.records, page, pageSize, total: source.total }
}

// 后端目前只有实体与 Mapper；聚合查询和状态动作均是待业务服务确认的临时契约。
export async function getOnboardings(query: OnboardingQuery) {
  const params = {
    pageNum: query.page,
    pageSize: query.pageSize,
    ...(query.keyword ? { keyword: query.keyword } : {}),
    ...(query.status ? { status: query.status } : {}),
  }
  const result = await unwrapResult(
    http.get<Result<OnboardingPageResponse>>('/onboardings', { params }),
  )
  return adaptOnboardingPage(result, query.page, query.pageSize)
}

export function getOnboardingById(id: number) {
  return unwrapResult(http.get<Result<OnboardingRecord>>(`/onboardings/${id}`))
}

export function startOnboardingReview(id: number, data: OnboardingActionRequest) {
  return unwrapVoidResult(http.post<Result<null>>(`/onboardings/${id}/review`, data))
}

export function reviewOnboardingMaterial(id: number, data: MaterialReviewRequest) {
  return unwrapVoidResult(http.post<Result<null>>(`/onboardings/${id}/material-review`, data))
}

export function completeOnboarding(id: number, data: CompleteOnboardingRequest) {
  return unwrapVoidResult(http.post<Result<null>>(`/onboardings/${id}/complete`, data))
}
