import { defineStore } from 'pinia'
import { ref } from 'vue'

const safeParse = (key) => {
  try {
    return JSON.parse(localStorage.getItem(key) || '{}')
  } catch {
    localStorage.removeItem(key)
    return {}
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(safeParse('userInfo'))
  const adminToken = ref(localStorage.getItem('adminToken') || '')
  const adminInfo = ref(safeParse('adminInfo'))

  // 登录注册弹窗状态
  const showLoginDialog = ref(false)
  const showRegisterDialog = ref(false)

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  const setAdminToken = (newToken) => {
    adminToken.value = newToken
    localStorage.setItem('adminToken', newToken)
  }

  const setAdminInfo = (info) => {
    adminInfo.value = info
    localStorage.setItem('adminInfo', JSON.stringify(info))
  }

  const clearToken = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  const clearAdminToken = () => {
    adminToken.value = ''
    adminInfo.value = {}
    localStorage.removeItem('adminToken')
    localStorage.removeItem('adminInfo')
  }

  return {
    token,
    userInfo,
    adminToken,
    adminInfo,
    showLoginDialog,
    showRegisterDialog,
    setToken,
    setUserInfo,
    setAdminToken,
    setAdminInfo,
    clearToken,
    clearAdminToken
  }
})
