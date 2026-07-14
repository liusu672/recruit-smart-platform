import { describe, expect, it } from 'vitest'

import { normalizeRole } from '@/api/auth'
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
})
