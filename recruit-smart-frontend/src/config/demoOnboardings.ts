import { getMaterialStatusText, getOnboardingStatusText } from '@/config/onboardings'
import type {
  CancelOnboardingRequest,
  CompleteOnboardingRequest,
  MaterialReviewRequest,
  OnboardingActionRequest,
  OnboardingQuery,
  OnboardingRecord,
} from '@/types/onboarding'

export const initialDemoOnboardings: OnboardingRecord[] = [
  {
    id: 901,
    offerId: 803,
    candidateId: 211,
    candidateName: '林一凡',
    candidatePhone: '13900000211',
    candidateEmail: 'linyifan@example.com',
    jobTitle: '招聘产品经理',
    department: '产品部',
    entryDate: '2026-07-28',
    status: 'APPROVED',
    statusText: '已通过',
    currentStep: '待确认入职',
    materialStatus: 'APPROVED',
    materialStatusText: '已通过',
    remark: '入职材料已完成复核。',
    completedAt: null,
    createdAt: '2026-07-13T10:00:00',
    updatedAt: '2026-07-15T14:00:00',
    timeline: [
      {
        id: '901-approved',
        title: '材料审核通过',
        description: '身份证明与入职材料已由 HR 复核。',
        actorName: '周倩',
        occurredAt: '2026-07-15T14:00:00',
      },
    ],
  },
  {
    id: 902,
    offerId: 806,
    candidateId: 226,
    candidateName: '顾知行',
    candidatePhone: '13900000226',
    candidateEmail: 'guzhixing@example.com',
    jobTitle: 'Java 后端开发工程师',
    department: '技术部',
    entryDate: '2026-08-08',
    status: 'REVIEWING',
    statusText: '审核中',
    currentStep: '材料审核',
    materialStatus: 'REVIEWING',
    materialStatusText: '审核中',
    remark: '等待学历与离职证明复核。',
    completedAt: null,
    createdAt: '2026-07-15T09:00:00',
    updatedAt: '2026-07-16T08:30:00',
    timeline: [
      {
        id: '902-review',
        title: '开始材料审核',
        description: 'HR 已接收入职材料。',
        actorName: '李然',
        occurredAt: '2026-07-16T08:30:00',
      },
    ],
  },
  {
    id: 903,
    offerId: 807,
    candidateId: 228,
    candidateName: '赵可欣',
    candidatePhone: '13900000228',
    candidateEmail: 'zhaokexin@example.com',
    jobTitle: 'UI/UX 设计师',
    department: '产品设计部',
    entryDate: '2026-08-18',
    status: 'PENDING',
    statusText: '待提交',
    currentStep: '待提交材料',
    materialStatus: 'PENDING',
    materialStatusText: '待提交',
    remark: '候选人尚未完成材料提交。',
    completedAt: null,
    createdAt: '2026-07-16T09:10:00',
    updatedAt: '2026-07-16T09:10:00',
    timeline: [],
  },
  {
    id: 904,
    offerId: 1,
    candidateId: 1,
    candidateName: '张三',
    candidatePhone: '13900000001',
    candidateEmail: 'zhangsan@example.com',
    jobTitle: 'Java 后端开发工程师',
    department: '研发部',
    entryDate: '2026-07-15',
    status: 'ONBOARDED',
    statusText: '已入职',
    currentStep: '入职完成',
    materialStatus: 'APPROVED',
    materialStatusText: '已通过',
    remark: '已生成员工档案 EMP20260709001。',
    completedAt: '2026-07-15T09:00:00',
    createdAt: '2026-07-10T09:00:00',
    updatedAt: '2026-07-15T09:00:00',
    timeline: [
      {
        id: '904-complete',
        title: '确认入职并生成档案',
        description: '员工编号 EMP20260709001。',
        actorName: '招聘负责人',
        occurredAt: '2026-07-15T09:00:00',
      },
    ],
  },
]

export function getDemoOnboardingPage(records: OnboardingRecord[], query: OnboardingQuery) {
  const keyword = query.keyword.trim().toLocaleLowerCase()
  const filtered = records.filter(
    (record) =>
      (!keyword ||
        [record.candidateName, record.jobTitle, record.department].some((value) =>
          value.toLocaleLowerCase().includes(keyword),
        )) &&
      (!query.status || record.status === query.status),
  )
  const start = (query.page - 1) * query.pageSize
  return {
    items: filtered.slice(start, start + query.pageSize),
    page: query.page,
    pageSize: query.pageSize,
    total: filtered.length,
  }
}

