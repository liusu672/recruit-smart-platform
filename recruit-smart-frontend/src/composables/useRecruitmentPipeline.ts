import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { computed, reactive, ref } from 'vue'

import {
  getPipelineApplication,
  getPipelineApplications,
  reviewApplication,
  updateApplicationStatus,
} from '@/api/pipeline'
import {
  applyDemoScreeningDecision,
  applyDemoStatusUpdate,
  getDemoPipelinePage,
  initialDemoPipeline,
} from '@/config/demoPipeline'
import type {
  PipelineApplication,
  PipelineQuery,
  PipelineReviewRequest,
  PipelineStatusUpdateRequest,
} from '@/types/pipeline'

function cloneDemoPipeline() {
  return structuredClone(initialDemoPipeline)
}

export function useRecruitmentPipeline() {
  const queryClient = useQueryClient()
  const demoMode = ref(false)
  const demoRecords = ref<PipelineApplication[]>(cloneDemoPipeline())
  const selectedApplicationId = ref<number | null>(null)
  const query = reactive<PipelineQuery>({
    keyword: '',
    jobId: null,
    status: '',
    page: 1,
    pageSize: 20,
  })

  const pipelineQuery = useQuery({
    queryKey: computed(() => [
      'pipeline',
      demoMode.value ? 'demo' : 'api',
      query.keyword,
      query.jobId,
      query.status,
      query.page,
      query.pageSize,
    ]),
    queryFn: () =>
      demoMode.value
        ? Promise.resolve(getDemoPipelinePage(demoRecords.value, { ...query }))
        : getPipelineApplications({ ...query }),
  })

  const detailQuery = useQuery({
    queryKey: computed(() => [
      'pipeline-detail',
      demoMode.value ? 'demo' : 'api',
      selectedApplicationId.value,
    ]),
    enabled: computed(() => selectedApplicationId.value !== null),
    queryFn: async () => {
      const id = selectedApplicationId.value
      if (id === null) throw new Error('尚未选择投递记录')
      if (!demoMode.value) return getPipelineApplication(id)

      const application = demoRecords.value.find((item) => item.id === id)
      if (!application) throw new Error('演示投递记录不存在')
      return structuredClone(application)
    },
  })

  async function invalidatePipeline() {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['pipeline'] }),
      queryClient.invalidateQueries({ queryKey: ['pipeline-detail'] }),
    ])
  }

  const statusMutation = useMutation({
    mutationFn: async ({ id, data }: { id: number; data: PipelineStatusUpdateRequest }) => {
      if (!demoMode.value) return updateApplicationStatus(id, data)
      const index = demoRecords.value.findIndex((item) => item.id === id)
      if (index < 0) throw new Error('演示投递记录不存在')
      demoRecords.value[index] = applyDemoStatusUpdate(demoRecords.value[index]!, data)
    },
    onSuccess: invalidatePipeline,
  })

  const reviewMutation = useMutation({
    mutationFn: async ({ id, data }: { id: number; data: PipelineReviewRequest }) => {
      if (!demoMode.value) return reviewApplication(id, data)
      const index = demoRecords.value.findIndex((item) => item.id === id)
      if (index < 0) throw new Error('演示投递记录不存在')
      demoRecords.value[index] = applyDemoScreeningDecision(demoRecords.value[index]!, data)
    },
    onSuccess: invalidatePipeline,
  })

  function applyFilters(filters: Pick<PipelineQuery, 'keyword' | 'jobId' | 'status'>) {
    Object.assign(query, filters, { page: 1 })
  }

  function resetFilters() {
    Object.assign(query, { keyword: '', jobId: null, status: '', page: 1 })
  }

  function useDemoData() {
    demoMode.value = true
    query.page = 1
    selectedApplicationId.value = null
  }

  function useApiData() {
    demoMode.value = false
    query.page = 1
    selectedApplicationId.value = null
  }

  function selectApplication(id: number) {
    selectedApplicationId.value = id
  }

  function closeDetail() {
    selectedApplicationId.value = null
  }

  return {
    query,
    demoMode,
    selectedApplicationId,
    pipelineQuery,
    detailQuery,
    statusMutation,
    reviewMutation,
    applyFilters,
    resetFilters,
    useDemoData,
    useApiData,
    selectApplication,
    closeDetail,
  }
}
