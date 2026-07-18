import { getOfferStatusText, validateOfferForSend } from '@/config/offers'
import { initialDemoPipeline } from '@/config/demoPipeline'
import type { OfferCreateRequest, OfferQuery, OfferRecord, OfferUpdateRequest } from '@/types/offer'

export const initialDemoOffers: OfferRecord[] = [
  {
    id: 801,
    applicationId: 505,
    candidateId: 213,
    candidateName: '周雯',
    candidatePhone: '13900000213',
    candidateEmail: 'zhouwen@example.com',
    jobId: 104,
    jobTitle: '前端开发工程师',
    department: '技术部',
    interviewScore: 89,
    interviewSuggestion: 'PASS',
    salary: 18000,
    entryDate: '2026-08-01',
    probationMonths: 3,
    workLocation: '武汉',
    status: 'SENT',
    statusText: getOfferStatusText('SENT'),
    remark: '月薪包含固定薪资，其他福利以正式 Offer 附件为准。',
    sentAt: '2026-07-15T10:05:00',
    acceptedAt: null,
    createdByName: '李然',
    createdAt: '2026-07-15T09:30:00',
    updatedAt: '2026-07-15T10:05:00',
    timeline: [
      {
        id: '801-created',
        title: '创建 Offer 草稿',
        description: 'HR 根据面试结论创建录用方案。',
        actorName: '李然',
        occurredAt: '2026-07-15T09:30:00',
      },
      {
        id: '801-sent',
        title: '发送 Offer',
        description: 'Offer 已发送给候选人，等待候选人决定。',
        actorName: '李然',
        occurredAt: '2026-07-15T10:05:00',
      },
    ],
  },
  {
    id: 802,
    applicationId: 509,
    candidateId: 221,
    candidateName: '陈思悦',
    candidatePhone: '13900000104',
    candidateEmail: 'chensiyue@example.com',
    jobId: 103,
    jobTitle: 'UI/UX 设计师',
    department: '产品设计部',
    interviewScore: 81,
    interviewSuggestion: 'PASS',
    salary: 19000,
    entryDate: '2026-08-11',
    probationMonths: 3,
    workLocation: '上海',
    status: 'DRAFT',
    statusText: getOfferStatusText('DRAFT'),
    remark: '等待招聘负责人复核薪资和入职日期。',
    sentAt: null,
    acceptedAt: null,
    createdByName: '周倩',
    createdAt: '2026-07-15T16:40:00',
    updatedAt: '2026-07-15T16:40:00',
    timeline: [
      {
        id: '802-created',
        title: '创建 Offer 草稿',
        description: '草稿尚未发送，可继续编辑。',
        actorName: '周倩',
        occurredAt: '2026-07-15T16:40:00',
      },
    ],
  },
  {
    id: 803,
    applicationId: 501,
    candidateId: 211,
    candidateName: '林一凡',
    candidatePhone: '13900000211',
    candidateEmail: 'linyifan@example.com',
    jobId: 102,
    jobTitle: '招聘产品经理',
    department: '产品部',
    interviewScore: 86,
    interviewSuggestion: 'PASS',
    salary: 24000,
    entryDate: '2026-07-28',
    probationMonths: 3,
    workLocation: '上海',
    status: 'ACCEPTED',
    statusText: getOfferStatusText('ACCEPTED'),
    remark: '候选人已确认入职日期。',
    sentAt: '2026-07-12T11:20:00',
    acceptedAt: '2026-07-13T09:10:00',
    createdByName: '周倩',
    createdAt: '2026-07-12T10:40:00',
    updatedAt: '2026-07-13T09:10:00',
    timeline: [
      {
        id: '803-sent',
        title: '发送 Offer',
        description: 'HR 完成复核后发送。',
        actorName: '周倩',
        occurredAt: '2026-07-12T11:20:00',
      },
      {
        id: '803-accepted',
        title: '候选人接受 Offer',
        description: '候选人确认录用方案与预计入职日期。',
        actorName: '林一凡',
        occurredAt: '2026-07-13T09:10:00',
      },
    ],
  },
  {
    id: 804,
    applicationId: 510,
    candidateId: 224,
    candidateName: '许嘉言',
    candidatePhone: '13900000224',
    candidateEmail: 'xujiayan@example.com',
    jobId: 101,
    jobTitle: 'Java 后端开发工程师',
    department: '技术部',
    interviewScore: 83,
    interviewSuggestion: 'PASS',
    salary: 16000,
    entryDate: '2026-08-04',
    probationMonths: 3,
    workLocation: '武汉',
    status: 'REJECTED',
    statusText: getOfferStatusText('REJECTED'),
    remark: '候选人选择其他发展机会。',
    sentAt: '2026-07-10T15:30:00',
    acceptedAt: null,
    createdByName: '李然',
    createdAt: '2026-07-10T14:50:00',
    updatedAt: '2026-07-12T10:20:00',
    timeline: [
      {
        id: '804-rejected',
        title: '候选人拒绝 Offer',
        description: '候选人主动结束本次录用沟通。',
        actorName: '许嘉言',
        occurredAt: '2026-07-12T10:20:00',
      },
    ],
  },
]

