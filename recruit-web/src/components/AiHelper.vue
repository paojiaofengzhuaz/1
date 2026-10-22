<template>
  <div class="ai-helper">
    <!-- 悬浮入口 -->
    <div v-show="!open" class="fab" @click="toggle(true)">
      <span class="fab-icon">🤖</span>
      <span class="fab-text">AI 求职助手</span>
    </div>

    <!-- 对话面板 -->
    <section v-show="open" class="panel">
      <header class="phead">
        <div class="ptitle">
          <span class="avatar">🤖</span>
          <div class="pmeta">
            <b>小招 · AI 求职助手</b>
            <em>简历 / 面试 / 选岗 / 附件修改</em>
          </div>
        </div>
        <div class="pact">
          <el-button link size="small" @click="clearChat">清空</el-button>
          <el-button link size="small" @click="toggle(false)">收起</el-button>
        </div>
      </header>

      <div ref="bodyEl" class="pbody">
        <div v-for="(m, i) in messages" :key="i" class="msg" :class="m.role">
          <div class="bubble">{{ m.content }}</div>
        </div>
        <div v-if="loading || rewriting" class="msg ai">
          <div class="bubble typing">
            <i /><i /><i />
          </div>
        </div>
      </div>

      <div v-if="showQuick" class="quick">
        <span v-for="q in quickList" :key="q" class="qtag" @click="send(q)">{{ q }}</span>
      </div>

      <!-- 附件条 -->
      <div v-if="attachment" class="attach">
        <span class="afile" @click="previewVisible = true">📎 {{ attachment.fileName }}</span>
        <span class="ameta">{{ attachment.chars }} 字{{ attachment.truncated ? '（已截断）' : '' }}</span>
        <el-button link size="small" @click="previewVisible = true">预览</el-button>
        <el-button link size="small" type="primary" :loading="rewriting" @click="rewriteDoc">AI 修改</el-button>
        <el-button link size="small" @click="attachment = null">移除</el-button>
      </div>

      <footer class="pfoot">
        <input
          ref="fileEl"
          class="hidden-file"
          type="file"
          accept=".txt,.md,.markdown,.csv,.json,.log,.pdf,.docx"
          @change="onFile"
        />
        <el-tooltip content="上传附件（txt / md / pdf / docx，≤10MB）" placement="top">
          <el-button class="attach-btn" :loading="uploading" @click="fileEl.click()">📎</el-button>
        </el-tooltip>
        <el-input
          v-model="input"
          type="textarea"
          :rows="2"
          resize="none"
          maxlength="500"
          :placeholder="attachment ? '说说你想怎么改这份附件，Enter 发送' : '说说你的求职问题，Enter 发送 / Ctrl+Enter 换行'"
          @keydown.enter.exact.prevent="send()"
        />
        <el-button type="primary" :loading="loading" :disabled="!input.trim()" @click="send()">发送</el-button>
      </footer>
    </section>

    <!-- 附件预览 -->
    <el-dialog v-model="previewVisible" title="附件内容预览" width="680px" append-to-body>
      <div v-if="attachment" class="dlg-meta">
        <el-tag size="small" type="success">{{ attachment.fileType }}</el-tag>
        <span>{{ attachment.fileName }} · {{ attachment.chars }} 字</span>
      </div>
      <pre class="doc">{{ attachment?.text }}</pre>
    </el-dialog>

    <!-- 修改结果 -->
    <el-dialog v-model="resultVisible" title="AI 修改稿" width="780px" append-to-body>
      <div class="dlg-meta">
        <el-tag size="small" :type="result.ai ? 'success' : 'warning'">
          {{ result.ai ? '大模型生成' : 'AI 暂不可用' }}
        </el-tag>
        <span>{{ result.fileName }}</span>
      </div>
      <div v-if="result.notes" class="notes">
        <b>修改说明</b>
        <pre class="note-text">{{ result.notes }}</pre>
      </div>
      <el-input v-model="result.content" type="textarea" :rows="14" resize="vertical" />
      <template #footer>
        <el-button @click="copyResult">复制全文</el-button>
        <el-button type="primary" @click="downloadResult">下载修改稿</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, ref } from 'vue'
import { api } from '../api'
import { auth } from '../store'

const WELCOME = auth.token
  ? '你好，我是小招 👋 已读取你的简历，可以问我：简历怎么改、这个岗位适不适合我、面试怎么准备、薪资怎么谈。\n也可以点左下角 📎 上传简历/文档（txt、md、pdf、docx），我能直接帮你修改。'
  : '你好，我是小招 👋 可以问我简历优化、面试准备、岗位选择、薪资谈判等求职问题。\n也可以点左下角 📎 上传简历/文档（txt、md、pdf、docx），我能直接帮你修改。'

