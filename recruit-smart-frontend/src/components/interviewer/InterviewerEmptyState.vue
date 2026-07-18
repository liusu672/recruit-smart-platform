<script setup lang="ts">
import { CalendarCheck } from 'lucide-vue-next'

defineProps<{ title: string; description: string }>()
</script>

<template>
  <section class="interviewer-empty-state">
    <span><CalendarCheck :size="24" :stroke-width="1.75" aria-hidden="true" /></span>
    <h2>{{ title }}</h2>
    <p>{{ description }}</p>
    <div v-if="$slots.actions"><slot name="actions" /></div>
  </section>
</template>

<style scoped lang="scss">
.interviewer-empty-state {
  display: grid;
  min-height: 240px;
  place-items: center;
  align-content: center;
  gap: var(--rs-space-2);
  padding: var(--rs-space-8);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-md);
  background: var(--rs-surface-primary);
  text-align: center;
}
.interviewer-empty-state > span {
  display: grid;
  width: 48px;
  height: 48px;
  place-items: center;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-blue-050);
  color: var(--rs-blue-700);
}
.interviewer-empty-state h2,
.interviewer-empty-state p {
  margin: 0;
}
.interviewer-empty-state h2 {
  font-size: 16px;
}
.interviewer-empty-state p {
  max-width: 480px;
  color: var(--rs-text-secondary);
}
.interviewer-empty-state > div {
  margin-top: var(--rs-space-2);
}
</style>
