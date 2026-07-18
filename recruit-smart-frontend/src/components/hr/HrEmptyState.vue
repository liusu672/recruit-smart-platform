<script setup lang="ts">
import type { Component } from 'vue'

defineProps<{
  icon: Component
  title: string
  description: string
}>()
</script>

<template>
  <section class="hr-state hr-state--empty">
    <span class="hr-state__icon"><component :is="icon" :size="24" :stroke-width="1.6" /></span>
    <div>
      <h3>{{ title }}</h3>
      <p>{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="hr-state__actions"><slot name="actions" /></div>
  </section>
</template>
