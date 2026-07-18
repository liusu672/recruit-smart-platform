import { afterEach, describe, expect, it, vi } from 'vitest'

import { getCurrentUser } from '@/api/auth'
import { http } from '@/api/http'
import { updateCurrentPassword, updateCurrentUser } from '@/api/user'

describe('current user API contracts', () => {
  afterEach(() => vi.restoreAllMocks())

  it('loads /auth/me through the business Result envelope', async () => {
    const current = { userId: 1, username: 'hr01' }
    const get = vi.spyOn(http, 'get').mockResolvedValue({
      data: { code: 200, message: 'success', data: current },
    } as never)
    await expect(getCurrentUser()).resolves.toBe(current)
    expect(get).toHaveBeenCalledWith('/auth/me')
  })

  it('sends profile and password updates to separate endpoints', async () => {
    const put = vi.spyOn(http, 'put').mockResolvedValue({
      data: { code: 200, message: 'success', data: null },
    } as never)
    const profile = { realName: '张三', phone: '13900000001', email: 'z@example.com' }
    const password = { oldPassword: 'old123', newPassword: 'new123', confirmPassword: 'new123' }
    await updateCurrentUser(profile)
    await updateCurrentPassword(password)
    expect(put).toHaveBeenNthCalledWith(1, '/users/me', profile)
    expect(put).toHaveBeenNthCalledWith(2, '/users/me/password', password)
  })
})
