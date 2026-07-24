<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { Plus, UserRound, UsersRound } from 'lucide-vue-next'
import { computed, reactive, ref, watch } from 'vue'

import CandidateFormDrawer from '@/components/candidates/CandidateFormDrawer.vue'
import CandidatePreviewPanel from '@/components/candidates/CandidatePreviewPanel.vue'
import HrEmptyState from '@/components/hr/HrEmptyState.vue'
import HrErrorState from '@/components/hr/HrErrorState.vue'
import HrFilterBar from '@/components/hr/HrFilterBar.vue'
import HrPageHeader from '@/components/hr/HrPageHeader.vue'
import HrStatusBadge from '@/components/hr/HrStatusBadge.vue'
import {
  candidateEducationOptions,
  candidateStatusOptions,
  getApplicationStatusTone,
  getCandidateStatusTone,
} from '@/config/candidates'
import { useCandidateManagement } from '@/composables/useCandidateManagement'
import { useHrUrlFilters } from '@/composables/useHrUrlFilters'
import type {
  CandidateCreateRequest,
  CandidateDetail,
  CandidateQuery,
  CandidateStatus,
  CandidateSummary,
} from '@/types/candidate'

type CandidateFormValue = CandidateCreateRequest

const {
  query,
  demoMode,
  selectedCandidateId,
  candidatesQuery,
  detailQuery,
  createMutation,
  updateMutation,
  applyFilters,
  resetFilters,
  useApiData,
  selectCandidate,
  closePreview,
} = useCandidateManagement()
const urlFilters = useHrUrlFilters([
  'keyword',
  'education',
  'school',
  'yearsOfExperienceMin',
  'currentStatus',
  'page',
  'pageSize',
])

const initialYearsValue = urlFilters.readString('yearsOfExperienceMin')
const initialYears = initialYearsValue === '' ? Number.NaN : Number(initialYearsValue)

const filterForm = reactive<
  Pick<
    CandidateQuery,
    'keyword' | 'education' | 'school' | 'yearsOfExperienceMin' | 'currentStatus'
  >
>({
  keyword: urlFilters.readString('keyword'),
  education: urlFilters.readString('education'),
  school: urlFilters.readString('school'),
  yearsOfExperienceMin: Number.isFinite(initialYears) && initialYears >= 0 ? initialYears : null,
  currentStatus: urlFilters.readString('currentStatus') as CandidateStatus | '',
})
Object.assign(query, filterForm, {
  page: urlFilters.readNumber('page', 1),
  pageSize: urlFilters.readNumber('pageSize', 10),
})
const formVisible = ref(false)
const editingCandidate = ref<CandidateDetail | null>(null)
const candidates = computed(() => candidatesQuery.data.value?.items ?? [])
const total = computed(() => candidatesQuery.data.value?.total ?? 0)
const listError = computed(() => candidatesQuery.error.value as Error | null)
const detailError = computed(() => detailQuery.error.value as Error | null)
const activeFilterCount = computed(
  () =>
    [
      filterForm.keyword.trim(),
      filterForm.education,
      filterForm.school,
      filterForm.yearsOfExperienceMin,
      filterForm.currentStatus,
    ].filter((value) => value !== '' && value !== null).length,
)

function formatDate(value: string | null) {
  if (!value) return '暂无记录'
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(new Date(value))
}

function runSearch() {
  applyFilters({ ...filterForm })
  closePreview()
  syncUrl(1)
}

function clearFilters() {
  Object.assign(filterForm, {
    keyword: '',
    education: '',
    school: '',
    yearsOfExperienceMin: null,
    currentStatus: '',
  })
  resetFilters()
  closePreview()
  syncUrl(1)
}

function syncUrl(page = query.page) {
  urlFilters.sync({
    keyword: filterForm.keyword.trim(),
    education: filterForm.education,
    school: filterForm.school,
    yearsOfExperienceMin: filterForm.yearsOfExperienceMin,
    currentStatus: filterForm.currentStatus,
    page: page > 1 ? page : null,
    pageSize: query.pageSize !== 10 ? query.pageSize : null,
  })
}

