import { afterEach, describe, expect, it, vi } from 'vitest'

import {
  adaptJobPage,
  createJob,
  getJobApplications,
  getOpenJobById,
  parseSalaryRange,
  pauseJob,
  resumeJob,
} from '@/api/jobs'
import { http, unwrapVoidResult } from '@/api/http'
import { getDemoJobPage, initialDemoJobs } from '@/config/demoJobs'

describe('job API adaptation', () => {
  it('maps backend records pagination to the frontend page contract', () => {
    const page = adaptJobPage(
      {
        total: 1,
        records: [initialDemoJobs[0]!],
      },
      2,
      10,
    )

    expect(page).toEqual({
      items: [initialDemoJobs[0]],
      page: 2,
      pageSize: 10,
      total: 1,
    })
  })

  it('parses salary ranges without leaking invalid numbers into the form', () => {
    expect(parseSalaryRange('12000-18000')).toEqual([12000, 18000])
    expect(parseSalaryRange(null)).toEqual([0, 0])
    expect(parseSalaryRange('invalid')).toEqual([0, 0])
  })

  it('accepts successful null-data responses for state-changing endpoints', async () => {
    await expect(
      unwrapVoidResult(Promise.resolve({ data: { code: 200, message: 'success', data: null } })),
    ).resolves.toBeUndefined()
  })

  it('keeps business errors from null-data endpoints visible', async () => {
    await expect(
      unwrapVoidResult(
        Promise.resolve({ data: { code: 400, message: '只有草稿职位可以发布', data: null } }),
      ),
    ).rejects.toThrow('只有草稿职位可以发布')
  })
})

describe('job demo data', () => {
  it('uses the same filters and pagination shape as the real endpoint', () => {
    const page = getDemoJobPage(initialDemoJobs, {
      keyword: 'java',
      department: '技术部',
      status: 'OPEN',
      page: 1,
      pageSize: 10,
    })

    expect(page.total).toBe(1)
    expect(page.items[0]?.title).toBe('Java 后端开发工程师')
  })
})

describe('job state API', () => {
  afterEach(() => vi.restoreAllMocks())

  it('calls the backend pause and resume endpoints', async () => {
    const put = vi.spyOn(http, 'put').mockResolvedValue({
      data: { code: 200, message: 'success', data: null },
    })

    await pauseJob(101)
    await resumeJob(101)

    expect(put).toHaveBeenNthCalledWith(1, '/jobs/101/pause')
    expect(put).toHaveBeenNthCalledWith(2, '/jobs/101/resume')
  })

  it('loads public details and job-scoped applications with real pagination', async () => {
    const get = vi.spyOn(http, 'get').mockResolvedValue({
      data: { code: 200, message: 'success', data: { total: 0, records: [] } },
    } as never)
    await getOpenJobById(18)
    await getJobApplications(18, { page: 2, pageSize: 10, status: 'SCREENING' })
    expect(get).toHaveBeenNthCalledWith(1, '/jobs/open/18')
    expect(get).toHaveBeenNthCalledWith(2, '/jobs/18/applications', {
      params: { pageNum: 2, pageSize: 10, status: 'SCREENING' },
    })
  })

  it('keeps required interview rounds in create payloads', async () => {
    const post = vi.spyOn(http, 'post').mockResolvedValue({
      data: { code: 200, message: 'success', data: 19 },
    } as never)
    const request = {
      title: 'Java 工程师',
      department: '技术部',
      location: '武汉',
      jobType: '全职',
      headcount: 2,
      salaryRange: '12000-18000',
      salaryMin: 12000,
      salaryMax: 18000,
      experienceRequirement: '2 年以上',
      educationRequirement: '本科',
      description: '岗位描述',
      responsibilities: '岗位职责',
      requirements: '任职要求',
      requiredInterviewRounds: 2,
    }
    await createJob(request)
    expect(post).toHaveBeenCalledWith(
      '/jobs',
      expect.objectContaining({ requiredInterviewRounds: 2 }),
    )
  })
})
