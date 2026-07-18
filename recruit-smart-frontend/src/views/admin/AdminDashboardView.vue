<script setup lang="ts">
import { Activity, ScrollText, Settings2, UsersRound } from 'lucide-vue-next'
const modules = [
  {
    title: '用户管理',
    description: '创建、编辑和禁用系统账号',
    to: '/admin/users',
    icon: UsersRound,
    status: '可用',
    detail: '用户管理接口',
    tone: 'success',
  },
  {
    title: '角色与字典',
    description: '维护权限边界和基础配置',
    to: '/admin/roles',
    icon: Settings2,
    status: '待建设',
    detail: '后端治理模块',
    tone: 'warning',
  },
  {
    title: '审计日志',
    description: '追踪账号与配置关键变更',
    to: '/admin/audit',
    icon: ScrollText,
    status: '待建设',
    detail: '后端治理模块',
    tone: 'warning',
  },
  {
    title: '系统状态',
    description: '查看网关和业务服务运行情况',
    to: '/admin/system',
    icon: Activity,
    status: '待建设',
    detail: '后端治理模块',
    tone: 'warning',
  },
]
</script>
<template>
  <div class="role-portal">
    <div class="portal-toolbar">
      <div>
        <h2>系统治理中心</h2>
        <p>管理员负责账号、权限、配置和审计，不直接参与招聘业务操作。</p>
      </div>
    </div>
    <div class="portal-note">
      <Activity :size="17" /><span
        >用户管理接口已经接入；角色权限、字典、审计和系统状态仍等待后端能力。</span
      >
    </div>
    <section class="portal-panel">
      <div class="portal-list">
        <RouterLink v-for="item in modules" :key="item.to" :to="item.to" class="portal-row"
          ><div class="portal-row__primary">
            <h3><component :is="item.icon" :size="17" /> {{ item.title }}</h3>
            <p>{{ item.description }}</p>
          </div>
          <div class="portal-row__cell">
            <strong>{{ item.detail }}</strong
            ><span>系统治理能力</span>
          </div>
          <span class="rs-status-pill" :class="`rs-status-pill--${item.tone}`">{{
            item.status
          }}</span
          ><span>查看</span></RouterLink
        >
      </div>
    </section>
  </div>
</template>
