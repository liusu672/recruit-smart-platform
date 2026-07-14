<script setup lang="ts">
import { LockKeyhole, Mail, Sparkles, UserRound } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'

import type { CandidateMode, LoginFormState, LoginRoleCopy } from '@/types/login'

defineProps<{
  isCandidate: boolean
  candidateMode: CandidateMode
  activeRoleCopy: LoginRoleCopy
  fieldErrors: Record<string, string>
  formError: string
  isSubmitting: boolean
  submitLabel: string
}>()

const emit = defineEmits<{
  submit: []
  fillDemo: []
  'update:candidateMode': [mode: CandidateMode]
}>()

// 使用 v-model 管理两个表单对象，避免子组件直接修改只读 props。
const candidateForm = defineModel<LoginFormState>('candidateForm', { required: true })
const managementForm = defineModel<LoginFormState>('managementForm', { required: true })
</script>

<template>
  <aside class="login-card">
    <div class="login-view" :class="{ active: isCandidate }" data-login-view="candidate">
      <div class="login-card-header">
        <div class="login-card-meta">
          <span>欢迎来到 Recruit Smart</span>
          <span>没有账号？<RouterLink to="/register">注册</RouterLink></span>
        </div>
        <h2 class="login-title">候选人登录</h2>
        <p class="login-description">登录后查看推荐职位、投递进展和面试安排。</p>
      </div>

      <div class="login-segmented" role="tablist" aria-label="候选人登录方式">
        <button
          class="login-segment"
          :class="{ active: candidateMode === 'password' }"
          type="button"
          @click="emit('update:candidateMode', 'password')"
        >
          账号密码
        </button>
        <button
          class="login-segment"
          :class="{ active: candidateMode === 'code' }"
          type="button"
          @click="emit('update:candidateMode', 'code')"
        >
          验证码
        </button>
      </div>

      <form class="login-form" novalidate @submit.prevent="emit('submit')">
        <div v-if="formError && isCandidate" class="login-alert" role="alert">{{ formError }}</div>

        <div class="login-field" :class="{ 'has-error': fieldErrors.username }">
          <label class="field-label" for="candidate-account">手机号 / 邮箱 / 账号</label>
          <div class="login-input-wrap">
            <UserRound class="icon small" :stroke-width="1.75" aria-hidden="true" />
            <input
              id="candidate-account"
              v-model="candidateForm.username"
              class="login-input"
              name="account"
              placeholder="请输入登录账号"
              autocomplete="username"
              :aria-invalid="Boolean(fieldErrors.username)"
            />
          </div>
          <div class="login-field-error">
            {{ fieldErrors.username || '请输入手机号、邮箱或账号。' }}
          </div>
        </div>

        <div
          class="login-field"
          :class="{ 'has-error': fieldErrors.password }"
          :hidden="candidateMode !== 'password'"
        >
          <label class="field-label" for="candidate-password">密码</label>
          <div class="login-input-wrap">
            <LockKeyhole class="icon small" :stroke-width="1.75" aria-hidden="true" />
            <input
              id="candidate-password"
              v-model="candidateForm.password"
              class="login-input"
              name="password"
              type="password"
              placeholder="请输入密码"
              autocomplete="current-password"
              :aria-invalid="Boolean(fieldErrors.password)"
            />
          </div>
          <div class="login-field-error">{{ fieldErrors.password || '请输入密码。' }}</div>
        </div>

        <div
          class="login-field"
          :class="{ 'has-error': fieldErrors.code }"
          :hidden="candidateMode !== 'code'"
        >
          <label class="field-label" for="candidate-code">验证码</label>
          <div class="login-input-wrap">
            <Mail class="icon small" :stroke-width="1.75" aria-hidden="true" />
            <input
              id="candidate-code"
              v-model="candidateForm.code"
              class="login-input"
              name="code"
              placeholder="请输入验证码"
              inputmode="numeric"
              :aria-invalid="Boolean(fieldErrors.code)"
            />
          </div>
          <div class="login-field-error">{{ fieldErrors.code || '请输入验证码。' }}</div>
        </div>

        <div class="login-inline-actions">
          <button class="button tertiary" type="button" @click="emit('fillDemo')">
            <Sparkles class="icon small" :stroke-width="1.75" aria-hidden="true" />
            一键填充
          </button>
          <span class="caption">忘记密码请联系管理员</span>
        </div>

        <button class="button primary full" type="submit" :disabled="isSubmitting">
          {{ submitLabel }}
        </button>
        <div class="login-note">
          候选人信息仅用于投递、面试安排和 Offer 沟通，请勿在公共设备保存密码。
        </div>
      </form>
    </div>

    <div class="login-view" :class="{ active: !isCandidate }" data-login-view="management">
      <div class="login-card-header">
        <div class="login-card-meta">
          <span>企业招聘工作台</span>
          <span>账号权限由系统管理员开通</span>
        </div>
        <h2 class="login-title">{{ activeRoleCopy.formTitle }}</h2>
        <p class="login-description">{{ activeRoleCopy.formDesc }}</p>
      </div>

      <form class="login-form" novalidate @submit.prevent="emit('submit')">
        <div v-if="formError && !isCandidate" class="login-alert" role="alert">{{ formError }}</div>

        <div class="login-field" :class="{ 'has-error': fieldErrors.username }">
          <label class="field-label" for="management-username">企业账号</label>
          <div class="login-input-wrap">
            <UserRound class="icon small" :stroke-width="1.75" aria-hidden="true" />
            <input
              id="management-username"
              v-model="managementForm.username"
              class="login-input"
              name="username"
              placeholder="请输入企业账号"
              autocomplete="username"
              :aria-invalid="Boolean(fieldErrors.username)"
            />
          </div>
          <div class="login-field-error">{{ fieldErrors.username || '请输入企业账号。' }}</div>
        </div>

        <div class="login-field" :class="{ 'has-error': fieldErrors.password }">
          <label class="field-label" for="management-password">密码</label>
          <div class="login-input-wrap">
            <LockKeyhole class="icon small" :stroke-width="1.75" aria-hidden="true" />
            <input
              id="management-password"
              v-model="managementForm.password"
              class="login-input"
              name="password"
              type="password"
              placeholder="请输入密码"
              autocomplete="current-password"
              :aria-invalid="Boolean(fieldErrors.password)"
            />
          </div>
          <div class="login-field-error">{{ fieldErrors.password || '请输入密码。' }}</div>
        </div>

        <div class="login-inline-actions">
          <button class="button tertiary" type="button" @click="emit('fillDemo')">
            <Sparkles class="icon small" :stroke-width="1.75" aria-hidden="true" />
            一键填充
          </button>
          <span class="caption">账号问题请联系管理员</span>
        </div>

        <button class="button primary full" type="submit" :disabled="isSubmitting">
          {{ submitLabel }}
        </button>
        <div class="login-note">
          AI 结果仅作为辅助建议，候选人筛选、录用和入职状态仍由业务角色确认。
        </div>
      </form>
    </div>
  </aside>
</template>