function maskPhone(phone: string | null) {
  if (!phone) return ''
  if (phone.length < 7) return phone
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`
}

watch([() => query.page, () => query.pageSize], () => syncUrl())

function showError(error: unknown) {
  ElMessage.error(error instanceof Error ? error.message : '操作失败，请稍后重试')
}

async function submitCandidate(data: CandidateFormValue) {
  try {
    const id = editingCandidate.value?.id
    if (id) {
      await updateMutation.mutateAsync({ id, data })
    } else {
      await createMutation.mutateAsync(data)
    }
    formVisible.value = false
    editingCandidate.value = null
    if (id) selectCandidate(id)
    ElMessage.success(
      id ? '候选人信息已更新' : demoMode.value ? '演示候选人已保存' : '候选人已保存',
    )
  } catch (error) {
    showError(error)
  }
}

function openCreateCandidate() {
  editingCandidate.value = null
  formVisible.value = true
}

function openEditCandidate() {
  const candidate = detailQuery.data.value
  if (!candidate) return
  editingCandidate.value = structuredClone(candidate)
  formVisible.value = true
}
</script>

<template>
  <div class="candidates-view">
    <HrPageHeader title="候选人" description="集中管理候选人资料、投递记录和招聘状态。">
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openCreateCandidate">录入候选人</el-button>
      </template>
    </HrPageHeader>

    <el-alert
      v-if="demoMode"
      title="当前使用演示数据，录入操作只在本次页面会话中生效。"
      type="warning"
      :closable="false"
      show-icon
    >
      <template #default>
        <el-button link type="primary" @click="useApiData">切换到真实数据</el-button>
      </template>
    </el-alert>

    <HrFilterBar
      :loading="candidatesQuery.isFetching.value"
      :active-count="activeFilterCount"
      :reset-disabled="activeFilterCount === 0"
      @submit="runSearch"
      @reset="clearFilters"
      @refresh="candidatesQuery.refetch()"
    >
      <el-input v-model="filterForm.keyword" clearable placeholder="姓名、手机号或邮箱" />
      <el-select v-model="filterForm.education" clearable placeholder="全部学历">
        <el-option
          v-for="education in candidateEducationOptions"
          :key="education"
          :label="education"
          :value="education"
        />
      </el-select>
      <el-input v-model="filterForm.school" clearable placeholder="毕业学校" />
      <el-input-number
        v-model="filterForm.yearsOfExperienceMin"
        :min="0"
        :max="60"
        controls-position="right"
        placeholder="最低年限"
      />
      <el-select v-model="filterForm.currentStatus" clearable placeholder="全部状态">
        <el-option
          v-for="status in candidateStatusOptions"
          :key="status.value"
          :label="status.label"
          :value="status.value"
        />
      </el-select>
    </HrFilterBar>

    <HrErrorState
      v-if="listError && !demoMode"
      title="候选人列表暂时无法加载"
      description="请稍后重试。如果问题持续存在，请联系系统管理员。"
      :loading="candidatesQuery.isFetching.value"
      @retry="candidatesQuery.refetch()"
    />

    <section
      v-else
      class="candidate-workspace"
      :class="{ 'candidate-workspace--with-preview': selectedCandidateId !== null }"
    >
      <div class="candidate-table" aria-label="候选人数据表格">
        <el-table
          v-loading="candidatesQuery.isLoading.value"
          :data="candidates"
          row-key="id"
          height="calc(100dvh - 308px)"
          highlight-current-row
          @row-click="(row: CandidateSummary) => selectCandidate(row.id)"
        >
          <el-table-column prop="name" label="候选人" min-width="190" fixed="left">
            <template #default="{ row }: { row: CandidateSummary }">
              <div class="candidate-name-cell">
                <span class="candidate-name-cell__avatar" aria-hidden="true">
                  <UserRound :size="18" :stroke-width="1.9" />
                </span>
                <div>
                  <strong>{{ row.name }}</strong>
                  <span>{{ maskPhone(row.phone) || row.email || '联系方式待补充' }}</span>
                </div>
                <el-tooltip v-if="row.duplicateRisk" content="联系方式可能重复" placement="top">
                  <span class="candidate-name-cell__risk" aria-label="联系方式可能重复">!</span>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="education" label="学历与学校" min-width="170">
            <template #default="{ row }: { row: CandidateSummary }">
              <div class="candidate-two-line-cell">
                <strong>{{ row.education || '待补充' }}</strong>
                <span>{{ row.school || '学校待补充' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="yearsOfExperience" label="经验" width="88" align="right" sortable>
            <template #default="{ row }: { row: CandidateSummary }">
              <span class="rs-tabular-number">{{ row.yearsOfExperience }} 年</span>
            </template>
          </el-table-column>
          <el-table-column prop="latestJobTitle" label="最近投递" min-width="180">
            <template #default="{ row }: { row: CandidateSummary }">
              <div class="candidate-two-line-cell">
                <strong>{{ row.latestJobTitle || '暂无投递' }}</strong>
                <HrStatusBadge
                  v-if="row.latestApplicationStatus && row.latestApplicationStatusText"
                  :status="row.latestApplicationStatus"
                  :label="row.latestApplicationStatusText"
                  :tone="getApplicationStatusTone(row.latestApplicationStatus)"
                />
              </div>
            </template>
          </el-table-column>
          <el-table-column
            prop="latestMatchScore"
            label="AI 匹配"
            width="100"
            align="right"
            sortable
          >
            <template #default="{ row }: { row: CandidateSummary }">
              <div v-if="row.latestMatchScore !== null" class="candidate-score">
                <strong>{{ row.latestMatchScore }}</strong
                ><small>仅供参考</small>
              </div>
              <span v-else class="candidate-muted">暂无</span>
            </template>
          </el-table-column>
          <el-table-column prop="currentStatusText" label="人才状态" width="108">
            <template #default="{ row }: { row: CandidateSummary }">
              <HrStatusBadge
                :status="row.currentStatus"
                :label="row.currentStatusText"
                :tone="getCandidateStatusTone(row.currentStatus)"
              />
            </template>
          </el-table-column>
          <el-table-column
            prop="lastActivityAt"
            label="最近活动"
            width="120"
            align="right"
            sortable
          >
            <template #default="{ row }: { row: CandidateSummary }">
              <span class="rs-tabular-number">{{ formatDate(row.lastActivityAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="88" fixed="right" align="right">
            <template #default="{ row }: { row: CandidateSummary }">
              <div @click.stop>
                <el-button link type="primary" @click="selectCandidate(row.id)">查看</el-button>
              </div>
            </template>
          </el-table-column>

          <template #empty>
            <HrEmptyState
              :icon="UsersRound"
              title="没有符合条件的候选人"
              description="调整筛选条件，或由 HR 录入新的候选人资料。"
            >
              <template #actions
                ><el-button type="primary" :icon="Plus" @click="openCreateCandidate"
                  >录入候选人</el-button
                ></template
              >
            </HrEmptyState>
          </template>
        </el-table>

        <footer class="candidate-pagination">
          <span>共 {{ total }} 位候选人</span>
          <el-pagination
            v-model:current-page="query.page"
            v-model:page-size="query.pageSize"
            background
            layout="sizes, prev, pager, next"
            :page-sizes="[10, 20, 50]"
            :total="total"
          />
        </footer>
      </div>

      <CandidatePreviewPanel
        v-if="selectedCandidateId !== null"
        :candidate="detailQuery.data.value"
        :loading="detailQuery.isLoading.value"
        :error="detailError"
        :demo-mode="demoMode"
        @close="closePreview"
        @edit="openEditCandidate"
      />
    </section>

    <CandidateFormDrawer
      v-model:visible="formVisible"
      :submitting="createMutation.isPending.value || updateMutation.isPending.value"
      :candidate="editingCandidate"
      @submit="submitCandidate"
    />
  </div>
</template>

<style scoped lang="scss">
.candidates-view {
  display: grid;
  gap: var(--rs-space-4);
}

.candidates-view__intro,
.candidate-selection,
.candidates-error,
.candidate-pagination,
.candidate-name-cell,
.candidates-error__actions,
.candidate-toolbar__actions {
  display: flex;
  align-items: center;
}

.candidates-view__intro,
.candidate-selection,
.candidates-error,
.candidate-pagination {
  justify-content: space-between;
}

.candidates-view__intro p,
.candidates-error h3,
.candidates-error p,
.candidates-empty h3,
.candidates-empty p {
  margin: 0;
}

.candidates-view__intro p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}

.candidate-toolbar {
  display: grid;
  grid-template-columns: 200px 112px 140px 112px 120px auto;
  gap: var(--rs-space-2);
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.candidate-toolbar :deep(.el-input-number) {
  width: 100%;
}

.candidate-toolbar__actions,
.candidates-error__actions {
  justify-content: flex-end;
  gap: var(--rs-space-2);
}

.candidate-selection {
  min-height: 40px;
  padding: 0 var(--rs-space-3);
  border: 1px solid var(--rs-blue-500);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-selected);
  color: var(--rs-blue-700);
  font-weight: 600;
}

.candidates-error {
  min-height: 112px;
  gap: var(--rs-space-6);
  padding: var(--rs-space-4);
  border: 1px solid var(--rs-danger-700);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-danger-050);
}

.candidates-error h3 {
  font-size: 14px;
}

.candidates-error p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}

.candidate-workspace {
  min-width: 0;
}

.candidate-workspace--with-preview {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 400px;
  gap: var(--rs-space-4);
}

.candidate-table {
  min-width: 0;
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.candidate-table :deep(.el-table__row) {
  cursor: pointer;
}

.candidate-name-cell {
  gap: var(--rs-space-2);
}

.candidate-name-cell__avatar {
  display: grid;
  flex: 0 0 32px;
  width: 32px;
  height: 32px;
  place-items: center;
  border-radius: 50%;
  background: var(--rs-blue-050);
  color: var(--rs-blue-700);
}

.candidate-name-cell > div,
.candidate-two-line-cell {
  display: grid;
  min-width: 0;
}

.candidate-name-cell strong,
.candidate-two-line-cell strong {
  overflow: hidden;
  color: var(--rs-text-primary);
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.candidate-name-cell > div > span,
.candidate-two-line-cell > span:not(.rs-status-pill) {
  overflow: hidden;
  color: var(--rs-text-tertiary);
  font-size: 10px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.candidate-name-cell > .candidate-name-cell__risk {
  display: grid;
  flex: 0 0 20px;
  width: 20px;
  height: 20px;
  place-items: center;
  border-radius: 50%;
  background: var(--rs-warning-050);
  color: var(--rs-warning-800);
  font-size: 12px;
  font-weight: 700;
}

.candidate-two-line-cell .rs-status-pill {
  justify-self: start;
  min-height: 20px;
  margin-top: var(--rs-space-1);
}

.candidate-score {
  display: grid;
  justify-items: end;
  color: var(--rs-blue-700);
  font-variant-numeric: tabular-nums;
}

.candidate-score strong {
  font-weight: 700;
}
.candidate-score small {
  color: var(--rs-text-tertiary);
  font-size: 12px;
  font-weight: 400;
}

.candidate-muted {
  color: var(--rs-text-tertiary);
}

.rs-tabular-number {
  font-variant-numeric: tabular-nums;
}

.candidates-empty {
  display: grid;
  justify-items: center;
  gap: var(--rs-space-2);
  padding: var(--rs-space-8);
  color: var(--rs-text-secondary);
}

.candidates-empty h3 {
  color: var(--rs-text-primary);
  font-size: 14px;
}

.candidate-pagination {
  min-height: 56px;
  padding: 0 var(--rs-space-4);
  border-top: 1px solid var(--rs-border-default);
  color: var(--rs-text-secondary);
}

@media (max-width: 1439px) {
  .candidate-workspace--with-preview {
    display: block;
  }
}
</style>
