import { reactive, ref } from 'vue'

interface CandidateRegisterForm {
  realName: string
  username: string
  password: string
  confirmPassword: string
}

export function useCandidateRegister() {
  const form = reactive<CandidateRegisterForm>({
    realName: '',
    username: '',
    password: '',
    confirmPassword: '',
  })
  const fieldErrors = reactive<Record<string, string>>({})
  const formNotice = ref('')

  function clearFeedback() {
    formNotice.value = ''
    Object.keys(fieldErrors).forEach((key) => delete fieldErrors[key])
  }

  function validate() {
    clearFeedback()

    if (!form.realName.trim()) fieldErrors.realName = '请输入真实姓名。'
    if (!form.username.trim()) fieldErrors.username = '请输入手机号、邮箱或账号。'
    if (form.password.length < 6) fieldErrors.password = '密码至少需要 6 位。'
    if (form.confirmPassword !== form.password)
      fieldErrors.confirmPassword = '两次输入的密码不一致。'

    return Object.keys(fieldErrors).length === 0
  }

  function submitRegister() {
    if (!validate()) return

    // 当前后端只提供登录接口，先明确告知用户，不伪造注册成功状态。
    formNotice.value = '注册页面已就绪，候选人注册接口尚未接入，请联系管理员开通账号。'
  }

  return { fieldErrors, form, formNotice, submitRegister }
}
