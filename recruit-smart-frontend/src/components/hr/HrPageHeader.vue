<script setup lang="ts">
import { useWorkspacePageHeader } from '@/composables/useWorkspacePageHeader'

const props = defineProps<{
  title: string
  description: string
}>()

const usesTopbarHeader = useWorkspacePageHeader(() => ({
  title: props.title,
  description: props.description,
}))
</script>

<template>
  <header
    v-if="!usesTopbarHeader || $slots.actions"
    class="hr-page-header"
    :class="{ 'hr-page-header--actions-only': usesTopbarHeader }"
  >
    <div v-if="!usesTopbarHeader" class="hr-page-header__copy">
      <h1>{{ title }}</h1>
      <p>{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="hr-page-header__actions">
      <slot name="actions" />
    </div>
  </header>
</template>
