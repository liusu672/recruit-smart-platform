import {
  defaultInterviewScorecard,
  getFeedbackStateText,
  getInterviewMethodText,
  getInterviewRoundText,
  getInterviewStatusText,
} from '@/config/interviews'
import type {
  InterviewFeedbackRequest,
  InterviewTaskQuery,
  InterviewScheduleRequest,
  InterviewWorkspace,
} from '@/types/interview'

function createScorecard(scores: Array<number | null>, evidence: string[]) {
  return defaultInterviewScorecard.map((item, index) => ({
    ...item,
    score: scores[index] ?? null,
    evidence: evidence[index] ?? '',
  }))
}

export const initialDemoInterviews: InterviewWorkspace[] = [
  {
    id: 701,
    applicationId: 503,
    candidateId: 201,
    candidateName: '张晨',
    jobTitle: 'Java 后端开发工程师',
    department: '技术部',
    round: 'FIRST',
    roundText: getInterviewRoundText('FIRST'),
    interviewTime: '2026-07-16T14:00:00',
    method: 'ONLINE',
    methodText: getInterviewMethodText('ONLINE'),
    location: '腾讯会议 123-456-789',
    status: 'SCHEDULED',
    statusText: getInterviewStatusText('SCHEDULED'),
    interviewerId: 3,
    interviewerName: '王面试官',
    assignedAt: '2026-07-15T09:20:00',
    scheduledAt: '2026-07-15T10:00:00',
    feedbackState: 'DRAFT',
    feedbackStateText: getFeedbackStateText('DRAFT'),
    candidateBrief: {
      education: '本科',
      school: '武汉理工大学',
      yearsOfExperience: 3,
      resumeName: '张晨-Java 后端简历',
      skills: ['Java', 'Spring Boot', 'MySQL', 'Redis'],
      workExperience: '3 年企业应用后端开发经验。',
      projectExperience: '负责职位、投递和面试模块接口与数据模型设计。',
      matchScore: 88.5,
      matchSummary: '技术栈与岗位要求匹配，相关业务项目经历较完整。',
      riskPoints: ['复杂业务建模深度仍需面试核实'],
    },
    scorecard: createScorecard(
      [3, 3, null, null],
      ['能说明接口幂等与状态流转。', '按领域拆分问题，思路较清晰。'],
    ),
    questions: [
      {
        id: '701-job-1',
        category: '专业能力',
        question: '请说明你如何设计投递、面试与 Offer 之间的状态流转和幂等控制？',
        source: 'JOB',
      },
      {
        id: '701-resume-1',
        category: '项目深挖',
        question: '在招聘管理项目中，你负责的模块遇到过哪些一致性问题，最终如何解决？',
        source: 'RESUME',
      },
      {
        id: '701-risk-1',
        category: '风险追问',
        question: '如果业务状态写入成功但消息发送失败，你会怎样恢复并避免重复处理？',
        source: 'RISK',
      },
    ],
    feedback: {
      id: null,
      interviewId: 701,
      interviewerId: 3,
      score: null,
      comment: '',
      suggestion: null,
      aiSummary: null,
      state: 'DRAFT',
      submittedAt: null,
    },
  },
  {
    id: 702,
    applicationId: 505,
    candidateId: 213,
    candidateName: '周雯',
    jobTitle: '前端开发工程师',
    department: '技术部',
    round: 'SECOND',
    roundText: getInterviewRoundText('SECOND'),
    interviewTime: '2026-07-15T10:30:00',
    method: 'OFFLINE',
    methodText: getInterviewMethodText('OFFLINE'),
    location: '武汉研发中心 3A',
    status: 'REINTERVIEW',
    statusText: getInterviewStatusText('REINTERVIEW'),
    interviewerId: 3,
    interviewerName: '王面试官',
    assignedAt: '2026-07-14T09:20:00',
    scheduledAt: '2026-07-14T10:00:00',
    feedbackState: 'EMPTY',
    feedbackStateText: getFeedbackStateText('EMPTY'),
    candidateBrief: {
      education: '本科',
      school: '北京邮电大学',
      yearsOfExperience: 4,
      resumeName: '周雯-前端工程师简历',
      skills: ['Vue', 'TypeScript', '性能优化', '设计系统'],
      workExperience: '4 年企业前端开发经验。',
      projectExperience: '负责复杂工作台、权限体系和组件库建设。',
      matchScore: 86,
      matchSummary: '企业工作台经验与岗位匹配，工程深度需要终面确认。',
      riskPoints: ['跨团队技术决策的推动方式需要进一步确认'],
    },
    scorecard: createScorecard([null, null, null, null], []),
    questions: [
      {
        id: '702-job-1',
        category: '架构判断',
        question: '你会如何划分大型 Vue 应用的领域边界，并避免全局状态持续膨胀？',
        source: 'JOB',
      },
      {
        id: '702-risk-1',
        category: '协作验证',
        question: '遇到设计规范与交付期限冲突时，你怎样推动团队做出可维护的取舍？',
        source: 'RISK',
      },
    ],
    feedback: {
      id: null,
      interviewId: 702,
      interviewerId: 3,
      score: null,
      comment: '',
      suggestion: null,
      aiSummary: null,
      state: 'EMPTY',
      submittedAt: null,
    },
  },
  {
    id: 703,
    applicationId: 509,
    candidateId: 221,
    candidateName: '陈思悦',
    jobTitle: 'UI/UX 设计师',
    department: '产品设计部',
    round: 'FIRST',
    roundText: getInterviewRoundText('FIRST'),
    interviewTime: '2026-07-14T15:00:00',
    method: 'ONLINE',
    methodText: getInterviewMethodText('ONLINE'),
    location: '飞书会议 482 731 002',
    status: 'COMPLETED',
    statusText: getInterviewStatusText('COMPLETED'),
    interviewerId: 3,
    interviewerName: '王面试官',
    assignedAt: '2026-07-13T09:20:00',
    scheduledAt: '2026-07-13T10:00:00',
    feedbackState: 'SUBMITTED',
    feedbackStateText: getFeedbackStateText('SUBMITTED'),
    candidateBrief: {
      education: '本科',
      school: '江南大学',
      yearsOfExperience: 6,
      resumeName: '陈思悦-产品设计作品集',
      skills: ['企业产品设计', '设计系统', '用户研究', 'Figma'],
      workExperience: '6 年产品与交互设计经验。',
      projectExperience: '主导企业招聘与协作产品的设计系统建设。',
      matchScore: 84,
      matchSummary: '企业产品与设计系统经验符合岗位重点。',
      riskPoints: ['团队管理范围需要进一步核实'],
    },
    scorecard: createScorecard(
      [4, 3, 3, 3],
      [
        '能用真实案例说明设计系统治理。',
        '问题拆解完整，但量化依据可更清晰。',
        '跨团队推进方式具体。',
        '能复盘失败方案和后续改进。',
      ],
    ),
    questions: [
      {
        id: '703-job-1',
        category: '专业能力',
        question: '请用一个案例说明你如何把复杂业务规则转化为清晰的操作路径。',
        source: 'JOB',
      },
    ],
    feedback: {
      id: 901,
      interviewId: 703,
      interviewerId: 3,
      score: 81,
      comment:
        '候选人企业产品经验完整，设计系统治理与跨团队协作案例可信。量化验证方法仍可继续追问。',
      suggestion: 'PASS',
      aiSummary: '候选人在企业产品和设计系统方面表现稳定，协作案例完整；建议后续关注量化验证能力。',
      state: 'SUBMITTED',
      submittedAt: '2026-07-14T16:10:00',
    },
  },
]

