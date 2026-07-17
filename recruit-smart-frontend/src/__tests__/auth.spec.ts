import { describe, expect, it } from 'vitest'

import { normalizeRole, toLoginPayload } from '@/api/auth'
import { ApiError } from '@/types/api'

describe('auth role normalization', () => {
  it.each(['ADMIN', 'HR', 'INTERVIEWER', 'CANDIDATE'] as const)(
    'accepts supported role %s',
    (role) => {
      expect(normalizeRole(role)).toBe(role)
    },
  )

  it('rejects missing or unknown roles instead of granting HR access', () => {
    expect(() => normalizeRole(undefined)).toThrow(ApiError)
    expect(() => normalizeRole('UNKNOWN')).toThrow('账号角色未配置')
  })

  it('adapts registration login response into candidate session payload', () => {
    const payload = toLoginPayload({
      token: 'token-1',
      tokenType: 'Bearer',
      userInfo: {
        userId: 42,
        username: 'candidate42',
        realName: '候选人四二',
        phone: '13900000042',
        email: null,
        roleCode: 'CANDIDATE',
        roleName: '候选人',
        roleId: 4,
        status: 1,
      },
    })

    expect(payload).toMatchObject({
      token: 'token-1',
      tokenType: 'Bearer',
      user: {
        id: '42',
        username: 'candidate42',
        name: '候选人四二',
        role: 'CANDIDATE',
        phone: '13900000042',
      },
    })
  })
})
