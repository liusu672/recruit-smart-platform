<script setup lang="ts">
import { CircleAlert, CircleCheck, Clock3, Info } from 'lucide-vue-next'
import { computed } from 'vue'

export type CandidateStatusTone = 'info' | 'success' | 'warning' | 'danger' | 'neutral'

const props = defineProps<{
  status: string
  label: string
  tone?: CandidateStatusTone
}>()

const inferredTone = computed<CandidateStatusTone>(() => {
  if (props.tone) return props.tone
  if (
    ['HIRED', 'ACCEPTED', 'APPROVED', 'ONBOARDED', 'COMPLETED', 'SUCCESS'].includes(props.status)
  ) {
    return 'success'
  }
  if (['REJECTED', 'SCREEN_REJECT', 'FAILED', 'CANCELED', 'REVOKED'].includes(props.status)) {
    return 'danger'
  }
  if (['SENT', 'PENDING', 'REINTERVIEW', 'PROCESSING'].includes(props.status)) return 'warning'
  if (['WITHDRAWN', 'DRAFT'].includes(props.status)) return 'neutral'
  return 'info'
})

const icon = computed(() => {
  if (inferredTone.value === 'success') return CircleCheck
  if (inferredTone.value === 'warning') return Clock3
  if (inferredTone.value === 'danger') return CircleAlert
  return Info
})
</script>

<template>
  <span class="candidate-status-badge" :class="`candidate-status-badge--${inferredTone}`">
    <component :is="icon" :size="13" :stroke-width="1.9" aria-hidden="true" />
    {{ label }}
  </span>
</template>
