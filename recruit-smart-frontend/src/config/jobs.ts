import type { JobStatus } from '@/types/job'

export const jobStatusOptions: Array<{ label: string; value: JobStatus }> = [
  { label: '草稿', value: 'DRAFT' },
  { label: '招聘中', value: 'OPEN' },
  { label: '已暂停', value: 'PAUSED' },
  { label: '已关闭', value: 'CLOSED' },
]

export const jobDepartmentOptions = ['技术部', '产品部', '设计部', '市场部', '人力资源部']

export function getJobStatusTone(status: JobStatus) {
  if (status === 'OPEN') return 'success'
  if (status === 'PAUSED') return 'info'
  if (status === 'CLOSED') return 'danger'
  return 'warning'
}
