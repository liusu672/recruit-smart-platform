<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { Database, Plus, RefreshCw, RotateCcw, Search } from 'lucide-vue-next'
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import AdminUserDetailDrawer from '@/components/admin/AdminUserDetailDrawer.vue'
import AdminUserFormDrawer from '@/components/admin/AdminUserFormDrawer.vue'
import AdminUserTable, { type AdminUserCommand } from '@/components/admin/AdminUserTable.vue'
import SupportErrorState from '@/components/shared/SupportErrorState.vue'
import SupportPageHeader from '@/components/shared/SupportPageHeader.vue'
import { useAdminUserManagement } from '@/composables/useAdminUserManagement'
import { adminUserStatusOptions } from '@/config/adminUsers'
import { useSessionStore } from '@/stores/session'
import type {
  AdminPasswordResetRequest,
  AdminUserCreateRequest,
  AdminUserRecord,
  AdminUserStatus,
  AdminUserUpdateRequest,
} from '@/types/adminUser'

type FormMode = 'create' | 'edit'

const route = useRoute()
const router = useRouter()
const session = useSessionStore()
const state = useAdminUserManagement()

function readNumber(value: unknown, fallback: number) {
  const parsed = Number(Array.isArray(value) ? value[0] : value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback
}

function readStatus(value: unknown): AdminUserStatus | '' {
  const raw = Array.isArray(value) ? value[0] : value
  return raw === '0' ? 0 : raw === '1' ? 1 : ''
}

const filterForm = reactive<{
  keyword: string
  roleId: number | null
  status: AdminUserStatus | ''
}>({
  keyword: typeof route.query.keyword === 'string' ? route.query.keyword : '',
  roleId: route.query.roleId ? readNumber(route.query.roleId, 0) || null : null,
  status: readStatus(route.query.status),
})
Object.assign(state.query, filterForm, {
  page: readNumber(route.query.page, 1),
  pageSize: readNumber(route.query.pageSize, 10),
})

const records = computed(() => state.listQuery.data.value?.items ?? [])
const total = computed(() => state.listQuery.data.value?.total ?? 0)
const roles = computed(() => state.rolesQuery.data.value ?? [])
const listError = computed(() => state.listQuery.error.value as Error | null)
const detailError = computed(() => state.detailQuery.error.value as Error | null)
const currentUserId = computed(() => {
  const id = Number(session.user?.id)
  return Number.isFinite(id) ? id : null
})
const activeFilterCount = computed(
  () =>
    [
      Boolean(filterForm.keyword.trim()),
      filterForm.roleId !== null,
      filterForm.status !== '',
    ].filter(Boolean).length,
)
const detailVisible = computed({
  get: () => state.selectedId.value !== null,
  set: (value) => {
    if (!value) state.closeDetail()
  },
})

const formVisible = ref(false)
const formMode = ref<FormMode>('create')
const editingRecord = ref<AdminUserRecord | null>(null)
const roleDialogVisible = ref(false)
const roleTarget = ref<AdminUserRecord | null>(null)
const selectedRoleId = ref<number | null>(null)
const passwordDialogVisible = ref(false)
const passwordTarget = ref<AdminUserRecord | null>(null)
const passwordForm = reactive<AdminPasswordResetRequest>({ newPassword: '', confirmPassword: '' })
const passwordError = computed(() => {
  if (!passwordForm.newPassword && !passwordForm.confirmPassword) return ''
  if (passwordForm.newPassword.length < 6 || passwordForm.newPassword.length > 32) {
    return '新密码长度必须为 6-32 位'
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) return '两次输入的密码不一致'
  return ''
})

function syncUrl() {
  void router.replace({
    query: {
      ...route.query,
      keyword: state.query.keyword || undefined,
      roleId: state.query.roleId ?? undefined,
      status: state.query.status === '' ? undefined : state.query.status,
      page: state.query.page > 1 ? state.query.page : undefined,
      pageSize: state.query.pageSize !== 10 ? state.query.pageSize : undefined,
    },
  })
}

watch([() => state.query.page, () => state.query.pageSize], syncUrl)

function submitFilters() {
  state.applyFilters({
    keyword: filterForm.keyword.trim(),
    roleId: filterForm.roleId,
    status: filterForm.status,
  })
  syncUrl()
}

function resetFilters() {
  Object.assign(filterForm, { keyword: '', roleId: null, status: '' })
  state.resetFilters()
  syncUrl()
}

function openCreate() {
  formMode.value = 'create'
  editingRecord.value = null
  formVisible.value = true
}

function openEdit(record: AdminUserRecord) {
  state.closeDetail()
  formMode.value = 'edit'
  editingRecord.value = record
  formVisible.value = true
}

async function submitForm(data: AdminUserCreateRequest | AdminUserUpdateRequest) {
  try {
    if ('username' in data) {
      await state.createUser(data)
      ElMessage.success('系统用户已创建')
    } else if (editingRecord.value) {
      await state.updateMutation.mutateAsync({ id: editingRecord.value.id, data })
      ElMessage.success('用户资料已更新')
    }
    formVisible.value = false
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '用户资料保存失败')
  }
}

