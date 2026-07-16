import { describe, expect, it } from 'vitest'

import { adaptOfferPage } from '@/api/offers'
import {
  getDemoOfferPage,
  initialDemoOffers,
  revokeDemoOffer,
  sendDemoOffer,
  updateDemoOffer,
} from '@/config/demoOffers'
import { validateOfferForSend } from '@/config/offers'

describe('offer API adaptation', () => {
  it('maps Offer records to the shared pagination contract', () => {
    const offer = initialDemoOffers[0]!
    const page = adaptOfferPage({ total: 1, records: [offer] }, 2, 10)

    expect(page).toEqual({ items: [offer], page: 2, pageSize: 10, total: 1 })
  })
})

describe('offer demo operations', () => {
  it('filters by keyword and exact status', () => {
    const page = getDemoOfferPage(initialDemoOffers, {
      keyword: '陈思悦',
      status: 'DRAFT',
      page: 1,
      pageSize: 10,
    })

    expect(page.total).toBe(1)
    expect(page.items[0]?.candidateName).toBe('陈思悦')
  })

  it('allows editing only while the Offer is a draft', () => {
    const draft = structuredClone(initialDemoOffers.find((item) => item.status === 'DRAFT')!)
    const sent = structuredClone(initialDemoOffers.find((item) => item.status === 'SENT')!)
    const request = {
      salary: 20500,
      entryDate: '2026-08-18',
      probationMonths: 3,
      workLocation: '上海',
      remark: '招聘负责人已复核。',
    }

    expect(updateDemoOffer(draft, request).salary).toBe(20500)
    expect(() => updateDemoOffer(sent, request)).toThrow('只有草稿 Offer 可以编辑')
  })

  it('requires salary, entry date and work location before sending', () => {
    const draft = structuredClone(initialDemoOffers.find((item) => item.status === 'DRAFT')!)
    draft.salary = 0

    expect(validateOfferForSend(draft)).toBe('请先填写有效月薪')
    expect(() => sendDemoOffer(draft, { note: '' })).toThrow('请先填写有效月薪')
  })

  it('moves a reviewed draft to sent and records the HR action', () => {
    const draft = structuredClone(initialDemoOffers.find((item) => item.status === 'DRAFT')!)
    const sent = sendDemoOffer(draft, { note: '薪资与入职日期已完成复核。' }, '2026-07-16T10:00:00')

    expect(sent).toMatchObject({ status: 'SENT', sentAt: '2026-07-16T10:00:00' })
    expect(sent.timeline.at(-1)?.title).toBe('发送 Offer')
  })

  it('revokes a sent Offer and records the HR action', () => {
    const sent = structuredClone(initialDemoOffers.find((item) => item.status === 'SENT')!)

    const revoked = revokeDemoOffer(
      sent,
      { reason: '录用方案调整，需重新审批。' },
      '2026-07-16T11:00:00',
    )
    expect(revoked).toMatchObject({ status: 'REVOKED', remark: '录用方案调整，需重新审批。' })
  })

  it.each(['ACCEPTED', 'REJECTED'] as const)(
    'does not let HR actions overwrite the candidate terminal state %s',
    (status) => {
      const offer = structuredClone(initialDemoOffers.find((item) => item.status === status)!)

      expect(() => sendDemoOffer(offer, { note: '尝试重新发送' })).toThrow(
        '只有草稿 Offer 可以发送',
      )
      expect(() => revokeDemoOffer(offer, { reason: '尝试撤回候选人终态' })).toThrow(
        '只有已发送 Offer 可以撤回',
      )
    },
  )
})
