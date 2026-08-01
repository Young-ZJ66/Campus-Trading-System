<template>
  <el-container direction="vertical">
    <Navbar />

    <el-main class="home-main">
      <!-- 轮播资讯 -->
      <div class="carousel-section">
        <el-carousel height="300px" indicator-position="outside" v-loading="newsLoading">
          <el-carousel-item v-for="item in newsList" :key="item.newsId" @click="goToNews(item.newsId)">
            <div class="carousel-item-content">
              <img :src="getCoverImage(item.coverImage)" class="carousel-image" v-if="item.coverImage" />
              <div class="carousel-mask" v-else>校园资讯</div>
              <div class="carousel-title">
                <h3>{{ item.title }}</h3>
                <span class="news-date">{{ formatTime(item.createTime) }}</span>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- 热门闲置 -->
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">
            <el-icon class="title-icon"><TrendCharts /></el-icon>热门闲置
          </h2>
          <el-button type="text" @click="router.push('/market')">查看更多 <el-icon><ArrowRight /></el-icon></el-button>
        </div>
        <div class="goods-row" v-loading="hotGoodsLoading">
          <el-card 
            v-for="item in hotGoodsList" 
            :key="item.goodsId" 
            class="goods-card" 
            :body-style="{ padding: '0px' }" 
            @click="goToGoods(item.goodsId)"
          >
            <div class="image-wrapper">
              <img v-if="getCoverImage(item.images)" :src="getCoverImage(item.images)" class="image" />
              <div v-else class="no-image">暂无图片</div>
              <div class="exchange-tag" v-if="item.isExchange === 1">支持换物</div>
            </div>
            <div class="goods-info">
              <h3 class="goods-title">{{ item.title }}</h3>
              <div class="price-box">
                <span class="price">￥{{ item.price }}</span>
                <span class="view-count"><el-icon><View /></el-icon> {{ item.viewCount || 0 }}</span>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 积分商城上新 -->
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">
            <el-icon class="title-icon"><Present /></el-icon>积分商城上新
          </h2>
          <el-button type="text" @click="router.push('/point')">去兑换 <el-icon><ArrowRight /></el-icon></el-button>
        </div>
        <div class="goods-row" v-loading="pointGoodsLoading">
          <el-card 
            v-for="item in pointGoodsList" 
            :key="item.itemId" 
            class="goods-card" 
            :body-style="{ padding: '0px' }" 
            @click="goToPointGoods(item.itemId)"
          >
            <div class="image-wrapper">
              <img :src="getCoverImage(item.image)" class="image" />
            </div>
            <div class="goods-info">
              <h3 class="goods-title">{{ item.name }}</h3>
              <div class="price-box point-price">
                <span class="price">
                  <el-icon class="coin-icon"><Coin /></el-icon>{{ item.pointsRequired }}
                </span>
                <span class="stock-count">剩 {{ item.stock }} 件</span>
              </div>
            </div>
          </el-card>
        </div>
      </div>

    </el-main>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, View, TrendCharts, Present, Coin } from '@element-plus/icons-vue'
import request from '../utils/request'
import Navbar from '../components/Navbar.vue'
import { formatTime } from '../utils/time'
import { getCoverImage } from '../utils/image'

const router = useRouter()

const newsList = ref([])
const newsLoading = ref(false)

const hotGoodsList = ref([])
const hotGoodsLoading = ref(false)

const pointGoodsList = ref([])
const pointGoodsLoading = ref(false)

onMounted(() => {
  fetchNews()
  fetchHotGoods()
  fetchPointGoods()
})

const fetchNews = async () => {
  newsLoading.value = true
  try {
    const res = await request.get('/api/news/list', { params: { pageNum: 1, pageSize: 3 } })
    newsList.value = res.list || []
  } catch (e) {
    console.error(e)
  } finally {
    newsLoading.value = false
  }
}

const fetchHotGoods = async () => {
  hotGoodsLoading.value = true
  try {
    const res = await request.post('/api/goods/list', {
      pageNum: 1,
      pageSize: 4,
      sortType: 2 
    })
    hotGoodsList.value = res.list || []
  } catch (e) {
    console.error(e)
  } finally {
    hotGoodsLoading.value = false
  }
}

const fetchPointGoods = async () => {
  pointGoodsLoading.value = true
  try {
    const res = await request.get('/api/point/goods')
    
    pointGoodsList.value = (res || []).slice(0, 4)
  } catch (e) {
    console.error(e)
  } finally {
    pointGoodsLoading.value = false
  }
}

const goToNews = (id) => {
  router.push('/news')
}

const goToGoods = (id) => {
  router.push(`/goods/${id}`)
}

const goToPointGoods = (id) => {
  router.push(`/point/goods/${id}`)
}
</script>

<style scoped>
.home-main {
  background-color: var(--color-bg-page);
  padding: 30px 0;
  min-height: calc(100vh - 70px);
}

.carousel-section {
  max-width: 1200px;
  margin: 0 auto 40px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
}

.carousel-item-content {
  position: relative;
  width: 100%;
  height: 100%;
  cursor: pointer;
  background-color: #0f172a;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.85;
  transition: transform var(--transition-image);
}

.carousel-item-content:hover .carousel-image {
  transform: scale(1.04);
}

.carousel-mask {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #fff;
  font-weight: 800;
  background: linear-gradient(135deg, var(--color-border) 0%, #cbd5e1 100%);
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.carousel-title {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24px;
  background: linear-gradient(to top, rgba(15, 23, 42, 0.95), transparent);
  color: #fff;
}

.carousel-title h3 {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.news-date {
  font-size: 13px;
  color: #94a3b8;
}

.section-container {
  max-width: 1200px;
  margin: 0 auto 40px;
  padding: 0 20px;
  box-sizing: border-box;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: var(--color-text-heading);
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -6px;
  left: 0;
  width: 40px;
  height: 4px;
  background: linear-gradient(90deg, var(--color-primary), var(--color-primary-dark));
  border-radius: 2px;
}

.goods-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.goods-card {
  border-radius: var(--radius-lg);
  transition: transform var(--transition-smooth), box-shadow var(--transition-smooth);
  cursor: pointer;
  border: 1px solid var(--color-border-light);
  background: var(--color-bg-card);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.goods-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-xl);
}

.image-wrapper {
  position: relative;
  width: 100%;
  height: 220px;
  overflow: hidden;
}

.image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.goods-card:hover .image {
  transform: scale(1.05);
}

.no-image {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--color-bg-hover) 0%, var(--color-border) 100%);
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-base);
  font-weight: 500;
}

.exchange-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  background: linear-gradient(135deg, #10b981, #34d399);
  color: #fff;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
  z-index: 2;
}

.goods-info {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.goods-title {
  margin: 0 0 12px 0;
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text-heading);
  line-height: 1.5;
  height: 48px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.price-box {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  color: #f43f5e;
  font-size: 22px;
  font-weight: 800;
  display: inline-flex;
  align-items: center;
}

.title-icon {
  margin-right: 8px;
  color: var(--color-primary);
  vertical-align: middle;
}

.coin-icon {
  margin-right: 4px;
  color: #f59e0b;
  vertical-align: middle;
}

.point-price .price {
  color: #f59e0b;
}

.view-count, .stock-count {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 响应式调整 */
@media (max-width: 1024px) {
  .goods-row {
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
  }
}
@media (max-width: 768px) {
  .goods-row {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
}

</style>
