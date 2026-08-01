<template>
  <el-header>
    <div class="logo" @click="goHome" style="cursor: pointer;">
      <el-icon class="logo-icon-svg"><Goods /></el-icon>校园闲置物品交易系统
    </div>
    <div class="nav-menu">
      <el-menu mode="horizontal" :default-active="activeIndex" @select="handleSelect" :ellipsis="false">
        <el-menu-item index="home">首页</el-menu-item>
        <el-menu-item index="market">闲置市场</el-menu-item>
        <el-menu-item index="news">校园资讯</el-menu-item>
        <el-menu-item index="point">积分商城</el-menu-item>
        <el-menu-item index="user">个人中心</el-menu-item>
      </el-menu>
    </div>
    <div class="user-info">
      <!-- 发布闲置按钮 -->
      <el-button type="primary" round class="publish-btn" @click="openPublishDialog">
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
              <el-dropdown-item command="point" class="point-item" style="display: inline-flex; align-items: center;">
                <el-icon style="margin-right: 4px; vertical-align: middle;"><Coin /></el-icon>积分: {{ userStore.userInfo.points || 0 }}
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
    </div>

    <!-- 挂载全局发布弹窗 -->
    <PublishDialog ref="publishDialogRef" @success="handlePublishSuccess" />
  </el-header>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus, ArrowDown, SwitchButton, Goods, Coin } from '@element-plus/icons-vue'
import request from '../utils/request'
import PublishDialog from './PublishDialog.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const publishDialogRef = ref(null)

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

const handleSelect = (key) => {
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

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        await request.post('/api/user/logout')
      } catch (e) {}
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
.el-header {
  background-color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 5%;
  height: 70px !important;
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 0;
  z-index: 10;
}

.logo {
  font-size: 24px;
  font-weight: 800;
  color: var(--color-primary);
  letter-spacing: 1px;
  cursor: pointer;
  display: flex;
  align-items: center;
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
  margin-left: 15px;
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
</style>
