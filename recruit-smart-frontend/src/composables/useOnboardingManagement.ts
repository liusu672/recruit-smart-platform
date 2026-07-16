import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { computed, reactive, ref } from 'vue'

import {
  completeOnboarding,
  getOnboardingById,
  getOnboardings,
  reviewOnboardingMaterial,
  startOnboardingReview,
} from '@/api/onboardings'
import {
  completeDemoOnboarding,
  getDemoOnboardingPage,
  initialDemoOnboardings,
  reviewDemoOnboardingMaterial,
  startDemoOnboardingReview,
} from '@/config/demoOnboardings'
import type {
  CompleteOnboardingRequest,
  MaterialReviewRequest,
  OnboardingActionRequest,
  OnboardingQuery,
  OnboardingRecord,
} from '@/types/onboarding'

function cloneRecord(record: OnboardingRecord): OnboardingRecord {
  return { ...record, timeline: record.timeline.map((event) => ({ ...event })) }
}

export function useOnboardingManagement() {
  const queryClient = useQueryClient()
  const demoMode = ref(false)
  const demoRecords = ref(initialDemoOnboardings.map(cloneRecord))
  const selectedId = ref<number | null>(null)
  const query = reactive<OnboardingQuery>({ keyword: '', status: '', page: 1, pageSize: 10 })

  const listQuery = useQuery({
    queryKey: computed(() => [
      'onboardings',
      demoMode.value ? 'demo' : 'api',
      query.keyword,
      query.status,
      query.page,
      query.pageSize,
    ]),
    queryFn: () =>
      demoMode.value
        ? Promise.resolve(getDemoOnboardingPage(demoRecords.value, { ...query }))
        : getOnboardings({ ...query }),
  })
  const detailQuery = useQuery({
    queryKey: computed(() => [
      'onboarding-detail',
      demoMode.value ? 'demo' : 'api',
      selectedId.value,
    ]),
    enabled: computed(() => selectedId.value !== null),
    queryFn: async () => {
      if (selectedId.value === null) throw new Error('尚未选择入职记录')
      if (!demoMode.value) return getOnboardingById(selectedId.value)
      const record = demoRecords.value.find((item) => item.id === selectedId.value)
      if (!record) throw new Error('演示入职记录不存在')
      return cloneRecord(record)
    },
  })

  function updateDemoRecord(id: number, updater: (record: OnboardingRecord) => OnboardingRecord) {
    const index = demoRecords.value.findIndex((item) => item.id === id)
    if (index < 0) throw new Error('演示入职记录不存在')
    demoRecords.value[index] = updater(demoRecords.value[index]!)
  }
  async function refresh() {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['onboardings'] }),
      queryClient.invalidateQueries({ queryKey: ['onboarding-detail'] }),
    ])
  }
  const startReviewMutation = useMutation({
    mutationFn: async ({ id, data }: { id: number; data: OnboardingActionRequest }) =>
      demoMode.value
        ? updateDemoRecord(id, (record) => startDemoOnboardingReview(record, data))
        : startOnboardingReview(id, data),
    onSuccess: refresh,
  })
  const materialReviewMutation = useMutation({
    mutationFn: async ({ id, data }: { id: number; data: MaterialReviewRequest }) =>
      demoMode.value
        ? updateDemoRecord(id, (record) => reviewDemoOnboardingMaterial(record, data))
        : reviewOnboardingMaterial(id, data),
    onSuccess: refresh,
  })
  const completeMutation = useMutation({
    mutationFn: async ({ id, data }: { id: number; data: CompleteOnboardingRequest }) =>
      demoMode.value
        ? updateDemoRecord(id, (record) => completeDemoOnboarding(record, data))
        : completeOnboarding(id, data),
    onSuccess: refresh,
  })

  return {
    query,
    demoMode,
    selectedId,
    listQuery,
    detailQuery,
    startReviewMutation,
    materialReviewMutation,
    completeMutation,
    applyFilters: (filters: Pick<OnboardingQuery, 'keyword' | 'status'>) =>
      Object.assign(query, filters, { page: 1 }),
    resetFilters: () => Object.assign(query, { keyword: '', status: '', page: 1 }),
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
