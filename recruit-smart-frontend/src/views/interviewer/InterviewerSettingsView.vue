<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { KeyRound, LockKeyhole, ShieldCheck, UserRound } from 'lucide-vue-next'
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'

import InterviewerErrorState from '@/components/interviewer/InterviewerErrorState.vue'
import InterviewerPageHeader from '@/components/interviewer/InterviewerPageHeader.vue'
import { useUserSettings } from '@/composables/useUserSettings'
import type { PasswordUpdateRequest, UserProfileUpdateRequest } from '@/types/user'

const { userQuery, profileMutation, passwordMutation } = useUserSettings()
const profile = reactive<UserProfileUpdateRequest>({ realName: '', phone: '', email: '' })
const initialProfile = ref<UserProfileUpdateRequest>({ ...profile })
const password = reactive<PasswordUpdateRequest>({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})
const passwordDialogVisible = ref(false)
const lastSavedAt = ref('')

const profileDirty = computed(
  () => JSON.stringify(profile) !== JSON.stringify(initialProfile.value),
)
const passwordDirty = computed(() =>
  Boolean(password.oldPassword || password.newPassword || password.confirmPassword),
)
const passwordStrength = computed(() => {
  const value = password.newPassword
  if (!value) return { label: '尚未输入', level: 0 }
  let level = value.length >= 6 ? 1 : 0
  if (value.length >= 10 && /[A-Za-z]/.test(value) && /\d/.test(value)) level = 2
  if (
    value.length >= 12 &&
    /[A-Z]/.test(value) &&
    /[a-z]/.test(value) &&
    /\d/.test(value) &&
    /[^A-Za-z0-9]/.test(value)
  )
    level = 3
  return { label: ['不符合规则', '基础', '较强', '强'][level]!, level }
})

watch(
  () => userQuery.data.value,
  (user) => {
    if (!user) return
    const next = {
      realName: user.realName ?? '',
      phone: user.phone ?? '',
      email: user.email ?? '',
    }
    Object.assign(profile, next)
    initialProfile.value = next
  },
  { immediate: true },
)

function cancelProfileChanges() {
  Object.assign(profile, initialProfile.value)
}

async function saveProfile() {
  if (!profileDirty.value || profileMutation.isPending.value) return
  try {
    const payload = { ...profile }
    await profileMutation.mutateAsync(payload)
    initialProfile.value = payload
    lastSavedAt.value = new Date().toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
    })
    ElMessage.success('个人资料已更新')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '个人资料更新失败')
  }
}

function resetPassword() {
  Object.assign(password, { oldPassword: '', newPassword: '', confirmPassword: '' })
}

async function savePassword() {
  if (password.newPassword.length < 6 || password.newPassword.length > 32) {
    ElMessage.warning('新密码长度需要为 6-32 位')
    return
  }
  if (password.newPassword !== password.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  try {
    await passwordMutation.mutateAsync({ ...password })
    resetPassword()
    passwordDialogVisible.value = false
    ElMessage.success('密码已更新')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '密码更新失败')
  }
}

async function closePasswordDialog(done: () => void) {
  if (passwordDirty.value) {
    try {
      await ElMessageBox.confirm('当前密码修改尚未提交，确认关闭吗？', '放弃修改密码', {
        confirmButtonText: '确认关闭',
        cancelButtonText: '继续修改',
        type: 'warning',
      })
    } catch {
      return
    }
  }
  resetPassword()
  done()
}

function beforeUnload(event: BeforeUnloadEvent) {
  if (!profileDirty.value) return
  event.preventDefault()
  event.returnValue = ''
}

onMounted(() => window.addEventListener('beforeunload', beforeUnload))
onBeforeUnmount(() => window.removeEventListener('beforeunload', beforeUnload))
onBeforeRouteLeave(
  () => !profileDirty.value || window.confirm('你有未保存的个人资料更改，确认离开吗？'),
)
</script>

