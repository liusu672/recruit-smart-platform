import { Buffer } from 'node:buffer'
import { createServer } from 'node:http'
import { stdout } from 'node:process'
import { URL } from 'node:url'

import { interviewTasks } from './mock-interview-data.mjs'
import { employees } from './mock-employee-data.mjs'
import { materialStatusText, onboardings, onboardingStatusText } from './mock-onboarding-data.mjs'
import { offers, offerStatusText } from './mock-offer-data.mjs'
import { getPipelineStatusText, pipelineApplications } from './mock-pipeline-data.mjs'

const port = 8080
let nextJobId = 104
let nextCandidateId = 204
let nextOfferId = 805
const jobs = [
  {
    id: 101,
    title: 'Java 后端开发工程师',
    department: '技术部',
    location: '武汉',
    salaryRange: '12000-18000',
    headcount: 3,
    description: '负责招聘平台业务服务、接口设计和核心流程实现。',
    requirement: '熟悉 Java、Spring Boot、MySQL 和常见微服务组件。',
    status: 'OPEN',
    statusText: '招聘中',
    createdAt: '2026-07-08T09:30:00',
    updatedAt: '2026-07-14T16:20:00',
  },
  {
    id: 102,
    title: '招聘产品经理',
    department: '产品部',
    location: '上海',
    salaryRange: '18000-26000',
    headcount: 1,
    description: '负责招聘主流程产品规划、需求分析和跨团队交付。',
    requirement: '具备企业软件或招聘产品经验，能够推动复杂业务落地。',
    status: 'DRAFT',
    statusText: '草稿',
    createdAt: '2026-07-12T11:10:00',
    updatedAt: '2026-07-12T11:10:00',
  },
  {
    id: 103,
    title: 'UI/UX 设计师',
    department: '设计部',
    location: '深圳',
    salaryRange: '15000-22000',
    headcount: 2,
    description: '负责招聘工作台的交互设计、视觉规范和可用性验证。',
    requirement: '熟悉企业级产品设计，重视信息密度、无障碍与设计系统。',
    status: 'CLOSED',
    statusText: '已关闭',
    createdAt: '2026-06-20T14:00:00',
    updatedAt: '2026-07-10T18:00:00',
  },
]

