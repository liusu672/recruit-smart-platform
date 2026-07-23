import { shallowMount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import EmployeeBehaviorRecordsPanel from '@/components/employees/EmployeeBehaviorRecordsPanel.vue'
import EmployeeDetailDrawer from '@/components/employees/EmployeeDetailDrawer.vue'
import EmployeeRiskHistoryPanel from '@/components/employees/EmployeeRiskHistoryPanel.vue'
import { initialDemoEmployees } from '@/config/demoEmployees'
import type { TurnoverRiskHistoryResponse } from '@/types/ai'
import type {
  EmployeeBehaviorRecord,
  EmployeeBehaviorSaveRequest,
  EmployeeRecord,
} from '@/types/employee'

const stubs = {
  'el-drawer': { template: '<section><slot /></section>' },
  'el-skeleton': { template: '<div />' },
  'el-result': { template: '<div />' },
  'el-select': { template: '<div><slot /></div>' },
  'el-option': { template: '<span />' },
  'el-button': {
    props: ['disabled', 'nativeType'],
    template: '<button :disabled="disabled" :type="nativeType || \'button\'"><slot /></button>',
  },
}

function makeBehaviorRecords(count = 3): EmployeeBehaviorRecord[] {
  return Array.from({ length: count }, (_, index) => ({
    id: 7000 + index,
    employeeId: 1001,
    periodStart: `2026-0${index + 4}-01`,
    periodEnd: `2026-0${index + 4}-28`,
    performanceScore: 80 + index,
    performanceSummary: '绩效稳定',
    taskCompletionRate: 90,
    lateCount: 0,
    absenceDays: 0,
    leaveDays: 1,
    overtimeHours: 6,
    attendanceScore: 92,
    attendanceSummary: '考勤正常',
    satisfactionScore: 85,
    feedbackText: '反馈稳定',
    sourceType: 'HR_INPUT',
    recordStatus: 'CONFIRMED',
    createdBy: null,
    createdAt: '2026-07-01T10:00:00',
    updatedAt: '2026-07-01T10:00:00',
  }))
}

const riskHistory: TurnoverRiskHistoryResponse[] = [
  {
    id: 1,
    taskId: 10,
    employeeId: 1001,
    riskLevel: 'LOW',
    riskScore: 24,
    summary: '风险较低',
    riskReasons: [],
    suggestions: ['保持沟通'],
    sentimentLabel: '稳定',
    sentimentRiskScore: 18,
    sentimentSummary: '反馈稳定',
    periodStart: '2026-04-01',
    periodEnd: '2026-06-30',
    behaviorRecordIds: [7000, 7001, 7002],
    source: 'LLM',
    modelName: 'deepseek-chat',
    promptVersion: 'turnover-risk-v2',
    generatedAt: '2026-07-20T18:00:00',
  },
]

function mountDrawer(
  record: EmployeeRecord = initialDemoEmployees[0]!,
  behaviorRecords = makeBehaviorRecords(),
) {
  return shallowMount(EmployeeDetailDrawer, {
    props: {
      visible: true,
      record,
      loading: false,
      error: null,
      updating: false,
      riskAnalysis: null,
      analyzingRisk: false,
      behaviorRecords,
      loadingBehaviorRecords: false,
      savingBehaviorRecord: false,
      confirmingBehaviorRecord: false,
      riskHistory,
      loadingRiskHistory: false,
    },
    global: { stubs },
  })
}

describe('EmployeeDetailDrawer behavior and risk panels', () => {
  it('passes behavior records and risk history into child panels', () => {
    const wrapper = mountDrawer()

    expect(wrapper.findComponent(EmployeeBehaviorRecordsPanel).props('records')).toHaveLength(3)
    expect(wrapper.findComponent(EmployeeRiskHistoryPanel).props('records')).toEqual(riskHistory)
  })

  it('emits behavior record save and confirm events from the behavior panel', async () => {
    const wrapper = mountDrawer()
    const payload: EmployeeBehaviorSaveRequest = {
      periodStart: '2026-07-01',
      periodEnd: '2026-07-31',
      performanceScore: 82,
      performanceSummary: '绩效稳定',
      taskCompletionRate: 90,
      lateCount: 0,
      absenceDays: 0,
      leaveDays: 1,
      overtimeHours: 8,
      attendanceScore: 94,
      attendanceSummary: '考勤正常',
      satisfactionScore: 88,
      feedbackText: '反馈稳定',
      sourceType: 'HR_INPUT',
    }

    wrapper.findComponent(EmployeeBehaviorRecordsPanel).vm.$emit('save', payload)
    wrapper.findComponent(EmployeeBehaviorRecordsPanel).vm.$emit('confirm', 9001)
    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('saveBehaviorRecord')?.[0]).toEqual([payload])
    expect(wrapper.emitted('confirmBehaviorRecord')?.[0]).toEqual([9001])
  })

  it('requires three confirmed behavior records before risk assessment', () => {
    const wrapper = mountDrawer(initialDemoEmployees[0]!, makeBehaviorRecords(2))

    const riskButton = wrapper
      .findAll('button')
      .find((button) => button.text().includes('重新评估风险'))

    expect(riskButton?.attributes('disabled')).toBeDefined()
    expect(wrapper.text()).toContain('至少需要 3 期已确认行为记录后才能评估。')
  })
})
