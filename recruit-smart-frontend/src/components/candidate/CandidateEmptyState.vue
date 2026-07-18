<script setup lang="ts">
import type { Component } from 'vue'

defineProps<{
  icon: Component
  title: string
  description: string
}>()
</script>

<template>
  <div class="candidate-state candidate-state--empty">
    <span class="candidate-state__icon">
      <component :is="icon" :size="28" :stroke-width="1.6" aria-hidden="true" />
    </span>
    <div>
      <h2>{{ title }}</h2>
      <p>{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="candidate-state__actions"><slot name="actions" /></div>
  </div>
</template>
