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

  it('exposes messages and settings to every role without restoring the fake AI approval nav', () => {
    for (const workspace of Object.values(ROLE_WORKSPACES)) {
      const paths = workspace.navItems.map((item) => item.to)
      expect(paths.some((path) => path.endsWith('/messages'))).toBe(true)
      expect(paths.some((path) => path.endsWith('/settings'))).toBe(true)
    }
    expect(ROLE_WORKSPACES.HR.navItems.map((item) => item.to)).not.toContain('/hr/ai-approvals')
  })

  it('groups the HR navigation by business responsibility', () => {
    expect(new Set(ROLE_WORKSPACES.HR.navItems.map((item) => item.group))).toEqual(
      new Set(['概览', '招聘管理', '员工管理', '系统管理']),
    )
  })
})
