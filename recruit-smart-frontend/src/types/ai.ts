export type AiStreamEvent =
  | { type: 'meta'; requestId: string }
  | { type: 'delta'; content: string }
  | { type: 'source'; sourceId: string; label: string }
  | { type: 'tool'; name: string; status: 'running' | 'success' | 'failed' }
  | { type: 'approval'; action: string; payload: unknown }
  | { type: 'done' }
  | { type: 'error'; code: string; message: string }

export interface AiApprovalRequest {
  proposedAction: string
  affectedRecord: string
  editableContent: string
  auditNote: string
}

export interface FeedbackSummaryRequest {
  interviewId: number
  candidateId: number
  jobId: number
  jobTitle: string
  candidateName: string
  feedbackText: string
  score: number | null
}

export interface FeedbackSummaryResponse {
  summary: string
  advantages: string[]
  risks: string[]
  suggestion: string
}

export interface TurnoverRiskRequest {
  employeeId: number
  employeeName: string
  department: string
  position: string
  performanceSummary: string
  attendanceSummary: string
  satisfactionFeedback: string
}

export interface TurnoverRiskResponse {
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH'
  riskScore: number
  summary: string
  riskReasons: string[]
  suggestions: string[]
}
