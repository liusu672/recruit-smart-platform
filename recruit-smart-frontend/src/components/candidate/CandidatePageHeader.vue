<script setup lang="ts">
defineProps<{
  title: string
  description: string
}>()
</script>

<template>
  <header class="candidate-page-header">
    <div class="candidate-page-header__copy">
      <h1>{{ title }}</h1>
      <p>{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="candidate-page-header__actions">
      <slot name="actions" />
    </div>
  </header>
</template>
