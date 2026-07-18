import type { UserRole } from '@/types/auth'
import type { PagedData } from '@/types/api'

export type AdminUserStatus = 0 | 1

export interface AdminRoleOption {
  id: number
  roleCode: UserRole
  roleName: string
  description: string | null
}

export interface AdminUserRecord {
  id: number
  username: string
  realName: string
  phone: string | null
  email: string | null
  roleId: number
  roleCode: UserRole
  roleName: string
  status: AdminUserStatus
  statusText: string
  lastLoginAt: string | null
  createdAt: string
  updatedAt: string
}

export interface AdminUserPageResponse {
  total: number
  records: AdminUserRecord[]
}

export interface AdminUserQuery {
  page: number
  pageSize: number
  keyword: string
  roleId: number | null
  status: AdminUserStatus | ''
}

export type AdminUserPagedData = PagedData<AdminUserRecord>

export interface AdminUserCreateRequest {
  username: string
  password: string
  realName: string
  phone: string
  email: string
  roleId: number
}

export interface AdminUserUpdateRequest {
  realName: string
  phone: string
  email: string
}

export interface AdminUserStatusUpdateRequest {
  status: AdminUserStatus
}

export interface AdminUserRoleUpdateRequest {
  roleId: number
}

export interface AdminPasswordResetRequest {
  newPassword: string
  confirmPassword: string
}
