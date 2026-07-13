import type { UserRole } from '@/types/auth'

export type LoginSurface = 'candidate' | 'management'
export type CandidateMode = 'password' | 'code'
export type ManagementRole = Exclude<UserRole, 'CANDIDATE'>

export interface LoginFormState {
  username: string
  password: string
  code: string
}

export interface LoginRoleCopy {
  heroKicker: string
  heroTitle: string
  heroDesc: string
  formTitle: string
  formDesc: string
  username: string
  password: string
}
