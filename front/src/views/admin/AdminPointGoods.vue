<template>
  <div class="admin-point-goods">
    <el-card class="toolbar">
      <div class="toolbar-row">
        <el-input v-model="query.keyword" placeholder="搜索名称/描述" clearable style="width: 260px" @keyup.enter="fetchList" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 150px">
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
        <el-button type="primary" @click="fetchList">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <div class="spacer" />
        <el-button type="primary" @click="openCreate">新增积分商品</el-button>
      </div>
    </el-card>

    <el-card>
      <el-table :data="list" v-loading="loading" stripe border table-layout="auto">
        <el-table-column prop="itemId" label="ID" width="90" />
        <el-table-column label="图片" width="90">
          <template #default="scope">
            <img v-if="getImageUrl(scope.row.image)" :src="getImageUrl(scope.row.image)" class="img" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="所需积分" width="110">
          <template #default="scope">
            {{ scope.row.pointsRequired }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '上架' : '下架' }}
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
              <el-button size="small" class="op-btn" @click="openEdit(scope.row)">编辑</el-button>
              <el-button size="small" class="op-btn" :type="scope.row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(scope.row)">
                {{ scope.row.status === 1 ? '下架' : '上架' }}
              </el-button>
              <el-button size="small" class="op-btn" @click="openStock(scope.row)">改库存</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="图片" prop="image">
          <el-upload
            action="http://localhost:8080/api/file/upload"
            :headers="uploadHeaders"
            list-type="picture-card"
            v-model:file-list="fileList"
            :limit="1"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
            :before-upload="beforeUpload"
            :on-exceed="handleExceed"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所需积分" prop="pointsRequired">
              <el-input-number v-model="form.pointsRequired" :min="0" :max="999999" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" :max="999999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="stockVisible" title="修改库存" width="420px" @close="resetStock">
      <el-form :model="stockForm" ref="stockRef" label-width="100px">
        <el-form-item label="库存">
          <el-input-number v-model="stockForm.stock" :min="0" :max="999999" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockVisible = false">取消</el-button>
        <el-button type="primary" :loading="stockSaving" @click="submitStock">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '../../utils/request'
import { formatTime } from '../../utils/time'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()

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
    const res = await request.get('/api/admin/point/goods/list', {
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

const getImageUrl = (image) => {
  if (!image) return ''
  if (String(image).startsWith('http')) return image
  return 'http://localhost:8080' + image
}

const dialogVisible = ref(false)
const dialogMode = ref('create')
const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增积分商品' : '编辑积分商品'))
const formRef = ref(null)
const saving = ref(false)
const fileList = ref([])

const uploadHeaders = computed(() => ({
  Authorization: userStore.adminToken
}))

const form = reactive({
  itemId: null,
  name: '',
  description: '',
  image: '',
  pointsRequired: 0,
  stock: 0,
  status: 1
})

const rules = reactive({
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  image: [{ required: true, message: '请上传图片', trigger: 'change' }],
  pointsRequired: [{ required: true, message: '请输入所需积分', trigger: 'change' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'change' }]
})

const beforeUpload = (file) => {
  const isImage = file.type && file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('仅支持上传图片文件')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    form.image = response.data
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

const handleRemove = () => {
  form.image = ''
}

const handleExceed = (files) => {
  fileList.value = []
  form.image = ''
  fileList.value = [files[0]]
}

const openCreate = () => {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  dialogMode.value = 'edit'
  resetForm()
  form.itemId = row.itemId
  form.name = row.name
  form.description = row.description || ''
  form.image = row.image || ''
  form.pointsRequired = row.pointsRequired ?? 0
  form.stock = row.stock ?? 0
  form.status = row.status ?? 1
  if (form.image) {
    fileList.value = [{ name: form.image, url: getImageUrl(form.image) }]
  }
  dialogVisible.value = true
}

const resetForm = () => {
  if (formRef.value) formRef.value.clearValidate()
  form.itemId = null
  form.name = ''
  form.description = ''
  form.image = ''
  form.pointsRequired = 0
  form.stock = 0
  form.status = 1
  fileList.value = []
}

const submit = async () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (dialogMode.value === 'create') {
        await request.post('/api/admin/point/goods/create', form)
        ElMessage.success('已创建')
      } else {
        await request.post('/api/admin/point/goods/update', form)
        ElMessage.success('已保存')
      }
      dialogVisible.value = false
      fetchList()
    } catch (e) {
    } finally {
      saving.value = false
    }
  })
}

const toggleStatus = (row) => {
  const next = row.status === 1 ? 0 : 1
  const text = next === 1 ? '上架' : '下架'
  ElMessageBox.confirm(`确定要${text}该积分商品吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post(`/api/admin/point/goods/updateStatus?itemId=${row.itemId}&status=${next}`)
      ElMessage.success('操作成功')
      fetchList()
    } catch (e) {
    }
  }).catch(() => {})
}

const stockVisible = ref(false)
const stockSaving = ref(false)
const stockRef = ref(null)
const stockForm = reactive({
  itemId: null,
  stock: 0
})

const openStock = (row) => {
  stockForm.itemId = row.itemId
  stockForm.stock = row.stock ?? 0
  stockVisible.value = true
}

const resetStock = () => {
  stockForm.itemId = null
  stockForm.stock = 0
}

const submitStock = async () => {
  if (!stockForm.itemId && stockForm.itemId !== 0) return
  stockSaving.value = true
  try {
    await request.post(`/api/admin/point/goods/updateStock?itemId=${stockForm.itemId}&stock=${stockForm.stock}`)
    ElMessage.success('已更新')
    stockVisible.value = false
    fetchList()
  } catch (e) {
  } finally {
    stockSaving.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.admin-point-goods {
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
.spacer {
  flex: 1;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
.img {
  width: 56px;
  height: 56px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #ebeef5;
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
