<script setup lang="ts">
import { computed, ref } from 'vue'

import { getMissingFeedbackCount, interviewScoreOptions } from '@/config/interviewer'
import { interviewSuggestionOptions } from '@/config/interviews'
import type { InterviewScoreItem, InterviewSuggestion } from '@/types/interview'

const props = defineProps<{
  scorecard: InterviewScoreItem[]
  comment: string
  suggestion: InterviewSuggestion | null
  overallScore: number | null
  readonly: boolean
}>()

const emit = defineEmits<{
  'update:scorecard': [scorecard: InterviewScoreItem[]]
  'update:comment': [comment: string]
  'update:suggestion': [suggestion: InterviewSuggestion]
}>()

const itemElements = ref<HTMLElement[]>([])
const missingCount = computed(() =>
  getMissingFeedbackCount(props.scorecard, props.comment, props.suggestion),
)

function updateItem(index: number, patch: Partial<InterviewScoreItem>) {
  const next = props.scorecard.map((item, itemIndex) =>
    itemIndex === index ? { ...item, ...patch } : item,
  )
  emit('update:scorecard', next)
}

function handleRatingKeydown(event: KeyboardEvent, index: number) {
  if (
    props.readonly ||
    event.target instanceof HTMLInputElement ||
    event.target instanceof HTMLTextAreaElement
  )
    return
  const score = Number(event.key)
  if (score < 1 || score > 4) return
  event.preventDefault()
  updateItem(index, { score })
}

function setItemRef(element: unknown, index: number) {
  if (element instanceof HTMLElement) itemElements.value[index] = element
}

function focusFirstMissing() {
  const index = props.scorecard.findIndex((item) => item.score === null || !item.evidence.trim())
  if (index >= 0) {
    itemElements.value[index]?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    itemElements.value[index]?.focus()
    return
  }
  const selector = !props.comment.trim()
    ? '#interviewer-overall-comment textarea'
    : '#interviewer-suggestion'
  document.querySelector<HTMLElement>(selector)?.focus()
}

defineExpose({ focusFirstMissing })
</script>

<template>
  <section class="interviewer-scorecard" aria-label="结构化面试评分">
    <header class="interviewer-scorecard__header">
      <div>
        <h2>结构化面试评分</h2>
        <p>用候选人的具体回答、项目或行为作为评价依据。</p>
      </div>
      <span :class="`rs-status-pill rs-status-pill--${missingCount ? 'warning' : 'success'}`">
        {{ missingCount ? `还有 ${missingCount} 项未完成` : '反馈内容已完整' }}
      </span>
    </header>

    <div class="interviewer-scorecard__items">
      <article
        v-for="(item, index) in scorecard"
        :key="item.key"
        :ref="(element) => setItemRef(element, index)"
        class="interviewer-score-item"
        :class="{ 'interviewer-score-item--missing': item.score === null || !item.evidence.trim() }"
        tabindex="0"
        @keydown="handleRatingKeydown($event, index)"
      >
        <div class="interviewer-score-item__heading">
          <div>
            <h3>{{ item.label }}</h3>
            <p>{{ item.description }}</p>
          </div>
          <span v-if="item.score !== null" class="interviewer-score-item__selected">{{
            interviewScoreOptions[item.score - 1]?.label
          }}</span>
          <span v-else class="interviewer-score-item__missing-label">待评分</span>
        </div>
        <div class="interviewer-score-item__ratings" :aria-label="`${item.label}评分`">
          <button
            v-for="option in interviewScoreOptions"
            :key="option.value"
            type="button"
            :class="{ 'interviewer-score-item__rating--active': item.score === option.value }"
            :disabled="readonly"
            :aria-pressed="item.score === option.value"
            :title="option.description"
            @click="updateItem(index, { score: option.value })"
          >
            <strong>{{ option.value }}</strong
            ><span>{{ option.label }}</span>
          </button>
        </div>
        <p v-if="item.score !== null" class="interviewer-score-item__criterion">
          {{ interviewScoreOptions[item.score - 1]?.description }}
        </p>
        <label :for="`evidence-${item.key}`">评价依据</label>
        <el-input
          :id="`evidence-${item.key}`"
          :model-value="item.evidence"
          type="textarea"
          :rows="3"
          maxlength="1000"
          :disabled="readonly"
          placeholder="记录候选人的具体回答、项目或行为证据"
          :aria-label="`${item.label}评价依据`"
          @update:model-value="updateItem(index, { evidence: $event })"
        />
        <small
          v-if="item.score !== null && !item.evidence.trim()"
          class="interviewer-score-item__error"
          >缺少评价依据，请补充可复核的事实。</small
        >
      </article>
    </div>

    <section class="interviewer-scorecard__summary">
      <div class="interviewer-scorecard__summary-heading">
        <div>
          <h3>综合评价与建议</h3>
          <p>该建议不会直接推进录用状态，后续仍由 HR 人工确认。</p>
        </div>
        <strong>{{ overallScore === null ? '--' : overallScore }}<small>/100</small></strong>
      </div>
      <div id="interviewer-overall-comment">
        <label for="interviewer-comment">综合评价</label>
        <el-input
          id="interviewer-comment"
          :model-value="comment"
          type="textarea"
          :rows="5"
          maxlength="2000"
          show-word-limit
          :disabled="readonly"
          placeholder="总结关键表现、证据、风险和建议跟进项"
          @update:model-value="emit('update:comment', $event)"
        />
        <small v-if="!comment.trim()" class="interviewer-score-item__error">请填写综合评价。</small>
      </div>
      <div id="interviewer-suggestion" tabindex="-1">
        <label>录用建议</label>
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
            >{{ option.label }}</el-radio-button
          >
        </el-radio-group>
        <small v-if="!suggestion" class="interviewer-score-item__error">请选择录用建议。</small>
      </div>
    </section>
  </section>
