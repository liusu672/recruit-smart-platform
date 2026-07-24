<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessageBox } from 'element-plus'
import { computed, reactive, ref, watch } from 'vue'

import { probationMonthOptions } from '@/config/offers'
import type {
  OfferCandidateOption,
  OfferFormSubmitValue,
  OfferFormValue,
  OfferRecord,
} from '@/types/offer'

const props = defineProps<{
  visible: boolean
  offer: OfferRecord | null
  candidates: OfferCandidateOption[]
  submitting: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  submit: [value: OfferFormSubmitValue]
}>()

const formRef = ref<FormInstance>()
const initialSnapshot = ref('')
const form = reactive<OfferFormValue>({
  applicationId: null,
  candidateName: '',
  jobTitle: '',
  salary: 0,
  entryDate: '',
  probationMonths: 3,
  workLocation: '',
  remark: '',
})

const title = computed(() => (props.offer ? '编辑 Offer 草稿' : '创建 Offer 草稿'))
const isDirty = computed(() => JSON.stringify(form) !== initialSnapshot.value)

const rules: FormRules<OfferFormValue> = {
  applicationId: [{ required: true, message: '请选择面试中的投递', trigger: 'change' }],
  salary: [
    { required: true, message: '请输入录用月薪', trigger: 'change' },
    {
      validator: (_rule, value: number, callback) => {
        callback(value > 0 ? undefined : new Error('录用月薪必须大于 0'))
      },
      trigger: 'change',
    },
  ],
  entryDate: [{ required: true, message: '请选择预计入职日期', trigger: 'change' }],
  workLocation: [{ required: true, message: '请输入工作地点', trigger: 'blur' }],
}

function fillForm(offer: OfferRecord | null) {
  Object.assign(form, {
    applicationId: offer?.applicationId ?? null,
    candidateName: offer?.candidateName ?? '',
    jobTitle: offer?.jobTitle ?? '',
    salary: offer?.salary ?? 0,
    entryDate: offer?.entryDate ?? '',
    probationMonths: offer?.probationMonths ?? 3,
    workLocation: offer?.workLocation ?? '',
    remark: offer?.remark ?? '',
  })
  initialSnapshot.value = JSON.stringify(form)
  formRef.value?.clearValidate()
}

watch(
  () => [props.visible, props.offer] as const,
  ([visible, offer]) => {
    if (visible) fillForm(offer)
  },
)

function selectCandidate(applicationId: number) {
  const candidate = props.candidates.find((item) => item.applicationId === applicationId)
  form.candidateName = candidate?.candidateName ?? ''
  form.jobTitle = candidate?.jobTitle ?? ''
}

function formatCandidateLabel(candidate: OfferCandidateOption) {
  const score = candidate.interviewScore === null ? '面试已提交' : `面试 ${candidate.interviewScore} 分`
  const suggestion =
    candidate.interviewSuggestion === 'PASS'
      ? '建议通过'
      : candidate.interviewSuggestion === 'REJECT'
        ? '建议不通过'
        : candidate.interviewSuggestion === 'PENDING'
          ? '建议待定'
          : '已完成反馈'
  return `${candidate.candidateName} - ${candidate.jobTitle}（${score} / ${suggestion}）`
}

async function requestClose(done?: () => void) {
  if (isDirty.value && !props.submitting) {
    try {
      await ElMessageBox.confirm('当前 Offer 草稿尚未保存，确认关闭吗？', '放弃修改', {
        confirmButtonText: '确认关闭',
        cancelButtonText: '继续编辑',
        type: 'warning',
      })
    } catch {
      return
    }
  }
  done?.()
  emit('update:visible', false)
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || form.applicationId === null) return
  emit('submit', {
    applicationId: form.applicationId,
    salary: form.salary,
    entryDate: form.entryDate,
    probationMonths: form.probationMonths,
    workLocation: form.workLocation.trim(),
    remark: form.remark.trim(),
  })
}
</script>

<template>
  <el-drawer
    :model-value="visible"
    :title="title"
    size="560px"
    :close-on-click-modal="false"
    :before-close="requestClose"
    @update:model-value="emit('update:visible', $event)"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      class="offer-form"
      aria-label="Offer 草稿表单"
    >
      <el-alert
        title="保存草稿不会发送给候选人"
        description="薪资、入职日期和备注需要由 HR 复核，发送时会再次确认。"
        type="info"
        :closable="false"
        show-icon
      />

      <el-form-item label="候选人与录用职位" prop="applicationId">
        <el-select
          v-if="!offer"
          v-model="form.applicationId"
          placeholder="选择面试中的候选人"
          @change="selectCandidate"
        >
          <el-option
            v-for="candidate in candidates"
            :key="candidate.applicationId"
            :label="formatCandidateLabel(candidate)"
            :value="candidate.applicationId"
          />
        </el-select>
        <div v-else class="offer-form__candidate">
          <strong>{{ form.candidateName }}</strong>
          <span>{{ form.jobTitle }}，投递 #{{ form.applicationId }}</span>
        </div>
      </el-form-item>

      <div class="offer-form__grid">
        <el-form-item label="录用月薪（元）" prop="salary">
          <el-input-number v-model="form.salary" :min="0" :step="1000" controls-position="right" />
        </el-form-item>
        <el-form-item label="预计入职日期" prop="entryDate">
          <el-date-picker
            v-model="form.entryDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
          />
        </el-form-item>
        <el-form-item label="试用期" prop="probationMonths">
          <el-select v-model="form.probationMonths">
            <el-option
              v-for="month in probationMonthOptions"
              :key="month"
              :label="`${month} 个月`"
              :value="month"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="工作地点" prop="workLocation">
          <el-input v-model="form.workLocation" maxlength="50" placeholder="例如：武汉" />
        </el-form-item>
        <el-form-item label="方案备注" prop="remark" class="offer-form__wide">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
            placeholder="记录薪资组成、福利说明或需要候选人确认的事项"
          />
        </el-form-item>
      </div>
    </el-form>

    <template #footer>
      <div class="offer-form__footer">
        <el-button :disabled="submitting" @click="requestClose()">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存草稿</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<style scoped lang="scss">
.offer-form {
  display: grid;
  gap: var(--rs-space-4);
}

.offer-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 var(--rs-space-4);
}

.offer-form__wide {
  grid-column: 1 / -1;
}

.offer-form :deep(.el-select),
.offer-form :deep(.el-input-number),
.offer-form :deep(.el-date-editor) {
  width: 100%;
}

.offer-form__candidate {
  display: grid;
  width: 100%;
  gap: var(--rs-space-1);
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
}

.offer-form__candidate span {
  color: var(--rs-text-secondary);
  font-size: 12px;
}

.offer-form__footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--rs-space-2);
}
</style>
