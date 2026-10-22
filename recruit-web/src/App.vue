<template>
  <div class="shell">
    <header class="topbar">
      <div class="container bar">
        <router-link to="/" class="logo">
          <span class="dot">直</span>
          <b>直聘通</b>
          <em>BOSS 风格在线招聘</em>
        </router-link>
        <nav class="nav">
          <router-link to="/" class="link" exact-active-class="on">首页</router-link>
          <router-link to="/jobs" class="link" active-class="on">职位</router-link>
          <router-link to="/match" class="link" active-class="on">智能匹配</router-link>
        </nav>
        <div class="right">
          <template v-if="auth.token">
            <router-link v-if="auth.role === 2" to="/publish" class="link">发布职位</router-link>
            <router-link to="/chat" class="link">消息</router-link>
            <router-link to="/deliveries" class="link">投递</router-link>
            <router-link v-if="auth.role === 1" to="/resume" class="link">我的简历</router-link>
            <el-dropdown @command="onCommand">
              <span class="user">
                <el-avatar :size="28" style="background:#00bebd">{{ (auth.nickname || 'U').slice(0, 1) }}</el-avatar>
                <span class="uname">{{ auth.nickname || '用户' }}</span>
                <el-tag size="small" :type="auth.role === 2 ? 'warning' : 'success'" effect="plain">
                  {{ auth.role === 2 ? '招聘者' : '求职者' }}
                </el-tag>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <div class="entry" @click="$router.push('/login')">
              <b>我要招聘</b>
              <span>人才匹配度高</span>
            </div>
            <div class="entry" @click="$router.push('/login')">
              <b>我要找工作</b>
              <span>各大行业职位任选</span>
            </div>
            <el-button size="small" type="primary" plain @click="$router.push('/login')">登录 / 注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <main class="main">
      <router-view />
    </main>

    <footer class="footer">
      直聘通 · 基于 Spring Cloud Alibaba + Vue3 + Element Plus 的在线招聘求职平台（课程实验项目）
    </footer>

    <!-- 全局 AI 求职小助手 -->
    <AiHelper />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auth, clearAuth } from './store'
import { api } from './api'
import AiHelper from './components/AiHelper.vue'

const router = useRouter()

async function onCommand(cmd) {
  if (cmd === 'profile') {
    router.push(auth.role === 2 ? '/publish' : '/resume')
    return
  }
  if (cmd === 'logout') {
    try {
      await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
    } catch {
      return
    }
    try {
      await api.logout()
    } catch {
      /* 忽略 */
    }
    clearAuth()
    ElMessage.success('已退出登录')
    router.push('/')
  }
}
</script>

<style scoped>
.shell { min-height: 100vh; display: flex; flex-direction: column; }
.topbar {
  position: sticky; top: 0; z-index: 100;
  background: #fff; border-bottom: 1px solid var(--border);
}
.bar { display: flex; align-items: center; height: 60px; gap: 28px; }
.logo { display: flex; align-items: center; gap: 8px; }
.logo .dot {
  width: 28px; height: 28px; border-radius: 8px; background: var(--brand);
  color: #fff; font-weight: 700; display: flex; align-items: center; justify-content: center;
}
.logo b { font-size: 18px; }
.logo em { font-style: normal; font-size: 12px; color: var(--ink-3); }
.nav { display: flex; gap: 6px; flex: 1; }
.link { padding: 6px 12px; border-radius: 8px; color: var(--ink-2); font-size: 14px; }
.link:hover { background: #f2fbfb; color: var(--brand); }
.link.on { background: #e6fafa; color: var(--brand); font-weight: 600; }
.right { display: flex; align-items: center; gap: 6px; }
.entry { display: flex; flex-direction: column; gap: 1px; padding: 5px 10px; border-radius: 8px; cursor: pointer; line-height: 1.25; }
.entry:hover { background: #f2fbfb; }
.entry:hover b { color: var(--brand); }
.entry b { font-size: 13px; font-weight: 600; color: var(--ink); }
.entry span { font-size: 11px; color: var(--ink-3); }
.user { display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 4px 8px; border-radius: 8px; }
.user:hover { background: #f6f7f9; }
.uname { font-size: 14px; color: var(--ink); }
.main { flex: 1; }
.footer { padding: 20px; text-align: center; color: var(--ink-3); font-size: 12px; }
</style>
