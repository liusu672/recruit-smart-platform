export interface Result<T> {
  code: number
  message: string
  data: T | null
}

export interface PagedData<T> {
  items: T[]
  page: number
  pageSize: number
  total: number
}

export class ApiError extends Error {
  readonly code: number

  constructor(code: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.code = code
  }
}
