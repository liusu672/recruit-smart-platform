import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { computed, reactive, ref } from 'vue'

import {
  cancelInterview,
  createInterview,
  getInterviewers,
  updateInterview,
} from '@/api/interviews'
import {
  getPipelineApplication,
  getPipelineApplications,
  reviewApplication,
  updateApplicationStatus,
} from '@/api/pipeline'
import {
  applyDemoInterviewAssignment,
  applyDemoInterviewCancellation,
  applyDemoInterviewReassignment,
  applyDemoScreeningDecision,
  applyDemoStatusUpdate,
  demoInterviewerOptions,
  getDemoPipelinePage,
  initialDemoPipeline,
} from '@/config/demoPipeline'
import type { InterviewAssignmentRequest, InterviewUpdateRequest } from '@/types/interview'
import type {
  PipelineApplicationDetail,
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
  const demoRecords = ref<PipelineApplicationDetail[]>(cloneDemoPipeline())
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

  const interviewerQuery = useQuery({
    queryKey: ['interviewers'],
    queryFn: () => (demoMode.value ? Promise.resolve(demoInterviewerOptions) : getInterviewers()),
    enabled: false,
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

  const interviewAssignmentMutation = useMutation({
    mutationFn: async ({ id, data }: { id: number; data: InterviewAssignmentRequest }) => {
      if (!demoMode.value) return createInterview(data)
      const index = demoRecords.value.findIndex((item) => item.id === id)
      if (index < 0) throw new Error('演示投递记录不存在')
      demoRecords.value[index] = applyDemoInterviewAssignment(demoRecords.value[index]!, data)
    },
    onSuccess: invalidatePipeline,
  })

  const interviewReassignmentMutation = useMutation({
    mutationFn: async ({
      applicationId,
      interviewId,
      data,
    }: {
      applicationId: number
      interviewId: number
      data: InterviewUpdateRequest
    }) => {
      if (!demoMode.value) return updateInterview(interviewId, data)
      const index = demoRecords.value.findIndex((item) => item.id === applicationId)
      if (index < 0) throw new Error('演示投递记录不存在')
      demoRecords.value[index] = applyDemoInterviewReassignment(demoRecords.value[index]!, data)
    },
    onSuccess: invalidatePipeline,
  })

  const interviewCancellationMutation = useMutation({
    mutationFn: async ({
      applicationId,
      interviewId,
    }: {
      applicationId: number
      interviewId: number
    }) => {
      if (!demoMode.value) return cancelInterview(interviewId)
      const index = demoRecords.value.findIndex((item) => item.id === applicationId)
      if (index < 0) throw new Error('演示投递记录不存在')
      demoRecords.value[index] = applyDemoInterviewCancellation(demoRecords.value[index]!)
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
    interviewerQuery,
    statusMutation,
    reviewMutation,
    interviewAssignmentMutation,
    interviewReassignmentMutation,
    interviewCancellationMutation,
    applyFilters,
    resetFilters,
    useDemoData,
    useApiData,
    selectApplication,
    closeDetail,
  }
}
