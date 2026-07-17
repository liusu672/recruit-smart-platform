<script setup lang="ts">
import { BriefcaseBusiness, FileText, GraduationCap, TriangleAlert } from 'lucide-vue-next'

import type { InterviewWorkspace } from '@/types/interview'

defineProps<{
  interview: InterviewWorkspace
}>()

function formatTime(value: string | null) {
  if (!value) return '待预约'
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
  <aside class="candidate-brief" aria-label="候选人面试简报">
    <header class="candidate-brief__identity">
      <span class="candidate-brief__avatar">{{ interview.candidateName.slice(0, 1) }}</span>
      <div>
        <h3>{{ interview.candidateName }}</h3>
        <p>{{ interview.jobTitle }}</p>
      </div>
    </header>

    <dl class="candidate-brief__schedule">
      <div>
        <dt>面试时间</dt>
        <dd>{{ formatTime(interview.interviewTime) }}</dd>
      </div>
      <div>
        <dt>面试安排</dt>
        <dd>{{ interview.roundText }}，{{ interview.methodText }}</dd>
      </div>
      <div>
        <dt>地点或会议</dt>
        <dd>{{ interview.location || '待确认' }}</dd>
      </div>
    </dl>

    <section class="candidate-brief__section">
      <h4><GraduationCap :size="16" :stroke-width="1.75" />候选人背景</h4>
      <p>
        {{ interview.candidateBrief.school || '学校待补充' }}，{{
          interview.candidateBrief.education || '学历待补充'
        }}，{{ interview.candidateBrief.yearsOfExperience }} 年经验
      </p>
    </section>

    <section class="candidate-brief__section">
      <h4><BriefcaseBusiness :size="16" :stroke-width="1.75" />项目与经历</h4>
      <p>{{ interview.candidateBrief.projectExperience || '暂无项目经历摘要' }}</p>
      <p>{{ interview.candidateBrief.workExperience || '暂无工作经历摘要' }}</p>
    </section>

    <section class="candidate-brief__section">
      <h4><FileText :size="16" :stroke-width="1.75" />技能与简历</h4>
      <p>{{ interview.candidateBrief.resumeName }}</p>
      <div class="candidate-brief__skills">
        <span v-for="skill in interview.candidateBrief.skills" :key="skill">{{ skill }}</span>
      </div>
    </section>

    <section v-if="interview.candidateBrief.matchScore !== null" class="candidate-brief__ai">
      <div>
        <span>AI 岗位匹配参考</span>
        <strong>{{ interview.candidateBrief.matchScore }}%</strong>
      </div>
      <p>{{ interview.candidateBrief.matchSummary }}</p>
      <ul v-if="interview.candidateBrief.riskPoints.length">
        <li v-for="risk in interview.candidateBrief.riskPoints" :key="risk">
          <TriangleAlert :size="14" :stroke-width="1.75" aria-hidden="true" />{{ risk }}
        </li>
      </ul>
      <small>仅供准备面试时参考，不替代面试官判断。</small>
    </section>
  </aside>
</template>

<style scoped lang="scss">
.candidate-brief {
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.candidate-brief__identity,
.candidate-brief__section,
.candidate-brief__ai {
  padding: var(--rs-space-4);
}

.candidate-brief__identity {
  display: flex;
  align-items: center;
  gap: var(--rs-space-3);
  border-bottom: 1px solid var(--rs-border-default);
}

.candidate-brief__avatar {
  display: grid;
  width: 48px;
  height: 48px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: var(--rs-radius-sm);
  background: var(--rs-action-primary);
  color: var(--rs-white);
  font-size: 18px;
  font-weight: 700;
}

.candidate-brief h3,
.candidate-brief h4,
.candidate-brief p,
.candidate-brief dl {
  margin: 0;
}

.candidate-brief h3 {
  font-size: 16px;
}

.candidate-brief__identity p,
.candidate-brief__section p,
.candidate-brief__ai p,
.candidate-brief__ai small {
  color: var(--rs-text-secondary);
}

.candidate-brief__schedule {
  display: grid;
  gap: var(--rs-space-3);
  padding: var(--rs-space-4);
  border-bottom: 1px solid var(--rs-border-default);
}

.candidate-brief__schedule div {
  display: grid;
  gap: var(--rs-space-1);
}

.candidate-brief__schedule dt {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}

.candidate-brief__schedule dd {
  margin: 0;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  overflow-wrap: anywhere;
}

.candidate-brief__section + .candidate-brief__section {
  padding-top: 0;
}

.candidate-brief h4 {
  display: flex;
  align-items: center;
  gap: var(--rs-space-2);
  margin-bottom: var(--rs-space-2);
  font-size: 14px;
}

.candidate-brief__section p + p {
  margin-top: var(--rs-space-2);
}

.candidate-brief__skills {
  display: flex;
  flex-wrap: wrap;
  gap: var(--rs-space-1);
  margin-top: var(--rs-space-2);
}

.candidate-brief__skills span {
  padding: 2px var(--rs-space-2);
  border-radius: var(--rs-radius-pill);
  background: var(--rs-surface-muted);
  color: var(--rs-text-secondary);
  font-size: 12px;
}

.candidate-brief__ai {
  border-top: 1px solid var(--rs-border-default);
  background: var(--rs-surface-selected);
}

.candidate-brief__ai > div,
.candidate-brief__ai li {
  display: flex;
  align-items: center;
}

.candidate-brief__ai > div {
  justify-content: space-between;
  color: var(--rs-blue-700);
  font-weight: 600;
}

.candidate-brief__ai strong {
  font-size: 20px;
  font-variant-numeric: tabular-nums;
}

.candidate-brief__ai p {
  margin-top: var(--rs-space-2);
}

.candidate-brief__ai ul {
  display: grid;
  gap: var(--rs-space-1);
  padding: 0;
  margin: var(--rs-space-2) 0;
  list-style: none;
}

.candidate-brief__ai li {
  align-items: flex-start;
  gap: var(--rs-space-1);
  color: var(--rs-warning-800);
  font-size: 12px;
}

.candidate-brief__ai li svg {
  flex: 0 0 auto;
  margin-top: var(--rs-space-1);
}
</style>
