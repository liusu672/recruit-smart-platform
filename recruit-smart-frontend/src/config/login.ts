import {
  BriefcaseBusiness,
  ClipboardCheck,
  CodeXml,
  Layers3,
  Shield,
  UsersRound,
} from 'lucide-vue-next'

import type { LoginRoleCopy, ManagementRole } from '@/types/login'

// 登录页文案集中管理，切换角色时只改变配置，不重复编写模板。
export const loginRoleCopy: Record<ManagementRole, LoginRoleCopy> = {
  HR: {
    heroKicker: 'HR 工作台',
    heroTitle: '统筹招聘全流程协作',
    heroDesc: '在统一工作台管理职位、候选人、面试和 Offer，AI 只提供参考，最终推进由 HR 确认。',
    formTitle: 'HR 登录',
    formDesc: '处理候选人筛选、面试安排、Offer 和入职协同。',
    username: 'hr01',
    password: '123456',
  },
  INTERVIEWER: {
    heroKicker: '面试官工作台',
    heroTitle: '集中处理面试反馈',
    heroDesc: '查看候选人经历、职位要求和建议问题，完成结构化评价，让面试结论可追踪。',
    formTitle: '面试官登录',
    formDesc: '进入面试工作台，提交结构化评分和反馈依据。',
    username: 'interviewer01',
    password: '123456',
  },
  ADMIN: {
    heroKicker: '系统管理',
    heroTitle: '配置权限与流程规范',
    heroDesc: '维护账号权限、组织配置和审计记录，保障招聘流程安全、清晰、可回溯。',
    formTitle: '管理员登录',
    formDesc: '维护账号权限、组织配置、审计记录和系统安全。',
    username: 'admin',
    password: '123456',
  },
}

export const roleCards = [
  {
    role: 'HR' as const,
    title: 'HR',
    desc: '筛选候选人，协调面试与录用流程',
    feature: '招聘全流程管理',
    icon: UsersRound,
  },
  {
    role: 'INTERVIEWER' as const,
    title: '面试官',
    desc: '查看面试任务，提交结构化评价',
    feature: '专注面试与反馈',
    icon: ClipboardCheck,
  },
  {
    role: 'ADMIN' as const,
    title: '管理员',
    desc: '维护账号权限与系统安全',
    feature: '组织权限与治理',
    icon: Shield,
  },
]

export const candidateJobs = [
  {
    title: '高级产品设计师',
    meta: '上海 · 混合办公',
    status: '匹配度高',
    tone: 'info',
    icon: Layers3,
  },
  {
    title: 'Java 后端工程师',
    meta: '杭州 · 全职',
    status: '新推荐',
    tone: 'info',
    icon: CodeXml,
  },
  {
    title: '招聘运营负责人',
    meta: '北京 · 远程办公',
    status: '可投递',
    tone: 'success',
    icon: BriefcaseBusiness,
  },
]
