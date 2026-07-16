import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

import type { AuthUser, LoginPayload, UserRole } from '@/types/auth'

const TOKEN_STORAGE_KEY = 'recruit-smart-token'
const TOKEN_TYPE_STORAGE_KEY = 'recruit-smart-token-type'
const USER_STORAGE_KEY = 'recruit-smart-user'

// 只恢复当前浏览器会话；长期登录策略应在后端认证方案明确后再扩展。
function readStoredUser(): AuthUser | null {
  const raw = sessionStorage.getItem(USER_STORAGE_KEY)
  if (!raw) return null

  try {
    return JSON.parse(raw) as AuthUser
  } catch {
    sessionStorage.removeItem(USER_STORAGE_KEY)
    return null
  }
}

export const useSessionStore = defineStore('session', () => {
  const token = ref<string | null>(sessionStorage.getItem(TOKEN_STORAGE_KEY))
  const tokenType = ref<string>(sessionStorage.getItem(TOKEN_TYPE_STORAGE_KEY) ?? 'Bearer')
  const user = ref<AuthUser | null>(readStoredUser())

  const isAuthenticated = computed(() => Boolean(token.value && user.value))
  // 缺失用户时不授予默认角色，避免异常会话在任何前端权限判断中被当作 HR。
  const currentRole = computed<UserRole | null>(() => user.value?.role ?? null)

  // token 与用户信息必须同步写入，避免路由守卫读到半登录状态。
  function setSession(payload: LoginPayload) {
    token.value = payload.token
    tokenType.value = payload.tokenType ?? 'Bearer'
    user.value = payload.user
    sessionStorage.setItem(TOKEN_STORAGE_KEY, payload.token)
    sessionStorage.setItem(TOKEN_TYPE_STORAGE_KEY, tokenType.value)
    sessionStorage.setItem(USER_STORAGE_KEY, JSON.stringify(payload.user))
  }

  function clearSession() {
    token.value = null
    tokenType.value = 'Bearer'
    user.value = null
    sessionStorage.removeItem(TOKEN_STORAGE_KEY)
    sessionStorage.removeItem(TOKEN_TYPE_STORAGE_KEY)
    sessionStorage.removeItem(USER_STORAGE_KEY)
  }

  return {
    token,
    tokenType,
    user,
    isAuthenticated,
    currentRole,
    setSession,
    clearSession,
  }
})
