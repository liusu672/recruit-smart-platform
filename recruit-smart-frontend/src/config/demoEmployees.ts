import type { EmployeeQuery, EmployeeRecord } from '@/types/employee'

export const initialDemoEmployees: EmployeeRecord[] = [
  {
    id: 1001,
    userId: null,
    candidateId: 1,
    onboardingId: 904,
    employeeNo: 'EMP20260709001',
    name: '张三',
    phone: '13900000001',
    email: 'zhangsan@example.com',
    department: '研发部',
    position: 'Java 后端开发工程师',
    entryDate: '2026-07-15',
    status: 'PROBATION',
    statusText: '试用期',
    performanceSummary: '初始入职员工，暂无绩效记录。',
    attendanceSummary: '入职以来考勤正常。',
    satisfactionFeedback: '对岗位内容与团队安排表示认可。',
    turnoverRiskLevel: 'LOW',
    riskAssessedAt: '2026-07-15T18:00:00',
    createdAt: '2026-07-15T09:00:00',
    updatedAt: '2026-07-15T18:00:00',
  },
  {
    id: 1002,
    userId: 42,
    candidateId: 198,
    onboardingId: 870,
    employeeNo: 'EMP20260418007',
    name: '沈清和',
    phone: '13900000198',
    email: 'shenqinghe@example.com',
    department: '产品部',
    position: '产品经理',
    entryDate: '2026-04-18',
    status: 'ACTIVE',
    statusText: '在职',
    performanceSummary: '负责候选人体验优化，季度目标推进正常。',
    attendanceSummary: '近 30 天无异常考勤。',
    satisfactionFeedback: '希望增加跨部门需求评审透明度。',
    turnoverRiskLevel: 'MEDIUM',
    riskAssessedAt: '2026-07-14T10:00:00',
    createdAt: '2026-04-18T09:00:00',
    updatedAt: '2026-07-14T10:00:00',
  },
  {
    id: 1003,
    userId: 51,
    candidateId: 205,
    onboardingId: 881,
    employeeNo: 'EMP20260512011',
    name: '梁嘉树',
    phone: '13900000205',
    email: 'liangjiashu@example.com',
    department: '产品设计部',
    position: 'UI/UX 设计师',
    entryDate: '2026-05-12',
    status: 'ACTIVE',
    statusText: '在职',
    performanceSummary: '完成招聘流程视觉规范更新。',
    attendanceSummary: '考勤正常。',
    satisfactionFeedback: '近期反馈稳定。',
    turnoverRiskLevel: null,
    riskAssessedAt: null,
    createdAt: '2026-05-12T09:00:00',
    updatedAt: '2026-07-12T15:00:00',
  },
]

export function getDemoEmployeePage(records: EmployeeRecord[], query: EmployeeQuery) {
  const keyword = query.keyword.trim().toLocaleLowerCase()
  const filtered = records.filter(
    (record) =>
      (!keyword ||
        [record.name, record.employeeNo, record.position].some((value) =>
          value.toLocaleLowerCase().includes(keyword),
        )) &&
      (!query.department || record.department === query.department) &&
      (!query.status || record.status === query.status),
  )
  const start = (query.page - 1) * query.pageSize
  return {
    items: filtered.slice(start, start + query.pageSize),
    page: query.page,
    pageSize: query.pageSize,
    total: filtered.length,
  }
}
