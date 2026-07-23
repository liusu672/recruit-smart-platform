<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { computed, ref } from 'vue'

import type {
  EmployeeBehaviorRecord,
  EmployeeBehaviorSaveRequest,
} from '@/types/employee'

const props = defineProps<{
  records: EmployeeBehaviorRecord[]
  loading: boolean
  saving: boolean
  confirming: boolean
  disabled: boolean
}>()

const emit = defineEmits<{
  save: [data: EmployeeBehaviorSaveRequest]
  confirm: [recordId: number]
}>()

const confirmedCount = computed(
  () => props.records.filter((record) => record.recordStatus === 'CONFIRMED').length,
)

function toDateInput(date: Date) {
  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60_000)
  return local.toISOString().slice(0, 10)
}

function initialStartDate() {
  const date = new Date()
  date.setMonth(date.getMonth() - 1)
  return toDateInput(date)
}

const periodStart = ref(initialStartDate())
const periodEnd = ref(toDateInput(new Date()))
const performanceScore = ref<number | null>(null)
const performanceSummary = ref('')
const taskCompletionRate = ref<number | null>(null)
const lateCount = ref<number | null>(0)
const absenceDays = ref<number | null>(0)
const leaveDays = ref<number | null>(0)
const overtimeHours = ref<number | null>(0)
const attendanceScore = ref<number | null>(null)
const attendanceSummary = ref('')
const satisfactionScore = ref<number | null>(null)
const feedbackText = ref('')

function cleanText(value: string) {
  const trimmed = value.trim()
  return trimmed ? trimmed : null
}

function isScore(value: number | null): value is number {
  return typeof value === 'number' && Number.isFinite(value) && value >= 0 && value <= 100
}

function isOptionalNonNegative(value: number | null) {
  return value === null || (Number.isFinite(value) && value >= 0)
}

function isOptionalPercent(value: number | null) {
  return value === null || (Number.isFinite(value) && value >= 0 && value <= 100)
}

function resetForm() {
  periodStart.value = initialStartDate()
  periodEnd.value = toDateInput(new Date())
  performanceScore.value = null
  performanceSummary.value = ''
  taskCompletionRate.value = null
  lateCount.value = 0
  absenceDays.value = 0
  leaveDays.value = 0
  overtimeHours.value = 0
  attendanceScore.value = null
  attendanceSummary.value = ''
  satisfactionScore.value = null
  feedbackText.value = ''
}

function submit() {
  if (!periodStart.value || !periodEnd.value) {
    ElMessage.warning('请选择行为记录周期')
    return
  }
  if (periodStart.value > periodEnd.value) {
    ElMessage.warning('周期开始日期不能晚于结束日期')
    return
  }
  if (!isScore(performanceScore.value)) {
    ElMessage.warning('请填写 0-100 的绩效评分')
    return
  }
  if (!isScore(attendanceScore.value)) {
    ElMessage.warning('请填写 0-100 的考勤评分')
    return
  }
  if (!isScore(satisfactionScore.value)) {
    ElMessage.warning('请填写 0-100 的满意度评分')
    return
  }
  if (!isOptionalPercent(taskCompletionRate.value)) {
    ElMessage.warning('任务完成率需在 0-100 之间')
    return
  }
  if (
    !isOptionalNonNegative(lateCount.value) ||
    !isOptionalNonNegative(absenceDays.value) ||
    !isOptionalNonNegative(leaveDays.value) ||
    !isOptionalNonNegative(overtimeHours.value)
  ) {
    ElMessage.warning('考勤指标不能为负数')
    return
  }

  emit('save', {
    periodStart: periodStart.value,
    periodEnd: periodEnd.value,
    performanceScore: performanceScore.value,
    performanceSummary: cleanText(performanceSummary.value),
    taskCompletionRate: taskCompletionRate.value,
    lateCount: lateCount.value,
    absenceDays: absenceDays.value,
    leaveDays: leaveDays.value,
    overtimeHours: overtimeHours.value,
    attendanceScore: attendanceScore.value,
    attendanceSummary: cleanText(attendanceSummary.value),
    satisfactionScore: satisfactionScore.value,
    feedbackText: cleanText(feedbackText.value),
    sourceType: 'HR_INPUT',
  })
  resetForm()
}

function formatDate(value: string | null) {
  if (!value) return '未填写'
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
  }).format(new Date(value))
}

function formatPeriod(record: EmployeeBehaviorRecord) {
  return `${formatDate(record.periodStart)} - ${formatDate(record.periodEnd)}`
}

function formatScore(value: number | null) {
  return typeof value === 'number' ? `${value}分` : '未填'
}

function formatPercent(value: number | null) {
  return typeof value === 'number' ? `${value}%` : '未填'
}
</script>

