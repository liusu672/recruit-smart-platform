<script setup lang="ts">
import { CalendarDays, Mail, Phone, UserRoundCheck, XCircle } from 'lucide-vue-next'
import {
  canCancelOnboarding,
  canCompleteOnboarding,
  getOnboardingStatusTone,
} from '@/config/onboardings'
import type { OnboardingRecord } from '@/types/onboarding'

defineProps<{
  visible: boolean
  record: OnboardingRecord | undefined
  loading: boolean
  error: Error | null
}>()
const emit = defineEmits<{
  'update:visible': [value: boolean]
  complete: [record: OnboardingRecord]
  cancel: [record: OnboardingRecord]
}>()
function formatDate(value: string | null, withTime = false) {
  if (!value) return '尚未记录'
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    ...(withTime ? { hour: '2-digit', minute: '2-digit', hour12: false } : {}),
  }).format(new Date(value))
}
</script>

<template>
  <el-drawer
    :model-value="visible"
    title="入职办理详情"
    size="520px"
    @update:model-value="emit('update:visible', $event)"
  >
    <el-skeleton v-if="loading" :rows="10" animated />
    <el-result v-else-if="error" icon="error" title="入职记录加载失败" :sub-title="error.message" />
    <div v-else-if="record" class="detail">
      <header>
        <span class="avatar">{{ record.candidateName.slice(0, 1) }}</span>
        <div>
          <h3>{{ record.candidateName }}</h3>
          <p>{{ record.jobTitle }}</p>
        </div>
        <span :class="`rs-status-pill rs-status-pill--${getOnboardingStatusTone(record.status)}`">{{
          record.statusText
        }}</span>
      </header>
      <section class="facts">
        <div>
          <CalendarDays :size="18" /><span>预计入职</span
          ><strong>{{ formatDate(record.entryDate) }}</strong>
        </div>
        <div>
          <span>当前节点</span><strong>{{ record.currentStep }}</strong>
        </div>
        <div>
          <span>材料状态</span><strong>{{ record.materialStatusText }}</strong>
        </div>
        <div>
          <span>完成时间</span><strong>{{ formatDate(record.completedAt) }}</strong>
        </div>
      </section>
      <section>
        <h4>候选人联系方式</h4>
        <dl>
          <div>
            <dt><Phone :size="14" />电话</dt>
            <dd>{{ record.candidatePhone || '未提供' }}</dd>
          </div>
          <div>
            <dt><Mail :size="14" />邮箱</dt>
            <dd>{{ record.candidateEmail || '未提供' }}</dd>
          </div>
        </dl>
      </section>
      <section>
        <h4>办理备注</h4>
        <p>{{ record.remark || '暂无备注' }}</p>
      </section>
      <section>
        <h4>操作记录</h4>
        <el-timeline
          ><el-timeline-item
            v-for="event in record.timeline"
            :key="event.id"
            :timestamp="formatDate(event.occurredAt, true)"
            placement="top"
            ><strong>{{ event.title }}</strong>
            <p>{{ event.description }}</p>
            <small>{{ event.actorName }}</small></el-timeline-item
          ></el-timeline
        >
      </section>
    </div>
    <template v-if="record" #footer
      ><div class="detail-actions">
        <el-button
          v-if="canCancelOnboarding(record.status)"
          type="danger"
          plain
          :icon="XCircle"
          @click="emit('cancel', record)"
          >取消流程</el-button
        ><el-button
          v-if="canCompleteOnboarding(record.status, record.materialStatus)"
          type="primary"
          :icon="UserRoundCheck"
          @click="emit('complete', record)"
          >确认入职并生成档案</el-button
        >
      </div></template
    >
  </el-drawer>
</template>

<style scoped lang="scss">
.detail {
  display: grid;
  gap: var(--rs-space-4);
}
header {
  display: flex;
  align-items: center;
  gap: var(--rs-space-3);
}
.detail-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--rs-space-2);
}
header > div {
  min-width: 0;
  flex: 1;
}
h3,
p,
h4 {
  margin: 0;
}
header p,
section p,
section small {
  color: var(--rs-text-secondary);
}
.avatar {
  display: grid;
  width: 48px;
  height: 48px;
  place-items: center;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-action-primary);
  color: var(--rs-white);
  font-weight: 700;
}
.facts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
}
.facts > div {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 0 var(--rs-space-2);
  padding: var(--rs-space-3);
}
.facts > div:nth-child(odd) {
  border-right: 1px solid var(--rs-border-default);
}
.facts > div:nth-child(n + 3) {
  border-top: 1px solid var(--rs-border-default);
}
.facts svg {
  grid-row: 1/3;
  color: var(--rs-blue-700);
}
.facts span,
dt {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}
.detail > section:not(.facts) {
  padding-top: var(--rs-space-4);
  border-top: 1px solid var(--rs-border-default);
}
h4 {
  margin-bottom: var(--rs-space-3);
  font-size: 14px;
}
dl {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--rs-space-3);
  margin: 0;
}
dt {
  display: flex;
  align-items: center;
  gap: var(--rs-space-1);
}
dd {
  margin: var(--rs-space-1) 0 0;
  overflow-wrap: anywhere;
}
:deep(.el-timeline) {
  padding-left: var(--rs-space-2);
}
</style>
