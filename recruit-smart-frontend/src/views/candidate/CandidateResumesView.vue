<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Eye, FileText, Pencil, Trash2, Upload } from 'lucide-vue-next'
import { ref } from 'vue'

import { getMyResumes, setMyDefaultResume, uploadMyResume } from '@/api/candidatePortal'
import {
  deleteResume,
  getResumeDownloadFile,
  getResumePreviewFile,
  openBlobPreview,
  renameResume,
  saveBlobAsFile,
} from '@/api/resumes'
import { usePortalResource } from '@/composables/usePortalResource'
import { demoMyResumes } from '@/config/demoCandidatePortal'
import type { ResumeSummary } from '@/types/portal'

const resource = usePortalResource(getMyResumes, demoMyResumes)
const fileInput = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const fileActionId = ref<number | null>(null)

function isPdfResume(resume: ResumeSummary) {
  return resume.fileType?.toUpperCase() === 'PDF' || resume.resumeName.toLowerCase().endsWith('.pdf')
}

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

async function previewResume(resume: ResumeSummary) {
  if (!isPdfResume(resume)) {
    ElMessage.warning('仅 PDF 简历支持在线预览，请下载后查看')
    return
  }
  if (resource.demoMode.value) {
    ElMessage.info('演示模式没有真实 PDF 文件')
    return
  }
  fileActionId.value = resume.id
  try {
    openBlobPreview(await getResumePreviewFile(resume.id))
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '简历预览失败')
  } finally {
    fileActionId.value = null
  }
}

async function downloadResume(resume: ResumeSummary) {
  if (resource.demoMode.value) {
    ElMessage.info('演示模式没有真实简历文件')
    return
  }
  fileActionId.value = resume.id
  try {
    saveBlobAsFile(await getResumeDownloadFile(resume.id))
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '简历下载失败')
  } finally {
    fileActionId.value = null
  }
}

async function renameCurrentResume(resume: ResumeSummary) {
  try {
    const result = await ElMessageBox.prompt('请输入新的简历名称', '修改简历名称', {
      confirmButtonText: '保存',
      cancelButtonText: '取消',
      inputValue: resume.resumeName,
      inputValidator: (value) => value.trim().length > 0 || '简历名称不能为空',
    })
    const resumeName = result.value.trim()
    if (!resource.demoMode.value) await renameResume(resume.id, resumeName)
    resource.data.value = resource.data.value.map((item) =>
      item.id === resume.id ? { ...item, resumeName } : item,
    )
    ElMessage.success('简历名称已更新')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error instanceof Error ? error.message : '简历改名失败')
  }
}

async function deleteCurrentResume(resume: ResumeSummary) {
  try {
    await ElMessageBox.confirm(`确认删除“${resume.resumeName}”？删除后无法用于新的职位投递。`, '删除简历', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    if (!resource.demoMode.value) await deleteResume(resume.id)
    resource.data.value = resource.data.value.filter((item) => item.id !== resume.id)
    ElMessage.success('简历已删除')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error instanceof Error ? error.message : '简历删除失败')
  }
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
          ><span v-else />
          <div class="portal-row__actions">
            <el-tooltip content="预览 PDF">
              <el-button
                circle
                :icon="Eye"
                :loading="fileActionId === item.id"
                aria-label="预览 PDF 简历"
                @click="previewResume(item)"
              />
            </el-tooltip>
            <el-tooltip content="下载简历">
              <el-button
                circle
                :icon="Download"
                :loading="fileActionId === item.id"
                aria-label="下载简历"
                @click="downloadResume(item)"
              />
            </el-tooltip>
            <el-tooltip content="修改名称">
              <el-button
                circle
                :icon="Pencil"
                aria-label="修改简历名称"
                @click="renameCurrentResume(item)"
              />
            </el-tooltip>
            <el-tooltip content="删除简历">
              <el-button
                circle
                type="danger"
                plain
                :icon="Trash2"
                aria-label="删除简历"
                @click="deleteCurrentResume(item)"
              />
            </el-tooltip>
            <el-button v-if="item.isDefault !== 1" text type="primary" @click="setDefault(item.id)"
              >设为默认</el-button
            >
          </div>
        </article>
      </div>
      <div v-else class="portal-empty">尚未上传简历，上传后即可投递职位。</div>
    </section>
  </div>
</template>
