import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { computed, reactive, ref } from 'vue'

import { assessTurnoverRisk, listTurnoverRiskHistory } from '@/api/ai'
import {
  confirmEmployeeBehaviorRecord,
  createEmployeeBehaviorRecord,
  getEmployeeById,
  getEmployees,
  listEmployeeBehaviorRecords,
  updateEmployeeStatus,
} from '@/api/employees'
import { getEmployeeStatusText } from '@/config/employees'
import { getDemoEmployeePage, initialDemoEmployees } from '@/config/demoEmployees'
import type { TurnoverRiskHistoryResponse, TurnoverRiskResponse } from '@/types/ai'
import type {
  EmployeeBehaviorRecord,
  EmployeeBehaviorSaveRequest,
  EmployeeQuery,
  EmployeeRecord,
  EmployeeStatusUpdateRequest,
} from '@/types/employee'

function cloneDemoEmployees() {
  return initialDemoEmployees.map((record) => ({ ...record }))
}

function clampScore(value: number | null, offset: number) {
  if (value === null) return null
  return Math.min(100, Math.max(0, value + offset))
}

function makeDemoBehaviorRecord(
  employee: EmployeeRecord,
  index: number,
  periodStart: string,
  periodEnd: string,
  offset: number,
): EmployeeBehaviorRecord {
  return {
    id: employee.id * 100 + index,
    employeeId: employee.id,
    periodStart,
    periodEnd,
    performanceScore: clampScore(employee.performanceScore, offset),
    performanceSummary: employee.performanceSummary,
    taskCompletionRate: clampScore(employee.performanceScore, offset + 8),
    lateCount: Math.max(0, 2 - index),
    absenceDays: 0,
    leaveDays: index,
    overtimeHours: Math.max(0, 6 - index * 2),
    attendanceScore: clampScore(employee.attendanceScore, offset),
    attendanceSummary: employee.attendanceSummary,
    satisfactionScore: clampScore(employee.satisfactionScore, offset),
    feedbackText: employee.satisfactionFeedback,
    sourceType: 'HR_INPUT',
    recordStatus: 'CONFIRMED',
    createdBy: null,
    createdAt: `${periodEnd}T18:00:00`,
    updatedAt: `${periodEnd}T18:00:00`,
  }
}

function cloneDemoBehaviorRecords() {
  const periods = [
    { start: '2026-06-01', end: '2026-06-30', offset: 0 },
    { start: '2026-05-01', end: '2026-05-31', offset: -3 },
    { start: '2026-04-01', end: '2026-04-30', offset: -6 },
  ]
  return Object.fromEntries(
    initialDemoEmployees.map((employee) => [
      employee.id,
      periods.map((period, index) =>
        makeDemoBehaviorRecord(employee, index + 1, period.start, period.end, period.offset),
      ),
    ]),
  ) as Record<number, EmployeeBehaviorRecord[]>
}

function makeDemoRisk(employeeId: number, records: EmployeeBehaviorRecord[]): TurnoverRiskResponse {
  const latest = records[0]
  const satisfaction = latest?.satisfactionScore ?? 80
  const attendance = latest?.attendanceScore ?? 90
  const riskScore = Math.min(100, Math.max(0, 100 - Math.round((satisfaction + attendance) / 2)))
  const riskLevel = riskScore >= 55 ? 'HIGH' : riskScore >= 35 ? 'MEDIUM' : 'LOW'
  return {
    riskLevel,
    riskScore,
    summary: `基于员工 #${employeeId} 最近 ${records.length} 期行为记录生成的演示风险参考。`,
    riskReasons:
      riskLevel === 'LOW'
        ? ['近期绩效、考勤和满意度整体稳定']
        : ['满意度或考勤指标存在下降，需要 HR 进一步访谈确认'],
    suggestions:
      riskLevel === 'LOW'
        ? ['保持常规沟通和团队支持']
        : ['安排一次一对一沟通', '复核近期工作负荷和团队协作问题'],
    sentimentLabel: riskLevel === 'LOW' ? '稳定' : '需关注',
    sentimentRiskScore: riskScore,
    sentimentSummary:
      riskLevel === 'LOW'
        ? '访谈反馈未出现明显负向倾向。'
        : '访谈反馈存在压力或投入度下降信号。',
  }
}

