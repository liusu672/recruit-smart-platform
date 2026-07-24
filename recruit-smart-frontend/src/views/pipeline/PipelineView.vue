<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { Columns3, List } from 'lucide-vue-next'
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useRouter } from 'vue-router'

import ApplicationDetailDrawer from '@/components/pipeline/ApplicationDetailDrawer.vue'
import HrErrorState from '@/components/hr/HrErrorState.vue'
import HrFilterBar from '@/components/hr/HrFilterBar.vue'
import HrPageHeader from '@/components/hr/HrPageHeader.vue'
import PipelineBoard from '@/components/pipeline/PipelineBoard.vue'
import PipelineTable from '@/components/pipeline/PipelineTable.vue'
import ScreeningDecisionDialog from '@/components/pipeline/ScreeningDecisionDialog.vue'
import {
  applicationStatusOptions,
  defaultPipelineStageKey,
  getApplicationStatusText,
  getPipelineStage,
  getPipelineStageKey,
  isPipelineStageKey,
  pipelineStages,
} from '@/config/pipeline'
import { getInterviewArrangementText, interviewRoundOptions } from '@/config/interviews'
import { useRecruitmentPipeline } from '@/composables/useRecruitmentPipeline'
import { useHrUrlFilters } from '@/composables/useHrUrlFilters'
import { getOrCreateConversation } from '@/api/messages'
import { useSessionStore } from '@/stores/session'
import type { ApplicationStatus } from '@/types/candidate'
import type { InterviewAssignmentRequest, InterviewRound } from '@/types/interview'
import type {
  PipelineApplicationDetail,
  PipelineApplicationSummary,
  PipelineStageKey,
  PipelineViewMode,
  ScreeningDecision,
} from '@/types/pipeline'

const session = useSessionStore()
const route = useRoute()
const router = useRouter()
const {
  query,
  demoMode,
  selectedApplicationId,
  pipelineQuery,
  stageCountsQuery,
  detailQuery,
  interviewerQuery,
  statusMutation,
  reviewMutation,
  rejectMutation,
  interviewAssignmentMutation,
  interviewReassignmentMutation,
  interviewCancellationMutation,
  aiMatchMutation,
  applyFilters,
  useApiData,
  selectApplication,
  closeDetail,
} = useRecruitmentPipeline()
const urlFilters = useHrUrlFilters(['keyword', 'jobId', 'stage', 'status', 'page', 'pageSize'])

const viewMode = ref<PipelineViewMode>('BOARD')
const decisionDialogVisible = ref(false)
const pendingDecision = ref<ScreeningDecision>('PASS')
const pendingApplication = ref<PipelineApplicationSummary | null>(null)
const rejectDialogVisible = ref(false)
const rejectingApplication = ref<PipelineApplicationDetail | null>(null)
const assignmentDialogVisible = ref(false)
const assignmentMode = ref<'CREATE' | 'REASSIGN'>('CREATE')
const assignmentApplication = ref<PipelineApplicationDetail | null>(null)
const assignmentForm = reactive<{
  interviewerId: number | null
  round: InterviewRound
}>({
  interviewerId: null,
  round: 'FIRST',
})
const routeStatus = urlFilters.readString('status')
const initialStatus: ApplicationStatus | '' = isApplicationStatus(routeStatus) ? routeStatus : ''
const activeStageKey = ref<PipelineStageKey>(
  resolveInitialStage(urlFilters.readString('stage'), initialStatus),
)
const filterForm = reactive<{
  keyword: string
  jobId: number | null
  status: ApplicationStatus | ''
}>({
  keyword: urlFilters.readString('keyword'),
  jobId: null,
  status: initialStatus,
})
Object.assign(query, {
  keyword: filterForm.keyword,
  stage: activeStageKey.value,
  status: filterForm.status,
  page: urlFilters.readNumber('page', 1),
  pageSize: urlFilters.readNumber('pageSize', 20),
})

