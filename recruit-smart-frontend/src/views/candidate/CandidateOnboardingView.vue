<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { FileClock, MapPin } from 'lucide-vue-next'
import { ref } from 'vue'

import { getMyOnboardings, submitMyOnboardingMaterials } from '@/api/candidatePortal'
import CandidateEmptyState from '@/components/candidate/CandidateEmptyState.vue'
import CandidateErrorState from '@/components/candidate/CandidateErrorState.vue'
import CandidateListItem from '@/components/candidate/CandidateListItem.vue'
import CandidatePageHeader from '@/components/candidate/CandidatePageHeader.vue'
import CandidateProgressSteps from '@/components/candidate/CandidateProgressSteps.vue'
import CandidateStatusBadge from '@/components/candidate/CandidateStatusBadge.vue'
import { usePortalResource } from '@/composables/usePortalResource'
import { demoMyOnboardings } from '@/config/demoCandidatePortal'

const onboardingSteps = [
  { key: 'submit', label: '材料提交' },
  { key: 'review', label: '材料审核' },
  { key: 'complete', label: '入职完成' },
]
const resource = usePortalResource(getMyOnboardings, demoMyOnboardings)
const submittingId = ref<number | null>(null)

function currentStep(status: string, materialStatus: string) {
  if (status === 'ONBOARDED') return 2
  if (['REVIEWING', 'APPROVED'].includes(materialStatus)) return 1
  return 0
}

async function submit(id: number) {
  if (submittingId.value !== null) return
  try {
    await ElMessageBox.confirm('确认材料已准备完整并提交 HR 审核？', '提交入职材料', {
      confirmButtonText: '确认提交',
      cancelButtonText: '取消',
      type: 'info',
    })
    submittingId.value = id
    if (!resource.demoMode.value) await submitMyOnboardingMaterials(id)
    resource.data.value = resource.data.value.map((item) =>
      item.id === id
        ? { ...item, materialStatus: 'REVIEWING', materialStatusText: '审核中' }
        : item,
    )
    ElMessage.success('入职材料已提交')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error instanceof Error ? error.message : '材料提交失败')
  } finally {
    submittingId.value = null
  }
}
</script>

<template>
  <div class="candidate-page candidate-onboarding">
    <CandidatePageHeader
      title="入职资料"
      description="查看材料提交与审核进度，入职确认由 HR 完成。"
    />

    <div v-if="resource.loading.value" class="candidate-skeleton-list">
      <div class="candidate-skeleton-card"><el-skeleton :rows="4" animated /></div>
    </div>
    <CandidateErrorState
      v-else-if="resource.error.value"
      description="入职流程暂时无法加载，请稍后重试。"
      retryable
      @retry="resource.reload"
    />
    <div v-else-if="resource.data.value.length" class="candidate-list">
      <CandidateListItem v-for="item in resource.data.value" :key="item.id">
        <div class="candidate-onboarding-card__header">
          <div>
            <h2>{{ item.jobTitle }}</h2>
            <p><MapPin :size="14" />{{ item.department }}，预计 {{ item.entryDate }} 入职</p>
          </div>
          <CandidateStatusBadge :status="item.materialStatus" :label="item.materialStatusText" />
        </div>
        <CandidateProgressSteps
          :steps="onboardingSteps"
          :current-step="currentStep(item.status, item.materialStatus)"
          :terminal-state="item.status === 'CANCELED' ? 'neutral' : null"
          :terminal-label="item.status === 'CANCELED' ? '流程已取消' : undefined"
        />
        <div class="candidate-onboarding-card__footer">
          <div>
            <span>当前步骤</span><strong>{{ item.currentStep }}</strong>
          </div>
          <el-button
            v-if="['PENDING', 'REJECTED'].includes(item.materialStatus)"
            type="primary"
            :loading="submittingId === item.id"
            @click="submit(item.id)"
            >提交材料</el-button
          >
        </div>
      </CandidateListItem>
    </div>
    <CandidateEmptyState
      v-else
      :icon="FileClock"
      title="暂无入职流程"
      description="接受 Offer 后，入职流程和材料状态会显示在这里。"
    >
      <template #actions>
        <RouterLink to="/candidate/jobs"><el-button type="primary">浏览职位</el-button></RouterLink>
      </template>
    </CandidateEmptyState>
  </div>
</template>

<style scoped lang="scss">
.candidate-onboarding-card__header,
.candidate-onboarding-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--rs-space-4);
}
.candidate-onboarding-card h2,
.candidate-onboarding-card p {
  margin: 0;
}
.candidate-onboarding-card h2 {
  font-size: 18px;
}
.candidate-onboarding-card p {
  display: flex;
  align-items: center;
  gap: var(--rs-space-1);
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}
.candidate-onboarding-card__footer {
  padding-top: var(--rs-space-3);
  border-top: 1px solid var(--rs-border-default);
}
.candidate-onboarding-card__footer div {
  display: grid;
  gap: 2px;
}
.candidate-onboarding-card__footer span {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}
</style>
