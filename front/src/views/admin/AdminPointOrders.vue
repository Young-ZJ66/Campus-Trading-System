<template>
  <div class="admin-point-orders">
    <el-card class="toolbar">
      <div class="toolbar-row">
        <el-input v-model="query.keyword" placeholder="搜索订单号/商品/用户" clearable style="width: 280px" @keyup.enter="fetchList" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 150px">
          <el-option label="待核销" :value="0" />
          <el-option label="已完成" :value="1" />
        </el-select>
        <el-button type="primary" @click="fetchList">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>
    </el-card>

    <el-card>
      <el-table :data="list" v-loading="loading" stripe border table-layout="auto">
        <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="itemName" label="商品" min-width="160" show-overflow-tooltip />
        <el-table-column label="用户" width="160">
          <template #default="scope">
            <div class="user-cell">
              <div class="nickname">{{ scope.row.userNickname || '-' }}</div>
              <div class="studentno">{{ scope.row.userStudentNo || '-' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="消耗积分" width="110">
          <template #default="scope">
            {{ scope.row.pointsUsed }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'warning' : 'success'">
              {{ scope.row.status === 0 ? '待核销' : '已完成' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="160">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="更新时间" min-width="160">
          <template #default="scope">
            {{ formatTime(scope.row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="110" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" :disabled="scope.row.status !== 0" @click="verify(scope.row)">核销</el-button>
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
  status: null
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/admin/point/orders/list', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        keyword: query.keyword || undefined,
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
  query.status = null
  pageNum.value = 1
  fetchList()
}

const verify = (row) => {
  ElMessageBox.confirm('确定要核销该订单吗？核销后不可撤回。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post(`/api/admin/point/orders/verify?orderId=${row.orderId}`)
      ElMessage.success('已核销')
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
.admin-point-orders {
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
.user-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.nickname {
  color: #303133;
}
.studentno {
  font-size: 12px;
  color: #909399;
}
</style>
