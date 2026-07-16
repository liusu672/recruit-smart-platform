import type { PagedData } from '@/types/api'

export type EmployeeStatus = 'PROBATION' | 'ACTIVE' | 'LEFT'
export type TurnoverRiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | null

export interface EmployeeRecord {
  id: number
  userId: number | null
  candidateId: number
  onboardingId: number | null
  employeeNo: string
  name: string
  phone: string | null
  email: string | null
  department: string
  position: string
  entryDate: string
  status: EmployeeStatus
  statusText: string
  performanceSummary: string | null
  attendanceSummary: string | null
  satisfactionFeedback: string | null
  turnoverRiskLevel: TurnoverRiskLevel
  riskAssessedAt: string | null
  createdAt: string
  updatedAt: string
}

export interface EmployeeQuery {
  keyword: string
  department: string
  status: EmployeeStatus | ''
  page: number
  pageSize: number
}

export interface EmployeePageResponse {
  total: number
  records: EmployeeRecord[]
}

export type EmployeePagedData = PagedData<EmployeeRecord>
