<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { BriefcaseBusiness, MoreHorizontal, Plus } from 'lucide-vue-next'
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import HrEmptyState from '@/components/hr/HrEmptyState.vue'
import HrErrorState from '@/components/hr/HrErrorState.vue'
import HrFilterBar from '@/components/hr/HrFilterBar.vue'
import HrPageHeader from '@/components/hr/HrPageHeader.vue'
import HrStatusBadge from '@/components/hr/HrStatusBadge.vue'
import JobDetailDrawer from '@/components/jobs/JobDetailDrawer.vue'
import JobFormDrawer from '@/components/jobs/JobFormDrawer.vue'
import { jobDepartmentOptions, jobStatusOptions, getJobStatusTone } from '@/config/jobs'
import { useHrUrlFilters } from '@/composables/useHrUrlFilters'
import { useJobManagement } from '@/composables/useJobManagement'
import type { JobPosition, JobQuery, JobStatus, JobUpdateRequest } from '@/types/job'

const {
  query,
  demoMode,
  selectedJobId,
  jobsQuery,
  detailQuery,
  applicationsQuery,
  createMutation,
  updateMutation,
  publishMutation,
  pauseMutation,
  resumeMutation,
  closeMutation,
  isMutating,
  applyFilters,
  resetFilters,
  useApiData,
  openDetail,
  closeDetail,
} = useJobManagement()
const router = useRouter()
const urlFilters = useHrUrlFilters(['keyword', 'department', 'status', 'page', 'pageSize'])

const filterForm = reactive<Pick<JobQuery, 'keyword' | 'department' | 'status'>>({
  keyword: urlFilters.readString('keyword'),
  department: urlFilters.readString('department'),
  status: urlFilters.readString('status') as JobStatus | '',
})
Object.assign(query, filterForm, {
  page: urlFilters.readNumber('page', 1),
  pageSize: urlFilters.readNumber('pageSize', 10),
})
const formVisible = ref(false)
const editingJob = ref<JobPosition | null>(null)
const detailVisible = computed({
  get: () => selectedJobId.value !== null,
  set: (value: boolean) => {
    if (!value) closeDetail()
  },
})
const jobs = computed(() => jobsQuery.data.value?.items ?? [])
const total = computed(() => jobsQuery.data.value?.total ?? 0)
const listError = computed(() => jobsQuery.error.value as Error | null)
const detailError = computed(() => detailQuery.error.value as Error | null)
const applicationsError = computed(() => applicationsQuery.error.value as Error | null)
const activeFilterCount = computed(
  () =>
    [filterForm.keyword.trim(), filterForm.department, filterForm.status].filter(Boolean).length,
)

function formatDate(value: string | null) {
  if (!value) return '暂无记录'
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(new Date(value))
}

function runSearch() {
  applyFilters({ ...filterForm })
  syncUrl(1)
}

function clearFilters() {
  Object.assign(filterForm, { keyword: '', department: '', status: '' })
  resetFilters()
  syncUrl(1)
}

function syncUrl(page = query.page) {
  urlFilters.sync({
    keyword: filterForm.keyword.trim(),
    department: filterForm.department,
    status: filterForm.status,
    page: page > 1 ? page : null,
    pageSize: query.pageSize !== 10 ? query.pageSize : null,
  })
}

watch([() => query.page, () => query.pageSize], () => syncUrl())

function openCreate() {
  editingJob.value = null
  formVisible.value = true
}

function openEdit(job: JobPosition) {
  closeDetail()
  editingJob.value = job
  formVisible.value = true
}

function viewPipeline(jobId: number) {
  void router.push({ path: '/hr/pipeline', query: { jobId: String(jobId) } })
}

function showError(error: unknown) {
  ElMessage.error(error instanceof Error ? error.message : '操作失败，请稍后重试')
}