export function getDemoOfferPage(records: OfferRecord[], query: OfferQuery) {
  const keyword = query.keyword.trim().toLocaleLowerCase()
  const filtered = records.filter((offer) => {
    const keywordFields = [offer.candidateName, offer.jobTitle, offer.department]
    return (
      (!keyword || keywordFields.some((value) => value.toLocaleLowerCase().includes(keyword))) &&
      (!query.status || offer.status === query.status)
    )
  })
  const start = (query.page - 1) * query.pageSize
  return {
    items: filtered.slice(start, start + query.pageSize),
    page: query.page,
    pageSize: query.pageSize,
    total: filtered.length,
  }
}

export function createDemoOffer(
  records: OfferRecord[],
  request: OfferCreateRequest,
  createdAt = new Date().toISOString(),
) {
  const id = Math.max(800, ...records.map((item) => item.id)) + 1
  const application = initialDemoPipeline.find(
    (item) => item.id === request.applicationId && item.status === 'INTERVIEWING',
  )
  const offer: OfferRecord = {
    id,
    applicationId: request.applicationId,
    candidateId: application?.candidateId ?? request.applicationId + 100,
    candidateName: application?.candidateName ?? '待接口补充候选人',
    candidatePhone: application?.candidatePhone ?? null,
    candidateEmail: application?.candidateEmail ?? null,
    jobId: application?.jobId ?? 0,
    jobTitle: application?.jobTitle ?? '待接口补充职位',
    department: application?.department ?? '待补充',
    interviewScore: application?.interview?.feedbackScore ?? null,
    interviewSuggestion: application?.interview?.feedbackSuggestion ?? null,
    salary: request.salary,
    entryDate: request.entryDate,
    probationMonths: request.probationMonths,
    workLocation: request.workLocation,
    status: 'DRAFT',
    statusText: getOfferStatusText('DRAFT'),
    remark: request.remark,
    sentAt: null,
    acceptedAt: null,
    createdByName: '当前 HR',
    createdAt,
    updatedAt: createdAt,
    timeline: [
      {
        id: `${id}-created`,
        title: '创建 Offer 草稿',
        description: 'HR 创建录用方案，尚未发送。',
        actorName: '当前 HR',
        occurredAt: createdAt,
      },
    ],
  }
  return offer
}

export function updateDemoOffer(
  offer: OfferRecord,
  request: OfferUpdateRequest,
  updatedAt = new Date().toISOString(),
) {
  if (offer.status !== 'DRAFT') throw new Error('只有草稿 Offer 可以编辑')
  return {
    ...offer,
    ...request,
    updatedAt,
  }
}

export function sendDemoOffer(offer: OfferRecord, sentAt = new Date().toISOString()) {
  if (offer.status !== 'DRAFT') throw new Error('只有草稿 Offer 可以发送')
  const validationError = validateOfferForSend(offer)
  if (validationError) throw new Error(validationError)
  return {
    ...offer,
    status: 'SENT' as const,
    statusText: getOfferStatusText('SENT'),
    sentAt,
    updatedAt: sentAt,
    timeline: [
      ...offer.timeline,
      {
        id: `${offer.id}-sent-${sentAt}`,
        title: '发送 Offer',
        description: 'Offer 已发送给候选人。',
        actorName: '当前 HR',
        occurredAt: sentAt,
      },
    ],
  }
}

export function revokeDemoOffer(offer: OfferRecord, revokedAt = new Date().toISOString()) {
  if (offer.status !== 'SENT') throw new Error('只有已发送 Offer 可以撤回')
  const reason = 'HR 已确认撤回该 Offer。'
  return {
    ...offer,
    status: 'REVOKED' as const,
    statusText: getOfferStatusText('REVOKED'),
    remark: reason,
    updatedAt: revokedAt,
    timeline: [
      ...offer.timeline,
      {
        id: `${offer.id}-revoked-${revokedAt}`,
        title: '撤回 Offer',
        description: reason,
        actorName: '当前 HR',
        occurredAt: revokedAt,
      },
    ],
  }
}
