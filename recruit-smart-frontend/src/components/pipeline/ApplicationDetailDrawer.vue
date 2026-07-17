<script setup lang="ts">
import { ElMessage } from 'element-plus'
import {
  CalendarClock,
  Download,
  Eye,
  FileText,
  ShieldCheck,
  Sparkles,
  UserRound,
} from 'lucide-vue-next'
import { ref, watch } from 'vue'

import {
  getResumeDownloadFile,
  getResumePreviewFile,
  openBlobPreview,
  saveBlobAsFile,
} from '@/api/resumes'
import { getApplicationStatusTone } from '@/config/pipeline'
import type { PipelineApplicationDetail, ScreeningDecision } from '@/types/pipeline'

const visible = defineModel<boolean>('visible', { required: true })

defineProps<{
  application: PipelineApplicationDetail | undefined
  loading: boolean
  error: Error | null
  canManage: boolean
  demoMode: boolean
}>()

const emit = defineEmits<{
  startScreening: [application: PipelineApplicationDetail]
  review: [decision: ScreeningDecision, application: PipelineApplicationDetail]
}>()

const activeTab = ref('summary')
const resumeActionLoading = ref(false)

watch(visible, (isVisible) => {
  if (isVisible) activeTab.value = 'summary'
})

function formatDate(value: string | null | undefined) {
  if (!value) return '待补充'
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}

function isPdfResume(application: PipelineApplicationDetail) {
  return (
    application.resumeFileType?.toUpperCase() === 'PDF' ||
    application.resumeName?.toLowerCase().endsWith('.pdf')
  )
}

async function previewResume(application: PipelineApplicationDetail) {
  if (!isPdfResume(application)) {
    ElMessage.warning('仅 PDF 简历支持在线预览，请下载后查看')
    return
  }
  resumeActionLoading.value = true
  try {
    openBlobPreview(await getResumePreviewFile(application.resumeId))
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '简历预览失败')
  } finally {
    resumeActionLoading.value = false
  }
}

async function downloadResume(application: PipelineApplicationDetail) {
  resumeActionLoading.value = true
  try {
    saveBlobAsFile(await getResumeDownloadFile(application.resumeId))
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '简历下载失败')
  } finally {
    resumeActionLoading.value = false
  }
}
</script>

