import { http, unwrapResult, unwrapVoidResult } from '@/api/http'
import type { Result } from '@/types/api'
import type {
  InterviewFeedbackRequest,
  InterviewQuestion,
  InterviewQuestionRequest,
  InterviewTaskPagedData,
  InterviewTaskPageResponse,
  InterviewTaskQuery,
  InterviewWorkspace,
} from '@/types/interview'

interface BackendQuestionResponse {
  category: string
  summary: string
  questions: string[]
}

export function adaptInterviewTaskPage(
  source: InterviewTaskPageResponse,
  page: number,
  pageSize: number,
): InterviewTaskPagedData {
  return { items: source.records, page, pageSize, total: source.total }
}

export async function getInterviewTasks(query: InterviewTaskQuery) {
  const params = {
    pageNum: query.page,
    pageSize: query.pageSize,
    ...(query.keyword ? { keyword: query.keyword } : {}),
    ...(query.status ? { status: query.status } : {}),
    ...(query.feedbackState ? { feedbackState: query.feedbackState } : {}),
  }
  const result = await unwrapResult(
    http.get<Result<InterviewTaskPageResponse>>('/interviews/tasks', { params }),
  )
  return adaptInterviewTaskPage(result, query.page, query.pageSize)
}

export function getInterviewWorkspace(id: number) {
  return unwrapResult(http.get<Result<InterviewWorkspace>>(`/interviews/${id}/workspace`))
}

export function saveInterviewFeedbackDraft(id: number, data: InterviewFeedbackRequest) {
  return unwrapVoidResult(
    http.put<Result<null>>(`/interviews/${id}/feedback/draft`, {
      scorecard: data.scorecard,
      score: data.score,
      comment: data.comment,
      suggestion: data.suggestion,
    }),
  )
}

export function submitInterviewFeedback(id: number, data: InterviewFeedbackRequest) {
  if (data.score === null || data.suggestion === null) {
    return Promise.reject(new Error('请填写评分和录用建议'))
  }
  return unwrapResult(
    http.post<Result<number>>(`/interviews/${id}/feedback`, {
      scorecard: data.scorecard,
      score: data.score,
      comment: data.comment,
      suggestion: data.suggestion,
    }),
  ).then(() => undefined)
}

export async function generateInterviewQuestions(id: number, data: InterviewQuestionRequest) {
  const response = await http.post<BackendQuestionResponse>('/ai/interview-questions', {
    jobTitle: data.focus,
    requirements: data.focus,
  })
  return response.data.questions.map<InterviewQuestion>((question, index) => ({
    id: `${id}-ai-${Date.now()}-${index}`,
    category: response.data.category || 'AI 追问',
    question,
    source: 'MANUAL',
  }))
}
