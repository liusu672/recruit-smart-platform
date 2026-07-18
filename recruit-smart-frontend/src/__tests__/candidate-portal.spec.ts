import { afterEach, describe, expect, it, vi } from 'vitest'

import { getMyApplications, getOpenJobs } from '@/api/candidatePortal'
import { http } from '@/api/http'

describe('candidate portal pagination adapters', () => {
  afterEach(() => vi.restoreAllMocks())

  it('sends pageNum/pageSize and maps public jobs to PagedData', async () => {
    const get = vi.spyOn(http, 'get').mockResolvedValue({
      data: { code: 200, message: 'success', data: { total: 12, records: [{ id: 7 }] } },
    } as never)

    await expect(getOpenJobs({ page: 2, pageSize: 5, keyword: 'java' })).resolves.toEqual({
      items: [{ id: 7 }],
      page: 2,
      pageSize: 5,
      total: 12,
    })
    expect(get).toHaveBeenCalledWith('/jobs/open', {
      params: { pageNum: 2, pageSize: 5, keyword: 'java' },
    })
  })

  it('keeps candidate application pages isolated to /applications/me', async () => {
    const get = vi.spyOn(http, 'get').mockResolvedValue({
      data: { code: 200, message: 'success', data: { total: 1, records: [] } },
    } as never)

    await getMyApplications({ page: 1, pageSize: 10 })
    expect(get).toHaveBeenCalledWith('/applications/me', {
      params: { pageNum: 1, pageSize: 10 },
    })
  })
})
