<template>
  <section class="placeholder-view">
    <div>
      <h2 class="rs-section-title">面试任务</h2>
      <p>后续接入候选人 Brief、结构化评分表和 AI 问题建议。</p>
    </div>
    <span class="rs-status-pill rs-status-pill--success">保留原始意见</span>
  </section>
</template>

<style scoped lang="scss">
.placeholder-view {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 160px;
  padding: var(--rs-space-6);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.placeholder-view p {
  margin: var(--rs-space-2) 0 0;
  color: var(--rs-text-secondary);
}
</style>
