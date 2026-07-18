<script setup lang="ts">
import {
  ArrowRight,
  BriefcaseBusiness,
  CalendarClock,
  FileCheck2,
  FileText,
  Search,
} from 'lucide-vue-next'
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
import CandidateEmptyState from '@/components/candidate/CandidateEmptyState.vue'
import CandidateErrorState from '@/components/candidate/CandidateErrorState.vue'
import CandidateListItem from '@/components/candidate/CandidateListItem.vue'
import CandidatePageHeader from '@/components/candidate/CandidatePageHeader.vue'
import CandidateStatusBadge from '@/components/candidate/CandidateStatusBadge.vue'
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
    return {
      profile,
      resumes,
      jobs: jobs.items,
      jobTotal: jobs.total,
      applications: applications.items,
      applicationTotal: applications.total,
      interviews: interviews.items,
      interviewTotal: interviews.total,
      offers: offers.items,
      offerTotal: offers.total,
      onboardings,
    }
  },
  {
    profile: demoMyProfile,
    resumes: demoMyResumes,
    jobs: demoOpenJobs,
    jobTotal: demoOpenJobs.length,
    applications: demoMyApplications,
    applicationTotal: demoMyApplications.length,
    interviews: demoMyInterviews,
    interviewTotal: demoMyInterviews.length,
    offers: demoMyOffers,
    offerTotal: demoMyOffers.length,
    onboardings: demoMyOnboardings,
  },
)

const scheduledInterviews = computed(() =>
  resource.data.value.interviews.filter((item) => item.status === 'SCHEDULED'),
)
const pendingOffers = computed(() =>
  resource.data.value.offers.filter((item) => item.status === 'SENT'),
)
const pendingOnboardings = computed(() =>
  resource.data.value.onboardings.filter((item) =>
    ['PENDING', 'REJECTED'].includes(item.materialStatus),
  ),
)
const recentApplications = computed(() => resource.data.value.applications.slice(0, 3))

const primaryAction = computed(() => {
  const offer = pendingOffers.value[0]
  if (offer) {
    return {
      icon: FileCheck2,
      eyebrow: '需要你的确认',
      title: `${offer.jobTitle} Offer 等待处理`,
      description: `${offer.department}，工作地点 ${offer.workLocation}`,
      route: '/candidate/offers',
      action: '查看 Offer',
      status: offer.status,
      statusText: offer.statusText,
    }
  }
  const onboarding = pendingOnboardings.value[0]
  if (onboarding) {
    return {
      icon: FileText,
      eyebrow: '入职事项',
      title: `${onboarding.jobTitle} 入职资料待处理`,
      description: `${onboarding.currentStep}，预计 ${onboarding.entryDate} 入职`,
      route: '/candidate/onboarding',
      action: '查看入职资料',
      status: onboarding.materialStatus,
      statusText: onboarding.materialStatusText,
    }
  }
  const interview = scheduledInterviews.value[0]
  if (interview) {
    return {
      icon: CalendarClock,
      eyebrow: '面试安排',
      title: `${interview.jobTitle} ${interview.roundText}`,
      description: `${formatDateTime(interview.interviewTime)}，${interview.methodText}，${interview.location || '地点待通知'}`,
      route: '/candidate/interviews',
      action: '查看面试安排',
      status: interview.status,
      statusText: interview.statusText,
    }
  }
  if (!resource.data.value.resumes.length) {
    return {
      icon: FileText,
      eyebrow: '开始求职',
      title: '上传一份简历',
      description: '准备好简历后，就可以选择感兴趣的职位进行投递。',
      route: '/candidate/resumes',
      action: '上传简历',
      status: 'PENDING',
      statusText: '待完成',
    }
  }
  return {
    icon: Search,
    eyebrow: '继续探索',
    title: '看看新的招聘职位',
    description: `目前有 ${resource.data.value.jobTotal} 个开放职位，可以继续寻找合适机会。`,
    route: '/candidate/jobs',
    action: '浏览招聘职位',
    status: 'OPEN',
    statusText: '可开始',
  }
})

function formatDate(value: string) {
  return new Date(value).toLocaleDateString('zh-CN')
}

function formatDateTime(value: string | null) {
  return value ? new Date(value).toLocaleString('zh-CN') : '时间待通知'
}
</script>

