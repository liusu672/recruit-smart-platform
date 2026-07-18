<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { CircleCheck, ExternalLink, Info } from 'lucide-vue-next'
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { onBeforeRouteLeave } from 'vue-router'

import { getMyCandidateProfile, updateMyCandidateProfile } from '@/api/candidatePortal'
import CandidateErrorState from '@/components/candidate/CandidateErrorState.vue'
import CandidateFormFooter from '@/components/candidate/CandidateFormFooter.vue'
import CandidatePageHeader from '@/components/candidate/CandidatePageHeader.vue'
import { usePortalResource } from '@/composables/usePortalResource'
import { demoMyProfile } from '@/config/demoCandidatePortal'
import type { CandidateProfileUpdate } from '@/types/portal'

const resource = usePortalResource(getMyCandidateProfile, demoMyProfile)
const form = reactive<CandidateProfileUpdate>({
  gender: '',
  age: null,
  education: '',
  school: '',
  major: '',
  yearsOfExperience: 0,
})
const initialForm = ref<CandidateProfileUpdate>({ ...form })
const saving = ref(false)
const lastSavedAt = ref('')
const dirty = computed(() => JSON.stringify(form) !== JSON.stringify(initialForm.value))

watch(
  () => resource.data.value,
  (profile) => {
    const next = {
      gender: profile.gender ?? '',
      age: profile.age,
      education: profile.education ?? '',
      school: profile.school ?? '',
      major: profile.major ?? '',
      yearsOfExperience: profile.yearsOfExperience,
    }
    Object.assign(form, next)
    initialForm.value = { ...next }
  },
  { immediate: true },
)

function cancelChanges() {
  Object.assign(form, initialForm.value)
}

async function save() {
  if (!dirty.value || saving.value) return
  saving.value = true
  try {
    const payload = { ...form }
    if (!resource.demoMode.value) await updateMyCandidateProfile(payload)
    resource.data.value = { ...resource.data.value, ...payload }
    initialForm.value = payload
    lastSavedAt.value = new Date().toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
    })
    ElMessage.success('个人求职资料已更新')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '求职资料保存失败')
  } finally {
    saving.value = false
  }
}

function beforeUnload(event: BeforeUnloadEvent) {
  if (!dirty.value) return
  event.preventDefault()
  event.returnValue = ''
}

onMounted(() => window.addEventListener('beforeunload', beforeUnload))
onBeforeUnmount(() => window.removeEventListener('beforeunload', beforeUnload))
onBeforeRouteLeave(() => !dirty.value || window.confirm('你有未保存的更改，确认离开当前页面？'))
</script>

<template>
  <div class="candidate-page candidate-profile">
    <CandidatePageHeader title="个人中心" description="维护用于求职流程的个人、教育和经历信息。" />

    <div class="candidate-profile-layout">
      <div class="candidate-profile-main">
        <div v-if="resource.loading.value" class="candidate-skeleton-card">
          <el-skeleton :rows="8" animated />
        </div>
        <CandidateErrorState
          v-else-if="resource.error.value"
          description="个人资料暂时无法加载，请稍后重试。"
          retryable
          @retry="resource.reload"
        />
        <el-form v-else label-position="top" class="candidate-profile-form" @submit.prevent="save">
          <section class="candidate-form-section">
            <div class="candidate-form-section__heading">
              <h2>基本信息</h2>
              <p>姓名和联系方式来自你的登录账户。</p>
            </div>
            <div class="candidate-form-grid">
              <el-form-item label="姓名"
                ><el-input :model-value="resource.data.value.name" disabled
              /></el-form-item>
              <el-form-item label="联系邮箱"
                ><el-input :model-value="resource.data.value.email ?? ''" disabled
              /></el-form-item>
              <el-form-item label="性别">
                <el-select v-model="form.gender" clearable placeholder="请选择">
                  <el-option label="男" value="男" /><el-option label="女" value="女" /><el-option
                    label="其他"
                    value="其他"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="年龄"
                ><el-input-number v-model="form.age" :min="16" :max="80"
              /></el-form-item>
            </div>
          </section>

          <div class="candidate-profile-form__secondary">
            <section class="candidate-form-section">
              <div class="candidate-form-section__heading">
                <h2>教育背景</h2>
                <p>填写与你求职相关的最高学历信息。</p>
              </div>
              <div class="candidate-form-grid">
                <el-form-item label="最高学历"><el-input v-model="form.education" /></el-form-item>
                <el-form-item label="毕业学校"><el-input v-model="form.school" /></el-form-item>
                <el-form-item label="专业"><el-input v-model="form.major" /></el-form-item>
              </div>
            </section>

            <section class="candidate-form-section">
              <div class="candidate-form-section__heading">
                <h2>求职经历</h2>
                <p>工作年限用于帮助招聘方了解你的经验背景。</p>
              </div>
              <div class="candidate-form-grid candidate-form-grid--single">
                <el-form-item label="工作年限"
                  ><el-input-number v-model="form.yearsOfExperience" :min="0"
                /></el-form-item>
              </div>
            </section>
          </div>

          <CandidateFormFooter
            :dirty="dirty"
            :saving="saving"
            :last-saved-at="lastSavedAt"
            save-label="保存求职资料"
            @cancel="cancelChanges"
            @save="save"
          />
        </el-form>
      </div>

      <aside class="candidate-profile-aside" aria-label="资料填写提示">
        <section class="candidate-profile-guide">
          <header>
            <span class="candidate-profile-guide__icon"
              ><Info :size="18" :stroke-width="1.75"
            /></span>
            <div>
              <h2>填写提示</h2>
              <p>保持资料真实、完整，方便招聘方快速了解你。</p>
            </div>
          </header>
          <ul>
            <li><CircleCheck :size="16" /><span>学历、学校和专业应与上传简历保持一致。</span></li>
            <li><CircleCheck :size="16" /><span>工作年限请按实际累计的工作经验填写。</span></li>
            <li><CircleCheck :size="16" /><span>保存后的内容会用于后续职位投递。</span></li>
          </ul>
          <div class="candidate-profile-guide__account">
            <strong>需要修改姓名或邮箱？</strong>
            <p>这些信息由登录账户统一管理。</p>
            <RouterLink to="/candidate/settings">
              前往账户与安全<ExternalLink :size="14" :stroke-width="1.75" />
            </RouterLink>
          </div>
        </section>
      </aside>
    </div>
  </div>
