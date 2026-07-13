import type { VueQueryPluginOptions } from '@tanstack/vue-query'

// 集中配置查询策略，避免各业务模块各自定义不兼容的缓存规则。
export const queryClientOptions: VueQueryPluginOptions = {
  queryClientConfig: {
    defaultOptions: {
      queries: {
        staleTime: 30_000,
        retry: 1,
        refetchOnWindowFocus: false,
      },
      mutations: {
        retry: 0,
      },
    },
  },
}
