<script setup lang="ts">
import { Building2, MapPin, UsersRound, WalletCards } from 'lucide-vue-next'

import type { JobPosition } from '@/types/job'

defineProps<{
  visible: boolean
  job: JobPosition | null
  loading: boolean
  error: string
  submitting?: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  apply: [job: JobPosition]
}>()
</script>

<template>
  <el-drawer
    :model-value="visible"
    title="职位详情"
    size="440px"
    @update:model-value="emit('update:visible', $event)"
  >
    <el-skeleton v-if="loading" :rows="8" animated />
    <el-result v-else-if="error" icon="error" title="职位详情加载失败" :sub-title="error" />
    <div v-else-if="job" class="candidate-job-detail">
      <header>
        <h2>{{ job.title }}</h2>
        <p>{{ job.department }}</p>
      </header>
      <dl>
        <div>
          <dt><Building2 :size="16" :stroke-width="1.75" />所属部门</dt>
          <dd>{{ job.department }}</dd>
        </div>
        <div>
          <dt><MapPin :size="16" :stroke-width="1.75" />工作地点</dt>
          <dd>{{ job.location || '待定' }}</dd>
        </div>
        <div>
          <dt><WalletCards :size="16" :stroke-width="1.75" />薪资范围</dt>
          <dd>{{ job.salaryRange || '薪资面议' }}</dd>
        </div>
        <div>
          <dt><UsersRound :size="16" :stroke-width="1.75" />招聘人数</dt>
          <dd>{{ job.headcount }} 人</dd>
        </div>
      </dl>
      <section>
        <h3>岗位职责</h3>
        <p>{{ job.description || '暂未填写岗位职责。' }}</p>
      </section>
      <section>
        <h3>任职要求</h3>
        <p>{{ job.requirement || '暂未填写任职要求。' }}</p>
      </section>
    </div>
    <template v-if="job && !loading && !error" #footer>
      <div class="candidate-job-detail__footer">
        <span>投递前请确认默认简历内容已更新。</span>
        <el-button type="primary" :loading="submitting" @click="emit('apply', job)"
          >投递该职位</el-button
        >
      </div>
    </template>
  </el-drawer>
</template>

<style scoped lang="scss">
.candidate-job-detail {
  display: grid;
  gap: var(--rs-space-6);
}
.candidate-job-detail h2,
.candidate-job-detail h3,
.candidate-job-detail p,
.candidate-job-detail dl,
.candidate-job-detail dd {
  margin: 0;
}
.candidate-job-detail header p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}
.candidate-job-detail dl {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--rs-space-4);
  padding: var(--rs-space-4);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
}
.candidate-job-detail dt {
  display: flex;
  align-items: center;
  gap: var(--rs-space-1);
  color: var(--rs-text-tertiary);
  font-size: 12px;
}
.candidate-job-detail dd {
  margin-top: var(--rs-space-1);
  font-weight: 600;
}
.candidate-job-detail section {
  padding-top: var(--rs-space-4);
  border-top: 1px solid var(--rs-border-default);
}
.candidate-job-detail section h3 {
  font-size: 14px;
}
.candidate-job-detail section p {
  margin-top: var(--rs-space-2);
  color: var(--rs-text-secondary);
  white-space: pre-wrap;
}
.candidate-job-detail__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--rs-space-4);
  width: 100%;
}
.candidate-job-detail__footer span {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}
</style>
