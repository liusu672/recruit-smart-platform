/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_PORTAL_DEMO_FALLBACK?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
