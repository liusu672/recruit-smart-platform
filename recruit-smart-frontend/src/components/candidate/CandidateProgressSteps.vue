<script setup lang="ts">
export interface CandidateProgressStep {
  key: string
  label: string
}

defineProps<{
  steps: CandidateProgressStep[]
  currentStep: number
  terminalState?: 'danger' | 'neutral' | null
  terminalLabel?: string | undefined
}>()
</script>

<template>
  <div class="candidate-progress" :aria-label="terminalLabel || '当前流程进度'">
    <template v-for="(step, index) in steps" :key="step.key">
      <div
        class="candidate-progress__step"
        :class="{
          'candidate-progress__step--complete': index < currentStep,
          'candidate-progress__step--current': index === currentStep && !terminalState,
        }"
      >
        <span class="candidate-progress__marker">{{ index + 1 }}</span>
        <span>{{ step.label }}</span>
      </div>
      <span v-if="index < steps.length - 1" class="candidate-progress__line" aria-hidden="true" />
    </template>
    <span
      v-if="terminalState"
      class="candidate-progress__terminal"
      :class="`candidate-progress__terminal--${terminalState}`"
    >
      {{ terminalLabel }}
    </span>
  </div>
</template>
