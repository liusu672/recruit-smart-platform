<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyOnboardings, submitMyOnboardingMaterials } from '@/api/candidatePortal'
import { usePortalResource } from '@/composables/usePortalResource'
import { demoMyOnboardings } from '@/config/demoCandidatePortal'
const resource = usePortalResource(getMyOnboardings, demoMyOnboardings)
async function submit(id: number) {
  await ElMessageBox.confirm('确认材料已准备完整并提交 HR 审核？', '提交入职材料', { type: 'info' })
  if (!resource.demoMode.value) await submitMyOnboardingMaterials(id)
  resource.data.value = resource.data.value.map((item) =>
    item.id === id ? { ...item, materialStatus: 'REVIEWING', materialStatusText: '审核中' } : item,
  )
  ElMessage.success('入职材料已提交')
}
</script>
<template>
  <div class="role-portal">
    <div class="portal-toolbar">
      <div>
        <h2>入职资料</h2>
        <p>你只能维护本人入职材料，审核和入职确认由 HR 完成。</p>
      </div>
    </div>
    <section class="portal-panel">
      <div v-if="resource.loading.value" class="portal-loading">正在加载入职流程...</div>
      <div v-else-if="resource.error.value" class="portal-error">{{ resource.error.value }}</div>
      <div v-else-if="resource.data.value.length" class="portal-list">
        <article v-for="item in resource.data.value" :key="item.id" class="portal-row">
          <div class="portal-row__primary">
            <h3>{{ item.jobTitle }}</h3>
            <p>{{ item.department }} · 预计 {{ item.entryDate }} 入职</p>
          </div>
          <div class="portal-row__cell">
            <strong>{{ item.currentStep }}</strong
            ><span>当前步骤</span>
          </div>
          <span class="rs-status-pill rs-status-pill--info">{{ item.materialStatusText }}</span
          ><el-button
            v-if="['PENDING', 'REJECTED'].includes(item.materialStatus)"
            type="primary"
            @click="submit(item.id)"
            >提交材料</el-button
          ><span v-else />
        </article>
      </div>
      <div v-else class="portal-empty">接受 Offer 后，入职流程会显示在这里。</div>
    </section>
  </div>
</template>