const applications = computed(() => pipelineQuery.data.value?.items ?? [])
const total = computed(() => pipelineQuery.data.value?.total ?? 0)
const canManage = computed(() => session.currentRole === 'HR')
const listError = computed(() => pipelineQuery.error.value as Error | null)
const detailError = computed(() => detailQuery.error.value as Error | null)
const detailVisible = computed({
  get: () => selectedApplicationId.value !== null,
  set: (value) => {
    if (!value) closeDetail()
  },
})
const jobOptions = computed(() => {
  const values = new Map<number, string>()
  for (const application of applications.value) values.set(application.jobId, application.jobTitle)
  return [...values.entries()].map(([value, label]) => ({ value, label }))
})
const activeStage = computed(() => getPipelineStage(activeStageKey.value))
const activeStageStatusOptions = computed(() =>
  applicationStatusOptions.filter((option) => activeStage.value.statuses.includes(option.value)),
)
const stageCountMap = computed(
  () => new Map((stageCountsQuery.data.value ?? []).map((item) => [item.stage, item.count])),
)
const stageSummary = computed(() =>
  pipelineStages.map((stage) => ({
    ...stage,
    count: stageCountMap.value.get(stage.key) ?? 0,
  })),
)
const stageCountTotal = computed(() =>
  stageSummary.value.reduce((sum, stage) => sum + stage.count, 0),
)
const currentListLabel = computed(() =>
  filterForm.status ? getApplicationStatusText(filterForm.status) : activeStage.value.label,
)
const interviewerOptions = computed(() => interviewerQuery.data.value ?? [])
const assignmentRequiredRounds = computed(() => {
  const rounds = assignmentApplication.value?.requiredInterviewRounds ?? 1
  return Math.min(Math.max(rounds, 1), 3)
})
const assignmentRoundOptions = computed(() =>
  interviewRoundOptions.slice(0, assignmentRequiredRounds.value),
)
const assignmentActionText = computed(() =>
  getInterviewArrangementText(assignmentForm.round),
)
const assignmentDialogTitle = computed(() =>
  assignmentMode.value === 'CREATE'
    ? assignmentActionText.value
    : '\u91cd\u65b0\u6307\u6d3e\u9762\u8bd5\u5b98',
)
const assignmentContextText = computed(() => {
  if (assignmentMode.value !== 'CREATE') {
    return assignmentApplication.value?.interview?.roundText ?? '\u9762\u8bd5'
  }
  return `\u672c\u5c97\u4f4d\u5171 ${assignmentRequiredRounds.value} \u8f6e\uff0c${assignmentActionText.value}`
})
const assignmentSubmitText = computed(() =>
  assignmentMode.value === 'CREATE'
    ? `\u786e\u8ba4${assignmentActionText.value}`
    : '\u786e\u8ba4\u91cd\u65b0\u6307\u6d3e',
)
const assignmentSubmitting = computed(
  () =>
    interviewAssignmentMutation.isPending.value || interviewReassignmentMutation.isPending.value,
)
const activeFilterCount = computed(
  () => [filterForm.keyword.trim(), filterForm.jobId, filterForm.status].filter(Boolean).length,
)

function parseRouteId(value: unknown) {
  const raw = Array.isArray(value) ? value[0] : value
  const id = Number(raw)
  return Number.isFinite(id) && id > 0 ? id : null
}

function isApplicationStatus(value: string): value is ApplicationStatus {
  return applicationStatusOptions.some((option) => option.value === value)
}

function resolveInitialStage(stage: string, status: ApplicationStatus | ''): PipelineStageKey {
  if (status) return getPipelineStageKey(status)
  if (isPipelineStageKey(stage)) return stage
  return defaultPipelineStageKey
}

watch(
  () => route.query.applicationId,
  (value) => {
    const applicationId = parseRouteId(value)
    if (applicationId !== null) selectApplication(applicationId)
  },
  { immediate: true },
)

watch(
  () => route.query.jobId,
  (value) => {
    const jobId = parseRouteId(value)
    filterForm.jobId = jobId
    applyFilters({
      keyword: filterForm.keyword.trim(),
      jobId,
      stage: activeStageKey.value,
      status: filterForm.status as typeof query.status,
    })
  },
  { immediate: true },
)

function selectStage(stageKey: PipelineStageKey) {
  activeStageKey.value = stageKey
  if (filterForm.status && getPipelineStageKey(filterForm.status) !== stageKey) {
    filterForm.status = ''
  }
  applyFilters({
    keyword: filterForm.keyword.trim(),
    jobId: filterForm.jobId,
    stage: stageKey,
    status: filterForm.status,
  })
  syncUrl(1, stageKey)
}

