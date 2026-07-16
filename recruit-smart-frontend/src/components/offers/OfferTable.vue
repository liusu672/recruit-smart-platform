<script setup lang="ts">
import { Eye, Pencil, RotateCcw, Send } from 'lucide-vue-next'

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
        <span :class="`rs-status-pill rs-status-pill--${getOfferStatusTone(row.status)}`">
          {{ row.statusText }}
        </span>
      </template>
    </el-table-column>
    <el-table-column label="发送时间" width="144">
      <template #default="{ row }: { row: OfferRecord }">
        <span class="offer-number">{{ row.sentAt ? formatDate(row.sentAt) : '尚未发送' }}</span>
      </template>
    </el-table-column>
    <el-table-column label="操作" width="188" fixed="right" align="right">
      <template #default="{ row }: { row: OfferRecord }">
        <div class="offer-actions" @click.stop>
          <el-tooltip content="查看 Offer" placement="top">
            <el-button circle :icon="Eye" aria-label="查看 Offer" @click="emit('select', row)" />
          </el-tooltip>
          <el-tooltip v-if="canEditOffer(row.status)" content="编辑草稿" placement="top">
            <el-button circle :icon="Pencil" aria-label="编辑草稿" @click="emit('edit', row)" />
          </el-tooltip>
          <el-tooltip v-if="canSendOffer(row.status)" content="发送 Offer" placement="top">
            <el-button
              circle
              type="primary"
              :icon="Send"
              aria-label="发送 Offer"
              @click="emit('send', row)"
            />
          </el-tooltip>
          <el-tooltip v-if="canRevokeOffer(row.status)" content="撤回 Offer" placement="top">
            <el-button
              circle
              type="danger"
              plain
              :icon="RotateCcw"
              aria-label="撤回 Offer"
              @click="emit('revoke', row)"
            />
          </el-tooltip>
        </div>
      </template>
    </el-table-column>
  </el-table>
</template>

<style scoped lang="scss">
.offer-candidate,
.offer-actions {
  display: flex;
  align-items: center;
}

.offer-candidate {
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
  justify-content: flex-end;
  gap: var(--rs-space-1);
}
</style>
