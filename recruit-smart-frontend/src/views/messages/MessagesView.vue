<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { MessageCircle, RefreshCw, Send, WifiOff } from 'lucide-vue-next'
import { computed, nextTick, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import SupportPageHeader from '@/components/shared/SupportPageHeader.vue'
import { useMessages } from '@/composables/useMessages'
import { useSessionStore } from '@/stores/session'

const {
  selectedConversationId,
  conversationPage,
  conversationPageSize,
  messagePage,
  messagePageSize,
  conversationsQuery,
  messagesQuery,
  sendMutation,
  streamStatus,
  streamFailureCount,
  streamLastSyncedAt,
  retryStream,
  selectConversation,
} = useMessages()
const route = useRoute()
const session = useSessionStore()
const draft = ref('')
const messageList = ref<HTMLElement>()
const conversations = computed(() => conversationsQuery.data.value?.items ?? [])
const messages = computed(() => [...(messagesQuery.data.value?.items ?? [])].reverse())
const conversationTotal = computed(() => conversationsQuery.data.value?.total ?? 0)
const messageTotal = computed(() => messagesQuery.data.value?.total ?? 0)
const isCandidate = computed(() => session.currentRole === 'CANDIDATE')
const showConnectionWarning = computed(() => streamFailureCount.value >= 2)
const pageDescription = computed(() => {
  if (isCandidate.value) return '围绕投递记录与招聘方沟通，消息会保留在对应职位中。'
  if (session.currentRole === 'INTERVIEWER') return '查看与候选人的面试通知、时间调整和招聘沟通。'
  return '围绕招聘上下文查看通知、安排调整和候选人沟通。'
})

function parseConversationId(value: unknown) {
  const raw = Array.isArray(value) ? value[0] : value
  const id = Number(raw)
  return Number.isFinite(id) && id > 0 ? id : null
}

watch(
  () => route.query.conversationId,
  (value) => {
    const id = parseConversationId(value)
    if (id !== null) selectConversation(id)
  },
  { immediate: true },
)
watch(
  conversations,
  (items) => {
    if (selectedConversationId.value === null && items[0]) selectConversation(items[0].id)
  },
  { immediate: true },
)
watch(messages, async () => {
  await nextTick()
  if (messageList.value) messageList.value.scrollTop = messageList.value.scrollHeight
})

async function send() {
  const conversationId = selectedConversationId.value
  const content = draft.value.trim()
  if (!conversationId || !content || sendMutation.isPending.value) return
  try {
    await sendMutation.mutateAsync({ conversationId, content })
    draft.value = ''
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '消息发送失败')
  }
}

function formatTime(value: string | null) {
  return value ? new Date(value).toLocaleString('zh-CN') : '暂无消息'
}

