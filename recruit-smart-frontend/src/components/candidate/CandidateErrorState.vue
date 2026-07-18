<script setup lang="ts">
import { CircleAlert } from 'lucide-vue-next'

defineProps<{
  title?: string
  description: string
  retryable?: boolean
}>()

const emit = defineEmits<{ retry: [] }>()
</script>

<template>
  <div class="candidate-state candidate-state--error" role="alert">
    <span class="candidate-state__icon">
      <CircleAlert :size="28" :stroke-width="1.6" aria-hidden="true" />
    </span>
    <div>
      <h2>{{ title || '暂时无法加载' }}</h2>
      <p>{{ description }}</p>
    </div>
    <el-button v-if="retryable" @click="emit('retry')">重新加载</el-button>
  </div>
</template>
