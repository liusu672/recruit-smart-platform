<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshCw, RotateCcw, Search } from 'lucide-vue-next'
import { computed, reactive, watch } from 'vue'
import { useRoute } from 'vue-router'
import OnboardingDetailDrawer from '@/components/onboardings/OnboardingDetailDrawer.vue'
import OnboardingTable from '@/components/onboardings/OnboardingTable.vue'
import { useOnboardingManagement } from '@/composables/useOnboardingManagement'
import { onboardingStatusOptions } from '@/config/onboardings'
import type { OnboardingRecord, OnboardingStatus } from '@/types/onboarding'

const state = useOnboardingManagement()
const route = useRoute()
const filterForm = reactive<{ keyword: string; status: OnboardingStatus | '' }>({
  keyword: '',
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
}
function clearFilters() {
  Object.assign(filterForm, { keyword: '', status: '' })
  state.resetFilters()
}
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
      `确认“${record.candidateName}”已于 ${record.entryDate} 入职？后端会自动生成员工编号并创建员工档案。`,
      '生成员工档案',
      { confirmButtonText: '确认生成', cancelButtonText: '返回检查', type: 'warning' },
    )
    await state.completeMutation.mutateAsync({
      id: record.id,
      data: {
        note: 'HR 已核对到岗信息并确认入职。',
      },
    })
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
    <section class="intro">
      <div>
        <h2 class="rs-section-title">入职办理</h2>
        <p>从已接受 Offer 开始审核材料，确认到岗后生成员工档案。</p>
      </div>
    </section>
    <section v-if="state.demoMode.value" class="source">
      <span><strong>演示数据模式</strong>：状态变更只保存在当前浏览器内存中。</span
      ><el-button link @click="state.useApiData">返回入职接口</el-button>
    </section>
    <section class="toolbar" aria-label="入职筛选">
      <el-input
        v-model="filterForm.keyword"
        placeholder="候选人、岗位或部门"
        clearable
        @keyup.enter="submitFilters"
      /><el-select v-model="filterForm.status" placeholder="全部状态" clearable
        ><el-option
          v-for="option in onboardingStatusOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
      /></el-select>
      <div>
        <el-button type="primary" :icon="Search" @click="submitFilters">查询</el-button
        ><el-button :icon="RotateCcw" @click="clearFilters">重置</el-button
        ><el-tooltip content="刷新入职记录"
          ><el-button
            circle
            :icon="RefreshCw"
            :loading="state.listQuery.isFetching.value"
            aria-label="刷新入职记录"
            @click="state.listQuery.refetch()"
        /></el-tooltip>
      </div>
    </section>
    <section v-if="listError && !state.demoMode.value" class="error">
      <div>
        <h3>入职接口暂不可用</h3>
        <p>{{ listError.message }}。请确认 Gateway、业务服务和当前账号权限正常。</p>
      </div>
      <div>
        <el-button @click="state.listQuery.refetch()">重试接口</el-button
        ><el-button type="primary" @click="state.useDemoData">使用演示数据</el-button>
      </div>
    </section>
    <section v-else class="table">
      <OnboardingTable
        :records="records"
        :loading="state.listQuery.isLoading.value"
        @select="state.openDetail($event.id)"
        @start="startReview"
        @approve="reviewMaterial($event, 'APPROVE')"
        @reject="reviewMaterial($event, 'REJECT')"
        @complete="complete"
        @cancel="cancel"
      />
      <footer>
        <span
          >共 {{ total }} 条入职记录，数据来源：{{
            state.demoMode.value ? '演示数据' : '入职接口'
          }}</span
        ><el-pagination
          v-model:current-page="state.query.page"
          background
          layout="prev, pager, next"
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
