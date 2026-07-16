<script setup lang="ts">
import { Eye } from 'lucide-vue-next'

import { getApplicationStatusTone } from '@/config/pipeline'
import type { PipelineApplication } from '@/types/pipeline'

defineProps<{
  applications: PipelineApplication[]
  loading: boolean
}>()

const emit = defineEmits<{
  select: [id: number]
}>()

function formatDate(value: string) {
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}
</script>

<template>
  <el-table
    v-loading="loading"
    :data="applications"
    row-key="id"
    height="calc(100dvh - 330px)"
    highlight-current-row
    @row-click="(row: PipelineApplication) => emit('select', row.id)"
  >
    <el-table-column type="selection" width="48" />
    <el-table-column prop="candidateName" label="候选人" min-width="160" fixed="left">
      <template #default="{ row }: { row: PipelineApplication }">
        <div class="pipeline-table__candidate">
          <strong>{{ row.candidateName }}</strong>
          <span>{{ row.education || '学历待补充' }} · {{ row.yearsOfExperience }} 年经验</span>
        </div>
      </template>
    </el-table-column>
    <el-table-column prop="jobTitle" label="投递职位" min-width="190">
      <template #default="{ row }: { row: PipelineApplication }">
        <div class="pipeline-table__candidate">
          <strong>{{ row.jobTitle }}</strong>
          <span>{{ row.department }}</span>
        </div>
      </template>
    </el-table-column>
    <el-table-column prop="statusText" label="当前阶段" width="128">
      <template #default="{ row }: { row: PipelineApplication }">
        <span
          class="rs-status-pill"
          :class="`rs-status-pill--${getApplicationStatusTone(row.status)}`"
        >
          {{ row.statusText }}
        </span>
      </template>
    </el-table-column>
    <el-table-column prop="aiMatch.score" label="AI 匹配" width="96" align="right" sortable>
      <template #default="{ row }: { row: PipelineApplication }">
        <strong v-if="row.aiMatch" class="pipeline-table__score">{{ row.aiMatch.score }}</strong>
        <span v-else class="pipeline-table__muted">暂无</span>
      </template>
    </el-table-column>
    <el-table-column prop="ownerName" label="负责人" width="112">
      <template #default="{ row }: { row: PipelineApplication }">
        {{ row.ownerName || '待分配' }}
      </template>
    </el-table-column>
    <el-table-column prop="sourceText" label="来源" width="112" />
    <el-table-column prop="lastActivityAt" label="最近活动" width="132" align="right" sortable>
      <template #default="{ row }: { row: PipelineApplication }">
        <span class="rs-tabular-number">{{ formatDate(row.lastActivityAt) }}</span>
      </template>
    </el-table-column>
    <el-table-column label="操作" width="64" fixed="right" align="right">
      <template #default="{ row }: { row: PipelineApplication }">
        <div @click.stop>
          <el-tooltip content="查看投递详情" placement="top">
            <el-button
              circle
              :icon="Eye"
              aria-label="查看投递详情"
              @click="emit('select', row.id)"
            />
          </el-tooltip>
        </div>
      </template>
    </el-table-column>

    <template #empty>
      <div class="pipeline-table__empty">
        <strong>没有符合条件的投递记录</strong>
        <span>调整职位、状态或关键字筛选条件。</span>
      </div>
    </template>
  </el-table>
</template>

<style scoped lang="scss">
.pipeline-table__candidate {
  display: grid;
  min-width: 0;
}

.pipeline-table__candidate strong,
.pipeline-table__candidate span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pipeline-table__candidate strong {
  color: var(--rs-text-primary);
  font-weight: 600;
}

.pipeline-table__candidate span,
.pipeline-table__muted {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}

.pipeline-table__score {
  color: var(--rs-success-700);
  font-variant-numeric: tabular-nums;
}

.pipeline-table__empty {
  display: grid;
  justify-items: center;
  gap: var(--rs-space-2);
  padding: var(--rs-space-8);
  color: var(--rs-text-tertiary);
}

.pipeline-table__empty strong {
  color: var(--rs-text-primary);
}
</style>
