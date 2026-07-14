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
