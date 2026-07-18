<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { computed, reactive, watch } from 'vue'
import { useRoute } from 'vue-router'

import HrErrorState from '@/components/hr/HrErrorState.vue'
import HrPageHeader from '@/components/hr/HrPageHeader.vue'
import { useUserSettings } from '@/composables/useUserSettings'
import type { PasswordUpdateRequest, UserProfileUpdateRequest } from '@/types/user'

const { userQuery, profileMutation, passwordMutation } = useUserSettings()
const route = useRoute()
const isHr = computed(() => route.path.startsWith('/hr/'))
const profile = reactive<UserProfileUpdateRequest>({ realName: '', phone: '', email: '' })
const password = reactive<PasswordUpdateRequest>({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

watch(
  () => userQuery.data.value,
  (user) => {
    if (!user) return
    profile.realName = user.realName ?? ''
    profile.phone = user.phone ?? ''
    profile.email = user.email ?? ''
  },
  { immediate: true },
)

async function saveProfile() {
  try {
    await profileMutation.mutateAsync({ ...profile })
    ElMessage.success('个人信息已更新')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '个人信息更新失败')
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
</script>

<template>
  <div class="settings-view">
    <HrPageHeader v-if="isHr" title="账户与安全" description="维护当前登录账号的联系方式和密码。" />
    <header v-else class="settings-view__intro">
      <div>
        <h2 class="rs-section-title">账户与安全</h2>
        <p>登录账号资料与候选人求职资料分开维护，修改后会同步当前会话。</p>
      </div>
    </header>

    <HrErrorState
      v-if="isHr && userQuery.error.value"
      title="部分账户信息暂时无法加载"
      description="请稍后重试，或联系系统管理员检查账户状态。"
      :loading="userQuery.isFetching.value"
      @retry="userQuery.refetch()"
    />
    <el-alert
      v-else-if="userQuery.error.value"
      type="error"
      :closable="false"
      show-icon
      :title="
        userQuery.error.value instanceof Error ? userQuery.error.value.message : '账户信息加载失败'
      "
    />

    <section class="settings-section">
      <div class="settings-section__heading">
        <h3>基本信息</h3>
        <span>用户名和角色由系统管理</span>
      </div>
      <el-form label-position="top" @submit.prevent="saveProfile">
        <div class="settings-grid">
          <div v-if="isHr" class="settings-readonly">
            <span>登录账号</span><strong>{{ userQuery.data.value?.username ?? '加载中' }}</strong>
          </div>
          <div v-if="isHr" class="settings-readonly">
            <span>账户角色</span><strong>{{ userQuery.data.value?.roleName ?? 'HR' }}</strong>
          </div>
          <el-form-item v-if="!isHr" label="登录账号">
            <el-input :model-value="userQuery.data.value?.username ?? ''" disabled />
          </el-form-item>
          <el-form-item v-if="!isHr" label="角色">
            <el-input :model-value="userQuery.data.value?.roleName ?? ''" disabled />
          </el-form-item>
          <el-form-item label="真实姓名">
            <el-input v-model="profile.realName" maxlength="64" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="profile.phone" maxlength="11" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="profile.email" maxlength="128" />
          </el-form-item>
        </div>
        <el-button type="primary" :loading="profileMutation.isPending.value" @click="saveProfile">
          保存基本信息
        </el-button>
      </el-form>
    </section>

    <section class="settings-section">
      <div class="settings-section__heading">
        <h3>修改密码</h3>
        <span>修改后请重新使用新密码登录</span>
      </div>
      <el-form label-position="top" @submit.prevent="savePassword">
        <div class="settings-grid settings-grid--password">
          <el-form-item label="原密码">
            <el-input v-model="password.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="password.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认新密码">
            <el-input v-model="password.confirmPassword" type="password" show-password />
          </el-form-item>
        </div>
        <el-button type="primary" :loading="passwordMutation.isPending.value" @click="savePassword">
          更新密码
        </el-button>
      </el-form>
    </section>
  </div>
</template>

<style scoped lang="scss">
.settings-view {
  display: grid;
  gap: var(--rs-space-4);
  max-width: 960px;
}
.settings-view__intro p {
  margin: var(--rs-space-1) 0 0;
  color: var(--rs-text-secondary);
}
.settings-section {
  display: grid;
  gap: var(--rs-space-4);
  padding: var(--rs-space-6);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
.settings-section__heading {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--rs-space-4);
}
.settings-section__heading h3,
.settings-section__heading span {
  margin: 0;
}
.settings-section__heading h3 {
  font-size: 16px;
}
.settings-section__heading span {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}
.settings-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0 var(--rs-space-4);
}
.settings-grid--password {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}
.settings-readonly {
  display: grid;
  align-content: center;
  min-height: 62px;
  padding: 10px 12px;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
}
.settings-readonly span {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}
.settings-readonly strong {
  margin-top: 3px;
  font-weight: 600;
}
@media (max-width: 1280px) {
  .settings-grid,
  .settings-grid--password {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