<template>
  <section class="behavior-panel">
    <div class="section-heading">
      <div>
        <h4>行为记录</h4>
        <p>离职风险评估至少需要 3 期已确认记录。</p>
      </div>
      <el-tag :type="confirmedCount >= 3 ? 'success' : 'warning'" effect="plain">
        已确认 {{ confirmedCount }}/3
      </el-tag>
    </div>

    <el-skeleton v-if="loading" :rows="4" animated />
    <el-empty v-else-if="records.length === 0" description="暂无行为记录" :image-size="72" />
    <el-table v-else :data="records" size="small" border>
      <el-table-column label="周期" min-width="132">
        <template #default="{ row }">{{ formatPeriod(row) }}</template>
      </el-table-column>
      <el-table-column label="评分" min-width="136">
        <template #default="{ row }">
          绩效 {{ formatScore(row.performanceScore) }} / 考勤
          {{ formatScore(row.attendanceScore) }} / 满意度
          {{ formatScore(row.satisfactionScore) }}
        </template>
      </el-table-column>
      <el-table-column label="指标" min-width="128">
        <template #default="{ row }">
          完成率 {{ formatPercent(row.taskCompletionRate) }}，迟到 {{ row.lateCount ?? 0 }} 次
        </template>
      </el-table-column>
      <el-table-column label="状态" width="82">
        <template #default="{ row }">
          <el-tag :type="row.recordStatus === 'CONFIRMED' ? 'success' : 'warning'" effect="plain">
            {{ row.recordStatus === 'CONFIRMED' ? '已确认' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="86">
        <template #default="{ row }">
          <el-button
            v-if="row.recordStatus === 'DRAFT'"
            link
            type="primary"
            :loading="confirming"
            :disabled="confirming"
            @click="emit('confirm', row.id)"
          >
            确认
          </el-button>
          <span v-else>--</span>
        </template>
      </el-table-column>
    </el-table>

    <form class="behavior-form" @submit.prevent="submit">
      <h4>新增一期记录</h4>
      <div class="date-row">
        <el-date-picker
          v-model="periodStart"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="开始日期"
          :disabled="disabled || saving"
        />
        <el-date-picker
          v-model="periodEnd"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="结束日期"
          :disabled="disabled || saving"
        />
      </div>
      <div class="score-grid">
        <label>
          <span>绩效评分</span>
          <el-input-number
            v-model="performanceScore"
            :min="0"
            :max="100"
            :precision="0"
            controls-position="right"
            :disabled="disabled || saving"
          />
        </label>
        <label>
          <span>考勤评分</span>
          <el-input-number
            v-model="attendanceScore"
            :min="0"
            :max="100"
            :precision="0"
            controls-position="right"
            :disabled="disabled || saving"
          />
        </label>
        <label>
          <span>满意度评分</span>
          <el-input-number
            v-model="satisfactionScore"
            :min="0"
            :max="100"
            :precision="0"
            controls-position="right"
            :disabled="disabled || saving"
          />
        </label>
      </div>
      <div class="score-grid">
        <label>
          <span>任务完成率</span>
          <el-input-number
            v-model="taskCompletionRate"
            :min="0"
            :max="100"
            :precision="1"
            controls-position="right"
            :disabled="disabled || saving"
          />
        </label>
        <label>
          <span>迟到次数</span>
          <el-input-number
            v-model="lateCount"
            :min="0"
            :precision="0"
            controls-position="right"
            :disabled="disabled || saving"
          />
        </label>
        <label>
          <span>缺勤天数</span>
          <el-input-number
            v-model="absenceDays"
            :min="0"
            :precision="1"
            controls-position="right"
            :disabled="disabled || saving"
          />
        </label>
        <label>
          <span>请假天数</span>
          <el-input-number
            v-model="leaveDays"
            :min="0"
            :precision="1"
            controls-position="right"
            :disabled="disabled || saving"
          />
        </label>
        <label>
          <span>加班小时</span>
          <el-input-number
            v-model="overtimeHours"
            :min="0"
            :precision="1"
            controls-position="right"
            :disabled="disabled || saving"
          />
        </label>
      </div>
      <label>
        <span>绩效摘要</span>
        <el-input
          v-model="performanceSummary"
          type="textarea"
          :rows="2"
          maxlength="2000"
          show-word-limit
          :disabled="disabled || saving"
          placeholder="填写本期绩效变化、关键任务和风险信号"
        />
      </label>
      <label>
        <span>考勤摘要</span>
        <el-input
          v-model="attendanceSummary"
          type="textarea"
          :rows="2"
          maxlength="2000"
          show-word-limit
          :disabled="disabled || saving"
          placeholder="填写迟到、缺勤、请假或加班异常"
        />
      </label>
      <label>
        <span>访谈与满意度反馈</span>
        <el-input
          v-model="feedbackText"
          type="textarea"
          :rows="3"
          maxlength="5000"
          show-word-limit
          :disabled="disabled || saving"
          placeholder="填写访谈反馈、满意度变化和需要 HR 关注的描述"
        />
      </label>
      <el-button type="primary" native-type="submit" :loading="saving" :disabled="disabled || saving">
        保存为草稿
      </el-button>
      <small v-if="disabled">已离职员工不能新增行为记录。</small>
    </form>
  </section>
</template>

<style scoped lang="scss">
.behavior-panel {
  display: grid;
  gap: var(--rs-space-3);
}

.section-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--rs-space-3);
}

h4,
p {
  margin: 0;
}

p,
small {
  color: var(--rs-text-secondary);
}

.behavior-form {
  display: grid;
  gap: var(--rs-space-3);
  padding-top: var(--rs-space-3);
  border-top: 1px solid var(--rs-border-default);
}

.date-row,
.score-grid {
  display: grid;
  gap: var(--rs-space-2);
}

.date-row {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.score-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.behavior-form label {
  display: grid;
  gap: var(--rs-space-2);
}

.behavior-form label > span {
  color: var(--rs-text-secondary);
  font-size: 12px;
  font-weight: 600;
}

.behavior-form :deep(.el-input-number),
.behavior-form :deep(.el-date-editor) {
  width: 100%;
}

.behavior-form .el-button {
  justify-self: start;
}
</style>
