<script setup lang="ts">
import { CalendarClock, MapPin, Video } from 'lucide-vue-next'

import { getFeedbackStateTone, getInterviewStatusTone } from '@/config/interviews'
import type { InterviewTaskSummary } from '@/types/interview'

defineProps<{
  tasks: InterviewTaskSummary[]
  selectedId: number | null
  loading: boolean
}>()

const emit = defineEmits<{
  select: [id: number]
}>()

function formatTaskTime(value: string) {
  const date = new Date(value)
  return new Intl.DateTimeFormat('zh-CN', {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(date)
}
</script>

<template>
  <div v-if="loading" class="task-queue__loading">
    <el-skeleton :rows="2" animated />
  </div>
  <div v-else-if="tasks.length" class="task-queue" role="list" aria-label="面试任务队列">
    <button
      v-for="task in tasks"
      :key="task.id"
      type="button"
      class="task-queue__item"
      :class="{ 'task-queue__item--selected': task.id === selectedId }"
      :aria-pressed="task.id === selectedId"
      @click="emit('select', task.id)"
    >
      <span class="task-queue__avatar">{{ task.candidateName.slice(0, 1) }}</span>
      <span class="task-queue__main">
        <span class="task-queue__title">
          <strong>{{ task.candidateName }}</strong>
          <span>{{ task.roundText }}</span>
        </span>
        <span class="task-queue__job">{{ task.jobTitle }}</span>
        <span class="task-queue__meta">
          <CalendarClock :size="14" :stroke-width="1.75" aria-hidden="true" />
          {{ formatTaskTime(task.interviewTime) }}
          <component
            :is="task.method === 'ONLINE' ? Video : MapPin"
            :size="14"
            :stroke-width="1.75"
            aria-hidden="true"
          />
          {{ task.methodText }}
        </span>
      </span>
      <span class="task-queue__states">
        <span :class="`rs-status-pill rs-status-pill--${getInterviewStatusTone(task.status)}`">
          {{ task.statusText }}
        </span>
        <span :class="`rs-status-pill rs-status-pill--${getFeedbackStateTone(task.feedbackState)}`">
          {{ task.feedbackStateText }}
        </span>
      </span>
    </button>
  </div>
  <el-empty v-else description="当前筛选条件下没有面试任务" :image-size="72" />
</template>

<style scoped lang="scss">
.task-queue {
  display: grid;
  grid-auto-columns: minmax(300px, 1fr);
  grid-auto-flow: column;
  gap: var(--rs-space-2);
  overflow-x: auto;
  padding-bottom: var(--rs-space-1);
}

.task-queue__item {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr) auto;
  gap: var(--rs-space-3);
  min-height: 96px;
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
  color: var(--rs-text-primary);
  text-align: left;
  cursor: pointer;
  transition:
    border-color var(--rs-motion-fast) var(--rs-ease-standard),
    background-color var(--rs-motion-fast) var(--rs-ease-standard),
    transform var(--rs-motion-fast) var(--rs-ease-standard);
}

.task-queue__item:hover,
.task-queue__item--selected {
  border-color: var(--rs-action-primary);
  background: var(--rs-surface-selected);
}

.task-queue__item:active {
  transform: scale(0.99);
}

.task-queue__avatar {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-action-primary);
  color: var(--rs-white);
  font-size: 16px;
  font-weight: 700;
}

.task-queue__main,
.task-queue__states {
  display: grid;
  align-content: start;
}

.task-queue__title,
.task-queue__meta {
  display: flex;
  align-items: center;
}

.task-queue__title {
  gap: var(--rs-space-2);
}

.task-queue__title span,
.task-queue__job,
.task-queue__meta {
  color: var(--rs-text-secondary);
  font-size: 12px;
}

.task-queue__job {
  overflow: hidden;
  margin-top: var(--rs-space-1);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-queue__meta {
  gap: var(--rs-space-1);
  margin-top: var(--rs-space-2);
  font-variant-numeric: tabular-nums;
}

.task-queue__meta svg:nth-of-type(2) {
  margin-left: var(--rs-space-2);
}

.task-queue__states {
  justify-items: end;
  gap: var(--rs-space-1);
}

.task-queue__loading {
  min-height: 96px;
  padding: var(--rs-space-3);
}
</style>
