<template>
  <div class="page">
    <div class="container">
      <div class="card wrap">
        <h2 class="page-title">{{ auth.role === 2 ? '收到的简历投递' : '我的投递记录' }}</h2>

        <el-table :data="list" empty-text="暂无投递记录" v-loading="loading">
          <el-table-column v-if="auth.role === 2" prop="seekerName" label="求职者" width="120" />
          <el-table-column prop="jobTitle" label="岗位" min-width="160" />
          <el-table-column prop="company" label="公司" width="140" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
                {{ statusMap[row.status]?.text || row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="投递时间" width="170">
            <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ').slice(0, 16) }}</template>
          </el-table-column>
          <el-table-column v-if="auth.role === 2" label="操作" width="200">
            <template #default="{ row }">
              <el-button size="small" type="primary" text @click="setStatus(row, 1)">已查看</el-button>
              <el-button size="small" type="success" text @click="setStatus(row, 2)">邀面试</el-button>
              <el-button size="small" type="danger" text @click="setStatus(row, 3)">不合适</el-button>
            </template>
          </el-table-column>
          <el-table-column v-else label="操作" width="110">
            <template #default="{ row }">
              <el-button size="small" text type="primary" @click="$router.push(`/job/${row.jobId}`)">查看岗位</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { auth } from '../store'

const list = ref([])
const loading = ref(true)
const statusMap = {
  0: { text: '待查看', type: 'info' },
  1: { text: '已查看', type: 'primary' },
  2: { text: '邀面试', type: 'success' },
  3: { text: '不合适', type: 'danger' }
}

async function load() {
  loading.value = true
  try {
    list.value = auth.role === 2 ? await api.deliveryReceived() : await api.deliveryMy()
  } finally {
    loading.value = false
  }
}

async function setStatus(row, status) {
  await api.deliveryStatus(row.id, status)
  ElMessage.success('已更新')
  load()
}

onMounted(load)
</script>

<style scoped>
.wrap { padding: 24px 28px; }
</style>
