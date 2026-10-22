<template>
  <div class="page">
    <div class="container">
      <div class="chat card">
        <!-- 会话列表 -->
        <aside class="convs">
          <div class="convs-head">会话</div>
          <div v-for="c in convs" :key="c.id" class="conv" :class="{ on: c.id === activeId }" @click="openConv(c)">
            <el-avatar :size="34" style="background:#00bebd">{{ (otherName(c) || 'U').slice(0, 1) }}</el-avatar>
            <div class="cbody">
              <div class="cline">
                <b>{{ otherName(c) }}</b>
                <span class="muted">{{ (c.lastTime || '').replace('T', ' ').slice(5, 16) }}</span>
              </div>
              <div class="cline">
                <span class="last">{{ c.lastMsg || '暂无消息' }}</span>
                <el-badge v-if="unreadOf(c) > 0" :value="unreadOf(c)" />
              </div>
            </div>
          </div>
          <el-empty v-if="!convs.length" :image-size="60" description="暂无会话" />
        </aside>

        <!-- 聊天窗口 -->
        <section class="window">
          <template v-if="activeId">
            <div class="w-head">
              <b>{{ activeConv?.jobTitle || '在线沟通' }}</b>
              <span class="muted">与 {{ otherName(activeConv) }} 沟通中</span>
            </div>
            <div ref="msgBox" class="msgs">
              <div v-for="m in messages" :key="m.id" class="msg" :class="{ mine: m.fromId === myId }">
                <div class="bubble">{{ m.content }}</div>
                <div class="time">{{ (m.createTime || '').replace('T', ' ').slice(5, 16) }}</div>
              </div>
            </div>
            <div class="input">
              <el-input v-model="content" placeholder="输入消息，回车发送" @keyup.enter="send" />
              <el-button type="primary" @click="send">发送</el-button>
            </div>
          </template>
          <el-empty v-else description="选择一个会话开始沟通" />
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { auth } from '../store'

const route = useRoute()
const myId = Number(auth.userId)
const convs = ref([])
const messages = ref([])
const activeId = ref(null)
const content = ref('')
const msgBox = ref(null)
let timer = null

const activeConv = computed(() => convs.value.find((c) => c.id === activeId.value))

function otherName(c) {
  if (!c) return ''
  return c.userA === myId ? '招聘官 #' + c.userB : (c.jobTitle ? '求职者' : '会话')
}

function unreadOf(c) {
  return c.userA === myId ? (c.unreadA || 0) : (c.unreadB || 0)
}

async function loadConvs() {
  convs.value = (await api.imConversations()) || []
  if (!activeId.value && route.query.convId) {
    const id = Number(route.query.convId)
    const target = convs.value.find((c) => c.id === id)
    if (target) openConv(target)
  }
}

async function openConv(c) {
  activeId.value = c.id
  await loadMessages()
  await api.imRead(c.id)
  await loadConvsSilent()
}

async function loadMessages() {
  if (!activeId.value) return
  messages.value = (await api.imMessages(activeId.value)) || []
  await nextTick()
  if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight
}

async function loadConvsSilent() {
  convs.value = (await api.imConversations()) || []
}

async function send() {
  if (!content.value.trim()) return
  const text = content.value
  content.value = ''
  await api.imSend({ convId: activeId.value, toId: activeConv.value ? (activeConv.value.userA === myId ? activeConv.value.userB : activeConv.value.userA) : 0, content: text })
  await loadMessages()
  await loadConvsSilent()
}

onMounted(async () => {
  await loadConvs()
  if (route.query.convId) {
    const id = Number(route.query.convId)
    const target = convs.value.find((c) => c.id === id)
    if (target) await openConv(target)
  }
  timer = setInterval(async () => {
    try {
      await loadConvsSilent()
      if (activeId.value) await loadMessages()
    } catch {
      /* 轮询失败忽略 */
    }
  }, 3000)
})

onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.chat { display: flex; height: calc(100vh - 180px); min-height: 480px; overflow: hidden; }
.convs { width: 290px; border-right: 1px solid var(--border); overflow-y: auto; flex-shrink: 0; }
.convs-head { padding: 16px 18px; font-weight: 700; border-bottom: 1px solid var(--border); }
.conv { display: flex; gap: 10px; padding: 12px 16px; cursor: pointer; border-bottom: 1px solid #f5f6f8; }
.conv:hover, .conv.on { background: #f2fbfb; }
.cbody { flex: 1; min-width: 0; }
.cline { display: flex; justify-content: space-between; align-items: center; gap: 8px; }
.cline b { font-size: 13px; }
.last { font-size: 12px; color: var(--ink-3); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.window { flex: 1; display: flex; flex-direction: column; }
.w-head { padding: 14px 18px; border-bottom: 1px solid var(--border); display: flex; align-items: baseline; gap: 12px; }
.msgs { flex: 1; overflow-y: auto; padding: 18px; background: #fafbfc; display: flex; flex-direction: column; gap: 12px; }
.msg { display: flex; flex-direction: column; align-items: flex-start; }
.msg.mine { align-items: flex-end; }
.bubble { max-width: 60%; padding: 9px 14px; border-radius: 10px; background: #fff; box-shadow: 0 1px 4px rgba(31, 35, 41, .08); line-height: 1.6; word-break: break-word; }
.msg.mine .bubble { background: #00bebd; color: #fff; }
.time { font-size: 11px; color: var(--ink-3); margin-top: 3px; }
.input { display: flex; gap: 10px; padding: 14px 18px; border-top: 1px solid var(--border); }
</style>
