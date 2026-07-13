import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { login } from '@/api/auth'
import { loginRoleCopy } from '@/config/login'
import { useSessionStore } from '@/stores/session'
import { ApiError } from '@/types/api'
import type { ManagementRole, CandidateMode, LoginFormState, LoginSurface } from '@/types/login'

export function useLogin() {
  const route = useRoute()
  const router = useRouter()
  const session = useSessionStore()

  const activeSurface = ref<LoginSurface>('candidate')
  const candidateMode = ref<CandidateMode>('password')
  const activeRole = ref<ManagementRole>('HR')
  const isSubmitting = ref(false)
  const formError = ref('')
  const fieldErrors = reactive<Record<string, string>>({})

  const candidateForm = reactive<LoginFormState>({ username: '', password: '', code: '' })
  const managementForm = reactive<LoginFormState>({ username: '', password: '', code: '' })

  const redirect = computed(() => String(route.query.redirect ?? '/'))
  const isCandidate = computed(() => activeSurface.value === 'candidate')
  const activeForm = computed(() => (isCandidate.value ? candidateForm : managementForm))
  const activeRoleCopy = computed(() => loginRoleCopy[activeRole.value])
  const submitLabel = computed(() => {
    if (isSubmitting.value) return '登录中'
    return isCandidate.value ? '登录候选人中心' : '登录管理端'
  })

  function clearErrors() {
    formError.value = ''
    Object.keys(fieldErrors).forEach((key) => delete fieldErrors[key])
  }

  function switchSurface(surface: LoginSurface) {
    activeSurface.value = surface
    clearErrors()
  }

  function switchRole(role: ManagementRole) {
    activeRole.value = role
    switchSurface('management')
  }

  function fillDemo() {
    clearErrors()

    if (isCandidate.value) {
      candidateForm.username = 'candidate01'
      candidateForm.password = '123456'
      candidateForm.code = '246810'
      return
    }

    managementForm.username = activeRoleCopy.value.username
    managementForm.password = activeRoleCopy.value.password
  }

  function validateForm() {
    clearErrors()
    const form = activeForm.value

    if (!form.username.trim()) {
      fieldErrors.username = isCandidate.value ? '请输入手机号、邮箱或账号。' : '请输入企业账号。'
    }

    if (isCandidate.value && candidateMode.value === 'code') {
      fieldErrors.code = '当前后端登录接口只支持账号密码，请切换到账号密码登录。'
    } else if (!form.password.trim()) {
      fieldErrors.password = '请输入密码。'
    }

    return Object.keys(fieldErrors).length === 0
  }

  function normalizeError(error: unknown) {
    if (error instanceof ApiError) return error.message
    if (error instanceof Error) return error.message
    return '登录失败，请稍后重试。'
  }

  async function submitLogin() {
    if (!validateForm() || isSubmitting.value) return

    isSubmitting.value = true
    formError.value = ''

    try {
      const payload = await login({
        username: activeForm.value.username.trim(),
        password: activeForm.value.password,
      })

      session.setSession(payload)
      await router.push(redirect.value)
    } catch (error) {
      formError.value = normalizeError(error)
    } finally {
      isSubmitting.value = false
    }
  }

  return {
    activeRole,
    activeRoleCopy,
    activeSurface,
    candidateForm,
    candidateMode,
    fieldErrors,
    formError,
    isCandidate,
    isSubmitting,
    managementForm,
    submitLabel,
    fillDemo,
    submitLogin,
    switchRole,
    switchSurface,
  }
}
