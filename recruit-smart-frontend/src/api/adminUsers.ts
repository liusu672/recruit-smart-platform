import { http, unwrapResult, unwrapVoidResult } from '@/api/http'
import type { Result } from '@/types/api'
import type {
  AdminPasswordResetRequest,
  AdminRoleOption,
  AdminUserCreateRequest,
  AdminUserPagedData,
  AdminUserPageResponse,
  AdminUserQuery,
  AdminUserRecord,
  AdminUserRoleUpdateRequest,
  AdminUserStatusUpdateRequest,
  AdminUserUpdateRequest,
} from '@/types/adminUser'

export function adaptAdminUserPage(
  source: AdminUserPageResponse,
  page: number,
  pageSize: number,
): AdminUserPagedData {
  return {
    items: source.records,
    page,
    pageSize,
    total: source.total,
  }
}

export async function getAdminUsers(query: AdminUserQuery): Promise<AdminUserPagedData> {
  const params = {
    pageNum: query.page,
    pageSize: query.pageSize,
    ...(query.keyword ? { keyword: query.keyword } : {}),
    ...(query.roleId !== null ? { roleId: query.roleId } : {}),
    ...(query.status !== '' ? { status: query.status } : {}),
  }
  const page = await unwrapResult(
    http.get<Result<AdminUserPageResponse>>('/admin/users', { params }),
  )
  return adaptAdminUserPage(page, query.page, query.pageSize)
}

export function getAdminUserById(id: number): Promise<AdminUserRecord> {
  return unwrapResult(http.get<Result<AdminUserRecord>>(`/admin/users/${id}`))
}

export function getAdminRoles(): Promise<AdminRoleOption[]> {
  return unwrapResult(http.get<Result<AdminRoleOption[]>>('/admin/roles'))
}

export function createAdminUser(data: AdminUserCreateRequest): Promise<number> {
  return unwrapResult(http.post<Result<number>>('/admin/users', data))
}

export function updateAdminUser(id: number, data: AdminUserUpdateRequest): Promise<void> {
  return unwrapVoidResult(http.put<Result<null>>(`/admin/users/${id}`, data))
}

export function updateAdminUserStatus(
  id: number,
  data: AdminUserStatusUpdateRequest,
): Promise<void> {
  return unwrapVoidResult(http.put<Result<null>>(`/admin/users/${id}/status`, data))
}

export function updateAdminUserRole(id: number, data: AdminUserRoleUpdateRequest): Promise<void> {
  return unwrapVoidResult(http.put<Result<null>>(`/admin/users/${id}/role`, data))
}

export function resetAdminUserPassword(id: number, data: AdminPasswordResetRequest): Promise<void> {
  return unwrapVoidResult(http.put<Result<null>>(`/admin/users/${id}/reset-password`, data))
}