function formatSyncTime(value: string | null) {
  if (!value) return '尚未成功连接'
  return new Date(value).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
</script>

<template>
  <div class="support-messages-view">
    <SupportPageHeader title="消息中心" :description="pageDescription">
      <template v-if="streamStatus !== 'connected'" #actions>
        <span class="support-message-connection" role="status">
          <span />{{ streamStatus === 'reconnecting' ? '正在重连' : '正在连接' }}
        </span>
      </template>
    </SupportPageHeader>

    <section class="support-messages-workspace">
      <aside class="support-conversations">
        <header>
          <h2>会话</h2>
          <span>{{ conversationTotal }}</span>
        </header>
        <div v-if="conversationsQuery.isLoading.value" class="support-messages-local-state">
          <el-skeleton :rows="5" animated />
        </div>
        <div
          v-else-if="conversationsQuery.error.value"
          class="support-messages-local-state support-messages-local-state--error"
        >
          <WifiOff :size="22" :stroke-width="1.75" />
          <strong>会话暂时无法加载</strong>
          <p>请检查网络后重新加载。</p>
          <el-button
            :icon="RefreshCw"
            :loading="conversationsQuery.isFetching.value"
            @click="conversationsQuery.refetch()"
            >重新加载</el-button
          >
        </div>
        <div v-else-if="conversations.length" class="support-conversation-list">
          <button
            v-for="conversation in conversations"
            :key="conversation.id"
            type="button"
            :class="{
              'support-conversation-item--active': selectedConversationId === conversation.id,
            }"
            @click="selectConversation(conversation.id)"
          >
            <span class="support-conversation-item__avatar">
              {{ (isCandidate ? conversation.jobTitle : conversation.candidateName).slice(0, 1) }}
            </span>
            <span class="support-conversation-item__body">
              <strong>{{
                isCandidate ? conversation.jobTitle : conversation.candidateName
              }}</strong>
              <small>{{ isCandidate ? '招聘沟通' : conversation.jobTitle }}</small>
              <span>{{ conversation.lastMessagePreview || '暂无消息' }}</span>
            </span>
            <span class="support-conversation-item__aside">
              <small>{{
                conversation.lastMessageAt
                  ? new Date(conversation.lastMessageAt).toLocaleDateString('zh-CN')
                  : ''
              }}</small>
              <span v-if="conversation.unreadCount">{{
                conversation.unreadCount > 99 ? '99+' : conversation.unreadCount
              }}</span>
            </span>
          </button>
        </div>
        <div v-else class="support-messages-local-state">
          <MessageCircle :size="24" :stroke-width="1.75" />
          <strong>暂无沟通记录</strong>
          <p>{{ isCandidate ? '与招聘方的沟通会显示在这里。' : '候选人会话会显示在这里。' }}</p>
        </div>
        <el-pagination
          v-if="conversationTotal > conversationPageSize"
          v-model:current-page="conversationPage"
          class="support-messages-pagination"
          small
          layout="prev, pager, next"
          :page-size="conversationPageSize"
          :total="conversationTotal"
        />
      </aside>

      <section class="support-message-thread">
        <div v-if="showConnectionWarning" class="support-message-warning" role="alert">
          <WifiOff :size="18" :stroke-width="1.75" />
          <div>
            <strong>连接暂时中断</strong>
            <p>
              消息可能不是最新状态，系统仍在自动重试。上次成功连接：{{
                formatSyncTime(streamLastSyncedAt)
              }}
            </p>
          </div>
          <el-button :icon="RefreshCw" @click="retryStream">立即重试</el-button>
        </div>
        <div v-if="selectedConversationId === null" class="support-message-thread__empty">
          <MessageCircle :size="28" :stroke-width="1.75" />
          <h2>选择左侧会话查看沟通记录</h2>
          <p>
            {{
              isCandidate
                ? '与招聘方的职位沟通和安排通知会展示在这里。'
                : '与候选人的通知、安排调整和招聘沟通会展示在这里。'
            }}
          </p>
        </div>
        <template v-else>
          <div ref="messageList" class="support-message-thread__list">
            <div v-if="messagesQuery.isLoading.value" class="support-messages-local-state">
              <el-skeleton :rows="6" animated />
            </div>
            <div
              v-else-if="messagesQuery.error.value"
              class="support-messages-local-state support-messages-local-state--error"
            >
              <WifiOff :size="22" :stroke-width="1.75" />
              <strong>消息暂时无法加载</strong>
              <p>当前会话仍然保留，请重新加载消息。</p>
              <el-button
                :icon="RefreshCw"
                :loading="messagesQuery.isFetching.value"
                @click="messagesQuery.refetch()"
                >重新加载</el-button
              >
            </div>
            <div v-else-if="!messages.length" class="support-messages-local-state">
              <MessageCircle :size="24" :stroke-width="1.75" />
              <strong>这个会话还没有消息</strong>
              <p>发送第一条消息开始沟通。</p>
            </div>
            <article
              v-for="message in messages"
              :key="message.id"
              class="support-message-bubble"
              :class="{ 'support-message-bubble--mine': message.mine }"
            >
              <div>
                {{ message.mine ? '我' : message.senderName }} · {{ formatTime(message.createdAt) }}
              </div>
              <p>{{ message.content }}</p>
            </article>
          </div>
          <el-pagination
            v-if="messageTotal > messagePageSize"
            v-model:current-page="messagePage"
            class="support-messages-pagination support-messages-pagination--thread"
            small
            layout="prev, pager, next"
            :page-size="messagePageSize"
            :total="messageTotal"
          />
          <form class="support-message-composer" @submit.prevent="send">
            <el-input
              v-model="draft"
              type="textarea"
              :rows="3"
              maxlength="5000"
              show-word-limit
              placeholder="输入消息，Enter 换行后点击发送"
              :disabled="sendMutation.isPending.value"
            />
            <el-button
              native-type="submit"
              type="primary"
              :icon="Send"
              :loading="sendMutation.isPending.value"
              :disabled="!draft.trim()"
              >发送消息</el-button
            >
          </form>
        </template>
      </section>
    </section>
  </div>
</template>

