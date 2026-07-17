import { http, unwrapResult, unwrapVoidResult } from '@/api/http'
import type { Result } from '@/types/api'
import type {
  CompleteOnboardingRequest,
  CancelOnboardingRequest,
  MaterialReviewRequest,
  OnboardingActionRequest,
  OnboardingPageResponse,
  OnboardingPagedData,
  OnboardingQuery,
  OnboardingRecord,
} from '@/types/onboarding'

interface BackendOnboardingRecord {
  id: number
  offerId: number
  applicationId?: number
  jobId?: number
  jobTitle: string
  department: string
  candidateId: number
  candidateName: string
  phone: string | null
  email: string | null
  entryDate: string
  status: OnboardingRecord['status']
  statusText: string
  currentStep: string
  materialStatus: OnboardingRecord['materialStatus']
  materialStatusText: string
  remark: string | null
  completedAt: string | null
  createdAt: string
  updatedAt?: string | null
}

interface BackendOnboardingPageResponse {
  total: number
  records: BackendOnboardingRecord[]
}

function buildOnboardingTimeline(record: BackendOnboardingRecord): OnboardingRecord['timeline'] {
  const events: OnboardingRecord['timeline'] = [
    {
      id: `${record.id}-created`,
      title: '创建入职流程',
      description: record.currentStep || '入职流程已创建。',
      actorName: '业务系统',
      occurredAt: record.createdAt,
    },
  ]
  if (record.remark) {
    events.push({
      id: `${record.id}-remark`,
      title: '办理备注',
      description: record.remark,
      actorName: '业务系统',
      occurredAt: record.updatedAt ?? record.createdAt,
    })
  }
  if (record.completedAt) {
    events.push({
      id: `${record.id}-completed`,
      title: '完成入职',
      description: '入职流程已完成并创建员工档案。',
      actorName: '业务系统',
      occurredAt: record.completedAt,
    })
  }
  return events
}

function adaptOnboarding(record: BackendOnboardingRecord): OnboardingRecord {
  return {
    id: record.id,
    offerId: record.offerId,
    candidateId: record.candidateId,
    candidateName: record.candidateName,
    candidatePhone: record.phone,
    candidateEmail: record.email,
    jobTitle: record.jobTitle,
    department: record.department,
    entryDate: record.entryDate,
    status: record.status,
    statusText: record.statusText,
    currentStep: record.currentStep,
    materialStatus: record.materialStatus,
    materialStatusText: record.materialStatusText,
    remark: record.remark,
    completedAt: record.completedAt,
    createdAt: record.createdAt,
    updatedAt: record.updatedAt ?? record.createdAt,
    timeline: buildOnboardingTimeline(record),
  }
}

export function adaptOnboardingPage(
  source: OnboardingPageResponse,
  page: number,
  pageSize: number,
): OnboardingPagedData {
  return { items: source.records, page, pageSize, total: source.total }
}

export async function getOnboardings(query: OnboardingQuery) {
  const params = {
    pageNum: query.page,
    pageSize: query.pageSize,
    ...(query.keyword ? { candidateKeyword: query.keyword } : {}),
    ...(query.status ? { status: query.status } : {}),
  }
  const result = await unwrapResult(
    http.get<Result<BackendOnboardingPageResponse>>('/onboarding', { params }),
  )
  return adaptOnboardingPage(
    {
      total: result.total,
      records: result.records.map(adaptOnboarding),
    },
    query.page,
    query.pageSize,
  )
}

export function getOnboardingById(id: number) {
  return unwrapResult(http.get<Result<BackendOnboardingRecord>>(`/onboarding/${id}`)).then(
    adaptOnboarding,
  )
}

export function startOnboardingReview(id: number, data: OnboardingActionRequest) {
  void id
  void data
  return Promise.reject(new Error('后端不提供开始审核接口；候选人提交材料后会自动进入审核中'))
}

export function reviewOnboardingMaterial(id: number, data: MaterialReviewRequest) {
  if (data.decision === 'APPROVE') {
    return unwrapVoidResult(http.put<Result<null>>(`/onboarding/${id}/approve-materials`))
  }
  return unwrapVoidResult(
    http.put<Result<null>>(`/onboarding/${id}/reject-materials`, { reason: data.note }),
  )
}

export function completeOnboarding(id: number, data: CompleteOnboardingRequest) {
  void data
  return unwrapVoidResult(http.put<Result<null>>(`/onboarding/${id}/complete`))
}

export function cancelOnboarding(id: number, data: CancelOnboardingRequest) {
  return unwrapVoidResult(http.put<Result<null>>(`/onboarding/${id}/cancel`, data))
}
