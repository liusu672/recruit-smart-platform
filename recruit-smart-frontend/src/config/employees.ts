import type { EmployeeStatus, TurnoverRiskLevel } from '@/types/employee'

export const employeeStatusOptions: Array<{ label: string; value: EmployeeStatus }> = [
  { label: '试用期', value: 'PROBATION' },
  { label: '在职', value: 'ACTIVE' },
  { label: '已离职', value: 'LEFT' },
]

export function getEmployeeStatusText(status: EmployeeStatus) {
  return employeeStatusOptions.find((item) => item.value === status)?.label ?? status
}

export function getEmployeeStatusTone(status: EmployeeStatus) {
  if (status === 'ACTIVE') return 'success'
  if (status === 'LEFT') return 'danger'
  return 'warning'
}

export function getTurnoverRiskText(level: TurnoverRiskLevel) {
  if (level === 'HIGH') return '高风险'
  if (level === 'MEDIUM') return '中风险'
  if (level === 'LOW') return '低风险'
  return '未评估'
}

export function getTurnoverRiskTone(level: TurnoverRiskLevel) {
  if (level === 'HIGH') return 'danger'
  if (level === 'MEDIUM') return 'warning'
  if (level === 'LOW') return 'success'
  return 'info'
}
