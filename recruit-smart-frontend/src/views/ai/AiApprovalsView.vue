<script setup lang="ts">
import { Bot, ShieldAlert } from 'lucide-vue-next'

// 后端暂无审批队列和确认接口，因此这里只说明已接入的页面内 AI 辅助能力。
const capabilities = [
  '投递详情：展示已持久化的简历匹配分、亮点和待核实事项。',
  '面试工作区：生成面试题，并在提交反馈后生成摘要、优势、风险与建议。',
  '员工详情：按需分析离职风险，仅作为参考，不会自动修改员工状态。',
]
</script>

<template>
  <section class="ai-approval">
    <div class="ai-approval__header">
      <Bot :size="20" :stroke-width="1.75" aria-hidden="true" />
      <div>
        <h2 class="rs-section-title">AI 能力说明</h2>
        <p>当前 AI 仅嵌入招聘业务页面提供参考，最终业务状态仍由人工和业务接口确认。</p>
      </div>
    </div>

    <ul class="ai-approval__capabilities">
      <li v-for="capability in capabilities" :key="capability">{{ capability }}</li>
    </ul>

    <div class="ai-approval__footer">
      <span class="rs-status-pill rs-status-pill--danger">
        <ShieldAlert :size="14" :stroke-width="1.75" aria-hidden="true" />
        无审批队列，不宣称为生产审批功能
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

.ai-approval__capabilities {
  display: grid;
  gap: var(--rs-space-3);
  margin: 0;
  padding: var(--rs-space-4) var(--rs-space-4) var(--rs-space-4) var(--rs-space-6);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
  color: var(--rs-text-secondary);
}
</style>
