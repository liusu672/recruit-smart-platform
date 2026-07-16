import { http, unwrapResult } from '@/api/http'
import type { Result } from '@/types/api'
import type { PageResponse, PortalInterview } from '@/types/portal'

// 面试官列表必须由后端按当前账号分配关系过滤，不能退回 HR 全量任务接口。
export async function getMyInterviewerTasks(): Promise<PortalInterview[]> {
  const page = await unwrapResult(
    http.get<Result<PageResponse<PortalInterview>>>('/interviews/interviewer/me', {
      params: { pageNum: 1, pageSize: 100 },
    }),
  )
  return page.records
}
