<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshCw, RotateCcw, Search, ShieldCheck, TriangleAlert } from 'lucide-vue-next'
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import CandidateInterviewBrief from '@/components/interviews/CandidateInterviewBrief.vue'
import InterviewCopilotPanel from '@/components/interviews/InterviewCopilotPanel.vue'
import InterviewScorecard from '@/components/interviews/InterviewScorecard.vue'
import InterviewTaskQueue from '@/components/interviews/InterviewTaskQueue.vue'
import { useInterviewWorkspace } from '@/composables/useInterviewWorkspace'
import {
  calculateInterviewScore,
  interviewFeedbackStateOptions,
  interviewStatusOptions,
} from '@/config/interviews'
import { useSessionStore } from '@/stores/session'
import type {
  InterviewFeedbackState,
  InterviewQuestion,
  InterviewScoreItem,
  InterviewStatus,
  InterviewSuggestion,
} from '@/types/interview'

const session = useSessionStore()
const route = useRoute()
const {
  demoMode,
  selectedInterviewId,
  taskQuery,
  workspaceQuery,
  draftMutation,
  submitMutation,
  questionMutation,
  applyFilters,
  resetFilters,
  useDemoData,
  useApiData,
  selectInterview,
} = useInterviewWorkspace()

const filterForm = reactive<{
  keyword: string
  status: InterviewStatus | ''
  feedbackState: InterviewFeedbackState | ''
}>({
  keyword: '',
  status: '',
  feedbackState: '',
})
const scorecard = ref<InterviewScoreItem[]>([])
const comment = ref('')
const suggestion = ref<InterviewSuggestion | null>(null)
const extraQuestions = ref<InterviewQuestion[]>([])

const tasks = computed(() => taskQuery.data.value?.items ?? [])
const workspace = computed(() => workspaceQuery.data.value)
const listError = computed(() => taskQuery.error.value as Error | null)
const workspaceError = computed(() => workspaceQuery.error.value as Error | null)
const overallScore = computed(() =>
  calculateInterviewScore(scorecard.value.map((item) => item.score)),
)
const canEdit = computed(
  () => session.currentRole === 'INTERVIEWER' && workspace.value?.feedbackState !== 'SUBMITTED',
)
const readonly = computed(() => !canEdit.value)
const visibleQuestions = computed(() => [
  ...(workspace.value?.questions ?? []),
  ...extraQuestions.value,
])
const routeInterviewId = computed(() => {
  const raw = Array.isArray(route.query.interviewId)
    ? route.query.interviewId[0]
    : route.query.interviewId
  const id = Number(raw)
  return Number.isFinite(id) && id > 0 ? id : null
})

watch(
  routeInterviewId,
  (id) => {
    if (id !== null) selectInterview(id)
  },
  { immediate: true },
)

watch(
  tasks,
  (items) => {
    if (routeInterviewId.value !== null) {
      if (selectedInterviewId.value !== routeInterviewId.value) {
        selectInterview(routeInterviewId.value)
      }
      return
    }
    if (!items.length) return
    if (!items.some((item) => item.id === selectedInterviewId.value)) {
      selectInterview(items[0]!.id)
    }
  },
  { immediate: true },
)

watch(
  workspace,
  (interview) => {
    if (!interview) return
    // Vue Query 数据会被包装为只读代理，逐项复制可安全建立本地可编辑草稿。
    scorecard.value = interview.scorecard.map((item) => ({ ...item }))
    comment.value = interview.feedback.comment
    suggestion.value = interview.feedback.suggestion
    extraQuestions.value = []
  },
  { immediate: true },
)

function submitFilters() {
  applyFilters({
    keyword: filterForm.keyword.trim(),
    status: filterForm.status,
    feedbackState: filterForm.feedbackState,
  })
}

function clearFilters() {
  Object.assign(filterForm, { keyword: '', status: '', feedbackState: '' })
  resetFilters()
}

function buildFeedbackRequest() {
  return {
    scorecard: scorecard.value.map((item) => ({ ...item })),
    score: overallScore.value,
    comment: comment.value,
    suggestion: suggestion.value,
    interviewerId: Number(session.user?.id ?? 0),
  }
}

