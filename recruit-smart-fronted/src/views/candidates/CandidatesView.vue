<template>
  <section class="placeholder-view">
    <div>
      <h2 class="rs-section-title">候选人搜索与筛选</h2>
      <p>这里将承载结构化筛选、结果表格、行选择和右侧候选人预览。</p>
    </div>
    <span class="rs-status-pill rs-status-pill--info">框架占位</span>
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
