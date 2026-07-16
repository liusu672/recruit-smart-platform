<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, RefreshCw, RotateCcw, Search } from 'lucide-vue-next'
import { computed, reactive, ref } from 'vue'

import OfferDetailDrawer from '@/components/offers/OfferDetailDrawer.vue'
import OfferFormDrawer from '@/components/offers/OfferFormDrawer.vue'
import OfferTable from '@/components/offers/OfferTable.vue'
import { useOfferManagement } from '@/composables/useOfferManagement'
import { demoEligibleOfferApplications } from '@/config/demoOffers'
import { formatOfferSalary, offerStatusOptions, validateOfferForSend } from '@/config/offers'
import type {
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
  useDemoData,
  useApiData,
  openDetail,
  closeDetail,
} = useOfferManagement()

const filterForm = reactive<{ keyword: string; status: OfferStatus | '' }>({
  keyword: '',
  status: '',
})
const formVisible = ref(false)
const editingOffer = ref<OfferRecord | null>(null)

const offers = computed(() => offersQuery.data.value?.items ?? [])
const total = computed(() => offersQuery.data.value?.total ?? 0)
const listError = computed(() => offersQuery.error.value as Error | null)
const detailError = computed(() => detailQuery.error.value as Error | null)
const detailVisible = computed({
  get: () => selectedOfferId.value !== null,
  set: (value) => {
    if (!value) closeDetail()
  },
})

function submitFilters() {
  applyFilters({ keyword: filterForm.keyword.trim(), status: filterForm.status })
}

function clearFilters() {
  Object.assign(filterForm, { keyword: '', status: '' })
  resetFilters()
}

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
    await sendMutation.mutateAsync({
      id: offer.id,
      data: {
        note: 'HR 已复核薪资、入职日期与工作地点。',
      },
    })
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
    await revokeMutation.mutateAsync({
      id: offer.id,
      data: {
        reason: 'HR 已确认撤回该 Offer。',
      },
    })
    ElMessage.success('Offer 已撤回')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error instanceof Error ? error.message : 'Offer 撤回失败')
  }
}
</script>

<template>
  <div class="offers-view">
    <section class="offers-view__intro">
      <div>
        <h2 class="rs-section-title">Offer 管理</h2>
        <p>集中复核录用方案、发送记录和候选人回复，关键动作必须由 HR 确认。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreateForm">创建 Offer 草稿</el-button>
    </section>

    <section v-if="demoMode" class="offer-source" aria-live="polite">
      <span><strong>演示数据模式</strong>：草稿、发送与撤回只保存在当前浏览器内存中。</span>
      <el-button link @click="useApiData">返回 Offer 接口</el-button>
    </section>

    <section class="offer-toolbar" aria-label="Offer 筛选">
      <el-input
        v-model="filterForm.keyword"
        placeholder="候选人、职位或部门"
        clearable
        @keyup.enter="submitFilters"
      />
      <el-select v-model="filterForm.status" placeholder="全部状态" clearable>
        <el-option
          v-for="option in offerStatusOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
      <div class="offer-toolbar__actions">
        <el-button type="primary" :icon="Search" @click="submitFilters">查询</el-button>
        <el-button :icon="RotateCcw" @click="clearFilters">重置</el-button>
        <el-tooltip content="刷新 Offer" placement="top">
          <el-button
            circle
            :icon="RefreshCw"
            :loading="offersQuery.isFetching.value"
            aria-label="刷新 Offer"
            @click="offersQuery.refetch()"
          />
        </el-tooltip>
      </div>
    </section>

    <section v-if="listError && !demoMode" class="offer-error" role="alert">
      <div>
        <h3>Offer 接口暂不可用</h3>
        <p>
          {{ listError.message }}。请确认
          Gateway、业务服务和当前账号权限正常，也可以使用演示数据继续评审。
        </p>
      </div>
      <div>
        <el-button :loading="offersQuery.isFetching.value" @click="offersQuery.refetch()">
          重试接口
        </el-button>
        <el-button type="primary" @click="useDemoData">使用演示数据</el-button>
      </div>
    </section>

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
        <span>共 {{ total }} 份 Offer，数据来源：{{ demoMode ? '演示数据' : 'Offer 接口' }}</span>
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          background
          layout="prev, pager, next"
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
      :candidates="demoEligibleOfferApplications"
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