async function saveDraft() {
  const interview = workspace.value
  if (!interview || !canEdit.value) return
  try {
    await draftMutation.mutateAsync({ id: interview.id, data: buildFeedbackRequest() })
    ElMessage.success('面试反馈草稿已保存')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '草稿保存失败')
  }
}

async function submitFeedback() {
  const interview = workspace.value
  if (!interview || !canEdit.value) return
  if (
    scorecard.value.some((item) => item.score === null || !item.evidence.trim()) ||
    !comment.value.trim() ||
    !suggestion.value
  ) {
    ElMessage.warning('请完成所有评分、评价证据、综合评价和录用建议')
    return
  }

  try {
    await ElMessageBox.confirm(
      '提交后原始评价将锁定。该建议不会直接推进录用状态，仍需 HR 审核确认。',
      '提交面试反馈',
      {
        confirmButtonText: '确认提交',
        cancelButtonText: '继续编辑',
        type: 'warning',
      },
    )
    await submitMutation.mutateAsync({ id: interview.id, data: buildFeedbackRequest() })
    ElMessage.success('面试反馈已提交，等待 HR 审核')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error instanceof Error ? error.message : '反馈提交失败')
  }
}

async function generateQuestion(focus: string) {
  const interview = workspace.value
  if (!interview) return
  try {
    const questions = await questionMutation.mutateAsync({
      id: interview.id,
      data: { focus },
    })
    extraQuestions.value.push(...questions)
    ElMessage.success('已生成参考追问')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '参考问题生成失败')
  }
}
</script>

<template>
  <div class="interviews-view">
    <section class="interviews-view__intro">
      <div>
        <h2 class="rs-section-title">面试工作区</h2>
        <p>在同一上下文中查看任务、候选人材料、结构化评分与 AI 问题建议。</p>
      </div>
      <div class="interviews-view__authority">
        <ShieldCheck :size="16" :stroke-width="1.75" aria-hidden="true" />
        <span>面试官保留原始意见，HR 负责后续业务决策</span>
      </div>
    </section>

    <section v-if="demoMode" class="interview-source" aria-live="polite">
      <span><strong>演示数据模式</strong>：草稿、评分和提交结果只保存在当前浏览器内存中。</span>
      <el-button link @click="useApiData">返回面试接口</el-button>
    </section>

    <section class="interview-toolbar" aria-label="面试任务筛选">
      <el-input
        v-model="filterForm.keyword"
        placeholder="候选人、职位或面试官"
        clearable
        @keyup.enter="submitFilters"
      />
      <el-select v-model="filterForm.status" placeholder="全部面试状态" clearable>
        <el-option
          v-for="option in interviewStatusOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
      <el-select v-model="filterForm.feedbackState" placeholder="全部反馈状态" clearable>
        <el-option
          v-for="option in interviewFeedbackStateOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
      <div class="interview-toolbar__actions">
        <el-button type="primary" :icon="Search" @click="submitFilters">查询</el-button>
        <el-button :icon="RotateCcw" @click="clearFilters">重置</el-button>
        <el-tooltip content="刷新面试任务" placement="top">
          <el-button
            circle
            :icon="RefreshCw"
            :loading="taskQuery.isFetching.value"
            aria-label="刷新面试任务"
            @click="taskQuery.refetch()"
          />
        </el-tooltip>
      </div>
    </section>

    <section v-if="listError && !demoMode" class="interview-error" role="alert">
      <div>
        <h3>面试工作区接口暂不可用</h3>
        <p>
          {{ listError.message }}。请确认
          Gateway、业务服务和当前账号权限正常，也可以使用演示数据继续评审。
        </p>
      </div>
      <div>
        <el-button :loading="taskQuery.isFetching.value" @click="taskQuery.refetch()">
          重试接口
        </el-button>
        <el-button type="primary" @click="useDemoData">使用演示数据</el-button>
      </div>
    </section>

    <template v-else>
      <InterviewTaskQueue
        :tasks="tasks"
        :selected-id="selectedInterviewId"
        :loading="taskQuery.isLoading.value"
        @select="selectInterview"
      />

      <section v-if="workspaceError && !demoMode" class="interview-error" role="alert">
        <div>
          <h3>面试详情加载失败</h3>
          <p>{{ workspaceError.message }}</p>
        </div>
        <el-button @click="workspaceQuery.refetch()">重新加载</el-button>
      </section>

      <div v-else-if="workspaceQuery.isLoading.value" class="interview-loading">
        <el-skeleton :rows="12" animated />
      </div>

      <template v-else-if="workspace">
        <section
          v-if="workspace.feedbackState !== 'SUBMITTED'"
          class="interview-reminder"
          role="status"
        >
          <TriangleAlert :size="18" :stroke-width="1.75" aria-hidden="true" />
          <div>
            <strong>{{ canEdit ? '反馈尚未完成' : '等待面试官提交反馈' }}</strong>
            <span>
              {{
                canEdit
                  ? '请完成所有评分与评价证据；提交后如需修改，必须走审计流程。'
                  : 'HR 可以查看候选人上下文，但不能代替面试官填写原始评价。'
              }}
            </span>
          </div>
        </section>

        <section class="interview-workspace">
          <CandidateInterviewBrief :interview="workspace" />
          <InterviewScorecard
            :scorecard="scorecard"
            :comment="comment"
            :suggestion="suggestion"
            :overall-score="overallScore"
            :readonly="readonly"
            :saving="draftMutation.isPending.value"
            :submitting="submitMutation.isPending.value"
            @update:scorecard="scorecard = $event"
            @update:comment="comment = $event"
            @update:suggestion="suggestion = $event"
            @save="saveDraft"
            @submit="submitFeedback"
          />
          <InterviewCopilotPanel
            :questions="visibleQuestions"
            :ai-summary="workspace.feedback.aiSummary"
            :generating="questionMutation.isPending.value"
            @generate="generateQuestion"
          />
        </section>
      </template>

      <el-empty v-else description="请选择一个面试任务进入工作区" :image-size="80" />
    </template>
  </div>
