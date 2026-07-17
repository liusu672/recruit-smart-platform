<script setup lang="ts">
import { AlertTriangle, CheckCircle2, Clock3, RefreshCw } from 'lucide-vue-next'
import { computed } from 'vue'
import { useRouter } from 'vue-router'

import { useDashboardOverview } from '@/composables/useDashboardOverview'
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

function reloadOverview() {
  void overviewQuery.refetch()
}

function openTask(task: DashboardTask) {
  void router.push(getDashboardTaskRoute(task.type))
}
</script>

<template>
  <div class="dashboard-view">
    <section class="dashboard-metrics" aria-label="招聘指标">
      <article v-for="metric in metrics" :key="metric.label" class="metric-card">
        <span>{{ metric.label }}</span>
        <strong>{{ isInitialLoading ? '--' : metric.value }}</strong>
        <small>{{ metric.status }}</small>
      </article>
    </section>

    <section class="work-panel">
      <div class="work-panel__header">
        <div>
          <h2 class="rs-section-title">需要决策的任务</h2>
          <p>优先展示会影响招聘推进和候选人体验的事项。</p>
        </div>
        <el-tooltip content="重新加载仪表盘">
          <el-button
            circle
            :icon="RefreshCw"
            :loading="overviewQuery.isFetching.value"
            aria-label="重新加载仪表盘"
            @click="reloadOverview"
          />
        </el-tooltip>
      </div>

      <section v-if="overviewError" class="dashboard-state dashboard-state--error">
        <div>
          <h3>仪表盘接口暂不可用</h3>
          <p>{{ overviewError.message }}。请确认 Gateway、service-biz 和当前账号权限正常。</p>
        </div>
        <el-button type="primary" :icon="RefreshCw" @click="reloadOverview">重新加载</el-button>
      </section>

      <section v-else-if="isInitialLoading" class="dashboard-state">
        <span class="dashboard-state__spinner" aria-hidden="true" />
        <p>正在加载真实仪表盘数据...</p>
      </section>

      <section v-else-if="taskRows.length === 0" class="dashboard-state">
        <CheckCircle2 :size="22" :stroke-width="1.75" aria-hidden="true" />
        <div>
          <h3>暂无待处理任务</h3>
          <p>
            当前数据库统计为 0 时这是正常状态，产生待筛选投递、反馈草稿、Offer
            草稿或审核中入职流程后会显示数据。
          </p>
        </div>
      </section>

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

.metric-card {
  display: grid;
  gap: var(--rs-space-2);
  padding: var(--rs-space-4);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.metric-card span,
.metric-card small {
  color: var(--rs-text-secondary);
}

.metric-card strong {
  color: var(--rs-text-primary);
  font-size: 28px;
  font-weight: 600;
  line-height: 1.15;
  font-variant-numeric: tabular-nums;
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
  padding: var(--rs-space-5);
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

.dashboard-state__spinner {
  width: 18px;
  height: 18px;
  border: 2px solid var(--rs-border-default);
  border-top-color: var(--rs-blue-600);
  border-radius: 999px;
  animation: dashboard-spin 0.9s linear infinite;
}

@keyframes dashboard-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
