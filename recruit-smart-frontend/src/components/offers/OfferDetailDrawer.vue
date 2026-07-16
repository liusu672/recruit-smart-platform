<script setup lang="ts">
import {
  BriefcaseBusiness,
  CalendarDays,
  CircleDollarSign,
  Mail,
  MapPin,
  Pencil,
  Phone,
  RotateCcw,
  Send,
  UserRound,
} from 'lucide-vue-next'

import {
  canEditOffer,
  canRevokeOffer,
  canSendOffer,
  formatOfferSalary,
  getOfferStatusTone,
} from '@/config/offers'
import type { OfferRecord } from '@/types/offer'

defineProps<{
  visible: boolean
  offer: OfferRecord | undefined
  loading: boolean
  error: Error | null
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  edit: [offer: OfferRecord]
  send: [offer: OfferRecord]
  revoke: [offer: OfferRecord]
}>()

function formatDate(value: string | null, withTime = false) {
  if (!value) return '尚未记录'
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    ...(withTime ? { hour: '2-digit', minute: '2-digit', hour12: false } : {}),
  }).format(new Date(value))
}
</script>

<template>
  <el-drawer
    :model-value="visible"
    title="Offer 详情"
    size="520px"
    @update:model-value="emit('update:visible', $event)"
  >
    <div v-if="loading" class="offer-detail__loading">
      <el-skeleton :rows="10" animated />
    </div>
    <el-result v-else-if="error" icon="error" title="Offer 加载失败" :sub-title="error.message" />
    <div v-else-if="offer" class="offer-detail">
      <header class="offer-detail__header">
        <span class="offer-detail__avatar">{{ offer.candidateName.slice(0, 1) }}</span>
        <div>
          <h3>{{ offer.candidateName }}</h3>
          <p>{{ offer.jobTitle }}</p>
        </div>
        <span :class="`rs-status-pill rs-status-pill--${getOfferStatusTone(offer.status)}`">
          {{ offer.statusText }}
        </span>
      </header>

      <section class="offer-detail__facts">
        <div>
          <CircleDollarSign :size="18" :stroke-width="1.75" />
          <span>录用月薪</span>
          <strong>{{ formatOfferSalary(offer.salary) }}</strong>
        </div>
        <div>
          <CalendarDays :size="18" :stroke-width="1.75" />
          <span>预计入职</span>
          <strong>{{ formatDate(offer.entryDate) }}</strong>
        </div>
        <div>
          <MapPin :size="18" :stroke-width="1.75" />
          <span>工作地点</span>
          <strong>{{ offer.workLocation || '待填写' }}</strong>
        </div>
        <div>
          <BriefcaseBusiness :size="18" :stroke-width="1.75" />
          <span>试用期</span>
          <strong>{{ offer.probationMonths }} 个月</strong>
        </div>
      </section>

      <section class="offer-detail__section">
        <h4><UserRound :size="16" :stroke-width="1.75" />候选人与决策依据</h4>
        <dl>
          <div>
            <dt><Phone :size="14" :stroke-width="1.75" />联系电话</dt>
            <dd>{{ offer.candidatePhone || '未提供' }}</dd>
          </div>
          <div>
            <dt><Mail :size="14" :stroke-width="1.75" />联系邮箱</dt>
            <dd>{{ offer.candidateEmail || '未提供' }}</dd>
          </div>
          <div>
            <dt>面试评分</dt>
            <dd>{{ offer.interviewScore ?? '暂无' }}</dd>
          </div>
          <div>
            <dt>面试建议</dt>
            <dd>{{ offer.interviewSuggestion || '暂无' }}</dd>
          </div>
        </dl>
      </section>

      <section class="offer-detail__section">
        <h4>方案备注</h4>
        <p>{{ offer.remark || '暂无备注' }}</p>
      </section>

      <section class="offer-detail__section">
        <h4>操作记录</h4>
        <el-timeline>
          <el-timeline-item
            v-for="event in offer.timeline"
            :key="event.id"
            :timestamp="formatDate(event.occurredAt, true)"
            placement="top"
          >
            <strong>{{ event.title }}</strong>
            <p>{{ event.description }}</p>
            <small>{{ event.actorName }}</small>
          </el-timeline-item>
        </el-timeline>
      </section>
    </div>

    <template v-if="offer" #footer>
      <div class="offer-detail__footer">
        <el-button v-if="canEditOffer(offer.status)" :icon="Pencil" @click="emit('edit', offer)">
          编辑草稿
        </el-button>
        <el-button
          v-if="canRevokeOffer(offer.status)"
          type="danger"
          plain
          :icon="RotateCcw"
          @click="emit('revoke', offer)"
        >
          撤回 Offer
        </el-button>
        <el-button
          v-if="canSendOffer(offer.status)"
          type="primary"
          :icon="Send"
          @click="emit('send', offer)"
        >
          发送 Offer
        </el-button>
      </div>
    </template>
  </el-drawer>
</template>

<style scoped lang="scss">
.offer-detail {
  display: grid;
  gap: var(--rs-space-4);
}

.offer-detail__header,
.offer-detail__facts > div,
.offer-detail__section h4,
.offer-detail__section dt,
.offer-detail__footer {
  display: flex;
  align-items: center;
}

.offer-detail__header {
  gap: var(--rs-space-3);
}

.offer-detail__header > div {
  min-width: 0;
  flex: 1;
}

.offer-detail__header h3,
.offer-detail__header p,
.offer-detail__section h4,
.offer-detail__section p {
  margin: 0;
}

.offer-detail__header p,
.offer-detail__section p,
.offer-detail__section small {
  color: var(--rs-text-secondary);
}

.offer-detail__avatar {
  display: grid;
  width: 48px;
  height: 48px;
  place-items: center;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-action-primary);
  color: var(--rs-white);
  font-size: 18px;
  font-weight: 700;
}

.offer-detail__facts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
}

.offer-detail__facts > div {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 0 var(--rs-space-2);
  min-height: 80px;
  padding: var(--rs-space-3);
}

.offer-detail__facts > div:nth-child(odd) {
  border-right: 1px solid var(--rs-border-default);
}

.offer-detail__facts > div:nth-child(n + 3) {
  border-top: 1px solid var(--rs-border-default);
}

.offer-detail__facts svg {
  grid-row: 1 / 3;
  color: var(--rs-blue-700);
}

.offer-detail__facts span {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}

.offer-detail__facts strong {
  font-variant-numeric: tabular-nums;
}

.offer-detail__section {
  padding-top: var(--rs-space-4);
  border-top: 1px solid var(--rs-border-default);
}

.offer-detail__section h4 {
  gap: var(--rs-space-2);
  margin-bottom: var(--rs-space-3);
  font-size: 14px;
}

.offer-detail__section dl {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--rs-space-3);
  margin: 0;
}

.offer-detail__section dl div {
  display: grid;
  gap: var(--rs-space-1);
}

.offer-detail__section dt {
  gap: var(--rs-space-1);
  color: var(--rs-text-tertiary);
  font-size: 12px;
}

.offer-detail__section dd {
  margin: 0;
  overflow-wrap: anywhere;
}

.offer-detail__section :deep(.el-timeline) {
  padding-left: var(--rs-space-2);
}

.offer-detail__section :deep(.el-timeline-item__content) p {
  margin-top: var(--rs-space-1);
}

.offer-detail__footer {
  justify-content: flex-end;
  gap: var(--rs-space-2);
}

.offer-detail__loading {
  padding: var(--rs-space-4);
}
</style>
