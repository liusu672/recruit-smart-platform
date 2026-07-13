export type UserRole = 'ADMIN' | 'HR' | 'INTERVIEWER' | 'CANDIDATE'

export interface AuthUser {
  id: string
  username: string
  name: string
  role: UserRole
  roleName?: string
  department?: string
  email?: string
  phone?: string
  status?: number
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginUserInfo {
  userId: number
  username: string
  realName: string | null
  phone: string | null
  email: string | null
  roleCode: string | null
  roleName: string | null
  roleId: number | null
  status: number
}

export interface LoginResponse {
  token: string
  tokenType: string
  userInfo: LoginUserInfo
}

export interface LoginPayload {
  token: string
  tokenType?: string
  user: AuthUser
}
