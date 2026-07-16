import { http, unwrapResult, unwrapVoidResult } from '@/api/http'
import type { Result } from '@/types/api'
import type {
  CandidateApplication,
  CandidateOffer,
  CandidateOnboarding,
  CandidateProfile,
  CandidateProfileUpdate,
  PageResponse,
  PortalInterview,
  ResumeSummary,
} from '@/types/portal'
import type { JobPosition } from '@/types/job'

const pageParams = { pageNum: 1, pageSize: 100 }

// 候选人端只封装公开职位与本人资源，禁止在此文件引入 HR 聚合列表接口。
export async function getOpenJobs(): Promise<JobPosition[]> {
  const page = await unwrapResult(
    http.get<Result<PageResponse<JobPosition>>>('/jobs/open', { params: pageParams }),
  )
  return page.records
}

export function getMyCandidateProfile() {
  return unwrapResult(http.get<Result<CandidateProfile>>('/candidate/me'))
}

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

export async function getMyApplications(): Promise<CandidateApplication[]> {
  const page = await unwrapResult(
    http.get<Result<PageResponse<CandidateApplication>>>('/applications/me', {
      params: pageParams,
    }),
  )
  return page.records
}

export function applyToJob(jobId: number, resumeId: number) {
  return unwrapResult(
    http.post<Result<number>>('/applications', { jobId, resumeId, allowAdjustment: false }),
  )
}

export function withdrawMyApplication(id: number) {
  return unwrapVoidResult(http.put<Result<null>>(`/applications/${id}/withdraw`))
}

export async function getMyInterviews(): Promise<PortalInterview[]> {
  const page = await unwrapResult(
    http.get<Result<PageResponse<PortalInterview>>>('/interviews/me', { params: pageParams }),
  )
  return page.records
}

export async function getMyOffers(): Promise<CandidateOffer[]> {
  const page = await unwrapResult(
    http.get<Result<PageResponse<CandidateOffer>>>('/offers/me', { params: pageParams }),
  )
  return page.records
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
