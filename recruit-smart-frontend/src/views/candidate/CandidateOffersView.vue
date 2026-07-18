<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { CalendarDays, FileCheck2, MapPin, WalletCards } from 'lucide-vue-next'
import { ref } from 'vue'

import { acceptMyOffer, getMyOffers, rejectMyOffer } from '@/api/candidatePortal'
import CandidateEmptyState from '@/components/candidate/CandidateEmptyState.vue'
import CandidateErrorState from '@/components/candidate/CandidateErrorState.vue'
import CandidateListItem from '@/components/candidate/CandidateListItem.vue'
import CandidatePageHeader from '@/components/candidate/CandidatePageHeader.vue'
import CandidateStatusBadge from '@/components/candidate/CandidateStatusBadge.vue'
import { usePortalPagedResource } from '@/composables/usePortalPagedResource'
import { demoMyOffers } from '@/config/demoCandidatePortal'

const resource = usePortalPagedResource(getMyOffers, demoMyOffers)
const decidingId = ref<number | null>(null)

async function decide(id: number, decision: 'ACCEPTED' | 'REJECTED') {
  if (decidingId.value !== null) return
  const accepted = decision === 'ACCEPTED'
  try {
    await ElMessageBox.confirm(
      accepted ? '接受后将进入入职流程，确认接受？' : '拒绝后该 Offer 将结束，确认拒绝？',
      accepted ? '接受 Offer' : '拒绝 Offer',
      {
        confirmButtonText: accepted ? '确认接受' : '确认拒绝',
        cancelButtonText: '取消',
        type: accepted ? 'success' : 'warning',
      },
    )
    decidingId.value = id
    if (!resource.demoMode.value) await (accepted ? acceptMyOffer(id) : rejectMyOffer(id))
    resource.data.value.items = resource.data.value.items.map((item) =>
      item.id === id
        ? { ...item, status: decision, statusText: accepted ? '已接受' : '已拒绝' }
        : item,
    )
    ElMessage.success(accepted ? '已接受 Offer' : '已拒绝 Offer')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error instanceof Error ? error.message : 'Offer 处理失败')
  } finally {
    decidingId.value = null
  }
}
</script>

<template>
  <div class="candidate-page candidate-offers">
    <CandidatePageHeader
      title="我的 Offer"
      description="查看录用信息，并由你本人确认接受或拒绝。"
    />

    <div v-if="resource.loading.value" class="candidate-skeleton-list">
      <div class="candidate-skeleton-card"><el-skeleton :rows="4" animated /></div>
    </div>
    <CandidateErrorState
      v-else-if="resource.error.value"
      description="Offer 暂时无法加载，请稍后重试。"
      retryable
      @retry="resource.reload"
    />
    <div v-else-if="resource.data.value.items.length" class="candidate-list">
      <CandidateListItem v-for="item in resource.data.value.items" :key="item.id">
        <div class="candidate-offer-card__header">
          <div>
            <span>录用职位</span>
            <h2>{{ item.jobTitle }}</h2>
            <p>{{ item.department }}</p>
          </div>
          <CandidateStatusBadge :status="item.status" :label="item.statusText" />
        </div>
        <dl class="candidate-offer-card__facts">
          <div>
            <dt><WalletCards :size="16" />录用薪资</dt>
            <dd>¥{{ item.salary.toLocaleString() }}</dd>
          </div>
          <div>
            <dt><MapPin :size="16" />工作地点</dt>
            <dd>{{ item.workLocation }}</dd>
          </div>
          <div>
            <dt><CalendarDays :size="16" />预计入职</dt>
            <dd>{{ item.entryDate }}</dd>
          </div>
        </dl>
        <div class="candidate-offer-card__footer">
          <p>
            {{
              item.status === 'SENT' ? '请确认以上信息后再做决定。' : `当前状态：${item.statusText}`
            }}
          </p>
          <div v-if="item.status === 'SENT'" class="candidate-actions">
            <el-button text :disabled="decidingId !== null" @click="decide(item.id, 'REJECTED')"
              >拒绝 Offer</el-button
            >
            <el-button
              type="primary"
              :loading="decidingId === item.id"
              @click="decide(item.id, 'ACCEPTED')"
              >接受 Offer</el-button
            >
          </div>
        </div>
      </CandidateListItem>
    </div>
    <CandidateEmptyState
      v-else
      :icon="FileCheck2"
      title="暂无 Offer"
      description="新的 Offer 会显示在这里，你也可以继续查看其他招聘职位。"
    >
      <template #actions>
        <RouterLink to="/candidate/jobs"><el-button type="primary">浏览职位</el-button></RouterLink>
      </template>
    </CandidateEmptyState>

    <el-pagination
      v-if="resource.data.value.total > resource.query.pageSize"
      v-model:current-page="resource.query.page"
      class="portal-pagination"
      background
      layout="prev, pager, next"
      :page-size="resource.query.pageSize"
      :total="resource.data.value.total"
    />
  </div>
</template>

<style scoped lang="scss">
.candidate-offer-card__header,
.candidate-offer-card__footer {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--rs-space-4);
}
.candidate-offer-card__header span {
  color: var(--rs-blue-700);
  font-size: 13px;
  font-weight: 600;
}
.candidate-offer-card h2,
.candidate-offer-card p,
.candidate-offer-card dl,
.candidate-offer-card dd {
  margin: 0;
}
.candidate-offer-card__header h2 {
  margin: var(--rs-space-1) 0 0;
  font-size: 20px;
}
.candidate-offer-card__header p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}
.candidate-offer-card__facts {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  padding: var(--rs-space-6) 0;
  border-top: 1px solid var(--rs-border-default);
  border-bottom: 1px solid var(--rs-border-default);
}
.candidate-offer-card__facts > div {
  padding: 0 var(--rs-space-6);
}
.candidate-offer-card__facts > div:first-child {
  padding-left: 0;
}
.candidate-offer-card__facts > div + div {
  border-left: 1px solid var(--rs-border-default);
}
.candidate-offer-card__facts dt {
  display: flex;
  align-items: center;
  gap: var(--rs-space-1);
  color: var(--rs-text-tertiary);
  font-size: 12px;
}
.candidate-offer-card__facts dd {
  margin-top: var(--rs-space-2);
  font-size: 16px;
  font-weight: 600;
}
.candidate-offer-card__footer {
  align-items: center;
}
.candidate-offer-card__footer p {
  color: var(--rs-text-secondary);
}
</style>
