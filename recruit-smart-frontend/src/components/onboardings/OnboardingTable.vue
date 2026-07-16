<script setup lang="ts">
import { CheckCircle2, Eye, FileCheck2, RotateCcw, UserRoundCheck } from 'lucide-vue-next'

import {
  canCompleteOnboarding,
  canReviewOnboardingMaterial,
  canStartOnboardingReview,
  getOnboardingStatusTone,
} from '@/config/onboardings'
import type { OnboardingRecord } from '@/types/onboarding'

defineProps<{ records: OnboardingRecord[]; loading: boolean }>()
const emit = defineEmits<{
  select: [record: OnboardingRecord]
  start: [record: OnboardingRecord]
  approve: [record: OnboardingRecord]
  reject: [record: OnboardingRecord]
  complete: [record: OnboardingRecord]
}>()

function formatDate(value: string) {
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(new Date(value))
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
    <el-table-column label="当前节点" min-width="132" prop="currentStep" />
    <el-table-column label="材料" width="100" align="center"
      ><template #default="{ row }: { row: OnboardingRecord }">{{
        row.materialStatusText
      }}</template></el-table-column
    >
    <el-table-column label="预计入职" width="128"
      ><template #default="{ row }: { row: OnboardingRecord }"
        ><span class="number">{{ formatDate(row.entryDate) }}</span></template
      ></el-table-column
    >
    <el-table-column label="状态" width="100" align="center"
      ><template #default="{ row }: { row: OnboardingRecord }"
        ><span :class="`rs-status-pill rs-status-pill--${getOnboardingStatusTone(row.status)}`">{{
          row.statusText
        }}</span></template
      ></el-table-column
    >
    <el-table-column label="操作" width="224" fixed="right" align="right"
      ><template #default="{ row }: { row: OnboardingRecord }"
        ><div class="actions" @click.stop>
          <el-tooltip content="查看入职详情"
            ><el-button circle :icon="Eye" aria-label="查看入职详情" @click="emit('select', row)"
          /></el-tooltip>
          <el-tooltip v-if="canStartOnboardingReview(row.status)" content="开始材料审核"
            ><el-button
              circle
              type="primary"
              :icon="FileCheck2"
              aria-label="开始材料审核"
              @click="emit('start', row)"
          /></el-tooltip>
          <template v-if="canReviewOnboardingMaterial(row.status)"
            ><el-tooltip content="材料通过"
              ><el-button
                circle
                type="success"
                :icon="CheckCircle2"
                aria-label="材料通过"
                @click="emit('approve', row)" /></el-tooltip
            ><el-tooltip content="退回补充"
              ><el-button
                circle
                type="danger"
                plain
                :icon="RotateCcw"
                aria-label="退回补充"
                @click="emit('reject', row)" /></el-tooltip
          ></template>
          <el-tooltip
            v-if="canCompleteOnboarding(row.status, row.materialStatus)"
            content="确认入职并生成员工档案"
            ><el-button
              circle
              type="primary"
              :icon="UserRoundCheck"
              aria-label="确认入职并生成员工档案"
              @click="emit('complete', row)"
          /></el-tooltip></div></template
    ></el-table-column>
  </el-table>
</template>

<style scoped lang="scss">
.person,
.actions {
  display: flex;
  align-items: center;
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
  justify-content: flex-end;
  gap: var(--rs-space-1);
}
</style>
