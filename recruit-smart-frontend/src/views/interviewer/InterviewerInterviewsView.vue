<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  CalendarClock,
  Check,
  Circle,
  CircleAlert,
  PanelLeftOpen,
  PanelRightOpen,
} from 'lucide-vue-next'
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'

import { getInterviewApplicationContext } from '@/api/interviews'
import InterviewerCandidateContext from '@/components/interviewer/InterviewerCandidateContext.vue'
import InterviewerCopilotPanel from '@/components/interviewer/InterviewerCopilotPanel.vue'
import InterviewerDraftFooter from '@/components/interviewer/InterviewerDraftFooter.vue'
import InterviewerErrorState from '@/components/interviewer/InterviewerErrorState.vue'
import InterviewerPageHeader from '@/components/interviewer/InterviewerPageHeader.vue'
import InterviewerScorecard from '@/components/interviewer/InterviewerScorecard.vue'
import InterviewerTaskList from '@/components/interviewer/InterviewerTaskList.vue'
import {
  createInterviewDraftAutosave,
  INTERVIEW_DRAFT_AUTOSAVE_DELAY,
} from '@/composables/interviewDraftAutosave'
import { useInterviewWorkspace } from '@/composables/useInterviewWorkspace'
import {
  getInterviewerTaskStage,
  getMissingFeedbackCount,
  isHttpMeetingLocation,
} from '@/config/interviewer'
import { calculateInterviewScore, interviewMethodOptions } from '@/config/interviews'
import { useSessionStore } from '@/stores/session'
import type { FeedbackSummaryResponse } from '@/types/ai'
import type {
  InterviewDraftSaveState,
  InterviewFeedbackRequest,
  InterviewFeedbackState,
  InterviewQuestion,
  InterviewScheduleRequest,
  InterviewScoreItem,
  InterviewStatus,
  InterviewSuggestion,
} from '@/types/interview'

const COPILOT_STORAGE_KEY = 'rs-interviewer-copilot-collapsed'
const TASK_LIST_STORAGE_KEY = 'rs-interviewer-task-list-collapsed'
const route = useRoute()
const router = useRouter()
const session = useSessionStore()
const {
  query,
  selectedInterviewId,
  taskQuery,
  workspaceQuery,
  draftMutation,
  scheduleMutation,
  completeMutation,
  submitMutation,
  questionMutation,
  summaryMutation,
  applyFilters,
  resetFilters,
  selectInterview,
} = useInterviewWorkspace()

function readQueryString(key: string) {
  const raw = route.query[key]
  return Array.isArray(raw) ? (raw[0] ?? '') : (raw ?? '')
}

function readQueryPage() {
  const value = Number(readQueryString('page'))
  return Number.isFinite(value) && value > 0 ? value : 1
}

const filterForm = reactive<{
  keyword: string
  status: InterviewStatus | ''
  feedbackState: InterviewFeedbackState | ''
}>({
  keyword: readQueryString('keyword'),
  status: readQueryString('status') as InterviewStatus | '',
  feedbackState: readQueryString('feedbackState') as InterviewFeedbackState | '',
})
applyFilters({ ...filterForm })
query.page = readQueryPage()

const scorecard = ref<InterviewScoreItem[]>([])
const comment = ref('')
const suggestion = ref<InterviewSuggestion | null>(null)
const extraQuestions = ref<InterviewQuestion[]>([])
const feedbackSummary = ref<FeedbackSummaryResponse | null>(null)
const copilotError = ref('')
const scorecardRef = ref<InstanceType<typeof InterviewerScorecard>>()
const scheduleDialogVisible = ref(false)
const copilotDrawerVisible = ref(false)
const copilotCollapsed = ref(window.localStorage.getItem(COPILOT_STORAGE_KEY) === 'true')
const taskListCollapsed = ref(window.localStorage.getItem(TASK_LIST_STORAGE_KEY) === 'true')
const isNarrow = ref(window.innerWidth < 1280)
const scheduleForm = reactive<InterviewScheduleRequest>({
  interviewTime: '',
  method: 'ONLINE',
  location: '',
})

