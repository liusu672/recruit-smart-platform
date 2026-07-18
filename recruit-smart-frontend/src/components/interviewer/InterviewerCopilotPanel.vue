<script setup lang="ts">
import {
  Bot,
  Check,
  Clipboard,
  PanelRightClose,
  Send,
  ShieldCheck,
  TriangleAlert,
} from 'lucide-vue-next'
import { ref, watch } from 'vue'

import type { FeedbackSummaryResponse } from '@/types/ai'
import type { InterviewQuestion } from '@/types/interview'

const props = defineProps<{
  questions: InterviewQuestion[]
  riskPoints: string[]
  aiSummary: string | null
  feedbackSummary: FeedbackSummaryResponse | null
  generating: boolean
  summarizing: boolean
  canSummarize: boolean
  error: string
  interviewId: number
}>()

const emit = defineEmits<{
  close: []
  generate: [focus: string]
  summarize: []
}>()

const focus = ref('')
const askedQuestionIds = ref(new Set<string>())

watch(
  () => props.interviewId,
  () => (askedQuestionIds.value = new Set()),
)

const sourceText: Record<InterviewQuestion['source'], string> = {
  JOB: '岗位要求',
  RESUME: '候选人简历',
  RISK: '风险核实',
  MANUAL: '临场追问',
}

function submitFocus() {
  const value = focus.value.trim()
  if (!value) return
  emit('generate', value)
  focus.value = ''
}

async function copyQuestion(question: InterviewQuestion) {
  await navigator.clipboard.writeText(question.question)
}

function toggleAsked(question: InterviewQuestion) {
  const next = new Set(askedQuestionIds.value)
  if (next.has(question.id)) next.delete(question.id)
  else next.add(question.id)
  askedQuestionIds.value = next
}
</script>

<template>
  <aside class="interviewer-copilot" aria-label="面试 Copilot">
    <header class="interviewer-copilot__header">
      <div>
        <Bot :size="18" :stroke-width="1.75" />
        <h2>面试 Copilot</h2>
        <span>仅供参考</span>
      </div>
      <el-tooltip content="收起辅助面板" placement="top">
        <button type="button" aria-label="收起辅助面板" @click="emit('close')">
          <PanelRightClose :size="18" :stroke-width="1.75" />
        </button>
      </el-tooltip>
    </header>

    <div class="interviewer-copilot__boundary">
      <ShieldCheck :size="16" :stroke-width="1.75" />
      <p>建议基于岗位和候选人上下文，不会写入评分或替代面试官意见。</p>
    </div>

    <p v-if="error" class="interviewer-copilot__error" role="alert">
      <TriangleAlert :size="16" :stroke-width="1.75" />{{ error }}
    </p>

    <section class="interviewer-copilot__section">
      <h3>推荐问题</h3>
      <div v-if="questions.length" class="interviewer-copilot__questions">
        <article v-for="question in questions" :key="question.id">
          <div>
            <strong>{{ question.category }}</strong
            ><span>{{ sourceText[question.source] }}</span>
          </div>
          <p>{{ question.question }}</p>
          <footer>
            <el-button text :icon="Clipboard" @click="copyQuestion(question)">复制</el-button>
            <el-button
              text
              :type="askedQuestionIds.has(question.id) ? 'success' : undefined"
              :icon="askedQuestionIds.has(question.id) ? Check : undefined"
              @click="toggleAsked(question)"
            >
              {{ askedQuestionIds.has(question.id) ? '本次已提问' : '标记已提问' }}
            </el-button>
          </footer>
        </article>
      </div>
      <p v-else class="interviewer-copilot__empty">暂无推荐问题，可在下方补充追问主题。</p>
      <small class="interviewer-copilot__local-note"
        >“已提问”仅用于本次页面记录，刷新后不会保留。</small
      >
    </section>

    <section
      v-if="riskPoints.length"
      class="interviewer-copilot__section interviewer-copilot__risks"
    >
      <h3>待核实风险</h3>
      <ul>
        <li v-for="risk in riskPoints" :key="risk">
          <TriangleAlert :size="14" :stroke-width="1.75" />{{ risk }}
        </li>
      </ul>
    </section>

    <section
      v-if="aiSummary || feedbackSummary"
      class="interviewer-copilot__section interviewer-copilot__summary"
    >
      <h3>反馈摘要</h3>
      <p v-if="feedbackSummary">{{ feedbackSummary.summary }}</p>
      <p v-else>{{ aiSummary }}</p>
      <template v-if="feedbackSummary">
        <strong>优势</strong>
        <ul>
          <li v-for="item in feedbackSummary.advantages" :key="item">{{ item }}</li>
        </ul>
        <strong>待核实</strong>
        <ul>
          <li v-for="item in feedbackSummary.risks" :key="item">{{ item }}</li>
        </ul>
        <small>{{ feedbackSummary.suggestion }}。该结果未覆盖原始评价。</small>
      </template>
    </section>

    <section class="interviewer-copilot__section interviewer-copilot__actions">
      <el-button :loading="summarizing" :disabled="!canSummarize" @click="emit('summarize')"
        >生成反馈摘要</el-button
      >
      <form @submit.prevent="submitFocus">
        <label for="interviewer-question-focus">补充追问主题</label>
        <div>
          <el-input
            id="interviewer-question-focus"
            v-model="focus"
            placeholder="例如：复杂状态流转"
            :disabled="generating"
          /><el-tooltip content="生成参考问题" placement="top"
            ><el-button
              native-type="submit"
              circle
              :icon="Send"
              :loading="generating"
              :disabled="!focus.trim()"
              aria-label="生成参考问题"
          /></el-tooltip>
        </div>
      </form>
    </section>
  </aside>
