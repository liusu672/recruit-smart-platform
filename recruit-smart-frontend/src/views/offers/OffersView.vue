<script setup lang="ts">
import { useQuery } from '@tanstack/vue-query'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from 'lucide-vue-next'
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import { getPipelineApplications } from '@/api/pipeline'
import HrErrorState from '@/components/hr/HrErrorState.vue'
import HrFilterBar from '@/components/hr/HrFilterBar.vue'
import HrPageHeader from '@/components/hr/HrPageHeader.vue'
import OfferDetailDrawer from '@/components/offers/OfferDetailDrawer.vue'
import OfferFormDrawer from '@/components/offers/OfferFormDrawer.vue'
import OfferTable from '@/components/offers/OfferTable.vue'
import { useOfferManagement } from '@/composables/useOfferManagement'
import { useHrUrlFilters } from '@/composables/useHrUrlFilters'
import { getDemoPipelinePage, initialDemoPipeline } from '@/config/demoPipeline'
import { formatOfferSalary, offerStatusOptions, validateOfferForSend } from '@/config/offers'
import type {
  OfferCandidateOption,
  OfferFormSubmitValue,
  OfferRecord,
  OfferStatus,
  OfferUpdateRequest,
} from '@/types/offer'

const {
  query,
  demoMode,
  selectedOfferId,
  offersQuery,
  detailQuery,
  createMutation,
  updateMutation,
  sendMutation,
  revokeMutation,
  applyFilters,
  resetFilters,
  useApiData,
  openDetail,
  closeDetail,
} = useOfferManagement()

const route = useRoute()
const urlFilters = useHrUrlFilters(['keyword', 'status', 'page', 'pageSize'])
const filterForm = reactive<{ keyword: string; status: OfferStatus | '' }>({
  keyword: urlFilters.readString('keyword'),
  status: urlFilters.readString('status') as OfferStatus | '',
})
Object.assign(query, filterForm, {
  page: urlFilters.readNumber('page', 1),
  pageSize: urlFilters.readNumber('pageSize', 10),
})
const formVisible = ref(false)
const editingOffer = ref<OfferRecord | null>(null)

const offers = computed(() => offersQuery.data.value?.items ?? [])
const total = computed(() => offersQuery.data.value?.total ?? 0)
const listError = computed(() => offersQuery.error.value as Error | null)
const detailError = computed(() => detailQuery.error.value as Error | null)
const activeFilterCount = computed(
  () => [filterForm.keyword.trim(), filterForm.status].filter(Boolean).length,
)
const eligibleApplicationsQuery = useQuery({
  queryKey: computed(() => ['offer-eligible-applications', demoMode.value ? 'demo' : 'api']),
  queryFn: async (): Promise<OfferCandidateOption[]> => {
    const page = demoMode.value
      ? getDemoPipelinePage(initialDemoPipeline, {
          keyword: '',
          jobId: null,
          status: 'INTERVIEWING',
          page: 1,
          pageSize: 20,
        })
      : await getPipelineApplications({
          keyword: '',
          jobId: null,
          status: 'INTERVIEWING',
          page: 1,
          pageSize: 20,
        })

    return page.items.map((application) => ({
      applicationId: application.id,
      candidateName: application.candidateName,
      jobTitle: application.jobTitle,
      interviewScore: null,
    }))
  },
})
const offerCandidates = computed(() => {
  const existingApplicationIds = new Set(offers.value.map((offer) => offer.applicationId))
  return (eligibleApplicationsQuery.data.value ?? []).filter(
    (candidate) => !existingApplicationIds.has(candidate.applicationId),
  )
})
const detailVisible = computed({
  get: () => selectedOfferId.value !== null,
  set: (value) => {
    if (!value) closeDetail()
  },
})

function parseRouteId(value: unknown) {
  const raw = Array.isArray(value) ? value[0] : value
  const id = Number(raw)
  return Number.isFinite(id) && id > 0 ? id : null
}

watch(
  () => route.query.offerId,
  (value) => {
    const offerId = parseRouteId(value)
    if (offerId !== null) openDetail(offerId)
  },
  { immediate: true },
)

function submitFilters() {
  applyFilters({ keyword: filterForm.keyword.trim(), status: filterForm.status })
  syncUrl(1)
}

function clearFilters() {
  Object.assign(filterForm, { keyword: '', status: '' })
  resetFilters()
  syncUrl(1)
}

function syncUrl(page = query.page) {
  urlFilters.sync({
    keyword: filterForm.keyword.trim(),
    status: filterForm.status,
    page: page > 1 ? page : null,
    pageSize: query.pageSize !== 10 ? query.pageSize : null,
  })
}

watch([() => query.page, () => query.pageSize], () => syncUrl())

function openCreateForm() {
  editingOffer.value = null
  formVisible.value = true
}

function openEditForm(offer: OfferRecord) {
  editingOffer.value = offer
  formVisible.value = true
}

async function submitOfferForm(value: OfferFormSubmitValue) {
  try {
    if (editingOffer.value) {
      const data: OfferUpdateRequest = {
        salary: value.salary,
        entryDate: value.entryDate,
        probationMonths: value.probationMonths,
        workLocation: value.workLocation,
        remark: value.remark,
      }
      await updateMutation.mutateAsync({ id: editingOffer.value.id, data })
      ElMessage.success('Offer 草稿已更新')
    } else {
      const id = await createMutation.mutateAsync(value)
      ElMessage.success('Offer 草稿已创建')
      openDetail(id)
    }
    formVisible.value = false
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : 'Offer 草稿保存失败')
  }
}

