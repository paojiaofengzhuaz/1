<template>
  <div class="page">
    <div class="container">
      <div class="card wrap">
        <div class="head">
          <h2 class="page-title">我的简历</h2>
          <span class="muted">完善的简历能被智能匹配推荐给更多合适的职位</span>
        </div>

        <el-alert v-if="!loaded && !resumeId" type="warning" :closable="false" show-icon style="margin-bottom:16px"
                  title="还没有简历，填写后保存即可" />

        <el-form :model="form" label-width="90px">
          <el-row :gutter="18">
            <el-col :span="8">
              <el-form-item label="姓名" required>
                <el-input v-model="form.name" placeholder="请输入姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="性别">
                <el-radio-group v-model="form.gender">
                  <el-radio value="男">男</el-radio>
                  <el-radio value="女">女</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="年龄">
                <el-input-number v-model="form.age" :min="16" :max="65" controls-position="right" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="手机号">
                <el-input v-model="form.phone" placeholder="联系电话" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="邮箱">
                <el-input v-model="form.email" placeholder="联系邮箱" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="学历">
                <el-select v-model="form.education">
                  <el-option v-for="e in educations" :key="e" :label="e" :value="e" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="工作年限">
                <el-input-number v-model="form.experienceYears" :min="0" :max="40" controls-position="right" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="期望城市">
                <el-select v-model="form.city">
                  <el-option v-for="c in cities" :key="c" :label="c" :value="c" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="期望薪资">
                <el-input-number v-model="form.salary" :min="1" :max="200" controls-position="right" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="技能">
                <el-input v-model="form.skills" placeholder="逗号分隔，如：Java,SpringBoot,MySQL,Redis,Vue" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="自我介绍">
                <el-input v-model="form.selfIntro" type="textarea" :rows="4" placeholder="介绍你的项目经验与优势" />
              </el-form-item>
            </el-col>
          </el-row>
          <div class="actions">
            <el-button type="primary" :loading="saving" @click="save">保存简历</el-button>
            <el-button plain @click="$router.push('/match')">看看智能匹配</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'

const cities = ['北京', '上海', '广州', '深圳', '杭州', '武汉', '成都', '南京']
const educations = ['大专', '本科', '硕士', '博士']
const resumeId = ref(null)
const loaded = ref(true)
const saving = ref(false)
const form = reactive({
  name: '', gender: '男', age: 24, phone: '', email: '', education: '本科',
  experienceYears: 0, city: '武汉', salary: 10, skills: '', selfIntro: ''
})

function fill(r) {
  Object.keys(form).forEach((k) => {
    if (r[k] !== undefined && r[k] !== null) form[k] = r[k]
  })
}

async function load() {
  const r = await api.resumeMine()
  if (r) {
    resumeId.value = r.id
    fill(r)
  }
  loaded.value = false
}

async function save() {
  if (!form.name) return ElMessage.warning('请填写姓名')
  saving.value = true
  try {
    const r = await api.resumeSave({ ...form })
    resumeId.value = r.id
    ElMessage.success('简历已保存')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.wrap { padding: 24px 28px; }
.head { display: flex; align-items: baseline; gap: 14px; }
.actions { display: flex; gap: 12px; }
</style>
