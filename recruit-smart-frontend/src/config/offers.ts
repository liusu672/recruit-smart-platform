import type { OfferRecord, OfferStatus } from '@/types/offer'

export const offerStatusOptions: Array<{ label: string; value: OfferStatus }> = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发送', value: 'SENT' },
  { label: '已接受', value: 'ACCEPTED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已撤回', value: 'REVOKED' },
]

export const probationMonthOptions = [1, 2, 3, 6]

export function getOfferStatusText(status: OfferStatus) {
  return offerStatusOptions.find((item) => item.value === status)?.label ?? status
}

export function getOfferStatusTone(status: OfferStatus) {
  if (status === 'ACCEPTED') return 'success'
  if (status === 'REJECTED' || status === 'REVOKED') return 'danger'
  if (status === 'SENT') return 'info'
  return 'warning'
}

export function canEditOffer(status: OfferStatus) {
  return status === 'DRAFT'
}

export function canSendOffer(status: OfferStatus) {
  return status === 'DRAFT'
}

export function canRevokeOffer(status: OfferStatus) {
  return status === 'SENT'
}

export function formatOfferSalary(value: number | null) {
  if (value === null) return '待填写'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    maximumFractionDigits: 0,
  }).format(value)
}

export function validateOfferForSend(offer: OfferRecord) {
  if (!offer.salary || offer.salary <= 0) return '请先填写有效月薪'
  if (!offer.entryDate) return '请先填写预计入职日期'
  if (!offer.workLocation?.trim()) return '请先填写工作地点'
  return null
}