const tasks = computed(() => taskQuery.data.value?.items ?? [])
const total = computed(() => taskQuery.data.value?.total ?? 0)
const workspace = computed(() => workspaceQuery.data.value)
const overallScore = computed(() =>
  calculateInterviewScore(scorecard.value.map((item) => item.score)),
)
const canEdit = computed(
  () =>
    Boolean(workspace.value) &&
    (workspace.value?.status === 'SCHEDULED' || workspace.value?.status === 'COMPLETED') &&
    workspace.value?.feedbackState !== 'SUBMITTED',
)
const readonly = computed(() => !canEdit.value)
const stage = computed(() => (workspace.value ? getInterviewerTaskStage(workspace.value) : null))
const missingCount = computed(() =>
  getMissingFeedbackCount(scorecard.value, comment.value, suggestion.value),
)
const canSubmit = computed(() => stage.value === 'FEEDBACK' && missingCount.value === 0)
const visibleQuestions = computed(() => [
  ...(workspace.value?.questions ?? []),
  ...extraQuestions.value,
])

const saveState = ref<InterviewDraftSaveState>('clean')
const lastSavedAt = ref('')
const lastPersistedJson = ref('')
let hydrating = false

interface DraftSnapshot {
  interviewId: number
  payload: InterviewFeedbackRequest
  serialized: string
}

function buildFeedbackRequest(): InterviewFeedbackRequest {
  return {
    scorecard: scorecard.value.map((item) => ({ ...item })),
    score: overallScore.value,
    comment: comment.value,
    suggestion: suggestion.value,
    interviewerId: Number(session.user?.id ?? 0),
  }
}

function serializeCurrentDraft() {
  return JSON.stringify(buildFeedbackRequest())
}

const autosave = createInterviewDraftAutosave<DraftSnapshot>({
  delay: INTERVIEW_DRAFT_AUTOSAVE_DELAY,
  onStateChange(state) {
    saveState.value = state
  },
  async save(snapshot) {
    await draftMutation.mutateAsync({ id: snapshot.interviewId, data: snapshot.payload })
    if (selectedInterviewId.value === snapshot.interviewId) {
      lastPersistedJson.value = snapshot.serialized
      lastSavedAt.value = new Date().toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
      })
    }
  },
})

function currentDraftSnapshot(): DraftSnapshot | null {
  if (!workspace.value || !canEdit.value) return null
  const payload = buildFeedbackRequest()
  return { interviewId: workspace.value.id, payload, serialized: JSON.stringify(payload) }
}

function scheduleAutosave() {
  const snapshot = currentDraftSnapshot()
  if (snapshot) autosave.schedule(snapshot)
}

async function persistDraft() {
  const snapshot = currentDraftSnapshot()
  if (!snapshot) return true
  if (snapshot.serialized === lastPersistedJson.value && !autosave.hasPending()) {
    saveState.value = lastSavedAt.value ? 'saved' : 'clean'
    return true
  }
  autosave.schedule(snapshot)
  return autosave.flush()
}

watch(
  workspace,
  async (interview) => {
    if (!interview) return
    hydrating = true
    autosave.cancelPending()
    scorecard.value = interview.scorecard.map((item) => ({ ...item }))
    comment.value = interview.feedback.comment
    suggestion.value = interview.feedback.suggestion
    extraQuestions.value = []
    feedbackSummary.value = null
    copilotError.value = ''
    lastSavedAt.value = ''
    lastPersistedJson.value = serializeCurrentDraft()
    saveState.value = interview.feedbackState === 'DRAFT' ? 'saved' : 'clean'
    await nextTick()
    hydrating = false
  },
  { immediate: true },
)

watch(
  [scorecard, comment, suggestion],
  () => {
    if (hydrating || !canEdit.value) return
    if (serializeCurrentDraft() === lastPersistedJson.value) {
      saveState.value = lastSavedAt.value ? 'saved' : 'clean'
      autosave.cancelPending()
      return
    }
    saveState.value = 'dirty'
    scheduleAutosave()
  },
  { deep: true },
)

const routeInterviewId = computed(() => {
  const id = Number(readQueryString('interviewId'))
  return Number.isFinite(id) && id > 0 ? id : null
})

watch(
  routeInterviewId,
  (id) => {
    if (id !== null && id !== selectedInterviewId.value) selectInterview(id)
  },
  { immediate: true },
)

