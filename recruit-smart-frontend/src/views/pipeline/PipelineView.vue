<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { Columns3, List, RefreshCw, RotateCcw, Search } from 'lucide-vue-next'
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useRouter } from 'vue-router'

import ApplicationDetailDrawer from '@/components/pipeline/ApplicationDetailDrawer.vue'
import PipelineBoard from '@/components/pipeline/PipelineBoard.vue'
import PipelineTable from '@/components/pipeline/PipelineTable.vue'
import ScreeningDecisionDialog from '@/components/pipeline/ScreeningDecisionDialog.vue'
import { applicationStatusOptions, getPipelineStageKey, pipelineStages } from '@/config/pipeline'
import { interviewRoundOptions } from '@/config/interviews'
import { useRecruitmentPipeline } from '@/composables/useRecruitmentPipeline'
import { getOrCreateConversation } from '@/api/messages'
import { useSessionStore } from '@/stores/session'
import type { ApplicationStatus } from '@/types/candidate'
import type { InterviewAssignmentRequest, InterviewRound } from '@/types/interview'
import type {
  PipelineApplicationDetail,
  PipelineApplicationSummary,
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
  detailQuery,
  interviewerQuery,
  statusMutation,
  reviewMutation,
  interviewAssignmentMutation,
  interviewReassignmentMutation,
  interviewCancellationMutation,
  applyFilters,
  resetFilters,
  useDemoData,
  useApiData,
  selectApplication,
  closeDetail,
} = useRecruitmentPipeline()

const viewMode = ref<PipelineViewMode>('BOARD')
const decisionDialogVisible = ref(false)
const pendingDecision = ref<ScreeningDecision>('PASS')
const pendingApplication = ref<PipelineApplicationSummary | null>(null)
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
const filterForm = reactive({
  keyword: '',
  jobId: null as number | null,
  status: '' as ApplicationStatus | '',
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
const stageSummary = computed(() =>
  pipelineStages.map((stage) => ({
    ...stage,
    count: applications.value.filter(
      (application) => getPipelineStageKey(application.status) === stage.key,
    ).length,
  })),
)
const interviewerOptions = computed(() => interviewerQuery.data.value ?? [])
const assignmentSubmitting = computed(
  () =>
    interviewAssignmentMutation.isPending.value || interviewReassignmentMutation.isPending.value,
)

function parseRouteId(value: unknown) {
  const raw = Array.isArray(value) ? value[0] : value
  const id = Number(raw)
  return Number.isFinite(id) && id > 0 ? id : null
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
      status: filterForm.status as typeof query.status,
    })
  },
  { immediate: true },
)

function submitFilters() {
  applyFilters({
    keyword: filterForm.keyword.trim(),
    jobId: filterForm.jobId,
    status: filterForm.status as typeof query.status,
  })
}