<template>
  <el-drawer v-model="visible" title="投递详情" size="520px" destroy-on-close>
    <div v-if="loading" class="application-detail__loading">
      <el-skeleton :rows="9" animated />
    </div>

    <div v-else-if="error" class="application-detail__error" role="alert">
      <strong>投递详情加载失败</strong>
      <span>{{ error.message }}</span>
    </div>

    <div v-else-if="application" class="application-detail">
      <header class="application-detail__header">
        <span class="application-detail__avatar" aria-hidden="true">
          {{ application.candidateName.slice(0, 1) }}
        </span>
        <div>
          <div class="application-detail__title">
            <h2>{{ application.candidateName }}</h2>
            <span
              class="rs-status-pill"
              :class="`rs-status-pill--${getApplicationStatusTone(application.status)}`"
            >
              {{ application.statusText }}
            </span>
          </div>
          <p>{{ application.jobTitle }} · {{ application.department }}</p>
          <span>投递于 {{ formatDate(application.appliedAt) }}</span>
        </div>
      </header>

      <div v-if="demoMode" class="application-detail__source">当前详情来自前端演示数据</div>

      <el-tabs v-model="activeTab" class="application-detail__tabs">
        <el-tab-pane label="概览" name="summary">
          <dl class="application-detail__facts">
            <div>
              <dt>学历与学校</dt>
              <dd>
                {{ application.education || '待补充' }} · {{ application.school || '待补充' }}
              </dd>
            </div>
            <div>
              <dt>工作经验</dt>
              <dd>{{ application.yearsOfExperience }} 年</dd>
            </div>
            <div>
              <dt>投递简历</dt>
              <dd class="application-detail__resume">
                <span>{{ application.resumeName || '待补充' }}</span>
                <span v-if="application.resumeName" class="application-detail__resume-actions">
                  <el-button
                    text
                    type="primary"
                    :icon="Eye"
                    :loading="resumeActionLoading"
                    @click="previewResume(application)"
                  >
                    预览 PDF
                  </el-button>
                  <el-button
                    text
                    type="primary"
                    :icon="Download"
                    :loading="resumeActionLoading"
                    @click="downloadResume(application)"
                  >
                    下载
                  </el-button>
                </span>
              </dd>
            </div>
            <div>
              <dt>处理人</dt>
              <dd>{{ application.ownerName || '待分配' }}</dd>
            </div>
            <div>
              <dt>来源</dt>
              <dd>{{ application.sourceText }}</dd>
            </div>
            <div>
              <dt>接受调剂</dt>
              <dd>{{ application.allowAdjustment ? '是' : '否' }}</dd>
            </div>
          </dl>

          <section class="application-detail__section">
            <h3><FileText :size="16" :stroke-width="1.75" /> HR 记录</h3>
            <p>{{ application.hrNote || '暂无 HR 备注。' }}</p>
            <p v-if="application.rejectReason" class="application-detail__reject">
              拒绝原因：{{ application.rejectReason }}
            </p>
          </section>

          <section v-if="application.interview" class="application-detail__section">
            <h3><CalendarClock :size="16" :stroke-width="1.75" /> 面试安排</h3>
            <p>
              {{ application.interview.round }} · {{ application.interview.interviewerName }} ·
              {{ formatDate(application.interview.interviewTime) }}
            </p>
            <span
              >{{ application.interview.statusText }} · {{ application.interview.location }}</span
            >
          </section>

          <section v-if="application.offer" class="application-detail__section">
            <h3><ShieldCheck :size="16" :stroke-width="1.75" /> Offer</h3>
            <p>{{ application.offer.statusText }} · {{ application.offer.workLocation }}</p>
            <span>预计入职：{{ application.offer.entryDate || '待确认' }}</span>
          </section>
        </el-tab-pane>

        <el-tab-pane label="AI 参考" name="ai">
          <div v-if="application.aiMatch" class="application-ai">
            <header>
              <div>
                <Sparkles :size="18" :stroke-width="1.75" aria-hidden="true" />
                <strong>岗位匹配参考 {{ application.aiMatch.matchScore }} 分</strong>
              </div>
              <span
                >{{ application.aiMatch.modelName }} ·
                {{ formatDate(application.aiMatch.generatedAt) }}</span
              >
            </header>
            <p>{{ application.aiMatch.recommendReason }}</p>
            <h3>匹配亮点</h3>
            <p>{{ application.aiMatch.highlightSummary || '暂无匹配亮点。' }}</p>
            <h3>待核实事项</h3>
            <p>{{ application.aiMatch.riskSummary || '暂无待核实事项。' }}</p>
            <div class="application-ai__suggestion">
              推荐等级：{{ application.aiMatch.recommendLevel }}
            </div>
            <p class="application-ai__authority">
              AI 只提供分析参考，不会自动通过、拒绝或改变候选人阶段。
            </p>
          </div>
          <div v-else class="application-detail__empty">当前投递暂无 AI 匹配结果。</div>
        </el-tab-pane>

        <el-tab-pane label="活动" name="timeline">
          <ol class="application-timeline">
            <li v-for="event in [...application.timeline].reverse()" :key="event.id">
              <span class="application-timeline__marker" aria-hidden="true" />
              <div>
                <header>
                  <strong>{{ event.title }}</strong>
                  <span v-if="event.source === 'AI'" class="application-timeline__source">AI</span>
                </header>
                <p>{{ event.description }}</p>
                <span>{{ event.actorName }} · {{ formatDate(event.occurredAt) }}</span>
              </div>
            </li>
          </ol>
        </el-tab-pane>
      </el-tabs>

      <footer class="application-detail__actions">
        <template v-if="canManage && application.status === 'SUBMITTED'">
          <el-button type="primary" @click="emit('startScreening', application)">
            开始筛选
          </el-button>
        </template>
        <template v-else-if="canManage && application.status === 'SCREENING'">
          <el-button @click="emit('review', 'PENDING', application)">保留待定</el-button>
          <el-button type="danger" plain @click="emit('review', 'REJECT', application)">
            初筛未通过
          </el-button>
          <el-button type="primary" @click="emit('review', 'PASS', application)">
            初筛通过
          </el-button>
        </template>
        <div v-else-if="!canManage" class="application-detail__readonly">
          <UserRound :size="16" :stroke-width="1.75" aria-hidden="true" />
          面试官可查看流程上下文，筛选状态只能由 HR 修改。
        </div>
        <span v-else>当前阶段的后续操作将在面试、Offer 或入职页面处理。</span>
      </footer>
    </div>
  </el-drawer>
</template>

<style scoped lang="scss">
.application-detail,
.application-detail__header > div,
.application-detail__section,
.application-ai,
.application-timeline li > div {
  display: grid;
  gap: var(--rs-space-3);
}

