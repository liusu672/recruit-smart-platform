<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { BriefcaseBusiness, FileText } from 'lucide-vue-next'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

import {
  getMyApplicationDetail,
  getMyApplications,
  withdrawMyApplication,
} from '@/api/candidatePortal'
import { getOrCreateConversation } from '@/api/messages'
import CandidateApplicationDetailDrawer from '@/components/candidate/CandidateApplicationDetailDrawer.vue'
import CandidateEmptyState from '@/components/candidate/CandidateEmptyState.vue'
import CandidateErrorState from '@/components/candidate/CandidateErrorState.vue'
import CandidateListItem from '@/components/candidate/CandidateListItem.vue'
import CandidatePageHeader from '@/components/candidate/CandidatePageHeader.vue'
import CandidateProgressSteps from '@/components/candidate/CandidateProgressSteps.vue'
import CandidateStatusBadge from '@/components/candidate/CandidateStatusBadge.vue'
import { usePortalPagedResource } from '@/composables/usePortalPagedResource'
import { demoMyApplications } from '@/config/demoCandidatePortal'
import type { CandidateApplicationDetail } from '@/types/portal'

const progressSteps = [
  { key: 'submitted', label: '已投递' },
  { key: 'screening', label: '简历筛选' },
  { key: 'interview', label: '面试' },
  { key: 'offer', label: 'Offer' },
  { key: 'complete', label: '已完成' },
]

const resource = usePortalPagedResource(getMyApplications, demoMyApplications)
const router = useRouter()
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailError = ref('')
const selectedApplication = ref<CandidateApplicationDetail | null>(null)
const withdrawingId = ref<number | null>(null)

const withdrawable = (status: string) => ['SUBMITTED', 'SCREENING'].includes(status)

function progressIndex(status: string) {
  if (status === 'HIRED') return 4
  if (status === 'OFFERED') return 3
  if (status === 'INTERVIEWING') return 2
  if (['SCREENING', 'SCREEN_PASSED'].includes(status)) return 1
  return 0
}

function terminalState(status: string): 'danger' | 'neutral' | null {
  if (['SCREEN_REJECT', 'REJECTED'].includes(status)) return 'danger'
  if (status === 'WITHDRAWN') return 'neutral'
  return null
}

function terminalLabel(status: string) {
  if (status === 'WITHDRAWN') return '已撤回'
  if (['SCREEN_REJECT', 'REJECTED'].includes(status)) return '流程已结束'
  return undefined
}

async function withdraw(id: number) {
  if (withdrawingId.value !== null) return
  try {
    await ElMessageBox.confirm('撤回后该投递将停止推进，确认撤回？', '撤回投递', {
      confirmButtonText: '确认撤回',
      cancelButtonText: '取消',
      type: 'warning',
    })
    withdrawingId.value = id
    if (!resource.demoMode.value) await withdrawMyApplication(id)
    resource.data.value.items = resource.data.value.items.map((item) =>
      item.id === id ? { ...item, status: 'WITHDRAWN', statusText: '已撤回' } : item,
    )
    ElMessage.success('投递已撤回')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error instanceof Error ? error.message : '投递撤回失败')
  } finally {
    withdrawingId.value = null
  }
}

async function openDetail(id: number) {
  detailVisible.value = true
  detailLoading.value = true
  detailError.value = ''
  try {
    if (resource.demoMode.value) {
      const application = resource.data.value.items.find((item) => item.id === id)
      if (!application) throw new Error('演示投递记录不存在')
      selectedApplication.value = {
        ...application,
        candidateId: 0,
        candidateName: '当前候选人',
        resumeFileType: null,
        source: 'DEMO',
        rejectReasonCode: null,
        rejectReason: null,
        hrNote: null,
        reviewedAt: null,
        createdAt: application.appliedAt,
        updatedAt: application.appliedAt,
      }
    } else {
      selectedApplication.value = await getMyApplicationDetail(id)
    }
  } catch {
    detailError.value = '投递详情暂时无法加载，请稍后重试。'
  } finally {
    detailLoading.value = false
  }
}

