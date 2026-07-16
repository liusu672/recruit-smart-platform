<script setup lang="ts">
import { ShieldX } from 'lucide-vue-next'
import { useRouter } from 'vue-router'

import { getRoleHomePath } from '@/config/roleAccess'
import { useSessionStore } from '@/stores/session'

const router = useRouter()
const session = useSessionStore()

function returnHome() {
  void router.replace(getRoleHomePath(session.currentRole))
}
</script>

<template>
  <main class="system-state">
    <ShieldX :size="36" :stroke-width="1.6" aria-hidden="true" />
    <p class="system-state__code">403</p>
    <h1>当前角色无权访问此页面</h1>
    <p>页面入口与数据范围按角色隔离。如需额外权限，请联系系统管理员。</p>
    <el-button type="primary" @click="returnHome">返回我的工作台</el-button>
  </main>
</template>

<style scoped lang="scss">
.system-state {
  display: grid;
  width: 520px;
  margin: 18vh auto 0;
  justify-items: center;
  gap: var(--rs-space-3);
  padding: var(--rs-space-8);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-md);
  background: var(--rs-surface-primary);
  text-align: center;
  color: var(--rs-text-secondary);
}
.system-state svg,
.system-state__code {
  color: var(--rs-blue-700);
}
.system-state__code {
  margin: 0;
  font-weight: 700;
  letter-spacing: 0.08em;
}
.system-state h1 {
  margin: 0;
  color: var(--rs-text-primary);
  font-size: 22px;
}
.system-state p {
  max-width: 420px;
  margin: 0;
}
</style>
