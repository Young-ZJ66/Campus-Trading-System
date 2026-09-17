<template>
  <div class="news-container">
    <el-container direction="vertical">
      <Navbar />

      <el-main>
        <div class="news-header">
          <h2>
            <el-icon style="margin-right: 8px; vertical-align: middle; color: var(--color-text-body);"><Notification /></el-icon>最新校园资讯与公告
          </h2>
          <p>了解校园新鲜事，防骗防欺指南</p>
        </div>

        <div class="news-list" v-loading="loading">
          <el-card v-for="item in newsList" :key="item.newsId" class="news-card" @click="openDetail(item.newsId)">
            <div class="news-content-wrapper">
              <div class="news-img" v-if="item.coverImage">
                <img :src="getCoverImage(item.coverImage)" alt="cover" loading="lazy" />
              </div>
              <div class="news-text">
                <h3 class="news-title">
                  <el-tag v-if="item.isTop === 1" type="danger" size="small" effect="dark" class="top-tag">置顶</el-tag>
                  {{ item.title }}
                </h3>
                <div class="news-meta">
                  <span>发布时间：{{ formatTime(item.createTime) }}</span>
                  <span style="margin-left: 20px;">阅读量：{{ item.viewCount }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </div>

        <div class="pagination-container">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            v-model:current-page="pageNum"
            :page-size="pageSize"
            @current-change="fetchNewsList"
          />
        </div>
      </el-main>

      <!-- 资讯详情弹窗 -->
      <el-dialog v-model="dialogVisible" :title="currentNews.title" width="60%">
        <div class="dialog-meta">
          发布于：{{ formatTime(currentNews.createTime) }} | 阅读：{{ currentNews.viewCount }}
        </div>
        <el-divider />
        <div class="dialog-content" v-html="sanitizeHtml(currentNews.content)"></div>
      </el-dialog>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import Navbar from '../components/Navbar.vue'
import { formatTime } from '../utils/time'
import { Notification } from '@element-plus/icons-vue'
import { getCoverImage } from '../utils/image'
import DOMPurify from 'dompurify'

const router = useRouter()
const userStore = useUserStore()

const newsList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const dialogVisible = ref(false)
const currentNews = ref({})

onMounted(() => {
  fetchNewsList()
})

const fetchNewsList = async () => {
  loading.value = true
  try {
    const res = await request.get(`/api/news/list?pageNum=${pageNum.value}&pageSize=${pageSize.value}`)
    newsList.value = res.list
    total.value = res.total
  } catch (error) {
    ElMessage.error('获取资讯列表失败')
  } finally {
    loading.value = false
  }
}

const sanitizeHtml = (html) => {
  return DOMPurify.sanitize(html || '')
}

const openDetail = async (id) => {
  try {
    const res = await request.get(`/api/news/detail/${id}`)
    currentNews.value = res
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取资讯详情失败')
  }
}



</script>

<style scoped>
.news-container {
  min-height: 100vh;
  background-color: var(--color-bg-page);
  font-family: var(--font-family);
}
.el-menu-item:hover {
  color: var(--color-primary) !important;
  background-color: var(--color-primary-bg) !important;
}
.el-main {
  padding: 30px 10%;
}
.news-header {
  margin-bottom: 30px;
  text-align: center;
}
.news-header h2 {
  color: var(--color-text-body);
  margin-bottom: 10px;
}
.news-header p {
  color: #7f8c8d;
  font-size: 14px;
}
.news-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.news-card {
  border-radius: var(--radius-md);
  border: none;
  cursor: pointer;
  transition: all 0.3s;
}
.news-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-lg);
}
.news-content-wrapper {
  display: flex;
  align-items: center;
  padding: 10px;
}
.news-img {
  width: 140px;
  height: 90px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  margin-right: 20px;
  flex-shrink: 0;
}
.news-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.news-text {
  flex: 1;
}
.news-title {
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #303133;
}
.top-tag {
  margin-right: 8px;
  border-radius: 4px;
}
.news-meta {
  color: var(--color-text-muted);
  font-size: 13px;
}
.pagination-container {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}
:deep(.el-pagination.is-background .el-pager li.is-active) {
  background-color: var(--color-primary) !important;
}
.dialog-meta {
  color: var(--color-text-muted);
  font-size: 14px;
  text-align: center;
}
.dialog-content {
  line-height: 1.8;
  color: #303133;
  font-size: 16px;
  padding: 0 20px;
}

/* 响应式 */
@media (max-width: 768px) {
  .el-main {
    padding: 16px !important;
  }
  .news-content-wrapper {
    flex-direction: column;
    align-items: flex-start;
  }
  .news-img {
    width: 100%;
    height: 160px;
    margin-right: 0;
    margin-bottom: 12px;
  }
  .news-title {
    font-size: 16px;
  }
  :deep(.el-dialog) {
    width: 90% !important;
    margin: 16px auto;
  }
}
</style>
