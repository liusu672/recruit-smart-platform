<script setup lang="ts">
import { useWorkspacePageHeader } from '@/composables/useWorkspacePageHeader'

const props = defineProps<{ title: string; description?: string }>()

const usesTopbarHeader = useWorkspacePageHeader(() => ({
  title: props.title,
  description: props.description ?? '',
}))
</script>

<template>
  <header
    v-if="!usesTopbarHeader || $slots.actions"
    class="interviewer-page-header"
    :class="{ 'interviewer-page-header--actions-only': usesTopbarHeader }"
  >
    <div v-if="!usesTopbarHeader">
      <h1>{{ title }}</h1>
      <p v-if="description">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="interviewer-page-header__actions">
      <slot name="actions" />
    </div>
  </header>
</template>

<style scoped lang="scss">
.interviewer-page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--rs-space-6);
}

.interviewer-page-header--actions-only {
  justify-content: flex-end;
}

.interviewer-page-header h1,
.interviewer-page-header p {
  margin: 0;
}
.interviewer-page-header h1 {
  font-size: 24px;
  font-weight: 600;
  line-height: 1.3;
}
.interviewer-page-header p {
  max-width: 720px;
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}
.interviewer-page-header__actions {
  display: flex;
  align-items: center;
  gap: var(--rs-space-2);
}
</style>