</template>

<style scoped lang="scss">
.interviews-view {
  display: grid;
  gap: var(--rs-space-4);
}

.interviews-view__intro,
.interviews-view__authority,
.interview-source,
.interview-toolbar__actions,
.interview-error,
.interview-reminder {
  display: flex;
  align-items: center;
}

.interviews-view__intro,
.interview-source,
.interview-error {
  justify-content: space-between;
}

.interviews-view__intro p,
.interview-error h3,
.interview-error p {
  margin: 0;
}

.interviews-view__intro p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}

.interviews-view__authority {
  gap: var(--rs-space-2);
  color: var(--rs-blue-700);
  font-size: 12px;
  font-weight: 600;
}

.interview-source {
  min-height: 40px;
  padding: 0 var(--rs-space-3);
  border: 1px solid var(--rs-blue-500);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-selected);
  color: var(--rs-blue-700);
}

.interview-toolbar {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) 180px 180px auto;
  gap: var(--rs-space-2);
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.interview-toolbar__actions {
  justify-content: flex-end;
  gap: var(--rs-space-2);
}

.interview-error {
  min-height: 112px;
  gap: var(--rs-space-6);
  padding: var(--rs-space-4);
  border: 1px solid var(--rs-danger-700);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-danger-050);
}

.interview-error > div:last-child {
  display: flex;
  gap: var(--rs-space-2);
}

.interview-error h3 {
  font-size: 14px;
}

.interview-error p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}

.interview-loading {
  min-height: 520px;
  padding: var(--rs-space-6);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.interview-reminder {
  align-items: flex-start;
  gap: var(--rs-space-3);
  padding: var(--rs-space-3) var(--rs-space-4);
  border: 1px solid var(--rs-warning-800);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-warning-050);
  color: var(--rs-warning-800);
}

.interview-reminder svg {
  flex: 0 0 auto;
  margin-top: var(--rs-space-1);
}

.interview-reminder div {
  display: grid;
}

.interview-reminder span {
  color: var(--rs-text-secondary);
  font-size: 12px;
}

.interview-workspace {
  display: grid;
  grid-template-columns: minmax(216px, 240px) minmax(420px, 1fr) minmax(280px, 320px);
  gap: var(--rs-space-3);
  align-items: start;
}

@media (max-width: 1439px) {
  .interview-workspace {
    grid-template-columns: 216px minmax(420px, 1fr) 280px;
  }
}
</style>