export function getDemoInterviewPage(records: InterviewWorkspace[], query: InterviewTaskQuery) {
  const keyword = query.keyword.trim().toLocaleLowerCase()
  const filtered = records.filter((interview) => {
    const keywordFields = [interview.candidateName, interview.jobTitle, interview.interviewerName]
    return (
      (!keyword || keywordFields.some((value) => value.toLocaleLowerCase().includes(keyword))) &&
      (!query.status || interview.status === query.status) &&
      (!query.feedbackState || interview.feedbackState === query.feedbackState)
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

export function applyDemoInterviewSchedule(
  interview: InterviewWorkspace,
  request: InterviewScheduleRequest,
  scheduledAt = new Date().toISOString(),
) {
  if (interview.status !== 'ASSIGNED' && interview.status !== 'SCHEDULED') {
    throw new Error('当前状态不能预约面试')
  }

  return {
    ...interview,
    interviewTime: request.interviewTime,
    method: request.method,
    methodText: getInterviewMethodText(request.method),
    location: request.location.trim(),
    status: 'SCHEDULED' as const,
    statusText: getInterviewStatusText('SCHEDULED'),
    scheduledAt,
  }
}

export function applyDemoInterviewCompletion(interview: InterviewWorkspace) {
  if (interview.status !== 'SCHEDULED') {
    throw new Error('只有待面试状态可以标记为完成')
  }

  return {
    ...interview,
    status: 'COMPLETED' as const,
    statusText: getInterviewStatusText('COMPLETED'),
  }
}

export function applyDemoInterviewDraft(
  interview: InterviewWorkspace,
  request: InterviewFeedbackRequest,
) {
  if (interview.status !== 'SCHEDULED' && interview.status !== 'COMPLETED') {
    throw new Error('只有已确认预约或已完成的面试可以保存反馈草稿')
  }
  if (interview.feedbackState === 'SUBMITTED') throw new Error('已提交的反馈不能覆盖')
  return {
    ...interview,
    scorecard: structuredClone(request.scorecard),
    feedbackState: 'DRAFT' as const,
    feedbackStateText: getFeedbackStateText('DRAFT'),
    feedback: {
      ...interview.feedback,
      score: request.score,
      comment: request.comment.trim(),
      suggestion: request.suggestion,
      state: 'DRAFT' as const,
    },
  }
}

export function applyDemoInterviewSubmit(
  interview: InterviewWorkspace,
  request: InterviewFeedbackRequest,
  submittedAt = new Date().toISOString(),
) {
  if (interview.status !== 'COMPLETED') {
    throw new Error('只有已完成的面试可以提交反馈')
  }
  if (interview.feedbackState === 'SUBMITTED') throw new Error('该面试反馈已经提交')
  if (request.scorecard.some((item) => item.score === null || !item.evidence.trim())) {
    throw new Error('请完成所有评分并填写评价证据')
  }
  if (request.score === null || !request.comment.trim() || !request.suggestion) {
    throw new Error('请填写综合评价和录用建议')
  }

  return {
    ...interview,
    status: 'COMPLETED' as const,
    statusText: getInterviewStatusText('COMPLETED'),
    scorecard: structuredClone(request.scorecard),
    feedbackState: 'SUBMITTED' as const,
    feedbackStateText: getFeedbackStateText('SUBMITTED'),
    feedback: {
      ...interview.feedback,
      id: interview.feedback.id ?? interview.id + 200,
      score: request.score,
      comment: request.comment.trim(),
      suggestion: request.suggestion,
      state: 'SUBMITTED' as const,
      submittedAt,
    },
  }
}
