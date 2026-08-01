<template>
  <el-dialog v-model="userStore.showLoginDialog" width="400px" :show-close="true" class="login-dialog" @close="resetForm">
    <div class="dialog-header">
      <el-icon class="logo-icon-svg"><Goods /></el-icon>
      <h2 class="title">校园闲置物品交易系统</h2>
      <p class="subtitle">让你的闲置物品再次发光</p>
    </div>
    <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
      <el-form-item prop="studentNo">
        <el-input v-model="loginForm.studentNo" placeholder="请输入学号/手机号">
          <template #prefix>
            <el-icon><User /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password @keyup.enter="handleLogin">
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleLogin" class="login-btn" :loading="loading">登录</el-button>
      </el-form-item>
      <div class="register-link">
        还没有账号？<a href="javascript:void(0)" @click="goToRegister">立即注册</a>
      </div>
    </el-form>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock, Goods } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  studentNo: '',
  password: ''
})

const rules = reactive({
  studentNo: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

const handleLogin = () => {
  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const data = await request.post('/api/user/login', loginForm)
        userStore.setToken(data.token)
        
        // 获取用户信息
        const userInfo = await request.get('/api/user/info')
        userStore.setUserInfo(userInfo)
        
        ElMessage.success('登录成功')
        userStore.showLoginDialog = false
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}

const goToRegister = () => {
  userStore.showLoginDialog = false
  userStore.showRegisterDialog = true
}

const resetForm = () => {
  if (loginFormRef.value) {
    loginFormRef.value.resetFields()
  }
}
</script>

<style scoped>
.login-dialog {
  border-radius: var(--radius-lg);
}
.dialog-header {
  text-align: center;
  margin-bottom: 25px;
}
.logo-icon-svg {
  font-size: 40px;
  color: var(--color-primary);
  margin-bottom: 10px;
}
.title {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  color: var(--color-text-body);
  letter-spacing: 1px;
}
.subtitle {
  margin: 5px 0 0;
  font-size: 13px;
  color: #95a5a6;
}
:deep(.el-input__wrapper) {
  padding: 8px 15px;
  border-radius: var(--radius-sm);
  box-shadow: 0 0 0 1px #e0e6ed inset;
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}
.login-btn {
  width: 100%;
  padding: 12px;
  border-radius: var(--radius-sm);
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-primary));
  border: none;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 2px;
  box-shadow: 0 4px 15px var(--color-primary-shadow);
  transition: all 0.3s;
}
.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 129, 0.4);
}
.register-link {
  text-align: center;
  margin-top: 10px;
  font-size: 14px;
  color: #7f8c8d;
}
.register-link a {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: bold;
  margin-left: 5px;
}
</style>
