<template>
  <div class="admin-news">
    <el-card class="toolbar">
      <div class="toolbar-row">
        <el-input v-model="query.keyword" placeholder="搜索标题" clearable style="width: 260px" @keyup.enter="fetchList" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px">
          <el-option label="发布" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
        <el-select v-model="query.isTop" placeholder="置顶" clearable style="width: 140px">
          <el-option label="置顶" :value="1" />
          <el-option label="不置顶" :value="0" />
        </el-select>
        <el-button type="primary" @click="fetchList">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <div class="spacer" />
        <el-button type="primary" @click="openCreate">新增资讯</el-button>
      </div>
    </el-card>

    <el-card>
      <el-table :data="list" v-loading="loading" stripe border table-layout="auto">
        <el-table-column prop="newsId" label="ID" width="90" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column label="置顶" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.isTop === 1 ? 'danger' : 'info'">
              {{ scope.row.isTop === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '发布' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="阅读量" width="100">
          <template #default="scope">
            {{ scope.row.viewCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="发布时间" min-width="160">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="240" align="center">
          <template #default="scope">
            <div class="op-grid">
              <el-button size="small" class="op-btn" @click="openEdit(scope.row)">编辑</el-button>
              <el-button size="small" class="op-btn" :type="scope.row.isTop === 1 ? 'warning' : 'primary'" @click="toggleTop(scope.row)">
                {{ scope.row.isTop === 1 ? '取消置顶' : '置顶' }}
              </el-button>
              <el-button size="small" class="op-btn" :type="scope.row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(scope.row)">
                {{ scope.row.status === 1 ? '下架' : '发布' }}
              </el-button>
              <el-button size="small" class="op-btn" type="danger" @click="remove(scope.row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="封面图" prop="coverImage">
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
        <el-form-item label="置顶" prop="isTop">
          <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">发布</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="10" placeholder="支持 HTML" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
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
  status: null,
  isTop: null
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/admin/news/list', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        keyword: query.keyword || undefined,
        status: query.status ?? undefined,
        isTop: query.isTop ?? undefined
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
  query.isTop = null
  pageNum.value = 1
  fetchList()
}

const dialogVisible = ref(false)
const dialogMode = ref('create')
const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增资讯' : '编辑资讯'))
const formRef = ref(null)
const saving = ref(false)
const fileList = ref([])

const uploadHeaders = computed(() => ({
  Authorization: userStore.adminToken
}))

const form = reactive({
  newsId: null,
  title: '',
  coverImage: '',
  content: '',
  isTop: 0,
  status: 1
})

const rules = reactive({
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  coverImage: [{ required: true, message: '请上传封面图', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
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
    form.coverImage = response.data
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

const handleRemove = () => {
  form.coverImage = ''
}

const handleExceed = (files) => {
  fileList.value = []
  form.coverImage = ''
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
  form.newsId = row.newsId
  form.title = row.title
  form.coverImage = row.coverImage || ''
  form.content = row.content || ''
  form.isTop = row.isTop ?? 0
  form.status = row.status ?? 1
  if (form.coverImage) {
    fileList.value = [{
      name: form.coverImage,
      url: form.coverImage.startsWith('http') ? form.coverImage : 'http://localhost:8080' + form.coverImage
    }]
  }
  dialogVisible.value = true
}

const resetForm = () => {
  if (formRef.value) formRef.value.clearValidate()
  form.newsId = null
  form.title = ''
  form.coverImage = ''
  form.content = ''
  form.isTop = 0
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
        await request.post('/api/admin/news/create', form)
        ElMessage.success('已创建')
      } else {
        await request.post('/api/admin/news/update', form)
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

const toggleStatus = async (row) => {
  const nextStatus = row.status === 1 ? 0 : 1
  try {
    await request.post(`/api/admin/news/updateStatus?newsId=${row.newsId}&status=${nextStatus}`)
    ElMessage.success('操作成功')
    fetchList()
  } catch (e) {
  }
}

const toggleTop = async (row) => {
  const nextTop = row.isTop === 1 ? 0 : 1
  try {
    await request.post(`/api/admin/news/updateTop?newsId=${row.newsId}&isTop=${nextTop}`)
    ElMessage.success('操作成功')
    fetchList()
  } catch (e) {
  }
}

const remove = async (row) => {
  ElMessageBox.confirm('确定要删除该资讯吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post(`/api/admin/news/delete?newsId=${row.newsId}`)
      ElMessage.success('已删除')
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
.admin-news {
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