watch(
  tasks,
  (items) => {
    if (!items.length || routeInterviewId.value !== null) return
    if (!items.some((item) => item.id === selectedInterviewId.value)) selectInterview(items[0]!.id)
  },
  { immediate: true },
)

function syncUrl() {
  const nextQuery: Record<string, string> = {}
  if (filterForm.keyword.trim()) nextQuery.keyword = filterForm.keyword.trim()
  if (filterForm.status) nextQuery.status = filterForm.status
  if (filterForm.feedbackState) nextQuery.feedbackState = filterForm.feedbackState
  if (query.page > 1) nextQuery.page = String(query.page)
  if (selectedInterviewId.value) nextQuery.interviewId = String(selectedInterviewId.value)
  void router.replace({ query: nextQuery })
}

function submitFilters() {
  applyFilters({
    keyword: filterForm.keyword.trim(),
    status: filterForm.status,
    feedbackState: filterForm.feedbackState,
  })
  syncUrl()
}

function clearFilters() {
  Object.assign(filterForm, { keyword: '', status: '', feedbackState: '' })
  resetFilters()
  syncUrl()
}

function changePage(page: number) {
  query.page = page
  syncUrl()
}

async function handleTaskSelect(id: number) {
  if (id === selectedInterviewId.value) return
  if (
    (saveState.value === 'dirty' || saveState.value === 'error' || saveState.value === 'saving') &&
    !(await persistDraft())
  ) {
    ElMessage.warning('当前草稿保存失败，请重试后再切换任务')
    return
  }
  selectInterview(id)
  await router.replace({ query: { ...route.query, interviewId: String(id) } })
}

function openScheduleDialog() {
  const interview = workspace.value
  if (!interview || stage.value !== 'SCHEDULE') return
  scheduleForm.interviewTime = interview.interviewTime ?? ''
  scheduleForm.method = interview.method ?? 'ONLINE'
  scheduleForm.location = interview.location ?? ''
  scheduleDialogVisible.value = true
}

async function submitSchedule() {
  const interview = workspace.value
  if (!interview || stage.value !== 'SCHEDULE') return
  if (!scheduleForm.interviewTime || !scheduleForm.location.trim()) {
    ElMessage.warning('请选择面试时间并填写地点或会议链接')
    return
  }
  try {
    await scheduleMutation.mutateAsync({
      id: interview.id,
      data: { ...scheduleForm, location: scheduleForm.location.trim() },
    })
    scheduleDialogVisible.value = false
    ElMessage.success('面试预约已确认')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '面试预约失败')
  }
}

async function completeCurrentInterview() {
  const interview = workspace.value
  if (!interview || stage.value !== 'ATTEND') return
  if (!(await persistDraft())) {
    ElMessage.warning('草稿保存失败，请重试后再完成面试')
    return
  }
  try {
    await ElMessageBox.confirm('确认本次面试已经结束。完成后仍需单独提交面试反馈。', '完成面试', {
      confirmButtonText: '确认完成',
      cancelButtonText: '继续面试',
      type: 'info',
    })
    await completeMutation.mutateAsync(interview.id)
    ElMessage.success('面试已完成，请继续提交反馈')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error instanceof Error ? error.message : '完成面试失败')
  }
}

async function submitFeedback() {
  const interview = workspace.value
  if (!interview || stage.value !== 'FEEDBACK') return
  if (missingCount.value > 0) {
    scorecardRef.value?.focusFirstMissing()
    ElMessage.warning(`还有 ${missingCount.value} 项反馈内容未完成`)
    return
  }
  if (!(await persistDraft())) {
    ElMessage.warning('草稿保存失败，请重试后再提交反馈')
    return
  }
  autosave.cancelPending()
  try {
    await ElMessageBox.confirm(
      '提交后原始评价将锁定。该建议不会直接推进录用状态，仍需 HR 审核确认。',
      '提交面试反馈',
      { confirmButtonText: '确认提交', cancelButtonText: '继续编辑', type: 'warning' },
    )
    await submitMutation.mutateAsync({ id: interview.id, data: buildFeedbackRequest() })
    lastPersistedJson.value = serializeCurrentDraft()
    saveState.value = 'clean'
    ElMessage.success('面试反馈已提交，等待 HR 审核')
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      scheduleAutosave()
      return
    }
    ElMessage.error(error instanceof Error ? error.message : '反馈提交失败')
  }
}

