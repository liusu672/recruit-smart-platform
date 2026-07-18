import { afterEach, describe, expect, it, vi } from 'vitest'

import {
  adaptAdminUserPage,
  createAdminUser,
  getAdminRoles,
  getAdminUserById,
  getAdminUsers,
  resetAdminUserPassword,
  updateAdminUser,
  updateAdminUserRole,
  updateAdminUserStatus,
} from '@/api/adminUsers'
import { http } from '@/api/http'

describe('administrator user API contracts', () => {
  afterEach(() => vi.restoreAllMocks())

  it('adapts backend pagination and sends only active filters', async () => {
    const get = vi.spyOn(http, 'get').mockResolvedValue({
      data: {
        code: 200,
        message: 'success',
        data: { total: 1, records: [{ id: 2, username: 'hr01' }] },
      },
    } as never)

    await expect(
      getAdminUsers({
        page: 2,
        pageSize: 20,
        keyword: '林',
        roleId: 2,
        status: 0,
      }),
    ).resolves.toMatchObject({ page: 2, pageSize: 20, total: 1 })
    expect(get).toHaveBeenCalledWith('/admin/users', {
      params: { pageNum: 2, pageSize: 20, keyword: '林', roleId: 2, status: 0 },
    })
  })

  it('keeps records unchanged when adapting a user page', () => {
    const records = [{ id: 1 }] as never
    expect(adaptAdminUserPage({ total: 1, records }, 1, 10)).toEqual({
      items: records,
      page: 1,
      pageSize: 10,
      total: 1,
    })
  })

  it('uses the dedicated user detail and role option endpoints', async () => {
    const get = vi.spyOn(http, 'get').mockResolvedValue({
      data: { code: 200, message: 'success', data: [] },
    } as never)
    await getAdminRoles()
    get.mockResolvedValueOnce({
      data: { code: 200, message: 'success', data: { id: 9 } },
    } as never)
    await getAdminUserById(9)
    expect(get).toHaveBeenNthCalledWith(1, '/admin/roles')
    expect(get).toHaveBeenNthCalledWith(2, '/admin/users/9')
  })

  it('maps create, update, status, role and password actions exactly', async () => {
    const post = vi.spyOn(http, 'post').mockResolvedValue({
      data: { code: 200, message: 'success', data: 11 },
    } as never)
    const put = vi.spyOn(http, 'put').mockResolvedValue({
      data: { code: 200, message: 'success', data: null },
    } as never)
    const create = {
      username: 'hr03',
      password: '123456',
      realName: '沈知夏',
      phone: '',
      email: '',
      roleId: 2,
    }
    const profile = { realName: '沈知夏', phone: '', email: 'shen@example.com' }
    const password = { newPassword: 'new123', confirmPassword: 'new123' }

    await expect(createAdminUser(create)).resolves.toBe(11)
    await updateAdminUser(11, profile)
    await updateAdminUserStatus(11, { status: 0 })
    await updateAdminUserRole(11, { roleId: 3 })
    await resetAdminUserPassword(11, password)

    expect(post).toHaveBeenCalledWith('/admin/users', create)
    expect(put).toHaveBeenNthCalledWith(1, '/admin/users/11', profile)
    expect(put).toHaveBeenNthCalledWith(2, '/admin/users/11/status', { status: 0 })
    expect(put).toHaveBeenNthCalledWith(3, '/admin/users/11/role', { roleId: 3 })
    expect(put).toHaveBeenNthCalledWith(4, '/admin/users/11/reset-password', password)
  })
})
