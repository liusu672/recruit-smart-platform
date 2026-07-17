import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { register } from '@/api/auth'
import { useSessionStore } from '@/stores/session'
import { ApiError } from '@/types/api'

interface CandidateRegisterForm {
  realName: string
  username: string
  phone: string
  password: string
  confirmPassword: string
}

export function useCandidateRegister() {
  const router = useRouter()
  const session = useSessionStore()

  const form = reactive<CandidateRegisterForm>({
    realName: '',
    username: '',
    phone: '',
    password: '',
    confirmPassword: '',
  })
  const fieldErrors = reactive<Record<string, string>>({})
  const formError = ref('')
  const formNotice = ref('')
  const isSubmitting = ref(false)

  function clearFeedback() {
    formError.value = ''
    formNotice.value = ''
    Object.keys(fieldErrors).forEach((key) => delete fieldErrors[key])
  }

  function validate() {
    clearFeedback()

    if (!form.realName.trim()) fieldErrors.realName = '请输入真实姓名。'
    if (form.username.trim().length < 4 || form.username.trim().length > 32)
      fieldErrors.username = '登录账号需要 4-32 位。'
    if (!/^1[3-9]\d{9}$/.test(form.phone.trim())) fieldErrors.phone = '请输入有效的 11 位手机号。'
    if (form.password.length < 6 || form.password.length > 32)
      fieldErrors.password = '密码需要 6-32 位。'
    if (form.confirmPassword !== form.password)
      fieldErrors.confirmPassword = '两次输入的密码不一致。'

    return Object.keys(fieldErrors).length === 0
  }

  function normalizeError(error: unknown) {
    if (error instanceof ApiError) return error.message
    if (error instanceof Error) return error.message
    return '注册失败，请稍后重试。'
  }

  async function submitRegister() {
    if (!validate() || isSubmitting.value) return

    isSubmitting.value = true

    try {
      const payload = await register({
        username: form.username.trim(),
        password: form.password,
        confirmPassword: form.confirmPassword,
        name: form.realName.trim(),
        phone: form.phone.trim(),
      })

      session.setSession(payload)
      formNotice.value = '注册成功，正在进入候选人中心。'
      await router.push('/')
    } catch (error) {
      formError.value = normalizeError(error)
    } finally {
      isSubmitting.value = false
    }
  }

  return { fieldErrors, form, formError, formNotice, isSubmitting, submitRegister }
}
