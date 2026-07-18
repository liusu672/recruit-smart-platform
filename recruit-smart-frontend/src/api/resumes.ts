import { http, unwrapVoidResult } from '@/api/http'
import type { Result } from '@/types/api'

export interface ResumeFileResponse {
  blob: Blob
  fileName: string
  contentType: string
}

function readHeader(headers: unknown, name: string): string {
  if (!headers || typeof headers !== 'object') return ''
  const record = headers as Record<string, unknown>
  const value = record[name] ?? record[name.toLowerCase()]
  return typeof value === 'string' ? value : ''
}

function parseContentDispositionFileName(value: string): string {
  const encoded = value.match(/filename\*=UTF-8''([^;]+)/i)?.[1]
  if (encoded) return decodeURIComponent(encoded)

  const plain = value.match(/filename="?([^";]+)"?/i)?.[1]
  return plain ? decodeURIComponent(plain) : ''
}

async function getResumeFile(
  id: number,
  action: 'download' | 'preview',
): Promise<ResumeFileResponse> {
  const response = await http.get<Blob>(`/resumes/${id}/${action}`, { responseType: 'blob' })
  const disposition = readHeader(response.headers, 'content-disposition')
  const contentType = readHeader(response.headers, 'content-type') || response.data.type

  return {
    blob: response.data,
    fileName: parseContentDispositionFileName(disposition) || `resume-${id}`,
    contentType,
  }
}

export function getResumeDownloadFile(id: number) {
  return getResumeFile(id, 'download')
}

export function getResumePreviewFile(id: number) {
  return getResumeFile(id, 'preview')
}

export function renameResume(id: number, resumeName: string) {
  return unwrapVoidResult(http.put<Result<null>>(`/resumes/${id}/name`, { resumeName }))
}

export function deleteResume(id: number) {
  return unwrapVoidResult(http.delete<Result<null>>(`/resumes/${id}`))
}

export function saveBlobAsFile(file: ResumeFileResponse) {
  const url = URL.createObjectURL(file.blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = file.fileName
  anchor.click()
  window.setTimeout(() => URL.revokeObjectURL(url), 1_000)
}

export function openBlobPreview(file: ResumeFileResponse) {
  const url = URL.createObjectURL(file.blob)
  const preview = window.open(url, '_blank')
  if (!preview) {
    URL.revokeObjectURL(url)
    throw new Error('浏览器阻止了新窗口，请允许弹窗后重试')
  }
  preview.opener = null
  window.setTimeout(() => URL.revokeObjectURL(url), 60_000)
}
