import { afterEach, describe, expect, it, vi } from 'vitest'

import { http } from '@/api/http'
import { getPipelineApplications, getPipelineStageCounts } from '@/api/pipeline'
import { initialDemoPipeline } from '@/config/demoPipeline'

function mockUnfilteredPipelineEndpoint(records = initialDemoPipeline) {
  return vi.spyOn(http, 'get').mockImplementation(async (url, config) => {
    expect(url).toBe('/applications/pipeline')
    expect(config?.params).not.toHaveProperty('stage')
    expect(config?.params).not.toHaveProperty('status')

    const pageNum = Number(config?.params?.pageNum ?? 1)
    const pageSize = Number(config?.params?.pageSize ?? 100)
    const start = (pageNum - 1) * pageSize
    return {
      data: {
        code: 200,
        message: 'success',
        data: {
          total: records.length,
          records: records.slice(start, start + pageSize),
        },
      },
      status: 200,
      statusText: 'OK',
      headers: {},
      config: {} as never,
    }
  })
}

describe('pipeline stage compatibility', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('counts stages from the existing unfiltered pipeline endpoint', async () => {
    mockUnfilteredPipelineEndpoint()

    await expect(getPipelineStageCounts({ keyword: '', jobId: null })).resolves.toEqual([
      { stage: 'NEW', count: 1 },
      { stage: 'SCREENING', count: 1 },
      { stage: 'INTERVIEW', count: 2 },
      { stage: 'OFFER', count: 1 },
      { stage: 'HIRED', count: 1 },
      { stage: 'CLOSED', count: 2 },
    ])
  })

  it('counts every application when the unfiltered result spans multiple backend pages', async () => {
    const records = Array.from({ length: 124 }, (_, index) => ({
      ...initialDemoPipeline[index % initialDemoPipeline.length]!,
      id: index + 1,
    }))
    const request = mockUnfilteredPipelineEndpoint(records)

    const counts = await getPipelineStageCounts({ keyword: '', jobId: null })

    expect(counts.reduce((sum, item) => sum + item.count, 0)).toBe(124)
    expect(request).toHaveBeenCalledTimes(2)
    expect(request.mock.calls[1]?.[1]?.params).toMatchObject({ pageNum: 2, pageSize: 100 })
  })

  it('filters candidates by the selected stage even when the backend ignores stage parameters', async () => {
    mockUnfilteredPipelineEndpoint()

    const page = await getPipelineApplications({
      keyword: '',
      jobId: null,
      stage: 'INTERVIEW',
      status: '',
      page: 1,
      pageSize: 20,
    })

    expect(page.total).toBe(2)
    expect(page.items.map((item) => item.status).sort()).toEqual(['INTERVIEWING', 'SCREEN_PASSED'])
  })
})
