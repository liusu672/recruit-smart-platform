<script setup lang="ts">
import { CalendarClock, MapPin, Video } from 'lucide-vue-next'

import { getMyInterviews } from '@/api/candidatePortal'
import CandidateEmptyState from '@/components/candidate/CandidateEmptyState.vue'
import CandidateErrorState from '@/components/candidate/CandidateErrorState.vue'
import CandidateListItem from '@/components/candidate/CandidateListItem.vue'
import CandidatePageHeader from '@/components/candidate/CandidatePageHeader.vue'
import CandidateStatusBadge from '@/components/candidate/CandidateStatusBadge.vue'
import { usePortalPagedResource } from '@/composables/usePortalPagedResource'
import { demoMyInterviews } from '@/config/demoCandidatePortal'

const resource = usePortalPagedResource(getMyInterviews, demoMyInterviews)

function interviewDate(value: string | null) {
  if (!value) return { month: '待定', day: '--', time: '时间待通知' }
  const date = new Date(value)
  return {
    month: date.toLocaleDateString('zh-CN', { month: 'short' }),
    day: date.toLocaleDateString('zh-CN', { day: '2-digit' }),
    time: date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
  }
}
</script>

<template>
  <div class="candidate-page candidate-interviews">
    <CandidatePageHeader
      title="我的面试"
      description="集中查看与你本人投递关联的面试时间、方式和地点。"
    >
      <template v-if="resource.demoMode.value" #actions>
        <span class="candidate-demo-note">候选人演示数据</span>
      </template>
    </CandidatePageHeader>

    <div v-if="resource.loading.value" class="candidate-skeleton-list">
      <div v-for="index in 2" :key="index" class="candidate-skeleton-card">
        <el-skeleton :rows="3" animated />
      </div>
    </div>
    <CandidateErrorState
      v-else-if="resource.error.value"
      description="面试安排暂时无法加载，请稍后重试。"
      retryable
      @retry="resource.reload"
    />
    <div v-else-if="resource.data.value.items.length" class="candidate-list">
      <CandidateListItem v-for="item in resource.data.value.items" :key="item.id" interactive>
        <div class="candidate-interview-card">
          <div class="candidate-interview-card__date">
            <span>{{ interviewDate(item.interviewTime).month }}</span>
            <strong>{{ interviewDate(item.interviewTime).day }}</strong>
            <small>{{ interviewDate(item.interviewTime).time }}</small>
          </div>
          <div class="candidate-interview-card__copy">
            <div class="candidate-interview-card__title">
              <h2>{{ item.jobTitle }}</h2>
              <CandidateStatusBadge :status="item.status" :label="item.statusText" />
            </div>
            <p>{{ item.roundText }}，面试官 {{ item.interviewerName || '待确定' }}</p>
            <div class="candidate-interview-card__meta">
              <span><Video :size="15" />{{ item.methodText }}</span>
              <span><MapPin :size="15" />{{ item.location || '地点待通知' }}</span>
            </div>
          </div>
        </div>
      </CandidateListItem>
    </div>
    <CandidateEmptyState
      v-else
      :icon="CalendarClock"
      title="暂无面试安排"
      description="新的面试安排会显示在这里，你也可以继续浏览招聘职位。"
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
  </div>
</template>

<style scoped lang="scss">
.candidate-interview-card {
  display: grid;
  grid-template-columns: 104px minmax(0, 1fr);
  align-items: stretch;
  gap: var(--rs-space-6);
}
.candidate-interview-card__date {
  display: grid;
  place-items: center;
  align-content: center;
  min-height: 112px;
  padding: var(--rs-space-3);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-blue-050);
  color: var(--rs-blue-700);
  text-align: center;
}
.candidate-interview-card__date span {
  font-size: 12px;
  font-weight: 600;
}
.candidate-interview-card__date strong {
  color: var(--rs-text-primary);
  font-size: 28px;
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}
.candidate-interview-card__date small {
  margin-top: var(--rs-space-1);
  font-size: 13px;
  font-weight: 600;
}
.candidate-interview-card__copy {
  display: grid;
  align-content: center;
  gap: var(--rs-space-2);
}
.candidate-interview-card__title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--rs-space-4);
}
.candidate-interview-card h2,
.candidate-interview-card p {
  margin: 0;
}
.candidate-interview-card h2 {
  font-size: 18px;
}
.candidate-interview-card p {
  color: var(--rs-text-secondary);
}
.candidate-interview-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--rs-space-6);
  color: var(--rs-text-tertiary);
}
.candidate-interview-card__meta span {
  display: flex;
  align-items: center;
  gap: var(--rs-space-1);
}
</style>
