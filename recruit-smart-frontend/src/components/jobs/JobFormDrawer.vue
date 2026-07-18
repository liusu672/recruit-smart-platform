<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessageBox } from 'element-plus'
import { computed, reactive, ref, watch } from 'vue'

import { parseSalaryRange } from '@/api/jobs'
import { jobDepartmentOptions } from '@/config/jobs'
import type { JobFormValue, JobPosition, JobUpdateRequest } from '@/types/job'

const props = defineProps<{
  visible: boolean
  job: JobPosition | null
  submitting: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  submit: [value: JobUpdateRequest]
}>()

const formRef = ref<FormInstance>()
const initialSnapshot = ref('')
const form = reactive<JobFormValue>({
  id: null,
  title: '',
  department: '',
  location: '',
  salaryMin: 0,
  salaryMax: 0,
  headcount: 1,
  requiredInterviewRounds: 2,
  responsibilities: '',
  requirements: '',
})

const drawerTitle = computed(() => (props.job ? '编辑职位' : '创建职位'))
const isDirty = computed(() => JSON.stringify(form) !== initialSnapshot.value)

const rules: FormRules<JobFormValue> = {
  title: [{ required: true, message: '请输入职位名称', trigger: 'blur' }],
  department: [{ required: true, message: '请选择或输入所属部门', trigger: 'change' }],
  salaryMin: [
    { required: true, message: '请输入最低薪资', trigger: 'change' },
    {
      validator: (_rule, value: number, callback) => {
        callback(value >= 0 ? undefined : new Error('最低薪资不能小于 0'))
      },
      trigger: 'change',
    },
  ],
  salaryMax: [
    { required: true, message: '请输入最高薪资', trigger: 'change' },
    {
      validator: (_rule, value: number, callback) => {
        callback(value >= form.salaryMin ? undefined : new Error('最高薪资不能低于最低薪资'))
      },
      trigger: 'change',
    },
  ],
  headcount: [
    { required: true, message: '请输入招聘人数', trigger: 'change' },
    {
      validator: (_rule, value: number, callback) => {
        callback(value >= 1 ? undefined : new Error('招聘人数至少为 1 人'))
      },
      trigger: 'change',
    },
  ],
}

function fillForm(job: JobPosition | null) {
  const [salaryMin, salaryMax] = parseSalaryRange(job?.salaryRange ?? null)
  Object.assign(form, {
    id: job?.id ?? null,
    title: job?.title ?? '',
    department: job?.department ?? '',
    location: job?.location ?? '',
    salaryMin,
    salaryMax,
    headcount: job?.headcount ?? 1,
    requiredInterviewRounds: job?.requiredInterviewRounds ?? 2,
    responsibilities: job?.description ?? '',
    requirements: job?.requirement ?? '',
  })
  initialSnapshot.value = JSON.stringify(form)
  formRef.value?.clearValidate()
}

watch(
  () => [props.visible, props.job] as const,
  ([visible, job]) => {
    if (visible) fillForm(job)
  },
)

async function requestClose(done?: () => void) {
  if (isDirty.value && !props.submitting) {
    try {
      await ElMessageBox.confirm('当前修改尚未保存，确认关闭吗？', '放弃修改', {
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
  if (!valid) return

  emit('submit', {
    title: form.title.trim(),
    department: form.department.trim(),
    location: form.location.trim(),
    salaryMin: form.salaryMin,
    salaryMax: form.salaryMax,
    headcount: form.headcount,
    requiredInterviewRounds: form.requiredInterviewRounds,
    responsibilities: form.responsibilities.trim(),
    requirements: form.requirements.trim(),
  })
}
</script>

<template>
  <el-drawer
    :model-value="visible"
    :title="drawerTitle"
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
      class="job-form"
      aria-label="职位信息表单"
    >
      <div class="job-form__grid">
        <el-form-item label="职位名称" prop="title" class="job-form__wide">
          <el-input
            v-model="form.title"
            maxlength="50"
            show-word-limit
            placeholder="例如：Java 后端开发工程师"
          />
        </el-form-item>

        <el-form-item label="所属部门" prop="department">
          <el-select
            v-model="form.department"
            filterable
            allow-create
            default-first-option
            placeholder="选择或输入部门"
          >
            <el-option
              v-for="department in jobDepartmentOptions"
              :key="department"
              :label="department"
              :value="department"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="工作地点" prop="location">
          <el-input v-model="form.location" maxlength="30" placeholder="例如：武汉" />
        </el-form-item>

        <el-form-item label="最低月薪（元）" prop="salaryMin">
          <el-input-number
            v-model="form.salaryMin"
            :min="0"
            :step="1000"
            controls-position="right"
          />
        </el-form-item>

        <el-form-item label="最高月薪（元）" prop="salaryMax">
          <el-input-number
            v-model="form.salaryMax"
            :min="0"
            :step="1000"
            controls-position="right"
          />
        </el-form-item>

        <el-form-item label="招聘人数" prop="headcount">
          <el-input-number v-model="form.headcount" :min="1" :max="999" controls-position="right" />
        </el-form-item>

        <el-form-item label="面试轮数" prop="requiredInterviewRounds">
          <el-select v-model="form.requiredInterviewRounds">
            <el-option label="1 轮（一面）" :value="1" />
            <el-option label="2 轮（一面 + 二面）" :value="2" />
            <el-option label="3 轮（一面 + 二面 + HR 面）" :value="3" />
          </el-select>
        </el-form-item>

        <el-form-item label="岗位职责" prop="responsibilities" class="job-form__wide">
          <el-input
            v-model="form.responsibilities"
            type="textarea"
            :rows="5"
            maxlength="1000"
            show-word-limit
            placeholder="说明该职位需要承担的核心工作"
          />
        </el-form-item>

        <el-form-item label="任职要求" prop="requirements" class="job-form__wide">
          <el-input
            v-model="form.requirements"
            type="textarea"
            :rows="5"
            maxlength="1000"
            show-word-limit
            placeholder="说明经验、技能和能力要求"
          />
        </el-form-item>
      </div>
    </el-form>

    <template #footer>
      <div class="job-form__footer">
        <el-button :disabled="submitting" @click="requestClose()">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">
          {{ job ? '保存修改' : '创建草稿' }}
        </el-button>
      </div>
    </template>
  </el-drawer>
</template>

<style scoped lang="scss">
.job-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 var(--rs-space-4);
}

.job-form__wide {
  grid-column: 1 / -1;
}

.job-form :deep(.el-select),
.job-form :deep(.el-input-number) {
  width: 100%;
}

.job-form__footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--rs-space-2);
}
</style>
