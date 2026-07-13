<script setup lang="ts">
import { Shield } from 'lucide-vue-next'

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
        <Shield :size="16" :stroke-width="1.75" aria-hidden="true" />
        <span>{{ isCandidate ? '管理端登录' : '候选人登录' }}</span>
      </button>
    </div>
  </header>
</template>
