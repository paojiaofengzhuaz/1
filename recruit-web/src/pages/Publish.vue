<template>
  <div class="page">
    <div class="container">
      <div class="card form-card">
        <h2 class="page-title">发布职位</h2>
        <el-form :model="form" label-width="90px">
          <el-row :gutter="18">
            <el-col :span="12">
              <el-form-item label="职位名称" required>
                <el-input v-model="form.title" placeholder="如：Java 后端开发工程师" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="公司" required>
                <el-input v-model="form.company" :placeholder="auth.company || '公司名称'" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="所属行业" required>
                <el-select v-model="form.category" placeholder="请选择行业" filterable>
                  <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="城市" required>
                <el-select v-model="form.city" placeholder="请选择">
                  <el-option v-for="c in cities" :key="c" :label="c" :value="c" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="薪资(K)" required>
                <div class="salary-range">
                  <el-input-number v-model="form.salaryMin" :min="1" :max="100" controls-position="right" />
                  <span>-</span>
                  <el-input-number v-model="form.salaryMax" :min="1" :max="200" controls-position="right" />
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="经验要求">
                <el-select v-model="form.experience">
                  <el-option v-for="e in experiences" :key="e" :label="e" :value="e" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="学历要求">
                <el-select v-model="form.education">
                  <el-option v-for="e in educations" :key="e" :label="e" :value="e" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="技能标签">
                <el-input v-model="form.tags" placeholder="逗号分隔：Java,MySQL" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="职位描述">
                <el-input v-model="form.description" type="textarea" :rows="3" placeholder="工作内容、团队介绍等" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="任职要求">
                <el-input v-model="form.requirement" type="textarea" :rows="3" placeholder="技能、经验、学历等要求" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-button type="primary" :loading="saving" @click="submit">发布职位</el-button>
        </el-form>
      </div>

      <div class="card list-card">
        <h2 class="page-title">我发布的职位</h2>
        <el-table :data="myJobs" empty-text="还没有发布过职位">
          <el-table-column prop="title" label="职位" min-width="160" />
          <el-table-column prop="category" label="行业" width="110" />
          <el-table-column prop="city" label="城市" width="90" />
          <el-table-column label="薪资" width="110">
            <template #default="{ row }">{{ row.salaryMin }}-{{ row.salaryMax }}K</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '在招' : '已下线' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发布时间" width="170">
            <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ').slice(0, 16) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="130">
            <template #default="{ row }">
              <el-button v-if="row.status === 1" size="small" text type="danger" @click="offline(row)">下线</el-button>
              <el-button size="small" text @click="$router.push(`/job/${row.id}`)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { auth } from '../store'

const cities = ['北京', '上海', '广州', '深圳', '杭州', '武汉', '成都', '南京', '西安', '长沙', '郑州', '苏州', '重庆', '天津', '合肥', '青岛']
const educations = ['大专', '本科', '硕士', '博士', '在校/应届']
const experiences = ['在校/应届', '1-3年', '3-5年', '5-10年']
const categories = ['互联网/IT', '金融/银行', '教育培训', '医疗/医药', '制造/工业', '房地产/建筑',
  '销售/零售', '物流/交通', '传媒/设计', '餐饮/酒店', '法律/咨询', '人事/行政', '能源/环保', '汽车/机械', '政府/公共']

const form = reactive({
  title: '', company: auth.company || '', category: '', city: '武汉',
  salaryMin: 10, salaryMax: 20, education: '本科', experience: '1-3年',
  tags: '', description: '', requirement: '', hrName: auth.nickname || ''
})
const saving = ref(false)
const myJobs = ref([])

async function load() {
  myJobs.value = (await api.jobMy()) || []
}

async function submit() {
  if (!form.title) return ElMessage.warning('请填写职位名称')
  if (!form.company) return ElMessage.warning('请填写公司名称')
  if (!form.category) return ElMessage.warning('请选择所属行业')
  if (form.salaryMin > form.salaryMax) return ElMessage.warning('薪资下限不能大于上限')
  saving.value = true
  try {
    await api.jobPublish({ ...form })
    ElMessage.success('发布成功')
    form.title = ''
    form.tags = ''
    form.description = ''
    form.requirement = ''
    load()
  } finally {
    saving.value = false
  }
}

async function offline(row) {
  await api.jobOffline(row.id)
  ElMessage.success('已下线')
  load()
}

onMounted(load)
</script>

<style scoped>
.form-card { padding: 22px 26px; }
.list-card { margin-top: 18px; padding: 22px 26px; }
.salary-range { display: flex; align-items: center; gap: 8px; }
.salary-range .el-input-number { width: 110px; }
</style>
