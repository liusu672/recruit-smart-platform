<script setup lang="ts">
import { ArrowRight, CalendarClock } from 'lucide-vue-next'
import { computed } from 'vue'

import {
  getMyApplications,
  getMyCandidateProfile,
  getMyInterviews,
  getMyOffers,
  getMyOnboardings,
  getMyResumes,
  getOpenJobs,
} from '@/api/candidatePortal'
import { usePortalResource } from '@/composables/usePortalResource'
import {
  demoMyApplications,
  demoMyInterviews,
  demoMyOffers,
  demoMyOnboardings,
  demoMyProfile,
  demoMyResumes,
  demoOpenJobs,
} from '@/config/demoCandidatePortal'

const resource = usePortalResource(
  async () => {
    const [profile, resumes, jobs, applications, interviews, offers, onboardings] =
      await Promise.all([
        getMyCandidateProfile(),
        getMyResumes(),
        getOpenJobs(),
        getMyApplications(),
        getMyInterviews(),
        getMyOffers(),
        getMyOnboardings(),
      ])
    return { profile, resumes, jobs, applications, interviews, offers, onboardings }
  },
  {
    profile: demoMyProfile,
    resumes: demoMyResumes,
    jobs: demoOpenJobs,
    applications: demoMyApplications,
    interviews: demoMyInterviews,
    offers: demoMyOffers,
    onboardings: demoMyOnboardings,
  },
)

const nextInterview = computed(() =>
  resource.data.value.interviews.find((item) => item.status === 'SCHEDULED'),
)

function formatInterviewTime(value: string | null) {
  return value ? new Date(value).toLocaleString('zh-CN') : '待预约'
}
</script>

<template>
  <div class="role-portal">
    <div class="portal-toolbar">
      <div>
        <h2>{{ resource.data.value.profile.name }}，查看你的求职进度</h2>
        <p>这里只展示与你当前账号关联的职位、投递、面试和 Offer。</p>
      </div>
      <span v-if="resource.demoMode.value" class="portal-demo-note">候选人演示数据</span>
    </div>
    <div v-if="resource.loading.value" class="portal-panel portal-loading">
      正在加载本人求职数据...
    </div>
    <div v-else-if="resource.error.value" class="portal-panel portal-error">
      {{ resource.error.value }}
    </div>
    <template v-else>
      <section class="portal-metrics" aria-label="我的求职概览">
        <article class="portal-metric">
          <span>可投职位</span><strong>{{ resource.data.value.jobs.length }}</strong>
        </article>
        <article class="portal-metric">
          <span>我的投递</span><strong>{{ resource.data.value.applications.length }}</strong>
        </article>
        <article class="portal-metric">
          <span>待参加面试</span
          ><strong>{{
            resource.data.value.interviews.filter((item) => item.status === 'SCHEDULED').length
          }}</strong>
        </article>
        <article class="portal-metric">
          <span>待确认 Offer</span
          ><strong>{{
            resource.data.value.offers.filter((item) => item.status === 'SENT').length
          }}</strong>
        </article>
      </section>
      <section class="portal-panel">
        <div class="portal-panel__header"><h2>下一步</h2></div>
        <div v-if="nextInterview" class="portal-row">
          <div class="portal-row__primary">
            <h3>{{ nextInterview.jobTitle }} · {{ nextInterview.roundText }}</h3>
            <p>{{ nextInterview.methodText }} · {{ nextInterview.location || '地点待通知' }}</p>
          </div>
          <div class="portal-row__cell">
            <strong>{{ formatInterviewTime(nextInterview.interviewTime) }}</strong
            ><span>面试时间</span>
          </div>
          <span class="rs-status-pill rs-status-pill--info">{{ nextInterview.statusText }}</span>
          <RouterLink to="/candidate/interviews"
            ><el-button>查看安排 <ArrowRight :size="14" /></el-button
          ></RouterLink>
        </div>
        <div v-else class="portal-empty">
          <CalendarClock :size="28" :stroke-width="1.6" /><span
            >暂无待参加面试，可继续浏览招聘职位。</span
          >
        </div>
      </section>
    </template>
  </div>
</template>
