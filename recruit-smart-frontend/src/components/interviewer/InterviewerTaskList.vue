<script setup lang="ts">
import { CalendarClock, MapPin, PanelLeftClose, Search, Video } from 'lucide-vue-next'

import {
  getInterviewerStageText,
  getInterviewerStageTone,
  getInterviewerTaskStage,
} from '@/config/interviewer'
import { interviewFeedbackStateOptions, interviewStatusOptions } from '@/config/interviews'
import type {
  InterviewFeedbackState,
  InterviewStatus,
  InterviewTaskSummary,
} from '@/types/interview'

defineProps<{
  tasks: InterviewTaskSummary[]
  selectedId: number | null
  keyword: string
  status: InterviewStatus | ''
  feedbackState: InterviewFeedbackState | ''
  loading: boolean
  page: number
  pageSize: number
  total: number
}>()

const emit = defineEmits<{
  select: [id: number]
  search: []
  reset: []
  'update:keyword': [value: string]
  'update:status': [value: InterviewStatus | '']
  'update:feedbackState': [value: InterviewFeedbackState | '']
  'update:page': [value: number]
  collapse: []
}>()

function formatTaskTime(value: string | null) {
  if (!value) return '时间待确认'
  return new Intl.DateTimeFormat('zh-CN', {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(new Date(value))
}
</script>

<template>
  <aside class="interviewer-task-list" aria-label="我的面试任务">
    <header class="interviewer-task-list__header">
      <div>
        <span class="interviewer-task-list__title">
          <h2>面试任务</h2>
          <small>共 {{ total }} 项</small>
        </span>
        <el-tooltip content="收起面试任务" placement="top">
          <el-button
            text
            circle
            :icon="PanelLeftClose"
            aria-label="收起面试任务"
            @click="emit('collapse')"
          />
        </el-tooltip>
      </div>
      <el-input
        :model-value="keyword"
        :prefix-icon="Search"
        placeholder="搜索候选人或职位"
        clearable
        aria-label="搜索面试任务"
        @update:model-value="emit('update:keyword', $event)"
        @keyup.enter="emit('search')"
      />
      <div class="interviewer-task-list__filters">
        <el-select
          :model-value="status"
          placeholder="面试状态"
          clearable
          @update:model-value="emit('update:status', $event as InterviewStatus | '')"
        >
          <el-option
            v-for="option in interviewStatusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        <el-select
          :model-value="feedbackState"
          placeholder="反馈状态"
          clearable
          @update:model-value="emit('update:feedbackState', $event as InterviewFeedbackState | '')"
        >
          <el-option
            v-for="option in interviewFeedbackStateOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </div>
      <div class="interviewer-task-list__filter-actions">
        <el-button type="primary" @click="emit('search')">查询</el-button>
        <el-button @click="emit('reset')">重置</el-button>
      </div>
    </header>

    <div v-if="loading" class="interviewer-task-list__loading">
      <el-skeleton v-for="index in 4" :key="index" :rows="2" animated />
    </div>
    <div v-else-if="tasks.length" class="interviewer-task-list__items" role="list">
      <button
        v-for="task in tasks"
        :key="task.id"
        type="button"
        class="interviewer-task-item"
        :class="{ 'interviewer-task-item--selected': task.id === selectedId }"
        :aria-pressed="task.id === selectedId"
        @click="emit('select', task.id)"
      >
        <span class="interviewer-task-item__heading">
          <strong>{{ task.candidateName }}</strong>
          <span
            :class="`rs-status-pill rs-status-pill--${getInterviewerStageTone(getInterviewerTaskStage(task))}`"
          >
            {{ getInterviewerStageText(getInterviewerTaskStage(task)) }}
          </span>
        </span>
        <span class="interviewer-task-item__job">{{ task.jobTitle }} · {{ task.roundText }}</span>
        <span class="interviewer-task-item__time">
          <CalendarClock :size="14" :stroke-width="1.75" aria-hidden="true" />
          {{ formatTaskTime(task.interviewTime) }}
        </span>
        <span class="interviewer-task-item__meta">
          <span>
            <component
              :is="task.method === 'ONLINE' ? Video : MapPin"
              :size="14"
              :stroke-width="1.75"
              aria-hidden="true"
            />
            {{ task.methodText }}
          </span>
          <small>反馈：{{ task.feedbackStateText }}</small>
        </span>
      </button>
    </div>
    <div v-else class="interviewer-task-list__empty">
      <CalendarClock :size="24" :stroke-width="1.75" aria-hidden="true" />
      <strong>没有符合条件的任务</strong>
      <span>调整筛选条件后重试。</span>
    </div>

    <el-pagination
      v-if="total > pageSize"
      class="interviewer-task-list__pagination"
      small
      layout="prev, pager, next"
      :current-page="page"
      :page-size="pageSize"
      :total="total"
      @update:current-page="emit('update:page', $event)"
    />
  </aside>
</template>

<style scoped lang="scss">
.interviewer-task-list {
  position: sticky;
  top: 0;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  min-width: 0;
  max-height: calc(100dvh - 112px);
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
.interviewer-task-list__header {
  display: grid;
  gap: var(--rs-space-2);
  padding: var(--rs-space-3);
  border-bottom: 1px solid var(--rs-border-default);
}
.interviewer-task-list__header > div:first-child,
.interviewer-task-list__filter-actions,
.interviewer-task-item__heading,
.interviewer-task-item__meta,
.interviewer-task-item__meta > span {
  display: flex;
  align-items: center;
}
.interviewer-task-list__header > div:first-child {
  justify-content: space-between;
}
.interviewer-task-list__title {
  display: flex;
  align-items: baseline;
  gap: var(--rs-space-2);
}
.interviewer-task-list__title small {
  color: var(--rs-text-secondary);
  font-size: 12px;
}
.interviewer-task-list__header h2 {
  margin: 0;
  font-size: 16px;
}
.interviewer-task-list__header span {
  color: var(--rs-text-secondary);
  font-size: 12px;
}
.interviewer-task-list__filters {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--rs-space-2);
}
.interviewer-task-list__filter-actions {
  justify-content: flex-end;
  gap: var(--rs-space-1);
}
.interviewer-task-list__items,
.interviewer-task-list__loading {
  min-height: 0;
  overflow-y: auto;
}
.interviewer-task-list__loading {
  display: grid;
  align-content: start;
  gap: var(--rs-space-3);
  padding: var(--rs-space-3);
}
.interviewer-task-item {
  position: relative;
  display: grid;
  width: 100%;
  gap: var(--rs-space-2);
  padding: var(--rs-space-3);
  border: 0;
  border-bottom: 1px solid var(--rs-border-default);
  background: transparent;
  color: var(--rs-text-primary);
  text-align: left;
  cursor: pointer;
}
.interviewer-task-item:hover {
  background: var(--rs-surface-subtle);
}
.interviewer-task-item--selected {
  background: var(--rs-surface-selected);
}
.interviewer-task-item--selected::before {
  position: absolute;
  inset: var(--rs-space-2) auto var(--rs-space-2) 0;
  width: 3px;
  border-radius: var(--rs-radius-pill);
  background: var(--rs-action-primary);
  content: '';
}
.interviewer-task-item__heading {
  justify-content: space-between;
  gap: var(--rs-space-2);
}
.interviewer-task-item__heading strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.interviewer-task-item__job,
.interviewer-task-item__time,
.interviewer-task-item__meta {
  color: var(--rs-text-secondary);
  font-size: 12px;
}
.interviewer-task-item__time {
  display: flex;
  align-items: center;
  gap: var(--rs-space-1);
  font-variant-numeric: tabular-nums;
}
.interviewer-task-item__meta {
  justify-content: space-between;
  gap: var(--rs-space-2);
}
.interviewer-task-item__meta > span {
  gap: var(--rs-space-1);
}
.interviewer-task-item__meta small {
  color: var(--rs-text-tertiary);
}
.interviewer-task-list__empty {
  display: grid;
  place-items: center;
  align-content: center;
  gap: var(--rs-space-2);
  min-height: 240px;
  padding: var(--rs-space-4);
  color: var(--rs-text-secondary);
  text-align: center;
}
.interviewer-task-list__empty > svg {
  color: var(--rs-blue-700);
}
.interviewer-task-list__empty span {
  font-size: 12px;
}
.interviewer-task-list__pagination {
  display: flex;
  justify-content: center;
  padding: var(--rs-space-2);
  border-top: 1px solid var(--rs-border-default);
}
</style>