function submitFilters() {
  if (filterForm.status && getPipelineStageKey(filterForm.status) !== activeStageKey.value) {
    activeStageKey.value = getPipelineStageKey(filterForm.status)
  }
  applyFilters({
    keyword: filterForm.keyword.trim(),
    jobId: filterForm.jobId,
    stage: activeStageKey.value,
    status: filterForm.status as typeof query.status,
  })
  syncUrl(1, activeStageKey.value)
}

function clearFilters() {
  Object.assign(filterForm, { keyword: '', jobId: null, status: '' })
  applyFilters({
    keyword: '',
    jobId: null,
    stage: activeStageKey.value,
    status: '',
  })
  syncUrl(1, activeStageKey.value)
}

function syncUrl(page = query.page, stage = activeStageKey.value) {
  urlFilters.sync({
    keyword: filterForm.keyword.trim(),
    jobId: filterForm.jobId,
    stage: stage !== defaultPipelineStageKey || filterForm.status ? stage : null,
    status: filterForm.status,
    page: page > 1 ? page : null,
    pageSize: query.pageSize !== 20 ? query.pageSize : null,
  })
}

watch([() => query.page, () => query.pageSize], () => syncUrl())

function refreshPipeline() {
  void Promise.all([pipelineQuery.refetch(), stageCountsQuery.refetch()])
}

async function startScreening(application: PipelineApplicationSummary) {
  try {
    await ElMessageBox.confirm(
      `确认由 HR 开始筛选“${application.candidateName}”的投递记录？`,
      '开始筛选',
      { confirmButtonText: '确认开始', cancelButtonText: '取消', type: 'info' },
    )
    await statusMutation.mutateAsync({
      id: application.id,
      data: {
        status: 'SCREENING',
      },
    })
    ElMessage.success('投递记录已进入 HR 筛选')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error instanceof Error ? error.message : '状态更新失败')
  }
}

function openReview(decision: ScreeningDecision, application: PipelineApplicationDetail) {
  pendingDecision.value = decision
  pendingApplication.value = application
  decisionDialogVisible.value = true
}

function openApplicationReject(application: PipelineApplicationDetail) {
  rejectingApplication.value = application
  rejectDialogVisible.value = true
}

function openInterviewAssignment(
  application: PipelineApplicationDetail,
  mode: 'CREATE' | 'REASSIGN',
  round?: InterviewRound,
) {
  assignmentMode.value = mode
  assignmentApplication.value = application
  assignmentForm.interviewerId = application.interview?.interviewerId ?? null
  assignmentForm.round = round ?? application.interview?.round ?? 'FIRST'
  closeDetail()
  assignmentDialogVisible.value = true
  void interviewerQuery.refetch()
}

async function submitInterviewAssignment() {
  const application = assignmentApplication.value
  const interviewerId = assignmentForm.interviewerId
  if (!application || interviewerId === null) {
    ElMessage.warning('请选择面试官')
    return
  }

  try {
    if (assignmentMode.value === 'CREATE') {
      const data: InterviewAssignmentRequest = {
        applicationId: application.id,
        interviewerId,
        round: assignmentForm.round,
      }
      await interviewAssignmentMutation.mutateAsync({ id: application.id, data })
      ElMessage.success('面试官已指派，等待面试官预约')
    } else if (application.interview) {
      await interviewReassignmentMutation.mutateAsync({
        applicationId: application.id,
        interviewId: application.interview.id,
        data: { interviewerId },
      })
      ElMessage.success('面试官已重新指派')
    }
    assignmentDialogVisible.value = false
    assignmentApplication.value = null
    selectApplication(application.id)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '面试官指派失败')
  }
}

async function cancelInterview(application: PipelineApplicationDetail) {
  const interview = application.interview
  if (!interview) return

  try {
    await ElMessageBox.confirm(
      `取消“${application.candidateName}”的${interview.roundText}面试后，候选人将无法查看该安排。`,
      '取消面试',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '保留安排',
        type: 'warning',
      },
    )
    await interviewCancellationMutation.mutateAsync({
      applicationId: application.id,
      interviewId: interview.id,
    })
    ElMessage.success('面试已取消')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error instanceof Error ? error.message : '取消面试失败')
  }
}

