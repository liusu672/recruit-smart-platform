<script setup lang="ts">
import { LogOut, Search } from 'lucide-vue-next'
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'

import { ROLE_WORKSPACES } from '@/config/roleAccess'
import { useSessionStore } from '@/stores/session'

const route = useRoute()
const router = useRouter()
const session = useSessionStore()
const workspace = ROLE_WORKSPACES.CANDIDATE
const title = computed(() => route.meta.title ?? workspace.workspaceLabel)

function logout() {
  session.clearSession()
  void router.push({ name: 'login' })
}
</script>

<template>
  <div class="workspace-shell candidate-shell">
    <aside class="workspace-sidebar" aria-label="候选人中心主导航">
      <div class="workspace-brand">
        <span class="workspace-brand__mark">RS</span>
        <div><strong>Recruit Smart</strong><span>候选人中心</span></div>
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
          <p class="workspace-topbar__context">我的求职进度</p>
          <h1 class="rs-page-title">{{ title }}</h1>
        </div>
        <div class="workspace-topbar__actions">
          <label class="workspace-search" aria-label="候选人中心搜索">
            <Search :size="16" :stroke-width="1.75" aria-hidden="true" />
            <input :placeholder="workspace.searchPlaceholder" />
          </label>
          <div class="workspace-user">
            <strong>{{ session.user?.name ?? '候选人' }}</strong
            ><span>候选人</span>
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
