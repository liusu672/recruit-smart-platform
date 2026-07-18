<script setup lang="ts">
import { CalendarDays, KeyRound, Mail, Phone, ShieldCheck, UserRoundCog } from 'lucide-vue-next'

import SupportErrorState from '@/components/shared/SupportErrorState.vue'
import HrStatusBadge from '@/components/hr/HrStatusBadge.vue'
import { formatAdminDateTime, getAdminUserStatusTone } from '@/config/adminUsers'
import type { AdminUserRecord } from '@/types/adminUser'

defineProps<{
  visible: boolean
  record: AdminUserRecord | undefined
  loading: boolean
  error: Error | null
  currentUserId: number | null
}>()
const emit = defineEmits<{
  'update:visible': [value: boolean]
  retry: []
  edit: [record: AdminUserRecord]
  role: [record: AdminUserRecord]
  password: [record: AdminUserRecord]
  status: [record: AdminUserRecord]
}>()
</script>

<template>
  <el-drawer
    :model-value="visible"
    title="用户详情"
    size="520px"
    @update:model-value="emit('update:visible', $event)"
  >
    <div v-if="loading" class="admin-user-detail-skeleton">
      <el-skeleton :rows="8" animated />
    </div>
    <SupportErrorState
      v-else-if="error"
      title="用户详情暂时无法加载"
      description="请重新加载。如果问题持续存在，请稍后再试。"
      @retry="emit('retry')"
    />
    <div v-else-if="record" class="admin-user-detail">
      <header class="admin-user-detail__hero">
        <span>{{ record.realName.slice(0, 1) }}</span>
        <div>
          <div>
            <h2>{{ record.realName }}</h2>
            <HrStatusBadge
              :label="record.statusText"
              :tone="getAdminUserStatusTone(record.status)"
            />
          </div>
          <p>@{{ record.username }} · {{ record.roleName }}</p>
        </div>
      </header>
      <section class="admin-user-detail__section">
        <h3>账号信息</h3>
        <dl>
          <div>
            <dt><ShieldCheck :size="16" />系统角色</dt>
            <dd>{{ record.roleName }}</dd>
          </div>
          <div>
            <dt><Phone :size="16" />手机号</dt>
            <dd>{{ record.phone || '未提供' }}</dd>
          </div>
          <div>
            <dt><Mail :size="16" />邮箱</dt>
            <dd>{{ record.email || '未提供' }}</dd>
          </div>
        </dl>
      </section>
      <section class="admin-user-detail__section">
        <h3>使用记录</h3>
        <dl>
          <div>
            <dt><CalendarDays :size="16" />最近登录</dt>
            <dd>{{ formatAdminDateTime(record.lastLoginAt) }}</dd>
          </div>
          <div>
            <dt><CalendarDays :size="16" />创建时间</dt>
            <dd>{{ formatAdminDateTime(record.createdAt) }}</dd>
          </div>
          <div>
            <dt><CalendarDays :size="16" />最近更新</dt>
            <dd>{{ formatAdminDateTime(record.updatedAt) }}</dd>
          </div>
        </dl>
      </section>
      <p v-if="record.id === currentUserId" class="admin-user-detail__self-note">
        当前登录账号不能在此处禁用、修改角色或重置密码。
      </p>
    </div>
    <template v-if="record && !loading && !error" #footer>
      <div class="admin-user-detail__footer">
        <el-button @click="emit('edit', record)">编辑资料</el-button>
        <div>
          <el-button
            :icon="UserRoundCog"
            :disabled="record.id === currentUserId"
            @click="emit('role', record)"
          >
            修改角色
          </el-button>
          <el-button
            :icon="KeyRound"
            :disabled="record.id === currentUserId"
            @click="emit('password', record)"
          >
            重置密码
          </el-button>
          <el-button
            :type="record.status === 1 ? 'danger' : 'primary'"
            plain
            :disabled="record.id === currentUserId"
            @click="emit('status', record)"
          >
            {{ record.status === 1 ? '禁用账号' : '启用账号' }}
          </el-button>
        </div>
      </div>
    </template>
  </el-drawer>
</template>

<style scoped lang="scss">
.admin-user-detail {
  display: grid;
  gap: var(--rs-space-6);
}
.admin-user-detail__hero {
  display: flex;
  align-items: center;
  gap: var(--rs-space-3);
  padding-bottom: var(--rs-space-6);
  border-bottom: 1px solid var(--rs-border-default);
}
.admin-user-detail__hero > span {
  display: grid;
  width: 48px;
  height: 48px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-selected);
  color: var(--rs-blue-700);
  font-size: 18px;
  font-weight: 700;
}
.admin-user-detail__hero h2,
.admin-user-detail__hero p,
.admin-user-detail__section h3,
.admin-user-detail__section dl {
  margin: 0;
}
.admin-user-detail__hero > div,
.admin-user-detail__hero > div > div {
  min-width: 0;
}
.admin-user-detail__hero > div > div {
  display: flex;
  align-items: center;
  gap: var(--rs-space-2);
}
.admin-user-detail__hero h2 {
  font-size: 18px;
}
.admin-user-detail__hero p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}
.admin-user-detail__section {
  display: grid;
  gap: var(--rs-space-3);
}
.admin-user-detail__section h3 {
  font-size: 14px;
}
.admin-user-detail__section dl {
  display: grid;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
.admin-user-detail__section dl > div {
  display: grid;
  grid-template-columns: 132px minmax(0, 1fr);
  align-items: center;
  min-height: 48px;
  padding: 0 var(--rs-space-4);
}
.admin-user-detail__section dl > div + div {
  border-top: 1px solid var(--rs-border-default);
}
.admin-user-detail__section dt {
  display: flex;
  align-items: center;
  gap: var(--rs-space-2);
  color: var(--rs-text-secondary);
}
.admin-user-detail__section dd {
  margin: 0;
  overflow-wrap: anywhere;
  color: var(--rs-text-primary);
  font-variant-numeric: tabular-nums;
}
.admin-user-detail__self-note {
  margin: 0;
  padding: var(--rs-space-3);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
  color: var(--rs-text-secondary);
  font-size: 12px;
}
.admin-user-detail__footer,
.admin-user-detail__footer > div {
  display: flex;
  align-items: center;
  gap: var(--rs-space-2);
}
.admin-user-detail__footer {
  justify-content: space-between;
}
</style>