const QUICK = [
  '简历怎么写才吸引 HR？',
  '面试自我介绍怎么说？',
  '怎么跟 HR 谈薪资？',
  '我该选大厂还是小公司？'
]

const open = ref(false)
const input = ref('')
const loading = ref(false)
const bodyEl = ref(null)
const messages = ref([{ role: 'assistant', content: WELCOME }])
const quickList = QUICK
const showQuick = ref(true)

// 附件相关
const fileEl = ref(null)
const attachment = ref(null)
const uploading = ref(false)
const rewriting = ref(false)
const previewVisible = ref(false)
const resultVisible = ref(false)
const result = ref({ content: '', notes: '', fileName: '', ai: false })

function toggle(v) {
  open.value = v
  if (v) scrollBottom()
}

function clearChat() {
  messages.value = [{ role: 'assistant', content: WELCOME }]
  showQuick.value = true
  attachment.value = null
}

async function send(text) {
  const q = (typeof text === 'string' ? text : input.value).trim()
  if (!q || loading.value) return
  messages.value.push({ role: 'user', content: q })
  input.value = ''
  showQuick.value = false
  loading.value = true
  scrollBottom()
  try {
    const payload = messages.value.map((m) => ({ role: m.role, content: m.content }))
    // 有附件时，把附件正文作为上下文插到本轮提问之前（单条消息后端会截断到 2000 字）
    if (attachment.value) {
      payload.splice(payload.length - 1, 0, {
        role: 'user',
        content: `【附件：${attachment.value.fileName}】\n${attachment.value.text.slice(0, 2000)}`
      })
    }
    const r = await api.aiChat(payload)
    messages.value.push({
      role: 'assistant',
      content: (r && r.reply) || '这个问题我还没想明白，换个说法再问问？'
    })
  } catch {
    messages.value.push({ role: 'assistant', content: '网络有点小波动，请稍后再问我一次～' })
  } finally {
    loading.value = false
    scrollBottom()
  }
}

async function onFile(e) {
  const f = e.target.files && e.target.files[0]
  e.target.value = ''
  if (!f) return
  uploading.value = true
  try {
    const r = await api.aiUpload(f)
    attachment.value = r
    showQuick.value = false
    messages.value.push({
      role: 'assistant',
      content: `已读取附件「${r.fileName}」，共 ${r.chars} 字${r.truncated ? '（超长部分已截断）' : ''}。\n你可以：① 直接问我这份文档的问题；② 点附件条上的「AI 修改」让我按你的要求改写；③ 输入具体要求（如"帮我改得更专业、突出量化成果"）后点发送。`
    })
    scrollBottom()
  } catch (err) {
    messages.value.push({
      role: 'assistant',
      content: `附件读取失败：${err?.message || '请确认格式为 txt / md / pdf / docx，且不超过 10MB'}`
    })
    scrollBottom()
  } finally {
    uploading.value = false
  }
}

async function rewriteDoc() {
  if (!attachment.value || rewriting.value) return
  rewriting.value = true
  const instruction = input.value.trim() || '优化结构与表达，突出重点与量化成果，保持事实真实'
  messages.value.push({ role: 'user', content: `【附件修改】${attachment.value.fileName}\n要求：${instruction}` })
  input.value = ''
  showQuick.value = false
  scrollBottom()
  try {
    const r = await api.aiRewrite({
      fileName: attachment.value.fileName,
      content: attachment.value.text,
      instruction
    })
    result.value = {
      content: r?.content || '',
      notes: r?.notes || '',
      fileName: r?.fileName || '修改稿.md',
      ai: !!r?.ai
    }
    resultVisible.value = true
    messages.value.push({
      role: 'assistant',
      content: r?.ai
        ? `修改稿已生成（${result.value.fileName}），可在弹窗中查看、复制或下载。\n\n修改说明：\n${(r.notes || '').slice(0, 200)}`
        : (r?.notes || 'AI 暂不可用，未生成修改稿，请稍后重试。')
    })
  } catch (err) {
    messages.value.push({ role: 'assistant', content: `修改失败：${err?.message || '请稍后重试'}` })
  } finally {
    rewriting.value = false
    scrollBottom()
  }
}

