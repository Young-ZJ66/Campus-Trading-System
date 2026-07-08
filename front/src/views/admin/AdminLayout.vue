<template>
  <div class="admin-layout">
    <el-container direction="vertical" class="layout-container">
      <el-header class="topbar">
        <div class="topbar-left">
          <div class="topbar-title">校园闲置物品交易系统后台</div>
        </div>
        <div class="topbar-right">
          <span class="admin-name">{{ userStore.adminInfo.nickname || '管理员' }}</span>
          <span class="logout" @click="logout">退出</span>
        </div>
      </el-header>
      <el-container class="body-container">
        <el-aside width="220px" class="aside">
          <el-menu :default-active="activeMenu" router class="menu">
            <el-menu-item index="/admin/dashboard">仪表盘</el-menu-item>
            <el-menu-item index="/admin/goods">商品管理</el-menu-item>
            <el-menu-item index="/admin/category">分类管理</el-menu-item>
            <el-menu-item index="/admin/news">资讯管理</el-menu-item>
            <el-menu-item index="/admin/point-goods">积分商品</el-menu-item>
            <el-menu-item index="/admin/point-orders">积分订单</el-menu-item>
            <el-menu-item index="/admin/users">用户管理</el-menu-item>
            <el-menu-item index="/admin/orders">订单管理</el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import { useUserStore } from '../../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const logout = async () => {
  try {
    await request.post('/api/user/logout')
  } catch (e) {}
  userStore.clearAdminToken()
  router.replace('/admin/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  background: #f5f7fa;
}
.layout-container {
  height: 100%;
}
.topbar {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #1f2d3d;
  border-bottom: 1px solid #101c24;
}
.topbar-title {
  font-weight: 800;
  color: rgba(255, 255, 255, 0.95);
  letter-spacing: 1px;
}
.topbar-right {
  display: flex;
  align-items: center;
  gap: 14px;
}
.body-container {
  flex: 1;
  min-height: 0;
  height: calc(100vh - 56px);
}
.aside {
  background: #1f2d3d;
  color: #fff;
  height: 100%;
}
.menu {
  height: 100%;
  border-right: none;
  background: transparent;
}
.menu :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.85);
  transition: background-color 0.25s, color 0.25s;
}
.menu :deep(.el-menu-item:hover) {
  background-color: #304156 !important;
  color: #ffffff !important;
}
.menu :deep(.el-menu-item.is-active) {
  background-color: #1890ff !important;
  color: #ffffff !important;
  font-weight: bold;
}
.admin-name {
  color: rgba(255, 255, 255, 0.85);
}
.logout {
  color: #f56c6c;
  cursor: pointer;
}
.logout:hover {
  text-decoration: underline;
}
.main {
  padding: 16px;
  height: 100%;
  min-height: 0;
  overflow: auto;
}
</style>
