<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { computed, nextTick, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import { useMessages } from '@/composables/useMessages'

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
  streamError,
  selectConversation,
} = useMessages()
const route = useRoute()
const draft = ref('')
const messageList = ref<HTMLElement>()
const conversations = computed(() => conversationsQuery.data.value?.items ?? [])
const messages = computed(() => [...(messagesQuery.data.value?.items ?? [])].reverse())
const conversationTotal = computed(() => conversationsQuery.data.value?.total ?? 0)
const messageTotal = computed(() => messagesQuery.data.value?.total ?? 0)

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
  if (!conversationId || !content) return
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
</script>

<template>
  <div class="messages-view">
    <header class="messages-view__intro">
      <div>
        <h2 class="rs-section-title">消息中心</h2>
        <p>围绕投递记录沟通，所有消息都会保留在对应招聘上下文中。</p>
      </div>
      <span class="messages-status" :class="`messages-status--${streamStatus}`">
        {{
          streamStatus === 'connected'
            ? '实时连接中'
            : streamStatus === 'reconnecting'
              ? '正在重连'
              : '连接中'
        }}
      </span>
    </header>

    <el-alert
      v-if="streamError"
      type="warning"
      :closable="false"
      show-icon
      title="消息实时连接暂不可用，页面仍会定时刷新未读数。"
    />

    <section class="messages-workspace">
      <aside class="messages-conversations">
        <div v-if="conversationsQuery.isLoading.value" class="messages-empty">正在加载会话...</div>
        <div
          v-else-if="conversationsQuery.error.value"
          class="messages-empty messages-empty--error"
        >
          {{
            conversationsQuery.error.value instanceof Error
              ? conversationsQuery.error.value.message
              : '会话加载失败'
          }}
        </div>
        <button
          v-for="conversation in conversations"
          :key="conversation.id"
          type="button"
          class="conversation-item"
          :class="{ 'conversation-item--active': selectedConversationId === conversation.id }"
          @click="selectConversation(conversation.id)"
        >
          <span class="conversation-item__avatar">{{
            conversation.candidateName.slice(0, 1)
          }}</span>
          <span class="conversation-item__body">
            <strong>{{ conversation.candidateName }}</strong>
            <small>{{ conversation.jobTitle }}</small>
            <span>{{ conversation.lastMessagePreview || '暂无消息' }}</span>
          </span>
          <span v-if="conversation.unreadCount" class="conversation-item__unread">
            {{ conversation.unreadCount > 99 ? '99+' : conversation.unreadCount }}
          </span>
        </button>
        <div
          v-if="!conversationsQuery.isLoading.value && !conversations.length"
          class="messages-empty"
        >
          暂无投递消息会话。
        </div>
        <el-pagination
          v-if="conversationTotal > conversationPageSize"
          v-model:current-page="conversationPage"
          class="messages-pagination"
          small
          layout="prev, pager, next"
          :page-size="conversationPageSize"
          :total="conversationTotal"
        />
      </aside>

      <section class="messages-thread">
        <div v-if="selectedConversationId === null" class="messages-thread__empty">
          <h3>选择一个会话</h3>
          <p>从左侧选择投递记录后开始沟通。</p>
        </div>
        <template v-else>
          <div ref="messageList" class="messages-thread__list">
            <div v-if="messagesQuery.isLoading.value" class="messages-empty">正在加载消息...</div>
            <div v-else-if="messagesQuery.error.value" class="messages-empty messages-empty--error">
              {{
                messagesQuery.error.value instanceof Error
                  ? messagesQuery.error.value.message
                  : '消息加载失败'
              }}
            </div>
            <div v-else-if="!messages.length" class="messages-empty">这个会话还没有消息。</div>
            <article
              v-for="message in messages"
              :key="message.id"
              class="message-bubble"
              :class="{ 'message-bubble--mine': message.mine }"
            >
              <div class="message-bubble__meta">
                {{ message.mine ? '我' : message.senderName }} · {{ formatTime(message.createdAt) }}
              </div>
              <p>{{ message.content }}</p>
            </article>
          </div>
          <el-pagination
            v-if="messageTotal > messagePageSize"
            v-model:current-page="messagePage"
            class="messages-pagination messages-pagination--thread"
            small
            layout="prev, pager, next"
            :page-size="messagePageSize"
            :total="messageTotal"
          />
          <form class="messages-composer" @submit.prevent="send">
            <el-input
              v-model="draft"
              type="textarea"
              :rows="3"
              maxlength="5000"
              show-word-limit
              placeholder="输入消息，说明需要对方确认的事项"
            />
            <el-button
              type="primary"
              native-type="submit"
              :loading="sendMutation.isPending.value"
              :disabled="!draft.trim()"
            >
              发送消息
            </el-button>
          </form>
        </template>
      </section>
    </section>
  </div>
