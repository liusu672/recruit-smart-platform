<script setup lang="ts">
import { computed, reactive, watch } from 'vue'

import { pipelineRejectReasonOptions, screeningDecisionCopy } from '@/config/pipeline'
import type { ScreeningDecision } from '@/types/pipeline'

const visible = defineModel<boolean>('visible', { required: true })

const props = defineProps<{
  decision: ScreeningDecision
  candidateName: string
  submitting: boolean
}>()

const emit = defineEmits<{
  submit: [payload: { decision: ScreeningDecision; rejectReasonCode: string; note: string }]
}>()

const form = reactive({ rejectReasonCode: '', note: '' })
const errors = reactive({ rejectReasonCode: '', note: '' })
const copy = computed(() => screeningDecisionCopy[props.decision])

watch(visible, (isVisible) => {
  if (!isVisible) return
  form.rejectReasonCode = ''
  form.note = ''
  errors.rejectReasonCode = ''
  errors.note = ''
})

function submit() {
  errors.rejectReasonCode = ''
  errors.note = ''

  if (props.decision === 'REJECT' && !form.rejectReasonCode) {
    errors.rejectReasonCode = '请选择拒绝原因'
  }
  if (props.decision !== 'PASS' && !form.note.trim()) {
    errors.note = props.decision === 'REJECT' ? '请填写拒绝说明' : '请填写待核实事项'
  }
  if (errors.rejectReasonCode || errors.note) return

  emit('submit', {
    decision: props.decision,
    rejectReasonCode: form.rejectReasonCode,
    note: form.note.trim(),
  })
}
</script>

<template>
  <el-dialog v-model="visible" :title="copy.title" width="480px" destroy-on-close>
    <div class="screening-dialog">
      <p>
        候选人：<strong>{{ candidateName }}</strong>
      </p>
      <div class="screening-dialog__notice">{{ copy.description }}</div>

      <label v-if="decision === 'REJECT'" class="screening-dialog__field">
        <span>拒绝原因</span>
        <el-select v-model="form.rejectReasonCode" placeholder="请选择可审计的业务原因">
          <el-option
            v-for="option in pipelineRejectReasonOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        <small v-if="errors.rejectReasonCode">{{ errors.rejectReasonCode }}</small>
      </label>

      <label class="screening-dialog__field">
        <span>{{ decision === 'PASS' ? 'HR 备注（可选）' : '业务说明' }}</span>
        <el-input
          v-model="form.note"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
          :placeholder="decision === 'PASS' ? '记录通过依据或下一步关注点' : '填写具体业务依据'"
        />
        <small v-if="errors.note">{{ errors.note }}</small>
      </label>
    </div>

    <template #footer>
      <el-button :disabled="submitting" @click="visible = false">取消</el-button>
      <el-button
        :type="decision === 'REJECT' ? 'danger' : 'primary'"
        :loading="submitting"
        @click="submit"
      >
        {{ copy.confirmLabel }}
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
.screening-dialog,
.screening-dialog__field {
  display: grid;
  gap: var(--rs-space-3);
}

.screening-dialog p {
  margin: 0;
  color: var(--rs-text-secondary);
}

.screening-dialog__notice {
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-muted);
  color: var(--rs-text-secondary);
}

.screening-dialog__field > span {
  color: var(--rs-text-primary);
  font-weight: 600;
}

.screening-dialog__field small {
  color: var(--rs-danger-700);
}
</style>
