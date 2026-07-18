import { describe, expect, it, vi } from 'vitest'
import { adaptOnboardingPage, completeOnboarding } from '@/api/onboardings'
import { http } from '@/api/http'
import {
  completeDemoOnboarding,
  getDemoOnboardingPage,
  initialDemoOnboardings,
  reviewDemoOnboardingMaterial,
  startDemoOnboardingReview,
} from '@/config/demoOnboardings'

describe('onboarding API adaptation', () => {
  it('maps records to the shared pagination contract', () => {
    const record = initialDemoOnboardings[0]!
    expect(adaptOnboardingPage({ total: 1, records: [record] }, 2, 10)).toEqual({
      items: [record],
      page: 2,
      pageSize: 10,
      total: 1,
    })
  })
})

describe('onboarding action API contracts', () => {
  it('completes onboarding without a request body', async () => {
    const put = vi.spyOn(http, 'put').mockResolvedValue({
      data: { code: 200, message: 'success', data: null },
    } as never)
    await completeOnboarding(12)
    expect(put).toHaveBeenCalledWith('/onboarding/12/complete')
    vi.restoreAllMocks()
  })
})

describe('onboarding demo state machine', () => {
  it('filters by candidate keyword and exact status', () => {
    const page = getDemoOnboardingPage(initialDemoOnboardings, {
      keyword: '顾知行',
      status: 'REVIEWING',
      page: 1,
      pageSize: 10,
    })
    expect(page.total).toBe(1)
    expect(page.items[0]?.currentStep).toBe('材料审核')
  })

  it('starts review only from a pending record', () => {
    const pending = structuredClone(
      initialDemoOnboardings.find((item) => item.status === 'PENDING')!,
    )
    const reviewing = startDemoOnboardingReview(
      pending,
      { note: '材料已接收。' },
      '2026-07-16T10:00:00',
    )
    expect(reviewing).toMatchObject({ status: 'REVIEWING', materialStatus: 'REVIEWING' })
    expect(() => startDemoOnboardingReview(reviewing, { note: '重复操作' })).toThrow(
      '只有待提交记录可以开始审核',
    )
  })

  it('records approval and rejection as explicit HR decisions', () => {
    const source = structuredClone(
      initialDemoOnboardings.find((item) => item.status === 'REVIEWING')!,
    )
    const approved = reviewDemoOnboardingMaterial(source, {
      decision: 'APPROVE',
      note: '材料完整且可核验。',
    })
    const rejected = reviewDemoOnboardingMaterial(source, {
      decision: 'REJECT',
      note: '请补充离职证明。',
    })
    expect(approved).toMatchObject({ status: 'APPROVED', materialStatus: 'APPROVED' })
    expect(rejected).toMatchObject({ status: 'PENDING', materialStatus: 'REJECTED' })
  })

  it('creates an employee boundary only after material approval', () => {
    const approved = structuredClone(
      initialDemoOnboardings.find((item) => item.status === 'APPROVED')!,
    )
    const reviewing = structuredClone(
      initialDemoOnboardings.find((item) => item.status === 'REVIEWING')!,
    )
    expect(completeDemoOnboarding(approved, '2026-07-28T09:00:00')).toMatchObject({
      status: 'ONBOARDED',
      completedAt: '2026-07-28T09:00:00',
    })
    expect(() => completeDemoOnboarding(reviewing)).toThrow('只有材料已通过的记录可以确认入职')
  })
})