</template>

<style scoped lang="scss">
.candidate-profile-layout {
  display: grid;
  grid-template-columns: minmax(0, var(--rs-candidate-form-width)) minmax(248px, 1fr);
  align-items: start;
  gap: var(--rs-space-6);
}
.candidate-profile-main {
  min-width: 0;
}
.candidate-profile-form {
  display: grid;
  gap: var(--rs-space-3);
}
.candidate-form-section {
  padding: var(--rs-space-4);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
.candidate-form-section__heading {
  display: flex;
  align-items: baseline;
  gap: var(--rs-space-2);
  margin-bottom: var(--rs-space-3);
}
.candidate-form-section__heading h2,
.candidate-form-section__heading p {
  margin: 0;
}
.candidate-form-section__heading h2 {
  font-size: 18px;
}
.candidate-form-section__heading p {
  color: var(--rs-text-secondary);
  font-size: 13px;
}
.candidate-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 var(--rs-space-4);
}
.candidate-form-grid--single {
  grid-template-columns: 1fr;
}
.candidate-profile-form__secondary {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: var(--rs-space-3);
}
.candidate-profile-form__secondary .candidate-form-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}
.candidate-form-grid :deep(.el-input-number),
.candidate-form-grid :deep(.el-select) {
  width: 100%;
}
.candidate-form-grid :deep(.el-form-item) {
  margin-bottom: 10px;
}
.candidate-profile-aside {
  position: sticky;
  top: 0;
}
.candidate-profile-guide {
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
.candidate-profile-guide > header {
  display: flex;
  align-items: flex-start;
  gap: var(--rs-space-3);
  padding: var(--rs-space-4);
  background: var(--rs-blue-050);
}
.candidate-profile-guide__icon {
  display: grid;
  flex: 0 0 36px;
  width: 36px;
  height: 36px;
  place-items: center;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
  color: var(--rs-blue-700);
}
.candidate-profile-guide h2,
.candidate-profile-guide p {
  margin: 0;
}
.candidate-profile-guide h2 {
  font-size: 16px;
}
.candidate-profile-guide header p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
  font-size: 13px;
}
.candidate-profile-guide ul {
  display: grid;
  gap: var(--rs-space-3);
  margin: 0;
  padding: var(--rs-space-4);
  list-style: none;
}
.candidate-profile-guide li {
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr);
  align-items: start;
  gap: var(--rs-space-2);
  color: var(--rs-text-secondary);
  line-height: 1.6;
}
.candidate-profile-guide li svg {
  margin-top: 3px;
  color: var(--rs-success-700);
}
.candidate-profile-guide__account {
  padding: var(--rs-space-3) var(--rs-space-4) var(--rs-space-4);
  border-top: 1px solid var(--rs-border-default);
}
.candidate-profile-guide__account strong {
  font-size: 13px;
}
.candidate-profile-guide__account p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-tertiary);
  font-size: 12px;
}
.candidate-profile-guide__account a {
  display: inline-flex;
  align-items: center;
  gap: var(--rs-space-1);
  margin-top: var(--rs-space-3);
  font-weight: 600;
}

@media (max-width: 1100px) {
  .candidate-profile-layout {
    grid-template-columns: 1fr;
  }
  .candidate-profile-aside {
    position: static;
    order: -1;
  }
  .candidate-profile-form__secondary {
    grid-template-columns: 1fr;
  }
  .candidate-form-section__heading {
    display: block;
  }
  .candidate-form-section__heading p {
    margin-top: var(--rs-space-1);
  }
}
</style>
