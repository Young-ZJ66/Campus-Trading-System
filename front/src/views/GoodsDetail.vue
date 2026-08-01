<template>
  <div class="detail-container">
    <el-container direction="vertical">
      <Navbar />

      <el-main class="main-content">
        <div class="breadcrumb-box">
          <el-button link type="primary" class="back-btn" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/market' }">闲置市场</el-breadcrumb-item>
            <el-breadcrumb-item>商品详情</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <el-row :gutter="40" class="goods-info-row">
          <el-col :span="10">
            <div class="img-preview">
              <img :src="currentImage" class="main-img" />
              <div class="thumb-list">
                <div 
                  v-for="(img, index) in imageList" 
                  :key="index" 
                  class="thumb-item" 
                  :class="{ active: currentImage === img }"
                  @click="currentImage = img"
                >
                  <img :src="img" />
                </div>
              </div>
            </div>
          </el-col>
          
          <el-col :span="14">
            <div class="info-content">
              <h2 class="title">
                <el-tag v-if="goods.isExchange === 1" type="success" effect="dark" class="exchange-tag">支持换物</el-tag>
                {{ goods.title }}
              </h2>
              
              <div class="price-box">
                <div class="price-item">
                  <span class="label">转手价</span>
                  <span class="symbol">￥</span>
                  <span class="price">{{ goods.price }}</span>
                </div>
                <div class="original-price" v-if="goods.originalPrice">
                  原价：￥{{ goods.originalPrice }}
                </div>
              </div>
              
              <div class="meta-info">
                <div class="meta-item">
                  <span class="label">浏览次数：</span>
                  <span class="value">{{ goods.viewCount }} 次</span>
                </div>
                <div class="meta-item">
                  <span class="label">发布时间：</span>
                  <span class="value">{{ formatTime(goods.createTime) }}</span>
                </div>
                <div class="meta-item" v-if="goods.isExchange === 1">
                  <span class="label" style="color: #4facfe; font-weight: bold;">期望换取：</span>
                  <span class="value" style="color: #4facfe;">{{ goods.exchangeDesc }}</span>
                </div>
              </div>

              <div class="seller-info">
                <div class="avatar">{{ goods.publisherName?.charAt(0).toUpperCase() || 'U' }}</div>
                <div class="seller-detail">
                  <div class="name">{{ goods.publisherName }}</div>
                </div>
              </div>

              <div class="action-box">
                <template v-if="goods.status === 0">
                  <el-button type="danger" class="buy-btn" size="large" @click="handleBuy(0)">
                    立即购买
                  </el-button>
                  <el-button v-if="goods.isExchange === 1" type="primary" class="exchange-btn" size="large" @click="handleBuy(1)">
                    发起换物
                  </el-button>
                </template>
                <template v-else-if="goods.status === 1">
                  <el-button type="info" class="buy-btn" size="large" disabled>
                    已售出
                  </el-button>
                </template>
                <template v-else-if="goods.status === 2">
                  <el-button type="info" class="buy-btn" size="large" disabled>
                    已下架
                  </el-button>
                </template>
                <el-button size="large" @click="toggleFavorite" :type="isFavorite ? 'warning' : 'default'">
                  <el-icon><Star v-if="!isFavorite" /><StarFilled v-else /></el-icon>
                  {{ isFavorite ? '已收藏' : '收藏' }}
                </el-button>
              </div>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="40" class="detail-row">
          <el-col :span="16">
            <el-card class="detail-card">
              <template #header>
                <div class="card-header">物品详细描述</div>
              </template>
              <div class="desc-content">{{ goods.description }}</div>
            </el-card>

            <el-card class="comment-card">
              <template #header>
                <div class="card-header">留言问答 ({{ comments.length }})</div>
              </template>
              
              <div class="comment-input-box">
                <el-input
                  v-model="newComment"
                  type="textarea"
                  :rows="3"
                  placeholder="对这件物品感兴趣？留言问问卖家吧..."
                />
                <div class="comment-actions">
                  <el-button type="primary" @click="submitComment">发送留言</el-button>
                </div>
              </div>

              <div class="comment-list">
                <div class="comment-item" v-for="item in flatComments" :key="item.commentId" :style="{ marginLeft: `${item.level * 28}px` }">
                  <div class="comment-avatar">{{ item.nickname.charAt(0).toUpperCase() }}</div>
                  <div class="comment-body">
                    <div class="comment-meta">
                      <span class="name">{{ item.nickname }} <el-tag size="small" type="danger" v-if="item.userId === goods.userId">卖家</el-tag></span>
                      <span class="time">{{ formatTime(item.createTime) }}</span>
                    </div>
                    <div class="comment-text">
                      <span v-if="item.parentId" class="reply-tag">回复 @{{ item.parentNickname }}: </span>
                      {{ item.content }}
                    </div>
                    <div class="comment-reply">
                      <el-button link type="primary" size="small" @click="openReply(item)">回复</el-button>
                    </div>
                    <div v-if="activeReplyId === item.commentId" class="reply-input-box">
                      <el-input v-model="replyContent" type="textarea" :rows="2" :placeholder="`回复 ${item.nickname}：`" />
                      <div class="comment-actions">
                        <el-button link @click="cancelReply">取消</el-button>
                        <el-button type="primary" @click="submitReply(item)">发送</el-button>
                      </div>
                    </div>
                  </div>
                </div>
                <el-empty v-if="comments.length === 0" description="暂无留言，快来抢沙发吧~" :image-size="100"></el-empty>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card class="safety-card">
              <template #header>
                <div class="card-header" style="color: var(--color-primary);">🛡️ 校园交易安全提示</div>
              </template>
              <ul class="safety-list">
                <li>本平台仅供本校师生交流使用。</li>
                <li><strong>强烈建议在校内当面交易</strong>，当面验货满意后再确认收货。</li>
                <li>切勿轻信脱离平台的第三方支付链接或二维码。</li>
                <li>若发现违规商品或诈骗行为，请及时向管理员举报。</li>
              </ul>
            </el-card>
          </el-col>
        </el-row>
      </el-main>

      <!-- 换物弹窗 -->
      <el-dialog v-model="exchangeDialogVisible" title="发起以物换物" width="500px">
        <p style="margin-bottom: 20px; color: #606266;">请选择您要用来交换的已发布闲置物品：</p>
        <el-select v-model="selectedMyGoods" placeholder="请选择您的商品" style="width: 100%;">
          <el-option 
            v-for="item in mySellGoodsList" 
            :key="item.goodsId" 
            :label="item.title + (item.status === 0 ? '' : ' (不可用)')" 
            :value="item.goodsId" 
            :disabled="item.status !== 0"
          />
        </el-select>
        <div style="margin-top: 10px; font-size: 12px; color: var(--color-text-muted);">
          提示：只有处于"在售"状态的商品才能作为交换物。对方同意后，双方商品都会自动下架。
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="exchangeDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmCreateOrder(1)">确认发起请求</el-button>
          </span>
        </template>
      </el-dialog>

    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navbar from '../components/Navbar.vue'
