<script setup lang="ts">
import { LogOut, Search } from 'lucide-vue-next'
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'

import { ROLE_WORKSPACES } from '@/config/roleAccess'
import { useSessionStore } from '@/stores/session'

const route = useRoute()
const router = useRouter()
const session = useSessionStore()

const workspace = computed(() => {
  const role = session.currentRole
  if (!role || role === 'CANDIDATE') return ROLE_WORKSPACES.HR
  return ROLE_WORKSPACES[role]
})
const title = computed(() => route.meta.title ?? workspace.value.workspaceLabel)

function logout() {
  session.clearSession()
  void router.push({ name: 'login' })
}
</script>

<template>
  <div class="workspace-shell management-shell">
    <aside class="workspace-sidebar" aria-label="管理端主导航">
      <div class="workspace-brand">
        <span class="workspace-brand__mark">RS</span>
        <div>
          <strong>Recruit Smart</strong><span>{{ workspace.workspaceLabel }}</span>
        </div>
      </div>
      <nav class="workspace-nav">
        <RouterLink
          v-for="item in workspace.navItems"
          :key="item.to"
          :to="item.to"
          class="workspace-nav__link"
        >
          <component :is="item.icon" :size="18" :stroke-width="1.75" aria-hidden="true" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>
    </aside>
    <section class="workspace-main">
      <header class="workspace-topbar">
        <div>
          <p class="workspace-topbar__context">{{ workspace.workspaceLabel }}</p>
          <h1 class="rs-page-title">{{ title }}</h1>
        </div>
        <div class="workspace-topbar__actions">
          <label class="workspace-search" aria-label="当前角色搜索">
            <Search :size="16" :stroke-width="1.75" aria-hidden="true" />
            <input :placeholder="workspace.searchPlaceholder" />
          </label>
          <div class="workspace-user">
            <strong>{{ session.user?.name ?? workspace.label }}</strong
            ><span>{{ workspace.label }}</span>
          </div>
          <button class="workspace-icon-button" type="button" aria-label="退出登录" @click="logout">
            <LogOut :size="18" :stroke-width="1.75" />
          </button>
        </div>
      </header>
      <main class="workspace-content"><RouterView /></main>
    </section>
  </div>
</template>
