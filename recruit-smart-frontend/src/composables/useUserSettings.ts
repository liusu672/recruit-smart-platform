import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'

import { toAuthUser } from '@/api/auth'
import { getCurrentUser, updateCurrentPassword, updateCurrentUser } from '@/api/user'
import { useSessionStore } from '@/stores/session'
import type { PasswordUpdateRequest, UserProfileUpdateRequest } from '@/types/user'

export function useUserSettings() {
  const session = useSessionStore()
  const queryClient = useQueryClient()
  const userQuery = useQuery({ queryKey: ['current-user'], queryFn: getCurrentUser })

  const profileMutation = useMutation({
    mutationFn: (data: UserProfileUpdateRequest) => updateCurrentUser(data),
    onSuccess: async () => {
      const currentUser = await queryClient.fetchQuery({
        queryKey: ['current-user'],
        queryFn: getCurrentUser,
      })
      session.updateUser(toAuthUser(currentUser))
    },
  })

  const passwordMutation = useMutation({
    mutationFn: (data: PasswordUpdateRequest) => updateCurrentPassword(data),
  })

  return { userQuery, profileMutation, passwordMutation }
}
