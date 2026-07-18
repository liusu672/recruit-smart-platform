<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, reactive, watch } from 'vue'
import { useRoute } from 'vue-router'
import HrErrorState from '@/components/hr/HrErrorState.vue'
import HrFilterBar from '@/components/hr/HrFilterBar.vue'
import HrPageHeader from '@/components/hr/HrPageHeader.vue'
import OnboardingDetailDrawer from '@/components/onboardings/OnboardingDetailDrawer.vue'
import OnboardingTable from '@/components/onboardings/OnboardingTable.vue'
import { useHrUrlFilters } from '@/composables/useHrUrlFilters'
import { useOnboardingManagement } from '@/composables/useOnboardingManagement'
import { onboardingStatusOptions } from '@/config/onboardings'
import type { OnboardingRecord, OnboardingStatus } from '@/types/onboarding'

const state = useOnboardingManagement()
const route = useRoute()
const urlFilters = useHrUrlFilters(['keyword', 'status', 'page', 'pageSize'])
const filterForm = reactive<{ keyword: string; status: OnboardingStatus | '' }>({
  keyword: urlFilters.readString('keyword'),
  status: urlFilters.readString('status') as OnboardingStatus | '',
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
  () => [filterForm.keyword.trim(), filterForm.status].filter(Boolean).length,
)
const detailVisible = computed({
  get: () => state.selectedId.value !== null,
  set: (value) => {
    if (!value) state.closeDetail()
  },
})

function parseRouteId(value: unknown) {
  const raw = Array.isArray(value) ? value[0] : value
  const id = Number(raw)
  return Number.isFinite(id) && id > 0 ? id : null
}

watch(
  () => route.query.onboardingId,
  (value) => {
    const onboardingId = parseRouteId(value)
    if (onboardingId !== null) state.openDetail(onboardingId)
  },
  { immediate: true },
)

function submitFilters() {
  state.applyFilters({ keyword: filterForm.keyword.trim(), status: filterForm.status })
  syncUrl(1)
}
function clearFilters() {
  Object.assign(filterForm, { keyword: '', status: '' })
  state.resetFilters()
  syncUrl(1)
}
function syncUrl(page = state.query.page) {
  urlFilters.sync({
    keyword: filterForm.keyword.trim(),
    status: filterForm.status,
    page: page > 1 ? page : null,
    pageSize: state.query.pageSize !== 10 ? state.query.pageSize : null,
  })
}
watch([() => state.query.page, () => state.query.pageSize], () => syncUrl())
async function startReview(record: OnboardingRecord) {
  try {
    await ElMessageBox.confirm(
      `确认开始审核“${record.candidateName}”的入职材料？`,
      '开始材料审核',
      { confirmButtonText: '开始审核', cancelButtonText: '取消', type: 'warning' },
    )
    await state.startReviewMutation.mutateAsync({
      id: record.id,
      data: { note: 'HR 已接收入职材料并开始审核。' },
    })
    ElMessage.success('已进入材料审核')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error instanceof Error ? error.message : '操作失败')
  }
}
async function reviewMaterial(record: OnboardingRecord, decision: 'APPROVE' | 'REJECT') {
  try {
    const result = await ElMessageBox.prompt(
      decision === 'APPROVE' ? '填写材料通过说明。' : '填写需要候选人补充的材料。',
      decision === 'APPROVE' ? '材料审核通过' : '退回补充材料',
      {
        confirmButtonText: '确认提交',
        cancelButtonText: '取消',
        inputValidator: (value) => value.trim().length >= 4 || '请填写至少 4 个字的审核说明',
        type: 'warning',
      },
    )
    await state.materialReviewMutation.mutateAsync({
      id: record.id,
      data: { decision, note: result.value.trim() },
    })
    ElMessage.success(decision === 'APPROVE' ? '材料已通过' : '已退回补充')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error instanceof Error ? error.message : '材料审核失败')
  }
}
async function complete(record: OnboardingRecord) {
  try {
    await ElMessageBox.confirm(
      `确认“${record.candidateName}”已于 ${record.entryDate} 入职？系统会自动生成员工编号并创建员工档案。`,
      '生成员工档案',
      { confirmButtonText: '确认生成', cancelButtonText: '返回检查', type: 'warning' },
    )
    await state.completeMutation.mutateAsync(record.id)
    ElMessage.success('入职已完成，员工档案已生成')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error instanceof Error ? error.message : '确认入职失败')
  }
}

async function cancel(record: OnboardingRecord) {
  try {
    const result = await ElMessageBox.prompt(
      `请填写取消“${record.candidateName}”入职流程的原因。`,
      '取消入职流程',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '返回',
        inputType: 'textarea',
        inputValidator: (value) => value.trim().length >= 4 || '请填写至少 4 个字的取消原因',
        type: 'warning',
      },
    )
    await ElMessageBox.confirm(
      `确认取消“${record.candidateName}”的入职流程？该操作提交后不可撤销。`,
      '再次确认取消',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '返回',
        type: 'warning',
      },
    )
    await state.cancelMutation.mutateAsync({
      id: record.id,
      data: { reason: result.value.trim() },
    })
    ElMessage.success('入职流程已取消')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error instanceof Error ? error.message : '取消入职流程失败')
  }
}
</script>

<template>
  <div class="view">
    <HrPageHeader
      title="入职办理"
      description="从已接受 Offer 开始审核材料，确认到岗后生成员工档案。"
    />
    <section v-if="state.demoMode.value" class="source">
      <span><strong>演示数据模式</strong>：状态变更只保存在当前浏览器内存中。</span
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
      <el-input v-model="filterForm.keyword" placeholder="候选人、岗位或部门" clearable /><el-select
        v-model="filterForm.status"
        placeholder="全部状态"
        clearable
        ><el-option
          v-for="option in onboardingStatusOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
      /></el-select>
    </HrFilterBar>
    <HrErrorState
      v-if="listError && !state.demoMode.value"
      title="入职记录暂时无法加载"
      description="请稍后重试。如果问题持续存在，请联系系统管理员。"
      :loading="state.listQuery.isFetching.value"
      @retry="state.listQuery.refetch()"
    />
    <section v-else class="table">
      <OnboardingTable
        :records="records"
        :loading="state.listQuery.isLoading.value"
        :allow-start-review="state.demoMode.value"
        @select="state.openDetail($event.id)"
        @start="startReview"
        @approve="reviewMaterial($event, 'APPROVE')"
        @reject="reviewMaterial($event, 'REJECT')"
        @complete="complete"
        @cancel="cancel"
      />
      <footer>
        <span>共 {{ total }} 条入职记录</span
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
    <OnboardingDetailDrawer
      v-model:visible="detailVisible"
      :record="state.detailQuery.data.value"
      :loading="state.detailQuery.isLoading.value"
      :error="detailError"
      @complete="complete"
      @cancel="cancel"
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
  grid-template-columns: minmax(260px, 1fr) 180px auto;
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
