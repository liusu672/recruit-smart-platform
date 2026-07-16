import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { computed, reactive, ref } from 'vue'

import {
  generateInterviewQuestions,
  getInterviewTasks,
  getInterviewWorkspace,
  saveInterviewFeedbackDraft,
  submitInterviewFeedback,
} from '@/api/interviews'
import {
  applyDemoInterviewDraft,
  applyDemoInterviewSubmit,
  getDemoInterviewPage,
  initialDemoInterviews,
} from '@/config/demoInterviews'
import type {
  InterviewFeedbackRequest,
  InterviewQuestionRequest,
  InterviewTaskQuery,
  InterviewWorkspace,
} from '@/types/interview'

function cloneDemoInterviews() {
  return structuredClone(initialDemoInterviews)
}

export function useInterviewWorkspace() {
  const queryClient = useQueryClient()
  const demoMode = ref(false)
  const demoRecords = ref<InterviewWorkspace[]>(cloneDemoInterviews())
  const selectedInterviewId = ref<number | null>(null)
  const query = reactive<InterviewTaskQuery>({
    keyword: '',
    status: '',
    feedbackState: '',
    page: 1,
    pageSize: 8,
  })

  const taskQuery = useQuery({
    queryKey: computed(() => [
      'interview-tasks',
      demoMode.value ? 'demo' : 'api',
      query.keyword,
      query.status,
      query.feedbackState,
      query.page,
      query.pageSize,
    ]),
    queryFn: () =>
      demoMode.value
        ? Promise.resolve(getDemoInterviewPage(demoRecords.value, { ...query }))
        : getInterviewTasks({ ...query }),
  })

  const workspaceQuery = useQuery({
    queryKey: computed(() => [
      'interview-workspace',
      demoMode.value ? 'demo' : 'api',
      selectedInterviewId.value,
    ]),
    enabled: computed(() => selectedInterviewId.value !== null),
    queryFn: async () => {
      const id = selectedInterviewId.value
      if (id === null) throw new Error('尚未选择面试任务')
      if (!demoMode.value) return getInterviewWorkspace(id)
      const interview = demoRecords.value.find((item) => item.id === id)
      if (!interview) throw new Error('演示面试任务不存在')
      return structuredClone(interview)
    },
  })

  async function invalidateInterviews() {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['interview-tasks'] }),
      queryClient.invalidateQueries({ queryKey: ['interview-workspace'] }),
    ])
  }

  function updateDemoInterview(
    id: number,
    updater: (interview: InterviewWorkspace) => InterviewWorkspace,
  ) {
    const index = demoRecords.value.findIndex((item) => item.id === id)
    if (index < 0) throw new Error('演示面试任务不存在')
    demoRecords.value[index] = updater(demoRecords.value[index]!)
  }

  const draftMutation = useMutation({
    mutationFn: async ({ id, data }: { id: number; data: InterviewFeedbackRequest }) => {
      if (!demoMode.value) return saveInterviewFeedbackDraft(id, data)
      updateDemoInterview(id, (interview) => applyDemoInterviewDraft(interview, data))
    },
    onSuccess: invalidateInterviews,
  })

  const submitMutation = useMutation({
    mutationFn: async ({ id, data }: { id: number; data: InterviewFeedbackRequest }) => {
      if (!demoMode.value) return submitInterviewFeedback(id, data)
      updateDemoInterview(id, (interview) => applyDemoInterviewSubmit(interview, data))
    },
    onSuccess: invalidateInterviews,
  })

  const questionMutation = useMutation({
    mutationFn: async ({ id, data }: { id: number; data: InterviewQuestionRequest }) => {
      if (!demoMode.value) return generateInterviewQuestions(id, data)
      const interview = demoRecords.value.find((item) => item.id === id)
      if (!interview) throw new Error('演示面试任务不存在')
      const focus = data.focus.trim()
      if (!focus) throw new Error('请先填写需要追问的主题')
      return [
        {
          id: `${id}-manual-${Date.now()}`,
          category: '临场追问',
          question: `请结合真实项目说明“${focus}”中的关键判断、执行过程与最终结果。`,
          source: 'MANUAL' as const,
        },
      ]
    },
  })

  function applyFilters(filters: Pick<InterviewTaskQuery, 'keyword' | 'status' | 'feedbackState'>) {
    Object.assign(query, filters, { page: 1 })
  }

  function resetFilters() {
    Object.assign(query, { keyword: '', status: '', feedbackState: '', page: 1 })
  }

  function useDemoData() {
    demoMode.value = true
    query.page = 1
    selectedInterviewId.value = initialDemoInterviews[0]?.id ?? null
  }

  function useApiData() {
    demoMode.value = false
    query.page = 1
    selectedInterviewId.value = null
  }

  return {
    query,
    demoMode,
    selectedInterviewId,
    taskQuery,
    workspaceQuery,
    draftMutation,
    submitMutation,
    questionMutation,
    applyFilters,
    resetFilters,
    useDemoData,
    useApiData,
    selectInterview: (id: number) => (selectedInterviewId.value = id),
  }
}
