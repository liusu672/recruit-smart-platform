import { onMounted, ref, type Ref } from 'vue'

import { ApiError } from '@/types/api'

export interface PortalResource<T> {
  data: Ref<T>
  error: Ref<string>
  loading: Ref<boolean>
  demoMode: Ref<boolean>
  reload: () => Promise<void>
}

export function usePortalResource<T>(
  loader: () => Promise<T>,
  demoData: T | (() => T),
): PortalResource<T> {
  const getDemoData = () => (typeof demoData === 'function' ? (demoData as () => T)() : demoData)
  const data = ref(getDemoData()) as Ref<T>
  const error = ref('')
  const loading = ref(true)
  const demoMode = ref(false)

  async function reload() {
    loading.value = true
    error.value = ''
    demoMode.value = false
    try {
      data.value = await loader()
    } catch (caught) {
      const message = caught instanceof Error ? caught.message : '数据加载失败'
      const protectedFailure =
        caught instanceof ApiError && (caught.code === 401 || caught.code === 403)
      const demoFallbackEnabled = import.meta.env.VITE_PORTAL_DEMO_FALLBACK === 'true'
      if (demoFallbackEnabled && !protectedFailure) {
        // 演示回退也必须保持角色数据隔离，不能借用 HR 候选人库作为候选人数据。
        data.value = getDemoData()
        demoMode.value = true
      } else {
        error.value = message
      }
    } finally {
      loading.value = false
    }
  }

  onMounted(() => void reload())
  return { data, error, loading, demoMode, reload }
}
