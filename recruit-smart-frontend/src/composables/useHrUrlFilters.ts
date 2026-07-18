import { useRoute, useRouter } from 'vue-router'

type QueryValue = string | number | null | undefined

export function useHrUrlFilters(ownedKeys: string[]) {
  const route = useRoute()
  const router = useRouter()

  function readString(key: string) {
    const value = route.query[key]
    return typeof value === 'string' ? value : ''
  }

  function readNumber(key: string, fallback: number) {
    const value = Number(readString(key))
    return Number.isFinite(value) && value > 0 ? value : fallback
  }

  function sync(values: Record<string, QueryValue>) {
    const next = { ...route.query }
    for (const key of ownedKeys) delete next[key]
    for (const [key, value] of Object.entries(values)) {
      if (value !== '' && value !== null && value !== undefined) next[key] = String(value)
    }
    void router.replace({ query: next })
  }

  return { readString, readNumber, sync }
}
