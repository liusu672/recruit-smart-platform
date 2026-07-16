import type { PagedData } from '@/types/api'

export type OfferStatus = 'DRAFT' | 'SENT' | 'ACCEPTED' | 'REJECTED' | 'REVOKED'

export interface OfferTimelineEvent {
  id: string
  title: string
  description: string
  actorName: string
  occurredAt: string
}

export interface OfferRecord {
  id: number
  applicationId: number
  candidateId: number
  candidateName: string
  candidatePhone: string | null
  candidateEmail: string | null
  jobId: number
  jobTitle: string
  department: string
  interviewScore: number | null
  interviewSuggestion: string | null
  salary: number | null
  entryDate: string | null
  probationMonths: number
  workLocation: string | null
  status: OfferStatus
  statusText: string
  remark: string | null
  sentAt: string | null
  acceptedAt: string | null
  createdByName: string
  createdAt: string
  updatedAt: string
  timeline: OfferTimelineEvent[]
}

export interface OfferQuery {
  keyword: string
  status: OfferStatus | ''
  page: number
  pageSize: number
}

export interface OfferPageResponse {
  total: number
  records: OfferRecord[]
}

export interface OfferFormValue {
  applicationId: number | null
  candidateName: string
  jobTitle: string
  salary: number
  entryDate: string
  probationMonths: number
  workLocation: string
  remark: string
}

export interface OfferCandidateOption {
  applicationId: number
  candidateName: string
  jobTitle: string
  interviewScore: number
}

export interface OfferFormSubmitValue {
  applicationId: number
  salary: number
  entryDate: string
  probationMonths: number
  workLocation: string
  remark: string
}

export interface OfferCreateRequest {
  applicationId: number
  salary: number
  entryDate: string
  probationMonths: number
  workLocation: string
  remark: string
  createdBy: number
}

export type OfferUpdateRequest = Omit<OfferCreateRequest, 'applicationId' | 'createdBy'>

export interface OfferSendRequest {
  operatorId: number
  note: string
}

export interface OfferRevokeRequest {
  operatorId: number
  reason: string
}

export type OfferPagedData = PagedData<OfferRecord>
