import type {
  CandidateCreateRequest,
  CandidateDetail,
  CandidateQuery,
  CandidateStatus,
} from '@/types/candidate'

export const initialDemoCandidates: CandidateDetail[] = [
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
    resumeCount: 2,
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
        skills: ['Java', 'Spring Boot', 'MySQL', 'Redis', 'Git'],
        projectExperience: '负责职位、投递和面试模块接口与数据模型设计。',
        workExperience: '3 年企业应用后端开发经验。',
        isDefault: true,
        updatedAt: '2026-07-14T18:10:00',
      },
      {
        id: 302,
        resumeName: '张晨-项目经历补充',
        fileUrl: null,
        fileType: 'TEXT',
        parsedContent: '补充微服务拆分与性能治理项目经历。',
        skills: ['Spring Cloud', 'RabbitMQ', 'Docker'],
        projectExperience: '参与订单与招聘业务微服务拆分。',
        workExperience: null,
        isDefault: false,
        updatedAt: '2026-07-13T09:30:00',
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
        id: 303,
        resumeName: '李若溪-AI 算法简历',
        fileUrl: '/files/resume/liruoxi-ai.pdf',
        fileType: 'PDF',
        parsedContent: '熟悉 Python、机器学习、文本向量化和语义检索。',
        skills: ['Python', 'Machine Learning', 'RAG', 'Qdrant', 'PyTorch'],
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
        resumeId: 303,
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
    name: '王嘉禾',
    gender: '男',
    phone: '13900000103',
    email: 'wangjiahe@example.com',
    education: '本科',
    school: '湖北大学',
    major: '人力资源管理',
    yearsOfExperience: 4,
    currentStatus: 'AVAILABLE',
    currentStatusText: '可应聘',
    source: 'HR_IMPORT',
    sourceText: 'HR 录入',
    resumeCount: 1,
    latestApplicationStatus: 'SCREEN_REJECT',
    latestApplicationStatusText: '初筛未通过',
    latestJobTitle: 'Java 后端开发工程师',
    latestMatchScore: 35,
    lastActivityAt: '2026-07-14T17:20:00',
    duplicateRisk: false,
    resumes: [
      {
        id: 304,
        resumeName: '王嘉禾-HR 简历',
        fileUrl: '/files/resume/wangjiahe-hr.pdf',
        fileType: 'PDF',
        parsedContent: '熟悉招聘流程、候选人沟通和 Offer 管理。',
        skills: ['招聘流程', '候选人沟通', 'Offer 管理', '入职办理'],
        projectExperience: '参与校园招聘活动组织和候选人跟进。',
        workExperience: '4 年人力资源相关经验。',
        isDefault: true,
        updatedAt: '2026-07-13T15:00:00',
      },
    ],
    applications: [
      {
        id: 403,
        jobId: 101,
        jobTitle: 'Java 后端开发工程师',
        resumeId: 304,
        status: 'SCREEN_REJECT',
        statusText: '初筛未通过',
        source: 'HR_IMPORT',
        allowAdjustment: false,
        hrNote: '与目标岗位技术方向不匹配。',
        appliedAt: '2026-07-14T16:40:00',
        aiMatch: {
          score: 35,
          level: 'LOW',
          summary: '主要经历集中在人力资源方向，与后端岗位要求差距较大。',
          highlights: ['招聘流程与沟通经验较完整'],
          risks: ['缺少 Java、Spring Boot 和数据库开发经验'],
          suggestion: '该结果仅作为岗位匹配参考，拒绝原因仍需由 HR 审核记录。',
          modelName: 'demo-model',
          generatedAt: '2026-07-14T16:45:00',
        },
      },
    ],
  },
  {
    id: 204,
    userId: null,
    name: '陈思悦',
    gender: '女',
    phone: '13900000104',
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
        id: 305,
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
        id: 404,
        jobId: 103,
        jobTitle: 'UI/UX 设计师',
        resumeId: 305,
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

export function getCandidateStatusText(status: CandidateStatus) {
  if (status === 'HIRED') return '已入职'
  if (status === 'INTERVIEWING') return '面试中'
  return '可应聘'
}

export function createDemoCandidate(id: number, data: CandidateCreateRequest): CandidateDetail {
  return {
    id,
    userId: null,
    name: data.name,
    gender: data.gender || null,
    phone: data.phone || null,
    email: data.email || null,
    education: data.education || null,
    school: data.school || null,
    major: data.major || null,
    yearsOfExperience: data.yearsOfExperience,
    currentStatus: data.currentStatus,
    currentStatusText: getCandidateStatusText(data.currentStatus),
    source: data.source,
    sourceText: data.source === 'HR_IMPORT' ? 'HR 录入' : '在线来源',
    resumeCount: 0,
    latestApplicationStatus: null,
    latestApplicationStatusText: null,
    latestJobTitle: null,
    latestMatchScore: null,
    lastActivityAt: new Date().toISOString(),
    duplicateRisk: false,
    resumes: [],
    applications: [],
  }
}

export function getDemoCandidatePage(records: CandidateDetail[], query: CandidateQuery) {
  const keyword = query.keyword.trim().toLocaleLowerCase()
  const school = query.school.trim().toLocaleLowerCase()
  const filtered = records.filter((candidate) => {
    const keywordFields = [candidate.name, candidate.phone ?? '', candidate.email ?? '']
    return (
      (!keyword || keywordFields.some((value) => value.toLocaleLowerCase().includes(keyword))) &&
      (!query.education || candidate.education === query.education) &&
      (!school || candidate.school?.toLocaleLowerCase().includes(school)) &&
      (query.yearsOfExperienceMin === null ||
        candidate.yearsOfExperience >= query.yearsOfExperienceMin) &&
      (!query.currentStatus || candidate.currentStatus === query.currentStatus)
    )
  })
  const start = (query.page - 1) * query.pageSize
  const recordsPage = filtered
    .slice(start, start + query.pageSize)
    .map(({ resumes, applications, ...candidate }) => {
      // 列表契约只返回摘要字段，完整简历和投递记录由详情查询加载。
      void resumes
      void applications
      return candidate
    })

  return {
    items: recordsPage,
    page: query.page,
    pageSize: query.pageSize,
    total: filtered.length,
  }
}