async function contact(applicationId: number) {
  try {
    const conversationId = resource.demoMode.value
      ? null
      : await getOrCreateConversation(applicationId)
    await router.push({
      path: '/candidate/messages',
      ...(conversationId ? { query: { conversationId: String(conversationId) } } : {}),
    })
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '消息会话创建失败')
  }
}
</script>

<template>
  <div class="candidate-page candidate-applications">
    <CandidatePageHeader title="我的投递" description="查看每个职位申请的当前进度和处理状态。">
      <template v-if="resource.demoMode.value" #actions>
        <span class="candidate-demo-note">候选人演示数据</span>
      </template>
    </CandidatePageHeader>

    <div v-if="resource.loading.value" class="candidate-skeleton-list">
      <div v-for="index in 3" :key="index" class="candidate-skeleton-card">
        <el-skeleton :rows="3" animated />
      </div>
    </div>
    <CandidateErrorState
      v-else-if="resource.error.value"
      description="投递记录暂时无法加载，请稍后重试。"
      retryable
      @retry="resource.reload"
    />
    <div v-else-if="resource.data.value.items.length" class="candidate-list">
      <CandidateListItem v-for="item in resource.data.value.items" :key="item.id" interactive>
        <div class="candidate-application-card__header">
          <div class="candidate-application-card__title">
            <span><BriefcaseBusiness :size="20" :stroke-width="1.7" /></span>
            <div>
              <h2>{{ item.jobTitle }}</h2>
              <p>
                {{ item.department }}，{{ new Date(item.appliedAt).toLocaleDateString('zh-CN') }}
                投递
              </p>
            </div>
          </div>
          <CandidateStatusBadge :status="item.status" :label="item.statusText" />
        </div>
        <CandidateProgressSteps
          :steps="progressSteps"
          :current-step="progressIndex(item.status)"
          :terminal-state="terminalState(item.status)"
          :terminal-label="terminalLabel(item.status)"
        />
        <div class="candidate-application-card__footer">
          <span class="candidate-meta"><FileText :size="14" />使用简历：{{ item.resumeName }}</span>
          <div class="candidate-actions">
            <el-button @click="openDetail(item.id)">查看详情</el-button>
            <el-button
              v-if="withdrawable(item.status)"
              text
              type="danger"
              :loading="withdrawingId === item.id"
              @click="withdraw(item.id)"
              >撤回投递</el-button
            >
          </div>
        </div>
      </CandidateListItem>
    </div>
    <CandidateEmptyState
      v-else
      :icon="BriefcaseBusiness"
      title="还没有投递记录"
      description="浏览开放职位，找到适合你的机会后开始投递。"
    >
      <template #actions>
        <RouterLink to="/candidate/jobs"><el-button type="primary">浏览职位</el-button></RouterLink>
      </template>
    </CandidateEmptyState>

    <el-pagination
      v-if="resource.data.value.total > resource.query.pageSize"
      v-model:current-page="resource.query.page"
      class="portal-pagination"
      background
      layout="prev, pager, next"
      :page-size="resource.query.pageSize"
      :total="resource.data.value.total"
    />
    <CandidateApplicationDetailDrawer
      v-model:visible="detailVisible"
      :application="selectedApplication"
      :loading="detailLoading"
      :error="detailError"
      @contact="contact"
    />
  </div>
</template>

<style scoped lang="scss">
.candidate-application-card__header,
.candidate-application-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--rs-space-4);
}
.candidate-application-card__title {
  display: flex;
  align-items: flex-start;
  gap: var(--rs-space-3);
}
.candidate-application-card__title > span {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-blue-050);
  color: var(--rs-blue-700);
}
.candidate-application-card h2,
.candidate-application-card p {
  margin: 0;
}
.candidate-application-card__title h2 {
  margin: 0;
  font-size: 16px;
}
.candidate-application-card__title p {
  margin: var(--rs-space-1) 0 0;
  color: var(--rs-text-secondary);
}
.candidate-application-card__footer {
  padding-top: var(--rs-space-3);
  border-top: 1px solid var(--rs-border-default);
}
.candidate-application-card__footer .candidate-meta {
  display: flex;
  align-items: center;
  gap: var(--rs-space-1);
}
</style>
