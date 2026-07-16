import { http, unwrapResult, unwrapVoidResult } from '@/api/http'
import type { Result } from '@/types/api'
import type {
  OfferCreateRequest,
  OfferPageResponse,
  OfferPagedData,
  OfferQuery,
  OfferRecord,
  OfferRevokeRequest,
  OfferSendRequest,
  OfferUpdateRequest,
} from '@/types/offer'

export function adaptOfferPage(
  source: OfferPageResponse,
  page: number,
  pageSize: number,
): OfferPagedData {
  return { items: source.records, page, pageSize, total: source.total }
}

// 后端目前只有 Offer 实体与 Mapper；以下聚合和动作接口待业务服务实现后核对。
export async function getOffers(query: OfferQuery) {
  const params = {
    pageNum: query.page,
    pageSize: query.pageSize,
    ...(query.keyword ? { keyword: query.keyword } : {}),
    ...(query.status ? { status: query.status } : {}),
  }
  const result = await unwrapResult(http.get<Result<OfferPageResponse>>('/offers', { params }))
  return adaptOfferPage(result, query.page, query.pageSize)
}

export function getOfferById(id: number) {
  return unwrapResult(http.get<Result<OfferRecord>>(`/offers/${id}`))
}

export function createOffer(data: OfferCreateRequest) {
  return unwrapResult(http.post<Result<number>>('/offers', data))
}

export function updateOffer(id: number, data: OfferUpdateRequest) {
  return unwrapVoidResult(http.put<Result<null>>(`/offers/${id}`, data))
}

export function sendOffer(id: number, data: OfferSendRequest) {
  return unwrapVoidResult(http.post<Result<null>>(`/offers/${id}/send`, data))
}

export function revokeOffer(id: number, data: OfferRevokeRequest) {
  return unwrapVoidResult(http.post<Result<null>>(`/offers/${id}/revoke`, data))
}
