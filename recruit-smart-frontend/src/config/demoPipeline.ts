import { getApplicationStatusText, getScreeningDecisionStatus } from '@/config/pipeline'
import { getInterviewRoundText, getInterviewStatusText } from '@/config/interviews'
import type {
  InterviewAssignmentRequest,
  InterviewUpdateRequest,
  InterviewerOption,
} from '@/types/interview'
import type {
  PipelineApplicationDetail,
  PipelineApplicationSummary,
  PipelineInterviewSummary,
  PipelineQuery,
  PipelineReviewRequest,
  PipelineStatusUpdateRequest,
} from '@/types/pipeline'

export const demoInterviewerOptions: InterviewerOption[] = [
  { id: 3, username: 'interviewer01', realName: '王面试官' },
  { id: 4, username: 'interviewer02', realName: '刘晓' },
  { id: 5, username: 'interviewer03', realName: '陈明' },
]

export const initialDemoPipeline: PipelineApplicationDetail[] = [
  {
    id: 501,
    candidateId: 211,
    candidateName: '林一凡',
    candidatePhone: '13900000211',
    candidateEmail: 'linyifan@example.com',
    education: '硕士',
    school: '浙江大学',
    yearsOfExperience: 5,
    resumeId: 601,
    resumeName: '林一凡-设计系统负责人简历',
    resumeFileType: 'PDF',
    jobId: 103,
    jobTitle: 'UI/UX 设计师',
    department: '产品设计部',
    status: 'SCREENING',
    statusText: '筛选中',
    source: 'ONLINE',
    sourceText: '官网投递',
    matchScore: 92,
    recommendLevel: 'HIGH',
    allowAdjustment: true,
    adjustedJobId: null,
    ownerId: 2,
    ownerName: '周倩',
    appliedAt: '2026-07-15T08:40:00',
    lastActivityAt: '2026-07-15T11:30:00',
    hrNote: '作品集完整，需要核实跨团队推动范围。',
    rejectReasonCode: null,
    rejectReason: null,
    reviewedAt: null,
    reviewDecision: 'PENDING',
    aiMatch: {
      matchScore: 92,
      recommendLevel: 'HIGH',
      recommendReason: '企业产品设计和设计系统建设经历与岗位要求高度相关。',
      highlightSummary: '主导过多产品线设计系统；具备复杂工作台项目经验。',
      riskSummary: '人员管理范围尚未从简历中得到充分验证。',
      modelName: 'demo-model',
      generatedAt: '2026-07-15T08:45:00',
    },
    interview: null,
    offer: null,
    timeline: [
      {
        id: '501-ai',
        title: '生成岗位匹配参考',
        description: '输出 92 分匹配参考与 1 项待核实风险。',
        actorName: 'demo-model',
        occurredAt: '2026-07-15T08:45:00',
        source: 'AI',
        relatedObject: 'UI/UX 设计师',
      },
      {
        id: '501-screening',
        title: '进入 HR 筛选',
        description: '周倩开始审核候选人资料。',
        actorName: '周倩',
        occurredAt: '2026-07-15T11:30:00',
        source: 'BUSINESS',
        relatedObject: '投递 #501',
      },
    ],
  },
  {
    id: 502,
    candidateId: 212,
    candidateName: '赵可欣',
    candidatePhone: '13900000212',
    candidateEmail: 'zhaokexin@example.com',
    education: '本科',
    school: '同济大学',
    yearsOfExperience: 3,
    resumeId: 602,
    resumeName: '赵可欣-AI 产品设计简历',
    resumeFileType: 'PDF',
    jobId: 103,
    jobTitle: 'UI/UX 设计师',
    department: '产品设计部',
    status: 'SUBMITTED',
    statusText: '已投递',
    source: 'ONLINE',
    sourceText: '官网投递',
    matchScore: 86,
    recommendLevel: 'HIGH',
    allowAdjustment: false,
    adjustedJobId: null,
    ownerId: null,
    ownerName: null,
    appliedAt: '2026-07-15T10:50:00',
    lastActivityAt: '2026-07-15T10:50:00',
    hrNote: null,
    rejectReasonCode: null,
    rejectReason: null,
    reviewedAt: null,
    reviewDecision: null,
    aiMatch: {
      matchScore: 86,
      recommendLevel: 'HIGH',
      recommendReason: '产品设计基础符合岗位要求，AI 产品经验具有相关性。',
      highlightSummary: '有企业 SaaS 项目经验；作品集结构完整。',
      riskSummary: '设计系统经验深度需要进一步确认。',
      modelName: 'demo-model',
      generatedAt: '2026-07-15T10:55:00',
    },
    interview: null,
    offer: null,
    timeline: [
      {
        id: '502-submit',
        title: '候选人提交投递',
        description: '使用“AI 产品设计简历”投递该职位。',
        actorName: '赵可欣',
        occurredAt: '2026-07-15T10:50:00',
        source: 'BUSINESS',
        relatedObject: 'UI/UX 设计师',
      },
    ],
  },
  {
    id: 503,
    candidateId: 201,
    candidateName: '张晨',
    candidatePhone: '13900000101',
    candidateEmail: 'zhangchen@example.com',
    education: '本科',
    school: '武汉理工大学',
    yearsOfExperience: 3,
    resumeId: 301,
    resumeName: '张晨-Java 后端简历',
    resumeFileType: 'PDF',
    jobId: 101,
    jobTitle: 'Java 后端开发工程师',
    department: '技术部',
    status: 'INTERVIEWING',
    statusText: '面试中',
    source: 'ONLINE',
    sourceText: '官网投递',
    matchScore: 88.5,
    recommendLevel: 'HIGH',
    allowAdjustment: true,
    adjustedJobId: null,
    ownerId: 2,
    ownerName: '周倩',
    appliedAt: '2026-07-12T09:20:00',
    lastActivityAt: '2026-07-15T09:20:00',
    hrNote: '技术栈匹配，已安排一面。',
    rejectReasonCode: null,
    rejectReason: null,
    reviewedAt: '2026-07-14T16:30:00',
    reviewDecision: 'PASS',
    aiMatch: {
      matchScore: 88.5,
      recommendLevel: 'HIGH',
      recommendReason: '技术栈与岗位要求匹配，相关业务项目经历较完整。',
      highlightSummary: '熟悉 Spring Boot 与 MySQL；具备招聘系统相关项目经历。',
      riskSummary: '复杂业务建模深度仍需面试核实。',
      modelName: 'demo-model',
      generatedAt: '2026-07-12T09:25:00',
    },
    interview: {
      id: 701,
      round: 'FIRST',
      roundText: '一面',
      interviewerId: 3,
      interviewerName: '陈明',
      interviewTime: '2026-07-16T14:00:00',
      method: 'ONLINE',
      methodText: '线上面试',
      location: '腾讯会议 123-456-789',
      status: 'SCHEDULED',
      statusText: '待面试',
      assignedAt: '2026-07-15T09:20:00',
      scheduledAt: '2026-07-15T10:00:00',
      feedbackScore: null,
      feedbackSuggestion: null,
    },
    offer: null,
    timeline: [
      {
        id: '503-pass',
        title: 'HR 确认初筛通过',
        description: '审核候选人简历与 AI 参考后，由 HR 决定推进。',
        actorName: '周倩',
        occurredAt: '2026-07-14T16:30:00',
        source: 'BUSINESS',
        relatedObject: '投递 #503',
      },
      {
        id: '503-interview',
        title: '创建一面安排',
        description: '面试官陈明，线上面试。',
        actorName: '周倩',
        occurredAt: '2026-07-15T09:20:00',
        source: 'BUSINESS',
        relatedObject: '面试 #701',
      },
    ],
  },
  {
    id: 504,
    candidateId: 204,
    candidateName: '陈思悦',
    candidatePhone: '13900000104',
    candidateEmail: 'chensiyue@example.com',
    education: '本科',
    school: '江南大学',
    yearsOfExperience: 6,
    resumeId: 305,
    resumeName: '陈思悦-产品设计作品集',
    resumeFileType: 'PDF',
    jobId: 103,
    jobTitle: 'UI/UX 设计师',
    department: '产品设计部',
    status: 'SCREEN_PASSED',
    statusText: '初筛通过',
    source: 'ONLINE',
    sourceText: '招聘网站',
    matchScore: 84,
    recommendLevel: 'HIGH',
    allowAdjustment: false,
    adjustedJobId: null,
    ownerId: 2,
    ownerName: '周倩',
    appliedAt: '2026-07-13T14:10:00',
    lastActivityAt: '2026-07-15T10:10:00',
    hrNote: '作品集完整，等待确认面试官时间。',
    rejectReasonCode: null,
    rejectReason: null,
    reviewedAt: '2026-07-15T10:10:00',
    reviewDecision: 'PASS',
    aiMatch: {
      matchScore: 84,
      recommendLevel: 'HIGH',
      recommendReason: '企业产品与设计系统经验符合岗位重点。',
      highlightSummary: '复杂工作台经验；设计系统建设经验。',
      riskSummary: '团队管理范围需要进一步核实。',
      modelName: 'demo-model',
      generatedAt: '2026-07-13T14:15:00',
    },
    interview: null,
    offer: null,
    timeline: [
      {
        id: '504-pass',
        title: 'HR 确认初筛通过',
        description: '下一步需要创建面试安排。',
        actorName: '周倩',
        occurredAt: '2026-07-15T10:10:00',
        source: 'BUSINESS',
        relatedObject: '投递 #504',
      },
    ],
  },
  {
    id: 505,
    candidateId: 213,
    candidateName: '周雯',
    candidatePhone: '13900000213',
    candidateEmail: 'zhouwen@example.com',
    education: '本科',
    school: '北京邮电大学',
    yearsOfExperience: 4,
    resumeId: 603,
    resumeName: '周雯-前端工程师简历',
    resumeFileType: 'PDF',
    jobId: 104,
    jobTitle: '前端开发工程师',
    department: '技术部',
    status: 'OFFERED',
    statusText: 'Offer 阶段',
    source: 'REFERRAL',
    sourceText: '员工推荐',
    matchScore: null,
    recommendLevel: null,
    allowAdjustment: true,
    adjustedJobId: null,
    ownerId: 3,
    ownerName: '李然',
    appliedAt: '2026-07-05T09:30:00',
    lastActivityAt: '2026-07-15T10:05:00',
    hrNote: 'Offer 已发送，等待候选人回复。',
    rejectReasonCode: null,
    rejectReason: null,
    reviewedAt: null,
    reviewDecision: 'PASS',
    aiMatch: null,
    interview: {
      id: 702,
      round: 'SECOND',
      roundText: '二面',
      interviewerId: 4,
      interviewerName: '刘晓',
      interviewTime: '2026-07-12T15:00:00',
      method: 'OFFLINE',
      methodText: '线下面试',
      location: '武汉研发中心 3A',
      status: 'COMPLETED',
      statusText: '已完成',
      assignedAt: '2026-07-10T09:20:00',
      scheduledAt: '2026-07-10T10:00:00',
      feedbackScore: 89,
      feedbackSuggestion: 'PASS',
    },
    offer: {
      id: 801,
      salary: 18000,
      entryDate: '2026-08-01',
      workLocation: '武汉',
      status: 'SENT',
      statusText: '已发送',
    },
    timeline: [
      {
        id: '505-offer',
        title: 'HR 发送 Offer',
        description: '候选人尚未接受或拒绝。',
        actorName: '李然',
        occurredAt: '2026-07-15T10:05:00',
        source: 'BUSINESS',
        relatedObject: 'Offer #801',
      },
    ],
  },
  {
    id: 506,
    candidateId: 214,
    candidateName: '魏然',
    candidatePhone: '13900000214',
    candidateEmail: 'weiran@example.com',
    education: '硕士',
    school: '华中科技大学',
    yearsOfExperience: 5,
    resumeId: 604,
    resumeName: '魏然-视觉交互设计简历',
    resumeFileType: 'PDF',
    jobId: 103,
    jobTitle: 'UI/UX 设计师',
    department: '产品设计部',
    status: 'HIRED',
    statusText: '已入职',
    source: 'REFERRAL',
    sourceText: '员工推荐',
    matchScore: null,
    recommendLevel: null,
    allowAdjustment: false,
    adjustedJobId: null,
    ownerId: 2,
    ownerName: '周倩',
    appliedAt: '2026-06-20T10:00:00',
    lastActivityAt: '2026-07-15T09:00:00',
    hrNote: '候选人已接受 Offer，入职材料审核中。',
    rejectReasonCode: null,
    rejectReason: null,
    reviewedAt: null,
    reviewDecision: 'PASS',
    aiMatch: null,
    interview: null,
    offer: {
      id: 802,
      salary: 19000,
      entryDate: '2026-07-19',
      workLocation: '上海',
      status: 'ACCEPTED',
      statusText: '已接受',
    },
    timeline: [
      {
        id: '506-accept',
        title: '候选人接受 Offer',
        description: '系统进入入职材料准备阶段。',
        actorName: '魏然',
        occurredAt: '2026-07-15T09:00:00',
        source: 'BUSINESS',
        relatedObject: 'Offer #802',
      },
    ],
  },
  {
    id: 507,
    candidateId: 203,
    candidateName: '王嘉禾',
    candidatePhone: '13900000103',
    candidateEmail: 'wangjiahe@example.com',
    education: '本科',
    school: '湖北大学',
    yearsOfExperience: 4,
    resumeId: 304,
    resumeName: '王嘉禾-HR 简历',
    resumeFileType: 'PDF',
    jobId: 101,
    jobTitle: 'Java 后端开发工程师',
    department: '技术部',
    status: 'SCREEN_REJECT',
    statusText: '初筛未通过',
    source: 'HR_IMPORT',
    sourceText: 'HR 录入',
    matchScore: 35,
    recommendLevel: 'LOW',
    allowAdjustment: false,
    adjustedJobId: null,
    ownerId: 2,
    ownerName: '周倩',
    appliedAt: '2026-07-14T16:40:00',
    lastActivityAt: '2026-07-14T17:20:00',
    hrNote: '与目标岗位技术方向不匹配。',
    rejectReasonCode: 'SKILL_NOT_MATCH',
    rejectReason: '缺少 Java、Spring Boot 和数据库开发经验。',
    reviewedAt: '2026-07-14T17:20:00',
    reviewDecision: 'REJECT',
    aiMatch: {
      matchScore: 35,
      recommendLevel: 'LOW',
      recommendReason: '主要经历集中在人力资源方向，与后端岗位要求差距较大。',
      highlightSummary: '招聘流程与沟通经验较完整。',
      riskSummary: '缺少 Java、Spring Boot 和数据库开发经验。',
      modelName: 'demo-model',
      generatedAt: '2026-07-14T16:45:00',
    },
    interview: null,
    offer: null,
    timeline: [
      {
        id: '507-reject',
        title: 'HR 结束投递流程',
        description: '拒绝原因：核心技能不匹配。',
        actorName: '周倩',
        occurredAt: '2026-07-14T17:20:00',
        source: 'BUSINESS',
        relatedObject: '投递 #507',
      },
    ],
  },
  {
    id: 508,
    candidateId: 215,
    candidateName: '顾知行',
    candidatePhone: '13900000215',
    candidateEmail: 'guzhixing@example.com',
    education: '本科',
    school: '南京理工大学',
    yearsOfExperience: 2,
    resumeId: 605,
    resumeName: '顾知行-Java 工程师简历',
    resumeFileType: 'PDF',
    jobId: 101,
    jobTitle: 'Java 后端开发工程师',
    department: '技术部',
    status: 'WITHDRAWN',
    statusText: '已撤回',
    source: 'ONLINE',
    sourceText: '官网投递',
    matchScore: null,
    recommendLevel: null,
    allowAdjustment: false,
    adjustedJobId: null,
    ownerId: null,
    ownerName: null,
    appliedAt: '2026-07-10T11:00:00',
    lastActivityAt: '2026-07-12T18:20:00',
    hrNote: null,
    rejectReasonCode: 'CANDIDATE_WITHDRAW',
    rejectReason: '候选人已接受其他公司的录用邀请。',
    reviewedAt: null,
    reviewDecision: null,
    aiMatch: null,
    interview: null,
    offer: null,
    timeline: [
      {
        id: '508-withdraw',
        title: '候选人撤回投递',
        description: '候选人主动结束本次投递。',
        actorName: '顾知行',
        occurredAt: '2026-07-12T18:20:00',
        source: 'BUSINESS',
        relatedObject: '投递 #508',
      },
    ],
  },
]

