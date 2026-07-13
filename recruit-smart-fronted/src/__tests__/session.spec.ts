import { setActivePinia, createPinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'

import { useSessionStore } from '@/stores/session'

describe('session store', () => {
  beforeEach(() => {
    sessionStorage.clear()
    setActivePinia(createPinia())
  })

  it('persists and clears JWT session state', () => {
    const store = useSessionStore()

    store.setSession({
      token: 'demo-token',
      user: {
        id: 'u1',
        username: 'hr01',
        name: 'HR 用户',
        role: 'HR',
      },
    })

    expect(store.isAuthenticated).toBe(true)
    expect(store.currentRole).toBe('HR')

    store.clearSession()

    expect(store.isAuthenticated).toBe(false)
    expect(sessionStorage.getItem('recruit-smart-token')).toBeNull()
  })
})
