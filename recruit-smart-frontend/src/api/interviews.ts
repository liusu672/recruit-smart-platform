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

export function adaptInterviewTaskPage(
  source: InterviewTaskPageResponse,
  page: number,
  pageSize: number,
): InterviewTaskPagedData {
  return { items: source.records, page, pageSize, total: source.total }
}

// 后端目前仅有面试实体和 Mapper；聚合、草稿与提交接口均是待业务服务确认的前端契约。
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
  return unwrapVoidResult(http.put<Result<null>>(`/interviews/${id}/feedback/draft`, data))
}

export function submitInterviewFeedback(id: number, data: InterviewFeedbackRequest) {
  return unwrapVoidResult(http.post<Result<null>>(`/interviews/${id}/feedback`, data))
}

export function generateInterviewQuestions(id: number, data: InterviewQuestionRequest) {
  return unwrapResult(
    http.post<Result<InterviewQuestion[]>>(`/ai/interviews/${id}/questions`, data),
  )
}
