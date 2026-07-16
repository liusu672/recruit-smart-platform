import type { JobPosition } from '@/types/job'
import type {
  CandidateApplication,
  CandidateOffer,
  CandidateOnboarding,
  CandidateProfile,
  PortalInterview,
  ResumeSummary,
} from '@/types/portal'

export const demoOpenJobs: JobPosition[] = [
  {
    id: 101,
    title: 'Java 后端工程师',
    department: '平台研发部',
    location: '杭州',
    jobType: 'FULL_TIME',
    salaryRange: '18000-26000',
    headcount: 3,
    experienceRequirement: '3 年以上',
    educationRequirement: '本科',
    description: '参与招聘业务平台与服务治理建设。',
    requirement: '熟悉 Java、Spring Boot 与关系型数据库。',
    status: 'OPEN',
    statusText: '招聘中',
    createdAt: '2026-07-12T09:00:00',
    updatedAt: '2026-07-15T10:00:00',
  },
  {
    id: 102,
    title: '高级产品设计师',
    department: '产品体验部',
    location: '上海',
    jobType: 'FULL_TIME',
    salaryRange: '20000-30000',
    headcount: 1,
    experienceRequirement: '5 年以上',
    educationRequirement: '本科',
    description: '负责企业招聘产品的端到端体验设计。',
    requirement: '具备复杂 B 端产品设计经验。',
    status: 'OPEN',
    statusText: '招聘中',
    createdAt: '2026-07-10T09:00:00',
    updatedAt: '2026-07-14T10:00:00',
  },
]

export const demoMyResumes: ResumeSummary[] = [
  {
    id: 201,
    resumeName: '我的后端开发简历',
    fileUrl: null,
    fileType: 'PDF',
    isDefault: 1,
    createdAt: '2026-07-12T10:30:00',
    parseStatus: 'SUCCESS',
    parseStatusText: '解析完成',
  },
]

export const demoMyApplications: CandidateApplication[] = [
  {
    id: 301,
    jobId: 101,
    jobTitle: 'Java 后端工程师',
    department: '平台研发部',
    resumeId: 201,
    resumeName: '我的后端开发简历',
    status: 'INTERVIEW',
    statusText: '面试中',
    allowAdjustment: 0,
    adjustedJobId: null,
    appliedAt: '2026-07-13T14:00:00',
  },
]

export const demoMyInterviews: PortalInterview[] = [
  {
    id: 401,
    applicationId: 301,
    jobId: 101,
    jobTitle: 'Java 后端工程师',
    candidateId: 1,
    candidateName: '当前候选人',
    interviewerId: 9,
    interviewerName: '技术面试官',
    round: 'FIRST',
    roundText: '技术一面',
    interviewTime: '2026-07-18T14:30:00',
    method: 'ONLINE',
    methodText: '线上面试',
    location: '腾讯会议',
    status: 'SCHEDULED',
    statusText: '待面试',
  },
]

export const demoMyOffers: CandidateOffer[] = []
export const demoMyOnboardings: CandidateOnboarding[] = []

export const demoMyProfile: CandidateProfile = {
  id: 1,
  name: '当前候选人',
  gender: null,
  age: null,
  phone: '138****0001',
  email: 'candidate@example.com',
  education: '本科',
  school: '示例大学',
  major: '软件工程',
  yearsOfExperience: 3,
  currentStatus: 'OPEN_TO_WORK',
  source: 'SELF_REGISTER',
  hasAccount: true,
  createdAt: '2026-07-01T09:00:00',
  updatedAt: '2026-07-15T09:00:00',
  resumes: demoMyResumes,
}

export const demoInterviewerTasks: PortalInterview[] = [
  {
    id: 501,
    applicationId: 301,
    jobId: 101,
    jobTitle: 'Java 后端工程师',
    candidateId: 1,
    candidateName: '陈昕',
    interviewerId: 2,
    interviewerName: '当前面试官',
    round: 'FIRST',
    roundText: '技术一面',
    interviewTime: '2026-07-18T14:30:00',
    method: 'ONLINE',
    methodText: '线上面试',
    location: '腾讯会议',
    status: 'SCHEDULED',
    statusText: '待面试',
  },
  {
    id: 502,
    applicationId: 302,
    jobId: 102,
    jobTitle: '高级产品设计师',
    candidateId: 2,
    candidateName: '林悦',
    interviewerId: 2,
    interviewerName: '当前面试官',
    round: 'SECOND',
    roundText: '专业二面',
    interviewTime: '2026-07-16T10:00:00',
    method: 'OFFLINE',
    methodText: '线下面试',
    location: '上海办公室 3A',
    status: 'COMPLETED',
    statusText: '待提交反馈',
  },
]
