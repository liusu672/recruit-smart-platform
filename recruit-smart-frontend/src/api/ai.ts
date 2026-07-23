import { http, unwrapResult } from '@/api/http'
import type { Result } from '@/types/api'
import type {
  FeedbackSummaryResponse,
  TurnoverRiskHistoryResponse,
  TurnoverRiskResponse,
} from '@/types/ai'

function toStringArray(value: unknown): string[] {
  return Array.isArray(value)
    ? value.filter((item): item is string => typeof item === 'string')
    : []
}

export function adaptFeedbackSummary(source: unknown): FeedbackSummaryResponse {
  const value = (source ?? {}) as Partial<FeedbackSummaryResponse>
  return {
    summary: typeof value.summary === 'string' ? value.summary : '',
    advantages: toStringArray(value.advantages),
    risks: toStringArray(value.risks),
    suggestion: typeof value.suggestion === 'string' ? value.suggestion : '',
  }
}

export function adaptTurnoverRisk(source: unknown): TurnoverRiskResponse {
  const value = (source ?? {}) as Partial<TurnoverRiskResponse>
  const score =
    typeof value.riskScore === 'number' && Number.isFinite(value.riskScore)
      ? Math.min(100, Math.max(0, value.riskScore))
      : 0
  const sentimentRiskScore =
    typeof value.sentimentRiskScore === 'number' && Number.isFinite(value.sentimentRiskScore)
      ? Math.min(100, Math.max(0, value.sentimentRiskScore))
      : null
  return {
    riskLevel: ['LOW', 'MEDIUM', 'HIGH'].includes(value.riskLevel ?? '')
      ? (value.riskLevel as TurnoverRiskResponse['riskLevel'])
      : 'MEDIUM',
    riskScore: score,
    summary: typeof value.summary === 'string' ? value.summary : '',
    riskReasons: toStringArray(value.riskReasons),
    suggestions: toStringArray(value.suggestions),
    sentimentLabel: typeof value.sentimentLabel === 'string' ? value.sentimentLabel : null,
    sentimentRiskScore,
    sentimentSummary: typeof value.sentimentSummary === 'string' ? value.sentimentSummary : null,
  }
}

function toNumberArray(value: unknown): number[] {
  return Array.isArray(value)
    ? value.filter((item): item is number => typeof item === 'number' && Number.isFinite(item))
    : []
}

export function adaptTurnoverRiskHistory(source: unknown): TurnoverRiskHistoryResponse {
  const value = (source ?? {}) as Partial<TurnoverRiskHistoryResponse>
  return {
    ...adaptTurnoverRisk(source),
    id: typeof value.id === 'number' ? value.id : 0,
    taskId: typeof value.taskId === 'number' ? value.taskId : null,
    employeeId: typeof value.employeeId === 'number' ? value.employeeId : 0,
    periodStart: typeof value.periodStart === 'string' ? value.periodStart : null,
    periodEnd: typeof value.periodEnd === 'string' ? value.periodEnd : null,
    behaviorRecordIds: toNumberArray(value.behaviorRecordIds),
    source: typeof value.source === 'string' ? value.source : null,
    modelName: typeof value.modelName === 'string' ? value.modelName : null,
    promptVersion: typeof value.promptVersion === 'string' ? value.promptVersion : null,
    generatedAt: typeof value.generatedAt === 'string' ? value.generatedAt : null,
  }
}

export async function generateFeedbackSummary(interviewId: number) {
  const result = await unwrapResult(
    http.post<Result<unknown>>(`/interviews/${interviewId}/ai-summary`),
  )
  return adaptFeedbackSummary(result)
}

export async function assessTurnoverRisk(employeeId: number) {
  const result = await unwrapResult(
    http.post<Result<unknown>>(`/employees/${employeeId}/turnover-risk`),
  )
  return adaptTurnoverRisk(result)
}

export async function listTurnoverRiskHistory(employeeId: number) {
  const result = await unwrapResult(
    http.get<Result<unknown[]>>(`/employees/${employeeId}/turnover-risks`),
  )
  return Array.isArray(result) ? result.map(adaptTurnoverRiskHistory) : []
}
