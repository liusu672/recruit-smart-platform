import {
  Activity,
  Bot,
  BriefcaseBusiness,
  CalendarCheck,
  ClipboardList,
  ContactRound,
  FileClock,
  FileSignature,
  Files,
  LayoutDashboard,
  ListChecks,
  MessageCircle,
  ScrollText,
  Settings,
  ShieldCheck,
  UserRound,
  UserRoundCheck,
  UsersRound,
} from 'lucide-vue-next'
import type { Component } from 'vue'

import type { UserRole } from '@/types/auth'

export interface RoleNavItem {
  label: string
  to: string
  icon: Component
  group?: string
}

export interface RoleWorkspaceConfig {
  label: string
  workspaceLabel: string
  homePath: string
  searchPlaceholder: string
  navItems: RoleNavItem[]
}

// 路由、侧栏和登录落点共用同一份角色配置，避免三处权限表逐渐不一致。
export const ROLE_WORKSPACES: Record<UserRole, RoleWorkspaceConfig> = {
  HR: {
    label: 'HR',
    workspaceLabel: '招聘管理工作台',
    homePath: '/hr/dashboard',
    searchPlaceholder: '搜索职位、候选人或面试',
    navItems: [
      { label: '工作台', to: '/hr/dashboard', icon: LayoutDashboard, group: '概览' },
      { label: '职位管理', to: '/hr/jobs', icon: ClipboardList, group: '招聘管理' },
      { label: '候选人', to: '/hr/candidates', icon: UsersRound, group: '招聘管理' },
      { label: '招聘流程', to: '/hr/pipeline', icon: BriefcaseBusiness, group: '招聘管理' },
      { label: '面试安排', to: '/hr/interviews', icon: CalendarCheck, group: '招聘管理' },
      { label: 'Offer 管理', to: '/hr/offers', icon: FileSignature, group: '招聘管理' },
      { label: '入职办理', to: '/hr/onboardings', icon: UserRoundCheck, group: '员工管理' },
      { label: '员工档案', to: '/hr/employees', icon: ContactRound, group: '员工管理' },
      { label: 'AI 助手', to: '/hr/ai-assistant', icon: Bot, group: '系统管理' },
      { label: '消息中心', to: '/hr/messages', icon: MessageCircle, group: '系统管理' },
      { label: '账户与安全', to: '/hr/settings', icon: Settings, group: '系统管理' },
    ],
  },
  INTERVIEWER: {
    label: '面试官',
    workspaceLabel: '面试任务工作台',
    homePath: '/interviewer/dashboard',
    searchPlaceholder: '搜索我的面试任务',
    navItems: [
      { label: '工作台', to: '/interviewer/dashboard', icon: LayoutDashboard, group: '面试任务' },
      { label: '我的面试', to: '/interviewer/interviews', icon: CalendarCheck, group: '面试任务' },
      { label: '消息中心', to: '/interviewer/messages', icon: MessageCircle, group: '沟通与账户' },
      { label: '账户与安全', to: '/interviewer/settings', icon: Settings, group: '沟通与账户' },
    ],
  },
  ADMIN: {
    label: '系统管理员',
    workspaceLabel: '系统治理中心',
    homePath: '/admin/dashboard',
    searchPlaceholder: '搜索用户、角色或审计记录',
    navItems: [
      { label: '治理概览', to: '/admin/dashboard', icon: ShieldCheck, group: '概览' },
      { label: '用户管理', to: '/admin/users', icon: UsersRound, group: '账号与权限' },
      { label: '角色权限', to: '/admin/roles', icon: UserRoundCheck, group: '账号与权限' },
      { label: '基础字典', to: '/admin/dictionaries', icon: Settings, group: '系统治理' },
      { label: '审计日志', to: '/admin/audit', icon: ScrollText, group: '系统治理' },
      { label: '系统状态', to: '/admin/system', icon: Activity, group: '系统治理' },
      { label: '消息中心', to: '/admin/messages', icon: MessageCircle, group: '沟通与账户' },
      { label: '账户与安全', to: '/admin/settings', icon: Settings, group: '沟通与账户' },
    ],
  },
  CANDIDATE: {
    label: '候选人',
    workspaceLabel: '候选人中心',
    homePath: '/candidate/home',
    searchPlaceholder: '搜索招聘职位或我的投递',
    navItems: [
      { label: '求职首页', to: '/candidate/home', icon: LayoutDashboard, group: '求职流程' },
      { label: '招聘职位', to: '/candidate/jobs', icon: BriefcaseBusiness, group: '求职流程' },
      { label: '我的投递', to: '/candidate/applications', icon: ListChecks, group: '求职流程' },
      { label: '我的面试', to: '/candidate/interviews', icon: CalendarCheck, group: '求职流程' },
      { label: '我的 Offer', to: '/candidate/offers', icon: FileSignature, group: '求职流程' },
      { label: '入职资料', to: '/candidate/onboarding', icon: FileClock, group: '求职流程' },
      { label: '我的简历', to: '/candidate/resumes', icon: Files, group: '个人资料' },
      { label: '个人中心', to: '/candidate/profile', icon: UserRound, group: '个人资料' },
      { label: '消息中心', to: '/candidate/messages', icon: MessageCircle, group: '账户' },
      { label: '账户与安全', to: '/candidate/settings', icon: Settings, group: '账户' },
    ],
  },
}

export function getRoleHomePath(role: UserRole | null): string {
  return role ? ROLE_WORKSPACES[role].homePath : '/login'
}

export function canRoleAccess(allowedRoles: UserRole[] | undefined, role: UserRole | null) {
  return Boolean(role && (!allowedRoles || allowedRoles.includes(role)))
}
