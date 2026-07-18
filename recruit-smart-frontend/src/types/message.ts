import type { PagedData } from '@/types/api'

export interface MessageConversation {
  id: number
  applicationId: number
  jobId: number
  jobTitle: string
  candidateId: number
  candidateName: string
  applicationStatus: string
  lastMessagePreview: string | null
  lastMessageAt: string | null
  unreadCount: number
  createdAt: string
}

export interface MessageRecord {
  id: number
  conversationId: number
  senderId: number
  senderName: string
  senderRole: string
  messageType: string
  content: string
  mine: boolean
  createdAt: string
}

export interface MessagePageQuery {
  page: number
  pageSize: number
}

export type MessageConversationPagedData = PagedData<MessageConversation>
export type MessageRecordPagedData = PagedData<MessageRecord>

export interface MessageRealtimeEvent {
  type: 'connected' | 'message-updated'
  changedAt?: string
}
