import { http, unwrapVoidResult } from '@/api/http'
import type { Result } from '@/types/api'
import type { PasswordUpdateRequest, UserProfileUpdateRequest } from '@/types/user'

export { getCurrentUser } from '@/api/auth'

export function updateCurrentUser(data: UserProfileUpdateRequest): Promise<void> {
  return unwrapVoidResult(http.put<Result<null>>('/users/me', data))
}

export function updateCurrentPassword(data: PasswordUpdateRequest): Promise<void> {
  return unwrapVoidResult(http.put<Result<null>>('/users/me/password', data))
}
