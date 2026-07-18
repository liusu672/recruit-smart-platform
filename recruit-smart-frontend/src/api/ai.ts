import { http } from '@/api/http'
import type {
  FeedbackSummaryRequest,
  FeedbackSummaryResponse,
  TurnoverRiskRequest,
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

// AI 服务返回原始 DTO，不使用业务服务的 Result<T> 包装。
export async function generateFeedbackSummary(data: FeedbackSummaryRequest) {
  const response = await http.post<unknown>('/ai/feedback-summary', data)
  return adaptFeedbackSummary(response.data)
}

export async function assessTurnoverRisk(data: TurnoverRiskRequest) {
  const response = await http.post<unknown>('/ai/turnover-risk', data)
  return adaptTurnoverRisk(response.data)
}
