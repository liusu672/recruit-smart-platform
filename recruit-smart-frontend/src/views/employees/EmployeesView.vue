<script setup lang="ts">
import { RefreshCw, RotateCcw, Search } from 'lucide-vue-next'
import { computed, reactive } from 'vue'
import EmployeeDetailDrawer from '@/components/employees/EmployeeDetailDrawer.vue'
import EmployeeTable from '@/components/employees/EmployeeTable.vue'
import { useEmployeeDirectory } from '@/composables/useEmployeeDirectory'
import { employeeStatusOptions } from '@/config/employees'
import type { EmployeeStatus } from '@/types/employee'

const state = useEmployeeDirectory()
const filterForm = reactive<{ keyword: string; department: string; status: EmployeeStatus | '' }>({
  keyword: '',
  department: '',
  status: '',
})
const records = computed(() => state.listQuery.data.value?.items ?? [])
const total = computed(() => state.listQuery.data.value?.total ?? 0)
const listError = computed(() => state.listQuery.error.value as Error | null)
const detailError = computed(() => state.detailQuery.error.value as Error | null)
const detailVisible = computed({
  get: () => state.selectedId.value !== null,
  set: (value) => {
    if (!value) state.closeDetail()
  },
})
const departments = ['研发部', '技术部', '产品部', '产品设计部']
function submitFilters() {
  state.applyFilters({
    keyword: filterForm.keyword.trim(),
    department: filterForm.department,
    status: filterForm.status,
  })
}
function clearFilters() {
  Object.assign(filterForm, { keyword: '', department: '', status: '' })
  state.resetFilters()
}
</script>

<template>
  <div class="view">
    <section class="intro">
      <div>
        <h2 class="rs-section-title">员工档案</h2>
        <p>查看由已完成入职流程生成的员工资料、在职状态和辅助风险提示。</p>
      </div>
    </section>
    <section v-if="state.demoMode.value" class="source">
      <span><strong>演示数据模式</strong>：以下资料仅用于前端流程演示。</span
      ><el-button link @click="state.useApiData">返回员工接口</el-button>
    </section>
    <section class="toolbar" aria-label="员工筛选">
      <el-input
        v-model="filterForm.keyword"
        placeholder="姓名、员工编号或岗位"
        clearable
        @keyup.enter="submitFilters"
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
      <div>
        <el-button type="primary" :icon="Search" @click="submitFilters">查询</el-button
        ><el-button :icon="RotateCcw" @click="clearFilters">重置</el-button
        ><el-tooltip content="刷新员工档案"
          ><el-button
            circle
            :icon="RefreshCw"
            :loading="state.listQuery.isFetching.value"
            aria-label="刷新员工档案"
            @click="state.listQuery.refetch()"
        /></el-tooltip>
      </div>
    </section>
    <section v-if="listError && !state.demoMode.value" class="error">
      <div>
        <h3>员工档案接口尚不可用</h3>
        <p>{{ listError.message }}。后端目前只有 EmployeeProfile 实体与 Mapper。</p>
      </div>
      <div>
        <el-button @click="state.listQuery.refetch()">重试接口</el-button
        ><el-button type="primary" @click="state.useDemoData">使用演示数据</el-button>
      </div>
    </section>
    <section v-else class="table">
      <EmployeeTable
        :records="records"
        :loading="state.listQuery.isLoading.value"
        @select="state.openDetail($event.id)"
      />
      <footer>
        <span
          >共 {{ total }} 份员工档案，数据来源：{{
            state.demoMode.value ? '演示数据' : '员工接口'
          }}</span
        ><el-pagination
          v-model:current-page="state.query.page"
          background
          layout="prev, pager, next"
          :total="total"
        />
      </footer>
    </section>
    <EmployeeDetailDrawer
      v-model:visible="detailVisible"
      :record="state.detailQuery.data.value"
      :loading="state.detailQuery.isLoading.value"
      :error="detailError"
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
