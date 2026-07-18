<script setup lang="ts">
import HrStatusBadge from '@/components/hr/HrStatusBadge.vue'
import { getEmployeeStatusTone, getTurnoverRiskText, getTurnoverRiskTone } from '@/config/employees'
import type { EmployeeRecord } from '@/types/employee'
defineProps<{ records: EmployeeRecord[]; loading: boolean }>()
const emit = defineEmits<{ select: [record: EmployeeRecord] }>()
function formatDate(value: string) {
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(new Date(value))
}
function formatAssessment(value: string | null) {
  return value ? `评估于 ${formatDate(value)}` : '尚未评估'
}
</script>

<template>
  <el-table
    :data="records"
    :loading="loading"
    row-key="id"
    height="520"
    empty-text="当前筛选条件下没有员工档案"
    @row-click="emit('select', $event)"
    ><el-table-column label="员工" min-width="170"
      ><template #default="{ row }: { row: EmployeeRecord }"
        ><div class="person">
          <span>{{ row.name.slice(0, 1) }}</span>
          <div>
            <strong>{{ row.name }}</strong
            ><small>{{ row.employeeNo }}</small>
          </div>
        </div></template
      ></el-table-column
    ><el-table-column label="部门 / 岗位" min-width="230"
      ><template #default="{ row }: { row: EmployeeRecord }"
        ><div class="stack">
          <strong>{{ row.department }}</strong
          ><small>{{ row.position }}</small>
        </div></template
      ></el-table-column
    ><el-table-column label="入职日期" width="128"
      ><template #default="{ row }: { row: EmployeeRecord }"
        ><span class="number">{{ formatDate(row.entryDate) }}</span></template
      ></el-table-column
    ><el-table-column label="员工状态" width="104" align="center"
      ><template #default="{ row }: { row: EmployeeRecord }"
        ><HrStatusBadge
          :status="row.status"
          :label="row.statusText"
          :tone="getEmployeeStatusTone(row.status)" /></template></el-table-column
    ><el-table-column label="离职倾向参考" min-width="150" align="center"
      ><template #default="{ row }: { row: EmployeeRecord }"
        ><div class="risk-reference">
          <HrStatusBadge
            :label="getTurnoverRiskText(row.turnoverRiskLevel)"
            :tone="getTurnoverRiskTone(row.turnoverRiskLevel)"
          /><small>{{ formatAssessment(row.riskAssessedAt) }}</small>
        </div></template
      ></el-table-column
    ><el-table-column label="联系方式" min-width="190"
      ><template #default="{ row }: { row: EmployeeRecord }"
        ><div class="stack">
          <span>{{ row.phone || '未提供' }}</span
          ><small>{{ row.email || '未提供' }}</small>
        </div></template
      ></el-table-column
    ><el-table-column label="操作" width="88" fixed="right" align="right"
      ><template #default="{ row }: { row: EmployeeRecord }"
        ><div @click.stop>
          <el-button link type="primary" @click="emit('select', row)">查看</el-button>
        </div></template
      ></el-table-column
    ></el-table
  >
</template>

<style scoped lang="scss">
.person {
  display: flex;
  align-items: center;
  gap: var(--rs-space-2);
}
.person > span {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-selected);
  color: var(--rs-blue-700);
  font-weight: 700;
}
.person div,
.stack {
  display: grid;
}
.person small,
.stack small {
  color: var(--rs-text-tertiary);
  font-size: 11px;
}
.number {
  font-variant-numeric: tabular-nums;
}
.risk-reference {
  display: grid;
  justify-items: center;
  gap: 4px;
}
.risk-reference small {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}
</style>
