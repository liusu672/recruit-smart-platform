<script setup lang="ts">
import { Clock3, Sparkles, UserRound } from 'lucide-vue-next'
import { computed } from 'vue'

import { getPipelineStageKey, pipelineStages } from '@/config/pipeline'
import type { PipelineApplicationSummary } from '@/types/pipeline'

const props = defineProps<{
  applications: PipelineApplicationSummary[]
}>()

const emit = defineEmits<{
  select: [id: number]
}>()

const columns = computed(() =>
  pipelineStages.map((stage) => ({
    ...stage,
    applications: props.applications.filter(
      (application) => getPipelineStageKey(application.status) === stage.key,
    ),
  })),
)

function formatActivity(value: string) {
  const elapsed = Date.now() - new Date(value).getTime()
  if (!Number.isFinite(elapsed) || elapsed < 0) return '刚刚更新'
  const minutes = Math.floor(elapsed / 60_000)
  if (minutes < 60) return `${Math.max(1, minutes)} 分钟前更新`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours} 小时前更新`
  const days = Math.floor(hours / 24)
  return `${days} 天前更新`
}
</script>

<template>
  <div class="pipeline-board" aria-label="招聘流程看板">
    <section
      v-for="column in columns"
      :key="column.key"
      class="pipeline-column"
      :class="{
        'pipeline-column--result': column.key === 'HIRED' || column.key === 'CLOSED',
        'pipeline-column--hired': column.key === 'HIRED',
        'pipeline-column--closed': column.key === 'CLOSED',
      }"
    >
      <header class="pipeline-column__header">
        <div>
          <strong>{{ column.label }}</strong>
          <span>{{ column.description }}</span>
        </div>
        <span class="pipeline-column__count">{{ column.applications.length }}</span>
      </header>

      <div class="pipeline-column__body">
        <button
          v-for="application in column.applications"
          :key="application.id"
          class="pipeline-card"
          type="button"
          @click="emit('select', application.id)"
        >
          <span class="pipeline-card__heading">
            <strong>{{ application.candidateName }}</strong>
            <span
              v-if="application.matchScore !== null"
              class="pipeline-card__score"
              title="AI 匹配分仅供参考"
            >
              <Sparkles :size="13" :stroke-width="1.75" aria-hidden="true" />
              AI {{ application.matchScore }}
            </span>
          </span>
          <span class="pipeline-card__job">{{ application.jobTitle }}</span>
          <span class="pipeline-card__meta">
            <span>
              <UserRound :size="13" :stroke-width="1.75" aria-hidden="true" />
              {{ application.ownerName || '待分配' }}
            </span>
            <span>
              <Clock3 :size="13" :stroke-width="1.75" aria-hidden="true" />
              {{ formatActivity(application.lastActivityAt) }}
            </span>
          </span>
          <span v-if="application.reviewDecision === 'PENDING'" class="pipeline-card__pending">
            待补充核实
          </span>
        </button>

        <div v-if="!column.applications.length" class="pipeline-column__empty">暂无记录</div>
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.pipeline-board {
  display: grid;
  min-width: 1320px;
  grid-template-columns: repeat(4, minmax(220px, 1fr)) minmax(250px, 0.9fr);
  grid-template-rows: repeat(2, minmax(0, 1fr));
  gap: var(--rs-space-3);
}

.pipeline-column {
  min-width: 0;
  min-height: calc(100dvh - 292px);
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
}

.pipeline-column:nth-child(-n + 4) {
  grid-row: 1 / span 2;
}
.pipeline-column--result {
  margin-left: 8px;
  min-height: 0;
}
.pipeline-column--hired {
  grid-column: 5;
  grid-row: 1;
}
.pipeline-column--closed {
  grid-column: 5;
  grid-row: 2;
}
.pipeline-column--hired .pipeline-column__header {
  border-top: 3px solid var(--rs-success-700);
}
.pipeline-column--closed .pipeline-column__header {
  border-top: 3px solid var(--rs-gray-400);
}
.pipeline-column:not(.pipeline-column--result) .pipeline-column__header {
  border-top: 3px solid var(--rs-blue-500);
}

.pipeline-column__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  min-height: 68px;
  padding: var(--rs-space-3);
  border-bottom: 1px solid var(--rs-border-default);
  background: var(--rs-surface-primary);
}

.pipeline-column__header div {
  display: grid;
  gap: var(--rs-space-1);
}

.pipeline-column__header strong {
  color: var(--rs-text-primary);
  font-size: 13px;
}

.pipeline-column__header span,
.pipeline-card__job,
.pipeline-card__meta {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}

.pipeline-column__count {
  display: grid;
  min-width: 24px;
  height: 24px;
  place-items: center;
  border-radius: var(--rs-radius-pill);
  background: var(--rs-surface-muted);
  color: var(--rs-text-secondary);
  font-variant-numeric: tabular-nums;
  font-weight: 700;
}

.pipeline-column__body {
  display: grid;
  align-content: start;
  gap: var(--rs-space-2);
  padding: var(--rs-space-2);
}

.pipeline-card {
  display: grid;
  gap: var(--rs-space-2);
  width: 100%;
  min-height: 112px;
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  outline: 0;
  background: var(--rs-surface-primary);
  color: var(--rs-text-primary);
  text-align: left;
  cursor: pointer;
  transition:
    border-color var(--rs-motion-fast) var(--rs-ease-standard),
    transform var(--rs-motion-fast) var(--rs-ease-standard);
}

.pipeline-card:hover,
.pipeline-card:focus-visible {
  border-color: var(--rs-blue-500);
}

.pipeline-card:focus-visible {
  box-shadow: 0 0 0 3px rgb(49 131 216 / 16%);
}

.pipeline-card:active {
  transform: scale(0.99);
}

.pipeline-card__heading,
.pipeline-card__meta,
.pipeline-card__meta span,
.pipeline-card__score {
  display: flex;
  align-items: center;
}

.pipeline-card__heading,
.pipeline-card__meta {
  justify-content: space-between;
  gap: var(--rs-space-2);
}

.pipeline-card__heading strong,
.pipeline-card__job {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pipeline-card__score {
  flex: 0 0 auto;
  gap: var(--rs-space-1);
  color: var(--rs-blue-700);
  font-variant-numeric: tabular-nums;
  font-weight: 700;
}

.pipeline-card__meta span {
  gap: var(--rs-space-1);
}

.pipeline-card__pending {
  justify-self: start;
  padding: 2px 6px;
  border-radius: var(--rs-radius-xs);
  background: var(--rs-warning-050);
  color: var(--rs-warning-800);
  font-size: 12px;
  font-weight: 600;
}

.pipeline-column__empty {
  display: grid;
  min-height: 96px;
  place-items: center;
  color: var(--rs-text-tertiary);
  font-size: 12px;
}

@media (prefers-reduced-motion: reduce) {
  .pipeline-card {
    transition: none;
  }
}
</style>
