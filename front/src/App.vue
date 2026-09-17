<template>
  <el-config-provider :locale="zhCn">
    <ErrorFallback>
      <router-view v-slot="{ Component, route }">
        <Transition name="fade" mode="out-in">
          <keep-alive include="Market">
            <component :is="Component" :key="route.path" />
          </keep-alive>
        </Transition>
      </router-view>
    </ErrorFallback>

    <LoginDialog />
    <RegisterDialog />
  </el-config-provider>
</template>

<script setup>
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import LoginDialog from './components/LoginDialog.vue'
import RegisterDialog from './components/RegisterDialog.vue'
import ErrorFallback from './components/ErrorFallback.vue'
</script>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Noto Sans', sans-serif;
}

/* 路由过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* NProgress 样式覆盖 */
#nprogress .bar {
  background: var(--color-primary, #ff6b81) !important;
  height: 3px !important;
}
#nprogress .peg {
  box-shadow: 0 0 10px var(--color-primary, #ff6b81), 0 0 5px var(--color-primary, #ff6b81) !important;
}
</style>