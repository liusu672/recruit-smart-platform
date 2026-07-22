import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { computed, reactive, ref } from 'vue'

import {
  getEmployeeById,
  getEmployees,
  updateEmployeeRiskData,
  updateEmployeeStatus,
  type EmployeeRiskDataUpdateRequest,
} from '@/api/employees'
import { assessTurnoverRisk } from '@/api/ai'
import { getEmployeeStatusText } from '@/config/employees'
import { getDemoEmployeePage, initialDemoEmployees } from '@/config/demoEmployees'
import type { EmployeeQuery, EmployeeRecord, EmployeeStatusUpdateRequest } from '@/types/employee'

function cloneDemoEmployees() {
  return initialDemoEmployees.map((record) => ({ ...record }))
}

export function useEmployeeDirectory() {
  const queryClient = useQueryClient()
  const demoMode = ref(false)
  const demoRecords = ref<EmployeeRecord[]>(cloneDemoEmployees())
  const selectedId = ref<number | null>(null)
  const query = reactive<EmployeeQuery>({
    keyword: '',
    department: '',
    status: '',
    page: 1,
    pageSize: 10,
  })
  const listQuery = useQuery({
    queryKey: computed(() => [
      'employees',
      demoMode.value ? 'demo' : 'api',
      query.keyword,
      query.department,
      query.status,
      query.page,
      query.pageSize,
    ]),
    queryFn: () =>
      demoMode.value
        ? Promise.resolve(getDemoEmployeePage(demoRecords.value, { ...query }))
        : getEmployees({ ...query }),
  })
  const detailQuery = useQuery({
    queryKey: computed(() => [
      'employee-detail',
      demoMode.value ? 'demo' : 'api',
      selectedId.value,
    ]),
    enabled: computed(() => selectedId.value !== null),
    queryFn: async () => {
      if (selectedId.value === null) throw new Error('尚未选择员工档案')
      if (!demoMode.value) return getEmployeeById(selectedId.value)
      const record = demoRecords.value.find((item) => item.id === selectedId.value)
      if (!record) throw new Error('演示员工档案不存在')
      return { ...record }
    },
  })

  async function refresh() {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['employees'] }),
      queryClient.invalidateQueries({ queryKey: ['employee-detail'] }),
    ])
  }

  const statusMutation = useMutation({
    mutationFn: async ({ id, data }: { id: number; data: EmployeeStatusUpdateRequest }) => {
      if (!demoMode.value) return updateEmployeeStatus(id, data)
      const record = demoRecords.value.find((item) => item.id === id)
      if (!record) throw new Error('演示员工档案不存在')
      record.status = data.status
      record.statusText = getEmployeeStatusText(data.status)
      record.updatedAt = new Date().toISOString()
    },
    onSuccess: refresh,
  })

  const riskDataMutation = useMutation({
    mutationFn: async ({ id, data }: { id: number; data: EmployeeRiskDataUpdateRequest }) => {
      if (!demoMode.value) return updateEmployeeRiskData(id, data)
      const record = demoRecords.value.find((item) => item.id === id)
      if (!record) throw new Error('演示员工档案不存在')
      Object.assign(record, {
        performanceSummary: data.performanceSummary,
        performanceScore: data.performanceScore,
        attendanceSummary: data.attendanceSummary,
        attendanceScore: data.attendanceScore,
        satisfactionFeedback: data.satisfactionFeedback,
        satisfactionScore: data.satisfactionScore,
        turnoverRiskLevel: null,
        riskAssessedAt: null,
        updatedAt: new Date().toISOString(),
      })
    },
    onSuccess: refresh,
  })

  const riskMutation = useMutation({ mutationFn: assessTurnoverRisk })

  return {
    query,
    demoMode,
    selectedId,
    listQuery,
    detailQuery,
    statusMutation,
    riskDataMutation,
    riskMutation,
    applyFilters: (filters: Pick<EmployeeQuery, 'keyword' | 'department' | 'status'>) =>
      Object.assign(query, filters, { page: 1 }),
    resetFilters: () => Object.assign(query, { keyword: '', department: '', status: '', page: 1 }),
    useDemoData: () => {
      demoMode.value = true
      selectedId.value = null
      query.page = 1
    },
    useApiData: () => {
      demoMode.value = false
      selectedId.value = null
      query.page = 1
    },
    openDetail: (id: number) => {
      selectedId.value = id
    },
    closeDetail: () => {
      selectedId.value = null
    },
  }
}
