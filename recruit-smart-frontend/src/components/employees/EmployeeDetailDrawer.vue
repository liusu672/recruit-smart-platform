<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { BrainCircuit, CalendarDays, Mail, Phone, ShieldCheck } from 'lucide-vue-next'
import { ref, watch } from 'vue'

import type { EmployeeRiskDataUpdateRequest } from '@/api/employees'
import {
  employeeStatusOptions,
  getEmployeeStatusTone,
  getTurnoverRiskText,
  getTurnoverRiskTone,
} from '@/config/employees'
import type { EmployeeRecord, EmployeeStatus } from '@/types/employee'
import type { TurnoverRiskResponse } from '@/types/ai'

const props = defineProps<{
  visible: boolean
  record: EmployeeRecord | undefined
  loading: boolean
  error: Error | null
  updating: boolean
  riskAnalysis: TurnoverRiskResponse | null
  analyzingRisk: boolean
  savingRiskData: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  updateStatus: [status: EmployeeStatus]
  assessRisk: []
  saveRiskData: [data: EmployeeRiskDataUpdateRequest]
}>()

const selectedStatus = ref<EmployeeStatus>('PROBATION')
const performanceSummary = ref('')
const performanceScore = ref<number | null>(null)
const attendanceSummary = ref('')
const attendanceScore = ref<number | null>(null)
const satisfactionFeedback = ref('')
const satisfactionScore = ref<number | null>(null)

watch(
  () => props.record?.status,
  (status) => {
    if (status) selectedStatus.value = status
  },
  { immediate: true },
)

watch(
  () => props.record,
  (record) => {
    performanceSummary.value = record?.performanceSummary ?? ''
    performanceScore.value = record?.performanceScore ?? null
    attendanceSummary.value = record?.attendanceSummary ?? ''
    attendanceScore.value = record?.attendanceScore ?? null
    satisfactionFeedback.value = record?.satisfactionFeedback ?? ''
    satisfactionScore.value = record?.satisfactionScore ?? null
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

function isValidScore(value: number | null): value is number {
  return typeof value === 'number' && Number.isFinite(value) && value >= 0 && value <= 100
}

function submitRiskData() {
  const trimmedPerformanceSummary = performanceSummary.value.trim()
  const trimmedAttendanceSummary = attendanceSummary.value.trim()
  const trimmedSatisfactionFeedback = satisfactionFeedback.value.trim()

  if (!trimmedPerformanceSummary) {
    ElMessage.warning('请填写绩效摘要')
    return
  }
  if (!isValidScore(performanceScore.value)) {
    ElMessage.warning('请填写绩效评分')
    return
  }
  if (!trimmedAttendanceSummary) {
    ElMessage.warning('请填写考勤摘要')
    return
  }
  if (!isValidScore(attendanceScore.value)) {
    ElMessage.warning('请填写考勤评分')
    return
  }
  if (!trimmedSatisfactionFeedback) {
    ElMessage.warning('请填写满意度反馈')
    return
  }
  if (!isValidScore(satisfactionScore.value)) {
    ElMessage.warning('请填写满意度评分')
    return
  }

  emit('saveRiskData', {
    performanceSummary: trimmedPerformanceSummary,
    performanceScore: performanceScore.value,
    attendanceSummary: trimmedAttendanceSummary,
    attendanceScore: attendanceScore.value,
    satisfactionFeedback: trimmedSatisfactionFeedback,
    satisfactionScore: satisfactionScore.value,
  })
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
          :disabled="analyzingRisk"
          @click="emit('assessRisk')"
        >
          重新评估风险
        </el-button>
        <div v-if="riskAnalysis" class="risk-analysis">
          <strong>本次分析：{{ riskAnalysis.riskScore }} 分</strong>
          <p>{{ riskAnalysis.summary }}</p>
          <ul>
            <li v-for="reason in riskAnalysis.riskReasons" :key="reason">{{ reason }}</li>
          </ul>
          <p>{{ riskAnalysis.suggestions.join('；') }}</p>
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
      <section>
        <h4>风险数据维护</h4>
        <form class="risk-data-form" @submit.prevent="submitRiskData">
          <label>
            <span>绩效摘要</span>
            <el-input
              v-model="performanceSummary"
              type="textarea"
              :rows="3"
              maxlength="4000"
              show-word-limit
              placeholder="填写本期绩效表现和关键任务完成情况"
              :disabled="savingRiskData"
            />
          </label>
          <label>
            <span>绩效评分</span>
            <el-input-number
              v-model="performanceScore"
              :min="0"
              :max="100"
              :step="1"
              :precision="0"
              controls-position="right"
              :disabled="savingRiskData"
              aria-label="绩效评分"
            />
          </label>
          <label>
            <span>考勤摘要</span>
            <el-input
              v-model="attendanceSummary"
              type="textarea"
              :rows="3"
              maxlength="4000"
              show-word-limit
              placeholder="填写考勤表现和异常情况"
              :disabled="savingRiskData"
            />
          </label>
          <label>
            <span>考勤评分</span>
            <el-input-number
              v-model="attendanceScore"
              :min="0"
              :max="100"
              :step="1"
              :precision="0"
              controls-position="right"
              :disabled="savingRiskData"
              aria-label="考勤评分"
            />
          </label>
          <label>
            <span>满意度反馈</span>
            <el-input
              v-model="satisfactionFeedback"
              type="textarea"
              :rows="3"
              maxlength="4000"
              show-word-limit
              placeholder="填写员工满意度或访谈反馈"
              :disabled="savingRiskData"
            />
          </label>
          <label>
            <span>满意度评分</span>
            <el-input-number
              v-model="satisfactionScore"
              :min="0"
              :max="100"
              :step="1"
              :precision="0"
              controls-position="right"
              :disabled="savingRiskData"
              aria-label="满意度评分"
            />
          </label>
          <el-button
            type="primary"
            native-type="submit"
            :loading="savingRiskData"
            :disabled="savingRiskData"
          >
            保存风险数据
          </el-button>
        </form>
      </section>
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
.status-form {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: var(--rs-space-2);
}
.risk-data-form {
  display: grid;
  gap: var(--rs-space-3);
}
.risk-data-form label {
  display: grid;
  gap: var(--rs-space-2);
}
.risk-data-form label > span {
  color: var(--rs-text-secondary);
  font-size: 12px;
  font-weight: 600;
}
.risk-data-form :deep(.el-input-number) {
  width: 160px;
}
.risk-data-form .el-button {
  justify-self: start;
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