function toPipelineSummary(application: PipelineApplicationDetail): PipelineApplicationSummary {
  return {
    id: application.id,
    candidateId: application.candidateId,
    candidateName: application.candidateName,
    education: application.education,
    yearsOfExperience: application.yearsOfExperience,
    jobId: application.jobId,
    jobTitle: application.jobTitle,
    department: application.department,
    status: application.status,
    statusText: application.statusText,
    matchScore: application.matchScore,
    recommendLevel: application.recommendLevel,
    ownerId: application.ownerId,
    ownerName: application.ownerName,
    source: application.source,
    sourceText: application.sourceText,
    reviewDecision: application.reviewDecision,
    appliedAt: application.appliedAt,
    lastActivityAt: application.lastActivityAt,
  }
}

export function getDemoPipelinePage(records: PipelineApplicationDetail[], query: PipelineQuery) {
  const keyword = query.keyword.trim().toLocaleLowerCase()
  const filtered = records.filter((application) => {
    const keywordFields = [
      application.candidateName,
      application.candidatePhone ?? '',
      application.jobTitle,
    ]
    return (
      (!keyword || keywordFields.some((value) => value.toLocaleLowerCase().includes(keyword))) &&
      (query.jobId === null || application.jobId === query.jobId) &&
      (!query.status || application.status === query.status)
    )
  })
  const start = (query.page - 1) * query.pageSize

  return {
    items: filtered.slice(start, start + query.pageSize).map(toPipelineSummary),
    page: query.page,
    pageSize: query.pageSize,
    total: filtered.length,
  }
}

