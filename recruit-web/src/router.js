import { createRouter, createWebHashHistory } from 'vue-router'
import { auth } from './store'
import Home from './pages/Home.vue'
import JobList from './pages/JobList.vue'
import JobDetail from './pages/JobDetail.vue'
import Publish from './pages/Publish.vue'
import MyResume from './pages/MyResume.vue'
import Deliveries from './pages/Deliveries.vue'
import Chat from './pages/Chat.vue'
import Match from './pages/Match.vue'
import Login from './pages/Login.vue'

const routes = [
  { path: '/', component: Home },
  { path: '/jobs', component: JobList },
  { path: '/job/:id', component: JobDetail },
  { path: '/match', component: Match },
  { path: '/login', component: Login },
  { path: '/publish', component: Publish, meta: { auth: true, role: 2 } },
  { path: '/resume', component: MyResume, meta: { auth: true } },
  { path: '/deliveries', component: Deliveries, meta: { auth: true } },
  { path: '/chat', component: Chat, meta: { auth: true } },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to) => {
  document.title = '直聘通 · 在线招聘求职平台'
  if (to.meta.auth && !auth.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.role && auth.role !== to.meta.role) {
    return { path: '/' }
  }
})

export default router
