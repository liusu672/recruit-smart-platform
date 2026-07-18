<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { CircleCheck, ShieldCheck } from 'lucide-vue-next'
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'

import CandidateErrorState from '@/components/candidate/CandidateErrorState.vue'
import CandidateFormFooter from '@/components/candidate/CandidateFormFooter.vue'
import CandidatePageHeader from '@/components/candidate/CandidatePageHeader.vue'
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
const lastSavedAt = ref('')

const profileDirty = computed(
  () => JSON.stringify(profile) !== JSON.stringify(initialProfile.value),
)
const passwordDirty = computed(() =>
  Boolean(password.oldPassword || password.newPassword || password.confirmPassword),
)
const hasUnsavedChanges = computed(() => profileDirty.value || passwordDirty.value)

watch(
  () => userQuery.data.value,
  (user) => {
    if (!user) return
    const next = { realName: user.realName ?? '', phone: user.phone ?? '', email: user.email ?? '' }
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
    ElMessage.success('账户信息已更新')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '账户信息更新失败')
  }
}

async function savePassword() {
  if (password.newPassword.length < 6 || password.newPassword.length > 32) {
    ElMessage.warning('新密码需要为 6-32 位')
    return
  }
  if (password.newPassword !== password.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  try {
    await passwordMutation.mutateAsync({ ...password })
    Object.assign(password, { oldPassword: '', newPassword: '', confirmPassword: '' })
    ElMessage.success('密码已更新，请使用新密码登录')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '密码更新失败')
  }
}

function beforeUnload(event: BeforeUnloadEvent) {
  if (!hasUnsavedChanges.value) return
  event.preventDefault()
  event.returnValue = ''
}

onMounted(() => window.addEventListener('beforeunload', beforeUnload))
onBeforeUnmount(() => window.removeEventListener('beforeunload', beforeUnload))
onBeforeRouteLeave(
  () => !hasUnsavedChanges.value || window.confirm('你有未保存的更改，确认离开当前页面？'),
)
</script>

<template>
  <div class="candidate-page candidate-settings">
    <CandidatePageHeader
      title="账户与安全"
      description="管理登录账户信息和密码，求职资料请前往个人中心维护。"
    />

    <CandidateErrorState
      v-if="userQuery.error.value"
      description="部分账户信息暂时无法加载，请稍后重试。"
      retryable
      @retry="userQuery.refetch"
    />

    <div v-else class="candidate-settings-layout">
      <section class="candidate-settings-section candidate-settings-section--account">
        <div class="candidate-settings-section__heading">
          <h2>账户信息</h2>
          <p>登录账号由系统管理，不能在此修改。</p>
        </div>
        <div class="candidate-account-row">
          <span>登录账号</span><strong>{{ userQuery.data.value?.username ?? '加载中' }}</strong>
        </div>
        <el-form label-position="top" @submit.prevent="saveProfile">
          <div class="candidate-settings-grid">
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
        <CandidateFormFooter
          :dirty="profileDirty"
          :saving="profileMutation.isPending.value"
          :last-saved-at="lastSavedAt"
          save-label="保存账户信息"
          @cancel="cancelProfileChanges"
          @save="saveProfile"
        />
      </section>

      <section class="candidate-settings-section candidate-settings-section--password">
        <div class="candidate-settings-section__heading">
          <h2>修改密码</h2>
          <p>密码长度为 6-32 位。</p>
        </div>
        <el-form label-position="top" @submit.prevent="savePassword">
          <div class="candidate-settings-grid">
            <el-form-item label="原密码"
              ><el-input v-model="password.oldPassword" type="password" show-password
            /></el-form-item>
            <el-form-item label="新密码"
              ><el-input v-model="password.newPassword" type="password" show-password
            /></el-form-item>
            <el-form-item label="确认新密码"
              ><el-input v-model="password.confirmPassword" type="password" show-password
            /></el-form-item>
          </div>
          <el-button
            type="primary"
            :loading="passwordMutation.isPending.value"
            :disabled="!passwordDirty"
            @click="savePassword"
            >更新密码</el-button
          >
        </el-form>
      </section>

      <aside class="candidate-security-guide" aria-label="账户安全提示">
        <header>
          <span class="candidate-security-guide__icon"
            ><ShieldCheck :size="18" :stroke-width="1.75"
          /></span>
          <div>
            <h2>安全提示</h2>
            <p>保护好账户信息，避免求职资料泄露。</p>
          </div>
        </header>
        <ul>
          <li><CircleCheck :size="16" /><span>请勿使用与其他网站相同的密码。</span></li>
          <li><CircleCheck :size="16" /><span>密码长度需保持在 6-32 位。</span></li>
          <li><CircleCheck :size="16" /><span>修改成功后，请使用新密码重新登录。</span></li>
        </ul>
      </aside>
    </div>
  </div>
</template>

<style scoped lang="scss">
.candidate-settings-section {
  display: grid;
  align-content: start;
  gap: var(--rs-space-4);
  min-width: 0;
  padding: var(--rs-space-4);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
.candidate-settings-section__heading h2,
.candidate-settings-section__heading p {
  margin: 0;
}
.candidate-settings-section__heading h2 {
  font-size: 18px;
}
.candidate-settings-section__heading p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}
.candidate-account-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 40px;
  padding: 0 var(--rs-space-4);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
}
.candidate-account-row span {
  color: var(--rs-text-secondary);
}
.candidate-settings-grid {
  display: grid;
  grid-template-columns: 1fr;
}
.candidate-settings-layout {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr)) 248px;
  align-items: start;
  gap: var(--rs-space-4);
}
.candidate-settings-section :deep(.candidate-form-footer) {
  position: static;
  min-height: 56px;
  padding: var(--rs-space-2) var(--rs-space-3);
  box-shadow: none;
}
.candidate-settings-section :deep(.candidate-form-footer__actions) {
  gap: var(--rs-space-1);
}
.candidate-settings-section :deep(.candidate-form-footer .el-button) {
  padding-right: var(--rs-space-3);
  padding-left: var(--rs-space-3);
}
.candidate-security-guide {
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
.candidate-security-guide > header {
  display: flex;
  align-items: flex-start;
  gap: var(--rs-space-3);
  padding: var(--rs-space-4);
  background: var(--rs-blue-050);
}
.candidate-security-guide__icon {
  display: grid;
  flex: 0 0 36px;
  width: 36px;
  height: 36px;
  place-items: center;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
  color: var(--rs-blue-700);
}
.candidate-security-guide h2,
.candidate-security-guide p {
  margin: 0;
}
.candidate-security-guide h2 {
  font-size: 16px;
}
.candidate-security-guide header p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
  font-size: 13px;
}
.candidate-security-guide ul {
  display: grid;
  gap: var(--rs-space-3);
  margin: 0;
  padding: var(--rs-space-4);
  list-style: none;
}
.candidate-security-guide li {
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr);
  align-items: start;
  gap: var(--rs-space-2);
  color: var(--rs-text-secondary);
  line-height: 1.6;
}
.candidate-security-guide li svg {
  margin-top: 3px;
  color: var(--rs-success-700);
}

@media (max-width: 1100px) {
  .candidate-settings-layout {
    grid-template-columns: 1fr;
  }
  .candidate-security-guide {
    order: -1;
  }
}
</style>
