<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getMyApplicationDetail,
  getMyApplications,
  withdrawMyApplication,
} from '@/api/candidatePortal'
import { getOrCreateConversation } from '@/api/messages'
import CandidateApplicationDetailDrawer from '@/components/candidate/CandidateApplicationDetailDrawer.vue'
import { usePortalPagedResource } from '@/composables/usePortalPagedResource'
import { demoMyApplications } from '@/config/demoCandidatePortal'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import type { CandidateApplicationDetail } from '@/types/portal'

const resource = usePortalPagedResource(getMyApplications, demoMyApplications)
const router = useRouter()
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailError = ref('')
const selectedApplication = ref<CandidateApplicationDetail | null>(null)
const withdrawable = (status: string) => ['SUBMITTED', 'SCREENING'].includes(status)
async function withdraw(id: number) {
  await ElMessageBox.confirm('撤回后该投递将停止推进，确认撤回？', '撤回投递', { type: 'warning' })
  if (!resource.demoMode.value) await withdrawMyApplication(id)
  resource.data.value.items = resource.data.value.items.map((item) =>
    item.id === id ? { ...item, status: 'WITHDRAWN', statusText: '已撤回' } : item,
  )
  ElMessage.success('投递已撤回')
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
  } catch (error) {
    detailError.value = error instanceof Error ? error.message : '投递详情加载失败'
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
  <div class="role-portal">
    <div class="portal-toolbar">
      <div>
        <h2>我的投递记录</h2>
        <p>状态来自与你当前账号绑定的投递记录。</p>
      </div>
      <span v-if="resource.demoMode.value" class="portal-demo-note">候选人演示数据</span>
    </div>
    <section class="portal-panel">
      <div v-if="resource.loading.value" class="portal-loading">正在加载我的投递...</div>
      <div v-else-if="resource.error.value" class="portal-error">{{ resource.error.value }}</div>
      <div v-else-if="resource.data.value.items.length" class="portal-list">
        <article v-for="item in resource.data.value.items" :key="item.id" class="portal-row">
          <div class="portal-row__primary">
            <h3>{{ item.jobTitle }}</h3>
            <p>{{ item.department }} · {{ item.resumeName }}</p>
          </div>
          <div class="portal-row__cell">
            <strong>{{ new Date(item.appliedAt).toLocaleDateString('zh-CN') }}</strong
            ><span>投递日期</span>
          </div>
          <span class="rs-status-pill rs-status-pill--info">{{ item.statusText }}</span>
          <div class="portal-row__actions">
            <el-button text @click="openDetail(item.id)">查看详情</el-button>
            <el-button
              v-if="withdrawable(item.status)"
              text
              type="danger"
              @click="withdraw(item.id)"
              >撤回</el-button
            >
          </div>
        </article>
      </div>
      <div v-else class="portal-empty">你还没有投递记录。</div>
      <el-pagination
        v-if="resource.data.value.total > resource.query.pageSize"
        v-model:current-page="resource.query.page"
        class="portal-pagination"
        background
        layout="prev, pager, next"
        :page-size="resource.query.pageSize"
        :total="resource.data.value.total"
      />
    </section>
    <CandidateApplicationDetailDrawer
      v-model:visible="detailVisible"
      :application="selectedApplication"
      :loading="detailLoading"
      :error="detailError"
      @contact="contact"
    />
  </div>
</template>
