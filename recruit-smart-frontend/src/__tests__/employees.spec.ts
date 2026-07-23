import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  adaptEmployeePage,
  confirmEmployeeBehaviorRecord,
  createEmployeeBehaviorRecord,
  updateEmployeeStatus,
} from '@/api/employees'
import { http } from '@/api/http'
import { getDemoEmployeePage, initialDemoEmployees } from '@/config/demoEmployees'
import type { EmployeeBehaviorSaveRequest } from '@/types/employee'

describe('employee directory', () => {
  it('maps employee records to the shared pagination contract', () => {
    const employee = initialDemoEmployees[0]!
    expect(adaptEmployeePage({ total: 1, records: [employee] }, 1, 10)).toEqual({
      items: [employee],
      page: 1,
      pageSize: 10,
      total: 1,
    })
  })

  it('filters by keyword, department and employment status', () => {
    const page = getDemoEmployeePage(initialDemoEmployees, {
      keyword: '沈清和',
      department: '产品部',
      status: 'ACTIVE',
      page: 1,
      pageSize: 10,
    })
    expect(page.total).toBe(1)
    expect(page.items[0]?.employeeNo).toBe('EMP20260418007')
  })

  it('keeps AI risk as nullable reference data', () => {
    const unassessed = initialDemoEmployees.find((item) => item.turnoverRiskLevel === null)
    expect(unassessed).toMatchObject({ riskAssessedAt: null, status: 'ACTIVE' })
  })

  it('keeps demo employee risk scores as nullable reference data', () => {
    expect(initialDemoEmployees[0]).toMatchObject({
      performanceScore: expect.any(Number),
      attendanceScore: expect.any(Number),
      satisfactionScore: expect.any(Number),
    })
  })

  it('calls the backend employee status endpoint', async () => {
    const put = vi.spyOn(http, 'put').mockResolvedValue({
      data: { code: 200, message: 'success', data: null },
    })

    await updateEmployeeStatus(1001, { status: 'ACTIVE' })

    expect(put).toHaveBeenCalledWith('/employees/1001/status', { status: 'ACTIVE' })
  })

  it('calls the backend employee behavior record endpoints', async () => {
    const post = vi.spyOn(http, 'post').mockResolvedValue({
      data: { code: 200, message: 'success', data: 9001 },
    })
    const data: EmployeeBehaviorSaveRequest = {
      periodStart: '2026-07-01',
      periodEnd: '2026-07-31',
      performanceSummary: '季度目标完成稳定',
      performanceScore: 82,
      taskCompletionRate: 91,
      lateCount: 0,
      absenceDays: 0,
      leaveDays: 1,
      overtimeHours: 8,
      attendanceSummary: '近 30 天无异常考勤',
      attendanceScore: 94,
      satisfactionScore: 88,
      feedbackText: '访谈反馈稳定',
      sourceType: 'HR_INPUT',
    }

    await createEmployeeBehaviorRecord(1001, data)
    await confirmEmployeeBehaviorRecord(1001, 9001)

    expect(post).toHaveBeenNthCalledWith(1, '/employees/1001/behavior-records', data)
    expect(post).toHaveBeenNthCalledWith(2, '/employees/1001/behavior-records/9001/confirm')
  })

  afterEach(() => vi.restoreAllMocks())
})
