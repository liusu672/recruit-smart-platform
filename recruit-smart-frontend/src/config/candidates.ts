import type { ApplicationStatus, CandidateSource, CandidateStatus } from '@/types/candidate'

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

export function getCandidateStatusTone(status: CandidateStatus) {
  if (status === 'HIRED') return 'success'
  if (status === 'INTERVIEWING') return 'info'
  return 'warning'
}

export function getApplicationStatusTone(status: ApplicationStatus) {
  if (status === 'HIRED' || status === 'OFFER') return 'success'
  if (status === 'SCREEN_REJECT' || status === 'INTERVIEW_REJECT' || status === 'OFFER_REJECT') {
    return 'danger'
  }
  if (status === 'INTERVIEWING' || status === 'SCREEN_PASS') return 'info'
  return 'warning'
}
