<template>
  <el-header class="navbar">
    <div class="navbar-inner">
      <div class="logo" @click="goHome">
        <el-icon class="logo-icon-svg"><Goods /></el-icon>
        <span class="logo-text">校园闲置物品交易系统</span>
      </div>

      <!-- 桌面端导航菜单 -->
      <div class="nav-menu desktop-only">
        <el-menu mode="horizontal" :default-active="activeIndex" @select="handleSelect" :ellipsis="false">
          <el-menu-item index="home">首页</el-menu-item>
          <el-menu-item index="market">闲置市场</el-menu-item>
          <el-menu-item index="news">校园资讯</el-menu-item>
          <el-menu-item index="point">积分商城</el-menu-item>
          <el-menu-item index="user">个人中心</el-menu-item>
        </el-menu>
      </div>

      <div class="user-info">
        <!-- 主题切换 -->
        <el-icon class="theme-toggle" @click="toggleTheme" :title="theme === 'dark' ? '切换亮色' : '切换暗色'">
          <Moon v-if="theme === 'light'" />
          <Sunny v-else />
        </el-icon>

        <!-- 通知铃铛 -->
        <template v-if="userStore.token">
          <NotificationPanel
            :notifications="wsNotifications"
            :unread-count="wsUnreadCount"
            @clear="clearNotifications"
            @click="handleNotificationClick"
          />
        </template>

        <!-- 发布闲置按钮 -->
        <el-button type="primary" round class="publish-btn desktop-only" @click="openPublishDialog">
          <el-icon><Plus /></el-icon> 发布闲置
        </el-button>

        <!-- 用户信息下拉菜单 -->
        <template v-if="userStore.token">
          <el-dropdown @command="handleCommand" class="user-dropdown">
            <span class="nickname el-dropdown-link">
              {{ userStore.userInfo.nickname || userStore.userInfo.studentNo }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="point" class="point-item">
                  <el-icon style="margin-right: 4px;"><Coin /></el-icon>积分: {{ userStore.userInfo.points || 0 }}
                </el-dropdown-item>
                <el-dropdown-item divided command="logout" class="logout-item">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="text" @click="userStore.showLoginDialog = true" class="login-text-btn">登录</el-button>
        </template>

        <!-- 移动端汉堡菜单 -->
        <el-icon class="hamburger mobile-only" @click="mobileMenuVisible = !mobileMenuVisible">
          <Expand v-if="!mobileMenuVisible" />
          <Close v-else />
        </el-icon>
      </div>
    </div>

    <!-- 移动端侧边菜单 -->
    <Transition name="slide-down">
      <div v-if="mobileMenuVisible" class="mobile-menu mobile-only">
        <div class="mobile-menu-item" v-for="item in menuItems" :key="item.index"
             :class="{ active: activeIndex === item.index }"
             @click="handleMobileSelect(item.index)">
          {{ item.label }}
        </div>
        <div class="mobile-menu-item publish-mobile" @click="openPublishDialog">
          <el-icon><Plus /></el-icon> 发布闲置
        </div>
      </div>
    </Transition>

    <!-- 挂载全局发布弹窗 -->
    <PublishDialog ref="publishDialogRef" @success="handlePublishSuccess" />
  </el-header>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus, ArrowDown, SwitchButton, Goods, Coin, Expand, Close, Moon, Sunny } from '@element-plus/icons-vue'
import request from '../utils/request'
import PublishDialog from './PublishDialog.vue'
import NotificationPanel from './NotificationPanel.vue'
import { useWebSocket } from '../composables/useWebSocket'
import { useTheme } from '../composables/useTheme'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const publishDialogRef = ref(null)
const mobileMenuVisible = ref(false)

// 主题切换
const { theme, toggleTheme } = useTheme()

// WebSocket 实时通知
const {
  notifications: wsNotifications,
  unreadCount: wsUnreadCount,
  connect: wsConnect,
  disconnect: wsDisconnect,
  clearUnread
} = useWebSocket()

// 登录后连接 WebSocket，退出时断开
watch(() => userStore.token, (token) => {
  if (token) {
    wsConnect()
  } else {
    wsDisconnect()
  }
}, { immediate: true })

onUnmounted(() => {
  wsDisconnect()
})

const clearNotifications = () => {
  clearUnread()
}

const handleNotificationClick = (item) => {
  if (item.relatedId) {
    if (item.type === 'COMMENT_ADDED') {
      router.push('/goods/' + item.relatedId)
    } else {
      router.push('/user?tab=sell')
    }
  }
}

const menuItems = [
  { index: 'home', label: '首页' },
  { index: 'market', label: '闲置市场' },
  { index: 'news', label: '校园资讯' },
  { index: 'point', label: '积分商城' },
  { index: 'user', label: '个人中心' }
]

const activeIndex = computed(() => {
  const path = route.path
  if (path === '/') return 'home'
  if (path.startsWith('/market')) return 'market'
  if (path.startsWith('/news')) return 'news'
  if (path.startsWith('/point')) return 'point'
  if (path.startsWith('/user')) return 'user'
  return 'home'
})

