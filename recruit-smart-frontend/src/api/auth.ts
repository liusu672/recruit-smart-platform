import { http, unwrapResult } from '@/api/http'
import { ApiError } from '@/types/api'
import type {
  CandidateRegisterRequest,
  LoginPayload,
  LoginRequest,
  LoginResponse,
  UserRole,
} from '@/types/auth'
import type { AuthUser } from '@/types/auth'

export function normalizeRole(roleCode: string | null | undefined): UserRole {
  if (
    roleCode === 'ADMIN' ||
    roleCode === 'HR' ||
    roleCode === 'INTERVIEWER' ||
    roleCode === 'CANDIDATE'
  ) {
    return roleCode
  }

  // 未知角色不能默认获得 HR 权限，角色配置异常时直接拒绝建立前端会话。
  throw new ApiError(403, '账号角色未配置，请联系管理员。')
}

export async function login(request: LoginRequest): Promise<LoginPayload> {
  const response = await unwrapResult<LoginResponse>(http.post('/auth/login', request))
  return toLoginPayload(response)
}

export async function register(request: CandidateRegisterRequest): Promise<LoginPayload> {
  const response = await unwrapResult<LoginResponse>(http.post('/auth/register', request))
  return toLoginPayload(response)
}

export function toLoginPayload(response: LoginResponse): LoginPayload {
  const userInfo = response.userInfo
  const user: AuthUser = {
    id: String(userInfo.userId),
    username: userInfo.username,
    name: userInfo.realName || userInfo.username,
    role: normalizeRole(userInfo.roleCode),
    status: userInfo.status,
  }

  if (userInfo.roleName) {
    user.roleName = userInfo.roleName
    user.department = userInfo.roleName
  }

  if (userInfo.email) {
    user.email = userInfo.email
  }

  if (userInfo.phone) {
    user.phone = userInfo.phone
  }

  return {
    token: response.token,
    tokenType: response.tokenType,
    user,
  }
}
