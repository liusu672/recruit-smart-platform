<script setup lang="ts">
import HrStatusBadge from '@/components/hr/HrStatusBadge.vue'
import {
  canCancelOnboarding,
  canCompleteOnboarding,
  canReviewOnboardingMaterial,
  canStartOnboardingReview,
  getOnboardingStatusTone,
} from '@/config/onboardings'
import type { OnboardingRecord } from '@/types/onboarding'

const props = defineProps<{
  records: OnboardingRecord[]
  loading: boolean
  allowStartReview: boolean
}>()
const emit = defineEmits<{
  select: [record: OnboardingRecord]
  start: [record: OnboardingRecord]
  approve: [record: OnboardingRecord]
  reject: [record: OnboardingRecord]
  complete: [record: OnboardingRecord]
  cancel: [record: OnboardingRecord]
}>()

function formatDate(value: string) {
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(new Date(value))
}

function getProgress(record: OnboardingRecord) {
  if (record.status === 'CANCELED') return 0
  if (record.status === 'ONBOARDED') return 3
  if (record.status === 'APPROVED' || record.status === 'REVIEWING') return 2
  return 1
}

function hasPrimaryAction(record: OnboardingRecord) {
  return (
    (props.allowStartReview && canStartOnboardingReview(record.status)) ||
    canReviewOnboardingMaterial(record.status) ||
    canCompleteOnboarding(record.status, record.materialStatus)
  )
}
</script>

<template>
  <el-table
    :data="records"
    :loading="loading"
    row-key="id"
    height="520"
    empty-text="当前筛选条件下没有入职记录"
    @row-click="emit('select', $event)"
  >
    <el-table-column label="候选人" min-width="160"
      ><template #default="{ row }: { row: OnboardingRecord }"
        ><div class="person">
          <span>{{ row.candidateName.slice(0, 1) }}</span>
          <div>
            <strong>{{ row.candidateName }}</strong
            ><small>Offer #{{ row.offerId }}</small>
          </div>
        </div></template
      ></el-table-column
    >
    <el-table-column label="录用岗位" min-width="220"
      ><template #default="{ row }: { row: OnboardingRecord }"
        ><div class="stack">
          <strong>{{ row.jobTitle }}</strong
          ><small>{{ row.department }}</small>
        </div></template
      ></el-table-column
    >
    <el-table-column label="办理进度" min-width="230">
      <template #default="{ row }: { row: OnboardingRecord }">
        <div class="onboarding-progress">
          <div class="onboarding-progress__steps" aria-hidden="true">
            <span v-for="index in 3" :key="index" :class="{ active: index <= getProgress(row) }" />
          </div>
          <small>{{ row.currentStep }} · {{ row.materialStatusText }}</small>
        </div>
      </template>
    </el-table-column>
    <el-table-column label="预计入职" width="128"
      ><template #default="{ row }: { row: OnboardingRecord }"
        ><span class="number">{{ formatDate(row.entryDate) }}</span></template
      ></el-table-column
    >
    <el-table-column label="状态" width="100" align="center"
      ><template #default="{ row }: { row: OnboardingRecord }"
        ><HrStatusBadge
          :status="row.status"
          :label="row.statusText"
          :tone="getOnboardingStatusTone(row.status)" /></template
    ></el-table-column>
    <el-table-column label="操作" width="292" fixed="right" align="right"
      ><template #default="{ row }: { row: OnboardingRecord }"
        ><div class="actions" @click.stop>
          <el-button class="actions__view" link type="primary" @click="emit('select', row)"
            >查看详情</el-button
          >
          <el-button
            v-if="allowStartReview && canStartOnboardingReview(row.status)"
            class="actions__primary"
            link
            type="primary"
            @click="emit('start', row)"
            >开始审核</el-button
          >
          <template v-if="canReviewOnboardingMaterial(row.status)">
            <el-button class="actions__primary" link type="primary" @click="emit('approve', row)"
              >材料通过</el-button
            >
            <el-button class="actions__secondary" link type="danger" @click="emit('reject', row)"
              >退回补充</el-button
            >
          </template>
          <el-button
            v-if="canCompleteOnboarding(row.status, row.materialStatus)"
            class="actions__primary"
            link
            type="primary"
            @click="emit('complete', row)"
            >确认入职</el-button
          >
          <span
            v-if="!hasPrimaryAction(row)"
            class="hr-action-placeholder actions__primary"
            aria-hidden="true"
            >--</span
          >
          <span
            v-if="!canReviewOnboardingMaterial(row.status)"
            class="hr-action-placeholder actions__secondary"
            aria-hidden="true"
            >--</span
          >
          <el-button
            v-if="canCancelOnboarding(row.status)"
            class="actions__danger"
            link
            type="danger"
            @click="emit('cancel', row)"
            >取消办理</el-button
          >
          <span v-else class="hr-action-placeholder actions__danger" aria-hidden="true">--</span>
        </div></template
      ></el-table-column
    >
  </el-table>
</template>

<style scoped lang="scss">
.person,
.actions {
  align-items: center;
}

.person {
  display: flex;
}

.onboarding-progress {
  display: grid;
  gap: 6px;
}
.onboarding-progress small {
  color: var(--rs-text-tertiary);
}
.onboarding-progress__steps {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 4px;
}
.onboarding-progress__steps span {
  height: 4px;
  border-radius: var(--rs-radius-pill);
  background: var(--rs-surface-muted);
}
.onboarding-progress__steps span.active {
  background: var(--rs-blue-500);
}
.person {
  gap: var(--rs-space-2);
}
.person > span {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-selected);
  color: var(--rs-blue-700);
  font-weight: 700;
}
.person div,
.stack {
  display: grid;
}
.person small,
.stack small {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}
.number {
  font-variant-numeric: tabular-nums;
}
.actions {
  display: grid;
  justify-content: end;
  grid-template-columns: repeat(4, 56px);
  gap: var(--rs-space-2);
}
.actions__view {
  grid-column: 1;
}
.actions__primary {
  grid-column: 2;
}
.actions__secondary {
  grid-column: 3;
}
.actions__danger {
  grid-column: 4;
}
</style>
