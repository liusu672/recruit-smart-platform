import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { computed, reactive, ref } from 'vue'

import {
  completeInterview,
  generateInterviewQuestions,
  getInterviewTasks,
  getInterviewWorkspace,
  scheduleInterview,
  saveInterviewFeedbackDraft,
  submitInterviewFeedback,
} from '@/api/interviews'
import { generateFeedbackSummary } from '@/api/ai'
import {
  applyDemoInterviewCompletion,
  applyDemoInterviewDraft,
  applyDemoInterviewSchedule,
  applyDemoInterviewSubmit,
  getDemoInterviewPage,
  initialDemoInterviews,
} from '@/config/demoInterviews'
import type {
  InterviewFeedbackRequest,
  InterviewQuestionRequest,
  InterviewScheduleRequest,
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

  async function invalidateInterviewTasks() {
    await queryClient.invalidateQueries({ queryKey: ['interview-tasks'] })
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
    // 草稿保存期间保留本地正在继续输入的内容，只刷新任务列表中的反馈状态。
    onSuccess: invalidateInterviewTasks,
  })

  const scheduleMutation = useMutation({
    mutationFn: async ({ id, data }: { id: number; data: InterviewScheduleRequest }) => {
      if (!demoMode.value) return scheduleInterview(id, data)
      updateDemoInterview(id, (interview) => applyDemoInterviewSchedule(interview, data))
    },
    onSuccess: invalidateInterviews,
  })

  const completeMutation = useMutation({
    mutationFn: async (id: number) => {
      if (!demoMode.value) return completeInterview(id)
      updateDemoInterview(id, applyDemoInterviewCompletion)
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
      const timestamp = Date.now()
      const skillText = interview.candidateBrief.skills.slice(0, 3).join('、') || interview.jobTitle
      const projectText =
        interview.candidateBrief.projectExperience ??
        interview.candidateBrief.workExperience ??
        '简历项目经历'
      const riskText = interview.candidateBrief.riskPoints[0] ?? '简历真实性与岗位匹配度'
      return [
        {
          id: `${id}-resume-${timestamp}`,
          category: '简历追问',
          question: `请结合“${projectText}”说明候选人在 ${skillText} 方面的核心职责、技术取舍和最终结果。`,
          source: 'RESUME' as const,
        },
        {
          id: `${id}-job-${timestamp}`,
          category: '岗位匹配',
          question: `围绕“${interview.jobTitle}”岗位要求，请候选人说明一个最能体现岗位匹配度的项目案例。`,
          source: 'JOB' as const,
        },
        {
          id: `${id}-risk-${timestamp}`,
          category: '风险核实',
          question: `针对“${riskText}”，请候选人补充具体背景、个人贡献和可验证的结果指标。`,
          source: 'RISK' as const,
        },
      ]
    },
    onSuccess: (questions, { id }) => {
      if (demoMode.value) {
        updateDemoInterview(id, (interview) => ({ ...interview, questions }))
      }
      queryClient.setQueryData<InterviewWorkspace>(
        ['interview-workspace', demoMode.value ? 'demo' : 'api', id],
        (interview) => (interview ? { ...interview, questions } : interview),
      )
    },
  })

  const summaryMutation = useMutation({ mutationFn: generateFeedbackSummary })

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
    scheduleMutation,
    completeMutation,
    submitMutation,
    questionMutation,
    summaryMutation,
    applyFilters,
    resetFilters,
    useDemoData,
    useApiData,
    selectInterview: (id: number) => (selectedInterviewId.value = id),
  }
}
