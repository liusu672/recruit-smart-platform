<script setup lang="ts">
import {
  Bot,
  BriefcaseBusiness,
  CalendarCheck,
  LayoutDashboard,
  LogOut,
  Search,
  UsersRound,
} from 'lucide-vue-next'
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'

import { useSessionStore } from '@/stores/session'
import type { UserRole } from '@/types/auth'

const route = useRoute()
const router = useRouter()
const session = useSessionStore()

const navItems = [
  {
    label: '工作台',
    to: '/',
    icon: LayoutDashboard,
    roles: ['ADMIN', 'HR', 'INTERVIEWER', 'CANDIDATE'] satisfies UserRole[],
  },
  { label: '候选人', to: '/candidates', icon: UsersRound, roles: ['HR'] satisfies UserRole[] },
  {
    label: '招聘流程',
    to: '/pipeline',
    icon: BriefcaseBusiness,
    roles: ['HR', 'INTERVIEWER'] satisfies UserRole[],
  },
  {
    label: '面试任务',
    to: '/interviews',
    icon: CalendarCheck,
    roles: ['HR', 'INTERVIEWER'] satisfies UserRole[],
  },
  {
    label: 'AI 审批',
    to: '/ai-approvals',
    icon: Bot,
    roles: ['ADMIN', 'HR'] satisfies UserRole[],
  },
]

const title = computed(() => route.meta.title ?? 'Recruit Smart')
// 侧栏只展示当前角色可访问的入口，路由守卫仍负责最终前端拦截。
const visibleNavItems = computed(() =>
  navItems.filter((item) => item.roles.includes(session.currentRole)),
)

function logout() {
  session.clearSession()
  void router.push({ name: 'login' })
}
</script>

<template>
  <div class="app-shell">
    <aside class="app-sidebar" aria-label="主导航">
      <div class="app-sidebar__brand">
        <span class="app-sidebar__mark">RS</span>
        <div>
          <strong>Recruit Smart</strong>
          <span>招聘工作台</span>
        </div>
      </div>

      <nav class="app-sidebar__nav">
        <RouterLink
          v-for="item in visibleNavItems"
          :key="item.to"
          :to="item.to"
          class="app-sidebar__link"
        >
          <component :is="item.icon" :size="18" :stroke-width="1.75" aria-hidden="true" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>
    </aside>

    <section class="app-main">
      <header class="app-topbar">
        <div>
          <p class="app-topbar__eyebrow">Recruit Smart</p>
          <h1 class="rs-page-title">{{ title }}</h1>
        </div>

        <div class="app-topbar__actions">
          <label class="app-search" aria-label="全局搜索">
            <Search :size="16" :stroke-width="1.75" aria-hidden="true" />
            <input placeholder="搜索候选人、职位或面试" />
          </label>
          <div class="app-topbar__user">
            <span>{{ session.user?.name ?? 'HR 用户' }}</span>
            <small>{{ session.currentRole }}</small>
          </div>
          <button class="app-icon-button" type="button" aria-label="退出登录" @click="logout">
            <LogOut :size="18" :stroke-width="1.75" aria-hidden="true" />
          </button>
        </div>
      </header>

      <main class="app-content">
        <RouterView />
      </main>
    </section>
  </div>
</template>

<style scoped lang="scss">
.app-shell {
  display: grid;
  min-height: 100dvh;
  grid-template-columns: var(--rs-sidebar-width) 1fr;
  background: var(--rs-surface-canvas);
}

.app-sidebar {
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--rs-border-default);
  background: var(--rs-surface-primary);
}

.app-sidebar__brand {
  display: flex;
  align-items: center;
  gap: var(--rs-space-3);
  height: var(--rs-topbar-height);
  padding: 0 var(--rs-space-4);
  border-bottom: 1px solid var(--rs-border-default);
}

.app-sidebar__brand strong,
.app-sidebar__brand span {
  display: block;
}

.app-sidebar__brand strong {
  color: var(--rs-text-primary);
  font-size: 14px;
  font-weight: 700;
}

.app-sidebar__brand span {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}

.app-sidebar__mark {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-action-primary);
  color: var(--rs-white);
  font-weight: 700;
}

.app-sidebar__nav {
  display: grid;
  gap: var(--rs-space-1);
  padding: var(--rs-space-3);
}

.app-sidebar__link {
  display: flex;
  align-items: center;
  gap: var(--rs-space-2);
  height: 36px;
  padding: 0 var(--rs-space-3);
  border-radius: var(--rs-radius-sm);
  color: var(--rs-text-secondary);
  font-weight: 600;
}

.app-sidebar__link:hover {
  background: var(--rs-surface-muted);
  color: var(--rs-text-primary);
}

.app-sidebar__link.router-link-active {
  background: var(--rs-surface-selected);
  color: var(--rs-blue-700);
}

.app-main {
  min-width: 0;
}

.app-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: var(--rs-topbar-height);
  padding: 0 var(--rs-space-6);
  border-bottom: 1px solid var(--rs-border-default);
  background: var(--rs-surface-primary);
}

.app-topbar__eyebrow {
  margin: 0;
  color: var(--rs-text-tertiary);
  font-size: 12px;
  line-height: 1.35;
}

.app-topbar__actions {
  display: flex;
  align-items: center;
  gap: var(--rs-space-3);
}

.app-search {
  display: flex;
  align-items: center;
  gap: var(--rs-space-2);
  width: 320px;
  height: 36px;
  padding: 0 var(--rs-space-3);
  border: 1px solid var(--rs-border-strong);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
  color: var(--rs-text-tertiary);
}

.app-search input {
  width: 100%;
  border: 0;
  outline: 0;
  background: transparent;
  color: var(--rs-text-primary);
}

.app-search input::placeholder {
  color: var(--rs-text-tertiary);
}

.app-topbar__user {
  display: grid;
  gap: 0;
  min-width: 96px;
  color: var(--rs-text-secondary);
  font-size: 12px;
  text-align: right;
}

.app-topbar__user span {
  color: var(--rs-text-primary);
  font-weight: 600;
}

.app-icon-button {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border: 1px solid var(--rs-border-strong);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
  color: var(--rs-text-secondary);
  cursor: pointer;
}

.app-icon-button:hover {
  background: var(--rs-surface-muted);
  color: var(--rs-text-primary);
}

.app-content {
  max-width: 1600px;
  padding: var(--rs-space-6);
}
</style>
