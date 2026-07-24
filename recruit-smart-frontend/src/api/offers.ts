import { http, unwrapResult, unwrapVoidResult } from '@/api/http'
import type { Result } from '@/types/api'
import type {
  OfferCandidateOption,
  OfferCreateRequest,
  OfferPageResponse,
  OfferPagedData,
  OfferQuery,
  OfferRecord,
  OfferUpdateRequest,
} from '@/types/offer'

interface BackendOfferRecord {
  id: number
  applicationId: number
  applicationStatus?: string
  jobId: number
  jobTitle: string
  department: string
  candidateId: number
  candidateName: string
  phone?: string | null
  email?: string | null
  salary: number | null
  entryDate: string | null
  probationMonths?: number | null
  workLocation: string | null
  status: OfferRecord['status']
  statusText: string
  remark?: string | null
  sentAt: string | null
  acceptedAt: string | null
  createdAt: string
  updatedAt?: string | null
}

interface BackendOfferPageResponse {
  total: number
  records: BackendOfferRecord[]
}

interface BackendOfferCandidateOption {
  applicationId: number
  candidateId: number
  candidateName: string
  jobId: number
  jobTitle: string
  department: string
  interviewScore: number | null
  interviewSuggestion: string | null
}

function buildOfferTimeline(offer: BackendOfferRecord): OfferRecord['timeline'] {
  const events: OfferRecord['timeline'] = [
    {
      id: `${offer.id}-created`,
      title: '创建 Offer 草稿',
      description: offer.remark || 'HR 已创建 Offer 草稿。',
      actorName: '业务系统',
      occurredAt: offer.createdAt,
    },
  ]
  if (offer.sentAt) {
    events.push({
      id: `${offer.id}-sent`,
      title: '发送 Offer',
      description: 'Offer 已发送给候选人。',
      actorName: '业务系统',
      occurredAt: offer.sentAt,
    })
  }
  if (offer.acceptedAt) {
    events.push({
      id: `${offer.id}-accepted`,
      title: '候选人接受 Offer',
      description: '候选人已确认接受 Offer。',
      actorName: offer.candidateName,
      occurredAt: offer.acceptedAt,
    })
  }
  return events
}

function adaptOffer(offer: BackendOfferRecord): OfferRecord {
  return {
    id: offer.id,
    applicationId: offer.applicationId,
    candidateId: offer.candidateId,
    candidateName: offer.candidateName,
    candidatePhone: offer.phone ?? null,
    candidateEmail: offer.email ?? null,
    jobId: offer.jobId,
    jobTitle: offer.jobTitle,
    department: offer.department,
    interviewScore: null,
    interviewSuggestion: null,
    salary: offer.salary,
    entryDate: offer.entryDate,
    probationMonths: offer.probationMonths ?? 3,
    workLocation: offer.workLocation,
    status: offer.status,
    statusText: offer.statusText,
    remark: offer.remark ?? null,
    sentAt: offer.sentAt,
    acceptedAt: offer.acceptedAt,
    createdByName: '业务系统',
    createdAt: offer.createdAt,
    updatedAt: offer.updatedAt ?? offer.createdAt,
    timeline: buildOfferTimeline(offer),
  }
}

export function adaptOfferPage(
  source: OfferPageResponse,
  page: number,
  pageSize: number,
): OfferPagedData {
  return { items: source.records, page, pageSize, total: source.total }
}

export async function getOffers(query: OfferQuery) {
  const params = {
    pageNum: query.page,
    pageSize: query.pageSize,
    ...(query.keyword ? { candidateKeyword: query.keyword } : {}),
    ...(query.status ? { status: query.status } : {}),
  }
  const result = await unwrapResult(
    http.get<Result<BackendOfferPageResponse>>('/offers', { params }),
  )
  return adaptOfferPage(
    {
      total: result.total,
      records: result.records.map(adaptOffer),
    },
    query.page,
    query.pageSize,
  )
}

export function getOfferById(id: number) {
  return unwrapResult(http.get<Result<BackendOfferRecord>>(`/offers/${id}`)).then(adaptOffer)
}

export function getEligibleOfferApplications(): Promise<OfferCandidateOption[]> {
  return unwrapResult(
    http.get<Result<BackendOfferCandidateOption[]>>('/offers/eligible-applications'),
  )
}

export function createOffer(data: OfferCreateRequest) {
  const payload = {
    applicationId: data.applicationId,
    salary: data.salary,
    entryDate: data.entryDate,
    probationMonths: data.probationMonths,
    workLocation: data.workLocation,
    remark: data.remark,
  }
  return unwrapResult(http.post<Result<number>>('/offers', payload))
}

export function updateOffer(id: number, data: OfferUpdateRequest) {
  return unwrapVoidResult(http.put<Result<null>>(`/offers/${id}`, data))
}

export function sendOffer(id: number) {
  return unwrapVoidResult(http.put<Result<null>>(`/offers/${id}/send`))
}

export function revokeOffer(id: number) {
  return unwrapVoidResult(http.put<Result<null>>(`/offers/${id}/revoke`))
}
