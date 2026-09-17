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
              <img v-if="getCoverImage(item.images)" :src="getCoverImage(item.images)" class="image" loading="lazy" />
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
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import Navbar from '../components/Navbar.vue'
import { getCoverImage } from '../utils/image'

const router = useRouter()

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
    ElMessage.error('获取分类列表失败')
  }
}

const fetchGoodsList = async () => {
  try {
    const res = await request.post('/api/goods/list', queryParams)
    goodsList.value = res.list
    total.value = res.total
  } catch (error) {
    ElMessage.error('获取商品列表失败')
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


</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background-color: var(--color-bg-page);
  font-family: var(--font-family);
}

.filter-section {
  background: var(--color-bg-card);
  padding: 20px 24px;
  border-radius: var(--radius-lg);
  margin: 20px auto 30px;
  max-width: 1200px;
  box-sizing: border-box;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--color-border-light);
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 24px 0 0 24px;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  padding-left: 16px;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

.search-input :deep(.el-input-group__append) {
  border-radius: 0 var(--radius-full) var(--radius-full) 0;
  background-color: var(--color-primary);
  color: white;
  border: none;
  box-shadow: none;
  padding: 0 24px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color var(--transition-fast);
}

.search-input :deep(.el-input-group__append):hover {
  background-color: var(--color-primary-dark);
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
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: transform var(--transition-smooth), box-shadow var(--transition-smooth);
  border: 1px solid var(--color-border-light);
  background: var(--color-bg-card);
  box-shadow: none;
}

.goods-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-xl);
}

.image-wrapper {
  position: relative;
  overflow: hidden;
  height: 240px;
  background-color: var(--color-bg-hover);
}

.image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-image);
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
  background: var(--color-primary);
  color: white;
  box-shadow: 0 4px 12px var(--color-primary-shadow);
}

.pagination-container {
  margin: 40px 0 60px;
  display: flex;
  justify-content: center;
}

:deep(.el-pagination.is-background .el-pager li.is-active) {
  background-color: var(--color-primary) !important;
}

/* 响应式 */
@media (max-width: 768px) {
  .market-header {
    padding: 20px 16px;
  }
  .market-header h2 {
    font-size: 20px;
  }
  .filter-bar {
    padding: 16px;
    flex-direction: column;
    gap: 12px;
  }
  .filter-bar .el-input,
  .filter-bar .el-select {
    width: 100% !important;
  }
  .goods-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    padding: 0 16px;
  }
  .goods-title {
    font-size: 13px;
  }
  .goods-price {
    font-size: 16px;
  }
}

</style>
