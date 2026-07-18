import type { InterviewDraftSaveState } from '@/types/interview'

export const INTERVIEW_DRAFT_AUTOSAVE_DELAY = 1_500

interface InterviewDraftAutosaveOptions<T> {
  save: (snapshot: T) => Promise<void>
  onStateChange?: (state: InterviewDraftSaveState) => void
  delay?: number
}

export function createInterviewDraftAutosave<T>(options: InterviewDraftAutosaveOptions<T>) {
  const delay = options.delay ?? INTERVIEW_DRAFT_AUTOSAVE_DELAY
  let timer: ReturnType<typeof setTimeout> | undefined
  let pendingSnapshot: T | null = null
  let drainPromise: Promise<boolean> | null = null
  let disposed = false

  function setState(state: InterviewDraftSaveState) {
    if (!disposed) options.onStateChange?.(state)
  }

  function clearTimer() {
    if (timer !== undefined) clearTimeout(timer)
    timer = undefined
  }

  async function drain(): Promise<boolean> {
    while (pendingSnapshot !== null) {
      const snapshot = pendingSnapshot
      pendingSnapshot = null
      setState('saving')
      try {
        await options.save(snapshot)
      } catch {
        if (pendingSnapshot === null) pendingSnapshot = snapshot
        setState('error')
        return false
      }
    }
    setState('saved')
    return true
  }

  async function flush(): Promise<boolean> {
    clearTimer()
    if (drainPromise) return drainPromise
    if (pendingSnapshot === null) return true
    drainPromise = drain()
    const succeeded = await drainPromise
    drainPromise = null
    return succeeded
  }

  function schedule(snapshot: T) {
    pendingSnapshot = snapshot
    setState('dirty')
    clearTimer()
    timer = setTimeout(() => void flush(), delay)
  }

  function cancelPending() {
    clearTimer()
    pendingSnapshot = null
  }

  function dispose() {
    disposed = true
    cancelPending()
  }

  return {
    schedule,
    flush,
    cancelPending,
    dispose,
    hasPending: () => pendingSnapshot !== null || drainPromise !== null,
  }
}