async function submitReview(payload: {
  decision: ScreeningDecision
  rejectReasonCode: string
  note: string
}) {
  const application = pendingApplication.value
  if (!application) return

  try {
    await reviewMutation.mutateAsync({
      id: application.id,
      data: payload,
    })
    decisionDialogVisible.value = false
    ElMessage.success('筛选结论已保存')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '筛选结论保存失败')
  }
}

async function submitApplicationReject(payload: {
  decision: ScreeningDecision
  rejectReasonCode: string
  note: string
}) {
  const application = rejectingApplication.value
  if (!application) return

  try {
    await rejectMutation.mutateAsync({
      id: application.id,
      data: {
        reasonCode: payload.rejectReasonCode,
        reason: payload.note,
      },
    })
    rejectDialogVisible.value = false
    rejectingApplication.value = null
    closeDetail()
    ElMessage.success('候选人已淘汰，投递流程已结束')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '淘汰候选人失败')
  }
}

async function contactCandidate(applicationId: number) {
  try {
    if (demoMode.value) {
      await router.push('/hr/messages')
      return
    }
    const conversationId = await getOrCreateConversation(applicationId)
    await router.push({ path: '/hr/messages', query: { conversationId: String(conversationId) } })
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '消息会话创建失败')
  }
}

async function generateAiMatch(application: PipelineApplicationDetail) {
  try {
    await aiMatchMutation.mutateAsync(application.id)
    ElMessage.success('AI 匹配分析已生成')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : 'AI 匹配分析生成失败')
  }
}
</script>

