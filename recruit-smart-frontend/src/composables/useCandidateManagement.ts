import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { computed, reactive, ref } from 'vue'

import { createCandidate, getCandidateById, getCandidates } from '@/api/candidates'
import {
  createDemoCandidate,
  getDemoCandidatePage,
  initialDemoCandidates,
} from '@/config/demoCandidates'
import type { CandidateCreateRequest, CandidateDetail, CandidateQuery } from '@/types/candidate'

function cloneDemoCandidates(): CandidateDetail[] {
  return structuredClone(initialDemoCandidates)
}

export function useCandidateManagement() {
  const queryClient = useQueryClient()
  const demoMode = ref(false)
  const demoRecords = ref<CandidateDetail[]>(cloneDemoCandidates())
  const selectedCandidateId = ref<number | null>(null)
  const query = reactive<CandidateQuery>({
    keyword: '',
    education: '',
    school: '',
    yearsOfExperienceMin: null,
    currentStatus: '',
    page: 1,
    pageSize: 10,
  })

  const candidatesQuery = useQuery({
    queryKey: computed(() => [
      'candidates',
      demoMode.value ? 'demo' : 'api',
      query.keyword,
      query.education,
      query.school,
      query.yearsOfExperienceMin,
      query.currentStatus,
      query.page,
      query.pageSize,
    ]),
    queryFn: () =>
      demoMode.value
        ? Promise.resolve(getDemoCandidatePage(demoRecords.value, { ...query }))
        : getCandidates({ ...query }),
  })

  const detailQuery = useQuery({
    queryKey: computed(() => [
      'candidate-detail',
      demoMode.value ? 'demo' : 'api',
      selectedCandidateId.value,
    ]),
    enabled: computed(() => selectedCandidateId.value !== null),
    queryFn: async () => {
      const id = selectedCandidateId.value
      if (id === null) throw new Error('尚未选择候选人')

      if (demoMode.value) {
        const candidate = demoRecords.value.find((item) => item.id === id)
        if (!candidate) throw new Error('演示候选人不存在')
        return structuredClone(candidate)
      }

      return getCandidateById(id)
    },
  })

  const createMutation = useMutation({
    mutationFn: async (data: CandidateCreateRequest) => {
      if (!demoMode.value) return createCandidate(data)

      const duplicate = demoRecords.value.some(
        (candidate) =>
          (data.phone && candidate.phone === data.phone) ||
          (data.email && candidate.email === data.email),
      )
      if (duplicate) throw new Error('手机号或邮箱已关联其他候选人，请先检查重复记录')

      const id = Math.max(0, ...demoRecords.value.map((item) => item.id)) + 1
      demoRecords.value.unshift(createDemoCandidate(id, data))
      return id
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['candidates'] })
    },
  })

  function applyFilters(
    filters: Pick<
      CandidateQuery,
      'keyword' | 'education' | 'school' | 'yearsOfExperienceMin' | 'currentStatus'
    >,
  ) {
    Object.assign(query, filters, { page: 1 })
  }

  function resetFilters() {
    Object.assign(query, {
      keyword: '',
      education: '',
      school: '',
      yearsOfExperienceMin: null,
      currentStatus: '',
      page: 1,
    })
  }

  function useDemoData() {
    demoMode.value = true
    query.page = 1
    selectedCandidateId.value = null
  }

  function useApiData() {
    demoMode.value = false
    query.page = 1
    selectedCandidateId.value = null
  }

  function selectCandidate(id: number) {
    selectedCandidateId.value = id
  }

  function closePreview() {
    selectedCandidateId.value = null
  }

  return {
    query,
    demoMode,
    selectedCandidateId,
    candidatesQuery,
    detailQuery,
    createMutation,
    applyFilters,
    resetFilters,
    useDemoData,
    useApiData,
    selectCandidate,
    closePreview,
  }
}