async function generateQuestion(focus: string) {
  const interview = workspace.value
  if (!interview) return
  copilotError.value = ''
  try {
    const application = await getInterviewApplicationContext(interview.applicationId)
    const questions = await questionMutation.mutateAsync({
      id: interview.id,
      data: {
        focus,
        jobId: application.jobId,
        candidateId: interview.candidateId,
        resumeId: application.resumeId,
        jobTitle: interview.jobTitle,
        resumeText: interview.candidateBrief.workExperience ?? '',
        skills: interview.candidateBrief.skills.join(', '),
        projectExperience: interview.candidateBrief.projectExperience ?? '',
        workExperience: interview.candidateBrief.workExperience ?? '',
      },
    })
    extraQuestions.value.push(...questions)
  } catch (error) {
    copilotError.value = error instanceof Error ? error.message : '参考问题生成失败，请重试'
  }
}

async function summarizeFeedback() {
  const interview = workspace.value
  if (!interview || !comment.value.trim()) return
  copilotError.value = ''
  try {
    const application = await getInterviewApplicationContext(interview.applicationId)
    feedbackSummary.value = await summaryMutation.mutateAsync({
      interviewId: interview.id,
      candidateId: interview.candidateId,
      jobId: application.jobId,
      jobTitle: interview.jobTitle,
      candidateName: interview.candidateName,
      feedbackText: comment.value,
      score: overallScore.value,
    })
  } catch (error) {
    copilotError.value = error instanceof Error ? error.message : '反馈摘要生成失败，请重试'
  }
}

function toggleCopilot() {
  if (isNarrow.value) {
    copilotDrawerVisible.value = true
    return
  }
  copilotCollapsed.value = false
  window.localStorage.setItem(COPILOT_STORAGE_KEY, 'false')
}

function closeCopilot() {
  if (isNarrow.value) {
    copilotDrawerVisible.value = false
    return
  }
  copilotCollapsed.value = true
  window.localStorage.setItem(COPILOT_STORAGE_KEY, 'true')
}

function setTaskListCollapsed(collapsed: boolean) {
  taskListCollapsed.value = collapsed
  window.localStorage.setItem(TASK_LIST_STORAGE_KEY, String(collapsed))
}

function handleResize() {
  isNarrow.value = window.innerWidth < 1280
}

function beforeUnload(event: BeforeUnloadEvent) {
  if (saveState.value !== 'dirty' && saveState.value !== 'error' && saveState.value !== 'saving')
    return
  event.preventDefault()
  event.returnValue = ''
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  window.addEventListener('beforeunload', beforeUnload)
})
onBeforeUnmount(() => {
  autosave.dispose()
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('beforeunload', beforeUnload)
})
onBeforeRouteLeave(async () => {
  if (saveState.value !== 'dirty' && saveState.value !== 'error' && saveState.value !== 'saving')
    return true
  if (await persistDraft()) return true
  return window.confirm('草稿保存失败，离开后本次修改可能丢失。仍要离开吗？')
})
</script>

