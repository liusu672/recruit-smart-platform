import type { UserRole } from '@/types/auth'

declare module 'vue-router' {
  interface RouteMeta {
    public?: boolean
    roles?: UserRole[]
    title?: string
  }
}
