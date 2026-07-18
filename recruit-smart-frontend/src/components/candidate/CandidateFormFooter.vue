<script setup lang="ts">
defineProps<{
  dirty: boolean
  saving: boolean
  lastSavedAt?: string
  saveLabel?: string
}>()

const emit = defineEmits<{ cancel: []; save: [] }>()
</script>

<template>
  <footer class="candidate-form-footer" :class="{ 'candidate-form-footer--dirty': dirty }">
    <div>
      <strong v-if="dirty">你有未保存的更改</strong>
      <span v-else-if="lastSavedAt">已于 {{ lastSavedAt }} 保存</span>
      <span v-else>修改后记得保存</span>
    </div>
    <div class="candidate-form-footer__actions">
      <el-button :disabled="!dirty || saving" @click="emit('cancel')">取消修改</el-button>
      <el-button type="primary" :loading="saving" :disabled="!dirty" @click="emit('save')">
        {{ saveLabel || '保存更改' }}
      </el-button>
    </div>
  </footer>
</template>
