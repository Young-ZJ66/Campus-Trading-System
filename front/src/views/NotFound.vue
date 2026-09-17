<template>
  <div class="not-found">
    <div class="content">
      <h1 class="code">404</h1>
      <p class="desc">页面走丢了</p>
      <p class="tip">你访问的页面不存在或已被移除</p>
      <el-button type="primary" @click="router.push('/')">返回首页</el-button>
      <el-button @click="router.back()">返回上一页</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--color-bg-page, #f5f7fa);
}
.content {
  text-align: center;
}
.code {
  font-size: 120px;
  font-weight: 900;
  color: var(--color-primary, #ff6b81);
  margin: 0;
  line-height: 1;
  background: linear-gradient(135deg, var(--color-primary-light, #ff9a9e), var(--color-primary, #ff6b81));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.desc {
  font-size: 24px;
  color: var(--color-text-body, #303133);
  margin: 20px 0 10px;
  font-weight: 600;
}
.tip {
  font-size: 14px;
  color: var(--color-text-muted, #95a5a6);
  margin-bottom: 30px;
}
</style>