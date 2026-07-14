Warning: truncated output (original token count: 4802) Total output lines: 925

<script setup lang="ts">
import LoginBrandPanel from '@/components/auth/LoginBrandPanel.vue'
import LoginFormCard from '@/components/auth/LoginFormCard.vue'
import LoginTopbar from '@/components/auth/LoginTopbar.vue'
import '@/styles/login.scss'
import { useLogin } from '@/composables/useLogin'

const {
  activeRole,
  activeRoleCopy,
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
} = useLogin()

// 顶部切换和角色快捷入口共用同一套状态，保证视觉区与表单同步更新。
function toggleSurface() {
  switchSurface(isCandidate.value ? 'management' : 'candidate')
}
</script>

<template>
  <main class="login-screen" :class="{ 'is-management': !isCandidate }" aria-label="登录页">
    <div class="login-shell">
      <LoginTopbar :is-candidate="isCandidate" @toggle-surface="toggleSurface" />

      <section class="login-main" aria-label="登录">
        <LoginBrandPanel
          :is-candidate="isCandidate"
          :active-role="activeRole"
          @switch-role="switchRole"
        />

        <LoginFormCard
          v-model:candidate-mode="candidateMode"
          v-model:candidate-form="candidateForm"
          v-model:management-form="managementForm"
          :is-candidate="isCandidate"
          :active-role-copy="activeRoleCopy"
          :field-errors="fieldErrors"
          :form-error="formError"
          :is-submitting="isSubmitting"
          :submit-label="submitLabel"
          @fill-demo="fillDemo"
          @submit="submitLogin"
        />
      </section>
    </div>
  </main>
</template>
