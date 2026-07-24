<script setup lang="ts">
import {
  AlertTriangle,
  BriefcaseBusiness,
  FileText,
  GraduationCap,
  Mail,
  Phone,
  Sparkles,
  UserRound,
  X,
} from 'lucide-vue-next'
import { computed, ref, watch } from 'vue'

import { getApplicationStatusTone, getCandidateStatusTone } from '@/config/candidates'
import type { CandidateDetail } from '@/types/candidate'

const props = defineProps<{
  candidate: CandidateDetail | undefined
  loading: boolean
  error: Error | null
  demoMode: boolean
}>()

defineEmits<{
  close: []
  edit: []
}>()

const activeTab = ref('profile')
const latestAiMatch = computed(
  () => props.candidate?.applications.find((application) => application.aiMatch)?.aiMatch ?? null,
)

watch(
  () => props.candidate?.id,
  () => {
    activeTab.value = 'profile'
  },
)

function formatDate(value: string | null) {
  if (!value) return '暂无记录'
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(new Date(value))
}
</script>

<template>
  <aside class="candidate-preview" aria-label="候选人预览">
    <header class="candidate-preview__topbar">
      <strong>候选人预览</strong>
      <div class="candidate-preview__topbar-actions">
        <el-button v-if="candidate" size="small" @click="$emit('edit')">编辑</el-button>
        <el-tooltip content="关闭预览" placement="top">
          <button
            class="candidate-preview__close"
            type="button"
            aria-label="关闭候选人预览"
            @click="$emit('close')"
          >
            <X :size="18" :stroke-width="1.75" aria-hidden="true" />
          </button>
        </el-tooltip>
      </div>
    </header>

    <div v-if="loading" class="candidate-preview__loading">
      <el-skeleton :rows="9" animated />
    </div>

    <el-result
      v-else-if="error"
      icon="error"
      title="候选人详情加载失败"
      :sub-title="error.message"
    />

    <div v-else-if="candidate" class="candidate-preview__body">
      <el-alert
        v-if="demoMode"
        title="当前详情来自演示数据。"
        type="info"
        :closable="false"
        show-icon
      />

      <section class="candidate-preview__identity">
        <span class="candidate-avatar" aria-hidden="true">
          <UserRound :size="24" :stroke-width="1.9" />
        </span>
        <div>
          <div class="candidate-preview__name-row">
            <h2>{{ candidate.name }}</h2>
            <span
              class="rs-status-pill"
              :class="`rs-status-pill--${getCandidateStatusTone(candidate.currentStatus)}`"
            >
              {{ candidate.currentStatusText }}
            </span>
          </div>
          <p>{{ candidate.school || '学校待补充' }} · {{ candidate.major || '专业待补充' }}</p>
          <p>{{ candidate.yearsOfExperience }} 年经验 · {{ candidate.sourceText }}</p>
        </div>
      </section>

      <el-tabs v-model="activeTab" class="candidate-preview__tabs">
        <el-tab-pane label="资料" name="profile">
          <dl class="candidate-profile-facts">
            <div>
              <dt><Phone :size="16" :stroke-width="1.75" aria-hidden="true" />手机号</dt>
              <dd>{{ candidate.phone || '待补充' }}</dd>
            </div>
            <div>
              <dt><Mail :size="16" :stroke-width="1.75" aria-hidden="true" />邮箱</dt>
              <dd>{{ candidate.email || '待补充' }}</dd>
            </div>
            <div>
              <dt><GraduationCap :size="16" :stroke-width="1.75" aria-hidden="true" />学历</dt>
              <dd>{{ candidate.education || '待补充' }}</dd>
            </div>
            <div>
              <dt>
                <BriefcaseBusiness :size="16" :stroke-width="1.75" aria-hidden="true" />最近职位
              </dt>
              <dd>{{ candidate.latestJobTitle || '暂无投递' }}</dd>
            </div>
          </dl>

          <el-alert
            v-if="candidate.duplicateRisk"
            title="检测到相似联系方式，请核对是否存在重复候选人记录。"
            type="warning"
            :closable="false"
            show-icon
          />
        </el-tab-pane>

        <el-tab-pane :label="`简历 ${candidate.resumes.length}`" name="resumes">
          <div v-if="candidate.resumes.length" class="candidate-record-list">
            <article v-for="resume in candidate.resumes" :key="resume.id" class="candidate-record">
              <div class="candidate-record__header">
                <FileText :size="18" :stroke-width="1.75" aria-hidden="true" />
                <div>
                  <strong>{{ resume.resumeName }}</strong>
                  <span
                    >{{ resume.fileType || '文本简历' }} · {{ formatDate(resume.updatedAt) }}</span
                  >
                </div>
                <span v-if="resume.isDefault" class="rs-status-pill rs-status-pill--info"
                  >默认</span
                >
              </div>
              <p>{{ resume.workExperience || resume.parsedContent || '暂无解析内容。' }}</p>
              <div v-if="resume.skills.length" class="candidate-skill-list" aria-label="简历技能">
                <span v-for="skill in resume.skills" :key="skill">{{ skill }}</span>
              </div>
            </article>
          </div>
          <el-empty v-else description="暂无简历" :image-size="72" />
        </el-tab-pane>

        <el-tab-pane :label="`投递 ${candidate.applications.length}`" name="applications">
          <div v-if="candidate.applications.length" class="candidate-record-list">
            <article
              v-for="application in candidate.applications"
              :key="application.id"
              class="candidate-record"
            >
              <div class="candidate-record__header">
                <BriefcaseBusiness :size="18" :stroke-width="1.75" aria-hidden="true" />
                <div>
                  <strong>{{ application.jobTitle }}</strong>
                  <span>{{ formatDate(application.appliedAt) }} · {{ application.source }}</span>
                </div>
                <span
                  class="rs-status-pill"
                  :class="`rs-status-pill--${getApplicationStatusTone(application.status)}`"
                >
                  {{ application.statusText }}
                </span>
              </div>
              <p>{{ application.hrNote || '暂无 HR 备注。' }}</p>
            </article>
          </div>
          <el-empty v-else description="暂无投递记录" :image-size="72" />
        </el-tab-pane>

        <el-tab-pane label="AI 辅助" name="ai">
          <div v-if="latestAiMatch" class="candidate-ai">
            <div class="candidate-ai__header">
              <Sparkles :size="18" :stroke-width="1.75" aria-hidden="true" />
              <div>
                <strong>岗位匹配参考 {{ latestAiMatch.score }} 分</strong>
                <span
                  >{{ latestAiMatch.modelName }} · {{ formatDate(latestAiMatch.generatedAt) }}</span
                >
              </div>
            </div>
            <p>{{ latestAiMatch.summary }}</p>
            <section>
              <h3>匹配亮点</h3>
              <ul>
                <li v-for="highlight in latestAiMatch.highlights" :key="highlight">
                  {{ highlight }}
                </li>
              </ul>
            </section>
            <section>
              <h3>
                <AlertTriangle :size="16" :stroke-width="1.75" aria-hidden="true" />待核实事项
              </h3>
              <ul>
                <li v-for="risk in latestAiMatch.risks" :key="risk">{{ risk }}</li>
              </ul>
            </section>
            <el-alert
              :title="latestAiMatch.suggestion"
              type="warning"
              :closable="false"
              show-icon
            />
            <p class="candidate-ai__authority">
              AI 结果仅供参考，筛选与录用状态必须由业务角色确认。
            </p>
          </div>
          <el-empty v-else description="暂无 AI 匹配结果" :image-size="72" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </aside>
