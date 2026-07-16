import { describe, expect, it } from 'vitest'

import { adaptPipelinePage } from '@/api/pipeline'
import {
  applyDemoScreeningDecision,
  applyDemoStatusUpdate,
  getDemoPipelinePage,
  initialDemoPipeline,
} from '@/config/demoPipeline'
import { getPipelineStageKey } from '@/config/pipeline'

describe('pipeline API adaptation', () => {
  it('maps aggregation records to the frontend pagination contract', () => {
    const summary = getDemoPipelinePage(initialDemoPipeline, {
      keyword: '林一凡',
      jobId: 103,
      status: 'SCREENING',
      page: 1,
      pageSize: 20,
    }).items[0]!
    const page = adaptPipelinePage({ total: 1, records: [summary] }, 2, 20)

    expect(page).toEqual({
      items: [summary],
      page: 2,
      pageSize: 20,
      total: 1,
    })
  })
})

describe('pipeline stages and demo operations', () => {
  it('groups detailed application statuses into collaboration stages', () => {
    expect(getPipelineStageKey('SUBMITTED')).toBe('NEW')
    expect(getPipelineStageKey('SCREEN_PASSED')).toBe('INTERVIEW')
    expect(getPipelineStageKey('OFFERED')).toBe('OFFER')
    expect(getPipelineStageKey('SCREEN_REJECT')).toBe('CLOSED')
  })

  it('filters by keyword, job and exact status', () => {
    const page = getDemoPipelinePage(initialDemoPipeline, {
      keyword: '林一凡',
      jobId: 103,
      status: 'SCREENING',
      page: 1,
      pageSize: 20,
    })

    expect(page.total).toBe(1)
    expect(page.items[0]?.candidateName).toBe('林一凡')
  })

  it('starts screening only from a newly submitted application', () => {
    const submitted = initialDemoPipeline.find((item) => item.status === 'SUBMITTED')!
    const updated = applyDemoStatusUpdate(submitted, { status: 'SCREENING' }, '2026-07-15T12:00:00')

    expect(updated).toMatchObject({ status: 'SCREENING', ownerName: '当前 HR' })
    expect(updated.timeline.at(-1)?.title).toBe('进入 HR 筛选')
  })

  it('records the human screening decision and audit note', () => {
    const screening = initialDemoPipeline.find((item) => item.status === 'SCREENING')!
    const updated = applyDemoScreeningDecision(
      screening,
      { decision: 'PASS', rejectReasonCode: '', note: '作品集符合岗位重点' },
      '2026-07-15T12:10:00',
    )

    expect(updated).toMatchObject({
      status: 'SCREEN_PASSED',
      reviewDecision: 'PASS',
      hrNote: '作品集符合岗位重点',
    })
    expect(updated.timeline.at(-1)?.source).toBe('BUSINESS')
  })

  it('requires a business reason before rejecting a candidate', () => {
    const screening = initialDemoPipeline.find((item) => item.status === 'SCREENING')!

    expect(() =>
      applyDemoScreeningDecision(screening, {
        decision: 'REJECT',
        rejectReasonCode: '',
        note: '',
      }),
    ).toThrow('必须选择原因并填写说明')
  })
})
