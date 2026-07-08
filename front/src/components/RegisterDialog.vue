<template>
  <el-dialog v-model="userStore.showRegisterDialog" width="400px" :show-close="false" class="register-dialog" @close="resetForm">
    <div class="dialog-header">
      <el-icon class="logo-icon-svg"><Goods /></el-icon>
      <h2 class="title">加入校园闲置物品交易系统</h2>
      <p class="subtitle">注册账号，开启你的闲置之旅</p>
    </div>
    <el-form :model="registerForm" :rules="rules" ref="registerFormRef">
      <el-form-item prop="studentNo">
        <el-input v-model="registerForm.studentNo" placeholder="请输入学号">
          <template #prefix>
            <el-icon><User /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="nickname">
        <el-input v-model="registerForm.nickname" placeholder="请输入昵称">
          <template #prefix>
            <el-icon><UserFilled /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password>
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="phone">
        <el-input v-model="registerForm.phone" placeholder="请输入手机号">
          <template #prefix>
            <el-icon><Iphone /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleRegister" class="register-btn" :loading="loading">注册</el-button>
      </el-form-item>
      <div class="login-link">
        已有账号？<a href="javascript:void(0)" @click="goToLogin">返回登录</a>
      </div>
    </el-form>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock, UserFilled, Iphone, Goods } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  studentNo: '',
  nickname: '',
  password: '',
  phone: ''
})

const rules = reactive({
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

const handleRegister = () => {
  registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await request.post('/api/user/register', registerForm)
        ElMessage.success('注册成功，请登录')
        goToLogin()
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}

const goToLogin = () => {
  userStore.showRegisterDialog = false
  userStore.showLoginDialog = true
}

const resetForm = () => {
  if (registerFormRef.value) {
    registerFormRef.value.resetFields()
  }
}
</script>

<style scoped>
.register-dialog {
  border-radius: 16px;
}
.dialog-header {
  text-align: center;
  margin-bottom: 25px;
}
.logo-icon-svg {
  font-size: 40px;
  color: #ff6b81;
  margin-bottom: 10px;
}
.title {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  color: #2c3e50;
  letter-spacing: 1px;
}
.subtitle {
  margin: 5px 0 0;
  font-size: 13px;
  color: #95a5a6;
}
:deep(.el-input__wrapper) {
  padding: 8px 15px;
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e0e6ed inset;
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #ff6b81 inset;
}
.register-btn {
  width: 100%;
  padding: 12px;
  border-radius: 8px;
  background: linear-gradient(135deg, #ff9a9e, #ff6b81);
  border: none;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 2px;
  box-shadow: 0 4px 15px rgba(255, 107, 129, 0.3);
  transition: all 0.3s;
}
.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 129, 0.4);
}
.login-link {
  text-align: center;
  margin-top: 10px;
  font-size: 14px;
  color: #7f8c8d;
}
.login-link a {
  color: #ff6b81;
  text-decoration: none;
  font-weight: bold;
  margin-left: 5px;
}
</style>
