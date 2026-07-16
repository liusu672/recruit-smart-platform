<script setup lang="ts">
import { Activity, ScrollText, Settings2, UsersRound } from 'lucide-vue-next'
const modules = [
  {
    title: '用户管理',
    description: '创建、编辑和禁用系统账号',
    to: '/admin/users',
    icon: UsersRound,
  },
  {
    title: '角色与字典',
    description: '维护权限边界和基础配置',
    to: '/admin/roles',
    icon: Settings2,
  },
  {
    title: '审计日志',
    description: '追踪账号与配置关键变更',
    to: '/admin/audit',
    icon: ScrollText,
  },
  {
    title: '系统状态',
    description: '查看网关和业务服务运行情况',
    to: '/admin/system',
    icon: Activity,
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
        >治理接口尚未全部完成，当前页面不使用候选人、Offer 或员工数据模拟系统指标。</span
      >
    </div>
    <section class="portal-panel">
      <div class="portal-list">
        <RouterLink v-for="item in modules" :key="item.to" :to="item.to" class="portal-row"
          ><div class="portal-row__primary">
            <h3><component :is="item.icon" :size="17" /> {{ item.title }}</h3>
            <p>{{ item.description }}</p>
          </div>
          <div class="portal-row__cell"><strong>接口待接入</strong><span>后端治理模块</span></div>
          <span class="rs-status-pill rs-status-pill--warning">待建设</span
          ><span>查看</span></RouterLink
        >
      </div>
    </section>
  </div>
</template>
