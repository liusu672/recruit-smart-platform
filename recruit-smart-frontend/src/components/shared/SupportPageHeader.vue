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
    class="support-page-header"
    :class="{ 'support-page-header--actions-only': usesTopbarHeader }"
  >
    <div v-if="!usesTopbarHeader">
      <h1>{{ title }}</h1>
      <p v-if="description">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="support-page-header__actions">
      <slot name="actions" />
    </div>
  </header>
</template>

<style scoped lang="scss">
.support-page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--rs-space-6);
}

.support-page-header--actions-only {
  justify-content: flex-end;
}

.support-page-header h1,
.support-page-header p {
  margin: 0;
}
.support-page-header h1 {
  color: var(--rs-text-primary);
  font-size: 24px;
  font-weight: 600;
  line-height: 1.3;
}
.support-page-header p {
  max-width: 720px;
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}
.support-page-header__actions {
  display: flex;
  align-items: center;
  gap: var(--rs-space-2);
}
</style>
