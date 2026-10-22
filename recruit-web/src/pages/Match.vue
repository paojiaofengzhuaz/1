<template>
  <div class="page">
    <div class="container">
      <!-- 智能助手 -->
      <div class="card block">
        <h2 class="page-title">🤖 智能匹配助手</h2>
        <p class="muted" style="margin-top:-8px">描述你的技能或期望，例如：「我会 Java 和 SpringBoot，想在武汉找 15K 左右的后端岗位」</p>
        <div class="ask">
          <el-input v-model="question" type="textarea" :rows="3" placeholder="输入你的求职意向…" />
          <el-button type="primary" :loading="asking" @click="ask">开始咨询</el-button>
        </div>
        <div v-if="reply" class="reply">
          <div class="bubble">🤖 {{ reply }}</div>
          <div class="grid">
            <div v-for="j in assistantJobs" :key="j.id" class="jcard" @click="$router.push(`/job/${j.id}`)">
              <div class="jtop">
                <b>{{ j.title }}</b>
                <span class="salary">{{ j.salaryMin }}-{{ j.salaryMax }}K</span>
              </div>
              <div class="meta">{{ j.company }} · {{ j.city }} · {{ j.experience }} · {{ j.education }}</div>
              <div class="tags">
                <el-tag v-for="t in splitTags(j.tags)" :key="t" size="small" effect="plain">{{ t }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 我的匹配 -->
      <div class="card block">
        <div class="head">
          <h2 class="page-title">我的岗位匹配</h2>
          <div>
            <el-button type="primary" :loading="matching" @click="match">基于我的简历匹配</el-button>
            <el-button v-if="!auth.token" plain @click="$router.push('/login')">登录后使用</el-button>
          </div>
        </div>
        <el-alert v-if="needResume" type="warning" show-icon :closable="false"
                  title="还没有简历，请先完善简历后再进行匹配" style="margin-bottom:14px" />
        <div v-if="!items.length && !matching" class="muted" style="padding:10px 0">
          点击「基于我的简历匹配」查看结果（技能命中越多、城市薪资越契合，匹配度越高）
        </div>
        <div class="mwrap">
          <div v-for="(it, i) in items" :key="it.job.id" class="mitem">
            <div class="mrank">{{ i + 1 }}</div>
            <div class="mbody">
              <div class="jtop">
                <b class="jtitle" @click="$router.push(`/job/${it.job.id}`)">{{ it.job.title }}</b>
                <span class="salary">{{ it.job.salaryMin }}-{{ it.job.salaryMax }}K</span>
              </div>
              <div class="meta">{{ it.job.company }} · {{ it.job.city }} · {{ it.job.experience }} · {{ it.job.education }}</div>
              <div class="reasons">
                <el-tag v-for="r in it.reasons" :key="r" size="small" type="success" effect="plain">{{ r }}</el-tag>
              </div>
            </div>
            <div class="mscore">
              <el-progress type="circle" :percentage="it.score" :width="64" :color="scoreColor(it.score)" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { auth } from '../store'

const question = ref('')
const asking = ref(false)
const reply = ref('')
const assistantJobs = ref([])
const items = ref([])
const matching = ref(false)
const needResume = ref(false)

function splitTags(tags) {
  return (tags || '').split(/[,，]/).filter(Boolean).slice(0, 5)
}

function scoreColor(score) {
  if (score >= 80) return '#00b42a'
  if (score >= 60) return '#00bebd'
  return '#ff8a00'
}

async function ask() {
  if (!question.value.trim()) return ElMessage.warning('请先描述你的求职意向')
  asking.value = true
  try {
    const r = await api.matchAssistant(question.value)
    reply.value = r.reply
    assistantJobs.value = r.jobs || []
  } finally {
    asking.value = false
  }
}

async function match() {
  if (!auth.token) return ElMessage.warning('请先登录')
  matching.value = true
  try {
    items.value = (await api.matchJobs()) || []
    needResume.value = false
    if (!items.value.length) ElMessage.info('暂无匹配结果')
  } catch (e) {
    if (String(e?.message || '').includes('简历')) {
      needResume.value = true
    }
  } finally {
    matching.value = false
  }
}
</script>

<style scoped>
.block { padding: 24px 26px; margin-bottom: 18px; }
.ask { display: flex; gap: 12px; align-items: flex-start; margin: 14px 0; }
.reply { margin-top: 8px; }
.bubble { background: #f2fbfb; border-left: 3px solid var(--brand); padding: 12px 16px; border-radius: 8px; color: var(--ink-2); margin-bottom: 14px; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 12px; }
.jcard { border: 1px solid var(--border); border-radius: 10px; padding: 14px; cursor: pointer; transition: all .2s; }
.jcard:hover { border-color: var(--brand); box-shadow: 0 6px 16px rgba(0, 190, 189, .12); }
.jtop { display: flex; justify-content: space-between; align-items: baseline; gap: 10px; }
.meta { color: var(--ink-3); font-size: 12px; margin: 6px 0; }
.tags { display: flex; gap: 6px; flex-wrap: wrap; }
.head { display: flex; justify-content: space-between; align-items: center; }
.mwrap { display: flex; flex-direction: column; gap: 12px; margin-top: 10px; }
.mitem { display: flex; gap: 14px; align-items: center; border: 1px solid var(--border); border-radius: 10px; padding: 14px 16px; }
.mrank { width: 28px; height: 28px; border-radius: 50%; background: #e6fafa; color: var(--brand); font-weight: 700; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.mbody { flex: 1; min-width: 0; }
.jtitle { cursor: pointer; }
.jtitle:hover { color: var(--brand); }
.reasons { display: flex; gap: 6px; flex-wrap: wrap; margin-top: 6px; }
.mscore { flex-shrink: 0; }
</style>
