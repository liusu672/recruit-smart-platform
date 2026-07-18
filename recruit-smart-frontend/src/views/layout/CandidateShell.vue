<script setup lang="ts">
import {
  ChevronDown,
  LogOut,
  MessageCircle,
  PanelLeftClose,
  PanelLeftOpen,
  Settings,
  UserRound,
} from 'lucide-vue-next'
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'

import { useMessageNotifications } from '@/composables/useMessages'
import { ROLE_WORKSPACES } from '@/config/roleAccess'
import { useSessionStore } from '@/stores/session'

const SIDEBAR_STORAGE_KEY = 'rs-candidate-sidebar-collapsed'
const router = useRouter()
const session = useSessionStore()
const { unreadQuery } = useMessageNotifications()
const workspace = ROLE_WORKSPACES.CANDIDATE
const storedPreference = window.localStorage.getItem(SIDEBAR_STORAGE_KEY)
const collapsed = ref(
  storedPreference === null ? window.innerWidth <= 1280 : storedPreference === 'true',
)

const navigationGroups = computed(() => {
  const groups = new Map<string, typeof workspace.navItems>()
  for (const item of workspace.navItems) {
    const group = item.group ?? '导航'
    groups.set(group, [...(groups.get(group) ?? []), item])
  }
  return [...groups.entries()].map(([label, items]) => ({ label, items }))
})
const userInitial = computed(() => (session.user?.name ?? '候选人').slice(0, 1))

onMounted(() => document.documentElement.classList.add('rs-candidate-workspace'))
onBeforeUnmount(() => document.documentElement.classList.remove('rs-candidate-workspace'))

function toggleSidebar() {
  collapsed.value = !collapsed.value
  window.localStorage.setItem(SIDEBAR_STORAGE_KEY, String(collapsed.value))
}

function logout() {
  session.clearSession()
  void router.push({ name: 'login' })
}

function handleUserCommand(command: string) {
  if (command === 'profile') void router.push('/candidate/profile')
  if (command === 'settings') void router.push('/candidate/settings')
  if (command === 'logout') logout()
}
</script>

<template>
  <div class="workspace-shell candidate-shell" :class="{ 'candidate-shell--collapsed': collapsed }">
    <aside class="workspace-sidebar" aria-label="候选人中心主导航">
      <div class="workspace-brand">
        <span class="workspace-brand__mark">RS</span>
        <div class="candidate-brand__copy">
          <strong>Recruit Smart</strong><span>候选人中心</span>
        </div>
      </div>
      <nav class="workspace-nav">
        <section v-for="group in navigationGroups" :key="group.label" class="candidate-nav-group">
          <p class="candidate-nav-group__label">{{ group.label }}</p>
          <el-tooltip
            v-for="item in group.items"
            :key="item.to"
            :content="item.label"
            placement="right"
            :disabled="!collapsed"
          >
            <RouterLink :to="item.to" class="workspace-nav__link" :aria-label="item.label">
              <component :is="item.icon" :size="18" :stroke-width="1.75" aria-hidden="true" />
              <span>{{ item.label }}</span>
              <span
                v-if="item.to.endsWith('/messages') && unreadQuery.data.value"
                class="candidate-nav-unread"
              >
                {{ unreadQuery.data.value > 99 ? '99+' : unreadQuery.data.value }}
              </span>
            </RouterLink>
          </el-tooltip>
        </section>
      </nav>
    </aside>
    <section class="workspace-main">
      <header class="workspace-topbar">
        <button
          class="workspace-icon-button"
          type="button"
          :aria-label="collapsed ? '展开侧边栏' : '收起侧边栏'"
          @click="toggleSidebar"
        >
          <PanelLeftOpen v-if="collapsed" :size="18" :stroke-width="1.75" />
          <PanelLeftClose v-else :size="18" :stroke-width="1.75" />
        </button>
        <div class="workspace-topbar__actions">
          <RouterLink
            class="workspace-icon-button workspace-message-link"
            to="/candidate/messages"
            aria-label="打开消息中心"
          >
            <MessageCircle :size="18" :stroke-width="1.75" aria-hidden="true" />
            <span v-if="unreadQuery.data.value" class="workspace-message-badge">
              {{ unreadQuery.data.value > 99 ? '99+' : unreadQuery.data.value }}
            </span>
          </RouterLink>
          <el-dropdown trigger="click" @command="handleUserCommand">
            <button class="candidate-user-menu" type="button" aria-label="打开用户菜单">
              <span class="candidate-user-menu__avatar">{{ userInitial }}</span>
              <span class="candidate-user-menu__name">{{ session.user?.name ?? '候选人' }}</span>
              <ChevronDown :size="16" :stroke-width="1.75" aria-hidden="true" />
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <UserRound :size="16" :stroke-width="1.75" />个人中心
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <Settings :size="16" :stroke-width="1.75" />账户与安全
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <LogOut :size="16" :stroke-width="1.75" />退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <main class="workspace-content"><RouterView /></main>
    </section>
  </div>
</template>
