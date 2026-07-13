<script setup lang="ts">
import { AlertTriangle, CheckCircle2, Clock3 } from 'lucide-vue-next'

const metrics = [
  { label: '待筛选候选人', value: '42', status: '本周新增 18 人' },
  { label: '待补充反馈', value: '7', status: '3 项接近 SLA' },
  { label: '进行中 Offer', value: '5', status: '2 项等待候选人回复' },
]

const tasks = [
  {
    title: '高级 Java 工程师候选人需要初筛',
    meta: '职位：后端平台组 · 来源：官网投递',
    state: '待处理',
    tone: 'warning',
  },
  {
    title: '产品经理二面反馈缺少结构化评分',
    meta: '面试官：张明 · 截止：今天 18:00',
    state: '需补充',
    tone: 'danger',
  },
  {
    title: 'AI 已生成候选人摘要，等待 HR 审核',
    meta: '候选人：李同学 · 附 3 条来源证据',
    state: '待确认',
    tone: 'info',
  },
]
</script>

<template>
  <div class="dashboard-view">
    <section class="dashboard-metrics" aria-label="招聘指标">
      <article v-for="metric in metrics" :key="metric.label" class="metric-card">
        <span>{{ metric.label }}</span>
        <strong>{{ metric.value }}</strong>
        <small>{{ metric.status }}</small>
      </article>
    </section>

    <section class="work-panel">
      <div class="work-panel__header">
        <div>
          <h2 class="rs-section-title">需要决策的任务</h2>
          <p>优先展示会影响招聘推进和候选人体验的事项。</p>
        </div>
      </div>

      <div class="task-list">
        <article v-for="task in tasks" :key="task.title" class="task-row">
          <div class="task-row__icon" :class="`task-row__icon--${task.tone}`">
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
          </div>
          <div>
            <h3>{{ task.title }}</h3>
            <p>{{ task.meta }}</p>
          </div>
          <span class="rs-status-pill" :class="`rs-status-pill--${task.tone}`">
            {{ task.state }}
          </span>
        </article>
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
  grid-template-columns: repeat(3, minmax(0, 1fr));
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
  min-height: 72px;
  padding: var(--rs-space-3) var(--rs-space-4);
  border-bottom: 1px solid var(--rs-border-default);
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
</style>
