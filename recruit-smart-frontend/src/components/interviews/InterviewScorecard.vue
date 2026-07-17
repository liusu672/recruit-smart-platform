<script setup lang="ts">
import { computed } from 'vue'

import { interviewSuggestionOptions } from '@/config/interviews'
import type { InterviewScoreItem, InterviewSuggestion } from '@/types/interview'

const props = defineProps<{
  scorecard: InterviewScoreItem[]
  comment: string
  suggestion: InterviewSuggestion | null
  overallScore: number | null
  readonly: boolean
  submitDisabled: boolean
  saving: boolean
  submitting: boolean
}>()

const emit = defineEmits<{
  'update:scorecard': [scorecard: InterviewScoreItem[]]
  'update:comment': [comment: string]
  'update:suggestion': [suggestion: InterviewSuggestion]
  save: []
  submit: []
}>()

const completion = computed(() => {
  const completed = props.scorecard.filter(
    (item) => item.score !== null && item.evidence.trim(),
  ).length
  return `${completed}/${props.scorecard.length}`
})

function updateItem(index: number, patch: Partial<InterviewScoreItem>) {
  const next = props.scorecard.map((item, itemIndex) =>
    itemIndex === index ? { ...item, ...patch } : item,
  )
  emit('update:scorecard', next)
}
</script>

<template>
  <section class="scorecard" aria-label="结构化面试评分卡">
    <header class="scorecard__header">
      <div>
        <h3>结构化面试评分卡</h3>
        <p>每项评分都需要记录可复核的事实或候选人原话。</p>
      </div>
      <span class="rs-status-pill rs-status-pill--info">完成 {{ completion }}</span>
    </header>

    <div class="scorecard__criteria">
      <article v-for="(item, index) in scorecard" :key="item.key" class="scorecard__item">
        <div class="scorecard__copy">
          <h4>{{ item.label }}</h4>
          <p>{{ item.description }}</p>
        </div>
        <el-input
          :model-value="item.evidence"
          type="textarea"
          :rows="2"
          :disabled="readonly"
          :placeholder="`记录${item.label}的判断证据`"
          :aria-label="`${item.label}评价证据`"
          @update:model-value="updateItem(index, { evidence: $event })"
        />
        <div class="scorecard__rating" :aria-label="`${item.label}评分`">
          <button
            v-for="score in 4"
            :key="score"
            type="button"
            :class="{ 'scorecard__rating-button--active': item.score === score }"
            :disabled="readonly"
            :aria-pressed="item.score === score"
            @click="updateItem(index, { score })"
          >
            {{ score }}
          </button>
        </div>
      </article>
    </div>

    <section class="scorecard__summary">
      <div class="scorecard__summary-title">
        <div>
          <h4>综合评价与建议</h4>
          <p>该建议由面试官提交，后续业务状态仍由 HR 确认。</p>
        </div>
        <strong>{{ overallScore === null ? '--' : overallScore }}<small>/100</small></strong>
      </div>
      <el-input
        :model-value="comment"
        type="textarea"
        :rows="4"
        :disabled="readonly"
        placeholder="总结候选人的关键表现、证据、风险和建议跟进项"
        aria-label="面试综合评价"
        @update:model-value="emit('update:comment', $event)"
      />
      <el-radio-group
        :model-value="suggestion"
        :disabled="readonly"
        aria-label="录用建议"
        @update:model-value="emit('update:suggestion', $event as InterviewSuggestion)"
      >
        <el-radio-button
          v-for="option in interviewSuggestionOptions"
          :key="option.value"
          :value="option.value"
        >
          {{ option.label }}
        </el-radio-button>
      </el-radio-group>
    </section>

    <footer class="scorecard__footer">
      <p v-if="submitDisabled">完成面试后才能提交反馈。</p>
      <p v-else-if="readonly">反馈已提交，原始评价进入只读状态。</p>
      <p v-else>提交后不可在前端覆盖；需要修改时由招聘负责人发起审计流程。</p>
      <div v-if="!readonly">
        <el-button :loading="saving" @click="emit('save')">保存草稿</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          :disabled="submitDisabled"
          @click="emit('submit')"
        >
          提交反馈
        </el-button>
      </div>
    </footer>
  </section>
</template>

<style scoped lang="scss">
.scorecard {
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.scorecard__header,
.scorecard__item,
.scorecard__summary-title,
.scorecard__footer {
  display: flex;
  align-items: center;
}

.scorecard__header,
.scorecard__summary,
.scorecard__footer {
  padding: var(--rs-space-4);
}

.scorecard__header {
  justify-content: space-between;
  border-bottom: 1px solid var(--rs-border-default);
}

.scorecard h3,
.scorecard h4,
.scorecard p {
  margin: 0;
}

.scorecard h3 {
  font-size: 16px;
}

.scorecard__header p,
.scorecard__copy p,
.scorecard__summary-title p,
.scorecard__footer p {
  color: var(--rs-text-secondary);
  font-size: 12px;
}

.scorecard__criteria {
  display: grid;
}

.scorecard__item {
  display: grid;
  grid-template-columns: minmax(128px, 0.7fr) minmax(200px, 1.4fr) auto;
  gap: var(--rs-space-3);
  min-height: 112px;
  padding: var(--rs-space-4);
}

.scorecard__item + .scorecard__item {
  border-top: 1px solid var(--rs-border-default);
}

.scorecard__copy {
  align-self: start;
}

.scorecard__copy h4 {
  margin-bottom: var(--rs-space-1);
  font-size: 14px;
}

.scorecard__rating {
  display: flex;
  align-self: center;
  gap: var(--rs-space-1);
}

.scorecard__rating button {
  width: 32px;
  height: 32px;
  border: 1px solid var(--rs-border-strong);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
  color: var(--rs-text-secondary);
  cursor: pointer;
}

.scorecard__rating button:hover:not(:disabled),
.scorecard__rating-button--active {
  border-color: var(--rs-action-primary) !important;
  background: var(--rs-action-primary) !important;
  color: var(--rs-white) !important;
}

.scorecard__rating button:disabled {
  cursor: default;
}

.scorecard__summary {
  display: grid;
  gap: var(--rs-space-3);
  border-top: 1px solid var(--rs-border-default);
  background: var(--rs-surface-subtle);
}

.scorecard__summary-title {
  justify-content: space-between;
}

.scorecard__summary-title strong {
  color: var(--rs-blue-700);
  font-size: 24px;
  font-variant-numeric: tabular-nums;
}

.scorecard__summary-title small {
  font-size: 12px;
  font-weight: 500;
}

.scorecard__footer {
  justify-content: space-between;
  gap: var(--rs-space-4);
  border-top: 1px solid var(--rs-border-default);
}

.scorecard__footer > div {
  display: flex;
  flex: 0 0 auto;
  gap: var(--rs-space-2);
}

@media (max-width: 1439px) {
  .scorecard__item {
    grid-template-columns: minmax(120px, 0.7fr) minmax(180px, 1fr);
  }

  .scorecard__rating {
    grid-column: 1 / -1;
    justify-self: end;
  }
}
</style>
