import type { AdminUserStatus } from '@/types/adminUser'

export const adminUserStatusOptions: Array<{ label: string; value: AdminUserStatus }> = [
  { label: '已启用', value: 1 },
  { label: '已禁用', value: 0 },
]

export function getAdminUserStatusTone(status: AdminUserStatus) {
  return status === 1 ? ('success' as const) : ('neutral' as const)
}

export function formatAdminDateTime(value: string | null) {
  if (!value) return '从未登录'
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(new Date(value))
}