export function applyDemoStatusUpdate(
  application: PipelineApplicationDetail,
  request: PipelineStatusUpdateRequest,
  occurredAt = new Date().toISOString(),
) {
  if (application.status !== 'SUBMITTED' || request.status !== 'SCREENING') {
    throw new Error('当前状态不能开始筛选')
  }

  return {
    ...application,
    status: request.status,
    statusText: getApplicationStatusText(request.status),
    ownerName: '当前 HR',
    lastActivityAt: occurredAt,
    timeline: [
      ...application.timeline,
      {
        id: `${application.id}-screening-${occurredAt}`,
        title: '进入 HR 筛选',
        description: 'HR 开始审核候选人资料。',
        actorName: '当前 HR',
        occurredAt,
        source: 'BUSINESS' as const,
        relatedObject: `投递 #${application.id}`,
      },
    ],
  }
}

export function applyDemoScreeningDecision(
  application: PipelineApplicationDetail,
  request: PipelineReviewRequest,
  occurredAt = new Date().toISOString(),
) {
  if (application.status !== 'SCREENING') throw new Error('只有筛选中的投递可以提交筛选结论')
  if (request.decision === 'REJECT' && (!request.rejectReasonCode || !request.note.trim())) {
    throw new Error('拒绝候选人必须选择原因并填写说明')
  }
  if (request.decision === 'PENDING' && !request.note.trim()) {
    throw new Error('保留待定时必须填写待核实事项')
  }

  const nextStatus = getScreeningDecisionStatus(request.decision)
  const actionText =
    request.decision === 'PASS'
      ? 'HR 确认初筛通过'
      : request.decision === 'REJECT'
        ? 'HR 确认初筛未通过'
        : 'HR 保留候选人为待定'

  return {
    ...application,
    status: nextStatus,
    statusText: getApplicationStatusText(nextStatus),
    reviewDecision: request.decision,
    hrNote: request.note.trim() || application.hrNote,
    rejectReasonCode: request.decision === 'REJECT' ? request.rejectReasonCode : null,
    rejectReason: request.decision === 'REJECT' ? request.note.trim() : null,
    reviewedAt: occurredAt,
    lastActivityAt: occurredAt,
    timeline: [
      ...application.timeline,
      {
        id: `${application.id}-review-${occurredAt}`,
        title: actionText,
        description: request.note.trim() || 'HR 已完成本次筛选审核。',
        actorName: '当前 HR',
        occurredAt,
        source: 'BUSINESS' as const,
        relatedObject: `投递 #${application.id}`,
      },
    ],
  }
}