function openRoleDialog(record: AdminUserRecord) {
  state.closeDetail()
  roleTarget.value = record
  selectedRoleId.value = record.roleId
  roleDialogVisible.value = true
}

async function submitRole() {
  if (!roleTarget.value || selectedRoleId.value === null) return
  try {
    await state.roleMutation.mutateAsync({
      id: roleTarget.value.id,
      data: { roleId: selectedRoleId.value },
    })
    ElMessage.success('用户角色已更新')
    roleDialogVisible.value = false
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '用户角色更新失败')
  }
}

function openPasswordDialog(record: AdminUserRecord) {
  state.closeDetail()
  passwordTarget.value = record
  Object.assign(passwordForm, { newPassword: '', confirmPassword: '' })
  passwordDialogVisible.value = true
}

async function submitPassword() {
  if (!passwordTarget.value || passwordError.value || !passwordForm.newPassword) return
  try {
    await state.passwordMutation.mutateAsync({
      id: passwordTarget.value.id,
      data: { ...passwordForm },
    })
    ElMessage.success('用户密码已重置')
    passwordDialogVisible.value = false
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '密码重置失败')
  }
}

async function changeStatus(record: AdminUserRecord) {
  state.closeDetail()
  const nextStatus: AdminUserStatus = record.status === 1 ? 0 : 1
  const action = nextStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(
      nextStatus === 1
        ? `确认启用“${record.realName}”的账号？启用后该用户可重新登录。`
        : `确认禁用“${record.realName}”的账号？禁用后该用户将无法登录。`,
      `${action}账号`,
      {
        confirmButtonText: `确认${action}`,
        cancelButtonText: '取消',
        type: nextStatus === 1 ? 'info' : 'warning',
      },
    )
    await state.statusMutation.mutateAsync({ id: record.id, data: { status: nextStatus } })
    ElMessage.success(`账号已${action}`)
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error instanceof Error ? error.message : `账号${action}失败`)
  }
}

function handleCommand(command: AdminUserCommand, record: AdminUserRecord) {
  if (command === 'role') openRoleDialog(record)
  if (command === 'password') openPasswordDialog(record)
  if (command === 'status') void changeStatus(record)
}
</script>

