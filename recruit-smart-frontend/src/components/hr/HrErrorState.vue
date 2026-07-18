<script setup lang="ts">
import { AlertTriangle, RefreshCw } from 'lucide-vue-next'

withDefaults(
  defineProps<{
    title?: string
    description?: string
    retryable?: boolean
    loading?: boolean
  }>(),
  {
    title: '内容暂时无法加载',
    description: '请稍后重试。如果问题持续存在，请联系系统管理员。',
    retryable: true,
    loading: false,
  },
)

const emit = defineEmits<{ retry: [] }>()
</script>

<template>
  <section class="hr-state hr-state--error" role="alert">
    <span class="hr-state__icon"><AlertTriangle :size="22" :stroke-width="1.75" /></span>
    <div>
      <h3>{{ title }}</h3>
      <p>{{ description }}</p>
    </div>
    <el-button v-if="retryable" :icon="RefreshCw" :loading="loading" @click="emit('retry')">
      重新加载
    </el-button>
  </section>
</template>
