import type { JobPosition, JobQuery } from '@/types/job'

export const initialDemoJobs: JobPosition[] = [
  {
    id: 101,
    title: 'Java 后端开发工程师',
    department: '技术部',
    location: '武汉',
    jobType: 'FULL_TIME',
    salaryRange: '12000-18000',
    headcount: 3,
    experienceRequirement: '3 年以上',
    educationRequirement: '本科',
    description: '负责招聘平台业务服务、接口设计和核心流程实现。',
    requirement: '熟悉 Java、Spring Boot、MySQL 和常见微服务组件。',
    status: 'OPEN',
    statusText: '招聘中',
    createdAt: '2026-07-08T09:30:00',
    updatedAt: '2026-07-14T16:20:00',
  },
  {
    id: 102,
    title: '招聘产品经理',
    department: '产品部',
    location: '上海',
    jobType: 'FULL_TIME',
    salaryRange: '18000-26000',
    headcount: 1,
    experienceRequirement: '5 年以上',
    educationRequirement: '本科',
    description: '负责招聘主流程产品规划、需求分析和跨团队交付。',
    requirement: '具备企业软件或招聘产品经验，能够推动复杂业务落地。',
    status: 'DRAFT',
    statusText: '草稿',
    createdAt: '2026-07-12T11:10:00',
    updatedAt: '2026-07-12T11:10:00',
  },
  {
    id: 103,
    title: 'UI/UX 设计师',
    department: '设计部',
    location: '深圳',
    jobType: 'FULL_TIME',
    salaryRange: '15000-22000',
    headcount: 2,
    experienceRequirement: '3 年以上',
    educationRequirement: '本科',
    description: '负责招聘工作台的交互设计、视觉规范和可用性验证。',
    requirement: '熟悉企业级产品设计，重视信息密度、无障碍与设计系统。',
    status: 'CLOSED',
    statusText: '已关闭',
    createdAt: '2026-06-20T14:00:00',
    updatedAt: '2026-07-10T18:00:00',
  },
]

export function getDemoJobPage(records: JobPosition[], query: JobQuery) {
  const keyword = query.keyword.trim().toLocaleLowerCase()
  const filtered = records.filter((job) => {
    const matchesKeyword = !keyword || job.title.toLocaleLowerCase().includes(keyword)
    const matchesDepartment = !query.department || job.department === query.department
    const matchesStatus = !query.status || job.status === query.status
    return matchesKeyword && matchesDepartment && matchesStatus
  })
  const start = (query.page - 1) * query.pageSize

  return {
    items: filtered.slice(start, start + query.pageSize),
    page: query.page,
    pageSize: query.pageSize,
    total: filtered.length,
  }
}
