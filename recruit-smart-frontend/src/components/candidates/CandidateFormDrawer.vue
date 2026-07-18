<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessageBox } from 'element-plus'
import { computed, reactive, ref, watch } from 'vue'

import {
  candidateEducationOptions,
  candidateSourceOptions,
  candidateStatusOptions,
} from '@/config/candidates'
import type { CandidateCreateRequest, CandidateDetail } from '@/types/candidate'

type CandidateFormValue = CandidateCreateRequest

const props = defineProps<{
  visible: boolean
  submitting: boolean
  candidate?: CandidateDetail | null
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  submit: [value: CandidateFormValue]
}>()

const formRef = ref<FormInstance>()
const initialSnapshot = ref('')
const form = reactive<CandidateFormValue>({
  name: '',
  gender: '',
  age: null,
  phone: '',
  email: '',
  education: '',
  school: '',
  major: '',
  yearsOfExperience: 0,
  currentStatus: 'AVAILABLE',
  source: 'HR_IMPORT',
})

const isDirty = computed(() => JSON.stringify(form) !== initialSnapshot.value)

const rules: FormRules<CandidateFormValue> = {
  name: [{ required: true, message: '请输入候选人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback) => {
        callback(!value || /^1[3-9]\d{9}$/.test(value) ? undefined : new Error('手机号格式不正确'))
      },
      trigger: 'blur',
    },
  ],
  email: [{ type: 'email', message: '请输入有效邮箱地址', trigger: 'blur' }],
  education: [{ required: true, message: '请选择最高学历', trigger: 'change' }],
  yearsOfExperience: [
    { required: true, message: '请输入工作年限', trigger: 'change' },
    {
      validator: (_rule, value: number, callback) => {
        callback(value >= 0 ? undefined : new Error('工作年限不能小于 0'))
      },
      trigger: 'change',
    },
  ],
}

function resetForm() {
  const candidate = props.candidate
  Object.assign(
    form,
    candidate
      ? {
          name: candidate.name,
          gender: candidate.gender ?? '',
          age: candidate.age ?? null,
          phone: candidate.phone ?? '',
          email: candidate.email ?? '',
          education: candidate.education ?? '',
          school: candidate.school ?? '',
          major: candidate.major ?? '',
          yearsOfExperience: candidate.yearsOfExperience,
          currentStatus: candidate.currentStatus,
          source: candidate.source,
        }
      : {
          name: '',
          gender: '',
          age: null,
          phone: '',
          email: '',
          education: '',
          school: '',
          major: '',
          yearsOfExperience: 0,
          currentStatus: 'AVAILABLE',
          source: 'HR_IMPORT',
        },
  )
  initialSnapshot.value = JSON.stringify(form)
  formRef.value?.clearValidate()
}

watch(
  () => props.visible,
  (visible) => {
    if (visible) resetForm()
  },
)

async function requestClose(done?: () => void) {
  if (isDirty.value && !props.submitting) {
    try {
      await ElMessageBox.confirm('当前候选人信息尚未保存，确认关闭吗？', '放弃录入', {
        confirmButtonText: '确认关闭',
        cancelButtonText: '继续录入',
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
  if (!valid) return

  emit('submit', {
    name: form.name.trim(),
    gender: form.gender,
    age: form.age ?? null,
    phone: form.phone.trim(),
    email: form.email.trim(),
    education: form.education,
    school: form.school.trim(),
    major: form.major.trim(),
    yearsOfExperience: form.yearsOfExperience,
    currentStatus: form.currentStatus,
    source: form.source,
  })
}
</script>

<template>
  <el-drawer
    :model-value="visible"
    :title="candidate ? '编辑候选人' : '录入候选人'"
    size="560px"
    :close-on-click-modal="false"
    :before-close="requestClose"
    @update:model-value="emit('update:visible', $event)"
  >
    <el-alert
      title="手机号为必填信息，也用于识别重复候选人。"
      type="info"
      :closable="false"
      show-icon
      class="candidate-form__notice"
    />

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      aria-label="候选人信息表单"
    >
      <div class="candidate-form__grid">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" maxlength="30" placeholder="请输入候选人姓名" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" clearable placeholder="请选择">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="form.age" :min="16" :max="80" controls-position="right" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" placeholder="请输入 11 位手机号" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" maxlength="100" placeholder="请输入邮箱地址" />
        </el-form-item>

        <el-form-item label="最高学历" prop="education">
          <el-select v-model="form.education" placeholder="请选择学历">
            <el-option
              v-for="education in candidateEducationOptions"
              :key="education"
              :label="education"
              :value="education"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="工作年限" prop="yearsOfExperience">
          <el-input-number
            v-model="form.yearsOfExperience"
            :min="0"
            :max="60"
            controls-position="right"
          />
        </el-form-item>

        <el-form-item label="毕业学校" prop="school">
          <el-input v-model="form.school" maxlength="100" placeholder="请输入毕业学校" />
        </el-form-item>

        <el-form-item label="专业" prop="major">
          <el-input v-model="form.major" maxlength="100" placeholder="请输入专业" />
        </el-form-item>

        <el-form-item label="当前状态" prop="currentStatus">
          <el-select v-model="form.currentStatus" :disabled="Boolean(candidate)">
            <el-option
              v-for="status in candidateStatusOptions"
              :key="status.value"
              :label="status.label"
              :value="status.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="候选人来源" prop="source">
          <el-select v-model="form.source">
            <el-option
              v-for="source in candidateSourceOptions"
              :key="source.value"
              :label="source.label"
              :value="source.value"
            />
          </el-select>
        </el-form-item>
      </div>
    </el-form>

    <template #footer>
      <div class="candidate-form__footer">
        <el-button :disabled="submitting" @click="requestClose()">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">
          {{ candidate ? '保存修改' : '保存候选人' }}
        </el-button>
      </div>
    </template>
  </el-drawer>
</template>

<style scoped lang="scss">
.candidate-form__notice {
  margin-bottom: var(--rs-space-4);
}

.candidate-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 var(--rs-space-4);
}

.candidate-form__grid :deep(.el-select),
.candidate-form__grid :deep(.el-input-number) {
  width: 100%;
}

.candidate-form__footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--rs-space-2);
}
</style>