const goHome = () => {
  router.push('/')
}

const navigateTo = (key) => {
  if (key === 'home') {
    router.push('/')
  } else if (key === 'market') {
    router.push('/market')
  } else if (key === 'news') {
    router.push('/news')
  } else if (key === 'point') {
    router.push('/point')
  } else if (key === 'user') {
    if (!userStore.token) {
      userStore.showLoginDialog = true
      return
    }
    router.push('/user')
  }
}

const handleSelect = (key) => {
  navigateTo(key)
}

const handleMobileSelect = (key) => {
  mobileMenuVisible.value = false
  navigateTo(key)
}

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        await request.post('/api/user/logout')
      } catch (e) {
        // 退出登录接口失败不影响本地清除
      }
      userStore.clearToken()
      ElMessage.success('已退出登录')
      userStore.showLoginDialog = true
      router.push('/')
    }).catch(() => {})
  } else if (command === 'point') {
    router.push('/point')
  }
}

const openPublishDialog = () => {
  if (!userStore.token) {
    userStore.showLoginDialog = true
    return
  }
  if (publishDialogRef.value) {
    publishDialogRef.value.open()
  }
}

const emit = defineEmits(['publish-success'])

const handlePublishSuccess = () => {
  // 如果在首页，则刷新列表；如果在其他页面，可以提示或者跳转到首页
  if (route.path === '/') {
    emit('publish-success')
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
.navbar {
  background-color: #fff;
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 0;
  z-index: 10;
  height: auto !important;
  padding: 0;
}

.navbar-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 5%;
  height: 70px;
}

.logo {
  font-size: 24px;
  font-weight: 800;
  color: var(--color-primary);
  letter-spacing: 1px;
  cursor: pointer;
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.logo-icon-svg {
  margin-right: 8px;
  font-size: 28px;
  color: var(--color-primary);
}

.nav-menu {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-menu .el-menu {
  border-bottom: none;
  background: transparent;
}

.nav-menu .el-menu-item {
  font-size: 16px;
  font-weight: 500;
  color: #555;
  padding: 0 25px;
}

.nav-menu .el-menu-item.is-active {
  color: var(--color-primary) !important;
  border-bottom: 3px solid var(--color-primary) !important;
  background-color: transparent !important;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.publish-btn {
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-primary));
  border: none;
  font-weight: bold;
  padding: 8px 20px;
  box-shadow: 0 4px 15px var(--color-primary-shadow);
  transition: all 0.3s;
}

.publish-btn:hover {
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-primary-dark));
  box-shadow: 0 6px 20px rgba(255, 107, 129, 0.4);
  transform: translateY(-1px);
}

.user-dropdown {
  cursor: pointer;
}

.login-text-btn {
  font-size: 16px;
  color: var(--color-text-body);
  font-weight: 500;
}
.login-text-btn:hover {
  color: var(--color-primary);
}

.nickname {
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  font-size: 15px;
}

.nickname:hover {
  color: var(--color-primary);
}

.theme-toggle {
  font-size: 20px;
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: color 0.2s, transform 0.3s;
}
.theme-toggle:hover {
  color: var(--color-primary);
  transform: rotate(20deg);
}

.hamburger {
  font-size: 24px;
  cursor: pointer;
  color: var(--color-text-body);
  transition: color 0.2s;
}
.hamburger:hover {
  color: var(--color-primary);
}

/* 移动端菜单 */
.mobile-menu {
  background: #fff;
  border-top: 1px solid var(--color-border-light);
  padding: 8px 0;
}

.mobile-menu-item {
  padding: 14px 24px;
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-body);
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
}
.mobile-menu-item:hover,
.mobile-menu-item.active {
  color: var(--color-primary);
  background: var(--color-primary-bg);
}
.mobile-menu-item.publish-mobile {
  color: var(--color-primary);
  font-weight: 600;
}

/* 过渡动画 */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}
.slide-down-enter-from,
.slide-down-leave-to {
  max-height: 0;
  opacity: 0;
}
.slide-down-enter-to,
.slide-down-leave-from {
  max-height: 400px;
  opacity: 1;
}

/* 响应式工具类 */
.desktop-only {
  display: flex;
}
.mobile-only {
  display: none;
}

.point-item {
  color: #f39c12 !important;
  font-weight: bold;
  font-size: 14px;
}

.logout-item {
  color: #f56c6c;
}
.logout-item:hover {
  background-color: #fef0f0;
  color: #f56c6c;
}

/* 移动端响应式 */
@media (max-width: 768px) {
  .navbar-inner {
    padding: 0 16px;
    height: 56px;
  }
  .logo {
    font-size: 16px;
  }
  .logo-icon-svg {
    font-size: 22px;
  }
  .logo-text {
    display: none;
  }
  .desktop-only {
    display: none !important;
  }
  .mobile-only {
    display: flex;
  }
  .user-info {
    gap: 12px;
  }
}
</style>
