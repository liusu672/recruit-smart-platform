<script setup lang="ts">
import { CheckCircle2, LockKeyhole, Mail, Phone, UserRound } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'

import candidateRocketImage from '@/assets/login-candidate-rocket.png'
import { useCandidateRegister } from '@/composables/useCandidateRegister'
import '@/styles/login.scss'
import '@/styles/register.scss'

const { fieldErrors, form, formError, formNotice, isSubmitting, submitRegister } =
  useCandidateRegister()
</script>

<template>
  <main class="login-screen register-screen" aria-label="候选人注册页">
    <div class="login-shell">
      <header class="login-topbar">
        <div class="brand" aria-label="Recruit Smart">
          <div class="brand-mark">RS</div>
          <span>Recruit Smart</span>
        </div>
      </header>

      <section class="login-main register-main" aria-label="候选人注册">
        <section class="login-brand-panel register-brand-panel" aria-live="polite">
          <div class="login-preview-state active">
            <div>
              <div class="login-kicker">候选人注册</div>
              <h1 class="login-hero-title">建立你的职业档案</h1>
              <p class="login-hero-copy">
                完成注册后，你可以持续查看职位推荐、投递进展和面试安排。
              </p>
            </div>
          </div>

          <div class="register-visual" aria-hidden="true">
            <img class="register-rocket-image" :src="candidateRocketImage" alt="" />
          </div>

          <div class="login-brand-footer register-brand-footer">
            <span>从一次清晰的个人信息提交开始，逐步完善求职准备。</span>
          </div>
        </section>

        <aside class="login-card register-card">
          <div class="login-card-header">
            <div class="login-card-meta">
              <span>候选人中心</span>
              <span>已有账号？<RouterLink to="/login">直接登录</RouterLink></span>
            </div>
            <h2 class="login-title">注册候选人账号</h2>
            <p class="login-description">填写基础信息，创建你的 Recruit Smart 候选人档案。</p>
          </div>

          <form class="login-form" novalidate @submit.prevent="submitRegister">
            <div v-if="formError" class="login-alert" role="alert">{{ formError }}</div>

            <div v-if="formNotice" class="register-notice" role="status">
              <CheckCircle2 class="icon small" :stroke-width="1.75" aria-hidden="true" />
              <span>{{ formNotice }}</span>
            </div>

            <div class="login-field" :class="{ 'has-error': fieldErrors.realName }">
              <label class="field-label" for="register-real-name">真实姓名</label>
              <div class="login-input-wrap">
                <UserRound class="icon small" :stroke-width="1.75" aria-hidden="true" />
                <input
                  id="register-real-name"
                  v-model="form.realName"
                  class="login-input"
                  placeholder="请输入真实姓名"
                  autocomplete="name"
                  :aria-invalid="Boolean(fieldErrors.realName)"
                />
              </div>
              <div class="login-field-error">{{ fieldErrors.realName || '请输入真实姓名。' }}</div>
            </div>

            <div class="login-field" :class="{ 'has-error': fieldErrors.username }">
              <label class="field-label" for="register-username">登录账号</label>
              <div class="login-input-wrap">
                <Mail class="icon small" :stroke-width="1.75" aria-hidden="true" />
                <input
                  id="register-username"
                  v-model="form.username"
                  class="login-input"
                  placeholder="请输入 4-32 位登录账号"
                  autocomplete="username"
                  :aria-invalid="Boolean(fieldErrors.username)"
                />
              </div>
              <div class="login-field-error">
                {{ fieldErrors.username || '登录账号用于后续登录。' }}
              </div>
            </div>

            <div class="login-field" :class="{ 'has-error': fieldErrors.phone }">
              <label class="field-label" for="register-phone">手机号</label>
              <div class="login-input-wrap">
                <Phone class="icon small" :stroke-width="1.75" aria-hidden="true" />
                <input
                  id="register-phone"
                  v-model="form.phone"
                  class="login-input"
                  placeholder="请输入 11 位手机号"
                  autocomplete="tel"
                  :aria-invalid="Boolean(fieldErrors.phone)"
                />
              </div>
              <div class="login-field-error">
                {{ fieldErrors.phone || '用于候选人档案绑定和招聘联系。' }}
              </div>
            </div>

            <div class="login-field" :class="{ 'has-error': fieldErrors.password }">
              <label class="field-label" for="register-password">设置密码</label>
              <div class="login-input-wrap">
                <LockKeyhole class="icon small" :stroke-width="1.75" aria-hidden="true" />
                <input
                  id="register-password"
                  v-model="form.password"
                  class="login-input"
                  type="password"
                  placeholder="至少 6 位密码"
                  autocomplete="new-password"
                  :aria-invalid="Boolean(fieldErrors.password)"
                />
              </div>
              <div class="login-field-error">
                {{ fieldErrors.password || '密码需要 6-32 位。' }}
              </div>
            </div>

            <div class="login-field" :class="{ 'has-error': fieldErrors.confirmPassword }">
              <label class="field-label" for="register-confirm-password">确认密码</label>
              <div class="login-input-wrap">
                <LockKeyhole class="icon small" :stroke-width="1.75" aria-hidden="true" />
                <input
                  id="register-confirm-password"
                  v-model="form.confirmPassword"
                  class="login-input"
                  type="password"
                  placeholder="请再次输入密码"
                  autocomplete="new-password"
                  :aria-invalid="Boolean(fieldErrors.confirmPassword)"
                />
              </div>
              <div class="login-field-error">
                {{ fieldErrors.confirmPassword || '请再次输入相同密码。' }}
              </div>
            </div>

            <button class="button primary full" type="submit" :disabled="isSubmitting">
              {{ isSubmitting ? '注册中' : '创建候选人账号' }}
            </button>
            <div class="login-note">注册后请使用相同账号登录，完善简历和求职偏好。</div>
          </form>
        </aside>
      </section>
    </div>
  </main>
</template>
