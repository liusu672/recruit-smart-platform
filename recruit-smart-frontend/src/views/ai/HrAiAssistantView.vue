<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { Bot, RefreshCw, Send, Trash2 } from 'lucide-vue-next'
import { computed, nextTick, ref, watch } from 'vue'

import { sendToolChatMessage } from '@/api/aiAssistant'
import SupportPageHeader from '@/components/shared/SupportPageHeader.vue'

type AssistantRole = 'user' | 'assistant'
type AssistantMessageStatus = 'sending' | 'failed'

interface AssistantMessage {
  id: string
  role: AssistantRole
  content: string
  createdAt: string
  status?: AssistantMessageStatus
}

const messages = ref<AssistantMessage[]>([])
const draft = ref('')
const sending = ref(false)
const errorMessage = ref('')
const messageList = ref<HTMLElement>()

const canSend = computed(() => Boolean(draft.value.trim()) && !sending.value)

watch(messages, async () => {
  await nextTick()
  if (messageList.value) messageList.value.scrollTop = messageList.value.scrollHeight
})

function createMessage(role: AssistantRole, content: string, status?: AssistantMessageStatus) {
  const message: AssistantMessage = {
    id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
    role,
    content,
    createdAt: new Date().toISOString(),
  }
  if (status) message.status = status
  return message
}

function formatTime(value: string) {
  return new Date(value).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function clearConversation() {
  messages.value = []
  errorMessage.value = ''
}

async function send() {
  const content = draft.value.trim()
  if (!content || sending.value) return

  const userMessage = createMessage('user', content, 'sending')
  messages.value.push(userMessage)
  draft.value = ''
  errorMessage.value = ''
  sending.value = true

  try {
    const answer = await sendToolChatMessage(content)
    delete userMessage.status
    messages.value.push(createMessage('assistant', answer))
  } catch (error) {
    userMessage.status = 'failed'
    errorMessage.value = error instanceof Error ? error.message : 'HR AI 助手暂时无法响应'
    ElMessage.error(errorMessage.value)
  } finally {
    sending.value = false
  }
}
</script>

<template>
  <div class="hr-ai-assistant-view">
    <SupportPageHeader
      title="HR AI 助手"
      description="独立调用 Tool Chat，用于日期、时间和招聘辅助问答；不修改招聘流程状态。"
    >
      <template #actions>
        <el-button :icon="Trash2" :disabled="!messages.length && !draft" @click="clearConversation">
          清空会话
        </el-button>
      </template>
    </SupportPageHeader>

    <section class="hr-ai-assistant">
      <header class="hr-ai-assistant__notice">
        <Bot :size="18" :stroke-width="1.75" aria-hidden="true" />
        <p>此页面调用 /api/ai/tool-chat。面试工作区的输入框仍只生成候选人追问。</p>
      </header>

      <div ref="messageList" class="hr-ai-assistant__messages">
        <div v-if="!messages.length" class="hr-ai-assistant__empty">
          <Bot :size="28" :stroke-width="1.75" aria-hidden="true" />
          <h2>向 HR AI 助手提问</h2>
          <p>例如输入“今天几号？”验证 Tool Calling 日期工具链路。</p>
        </div>
        <article
          v-for="message in messages"
          :key="message.id"
          class="hr-ai-assistant-message"
          :class="{
            'hr-ai-assistant-message--user': message.role === 'user',
            'hr-ai-assistant-message--failed': message.status === 'failed',
          }"
        >
          <div>
            <strong>{{ message.role === 'user' ? '我' : 'HR AI 助手' }}</strong>
            <span>{{ formatTime(message.createdAt) }}</span>
          </div>
          <p>{{ message.content }}</p>
          <small v-if="message.status === 'sending'">发送中</small>
          <small v-else-if="message.status === 'failed'">发送失败</small>
        </article>
      </div>

      <div v-if="errorMessage" class="hr-ai-assistant__error" role="alert">
        <RefreshCw :size="16" :stroke-width="1.75" aria-hidden="true" />
        {{ errorMessage }}
      </div>

      <form class="hr-ai-assistant__composer" @submit.prevent="send">
        <el-input
          v-model="draft"
          type="textarea"
          :rows="3"
          maxlength="1000"
          show-word-limit
          placeholder="输入 HR AI 助手问题，例如：今天几号？"
          :disabled="sending"
        />
        <el-button
          native-type="submit"
          type="primary"
          :icon="Send"
          :loading="sending"
          :disabled="!canSend"
        >
          发送
        </el-button>
      </form>
    </section>
  </div>
</template>

<style scoped lang="scss">
.hr-ai-assistant-view {
  display: grid;
  height: 100%;
  min-height: 0;
  grid-template-rows: auto minmax(0, 1fr);
  gap: var(--rs-space-4);
  overflow: hidden;
}

.hr-ai-assistant {
  display: grid;
  min-height: 0;
  grid-template-rows: auto minmax(0, 1fr) auto auto;
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}

.hr-ai-assistant__notice {
  display: flex;
  align-items: center;
  gap: var(--rs-space-2);
  padding: var(--rs-space-3) var(--rs-space-4);
  border-bottom: 1px solid var(--rs-border-default);
  background: var(--rs-surface-subtle);
  color: var(--rs-text-secondary);
  font-size: 13px;
}

.hr-ai-assistant__notice p,
.hr-ai-assistant-message p,
.hr-ai-assistant__empty h2,
.hr-ai-assistant__empty p {
  margin: 0;
}

.hr-ai-assistant__notice svg,
.hr-ai-assistant__empty svg {
  color: var(--rs-blue-700);
}

.hr-ai-assistant__messages {
  display: grid;
  min-height: 0;
  align-content: start;
  gap: var(--rs-space-3);
  padding: var(--rs-space-6);
  overflow-y: auto;
}

.hr-ai-assistant__empty {
  display: grid;
  place-items: center;
  align-content: center;
  gap: var(--rs-space-2);
  min-height: 280px;
  color: var(--rs-text-secondary);
  text-align: center;
}

.hr-ai-assistant__empty h2 {
  color: var(--rs-text-primary);
  font-size: 18px;
}

.hr-ai-assistant-message {
  display: grid;
  max-width: 72%;
  gap: var(--rs-space-2);
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
}

.hr-ai-assistant-message--user {
  justify-self: end;
  background: var(--rs-blue-050);
}

.hr-ai-assistant-message--failed {
  border-color: var(--rs-danger-700);
}

.hr-ai-assistant-message > div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--rs-space-3);
  color: var(--rs-text-tertiary);
  font-size: 12px;
}

.hr-ai-assistant-message strong {
  color: var(--rs-text-primary);
}

.hr-ai-assistant-message p {
  white-space: pre-wrap;
}

.hr-ai-assistant-message small {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}

.hr-ai-assistant__error {
  display: flex;
  align-items: center;
  gap: var(--rs-space-2);
  padding: var(--rs-space-3) var(--rs-space-4);
  border-top: 1px solid var(--rs-border-default);
  background: var(--rs-danger-050);
  color: var(--rs-danger-700);
  font-size: 13px;
}

.hr-ai-assistant__composer {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: end;
  gap: var(--rs-space-3);
  padding: var(--rs-space-4);
  border-top: 1px solid var(--rs-border-default);
}
</style>
