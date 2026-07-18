<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  BriefcaseBusiness,
  GraduationCap,
  MapPin,
  Search,
  SearchX,
  WalletCards,
} from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

import { applyToJob, getMyResumes, getOpenJobById, getOpenJobs } from '@/api/candidatePortal'
import CandidateEmptyState from '@/components/candidate/CandidateEmptyState.vue'
import CandidateErrorState from '@/components/candidate/CandidateErrorState.vue'
import CandidateJobDetailDrawer from '@/components/candidate/CandidateJobDetailDrawer.vue'
import CandidateListItem from '@/components/candidate/CandidateListItem.vue'
import CandidatePageHeader from '@/components/candidate/CandidatePageHeader.vue'
import { usePortalPagedResource } from '@/composables/usePortalPagedResource'
import { usePortalResource } from '@/composables/usePortalResource'
import { demoMyResumes, demoOpenJobs } from '@/config/demoCandidatePortal'

const router = useRouter()
const keyword = ref('')
const applyingJobId = ref<number | null>(null)
const jobs = usePortalPagedResource(
  (query) => getOpenJobs({ ...query, keyword: keyword.value.trim() }),
  demoOpenJobs,
)
const resumes = usePortalResource(getMyResumes, demoMyResumes)
const filteredJobs = computed(() =>
  jobs.data.value.items.filter((item) =>
    `${item.title}${item.department}${item.location ?? ''}`
      .toLowerCase()
      .includes(keyword.value.trim().toLowerCase()),
  ),
)
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailError = ref('')
const selectedJob = ref<(typeof jobs.data.value.items)[number] | null>(null)

function searchJobs() {
  if (jobs.query.page === 1) void jobs.reload()
  else jobs.query.page = 1
}

function clearSearch() {
  keyword.value = ''
  searchJobs()
}

async function openDetail(id: number) {
  detailVisible.value = true
  detailLoading.value = true
  detailError.value = ''
  try {
    selectedJob.value = jobs.demoMode.value
      ? (jobs.data.value.items.find((item) => item.id === id) ?? null)
      : await getOpenJobById(id)
  } catch {
    detailError.value = '职位详情暂时无法加载，请稍后重试。'
  } finally {
    detailLoading.value = false
  }
}

async function apply(jobId: number, title: string) {
  if (applyingJobId.value !== null) return
  const resume = resumes.data.value.find((item) => item.isDefault === 1) ?? resumes.data.value[0]
  if (!resume) {
    ElMessage.warning('请先上传简历再投递')
    await router.push('/candidate/resumes')
    return
  }
  try {
    await ElMessageBox.confirm(`将使用“${resume.resumeName}”投递“${title}”。`, '确认投递', {
      confirmButtonText: '确认投递',
      cancelButtonText: '取消',
      type: 'info',
    })
    applyingJobId.value = jobId
    if (!jobs.demoMode.value) await applyToJob(jobId, resume.id)
    ElMessage.success('投递成功，可在“我的投递”查看进度')
    await router.push('/candidate/applications')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error instanceof Error ? error.message : '投递失败，请稍后重试')
  } finally {
    applyingJobId.value = null
  }
}
</script>

