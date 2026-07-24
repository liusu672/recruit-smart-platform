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
import type { TurnoverRiskHistoryResponse, TurnoverRiskResponse } from '@/types/ai'
import type {
  EmployeeBehaviorRecord,
  EmployeeBehaviorSaveRequest,
  EmployeeRecord,
  EmployeeStatus,
} from '@/types/employee'

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
const riskDialogVisible = ref(false)
const confirmedBehaviorCount = computed(
  () => props.behaviorRecords.filter((record) => record.recordStatus === 'CONFIRMED').length,
)
const displayedRiskAnalysis = computed(() => props.riskAnalysis ?? props.riskHistory[0] ?? null)

watch(
  () => props.record?.status,
  (status) => {
    if (status) selectedStatus.value = status
  },
  { immediate: true },
)

watch(
  () => props.riskAnalysis,
  (analysis) => {
    if (analysis) riskDialogVisible.value = true
  },
)

watch(
  () => props.visible,
  (visible) => {
    if (!visible) riskDialogVisible.value = false
  },
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
        <div class="risk__heading">
          <BrainCircuit :size="18" :stroke-width="1.75" />
          <strong>AI 离职风险参考</strong>
          <span
            :class="`rs-status-pill rs-status-pill--${getTurnoverRiskTone(record.turnoverRiskLevel)}`"
          >
            {{ getTurnoverRiskText(record.turnoverRiskLevel) }}
          </span>
        </div>
        <p>风险结果只用于提醒 HR 线下关注，不会自动改变员工状态或触发人事动作。</p>
        <div class="risk__meta">
          <small>最近评估：{{ formatDate(record.riskAssessedAt, true) }}</small>
          <strong v-if="displayedRiskAnalysis"> {{ displayedRiskAnalysis.riskScore }} 分 </strong>
        </div>
        <div class="risk__actions">
          <el-button
            type="primary"
            plain
            :disabled="!displayedRiskAnalysis"
            @click="riskDialogVisible = true"
          >
            查看分析详情
          </el-button>
          <el-button
            :loading="analyzingRisk"
            :disabled="analyzingRisk || confirmedBehaviorCount < 3"
            @click="emit('assessRisk')"
          >
            重新评估风险
          </el-button>
        </div>
        <small v-if="confirmedBehaviorCount < 3" class="risk-gate">
          至少需要 3 期已确认行为记录后才能评估。
        </small>
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
  <el-dialog
    v-model="riskDialogVisible"
    title="AI 离职风险参考"
    width="min(760px, calc(100vw - 32px))"
    class="employee-risk-dialog"
    append-to-body
    destroy-on-close
  >
    <div v-if="record && displayedRiskAnalysis" class="risk-dialog">
      <header class="risk-dialog__header">
        <div>
          <span>员工风险辅助评估</span>
          <h3>{{ record.name }} · {{ record.position }}</h3>
        </div>
        <span
          :class="`rs-status-pill rs-status-pill--${getTurnoverRiskTone(
            displayedRiskAnalysis.riskLevel,
          )}`"
        >
          {{ getTurnoverRiskText(displayedRiskAnalysis.riskLevel) }}
        </span>
      </header>

      <section class="risk-dialog__overview">
        <div class="risk-dialog__score">
          <strong>{{ displayedRiskAnalysis.riskScore }}</strong>
          <span>/100</span>
          <small>本次风险分</small>
        </div>
        <div>
          <h4>综合分析</h4>
          <p>{{ displayedRiskAnalysis.summary || '暂无综合分析摘要。' }}</p>
        </div>
      </section>

      <div class="risk-dialog__notice">
        该结果仅用于提醒 HR 线下关注，不会自动改变员工状态或触发人事动作。
      </div>

      <div class="risk-dialog__grid">
        <section class="risk-dialog__card risk-dialog__card--sentiment">
          <h4>情感倾向</h4>
          <strong>
            {{ displayedRiskAnalysis.sentimentLabel || '未返回' }}
            <span v-if="displayedRiskAnalysis.sentimentRiskScore !== null">
              / {{ displayedRiskAnalysis.sentimentRiskScore }} 分
            </span>
          </strong>
          <p>{{ displayedRiskAnalysis.sentimentSummary || '暂无情感摘要。' }}</p>
        </section>

        <section class="risk-dialog__card">
          <h4>风险因子</h4>
          <ul v-if="displayedRiskAnalysis.riskReasons.length">
            <li v-for="reason in displayedRiskAnalysis.riskReasons" :key="reason">
              {{ reason }}
            </li>
          </ul>
          <p v-else>暂无风险因子。</p>
        </section>

        <section class="risk-dialog__card risk-dialog__card--wide">
          <h4>干预建议</h4>
          <ol v-if="displayedRiskAnalysis.suggestions.length">
            <li v-for="suggestion in displayedRiskAnalysis.suggestions" :key="suggestion">
              {{ suggestion }}
            </li>
          </ol>
          <p v-else>暂无干预建议。</p>
        </section>
      </div>

      <footer class="risk-dialog__meta">
        <span>最近评估：{{ formatDate(record.riskAssessedAt, true) }}</span>
        <span v-if="riskHistory[0]?.modelName || riskHistory[0]?.source">
          模型来源：{{ riskHistory[0]?.modelName || riskHistory[0]?.source }}
        </span>
      </footer>
    </div>
    <el-empty v-else description="暂无可查看的风险分析结果" :image-size="84" />

    <template #footer>
      <el-button @click="riskDialogVisible = false">关闭</el-button>
      <el-button
        type="primary"
        :loading="analyzingRisk"
        :disabled="analyzingRisk || confirmedBehaviorCount < 3"
        @click="emit('assessRisk')"
      >
        重新评估风险
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
.detail {
  display: grid;
  gap: var(--rs-space-4);
}

.detail > header,
.risk__heading,
.risk__meta,
.risk__actions {
  display: flex;
  align-items: center;
  gap: var(--rs-space-3);
}

.detail > header > div {
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

.risk__heading strong {
  flex: 1;
}

.risk > p {
  margin-top: var(--rs-space-2);
  line-height: 1.6;
}

.risk__meta {
  justify-content: space-between;
  margin-top: var(--rs-space-3);
}

.risk__meta strong {
  color: var(--rs-blue-700);
  font-size: 18px;
}

.risk__actions {
  flex-wrap: wrap;
  margin-top: var(--rs-space-3);
}

.risk-gate {
  display: block;
  margin-top: var(--rs-space-2);
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

.risk-dialog {
  display: grid;
  gap: var(--rs-space-4);
}

.risk-dialog__header {
  display: flex;
  align-items: flex-start;
  gap: var(--rs-space-3);
}

.risk-dialog__header > div {
  display: grid;
  flex: 1;
  gap: var(--rs-space-1);
}

.risk-dialog__header span:first-child,
.risk-dialog__meta {
  color: var(--rs-text-secondary);
  font-size: 13px;
}

.risk-dialog__overview {
  display: grid;
  grid-template-columns: 132px minmax(0, 1fr);
  gap: var(--rs-space-4);
  align-items: center;
  padding: var(--rs-space-4);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-md);
  background: linear-gradient(135deg, var(--rs-surface-selected), var(--rs-surface-primary));
}

.risk-dialog__overview p,
.risk-dialog__card p {
  color: var(--rs-text-secondary);
  line-height: 1.7;
}

.risk-dialog__score {
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: baseline;
  padding-right: var(--rs-space-4);
  border-right: 1px solid var(--rs-border-default);
}

.risk-dialog__score strong {
  color: var(--rs-blue-700);
  font-size: 40px;
  line-height: 1;
}

.risk-dialog__score span {
  color: var(--rs-text-secondary);
}

.risk-dialog__score small {
  grid-column: 1 / -1;
  margin-top: var(--rs-space-2);
  color: var(--rs-text-tertiary);
}

.risk-dialog__notice {
  padding: var(--rs-space-3);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
  color: var(--rs-text-secondary);
  line-height: 1.6;
}

.risk-dialog__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--rs-space-3);
}

.risk-dialog__card {
  padding: var(--rs-space-4);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.risk-dialog__card--sentiment {
  background: var(--rs-surface-selected);
}

.risk-dialog__card--sentiment > strong {
  display: block;
  margin-bottom: var(--rs-space-2);
  font-size: 18px;
}

.risk-dialog__card--wide {
  grid-column: 1 / -1;
}

.risk-dialog__card ul,
.risk-dialog__card ol {
  display: grid;
  gap: var(--rs-space-2);
  margin: 0;
  padding-left: 20px;
  color: var(--rs-text-secondary);
  line-height: 1.6;
}

.risk-dialog__meta {
  display: flex;
  justify-content: space-between;
  gap: var(--rs-space-3);
  padding-top: var(--rs-space-3);
  border-top: 1px solid var(--rs-border-default);
}

:global(.employee-risk-dialog .el-dialog__body) {
  padding-top: var(--rs-space-2);
}

@media (max-width: 640px) {
  .risk-dialog__overview,
  .risk-dialog__grid {
    grid-template-columns: 1fr;
  }

  .risk-dialog__score {
    padding: 0 0 var(--rs-space-3);
    border-right: 0;
    border-bottom: 1px solid var(--rs-border-default);
  }

  .risk-dialog__card--wide {
    grid-column: auto;
  }

  .risk-dialog__meta {
    flex-direction: column;
  }
}
</style>