<template>
  <div class="admin-users-view">
    <SupportPageHeader
      title="用户管理"
      description="维护系统账号、联系人资料、角色与登录状态。关键变更均由管理员明确确认。"
    >
      <template #actions>
        <el-button
          type="primary"
          :icon="Plus"
          :disabled="state.rolesQuery.isError.value"
          @click="openCreate"
        >
          创建用户
        </el-button>
      </template>
    </SupportPageHeader>

    <section class="admin-users-source" aria-label="当前数据来源">
      <Database :size="17" :stroke-width="1.75" aria-hidden="true" />
      <div>
        <strong>管理员接口数据</strong>
        <span>页面按真实接口契约加载；本地验收时可使用 Mock API 提供的内存演示数据。</span>
      </div>
    </section>

    <form class="admin-users-filters" role="search" @submit.prevent="submitFilters">
      <div class="admin-users-filters__fields">
        <el-input
          v-model="filterForm.keyword"
          clearable
          placeholder="搜索账号、姓名、手机号或邮箱"
        />
        <el-select v-model="filterForm.roleId" clearable placeholder="全部角色">
          <el-option v-for="role in roles" :key="role.id" :label="role.roleName" :value="role.id" />
        </el-select>
        <el-select v-model="filterForm.status" clearable placeholder="全部状态">
          <el-option
            v-for="option in adminUserStatusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </div>
      <div class="admin-users-filters__actions">
        <el-button native-type="submit" type="primary" :icon="Search">查询</el-button>
        <el-button :icon="RotateCcw" :disabled="activeFilterCount === 0" @click="resetFilters">
          重置<span v-if="activeFilterCount">（{{ activeFilterCount }}）</span>
        </el-button>
        <el-tooltip content="刷新当前列表">
          <el-button
            :icon="RefreshCw"
            :loading="state.listQuery.isFetching.value"
            aria-label="刷新用户列表"
            @click="state.listQuery.refetch()"
          />
        </el-tooltip>
      </div>
    </form>

    <SupportErrorState
      v-if="listError"
      title="用户列表暂时无法加载"
      description="请确认当前接口服务可用；本地验收时也可检查 Mock API 是否已启动。"
      :loading="state.listQuery.isFetching.value"
      @retry="state.listQuery.refetch()"
    />
    <section v-else class="admin-users-table">
      <AdminUserTable
        :records="records"
        :loading="state.listQuery.isLoading.value"
        :current-user-id="currentUserId"
        @view="state.openDetail($event.id)"
        @edit="openEdit"
        @command="handleCommand"
      />
      <footer class="admin-users-pagination">
        <span>共 {{ total }} 个系统用户</span>
        <el-pagination
          v-model:current-page="state.query.page"
          v-model:page-size="state.query.pageSize"
          background
          layout="sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
          :total="total"
        />
      </footer>
    </section>

    <AdminUserDetailDrawer
      v-model:visible="detailVisible"
      :record="state.detailQuery.data.value"
      :loading="state.detailQuery.isLoading.value"
      :error="detailError"
      :current-user-id="currentUserId"
      @retry="state.detailQuery.refetch()"
      @edit="openEdit"
      @role="openRoleDialog"
      @password="openPasswordDialog"
      @status="changeStatus"
    />
    <AdminUserFormDrawer
      v-model:visible="formVisible"
      :mode="formMode"
      :record="editingRecord"
      :roles="roles"
      :saving="state.createMutation.isPending.value || state.updateMutation.isPending.value"
      @submit="submitForm"
    />

    <el-dialog v-model="roleDialogVisible" title="修改用户角色" width="440px">
      <div v-if="roleTarget" class="admin-users-dialog-body">
        <p>为“{{ roleTarget.realName }}”选择新的系统角色。</p>
        <el-form label-position="top">
          <el-form-item label="系统角色" required>
            <el-select v-model="selectedRoleId" placeholder="请选择系统角色">
              <el-option
                v-for="role in roles"
                :key="role.id"
                :label="role.roleName"
                :value="role.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <span class="admin-users-dialog-note">修改后，用户下次访问页面时将使用新的权限边界。</span>
      </div>
      <template #footer>
        <el-button
          :disabled="state.roleMutation.isPending.value"
          @click="roleDialogVisible = false"
        >
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="state.roleMutation.isPending.value"
          :disabled="!selectedRoleId || selectedRoleId === roleTarget?.roleId"
          @click="submitRole"
        >
          确认修改
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordDialogVisible" title="重置用户密码" width="440px">
      <div v-if="passwordTarget" class="admin-users-dialog-body">
        <p>为“{{ passwordTarget.realName }}”设置新的登录密码。</p>
        <el-form label-position="top">
          <el-form-item label="新密码" :error="passwordError">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              show-password
              maxlength="32"
              placeholder="6-32 位密码"
            />
          </el-form-item>
          <el-form-item label="确认新密码">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              show-password
              maxlength="32"
              placeholder="再次输入新密码"
            />
          </el-form-item>
        </el-form>
        <span class="admin-users-dialog-note">密码重置成功后，请通过安全渠道通知用户。</span>
      </div>
      <template #footer>
        <el-button
          :disabled="state.passwordMutation.isPending.value"
          @click="passwordDialogVisible = false"
        >
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="state.passwordMutation.isPending.value"
          :disabled="Boolean(passwordError) || !passwordForm.newPassword"
          @click="submitPassword"
        >
          确认重置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.admin-users-view {
  display: grid;
  gap: var(--rs-space-4);
  min-height: 0;
}
.admin-users-source,
.admin-users-filters,
.admin-users-filters__fields,
.admin-users-filters__actions,
.admin-users-pagination {
  display: flex;
  align-items: center;
}
.admin-users-source {
  gap: var(--rs-space-3);
  min-height: 48px;
  padding: 0 var(--rs-space-4);
  border: 1px solid var(--rs-blue-500);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-selected);
  color: var(--rs-blue-700);
}
.admin-users-source > div {
  display: flex;
  align-items: baseline;
  gap: var(--rs-space-2);
}
.admin-users-source span {
  color: var(--rs-text-secondary);
  font-size: 12px;
}
.admin-users-filters {
  justify-content: space-between;
  gap: var(--rs-space-4);
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
.admin-users-filters__fields {
  min-width: 0;
  flex: 1;
  gap: var(--rs-space-2);
}
.admin-users-filters__fields :deep(.el-input) {
  width: min(360px, 36vw);
}
.admin-users-filters__fields :deep(.el-select) {
  width: 160px;
}
.admin-users-filters__actions {
  flex: 0 0 auto;
  gap: var(--rs-space-2);
}
.admin-users-filters__actions :deep(.el-button + .el-button) {
  margin-left: 0;
}
.admin-users-table {
  min-height: 0;
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
.admin-users-pagination {
  min-height: 56px;
  justify-content: space-between;
  gap: var(--rs-space-4);
  padding: 0 var(--rs-space-4);
  border-top: 1px solid var(--rs-border-default);
  color: var(--rs-text-secondary);
}
.admin-users-dialog-body {
  display: grid;
  gap: var(--rs-space-4);
}
.admin-users-dialog-body p,
.admin-users-dialog-body :deep(.el-form-item) {
  margin: 0;
}
.admin-users-dialog-note {
  color: var(--rs-text-secondary);
  font-size: 12px;
}
@media (max-width: 1366px) {
  .admin-users-source > div {
    display: grid;
    gap: 0;
  }
  .admin-users-filters__fields :deep(.el-input) {
    width: 300px;
  }
}
</style>
