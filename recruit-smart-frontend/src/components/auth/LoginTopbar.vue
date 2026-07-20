<script setup lang="ts">
import { ArrowRight, BriefcaseBusiness, UserRound } from 'lucide-vue-next'

defineProps<{
  isCandidate: boolean
}>()

const emit = defineEmits<{
  toggleSurface: []
}>()
</script>

<template>
  <header class="login-topbar">
    <div class="brand" aria-label="Recruit Smart">
      <div class="brand-mark">RS</div>
      <span>Recruit Smart</span>
    </div>

    <div class="login-top-actions">
      <button
        class="button secondary login-surface-toggle"
        type="button"
        @click="emit('toggleSurface')"
      >
        <component
          :is="isCandidate ? BriefcaseBusiness : UserRound"
          :size="17"
          :stroke-width="1.75"
          aria-hidden="true"
        />
        <span>{{ isCandidate ? '企业管理人员入口' : '我是候选人' }}</span>
        <ArrowRight
          class="login-surface-toggle__arrow"
          :size="16"
          :stroke-width="1.75"
          aria-hidden="true"
        />
      </button>
    </div>
  </header>
</template>