</template>

<style scoped lang="scss">
.candidate-preview {
  min-width: 0;
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.candidate-preview__topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 var(--rs-space-4);
  border-bottom: 1px solid var(--rs-border-default);
}

.candidate-preview__close {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border: 0;
  border-radius: var(--rs-radius-sm);
  background: transparent;
  color: var(--rs-text-secondary);
  cursor: pointer;
}

.candidate-preview__topbar-actions {
  display: flex;
  align-items: center;
  gap: var(--rs-space-2);
}

.candidate-preview__close:hover {
  background: var(--rs-surface-muted);
  color: var(--rs-text-primary);
}

.candidate-preview__loading,
.candidate-preview__body {
  padding: var(--rs-space-4);
}

.candidate-preview__body {
  display: grid;
  gap: var(--rs-space-4);
  max-height: calc(100dvh - 252px);
  overflow-y: auto;
}

.candidate-preview__identity,
.candidate-preview__name-row,
.candidate-record__header,
.candidate-ai__header,
.candidate-profile-facts dt,
.candidate-ai h3 {
  display: flex;
  align-items: center;
}

.candidate-preview__identity {
  gap: var(--rs-space-3);
}

.candidate-avatar {
  display: grid;
  flex: 0 0 48px;
  width: 48px;
  height: 48px;
  place-items: center;
  border-radius: 50%;
  background: var(--rs-blue-050);
  color: var(--rs-blue-700);
}

