<script setup lang="ts">
import { useQuery } from '@tanstack/vue-query'
import {
  ArrowRight,
  CalendarCheck,
  CalendarClock,
  CheckCircle2,
  ClipboardCheck,
  Clock3,
  ExternalLink,
} from 'lucide-vue-next'
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

import { getInterviewTasks } from '@/api/interviews'
import InterviewerEmptyState from '@/components/interviewer/InterviewerEmptyState.vue'
import InterviewerErrorState from '@/components/interviewer/InterviewerErrorState.vue'
import InterviewerPageHeader from '@/components/interviewer/InterviewerPageHeader.vue'
import {
  getInterviewerPriorityTask,
  getInterviewerStageText,
  getInterviewerStageTone,
  getInterviewerTaskStage,
  isHttpMeetingLocation,
} from '@/config/interviewer'
import type { InterviewFeedbackState, InterviewStatus, InterviewTaskQuery } from '@/types/interview'

function taskQuery(
  status: InterviewStatus | '',
  feedbackState: InterviewFeedbackState | '',
  pageSize = 1,
): InterviewTaskQuery {
  return { keyword: '', status, feedbackState, page: 1, pageSize }
}

const assignedQuery = useQuery({
  queryKey: ['interviewer-dashboard', 'assigned'],
  queryFn: () => getInterviewTasks(taskQuery('ASSIGNED', '', 3)),
})
const scheduledQuery = useQuery({
  queryKey: ['interviewer-dashboard', 'scheduled'],
  queryFn: () => getInterviewTasks(taskQuery('SCHEDULED', '', 10)),
})
const feedbackEmptyQuery = useQuery({
  queryKey: ['interviewer-dashboard', 'feedback-empty'],
  queryFn: () => getInterviewTasks(taskQuery('COMPLETED', 'EMPTY', 3)),
})
const feedbackDraftQuery = useQuery({
  queryKey: ['interviewer-dashboard', 'feedback-draft'],
  queryFn: () => getInterviewTasks(taskQuery('COMPLETED', 'DRAFT', 3)),
})
const submittedQuery = useQuery({
  queryKey: ['interviewer-dashboard', 'submitted'],
  queryFn: () => getInterviewTasks(taskQuery('', 'SUBMITTED', 1)),
})
const recentQuery = useQuery({
  queryKey: ['interviewer-dashboard', 'recent'],
  queryFn: () => getInterviewTasks(taskQuery('', '', 6)),
})

const queries = [
  assignedQuery,
  scheduledQuery,
  feedbackEmptyQuery,
  feedbackDraftQuery,
  submittedQuery,
  recentQuery,
]
const loading = computed(() => queries.some((query) => query.isLoading.value))
const fetching = computed(() => queries.some((query) => query.isFetching.value))
const hasError = computed(() => queries.some((query) => query.error.value))
const pendingFeedbackTotal = computed(
  () => (feedbackEmptyQuery.data.value?.total ?? 0) + (feedbackDraftQuery.data.value?.total ?? 0),
)
const priorityTask = computed(() =>
  getInterviewerPriorityTask({
    feedbackEmpty: feedbackEmptyQuery.data.value?.items ?? [],
    feedbackDraft: feedbackDraftQuery.data.value?.items ?? [],
    scheduled: scheduledQuery.data.value?.items ?? [],
    assigned: assignedQuery.data.value?.items ?? [],
  }),
)
const recentTasks = computed(() => recentQuery.data.value?.items ?? [])

const metrics = computed(() => [
  {
    label: '待预约',
    value: assignedQuery.data.value?.total ?? 0,
    icon: CalendarClock,
    query: 'status=ASSIGNED',
  },
  {
    label: '待参加',
    value: scheduledQuery.data.value?.total ?? 0,
    icon: CalendarCheck,
    query: 'status=SCHEDULED',
  },
  {
    label: '待提交反馈',
    value: pendingFeedbackTotal.value,
    icon: ClipboardCheck,
    query: 'status=COMPLETED',
  },
  {
    label: '已提交反馈',
    value: submittedQuery.data.value?.total ?? 0,
    icon: CheckCircle2,
    query: 'feedbackState=SUBMITTED',
  },
])

function refresh() {
  for (const query of queries) void query.refetch()
}