export function applyDemoInterviewAssignment(
  application: PipelineApplicationDetail,
  request: InterviewAssignmentRequest,
  occurredAt = new Date().toISOString(),
) {
  const interviewer = demoInterviewerOptions.find((item) => item.id === request.interviewerId)
  if (!interviewer) throw new Error('演示面试官不存在')
  if (application.status !== 'SCREEN_PASSED' && application.status !== 'INTERVIEWING') {
    throw new Error('只有初筛通过的投递可以安排面试')
  }
  if (application.interview && ['ASSIGNED', 'SCHEDULED'].includes(application.interview.status)) {
    throw new Error('该投递已经存在有效面试安排')
  }

  const interview: PipelineInterviewSummary = {
    id: application.id * 100 + 1,
    round: request.round,
    roundText: getInterviewRoundText(request.round),
    interviewerId: interviewer.id,
    interviewerName: interviewer.realName,
    interviewTime: null,
    method: null,
    methodText: '待确认',
    location: null,
    status: 'ASSIGNED',
    statusText: getInterviewStatusText('ASSIGNED'),
    assignedAt: occurredAt,
    scheduledAt: null,
    feedbackScore: null,
    feedbackSuggestion: null,
  }

  return {
    ...application,
    status: 'INTERVIEWING' as const,
    statusText: getApplicationStatusText('INTERVIEWING'),
    interview,
    lastActivityAt: occurredAt,
    timeline: [
      ...application.timeline,
      {
        id: `${application.id}-interview-assigned-${occurredAt}`,
        title: '指派面试官',
        description: `已指派${interviewer.realName}负责${interview.roundText}。`,
        actorName: '当前 HR',
        occurredAt,
        source: 'BUSINESS' as const,
        relatedObject: `面试 #${interview.id}`,
      },
    ],
  }
}