import { ArrowLeft, Star, StarFilled } from '@element-plus/icons-vue'
import { formatTime } from '../utils/time'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const goodsId = route.params.id
const goods = ref({})
const imageList = ref([])
const currentImage = ref('')

const isFavorite = ref(false)

const comments = ref([])
const newComment = ref('')
const activeReplyId = ref(null)
const replyContent = ref('')

const exchangeDialogVisible = ref(false)
const selectedMyGoods = ref(null)
const mySellGoodsList = ref([])

onMounted(() => {
  fetchGoodsDetail()
  fetchComments()
  checkFavorite()
})

const goBack = () => {
  if (window.history.state && window.history.state.back) {
    router.back()
  } else {
    router.push('/')
  }
}

const checkFavorite = async () => {
  if (!userStore.token) return
  try {
    const res = await request.get(`/api/favorite/check?goodsId=${goodsId}`)
    isFavorite.value = res || false
  } catch (e) {}
}

const toggleFavorite = async () => {
  if (!userStore.token) {
    userStore.showLoginDialog = true
    return
  }
  try {
    if (isFavorite.value) {
      await request.post(`/api/favorite/remove?goodsId=${goodsId}`)
      isFavorite.value = false
      ElMessage.success('已取消收藏')
    } else {
      await request.post(`/api/favorite/add?goodsId=${goodsId}`)
      isFavorite.value = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    console.error(error)
  }
}

const fetchGoodsDetail = async () => {
  try {
    const res = await request.get(`/api/goods/detail/${goodsId}`)
    goods.value = res
    if (res.images) {
      const imgs = JSON.parse(res.images)
      const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
      imageList.value = imgs.map(img => img.startsWith('http') ? img : baseURL + img)
      if (imageList.value.length > 0) {
        currentImage.value = imageList.value[0]
      }
    }
  } catch (error) {
    console.error(error)
  }
}

const fetchComments = async () => {
  try {
    comments.value = await request.get(`/api/comment/list/${goodsId}`)
  } catch (error) {
    console.error(error)
  }
}

const flatComments = computed(() => {
  const list = Array.isArray(comments.value) ? comments.value : []
  const byParent = new Map()
  const byId = new Map()
  list.forEach(c => {
    byId.set(c.commentId, c)
    const pid = c.parentId || 0
    if (!byParent.has(pid)) byParent.set(pid, [])
    byParent.get(pid).push(c)
  })
  const roots = byParent.get(0) || byParent.get(null) || []
  const getRootId = (node) => {
    let cur = node
    while (cur && cur.parentId) {
      const parent = byId.get(cur.parentId)
      if (!parent) break
      cur = parent
    }
    return cur ? cur.commentId : node.commentId
  }
  const group = new Map()
  list.forEach(c => {
    if (!c.parentId) return
    const rid = getRootId(c)
    if (!group.has(rid)) group.set(rid, [])
    group.get(rid).push(c)
  })
  group.forEach(v => v.sort((a, b) => new Date(a.createTime) - new Date(b.createTime)))
  const result = []
  roots.forEach(r => {
    result.push({ ...r, level: 0 })
    const replies = group.get(r.commentId) || []
    replies.forEach(rep => result.push({ ...rep, level: 1 }))
  })
  return result
})

const openReply = (comment) => {
  activeReplyId.value = comment.commentId
  replyContent.value = ''
}

const cancelReply = () => {
  activeReplyId.value = null
  replyContent.value = ''
}

const submitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('留言内容不能为空')
    return
  }
  try {
    await request.post('/api/comment/add', {
      goodsId: goodsId,
      content: newComment.value,
      parentId: null
    })
    ElMessage.success('留言成功')
    newComment.value = ''
    fetchComments()
  } catch (error) {
    console.error(error)
  }
}