function formatDateTime(value: string | null) {
  if (!value) return '时间待确认'
  return new Intl.DateTimeFormat('zh-CN', {
    month: 'long',
    day: 'numeric',
    weekday: 'short',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(new Date(value))
}

function priorityTitle() {
  const task = priorityTask.value
  if (!task) return '当前没有待处理任务'
  const stage = getInterviewerTaskStage(task)
  if (stage === 'FEEDBACK') return '这场面试等待你的反馈'
  if (stage === 'SCHEDULE') return '新的面试任务等待确认安排'
  if (stage === 'ATTEND' && task.interviewTime && new Date(task.interviewTime) <= new Date()) {
    return '面试时间已过，请确认是否已经结束'
  }
  return '下一场需要关注的面试'
}

function priorityActionLabel() {
  const task = priorityTask.value
  if (!task) return '查看任务'
  const stage = getInterviewerTaskStage(task)
  if (stage === 'SCHEDULE') return '预约面试'
  if (stage === 'ATTEND') return '查看安排'
  if (stage === 'FEEDBACK') return '完成反馈'
  return '查看任务'
}
</script>

<template>
  <div class="interviewer-dashboard">
    <InterviewerPageHeader
      title="工作台"
      description="优先处理面试安排和待提交反馈，所有业务状态仍由人工确认。"
    >
      <template #actions
        ><el-button :loading="fetching" @click="refresh">刷新任务</el-button></template
      >
    </InterviewerPageHeader>

    <InterviewerErrorState
      v-if="hasError && !loading"
      title="面试任务暂时无法加载"
      description="请重新加载；已填写但尚未保存的反馈不会在此页面产生。"
      :loading="fetching"
      @retry="refresh"
    />

    <template v-else>
      <section
        v-if="loading"
        class="interviewer-dashboard__priority interviewer-dashboard__skeleton"
      >
        <el-skeleton :rows="4" animated />
      </section>
      <section v-else-if="priorityTask" class="interviewer-dashboard__priority">
        <div class="interviewer-dashboard__priority-copy">
          <span class="interviewer-dashboard__eyebrow">当前优先任务</span>
          <h2>{{ priorityTitle() }}</h2>
          <h3>{{ priorityTask.candidateName }} · {{ priorityTask.jobTitle }}</h3>
          <p>
            {{ formatDateTime(priorityTask.interviewTime) }} · {{ priorityTask.roundText }} ·
            {{ priorityTask.methodText }}
          </p>
          <div class="interviewer-dashboard__priority-state">
            <span
              :class="`rs-status-pill rs-status-pill--${getInterviewerStageTone(getInterviewerTaskStage(priorityTask))}`"
            >
              {{ getInterviewerStageText(getInterviewerTaskStage(priorityTask)) }}
            </span>
            <span>{{ priorityTask.feedbackStateText }}</span>
          </div>
        </div>
        <div class="interviewer-dashboard__priority-actions">
          <el-button
            v-if="
              isHttpMeetingLocation(priorityTask.location) && priorityTask.status === 'SCHEDULED'
            "
            tag="a"
            :href="priorityTask.location || undefined"
            target="_blank"
            rel="noopener noreferrer"
            :icon="ExternalLink"
            >打开会议链接</el-button
          >
          <RouterLink :to="`/interviewer/interviews?interviewId=${priorityTask.id}`">
            <el-button type="primary">{{ priorityActionLabel() }}</el-button>
          </RouterLink>
        </div>
      </section>
      <InterviewerEmptyState
        v-else
        title="当前没有待处理面试"
        description="新的面试任务或待提交反馈会优先显示在这里。"
      >
        <template #actions
          ><RouterLink to="/interviewer/interviews"
            ><el-button type="primary">查看我的面试</el-button></RouterLink
          ></template
        >
      </InterviewerEmptyState>

      <section class="interviewer-dashboard__metrics" aria-label="面试任务摘要">
        <RouterLink
          v-for="metric in metrics"
          :key="metric.label"
          :to="`/interviewer/interviews?${metric.query}`"
          class="interviewer-dashboard__metric"
        >
          <span><component :is="metric.icon" :size="18" :stroke-width="1.75" /></span>
          <div>
            <small>{{ metric.label }}</small
            ><strong>{{ metric.value }}</strong>
          </div>
          <ArrowRight :size="16" :stroke-width="1.75" aria-hidden="true" />
        </RouterLink>
      </section>

      <section class="interviewer-dashboard__schedule">
        <header>
          <div>
            <h2>近期任务</h2>
            <p>按面试安排时间展示当前接口返回的最近任务。</p>
          </div>
          <RouterLink to="/interviewer/interviews">查看全部</RouterLink>
        </header>
        <div v-if="loading" class="interviewer-dashboard__schedule-loading">
          <el-skeleton :rows="4" animated />
        </div>
        <div v-else-if="recentTasks.length" class="interviewer-dashboard__timeline">
          <RouterLink
            v-for="task in recentTasks"
            :key="task.id"
            :to="`/interviewer/interviews?interviewId=${task.id}`"
            class="interviewer-dashboard__timeline-item"
          >
            <span class="interviewer-dashboard__timeline-icon"
              ><Clock3 :size="16" :stroke-width="1.75"
            /></span>
            <div>
              <strong>{{ formatDateTime(task.interviewTime) }}</strong
              ><span>{{ task.candidateName }} · {{ task.jobTitle }} · {{ task.roundText }}</span>
            </div>
            <span
              :class="`rs-status-pill rs-status-pill--${getInterviewerStageTone(getInterviewerTaskStage(task))}`"
              >{{ getInterviewerStageText(getInterviewerTaskStage(task)) }}</span
            >
          </RouterLink>
        </div>
        <p v-else class="interviewer-dashboard__schedule-empty">暂无近期任务。</p>
      </section>
    </template>
  </div>
</template>

<style scoped lang="scss">
.interviewer-dashboard {
  display: grid;
  gap: var(--rs-space-6);
  max-width: var(--rs-interviewer-content-max-width);
  margin: 0 auto;
}
.interviewer-dashboard__priority {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--rs-space-8);
  min-height: 220px;
  padding: var(--rs-space-8);
  border: 1px solid var(--rs-blue-500);
  border-radius: var(--rs-radius-md);
  background: var(--rs-blue-050);
}
.interviewer-dashboard__skeleton {
  display: block;
}
.interviewer-dashboard__priority-copy {
  display: grid;
  gap: var(--rs-space-2);
}
.interviewer-dashboard__eyebrow {
  color: var(--rs-blue-700);
  font-size: 12px;
  font-weight: 600;
}
.interviewer-dashboard__priority h2,
.interviewer-dashboard__priority h3,
.interviewer-dashboard__priority p {
  margin: 0;
}
.interviewer-dashboard__priority h2 {
  font-size: 24px;
}
.interviewer-dashboard__priority h3 {
  margin-top: var(--rs-space-2);
  font-size: 18px;
}
.interviewer-dashboard__priority p {
  color: var(--rs-text-secondary);
  font-variant-numeric: tabular-nums;
}
.interviewer-dashboard__priority-state,
.interviewer-dashboard__priority-actions {
  display: flex;
  align-items: center;
  gap: var(--rs-space-2);
}
.interviewer-dashboard__priority-state {
  margin-top: var(--rs-space-2);
  color: var(--rs-text-secondary);
  font-size: 12px;
}
.interviewer-dashboard__priority-actions {
  flex: 0 0 auto;
}
.interviewer-dashboard__metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--rs-space-3);
}
.interviewer-dashboard__metric {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr) auto;
  align-items: center;
  gap: var(--rs-space-3);
  min-height: 88px;
  padding: var(--rs-space-4);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
  color: var(--rs-text-primary);
  transition: border-color var(--rs-motion-fast) var(--rs-ease-standard);
}
.interviewer-dashboard__metric:hover {
  border-color: var(--rs-blue-500);
}
.interviewer-dashboard__metric > span {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-blue-050);
  color: var(--rs-blue-700);
}
.interviewer-dashboard__metric div {
  display: grid;
}
.interviewer-dashboard__metric small {
  color: var(--rs-text-secondary);
}
.interviewer-dashboard__metric strong {
  font-size: 24px;
  font-variant-numeric: tabular-nums;
}
.interviewer-dashboard__schedule {
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
.interviewer-dashboard__schedule > header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--rs-space-4) var(--rs-space-6);
  border-bottom: 1px solid var(--rs-border-default);
}
.interviewer-dashboard__schedule h2,
.interviewer-dashboard__schedule p {
  margin: 0;
}
.interviewer-dashboard__schedule h2 {
  font-size: 18px;
}
.interviewer-dashboard__schedule header p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
  font-size: 12px;
}
.interviewer-dashboard__timeline {
  display: grid;
}
.interviewer-dashboard__timeline-item {
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr) auto;
  align-items: center;
  gap: var(--rs-space-3);
  min-height: 68px;
  padding: var(--rs-space-3) var(--rs-space-6);
  color: var(--rs-text-primary);
}
.interviewer-dashboard__timeline-item + .interviewer-dashboard__timeline-item {
  border-top: 1px solid var(--rs-border-default);
}
.interviewer-dashboard__timeline-item:hover {
  background: var(--rs-surface-subtle);
}
.interviewer-dashboard__timeline-icon {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-muted);
  color: var(--rs-blue-700);
}
.interviewer-dashboard__timeline-item div {
  display: grid;
  gap: var(--rs-space-1);
}
.interviewer-dashboard__timeline-item span {
  color: var(--rs-text-secondary);
  font-size: 12px;
}
.interviewer-dashboard__schedule-loading,
.interviewer-dashboard__schedule-empty {
  padding: var(--rs-space-6);
}
@media (max-width: 1280px) {
  .interviewer-dashboard__metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
