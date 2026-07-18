<script setup lang="ts">
import { ArrowRight } from 'lucide-vue-next'

import type { HrStatusTone } from '@/types/hr'

withDefaults(
  defineProps<{
    label: string
    value: string | number
    supporting: string
    tone?: HrStatusTone
    loading?: boolean
  }>(),
  { tone: 'info', loading: false },
)
</script>

<template>
  <article class="hr-metric-card" :class="`hr-metric-card--${tone}`">
    <span class="hr-metric-card__label">{{ label }}</span>
    <strong>{{ loading ? '--' : value }}</strong>
    <span class="hr-metric-card__supporting">{{ supporting }}</span>
    <ArrowRight :size="16" :stroke-width="1.75" aria-hidden="true" />
  </article>
</template>
