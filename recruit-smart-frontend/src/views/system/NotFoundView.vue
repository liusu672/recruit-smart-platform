<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
</script>
<template>
  <main class="not-found">
    <p>404</p>
    <h1>页面不存在</h1>
    <span>请检查地址，或返回系统入口。</span>
    <el-button type="primary" @click="router.push('/')">返回首页</el-button>
  </main>
</template>
<style scoped lang="scss">
.not-found {
  display: grid;
  width: 480px;
  margin: 20vh auto 0;
  justify-items: center;
  gap: var(--rs-space-3);
  color: var(--rs-text-secondary);
}
.not-found p {
  margin: 0;
  color: var(--rs-blue-700);
  font-weight: 700;
}
.not-found h1 {
  margin: 0;
  color: var(--rs-text-primary);
}
</style>
