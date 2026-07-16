export const onboardings = [
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

export function onboardingStatusText(status) {
  return (
    {
      PENDING: '待提交',
      REVIEWING: '审核中',
      APPROVED: '已通过',
      ONBOARDED: '已入职',
      CANCELED: '已取消',
    }[status] ?? status
  )
}

export function materialStatusText(status) {
  return (
    { PENDING: '待提交', REVIEWING: '审核中', APPROVED: '已通过', REJECTED: '需补充' }[status] ??
    status
  )
}
