<template>
  <div class="admin-goods">
    <el-card class="toolbar">
      <div class="toolbar-row">
        <el-input v-model="query.keyword" placeholder="搜索标题/描述" clearable style="width: 260px" @keyup.enter="fetchList" />
        <el-select v-model="query.status" placeholder="状态" style="width: 150px">
          <el-option label="全部" :value="-1" />
          <el-option label="在售" :value="0" />
          <el-option label="已售出" :value="1" />
          <el-option label="已下架" :value="2" />
        </el-select>
        <el-select v-model="query.isExchange" placeholder="换物" clearable style="width: 150px">
          <el-option label="支持换物" :value="1" />
        </el-select>
        <el-button type="primary" @click="fetchList">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>
    </el-card>

    <el-card>
      <el-table :data="list" v-loading="loading" stripe border table-layout="auto">
        <el-table-column prop="goodsId" label="ID" width="90" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="publisherName" label="发布者" width="130" show-overflow-tooltip />
        <el-table-column label="价格" width="110">
          <template #default="scope">
            ￥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column label="换物" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.isExchange === 1 ? 'success' : 'info'">
              {{ scope.row.isExchange === 1 ? '支持' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" min-width="160">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="180" align="center">
          <template #default="scope">
            <div class="op-grid">
              <el-button size="small" class="op-btn" @click="goDetail(scope.row.goodsId)">查看</el-button>
              <el-button
                size="small"
                class="op-btn"
                :disabled="scope.row.status === 1"
                :type="scope.row.status === 0 ? 'warning' : 'success'"
                @click="toggleStatus(scope.row)"
              >
                {{ scope.row.status === 0 ? '下架' : '上架' }}
              </el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import { formatTime } from '../../utils/time'

const router = useRouter()

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const query = reactive({
  keyword: '',
  status: -1,
  isExchange: null
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.post('/api/admin/goods/list', {
      keyword: query.keyword || undefined,
      status: query.status,
      isExchange: query.isExchange ?? undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
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
  query.status = -1
  query.isExchange = null
  pageNum.value = 1
  fetchList()
}

const getStatusText = (status) => {
  const map = { 0: '在售', 1: '已售出', 2: '已下架' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'success', 1: 'info', 2: 'warning' }
  return map[status] || 'info'
}

const goDetail = (goodsId) => {
  router.push('/goods/' + goodsId)
}

const toggleStatus = (row) => {
  if (row.status === 1) return
  const next = row.status === 0 ? 2 : 0
  const text = next === 2 ? '下架' : '上架'
  ElMessageBox.confirm(`确定要${text}该商品吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post(`/api/admin/goods/updateStatus?goodsId=${row.goodsId}&status=${next}`)
      ElMessage.success('操作成功')
      fetchList()
    } catch (e) {
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.admin-goods {
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
  grid-template-columns: 92px 92px;
  gap: 8px;
  justify-content: start;
}
.op-grid :deep(.el-button) {
  margin-left: 0;
}
</style>