<template>
  <div class="pipeline-view">
    <HrPageHeader
      title="招聘流程"
      description="推进进行中的候选人，并将录用与结束记录从主流程中清晰分开。"
    >
      <template #actions>
        <el-radio-group v-model="viewMode" aria-label="招聘流程视图">
          <el-radio-button value="BOARD"
            ><Columns3 :size="15" :stroke-width="1.75" />看板</el-radio-button
          >
          <el-radio-button value="TABLE"
            ><List :size="15" :stroke-width="1.75" />表格</el-radio-button
          >
        </el-radio-group>
      </template>
    </HrPageHeader>

    <section v-if="demoMode" class="pipeline-source" aria-live="polite">
      <span><strong>演示数据模式</strong>：所有筛选结论只保存在当前浏览器内存中。</span>
      <el-button link @click="useApiData">切换到真实数据</el-button>
    </section>

    <HrFilterBar
      :loading="pipelineQuery.isFetching.value"
      :active-count="activeFilterCount"
      :reset-disabled="activeFilterCount === 0"
      @submit="submitFilters"
      @reset="clearFilters"
      @refresh="refreshPipeline"
    >
      <el-input v-model="filterForm.keyword" placeholder="候选人、手机号或职位" clearable />
      <el-select v-model="filterForm.jobId" placeholder="全部职位" clearable>
        <el-option
          v-for="option in jobOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
      <el-select v-model="filterForm.status" placeholder="全部状态" clearable>
        <el-option
          v-for="option in activeStageStatusOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
    </HrFilterBar>

    <section class="pipeline-stage-entry" aria-label="流程阶段入口">
      <button
        v-for="stage in stageSummary"
        :key="stage.key"
        class="pipeline-stage-entry__item"
        :class="{ 'pipeline-stage-entry__item--active': stage.key === activeStageKey }"
        type="button"
        :aria-pressed="stage.key === activeStageKey"
        @click="selectStage(stage.key)"
      >
        <span class="pipeline-stage-entry__label">{{ stage.label }}</span>
        <strong>{{ stage.count }}</strong>
        <span>{{ stage.description }}</span>
      </button>
      <span class="pipeline-stage-entry__total">共 {{ stageCountTotal }} 条投递</span>
    </section>

    <HrErrorState
      v-if="listError && !demoMode"
      title="招聘流程暂时无法加载"
      description="请稍后重试。如果问题持续存在，请联系系统管理员。"
      :loading="pipelineQuery.isFetching.value"
      @retry="pipelineQuery.refetch()"
    />

    <section
      v-else
      class="pipeline-workspace"
      :class="`pipeline-workspace--${viewMode.toLowerCase()}`"
    >
      <header class="pipeline-workspace__header">
        <div>
          <strong>{{ activeStage.label }}</strong>
          <span>{{ activeStage.description }}</span>
        </div>
        <span>{{ currentListLabel }} · {{ total }} 条</span>
      </header>
      <div v-if="pipelineQuery.isLoading.value" class="pipeline-loading">
        <el-skeleton :rows="8" animated />
      </div>
      <PipelineBoard
        v-else-if="viewMode === 'BOARD'"
        :applications="applications"
        :stage-label="currentListLabel"
        @select="selectApplication"
      />
      <PipelineTable
        v-else
        :applications="applications"
        :loading="pipelineQuery.isLoading.value"
        @select="selectApplication"
      />
    </section>

    <footer v-if="viewMode === 'TABLE' && !listError" class="pipeline-pagination">
      <span>{{ currentListLabel }}共 {{ total }} 条投递</span>
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        background
        layout="sizes, prev, pager, next"
        :page-sizes="[20, 40, 80]"
        :total="total"
      />
    </footer>

    <ApplicationDetailDrawer
      v-model:visible="detailVisible"
      :application="detailQuery.data.value"
      :loading="detailQuery.isLoading.value"
      :error="detailError"
      :can-manage="canManage"
      :demo-mode="demoMode"
      :matching-ai="aiMatchMutation.isPending.value"
      @start-screening="startScreening"
      @review="openReview"
      @assign-next-interview="
        (application, round) => openInterviewAssignment(application, 'CREATE', round)
      "
      @assign-interview="openInterviewAssignment($event, 'CREATE')"
      @reassign-interview="openInterviewAssignment($event, 'REASSIGN')"
      @cancel-interview="cancelInterview"
      @reject-application="openApplicationReject"
      @contact="contactCandidate"
      @generate-ai-match="generateAiMatch"
    />

    <ScreeningDecisionDialog
      v-model:visible="decisionDialogVisible"
      :decision="pendingDecision"
      :candidate-name="pendingApplication?.candidateName || ''"
      :submitting="reviewMutation.isPending.value"
      @submit="submitReview"
    />

    <ScreeningDecisionDialog
      v-model:visible="rejectDialogVisible"
      decision="REJECT"
      context="APPLICATION_REJECT"
      :candidate-name="rejectingApplication?.candidateName || ''"
      :submitting="rejectMutation.isPending.value"
      @submit="submitApplicationReject"
    />

    <el-dialog
      v-model="assignmentDialogVisible"
      :title="assignmentDialogTitle"
      width="440px"
      destroy-on-close
    >
      <div v-if="assignmentApplication" class="interview-assignment">
        <div class="interview-assignment__context">
          <strong>{{ assignmentApplication.candidateName }}</strong>
          <span>
            {{ assignmentApplication.jobTitle }} · {{ assignmentContextText }}
          </span>
        </div>
        <el-form :model="assignmentForm" label-width="88px">
          <el-form-item label="面试官" required>
            <el-select
              v-model="assignmentForm.interviewerId"
              placeholder="选择面试官"
              :loading="interviewerQuery.isFetching.value"
              style="width: 100%"
            >
              <el-option
                v-for="option in interviewerOptions"
                :key="option.id"
                :label="option.realName + '（' + option.username + '）'"
                :value="option.id"
              />
            </el-select>
            <span v-if="interviewerQuery.error.value" class="interview-assignment__error">
              面试官列表加载失败，请重试
            </span>
          </el-form-item>
          <el-form-item v-if="assignmentMode === 'CREATE'" label="面试轮次" required>
            <el-select
              v-model="assignmentForm.round"
              placeholder="选择面试轮次"
              style="width: 100%"
            >
              <el-option
                v-for="option in assignmentRoundOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="assignmentDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="assignmentSubmitting"
          :disabled="!interviewerOptions.length"
          @click="submitInterviewAssignment"
        >
          {{ assignmentSubmitText }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.pipeline-view {
  display: grid;
  gap: var(--rs-space-4);
}

.pipeline-view__intro,
.pipeline-source,
.pipeline-error,
.pipeline-toolbar__actions,
.pipeline-pagination {
  display: flex;
  align-items: center;
}

.pipeline-view__intro,
.pipeline-source,
.pipeline-error,
.pipeline-pagination {
  justify-content: space-between;
}

.pipeline-view__intro p,
.pipeline-error h3,
.pipeline-error p {
  margin: 0;
}

.pipeline-view__intro p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}

