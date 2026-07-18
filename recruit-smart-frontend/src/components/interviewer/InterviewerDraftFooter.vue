<script setup lang="ts">
import { AlertCircle, Check, Cloud, CloudOff, LoaderCircle } from 'lucide-vue-next'
import { computed } from 'vue'

import type { InterviewDraftSaveState, InterviewerTaskStage } from '@/types/interview'

const props = defineProps<{
  stage: InterviewerTaskStage
  saveState: InterviewDraftSaveState
  lastSavedAt: string
  saving: boolean
  completing: boolean
  submitting: boolean
  scheduling: boolean
  submitDisabled: boolean
}>()

const emit = defineEmits<{ save: []; retry: []; schedule: []; complete: []; submit: [] }>()

const statusCopy = computed(() => {
  if (props.saveState === 'saving') return { text: '正在保存草稿', icon: LoaderCircle }
  if (props.saveState === 'dirty') return { text: '有更改等待保存', icon: Cloud }
  if (props.saveState === 'error') return { text: '自动保存失败', icon: CloudOff }
  if (props.saveState === 'saved')
    return { text: `草稿已保存${props.lastSavedAt ? `于 ${props.lastSavedAt}` : ''}`, icon: Check }
  return { text: '当前没有未保存更改', icon: Cloud }
})
</script>

<template>
  <footer class="interviewer-draft-footer">
    <div
      class="interviewer-draft-footer__status"
      :class="`interviewer-draft-footer__status--${saveState}`"
    >
      <component :is="statusCopy.icon" :size="16" :stroke-width="1.75" aria-hidden="true" />
      <span>{{ statusCopy.text }}</span>
      <el-button v-if="saveState === 'error'" text type="danger" @click="emit('retry')"
        >重试保存</el-button
      >
    </div>
    <div class="interviewer-draft-footer__actions">
      <el-button
        v-if="stage === 'ATTEND' || stage === 'FEEDBACK'"
        :loading="saving"
        :disabled="saveState === 'clean' || saveState === 'saved'"
        @click="emit('save')"
        >保存草稿</el-button
      >
      <el-button
        v-if="stage === 'SCHEDULE'"
        type="primary"
        :loading="scheduling"
        @click="emit('schedule')"
        >预约面试</el-button
      >
      <el-button
        v-else-if="stage === 'ATTEND'"
        type="primary"
        :loading="completing"
        @click="emit('complete')"
        >完成面试</el-button
      >
      <el-tooltip
        v-else-if="stage === 'FEEDBACK'"
        :disabled="!submitDisabled"
        content="请完成全部评分、评价依据、综合评价和录用建议"
        placement="top"
      >
        <span
          ><el-button
            type="primary"
            :loading="submitting"
            :disabled="submitDisabled"
            @click="emit('submit')"
            >提交反馈</el-button
          ></span
        >
      </el-tooltip>
      <span v-else-if="stage === 'SUBMITTED'" class="interviewer-draft-footer__readonly"
        ><Check :size="16" :stroke-width="1.75" />反馈已提交，原始评价为只读状态</span
      >
      <span v-else class="interviewer-draft-footer__readonly"
        ><AlertCircle :size="16" :stroke-width="1.75" />当前状态没有可执行操作</span
      >
    </div>
  </footer>
</template>

<style scoped lang="scss">
.interviewer-draft-footer {
  position: sticky;
  z-index: 12;
  top: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--rs-space-4);
  min-height: 64px;
  padding: var(--rs-space-3) var(--rs-space-4);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: color-mix(in srgb, var(--rs-surface-primary) 96%, transparent);
  box-shadow: var(--rs-shadow-floating);
}
.interviewer-draft-footer__status,
.interviewer-draft-footer__actions,
.interviewer-draft-footer__readonly {
  display: flex;
  align-items: center;
  gap: var(--rs-space-2);
}
.interviewer-draft-footer__status {
  color: var(--rs-text-secondary);
  font-size: 12px;
}
.interviewer-draft-footer__status--error {
  color: var(--rs-danger-700);
}
.interviewer-draft-footer__status--saved {
  color: var(--rs-success-700);
}
.interviewer-draft-footer__status--saving svg {
  animation: interviewer-spin 1s linear infinite;
}
.interviewer-draft-footer__readonly {
  color: var(--rs-text-secondary);
  font-size: 12px;
}
@keyframes interviewer-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
