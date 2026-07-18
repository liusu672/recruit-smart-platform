import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { computed, reactive, ref } from 'vue'

import {
  createAdminUser,
  getAdminRoles,
  getAdminUserById,
  getAdminUsers,
  resetAdminUserPassword,
  updateAdminUser,
  updateAdminUserRole,
  updateAdminUserStatus,
} from '@/api/adminUsers'
import type {
  AdminPasswordResetRequest,
  AdminUserCreateRequest,
  AdminUserQuery,
  AdminUserRoleUpdateRequest,
  AdminUserStatusUpdateRequest,
  AdminUserUpdateRequest,
} from '@/types/adminUser'

export function useAdminUserManagement() {
  const queryClient = useQueryClient()
  const selectedId = ref<number | null>(null)
  const query = reactive<AdminUserQuery>({
    keyword: '',
    roleId: null,
    status: '',
    page: 1,
    pageSize: 10,
  })

  const listQuery = useQuery({
    queryKey: computed(() => [
      'admin-users',
      query.keyword,
      query.roleId,
      query.status,
      query.page,
      query.pageSize,
    ]),
    queryFn: () => getAdminUsers({ ...query }),
  })
  const rolesQuery = useQuery({ queryKey: ['admin-roles'], queryFn: getAdminRoles })
  const detailQuery = useQuery({
    queryKey: computed(() => ['admin-user-detail', selectedId.value]),
    enabled: computed(() => selectedId.value !== null),
    queryFn: () => {
      if (selectedId.value === null) throw new Error('尚未选择用户')
      return getAdminUserById(selectedId.value)
    },
  })

  async function refreshUsers() {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['admin-users'] }),
      queryClient.invalidateQueries({ queryKey: ['admin-user-detail'] }),
    ])
  }

  const createMutation = useMutation({ mutationFn: createAdminUser, onSuccess: refreshUsers })
  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: AdminUserUpdateRequest }) =>
      updateAdminUser(id, data),
    onSuccess: refreshUsers,
  })
  const statusMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: AdminUserStatusUpdateRequest }) =>
      updateAdminUserStatus(id, data),
    onSuccess: refreshUsers,
  })
  const roleMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: AdminUserRoleUpdateRequest }) =>
      updateAdminUserRole(id, data),
    onSuccess: refreshUsers,
  })
  const passwordMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: AdminPasswordResetRequest }) =>
      resetAdminUserPassword(id, data),
  })

  return {
    query,
    selectedId,
    listQuery,
    rolesQuery,
    detailQuery,
    createMutation,
    updateMutation,
    statusMutation,
    roleMutation,
    passwordMutation,
    applyFilters: (filters: Pick<AdminUserQuery, 'keyword' | 'roleId' | 'status'>) =>
      Object.assign(query, filters, { page: 1 }),
    resetFilters: () => Object.assign(query, { keyword: '', roleId: null, status: '', page: 1 }),
    openDetail: (id: number) => {
      selectedId.value = id
    },
    closeDetail: () => {
      selectedId.value = null
    },
    createUser: (data: AdminUserCreateRequest) => createMutation.mutateAsync(data),
  }
}