const submitReply = async (comment) => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('回复内容不能为空')
    return
  }
  try {
    await request.post('/api/comment/add', {
      goodsId: goodsId,
      content: replyContent.value,
      parentId: comment.commentId
    })
    ElMessage.success('回复成功')
    cancelReply()
    fetchComments()
  } catch (error) {
    console.error(error)
  }
}

const fetchMySellGoods = async () => {
  try {
    const res = await request.get('/api/goods/myPublished')
    mySellGoodsList.value = res.filter(item => item.status === 0)
  } catch (error) {
    console.error(error)
  }
}

const handleBuy = async (tradeType) => {
  if (!userStore.token) {
    userStore.showLoginDialog = true
    return
  }

  if (userStore.userInfo.userId === goods.value.userId) {
    ElMessage.warning('不能购买或置换自己发布的商品')
    return
  }

  if (tradeType === 1) {
    await fetchMySellGoods()
    exchangeDialogVisible.value = true
  } else {
    ElMessageBox.confirm(`确认花费 ￥${goods.value.price} 购买该商品吗？`, '购买确认', {
      confirmButtonText: '确认购买',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      confirmCreateOrder(0)
    }).catch(() => {})
  }
}

const confirmCreateOrder = async (tradeType) => {
  if (tradeType === 1 && !selectedMyGoods.value) {
    ElMessage.warning('请选择要用来交换的商品')
    return
  }
  try {
    await request.post('/api/order/create', {
      goodsId: goodsId,
      tradeType: tradeType,
      exchangeGoodsId: selectedMyGoods.value || null
    })
    ElMessage.success(tradeType === 1 ? '已发送换物请求，请等待卖家同意' : '购买成功，请在个人中心查看订单')
    exchangeDialogVisible.value = false
    router.push('/user')
  } catch (error) {
    console.error(error)
  }
}

