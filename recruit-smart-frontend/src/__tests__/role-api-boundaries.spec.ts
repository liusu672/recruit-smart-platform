import { AxiosHeaders, type AxiosResponse } from 'axios'
import { afterEach, describe, expect, it, vi } from 'vitest'

import {
  getMyApplications,
  getMyCandidateProfile,
  getMyInterviews,
  getMyOffers,
  getMyOnboardings,
  getMyResumes,
  getOpenJobs,
} from '@/api/candidatePortal'
import { http } from '@/api/http'
import { getMyInterviewerTasks } from '@/api/interviewerWorkspace'
import type { Result } from '@/types/api'

function successResponse<T>(data: T): AxiosResponse<Result<T>> {
  return {
    data: { code: 200, message: 'success', data },
    status: 200,
    statusText: 'OK',
    headers: {},
    config: { headers: new AxiosHeaders() },
  }
}

describe('role-specific API boundaries', () => {
  afterEach(() => vi.restoreAllMocks())

  it('uses only open or current-candidate list endpoints', async () => {
    const get = vi.spyOn(http, 'get')
    get.mockResolvedValueOnce(successResponse({ total: 0, records: [] }))
    get.mockResolvedValueOnce(successResponse({ id: 1 }))
    get.mockResolvedValueOnce(successResponse([]))
    get.mockResolvedValueOnce(successResponse({ total: 0, records: [] }))
    get.mockResolvedValueOnce(successResponse({ total: 0, records: [] }))
    get.mockResolvedValueOnce(successResponse({ total: 0, records: [] }))
    get.mockResolvedValueOnce(successResponse([]))

    await getOpenJobs()
    await getMyCandidateProfile()
    await getMyResumes()
    await getMyApplications()
    await getMyInterviews()
    await getMyOffers()
    await getMyOnboardings()

    expect(get.mock.calls.map(([url]) => url)).toEqual([
      '/jobs/open',
      '/candidate/me',
      '/resumes/me',
      '/applications/me',
      '/interviews/me',
      '/offers/me',
      '/onboarding/me',
    ])
  })

  it('loads interviewer tasks from the current-user endpoint', async () => {
    const get = vi.spyOn(http, 'get').mockResolvedValue(successResponse({ total: 0, records: [] }))

    await getMyInterviewerTasks()

    expect(get).toHaveBeenCalledWith('/interviews/interviewer/me', {
      params: { pageNum: 1, pageSize: 10 },
    })
  })
})
