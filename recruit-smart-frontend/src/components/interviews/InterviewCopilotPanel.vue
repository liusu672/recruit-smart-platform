<script setup lang="ts">
import { Bot, FileText, ShieldCheck } from 'lucide-vue-next'
import { ref, watch } from 'vue'

import type { FeedbackSummaryResponse } from '@/types/ai'
import type { InterviewQuestion } from '@/types/interview'

const props = defineProps<{
  questions: InterviewQuestion[]
  aiSummary: string | null
  generating: boolean
  feedbackSummary: FeedbackSummaryResponse | null
  summarizing: boolean
  canSummarize: boolean
}>()

const emit = defineEmits<{
  generate: []
  summarize: []
}>()

const summaryDialogVisible = ref(false)

watch(
  () => props.feedbackSummary,
  (summary) => {
    if (summary) summaryDialogVisible.value = true
  },
)

const sourceText: Record<InterviewQuestion['source'], string> = {
  JOB: '岗位要求',
  RESUME: '候选人简历',
  RISK: '风险核实',
  MANUAL: '临场追问',
}
</script>

<template>
  <aside class="copilot" aria-label="面试 AI 参考">
    <header class="copilot__header">
      <div>
        <Bot :size="18" :stroke-width="1.75" aria-hidden="true" />
        <h3>面试 Copilot</h3>
      </div>
      <span class="rs-status-pill rs-status-pill--info">仅供参考</span>
    </header>

    <div class="copilot__boundary">
      <ShieldCheck :size="16" :stroke-width="1.75" aria-hidden="true" />
      <p>问题建议基于岗位和候选人上下文，不会写入评分或替代面试官意见。</p>
    </div>

    <section class="copilot__questions">
      <article v-for="question in questions" :key="question.id">
        <div>
          <strong>{{ question.category }}</strong>
          <span>{{ sourceText[question.source] }}</span>
        </div>
        <p>{{ question.question }}</p>
      </article>
    </section>

    <section v-if="aiSummary || feedbackSummary" class="copilot__summary-card">
      <div>
        <h4>反馈摘要</h4>
        <p>
          {{
            feedbackSummary
              ? '本次反馈摘要已生成，可在弹窗中查看完整内容。'
              : '已保存 AI 反馈摘要。'
          }}
        </p>
      </div>
      <el-button
        size="small"
        type="primary"
        plain
        :icon="FileText"
        @click="summaryDialogVisible = true"
      >
        查看摘要
      </el-button>
    </section>

    <div class="copilot__actions">
      <el-button
        type="primary"
        :loading="summarizing"
        :disabled="!canSummarize"
        @click="emit('summarize')"
      >
        生成反馈摘要
      </el-button>
      <el-button :loading="generating" @click="emit('generate')">生成面试问题</el-button>
    </div>

    <el-dialog
      v-model="summaryDialogVisible"
      title="反馈摘要"
      width="720px"
      append-to-body
      destroy-on-close
    >
      <div class="copilot-summary-dialog__content">
        <section v-if="aiSummary" class="copilot-summary-dialog__section">
          <h4>AI 反馈摘要</h4>
          <p>{{ aiSummary }}</p>
          <small>摘要与上方已提交的面试官原始评价分开保存。</small>
        </section>

        <section v-if="feedbackSummary" class="copilot-summary-dialog__section">
          <h4>本次生成的反馈摘要</h4>
          <p>{{ feedbackSummary.summary }}</p>
          <div class="copilot-summary-dialog__grid">
            <div>
              <strong>优势</strong>
              <ul>
                <li v-for="item in feedbackSummary.advantages" :key="item">{{ item }}</li>
              </ul>
            </div>
            <div>
              <strong>待核实</strong>
              <ul>
                <li v-for="item in feedbackSummary.risks" :key="item">{{ item }}</li>
              </ul>
            </div>
          </div>
          <small>{{ feedbackSummary.suggestion }}。该结果未覆盖面试官原始评价。</small>
        </section>
      </div>
    </el-dialog>
  </aside>
</template>

<style scoped lang="scss">
.copilot {
  align-self: start;
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.copilot__header,
.copilot__header > div,
.copilot__boundary,
.copilot__questions article > div {
  display: flex;
  align-items: center;
}

.copilot__header {
  justify-content: space-between;
  min-height: 48px;
  padding: 0 var(--rs-space-4);
  border-bottom: 1px solid var(--rs-border-default);
}

.copilot__header > div,
.copilot__boundary {
  gap: var(--rs-space-2);
}

.copilot h3,
.copilot h4,
.copilot p {
  margin: 0;
}

.copilot h3 {
  font-size: 16px;
}

.copilot__boundary {
  align-items: flex-start;
  padding: var(--rs-space-3) var(--rs-space-4);
  background: var(--rs-surface-subtle);
  color: var(--rs-text-secondary);
  font-size: 12px;
}

.copilot__boundary svg {
  flex: 0 0 auto;
  margin-top: var(--rs-space-1);
  color: var(--rs-blue-700);
}

.copilot__questions {
  display: grid;
  gap: var(--rs-space-2);
  padding: var(--rs-space-4);
}

.copilot__questions article {
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
}

.copilot__questions article > div {
  justify-content: space-between;
  margin-bottom: var(--rs-space-2);
}

.copilot__questions strong,
.copilot__summary-card h4,
.copilot-summary-dialog__content h4 {
  font-size: 12px;
  font-weight: 600;
}

.copilot__questions span,
.copilot__summary-card p,
.copilot-summary-dialog__content small {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}

.copilot__questions p {
  color: var(--rs-text-secondary);
}

.copilot__summary-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--rs-space-3);
  padding: var(--rs-space-4);
  border-top: 1px solid var(--rs-border-default);
  background: var(--rs-surface-selected);
}

.copilot__summary-card p,
.copilot-summary-dialog__content p {
  margin: var(--rs-space-2) 0 0;
  color: var(--rs-text-secondary);
}

.copilot__actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--rs-space-2);
  padding: var(--rs-space-4);
  border-top: 1px solid var(--rs-border-default);
}

.copilot-summary-dialog__content {
  display: grid;
  max-height: 62dvh;
  gap: var(--rs-space-4);
  overflow-y: auto;
}

.copilot-summary-dialog__section {
  padding: var(--rs-space-4);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-selected);
}

.copilot-summary-dialog__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--rs-space-3);
  margin: var(--rs-space-3) 0;
}

.copilot-summary-dialog__grid ul {
  padding-left: var(--rs-space-4);
  margin: var(--rs-space-1) 0 0;
  color: var(--rs-text-secondary);
}
</style>
