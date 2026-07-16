import { describe, expect, it } from 'vitest'
import { adaptEmployeePage } from '@/api/employees'
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
})
