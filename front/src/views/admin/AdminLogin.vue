<template>
  <div class="admin-login-container">
    <el-card class="admin-login-card">
      <div class="header">
        <div class="title">管理员登录</div>
        <div class="subtitle">校园闲置物品交易系统后台</div>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="studentNo">
          <el-input v-model="form.studentNo" placeholder="管理员账号" autocomplete="username" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password autocomplete="current-password" @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  studentNo: '',
  password: ''
})

const rules = reactive({
  studentNo: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

const handleLogin = () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const data = await request.post('/api/user/login', form)
      userStore.setAdminToken(data.token)
      const adminInfo = await request.get('/api/user/info')
      if (adminInfo?.studentNo !== 'admin') {
        userStore.clearAdminToken()
        ElMessage.error('无管理员权限')
        return
      }
      userStore.setAdminInfo(adminInfo)
      ElMessage.success('登录成功')
      router.replace('/admin')
    } catch (error) {
      ElMessage.error(error?.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}

onMounted(() => {
  if (userStore.adminToken && userStore.adminInfo?.studentNo === 'admin') {
    router.replace('/admin')
  }
})
</script>

<style scoped>
.admin-login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%);
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
}
.admin-login-card {
  width: 420px;
  border-radius: 16px;
  border: none;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.06);
}
.header {
  text-align: center;
  margin-bottom: 18px;
}
.title {
  font-size: 22px;
  font-weight: 800;
  color: #2c3e50;
  letter-spacing: 1px;
}
.subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: #95a5a6;
}
.login-btn {
  width: 100%;
  border-radius: 8px;
}
</style>
