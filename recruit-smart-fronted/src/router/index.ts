import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import { useSessionStore } from '@/stores/session'
import type { UserRole } from '@/types/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: {
      public: true,
      title: '登录',
    },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: {
      public: true,
      title: '候选人注册',
    },
  },
  {
    path: '/',
    // 登录后的主框架承载按角色隔离的桌面端招聘工作流。
    component: () => import('@/views/layout/AppShell.vue'),
    meta: {
      roles: ['ADMIN', 'HR', 'INTERVIEWER', 'CANDIDATE'] satisfies UserRole[],
    },
    children: [
      {
        path: '',
        name: 'dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: {
          title: '工作台',
          roles: ['ADMIN', 'HR', 'INTERVIEWER', 'CANDIDATE'] satisfies UserRole[],
        },
      },
      {
        path: 'candidates',
        name: 'candidates',
        component: () => import('@/views/candidates/CandidatesView.vue'),
        meta: {
          title: '候选人',
          roles: ['HR'] satisfies UserRole[],
        },
      },
      {
        path: 'pipeline',
        name: 'pipeline',
        component: () => import('@/views/pipeline/PipelineView.vue'),
        meta: {
          title: '招聘流程',
          roles: ['HR', 'INTERVIEWER'] satisfies UserRole[],
        },
      },
      {
        path: 'interviews',
        name: 'interviews',
        component: () => import('@/views/interviews/InterviewsView.vue'),
        meta: {
          title: '面试任务',
          roles: ['HR', 'INTERVIEWER'] satisfies UserRole[],
        },
      },
      {
        path: 'ai-approvals',
        name: 'ai-approvals',
        component: () => import('@/views/ai/AiApprovalsView.vue'),
        meta: {
          title: 'AI 审批',
          roles: ['ADMIN', 'HR'] satisfies UserRole[],
        },
      },
    ],
  },
]

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to) => {
  const session = useSessionStore()

  if (to.meta.public) {
    return true
  }

  if (!session.isAuthenticated) {
    return {
      name: 'login',
      query: {
        redirect: to.fullPath,
      },
    }
  }

  // 前端角色检查只改善体验；后端服务仍必须执行真实鉴权。
  const allowedRoles = to.meta.roles as UserRole[] | undefined
  if (allowedRoles && !allowedRoles.includes(session.currentRole)) {
    return { name: 'dashboard' }
  }

  return true
})
