<script setup lang="ts">
import {
  BriefcaseBusiness,
  CalendarClock,
  FileText,
  GraduationCap,
  MapPin,
  TriangleAlert,
} from 'lucide-vue-next'

import {
  getInterviewerStageText,
  getInterviewerStageTone,
  getInterviewerTaskStage,
} from '@/config/interviewer'
import type { InterviewWorkspace } from '@/types/interview'

defineProps<{ interview: InterviewWorkspace }>()

function formatTime(value: string | null) {
  if (!value) return '时间待确认'
  return new Intl.DateTimeFormat('zh-CN', {
    month: 'long',
    day: 'numeric',
    weekday: 'short',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(new Date(value))
}
</script>

<template>
  <section class="interviewer-candidate-context">
    <header class="interviewer-candidate-context__header">
      <span class="interviewer-candidate-context__avatar">{{
        interview.candidateName.slice(0, 1)
      }}</span>
      <div>
        <h2>{{ interview.candidateName }}</h2>
        <p>{{ interview.jobTitle }} · {{ interview.department }} · {{ interview.roundText }}</p>
      </div>
      <div class="interviewer-candidate-context__state">
        <span
          :class="`rs-status-pill rs-status-pill--${getInterviewerStageTone(getInterviewerTaskStage(interview))}`"
          >{{ getInterviewerStageText(getInterviewerTaskStage(interview)) }}</span
        >
        <small>反馈：{{ interview.feedbackStateText }}</small>
      </div>
    </header>

    <div class="interviewer-candidate-context__schedule">
      <div>
        <CalendarClock :size="16" :stroke-width="1.75" /><span>面试时间</span
        ><strong>{{ formatTime(interview.interviewTime) }}</strong>
      </div>
      <div>
        <BriefcaseBusiness :size="16" :stroke-width="1.75" /><span>方式与轮次</span
        ><strong>{{ interview.methodText }} · {{ interview.roundText }}</strong>
      </div>
      <div>
        <MapPin :size="16" :stroke-width="1.75" /><span>地点或会议</span
        ><strong>{{ interview.location || '待确认' }}</strong>
      </div>
    </div>

    <el-collapse class="interviewer-candidate-context__details">
      <el-collapse-item name="profile" title="展开候选人资料与岗位匹配参考">
        <div class="interviewer-candidate-context__detail-grid">
          <section>
            <h3><GraduationCap :size="16" :stroke-width="1.75" />背景信息</h3>
            <p>
              {{ interview.candidateBrief.school || '学校待补充' }}，{{
                interview.candidateBrief.education || '学历待补充'
              }}，{{ interview.candidateBrief.yearsOfExperience }} 年经验
            </p>
          </section>
          <section>
            <h3><BriefcaseBusiness :size="16" :stroke-width="1.75" />经历摘要</h3>
            <p>{{ interview.candidateBrief.projectExperience || '暂无项目经历摘要' }}</p>
            <p>{{ interview.candidateBrief.workExperience || '暂无工作经历摘要' }}</p>
          </section>
          <section>
            <h3><FileText :size="16" :stroke-width="1.75" />技能与简历</h3>
            <p>{{ interview.candidateBrief.resumeName }}</p>
            <div class="interviewer-candidate-context__skills">
              <span v-for="skill in interview.candidateBrief.skills" :key="skill">{{ skill }}</span>
            </div>
          </section>
          <section
            v-if="interview.candidateBrief.matchScore !== null"
            class="interviewer-candidate-context__ai"
          >
            <h3>AI 岗位匹配参考 <small>仅供参考</small></h3>
            <strong>{{ interview.candidateBrief.matchScore }}%</strong>
            <p>{{ interview.candidateBrief.matchSummary }}</p>
            <ul v-if="interview.candidateBrief.riskPoints.length">
              <li v-for="risk in interview.candidateBrief.riskPoints" :key="risk">
                <TriangleAlert :size="14" :stroke-width="1.75" />{{ risk }}
              </li>
            </ul>
          </section>
        </div>
      </el-collapse-item>
    </el-collapse>
  </section>
</template>

<style scoped lang="scss">
.interviewer-candidate-context {
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
.interviewer-candidate-context__header {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr) auto;
  align-items: center;
  gap: var(--rs-space-3);
  padding: var(--rs-space-4);
}
.interviewer-candidate-context__avatar {
  display: grid;
  width: 48px;
  height: 48px;
  place-items: center;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-action-primary);
  color: var(--rs-white);
  font-size: 18px;
  font-weight: 700;
}
.interviewer-candidate-context h2,
.interviewer-candidate-context h3,
.interviewer-candidate-context p {
  margin: 0;
}
.interviewer-candidate-context h2 {
  font-size: 18px;
}
.interviewer-candidate-context__header p {
  margin-top: var(--rs-space-1);
  color: var(--rs-text-secondary);
}
.interviewer-candidate-context__state {
  display: grid;
  justify-items: end;
  gap: var(--rs-space-1);
}
.interviewer-candidate-context__state small {
  color: var(--rs-text-secondary);
}
.interviewer-candidate-context__schedule {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  border-top: 1px solid var(--rs-border-default);
  border-bottom: 1px solid var(--rs-border-default);
  background: var(--rs-surface-subtle);
}
.interviewer-candidate-context__schedule > div {
  display: grid;
  grid-template-columns: 20px minmax(0, 1fr);
  gap: var(--rs-space-1) var(--rs-space-2);
  padding: var(--rs-space-3) var(--rs-space-4);
}
.interviewer-candidate-context__schedule > div + div {
  border-left: 1px solid var(--rs-border-default);
}
.interviewer-candidate-context__schedule svg {
  grid-row: 1 / 3;
  color: var(--rs-blue-700);
}
.interviewer-candidate-context__schedule span {
  color: var(--rs-text-secondary);
  font-size: 12px;
}
.interviewer-candidate-context__schedule strong {
  overflow-wrap: anywhere;
  font-size: 13px;
}
.interviewer-candidate-context__details {
  padding: 0 var(--rs-space-4);
  border: 0;
}
.interviewer-candidate-context__details :deep(.el-collapse-item__header) {
  font-size: 13px;
  font-weight: 600;
}
.interviewer-candidate-context__detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--rs-space-4);
  padding-bottom: var(--rs-space-4);
}
.interviewer-candidate-context__detail-grid section {
  min-width: 0;
  padding: var(--rs-space-3);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
}
.interviewer-candidate-context__detail-grid h3 {
  display: flex;
  align-items: center;
  gap: var(--rs-space-2);
  margin-bottom: var(--rs-space-2);
  font-size: 14px;
}
.interviewer-candidate-context__detail-grid p {
  color: var(--rs-text-secondary);
}
.interviewer-candidate-context__detail-grid p + p {
  margin-top: var(--rs-space-2);
}
.interviewer-candidate-context__skills {
  display: flex;
  flex-wrap: wrap;
  gap: var(--rs-space-1);
  margin-top: var(--rs-space-2);
}
.interviewer-candidate-context__skills span {
  padding: 2px var(--rs-space-2);
  border-radius: var(--rs-radius-pill);
  background: var(--rs-surface-muted);
  color: var(--rs-text-secondary);
  font-size: 12px;
}
.interviewer-candidate-context__ai strong {
  display: block;
  color: var(--rs-blue-700);
  font-size: 20px;
}
.interviewer-candidate-context__ai h3 small {
  color: var(--rs-text-tertiary);
  font-weight: 400;
}
.interviewer-candidate-context__ai ul {
  display: grid;
  gap: var(--rs-space-1);
  padding: 0;
  margin: var(--rs-space-2) 0 0;
  list-style: none;
}
.interviewer-candidate-context__ai li {
  display: flex;
  align-items: flex-start;
  gap: var(--rs-space-1);
  color: var(--rs-warning-800);
  font-size: 12px;
}
</style>
