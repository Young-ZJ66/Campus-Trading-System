<template>
  <div v-if="error" class="error-fallback">
    <div class="error-content">
      <el-icon class="error-icon" :size="48"><WarningFilled /></el-icon>
      <h2>页面出了点问题</h2>
      <p class="error-msg">{{ error.message || '未知错误' }}</p>
      <div class="error-actions">
        <el-button type="primary" @click="handleRetry">重试</el-button>
        <el-button @click="router.push('/')">返回首页</el-button>
      </div>
    </div>
  </div>
  <slot v-else />
</template>

<script setup>
import { ref, onErrorCaptured, provide } from 'vue'
import { useRouter } from 'vue-router'
import { WarningFilled } from '@element-plus/icons-vue'

const router = useRouter()
const error = ref(null)

onErrorCaptured((err) => {
  error.value = err
  return false
})

const handleRetry = () => {
  error.value = null
}

provide('resetError', () => {
  error.value = null
})
</script>

<style scoped>
.error-fallback {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--color-bg-page, #f5f7fa);
}
.error-content {
  text-align: center;
  padding: 40px;
}
.error-icon {
  color: var(--color-warning, #e6a23c);
  margin-bottom: 20px;
}
.error-content h2 {
  color: var(--color-text-body, #303133);
  margin-bottom: 10px;
}
.error-msg {
  color: var(--color-text-muted, #95a5a6);
  font-size: 14px;
  margin-bottom: 24px;
}
.error-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>