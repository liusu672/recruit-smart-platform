import { http, unwrapResult, unwrapVoidResult } from '@/api/http'
import type { Result } from '@/types/api'
import type {
  EmployeePagedData,
  EmployeePageResponse,
  EmployeeQuery,
  EmployeeRecord,
  EmployeeStatusUpdateRequest,
} from '@/types/employee'

export function adaptEmployeePage(
  source: EmployeePageResponse,
  page: number,
  pageSize: number,
): EmployeePagedData {
  return { items: source.records, page, pageSize, total: source.total }
}

// 员工档案后端暂缺 Controller/Service，页面先通过聚合契约消费只读档案。
export async function getEmployees(query: EmployeeQuery) {
  const params = {
    pageNum: query.page,
    pageSize: query.pageSize,
    ...(query.keyword ? { keyword: query.keyword } : {}),
    ...(query.department ? { department: query.department } : {}),
    ...(query.status ? { status: query.status } : {}),
  }
  const result = await unwrapResult(
    http.get<Result<EmployeePageResponse>>('/employees', { params }),
  )
  return adaptEmployeePage(result, query.page, query.pageSize)
}

export function getEmployeeById(id: number) {
  return unwrapResult(http.get<Result<EmployeeRecord>>(`/employees/${id}`))
}

export function updateEmployeeStatus(id: number, data: EmployeeStatusUpdateRequest) {
  return unwrapVoidResult(http.put<Result<null>>(`/employees/${id}/status`, data))
}

export interface EmployeeRiskDataUpdateRequest {
  performanceSummary: string
  performanceScore: number
  attendanceSummary: string
  attendanceScore: number
  satisfactionFeedback: string
  satisfactionScore: number
}

export function updateEmployeeRiskData(id: number, data: EmployeeRiskDataUpdateRequest) {
  return unwrapVoidResult(http.put<Result<null>>(`/employees/${id}/risk-data`, data))
}
