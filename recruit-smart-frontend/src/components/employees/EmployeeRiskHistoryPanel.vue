<script setup lang="ts">
import { getTurnoverRiskText, getTurnoverRiskTone } from '@/config/employees'
import type { TurnoverRiskHistoryResponse } from '@/types/ai'

defineProps<{
  records: TurnoverRiskHistoryResponse[]
  loading: boolean
}>()

function formatDate(value: string | null, withTime = false) {
  if (!value) return '未记录'
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    ...(withTime ? { hour: '2-digit', minute: '2-digit', hour12: false } : {}),
  }).format(new Date(value))
}

function formatPeriod(record: TurnoverRiskHistoryResponse) {
  if (!record.periodStart && !record.periodEnd) return '未记录评估周期'
  return `${formatDate(record.periodStart)} - ${formatDate(record.periodEnd)}`
}
</script>

<template>
  <section class="risk-history">
    <div class="section-heading">
      <h4>历史风险结果</h4>
      <span>{{ records.length }} 条</span>
    </div>
    <el-skeleton v-if="loading" :rows="3" animated />
    <el-empty v-else-if="records.length === 0" description="暂无历史风险结果" :image-size="72" />
    <ol v-else>
      <li v-for="record in records" :key="record.id">
        <div class="history-title">
          <span :class="`rs-status-pill rs-status-pill--${getTurnoverRiskTone(record.riskLevel)}`">
            {{ getTurnoverRiskText(record.riskLevel) }}
          </span>
          <strong>{{ record.riskScore }} 分</strong>
          <small>{{ formatDate(record.generatedAt, true) }}</small>
        </div>
        <p>{{ record.summary || '暂无摘要。' }}</p>
        <dl>
          <div>
            <dt>评估周期</dt>
            <dd>{{ formatPeriod(record) }}</dd>
          </div>
          <div>
            <dt>情感倾向</dt>
            <dd>
              {{ record.sentimentLabel || '未返回' }}
              <span v-if="record.sentimentRiskScore !== null">
                / {{ record.sentimentRiskScore }} 分
              </span>
            </dd>
          </div>
          <div>
            <dt>模型来源</dt>
            <dd>{{ record.modelName || record.source || '未记录' }}</dd>
          </div>
          <div>
            <dt>行为记录</dt>
            <dd>{{ record.behaviorRecordIds.length }} 条</dd>
          </div>
        </dl>
        <p v-if="record.sentimentSummary" class="sentiment">{{ record.sentimentSummary }}</p>
      </li>
    </ol>
  </section>
</template>

<style scoped lang="scss">
.risk-history {
  display: grid;
  gap: var(--rs-space-3);
}

.section-heading,
.history-title {
  display: flex;
  align-items: center;
  gap: var(--rs-space-2);
}

.section-heading {
  justify-content: space-between;
}

h4,
p,
ol,
dl {
  margin: 0;
}

.section-heading span,
p,
small,
dt {
  color: var(--rs-text-secondary);
}

ol {
  display: grid;
  gap: var(--rs-space-3);
  padding: 0;
  list-style: none;
}

li {
  display: grid;
  gap: var(--rs-space-2);
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
}

.history-title strong {
  margin-right: auto;
}

dl {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--rs-space-2);
}

dt {
  font-size: 12px;
}

dd {
  margin: var(--rs-space-1) 0 0;
}

.sentiment {
  padding-top: var(--rs-space-2);
  border-top: 1px solid var(--rs-border-default);
}
</style>
