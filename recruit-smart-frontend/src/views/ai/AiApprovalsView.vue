<script setup lang="ts">
import { Bot, ShieldAlert } from 'lucide-vue-next'

// 该页面固定 AI 在高风险动作前必须生成的审批对象结构。
const approval = {
  proposedAction: '建议将候选人推进到终面',
  affectedRecord: '候选人：王同学 · 职位：高级 Java 工程师',
  editableContent: 'AI 基于简历技能、项目经历和一面反馈生成推进建议。',
  auditNote: 'HR 需要确认来源和岗位要求是否一致，确认后才可推进状态。',
}
</script>

<template>
  <section class="ai-approval">
    <div class="ai-approval__header">
      <Bot :size="20" :stroke-width="1.75" aria-hidden="true" />
      <div>
        <h2 class="rs-section-title">AI 审批工作区</h2>
        <p>AI 只能生成建议和审批对象，最终业务状态由用户确认。</p>
      </div>
    </div>

    <dl class="ai-approval__details">
      <div>
        <dt>Proposed Action</dt>
        <dd>{{ approval.proposedAction }}</dd>
      </div>
      <div>
        <dt>Affected Record</dt>
        <dd>{{ approval.affectedRecord }}</dd>
      </div>
      <div>
        <dt>Editable Content</dt>
        <dd>{{ approval.editableContent }}</dd>
      </div>
      <div>
        <dt>Audit Note</dt>
        <dd>{{ approval.auditNote }}</dd>
      </div>
    </dl>

    <div class="ai-approval__footer">
      <span class="rs-status-pill rs-status-pill--danger">
        <ShieldAlert :size="14" :stroke-width="1.75" aria-hidden="true" />
        未确认前不改变候选人阶段
      </span>
    </div>
  </section>
</template>

<style scoped lang="scss">
.ai-approval {
  display: grid;
  gap: var(--rs-space-4);
  max-width: 840px;
  padding: var(--rs-space-6);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.ai-approval__header {
  display: flex;
  align-items: flex-start;
  gap: var(--rs-space-3);
}

.ai-approval__header svg {
  color: var(--rs-blue-700);
}

.ai-approval__header p {
  margin: var(--rs-space-1) 0 0;
  color: var(--rs-text-secondary);
}

.ai-approval__details {
  display: grid;
  margin: 0;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
}

.ai-approval__details div {
  display: grid;
  grid-template-columns: 180px 1fr;
  min-height: 48px;
  border-bottom: 1px solid var(--rs-border-default);
}

.ai-approval__details div:last-child {
  border-bottom: 0;
}

.ai-approval__details dt,
.ai-approval__details dd {
  display: flex;
  align-items: center;
  margin: 0;
  padding: var(--rs-space-3);
}

.ai-approval__details dt {
  background: var(--rs-surface-subtle);
  color: var(--rs-text-secondary);
  font-size: 12px;
  font-weight: 600;
}

.ai-approval__details dd {
  color: var(--rs-text-primary);
}
</style>
