import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { useUserStore } from '../store/user'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    const currentPath = router.currentRoute.value?.path || ''
    const scope = currentPath.startsWith('/admin') ? 'admin' : 'user'
    config._authScope = scope
    const token = scope === 'admin' ? userStore.adminToken : userStore.token
    if (token) {
      config.headers['Authorization'] = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    } else if (res.code === 401) {
      ElMessage.error(res.msg || '请先登录')
      const userStore = useUserStore()
      const scope = response.config?._authScope || (((router.currentRoute.value?.path || '').startsWith('/admin')) ? 'admin' : 'user')
      if (scope === 'admin') {
        userStore.clearAdminToken()
        router.replace('/admin/login')
      } else {
        userStore.clearToken()
        userStore.showLoginDialog = true
        const currentPath = router.currentRoute.value?.path || ''
        const authRequiredPages = ['/user']
        if (authRequiredPages.some(path => currentPath.startsWith(path))) {
          router.replace('/')
        }
      }
      return Promise.reject(new Error(res.msg))
    } else {
      ElMessage.error(res.msg || '系统错误')
      return Promise.reject(new Error(res.msg || 'Error'))
    }
  },
  error => {
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default request
