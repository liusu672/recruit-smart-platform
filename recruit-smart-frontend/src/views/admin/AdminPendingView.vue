<script setup lang="ts">
import { Construction } from 'lucide-vue-next'
defineProps<{ feature: string; description: string }>()
</script>
<template>
  <section class="portal-panel portal-pending">
    <div class="portal-pending__inner">
      <Construction :size="34" :stroke-width="1.6" /><span
        class="rs-status-pill rs-status-pill--warning"
        >接口待接入</span
      >
      <h2>{{ feature }}</h2>
      <p>{{ description }} 后端治理接口完成前，本页不会复用 HR 业务数据作为占位。</p>
    </div>
  </section>
</template>
