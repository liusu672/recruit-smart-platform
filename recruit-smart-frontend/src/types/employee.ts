import type { PagedData } from '@/types/api'

export type EmployeeStatus = 'PROBATION' | 'ACTIVE' | 'LEFT'
export type TurnoverRiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | null
export type BehaviorRecordStatus = 'DRAFT' | 'CONFIRMED'
export type BehaviorSourceType = 'HR_INPUT' | 'IMPORT' | 'SYSTEM'

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
  performanceScore: number | null
  attendanceSummary: string | null
  attendanceScore: number | null
  satisfactionFeedback: string | null
  satisfactionScore: number | null
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

export interface EmployeeStatusUpdateRequest {
  status: EmployeeStatus
}

export type EmployeePagedData = PagedData<EmployeeRecord>

export interface EmployeeBehaviorRecord {
  id: number
  employeeId: number
  periodStart: string
  periodEnd: string
  performanceScore: number | null
  performanceSummary: string | null
  taskCompletionRate: number | null
  lateCount: number | null
  absenceDays: number | null
  leaveDays: number | null
  overtimeHours: number | null
  attendanceScore: number | null
  attendanceSummary: string | null
  satisfactionScore: number | null
  feedbackText: string | null
  sourceType: BehaviorSourceType
  recordStatus: BehaviorRecordStatus
  createdBy: number | null
  createdAt: string | null
  updatedAt: string | null
}

export interface EmployeeBehaviorSaveRequest {
  periodStart: string
  periodEnd: string
  performanceScore: number | null
  performanceSummary: string | null
  taskCompletionRate: number | null
  lateCount: number | null
  absenceDays: number | null
  leaveDays: number | null
  overtimeHours: number | null
  attendanceScore: number | null
  attendanceSummary: string | null
  satisfactionScore: number | null
  feedbackText: string | null
  sourceType: BehaviorSourceType
}