.candidate-preview__name-row {
  gap: var(--rs-space-2);
}

.candidate-preview__identity h2,
.candidate-preview__identity p,
.candidate-profile-facts,
.candidate-profile-facts dd,
.candidate-record p,
.candidate-ai p,
.candidate-ai h3,
.candidate-ai ul {
  margin: 0;
}

.candidate-preview__identity h2 {
  font-size: 18px;
  line-height: 1.35;
}

.candidate-preview__identity p {
  color: var(--rs-text-secondary);
  font-size: 12px;
}

.candidate-profile-facts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--rs-space-3);
}

.candidate-profile-facts div {
  min-width: 0;
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
}

.candidate-profile-facts dt {
  gap: var(--rs-space-1);
  color: var(--rs-text-tertiary);
  font-size: 12px;
}

.candidate-profile-facts dd {
  margin-top: var(--rs-space-1);
  overflow-wrap: anywhere;
  color: var(--rs-text-primary);
  font-weight: 600;
}

.candidate-record-list,
.candidate-ai {
  display: grid;
  gap: var(--rs-space-3);
}

.candidate-record {
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
}

.candidate-record__header,
.candidate-ai__header {
  align-items: flex-start;
  gap: var(--rs-space-2);
}

.candidate-record__header > div,
.candidate-ai__header > div {
  display: grid;
  flex: 1;
  min-width: 0;
}

.candidate-record__header span,
.candidate-ai__header span {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}

.candidate-record p,
.candidate-ai p {
  margin-top: var(--rs-space-2);
  color: var(--rs-text-secondary);
}

.candidate-skill-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--rs-space-1);
  margin-top: var(--rs-space-2);
}

.candidate-skill-list span {
  padding: var(--rs-space-1) var(--rs-space-2);
  border-radius: var(--rs-radius-pill);
  background: var(--rs-surface-selected);
  color: var(--rs-blue-700);
  font-size: 12px;
  font-weight: 600;
}

.candidate-ai section {
  padding-top: var(--rs-space-3);
  border-top: 1px solid var(--rs-border-default);
}

.candidate-ai h3 {
  gap: var(--rs-space-1);
  font-size: 14px;
}

.candidate-ai ul {
  display: grid;
  gap: var(--rs-space-1);
  margin-top: var(--rs-space-2);
  padding-left: var(--rs-space-4);
  color: var(--rs-text-secondary);
}

.candidate-ai__authority {
  font-size: 12px;
}

@media (max-width: 1439px) {
  .candidate-preview {
    position: fixed;
    top: var(--rs-topbar-height);
    right: 0;
    bottom: 0;
    z-index: 20;
    width: var(--rs-detail-panel-width);
    border-radius: 0;
    box-shadow: var(--rs-shadow-floating);
  }

  .candidate-preview__body {
    max-height: calc(100dvh - 104px);
  }
}
</style>
