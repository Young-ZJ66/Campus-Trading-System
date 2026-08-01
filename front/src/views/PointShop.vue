<template>
  <div class="point-container">
    <el-container direction="vertical">
      <Navbar />

      <el-main>
        <div class="point-banner">
          <div class="banner-content">
            <h2 class="banner-title">
              <el-icon style="margin-right: 8px; vertical-align: middle;"><Present /></el-icon>校园积分商城
            </h2>
            <div class="banner-actions">
              <span>当前可用积分：<strong class="highlight-points">{{ userStore.userInfo.points || 0 }}</strong></span>
              <el-popover placement="bottom-end" :width="320" trigger="hover" @show="fetchSignInCalendar">
                <template #reference>
                  <el-button type="warning" round class="sign-btn" size="small" @click="handleSignIn" :loading="signLoading" :disabled="hasSignedInToday">
                    {{ hasSignedInToday ? '今日已签到' : '签到 +1~10分' }}
                  </el-button>
                </template>
                <div class="calendar-wrapper" v-loading="calendarLoading">
                  <div class="calendar-header">{{ calendarDate.getFullYear() }}年{{ calendarDate.getMonth() + 1 }}月签到记录</div>
                  <div class="week-header">
                    <span v-for="w in weeks" :key="w" class="week-item">{{ w }}</span>
                  </div>
                  <div class="days-grid">
                    <div
                      v-for="(cell, idx) in calendarCells"
                      :key="idx"
                      class="day-cell"
                      :class="{ 'is-empty': !cell.day, 'is-signed': cell.signed }"
                    >
                      <span class="day-number">{{ cell.day ? cell.day : '' }}</span>
                      <span v-if="cell.signed" class="signed-mark">✓</span>
                    </div>
                  </div>
                </div>
              </el-popover>
            </div>
          </div>
        </div>

        <div class="goods-grid" v-loading="loading">
          <el-card v-for="item in pointGoodsList" :key="item.itemId" class="goods-card" :body-style="{ padding: '0px' }" @click="goToDetail(item.itemId)" style="cursor: pointer;">
            <div class="image-wrapper">
              <img v-if="getCoverImage(item.image)" :src="getCoverImage(item.image)" class="image" />
              <div v-else class="no-image">暂无图片</div>
            </div>
            <div class="card-content">
              <h3 class="goods-title">{{ item.name }}</h3>
              <p class="goods-desc" :title="item.description">{{ item.description }}</p>
              
              <div class="goods-footer">
                <div class="points-cost">
                  <span class="cost-num">{{ item.pointsRequired }}</span>
                  <span class="cost-unit">积分</span>
                </div>
                <div class="stock-info">剩余 {{ item.stock }} 件</div>
              </div>
              
              <el-button 
                type="primary" 
                class="exchange-btn" 
                :disabled="item.stock <= 0"
                @click.stop="handleExchange(item)">
                {{ item.stock > 0 ? '立即兑换' : '已被抢光' }}
              </el-button>
            </div>
          </el-card>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navbar from '../components/Navbar.vue'
import { Present } from '@element-plus/icons-vue'
import { getCoverImage } from '../utils/image'

const router = useRouter()
const userStore = useUserStore()

const pointGoodsList = ref([])
const loading = ref(false)
const signLoading = ref(false)
const hasSignedInToday = ref(false)
const signedDates = ref([])
const calendarDate = ref(new Date())
const calendarLoading = ref(false)
const weeks = ['日', '一', '二', '三', '四', '五', '六']

const calendarCells = computed(() => {
  const date = calendarDate.value
  const year = date.getFullYear()
  const month = date.getMonth()
  const firstDay = new Date(year, month, 1).getDay()
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const cells = []
  for (let i = 0; i < firstDay; i++) {
    cells.push({ day: '', signed: false })
  }
  for (let d = 1; d <= daysInMonth; d++) {
    const dayStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    cells.push({ day: d, signed: signedDates.value.includes(dayStr) })
  }
  while (cells.length % 7 !== 0) {
    cells.push({ day: '', signed: false })
  }
  return cells
})

onMounted(() => {
  fetchPointGoods()
  checkSignInStatus()
})

const checkSignInStatus = async () => {
  if (!userStore.token) return
  try {
    hasSignedInToday.value = await request.get('/api/point/checkSignIn')
  } catch (e) {}
}

