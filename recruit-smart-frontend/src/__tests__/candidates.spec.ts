import { describe, expect, it, vi } from 'vitest'

import { adaptCandidatePage, updateCandidate } from '@/api/candidates'
import { http } from '@/api/http'
import {
  createDemoCandidate,
  getDemoCandidatePage,
  initialDemoCandidates,
} from '@/config/demoCandidates'
import { getApplicationStatusTone, getCandidateStatusTone } from '@/config/candidates'

describe('candidate API adaptation', () => {
  it('maps aggregation records to the frontend pagination contract', () => {
    const page = adaptCandidatePage(
      {
        total: 1,
        records: [initialDemoCandidates[0]!],
      },
      2,
      10,
    )

    expect(page).toEqual({
      items: [initialDemoCandidates[0]],
      page: 2,
      pageSize: 10,
      total: 1,
    })
  })
})

describe('candidate demo data', () => {
  it('uses the same combined filters as the pending aggregation endpoint', () => {
    const page = getDemoCandidatePage(initialDemoCandidates, {
      keyword: 'liruoxi',
      education: '硕士',
      school: '华中',
      yearsOfExperienceMin: 1,
      currentStatus: 'AVAILABLE',
      page: 1,
      pageSize: 10,
    })

    expect(page.total).toBe(1)
    expect(page.items[0]?.name).toBe('李若溪')
  })

  it('paginates filtered records without exposing detail-only collections', () => {
    const page = getDemoCandidatePage(initialDemoCandidates, {
      keyword: '',
      education: '',
      school: '',
      yearsOfExperienceMin: null,
      currentStatus: '',
      page: 2,
      pageSize: 2,
    })

    expect(page).toMatchObject({ page: 2, pageSize: 2, total: 4 })
    expect(page.items).toHaveLength(2)
    expect(page.items[0]).not.toHaveProperty('resumes')
    expect(page.items[0]).not.toHaveProperty('applications')
  })

  it('creates a normalized empty profile for HR-entered candidates', () => {
    const candidate = createDemoCandidate(205, {
      name: '周明远',
      gender: '男',
      phone: '13900000105',
      email: 'zhoumingyuan@example.com',
      education: '本科',
      school: '武汉大学',
      major: '计算机科学与技术',
      yearsOfExperience: 2,
      currentStatus: 'AVAILABLE',
      source: 'HR_IMPORT',
    })

    expect(candidate).toMatchObject({
      id: 205,
      name: '周明远',
      currentStatusText: '可应聘',
      sourceText: 'HR 录入',
      resumeCount: 0,
      resumes: [],
      applications: [],
    })
    expect(candidate.lastActivityAt).toEqual(expect.any(String))
  })
})

describe('candidate status tones', () => {
  it('keeps candidate and application state semantics consistent', () => {
    expect(getCandidateStatusTone('HIRED')).toBe('success')
    expect(getCandidateStatusTone('INTERVIEWING')).toBe('info')
    expect(getCandidateStatusTone('AVAILABLE')).toBe('neutral')
    expect(getApplicationStatusTone('OFFERED')).toBe('success')
    expect(getApplicationStatusTone('SCREEN_REJECT')).toBe('danger')
    expect(getApplicationStatusTone('SCREEN_PASSED')).toBe('info')
    expect(getApplicationStatusTone('SCREENING')).toBe('warning')
    expect(getApplicationStatusTone('WITHDRAWN')).toBe('neutral')
  })
})

describe('candidate update API', () => {
  it('keeps age and profile fields in the PUT payload', async () => {
    const put = vi.spyOn(http, 'put').mockResolvedValue({
      data: { code: 200, message: 'success', data: null },
    } as never)
    await updateCandidate(5, {
      name: '周明远',
      gender: '男',
      age: 26,
      phone: '13900000105',
      email: 'zhou@example.com',
      education: '本科',
      school: '武汉大学',
      major: '计算机科学与技术',
      yearsOfExperience: 2,
      source: 'HR_IMPORT',
    })
    expect(put).toHaveBeenCalledWith('/candidate/5', expect.objectContaining({ age: 26 }))
    vi.restoreAllMocks()
  })
})
