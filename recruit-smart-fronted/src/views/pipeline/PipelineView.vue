<template>
  <section class="placeholder-view">
    <div>
      <h2 class="rs-section-title">招聘流程 Pipeline</h2>
      <p>Table 精确操作模式和 Board 协同模式会在这里按权限切换。</p>
    </div>
    <span class="rs-status-pill rs-status-pill--warning">需人工推进</span>
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