<style scoped lang="scss">
.support-messages-view {
  display: grid;
  height: 100%;
  min-height: 560px;
  grid-template-rows: auto minmax(0, 1fr);
  gap: var(--rs-space-4);
}
.support-message-connection {
  display: flex;
  align-items: center;
  gap: var(--rs-space-2);
  color: var(--rs-warning-800);
  font-size: 11px;
}
.support-message-connection > span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--rs-warning-800);
}
.support-messages-workspace {
  display: grid;
  min-height: 0;
  grid-template-columns: var(--rs-support-conversation-width) minmax(0, 1fr);
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
.support-conversations {
  display: grid;
  min-height: 0;
  grid-template-rows: auto minmax(0, 1fr) auto;
  border-right: 1px solid var(--rs-border-default);
}
.support-conversations > header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 52px;
  padding: 0 var(--rs-space-4);
  border-bottom: 1px solid var(--rs-border-default);
}
.support-conversations h2 {
  margin: 0;
  font-size: 16px;
}
.support-conversations header span {
  color: var(--rs-text-secondary);
}
.support-conversation-list {
  min-height: 0;
  overflow-y: auto;
}
.support-conversation-list > button {
  display: grid;
  width: 100%;
  min-height: 88px;
  grid-template-columns: 40px minmax(0, 1fr) auto;
  gap: var(--rs-space-3);
  padding: var(--rs-space-3);
  border: 0;
  border-bottom: 1px solid var(--rs-border-default);
  background: transparent;
  color: var(--rs-text-primary);
  text-align: left;
  cursor: pointer;
}
.support-conversation-list > button:hover {
  background: var(--rs-surface-subtle);
}
.support-conversation-item--active {
  background: var(--rs-surface-selected) !important;
}
.support-conversation-item__avatar {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border-radius: 50%;
  background: var(--rs-blue-050);
  color: var(--rs-blue-700);
  font-weight: 700;
}
.support-conversation-item__body,
.support-conversation-item__aside {
  display: grid;
  align-content: start;
  min-width: 0;
}
.support-conversation-item__body strong,
.support-conversation-item__body small,
.support-conversation-item__body > span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.support-conversation-item__body small,
.support-conversation-item__body > span {
  color: var(--rs-text-secondary);
  font-size: 11px;
}
.support-conversation-item__aside {
  align-self: stretch;
  justify-items: end;
  align-content: space-between;
}
.support-conversation-item__aside small {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}
.support-conversation-item__aside > span {
  min-width: 22px;
  padding: 2px 5px;
  border-radius: var(--rs-radius-pill);
  background: var(--rs-danger-700);
  color: var(--rs-white);
  font-size: 12px;
  text-align: center;
}
.support-message-thread {
  display: grid;
  min-width: 0;
  min-height: 0;
  grid-template-rows: auto minmax(0, 1fr) auto auto;
}
.support-message-warning {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: var(--rs-space-3);
  padding: var(--rs-space-3) var(--rs-space-4);
  border-bottom: 1px solid var(--rs-border-default);
  background: var(--rs-warning-050);
  color: var(--rs-warning-800);
}
.support-message-warning p {
  margin: var(--rs-space-1) 0 0;
  color: var(--rs-text-secondary);
  font-size: 12px;
}
.support-message-thread__list {
  display: grid;
  min-height: 0;
  align-content: start;
  gap: var(--rs-space-3);
  padding: var(--rs-space-6);
  overflow-y: auto;
}
.support-message-bubble {
  max-width: 72%;
  padding: var(--rs-space-3);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
}
.support-message-bubble--mine {
  justify-self: end;
  background: var(--rs-blue-050);
}
.support-message-bubble div {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}
.support-message-bubble p {
  margin: var(--rs-space-2) 0 0;
  white-space: pre-wrap;
}
.support-message-composer {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: end;
  gap: var(--rs-space-3);
  padding: var(--rs-space-4);
  border-top: 1px solid var(--rs-border-default);
}
.support-messages-local-state,
.support-message-thread__empty {
  display: grid;
  place-items: center;
  align-content: center;
  gap: var(--rs-space-2);
  min-height: 180px;
  padding: var(--rs-space-4);
  color: var(--rs-text-secondary);
  text-align: center;
}
.support-messages-local-state p,
.support-message-thread__empty h2,
.support-message-thread__empty p {
  margin: 0;
}
.support-message-thread__empty {
  min-height: 100%;
}
.support-message-thread__empty h2 {
  color: var(--rs-text-primary);
  font-size: 16px;
}
.support-messages-local-state--error svg {
  color: var(--rs-warning-800);
}
.support-messages-pagination {
  display: flex;
  justify-content: center;
  padding: var(--rs-space-2);
  border-top: 1px solid var(--rs-border-default);
}
.support-messages-pagination--thread {
  justify-content: flex-end;
  border-top: 0;
}
@media (max-width: 1280px) {
  .support-messages-workspace {
    grid-template-columns: 288px minmax(0, 1fr);
  }
}
</style>