const fetchPointGoods = async () => {
  loading.value = true
  try {
    pointGoodsList.value = await request.get('/api/point/goods')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const fetchSignInCalendar = async () => {
  if (!userStore.token) return
  calendarLoading.value = true
  try {
    const year = calendarDate.value.getFullYear()
    const month = String(calendarDate.value.getMonth() + 1).padStart(2, '0')
    const res = await request.get(`/api/point/signInDates?yearMonth=${year}-${month}`)
    signedDates.value = res || []
    if (hasSignedInToday.value) {
      const today = new Date()
      const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
      if (!signedDates.value.includes(todayStr)) {
        signedDates.value = [...signedDates.value, todayStr]
      }
    }
  } catch (error) {
    console.error(error)
  } finally {
    calendarLoading.value = false
  }
}

const handleSignIn = async () => {
  if (!userStore.token) {
    userStore.showLoginDialog = true
    return
  }
  signLoading.value = true
  try {
    const pointsEarned = await request.post('/api/point/signIn')
    ElMessage.success(`签到成功！获得 ${pointsEarned} 积分`)
    hasSignedInToday.value = true
    
    
    const userInfo = await request.get('/api/user/info')
    userStore.setUserInfo(userInfo)
    fetchSignInCalendar()
  } catch (error) {
    
    console.error(error)
  } finally {
    signLoading.value = false
  }
}

const goToDetail = (id) => {
  router.push(`/point/goods/${id}`)
}

const handleExchange = (item) => {
  if (!userStore.token) {
    userStore.showLoginDialog = true
    return
  }
  if (userStore.userInfo.points < item.pointsRequired) {
    ElMessage.warning('您的可用积分不足，无法兑换该商品！')
    return
  }

  ElMessageBox.confirm(`确认消耗 ${item.pointsRequired} 积分兑换【${item.name}】吗？`, '积分兑换', {
    confirmButtonText: '确认兑换',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await request.post(`/api/point/exchange?itemId=${item.itemId}`)
      ElMessage.success('兑换成功！请留意系统通知或前往指定地点领取。')
      
      
      const userInfo = await request.get('/api/user/info')
      userStore.setUserInfo(userInfo)
      fetchPointGoods()
    } catch (error) {
      console.error(error)
    }
  }).catch(() => {})
}



</script>

<style scoped>
.point-container {
  min-height: 100vh;
  background-color: var(--color-bg-page);
  font-family: var(--font-family);
}
.el-menu-item:hover {
  color: var(--color-primary) !important;
  background-color: var(--color-primary-bg) !important;
}
.el-main {
  padding: 0 0 50px 0;
}
.point-banner {
  background: var(--color-bg-page);
  padding: 20px 5%;
  margin-bottom: 20px;
  box-shadow: none;
  display: flex;
  justify-content: center;
  align-items: center;
}
.banner-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  max-width: 100%;
  position: relative;
}
.banner-title {
  font-size: 24px;
  color: var(--color-text-body);
  margin: 0;
  font-weight: bold;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}
.banner-actions {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  padding: 8px 16px;
  border-radius: 30px;
  box-shadow: var(--shadow-md);
  margin-left: auto;
}
.calendar-wrapper {
  padding: 5px;
  max-height: 400px;
  overflow: auto;
}
.calendar-header {
  text-align: center;
  font-weight: bold;
  margin-bottom: 10px;
  color: var(--color-primary);
}
.week-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
  margin-bottom: 8px;
}
.week-item {
  text-align: center;
  color: #606266;
  font-size: 12px;
}
.days-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
}
.day-cell {
  height: 38px;
  border-radius: 4px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: default;
  user-select: none;
}
.day-cell.is-empty {
  background: transparent;
}
.day-number {
  font-size: 13px;
  font-weight: 700;
  color: #303133;
}
.day-cell.is-signed {
  background: #f0f9eb;
  box-shadow: inset 0 0 0 1px #e1f3d8;
}
.day-cell.is-signed .day-number {
  color: #a4da89;
}
.signed-mark {
  position: absolute;
  font-size: 26px;
  font-weight: 300;
  color: #303133;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  line-height: 1;
  opacity: 0.7;
}
.no-image {
  width: 100%;
  height: 240px;
  background: #f2f6fc;
  color: var(--color-text-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}
.highlight-points {
  color: #ff8c00;
  font-size: 24px;
  margin: 0 15px 0 5px;
}
.sign-btn {
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-primary));
  border: none;
  font-weight: bold;
  box-shadow: 0 4px 15px var(--color-primary-shadow);
}
.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 25px;
  padding: 0 5%;
}
.goods-card {
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: all var(--transition-normal);
  border: none;
  background: #fff;
}
.goods-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-xl);
}
.image-wrapper {
  overflow: hidden;
  height: 240px;
}
.image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-smooth);
}
.goods-card:hover .image {
  transform: scale(1.05);
}
.card-content {
  padding: 20px;
}
.goods-title {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-body);
}
.goods-desc {
  font-size: 13px;
  color: #7f8c8d;
  line-height: 1.5;
  margin-bottom: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  height: 39px;
}
.goods-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.points-cost {
  color: #ff8c00;
}
.cost-num {
  font-size: 24px;
  font-weight: 800;
}
.cost-unit {
  font-size: 13px;
  margin-left: 4px;
  font-weight: bold;
}
.stock-info {
  font-size: 12px;
  color: #a4b0be;
  background: #f1f2f6;
  padding: 4px 10px;
  border-radius: var(--radius-md);
}
.exchange-btn {
  width: 100%;
  border-radius: var(--radius-full);
  font-weight: bold;
  background: #67c23a;
  border: none;
}
.exchange-btn:disabled {
  background: #c8d6e5;
  color: #fff;
}
</style>
