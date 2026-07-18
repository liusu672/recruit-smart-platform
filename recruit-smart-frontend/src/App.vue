<script setup lang="ts">
import { onMounted } from 'vue'

import { toAuthUser } from '@/api/auth'
import { getCurrentUser } from '@/api/user'
import { useSessionStore } from '@/stores/session'

const session = useSessionStore()

onMounted(async () => {
  if (!session.token) return
  try {
    const currentUser = await getCurrentUser()
    session.updateUser(toAuthUser(currentUser))
  } catch {
    // 401 由统一 Axios 拦截器清理会话；页面无需重复弹出错误。
  }
})
</script>

<template>
  <RouterView />
</template>