function downloadResult() {
  const blob = new Blob([result.value.content || ''], { type: 'text/markdown;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = result.value.fileName || '修改稿.md'
  document.body.appendChild(a)
  a.click()
  a.remove()
  setTimeout(() => URL.revokeObjectURL(a.href), 1000)
}

async function copyResult() {
  try {
    await navigator.clipboard.writeText(result.value.content || '')
  } catch {
    /* 剪贴板不可用时忽略 */
  }
}

function scrollBottom() {
  nextTick(() => {
    const el = bodyEl.value
    if (el) el.scrollTop = el.scrollHeight
  })
}
</script>

<style scoped>
.ai-helper { position: fixed; right: 24px; bottom: 24px; z-index: 9999; }

/* 悬浮按钮 */
.fab {
  display: flex; align-items: center; gap: 8px;
  height: 48px; padding: 0 18px; border-radius: 24px;
  background: var(--brand); color: #fff; cursor: pointer;
  box-shadow: 0 8px 24px rgba(0, 190, 189, .35);
  transition: transform .2s;
}
.fab:hover { transform: translateY(-2px); }
.fab-icon { font-size: 20px; }
.fab-text { font-size: 14px; font-weight: 600; }

/* 面板 */
.panel {
  width: 400px; height: 580px; max-width: calc(100vw - 32px); max-height: calc(100vh - 120px);
  display: flex; flex-direction: column;
  background: #fff; border-radius: 14px; overflow: hidden;
  box-shadow: 0 12px 40px rgba(31, 35, 41, .18);
}
.phead {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 14px; background: var(--brand); color: #fff;
}
.ptitle { display: flex; align-items: center; gap: 10px; }
.avatar {
  width: 32px; height: 32px; border-radius: 50%; background: rgba(255, 255, 255, .22);
  display: flex; align-items: center; justify-content: center; font-size: 18px;
}
.pmeta { display: flex; flex-direction: column; line-height: 1.35; }
.pmeta b { font-size: 14px; }
.pmeta em { font-style: normal; font-size: 11px; opacity: .85; }
.pact { display: flex; gap: 2px; }
.pact :deep(.el-button) { color: #fff; }

.pbody { flex: 1; overflow-y: auto; padding: 14px; background: #f7f9fb; }
.msg { display: flex; margin-bottom: 12px; }
.msg.user { justify-content: flex-end; }
.bubble {
  max-width: 82%; padding: 10px 12px; border-radius: 10px;
  font-size: 13px; line-height: 1.6; white-space: pre-wrap; word-break: break-word;
}
.msg.ai .bubble { background: #fff; color: var(--ink-2); border-top-left-radius: 2px; }
.msg.user .bubble { background: var(--brand); color: #fff; border-top-right-radius: 2px; }

.typing { display: flex; align-items: center; gap: 4px; }
.typing i {
  width: 6px; height: 6px; border-radius: 50%; background: var(--ink-3);
  animation: blink 1.2s infinite ease-in-out;
}
.typing i:nth-child(2) { animation-delay: .2s; }
.typing i:nth-child(3) { animation-delay: .4s; }
@keyframes blink { 0%, 60%, 100% { opacity: .25; } 30% { opacity: 1; } }

.quick { display: flex; flex-wrap: wrap; gap: 6px; padding: 10px 14px 0; }
.qtag {
  padding: 4px 10px; font-size: 12px; color: var(--brand); cursor: pointer;
  border: 1px solid #cdeeed; border-radius: 12px; background: #f2fbfb;
}
.qtag:hover { background: var(--brand); color: #fff; }

/* 附件条 */
.attach {
  display: flex; align-items: center; gap: 6px; flex-wrap: wrap;
  margin: 8px 14px 0; padding: 7px 10px;
  background: #f2fbfb; border: 1px solid #cdeeed; border-radius: 8px;
  font-size: 12px; color: var(--ink-2);
}
.afile { cursor: pointer; font-weight: 600; color: var(--brand); max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ameta { color: var(--ink-3); margin-right: auto; }

.pfoot { display: flex; align-items: flex-end; gap: 8px; padding: 12px 14px; border-top: 1px solid var(--border); }
.pfoot .el-button { flex-shrink: 0; }
.hidden-file { display: none; }
.attach-btn { padding: 8px 10px; }

/* 弹窗 */
.dlg-meta { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; font-size: 13px; color: var(--ink-2); }
.doc {
  max-height: 420px; overflow: auto; margin: 0; padding: 12px 14px;
  background: #f7f9fb; border: 1px solid var(--border); border-radius: 8px;
  font-size: 13px; line-height: 1.7; white-space: pre-wrap; word-break: break-word;
}
.notes { margin-bottom: 10px; }
.notes b { font-size: 13px; color: var(--ink); }
.note-text {
  margin: 6px 0 0; padding: 10px 12px; background: #f2fbfb; border-radius: 8px;
  font-size: 12px; line-height: 1.7; white-space: pre-wrap; color: var(--ink-2);
}
</style>
