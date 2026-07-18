<script setup lang="ts">
withDefaults(
  defineProps<{
    interactive?: boolean
  }>(),
  { interactive: false },
)
</script>

<template>
  <article class="candidate-list-item" :class="{ 'candidate-list-item--interactive': interactive }">
    <slot />
  </article>
</template>
