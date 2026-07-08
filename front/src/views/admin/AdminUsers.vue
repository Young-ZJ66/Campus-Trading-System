<template>
  <div class="admin-users">
    <el-card class="toolbar">
      <div class="toolbar-row">
        <el-input v-model="query.keyword" placeholder="搜索学号/昵称/手机号" clearable style="width: 280px" @keyup.enter="fetchList" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 150px">
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="fetchList">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>
    </el-card>

    <el-card>
      <el-table :data="list" v-loading="loading" stripe border table-layout="auto">
        <el-table-column prop="userId" label="ID" width="90" />
        <el-table-column prop="studentNo" label="学号/账号" width="140" show-overflow-tooltip />
        <el-table-column prop="nickname" label="昵称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="140" show-overflow-tooltip />
        <el-table-column label="积分" width="90">
          <template #default="scope">
            {{ scope.row.points ?? 0 }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" min-width="160">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="110" align="center">
          <template #default="scope">
            <div class="op-grid">
              <el-button size="small" class="op-btn" @click="openPoints(scope.row)">改积分</el-button>
              <el-button size="small" class="op-btn" :disabled="scope.row.studentNo === 'admin'" :type="scope.row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(scope.row)">
                {{ scope.row.status === 1 ? '禁用' : '启用' }}
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

    <el-dialog v-model="pointsVisible" title="调整积分" width="420px" @close="resetPoints">
      <el-form :model="pointsForm" label-width="90px">
        <el-form-item label="用户">
          <span>{{ pointsForm.nickname }}（{{ pointsForm.studentNo }}）</span>
        </el-form-item>
        <el-form-item label="积分">
          <el-input-number v-model="pointsForm.points" :min="0" :max="999999" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pointsVisible = false">取消</el-button>
        <el-button type="primary" :loading="pointsSaving" @click="submitPoints">保存</el-button>
      </template>
    </el-dialog>
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
    const res = await request.get('/api/admin/user/list', {
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

const toggleStatus = (row) => {
  const next = row.status === 1 ? 0 : 1
  const text = next === 1 ? '启用' : '禁用'
  ElMessageBox.confirm(`确定要${text}该用户吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post(`/api/admin/user/updateStatus?userId=${row.userId}&status=${next}`)
      ElMessage.success('操作成功')
      fetchList()
    } catch (e) {
    }
  }).catch(() => {})
}

const pointsVisible = ref(false)
const pointsSaving = ref(false)
const pointsForm = reactive({
  userId: null,
  studentNo: '',
  nickname: '',
  points: 0
})

const openPoints = (row) => {
  pointsForm.userId = row.userId
  pointsForm.studentNo = row.studentNo
  pointsForm.nickname = row.nickname
  pointsForm.points = row.points ?? 0
  pointsVisible.value = true
}

const resetPoints = () => {
  pointsForm.userId = null
  pointsForm.studentNo = ''
  pointsForm.nickname = ''
  pointsForm.points = 0
}

const submitPoints = async () => {
  if (!pointsForm.userId && pointsForm.userId !== 0) return
  pointsSaving.value = true
  try {
    await request.post(`/api/admin/user/updatePoints?userId=${pointsForm.userId}&points=${pointsForm.points}`)
    ElMessage.success('已更新')
    pointsVisible.value = false
    fetchList()
  } catch (e) {
  } finally {
    pointsSaving.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.admin-users {
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
</style>
