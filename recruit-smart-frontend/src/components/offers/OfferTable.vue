<script setup lang="ts">
import HrStatusBadge from '@/components/hr/HrStatusBadge.vue'
import {
  canEditOffer,
  canRevokeOffer,
  canSendOffer,
  formatOfferSalary,
  getOfferStatusTone,
} from '@/config/offers'
import type { OfferRecord } from '@/types/offer'

defineProps<{
  offers: OfferRecord[]
  loading: boolean
}>()

const emit = defineEmits<{
  select: [offer: OfferRecord]
  edit: [offer: OfferRecord]
  send: [offer: OfferRecord]
  revoke: [offer: OfferRecord]
}>()

function formatDate(value: string | null) {
  if (!value) return '待确认'
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(new Date(value))
}
</script>

<template>
  <el-table
    :data="offers"
    :loading="loading"
    row-key="id"
    height="520"
    empty-text="当前筛选条件下没有 Offer"
    @row-click="emit('select', $event)"
  >
    <el-table-column label="候选人" min-width="152">
      <template #default="{ row }: { row: OfferRecord }">
        <div class="offer-candidate">
          <span>{{ row.candidateName.slice(0, 1) }}</span>
          <div>
            <strong>{{ row.candidateName }}</strong>
            <small>投递 #{{ row.applicationId }}</small>
          </div>
        </div>
      </template>
    </el-table-column>
    <el-table-column label="录用职位" min-width="210">
      <template #default="{ row }: { row: OfferRecord }">
        <div class="offer-job">
          <strong>{{ row.jobTitle }}</strong>
          <small>{{ row.department }}</small>
        </div>
      </template>
    </el-table-column>
    <el-table-column label="月薪" width="132" align="right">
      <template #default="{ row }: { row: OfferRecord }">
        <strong class="offer-number">{{ formatOfferSalary(row.salary) }}</strong>
      </template>
    </el-table-column>
    <el-table-column label="预计入职" width="128">
      <template #default="{ row }: { row: OfferRecord }">
        <span class="offer-number">{{ formatDate(row.entryDate) }}</span>
      </template>
    </el-table-column>
    <el-table-column label="状态" width="104" align="center">
      <template #default="{ row }: { row: OfferRecord }">
        <HrStatusBadge
          :status="row.status"
          :label="row.statusText"
          :tone="getOfferStatusTone(row.status)"
        />
      </template>
    </el-table-column>
    <el-table-column label="发送时间" width="144">
      <template #default="{ row }: { row: OfferRecord }">
        <span class="offer-number">{{ row.sentAt ? formatDate(row.sentAt) : '尚未发送' }}</span>
      </template>
    </el-table-column>
    <el-table-column label="操作" width="220" fixed="right" align="right">
      <template #default="{ row }: { row: OfferRecord }">
        <div class="offer-actions" @click.stop>
          <el-button class="offer-actions__view" link type="primary" @click="emit('select', row)"
            >查看</el-button
          >
          <el-button
            v-if="canEditOffer(row.status)"
            class="offer-actions__edit"
            link
            type="primary"
            @click="emit('edit', row)"
            >编辑</el-button
          >
          <span v-else class="hr-action-placeholder offer-actions__edit" aria-hidden="true"
            >--</span
          >
          <el-button
            v-if="canSendOffer(row.status)"
            class="offer-actions__send"
            link
            type="primary"
            @click="emit('send', row)"
            >发送</el-button
          >
          <span v-else class="hr-action-placeholder offer-actions__send" aria-hidden="true"
            >--</span
          >
          <el-button
            v-if="canRevokeOffer(row.status)"
            class="offer-actions__revoke"
            link
            type="danger"
            @click="emit('revoke', row)"
            >撤回</el-button
          >
          <span v-else class="hr-action-placeholder offer-actions__revoke" aria-hidden="true"
            >--</span
          >
        </div>
      </template>
    </el-table-column>
  </el-table>
</template>

<style scoped lang="scss">
.offer-candidate,
.offer-actions {
  align-items: center;
}

.offer-candidate {
  display: flex;
  gap: var(--rs-space-2);
}

.offer-candidate > span {
  display: grid;
  width: 32px;
  height: 32px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-selected);
  color: var(--rs-blue-700);
  font-weight: 700;
}

.offer-candidate div,
.offer-job {
  display: grid;
}

.offer-candidate small,
.offer-job small {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}

.offer-number {
  font-variant-numeric: tabular-nums;
}

.offer-actions {
  display: grid;
  justify-content: end;
  grid-template-columns: repeat(4, 40px);
  gap: var(--rs-space-2);
}

.offer-actions__view {
  grid-column: 1;
}

.offer-actions__edit {
  grid-column: 2;
}

.offer-actions__send {
  grid-column: 3;
}

.offer-actions__revoke {
  grid-column: 4;
}
</style>
