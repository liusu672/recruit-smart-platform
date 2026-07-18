<script setup lang="ts">
import { Building2, CalendarDays, MapPin, UsersRound, WalletCards } from 'lucide-vue-next'

import { getJobStatusTone } from '@/config/jobs'
import type { JobPosition } from '@/types/job'
import type { JobApplicationRecord } from '@/api/jobs'

defineProps<{
  visible: boolean
  job: JobPosition | undefined
  loading: boolean
  error: Error | null
  demoMode: boolean
  actionLoading: boolean
  applications: JobApplicationRecord[]
  applicationsLoading: boolean
  applicationsError: Error | null
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  edit: [job: JobPosition]
  publish: [job: JobPosition]
  pause: [job: JobPosition]
  resume: [job: JobPosition]
  closeJob: [job: JobPosition]
  viewPipeline: [jobId: number]
}>()

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
  <el-drawer
    :model-value="visible"
    title="职位详情"
    size="440px"
    @update:model-value="emit('update:visible', $event)"
  >
    <el-skeleton v-if="loading" :rows="8" animated />

    <el-result v-else-if="error" icon="error" title="职位详情加载失败" :sub-title="error.message">
      <template #extra>
        <el-button @click="emit('update:visible', false)">关闭</el-button>
      </template>
    </el-result>

    <div v-else-if="job" class="job-detail">
      <el-alert
        v-if="demoMode"
        title="当前展示演示数据，操作不会写入后端。"
        type="info"
        :closable="false"
        show-icon
      />

      <header class="job-detail__header">
        <div>
          <h2>{{ job.title }}</h2>
          <p>{{ job.department }}</p>
        </div>
        <span class="rs-status-pill" :class="`rs-status-pill--${getJobStatusTone(job.status)}`">
          {{ job.statusText }}
        </span>
      </header>

      <dl class="job-detail__facts">
        <div>
          <dt><Building2 :size="16" :stroke-width="1.75" aria-hidden="true" />所属部门</dt>
          <dd>{{ job.department }}</dd>
        </div>
        <div>
          <dt><MapPin :size="16" :stroke-width="1.75" aria-hidden="true" />工作地点</dt>
          <dd>{{ job.location || '待补充' }}</dd>
        </div>
        <div>
          <dt><WalletCards :size="16" :stroke-width="1.75" aria-hidden="true" />薪资范围</dt>
          <dd>{{ job.salaryRange ? `${job.salaryRange} 元/月` : '待补充' }}</dd>
        </div>
        <div>
          <dt><UsersRound :size="16" :stroke-width="1.75" aria-hidden="true" />招聘人数</dt>
          <dd>{{ job.headcount }} 人</dd>
        </div>
        <div class="job-detail__fact-wide">
          <dt><CalendarDays :size="16" :stroke-width="1.75" aria-hidden="true" />最近更新</dt>
          <dd>{{ formatDate(job.updatedAt || job.createdAt) }}</dd>
        </div>
      </dl>

      <section class="job-detail__section">
        <h3>岗位职责</h3>
        <p>{{ job.description || '暂未填写岗位职责。' }}</p>
      </section>

      <section class="job-detail__section">
        <div class="job-detail__section-heading">
          <h3>最近投递</h3>
          <el-button link type="primary" @click="emit('viewPipeline', job.id)"
            >查看招聘流程</el-button
          >
        </div>
        <div v-if="applicationsLoading" class="job-detail__empty">正在加载投递记录...</div>
        <el-alert
          v-else-if="applicationsError"
          type="error"
          :closable="false"
          :title="applicationsError.message"
        />
        <div v-else-if="applications.length" class="job-application-list">
          <article v-for="application in applications" :key="application.id">
            <div>
              <strong>{{ application.candidateName }}</strong>
              <span
                >{{ application.education || '学历待补充' }} ·
                {{ application.yearsOfExperience ?? 0 }} 年经验</span
              >
            </div>
            <span class="rs-status-pill rs-status-pill--info">{{ application.statusText }}</span>
          </article>
        </div>
        <div v-else class="job-detail__empty">该职位暂无投递记录。</div>
      </section>

      <section class="job-detail__section">
        <h3>任职要求</h3>
        <p>{{ job.requirement || '暂未填写任职要求。' }}</p>
      </section>
    </div>

    <template v-if="job && !loading && !error" #footer>
      <div class="job-detail__actions">
        <el-button :disabled="actionLoading || job.status === 'CLOSED'" @click="emit('edit', job)">
          编辑
        </el-button>
        <el-button
          v-if="job.status === 'DRAFT'"
          type="primary"
          :loading="actionLoading"
          @click="emit('publish', job)"
        >
          发布职位
        </el-button>
        <el-button
          v-else-if="job.status === 'OPEN'"
          type="warning"
          plain
          :loading="actionLoading"
          @click="emit('pause', job)"
        >
          暂停职位
        </el-button>
        <el-button
          v-else-if="job.status === 'PAUSED'"
          type="primary"
          :loading="actionLoading"
          @click="emit('resume', job)"
        >
          恢复职位
        </el-button>
        <el-button
          v-if="job.status === 'OPEN' || job.status === 'PAUSED'"
          type="danger"
          plain
          :loading="actionLoading"
          @click="emit('closeJob', job)"
        >
          关闭职位
        </el-button>
      </div>
    </template>
  </el-drawer>
</template>

<style scoped lang="scss">
.job-detail {
  display: grid;
  gap: var(--rs-space-6);
}

.job-detail__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--rs-space-4);
}

.job-detail__header h2,
.job-detail__header p,
.job-detail__section h3,
.job-detail__section p,
.job-detail__facts dd {
  margin: 0;
}

.job-detail__header h2 {
  font-size: 18px;
  line-height: 1.35;
}

.job-detail__header p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}

.job-detail__facts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--rs-space-4);
  margin: 0;
  padding: var(--rs-space-4);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
}

.job-detail__facts div {
  min-width: 0;
}

.job-detail__facts dt {
  display: flex;
  align-items: center;
  gap: var(--rs-space-1);
  color: var(--rs-text-tertiary);
  font-size: 12px;
}

.job-detail__facts dd {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-primary);
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.job-detail__fact-wide {
  grid-column: 1 / -1;
}

.job-detail__section {
  padding-top: var(--rs-space-4);
  border-top: 1px solid var(--rs-border-default);
}

.job-detail__section h3 {
  font-size: 14px;
  font-weight: 600;
}

.job-detail__section-heading,
.job-application-list article {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--rs-space-3);
}

.job-application-list {
  display: grid;
  gap: var(--rs-space-2);
  margin-top: var(--rs-space-2);
}

.job-application-list article {
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
}

.job-application-list article > div {
  display: grid;
}

.job-application-list article span,
.job-detail__empty {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}

.job-detail__empty {
  padding: var(--rs-space-4) 0;
}

.job-detail__section p {
  margin-top: var(--rs-space-2);
  color: var(--rs-text-secondary);
  white-space: pre-wrap;
}

.job-detail__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--rs-space-2);
}
</style>
