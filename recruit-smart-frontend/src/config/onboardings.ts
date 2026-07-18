import type { MaterialStatus, OnboardingStatus } from '@/types/onboarding'
import type { HrStatusTone } from '@/types/hr'

export const onboardingStatusOptions: Array<{ label: string; value: OnboardingStatus }> = [
  { label: '待提交', value: 'PENDING' },
  { label: '审核中', value: 'REVIEWING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已入职', value: 'ONBOARDED' },
  { label: '已取消', value: 'CANCELED' },
]

export function getOnboardingStatusText(status: OnboardingStatus) {
  return onboardingStatusOptions.find((item) => item.value === status)?.label ?? status
}

export function getOnboardingStatusTone(status: OnboardingStatus): HrStatusTone {
  if (status === 'ONBOARDED' || status === 'APPROVED') return 'success'
  if (status === 'CANCELED') return 'neutral'
  if (status === 'REVIEWING') return 'info'
  return 'warning'
}

export function getMaterialStatusText(status: MaterialStatus) {
  const labels: Record<MaterialStatus, string> = {
    PENDING: '待提交',
    REVIEWING: '审核中',
    APPROVED: '已通过',
    REJECTED: '需补充',
  }
  return labels[status]
}

export function canStartOnboardingReview(status: OnboardingStatus) {
  void status
  return false
}

export function canReviewOnboardingMaterial(status: OnboardingStatus) {
  return status === 'REVIEWING'
}

export function canCompleteOnboarding(status: OnboardingStatus, materialStatus: MaterialStatus) {
  return status === 'APPROVED' && materialStatus === 'APPROVED'
}

export function canCancelOnboarding(status: OnboardingStatus) {
  return status !== 'ONBOARDED' && status !== 'CANCELED'
}
