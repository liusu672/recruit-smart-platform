<script setup lang="ts">
import candidateRocketImage from '@/assets/login-candidate-rocket.png'
import managementIllustrationImage from '@/assets/login-management-team.png'
import { candidateJobs, loginRoleCopy, roleCards } from '@/config/login'
import type { ManagementRole } from '@/types/login'

defineProps<{
  isCandidate: boolean
  activeRole: ManagementRole
}>()

const emit = defineEmits<{
  switchRole: [role: ManagementRole]
}>()
</script>

<template>
  <section class="login-brand-panel" aria-live="polite">
    <div class="login-preview-state" :class="{ active: isCandidate }">
      <div>
        <div class="login-kicker">候选人入口</div>
        <h1 class="login-hero-title">开启你的下一段职业旅程</h1>
        <p class="login-hero-copy">查看推荐职位、投递进展和面试安排，让每一步都有清晰反馈。</p>
      </div>
    </div>

    <div
      v-for="(copy, role) in loginRoleCopy"
      :key="role"
      class="login-preview-state"
      :class="{ active: !isCandidate && activeRole === role }"
    >
      <div>
        <div class="login-kicker">{{ copy.heroKicker }}</div>
        <h1 class="login-hero-title">{{ copy.heroTitle }}</h1>
        <p class="login-hero-copy">{{ copy.heroDesc }}</p>
      </div>
    </div>

    <div class="login-visual" aria-hidden="true">
      <div
        class="login-visual-state login-rocket-visual"
        :class="{ active: isCandidate }"
        data-login-visual="candidate"
      >
        <img class="login-rocket-image" :src="candidateRocketImage" alt="" />
      </div>

      <div
        class="login-visual-state login-enterprise-visual"
        :class="{ active: !isCandidate }"
        data-login-visual="management"
      >
        <img class="login-management-image" :src="managementIllustrationImage" alt="" />
      </div>
    </div>

    <div class="login-brand-footer">
      <div class="login-preview-state" :class="{ active: isCandidate }">
        <div class="login-jobs" aria-label="相关职位预览">
          <article v-for="job in candidateJobs" :key="job.title" class="login-job-card">
            <div>
              <div class="login-job-title">{{ job.title }}</div>
              <div class="caption">{{ job.meta }}</div>
            </div>
            <span class="status" :class="job.tone">{{ job.status }}</span>
          </article>
        </div>
      </div>

      <div class="login-preview-state" :class="{ active: !isCandidate }">
        <div class="login-role-shortcuts" aria-label="管理端角色快速入口">
          <button
            v-for="role in roleCards"
            :key="role.role"
            class="login-role-card"
            :class="{ active: activeRole === role.role }"
            type="button"
            @click="emit('switchRole', role.role)"
          >
            <component :is="role.icon" class="icon small" :stroke-width="1.75" aria-hidden="true" />
            <span class="login-role-card-title">{{ role.title }}</span>
            <span class="caption">{{ role.desc }}</span>
          </button>
        </div>
      </div>
    </div>
  </section>
</template>
