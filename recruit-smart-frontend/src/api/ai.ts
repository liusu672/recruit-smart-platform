import { http, unwrapResult } from '@/api/http'
import type { Result } from '@/types/api'
import type {
  FeedbackSummaryResponse,
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
  return {
    riskLevel: ['LOW', 'MEDIUM', 'HIGH'].includes(value.riskLevel ?? '')
      ? (value.riskLevel as TurnoverRiskResponse['riskLevel'])
      : 'MEDIUM',
    riskScore: score,
    summary: typeof value.summary === 'string' ? value.summary : '',
    riskReasons: toStringArray(value.riskReasons),
    suggestions: toStringArray(value.suggestions),
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
