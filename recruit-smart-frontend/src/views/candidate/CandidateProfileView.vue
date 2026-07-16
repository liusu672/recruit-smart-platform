<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { reactive, watch } from 'vue'

import { getMyCandidateProfile, updateMyCandidateProfile } from '@/api/candidatePortal'
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
watch(
  () => resource.data.value,
  (profile) =>
    Object.assign(form, {
      gender: profile.gender ?? '',
      age: profile.age,
      education: profile.education ?? '',
      school: profile.school ?? '',
      major: profile.major ?? '',
      yearsOfExperience: profile.yearsOfExperience,
    }),
  { immediate: true },
)
async function save() {
  if (!resource.demoMode.value) await updateMyCandidateProfile(form)
  resource.data.value = { ...resource.data.value, ...form }
  ElMessage.success('个人求职资料已更新')
}
</script>

<template>
  <div class="role-portal">
    <div class="portal-toolbar">
      <div>
        <h2>个人求职资料</h2>
        <p>手机号和邮箱来自登录账号；求职资料仅由你本人维护。</p>
      </div>
    </div>
    <section class="portal-panel">
      <div v-if="resource.loading.value" class="portal-loading">正在加载个人资料...</div>
      <div v-else-if="resource.error.value" class="portal-error">{{ resource.error.value }}</div>
      <form v-else class="portal-profile-grid" @submit.prevent="save">
        <label>姓名<el-input :model-value="resource.data.value.name" disabled /></label
        ><label>联系邮箱<el-input :model-value="resource.data.value.email ?? ''" disabled /></label
        ><label
          >性别<el-select v-model="form.gender" clearable
            ><el-option label="男" value="男" /><el-option label="女" value="女" /><el-option
              label="其他"
              value="其他" /></el-select></label
        ><label
          >年龄<el-input-number v-model="form.age" :min="16" :max="80" style="width: 100%" /></label
        ><label>最高学历<el-input v-model="form.education" /></label
        ><label>毕业学校<el-input v-model="form.school" /></label
        ><label>专业<el-input v-model="form.major" /></label
        ><label
          >工作年限<el-input-number v-model="form.yearsOfExperience" :min="0" style="width: 100%"
        /></label>
        <div class="portal-profile-grid__wide">
          <el-button native-type="submit" type="primary">保存求职资料</el-button>
        </div>
      </form>
    </section>
  </div>
</template>