<template>
  <div class="interviewer-settings-view">
    <InterviewerPageHeader title="账户与安全" description="维护当前登录账号的联系方式和密码。" />

    <InterviewerErrorState
      v-if="userQuery.error.value"
      title="部分账户信息暂时无法加载"
      description="请重新加载，或联系系统管理员检查账户状态。"
      :loading="userQuery.isFetching.value"
      @retry="userQuery.refetch()"
    />

    <section class="interviewer-settings-section">
      <header>
        <span><UserRound :size="18" :stroke-width="1.75" /></span>
        <div>
          <h2>个人资料</h2>
          <p>只有真实姓名、手机号和邮箱可以在此修改。</p>
        </div>
      </header>
      <dl class="interviewer-account-facts">
        <div>
          <dt>登录账号</dt>
          <dd>{{ userQuery.data.value?.username ?? '加载中' }}</dd>
        </div>
        <div>
          <dt>系统角色</dt>
          <dd>{{ userQuery.data.value?.roleName || '面试官' }}</dd>
        </div>
      </dl>
      <el-form label-position="top" @submit.prevent="saveProfile">
        <div class="interviewer-settings-form-grid">
          <el-form-item label="真实姓名"
            ><el-input v-model="profile.realName" maxlength="64"
          /></el-form-item>
          <el-form-item label="手机号"
            ><el-input v-model="profile.phone" maxlength="11"
          /></el-form-item>
          <el-form-item label="邮箱"
            ><el-input v-model="profile.email" maxlength="128"
          /></el-form-item>
        </div>
      </el-form>
      <footer class="interviewer-settings-footer">
        <span v-if="profileDirty">你有未保存的更改</span>
        <span v-else-if="lastSavedAt">已于 {{ lastSavedAt }} 保存</span>
        <span v-else>修改后记得保存</span>
        <div>
          <el-button
            :disabled="!profileDirty || profileMutation.isPending.value"
            @click="cancelProfileChanges"
            >取消修改</el-button
          ><el-button
            type="primary"
            :loading="profileMutation.isPending.value"
            :disabled="!profileDirty"
            @click="saveProfile"
            >保存修改</el-button
          >
        </div>
      </footer>
    </section>

    <section class="interviewer-settings-section interviewer-settings-section--security">
      <header>
        <span><ShieldCheck :size="18" :stroke-width="1.75" /></span>
        <div>
          <h2>账号安全</h2>
          <p>定期更新密码，避免与其他网站使用相同密码。</p>
        </div>
      </header>
      <div class="interviewer-password-row">
        <span><LockKeyhole :size="18" :stroke-width="1.75" /></span>
        <div>
          <strong>登录密码</strong>
          <p>密码长度为 6-32 位，建议组合大小写字母、数字和符号。</p>
        </div>
        <el-button :icon="KeyRound" @click="passwordDialogVisible = true">修改密码</el-button>
      </div>
    </section>

    <el-dialog
      v-model="passwordDialogVisible"
      title="修改登录密码"
      width="480px"
      :before-close="closePasswordDialog"
      destroy-on-close
    >
      <el-form label-position="top" @submit.prevent="savePassword">
        <el-form-item label="原密码" required
          ><el-input
            v-model="password.oldPassword"
            type="password"
            show-password
            autocomplete="current-password"
        /></el-form-item>
        <el-form-item label="新密码" required
          ><el-input
            v-model="password.newPassword"
            type="password"
            show-password
            autocomplete="new-password"
        /></el-form-item>
        <div class="interviewer-password-strength">
          <span>输入强度参考：{{ passwordStrength.label }}</span>
          <div aria-hidden="true">
            <i
              v-for="level in 3"
              :key="level"
              :class="{ 'interviewer-password-strength__active': passwordStrength.level >= level }"
            />
          </div>
          <small>系统仅强制校验 6-32 位，强度提示不改变后端规则。</small>
        </div>
        <el-form-item label="确认新密码" required
          ><el-input
            v-model="password.confirmPassword"
            type="password"
            show-password
            autocomplete="new-password"
        /></el-form-item>
      </el-form>
      <template #footer
        ><el-button @click="closePasswordDialog(() => (passwordDialogVisible = false))"
          >取消</el-button
        ><el-button
          type="primary"
          :loading="passwordMutation.isPending.value"
          :disabled="!passwordDirty"
          @click="savePassword"
          >确认修改</el-button
        ></template
      >
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.interviewer-settings-view {
  display: grid;
  max-width: var(--rs-interviewer-settings-width);
  gap: var(--rs-space-4);
  margin: 0 auto;
}
.interviewer-settings-section {
  display: grid;
  gap: var(--rs-space-4);
  padding: var(--rs-space-6);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
.interviewer-settings-section > header {
  display: flex;
  align-items: flex-start;
  gap: var(--rs-space-3);
}
.interviewer-settings-section > header > span {
  display: grid;
  width: 36px;
  height: 36px;
  flex: 0 0 36px;
  place-items: center;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-blue-050);
  color: var(--rs-blue-700);
}
.interviewer-settings-section h2,
.interviewer-settings-section p,
.interviewer-account-facts {
  margin: 0;
}
.interviewer-settings-section h2 {
  font-size: 16px;
}
.interviewer-settings-section header p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
  font-size: 12px;
}
.interviewer-account-facts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
}
.interviewer-account-facts > div {
  display: grid;
  gap: var(--rs-space-1);
  padding: var(--rs-space-3) var(--rs-space-4);
}
.interviewer-account-facts > div + div {
  border-left: 1px solid var(--rs-border-default);
}
.interviewer-account-facts dt {
  color: var(--rs-text-secondary);
  font-size: 12px;
}
.interviewer-account-facts dd {
  margin: 0;
  font-weight: 600;
}
.interviewer-settings-form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0 var(--rs-space-4);
}
.interviewer-settings-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--rs-space-4);
  padding-top: var(--rs-space-3);
  border-top: 1px solid var(--rs-border-default);
  color: var(--rs-text-secondary);
  font-size: 12px;
}
.interviewer-settings-footer > div {
  display: flex;
  gap: var(--rs-space-2);
}
.interviewer-password-row {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr) auto;
  align-items: center;
  gap: var(--rs-space-3);
  padding: var(--rs-space-4);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
}
.interviewer-password-row > span {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
  color: var(--rs-blue-700);
}
.interviewer-password-row p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
  font-size: 12px;
}
.interviewer-password-strength {
  display: grid;
  gap: var(--rs-space-2);
  margin: calc(var(--rs-space-2) * -1) 0 var(--rs-space-4);
  color: var(--rs-text-secondary);
  font-size: 12px;
}
.interviewer-password-strength > div {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--rs-space-1);
}
.interviewer-password-strength i {
  height: 4px;
  border-radius: var(--rs-radius-xs);
  background: var(--rs-surface-muted);
}
.interviewer-password-strength__active {
  background: var(--rs-action-primary) !important;
}
.interviewer-password-strength small {
  color: var(--rs-text-tertiary);
}
</style>
