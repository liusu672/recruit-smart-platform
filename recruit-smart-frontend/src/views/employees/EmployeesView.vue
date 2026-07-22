<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, reactive, ref, watch } from 'vue'
import HrErrorState from '@/components/hr/HrErrorState.vue'
import HrFilterBar from '@/components/hr/HrFilterBar.vue'
import HrPageHeader from '@/components/hr/HrPageHeader.vue'
import EmployeeDetailDrawer from '@/components/employees/EmployeeDetailDrawer.vue'
import EmployeeTable from '@/components/employees/EmployeeTable.vue'
import { useEmployeeDirectory } from '@/composables/useEmployeeDirectory'
import { useHrUrlFilters } from '@/composables/useHrUrlFilters'
import { employeeStatusOptions, getEmployeeStatusText } from '@/config/employees'
import type { EmployeeStatus } from '@/types/employee'
import type { TurnoverRiskResponse } from '@/types/ai'
import type { EmployeeRiskDataUpdateRequest } from '@/api/employees'

const state = useEmployeeDirectory()
const urlFilters = useHrUrlFilters(['keyword', 'department', 'status', 'page', 'pageSize'])
const filterForm = reactive<{ keyword: string; department: string; status: EmployeeStatus | '' }>({
  keyword: urlFilters.readString('keyword'),
  department: urlFilters.readString('department'),
  status: urlFilters.readString('status') as EmployeeStatus | '',
})
Object.assign(state.query, filterForm, {
  page: urlFilters.readNumber('page', 1),
  pageSize: urlFilters.readNumber('pageSize', 10),
})
const records = computed(() => state.listQuery.data.value?.items ?? [])
const total = computed(() => state.listQuery.data.value?.total ?? 0)
const listError = computed(() => state.listQuery.error.value as Error | null)
const detailError = computed(() => state.detailQuery.error.value as Error | null)
const activeFilterCount = computed(
  () =>
    [filterForm.keyword.trim(), filterForm.department, filterForm.status].filter(Boolean).length,
)
const detailVisible = computed({
  get: () => state.selectedId.value !== null,
  set: (value) => {
    if (!value) state.closeDetail()
  },
})
const riskAnalysis = ref<TurnoverRiskResponse | null>(null)
watch(
  () => state.selectedId.value,
  () => {
    riskAnalysis.value = null
  },
)
const departments = ['研发部', '技术部', '产品部', '产品设计部']
function submitFilters() {
  state.applyFilters({
    keyword: filterForm.keyword.trim(),
    department: filterForm.department,
    status: filterForm.status,
  })
  syncUrl(1)
}
function clearFilters() {
  Object.assign(filterForm, { keyword: '', department: '', status: '' })
  state.resetFilters()
  syncUrl(1)
}
function syncUrl(page = state.query.page) {
  urlFilters.sync({
    keyword: filterForm.keyword.trim(),
    department: filterForm.department,
    status: filterForm.status,
    page: page > 1 ? page : null,
    pageSize: state.query.pageSize !== 10 ? state.query.pageSize : null,
  })
}
watch([() => state.query.page, () => state.query.pageSize], () => syncUrl())

async function updateStatus(status: EmployeeStatus) {
  const record = state.detailQuery.data.value
  if (!record || record.status === status) return
  try {
    await ElMessageBox.confirm(
      `确认将“${record.name}”的员工状态改为“${getEmployeeStatusText(status)}”？`,
      '修改员工状态',
      {
        confirmButtonText: '确认修改',
        cancelButtonText: '取消',
        type: status === 'LEFT' ? 'warning' : 'info',
      },
    )
    await state.statusMutation.mutateAsync({ id: record.id, data: { status } })
    ElMessage.success('员工状态已更新')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error instanceof Error ? error.message : '员工状态更新失败')
  }
}

async function assessRisk() {
  const record = state.detailQuery.data.value
  if (!record) return
  if (state.demoMode.value) {
    ElMessage.warning('演示模式不提供风险分析')
    return
  }
  try {
    riskAnalysis.value = await state.riskMutation.mutateAsync(record.id)
    ElMessage.success('AI 风险分析已生成')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : 'AI 风险分析失败')
  }
}