</script>

<style scoped>
.detail-container {
  min-height: 100vh;
  background-color: var(--color-bg-page);
  font-family: var(--font-family);
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
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
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
  border-radius: var(--radius-md);
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.thumb-list {
  display: flex;
  gap: 10px;
}
.thumb-item {
  width: 70px;
  height: 70px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.3s;
}
.thumb-item.active {
  border-color: var(--color-primary);
}
.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.info-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.title {
  font-size: 24px;
  color: var(--color-text-body);
  margin: 0 0 20px;
  line-height: 1.4;
}
.exchange-tag {
  background: linear-gradient(135deg, #67c23a, #95d475);
  border: none;
  font-weight: bold;
  margin-right: 10px;
  vertical-align: middle;
}
.price-box {
  background: var(--color-primary-bg);
  padding: 20px;
  border-radius: var(--radius-md);
  margin-bottom: 25px;
}
.price-item {
  color: var(--color-primary-dark);
  margin-bottom: 5px;
}
.price-item .label {
  font-size: 14px;
  margin-right: 10px;
}
.price-item .symbol {
  font-size: 18px;
  font-weight: bold;
}
.price-item .price {
  font-size: 36px;
  font-weight: 800;
}
.original-price {
  color: var(--color-text-muted);
  text-decoration: line-through;
  font-size: 14px;
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
  color: var(--color-text-muted);
  display: inline-block;
  width: 80px;
}
.meta-item .value {
  color: #303133;
}
.seller-info {
  display: flex;
  align-items: center;
  padding: 15px;
  background: var(--color-bg-input);
  border-radius: var(--radius-md);
  margin-bottom: auto;
}
.seller-info .avatar {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  background: linear-gradient(135deg, #a1c4fd, #c2e9fb);
  color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
  margin-right: 15px;
}
.seller-detail .name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}
.action-box {
  display: flex;
  gap: 20px;
  margin-top: 30px;
}
.buy-btn, .exchange-btn {
  flex: 1;
  border-radius: 25px;
  font-weight: bold;
  font-size: 16px;
  letter-spacing: 2px;
}
.buy-btn {
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-primary));
  border: none;
  box-shadow: 0 4px 15px var(--color-primary-shadow);
}
.exchange-btn {
  background: linear-gradient(135deg, #67c23a, #95d475);
  border: none;
  box-shadow: 0 4px 15px rgba(103, 194, 58, 0.3);
}
.detail-row {
  margin-top: 30px;
}
.detail-card, .comment-card, .safety-card {
  border: none;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  margin-bottom: 30px;
}
.card-header {
  font-size: 16px;
  font-weight: bold;
  color: var(--color-text-body);
}
.desc-content {
  line-height: 1.8;
  color: #444;
  font-size: 15px;
  white-space: pre-wrap;
  padding: 10px;
}
.comment-input-box {
  margin-bottom: 30px;
  background: var(--color-bg-input);
  padding: 20px;
  border-radius: var(--radius-md);
}
.comment-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
}
.reply-input-box {
  margin-top: 12px;
  background: var(--color-bg-input);
  padding: 12px;
  border-radius: 10px;
}
.comment-item {
  display: flex;
  margin-bottom: 25px;
  padding-bottom: 0;
}
.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #dfe4ea;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  color: #747d8c;
  margin-right: 15px;
}
.comment-body {
  flex: 1;
}
.comment-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
.comment-meta .name {
  font-weight: 600;
  color: var(--color-text-body);
  font-size: 14px;
}
.comment-meta .time {
  color: var(--color-text-muted);
  font-size: 12px;
}
.comment-text {
  color: #444;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 10px;
}
.reply-tag {
  color: #409EFF;
  font-weight: 500;
}
.safety-list {
  padding-left: 20px;
  color: #606266;
  font-size: 14px;
  line-height: 2;
}
.safety-list li {
  margin-bottom: 10px;
}
</style>
