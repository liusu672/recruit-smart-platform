import { afterEach, describe, expect, it, vi } from 'vitest'
import { adaptEmployeePage, updateEmployeeRiskData, updateEmployeeStatus } from '@/api/employees'
import { http } from '@/api/http'
import { getDemoEmployeePage, initialDemoEmployees } from '@/config/demoEmployees'

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

  it('calls the backend employee risk data endpoint', async () => {
    const put = vi.spyOn(http, 'put').mockResolvedValue({
      data: { code: 200, message: 'success', data: null },
    })
    const data = {
      performanceSummary: '季度目标完成稳定',
      performanceScore: 82,
      attendanceSummary: '近 30 天无异常考勤',
      attendanceScore: 94,
      satisfactionFeedback: '访谈反馈稳定',
      satisfactionScore: 88,
    }

    await updateEmployeeRiskData(1001, data)

    expect(put).toHaveBeenCalledWith('/employees/1001/risk-data', data)
  })

  afterEach(() => vi.restoreAllMocks())
})
