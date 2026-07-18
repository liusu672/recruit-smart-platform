<script setup lang="ts">
import { Info } from 'lucide-vue-next'

import CandidateStatusBadge from '@/components/candidate/CandidateStatusBadge.vue'
import type { CandidateApplicationDetail } from '@/types/portal'

defineProps<{
  visible: boolean
  application: CandidateApplicationDetail | null
  loading: boolean
  error: string
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  contact: [applicationId: number]
}>()

function formatDate(value: string | null) {
  return value ? new Date(value).toLocaleString('zh-CN') : '暂无记录'
}
</script>

<template>
  <el-drawer
    :model-value="visible"
    title="投递详情"
    size="440px"
    @update:model-value="emit('update:visible', $event)"
  >
    <el-skeleton v-if="loading" :rows="7" animated />
    <el-result v-else-if="error" icon="error" title="投递详情加载失败" :sub-title="error" />
    <div v-else-if="application" class="application-detail">
      <header>
        <div>
          <h2>{{ application.jobTitle }}</h2>
          <p>{{ application.department || '部门待定' }}</p>
        </div>
        <CandidateStatusBadge :status="application.status" :label="application.statusText" />
      </header>
      <dl>
        <div>
          <dt>使用简历</dt>
          <dd>{{ application.resumeName || '简历' }}</dd>
        </div>
        <div>
          <dt>投递来源</dt>
          <dd>{{ application.source || '候选人投递' }}</dd>
        </div>
        <div>
          <dt>投递时间</dt>
          <dd>{{ formatDate(application.appliedAt) }}</dd>
        </div>
        <div>
          <dt>审核时间</dt>
          <dd>{{ formatDate(application.reviewedAt) }}</dd>
        </div>
      </dl>
      <section v-if="application.hrNote || application.rejectReason">
        <h3>处理说明</h3>
        <p>{{ application.rejectReason || application.hrNote }}</p>
      </section>
      <div class="application-detail__note">
        <Info :size="17" :stroke-width="1.75" />
        <span>投递状态由招聘流程更新，如需确认进展可以联系招聘方。</span>
      </div>
    </div>
    <template v-if="application && !loading && !error" #footer>
      <el-button type="primary" @click="emit('contact', application.id)">联系招聘方</el-button>
    </template>
  </el-drawer>
</template>

<style scoped lang="scss">
.application-detail {
  display: grid;
  gap: var(--rs-space-6);
}
.application-detail header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--rs-space-4);
}
.application-detail h2,
.application-detail h3,
.application-detail p,
.application-detail dl,
.application-detail dd {
  margin: 0;
}
.application-detail header p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}
.application-detail dl {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--rs-space-4);
  padding: var(--rs-space-4);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
}
.application-detail dt {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}
.application-detail dd {
  margin-top: var(--rs-space-1);
  font-weight: 600;
}
.application-detail section {
  padding-top: var(--rs-space-4);
  border-top: 1px solid var(--rs-border-default);
}
.application-detail section h3 {
  font-size: 14px;
}
.application-detail section p {
  margin-top: var(--rs-space-2);
  color: var(--rs-text-secondary);
}
.application-detail__note {
  display: flex;
  align-items: flex-start;
  gap: var(--rs-space-2);
  padding: var(--rs-space-3) var(--rs-space-4);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-blue-050);
  color: var(--rs-blue-700);
}
</style>
