<template>
  <div class="detail-container">
    <el-container direction="vertical">
      <Navbar />

      <el-main>
        <div class="breadcrumb-box">
          <el-button link type="primary" class="back-btn" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/point' }">积分商城</el-breadcrumb-item>
            <el-breadcrumb-item>商品详情</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <el-row :gutter="40" class="goods-info-row" v-if="goods">
          <el-col :span="10">
            <div class="img-preview">
              <img v-if="getCoverImage(goods.image)" :src="getCoverImage(goods.image)" class="main-img" />
              <div v-else class="no-image">暂无图片</div>
            </div>
          </el-col>
          
          <el-col :span="14">
            <div class="info-content">
              <h2 class="title">
                <el-tag type="warning" effect="dark" class="exchange-tag">
                  <el-icon style="margin-right: 4px; vertical-align: middle;"><Coin /></el-icon>积分兑换
                </el-tag>
                {{ goods.name }}
              </h2>
              
              <div class="price-box">
                <div class="price-item">
                  <span class="label">所需积分</span>
                  <span class="price">{{ goods.pointsRequired }}</span>
                </div>
              </div>
              
              <div class="meta-info">
                <div class="meta-item">
                  <span class="label">库存：</span>
                  <span class="value">{{ goods.stock }} 件</span>
                </div>
              </div>

              <div class="action-box">
                <el-button
                  type="danger"
                  class="buy-btn"
                  size="large"
                  @click="handleExchange"
                  :disabled="goods.stock <= 0 || goods.status === 0"
                >
                  {{ goods.status === 0 ? '已下架' : (goods.stock > 0 ? '立即兑换' : '已被抢光') }}
                </el-button>
              </div>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="40" class="detail-row" v-if="goods">
          <el-col :span="16">
            <el-card class="detail-card">
              <template #header>
                <div class="card-header">物品详细描述</div>
              </template>
              <div class="desc-content">{{ goods.description }}</div>
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card class="safety-card">
              <template #header>
                <div class="card-header" style="color: var(--color-primary); display: flex; align-items: center;">
                  <el-icon style="margin-right: 6px; vertical-align: middle;"><Present /></el-icon>兑换须知
                </div>
              </template>
              <ul class="safety-list">
                <li>兑换成功后将扣除相应积分。</li>
                <li>可在个人中心的“积分兑换订单”查看兑换记录。</li>
              </ul>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navbar from '../components/Navbar.vue'
import { ArrowLeft, Coin, Present } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const goodsId = route.params.id
const goods = ref(null)

onMounted(() => {
  fetchDetail()
})

const fetchDetail = async () => {
  try {
    goods.value = await request.get(`/api/point/goods/${goodsId}`)
  } catch (error) {
    ElMessage.error('获取积分商品详情失败')
  }
}

const getCoverImage = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  return baseURL + url
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/point')
  }
}

const handleExchange = () => {
  if (!userStore.token) {
    userStore.showLoginDialog = true
    return
  }
  if (userStore.userInfo.points < goods.value.pointsRequired) {
    ElMessage.warning('您的可用积分不足，无法兑换该商品！')
    return
  }
  
  ElMessageBox.confirm(`确认消耗 ${goods.value.pointsRequired} 积分兑换【${goods.value.name}】吗？`, '兑换确认', {
    confirmButtonText: '确认兑换',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post(`/api/point/exchange?itemId=${goods.value.itemId}`)
      ElMessage.success('兑换成功！请在个人中心查看兑换订单')
      
      
      const userInfo = await request.get('/api/user/info')
      userStore.setUserInfo(userInfo)
      
      
      fetchDetail()
      router.push('/user')
    } catch (error) {
      // 错误已由 request.js 拦截器处理
    }
  }).catch(() => {})
}
</script>

<style scoped>
.detail-container {
  min-height: 100vh;
  background-color: #f0f4f8;
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
}

.el-main {
  padding: 20px 10% 60px;
}

.breadcrumb-box {
  margin-bottom: 25px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.back-btn {
  padding: 0;
}

.goods-info-row {
  background: #fff;
  padding: 30px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.03);
  margin-bottom: 30px;
}

.img-preview {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.main-img {
  width: 100%;
  height: 400px;
  object-fit: cover;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.no-image {
  width: 100%;
  height: 400px;
  background: #f2f6fc;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.info-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.title {
  font-size: 24px;
  color: #2c3e50;
  margin: 0 0 20px;
  line-height: 1.4;
}

.exchange-tag {
  border: none;
  font-weight: bold;
  margin-right: 10px;
  vertical-align: middle;
}

.price-box {
  background: #fff0f2;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 25px;
}

.price-item {
  color: #ff4757;
  margin-bottom: 5px;
}

.label {
  font-size: 14px;
  margin-right: 10px;
}

.price {
  color: var(--color-primary);
  font-size: 36px;
  font-weight: 800;
}

.meta-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 30px;
  padding: 0 5px;
}

.meta-item {
  font-size: 14px;
}
.meta-item .label {
  color: #909399;
  display: inline-block;
  width: 80px;
}

.value {
  color: #2c3e50;
}

.action-box {
  margin-top: auto;
  display: flex;
  gap: 15px;
}

.buy-btn {
  flex: 1;
  font-size: 16px;
  font-weight: bold;
}

.detail-row {
  margin-bottom: 20px;
}

.detail-card, .safety-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  margin-bottom: 20px;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}

.desc-content {
  line-height: 1.8;
  color: #2c3e50;
  white-space: pre-wrap;
  font-size: 15px;
}

.safety-list {
  padding-left: 20px;
  margin: 0;
  color: #606266;
  line-height: 2;
}

.safety-list li {
  margin-bottom: 10px;
}
</style>
