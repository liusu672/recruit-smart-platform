import { describe, expect, it } from 'vitest'

import { canRoleAccess, getRoleHomePath, ROLE_WORKSPACES } from '@/config/roleAccess'

describe('role workspace isolation', () => {
  it.each([
    ['CANDIDATE', '/candidate/home'],
    ['HR', '/hr/dashboard'],
    ['INTERVIEWER', '/interviewer/dashboard'],
    ['ADMIN', '/admin/dashboard'],
  ] as const)('routes %s to its own workspace', (role, path) => {
    expect(getRoleHomePath(role)).toBe(path)
  })

  it('does not expose HR navigation to candidates or administrators', () => {
    expect(ROLE_WORKSPACES.CANDIDATE.navItems.map((item) => item.to)).not.toContain(
      '/hr/candidates',
    )
    expect(ROLE_WORKSPACES.ADMIN.navItems.map((item) => item.to)).not.toContain('/hr/offers')
    expect(ROLE_WORKSPACES.ADMIN.navItems.map((item) => item.to)).toContain('/admin/audit')
  })

  it('fails closed when a role is missing or not allowed', () => {
    expect(canRoleAccess(['HR'], 'CANDIDATE')).toBe(false)
    expect(canRoleAccess(['INTERVIEWER'], null)).toBe(false)
    expect(canRoleAccess(['CANDIDATE'], 'CANDIDATE')).toBe(true)
  })
})
