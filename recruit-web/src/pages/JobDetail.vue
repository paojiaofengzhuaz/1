<template>
  <div class="page">
    <div class="container">
      <el-skeleton v-if="loading" :rows="6" animated />
      <template v-else-if="job">
        <div class="headcard card">
          <div class="hleft">
            <h1>{{ job.title }} <span class="salary">{{ job.salaryMin }}-{{ job.salaryMax }}K</span></h1>
            <div class="meta">
              <el-tag v-if="job.category" size="small" type="success" effect="plain">{{ job.category }}</el-tag>
              <span>{{ job.company }}</span>
              <el-divider direction="vertical" />
              <span>{{ job.city }}</span>
              <el-divider direction="vertical" />
              <span>{{ job.experience }}</span>
              <el-divider direction="vertical" />
              <span>{{ job.education }}</span>
            </div>
            <div class="tags">
              <el-tag v-for="t in splitTags(job.tags)" :key="t" effect="plain">{{ t }}</el-tag>
            </div>
          </div>
          <div class="hactions">
            <el-button type="primary" size="large" @click="chat">立即沟通</el-button>
            <el-button size="large" @click="deliver">投递简历</el-button>
          </div>
        </div>

        <div class="card block">
          <h3>职位描述</h3>
          <p class="text">{{ job.description || '暂无描述' }}</p>
        </div>
        <div class="card block">
          <h3>任职要求</h3>
          <p class="text">{{ job.requirement || '暂无要求' }}</p>
        </div>
        <div class="card block">
          <h3>招聘官</h3>
          <div class="hr">
            <el-avatar :size="42" style="background:#00bebd">{{ (job.hrName || 'H').slice(0, 1) }}</el-avatar>
            <div>
              <b>{{ job.hrName }}</b>
              <p class="muted">{{ job.company }} · 在线沟通更高效</p>
            </div>
            <el-button type="primary" plain style="margin-left:auto" @click="chat">和 TA 聊聊</el-button>
          </div>
        </div>
      </template>
      <el-empty v-else description="岗位不存在或已下线" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import { auth } from '../store'

const route = useRoute()
const router = useRouter()
const job = ref(null)
const loading = ref(true)

function splitTags(tags) {
  return (tags || '').split(/[,，]/).filter(Boolean)
}

function requireLogin() {
  if (!auth.token) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return false
  }
  return true
}

async function chat() {
  if (!requireLogin()) return
  if (auth.role === 2) return ElMessage.warning('招聘者账号不能主动沟通，请用求职者账号体验')
  const conv = await api.imStart({ toId: job.value.hrId, jobId: job.value.id, jobTitle: job.value.title })
  router.push({ path: '/chat', query: { convId: conv.id } })
}

async function deliver() {
  if (!requireLogin()) return
  if (auth.role === 2) return ElMessage.warning('请用求职者账号投递简历')
  try {
    await ElMessageBox.confirm(`确定投递「${job.value.title}」吗？`, '投递简历', { type: 'info' })
  } catch {
    return
  }
  await api.deliverySend({
    jobId: job.value.id,
    jobTitle: job.value.title,
    company: job.value.company,
    hrId: job.value.hrId
  })
  ElMessage.success('投递成功，可在「投递」中查看进度')
}

onMounted(async () => {
  try {
    job.value = await api.jobDetail(route.params.id)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.headcard { display: flex; justify-content: space-between; gap: 20px; padding: 24px; flex-wrap: wrap; }
.hleft h1 { margin: 0 0 10px; font-size: 24px; display: flex; align-items: baseline; gap: 14px; flex-wrap: wrap; }
.meta { color: var(--ink-2); display: flex; align-items: center; flex-wrap: wrap; margin-bottom: 10px; }
.tags { display: flex; gap: 8px; flex-wrap: wrap; }
.hactions { display: flex; flex-direction: column; gap: 10px; justify-content: center; }
.block { margin-top: 16px; padding: 20px 24px; }
.block h3 { margin: 0 0 10px; font-size: 16px; }
.text { margin: 0; color: var(--ink-2); line-height: 1.9; white-space: pre-wrap; }
.hr { display: flex; align-items: center; gap: 12px; }
.hr p { margin: 2px 0 0; }
</style>