function clearFilters() {
  Object.assign(filterForm, { keyword: '', jobId: null, status: '' })
  resetFilters()
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

function openInterviewAssignment(
  application: PipelineApplicationDetail,
  mode: 'CREATE' | 'REASSIGN',
) {
  assignmentMode.value = mode
  assignmentApplication.value = application
  assignmentForm.interviewerId = application.interview?.interviewerId ?? null
  assignmentForm.round = application.interview?.round ?? 'FIRST'
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
</script>

<template>
  <div class="pipeline-view">
    <section class="pipeline-view__intro">
      <div>
        <h2 class="rs-section-title">招聘流程</h2>
        <p>Board 用于团队协同，Table 用于精确筛选；关键状态变更始终由业务角色确认。</p>
      </div>
      <el-radio-group v-model="viewMode" aria-label="招聘流程视图">
        <el-radio-button value="BOARD">
          <Columns3 :size="15" :stroke-width="1.75" aria-hidden="true" />
          看板
        </el-radio-button>
        <el-radio-button value="TABLE">
          <List :size="15" :stroke-width="1.75" aria-hidden="true" />
          表格
        </el-radio-button>
      </el-radio-group>
    </section>

    <section v-if="demoMode" class="pipeline-source" aria-live="polite">
      <span><strong>演示数据模式</strong>：所有筛选结论只保存在当前浏览器内存中。</span>
      <el-button link @click="useApiData">返回投递接口</el-button>
    </section>

    <section class="pipeline-toolbar" aria-label="招聘流程筛选">
      <el-input
        v-model="filterForm.keyword"
        placeholder="候选人、手机号或职位"
        clearable
        @keyup.enter="submitFilters"
      />
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
          v-for="option in applicationStatusOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
      <div class="pipeline-toolbar__actions">
        <el-button type="primary" :icon="Search" @click="submitFilters">查询</el-button>
        <el-button :icon="RotateCcw" @click="clearFilters">重置</el-button>
        <el-tooltip content="刷新招聘流程" placement="top">
          <el-button
            circle
            :icon="RefreshCw"
            :loading="pipelineQuery.isFetching.value"
            aria-label="刷新招聘流程"
            @click="pipelineQuery.refetch()"
          />
        </el-tooltip>
      </div>
    </section>

    <section class="pipeline-summary" aria-label="流程阶段概览">
      <div v-for="stage in stageSummary" :key="stage.key">
        <span>{{ stage.label }}</span>
        <strong>{{ stage.count }}</strong>
      </div>
      <span class="pipeline-summary__total">共 {{ total }} 条投递</span>
    </section>

    <section v-if="listError && !demoMode" class="pipeline-error" role="alert">
      <div>
        <h3>招聘流程接口尚不可用</h3>
        <p>
          {{ listError.message }}。后端目前还没有投递聚合
          Controller，可以使用明确标识的演示数据继续开发。
        </p>
      </div>
      <div>
        <el-button :loading="pipelineQuery.isFetching.value" @click="pipelineQuery.refetch()">
          重试接口
        </el-button>
        <el-button type="primary" @click="useDemoData">使用演示数据</el-button>
      </div>
    </section>

    <section
      v-else
      class="pipeline-workspace"
      :class="`pipeline-workspace--${viewMode.toLowerCase()}`"
    >
      <div v-if="pipelineQuery.isLoading.value" class="pipeline-loading">
        <el-skeleton :rows="8" animated />
      </div>
      <PipelineBoard
        v-else-if="viewMode === 'BOARD'"
        :applications="applications"
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
      <span>第 {{ query.page }} 页 · 数据来源：{{ demoMode ? '演示数据' : '投递聚合接口' }}</span>
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        background
        layout="prev, pager, next"
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
      @start-screening="startScreening"
      @review="openReview"
      @assign-interview="openInterviewAssignment($event, 'CREATE')"
      @reassign-interview="openInterviewAssignment($event, 'REASSIGN')"
      @cancel-interview="cancelInterview"
      @contact="contactCandidate"
    />

    <ScreeningDecisionDialog
      v-model:visible="decisionDialogVisible"
      :decision="pendingDecision"
      :candidate-name="pendingApplication?.candidateName || ''"
      :submitting="reviewMutation.isPending.value"
      @submit="submitReview"
    />

    <el-dialog
      v-model="assignmentDialogVisible"
      :title="assignmentMode === 'CREATE' ? '指派面试官' : '重新指派面试官'"
      width="440px"
      destroy-on-close
    >
      <div v-if="assignmentApplication" class="interview-assignment">
        <div class="interview-assignment__context">
          <strong>{{ assignmentApplication.candidateName }}</strong>
          <span>
            {{ assignmentApplication.jobTitle }} ·
            {{
              assignmentMode === 'CREATE'
                ? '初筛已通过，等待面试官预约'
                : assignmentApplication.interview?.roundText
            }}
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
                v-for="option in interviewRoundOptions"
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
          {{ assignmentMode === 'CREATE' ? '确认指派' : '确认重新指派' }}
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
.pipeline-summary,
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

.pipeline-summary {
  min-height: 48px;
  padding: 0 var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.pipeline-summary > div {
  display: flex;
  align-items: baseline;
  gap: var(--rs-space-2);
  min-width: 110px;
  padding-right: var(--rs-space-4);
  color: var(--rs-text-secondary);
}

.pipeline-summary > div + div {
  padding-left: var(--rs-space-4);
  border-left: 1px solid var(--rs-border-default);
}

.pipeline-summary strong {
  color: var(--rs-text-primary);
  font-size: 16px;
  font-variant-numeric: tabular-nums;
}

.pipeline-summary__total {
  margin-left: auto;
  color: var(--rs-text-tertiary);
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
  overflow-x: auto;
}

.pipeline-workspace--table,
.pipeline-pagination {
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.pipeline-loading {
  min-height: 440px;
  padding: var(--rs-space-6);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
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
