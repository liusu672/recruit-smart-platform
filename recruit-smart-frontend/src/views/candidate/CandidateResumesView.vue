<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Eye, FileText, MoreHorizontal, Upload } from 'lucide-vue-next'
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
import CandidateEmptyState from '@/components/candidate/CandidateEmptyState.vue'
import CandidateErrorState from '@/components/candidate/CandidateErrorState.vue'
import CandidateListItem from '@/components/candidate/CandidateListItem.vue'
import CandidatePageHeader from '@/components/candidate/CandidatePageHeader.vue'
import CandidateStatusBadge from '@/components/candidate/CandidateStatusBadge.vue'
import { usePortalResource } from '@/composables/usePortalResource'
import { demoMyResumes } from '@/config/demoCandidatePortal'
import type { ResumeSummary } from '@/types/portal'

const resource = usePortalResource(getMyResumes, demoMyResumes)
const fileInput = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const fileActionId = ref<number | null>(null)
const defaultingId = ref<number | null>(null)

function isPdfResume(resume: ResumeSummary) {
  return (
    resume.fileType?.toUpperCase() === 'PDF' || resume.resumeName.toLowerCase().endsWith('.pdf')
  )
}

function formatDate(value: string | null) {
  return value ? new Date(value).toLocaleDateString('zh-CN') : '时间未知'
}

async function chooseFile(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!/\.(pdf|doc|docx)$/i.test(file.name)) {
    ElMessage.error('仅支持 PDF、DOC 或 DOCX 文件')
    input.value = ''
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('简历文件不能超过 10MB')
    input.value = ''
    return
  }
  uploading.value = true
  try {
    if (!resource.demoMode.value) await uploadMyResume(file, file.name.replace(/\.[^.]+$/, ''))
    await resource.reload()
    ElMessage.success('简历上传成功')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '简历上传失败')
  } finally {
    uploading.value = false
    input.value = ''
  }
}

async function setDefault(id: number) {
  if (defaultingId.value !== null) return
  defaultingId.value = id
  try {
    if (!resource.demoMode.value) await setMyDefaultResume(id)
    resource.data.value = resource.data.value.map((item) => ({
      ...item,
      isDefault: item.id === id ? 1 : 0,
    }))
    ElMessage.success('默认简历已更新')
  } finally {
    defaultingId.value = null
  }
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
    await ElMessageBox.confirm(
      `确认删除“${resume.resumeName}”？删除后无法用于新的职位投递。`,
      '删除简历',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    if (!resource.demoMode.value) await deleteResume(resume.id)
    await resource.reload()
    ElMessage.success('简历已删除')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error instanceof Error ? error.message : '简历删除失败')
  }
}

function handleMoreAction(command: string, resume: ResumeSummary) {
  if (command === 'rename') void renameCurrentResume(resume)
  if (command === 'delete') void deleteCurrentResume(resume)
}
</script>

<template>
  <div class="candidate-page candidate-resumes">
    <CandidatePageHeader title="我的简历" description="管理用于职位投递的简历文件。">
      <template #actions>
        <input ref="fileInput" hidden type="file" accept=".pdf,.doc,.docx" @change="chooseFile" />
        <el-button type="primary" :loading="uploading" @click="fileInput?.click()">
          <Upload :size="16" />上传简历
        </el-button>
      </template>
    </CandidatePageHeader>

    <div class="candidate-resume-upload-note">
      <FileText :size="18" :stroke-width="1.7" />
      <span>支持 PDF、DOC、DOCX，单个文件不超过 10MB。PDF 支持在线预览。</span>
    </div>

    <div v-if="resource.loading.value" class="candidate-skeleton-list">
      <div v-for="index in 2" :key="index" class="candidate-skeleton-card">
        <el-skeleton :rows="2" animated />
      </div>
    </div>
    <CandidateErrorState
      v-else-if="resource.error.value"
      description="简历列表暂时无法加载，请稍后重试。"
      retryable
      @retry="resource.reload"
    />
    <div v-else-if="resource.data.value.length" class="candidate-list">
      <CandidateListItem v-for="item in resource.data.value" :key="item.id" interactive>
        <div class="candidate-resume-row">
          <span class="candidate-resume-row__file">{{ item.fileType?.toUpperCase() || 'CV' }}</span>
          <div class="candidate-resume-row__copy">
            <div class="candidate-resume-row__title">
              <h2>{{ item.resumeName }}</h2>
              <CandidateStatusBadge v-if="item.isDefault === 1" status="SUCCESS" label="默认简历" />
            </div>
            <p>{{ item.fileType || '文件' }}，上传于 {{ formatDate(item.createdAt) }}</p>
          </div>
          <CandidateStatusBadge
            :status="item.parseStatus || 'PENDING'"
            :label="item.parseStatusText || '等待解析'"
          />
          <div class="candidate-actions">
            <el-tooltip content="预览简历">
              <el-button
                circle
                :icon="Eye"
                :loading="fileActionId === item.id"
                aria-label="预览简历"
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
            <el-button
              v-if="item.isDefault !== 1"
              text
              type="primary"
              :loading="defaultingId === item.id"
              @click="setDefault(item.id)"
              >设为默认</el-button
            >
            <el-dropdown
              trigger="click"
              @command="(command: string) => handleMoreAction(command, item)"
            >
              <el-button circle :icon="MoreHorizontal" aria-label="更多简历操作" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="rename">修改名称</el-dropdown-item>
                  <el-dropdown-item divided command="delete">删除简历</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </CandidateListItem>
    </div>
    <CandidateEmptyState
      v-else
      :icon="FileText"
      title="还没有简历"
      description="上传第一份简历后，就可以开始投递职位。"
    >
      <template #actions>
        <el-button type="primary" :loading="uploading" @click="fileInput?.click()"
          >上传第一份简历</el-button
        >
      </template>
    </CandidateEmptyState>
  </div>
</template>

<style scoped lang="scss">
.candidate-resume-upload-note {
  display: flex;
  align-items: center;
  gap: var(--rs-space-2);
  padding: var(--rs-space-3) var(--rs-space-4);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-blue-050);
  color: var(--rs-blue-700);
}
.candidate-resume-row {
  display: grid;
  grid-template-columns: auto minmax(240px, 1fr) auto auto;
  align-items: center;
  gap: var(--rs-space-6);
}
.candidate-resume-row__file {
  display: grid;
  width: 48px;
  height: 56px;
  place-items: center;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
  color: var(--rs-blue-700);
  font-size: 12px;
  font-weight: 700;
}
.candidate-resume-row__title {
  display: flex;
  align-items: center;
  gap: var(--rs-space-2);
}
.candidate-resume-row h2,
.candidate-resume-row p {
  margin: 0;
}
.candidate-resume-row h2 {
  font-size: 16px;
}
.candidate-resume-row p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
  font-size: 13px;
}
</style>
