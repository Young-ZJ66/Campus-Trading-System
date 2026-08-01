<template>
  <div class="user-center">
    <el-container direction="vertical">
      <Navbar />

      <el-main>
        <el-row :gutter="20">
          <el-col :span="4">
            <el-menu :default-active="currentTab" class="side-menu" @select="handleSideSelect">
              <el-menu-item index="profile">个人资料</el-menu-item>
              <el-menu-item index="buy">我买到的/换到的</el-menu-item>
              <el-menu-item index="sell">我卖出的/换出的</el-menu-item>
              <el-menu-item index="published">我发布的闲置</el-menu-item>
              <el-menu-item index="favorites">我的收藏</el-menu-item>
              <el-menu-item index="pointOrders">积分兑换订单</el-menu-item>
              <el-menu-item index="points">积分明细</el-menu-item>
            </el-menu>
          </el-col>
          
          <el-col :span="20">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>{{ getTabTitle(currentTab) }}</span>
                </div>
              </template>
              
              <!-- 个人资料 -->
              <div v-if="currentTab === 'profile'" class="profile-section">
                <el-form :model="profileForm" label-width="100px" style="max-width: 500px">
                  <el-form-item label="学号">
                    <el-input v-model="profileForm.studentNo" disabled />
                  </el-form-item>
                  <el-form-item label="昵称">
                    <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
                  </el-form-item>
                  <el-form-item label="手机号">
                    <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
                  </el-form-item>
                  <el-form-item label="修改密码">
                    <el-button @click="passwordDialogVisible = true">修改密码</el-button>
                  </el-form-item>
                  <el-form-item label="当前积分">
                    <el-tag type="warning" size="large">
                      <el-icon style="margin-right: 4px; vertical-align: middle;"><Coin /></el-icon>{{ profileForm.points }}
                    </el-tag>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="saveProfile" :loading="savingProfile">保存修改</el-button>
                  </el-form-item>
                </el-form>
              </div>

              <!-- 买入卖出订单 -->
              <el-table v-else-if="currentTab === 'buy' || currentTab === 'sell'" :data="loading ? [] : orderList" style="width: 100%" v-loading="loading" stripe border empty-text="暂无数据" table-layout="fixed">
                <el-table-column prop="orderNo" label="订单号" min-width="140" />
                <el-table-column label="商品信息" min-width="160">
                  <template #default="scope">
                    <div>{{ scope.row.goodsTitle }}</div>
                    <div v-if="scope.row.tradeType === 1 && scope.row.exchangeGoodsTitle" style="color: var(--color-text-muted); font-size: 12px; margin-top: 4px;">
                      交换物：{{ scope.row.exchangeGoodsTitle }}
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="交易类型" width="110">
                  <template #default="scope">
                    <el-tag :type="scope.row.tradeType === 1 ? 'success' : 'info'">
                      {{ scope.row.tradeType === 1 ? '以物换物' : '普通购买' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="amount" label="交易金额" width="100">
                  <template #default="scope">
                    <span v-if="scope.row.tradeType === 0" style="color: #F56C6C">￥{{ scope.row.amount }}</span>
                    <span v-else>-</span>
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="110">
                  <template #default="scope">
                    <el-tag :type="getStatusType(scope.row.status)">
                      {{ getStatusText(scope.row.status, scope.row.tradeType, scope.row) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="创建时间" width="140" show-overflow-tooltip>
                  <template #default="scope">
                    {{ formatTime(scope.row.createTime) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" fixed="right" width="110" align="center">
                  <template #default="scope">
                    <div class="uc-op">
                      <el-button size="small" class="uc-op-btn" @click="openOrderDetail(scope.row)">查看详情</el-button>
                      <template v-if="currentTab === 'sell' && scope.row.tradeType === 1 && scope.row.status === 0">
                        <el-button size="small" class="uc-op-btn" type="primary" @click="processOrder(scope.row.orderId, 1)">同意</el-button>
                        <el-button size="small" class="uc-op-btn" type="danger" @click="processOrder(scope.row.orderId, 4)">拒绝</el-button>
                      </template>
                      <template v-if="currentTab === 'buy' && scope.row.tradeType === 1 && scope.row.status === 0">
                        <el-button size="small" class="uc-op-btn" type="danger" @click="cancelOrder(scope.row)">取消换物</el-button>
                      </template>
                      <template v-if="currentTab === 'buy' && scope.row.status === 1">
                        <el-button size="small" class="uc-op-btn" type="primary" @click="processOrder(scope.row.orderId, 2)">确认收货</el-button>
                        <el-button size="small" class="uc-op-btn" type="danger" @click="cancelOrder(scope.row)">取消订单</el-button>
                      </template>
                      <template v-if="currentTab === 'sell' && scope.row.tradeType === 0 && scope.row.status === 1">
                        <el-button size="small" class="uc-op-btn" type="danger" @click="cancelOrderBySeller(scope.row)">取消订单</el-button>
                      </template>
                    </div>
                  </template>
                </el-table-column>
              </el-table>

              <!-- 我发布的闲置 -->
              <el-table v-else-if="currentTab === 'published'" :data="publishedList" style="width: 100%" v-loading="loading" stripe border empty-text="暂无数据" table-layout="fixed">
                <el-table-column prop="title" label="商品名称" min-width="160" />
                <el-table-column prop="price" label="价格" width="90">
                  <template #default="scope">
                    <span style="color: #F56C6C">￥{{ scope.row.price }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="交易方式" width="120">
                  <template #default="scope">
                    <el-tag :type="scope.row.isExchange === 1 ? 'success' : 'info'">
                      {{ scope.row.isExchange === 1 ? '支持换物' : '普通出售' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="scope.row.status === 0 ? 'success' : (scope.row.status === 1 ? 'info' : 'danger')">
                      {{ scope.row.status === 0 ? '在售' : (scope.row.status === 1 ? '已售出' : '已下架') }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="发布时间" min-width="160">
                  <template #default="scope">
                    {{ formatTime(scope.row.createTime) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" fixed="right" width="110" align="center">
                  <template #default="scope">
                    <div class="uc-op">
                      <el-button size="small" class="uc-op-btn" @click="router.push('/goods/' + scope.row.goodsId)">查看</el-button>
                      <el-button v-if="scope.row.status === 0" size="small" class="uc-op-btn" type="primary" @click="editGoods(scope.row)">编辑</el-button>
                      <el-button v-if="scope.row.status === 0" size="small" class="uc-op-btn" type="warning" @click="offlineGoods(scope.row.goodsId)">下架</el-button>
                      <el-button v-if="scope.row.status === 2" size="small" class="uc-op-btn" type="success" @click="republishGoods(scope.row.goodsId)">上架</el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>

              <!-- 我的收藏 -->
              <el-table v-else-if="currentTab === 'favorites'" :data="favoriteList" style="width: 100%" v-loading="loading" stripe border empty-text="暂无数据" table-layout="fixed">
                <el-table-column prop="title" label="商品名称" min-width="160" />
                <el-table-column prop="price" label="价格" width="90">
                  <template #default="scope">
                    <span style="color: #F56C6C">￥{{ scope.row.price }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="publisherName" label="发布者" width="110" />
                <el-table-column label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="scope.row.status === 0 ? 'success' : (scope.row.status === 1 ? 'info' : 'danger')">
                      {{ scope.row.status === 0 ? '在售' : (scope.row.status === 1 ? '已售出' : '已下架') }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" fixed="right" width="110" align="center">
                  <template #default="scope">
                    <div class="uc-op">
                      <el-button size="small" class="uc-op-btn" type="primary" @click="router.push('/goods/' + scope.row.goodsId)">去看看</el-button>
                      <el-button size="small" class="uc-op-btn" type="danger" @click="removeFavorite(scope.row.goodsId)">取消收藏</el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>

              <!-- 积分兑换订单 -->
              <el-table v-else-if="currentTab === 'pointOrders'" :data="pointOrderList" style="width: 100%" v-loading="loading" stripe border empty-text="暂无数据" table-layout="fixed">
                <el-table-column prop="orderNo" label="订单号" min-width="140" />
                <el-table-column prop="itemName" label="商品名称" min-width="160" />
                <el-table-column prop="pointsUsed" label="消耗积分" width="120">
                  <template #default="scope">
                    <span style="color: #E6A23C; font-weight: bold; display: inline-flex; align-items: center;">
                      <el-icon style="margin-right: 4px; vertical-align: middle;"><Coin /></el-icon>{{ scope.row.pointsUsed }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="120">
                  <template #default="scope">
                    <el-tag :type="scope.row.status === 0 ? 'warning' : 'success'">
                      {{ scope.row.status === 0 ? '待核销' : '已完成' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="兑换时间" min-width="160">
                  <template #default="scope">
                    {{ formatTime(scope.row.createTime) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" fixed="right" width="110" align="center">
                  <template #default="scope">
                    <div class="uc-op">
                      <el-button size="small" class="uc-op-btn" @click="router.push('/point/goods/' + scope.row.itemId)">查看商品</el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
              <!-- 积分明细 -->
              <el-table v-else-if="currentTab === 'points'" :data="pointList" style="width: 100%" v-loading="loading" stripe border empty-text="暂无数据" table-layout="auto">
                <el-table-column label="变动类型" width="150">
                  <template #default="scope">
                    <el-tag :type="scope.row.changeType === 0 ? 'success' : (scope.row.changeType === 1 ? 'warning' : 'danger')">
                      {{ scope.row.changeType === 0 ? '每日签到' : (scope.row.changeType === 1 ? '交易获取' : '积分兑换') }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="积分变动" width="110">
                  <template #default="scope">
                    <span :style="{ color: scope.row.changeAmount > 0 ? '#67C23A' : '#F56C6C', fontWeight: 'bold' }">
                      {{ scope.row.changeAmount > 0 ? '+' : '' }}{{ scope.row.changeAmount }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="balanceAfter" label="变动后余额" width="110" />
                <el-table-column label="记录时间" min-width="180">
                  <template #default="scope">
                    {{ formatTime(scope.row.createTime) }}
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
    
    <!-- 复用全局发布弹窗进行编辑 -->
    <PublishDialog ref="publishDialogRef" @success="fetchPublished" />
    <el-dialog v-model="orderDetailVisible" title="订单详情" width="620px">
      <el-descriptions v-if="currentOrder" :column="2" border>
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(currentOrder.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="交易类型">{{ currentOrder.tradeType === 1 ? '以物换物' : '普通购买' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusText(currentOrder.status, currentOrder.tradeType, currentOrder) }}</el-descriptions-item>
        <el-descriptions-item label="买家">{{ currentOrder.buyerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="卖家">{{ currentOrder.sellerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="交易金额">
          <span v-if="currentOrder.tradeType === 0">￥{{ currentOrder.amount }}</span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="商品">
          {{ currentOrder.goodsTitle }}
        </el-descriptions-item>
        <el-descriptions-item label="商品图片" :span="2">
          <img v-if="getOrderCoverImage(currentOrder.goodsImage)" :src="getOrderCoverImage(currentOrder.goodsImage)" class="order-goods-img" />
          <span v-else>-</span>
        </el-descriptions-item>
        <template v-if="currentOrder.tradeType === 1">
          <el-descriptions-item label="交换物品" :span="2">
            <div v-if="currentOrder.exchangeGoodsTitle">{{ currentOrder.exchangeGoodsTitle }}</div>
            <div v-else>-</div>
            <img
              v-if="getOrderCoverImage(currentOrder.exchangeGoodsImage)"
              :src="getOrderCoverImage(currentOrder.exchangeGoodsImage)"
              class="order-goods-img order-exchange-img"
              style="cursor: pointer; margin-top: 10px;"
              @click="goToOrderGoods(currentOrder.exchangeGoodsId)"
            />
          </el-descriptions-item>
        </template>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="orderDetailVisible = false">关闭</el-button>
          <el-button v-if="currentOrder" type="primary" @click="goToOrderGoods(currentOrder.goodsId)">商品详情</el-button>
          <el-button v-if="currentOrder && currentOrder.tradeType === 1 && currentOrder.exchangeGoodsId" @click="goToOrderGoods(currentOrder.exchangeGoodsId)">交换物详情</el-button>
          <el-button v-if="currentOrder && currentOrder.tradeType === 1 && currentOrder.status === 0 && currentOrder.buyerId === userStore.userInfo.userId" type="danger" @click="cancelOrder(currentOrder)">取消换物</el-button>
          <el-button v-if="currentOrder && currentOrder.tradeType === 0 && currentOrder.status === 1 && currentOrder.buyerId === userStore.userInfo.userId" type="danger" @click="cancelOrder(currentOrder)">取消订单</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px" @close="resetPasswordForm">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPassword" :loading="passwordLoading">确认修改</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/user'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navbar from '../components/Navbar.vue'
import { Coin } from '@element-plus/icons-vue'
import PublishDialog from '../components/PublishDialog.vue'
import { formatTime } from '../utils/time'
import { getCoverImage } from '../utils/image'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const publishDialogRef = ref(null)

const currentTab = ref('profile')
const orderList = ref([])
const publishedList = ref([])
const favoriteList = ref([])
const pointList = ref([])
const pointOrderList = ref([])
const loading = ref(false)

const orderDetailVisible = ref(false)
const currentOrder = ref(null)

const profileForm = ref({})
const savingProfile = ref(false)

const passwordDialogVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref(null)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const resetPasswordForm = () => {
  if (passwordFormRef.value) {
    passwordFormRef.value.resetFields()
  }
}

const submitPassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        await request.post('/api/user/updatePassword', {
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword
        })
        ElMessage.success('密码修改成功，请重新登录')
        passwordDialogVisible.value = false
        userStore.clearToken()
        userStore.showLoginDialog = true
        router.push('/')
      } catch (error) {
        console.error(error)
      } finally {
        passwordLoading.value = false
      }
    }
  })
}

const getTabTitle = (tab) => {
  const map = {
    profile: '个人资料',
    buy: '我的买入/换入订单',
    sell: '我的卖出/换出订单',
    published: '我发布的闲置',
    favorites: '我的收藏',
    pointOrders: '积分兑换订单',
    points: '积分明细'
  }
  return map[tab] || ''
}

const tabStorageKey = 'userCenterTab'
const availableTabs = new Set(['profile', 'buy', 'sell', 'published', 'favorites', 'pointOrders', 'points'])

const normalizeTab = (tab) => {
  if (!tab) return 'profile'
  const t = Array.isArray(tab) ? tab[0] : tab
  return availableTabs.has(t) ? t : 'profile'
}

const syncTabToRoute = (tab) => {
  const t = normalizeTab(tab)
  if (route.query.tab === t) return
  router.replace({ path: '/user', query: { ...route.query, tab: t } })
}

const loadTabData = (tab) => {
  const t = normalizeTab(tab)
  if (t === 'profile') {
    fetchProfile()
  } else if (t === 'buy' || t === 'sell') {
    fetchOrders()
  } else if (t === 'published') {
    fetchPublished()
  } else if (t === 'favorites') {
    fetchFavorites()
  } else if (t === 'pointOrders') {
    fetchPointOrders()
  } else if (t === 'points') {
    fetchPoints()
  }
}

onMounted(() => {
  if (!userStore.token) {
    userStore.showLoginDialog = true
    router.replace('/')
    return
  }
  const initialTab = normalizeTab(route.query.tab || localStorage.getItem(tabStorageKey))
  currentTab.value = initialTab
  localStorage.setItem(tabStorageKey, initialTab)
  syncTabToRoute(initialTab)
  loadTabData(initialTab)
})

const fetchProfile = async () => {
  try {
    const res = await request.get('/api/user/info')
    profileForm.value = res || {}
  } catch (error) {
    console.error(error)
  }
}

const saveProfile = async () => {
  savingProfile.value = true
  try {
    await request.post('/api/user/update', {
      nickname: profileForm.value.nickname,
      phone: profileForm.value.phone,
      password: profileForm.value.password || undefined
    })
    ElMessage.success('个人资料已更新')
    
    userStore.userInfo.nickname = profileForm.value.nickname
    userStore.userInfo.phone = profileForm.value.phone
    if (profileForm.value.password) {
      profileForm.value.password = ''
      ElMessage.success('密码已修改，请重新登录')
      userStore.clearToken()
      userStore.showLoginDialog = true
      router.push('/')
    }
  } catch (error) {
    console.error(error)
  } finally {
    savingProfile.value = false
  }
}

const handleSideSelect = (key) => {
  const nextTab = normalizeTab(key)
  if (nextTab === 'buy' || nextTab === 'sell') {
    loading.value = true
    orderList.value = []
  }
  currentTab.value = nextTab
  localStorage.setItem(tabStorageKey, nextTab)
  syncTabToRoute(nextTab)
  loadTabData(nextTab)
}

watch(
  () => route.query.tab,
  (tab) => {
    if (!userStore.token) return
    const t = normalizeTab(tab)
    if (t === currentTab.value) return
    currentTab.value = t
    localStorage.setItem(tabStorageKey, t)
    loadTabData(t)
  }
)

const fetchPointOrders = async () => {
  loading.value = true
  try {
    pointOrderList.value = await request.get('/api/point/orders')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const fetchPoints = async () => {
  loading.value = true
  try {
    pointList.value = await request.get('/api/point/records')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const fetchFavorites = async () => {
  loading.value = true
  try {
    favoriteList.value = await request.get('/api/favorite/list')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const removeFavorite = async (goodsId) => {
  ElMessageBox.confirm('确定要取消收藏吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await request.post(`/api/favorite/remove?goodsId=${goodsId}`)
      ElMessage.success('已取消收藏')
      fetchFavorites()
    } catch (error) {
      console.error(error)
    }
  }).catch(() => {})
}

const fetchPublished = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/goods/myPublished')
    publishedList.value = (res || []).filter(item => item.status !== 1)
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const editGoods = (goods) => {
  if (publishDialogRef.value) {
    publishDialogRef.value.open(goods)
  }
}

const offlineGoods = async (goodsId) => {
  ElMessageBox.confirm('确定要下架该商品吗？下架后其他用户将无法看到。', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await request.post(`/api/goods/updateStatus?goodsId=${goodsId}&status=2`)
      ElMessage.success('下架成功')
      fetchPublished()
    } catch (error) {
      console.error(error)
    }
  }).catch(() => {})
}

const republishGoods = async (goodsId) => {
  ElMessageBox.confirm('确定要重新上架该商品吗？', '提示', {
    type: 'info'
  }).then(async () => {
    try {
      await request.post(`/api/goods/updateStatus?goodsId=${goodsId}&status=0`)
      ElMessage.success('上架成功')
      fetchPublished()
    } catch (error) {
      console.error(error)
    }
  }).catch(() => {})
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const url = currentTab.value === 'buy' ? '/api/order/myBuy' : '/api/order/mySell'
    orderList.value = await request.get(url)
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const openOrderDetail = (order) => {
  currentOrder.value = order
  orderDetailVisible.value = true
}

const goToOrderGoods = (goodsId) => {
  orderDetailVisible.value = false
  router.push('/goods/' + goodsId)
}

const getOrderCoverImage = (imagesStr) => {
  if (!imagesStr) return ''
  if (typeof imagesStr !== 'string') return ''
  if (imagesStr.startsWith('http')) return imagesStr
  try {
    const images = JSON.parse(imagesStr)
    if (images && images.length > 0) {
      if (images[0].startsWith('http')) return images[0]
      return getCoverImage(imagesStr)
    }
  } catch (e) {
    if (imagesStr.startsWith('/')) return getCoverImage(imagesStr)
  }
  return ''
}

const getStatusText = (status, tradeType, order = null) => {
  if (tradeType === 1) { // 换物
    const map = { 0: '待卖家同意', 1: '换物成功', 2: '已完成', 3: '已取消', 4: '已拒绝' }
    return map[status] || '未知'
  } else { // 购买
    const map = { 0: '待处理', 1: '待收货', 2: '已完成', 3: '已取消' }
    return map[status] || '未知'
  }
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'primary', 2: 'success', 3: 'info', 4: 'danger' }
  return map[status] || 'info'
}

const processOrder = (orderId, status) => {
  let actionText = status === 1 ? '同意换物请求' : (status === 2 ? '确认收货' : '拒绝换物请求')
  
  ElMessageBox.confirm(`确定要${actionText}吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post(`/api/order/process?orderId=${orderId}&status=${status}`)
      ElMessage.success('操作成功')
      fetchOrders()
    } catch (error) {
      ElMessage.error(error?.message || '操作失败')
    }
  }).catch(() => {})
}

const cancelOrder = (order) => {
  const isExchange = order && order.tradeType === 1
  const msg = isExchange ? '确定要取消该换物订单吗？' : '确定要取消该订单吗？取消后商品将重新上架。'
  ElMessageBox.confirm(msg, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post(`/api/order/cancel?orderId=${order.orderId}`)
      ElMessage.success(isExchange ? '换物订单已取消' : '订单已取消')
      fetchOrders()
    } catch (error) {
      ElMessage.error(error?.message || '取消失败')
    }
  }).catch(() => {})
}

const cancelOrderBySeller = (order) => {
  const isExchange = order && order.tradeType === 1
  const msg = isExchange ? '确定要取消该换物订单吗？取消后双方商品将重新上架。' : '确定要取消该订单吗？取消后商品将重新上架。'
  ElMessageBox.confirm(msg, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post(`/api/order/cancelBySeller?orderId=${order.orderId}`)
      ElMessage.success(isExchange ? '换物订单已取消' : '订单已取消')
      fetchOrders()
    } catch (error) {
      ElMessage.error(error?.message || '取消失败')
    }
  }).catch(() => {})
}


</script>

<style scoped>
.user-center {
  min-height: 100vh;
  background-color: var(--color-bg-page);
  font-family: var(--font-family);
}
.el-menu-item:hover {
  color: var(--color-primary) !important;
  background-color: var(--color-primary-bg) !important;
}
.el-main {
  padding: 30px 5%;
}
.side-menu {
  height: calc(100vh - 130px);
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
  padding: 10px 0;
  border-right: none;
}
.order-goods-img {
  width: 160px;
  height: 120px;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid #ebeef5;
}
.uc-op {
  display: grid;
  grid-template-columns: 92px;
  justify-content: center;
  justify-items: center;
  gap: 8px;
}
.uc-op-btn {
}
.uc-op :deep(.uc-op-btn) {
  width: 92px !important;
  min-width: 92px !important;
  justify-content: center;
  margin-left: 0 !important;
}
.uc-op-row {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
}
.uc-op-row .uc-op-btn {
  width: auto;
}
:deep(.el-table__fixed-right) {
  box-shadow: -1px 0 0 #ebeef5;
}
:deep(.el-table__fixed-right-patch) {
  box-shadow: -1px 0 0 #ebeef5;
}
:deep(.el-table__border-left-patch) {
  box-shadow: -1px 0 0 #ebeef5;
}
:deep(.el-table__fixed-right::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 1px;
  background: #ebeef5;
  z-index: 5;
}
:deep(.el-table__fixed-right-patch::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 1px;
  background: #ebeef5;
  z-index: 5;
}
:deep(.el-card) {
  border-radius: var(--radius-md);
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
}
.card-header {
  font-size: 18px;
  font-weight: bold;
  color: var(--color-text-body);
  padding: 5px 0;
}
:deep(.el-table) {
  border-radius: var(--radius-sm);
  overflow: visible;
}
:deep(.el-table__inner-wrapper) {
  border-radius: var(--radius-sm);
  overflow: hidden;
}
:deep(.el-table th.el-table__cell) {
  background-color: var(--color-bg-input);
  color: #444;
  font-weight: 600;
}
</style>
