export interface CurrentUser {
  userId: number
  username: string
  realName: string | null
  phone: string | null
  email: string | null
  roleCode: string
  roleName: string | null
  roleId: number | null
  status: number
}

export interface UserProfileUpdateRequest {
  realName: string
  phone: string
  email: string
}

export interface PasswordUpdateRequest {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}
