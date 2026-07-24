<script setup lang="ts">
import {
  ChevronDown,
  LogOut,
  MessageCircle,
  PanelLeftClose,
  PanelLeftOpen,
  Settings,
} from 'lucide-vue-next'
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'

import { provideWorkspacePageHeader } from '@/composables/useWorkspacePageHeader'
import { useMessageNotifications } from '@/composables/useMessages'
import { ROLE_WORKSPACES } from '@/config/roleAccess'
import { useSessionStore } from '@/stores/session'

const SIDEBAR_STORAGE_KEY = 'rs-hr-sidebar-collapsed'
const router = useRouter()
const route = useRoute()
const session = useSessionStore()
const { unreadQuery } = useMessageNotifications()
const pageHeader = provideWorkspacePageHeader()
const workspace = ROLE_WORKSPACES.HR
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
const userInitial = computed(() => (session.user?.name ?? 'HR').slice(0, 1))
const topbarTitle = computed(() => {
  if (pageHeader.title.value) return pageHeader.title.value
  return typeof route.meta.title === 'string' ? route.meta.title : ''
})
const topbarDescription = computed(() => pageHeader.description.value)

onMounted(() => document.documentElement.classList.add('rs-hr-workspace'))
onBeforeUnmount(() => document.documentElement.classList.remove('rs-hr-workspace'))

function toggleSidebar() {
  collapsed.value = !collapsed.value
  window.localStorage.setItem(SIDEBAR_STORAGE_KEY, String(collapsed.value))
}

function logout() {
  session.clearSession()
  void router.push({ name: 'login' })
}

function handleUserCommand(command: string) {
  if (command === 'messages') void router.push('/hr/messages')
  if (command === 'settings') void router.push('/hr/settings')
  if (command === 'logout') logout()
}
</script>

<template>
  <div class="workspace-shell hr-shell" :class="{ 'hr-shell--collapsed': collapsed }">
    <aside class="workspace-sidebar" aria-label="HR 工作区主导航">
      <div class="workspace-brand">
        <span class="workspace-brand__mark">RS</span>
        <div class="hr-brand__copy"><strong>Recruit Smart</strong><span>招聘管理工作台</span></div>
      </div>
      <nav class="workspace-nav">
        <section v-for="group in navigationGroups" :key="group.label" class="hr-nav-group">
          <p class="hr-nav-group__label">{{ group.label }}</p>
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
                class="hr-nav-unread"
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
        <div v-if="topbarTitle || topbarDescription" class="workspace-topbar__page-copy">
          <h1 v-if="topbarTitle">{{ topbarTitle }}</h1>
          <p v-if="topbarDescription">{{ topbarDescription }}</p>
        </div>
        <div class="workspace-topbar__actions">
          <RouterLink
            class="workspace-icon-button workspace-message-link"
            to="/hr/messages"
            aria-label="打开消息中心"
          >
            <MessageCircle :size="18" :stroke-width="1.75" aria-hidden="true" />
            <span v-if="unreadQuery.data.value" class="workspace-message-badge">
              {{ unreadQuery.data.value > 99 ? '99+' : unreadQuery.data.value }}
            </span>
          </RouterLink>
          <el-dropdown trigger="click" @command="handleUserCommand">
            <button class="hr-user-menu" type="button" aria-label="打开用户菜单">
              <span class="hr-user-menu__avatar">{{ userInitial }}</span>
              <span class="hr-user-menu__name">{{ session.user?.name ?? 'HR' }}</span>
              <ChevronDown :size="16" :stroke-width="1.75" aria-hidden="true" />
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="messages">
                  <MessageCircle :size="16" :stroke-width="1.75" />消息中心
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
