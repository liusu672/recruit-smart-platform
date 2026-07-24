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
    class="candidate-page-header"
    :class="{ 'candidate-page-header--actions-only': usesTopbarHeader }"
  >
    <div v-if="!usesTopbarHeader" class="candidate-page-header__copy">
      <h1>{{ title }}</h1>
      <p>{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="candidate-page-header__actions">
      <slot name="actions" />
    </div>
  </header>
</template>