export function startDemoOnboardingReview(
  record: OnboardingRecord,
  request: OnboardingActionRequest,
  now = new Date().toISOString(),
) {
  if (record.status !== 'PENDING') throw new Error('只有待提交记录可以开始审核')
  return {
    ...record,
    status: 'REVIEWING' as const,
    statusText: getOnboardingStatusText('REVIEWING'),
    currentStep: '材料审核',
    materialStatus: 'REVIEWING' as const,
    materialStatusText: getMaterialStatusText('REVIEWING'),
    remark: request.note,
    updatedAt: now,
    timeline: [
      ...record.timeline,
      {
        id: `${record.id}-review-${now}`,
        title: '开始材料审核',
        description: request.note,
        actorName: '当前 HR',
        occurredAt: now,
      },
    ],
  }
}

export function reviewDemoOnboardingMaterial(
  record: OnboardingRecord,
  request: MaterialReviewRequest,
  now = new Date().toISOString(),
) {
  if (record.status !== 'REVIEWING') throw new Error('只有审核中的记录可以提交材料结论')
  if (!request.note.trim()) throw new Error('材料审核必须填写说明')
  const approved = request.decision === 'APPROVE'
  return {
    ...record,
    status: (approved ? 'APPROVED' : 'PENDING') as OnboardingRecord['status'],
    statusText: getOnboardingStatusText(approved ? 'APPROVED' : 'PENDING'),
    currentStep: approved ? '待确认入职' : '待补充材料',
    materialStatus: (approved ? 'APPROVED' : 'REJECTED') as OnboardingRecord['materialStatus'],
    materialStatusText: getMaterialStatusText(approved ? 'APPROVED' : 'REJECTED'),
    remark: request.note.trim(),
    updatedAt: now,
    timeline: [
      ...record.timeline,
      {
        id: `${record.id}-material-${now}`,
        title: approved ? '材料审核通过' : '材料退回补充',
        description: request.note.trim(),
        actorName: '当前 HR',
        occurredAt: now,
      },
    ],
  }
}

export function completeDemoOnboarding(
  record: OnboardingRecord,
  request: CompleteOnboardingRequest,
  now = new Date().toISOString(),
) {
  if (record.status !== 'APPROVED' || record.materialStatus !== 'APPROVED')
    throw new Error('只有材料已通过的记录可以确认入职')
  const employeeNo = `EMP${now.slice(0, 10).replaceAll('-', '')}${String(
    record.candidateId,
  ).padStart(3, '0')}`
  const note = request.note?.trim() || '已确认到岗。'
  return {
    ...record,
    status: 'ONBOARDED' as const,
    statusText: getOnboardingStatusText('ONBOARDED'),
    currentStep: '入职完成',
    remark: note,
    completedAt: now,
    updatedAt: now,
    timeline: [
      ...record.timeline,
      {
        id: `${record.id}-complete-${now}`,
        title: '确认入职并生成档案',
        description: `员工编号 ${employeeNo}。${note}`,
        actorName: '当前 HR',
        occurredAt: now,
      },
    ],
  }
}

export function cancelDemoOnboarding(
  record: OnboardingRecord,
  request: CancelOnboardingRequest,
  now = new Date().toISOString(),
) {
  if (record.status === 'ONBOARDED' || record.status === 'CANCELED')
    throw new Error('已入职或已取消的流程不能再次取消')
  const reason = request.reason.trim()
  if (!reason) throw new Error('取消入职流程必须填写原因')
  return {
    ...record,
    status: 'CANCELED' as const,
    statusText: getOnboardingStatusText('CANCELED'),
    currentStep: '流程已取消',
    remark: reason,
    updatedAt: now,
    timeline: [
      ...record.timeline,
      {
        id: `${record.id}-cancel-${now}`,
        title: '取消入职流程',
        description: reason,
        actorName: '当前 HR',
        occurredAt: now,
      },
    ],
  }
}
