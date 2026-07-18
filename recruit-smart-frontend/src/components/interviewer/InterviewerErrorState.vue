<script setup lang="ts">
import { RefreshCw, TriangleAlert } from 'lucide-vue-next'

defineProps<{ title: string; description: string; loading?: boolean }>()
const emit = defineEmits<{ retry: [] }>()
</script>

<template>
  <section class="interviewer-error-state" role="alert">
    <TriangleAlert :size="20" :stroke-width="1.75" aria-hidden="true" />
    <div>
      <h2>{{ title }}</h2>
      <p>{{ description }}</p>
    </div>
    <el-button :icon="RefreshCw" :loading="loading" @click="emit('retry')">重新加载</el-button>
  </section>
</template>

<style scoped lang="scss">
.interviewer-error-state {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: var(--rs-space-3);
  padding: var(--rs-space-4);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
.interviewer-error-state > svg {
  color: var(--rs-warning-800);
}
.interviewer-error-state h2,
.interviewer-error-state p {
  margin: 0;
}
.interviewer-error-state h2 {
  font-size: 14px;
}
.interviewer-error-state p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
  font-size: 12px;
}
</style>
