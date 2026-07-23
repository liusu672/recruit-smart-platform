<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { Bot, CircleStop, RefreshCw, Send, Trash2 } from 'lucide-vue-next'
import { computed, nextTick, ref, watch } from 'vue'

import { streamToolChatMessage } from '@/api/aiAssistant'
import SupportPageHeader from '@/components/shared/SupportPageHeader.vue'

type AssistantRole = 'user' | 'assistant'
type AssistantMessageStatus = 'streaming' | 'failed' | 'stopped'

interface AssistantMessage {
  id: string
  role: AssistantRole
  content: string
  createdAt: string
  status?: AssistantMessageStatus
}

const messages = ref<AssistantMessage[]>([])
const draft = ref('')
const streaming = ref(false)
const errorMessage = ref('')
const messageList = ref<HTMLElement>()
const activeController = ref<AbortController | null>(null)
const streamingAssistantId = ref('')

const canSend = computed(() => Boolean(draft.value.trim()) && !streaming.value)

watch(messages, async () => {
  await nextTick()
  if (messageList.value) messageList.value.scrollTop = messageList.value.scrollHeight
}, { deep: true })

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
  if (streaming.value) stopGeneration()
  messages.value = []
  errorMessage.value = ''
}

function replaceMessage(
  id: string,
  updater: (message: AssistantMessage) => AssistantMessage,
) {
  const index = messages.value.findIndex((message) => message.id === id)
  if (index === -1) return
  messages.value.splice(index, 1, updater(messages.value[index]!))
}

function getMessage(id: string) {
  return messages.value.find((message) => message.id === id)
}

function appendAssistantContent(id: string, content: string) {
  replaceMessage(id, (message) => ({
    ...message,
    content: message.content + content,
  }))
}

function completeAssistantMessage(id: string) {
  replaceMessage(id, (message) => {
    return {
      id: message.id,
      role: message.role,
      content: message.content || 'AI 未返回有效内容',
      createdAt: message.createdAt,
    }
  })
}

function markStopped(id: string) {
  replaceMessage(id, (message) => {
    if (message.status !== 'streaming') return message
    return {
      ...message,
      content: message.content || '已停止生成',
      status: 'stopped',
    }
  })
}

function stopGeneration() {
  const controller = activeController.value
  markStopped(streamingAssistantId.value)
  if (controller && !controller.signal.aborted) controller.abort()
  activeController.value = null
  streamingAssistantId.value = ''
  streaming.value = false
}

function failAssistantMessage(id: string, error: unknown) {
  if (getMessage(id)?.status === 'failed') return
  errorMessage.value = error instanceof Error ? error.message : 'HR AI 助手暂时无法响应'
  replaceMessage(id, (message) => ({
    ...message,
    status: 'failed',
  }))
  ElMessage.error(errorMessage.value)
}

async function send() {
  const content = draft.value.trim()
  if (!content || streaming.value) return

  const userMessage = createMessage('user', content)
  const assistantMessage = createMessage('assistant', '', 'streaming')
  const controller = new AbortController()
  messages.value.push(userMessage)
  messages.value.push(assistantMessage)
  draft.value = ''
  errorMessage.value = ''
  streaming.value = true
  activeController.value = controller
  streamingAssistantId.value = assistantMessage.id

  try {
    await streamToolChatMessage({
      message: content,
      signal: controller.signal,
      onEvent(event) {
        if (event.type === 'delta') {
          appendAssistantContent(assistantMessage.id, event.content)
          return
        }
        if (event.type === 'done') {
          completeAssistantMessage(assistantMessage.id)
          return
        }
        if (event.type === 'error') {
          failAssistantMessage(assistantMessage.id, new Error(event.message))
        }
      },
      onError(error) {
        if (!controller.signal.aborted) failAssistantMessage(assistantMessage.id, error)
      },
    })
    if (getMessage(assistantMessage.id)?.status === 'streaming') {
      completeAssistantMessage(assistantMessage.id)
    }
  } catch (error) {
    if (controller.signal.aborted) {
      markStopped(assistantMessage.id)
    } else {
      failAssistantMessage(assistantMessage.id, error)
    }
  } finally {
    if (activeController.value === controller) {
      activeController.value = null
      streamingAssistantId.value = ''
      streaming.value = false
    }
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
        <p>此页面调用 /api/ai/tool-chat/stream。面试工作区的输入框仍只生成候选人追问。</p>
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
            'hr-ai-assistant-message--stopped': message.status === 'stopped',
          }"
        >
          <div>
            <strong>{{ message.role === 'user' ? '我' : 'HR AI 助手' }}</strong>
            <span>{{ formatTime(message.createdAt) }}</span>
          </div>
          <p>{{ message.content }}</p>
          <small v-if="message.status === 'streaming'">生成中</small>
          <small v-else-if="message.status === 'failed'">发送失败</small>
          <small v-else-if="message.status === 'stopped'">已停止</small>
        </article>
      </div>

      <div v-if="errorMessage" class="hr-ai-assistant__error" role="alert">
        <RefreshCw :size="16" :stroke-width="1.75" aria-hidden="true" />
        {{ errorMessage }}
      </div>

      <form class="hr-ai-assistant__composer" @submit.prevent="streaming ? stopGeneration() : send()">
        <el-input
          v-model="draft"
          type="textarea"
          :rows="3"
          maxlength="1000"
          show-word-limit
          placeholder="输入 HR AI 助手问题，例如：今天几号？"
          :disabled="streaming"
        />
        <el-button
          native-type="submit"
          :type="streaming ? 'default' : 'primary'"
          :icon="streaming ? CircleStop : Send"
          :disabled="!canSend && !streaming"
        >
          {{ streaming ? '停止生成' : '发送' }}
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

.hr-ai-assistant-message--stopped {
  border-color: var(--rs-border-strong);
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
  overflow-wrap: anywhere;
  line-height: 1.7;
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
