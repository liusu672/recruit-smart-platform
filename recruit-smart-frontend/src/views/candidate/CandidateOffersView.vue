<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { acceptMyOffer, getMyOffers, rejectMyOffer } from '@/api/candidatePortal'
import { usePortalResource } from '@/composables/usePortalResource'
import { demoMyOffers } from '@/config/demoCandidatePortal'
const resource = usePortalResource(getMyOffers, demoMyOffers)
async function decide(id: number, decision: 'ACCEPTED' | 'REJECTED') {
  const accepted = decision === 'ACCEPTED'
  await ElMessageBox.confirm(
    accepted ? '接受后将进入入职流程，确认接受？' : '拒绝后该 Offer 将结束，确认拒绝？',
    accepted ? '接受 Offer' : '拒绝 Offer',
    { type: accepted ? 'success' : 'warning' },
  )
  if (!resource.demoMode.value) await (accepted ? acceptMyOffer(id) : rejectMyOffer(id))
  resource.data.value = resource.data.value.map((item) =>
    item.id === id
      ? { ...item, status: decision, statusText: accepted ? '已接受' : '已拒绝' }
      : item,
  )
  ElMessage.success(accepted ? '已接受 Offer' : '已拒绝 Offer')
}
</script>
<template>
  <div class="role-portal">
    <div class="portal-toolbar">
      <div>
        <h2>我的 Offer</h2>
        <p>Offer 的接受或拒绝只能由你本人确认，HR 无法代替操作。</p>
      </div>
    </div>
    <section class="portal-panel">
      <div v-if="resource.loading.value" class="portal-loading">正在加载 Offer...</div>
      <div v-else-if="resource.error.value" class="portal-error">{{ resource.error.value }}</div>
      <div v-else-if="resource.data.value.length" class="portal-list">
        <article v-for="item in resource.data.value" :key="item.id" class="portal-row">
          <div class="portal-row__primary">
            <h3>{{ item.jobTitle }}</h3>
            <p>{{ item.department }} · {{ item.workLocation }}</p>
          </div>
          <div class="portal-row__cell">
            <strong>¥{{ item.salary.toLocaleString() }}</strong
            ><span>录用薪资</span>
          </div>
          <span class="rs-status-pill rs-status-pill--info">{{ item.statusText }}</span>
          <div v-if="item.status === 'SENT'" class="portal-actions">
            <el-button @click="decide(item.id, 'REJECTED')">拒绝</el-button
            ><el-button type="primary" @click="decide(item.id, 'ACCEPTED')">接受</el-button>
          </div>
          <span v-else />
        </article>
      </div>
      <div v-else class="portal-empty">暂无待处理 Offer。</div>
    </section>
  </div>
</template>