async function confirmSend(offer: OfferRecord) {
  const validationError = validateOfferForSend(offer)
  if (validationError) {
    ElMessage.warning(validationError)
    openEditForm(offer)
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认向“${offer.candidateName}”发送 ${formatOfferSalary(offer.salary)} 的 Offer？发送后薪资方案不可直接编辑。`,
      '发送 Offer',
      {
        confirmButtonText: '确认发送',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await sendMutation.mutateAsync(offer.id)
    ElMessage.success('Offer 已发送，等待候选人决定')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error instanceof Error ? error.message : 'Offer 发送失败')
  }
}

async function confirmRevoke(offer: OfferRecord) {
  try {
    await ElMessageBox.confirm(
      `确认撤回“${offer.candidateName}”的 Offer？撤回后该录用沟通会终止。`,
      '撤回 Offer',
      {
        confirmButtonText: '确认撤回',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await revokeMutation.mutateAsync(offer.id)
    ElMessage.success('Offer 已撤回')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error instanceof Error ? error.message : 'Offer 撤回失败')
  }
}
</script>

<template>
  <div class="offers-view">
    <HrPageHeader
      title="Offer 管理"
      description="集中复核录用方案、发送记录和候选人回复，关键动作必须由 HR 确认。"
    >
      <template #actions
        ><el-button type="primary" :icon="Plus" @click="openCreateForm"
          >创建 Offer 草稿</el-button
        ></template
      >
    </HrPageHeader>

    <section v-if="demoMode" class="offer-source" aria-live="polite">
      <span><strong>演示数据模式</strong>：草稿、发送与撤回只保存在当前浏览器内存中。</span>
      <el-button link @click="useApiData">切换到真实数据</el-button>
    </section>

    <HrFilterBar
      :loading="offersQuery.isFetching.value"
      :active-count="activeFilterCount"
      :reset-disabled="activeFilterCount === 0"
      @submit="submitFilters"
      @reset="clearFilters"
      @refresh="offersQuery.refetch()"
    >
      <el-input v-model="filterForm.keyword" placeholder="候选人、职位或部门" clearable />
      <el-select v-model="filterForm.status" placeholder="全部状态" clearable>
        <el-option
          v-for="option in offerStatusOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
    </HrFilterBar>

    <HrErrorState
      v-if="listError && !demoMode"
      title="Offer 列表暂时无法加载"
      description="请稍后重试。如果问题持续存在，请联系系统管理员。"
      :loading="offersQuery.isFetching.value"
      @retry="offersQuery.refetch()"
    />

    <section v-else class="offer-table">
      <OfferTable
        :offers="offers"
        :loading="offersQuery.isLoading.value"
        @select="openDetail($event.id)"
        @edit="openEditForm"
        @send="confirmSend"
        @revoke="confirmRevoke"
      />
      <footer class="offer-pagination">
        <span>共 {{ total }} 份 Offer</span>
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

    <OfferDetailDrawer
      v-model:visible="detailVisible"
      :offer="detailQuery.data.value"
      :loading="detailQuery.isLoading.value"
      :error="detailError"
      @edit="openEditForm"
      @send="confirmSend"
      @revoke="confirmRevoke"
    />

    <OfferFormDrawer
      v-model:visible="formVisible"
      :offer="editingOffer"
      :candidates="offerCandidates"
      :submitting="createMutation.isPending.value || updateMutation.isPending.value"
      @submit="submitOfferForm"
    />
  </div>
</template>

<style scoped lang="scss">
.offers-view {
  display: grid;
  gap: var(--rs-space-4);
}

.offers-view__intro,
.offer-source,
.offer-toolbar__actions,
.offer-error,
.offer-pagination {
  display: flex;
  align-items: center;
}

.offers-view__intro,
.offer-source,
.offer-error,
.offer-pagination {
  justify-content: space-between;
}

.offers-view__intro p,
.offer-error h3,
.offer-error p {
  margin: 0;
}

.offers-view__intro p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}

.offer-source {
  min-height: 40px;
  padding: 0 var(--rs-space-3);
  border: 1px solid var(--rs-blue-500);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-selected);
  color: var(--rs-blue-700);
}

.offer-toolbar {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) 180px auto;
  gap: var(--rs-space-2);
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.offer-toolbar__actions {
  justify-content: flex-end;
  gap: var(--rs-space-2);
}

.offer-error {
  min-height: 112px;
  gap: var(--rs-space-6);
  padding: var(--rs-space-4);
  border: 1px solid var(--rs-danger-700);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-danger-050);
}

.offer-error > div:last-child {
  display: flex;
  gap: var(--rs-space-2);
}

.offer-error h3 {
  font-size: 14px;
}

.offer-error p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}

.offer-table {
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.offer-pagination {
  min-height: 56px;
  padding: 0 var(--rs-space-4);
  border-top: 1px solid var(--rs-border-default);
  color: var(--rs-text-secondary);
}
</style>
