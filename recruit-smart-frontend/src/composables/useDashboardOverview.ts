import { useQuery } from '@tanstack/vue-query'

import { getDashboardOverview } from '@/api/dashboard'

export function useDashboardOverview() {
  return useQuery({
    queryKey: ['dashboard-overview'],
    queryFn: getDashboardOverview,
    staleTime: 30_000,
    refetchInterval: 60_000,
  })
}
