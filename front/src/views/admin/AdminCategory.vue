<template>
  <div class="admin-category">
    <el-card class="toolbar">
      <div class="toolbar-row">
        <el-input v-model="keyword" placeholder="搜索分类名称" clearable style="width: 260px" @keyup.enter="fetchList" />
        <el-select v-model="status" placeholder="状态" clearable style="width: 150px">
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="fetchList">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <div class="spacer" />
        <el-button type="primary" @click="openCreate">新增分类</el-button>
      </div>
    </el-card>

    <el-card>
      <el-table :data="list" v-loading="loading" stripe border table-layout="auto">
        <el-table-column prop="categoryId" label="ID" width="90" />
        <el-table-column prop="name" label="分类名称" min-width="220" show-overflow-tooltip />
        <el-table-column label="父级" width="120">
          <template #default="scope">
            {{ scope.row.parentId === 0 ? '顶级' : scope.row.parentId }}
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="110" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="110" align="center">
          <template #default="scope">
            <div class="op-grid">
              <el-button size="small" class="op-btn" @click="openEdit(scope.row)">编辑</el-button>
              <el-button size="small" class="op-btn" :type="scope.row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(scope.row)">
                {{ scope.row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button size="small" class="op-btn" type="danger" @click="remove(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="父级分类" prop="parentId">
          <el-select v-model="form.parentId" style="width: 100%">
            <el-option label="顶级(0)" :value="0" />
            <el-option v-for="c in parentOptions" :key="c.categoryId" :label="`${c.name}(${c.categoryId})`" :value="c.categoryId" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
import request from '../../utils/request'

const list = ref([])
const loading = ref(false)
const keyword = ref('')
const status = ref(null)

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/admin/category/list', {
      params: {
        keyword: keyword.value || undefined,
        status: status.value ?? undefined
      }
    })
    list.value = res || []
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  keyword.value = ''
  status.value = null
  fetchList()
}

const dialogVisible = ref(false)
const dialogMode = ref('create')
const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增分类' : '编辑分类'))
const formRef = ref(null)
const saving = ref(false)

const form = reactive({
  categoryId: null,
  name: '',
  parentId: 0,
  sort: 0,
  status: 1
})

const rules = reactive({
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
})

const parentOptions = computed(() => list.value.filter(c => c.status === 1 && c.categoryId !== form.categoryId))

const openCreate = () => {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  dialogMode.value = 'edit'
  resetForm()
  form.categoryId = row.categoryId
  form.name = row.name
  form.parentId = row.parentId ?? 0
  form.sort = row.sort ?? 0
  form.status = row.status ?? 1
  dialogVisible.value = true
}

const resetForm = () => {
  if (formRef.value) formRef.value.clearValidate()
  form.categoryId = null
  form.name = ''
  form.parentId = 0
  form.sort = 0
  form.status = 1
}

const submit = async () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (dialogMode.value === 'create') {
        await request.post('/api/admin/category/create', form)
        ElMessage.success('已创建')
      } else {
        await request.post('/api/admin/category/update', form)
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
  const next = row.status === 1 ? 0 : 1
  try {
    await request.post(`/api/admin/category/updateStatus?categoryId=${row.categoryId}&status=${next}`)
    ElMessage.success('操作成功')
    fetchList()
  } catch (e) {
  }
}

const remove = async (row) => {
  ElMessageBox.confirm('确定要删除该分类吗？删除后将变为禁用状态。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post(`/api/admin/category/delete?categoryId=${row.categoryId}`)
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
.admin-category {
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
