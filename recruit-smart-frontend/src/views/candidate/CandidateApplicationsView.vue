<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyApplications, withdrawMyApplication } from '@/api/candidatePortal'
import { usePortalResource } from '@/composables/usePortalResource'
import { demoMyApplications } from '@/config/demoCandidatePortal'

const resource = usePortalResource(getMyApplications, demoMyApplications)
const withdrawable = (status: string) => ['SUBMITTED', 'SCREENING'].includes(status)
async function withdraw(id: number) {
  await ElMessageBox.confirm('撤回后该投递将停止推进，确认撤回？', '撤回投递', { type: 'warning' })
  if (!resource.demoMode.value) await withdrawMyApplication(id)
  resource.data.value = resource.data.value.map((item) =>
    item.id === id ? { ...item, status: 'WITHDRAWN', statusText: '已撤回' } : item,
  )
  ElMessage.success('投递已撤回')
}
</script>
<template>
  <div class="role-portal">
    <div class="portal-toolbar">
      <div>
        <h2>我的投递记录</h2>
        <p>状态来自与你当前账号绑定的投递记录。</p>
      </div>
      <span v-if="resource.demoMode.value" class="portal-demo-note">候选人演示数据</span>
    </div>
    <section class="portal-panel">
      <div v-if="resource.loading.value" class="portal-loading">正在加载我的投递...</div>
      <div v-else-if="resource.error.value" class="portal-error">{{ resource.error.value }}</div>
      <div v-else-if="resource.data.value.length" class="portal-list">
        <article v-for="item in resource.data.value" :key="item.id" class="portal-row">
          <div class="portal-row__primary">
            <h3>{{ item.jobTitle }}</h3>
            <p>{{ item.department }} · {{ item.resumeName }}</p>
          </div>
          <div class="portal-row__cell">
            <strong>{{ new Date(item.appliedAt).toLocaleDateString('zh-CN') }}</strong
            ><span>投递日期</span>
          </div>
          <span class="rs-status-pill rs-status-pill--info">{{ item.statusText }}</span
          ><el-button v-if="withdrawable(item.status)" text type="danger" @click="withdraw(item.id)"
            >撤回</el-button
          ><span v-else />
        </article>
      </div>
      <div v-else class="portal-empty">你还没有投递记录。</div>
    </section>
  </div>
</template>
