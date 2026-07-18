<script setup lang="ts">
import { MoreHorizontal, ShieldCheck, UserRound } from 'lucide-vue-next'

import HrStatusBadge from '@/components/hr/HrStatusBadge.vue'
import { formatAdminDateTime, getAdminUserStatusTone } from '@/config/adminUsers'
import type { AdminUserRecord } from '@/types/adminUser'

export type AdminUserCommand = 'role' | 'password' | 'status'

defineProps<{
  records: AdminUserRecord[]
  loading: boolean
  currentUserId: number | null
}>()

const emit = defineEmits<{
  view: [record: AdminUserRecord]
  edit: [record: AdminUserRecord]
  command: [command: AdminUserCommand, record: AdminUserRecord]
}>()

function maskPhone(value: string | null) {
  if (!value) return '未提供手机号'
  return value.replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2')
}

function handleCommand(command: AdminUserCommand, record: AdminUserRecord) {
  emit('command', command, record)
}
</script>

<template>
  <el-table
    :data="records"
    :loading="loading"
    row-key="id"
    height="calc(100dvh - 310px)"
    table-layout="fixed"
    @row-dblclick="emit('view', $event)"
  >
    <el-table-column label="用户" min-width="210">
      <template #default="{ row }: { row: AdminUserRecord }">
        <div class="admin-user-cell">
          <span class="admin-user-cell__avatar">{{ row.realName.slice(0, 1) }}</span>
          <div>
            <strong>{{ row.realName }}</strong>
            <span>@{{ row.username }}</span>
          </div>
          <el-tooltip v-if="row.id === currentUserId" content="当前登录账号">
            <ShieldCheck
              class="admin-user-cell__self"
              :size="16"
              :stroke-width="1.75"
              aria-label="当前登录账号"
            />
          </el-tooltip>
        </div>
      </template>
    </el-table-column>
    <el-table-column label="联系方式" min-width="224">
      <template #default="{ row }: { row: AdminUserRecord }">
        <div class="admin-user-stack">
          <span>{{ maskPhone(row.phone) }}</span>
          <small>{{ row.email || '未提供邮箱' }}</small>
        </div>
      </template>
    </el-table-column>
    <el-table-column label="系统角色" width="132">
      <template #default="{ row }: { row: AdminUserRecord }">
        <span class="admin-user-role"><UserRound :size="15" />{{ row.roleName }}</span>
      </template>
    </el-table-column>
    <el-table-column label="账号状态" width="104" align="center">
      <template #default="{ row }: { row: AdminUserRecord }">
        <HrStatusBadge
          :status="String(row.status)"
          :label="row.statusText"
          :tone="getAdminUserStatusTone(row.status)"
        />
      </template>
    </el-table-column>
    <el-table-column label="最近登录" width="168">
      <template #default="{ row }: { row: AdminUserRecord }">
        <span class="admin-user-time">{{ formatAdminDateTime(row.lastLoginAt) }}</span>
      </template>
    </el-table-column>
    <el-table-column label="创建时间" width="168">
      <template #default="{ row }: { row: AdminUserRecord }">
        <span class="admin-user-time">{{ formatAdminDateTime(row.createdAt) }}</span>
      </template>
    </el-table-column>
    <el-table-column label="操作" width="184" fixed="right" align="right">
      <template #default="{ row }: { row: AdminUserRecord }">
        <div class="admin-user-actions" @click.stop>
          <el-button link type="primary" @click="emit('view', row)">查看</el-button>
          <el-button link type="primary" @click="emit('edit', row)">编辑</el-button>
          <el-dropdown trigger="click" @command="handleCommand($event, row)">
            <el-button link aria-label="更多用户操作">
              更多<MoreHorizontal :size="16" :stroke-width="1.75" />
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="role" :disabled="row.id === currentUserId">
                  修改角色
                </el-dropdown-item>
                <el-dropdown-item command="password" :disabled="row.id === currentUserId">
                  重置密码
                </el-dropdown-item>
                <el-dropdown-item divided command="status" :disabled="row.id === currentUserId">
                  {{ row.status === 1 ? '禁用账号' : '启用账号' }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </template>
    </el-table-column>
    <template #empty>
      <div class="admin-user-empty">
        <UserRound :size="30" :stroke-width="1.6" aria-hidden="true" />
        <strong>没有符合条件的用户</strong>
        <span>调整筛选条件后再试，或创建新的系统账号。</span>
      </div>
    </template>
  </el-table>
</template>

<style scoped lang="scss">
.admin-user-cell,
.admin-user-role,
.admin-user-actions {
  display: flex;
  align-items: center;
}
.admin-user-cell {
  gap: var(--rs-space-2);
}
.admin-user-cell__avatar {
  display: grid;
  width: 32px;
  height: 32px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-selected);
  color: var(--rs-blue-700);
  font-weight: 700;
}
.admin-user-cell > div,
.admin-user-stack {
  display: grid;
  min-width: 0;
}
.admin-user-cell strong,
.admin-user-cell span,
.admin-user-stack span,
.admin-user-stack small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.admin-user-cell > div > span,
.admin-user-stack small {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}
.admin-user-cell__self {
  flex: 0 0 auto;
  color: var(--rs-blue-700);
}
.admin-user-role {
  width: fit-content;
  gap: var(--rs-space-1);
  color: var(--rs-text-secondary);
  font-weight: 500;
}
.admin-user-time {
  color: var(--rs-text-secondary);
  font-variant-numeric: tabular-nums;
}
.admin-user-actions {
  justify-content: flex-end;
  gap: var(--rs-space-2);
  white-space: nowrap;
}
.admin-user-actions :deep(.el-button + .el-button) {
  margin-left: 0;
}
.admin-user-actions :deep(.el-button > span) {
  gap: var(--rs-space-1);
}
.admin-user-empty {
  display: grid;
  min-height: 240px;
  place-items: center;
  align-content: center;
  gap: var(--rs-space-2);
  color: var(--rs-text-tertiary);
}
.admin-user-empty strong {
  color: var(--rs-text-primary);
}
</style>
