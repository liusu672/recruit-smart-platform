<script setup lang="ts">
import { computed } from 'vue'
import { getMyInterviewerTasks } from '@/api/interviewerWorkspace'
import { usePortalResource } from '@/composables/usePortalResource'
import { demoInterviewerTasks } from '@/config/demoCandidatePortal'
const resource = usePortalResource(getMyInterviewerTasks, demoInterviewerTasks)
const pending = computed(() => resource.data.value.filter((item) => item.status === 'SCHEDULED'))
const completed = computed(() => resource.data.value.filter((item) => item.status === 'COMPLETED'))

function formatInterviewTime(value: string | null) {
  return value ? new Date(value).toLocaleString('zh-CN') : '待预约'
}
</script>
<template>
  <div class="role-portal">
    <div class="portal-toolbar">
      <div>
        <h2>我的面试任务</h2>
        <p>只展示后端分配给当前面试官的任务，不包含招聘流程和 Offer 数据。</p>
      </div>
      <span v-if="resource.demoMode.value" class="portal-demo-note">面试官演示数据</span>
    </div>
    <section class="portal-metrics">
      <article class="portal-metric">
        <span>全部任务</span><strong>{{ resource.data.value.length }}</strong>
      </article>
      <article class="portal-metric">
        <span>待参加</span><strong>{{ pending.length }}</strong>
      </article>
      <article class="portal-metric">
        <span>已完成</span><strong>{{ completed.length }}</strong>
      </article>
      <article class="portal-metric">
        <span>需提交反馈</span><strong>{{ pending.length }}</strong>
      </article>
    </section>
    <section class="portal-panel">
      <div class="portal-panel__header">
        <h2>近期安排</h2>
        <RouterLink to="/interviewer/interviews">查看全部</RouterLink>
      </div>
      <div v-if="resource.loading.value" class="portal-loading">正在加载本人任务...</div>
      <div v-else-if="pending.length" class="portal-list">
        <article v-for="item in pending.slice(0, 3)" :key="item.id" class="portal-row">
          <div class="portal-row__primary">
            <h3>{{ item.jobTitle }} · {{ item.roundText }}</h3>
            <p>候选人：{{ item.candidateName }}</p>
          </div>
          <div class="portal-row__cell">
            <strong>{{ formatInterviewTime(item.interviewTime) }}</strong
            ><span>{{ item.methodText }}</span>
          </div>
          <span class="rs-status-pill rs-status-pill--info">{{ item.statusText }}</span
          ><span />
        </article>
      </div>
      <div v-else class="portal-empty">暂无待参加面试。</div>
    </section>
  </div>
</template>
