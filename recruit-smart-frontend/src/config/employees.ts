import type { EmployeeStatus, TurnoverRiskLevel } from '@/types/employee'
import type { HrStatusTone } from '@/types/hr'

export const employeeStatusOptions: Array<{ label: string; value: EmployeeStatus }> = [
  { label: '试用期', value: 'PROBATION' },
  { label: '在职', value: 'ACTIVE' },
  { label: '已离职', value: 'LEFT' },
]

export function getEmployeeStatusText(status: EmployeeStatus) {
  return employeeStatusOptions.find((item) => item.value === status)?.label ?? status
}

export function getEmployeeStatusTone(status: EmployeeStatus): HrStatusTone {
  if (status === 'ACTIVE') return 'success'
  if (status === 'LEFT') return 'neutral'
  return 'warning'
}

export function getTurnoverRiskText(level: TurnoverRiskLevel) {
  if (level === 'HIGH') return '重点关注'
  if (level === 'MEDIUM') return '建议关注'
  if (level === 'LOW') return '暂不需关注'
  return '未评估'
}

export function getTurnoverRiskTone(level: TurnoverRiskLevel): HrStatusTone {
  if (level === 'HIGH') return 'danger'
  if (level === 'MEDIUM') return 'warning'
  if (level === 'LOW') return 'success'
  return 'info'
}