.pipeline-view__intro :deep(.el-radio-button__inner) {
  display: flex;
  align-items: center;
  gap: var(--rs-space-1);
}

.pipeline-source {
  min-height: 40px;
  padding: 0 var(--rs-space-3);
  border: 1px solid var(--rs-blue-500);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-selected);
  color: var(--rs-blue-700);
}

.pipeline-toolbar {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) 220px 160px auto;
  gap: var(--rs-space-2);
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.pipeline-toolbar__actions {
  justify-content: flex-end;
  gap: var(--rs-space-2);
}

.pipeline-error {
  min-height: 112px;
  gap: var(--rs-space-6);
  padding: var(--rs-space-4);
  border: 1px solid var(--rs-danger-700);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-danger-050);
}

.pipeline-error > div:last-child {
  display: flex;
  gap: var(--rs-space-2);
}

.pipeline-error h3 {
  font-size: 14px;
}

.pipeline-error p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}

.pipeline-workspace {
  min-width: 0;
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.pipeline-pagination {
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.pipeline-stage-entry {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: var(--rs-space-3);
}

.pipeline-stage-entry__item {
  display: grid;
  min-height: 92px;
  gap: var(--rs-space-1);
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  outline: 0;
  background: var(--rs-surface-primary);
  color: var(--rs-text-secondary);
  text-align: left;
  cursor: pointer;
  transition:
    border-color var(--rs-motion-fast) var(--rs-ease-standard),
    background-color var(--rs-motion-fast) var(--rs-ease-standard),
    box-shadow var(--rs-motion-fast) var(--rs-ease-standard);
}

.pipeline-stage-entry__item:hover,
.pipeline-stage-entry__item:focus-visible {
  border-color: var(--rs-blue-500);
}

.pipeline-stage-entry__item:focus-visible {
  box-shadow: 0 0 0 3px rgb(49 131 216 / 16%);
}

.pipeline-stage-entry__item--active {
  border-color: var(--rs-blue-500);
  background: var(--rs-surface-selected);
}

.pipeline-stage-entry__label {
  color: var(--rs-text-primary);
  font-size: 13px;
  font-weight: 700;
}

.pipeline-stage-entry__item strong {
  color: var(--rs-text-primary);
  font-size: 22px;
  font-variant-numeric: tabular-nums;
  line-height: 1.1;
}

.pipeline-stage-entry__item span:last-child {
  overflow: hidden;
  color: var(--rs-text-tertiary);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pipeline-stage-entry__total {
  grid-column: 1 / -1;
  justify-self: end;
  color: var(--rs-text-tertiary);
  font-size: 12px;
}

.pipeline-workspace__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 56px;
  padding: 0 var(--rs-space-4);
  border-bottom: 1px solid var(--rs-border-default);
}

.pipeline-workspace__header div {
  display: grid;
  gap: var(--rs-space-1);
}

.pipeline-workspace__header strong {
  color: var(--rs-text-primary);
  font-size: 14px;
}

.pipeline-workspace__header span {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}

.pipeline-loading {
  min-height: calc(100dvh - 392px);
  padding: var(--rs-space-6);
  background: var(--rs-surface-primary);
}

.pipeline-pagination {
  min-height: 56px;
  padding: 0 var(--rs-space-4);
  color: var(--rs-text-secondary);
}

.interview-assignment {
  display: grid;
  gap: var(--rs-space-4);
}

.interview-assignment__context {
  display: grid;
  gap: var(--rs-space-1);
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
}

.interview-assignment__context span,
.interview-assignment__error {
  color: var(--rs-text-secondary);
  font-size: 12px;
}

.interview-assignment__error {
  display: block;
  margin-top: var(--rs-space-1);
  color: var(--rs-danger-700);
}
</style>
