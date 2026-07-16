<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

import { applyToJob, getMyResumes, getOpenJobs } from '@/api/candidatePortal'
import { usePortalResource } from '@/composables/usePortalResource'
import { demoMyResumes, demoOpenJobs } from '@/config/demoCandidatePortal'

const router = useRouter()
const keyword = ref('')
const jobs = usePortalResource(getOpenJobs, demoOpenJobs)
const resumes = usePortalResource(getMyResumes, demoMyResumes)
const filteredJobs = computed(() =>
  jobs.data.value.filter((item) =>
    `${item.title}${item.department}${item.location ?? ''}`
      .toLowerCase()
      .includes(keyword.value.trim().toLowerCase()),
  ),
)

async function apply(jobId: number, title: string) {
  const resume = resumes.data.value.find((item) => item.isDefault === 1) ?? resumes.data.value[0]
  if (!resume) {
    ElMessage.warning('请先上传简历再投递')
    await router.push('/candidate/resumes')
    return
  }
  await ElMessageBox.confirm(`确认使用“${resume.resumeName}”投递“${title}”？`, '确认投递', {
    confirmButtonText: '确认投递',
    cancelButtonText: '取消',
    type: 'info',
  })
  if (jobs.demoMode.value) {
    ElMessage.success('演示模式：投递已记录')
    return
  }
  await applyToJob(jobId, resume.id)
  ElMessage.success('投递成功')
}
</script>

<template>
  <div class="role-portal">
    <div class="portal-toolbar">
      <div>
        <h2>招聘中的职位</h2>
        <p>职位列表只包含当前对候选人开放的岗位。</p>
      </div>
      <el-input
        v-model="keyword"
        clearable
        placeholder="搜索职位、部门或地点"
        style="width: 320px"
      />
    </div>
    <section class="portal-panel">
      <div v-if="jobs.loading.value" class="portal-loading">正在加载招聘职位...</div>
      <div v-else-if="jobs.error.value" class="portal-error">{{ jobs.error.value }}</div>
      <div v-else-if="filteredJobs.length" class="portal-list">
        <article v-for="job in filteredJobs" :key="job.id" class="portal-row portal-row--job">
          <div class="portal-row__primary">
            <h3>{{ job.title }}</h3>
            <p>{{ job.department }} · {{ job.location || '地点待定' }}</p>
          </div>
          <div class="portal-row__cell">
            <strong>{{ job.salaryRange || '薪资面议' }}</strong
            ><span>{{ job.jobType || '全职' }}</span>
          </div>
          <div class="portal-row__cell">
            <strong>{{ job.experienceRequirement || '经验不限' }}</strong
            ><span>{{ job.educationRequirement || '学历不限' }}</span>
          </div>
          <el-button type="primary" @click="apply(job.id, job.title)">投递职位</el-button>
        </article>
      </div>
      <div v-else class="portal-empty">没有符合条件的招聘职位。</div>
    </section>
  </div>
</template>
