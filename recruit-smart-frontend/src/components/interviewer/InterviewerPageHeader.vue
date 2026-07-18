<script setup lang="ts">
defineProps<{ title: string; description?: string }>()
</script>

<template>
  <header class="interviewer-page-header">
    <div>
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
