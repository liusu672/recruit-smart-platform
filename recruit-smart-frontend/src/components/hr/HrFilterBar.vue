<script setup lang="ts">
import { RefreshCw, RotateCcw, Search } from 'lucide-vue-next'

withDefaults(
  defineProps<{
    loading?: boolean
    activeCount?: number
    resetDisabled?: boolean
    showRefresh?: boolean
  }>(),
  {
    loading: false,
    activeCount: 0,
    resetDisabled: false,
    showRefresh: true,
  },
)

const emit = defineEmits<{
  submit: []
  reset: []
  refresh: []
}>()
</script>

<template>
  <form class="hr-filter-bar" role="search" @submit.prevent="emit('submit')">
    <div class="hr-filter-bar__fields">
      <slot />
    </div>
    <div class="hr-filter-bar__actions">
      <el-button native-type="submit" type="primary" :icon="Search">查询</el-button>
      <el-button :icon="RotateCcw" :disabled="resetDisabled" @click="emit('reset')">
        重置<span v-if="activeCount">（{{ activeCount }}）</span>
      </el-button>
      <el-tooltip v-if="showRefresh" content="刷新当前列表">
        <el-button
          :icon="RefreshCw"
          :loading="loading"
          aria-label="刷新当前列表"
          @click="emit('refresh')"
        />
      </el-tooltip>
      <slot name="actions" />
    </div>
  </form>
</template>
