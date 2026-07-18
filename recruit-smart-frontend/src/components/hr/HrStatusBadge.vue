<script setup lang="ts">
import type { HrStatusTone } from '@/types/hr'

withDefaults(
  defineProps<{
    status?: string
    label: string
    tone?: HrStatusTone
  }>(),
  { status: '', tone: 'neutral' },
)
</script>

<template>
  <span class="rs-status-pill" :class="`rs-status-pill--${tone}`" :data-status="status">
    {{ label }}
  </span>
</template>