</template>

<style scoped lang="scss">
.interviewer-copilot {
  align-self: start;
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
.interviewer-copilot__header,
.interviewer-copilot__header > div,
.interviewer-copilot__boundary,
.interviewer-copilot__questions article > div,
.interviewer-copilot__questions footer,
.interviewer-copilot__risks li,
.interviewer-copilot__error,
.interviewer-copilot__actions form > div {
  display: flex;
  align-items: center;
}
.interviewer-copilot__header {
  justify-content: space-between;
  min-height: 52px;
  padding: 0 var(--rs-space-3);
  border-bottom: 1px solid var(--rs-border-default);
}
.interviewer-copilot__header > div {
  gap: var(--rs-space-2);
}
.interviewer-copilot h2,
.interviewer-copilot h3,
.interviewer-copilot p {
  margin: 0;
}
.interviewer-copilot h2 {
  font-size: 16px;
}
.interviewer-copilot__header span {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}
.interviewer-copilot__header button {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border: 0;
  border-radius: var(--rs-radius-sm);
  background: transparent;
  color: var(--rs-text-secondary);
  cursor: pointer;
}
.interviewer-copilot__header button:hover {
  background: var(--rs-surface-muted);
}
.interviewer-copilot__boundary {
  align-items: flex-start;
  gap: var(--rs-space-2);
  padding: var(--rs-space-3);
  background: var(--rs-surface-subtle);
  color: var(--rs-text-secondary);
  font-size: 12px;
}
.interviewer-copilot__boundary svg {
  flex: 0 0 auto;
  margin-top: var(--rs-space-1);
  color: var(--rs-blue-700);
}
.interviewer-copilot__error {
  align-items: flex-start;
  gap: var(--rs-space-2);
  padding: var(--rs-space-3);
  background: var(--rs-warning-050);
  color: var(--rs-warning-800);
  font-size: 12px;
}
.interviewer-copilot__section {
  display: grid;
  gap: var(--rs-space-3);
  padding: var(--rs-space-3);
  border-top: 1px solid var(--rs-border-default);
}
.interviewer-copilot__section h3 {
  font-size: 13px;
}
.interviewer-copilot__questions {
  display: grid;
  gap: var(--rs-space-2);
}
.interviewer-copilot__questions article {
  padding: var(--rs-space-3);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
}
.interviewer-copilot__questions article > div {
  justify-content: space-between;
  gap: var(--rs-space-2);
}
.interviewer-copilot__questions article > div span,
.interviewer-copilot__local-note,
.interviewer-copilot__summary small {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}
.interviewer-copilot__questions article p {
  margin-top: var(--rs-space-2);
  color: var(--rs-text-secondary);
}
.interviewer-copilot__questions footer {
  justify-content: flex-end;
  margin-top: var(--rs-space-2);
}
.interviewer-copilot__questions footer :deep(.el-button + .el-button) {
  margin-left: var(--rs-space-1);
}
.interviewer-copilot__empty {
  color: var(--rs-text-secondary);
}
.interviewer-copilot__risks ul,
.interviewer-copilot__summary ul {
  display: grid;
  gap: var(--rs-space-1);
  padding-left: var(--rs-space-4);
  margin: 0;
  color: var(--rs-text-secondary);
}
.interviewer-copilot__risks li {
  align-items: flex-start;
  gap: var(--rs-space-1);
  color: var(--rs-warning-800);
  font-size: 12px;
}
.interviewer-copilot__summary p {
  color: var(--rs-text-secondary);
}
.interviewer-copilot__actions form {
  display: grid;
  gap: var(--rs-space-2);
}
.interviewer-copilot__actions label {
  font-size: 12px;
  font-weight: 600;
}
.interviewer-copilot__actions form > div {
  gap: var(--rs-space-2);
}
.interviewer-copilot__actions :deep(.el-input) {
  flex: 1;
}
</style>
