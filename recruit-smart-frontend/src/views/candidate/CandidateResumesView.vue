<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { FileText, Upload } from 'lucide-vue-next'
import { ref } from 'vue'

import { getMyResumes, setMyDefaultResume, uploadMyResume } from '@/api/candidatePortal'
import { usePortalResource } from '@/composables/usePortalResource'
import { demoMyResumes } from '@/config/demoCandidatePortal'

const resource = usePortalResource(getMyResumes, demoMyResumes)
const fileInput = ref<HTMLInputElement | null>(null)
const uploading = ref(false)

async function chooseFile(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!/\.(pdf|doc|docx)$/i.test(file.name)) {
    ElMessage.error('仅支持 PDF、DOC 或 DOCX 文件')
    return
  }
  if (resource.demoMode.value) {
    ElMessage.success('演示模式：简历已上传')
    return
  }
  uploading.value = true
  try {
    await uploadMyResume(file, file.name.replace(/\.[^.]+$/, ''))
    await resource.reload()
    ElMessage.success('简历上传成功')
  } finally {
    uploading.value = false
    input.value = ''
  }
}

async function setDefault(id: number) {
  if (!resource.demoMode.value) await setMyDefaultResume(id)
  resource.data.value = resource.data.value.map((item) => ({
    ...item,
    isDefault: item.id === id ? 1 : 0,
  }))
  ElMessage.success('默认简历已更新')
}
</script>

<template>
  <div class="role-portal">
    <div class="portal-toolbar">
      <div>
        <h2>我的简历</h2>
        <p>简历仅用于你本人的职位投递，并由后端校验文件归属。</p>
      </div>
      <div>
        <input
          ref="fileInput"
          hidden
          type="file"
          accept=".pdf,.doc,.docx"
          @change="chooseFile"
        /><el-button type="primary" :loading="uploading" @click="fileInput?.click()"
          ><Upload :size="15" /> 上传简历</el-button
        >
      </div>
    </div>
    <section class="portal-panel">
      <div v-if="resource.loading.value" class="portal-loading">正在加载简历...</div>
      <div v-else-if="resource.error.value" class="portal-error">{{ resource.error.value }}</div>
      <div v-else-if="resource.data.value.length" class="portal-list">
        <article v-for="item in resource.data.value" :key="item.id" class="portal-row">
          <div class="portal-row__primary">
            <h3><FileText :size="16" /> {{ item.resumeName }}</h3>
            <p>
              {{ item.fileType || '文件' }} ·
              {{
                item.createdAt ? new Date(item.createdAt).toLocaleDateString('zh-CN') : '时间未知'
              }}
            </p>
          </div>
          <div class="portal-row__cell">
            <strong>{{ item.parseStatusText || '等待解析' }}</strong
            ><span>解析状态</span>
          </div>
          <span v-if="item.isDefault === 1" class="rs-status-pill rs-status-pill--success"
            >默认简历</span
          ><span v-else /><el-button
            v-if="item.isDefault !== 1"
            text
            type="primary"
            @click="setDefault(item.id)"
            >设为默认</el-button
          ><span v-else />
        </article>
      </div>
      <div v-else class="portal-empty">尚未上传简历，上传后即可投递职位。</div>
    </section>
  </div>
</template>
