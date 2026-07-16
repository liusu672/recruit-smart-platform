<script setup lang="ts">
import { Info } from 'lucide-vue-next'
import { getMyInterviewerTasks } from '@/api/interviewerWorkspace'
import { usePortalResource } from '@/composables/usePortalResource'
import { demoInterviewerTasks } from '@/config/demoCandidatePortal'
const resource = usePortalResource(getMyInterviewerTasks, demoInterviewerTasks)
</script>
<template>
  <div class="role-portal">
    <div class="portal-toolbar">
      <div>
        <h2>分配给我的面试</h2>
        <p>候选人资料仅在任务范围内使用，不提供候选人库和 HR 内部记录。</p>
      </div>
    </div>
    <div class="portal-note">
      <Info :size="17" /><span>原始面试反馈由面试官本人提交；最终筛选和录用状态仍由 HR 确认。</span>
    </div>
    <section class="portal-panel">
      <div v-if="resource.loading.value" class="portal-loading">正在加载本人任务...</div>
      <div v-else-if="resource.error.value" class="portal-error">{{ resource.error.value }}</div>
      <div v-else-if="resource.data.value.length" class="portal-list">
        <article v-for="item in resource.data.value" :key="item.id" class="portal-row">
          <div class="portal-row__primary">
            <h3>{{ item.jobTitle }} · {{ item.roundText }}</h3>
            <p>候选人：{{ item.candidateName }} · {{ item.methodText }}</p>
          </div>
          <div class="portal-row__cell">
            <strong>{{ new Date(item.interviewTime).toLocaleString('zh-CN') }}</strong
            ><span>{{ item.location || '地点待通知' }}</span>
          </div>
          <span class="rs-status-pill rs-status-pill--info">{{ item.statusText }}</span
          ><el-button :disabled="item.status !== 'SCHEDULED'">进入面试</el-button>
        </article>
      </div>
      <div v-else class="portal-empty">暂无分配给你的面试任务。</div>
    </section>
  </div>
</template>
