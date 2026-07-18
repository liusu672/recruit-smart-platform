<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessageBox } from 'element-plus'
import { computed, nextTick, reactive, ref, watch } from 'vue'

import type {
  AdminRoleOption,
  AdminUserCreateRequest,
  AdminUserRecord,
  AdminUserUpdateRequest,
} from '@/types/adminUser'

type FormMode = 'create' | 'edit'
type FormModel = {
  username: string
  password: string
  realName: string
  phone: string
  email: string
  roleId: number | null
}

const props = defineProps<{
  visible: boolean
  mode: FormMode
  record: AdminUserRecord | null
  roles: AdminRoleOption[]
  saving: boolean
}>()
const emit = defineEmits<{
  'update:visible': [value: boolean]
  submit: [data: AdminUserCreateRequest | AdminUserUpdateRequest]
}>()

const formRef = ref<FormInstance>()
const snapshot = ref('')
const form = reactive<FormModel>({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  roleId: null,
})
const title = computed(() => (props.mode === 'create' ? '创建系统用户' : '编辑用户资料'))
const dirty = computed(() => JSON.stringify(form) !== snapshot.value)
const rules: FormRules<FormModel> = {
  username: [
    { required: true, message: '请输入登录账号', trigger: 'blur' },
    { min: 4, max: 32, message: '账号长度为 4-32 位', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]+$/, message: '只能使用字母、数字和下划线', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' },
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { max: 64, message: '真实姓名不能超过 64 个字符', trigger: 'blur' },
  ],
  phone: [{ pattern: /^$|^1[3-9]\d{9}$/, message: '请输入正确的 11 位手机号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择系统角色', trigger: 'change' }],
}

function resetForm() {
  Object.assign(form, {
    username: props.mode === 'edit' ? (props.record?.username ?? '') : '',
    password: '',
    realName: props.record?.realName ?? '',
    phone: props.record?.phone ?? '',
    email: props.record?.email ?? '',
    roleId: props.mode === 'edit' ? (props.record?.roleId ?? null) : null,
  })
  snapshot.value = JSON.stringify(form)
  void nextTick(() => formRef.value?.clearValidate())
}

watch(() => [props.visible, props.mode, props.record?.id] as const, resetForm, { immediate: true })

async function requestClose(done?: () => void) {
  if (props.saving) return
  if (dirty.value) {
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
  if (done) done()
  else emit('update:visible', false)
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (props.mode === 'create') {
    if (form.roleId === null) return
    emit('submit', {
      username: form.username.trim(),
      password: form.password,
      realName: form.realName.trim(),
      phone: form.phone.trim(),
      email: form.email.trim(),
      roleId: form.roleId,
    })
    return
  }
  emit('submit', {
    realName: form.realName.trim(),
    phone: form.phone.trim(),
    email: form.email.trim(),
  })
}
</script>

<template>
  <el-drawer
    :model-value="visible"
    :title="title"
    size="520px"
    :before-close="requestClose"
    destroy-on-close
    @update:model-value="emit('update:visible', $event)"
  >
    <div class="admin-user-form-intro">
      <strong>{{ mode === 'create' ? '账号与身份信息' : `@${record?.username}` }}</strong>
      <span>
        {{
          mode === 'create'
            ? '创建后可继续调整角色或账号状态。'
            : '此处只修改联系人资料，角色和状态通过独立操作变更。'
        }}
      </span>
    </div>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <div v-if="mode === 'create'" class="admin-user-form-grid">
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" maxlength="32" placeholder="例如 hr02" />
        </el-form-item>
        <el-form-item label="初始密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            maxlength="32"
            placeholder="6-32 位密码"
          />
        </el-form-item>
      </div>
      <el-form-item label="真实姓名" prop="realName">
        <el-input v-model="form.realName" maxlength="64" placeholder="请输入真实姓名" />
      </el-form-item>
      <div class="admin-user-form-grid">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" placeholder="选填" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" maxlength="128" placeholder="选填" />
        </el-form-item>
      </div>
      <el-form-item v-if="mode === 'create'" label="系统角色" prop="roleId">
        <el-select v-model="form.roleId" placeholder="请选择系统角色">
          <el-option v-for="role in roles" :key="role.id" :label="role.roleName" :value="role.id">
            <div class="admin-role-option">
              <span>{{ role.roleName }}</span
              ><small>{{ role.description }}</small>
            </div>
          </el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="admin-user-form-footer">
        <span>{{ dirty ? '有未保存的更改' : '尚未修改' }}</span>
        <div>
          <el-button :disabled="saving" @click="requestClose()">取消</el-button>
          <el-button type="primary" :loading="saving" :disabled="!dirty" @click="submit">
            {{ mode === 'create' ? '创建用户' : '保存修改' }}
          </el-button>
        </div>
      </div>
    </template>
  </el-drawer>
</template>

<style scoped lang="scss">
.admin-user-form-intro {
  display: grid;
  gap: var(--rs-space-1);
  margin-bottom: var(--rs-space-6);
  padding: var(--rs-space-4);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
}
.admin-user-form-intro span,
.admin-user-form-footer > span,
.admin-role-option small {
  color: var(--rs-text-secondary);
  font-size: 12px;
}
.admin-user-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--rs-space-4);
}
.admin-role-option {
  display: flex;
  width: 100%;
  justify-content: space-between;
  gap: var(--rs-space-4);
}
.admin-user-form-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--rs-space-4);
}
.admin-user-form-footer > div {
  display: flex;
  gap: var(--rs-space-2);
}
</style>
