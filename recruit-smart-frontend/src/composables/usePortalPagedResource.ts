import { reactive, watch } from 'vue'

import { usePortalResource } from '@/composables/usePortalResource'
import type { PortalPagedData, PortalPageQuery } from '@/types/portal'

export function usePortalPagedResource<T>(
  loader: (query: PortalPageQuery) => Promise<PortalPagedData<T>>,
  demoItems: T[],
  pageSize = 10,
) {
  const query = reactive<PortalPageQuery>({ page: 1, pageSize })
  const resource = usePortalResource(
    () => loader({ ...query }),
    () => {
      const start = (query.page - 1) * query.pageSize
      return {
        items: demoItems.slice(start, start + query.pageSize),
        page: query.page,
        pageSize: query.pageSize,
        total: demoItems.length,
      }
    },
  )

  watch(
    () => [query.page, query.pageSize],
    () => void resource.reload(),
  )

  return { ...resource, query }
}