async function saveRiskData(data: EmployeeRiskDataUpdateRequest) {
  const record = state.detailQuery.data.value
  if (!record) return
  try {
    await state.riskDataMutation.mutateAsync({ id: record.id, data })
    riskAnalysis.value = null
    ElMessage.success('风险数据已更新')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '风险数据保存失败')
  }
}
</script>

<template>
  <div class="view">
    <HrPageHeader
      title="员工档案"
      description="查看由已完成入职流程生成的员工资料、在职状态和辅助参考信息。"
    />
    <section v-if="state.demoMode.value" class="source">
      <span><strong>演示数据模式</strong>：以下资料仅用于前端流程演示。</span
      ><el-button link @click="state.useApiData">切换到真实数据</el-button>
    </section>
    <HrFilterBar
      :loading="state.listQuery.isFetching.value"
      :active-count="activeFilterCount"
      :reset-disabled="activeFilterCount === 0"
      @submit="submitFilters"
      @reset="clearFilters"
      @refresh="state.listQuery.refetch()"
    >
      <el-input
        v-model="filterForm.keyword"
        placeholder="姓名、员工编号或岗位"
        clearable
      /><el-select v-model="filterForm.department" placeholder="全部部门" clearable
        ><el-option
          v-for="department in departments"
          :key="department"
          :label="department"
          :value="department" /></el-select
      ><el-select v-model="filterForm.status" placeholder="全部状态" clearable
        ><el-option
          v-for="option in employeeStatusOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
      /></el-select>
    </HrFilterBar>
    <HrErrorState
      v-if="listError && !state.demoMode.value"
      title="员工档案暂时无法加载"
      description="请稍后重试。如果问题持续存在，请联系系统管理员。"
      :loading="state.listQuery.isFetching.value"
      @retry="state.listQuery.refetch()"
    />
    <section v-else class="table">
      <EmployeeTable
        :records="records"
        :loading="state.listQuery.isLoading.value"
        @select="state.openDetail($event.id)"
      />
      <footer>
        <span>共 {{ total }} 份员工档案</span
        ><el-pagination
          v-model:current-page="state.query.page"
          v-model:page-size="state.query.pageSize"
          background
          layout="sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
          :total="total"
        />
      </footer>
    </section>
    <EmployeeDetailDrawer
      v-model:visible="detailVisible"
      :record="state.detailQuery.data.value"
      :loading="state.detailQuery.isLoading.value"
      :error="detailError"
      :updating="state.statusMutation.isPending.value"
      :risk-analysis="riskAnalysis"
      :analyzing-risk="state.riskMutation.isPending.value"
      :saving-risk-data="state.riskDataMutation.isPending.value"
      @update-status="updateStatus"
      @assess-risk="assessRisk"
      @save-risk-data="saveRiskData"
    />
  </div>
</template>

<style scoped lang="scss">
.view {
  display: grid;
  gap: var(--rs-space-4);
}
.intro,
.source,
.error,
footer,
.toolbar > div {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.intro p,
.error h3,
.error p {
  margin: 0;
}
.intro p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}
.source {
  min-height: 40px;
  padding: 0 var(--rs-space-3);
  border: 1px solid var(--rs-blue-500);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-selected);
  color: var(--rs-blue-700);
}
.toolbar {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) 160px 140px auto;
  gap: var(--rs-space-2);
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
.toolbar > div {
  gap: var(--rs-space-2);
}
.error {
  min-height: 112px;
  gap: var(--rs-space-6);
  padding: var(--rs-space-4);
  border: 1px solid var(--rs-danger-700);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-danger-050);
}
.error p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}
.error > div:last-child {
  display: flex;
  gap: var(--rs-space-2);
}
.table {
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
footer {
  min-height: 56px;
  padding: 0 var(--rs-space-4);
  border-top: 1px solid var(--rs-border-default);
  color: var(--rs-text-secondary);
}
</style>