export function useEmployeeDirectory() {
  const queryClient = useQueryClient()
  const demoMode = ref(false)
  const demoRecords = ref<EmployeeRecord[]>(cloneDemoEmployees())
  const demoBehaviorRecords = ref<Record<number, EmployeeBehaviorRecord[]>>(
    cloneDemoBehaviorRecords(),
  )
  const demoRiskHistory = ref<Record<number, TurnoverRiskHistoryResponse[]>>({})
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
  const behaviorRecordsQuery = useQuery({
    queryKey: computed(() => [
      'employee-behavior-records',
      demoMode.value ? 'demo' : 'api',
      selectedId.value,
    ]),
    enabled: computed(() => selectedId.value !== null),
    queryFn: async () => {
      if (selectedId.value === null) throw new Error('尚未选择员工档案')
      if (!demoMode.value) return listEmployeeBehaviorRecords(selectedId.value)
      return demoBehaviorRecords.value[selectedId.value] ?? []
    },
  })
  const riskHistoryQuery = useQuery({
    queryKey: computed(() => [
      'employee-risk-history',
      demoMode.value ? 'demo' : 'api',
      selectedId.value,
    ]),
    enabled: computed(() => selectedId.value !== null),
    queryFn: async () => {
      if (selectedId.value === null) throw new Error('尚未选择员工档案')
      if (!demoMode.value) return listTurnoverRiskHistory(selectedId.value)
      return demoRiskHistory.value[selectedId.value] ?? []
    },
  })

  async function refresh() {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['employees'] }),
      queryClient.invalidateQueries({ queryKey: ['employee-detail'] }),
    ])
  }

  async function refreshBehaviorData() {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['employee-behavior-records'] }),
      queryClient.invalidateQueries({ queryKey: ['employee-risk-history'] }),
      refresh(),
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

  const behaviorRecordMutation = useMutation({
    mutationFn: async ({
      employeeId,
      data,
    }: {
      employeeId: number
      data: EmployeeBehaviorSaveRequest
    }) => {
      if (!demoMode.value) return createEmployeeBehaviorRecord(employeeId, data)
      const existing = demoBehaviorRecords.value[employeeId] ?? []
      const nextId = Math.max(employeeId * 100, ...existing.map((record) => record.id)) + 1
      const now = new Date().toISOString()
      const record: EmployeeBehaviorRecord = {
        ...data,
        id: nextId,
        employeeId,
        recordStatus: 'DRAFT',
        createdBy: null,
        createdAt: now,
        updatedAt: now,
      }
      demoBehaviorRecords.value = {
        ...demoBehaviorRecords.value,
        [employeeId]: [record, ...existing],
      }
      return nextId
    },
    onSuccess: refreshBehaviorData,
  })

  const confirmBehaviorRecordMutation = useMutation({
    mutationFn: async ({ employeeId, recordId }: { employeeId: number; recordId: number }) => {
      if (!demoMode.value) return confirmEmployeeBehaviorRecord(employeeId, recordId)
      const records = demoBehaviorRecords.value[employeeId] ?? []
      const record = records.find((item) => item.id === recordId)
      if (!record) throw new Error('演示行为记录不存在')
      record.recordStatus = 'CONFIRMED'
      record.updatedAt = new Date().toISOString()
    },
    onSuccess: refreshBehaviorData,
  })

  const riskMutation = useMutation({
    mutationFn: async (employeeId: number) => {
      if (!demoMode.value) return assessTurnoverRisk(employeeId)
      const confirmedRecords = (demoBehaviorRecords.value[employeeId] ?? []).filter(
        (record) => record.recordStatus === 'CONFIRMED',
      )
      if (confirmedRecords.length < 3) throw new Error('至少需要3期已确认的行为数据')
      return makeDemoRisk(employeeId, confirmedRecords)
    },
    onSuccess: async (risk, employeeId) => {
      if (demoMode.value) {
        const record = demoRecords.value.find((item) => item.id === employeeId)
        const confirmedRecords = (demoBehaviorRecords.value[employeeId] ?? []).filter(
          (item) => item.recordStatus === 'CONFIRMED',
        )
        const now = new Date().toISOString()
        if (record) {
          record.turnoverRiskLevel = risk.riskLevel
          record.riskAssessedAt = now
          record.updatedAt = now
        }
        const oldest = confirmedRecords[confirmedRecords.length - 1]
        const latest = confirmedRecords[0]
        const history: TurnoverRiskHistoryResponse = {
          ...risk,
          id: Date.now(),
          taskId: null,
          employeeId,
          periodStart: oldest?.periodStart ?? null,
          periodEnd: latest?.periodEnd ?? null,
          behaviorRecordIds: confirmedRecords.map((item) => item.id),
          source: 'DEMO',
          modelName: 'demo-rule',
          promptVersion: null,
          generatedAt: now,
        }
        demoRiskHistory.value = {
          ...demoRiskHistory.value,
          [employeeId]: [history, ...(demoRiskHistory.value[employeeId] ?? [])],
        }
      }
      await refreshBehaviorData()
    },
  })

  return {
    query,
    demoMode,
    selectedId,
    listQuery,
    detailQuery,
    behaviorRecordsQuery,
    riskHistoryQuery,
    statusMutation,
    behaviorRecordMutation,
    confirmBehaviorRecordMutation,
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