.application-detail__header,
.application-detail__title,
.application-detail__section h3,
.application-ai header div,
.application-timeline header,
.application-detail__readonly {
  display: flex;
  align-items: center;
}

.application-detail__header {
  gap: var(--rs-space-3);
  padding-bottom: var(--rs-space-4);
  border-bottom: 1px solid var(--rs-border-default);
}

.application-detail__avatar {
  display: grid;
  flex: 0 0 44px;
  width: 44px;
  height: 44px;
  place-items: center;
  border-radius: 50%;
  background: var(--rs-action-primary);
  color: var(--rs-white);
  font-size: 16px;
  font-weight: 700;
}

.application-detail__title {
  gap: var(--rs-space-2);
}

.application-detail__title h2,
.application-detail__header p,
.application-detail__section h3,
.application-detail__section p,
.application-ai p,
.application-ai h3,
.application-timeline p,
.application-timeline header,
.application-timeline ol {
  margin: 0;
}

.application-detail__title h2 {
  font-size: 18px;
}

.application-detail__header p {
  color: var(--rs-text-secondary);
}

.application-detail__header span,
.application-detail__section span,
.application-ai header > span,
.application-timeline li span,
.application-detail__actions > span {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}

.application-detail__source,
.application-detail__readonly,
.application-ai__authority {
  padding: var(--rs-space-2) var(--rs-space-3);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-muted);
  color: var(--rs-text-secondary);
  font-size: 12px;
}

.application-detail__facts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--rs-space-2);
  margin: 0;
}

.application-detail__facts div {
  display: grid;
  gap: var(--rs-space-1);
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
}

.application-detail__facts dt {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}

.application-detail__facts dd {
  margin: 0;
  color: var(--rs-text-primary);
  font-weight: 600;
}

.application-detail__resume,
.application-detail__resume-actions {
  display: flex;
  align-items: center;
  gap: var(--rs-space-2);
}

.application-detail__resume {
  flex-wrap: wrap;
}

.application-detail__section {
  padding-top: var(--rs-space-4);
  border-top: 1px solid var(--rs-border-default);
}

.application-detail__section h3,
.application-ai header div {
  gap: var(--rs-space-2);
  font-size: 14px;
}

.application-detail__reject {
  color: var(--rs-danger-700);
}

.application-ai header {
  display: grid;
  gap: var(--rs-space-1);
}

.application-ai h3 {
  font-size: 13px;
}

.application-ai ul {
  display: grid;
  gap: var(--rs-space-2);
  margin: 0;
  padding-left: 20px;
  color: var(--rs-text-secondary);
}

.application-ai__suggestion {
  padding: var(--rs-space-3);
  border-left: 3px solid var(--rs-blue-500);
  background: var(--rs-blue-050);
  color: var(--rs-text-secondary);
}

.application-ai__authority {
  border: 1px solid var(--rs-border-default);
}

.application-timeline {
  display: grid;
  gap: 0;
  margin: 0;
  padding: 0;
  list-style: none;
}

.application-timeline li {
  position: relative;
  display: grid;
  grid-template-columns: 16px 1fr;
  gap: var(--rs-space-2);
  padding-bottom: var(--rs-space-4);
}

.application-timeline li:not(:last-child)::before {
  position: absolute;
  top: 14px;
  bottom: 0;
  left: 5px;
  width: 1px;
  background: var(--rs-border-strong);
  content: '';
}

.application-timeline__marker {
  z-index: 1;
  width: 11px;
  height: 11px;
  margin-top: 3px;
  border: 2px solid var(--rs-blue-500);
  border-radius: 50%;
  background: var(--rs-surface-primary);
}

.application-timeline header {
  justify-content: space-between;
  gap: var(--rs-space-2);
}

.application-timeline__source {
  padding: 1px 5px;
  border-radius: var(--rs-radius-xs);
  background: var(--rs-blue-050);
  color: var(--rs-blue-700);
  font-weight: 700;
}

.application-detail__actions {
  position: sticky;
  bottom: 0;
  display: flex;
  justify-content: flex-end;
  gap: var(--rs-space-2);
  padding: var(--rs-space-3) 0;
  border-top: 1px solid var(--rs-border-default);
  background: var(--rs-surface-primary);
}

.application-detail__readonly {
  width: 100%;
  gap: var(--rs-space-2);
}

.application-detail__error,
.application-detail__empty {
  display: grid;
  gap: var(--rs-space-2);
  padding: var(--rs-space-4);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  color: var(--rs-text-secondary);
}
</style>