const candidates = [
  {
    id: 201,
    userId: 4,
    name: '张晨',
    gender: '男',
    phone: '13900000101',
    email: 'zhangchen@example.com',
    education: '本科',
    school: '武汉理工大学',
    major: '软件工程',
    yearsOfExperience: 3,
    currentStatus: 'INTERVIEWING',
    currentStatusText: '面试中',
    source: 'SELF_REGISTER',
    sourceText: '候选人注册',
    resumeCount: 1,
    latestApplicationStatus: 'INTERVIEWING',
    latestApplicationStatusText: '面试中',
    latestJobTitle: 'Java 后端开发工程师',
    latestMatchScore: 88.5,
    lastActivityAt: '2026-07-15T10:20:00',
    duplicateRisk: false,
    resumes: [
      {
        id: 301,
        resumeName: '张晨-Java 后端简历',
        fileUrl: '/files/resume/zhangchen-java.pdf',
        fileType: 'PDF',
        parsedContent: '3 年 Java 后端开发经验，参与过招聘管理系统核心模块建设。',
        skills: ['Java', 'Spring Boot', 'MySQL', 'Redis'],
        projectExperience: '负责职位、投递和面试模块接口与数据模型设计。',
        workExperience: '3 年企业应用后端开发经验。',
        isDefault: true,
        updatedAt: '2026-07-14T18:10:00',
      },
    ],
    applications: [
      {
        id: 401,
        jobId: 101,
        jobTitle: 'Java 后端开发工程师',
        resumeId: 301,
        status: 'INTERVIEWING',
        statusText: '面试中',
        source: 'ONLINE',
        allowAdjustment: true,
        hrNote: '技术栈匹配，已安排一面。',
        appliedAt: '2026-07-12T09:20:00',
        aiMatch: {
          score: 88.5,
          level: 'HIGH',
          summary: '技术栈与岗位要求匹配，相关业务项目经历较完整。',
          highlights: ['熟悉 Spring Boot 与 MySQL', '具备招聘系统相关项目经历'],
          risks: ['复杂业务建模深度仍需面试核实'],
          suggestion: '建议结合项目细节和系统设计题进一步验证。',
          modelName: 'demo-model',
          generatedAt: '2026-07-12T09:25:00',
        },
      },
    ],
  },
  {
    id: 202,
    userId: null,
    name: '李若溪',
    gender: '女',
    phone: '13900000102',
    email: 'liruoxi@example.com',
    education: '硕士',
    school: '华中科技大学',
    major: '计算机科学与技术',
    yearsOfExperience: 1,
    currentStatus: 'AVAILABLE',
    currentStatusText: '可应聘',
    source: 'HR_IMPORT',
    sourceText: 'HR 录入',
    resumeCount: 1,
    latestApplicationStatus: 'SCREENING',
    latestApplicationStatusText: '筛选中',
    latestJobTitle: 'AI 算法实习生',
    latestMatchScore: 91,
    lastActivityAt: '2026-07-15T09:40:00',
    duplicateRisk: false,
    resumes: [
      {
        id: 302,
        resumeName: '李若溪-AI 算法简历',
        fileUrl: '/files/resume/liruoxi-ai.pdf',
        fileType: 'PDF',
        parsedContent: '熟悉 Python、机器学习、文本向量化和语义检索。',
        skills: ['Python', 'Machine Learning', 'RAG', 'Qdrant'],
        projectExperience: '参与简历语义匹配实验，负责文本向量化和相似度计算。',
        workExperience: '1 年 AI 算法实习经验。',
        isDefault: true,
        updatedAt: '2026-07-15T09:30:00',
      },
    ],
    applications: [
      {
        id: 402,
        jobId: 102,
        jobTitle: 'AI 算法实习生',
        resumeId: 302,
        status: 'SCREENING',
        statusText: '筛选中',
        source: 'ONLINE',
        allowAdjustment: true,
        hrNote: null,
        appliedAt: '2026-07-15T09:35:00',
        aiMatch: {
          score: 91,
          level: 'HIGH',
          summary: '机器学习、RAG 和向量检索经验与岗位方向高度相关。',
          highlights: ['具备语义匹配项目经验', '技能方向清晰'],
          risks: ['缺少完整工程落地经验'],
          suggestion: '建议由面试官核实模型评估方法和工程实践。',
          modelName: 'demo-model',
          generatedAt: '2026-07-15T09:40:00',
        },
      },
    ],
  },
  {
    id: 203,
    userId: null,
    name: '陈思悦',
    gender: '女',
    phone: '13900000103',
    email: 'chensiyue@example.com',
    education: '本科',
    school: '江南大学',
    major: '工业设计',
    yearsOfExperience: 6,
    currentStatus: 'INTERVIEWING',
    currentStatusText: '面试中',
    source: 'ONLINE',
    sourceText: '在线投递',
    resumeCount: 1,
    latestApplicationStatus: 'INTERVIEWING',
    latestApplicationStatusText: '面试中',
    latestJobTitle: 'UI/UX 设计师',
    latestMatchScore: 84,
    lastActivityAt: '2026-07-15T11:10:00',
    duplicateRisk: true,
    resumes: [
      {
        id: 303,
        resumeName: '陈思悦-产品设计作品集',
        fileUrl: '/files/resume/chensiyue-design.pdf',
        fileType: 'PDF',
        parsedContent: '6 年企业产品设计经验，负责复杂工作台和设计系统建设。',
        skills: ['企业产品设计', '设计系统', '用户研究', 'Figma'],
        projectExperience: '主导企业招聘与协作产品的设计系统建设。',
        workExperience: '6 年产品与交互设计经验。',
        isDefault: true,
        updatedAt: '2026-07-15T10:50:00',
      },
    ],
    applications: [
      {
        id: 403,
        jobId: 103,
        jobTitle: 'UI/UX 设计师',
        resumeId: 303,
        status: 'INTERVIEWING',
        statusText: '面试中',
        source: 'ONLINE',
        allowAdjustment: false,
        hrNote: '作品集完整，等待终面反馈。',
        appliedAt: '2026-07-13T14:10:00',
        aiMatch: {
          score: 84,
          level: 'HIGH',
          summary: '企业产品与设计系统经验符合岗位重点。',
          highlights: ['复杂工作台经验', '设计系统建设经验'],
          risks: ['团队管理范围需要进一步核实'],
          suggestion: '建议结合真实作品和跨团队协作案例判断。',
          modelName: 'demo-model',
          generatedAt: '2026-07-13T14:15:00',
        },
      },
    ],
  },
]

function send(response, status, payload) {
  const body = JSON.stringify(payload)
  response.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Content-Length': Buffer.byteLength(body),
  })
  response.end(body)
}

function success(response, data = null) {
  send(response, 200, { code: 200, message: 'success', data })
}

function fail(response, status, message) {
  send(response, status, { code: status, message, data: null })
}

async function readJson(request) {
  const chunks = []
  for await (const chunk of request) chunks.push(chunk)
  if (chunks.length === 0) return {}
  return JSON.parse(Buffer.concat(chunks).toString('utf8'))
}

function statusText(status) {
  if (status === 'OPEN') return '招聘中'
  if (status === 'CLOSED') return '已关闭'
  return '草稿'
}

function findJob(response, id) {
  const job = jobs.find((item) => item.id === id)
  if (!job) fail(response, 404, '职位不存在')
  return job
}

function findCandidate(response, id) {
  const candidate = candidates.find((item) => item.id === id)
  if (!candidate) fail(response, 404, '候选人不存在')
  return candidate
}

function findPipelineApplication(response, id) {
  const application = pipelineApplications.find((item) => item.id === id)
  if (!application) fail(response, 404, '投递记录不存在')
  return application
}

function findInterview(response, id) {
  const interview = interviewTasks.find((item) => item.id === id)
  if (!interview) fail(response, 404, '面试任务不存在')
  return interview
}

function findOffer(response, id) {
  const offer = offers.find((item) => item.id === id)
  if (!offer) fail(response, 404, 'Offer 不存在')
  return offer
}

function findOnboarding(response, id) {
  const record = onboardings.find((item) => item.id === id)
  if (!record) fail(response, 404, '入职记录不存在')
  return record
}

function findEmployee(response, id) {
  const record = employees.find((item) => item.id === id)
  if (!record) fail(response, 404, '员工档案不存在')
  return record
}

function candidateStatusText(status) {
  if (status === 'HIRED') return '已入职'
  if (status === 'INTERVIEWING') return '面试中'
  return '可应聘'
}

