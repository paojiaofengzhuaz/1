import { reactive } from 'vue'

// 登录态（localStorage 持久化）
const KEYS = {
  token: 'recruit_token',
  userId: 'recruit_userId',
  role: 'recruit_role',
  nickname: 'recruit_nickname',
  company: 'recruit_company'
}

export const auth = reactive({
  token: localStorage.getItem(KEYS.token) || '',
  userId: localStorage.getItem(KEYS.userId) || '',
  role: Number(localStorage.getItem(KEYS.role) || 0),
  nickname: localStorage.getItem(KEYS.nickname) || '',
  company: localStorage.getItem(KEYS.company) || ''
})

export function saveAuth(data) {
  auth.token = data.token
  auth.userId = String(data.userId)
  auth.role = data.role
  auth.nickname = data.nickname || ''
  auth.company = data.company || ''
  localStorage.setItem(KEYS.token, auth.token)
  localStorage.setItem(KEYS.userId, auth.userId)
  localStorage.setItem(KEYS.role, String(auth.role))
  localStorage.setItem(KEYS.nickname, auth.nickname)
  localStorage.setItem(KEYS.company, auth.company)
}

export function clearAuth() {
  auth.token = ''
  auth.userId = ''
  auth.role = 0
  auth.nickname = ''
  auth.company = ''
  Object.values(KEYS).forEach((k) => localStorage.removeItem(k))
}
