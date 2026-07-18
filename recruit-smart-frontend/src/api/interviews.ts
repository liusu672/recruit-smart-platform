import { http, unwrapResult, unwrapVoidResult } from '@/api/http'
import type { Result } from '@/types/api'
import type {
  InterviewAssignmentRequest,
  InterviewFeedbackRequest,
  InterviewQuestion,
  InterviewQuestionRequest,
  InterviewScheduleRequest,
  InterviewTaskPagedData,
  InterviewTaskPageResponse,
  InterviewTaskQuery,
  InterviewUpdateRequest,
  InterviewerOption,
  InterviewWorkspace,
} from '@/types/interview'
import type { CandidateApplicationDetail } from '@/types/portal'

interface BackendQuestionResponse {
  category?: unknown
  summary?: unknown
  questions?: unknown
}

export function getInterviewApplicationContext(applicationId: number) {
  return unwrapResult(
    http.get<Result<CandidateApplicationDetail>>(`/applications/${applicationId}`),
  )
}

export function adaptInterviewQuestions(
  id: number,
  source: BackendQuestionResponse,
): InterviewQuestion[] {
  const category =
    typeof source.category === 'string' && source.category.trim() ? source.category : 'AI 追问'
  const questions = Array.isArray(source.questions)
    ? source.questions.filter((question): question is string => typeof question === 'string')
    : []
  return questions.map((question, index) => ({
    id: `${id}-ai-${index}`,
    category,
    question,
    source: 'MANUAL',
  }))
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

export function getInterviewers(): Promise<InterviewerOption[]> {
  return unwrapResult(http.get<Result<InterviewerOption[]>>('/users/interviewers'))
}

export function createInterview(data: InterviewAssignmentRequest) {
  return unwrapResult(http.post<Result<number>>('/interviews', data))
}

export function updateInterview(id: number, data: InterviewUpdateRequest) {
  return unwrapVoidResult(http.put<Result<null>>(`/interviews/${id}`, data))
}

export function scheduleInterview(id: number, data: InterviewScheduleRequest) {
  return unwrapVoidResult(http.put<Result<null>>(`/interviews/${id}/schedule`, data))
}

export function completeInterview(id: number) {
  return unwrapVoidResult(http.put<Result<null>>(`/interviews/${id}/complete`))
}

export function cancelInterview(id: number) {
  return unwrapVoidResult(http.put<Result<null>>(`/interviews/${id}/cancel`))
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
    jobId: data.jobId,
    candidateId: data.candidateId,
    resumeId: data.resumeId,
    jobTitle: data.jobTitle,
    responsibilities: data.responsibilities,
    requirements: [data.requirements, data.focus].filter(Boolean).join('\n'),
    resumeText: data.resumeText,
    skills: data.skills,
    projectExperience: data.projectExperience,
    workExperience: data.workExperience,
  })
  return adaptInterviewQuestions(id, response.data)
}
