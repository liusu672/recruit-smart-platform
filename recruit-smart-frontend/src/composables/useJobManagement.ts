import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { computed, reactive, ref } from 'vue'

import {
  closeJob,
  createJob,
  getJobById,
  getJobs,
  pauseJob,
  publishJob,
  resumeJob,
  updateJob,
} from '@/api/jobs'
import { getDemoJobPage, initialDemoJobs } from '@/config/demoJobs'
import type {
  JobCreateRequest,
  JobPosition,
  JobQuery,
  JobUpdateCommand,
  JobUpdateRequest,
} from '@/types/job'

function cloneDemoJobs() {
  return initialDemoJobs.map((job) => ({ ...job }))
}

export function useJobManagement() {
  const queryClient = useQueryClient()
  const demoMode = ref(false)
  const demoRecords = ref<JobPosition[]>(cloneDemoJobs())
  const selectedJobId = ref<number | null>(null)
  const query = reactive<JobQuery>({
    keyword: '',
    department: '',
    status: '',
    page: 1,
    pageSize: 10,
  })

  const jobsQuery = useQuery({
    queryKey: computed(() => [
      'jobs',
      demoMode.value ? 'demo' : 'api',
      query.keyword,
      query.department,
      query.status,
      query.page,
      query.pageSize,
    ]),
    queryFn: () =>
      demoMode.value
        ? Promise.resolve(getDemoJobPage(demoRecords.value, { ...query }))
        : getJobs({ ...query }),
  })

  const detailQuery = useQuery({
    queryKey: computed(() => ['job-detail', demoMode.value ? 'demo' : 'api', selectedJobId.value]),
    enabled: computed(() => selectedJobId.value !== null),
    queryFn: async () => {
      const id = selectedJobId.value
      if (id === null) throw new Error('尚未选择职位')

      if (demoMode.value) {
        const job = demoRecords.value.find((item) => item.id === id)
        if (!job) throw new Error('演示职位不存在')
        return { ...job }
      }

      return getJobById(id)
    },
  })

  async function refreshQueries() {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['jobs'] }),
      queryClient.invalidateQueries({ queryKey: ['job-detail'] }),
    ])
  }

  const createMutation = useMutation({
    mutationFn: async (data: JobCreateRequest) => {
      if (!demoMode.value) return createJob(data)

      const id = Math.max(0, ...demoRecords.value.map((item) => item.id)) + 1
      const now = new Date().toISOString()
      demoRecords.value.unshift({
        id,
        title: data.title,
        department: data.department,
        location: data.location,
        jobType: 'FULL_TIME',
        salaryRange: `${data.salaryMin}-${data.salaryMax}`,
        headcount: data.headcount,
        experienceRequirement: null,
        educationRequirement: null,
        description: data.responsibilities,
        requirement: data.requirements,
        status: 'DRAFT',
        statusText: '草稿',
        createdAt: now,
        updatedAt: now,
      })
      return id
    },
    onSuccess: refreshQueries,
  })

  const updateMutation = useMutation({
    mutationFn: async ({ id, data }: JobUpdateCommand) => {
      if (!demoMode.value) return updateJob(id, data)

      const job = demoRecords.value.find((item) => item.id === id)
      if (!job) throw new Error('演示职位不存在')
      Object.assign(job, {
        title: data.title,
        department: data.department,
        location: data.location,
        salaryRange: `${data.salaryMin}-${data.salaryMax}`,
        headcount: data.headcount,
        description: data.responsibilities,
        requirement: data.requirements,
        updatedAt: new Date().toISOString(),
      } satisfies Partial<JobPosition>)
    },
    onSuccess: refreshQueries,
  })

  const publishMutation = useMutation({
    mutationFn: async (id: number) => {
      if (!demoMode.value) return publishJob(id)

      const job = demoRecords.value.find((item) => item.id === id)
      if (!job || job.status !== 'DRAFT') throw new Error('只有草稿职位可以发布')
      job.status = 'OPEN'
      job.statusText = '招聘中'
      job.updatedAt = new Date().toISOString()
    },
    onSuccess: refreshQueries,
  })

  const pauseMutation = useMutation({
    mutationFn: async (id: number) => {
      if (!demoMode.value) return pauseJob(id)

      const job = demoRecords.value.find((item) => item.id === id)
      if (!job || job.status !== 'OPEN') throw new Error('只有招聘中的职位可以暂停')
      job.status = 'PAUSED'
      job.statusText = '已暂停'
      job.updatedAt = new Date().toISOString()
    },
    onSuccess: refreshQueries,
  })

  const resumeMutation = useMutation({
    mutationFn: async (id: number) => {
      if (!demoMode.value) return resumeJob(id)

      const job = demoRecords.value.find((item) => item.id === id)
      if (!job || job.status !== 'PAUSED') throw new Error('只有已暂停的职位可以恢复')
      job.status = 'OPEN'
      job.statusText = '招聘中'
      job.updatedAt = new Date().toISOString()
    },
    onSuccess: refreshQueries,
  })

  const closeMutation = useMutation({
    mutationFn: async (id: number) => {
      if (!demoMode.value) return closeJob(id)

      const job = demoRecords.value.find((item) => item.id === id)
      if (!job || (job.status !== 'OPEN' && job.status !== 'PAUSED'))
        throw new Error('只有招聘中或已暂停的职位可以关闭')
      job.status = 'CLOSED'
      job.statusText = '已关闭'
      job.updatedAt = new Date().toISOString()
    },
    onSuccess: refreshQueries,
  })

  function applyFilters(filters: Pick<JobQuery, 'keyword' | 'department' | 'status'>) {
    Object.assign(query, filters, { page: 1 })
  }

  function resetFilters() {
    Object.assign(query, { keyword: '', department: '', status: '', page: 1 })
  }

  function useDemoData() {
    demoMode.value = true
    query.page = 1
  }

  function useApiData() {
    demoMode.value = false
    query.page = 1
  }

  function openDetail(id: number) {
    selectedJobId.value = id
  }

  function closeDetail() {
    selectedJobId.value = null
  }

  const isMutating = computed(
    () =>
      createMutation.isPending.value ||
      updateMutation.isPending.value ||
      publishMutation.isPending.value ||
      pauseMutation.isPending.value ||
      resumeMutation.isPending.value ||
      closeMutation.isPending.value,
  )

  return {
    query,
    demoMode,
    selectedJobId,
    jobsQuery,
    detailQuery,
    createMutation,
    updateMutation,
    publishMutation,
    pauseMutation,
    resumeMutation,
    closeMutation,
    isMutating,
    applyFilters,
    resetFilters,
    useDemoData,
    useApiData,
    openDetail,
    closeDetail,
  }
}

export function toJobUpdateRequest(job: JobPosition, salary: [number, number]): JobUpdateRequest {
  return {
    title: job.title,
    department: job.department,
    location: job.location ?? '',
    salaryMin: salary[0],
    salaryMax: salary[1],
    headcount: job.headcount,
    responsibilities: job.description ?? '',
    requirements: job.requirement ?? '',
  }
}