<template>
  <div class="interviewer-interviews-view">
    <InterviewerPageHeader
      title="我的面试"
      description="确认面试安排、记录结构化评价并独立提交反馈。"
    >
      <template #actions>
        <el-button
          v-if="
            workspace &&
            isHttpMeetingLocation(workspace.location) &&
            workspace.status === 'SCHEDULED'
          "
          tag="a"
          :href="workspace.location || undefined"
          target="_blank"
          rel="noopener noreferrer"
          >打开会议链接</el-button
        >
        <el-button v-if="copilotCollapsed || isNarrow" :icon="PanelRightOpen" @click="toggleCopilot"
          >打开 Copilot</el-button
        >
      </template>
    </InterviewerPageHeader>

    <InterviewerErrorState
      v-if="taskQuery.error.value"
      title="面试任务暂时无法加载"
      description="请重新加载任务列表。"
      :loading="taskQuery.isFetching.value"
      @retry="taskQuery.refetch()"
    />

    <section
      v-else
      class="interviewer-workspace-grid"
      :class="{
        'interviewer-workspace-grid--without-copilot': copilotCollapsed || isNarrow,
        'interviewer-workspace-grid--task-collapsed': taskListCollapsed,
      }"
    >
      <InterviewerTaskList
        v-if="!taskListCollapsed"
        :tasks="tasks"
        :selected-id="selectedInterviewId"
        :keyword="filterForm.keyword"
        :status="filterForm.status"
        :feedback-state="filterForm.feedbackState"
        :loading="taskQuery.isLoading.value"
        :page="query.page"
        :page-size="query.pageSize"
        :total="total"
        @select="handleTaskSelect"
        @search="submitFilters"
        @reset="clearFilters"
        @update:keyword="filterForm.keyword = $event"
        @update:status="filterForm.status = $event"
        @update:feedback-state="filterForm.feedbackState = $event"
        @update:page="changePage"
        @collapse="setTaskListCollapsed(true)"
      />
      <aside v-else class="interviewer-task-rail" aria-label="已收起的面试任务">
        <el-tooltip content="展开面试任务" placement="right">
          <el-button
            text
            circle
            :icon="PanelLeftOpen"
            aria-label="展开面试任务"
            @click="setTaskListCollapsed(false)"
          />
        </el-tooltip>
        <span>面试任务</span>
        <strong>{{ total }}</strong>
      </aside>

      <main class="interviewer-evaluation">
        <InterviewerErrorState
          v-if="workspaceQuery.error.value"
          title="面试详情暂时无法加载"
          description="任务列表仍可继续使用，请重新加载当前面试详情。"
          :loading="workspaceQuery.isFetching.value"
          @retry="workspaceQuery.refetch()"
        />
        <div v-else-if="workspaceQuery.isLoading.value" class="interviewer-evaluation__loading">
          <el-skeleton :rows="14" animated />
        </div>
        <template v-else-if="workspace && stage">
          <InterviewerCandidateContext :interview="workspace" />
          <div
            v-if="stage === 'CANCELED' || stage === 'REINTERVIEW'"
            class="interviewer-terminal-state"
            :class="`interviewer-terminal-state--${stage.toLowerCase()}`"
            role="status"
          >
            <CircleAlert :size="18" :stroke-width="1.75" />
            <div>
              <strong>{{ stage === 'CANCELED' ? '本次面试已取消' : '该任务需要复试安排' }}</strong>
              <p>
                {{
                  stage === 'CANCELED'
                    ? '当前记录仅供查看，没有可执行操作。'
                    : '请等待 HR 更新安排，当前评价内容保持只读。'
                }}
              </p>
            </div>
          </div>
          <ol v-else class="interviewer-progress" aria-label="面试任务阶段">
            <li :class="{ 'interviewer-progress__item--complete': stage !== 'SCHEDULE' }">
              <span
                ><Check v-if="stage !== 'SCHEDULE'" :size="14" /><Circle v-else :size="14" /></span
              ><strong>确认安排</strong>
            </li>
            <li
              :class="{
                'interviewer-progress__item--complete':
                  stage === 'FEEDBACK' || stage === 'SUBMITTED',
                'interviewer-progress__item--current': stage === 'ATTEND',
              }"
            >
              <span
                ><Check v-if="stage === 'FEEDBACK' || stage === 'SUBMITTED'" :size="14" /><Circle
                  v-else
                  :size="14" /></span
              ><strong>完成面试</strong>
            </li>
            <li
              :class="{
                'interviewer-progress__item--complete': stage === 'SUBMITTED',
                'interviewer-progress__item--current': stage === 'FEEDBACK',
              }"
            >
              <span
                ><Check v-if="stage === 'SUBMITTED'" :size="14" /><Circle v-else :size="14" /></span
              ><strong>填写反馈</strong>
            </li>
            <li :class="{ 'interviewer-progress__item--complete': stage === 'SUBMITTED' }">
              <span
                ><Check v-if="stage === 'SUBMITTED'" :size="14" /><Circle v-else :size="14" /></span
              ><strong>反馈已提交</strong>
            </li>
          </ol>
          <InterviewerDraftFooter
            :stage="stage"
            :save-state="saveState"
            :last-saved-at="lastSavedAt"
            :saving="draftMutation.isPending.value"
            :completing="completeMutation.isPending.value"
            :submitting="submitMutation.isPending.value"
            :scheduling="scheduleMutation.isPending.value"
            :submit-disabled="!canSubmit"
            @save="persistDraft"
            @retry="persistDraft"
            @schedule="openScheduleDialog"
            @complete="completeCurrentInterview"
            @submit="submitFeedback"
          />
          <InterviewerScorecard
            ref="scorecardRef"
            :scorecard="scorecard"
            :comment="comment"
            :suggestion="suggestion"
            :overall-score="overallScore"
            :readonly="readonly"
            @update:scorecard="scorecard = $event"
            @update:comment="comment = $event"
            @update:suggestion="suggestion = $event"
          />
        </template>
        <section v-else class="interviewer-evaluation__empty">
          <CalendarClock :size="24" :stroke-width="1.75" />
          <h2>选择一个面试任务</h2>
          <p>从左侧任务列表选择候选人后开始准备或填写反馈。</p>
        </section>
      </main>

      <InterviewerCopilotPanel
        v-if="workspace && !copilotCollapsed && !isNarrow"
        class="interviewer-workspace-grid__copilot"
        :interview-id="workspace.id"
        :questions="visibleQuestions"
        :risk-points="workspace.candidateBrief.riskPoints"
        :ai-summary="workspace.feedback.aiSummary"
        :feedback-summary="feedbackSummary"
        :generating="questionMutation.isPending.value"
        :summarizing="summaryMutation.isPending.value"
        :can-summarize="Boolean(comment.trim())"
        :error="copilotError"
        @close="closeCopilot"
        @generate="generateQuestion"
        @summarize="summarizeFeedback"
      />
    </section>

    <el-drawer v-model="copilotDrawerVisible" title="面试 Copilot" size="360px" destroy-on-close>
      <InterviewerCopilotPanel
        v-if="workspace"
        :interview-id="workspace.id"
        :questions="visibleQuestions"
        :risk-points="workspace.candidateBrief.riskPoints"
        :ai-summary="workspace.feedback.aiSummary"
        :feedback-summary="feedbackSummary"
        :generating="questionMutation.isPending.value"
        :summarizing="summaryMutation.isPending.value"
        :can-summarize="Boolean(comment.trim())"
        :error="copilotError"
        @close="closeCopilot"
        @generate="generateQuestion"
        @summarize="summarizeFeedback"
      />
    </el-drawer>

    <el-dialog v-model="scheduleDialogVisible" title="预约面试" width="480px" destroy-on-close>
      <el-form label-position="top" @submit.prevent="submitSchedule">
        <el-form-item label="面试时间" required
          ><el-date-picker
            v-model="scheduleForm.interviewTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            format="YYYY-MM-DD HH:mm"
            placeholder="选择面试时间"
            style="width: 100%"
        /></el-form-item>
        <el-form-item label="面试方式" required
          ><el-select v-model="scheduleForm.method" style="width: 100%"
            ><el-option
              v-for="option in interviewMethodOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value" /></el-select
        ></el-form-item>
        <el-form-item label="地点或会议" required
          ><el-input
            v-model="scheduleForm.location"
            maxlength="255"
            placeholder="填写会议链接、会议室或联系电话"
        /></el-form-item>
      </el-form>
      <template #footer
        ><el-button @click="scheduleDialogVisible = false">取消</el-button
        ><el-button
          type="primary"
          :loading="scheduleMutation.isPending.value"
          @click="submitSchedule"
          >确认预约</el-button
        ></template
      >
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.interviewer-interviews-view {
  display: grid;
  gap: var(--rs-space-4);
  max-width: var(--rs-interviewer-content-max-width);
  margin: 0 auto;
}
.interviewer-workspace-grid {
  --rs-interviewer-task-column-width: var(--rs-interviewer-task-list-width);
  display: grid;
  grid-template-columns: var(--rs-interviewer-task-column-width) minmax(520px, 1fr) minmax(
      304px,
      var(--rs-interviewer-copilot-width)
    );
  align-items: start;
  gap: var(--rs-space-3);
}
.interviewer-workspace-grid--without-copilot {
  grid-template-columns: var(--rs-interviewer-task-column-width) minmax(520px, 1fr);
}
.interviewer-workspace-grid--task-collapsed {
  --rs-interviewer-task-column-width: 48px;
}
.interviewer-task-rail {
  position: sticky;
  top: 0;
  display: flex;
  max-height: calc(100dvh - 112px);
  flex-direction: column;
  align-items: center;
  gap: var(--rs-space-3);
  padding: var(--rs-space-2) var(--rs-space-1) var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
  color: var(--rs-text-secondary);
}
.interviewer-task-rail > span {
  writing-mode: vertical-rl;
  font-size: 11px;
  letter-spacing: 0.12em;
}
.interviewer-task-rail > strong {
  display: grid;
  min-width: 24px;
  height: 24px;
  padding: 0 var(--rs-space-1);
  place-items: center;
  border-radius: var(--rs-radius-pill);
  background: var(--rs-blue-050);
  color: var(--rs-blue-700);
  font-size: 12px;
}
.interviewer-evaluation {
  display: grid;
  min-width: 0;
  gap: var(--rs-space-3);
}
.interviewer-evaluation__loading {
  min-height: 600px;
  padding: var(--rs-space-6);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
.interviewer-evaluation__empty {
  display: grid;
  min-height: 480px;
  place-items: center;
  align-content: center;
  gap: var(--rs-space-2);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
  color: var(--rs-text-secondary);
  text-align: center;
}
.interviewer-evaluation__empty h2,
.interviewer-evaluation__empty p {
  margin: 0;
}
.interviewer-evaluation__empty h2 {
  color: var(--rs-text-primary);
  font-size: 16px;
}
.interviewer-workspace-grid__copilot {
  position: sticky;
  top: 0;
  max-height: calc(100dvh - 112px);
  overflow-y: auto;
}
.interviewer-progress {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  padding: var(--rs-space-3) var(--rs-space-4);
  margin: 0;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
  list-style: none;
}
.interviewer-terminal-state {
  display: flex;
  align-items: flex-start;
  gap: var(--rs-space-3);
  padding: var(--rs-space-3) var(--rs-space-4);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
  color: var(--rs-text-secondary);
}
.interviewer-terminal-state--reinterview {
  border-color: color-mix(in srgb, var(--rs-warning-800) 24%, transparent);
  background: var(--rs-warning-050);
  color: var(--rs-warning-800);
}
.interviewer-terminal-state strong,
.interviewer-terminal-state p {
  margin: 0;
}
.interviewer-terminal-state strong {
  color: var(--rs-text-primary);
}
.interviewer-terminal-state p {
  margin-top: var(--rs-space-1);
  font-size: 12px;
}
.interviewer-progress li {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--rs-space-2);
  color: var(--rs-text-tertiary);
  font-size: 12px;
}
.interviewer-progress li:not(:last-child)::after {
  position: absolute;
  top: 50%;
  right: -24px;
  width: 48px;
  height: 1px;
  background: var(--rs-border-strong);
  content: '';
}
.interviewer-progress li > span {
  display: grid;
  width: 24px;
  height: 24px;
  place-items: center;
  border-radius: 50%;
  background: var(--rs-surface-muted);
}
.interviewer-progress__item--complete,
.interviewer-progress__item--current {
  color: var(--rs-blue-700) !important;
}
.interviewer-progress__item--complete > span {
  background: var(--rs-success-050) !important;
  color: var(--rs-success-700);
}
.interviewer-progress__item--current > span {
  background: var(--rs-blue-050) !important;
  color: var(--rs-blue-700);
}
.interviewer-interviews-view :deep(.el-drawer__body) {
  padding: 0;
}
.interviewer-interviews-view :deep(.el-drawer .interviewer-copilot) {
  border: 0;
  border-radius: 0;
}
@media (max-width: 1279px) {
  .interviewer-workspace-grid,
  .interviewer-workspace-grid--without-copilot {
    --rs-interviewer-task-column-width: 248px;
    grid-template-columns: var(--rs-interviewer-task-column-width) minmax(520px, 1fr);
  }
  .interviewer-workspace-grid--task-collapsed,
  .interviewer-workspace-grid--without-copilot.interviewer-workspace-grid--task-collapsed {
    --rs-interviewer-task-column-width: 48px;
  }
}
</style>
