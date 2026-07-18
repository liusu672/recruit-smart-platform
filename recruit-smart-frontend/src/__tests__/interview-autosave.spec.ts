import { afterEach, describe, expect, it, vi } from 'vitest'

import {
  createInterviewDraftAutosave,
  INTERVIEW_DRAFT_AUTOSAVE_DELAY,
} from '@/composables/interviewDraftAutosave'
import type { InterviewDraftSaveState } from '@/types/interview'

afterEach(() => {
  vi.useRealTimers()
})

describe('interviewer feedback autosave', () => {
  it('waits 1.5 seconds before saving the latest draft', async () => {
    vi.useFakeTimers()
    const save = vi.fn().mockResolvedValue(undefined)
    const queue = createInterviewDraftAutosave({ save })

    queue.schedule('first')
    queue.schedule('latest')
    await vi.advanceTimersByTimeAsync(INTERVIEW_DRAFT_AUTOSAVE_DELAY - 1)
    expect(save).not.toHaveBeenCalled()

    await vi.advanceTimersByTimeAsync(1)
    expect(save).toHaveBeenCalledTimes(1)
    expect(save).toHaveBeenCalledWith('latest')
  })

  it('serializes requests and keeps only the latest snapshot changed during a save', async () => {
    let releaseFirstSave: (() => void) | undefined
    const firstSave = new Promise<void>((resolve) => {
      releaseFirstSave = resolve
    })
    const save = vi
      .fn<(snapshot: string) => Promise<void>>()
      .mockImplementationOnce(() => firstSave)
      .mockResolvedValue(undefined)
    const queue = createInterviewDraftAutosave({ save })

    queue.schedule('first')
    const flushing = queue.flush()
    queue.schedule('intermediate')
    queue.schedule('latest')
    releaseFirstSave?.()

    await flushing
    expect(save.mock.calls).toEqual([['first'], ['latest']])
  })

  it('keeps a failed draft pending and succeeds after retry', async () => {
    const states: InterviewDraftSaveState[] = []
    const save = vi
      .fn<(snapshot: string) => Promise<void>>()
      .mockRejectedValueOnce(new Error('network'))
      .mockResolvedValue(undefined)
    const queue = createInterviewDraftAutosave({
      save,
      onStateChange: (state) => states.push(state),
    })

    queue.schedule('draft')
    await expect(queue.flush()).resolves.toBe(false)
    expect(queue.hasPending()).toBe(true)
    expect(states.at(-1)).toBe('error')

    await expect(queue.flush()).resolves.toBe(true)
    expect(save).toHaveBeenCalledTimes(2)
    expect(queue.hasPending()).toBe(false)
    expect(states.at(-1)).toBe('saved')
  })
})
