<template>
  <div class="page">
    <div class="box card">
      <div class="brand">
        <span class="dot">直</span>
        <div>
          <h2>直聘通</h2>
          <p>找工作 · 直接和招聘官谈</p>
        </div>
      </div>

      <el-tabs v-model="tab">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" label-position="top" @submit.prevent>
            <el-form-item label="用户名">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" @keyup.enter="doLogin" />
            </el-form-item>
            <el-button type="primary" class="full" :loading="loading" @click="doLogin">登 录</el-button>
          </el-form>
          <div class="demo">
            <span>演示账号：</span>
            <el-tag size="small" @click="fill('demo')">求职者 demo / 123456</el-tag>
            <el-tag size="small" type="warning" @click="fill('hr001')">招聘者 hr001 / 123456</el-tag>
          </div>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form :model="regForm" label-position="top">
            <el-form-item label="身份">
              <el-radio-group v-model="regForm.role">
                <el-radio :value="1">我是求职者</el-radio>
                <el-radio :value="2">我是招聘者</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="用户名（3-20位）">
              <el-input v-model="regForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码（至少6位）">
              <el-input v-model="regForm.password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="regForm.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="regForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <template v-if="regForm.role === 2">
              <el-form-item label="公司名称">
                <el-input v-model="regForm.company" placeholder="请输入公司名称" />
              </el-form-item>
              <el-form-item label="职位头衔">
                <el-input v-model="regForm.position" placeholder="如：招聘经理" />
              </el-form-item>
            </template>
            <el-button type="primary" class="full" :loading="loading" @click="doRegister">注册并登录</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { saveAuth } from '../store'

const route = useRoute()
const router = useRouter()
const tab = ref('login')
const loading = ref(false)

const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', nickname: '', phone: '', role: 1, company: '', position: '' })

function fill(name) {
  tab.value = 'login'
  loginForm.username = name
  loginForm.password = '123456'
}

function afterLogin(data) {
  saveAuth(data)
  ElMessage.success('欢迎回来，' + (data.nickname || data.userId))
  router.replace(route.query.redirect || '/')
}

async function doLogin() {
  if (!loginForm.username || !loginForm.password) return ElMessage.warning('请输入用户名和密码')
  loading.value = true
  try {
    afterLogin(await api.login({ ...loginForm }))
  } finally {
    loading.value = false
  }
}

async function doRegister() {
  if (!regForm.username || !regForm.password) return ElMessage.warning('请填写用户名和密码')
  loading.value = true
  try {
    await api.register({ ...regForm })
    ElMessage.success('注册成功，正在登录…')
    afterLogin(await api.login({ username: regForm.username, password: regForm.password }))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.box { max-width: 440px; margin: 40px auto; padding: 28px 30px; }
.brand { display: flex; align-items: center; gap: 12px; margin-bottom: 18px; }
.brand .dot {
  width: 44px; height: 44px; border-radius: 12px; background: var(--brand);
  color: #fff; font-size: 22px; font-weight: 700; display: flex; align-items: center; justify-content: center;
}
.brand h2 { margin: 0; font-size: 20px; }
.brand p { margin: 2px 0 0; font-size: 12px; color: var(--ink-3); }
.full { width: 100%; }
.demo { margin-top: 14px; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; font-size: 12px; color: var(--ink-3); }
.demo .el-tag { cursor: pointer; }
</style>
