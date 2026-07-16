<script setup lang="ts">
import { Clock3, Sparkles, UserRound } from 'lucide-vue-next'
import { computed } from 'vue'

import { getPipelineStageKey, pipelineStages } from '@/config/pipeline'
import type { PipelineApplication } from '@/types/pipeline'

const props = defineProps<{
  applications: PipelineApplication[]
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
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}
</script>

<template>
  <div class="pipeline-board" aria-label="招聘流程看板">
    <section v-for="column in columns" :key="column.key" class="pipeline-column">
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
            <span v-if="application.aiMatch" class="pipeline-card__score">
              <Sparkles :size="13" :stroke-width="1.75" aria-hidden="true" />
              {{ application.aiMatch.score }}
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
  grid-template-columns: repeat(6, minmax(200px, 1fr));
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
  gap: var(--rs-space-1);
  color: var(--rs-success-700);
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