async function submitJob(data: JobUpdateRequest) {
  try {
    if (editingJob.value) {
      await updateMutation.mutateAsync({ id: editingJob.value.id, data })
      ElMessage.success('职位信息已更新')
    } else {
      await createMutation.mutateAsync(data)
      ElMessage.success(demoMode.value ? '演示职位草稿已创建' : '职位草稿已创建')
    }
    formVisible.value = false
  } catch (error) {
    showError(error)
  }
}

async function confirmPublish(job: JobPosition) {
  try {
    await ElMessageBox.confirm(`发布后候选人将可以看到“${job.title}”，确认继续吗？`, '发布职位', {
      confirmButtonText: '确认发布',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await publishMutation.mutateAsync(job.id)
    ElMessage.success('职位已发布')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    showError(error)
  }
}

async function confirmPause(job: JobPosition) {
  try {
    await ElMessageBox.confirm(
      `暂停后“${job.title}”将不再接收新的候选人投递，历史记录仍会保留。`,
      '暂停职位',
      {
        confirmButtonText: '确认暂停',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await pauseMutation.mutateAsync(job.id)
    ElMessage.success('职位已暂停')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    showError(error)
  }
}

async function confirmResume(job: JobPosition) {
  try {
    await ElMessageBox.confirm(
      `恢复后候选人将可以继续投递“${job.title}”，确认继续吗？`,
      '恢复职位',
      {
        confirmButtonText: '确认恢复',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await resumeMutation.mutateAsync(job.id)
    ElMessage.success('职位已恢复招聘')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    showError(error)
  }
}

async function confirmClose(job: JobPosition) {
  try {
    await ElMessageBox.confirm(
      `关闭后将停止“${job.title}”的新投递，历史记录仍会保留。`,
      '关闭职位',
      {
        confirmButtonText: '确认关闭',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await closeMutation.mutateAsync(job.id)
    ElMessage.success('职位已关闭')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    showError(error)
  }
}

function handleJobCommand(command: string, job: JobPosition) {
  if (command === 'view') openDetail(job.id)
  if (command === 'edit') openEdit(job)
  if (command === 'publish') void confirmPublish(job)
  if (command === 'pause') void confirmPause(job)
  if (command === 'resume') void confirmResume(job)
  if (command === 'close') void confirmClose(job)
}
</script>

<template>
  <div class="jobs-view">
    <HrPageHeader
      title="职位管理"
      description="维护职位信息与招聘状态，所有关键变更均由 HR 人工确认。"
    >
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openCreate">创建职位</el-button>
      </template>
    </HrPageHeader>

    <el-alert
      v-if="demoMode"
      title="当前使用演示数据，创建、编辑和状态操作只在本次页面会话中生效。"
      type="warning"
      :closable="false"
      show-icon
    >
      <template #default>
        <el-button link type="primary" @click="useApiData">切换到真实数据</el-button>
      </template>
    </el-alert>

    <HrFilterBar
      :loading="jobsQuery.isFetching.value"
      :active-count="activeFilterCount"
      :reset-disabled="activeFilterCount === 0"
      @submit="runSearch"
      @reset="clearFilters"
      @refresh="jobsQuery.refetch()"
    >
      <el-input
        v-model="filterForm.keyword"
        clearable
        placeholder="搜索职位名称"
        class="jobs-toolbar__search"
      />
      <el-select v-model="filterForm.department" clearable placeholder="全部部门">
        <el-option
          v-for="department in jobDepartmentOptions"
          :key="department"
          :label="department"
          :value="department"
        />
      </el-select>
      <el-select v-model="filterForm.status" clearable placeholder="全部状态">
        <el-option
          v-for="status in jobStatusOptions"
          :key="status.value"
          :label="status.label"
          :value="status.value"
        />
      </el-select>
    </HrFilterBar>

    <HrErrorState
      v-if="listError && !demoMode"
      title="职位列表暂时无法加载"
      description="请稍后重试。如果问题持续存在，请联系系统管理员。"
      :loading="jobsQuery.isFetching.value"
      @retry="jobsQuery.refetch()"
    />

    <section v-else class="jobs-table" aria-label="职位数据表格">
      <el-table
        v-loading="jobsQuery.isLoading.value"
        :data="jobs"
        row-key="id"
        height="calc(100dvh - 300px)"
        highlight-current-row
        @row-click="(row: JobPosition) => openDetail(row.id)"
      >
        <el-table-column prop="title" label="职位名称" min-width="220" fixed="left">
          <template #default="{ row }: { row: JobPosition }">
            <div class="job-name-cell">
              <strong>{{ row.title }}</strong>
              <span>#{{ row.id }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" min-width="120" />
        <el-table-column prop="location" label="地点" min-width="100">
          <template #default="{ row }: { row: JobPosition }">{{
            row.location || '待补充'
          }}</template>
        </el-table-column>
        <el-table-column prop="salaryRange" label="月薪范围" min-width="150" align="right">
          <template #default="{ row }: { row: JobPosition }">
            <span class="rs-tabular-number">{{ row.salaryRange || '待补充' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="headcount" label="招聘人数" width="100" align="right">
          <template #default="{ row }: { row: JobPosition }">
            <span class="rs-tabular-number">{{ row.headcount }} 人</span>
          </template>
        </el-table-column>
        <el-table-column prop="statusText" label="状态" width="100">
          <template #default="{ row }: { row: JobPosition }">
            <HrStatusBadge
              :status="row.status"
              :label="row.statusText"
              :tone="getJobStatusTone(row.status)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="最近更新" width="130" align="right">
          <template #default="{ row }: { row: JobPosition }">
            <span class="rs-tabular-number">{{ formatDate(row.updatedAt || row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="right">
          <template #default="{ row }: { row: JobPosition }">
            <div class="job-row-actions" @click.stop>
              <el-button
                class="job-row-actions__view"
                link
                type="primary"
                @click="openDetail(row.id)"
                >查看</el-button
              >
              <el-button
                v-if="row.status === 'DRAFT'"
                class="job-row-actions__edit"
                link
                type="primary"
                @click="openEdit(row)"
                >编辑</el-button
              >
              <span v-else class="hr-action-placeholder job-row-actions__edit" aria-hidden="true"
                >--</span
              >
              <el-button
                v-if="row.status === 'DRAFT'"
                class="job-row-actions__state"
                link
                type="primary"
                @click="confirmPublish(row)"
                >发布</el-button
              >
              <el-button
                v-if="row.status === 'OPEN'"
                class="job-row-actions__state"
                link
                type="primary"
                @click="confirmPause(row)"
                >暂停</el-button
              >
              <el-button
                v-if="row.status === 'PAUSED'"
                class="job-row-actions__state"
                link
                type="primary"
                @click="confirmResume(row)"
                >恢复</el-button
              >
              <span
                v-if="row.status === 'CLOSED'"
                class="hr-action-placeholder job-row-actions__state"
                aria-hidden="true"
                >--</span
              >
              <el-dropdown
                v-if="row.status !== 'CLOSED'"
                class="job-row-actions__more"
                trigger="click"
                @command="(command: string) => handleJobCommand(command, row)"
              >
                <el-button link :icon="MoreHorizontal" aria-label="更多职位操作">更多</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="row.status !== 'DRAFT'" command="edit"
                      >编辑职位</el-dropdown-item
                    >
                    <el-dropdown-item command="view">查看详情</el-dropdown-item>
                    <el-dropdown-item
                      v-if="row.status === 'OPEN' || row.status === 'PAUSED'"
                      divided
                      command="close"
                      >关闭职位</el-dropdown-item
                    >
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <span v-else class="hr-action-placeholder job-row-actions__more" aria-hidden="true"
                >--</span
              >
            </div>
          </template>
        </el-table-column>

        <template #empty>
          <HrEmptyState
            :icon="BriefcaseBusiness"
            title="没有符合条件的职位"
            description="调整筛选条件，或创建一个新的职位草稿。"
          >
            <template #actions
              ><el-button type="primary" :icon="Plus" @click="openCreate"
                >创建职位</el-button
              ></template
            >
          </HrEmptyState>
        </template>
      </el-table>

      <footer class="jobs-pagination">
        <span>共 {{ total }} 个职位</span>
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          background
          layout="sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
          :total="total"
        />
      </footer>
    </section>

    <JobDetailDrawer
      v-model:visible="detailVisible"
      :job="detailQuery.data.value"
      :loading="detailQuery.isLoading.value"
      :error="detailError"
      :demo-mode="demoMode"
      :action-loading="isMutating"
      :applications="applicationsQuery.data.value?.records ?? []"
      :applications-loading="applicationsQuery.isLoading.value"
      :applications-error="applicationsError"
      @edit="openEdit"
      @publish="confirmPublish"
      @pause="confirmPause"
      @resume="confirmResume"
      @close-job="confirmClose"
      @view-pipeline="viewPipeline"
    />

    <JobFormDrawer
      v-model:visible="formVisible"
      :job="editingJob"
      :submitting="createMutation.isPending.value || updateMutation.isPending.value"
      @submit="submitJob"
    />
  </div>
</template>

<style scoped lang="scss">
.jobs-view {
  display: grid;
  gap: var(--rs-space-4);
}

.jobs-view__intro,
.jobs-toolbar,
.jobs-error,
.jobs-pagination,
.job-row-actions {
  display: grid;
  align-items: center;
  justify-content: end;
  grid-template-columns: 40px 40px 40px 60px;
}

.jobs-view__intro,
.jobs-error,
.jobs-pagination {
  justify-content: space-between;
}

.jobs-view__intro p,
.jobs-error p,
.jobs-error h3,
.jobs-empty h3,
.jobs-empty p {
  margin: 0;
}

.jobs-view__intro p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}

.jobs-toolbar {
  gap: var(--rs-space-2);
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.jobs-toolbar__search {
  width: 240px;
}

.jobs-toolbar :deep(.el-select) {
  width: 160px;
}

.jobs-toolbar__source {
  margin-left: auto;
  color: var(--rs-text-tertiary);
  font-size: 12px;
}

.jobs-error {
  min-height: 112px;
  gap: var(--rs-space-6);
  padding: var(--rs-space-4);
  border: 1px solid var(--rs-danger-700);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-danger-050);
}

.jobs-error h3 {
  font-size: 14px;
}

.jobs-error p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}

.jobs-error__actions,
.job-row-actions {
  gap: var(--rs-space-2);
}

.jobs-error__actions {
  display: flex;
}

.job-row-actions__view {
  grid-column: 1;
}

.job-row-actions__edit {
  grid-column: 2;
}

.job-row-actions__state {
  grid-column: 3;
}

.job-row-actions__more {
  grid-column: 4;
  justify-self: start;
}

.jobs-table {
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.jobs-table :deep(.el-table__row) {
  cursor: pointer;
}

.job-name-cell {
  display: grid;
  gap: var(--rs-space-1);
}

.job-name-cell strong {
  color: var(--rs-text-primary);
  font-weight: 600;
}

.job-name-cell span {
  color: var(--rs-text-tertiary);
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}

.rs-tabular-number {
  font-variant-numeric: tabular-nums;
}

.jobs-empty {
  display: grid;
  justify-items: center;
  gap: var(--rs-space-2);
  padding: var(--rs-space-8);
  color: var(--rs-text-secondary);
}

.jobs-empty h3 {
  color: var(--rs-text-primary);
  font-size: 14px;
}

.jobs-pagination {
  min-height: 56px;
  padding: 0 var(--rs-space-4);
  border-top: 1px solid var(--rs-border-default);
  color: var(--rs-text-secondary);
}
</style>
