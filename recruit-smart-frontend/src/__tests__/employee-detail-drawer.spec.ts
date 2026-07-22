import { shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import EmployeeDetailDrawer from '@/components/employees/EmployeeDetailDrawer.vue'
import { initialDemoEmployees } from '@/config/demoEmployees'
import type { EmployeeRecord } from '@/types/employee'

const message = vi.hoisted(() => ({
  warning: vi.fn(),
}))

vi.mock('element-plus', async () => {
  const actual = await vi.importActual<typeof import('element-plus')>('element-plus')
  return {
    ...actual,
    ElMessage: message,
  }
})

const stubs = {
  'el-drawer': { template: '<section><slot /></section>' },
  'el-skeleton': { template: '<div />' },
  'el-result': { template: '<div />' },
  'el-select': { template: '<div><slot /></div>' },
  'el-option': { template: '<span />' },
  'el-button': {
    props: ['nativeType'],
    template: '<button :type="nativeType || \'button\'"><slot /></button>',
  },
  'el-input': {
    props: ['modelValue', 'type'],
    emits: ['update:modelValue'],
    template:
      '<textarea v-if="type === \'textarea\'" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />' +
      '<input v-else :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
  },
  'el-input-number': {
    props: ['modelValue'],
    emits: ['update:modelValue'],
    template:
      '<input type="number" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value === \'\' ? null : Number($event.target.value))" />',
  },
}

function mountDrawer(record: EmployeeRecord = initialDemoEmployees[0]!) {
  return shallowMount(EmployeeDetailDrawer, {
    props: {
      visible: true,
      record,
      loading: false,
      error: null,
      updating: false,
      riskAnalysis: null,
      analyzingRisk: false,
      savingRiskData: false,
    },
    global: { stubs },
  })
}

describe('EmployeeDetailDrawer risk data form', () => {
  beforeEach(() => {
    message.warning.mockReset()
  })

  it('initializes the risk data form from the selected employee record', () => {
    const wrapper = mountDrawer()

    const textareas = wrapper.findAll('textarea')
    const numberInputs = wrapper.findAll('input[type="number"]')

    expect(textareas[0]?.element.value).toBe(initialDemoEmployees[0]!.performanceSummary)
    expect(textareas[1]?.element.value).toBe(initialDemoEmployees[0]!.attendanceSummary)
    expect(textareas[2]?.element.value).toBe(initialDemoEmployees[0]!.satisfactionFeedback)
    expect(numberInputs.map((input) => (input.element as HTMLInputElement).value)).toEqual([
      '72',
      '95',
      '88',
    ])
  })

  it('does not emit save when required risk data is blank', async () => {
    const wrapper = mountDrawer({
      ...initialDemoEmployees[0]!,
      performanceSummary: '',
      performanceScore: 72,
    })

    await wrapper.find('form').trigger('submit')

    expect(wrapper.emitted('saveRiskData')).toBeUndefined()
    expect(message.warning).toHaveBeenCalledWith('请填写绩效摘要')
  })

  it('emits a complete risk data payload when the form is valid', async () => {
    const wrapper = mountDrawer()
    const textareas = wrapper.findAll('textarea')
    const numberInputs = wrapper.findAll('input[type="number"]')

    await textareas[0]!.setValue(' 新绩效摘要 ')
    await numberInputs[0]!.setValue('81')
    await textareas[1]!.setValue(' 新考勤摘要 ')
    await numberInputs[1]!.setValue('93')
    await textareas[2]!.setValue(' 新满意度反馈 ')
    await numberInputs[2]!.setValue('86')
    await wrapper.find('form').trigger('submit')

    expect(wrapper.emitted('saveRiskData')?.[0]).toEqual([
      {
        performanceSummary: '新绩效摘要',
        performanceScore: 81,
        attendanceSummary: '新考勤摘要',
        attendanceScore: 93,
        satisfactionFeedback: '新满意度反馈',
        satisfactionScore: 86,
      },
    ])
  })
})
