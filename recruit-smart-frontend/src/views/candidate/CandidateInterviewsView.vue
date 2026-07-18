<script setup lang="ts">
import { getMyInterviews } from '@/api/candidatePortal'
import { usePortalPagedResource } from '@/composables/usePortalPagedResource'
import { demoMyInterviews } from '@/config/demoCandidatePortal'
const resource = usePortalPagedResource(getMyInterviews, demoMyInterviews)

function formatInterviewTime(value: string | null) {
  return value ? new Date(value).toLocaleString('zh-CN') : '待预约'
}
</script>
<template>
  <div class="role-portal">
    <div class="portal-toolbar">
      <div>
        <h2>我的面试安排</h2>
        <p>仅展示与你本人投递关联的时间、方式和地点。</p>
      </div>
      <span v-if="resource.demoMode.value" class="portal-demo-note">候选人演示数据</span>
    </div>
    <section class="portal-panel">
      <div v-if="resource.loading.value" class="portal-loading">正在加载面试安排...</div>
      <div v-else-if="resource.error.value" class="portal-error">{{ resource.error.value }}</div>
      <div v-else-if="resource.data.value.items.length" class="portal-list">
        <article v-for="item in resource.data.value.items" :key="item.id" class="portal-row">
          <div class="portal-row__primary">
            <h3>{{ item.jobTitle }} · {{ item.roundText }}</h3>
            <p>{{ item.methodText }} · {{ item.location || '地点待通知' }}</p>
          </div>
          <div class="portal-row__cell">
            <strong>{{ formatInterviewTime(item.interviewTime) }}</strong
            ><span>面试时间</span>
          </div>
          <span class="rs-status-pill rs-status-pill--info">{{ item.statusText }}</span
          ><span />
        </article>
      </div>
      <div v-else class="portal-empty">暂无面试安排。</div>
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
  </div>
</template>
