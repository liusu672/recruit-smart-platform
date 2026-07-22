import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import { canRoleAccess, getRoleHomePath } from '@/config/roleAccess'
import { useSessionStore } from '@/stores/session'
import type { UserRole } from '@/types/auth'

const allRoles = ['ADMIN', 'HR', 'INTERVIEWER', 'CANDIDATE'] satisfies UserRole[]

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { public: true, title: '登录' },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { public: true, title: '候选人注册' },
  },
  {
    path: '/',
    name: 'role-entry',
    redirect: () => getRoleHomePath(useSessionStore().currentRole),
    meta: { roles: allRoles },
  },
  {
    path: '/candidate',
    component: () => import('@/views/layout/CandidateShell.vue'),
    meta: { roles: ['CANDIDATE'] },
    children: [
      { path: '', redirect: '/candidate/home' },
      {
        path: 'home',
        name: 'candidate-home',
        component: () => import('@/views/candidate/CandidateHomeView.vue'),
        meta: { title: '求职首页', roles: ['CANDIDATE'] },
      },
      {
        path: 'jobs',
        name: 'candidate-jobs',
        component: () => import('@/views/candidate/CandidateJobsView.vue'),
        meta: { title: '招聘职位', roles: ['CANDIDATE'] },
      },
      {
        path: 'resumes',
        name: 'candidate-resumes',
        component: () => import('@/views/candidate/CandidateResumesView.vue'),
        meta: { title: '我的简历', roles: ['CANDIDATE'] },
      },
      {
        path: 'applications',
        name: 'candidate-applications',
        component: () => import('@/views/candidate/CandidateApplicationsView.vue'),
        meta: { title: '我的投递', roles: ['CANDIDATE'] },
      },
      {
        path: 'interviews',
        name: 'candidate-interviews',
        component: () => import('@/views/candidate/CandidateInterviewsView.vue'),
        meta: { title: '我的面试', roles: ['CANDIDATE'] },
      },
      {
        path: 'offers',
        name: 'candidate-offers',
        component: () => import('@/views/candidate/CandidateOffersView.vue'),
        meta: { title: '我的 Offer', roles: ['CANDIDATE'] },
      },
      {
        path: 'onboarding',
        name: 'candidate-onboarding',
        component: () => import('@/views/candidate/CandidateOnboardingView.vue'),
        meta: { title: '入职资料', roles: ['CANDIDATE'] },
      },
      {
        path: 'profile',
        name: 'candidate-profile',
        component: () => import('@/views/candidate/CandidateProfileView.vue'),
        meta: { title: '个人中心', roles: ['CANDIDATE'] },
      },
      {
        path: 'messages',
        name: 'candidate-messages',
        component: () => import('@/views/messages/MessagesView.vue'),
        meta: { title: '消息中心', roles: ['CANDIDATE'] },
      },
      {
        path: 'settings',
        name: 'candidate-settings',
        component: () => import('@/views/settings/CandidateSettingsView.vue'),
        meta: { title: '账户与安全', roles: ['CANDIDATE'] },
      },
    ],
  },
  {
    path: '/hr',
    component: () => import('@/views/layout/HrShell.vue'),
    meta: { roles: ['HR'] },
    children: [
      { path: '', redirect: '/hr/dashboard' },
      {
        path: 'dashboard',
        name: 'hr-dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '招聘工作台', roles: ['HR'] },
      },
      {
        path: 'jobs',
        name: 'hr-jobs',
        component: () => import('@/views/jobs/JobsView.vue'),
        meta: { title: '职位管理', roles: ['HR'] },
      },
      {
        path: 'candidates',
        name: 'hr-candidates',
        component: () => import('@/views/candidates/CandidatesView.vue'),
        meta: { title: '候选人', roles: ['HR'] },
      },
      {
        path: 'pipeline',
        name: 'hr-pipeline',
        component: () => import('@/views/pipeline/PipelineView.vue'),
        meta: { title: '招聘流程', roles: ['HR'] },
      },
      {
        path: 'interviews',
        name: 'hr-interviews',
        component: () => import('@/views/interviews/InterviewsView.vue'),
        meta: { title: '面试安排', roles: ['HR'] },
      },
      {
        path: 'offers',
        name: 'hr-offers',
        component: () => import('@/views/offers/OffersView.vue'),
        meta: { title: 'Offer 管理', roles: ['HR'] },
      },
      {
        path: 'onboardings',
        name: 'hr-onboardings',
        component: () => import('@/views/onboardings/OnboardingsView.vue'),
        meta: { title: '入职办理', roles: ['HR'] },
      },
      {
        path: 'employees',
        name: 'hr-employees',
        component: () => import('@/views/employees/EmployeesView.vue'),
        meta: { title: '员工档案', roles: ['HR'] },
      },
      {
        path: 'ai-approvals',
        name: 'hr-ai-approvals',
        component: () => import('@/views/ai/AiApprovalsView.vue'),
        meta: { title: 'AI 辅助审批', roles: ['HR'] },
      },
      {
        path: 'ai-assistant',
        name: 'hr-ai-assistant',
        component: () => import('@/views/ai/HrAiAssistantView.vue'),
        meta: { title: 'HR AI 助手', roles: ['HR'] },
      },
      {
        path: 'messages',
        name: 'hr-messages',
        component: () => import('@/views/messages/MessagesView.vue'),
        meta: { title: '消息中心', roles: ['HR'] },
      },
      {
        path: 'settings',
        name: 'hr-settings',
        component: () => import('@/views/settings/AccountSettingsView.vue'),
        meta: { title: '账户与安全', roles: ['HR'] },
      },
    ],
  },
  {
    path: '/interviewer',
    component: () => import('@/views/layout/InterviewerShell.vue'),
    meta: { roles: ['INTERVIEWER'] },
    children: [
      { path: '', redirect: '/interviewer/dashboard' },
      {
        path: 'dashboard',
        name: 'interviewer-dashboard',
        component: () => import('@/views/interviewer/InterviewerDashboardView.vue'),
        meta: { title: '面试工作台', roles: ['INTERVIEWER'] },
      },
      {
        path: 'interviews',
        name: 'interviewer-interviews',
        component: () => import('@/views/interviewer/InterviewerInterviewsView.vue'),
        meta: { title: '我的面试', roles: ['INTERVIEWER'] },
      },
      {
        path: 'messages',
        name: 'interviewer-messages',
        component: () => import('@/views/interviewer/InterviewerMessagesView.vue'),
        meta: { title: '消息中心', roles: ['INTERVIEWER'] },
      },
      {
        path: 'settings',
        name: 'interviewer-settings',
        component: () => import('@/views/interviewer/InterviewerSettingsView.vue'),
        meta: { title: '账户与安全', roles: ['INTERVIEWER'] },
      },
    ],
  },
  {
    path: '/admin',
    component: () => import('@/views/layout/ManagementShell.vue'),
    meta: { roles: ['ADMIN'] },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      {
        path: 'dashboard',
        name: 'admin-dashboard',
        component: () => import('@/views/admin/AdminDashboardView.vue'),
        meta: { title: '治理概览', roles: ['ADMIN'] },
      },
      {
        path: 'users',
        name: 'admin-users',
        component: () => import('@/views/admin/AdminUsersView.vue'),
        meta: { title: '用户管理', roles: ['ADMIN'] },
      },
      {
        path: 'roles',
        name: 'admin-roles',
        component: () => import('@/views/admin/AdminPendingView.vue'),
        props: { feature: '角色权限', description: '维护角色与功能权限关系。' },
        meta: { title: '角色权限', roles: ['ADMIN'] },
      },
      {
        path: 'dictionaries',
        name: 'admin-dictionaries',
        component: () => import('@/views/admin/AdminPendingView.vue'),
        props: { feature: '基础字典', description: '维护面试方式、拒绝原因等系统字典。' },
        meta: { title: '基础字典', roles: ['ADMIN'] },
      },
      {
        path: 'audit',
        name: 'admin-audit',
        component: () => import('@/views/admin/AdminPendingView.vue'),
        props: { feature: '审计日志', description: '查看关键账号和配置变更记录。' },
        meta: { title: '审计日志', roles: ['ADMIN'] },
      },
      {
        path: 'system',
        name: 'admin-system',
        component: () => import('@/views/admin/AdminPendingView.vue'),
        props: { feature: '系统状态', description: '查看网关与业务服务运行状态。' },
        meta: { title: '系统状态', roles: ['ADMIN'] },
      },
      {
        path: 'messages',
        name: 'admin-messages',
        component: () => import('@/views/messages/MessagesView.vue'),
        meta: { title: '消息中心', roles: ['ADMIN'] },
      },
      {
        path: 'settings',
        name: 'admin-settings',
        component: () => import('@/views/settings/AccountSettingsView.vue'),
        meta: { title: '账户与安全', roles: ['ADMIN'] },
      },
    ],
  },
  { path: '/jobs', redirect: '/hr/jobs' },
  { path: '/candidates', redirect: '/hr/candidates' },
  { path: '/pipeline', redirect: '/hr/pipeline' },
  { path: '/interviews', redirect: '/hr/interviews' },
  { path: '/offers', redirect: '/hr/offers' },
  { path: '/onboardings', redirect: '/hr/onboardings' },
  { path: '/employees', redirect: '/hr/employees' },
  { path: '/ai-approvals', redirect: '/hr/ai-approvals' },
  {
    path: '/forbidden',
    name: 'forbidden',
    component: () => import('@/views/system/ForbiddenView.vue'),
    meta: { title: '无权访问', roles: allRoles },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/system/NotFoundView.vue'),
    meta: { public: true, title: '页面不存在' },
  },
]

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to) => {
  const session = useSessionStore()

  if (to.meta.public) return true
  if (!session.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  // 前端守卫防止页面闪现和误请求；真实数据权限仍必须由后端按角色与数据归属校验。
  if (!canRoleAccess(to.meta.roles, session.currentRole)) {
    return { name: 'forbidden', query: { from: to.fullPath } }
  }

  return true
})
