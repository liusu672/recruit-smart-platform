<script setup lang="ts">
import { AlertTriangle, CheckCircle2, Clock3, RefreshCw } from 'lucide-vue-next'
import { computed } from 'vue'
import { useRouter } from 'vue-router'

import { useDashboardOverview } from '@/composables/useDashboardOverview'
import HrEmptyState from '@/components/hr/HrEmptyState.vue'
import HrErrorState from '@/components/hr/HrErrorState.vue'
import HrMetricCard from '@/components/hr/HrMetricCard.vue'
import HrPageHeader from '@/components/hr/HrPageHeader.vue'
import {
  buildDashboardMetricCards,
  getDashboardTaskKey,
  getDashboardTaskMeta,
  getDashboardTaskRoute,
  getDashboardTaskTone,
} from '@/config/dashboard'
import type { DashboardTask } from '@/types/dashboard'

const router = useRouter()
const overviewQuery = useDashboardOverview()

const isInitialLoading = computed(() => overviewQuery.isLoading.value && !overviewQuery.data.value)
const metrics = computed(() => buildDashboardMetricCards(overviewQuery.data.value?.metrics))
const taskRows = computed(() =>
  (overviewQuery.data.value?.tasks ?? []).map((task) => ({
    ...task,
    key: getDashboardTaskKey(task),
    meta: getDashboardTaskMeta(task),
    route: getDashboardTaskRoute(task.type),
    tone: getDashboardTaskTone(task.type),
  })),
)
const overviewError = computed(() => overviewQuery.error.value as Error | null)
const metricRoutes = {
  pendingScreening: '/hr/pipeline',
  pendingFeedback: '/hr/interviews',
  activeOffers: '/hr/offers',
  reviewingOnboardings: '/hr/onboardings',
} as const

function reloadOverview() {
  void overviewQuery.refetch()
}

function openTask(task: DashboardTask) {
  const path = getDashboardTaskRoute(task.type)
  const id = task.applicationId ?? task.relatedId
  if (!Number.isFinite(id) || id <= 0) {
    void router.push(path)
    return
  }
  const query =
    task.type === 'SCREENING'
      ? { applicationId: String(id) }
      : task.type === 'INTERVIEW_FEEDBACK'
        ? { interviewId: String(task.relatedId) }
        : task.type === 'OFFER'
          ? { offerId: String(task.relatedId) }
          : { onboardingId: String(task.relatedId) }
  void router.push({ path, query })
}
</script>

<template>
  <div class="dashboard-view">
    <HrPageHeader title="招聘工作台" description="集中处理会影响招聘推进和候选人体验的事项。">
      <template #actions>
        <el-button
          :icon="RefreshCw"
          :loading="overviewQuery.isFetching.value"
          @click="reloadOverview"
        >
          刷新数据
        </el-button>
      </template>
    </HrPageHeader>

    <section class="dashboard-metrics" aria-label="招聘指标">
      <RouterLink v-for="metric in metrics" :key="metric.label" :to="metricRoutes[metric.key]">
        <HrMetricCard
          :label="metric.label"
          :value="metric.value"
          :supporting="metric.status"
          :loading="isInitialLoading"
          :tone="metric.value > 0 ? 'warning' : 'neutral'"
        />
      </RouterLink>
    </section>

    <section class="work-panel">
      <div class="work-panel__header">
        <div>
          <h2 class="rs-section-title">需要决策的任务</h2>
          <p>优先展示会影响招聘推进和候选人体验的事项。</p>
        </div>
        <span class="work-panel__hint">点击任务可直接进入对应业务页面</span>
      </div>

      <HrErrorState
        v-if="overviewError"
        title="任务暂时无法加载"
        description="请稍后重试。如果问题持续存在，请联系系统管理员。"
        :loading="overviewQuery.isFetching.value"
        @retry="reloadOverview"
      />

      <section v-else-if="isInitialLoading" class="dashboard-state">
        <el-skeleton :rows="4" animated />
      </section>

      <HrEmptyState
        v-else-if="taskRows.length === 0"
        :icon="CheckCircle2"
        title="当前没有待处理任务"
        description="新的筛选、面试反馈、Offer 或入职审核任务会显示在这里。"
      />

      <div v-else class="task-list">
        <button
          v-for="task in taskRows"
          :key="task.key"
          class="task-row"
          type="button"
          @click="openTask(task)"
        >
          <span class="task-row__icon" :class="`task-row__icon--${task.tone}`">
            <AlertTriangle
              v-if="task.tone === 'danger'"
              :size="18"
              :stroke-width="1.75"
              aria-hidden="true"
            />
            <Clock3
              v-else-if="task.tone === 'warning'"
              :size="18"
              :stroke-width="1.75"
              aria-hidden="true"
            />
            <CheckCircle2 v-else :size="18" :stroke-width="1.75" aria-hidden="true" />
          </span>
          <div>
            <h3>{{ task.title }}</h3>
            <p>{{ task.meta }}</p>
          </div>
          <span class="rs-status-pill" :class="`rs-status-pill--${task.tone}`">
            {{ task.statusText }}
          </span>
        </button>
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.dashboard-view {
  display: grid;
  gap: var(--rs-space-6);
}

.dashboard-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--rs-space-4);
}

.dashboard-metrics > a {
  color: inherit;
}

.work-panel {
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.work-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--rs-space-4);
  padding: var(--rs-space-4);
  border-bottom: 1px solid var(--rs-border-default);
}

.work-panel__header p {
  margin: var(--rs-space-1) 0 0;
  color: var(--rs-text-secondary);
}

.work-panel__hint {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}

.task-list {
  display: grid;
}

.task-row {
  display: grid;
  grid-template-columns: 40px 1fr auto;
  align-items: center;
  gap: var(--rs-space-3);
  width: 100%;
  min-height: 72px;
  padding: var(--rs-space-3) var(--rs-space-4);
  border: 0;
  border-bottom: 1px solid var(--rs-border-default);
  background: transparent;
  color: inherit;
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.task-row:hover {
  background: var(--rs-surface-subtle);
}

.task-row:focus-visible {
  outline: 2px solid var(--rs-blue-500);
  outline-offset: -2px;
}

.task-row:last-child {
  border-bottom: 0;
}

.task-row h3 {
  margin: 0;
  color: var(--rs-text-primary);
  font-size: 14px;
  font-weight: 600;
}

.task-row p {
  margin: var(--rs-space-1) 0 0;
  color: var(--rs-text-secondary);
}

.task-row__icon {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border-radius: var(--rs-radius-sm);
}

.task-row__icon--info {
  background: var(--rs-blue-050);
  color: var(--rs-blue-700);
}

.task-row__icon--warning {
  background: var(--rs-warning-050);
  color: var(--rs-warning-800);
}

.task-row__icon--danger {
  background: var(--rs-danger-050);
  color: var(--rs-danger-700);
}

.dashboard-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--rs-space-3);
  min-height: 168px;
  padding: var(--rs-space-6);
  color: var(--rs-text-secondary);
  text-align: center;
}

.dashboard-state h3,
.dashboard-state p {
  margin: 0;
}

.dashboard-state h3 {
  color: var(--rs-text-primary);
  font-size: 15px;
  font-weight: 600;
}

.dashboard-state p {
  margin-top: var(--rs-space-1);
  max-width: 640px;
}

.dashboard-state--error {
  justify-content: space-between;
  border-top: 1px solid var(--rs-danger-700);
  background: var(--rs-danger-050);
  text-align: left;
}
</style>