<template>
  <div class="candidate-page candidate-jobs">
    <CandidatePageHeader title="招聘职位" description="浏览当前开放的岗位，选择适合你的机会。">
      <template #actions>
        <div class="candidate-job-search">
          <Search :size="17" :stroke-width="1.75" aria-hidden="true" />
          <el-input
            v-model="keyword"
            clearable
            placeholder="搜索职位、部门或地点"
            aria-label="搜索职位"
            @keyup.enter="searchJobs"
            @clear="searchJobs"
          />
          <el-button @click="searchJobs">搜索</el-button>
        </div>
      </template>
    </CandidatePageHeader>

    <div v-if="jobs.loading.value" class="candidate-skeleton-list">
      <div v-for="index in 3" :key="index" class="candidate-skeleton-card">
        <el-skeleton :rows="3" animated />
      </div>
    </div>
    <CandidateErrorState
      v-else-if="jobs.error.value"
      description="招聘职位暂时无法加载，请稍后重试。"
      retryable
      @retry="jobs.reload"
    />
    <div v-else-if="filteredJobs.length" class="candidate-list">
      <CandidateListItem v-for="job in filteredJobs" :key="job.id" interactive>
        <div class="candidate-job-card">
          <div class="candidate-job-card__title">
            <span class="candidate-job-card__icon">
              <BriefcaseBusiness :size="20" :stroke-width="1.7" />
            </span>
            <div>
              <h2>{{ job.title }}</h2>
              <p><MapPin :size="14" />{{ job.department }}，{{ job.location || '地点待定' }}</p>
            </div>
          </div>
          <dl class="candidate-job-card__facts">
            <div>
              <dt><WalletCards :size="15" />薪资</dt>
              <dd>{{ job.salaryRange || '薪资面议' }}</dd>
            </div>
            <div>
              <dt><BriefcaseBusiness :size="15" />经验</dt>
              <dd>{{ job.experienceRequirement || '经验不限' }}</dd>
            </div>
            <div>
              <dt><GraduationCap :size="15" />学历</dt>
              <dd>{{ job.educationRequirement || '学历不限' }}</dd>
            </div>
          </dl>
          <div class="candidate-job-card__actions">
            <el-button @click="openDetail(job.id)">查看详情</el-button>
            <el-button
              type="primary"
              :loading="applyingJobId === job.id"
              :disabled="applyingJobId !== null && applyingJobId !== job.id"
              @click="apply(job.id, job.title)"
              >投递职位</el-button
            >
          </div>
        </div>
      </CandidateListItem>
    </div>
    <CandidateEmptyState
      v-else
      :icon="keyword ? SearchX : BriefcaseBusiness"
      :title="keyword ? '没有找到匹配职位' : '暂时没有开放职位'"
      :description="
        keyword ? '尝试使用职位名称、部门或地点重新搜索。' : '新的职位发布后会显示在这里。'
      "
    >
      <template v-if="keyword" #actions>
        <el-button @click="clearSearch">清除搜索</el-button>
      </template>
    </CandidateEmptyState>

    <el-pagination
      v-if="jobs.data.value.total > jobs.query.pageSize"
      v-model:current-page="jobs.query.page"
      class="portal-pagination"
      background
      layout="prev, pager, next"
      :page-size="jobs.query.pageSize"
      :total="jobs.data.value.total"
    />
    <CandidateJobDetailDrawer
      v-model:visible="detailVisible"
      :job="selectedJob"
      :loading="detailLoading"
      :error="detailError"
      :submitting="applyingJobId === selectedJob?.id"
      @apply="(job) => apply(job.id, job.title)"
    />
  </div>
</template>

<style scoped lang="scss">
.candidate-job-search {
  display: flex;
  align-items: center;
  gap: var(--rs-space-2);
  width: 420px;
  padding-left: var(--rs-space-3);
  border: 1px solid var(--rs-border-strong);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
  color: var(--rs-text-tertiary);
}
.candidate-job-search :deep(.el-input__wrapper) {
  box-shadow: none;
  padding-left: 0;
}
.candidate-job-search > .el-button {
  margin-right: -1px;
  border-radius: 0 var(--rs-radius-sm) var(--rs-radius-sm) 0;
}
.candidate-job-card {
  display: grid;
  grid-template-columns: minmax(260px, 1.25fr) minmax(420px, 1.5fr) auto;
  align-items: center;
  gap: var(--rs-space-8);
}
.candidate-job-card__title {
  display: flex;
  align-items: flex-start;
  gap: var(--rs-space-3);
}
.candidate-job-card__icon {
  display: grid;
  flex: 0 0 40px;
  width: 40px;
  height: 40px;
  place-items: center;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-blue-050);
  color: var(--rs-blue-700);
}
.candidate-job-card h2,
.candidate-job-card p,
.candidate-job-card dl,
.candidate-job-card dd {
  margin: 0;
}
.candidate-job-card h2 {
  font-size: 16px;
}
.candidate-job-card p {
  display: flex;
  align-items: center;
  gap: var(--rs-space-1);
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}
.candidate-job-card__facts {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--rs-space-4);
}
.candidate-job-card__facts dt {
  display: flex;
  align-items: center;
  gap: var(--rs-space-1);
  color: var(--rs-text-tertiary);
  font-size: 12px;
}
.candidate-job-card__facts dd {
  margin-top: var(--rs-space-1);
  overflow: hidden;
  color: var(--rs-text-primary);
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.candidate-job-card__actions {
  display: flex;
  gap: var(--rs-space-2);
}
</style>
