<template>
  <div class="home-container">
    <el-container direction="vertical">
      <Navbar />

      <el-main>
        <!-- 搜索与筛选区 -->
        <div class="filter-section">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索你想要的闲置宝贝..."
            class="search-input"
            style="width: 350px; margin-right: 25px"
            clearable
            @keyup.enter="fetchGoodsList"
          >
            <template #append>
              <el-button :icon="Search" @click="fetchGoodsList">搜索</el-button>
            </template>
          </el-input>
          
          <el-select v-model="queryParams.categoryId" placeholder="全部分类" clearable style="width: 140px; margin-right: 20px" @change="fetchGoodsList">
            <el-option v-for="item in categoryList" :key="item.categoryId" :label="item.name" :value="item.categoryId" />
          </el-select>

          <el-select v-model="queryParams.sortType" style="width: 120px; margin-right: 30px" @change="fetchGoodsList">
            <el-option label="最新发布" :value="0" />
            <el-option label="价格最低" :value="1" />
            <el-option label="热度最高" :value="2" />
          </el-select>

          <el-checkbox v-model="isExchangeOnly" @change="handleExchangeChange" border>仅看支持换物</el-checkbox>
        </div>

        <!-- 商品列表区 -->
        <div class="goods-grid" v-loading="loading">
          <el-empty v-if="!loading && goodsList.length === 0" description="暂无商品" />
          <el-card v-for="item in goodsList" :key="item.goodsId" class="goods-card" :body-style="{ padding: '0px' }" @click="goToDetail(item.goodsId)" style="cursor: pointer;">
            <div class="image-wrapper">
              <span v-if="item.isExchange === 1" class="exchange-badge">支持换物</span>
              <img v-if="getCoverImage(item.images)" :src="getCoverImage(item.images)" class="image" />
              <div v-else class="no-image">暂无图片</div>
            </div>
            <div class="card-content">
              <h3 class="goods-title">{{ item.title }}</h3>
              <div class="goods-price-box">
                <span class="price-symbol">￥</span>
                <span class="price">{{ item.price }}</span>
                <span v-if="item.originalPrice" class="original-price">￥{{ item.originalPrice }}</span>
              </div>
              <div style="margin-bottom: 8px">
                <el-tag size="small" effect="plain" type="info" round>{{ item.categoryName }}</el-tag>
              </div>
              <div class="goods-footer">
                <div class="publisher-info">
                  <div class="publisher-avatar">
                    {{ item.publisherName.charAt(0).toUpperCase() }}
                  </div>
                  <span class="publisher">{{ item.publisherName }}</span>
                </div>
                <button class="view-btn" @click.stop="goToDetail(item.goodsId)">去看看</button>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            v-model:current-page="queryParams.pageNum"
            :page-size="queryParams.pageSize"
            @current-change="fetchGoodsList"
          />
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
defineOptions({
  name: 'Market'
})
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../store/user'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import Navbar from '../components/Navbar.vue'

const userStore = useUserStore()
const router = useRouter()

const activeIndex = ref('home')
const categoryList = ref([])
const goodsList = ref([])
const total = ref(0)
const isExchangeOnly = ref(false)

const queryParams = reactive({
  keyword: '',
  categoryId: null,
  isExchange: null,
  sortType: 0,
  pageNum: 1,
  pageSize: 12
})

onMounted(async () => {
  await fetchCategories()
  await fetchGoodsList()
})

const fetchCategories = async () => {
  try {
    categoryList.value = await request.get('/api/category/list')
  } catch (error) {
    console.error(error)
  }
}

const fetchGoodsList = async () => {
  try {
    const res = await request.post('/api/goods/list', queryParams)
    goodsList.value = res.list
    total.value = res.total
  } catch (error) {
    console.error(error)
  }
}

const handleExchangeChange = (val) => {
  queryParams.isExchange = val ? 1 : null
  queryParams.pageNum = 1
  fetchGoodsList()
}

const goToDetail = (id) => {
  router.push(`/goods/${id}`)
}

const getCoverImage = (imagesStr) => {
  if (!imagesStr) return ''
  try {
    const images = JSON.parse(imagesStr)
    if (images && images.length > 0) {
      if (images[0].startsWith('http')) {
        return images[0]
      }
      return 'http://localhost:8080' + images[0]
    }
  } catch (e) {}
  return ''
}


</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background-color: #f8fafc;
  font-family: system-ui, -apple-system, sans-serif;
}

.filter-section {
  background: #ffffff;
  padding: 20px 24px;
  border-radius: 16px;
  margin: 20px auto 30px;
  max-width: 1200px;
  box-sizing: border-box;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.05), 0 1px 2px -1px rgba(0, 0, 0, 0.05);
  border: 1px solid #f1f5f9;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 24px 0 0 24px;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  padding-left: 16px;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #ff6b81 inset;
}

.search-input :deep(.el-input-group__append) {
  border-radius: 0 24px 24px 0;
  background-color: #ff6b81;
  color: white;
  border: none;
  box-shadow: none;
  padding: 0 24px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.search-input :deep(.el-input-group__append):hover {
  background-color: #ff4757;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  box-sizing: border-box;
}

.goods-card {
  border-radius: 16px;
  overflow: hidden;
  transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  border: 1px solid #f1f5f9;
  background: #ffffff;
  box-shadow: none;
}

.goods-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.06), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.image-wrapper {
  position: relative;
  overflow: hidden;
  height: 240px;
  background-color: #f1f5f9;
}

.image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.no-image {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
}

.goods-card:hover .image {
  transform: scale(1.05);
}

.exchange-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: linear-gradient(135deg, #10b981, #34d399);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
  z-index: 2;
}

.card-content {
  padding: 20px;
}

.goods-title {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
  height: 48px;
}

.goods-price-box {
  display: flex;
  align-items: baseline;
  margin-bottom: 12px;
}

.price-symbol {
  color: #f43f5e;
  font-size: 14px;
  font-weight: 800;
}

.price {
  color: #f43f5e;
  font-size: 24px;
  font-weight: 800;
}

.original-price {
  color: #94a3b8;
  text-decoration: line-through;
  margin-left: 8px;
  font-size: 13px;
}

.goods-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #e2e8f0;
}

.publisher-info {
  display: flex;
  align-items: center;
}

.publisher-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #cbd5e1;
  color: #0f172a;
  margin-right: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 11px;
  font-weight: 700;
}

.publisher {
  color: #64748b;
  font-size: 13px;
  font-weight: 500;
}

.view-btn {
  background: #f1f5f9;
  color: #334155;
  border: none;
  border-radius: 20px;
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.goods-card:hover .view-btn {
  background: #ff6b81;
  color: white;
  box-shadow: 0 4px 12px rgba(255, 107, 129, 0.2);
}

.pagination-container {
  margin: 40px 0 60px;
  display: flex;
  justify-content: center;
}

:deep(.el-pagination.is-background .el-pager li.is-active) {
  background-color: #ff6b81 !important;
}

</style>
