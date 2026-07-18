import { http, unwrapResult, unwrapVoidResult } from '@/api/http'
import type { Result } from '@/types/api'
import type {
  ApplicationStatus,
  CandidateCreateRequest,
  CandidateUpdateRequest,
  CandidateAiMatch,
  CandidateApplication,
  CandidateDetail,
  CandidatePagedData,
  CandidatePageResponse,
  CandidateQuery,
  CandidateResume,
  CandidateSource,
  CandidateStatus,
  CandidateSummary,
} from '@/types/candidate'

interface BackendCandidateSummary {
  id: number
  name: string
  gender: string | null
  age: number | null
  phone: string | null
  email: string | null
  education: string | null
  school: string | null
  major: string | null
  yearsOfExperience: number | null
  currentStatus: CandidateStatus
  source: CandidateSource
  hasAccount: boolean
  createdAt: string
}

interface BackendCandidateDetail extends BackendCandidateSummary {
  updatedAt: string | null
  resumes: Array<{
    id: number
    resumeName: string
    fileUrl: string | null
    fileType: string | null
    isDefault: number | boolean | null
    createdAt: string | null
    parseStatus: string | null
    parseStatusText: string | null
  }>
  applications: Array<{
    id: number
    jobId: number
    jobTitle: string
    department: string | null
    resumeId: number
    status: ApplicationStatus
    statusText: string
    allowAdjustment: boolean
    source: string
    hrNote: string | null
    appliedAt: string
    aiMatch: {
      matchScore: number
      recommendLevel: CandidateAiMatch['level']
      recommendReason: string
      highlightSummary: string
      riskSummary: string
      modelName: string
      generatedAt: string
    } | null
  }>
}

interface BackendCandidatePageResponse {
  total: number
  records: BackendCandidateSummary[]
}

const candidateStatusText: Record<CandidateStatus, string> = {
  AVAILABLE: '可应聘',
  INTERVIEWING: '面试中',
  HIRED: '已入职',
}

const candidateSourceText: Record<CandidateSource, string> = {
  SELF_REGISTER: '候选人注册',
  HR_IMPORT: 'HR 录入',
  ONLINE: '在线投递',
}

function adaptAiMatch(
  source: BackendCandidateDetail['applications'][number]['aiMatch'],
): CandidateAiMatch | null {
  if (!source) return null
  return {
    score: source.matchScore,
    level: source.recommendLevel,
    summary: source.recommendReason,
    highlights: source.highlightSummary ? [source.highlightSummary] : [],
    risks: source.riskSummary ? [source.riskSummary] : [],
    suggestion: source.recommendReason,
    modelName: source.modelName,
    generatedAt: source.generatedAt,
  }
}

function adaptResume(source: BackendCandidateDetail['resumes'][number]): CandidateResume {
  return {
    id: source.id,
    resumeName: source.resumeName,
    fileUrl: source.fileUrl,
    fileType: source.fileType,
    parsedContent: null,
    skills: [],
    projectExperience: null,
    workExperience: null,
    isDefault: source.isDefault === true || source.isDefault === 1,
    updatedAt: source.createdAt,
  }
}

function adaptApplication(
  source: BackendCandidateDetail['applications'][number],
): CandidateApplication {
  return {
    id: source.id,
    jobId: source.jobId,
    jobTitle: source.jobTitle,
    resumeId: source.resumeId,
    status: source.status,
    statusText: source.statusText,
    source: source.source,
    allowAdjustment: source.allowAdjustment,
    hrNote: source.hrNote,
    appliedAt: source.appliedAt,
    aiMatch: adaptAiMatch(source.aiMatch),
  }
}

function adaptCandidateSummary(source: BackendCandidateSummary): CandidateSummary {
  return {
    id: source.id,
    userId: null,
    name: source.name,
    gender: source.gender,
    age: source.age,
    phone: source.phone,
    email: source.email,
    education: source.education,
    school: source.school,
    major: source.major,
    yearsOfExperience: source.yearsOfExperience ?? 0,
    currentStatus: source.currentStatus,
    currentStatusText: candidateStatusText[source.currentStatus] ?? source.currentStatus,
    source: source.source,
    sourceText: candidateSourceText[source.source] ?? source.source,
    resumeCount: 0,
    latestApplicationStatus: null,
    latestApplicationStatusText: null,
    latestJobTitle: null,
    latestMatchScore: null,
    lastActivityAt: source.createdAt,
    duplicateRisk: false,
  }
}

function adaptCandidateDetail(source: BackendCandidateDetail): CandidateDetail {
  const applications = source.applications.map(adaptApplication)
  const latestApplication = applications[0]
  return {
    ...adaptCandidateSummary(source),
    resumeCount: source.resumes.length,
    latestApplicationStatus: latestApplication?.status ?? null,
    latestApplicationStatusText: latestApplication?.statusText ?? null,
    latestJobTitle: latestApplication?.jobTitle ?? null,
    latestMatchScore: latestApplication?.aiMatch?.score ?? null,
    lastActivityAt: source.updatedAt ?? source.createdAt,
    resumes: source.resumes.map(adaptResume),
    applications,
  }
}

export function adaptCandidatePage(
  source: CandidatePageResponse,
  page: number,
  pageSize: number,
): CandidatePagedData {
  return {
    items: source.records,
    page,
    pageSize,
    total: source.total,
  }
}

export async function getCandidates(query: CandidateQuery): Promise<CandidatePagedData> {
  const params = {
    pageNum: query.page,
    pageSize: query.pageSize,
    ...(query.keyword ? { keyword: query.keyword } : {}),
    ...(query.education ? { education: query.education } : {}),
    ...(query.school ? { school: query.school } : {}),
    ...(query.yearsOfExperienceMin !== null
      ? { minYearsOfExperience: query.yearsOfExperienceMin }
      : {}),
    ...(query.currentStatus ? { currentStatus: query.currentStatus } : {}),
  }
  const result = await unwrapResult(
    http.get<Result<BackendCandidatePageResponse>>('/candidate', { params }),
  )
  return adaptCandidatePage(
    {
      total: result.total,
      records: result.records.map(adaptCandidateSummary),
    },
    query.page,
    query.pageSize,
  )
}

export function getCandidateById(id: number): Promise<CandidateDetail> {
  return unwrapResult(http.get<Result<BackendCandidateDetail>>(`/candidate/${id}`)).then(
    adaptCandidateDetail,
  )
}

export function createCandidate(data: CandidateCreateRequest): Promise<number> {
  const payload = {
    name: data.name,
    gender: data.gender,
    age: data.age,
    phone: data.phone,
    email: data.email,
    education: data.education,
    school: data.school,
    major: data.major,
    yearsOfExperience: data.yearsOfExperience,
    source: data.source,
  }
  return unwrapResult(http.post<Result<number>>('/candidate', payload))
}

export function updateCandidate(id: number, data: CandidateUpdateRequest): Promise<void> {
  return unwrapVoidResult(
    http.put<Result<null>>(`/candidate/${id}`, {
      name: data.name,
      gender: data.gender,
      age: data.age,
      phone: data.phone,
      email: data.email,
      education: data.education,
      school: data.school,
      major: data.major,
      yearsOfExperience: data.yearsOfExperience,
      source: data.source,
    }),
  )
}
