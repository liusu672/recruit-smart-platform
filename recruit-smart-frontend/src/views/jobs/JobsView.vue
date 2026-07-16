<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { Eye, Pencil, Plus, RefreshCw, RotateCcw, Search } from 'lucide-vue-next'
import { computed, reactive, ref } from 'vue'

import JobDetailDrawer from '@/components/jobs/JobDetailDrawer.vue'
import JobFormDrawer from '@/components/jobs/JobFormDrawer.vue'
import { jobDepartmentOptions, jobStatusOptions, getJobStatusTone } from '@/config/jobs'
import { useJobManagement } from '@/composables/useJobManagement'
import type { JobPosition, JobQuery, JobUpdateRequest } from '@/types/job'

const {
  query,
  demoMode,
  selectedJobId,
  jobsQuery,
  detailQuery,
  createMutation,
  updateMutation,
  publishMutation,
  closeMutation,
  isMutating,
  applyFilters,
  resetFilters,
  useDemoData,
  useApiData,
  openDetail,
  closeDetail,
} = useJobManagement()

const filterForm = reactive<Pick<JobQuery, 'keyword' | 'department' | 'status'>>({
  keyword: '',
  department: '',
  status: '',
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
}

function clearFilters() {
  Object.assign(filterForm, { keyword: '', department: '', status: '' })
  resetFilters()
}

function openCreate() {
  editingJob.value = null
  formVisible.value = true
}

function openEdit(job: JobPosition) {
  closeDetail()
  editingJob.value = job
  formVisible.value = true
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
</script>

<template>
  <div class="jobs-view">
    <header class="jobs-view__intro">
      <div>
        <h2 class="rs-section-title">职位列表</h2>
        <p>维护职位草稿、发布状态与招聘人数，关键状态变更由 HR 人工确认。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">创建职位</el-button>
    </header>

    <el-alert
      v-if="demoMode"
      title="当前使用演示数据，创建、编辑和状态操作只在本次页面会话中生效。"
      type="warning"
      :closable="false"
      show-icon
    >
      <template #default>
        <el-button link type="primary" @click="useApiData">重新连接后端接口</el-button>
      </template>
    </el-alert>

    <section class="jobs-toolbar" aria-label="职位筛选">
      <el-input
        v-model="filterForm.keyword"
        clearable
        placeholder="搜索职位名称"
        class="jobs-toolbar__search"
        @keyup.enter="runSearch"
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
      <el-button type="primary" :icon="Search" @click="runSearch">查询</el-button>
      <el-button :icon="RotateCcw" @click="clearFilters">重置</el-button>
      <el-button
        :icon="RefreshCw"
        :loading="jobsQuery.isFetching.value"
        aria-label="刷新职位列表"
        @click="jobsQuery.refetch()"
      >
        刷新
      </el-button>
      <span class="jobs-toolbar__source"> 数据来源：{{ demoMode ? '演示数据' : '后端接口' }} </span>
    </section>

    <section v-if="listError && !demoMode" class="jobs-error" role="alert">
      <div>
        <h3>职位接口暂时不可用</h3>
        <p>{{ listError.message }}。可以重试接口，或切换到明确标识的演示数据继续查看页面。</p>
      </div>
      <div class="jobs-error__actions">
        <el-button :loading="jobsQuery.isFetching.value" @click="jobsQuery.refetch()"
          >重试接口</el-button
        >
        <el-button type="primary" @click="useDemoData">使用演示数据</el-button>
      </div>
    </section>

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
            <span class="rs-tabular-number">{{
              row.salaryRange ? `${row.salaryRange} 元` : '待补充'
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="headcount" label="招聘人数" width="100" align="right">
          <template #default="{ row }: { row: JobPosition }">
            <span class="rs-tabular-number">{{ row.headcount }} 人</span>
          </template>
        </el-table-column>
        <el-table-column prop="statusText" label="状态" width="100">
          <template #default="{ row }: { row: JobPosition }">
            <span class="rs-status-pill" :class="`rs-status-pill--${getJobStatusTone(row.status)}`">
              {{ row.statusText }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="最近更新" width="130" align="right">
          <template #default="{ row }: { row: JobPosition }">
            <span class="rs-tabular-number">{{ formatDate(row.updatedAt || row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="112" fixed="right" align="right">
          <template #default="{ row }: { row: JobPosition }">
            <div class="job-row-actions" @click.stop>
              <el-tooltip content="查看详情" placement="top">
                <el-button
                  circle
                  :icon="Eye"
                  aria-label="查看职位详情"
                  @click="openDetail(row.id)"
                />
              </el-tooltip>
              <el-tooltip content="编辑职位" placement="top">
                <el-button
                  circle
                  :icon="Pencil"
                  :disabled="row.status === 'CLOSED'"
                  aria-label="编辑职位"
                  @click="openEdit(row)"
                />
              </el-tooltip>
            </div>
          </template>
        </el-table-column>

        <template #empty>
          <div class="jobs-empty">
            <h3>没有符合条件的职位</h3>
            <p>调整筛选条件，或创建一个新的职位草稿。</p>
            <el-button type="primary" :icon="Plus" @click="openCreate">创建职位</el-button>
          </div>
        </template>
      </el-table>

      <footer class="jobs-pagination">
        <span>共 {{ total }} 个职位</span>
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          background
          layout="prev, pager, next"
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
      @edit="openEdit"
      @publish="confirmPublish"
      @close-job="confirmClose"
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
  display: flex;
  align-items: center;
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
  display: flex;
  gap: var(--rs-space-2);
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
