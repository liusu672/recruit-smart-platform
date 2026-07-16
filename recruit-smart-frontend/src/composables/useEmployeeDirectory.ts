import { useQuery } from '@tanstack/vue-query'
import { computed, reactive, ref } from 'vue'

import { getEmployeeById, getEmployees } from '@/api/employees'
import { getDemoEmployeePage, initialDemoEmployees } from '@/config/demoEmployees'
import type { EmployeeQuery } from '@/types/employee'

export function useEmployeeDirectory() {
  const demoMode = ref(false)
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
        ? Promise.resolve(getDemoEmployeePage(initialDemoEmployees, { ...query }))
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
      const record = initialDemoEmployees.find((item) => item.id === selectedId.value)
      if (!record) throw new Error('演示员工档案不存在')
      return { ...record }
    },
  })
  return {
    query,
    demoMode,
    selectedId,
    listQuery,
    detailQuery,
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
