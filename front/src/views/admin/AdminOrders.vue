<template>
  <div class="admin-orders">
    <el-card class="toolbar">
      <div class="toolbar-row">
        <el-input v-model="query.keyword" placeholder="搜索订单号/商品/用户" clearable style="width: 280px" @keyup.enter="fetchList" />
        <el-select v-model="query.tradeType" placeholder="类型" clearable style="width: 150px">
          <el-option label="普通购买" :value="0" />
          <el-option label="以物换物" :value="1" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 150px">
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-button type="primary" @click="fetchList">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>
    </el-card>

    <el-card>
      <el-table :data="list" v-loading="loading" stripe border table-layout="auto">
        <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
        <el-table-column label="类型" width="110">
          <template #default="scope">
            <el-tag :type="scope.row.tradeType === 1 ? 'warning' : 'success'">
              {{ scope.row.tradeType === 1 ? '换物' : '购买' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="商品" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row.goodsTitle || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="买家" width="100" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row.buyerName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="卖家" width="100" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row.sellerName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="金额" width="110">
          <template #default="scope">
            {{ scope.row.tradeType === 1 ? '-' : `￥${scope.row.amount}` }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="130">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.tradeType, scope.row.status)">
              {{ getStatusText(scope.row.tradeType, scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="160">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="110" align="center">
          <template #default="scope">
            <div class="op-grid">
              <el-button size="small" class="op-btn" @click="openDetail(scope.row.orderId)">查看</el-button>
              <el-button size="small" class="op-btn" type="danger" :disabled="!canForceCancel(scope.row)" @click="forceCancel(scope.row)">强制取消</el-button>
              <el-button size="small" class="op-btn" type="success" :disabled="!canForceComplete(scope.row)" @click="forceComplete(scope.row)">强制完成</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          v-model:current-page="pageNum"
          :page-size="pageSize"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="订单详情" width="720px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ detail.orderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detail.tradeType === 1 ? '以物换物' : '普通购买' }}</el-descriptions-item>
        <el-descriptions-item label="买家">{{ detail.buyerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="卖家">{{ detail.sellerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detail.tradeType, detail.status)">
            {{ getStatusText(detail.tradeType, detail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="金额" v-if="detail.tradeType === 0">￥{{ detail.amount }}</el-descriptions-item>
        <el-descriptions-item label="金额" v-else>-</el-descriptions-item>
      </el-descriptions>

      <div class="goods-block">
        <div class="block-title">被购买/被换商品</div>
        <div class="goods-row">
          <img v-if="getCover(detail.goodsImage)" :src="getCover(detail.goodsImage)" class="cover" />
          <div class="goods-info">
            <div class="goods-title">{{ detail.goodsTitle || '-' }}</div>
          </div>
        </div>
      </div>

      <div class="goods-block" v-if="detail.tradeType === 1">
        <div class="block-title">用于交换的商品</div>
        <div class="goods-row">
          <img v-if="getCover(detail.exchangeGoodsImage)" :src="getCover(detail.exchangeGoodsImage)" class="cover" />
          <div class="goods-info">
            <div class="goods-title">{{ detail.exchangeGoodsTitle || '-' }}</div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="danger" :disabled="!canForceCancel(detail)" @click="forceCancel(detail)">强制取消</el-button>
        <el-button type="success" :disabled="!canForceComplete(detail)" @click="forceComplete(detail)">强制完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import { formatTime } from '../../utils/time'

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const query = reactive({
  keyword: '',
  tradeType: null,
  status: null
})

const statusOptions = computed(() => {
  if (query.tradeType === 0) {
    return [
      { value: 1, label: '待收货' },
      { value: 2, label: '已完成' },
      { value: 3, label: '已取消' }
    ]
  }
  if (query.tradeType === 1) {
    return [
      { value: 0, label: '待卖家同意' },
      { value: 1, label: '已同意' },
      { value: 2, label: '已完成' },
      { value: 3, label: '已取消' },
      { value: 4, label: '已拒绝' }
    ]
  }
  return [
    { value: 1, label: '待收货/已同意' },
    { value: 2, label: '已完成' },
    { value: 3, label: '已取消' },
    { value: 0, label: '待卖家同意' },
    { value: 4, label: '已拒绝' }
  ]
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/admin/order/list', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        keyword: query.keyword || undefined,
        tradeType: query.tradeType ?? undefined,
        status: query.status ?? undefined
      }
    })
    list.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.keyword = ''
  query.tradeType = null
  query.status = null
  pageNum.value = 1
  fetchList()
}

const getStatusText = (tradeType, status) => {
  if (tradeType === 0) {
    const map = { 1: '待收货', 2: '已完成', 3: '已取消' }
    return map[status] || '未知'
  }
  const map = { 0: '待卖家同意', 1: '已同意', 2: '已完成', 3: '已取消', 4: '已拒绝' }
  return map[status] || '未知'
}

const getStatusType = (tradeType, status) => {
  if (tradeType === 0) {
    const map = { 1: 'warning', 2: 'success', 3: 'info' }
    return map[status] || 'info'
  }
  const map = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info', 4: 'danger' }
  return map[status] || 'info'
}

const getCover = (imagesStr) => {
  if (!imagesStr) return ''
  try {
    const urls = JSON.parse(imagesStr)
    if (Array.isArray(urls) && urls.length > 0) {
      const u = urls[0]
      return String(u).startsWith('http') ? u : (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080') + u
    }
  } catch (e) {
  }
  if (String(imagesStr).startsWith('http')) return imagesStr
  if (String(imagesStr).startsWith('/')) return (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080') + imagesStr
  return ''
}

const detailVisible = ref(false)
const detail = reactive({})

const openDetail = async (orderId) => {
  const res = await request.get('/api/admin/order/detail/' + orderId)
  Object.keys(detail).forEach(k => delete detail[k])
  Object.assign(detail, res || {})
  detailVisible.value = true
}

const canForceCancel = (row) => {
  if (!row) return false
  if (row.tradeType === 0) return row.status === 1
  if (row.tradeType === 1) return row.status === 0 || row.status === 1
  return false
}

const canForceComplete = (row) => {
  if (!row) return false
  if (row.tradeType === 0) return row.status === 1
  if (row.tradeType === 1) return row.status === 0 || row.status === 1
  return false
}

const forceCancel = (row) => {
  ElMessageBox.confirm('确定要强制取消该订单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.post(`/api/admin/order/forceCancel?orderId=${row.orderId}`)
    ElMessage.success('已取消')
    fetchList()
  }).catch(() => {})
}

const forceComplete = (row) => {
  ElMessageBox.confirm('确定要强制完成该订单吗？完成后将发放交易积分奖励。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.post(`/api/admin/order/forceComplete?orderId=${row.orderId}`)
    ElMessage.success('已完成')
    fetchList()
  }).catch(() => {})
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.admin-orders {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.toolbar {
  border-radius: 12px;
}
.toolbar-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
.op-grid {
  display: grid;
  grid-template-columns: 92px;
  gap: 8px;
  justify-content: center;
}
.op-grid :deep(.el-button) {
  margin-left: 0;
}
.goods-block {
  margin-top: 14px;
}
.block-title {
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 10px;
}
.goods-row {
  display: flex;
  gap: 12px;
  align-items: center;
}
.cover {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid #ebeef5;
}
.goods-title {
  font-weight: 600;
  color: #303133;
}
</style>
