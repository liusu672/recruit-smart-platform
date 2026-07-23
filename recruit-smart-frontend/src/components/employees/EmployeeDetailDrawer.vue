<script setup lang="ts">
import { BrainCircuit, CalendarDays, Mail, Phone, ShieldCheck } from 'lucide-vue-next'
import { computed, ref, watch } from 'vue'

import EmployeeBehaviorRecordsPanel from '@/components/employees/EmployeeBehaviorRecordsPanel.vue'
import EmployeeRiskHistoryPanel from '@/components/employees/EmployeeRiskHistoryPanel.vue'
import {
  employeeStatusOptions,
  getEmployeeStatusTone,
  getTurnoverRiskText,
  getTurnoverRiskTone,
} from '@/config/employees'
import type {
  EmployeeBehaviorRecord,
  EmployeeBehaviorSaveRequest,
  EmployeeRecord,
  EmployeeStatus,
} from '@/types/employee'
import type { TurnoverRiskHistoryResponse, TurnoverRiskResponse } from '@/types/ai'

const props = defineProps<{
  visible: boolean
  record: EmployeeRecord | undefined
  loading: boolean
  error: Error | null
  updating: boolean
  riskAnalysis: TurnoverRiskResponse | null
  analyzingRisk: boolean
  behaviorRecords: EmployeeBehaviorRecord[]
  loadingBehaviorRecords: boolean
  savingBehaviorRecord: boolean
  confirmingBehaviorRecord: boolean
  riskHistory: TurnoverRiskHistoryResponse[]
  loadingRiskHistory: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  updateStatus: [status: EmployeeStatus]
  assessRisk: []
  saveBehaviorRecord: [data: EmployeeBehaviorSaveRequest]
  confirmBehaviorRecord: [recordId: number]
}>()

const selectedStatus = ref<EmployeeStatus>('PROBATION')
const confirmedBehaviorCount = computed(
  () => props.behaviorRecords.filter((record) => record.recordStatus === 'CONFIRMED').length,
)

watch(
  () => props.record?.status,
  (status) => {
    if (status) selectedStatus.value = status
  },
  { immediate: true },
)

