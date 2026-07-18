import { http, unwrapResult, unwrapVoidResult } from '@/api/http'
import type { Result } from '@/types/api'
import type {
  CandidateApplication,
  CandidateApplicationDetail,
  CandidateOffer,
  CandidateOnboarding,
  CandidateProfile,
  CandidateProfileUpdate,
  PageResponse,
  PortalPageQuery,
  PortalPagedData,
  PortalInterview,
  ResumeSummary,
} from '@/types/portal'
import type { JobPosition } from '@/types/job'
import { getOpenJobById } from '@/api/jobs'

const defaultPageQuery: PortalPageQuery = { page: 1, pageSize: 10 }

function adaptPage<T>(source: PageResponse<T>, query: PortalPageQuery): PortalPagedData<T> {
  return { items: source.records, page: query.page, pageSize: query.pageSize, total: source.total }
}

function toPageParams(query: PortalPageQuery) {
  return {
    pageNum: query.page,
    pageSize: query.pageSize,
    ...(query.keyword ? { keyword: query.keyword } : {}),
  }
}

// 候选人端只封装公开职位与本人资源，禁止在此文件引入 HR 聚合列表接口。
export async function getOpenJobs(
  query: PortalPageQuery = defaultPageQuery,
): Promise<PortalPagedData<JobPosition>> {
  const page = await unwrapResult(
    http.get<Result<PageResponse<JobPosition>>>('/jobs/open', { params: toPageParams(query) }),
  )
  return adaptPage(page, query)
}

export function getMyCandidateProfile() {
  return unwrapResult(http.get<Result<CandidateProfile>>('/candidate/me'))
}

export { getOpenJobById }

export function updateMyCandidateProfile(data: CandidateProfileUpdate) {
  return unwrapVoidResult(http.put<Result<null>>('/candidate/me', data))
}

export function getMyResumes() {
  return unwrapResult(http.get<Result<ResumeSummary[]>>('/resumes/me'))
}

export function uploadMyResume(file: File, resumeName: string) {
  const form = new FormData()
  form.append('file', file)
  form.append('resumeName', resumeName)
  form.append('setDefault', 'true')
  return unwrapResult(http.post<Result<number>>('/resumes', form))
}

export function setMyDefaultResume(id: number) {
  return unwrapVoidResult(http.put<Result<null>>(`/resumes/${id}/default`))
}

export async function getMyApplications(
  query: PortalPageQuery = defaultPageQuery,
): Promise<PortalPagedData<CandidateApplication>> {
  const page = await unwrapResult(
    http.get<Result<PageResponse<CandidateApplication>>>('/applications/me', {
      params: toPageParams(query),
    }),
  )
  return adaptPage(page, query)
}

export function getMyApplicationDetail(id: number): Promise<CandidateApplicationDetail> {
  return unwrapResult(http.get<Result<CandidateApplicationDetail>>(`/applications/${id}`))
}

export function applyToJob(jobId: number, resumeId: number) {
  return unwrapResult(
    http.post<Result<number>>('/applications', { jobId, resumeId, allowAdjustment: false }),
  )
}

export function withdrawMyApplication(id: number) {
  return unwrapVoidResult(http.put<Result<null>>(`/applications/${id}/withdraw`))
}

export async function getMyInterviews(
  query: PortalPageQuery = defaultPageQuery,
): Promise<PortalPagedData<PortalInterview>> {
  const page = await unwrapResult(
    http.get<Result<PageResponse<PortalInterview>>>('/interviews/me', {
      params: toPageParams(query),
    }),
  )
  return adaptPage(page, query)
}

export async function getMyOffers(
  query: PortalPageQuery = defaultPageQuery,
): Promise<PortalPagedData<CandidateOffer>> {
  const page = await unwrapResult(
    http.get<Result<PageResponse<CandidateOffer>>>('/offers/me', { params: toPageParams(query) }),
  )
  return adaptPage(page, query)
}

export function acceptMyOffer(id: number) {
  return unwrapVoidResult(http.put<Result<null>>(`/offers/${id}/accept`))
}

export function rejectMyOffer(id: number) {
  return unwrapVoidResult(http.put<Result<null>>(`/offers/${id}/reject`))
}

export function getMyOnboardings() {
  return unwrapResult(http.get<Result<CandidateOnboarding[]>>('/onboarding/me'))
}

export function submitMyOnboardingMaterials(id: number) {
  return unwrapVoidResult(http.put<Result<null>>(`/onboarding/${id}/submit-materials`))
}