<template>
  <div class="candidate-page candidate-home">
    <CandidatePageHeader
      :title="`欢迎回来，${resource.data.value.profile.name || '候选人'}`"
      description="集中查看需要你处理的求职事项和最近投递。"
    >
      <template v-if="resource.demoMode.value" #actions>
        <span class="candidate-demo-note">候选人演示数据</span>
      </template>
    </CandidatePageHeader>

    <div v-if="resource.loading.value" class="candidate-skeleton-list">
      <div class="candidate-skeleton-card"><el-skeleton :rows="4" animated /></div>
      <div class="candidate-skeleton-card"><el-skeleton :rows="3" animated /></div>
    </div>
    <CandidateErrorState
      v-else-if="resource.error.value"
      description="求职概览暂时无法加载，请稍后重试。"
      retryable
      @retry="resource.reload"
    />
    <template v-else>
      <section class="candidate-home-action" aria-labelledby="primary-action-title">
        <div class="candidate-home-action__icon">
          <component :is="primaryAction.icon" :size="28" :stroke-width="1.65" />
        </div>
        <div class="candidate-home-action__copy">
          <span>{{ primaryAction.eyebrow }}</span>
          <h2 id="primary-action-title">{{ primaryAction.title }}</h2>
          <p>{{ primaryAction.description }}</p>
        </div>
        <div class="candidate-home-action__status">
          <CandidateStatusBadge :status="primaryAction.status" :label="primaryAction.statusText" />
          <RouterLink :to="primaryAction.route">
            <el-button type="primary" size="large">
              {{ primaryAction.action }} <ArrowRight :size="16" />
            </el-button>
          </RouterLink>
        </div>
      </section>

      <section class="candidate-home-summary" aria-label="求职摘要">
        <RouterLink to="/candidate/applications" class="candidate-home-summary__item">
          <BriefcaseBusiness :size="20" :stroke-width="1.7" />
          <span
            ><strong>{{ resource.data.value.applicationTotal }}</strong
            >我的投递</span
          >
        </RouterLink>
        <RouterLink to="/candidate/resumes" class="candidate-home-summary__item">
          <FileText :size="20" :stroke-width="1.7" />
          <span
            ><strong>{{ resource.data.value.resumes.length }}</strong
            >可用简历</span
          >
        </RouterLink>
        <RouterLink to="/candidate/interviews" class="candidate-home-summary__item">
          <CalendarClock :size="20" :stroke-width="1.7" />
          <span
            ><strong>{{ scheduledInterviews.length }}</strong
            >面试安排</span
          >
        </RouterLink>
        <RouterLink to="/candidate/offers" class="candidate-home-summary__item">
          <FileCheck2 :size="20" :stroke-width="1.7" />
          <span
            ><strong>{{ pendingOffers.length }}</strong
            >待处理 Offer</span
          >
        </RouterLink>
      </section>

      <section class="candidate-home-recent">
        <div class="candidate-home-recent__header">
          <div>
            <h2>最近投递</h2>
            <p>快速了解最近提交的职位申请。</p>
          </div>
          <RouterLink v-if="recentApplications.length" to="/candidate/applications"
            >查看全部</RouterLink
          >
        </div>
        <div v-if="recentApplications.length" class="candidate-list">
          <CandidateListItem v-for="item in recentApplications" :key="item.id" interactive>
            <div class="candidate-home-application">
              <div>
                <h3>{{ item.jobTitle }}</h3>
                <p>{{ item.department }}，{{ formatDate(item.appliedAt) }} 投递</p>
              </div>
              <CandidateStatusBadge :status="item.status" :label="item.statusText" />
              <RouterLink to="/candidate/applications">
                <el-button>查看进度</el-button>
              </RouterLink>
            </div>
          </CandidateListItem>
        </div>
        <CandidateEmptyState
          v-else
          :icon="BriefcaseBusiness"
          title="还没有投递记录"
          description="浏览开放职位，找到合适的机会后开始投递。"
        >
          <template #actions>
            <RouterLink to="/candidate/jobs"
              ><el-button type="primary">浏览职位</el-button></RouterLink
            >
          </template>
        </CandidateEmptyState>
      </section>
    </template>
  </div>
</template>

<style scoped lang="scss">
.candidate-home-action {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: var(--rs-space-6);
  min-height: 184px;
  padding: var(--rs-space-8);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-md);
  background: var(--rs-surface-primary);
}
.candidate-home-action__icon {
  display: grid;
  width: 64px;
  height: 64px;
  place-items: center;
  border-radius: var(--rs-radius-md);
  background: var(--rs-blue-050);
  color: var(--rs-blue-700);
}
.candidate-home-action__copy span {
  color: var(--rs-blue-700);
  font-size: 13px;
  font-weight: 600;
}
.candidate-home-action h2,
.candidate-home-action p {
  margin: 0;
}
.candidate-home-action h2 {
  margin-top: var(--rs-space-2);
  font-size: 22px;
}
.candidate-home-action p {
  margin-top: var(--rs-space-2);
  color: var(--rs-text-secondary);
}
.candidate-home-action__status {
  display: grid;
  justify-items: end;
  gap: var(--rs-space-4);
}
.candidate-home-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
.candidate-home-summary__item {
  display: flex;
  align-items: center;
  gap: var(--rs-space-3);
  min-height: 88px;
  padding: var(--rs-space-4) var(--rs-space-6);
  color: var(--rs-text-secondary);
}
.candidate-home-summary__item + .candidate-home-summary__item {
  border-left: 1px solid var(--rs-border-default);
}
.candidate-home-summary__item:hover {
  background: var(--rs-surface-subtle);
  color: var(--rs-text-primary);
}
.candidate-home-summary__item svg {
  color: var(--rs-blue-700);
}
.candidate-home-summary__item span {
  display: grid;
  gap: 2px;
}
.candidate-home-summary__item strong {
  color: var(--rs-text-primary);
  font-size: 20px;
  font-variant-numeric: tabular-nums;
}
.candidate-home-recent {
  display: grid;
  gap: var(--rs-space-4);
}
.candidate-home-recent__header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--rs-space-4);
}
.candidate-home-recent__header h2,
.candidate-home-recent__header p {
  margin: 0;
}
.candidate-home-recent__header h2 {
  font-size: 18px;
}
.candidate-home-recent__header p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}
.candidate-home-application {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  align-items: center;
  gap: var(--rs-space-6);
}
.candidate-home-application h3,
.candidate-home-application p {
  margin: 0;
}
.candidate-home-application h3 {
  font-size: 16px;
}
.candidate-home-application p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}
</style>