</template>

<style scoped lang="scss">
.messages-view {
  display: grid;
  gap: var(--rs-space-4);
  min-height: calc(100dvh - 128px);
}
.messages-view__intro {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--rs-space-4);
}
.messages-view__intro p {
  margin: var(--rs-space-1) 0 0;
  color: var(--rs-text-secondary);
}
.messages-status {
  padding: var(--rs-space-1) var(--rs-space-2);
  border-radius: var(--rs-radius-pill);
  background: var(--rs-surface-muted);
  color: var(--rs-text-secondary);
  font-size: 12px;
}
.messages-status--connected {
  background: var(--rs-success-050);
  color: var(--rs-success-700);
}
.messages-status--reconnecting {
  background: var(--rs-warning-050);
  color: var(--rs-warning-800);
}
.messages-workspace {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  min-height: 560px;
  overflow: hidden;
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-primary);
}
.messages-conversations {
  border-right: 1px solid var(--rs-border-default);
  background: var(--rs-surface-subtle);
}
.conversation-item {
  display: flex;
  align-items: center;
  width: 100%;
  gap: var(--rs-space-2);
  padding: var(--rs-space-3);
  border: 0;
  border-bottom: 1px solid var(--rs-border-default);
  background: transparent;
  color: var(--rs-text-primary);
  text-align: left;
  cursor: pointer;
}
.conversation-item:hover,
.conversation-item--active {
  background: var(--rs-surface-selected);
}
.conversation-item__avatar {
  display: grid;
  flex: 0 0 32px;
  width: 32px;
  height: 32px;
  place-items: center;
  border-radius: 50%;
  background: var(--rs-action-primary);
  color: var(--rs-white);
  font-weight: 600;
}
.conversation-item__body {
  display: grid;
  flex: 1;
  min-width: 0;
  gap: 2px;
}
.conversation-item__body strong,
.conversation-item__body small,
.conversation-item__body span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.conversation-item__body small,
.conversation-item__body span {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}
.conversation-item__unread {
  min-width: 24px;
  padding: 2px 6px;
  border-radius: var(--rs-radius-pill);
  background: var(--rs-danger-050);
  color: var(--rs-danger-700);
  font-size: 12px;
  text-align: center;
}
.messages-thread {
  display: grid;
  grid-template-rows: minmax(0, 1fr) auto;
  min-width: 0;
}
.messages-thread__list {
  display: grid;
  align-content: start;
  gap: var(--rs-space-3);
  padding: var(--rs-space-6);
  overflow-y: auto;
}
.message-bubble {
  max-width: 72%;
  padding: var(--rs-space-3);
  border: 1px solid var(--rs-border-default);
  border-radius: var(--rs-radius-sm);
  background: var(--rs-surface-subtle);
}
.message-bubble--mine {
  justify-self: end;
  border-color: var(--rs-blue-050);
  background: var(--rs-blue-050);
}
.message-bubble__meta {
  color: var(--rs-text-tertiary);
  font-size: 12px;
}
.message-bubble p {
  margin: var(--rs-space-2) 0 0;
  white-space: pre-wrap;
}
.messages-composer {
  display: grid;
  gap: var(--rs-space-2);
  padding: var(--rs-space-4);
  border-top: 1px solid var(--rs-border-default);
}
.messages-composer .el-button {
  justify-self: end;
}
.messages-empty,
.messages-thread__empty {
  display: grid;
  place-items: center;
  min-height: 120px;
  padding: var(--rs-space-4);
  color: var(--rs-text-tertiary);
  text-align: center;
}
.messages-thread__empty h3,
.messages-thread__empty p {
  margin: 0;
}
.messages-thread__empty p {
  margin-top: var(--rs-space-2);
}
.messages-empty--error {
  color: var(--rs-danger-700);
}
.messages-pagination {
  display: flex;
  justify-content: center;
  padding: var(--rs-space-3);
  border-top: 1px solid var(--rs-border-default);
}
.messages-pagination--thread {
  justify-content: flex-end;
  border-top: 0;
}
@media (max-width: 1280px) {
  .messages-workspace {
    grid-template-columns: 280px minmax(0, 1fr);
  }
}
</style>