</template>

<style scoped lang="scss">
.interviewer-scorecard {
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
.interviewer-scorecard__header,
.interviewer-score-item__heading,
.interviewer-scorecard__summary-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--rs-space-4);
}
.interviewer-scorecard__header {
  padding: var(--rs-space-4);
  border-bottom: 1px solid var(--rs-border-default);
}
.interviewer-scorecard h2,
.interviewer-scorecard h3,
.interviewer-scorecard p {
  margin: 0;
}
.interviewer-scorecard h2 {
  font-size: 16px;
}
.interviewer-scorecard__header p,
.interviewer-score-item__heading p,
.interviewer-scorecard__summary-heading p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
  font-size: 12px;
}
.interviewer-scorecard__items {
  display: grid;
}
.interviewer-score-item {
  display: grid;
  gap: var(--rs-space-3);
  padding: var(--rs-space-4);
  outline-offset: -2px;
}
.interviewer-score-item + .interviewer-score-item {
  border-top: 1px solid var(--rs-border-default);
}
.interviewer-score-item--missing {
  background: color-mix(in srgb, var(--rs-warning-050) 32%, var(--rs-white));
}
.interviewer-score-item h3 {
  font-size: 15px;
}
.interviewer-score-item__selected,
.interviewer-score-item__missing-label {
  flex: 0 0 auto;
  color: var(--rs-success-700);
  font-size: 12px;
  font-weight: 600;
}
.interviewer-score-item__missing-label {
  color: var(--rs-warning-800);
}
.interviewer-score-item__ratings {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--rs-space-2);
}
.interviewer-score-item__ratings button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--rs-space-2);
  min-height: 40px;
  padding: 0 var(--rs-space-2);
  border: 1px solid var(--rs-border-strong);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
  color: var(--rs-text-secondary);
  cursor: pointer;
}
.interviewer-score-item__ratings button:hover:not(:disabled) {
  border-color: var(--rs-blue-500);
  color: var(--rs-blue-700);
}
.interviewer-score-item__ratings button:disabled {
  cursor: default;
}
.interviewer-score-item__rating--active {
  border-color: var(--rs-action-primary) !important;
  background: var(--rs-action-primary) !important;
  color: var(--rs-white) !important;
}
.interviewer-score-item__ratings strong {
  font-size: 16px;
}
.interviewer-score-item__ratings span {
  font-size: 12px;
}
.interviewer-score-item__criterion {
  padding: var(--rs-space-2) var(--rs-space-3);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-blue-050);
  color: var(--rs-blue-700);
  font-size: 12px;
}
.interviewer-score-item label,
.interviewer-scorecard__summary label {
  font-size: 13px;
  font-weight: 600;
}
.interviewer-score-item__error {
  color: var(--rs-danger-700);
  font-size: 12px;
}
.interviewer-scorecard__summary {
  display: grid;
  gap: var(--rs-space-4);
  padding: var(--rs-space-4);
  border-top: 1px solid var(--rs-border-default);
  background: var(--rs-surface-subtle);
}
.interviewer-scorecard__summary > div:not(.interviewer-scorecard__summary-heading) {
  display: grid;
  gap: var(--rs-space-2);
}
.interviewer-scorecard__summary-heading h3 {
  font-size: 15px;
}
.interviewer-scorecard__summary-heading > strong {
  color: var(--rs-blue-700);
  font-size: 24px;
  font-variant-numeric: tabular-nums;
}
.interviewer-scorecard__summary-heading small {
  font-size: 12px;
  font-weight: 500;
}
</style>
