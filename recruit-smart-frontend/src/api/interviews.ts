import { http, unwrapResult, unwrapVoidResult } from '@/api/http'
import type { Result } from '@/types/api'
import { defaultInterviewScorecard } from '@/config/interviews'
import type {
  InterviewFeedbackRequest,
  InterviewQuestion,
  InterviewQuestionRequest,
  InterviewTaskPagedData,
  InterviewTaskPageResponse,
  InterviewTaskQuery,
  InterviewTaskSummary,
  InterviewWorkspace,
} from '@/types/interview'

interface BackendInterviewSummary {
  id: number
  applicationId: number
  jobId: number
  jobTitle: string
  candidateId: number
  candidateName: string
  interviewerId: number
  interviewerName: string
  round: InterviewWorkspace['round']
  roundText: string
  interviewTime: string
  method: InterviewWorkspace['method']
  methodText: string
  location: string | null
  status: InterviewWorkspace['status']
  statusText: string
  feedbackScore: number | null
  feedbackSuggestion: InterviewWorkspace['feedback']['suggestion']
}

interface BackendInterviewDetail extends Omit<
  BackendInterviewSummary,
  'feedbackScore' | 'feedbackSuggestion'
> {
  applicationStatus: string
  department: string
  resumeId: number
  resumeName: string
  createdAt: string
  updatedAt: string
}

interface BackendInterviewFeedback {
  id: number
  interviewId: number
  interviewerId: number
  interviewerName: string
  score: number
  comment: string
  suggestion: InterviewWorkspace['feedback']['suggestion']
  suggestionText: string
  aiSummary: string | null
  createdAt: string
  updatedAt: string
}

interface BackendInterviewPageResponse {
  total: number
  records: BackendInterviewSummary[]
}

interface BackendQuestionResponse {
  category: string
  summary: string
  questions: string[]
}

function adaptTask(source: BackendInterviewSummary): InterviewTaskSummary {
  const feedbackState = source.feedbackScore !== null ? 'SUBMITTED' : 'EMPTY'
  return {
    id: source.id,
    applicationId: source.applicationId,
    candidateId: source.candidateId,
    candidateName: source.candidateName,
    jobTitle: source.jobTitle,
    department: '',
    round: source.round,
    roundText: source.roundText,
    interviewTime: source.interviewTime,
    method: source.method,
    methodText: source.methodText,
    location: source.location,
    status: source.status,
    statusText: source.statusText,
    interviewerId: source.interviewerId,
    interviewerName: source.interviewerName,
    feedbackState,
    feedbackStateText: feedbackState === 'SUBMITTED' ? '已提交' : '未填写',
  }
}

function buildScorecard(
  feedback: BackendInterviewFeedback | null,
): InterviewWorkspace['scorecard'] {
  return defaultInterviewScorecard.map((item) => ({
    ...item,
    score: null,
    evidence: feedback?.comment ?? '',
  }))
}

function adaptWorkspace(
  detail: BackendInterviewDetail,
  feedback: BackendInterviewFeedback | null,
): InterviewWorkspace {
  const feedbackState = feedback ? 'SUBMITTED' : 'EMPTY'
  return {
    id: detail.id,
    applicationId: detail.applicationId,
    candidateId: detail.candidateId,
    candidateName: detail.candidateName,
    jobTitle: detail.jobTitle,
    department: detail.department,
    round: detail.round,
    roundText: detail.roundText,
    interviewTime: detail.interviewTime,
    method: detail.method,
    methodText: detail.methodText,
    location: detail.location,
    status: detail.status,
    statusText: detail.statusText,
    interviewerId: detail.interviewerId,
    interviewerName: detail.interviewerName,
    feedbackState,
    feedbackStateText: feedbackState === 'SUBMITTED' ? '已提交' : '未填写',
    candidateBrief: {
      education: null,
      school: null,
      yearsOfExperience: 0,
      resumeName: detail.resumeName,
      skills: [],
      workExperience: null,
      projectExperience: null,
      matchScore: null,
      matchSummary: null,
      riskPoints: [],
    },
    scorecard: buildScorecard(feedback),
    questions: [],
    feedback: {
      id: feedback?.id ?? null,
      interviewId: detail.id,
      interviewerId: feedback?.interviewerId ?? detail.interviewerId,
      score: feedback?.score ?? null,
      comment: feedback?.comment ?? '',
      suggestion: feedback?.suggestion ?? null,
      aiSummary: feedback?.aiSummary ?? null,
      state: feedbackState,
      submittedAt: feedback?.createdAt ?? null,
    },
  }
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
    ...(query.status ? { status: query.status } : {}),
  }
  const result = await unwrapResult(
    http.get<Result<BackendInterviewPageResponse>>('/interviews/interviewer/me', { params }),
  )
  const records = result.records
    .map(adaptTask)
    .filter(
      (item) =>
        !query.keyword ||
        `${item.candidateName}${item.jobTitle}${item.interviewerName}`.includes(query.keyword),
    )
    .filter((item) => !query.feedbackState || item.feedbackState === query.feedbackState)
  return adaptInterviewTaskPage(
    {
      total: records.length,
      records,
    },
    query.page,
    query.pageSize,
  )
}

export async function getInterviewWorkspace(id: number) {
  const detail = await unwrapResult(http.get<Result<BackendInterviewDetail>>(`/interviews/${id}`))
  const feedback = await unwrapResult(
    http.get<Result<BackendInterviewFeedback>>(`/interviews/${id}/feedback`),
  ).catch(() => null)
  return adaptWorkspace(detail, feedback)
}

export function saveInterviewFeedbackDraft(id: number, data: InterviewFeedbackRequest) {
  void id
  void data
  return Promise.reject(new Error('后端不支持面试反馈草稿保存，请直接提交反馈'))
}

export function submitInterviewFeedback(id: number, data: InterviewFeedbackRequest) {
  if (data.score === null || data.suggestion === null) {
    return Promise.reject(new Error('请填写评分和录用建议'))
  }
  return unwrapVoidResult(
    http.post<Result<null>>(`/interviews/${id}/feedback`, {
      score: data.score,
      comment: data.comment,
      suggestion: data.suggestion,
    }),
  )
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
