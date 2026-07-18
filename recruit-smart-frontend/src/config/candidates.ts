import type { ApplicationStatus, CandidateSource, CandidateStatus } from '@/types/candidate'
import type { HrStatusTone } from '@/types/hr'

export const candidateStatusOptions: Array<{ label: string; value: CandidateStatus }> = [
  { label: '可应聘', value: 'AVAILABLE' },
  { label: '面试中', value: 'INTERVIEWING' },
  { label: '已入职', value: 'HIRED' },
]

export const candidateEducationOptions = ['大专', '本科', '硕士', '博士']

export const candidateSourceOptions: Array<{ label: string; value: CandidateSource }> = [
  { label: 'HR 录入', value: 'HR_IMPORT' },
  { label: '候选人注册', value: 'SELF_REGISTER' },
  { label: '在线投递', value: 'ONLINE' },
]

export function getCandidateStatusTone(status: CandidateStatus): HrStatusTone {
  if (status === 'HIRED') return 'success'
  if (status === 'INTERVIEWING') return 'info'
  return 'neutral'
}

export function getApplicationStatusTone(status: ApplicationStatus): HrStatusTone {
  if (status === 'HIRED' || status === 'OFFERED') return 'success'
  if (status === 'SCREEN_REJECT' || status === 'REJECTED') return 'danger'
  if (status === 'INTERVIEWING' || status === 'SCREEN_PASSED') return 'info'
  if (status === 'WITHDRAWN') return 'neutral'
  return 'warning'
}
