<script setup lang="ts">
import { Clock3, Sparkles, UserRound } from 'lucide-vue-next'

import type { PipelineApplicationSummary } from '@/types/pipeline'

defineProps<{
  applications: PipelineApplicationSummary[]
  stageLabel: string
}>()

const emit = defineEmits<{
  select: [id: number]
}>()

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
  <div class="pipeline-board" :aria-label="`${stageLabel}卡片列表`">
    <button
      v-for="application in applications"
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

    <div v-if="!applications.length" class="pipeline-board__empty">
      <strong>暂无{{ stageLabel }}记录</strong>
      <span>切换其他入口，或调整职位、状态和关键字筛选条件。</span>
    </div>
  </div>
</template>

<style scoped lang="scss">
.pipeline-board {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  align-content: start;
  gap: var(--rs-space-3);
  min-height: calc(100dvh - 392px);
  padding: var(--rs-space-3);
  background: var(--rs-surface-subtle);
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

.pipeline-card__job,
.pipeline-card__meta {
  color: var(--rs-text-tertiary);
  font-size: 12px;
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

.pipeline-board__empty {
  display: grid;
  grid-column: 1 / -1;
  min-height: 220px;
  align-content: center;
  justify-items: center;
  gap: var(--rs-space-2);
  place-items: center;
  color: var(--rs-text-tertiary);
  font-size: 13px;
}

.pipeline-board__empty strong {
  color: var(--rs-text-primary);
  font-size: 14px;
}

@media (prefers-reduced-motion: reduce) {
  .pipeline-card {
    transition: none;
  }
}
</style>