function formatDate(value: string | null, withTime = false) {
  if (!value) return '未评估'
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
    title="员工档案"
    size="540px"
    @update:model-value="emit('update:visible', $event)"
    ><el-skeleton v-if="loading" :rows="10" animated /><el-result
      v-else-if="error"
      icon="error"
      title="员工档案加载失败"
      :sub-title="error.message"
    />
    <div v-else-if="record" class="detail">
      <header>
        <span class="avatar">{{ record.name.slice(0, 1) }}</span>
        <div>
          <h3>{{ record.name }}</h3>
          <p>{{ record.employeeNo }}，{{ record.position }}</p>
        </div>
        <span :class="`rs-status-pill rs-status-pill--${getEmployeeStatusTone(record.status)}`">{{
          record.statusText
        }}</span>
      </header>
      <section class="facts">
        <div>
          <CalendarDays :size="18" :stroke-width="1.75" /><span>入职日期</span
          ><strong>{{ formatDate(record.entryDate) }}</strong>
        </div>
        <div>
          <ShieldCheck :size="18" :stroke-width="1.75" /><span>所属部门</span
          ><strong>{{ record.department }}</strong>
        </div>
        <div>
          <Phone :size="18" :stroke-width="1.75" /><span>联系电话</span
          ><strong>{{ record.phone || '未提供' }}</strong>
        </div>
        <div>
          <Mail :size="18" :stroke-width="1.75" /><span>联系邮箱</span
          ><strong>{{ record.email || '未提供' }}</strong>
        </div>
      </section>
      <section class="risk">
        <div>
          <BrainCircuit :size="18" :stroke-width="1.75" /><strong>AI 离职风险参考</strong
          ><span
            :class="`rs-status-pill rs-status-pill--${getTurnoverRiskTone(record.turnoverRiskLevel)}`"
            >{{ getTurnoverRiskText(record.turnoverRiskLevel) }}</span
          >
        </div>
        <p>风险结果只用于提醒 HR 线下关注，不会自动改变员工状态或触发人事动作。</p>
        <small>最近评估：{{ formatDate(record.riskAssessedAt, true) }}</small>
        <el-button
          size="small"
          :loading="analyzingRisk"
          :disabled="analyzingRisk || confirmedBehaviorCount < 3"
          @click="emit('assessRisk')"
        >
          重新评估风险
        </el-button>
        <small v-if="confirmedBehaviorCount < 3" class="risk-gate">
          至少需要 3 期已确认行为记录后才能评估。
        </small>
        <div v-if="riskAnalysis" class="risk-analysis">
          <strong>本次分析：{{ riskAnalysis.riskScore }} 分</strong>
          <p>{{ riskAnalysis.summary }}</p>
          <div
            v-if="
              riskAnalysis.sentimentLabel ||
              riskAnalysis.sentimentRiskScore !== null ||
              riskAnalysis.sentimentSummary
            "
            class="sentiment-card"
          >
            <strong>
              情感倾向：{{ riskAnalysis.sentimentLabel || '未返回' }}
              <span v-if="riskAnalysis.sentimentRiskScore !== null">
                / {{ riskAnalysis.sentimentRiskScore }} 分
              </span>
            </strong>
            <p>{{ riskAnalysis.sentimentSummary || '暂无情感摘要。' }}</p>
          </div>
          <h4>风险因子</h4>
          <ul v-if="riskAnalysis.riskReasons.length > 0">
            <li v-for="reason in riskAnalysis.riskReasons" :key="reason">{{ reason }}</li>
          </ul>
          <p v-else>暂无风险因子。</p>
          <h4>干预建议</h4>
          <p>{{ riskAnalysis.suggestions.join('；') || '暂无干预建议。' }}</p>
          <small>本次结果仅展示在当前页面，未自动修改员工状态。</small>
        </div>
      </section>
      <section>
        <h4>员工状态</h4>
        <div class="status-form">
          <el-select v-model="selectedStatus" aria-label="员工状态">
            <el-option
              v-for="option in employeeStatusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
          <el-button
            type="primary"
            :loading="updating"
            :disabled="selectedStatus === record.status"
            @click="emit('updateStatus', selectedStatus)"
          >
            更新状态
          </el-button>
        </div>
      </section>
      <EmployeeBehaviorRecordsPanel
        :records="behaviorRecords"
        :loading="loadingBehaviorRecords"
        :saving="savingBehaviorRecord"
        :confirming="confirmingBehaviorRecord"
        :disabled="record.status === 'LEFT'"
        @save="emit('saveBehaviorRecord', $event)"
        @confirm="emit('confirmBehaviorRecord', $event)"
      />
      <EmployeeRiskHistoryPanel :records="riskHistory" :loading="loadingRiskHistory" />
      <section>
        <h4>来源关联</h4>
        <dl>
          <div>
            <dt>候选人 ID</dt>
            <dd>#{{ record.candidateId }}</dd>
          </div>
          <div>
            <dt>入职记录 ID</dt>
            <dd>{{ record.onboardingId ? `#${record.onboardingId}` : '未关联' }}</dd>
          </div>
        </dl>
      </section>
    </div></el-drawer
  >
</template>

<style scoped lang="scss">
.detail {
  display: grid;
  gap: var(--rs-space-4);
}
header,
.risk > div {
  display: flex;
  align-items: center;
  gap: var(--rs-space-3);
}
header > div {
  min-width: 0;
  flex: 1;
}
h3,
p,
h4 {
  margin: 0;
}
header p,
section p,
section small {
  color: var(--rs-text-secondary);
}
.avatar {
  display: grid;
  width: 48px;
  height: 48px;
  place-items: center;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-action-primary);
  color: var(--rs-white);
  font-weight: 700;
}
.facts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
}
.facts > div {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 0 var(--rs-space-2);
  padding: var(--rs-space-3);
}
.facts > div:nth-child(odd) {
  border-right: 1px solid var(--rs-border-default);
}
.facts > div:nth-child(n + 3) {
  border-top: 1px solid var(--rs-border-default);
}
.facts svg {
  grid-row: 1/3;
  color: var(--rs-blue-700);
}
.facts span,
dt {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}
.facts strong {
  overflow-wrap: anywhere;
}
.detail > section:not(.facts) {
  padding-top: var(--rs-space-4);
  border-top: 1px solid var(--rs-border-default);
}
.risk {
  padding: var(--rs-space-3) !important;
  border: 1px solid var(--rs-blue-500) !important;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-selected);
}
.risk > div strong {
  flex: 1;
}
.risk p {
  margin-top: var(--rs-space-2);
}
.risk .el-button {
  margin-top: var(--rs-space-3);
}
.risk-gate {
  display: block;
  margin-top: var(--rs-space-2);
}
.risk-analysis {
  display: grid;
  gap: var(--rs-space-2);
  margin-top: var(--rs-space-3);
  padding-top: var(--rs-space-3);
  border-top: 1px solid var(--rs-border-default);
}
.risk-analysis ul {
  padding-left: var(--rs-space-4);
  margin: 0;
  color: var(--rs-text-secondary);
}
.sentiment-card {
  display: grid;
  gap: var(--rs-space-1);
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
.status-form {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: var(--rs-space-2);
}
h4 {
  margin-bottom: var(--rs-space-2);
  font-size: 14px;
}
dl {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--rs-space-3);
  margin: 0;
}
dd {
  margin: var(--rs-space-1) 0 0;
}
</style>