export function applyDemoInterviewReassignment(
  application: PipelineApplicationDetail,
  request: InterviewUpdateRequest,
  occurredAt = new Date().toISOString(),
) {
  const interview = application.interview
  const interviewer = demoInterviewerOptions.find((item) => item.id === request.interviewerId)
  if (!interview || interview.status !== 'ASSIGNED') {
    throw new Error('只有待预约面试可以重新指派')
  }
  if (!interviewer) throw new Error('演示面试官不存在')

  return {
    ...application,
    interview: {
      ...interview,
      interviewerId: interviewer.id,
      interviewerName: interviewer.realName,
      assignedAt: occurredAt,
    },
    lastActivityAt: occurredAt,
    timeline: [
      ...application.timeline,
      {
        id: `${application.id}-interview-reassigned-${occurredAt}`,
        title: '重新指派面试官',
        description: `已改为由${interviewer.realName}负责${interview.roundText}。`,
        actorName: '当前 HR',
        occurredAt,
        source: 'BUSINESS' as const,
        relatedObject: `面试 #${interview.id}`,
      },
    ],
  }
}

export function applyDemoInterviewCancellation(
  application: PipelineApplicationDetail,
  occurredAt = new Date().toISOString(),
) {
  const interview = application.interview
  if (!interview || !['ASSIGNED', 'SCHEDULED'].includes(interview.status)) {
    throw new Error('只有待预约或待面试状态可以取消')
  }

  return {
    ...application,
    interview: {
      ...interview,
      status: 'CANCELED' as const,
      statusText: getInterviewStatusText('CANCELED'),
    },
    lastActivityAt: occurredAt,
    timeline: [
      ...application.timeline,
      {
        id: `${application.id}-interview-canceled-${occurredAt}`,
        title: '取消面试',
        description: `已取消${interview.roundText}面试安排。`,
        actorName: '当前 HR',
        occurredAt,
        source: 'BUSINESS' as const,
        relatedObject: `面试 #${interview.id}`,
      },
    ],
  }
}
