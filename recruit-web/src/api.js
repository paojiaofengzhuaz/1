import axios from 'axios'
import { ElMessage } from 'element-plus'
import { auth, clearAuth } from './store'

// 统一请求：经 Vite 代理 /api -> 网关 18090
const http = axios.create({ baseURL: '/api', timeout: 20000 })

http.interceptors.request.use((cfg) => {
  if (auth.token) cfg.headers.Authorization = auth.token
  return cfg
})

http.interceptors.response.use(
  (res) => {
    const data = res.data
    if (data && data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    return data ? data.data : null
  },
  (err) => {
    if (err.response && err.response.status === 401) {
      clearAuth()
      ElMessage.warning('请先登录')
      const cur = location.hash.slice(1) || '/'
      if (!cur.startsWith('/login')) {
        location.hash = `#/login?redirect=${encodeURIComponent(cur)}`
      }
      return Promise.reject(err)
    }
    ElMessage.error(err.response?.data?.message || '网络异常，请稍后再试')
    return Promise.reject(err)
  }
)

export const api = {
  // 用户
  register: (body) => http.post('/user/register', body),
  login: (body) => http.post('/user/login', body),
  logout: () => http.post('/user/logout'),
  userInfo: () => http.get('/user/info'),

  // 岗位
  jobList: (params) => http.get('/job/list', { params }),
  jobStats: () => http.get('/job/stats'),
  jobCompanies: (limit) => http.get('/job/companies', { params: { limit } }),
  jobHot: () => http.get('/job/hot'),
  jobDetail: (id) => http.get(`/job/${id}`),
  jobPublish: (body) => http.post('/job/publish', body),
  jobMy: () => http.get('/job/my'),
  jobOffline: (id) => http.post(`/job/offline/${id}`),

  // 简历
  resumeMine: () => http.get('/resume'),
  resumeSave: (body) => http.post('/resume/save', body),

  // 投递
  deliverySend: (body) => http.post('/delivery/send', body),
  deliveryMy: () => http.get('/delivery/my'),
  deliveryReceived: () => http.get('/delivery/received'),
  deliveryStatus: (id, status) => http.post('/delivery/status', null, { params: { id, status } }),

  // 沟通
  imStart: (body) => http.post('/im/start', body),
  imConversations: () => http.get('/im/conversations'),
  imMessages: (convId) => http.get('/im/messages', { params: { convId } }),
  imSend: (body) => http.post('/im/send', body),
  imRead: (convId) => http.post('/im/read', null, { params: { convId } }),

  // 智能匹配
  matchJobs: () => http.get('/match/jobs'),
  matchAssistant: (q) => http.get('/match/assistant', { params: { q } }),

  // AI 求职小助手（多轮对话，大模型响应较慢，单独放宽超时）
  aiChat: (messages) => http.post('/match/ai/chat', { messages }, { timeout: 60000 }),
  // 上传附件（txt/md/pdf/docx），解析为纯文本
  aiUpload: (file) => {
    const fd = new FormData()
    fd.append('file', file)
    return http.post('/match/ai/upload', fd, { timeout: 60000 })
  },
  // 按修改要求生成修改稿
  aiRewrite: (body) => http.post('/match/ai/rewrite', body, { timeout: 120000 })
}