const server = createServer(async (request, response) => {
  const method = request.method ?? 'GET'
  const url = new URL(request.url ?? '/', `http://127.0.0.1:${port}`)

  try {
    if (method === 'POST' && url.pathname === '/auth/login') {
      const body = await readJson(request)
      if (!body.username || !body.password) {
        fail(response, 400, '请输入账号和密码')
        return
      }

      const username = body.username.toLowerCase()
      const roleCode = username.includes('admin')
        ? 'ADMIN'
        : username.includes('interviewer')
          ? 'INTERVIEWER'
          : username.includes('candidate')
            ? 'CANDIDATE'
            : 'HR'
      const roleName =
        roleCode === 'ADMIN'
          ? '系统管理员'
          : roleCode === 'INTERVIEWER'
            ? '面试官'
            : roleCode === 'CANDIDATE'
              ? '候选人'
              : '招聘 HR'
      success(response, {
        token: 'local-mock-token',
        tokenType: 'Bearer',
        userInfo: {
          userId: 1,
          username: body.username,
          realName:
            roleCode === 'ADMIN'
              ? '本地管理员'
              : roleCode === 'INTERVIEWER'
                ? '王面试官'
                : roleCode === 'CANDIDATE'
                  ? '当前候选人'
                  : '本地 HR',
          phone: null,
          email: null,
          roleCode,
          roleName,
          roleId:
            roleCode === 'ADMIN'
              ? 1
              : roleCode === 'INTERVIEWER'
                ? 3
                : roleCode === 'CANDIDATE'
                  ? 4
                  : 2,
          status: 1,
        },
      })
      return
    }

    if (method === 'GET' && url.pathname === '/jobs/open') {
      const records = jobs.filter((job) => job.status === 'OPEN')
      success(response, { total: records.length, records })
      return
    }

    if (method === 'GET' && url.pathname === '/candidate/me') {
      success(response, {
        id: 201,
        name: '当前候选人',
        gender: '男',
        age: 25,
        phone: '139****0101',
        email: 'candidate@example.com',
        education: '本科',
        school: '武汉理工大学',
        major: '软件工程',
        yearsOfExperience: 3,
        currentStatus: 'INTERVIEWING',
        source: 'SELF_REGISTER',
        hasAccount: true,
        createdAt: '2026-07-01T09:00:00',
        updatedAt: '2026-07-15T10:20:00',
        resumes: [],
      })
      return
    }

    if (method === 'GET' && url.pathname === '/resumes/me') {
      success(response, [
        {
          id: 301,
          resumeName: '我的 Java 后端简历',
          fileUrl: null,
          fileType: 'PDF',
          isDefault: 1,
          createdAt: '2026-07-12T10:30:00',
          parseStatus: 'SUCCESS',
          parseStatusText: '解析完成',
        },
      ])
      return
    }

    if (method === 'GET' && url.pathname === '/applications/me') {
      success(response, {
        total: 1,
        records: [
          {
            id: 401,
            jobId: 101,
            jobTitle: 'Java 后端开发工程师',
            department: '技术部',
            resumeId: 301,
            resumeName: '我的 Java 后端简历',
            status: 'INTERVIEW',
            statusText: '面试中',
            allowAdjustment: 0,
            adjustedJobId: null,
            appliedAt: '2026-07-13T14:00:00',
          },
        ],
      })
      return
    }

    if (method === 'GET' && url.pathname === '/interviews/me') {
      const item = interviewTasks[0]
      const record = item
        ? {
            id: item.id,
            applicationId: item.applicationId,
            jobId: item.jobId,
            jobTitle: item.jobTitle,
            candidateId: item.candidateId,
            candidateName: '当前候选人',
            interviewerId: item.interviewerId,
            interviewerName: item.interviewerName,
            round: item.round,
            roundText: item.roundText,
            interviewTime: item.interviewTime,
            method: item.method,
            methodText: item.methodText,
            location: item.location,
            status: item.status,
            statusText: item.statusText,
          }
        : null
      success(response, { total: record ? 1 : 0, records: record ? [record] : [] })
      return
    }

    if (method === 'GET' && url.pathname === '/interviews/interviewer/me') {
      const records = interviewTasks.map((item) => ({
        id: item.id,
        applicationId: item.applicationId,
        jobId: item.jobId,
        jobTitle: item.jobTitle,
        candidateId: item.candidateId,
        candidateName: item.candidateName,
        interviewerId: item.interviewerId,
        interviewerName: item.interviewerName,
        round: item.round,
        roundText: item.roundText,
        interviewTime: item.interviewTime,
        method: item.method,
        methodText: item.methodText,
        location: item.location,
        status: item.status,
        statusText: item.statusText,
      }))
      success(response, { total: records.length, records })
      return
    }

    if (method === 'GET' && url.pathname === '/offers/me') {
      success(response, { total: 0, records: [] })
      return
    }

    if (method === 'GET' && url.pathname === '/onboarding/me') {
      success(response, [])
      return
    }

    if (method === 'GET' && url.pathname === '/candidates') {
      const keyword = (url.searchParams.get('keyword') ?? '').toLocaleLowerCase()
      const education = url.searchParams.get('education') ?? ''
      const school = (url.searchParams.get('school') ?? '').toLocaleLowerCase()
      const yearsOfExperienceMin = Number(url.searchParams.get('yearsOfExperienceMin') ?? 0)
      const currentStatus = url.searchParams.get('currentStatus') ?? ''
      const pageNum = Number(url.searchParams.get('pageNum') ?? 1)
      const pageSize = Number(url.searchParams.get('pageSize') ?? 10)
      const filtered = candidates.filter((candidate) => {
        const keywordFields = [candidate.name, candidate.phone ?? '', candidate.email ?? '']
        return (
          (!keyword ||
            keywordFields.some((value) => value.toLocaleLowerCase().includes(keyword))) &&
          (!education || candidate.education === education) &&
          (!school || candidate.school?.toLocaleLowerCase().includes(school)) &&
          candidate.yearsOfExperience >= yearsOfExperienceMin &&
          (!currentStatus || candidate.currentStatus === currentStatus)
        )
      })
      const start = (pageNum - 1) * pageSize
      const records = filtered
        .slice(start, start + pageSize)
        .map(({ resumes, applications, ...candidate }) => {
          void resumes
          void applications
          return candidate
        })
      success(response, { total: filtered.length, records })
      return
    }

    const candidateDetailMatch = url.pathname.match(/^\/candidates\/(\d+)$/)
    if (method === 'GET' && candidateDetailMatch) {
      const candidate = findCandidate(response, Number(candidateDetailMatch[1]))
      if (candidate) success(response, candidate)
      return
    }

    if (method === 'POST' && url.pathname === '/candidates') {
      const body = await readJson(request)
      if (!body.name || (!body.phone && !body.email)) {
        fail(response, 400, '候选人姓名及手机号或邮箱不能为空')
        return
      }
      const duplicate = candidates.some(
        (candidate) =>
          (body.phone && candidate.phone === body.phone) ||
          (body.email && candidate.email === body.email),
      )
      if (duplicate) {
        fail(response, 409, '手机号或邮箱已关联其他候选人，请先检查重复记录')
        return
      }
      const now = new Date().toISOString()
      const candidate = {
        id: nextCandidateId++,
        userId: null,
        name: body.name,
        gender: body.gender || null,
        phone: body.phone || null,
        email: body.email || null,
        education: body.education || null,
        school: body.school || null,
        major: body.major || null,
        yearsOfExperience: body.yearsOfExperience ?? 0,
        currentStatus: body.currentStatus || 'AVAILABLE',
        currentStatusText: candidateStatusText(body.currentStatus || 'AVAILABLE'),
        source: body.source || 'HR_IMPORT',
        sourceText: body.source === 'SELF_REGISTER' ? '候选人注册' : 'HR 录入',
        resumeCount: 0,
        latestApplicationStatus: null,
        latestApplicationStatusText: null,
        latestJobTitle: null,
        latestMatchScore: null,
        lastActivityAt: now,
        duplicateRisk: false,
        resumes: [],
        applications: [],
      }
      candidates.unshift(candidate)
      success(response, candidate.id)
      return
    }

    if (method === 'GET' && url.pathname === '/applications/pipeline') {
      const keyword = (url.searchParams.get('keyword') ?? '').toLocaleLowerCase()
      const jobId = Number(url.searchParams.get('jobId') ?? 0)
      const status = url.searchParams.get('status') ?? ''
      const pageNum = Number(url.searchParams.get('pageNum') ?? 1)
      const pageSize = Number(url.searchParams.get('pageSize') ?? 20)
      const filtered = pipelineApplications.filter((application) => {
        const keywordFields = [
          application.candidateName,
          application.candidatePhone ?? '',
          application.jobTitle,
        ]
        return (
          (!keyword ||
            keywordFields.some((value) => value.toLocaleLowerCase().includes(keyword))) &&
          (!jobId || application.jobId === jobId) &&
          (!status || application.status === status)
        )
      })
      const start = (pageNum - 1) * pageSize
      success(response, {
        total: filtered.length,
        records: filtered.slice(start, start + pageSize),
      })
      return
    }

    const pipelineDetailMatch = url.pathname.match(/^\/applications\/(\d+)\/pipeline$/)
    if (method === 'GET' && pipelineDetailMatch) {
      const application = findPipelineApplication(response, Number(pipelineDetailMatch[1]))
      if (application) success(response, application)
      return
    }

    const applicationStatusMatch = url.pathname.match(/^\/applications\/(\d+)\/status$/)
    if (method === 'PUT' && applicationStatusMatch) {
      const application = findPipelineApplication(response, Number(applicationStatusMatch[1]))
      if (!application) return
      const body = await readJson(request)
      if (application.status !== 'SUBMITTED' || body.toStatus !== 'SCREENING') {
        fail(response, 400, '当前状态不能开始筛选')
        return
      }
      const now = new Date().toISOString()
      application.status = 'SCREENING'
      application.statusText = getPipelineStatusText(application.status)
      application.ownerName = '当前 HR'
      application.hrNote = body.note || application.hrNote
      application.lastActivityAt = now
      application.timeline.push({
        id: `${application.id}-screening-${now}`,
        title: '进入 HR 筛选',
        description: body.note || 'HR 开始审核候选人资料。',
        actorName: '当前 HR',
        occurredAt: now,
        source: 'BUSINESS',
        relatedObject: `投递 #${application.id}`,
      })
      success(response, null)
      return
    }

    const applicationScreeningMatch = url.pathname.match(/^\/applications\/(\d+)\/screening$/)
    if (method === 'PUT' && applicationScreeningMatch) {
      const application = findPipelineApplication(response, Number(applicationScreeningMatch[1]))
      if (!application) return
      const body = await readJson(request)
      if (application.status !== 'SCREENING') {
        fail(response, 400, '只有筛选中的投递可以提交筛选结论')
        return
      }
      if (body.decision === 'REJECT' && (!body.rejectReasonCode || !body.note?.trim())) {
        fail(response, 400, '拒绝候选人必须选择原因并填写说明')
        return
      }
      if (body.decision === 'PENDING' && !body.note?.trim()) {
        fail(response, 400, '保留待定时必须填写待核实事项')
        return
      }
      const now = new Date().toISOString()
      application.status =
        body.decision === 'PASS'
          ? 'SCREEN_PASS'
          : body.decision === 'REJECT'
            ? 'SCREEN_REJECT'
            : 'SCREENING'
      application.statusText = getPipelineStatusText(application.status)
      application.reviewDecision = body.decision
      application.hrNote = body.note?.trim() || application.hrNote
      application.rejectReasonCode = body.decision === 'REJECT' ? body.rejectReasonCode : null
      application.rejectReason = body.decision === 'REJECT' ? body.note.trim() : null
      application.lastActivityAt = now
      application.timeline.push({
        id: `${application.id}-review-${now}`,
        title:
          body.decision === 'PASS'
            ? 'HR 确认初筛通过'
            : body.decision === 'REJECT'
              ? 'HR 确认初筛未通过'
              : 'HR 保留候选人为待定',
        description: body.note?.trim() || 'HR 已完成本次筛选审核。',
        actorName: '当前 HR',
        occurredAt: now,
        source: 'BUSINESS',
        relatedObject: `投递 #${application.id}`,
      })
      success(response, null)
      return
    }

    if (method === 'GET' && url.pathname === '/interviews/tasks') {
      const keyword = (url.searchParams.get('keyword') ?? '').toLocaleLowerCase()
      const status = url.searchParams.get('status') ?? ''
      const feedbackState = url.searchParams.get('feedbackState') ?? ''
      const pageNum = Number(url.searchParams.get('pageNum') ?? 1)
      const pageSize = Number(url.searchParams.get('pageSize') ?? 8)
      const filtered = interviewTasks.filter((interview) => {
        const keywordFields = [
          interview.candidateName,
          interview.jobTitle,
          interview.interviewerName,
        ]
        return (
          (!keyword ||
            keywordFields.some((value) => value.toLocaleLowerCase().includes(keyword))) &&
          (!status || interview.status === status) &&
          (!feedbackState || interview.feedbackState === feedbackState)
        )
      })
      const start = (pageNum - 1) * pageSize
      const records = filtered.slice(start, start + pageSize).map((interview) => {
        const { candidateBrief, scorecard: items, questions, feedback, ...summary } = interview
        void candidateBrief
        void items
        void questions
        void feedback
        return summary
      })
      success(response, { total: filtered.length, records })
      return
    }

    const interviewWorkspaceMatch = url.pathname.match(/^\/interviews\/(\d+)\/workspace$/)
    if (method === 'GET' && interviewWorkspaceMatch) {
      const interview = findInterview(response, Number(interviewWorkspaceMatch[1]))
      if (interview) success(response, interview)
      return
    }

    const interviewDraftMatch = url.pathname.match(/^\/interviews\/(\d+)\/feedback\/draft$/)
    if (method === 'PUT' && interviewDraftMatch) {
      const interview = findInterview(response, Number(interviewDraftMatch[1]))
      if (!interview) return
      if (interview.feedbackState === 'SUBMITTED') {
        fail(response, 400, '已提交的反馈不能覆盖')
        return
      }
      const body = await readJson(request)
      interview.scorecard = body.scorecard
      interview.feedbackState = 'DRAFT'
      interview.feedbackStateText = '草稿'
      Object.assign(interview.feedback, {
        score: body.score,
        comment: body.comment?.trim() ?? '',
        suggestion: body.suggestion ?? null,
        state: 'DRAFT',
      })
      success(response)
      return
    }

    const interviewFeedbackMatch = url.pathname.match(/^\/interviews\/(\d+)\/feedback$/)
    if (method === 'POST' && interviewFeedbackMatch) {
      const interview = findInterview(response, Number(interviewFeedbackMatch[1]))
      if (!interview) return
      if (interview.feedbackState === 'SUBMITTED') {
        fail(response, 400, '该面试反馈已经提交')
        return
      }
      const body = await readJson(request)
      if (
        body.score === null ||
        !body.comment?.trim() ||
        !body.suggestion ||
        body.scorecard?.some((item) => item.score === null || !item.evidence?.trim())
      ) {
        fail(response, 400, '请完成所有评分、评价证据、综合评价和录用建议')
        return
      }
      const submittedAt = new Date().toISOString()
      interview.scorecard = body.scorecard
      interview.status = 'COMPLETED'
      interview.statusText = '已完成'
      interview.feedbackState = 'SUBMITTED'
      interview.feedbackStateText = '已提交'
      Object.assign(interview.feedback, {
        id: interview.feedback.id ?? interview.id + 200,
        score: body.score,
        comment: body.comment.trim(),
        suggestion: body.suggestion,
        state: 'SUBMITTED',
        submittedAt,
      })
      success(response)
      return
    }

    const interviewQuestionMatch = url.pathname.match(/^\/ai\/interviews\/(\d+)\/questions$/)
    if (method === 'POST' && interviewQuestionMatch) {
      const interview = findInterview(response, Number(interviewQuestionMatch[1]))
      if (!interview) return
      const body = await readJson(request)
      if (!body.focus?.trim()) {
        fail(response, 400, '请先填写需要追问的主题')
        return
      }
      success(response, [
        {
          id: `${interview.id}-manual-${Date.now()}`,
          category: '临场追问',
          question: `请结合真实项目说明“${body.focus.trim()}”中的关键判断、执行过程与最终结果。`,
          source: 'MANUAL',
        },
      ])
      return
    }

    if (method === 'GET' && url.pathname === '/offers') {
      const keyword = (url.searchParams.get('keyword') ?? '').toLocaleLowerCase()
      const status = url.searchParams.get('status') ?? ''
      const pageNum = Number(url.searchParams.get('pageNum') ?? 1)
      const pageSize = Number(url.searchParams.get('pageSize') ?? 10)
      const filtered = offers.filter((offer) => {
        const keywordFields = [offer.candidateName, offer.jobTitle, offer.department]
        return (
          (!keyword ||
            keywordFields.some((value) => value.toLocaleLowerCase().includes(keyword))) &&
          (!status || offer.status === status)
        )
      })
      const start = (pageNum - 1) * pageSize
      success(response, {
        total: filtered.length,
        records: filtered.slice(start, start + pageSize),
      })
      return
    }

    const offerDetailMatch = url.pathname.match(/^\/offers\/(\d+)$/)
    if (method === 'GET' && offerDetailMatch) {
      const offer = findOffer(response, Number(offerDetailMatch[1]))
      if (offer) success(response, offer)
      return
    }

    if (method === 'POST' && url.pathname === '/offers') {
      const body = await readJson(request)
      if (!body.applicationId || !body.salary || !body.entryDate || !body.workLocation?.trim()) {
        fail(response, 400, '投递、月薪、入职日期和工作地点不能为空')
        return
      }
      const now = new Date().toISOString()
      const id = nextOfferId++
      const offer = {
        id,
        applicationId: body.applicationId,
        candidateId: body.applicationId + 100,
        candidateName: '待接口补充候选人',
        candidatePhone: null,
        candidateEmail: null,
        jobId: 0,
        jobTitle: '待接口补充职位',
        department: '待补充',
        interviewScore: null,
        interviewSuggestion: null,
        salary: body.salary,
        entryDate: body.entryDate,
        probationMonths: body.probationMonths ?? 3,
        workLocation: body.workLocation.trim(),
        status: 'DRAFT',
        statusText: '草稿',
        remark: body.remark?.trim() ?? '',
        sentAt: null,
        acceptedAt: null,
        createdByName: '当前 HR',
        createdAt: now,
        updatedAt: now,
        timeline: [
          {
            id: `${id}-created`,
            title: '创建 Offer 草稿',
            description: 'HR 创建录用方案，尚未发送。',
            actorName: '当前 HR',
            occurredAt: now,
          },
        ],
      }
      offers.unshift(offer)
      success(response, offer.id)
      return
    }

    if (method === 'PUT' && offerDetailMatch) {
      const offer = findOffer(response, Number(offerDetailMatch[1]))
      if (!offer) return
      if (offer.status !== 'DRAFT') {
        fail(response, 400, '只有草稿 Offer 可以编辑')
        return
      }
      const body = await readJson(request)
      Object.assign(offer, {
        salary: body.salary,
        entryDate: body.entryDate,
        probationMonths: body.probationMonths,
        workLocation: body.workLocation,
        remark: body.remark,
        updatedAt: new Date().toISOString(),
      })
      success(response)
      return
    }

    const offerSendMatch = url.pathname.match(/^\/offers\/(\d+)\/send$/)
    if (method === 'POST' && offerSendMatch) {
      const offer = findOffer(response, Number(offerSendMatch[1]))
      if (!offer) return
      if (offer.status !== 'DRAFT') {
        fail(response, 400, '只有草稿 Offer 可以发送')
        return
      }
      if (!offer.salary || !offer.entryDate || !offer.workLocation?.trim()) {
        fail(response, 400, '请先完善薪资、入职日期和工作地点')
        return
      }
      const body = await readJson(request)
      const sentAt = new Date().toISOString()
      offer.status = 'SENT'
      offer.statusText = offerStatusText(offer.status)
      offer.sentAt = sentAt
      offer.updatedAt = sentAt
      offer.timeline.push({
        id: `${offer.id}-sent-${sentAt}`,
        title: '发送 Offer',
        description: body.note?.trim() || 'Offer 已发送给候选人。',
        actorName: '当前 HR',
        occurredAt: sentAt,
      })
      success(response)
      return
    }

    const offerRevokeMatch = url.pathname.match(/^\/offers\/(\d+)\/revoke$/)
    if (method === 'POST' && offerRevokeMatch) {
      const offer = findOffer(response, Number(offerRevokeMatch[1]))
      if (!offer) return
      const body = await readJson(request)
      if (offer.status !== 'SENT') {
        fail(response, 400, '只有已发送 Offer 可以撤回')
        return
      }
      if (!body.reason?.trim()) {
        fail(response, 400, '撤回 Offer 必须填写原因')
        return
      }
      const revokedAt = new Date().toISOString()
      offer.status = 'REVOKED'
      offer.statusText = offerStatusText(offer.status)
      offer.remark = body.reason.trim()
      offer.updatedAt = revokedAt
      offer.timeline.push({
        id: `${offer.id}-revoked-${revokedAt}`,
        title: '撤回 Offer',
        description: body.reason.trim(),
        actorName: '当前 HR',
        occurredAt: revokedAt,
      })
      success(response)
      return
    }

    if (method === 'GET' && url.pathname === '/onboardings') {
      const keyword = (url.searchParams.get('keyword') ?? '').toLocaleLowerCase()
      const status = url.searchParams.get('status') ?? ''
      const pageNum = Number(url.searchParams.get('pageNum') ?? 1)
      const pageSize = Number(url.searchParams.get('pageSize') ?? 10)
      const filtered = onboardings.filter(
        (record) =>
          (!keyword ||
            [record.candidateName, record.jobTitle, record.department].some((value) =>
              value.toLocaleLowerCase().includes(keyword),
            )) &&
          (!status || record.status === status),
      )
      const start = (pageNum - 1) * pageSize
      success(response, {
        total: filtered.length,
        records: filtered.slice(start, start + pageSize),
      })
      return
    }

    const onboardingDetailMatch = url.pathname.match(/^\/onboardings\/(\d+)$/)
    if (method === 'GET' && onboardingDetailMatch) {
      const record = findOnboarding(response, Number(onboardingDetailMatch[1]))
      if (record) success(response, record)
      return
    }

    const onboardingReviewMatch = url.pathname.match(/^\/onboardings\/(\d+)\/review$/)
    if (method === 'POST' && onboardingReviewMatch) {
      const record = findOnboarding(response, Number(onboardingReviewMatch[1]))
      if (!record) return
      if (record.status !== 'PENDING') {
        fail(response, 400, '只有待提交记录可以开始审核')
        return
      }
      const body = await readJson(request)
      const now = new Date().toISOString()
      Object.assign(record, {
        status: 'REVIEWING',
        statusText: onboardingStatusText('REVIEWING'),
        currentStep: '材料审核',
        materialStatus: 'REVIEWING',
        materialStatusText: materialStatusText('REVIEWING'),
        remark: body.note?.trim() || 'HR 已接收入职材料并开始审核。',
        updatedAt: now,
      })
      record.timeline.push({
        id: `${record.id}-review-${now}`,
        title: '开始材料审核',
        description: record.remark,
        actorName: '当前 HR',
        occurredAt: now,
      })
      success(response)
      return
    }

    const materialReviewMatch = url.pathname.match(/^\/onboardings\/(\d+)\/material-review$/)
    if (method === 'POST' && materialReviewMatch) {
      const record = findOnboarding(response, Number(materialReviewMatch[1]))
      if (!record) return
      const body = await readJson(request)
      if (record.status !== 'REVIEWING') {
        fail(response, 400, '只有审核中的记录可以提交材料结论')
        return
      }
      if (!['APPROVE', 'REJECT'].includes(body.decision) || !body.note?.trim()) {
        fail(response, 400, '材料审核结论和说明不能为空')
        return
      }
      const approved = body.decision === 'APPROVE'
      const now = new Date().toISOString()
      Object.assign(record, {
        status: approved ? 'APPROVED' : 'PENDING',
        statusText: onboardingStatusText(approved ? 'APPROVED' : 'PENDING'),
        currentStep: approved ? '待确认入职' : '待补充材料',
        materialStatus: approved ? 'APPROVED' : 'REJECTED',
        materialStatusText: materialStatusText(approved ? 'APPROVED' : 'REJECTED'),
        remark: body.note.trim(),
        updatedAt: now,
      })
      record.timeline.push({
        id: `${record.id}-material-${now}`,
        title: approved ? '材料审核通过' : '材料退回补充',
        description: body.note.trim(),
        actorName: '当前 HR',
        occurredAt: now,
      })
      success(response)
      return
    }

    const onboardingCompleteMatch = url.pathname.match(/^\/onboardings\/(\d+)\/complete$/)
    if (method === 'POST' && onboardingCompleteMatch) {
      const record = findOnboarding(response, Number(onboardingCompleteMatch[1]))
      if (!record) return
      const body = await readJson(request)
      if (record.status !== 'APPROVED' || record.materialStatus !== 'APPROVED') {
        fail(response, 400, '只有材料已通过的记录可以确认入职')
        return
      }
      if (
        !body.employeeNo?.trim() ||
        !body.department?.trim() ||
        !body.position?.trim() ||
        !body.entryDate
      ) {
        fail(response, 400, '员工编号、部门、岗位和入职日期不能为空')
        return
      }
      if (employees.some((item) => item.employeeNo === body.employeeNo.trim())) {
        fail(response, 409, '员工编号已存在')
        return
      }
      const now = new Date().toISOString()
      Object.assign(record, {
        status: 'ONBOARDED',
        statusText: onboardingStatusText('ONBOARDED'),
        currentStep: '入职完成',
        remark: body.note?.trim() || 'HR 已确认员工到岗。',
        completedAt: now,
        updatedAt: now,
      })
      record.timeline.push({
        id: `${record.id}-complete-${now}`,
        title: '确认入职并生成档案',
        description: `员工编号 ${body.employeeNo.trim()}。${record.remark}`,
        actorName: '当前 HR',
        occurredAt: now,
      })
      employees.unshift({
        id: Math.max(1000, ...employees.map((item) => item.id)) + 1,
        userId: null,
        candidateId: record.candidateId,
        onboardingId: record.id,
        employeeNo: body.employeeNo.trim(),
        name: record.candidateName,
        phone: record.candidatePhone,
        email: record.candidateEmail,
        department: body.department.trim(),
        position: body.position.trim(),
        entryDate: body.entryDate,
        status: 'PROBATION',
        statusText: '试用期',
        performanceSummary: '新入职员工，暂无绩效记录。',
        attendanceSummary: '新入职员工，暂无考勤记录。',
        satisfactionFeedback: null,
        turnoverRiskLevel: null,
        riskAssessedAt: null,
        createdAt: now,
        updatedAt: now,
      })
      success(response)
      return
    }

    if (method === 'GET' && url.pathname === '/employees') {
      const keyword = (url.searchParams.get('keyword') ?? '').toLocaleLowerCase()
      const department = url.searchParams.get('department') ?? ''
      const status = url.searchParams.get('status') ?? ''
      const pageNum = Number(url.searchParams.get('pageNum') ?? 1)
      const pageSize = Number(url.searchParams.get('pageSize') ?? 10)
      const filtered = employees.filter(
        (record) =>
          (!keyword ||
            [record.name, record.employeeNo, record.position].some((value) =>
              value.toLocaleLowerCase().includes(keyword),
            )) &&
          (!department || record.department === department) &&
          (!status || record.status === status),
      )
      const start = (pageNum - 1) * pageSize
      success(response, {
        total: filtered.length,
        records: filtered.slice(start, start + pageSize),
      })
      return
    }

    const employeeDetailMatch = url.pathname.match(/^\/employees\/(\d+)$/)
    if (method === 'GET' && employeeDetailMatch) {
      const record = findEmployee(response, Number(employeeDetailMatch[1]))
      if (record) success(response, record)
      return
    }

    if (method === 'GET' && url.pathname === '/jobs') {
      const keyword = (url.searchParams.get('keyword') ?? '').toLocaleLowerCase()
      const department = url.searchParams.get('department') ?? ''
      const status = url.searchParams.get('status') ?? ''
      const pageNum = Number(url.searchParams.get('pageNum') ?? 1)
      const pageSize = Number(url.searchParams.get('pageSize') ?? 10)
      const filtered = jobs.filter(
        (job) =>
          (!keyword || job.title.toLocaleLowerCase().includes(keyword)) &&
          (!department || job.department === department) &&
          (!status || job.status === status),
      )
      const start = (pageNum - 1) * pageSize
      success(response, {
        total: filtered.length,
        records: filtered.slice(start, start + pageSize),
      })
      return
    }

    const detailMatch = url.pathname.match(/^\/jobs\/(\d+)$/)
    if (method === 'GET' && detailMatch) {
      const job = findJob(response, Number(detailMatch[1]))
      if (job) success(response, job)
      return
    }

    if (method === 'POST' && url.pathname === '/jobs') {
      const body = await readJson(request)
      if (!body.title || !body.department || !body.headcount) {
        fail(response, 400, '职位名称、部门和招聘人数不能为空')
        return
      }
      const now = new Date().toISOString()
      const job = {
        id: nextJobId++,
        title: body.title,
        department: body.department,
        location: body.location || null,
        salaryRange: `${body.salaryMin}-${body.salaryMax}`,
        headcount: body.headcount,
        description: body.responsibilities || null,
        requirement: body.requirements || null,
        status: 'DRAFT',
        statusText: '草稿',
        createdAt: now,
        updatedAt: now,
      }
      jobs.unshift(job)
      success(response, job.id)
      return
    }

    if (method === 'PUT' && detailMatch) {
      const job = findJob(response, Number(detailMatch[1]))
      if (!job) return
      const body = await readJson(request)
      Object.assign(job, {
        title: body.title,
        department: body.department,
        location: body.location || null,
        salaryRange: `${body.salaryMin}-${body.salaryMax}`,
        headcount: body.headcount,
        description: body.responsibilities || null,
        requirement: body.requirements || null,
        updatedAt: new Date().toISOString(),
      })
      success(response)
      return
    }

    const actionMatch = url.pathname.match(/^\/jobs\/(\d+)\/(publish|close)$/)
    if (method === 'PUT' && actionMatch) {
      const job = findJob(response, Number(actionMatch[1]))
      if (!job) return
      const action = actionMatch[2]
      if (action === 'publish' && job.status !== 'DRAFT') {
        fail(response, 400, '只有草稿职位可以发布')
        return
      }
      if (action === 'close' && job.status !== 'OPEN') {
        fail(response, 400, '只有招聘中的职位可以关闭')
        return
      }
      job.status = action === 'publish' ? 'OPEN' : 'CLOSED'
      job.statusText = statusText(job.status)
      job.updatedAt = new Date().toISOString()
      success(response)
      return
    }

    fail(response, 404, '本地 Mock API 未实现该接口')
  } catch (error) {
    fail(response, 500, error instanceof Error ? error.message : '本地 Mock API 处理失败')
  }
})

server.listen(port, '127.0.0.1', () => {
  stdout.write(`Recruit Smart Mock API: http://127.0.0.1:${port}\n`)
})
