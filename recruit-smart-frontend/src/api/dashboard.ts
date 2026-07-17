import { http, unwrapResult } from '@/api/http'
import type { Result } from '@/types/api'
import type { DashboardOverview } from '@/types/dashboard'

export function getDashboardOverview() {
  return unwrapResult(http.get<Result<DashboardOverview>>('/dashboard/overview'))
}
