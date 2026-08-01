<template>
  <el-dialog v-model="visible" width="700px" destroy-on-close>
    <template #header>
      <div style="display: flex; align-items: center; font-size: 18px; font-weight: bold; color: var(--color-text-body);">
        <el-icon style="margin-right: 8px; color: var(--color-primary);"><Plus v-if="!isEdit" /><Edit v-else /></el-icon>
        {{ isEdit ? '编辑闲置物品' : '发布闲置物品' }}
      </div>
    </template>
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="publish-form">
      <el-form-item label="物品标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入物品标题（如：9成新 苹果耳机）" maxlength="50" show-word-limit />
      </el-form-item>
      
      <el-form-item label="物品分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
          <el-option v-for="item in categoryList" :key="item.categoryId" :label="item.name" :value="item.categoryId" />
        </el-select>
      </el-form-item>

      <el-form-item label="物品图片">
        <el-upload
          :action="uploadUrl"
          :headers="headers"
          list-type="picture-card"
          v-model:file-list="fileList"
          :on-success="handleUploadSuccess"
          :on-remove="handleRemove"
          :limit="5"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
        <div class="el-upload__tip">只能上传 jpg/png 文件，且不超过 5 张</div>
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="原价" prop="originalPrice">
            <el-input-number v-model="form.originalPrice" :min="0" :precision="2" :step="1" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="出售价格" prop="price">
            <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="支持换物" prop="isExchange">
        <el-switch v-model="form.isExchange" :active-value="1" :inactive-value="0" />
      </el-form-item>

      <el-form-item label="期望换取" prop="exchangeDesc" v-if="form.isExchange === 1">
        <el-input v-model="form.exchangeDesc" placeholder="请输入您期望换取的物品类型或具体物品" />
      </el-form-item>

      <el-form-item label="详细描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="请详细描述物品的新旧程度、购买时间、转手原因等信息"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" class="submit-btn" @click="submitForm" :loading="loading">{{ isEdit ? '保存修改' : '确认发布' }}</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { Plus, Edit } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { getUploadUrl, getCoverImage } from '../utils/image'

const visible = ref(false)
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const categoryList = ref([])
const imageUrls = ref([])

// 上传地址和请求头（响应式，token 变化时自动更新）
const uploadUrl = getUploadUrl()
const headers = computed(() => ({
  Authorization: userStore.token
}))

const defaultForm = {
  title: '',
  categoryId: null,
  originalPrice: undefined,
  price: undefined,
  isExchange: 0,
  exchangeDesc: '',
  description: '',
  images: ''
}

const form = reactive({ ...defaultForm })

const rules = reactive({
  title: [{ required: true, message: '请输入物品标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入出售价格', trigger: 'blur' }],
  description: [{ required: true, message: '请输入详细描述', trigger: 'blur' }],
  exchangeDesc: [{ required: true, message: '请输入期望换取的物品', trigger: 'blur' }]
})

const isEdit = ref(false)
const fileList = ref([])

const open = async (goods = null) => {
  if (goods) {
    isEdit.value = true
    Object.assign(form, goods)
    try {
      imageUrls.value = JSON.parse(goods.images)
      fileList.value = imageUrls.value.map(url => ({
        name: url,
        url: url.startsWith('http') ? url : getCoverImage(url)
      }))
    } catch (e) {
      imageUrls.value = []
      fileList.value = []
    }
  } else {
    isEdit.value = false
    Object.assign(form, defaultForm)
    imageUrls.value = []
    fileList.value = []
  }

  if (formRef.value) {
    formRef.value.resetFields()
  }
  visible.value = true
  
  if (categoryList.value.length === 0) {
    try {
      categoryList.value = await request.get('/api/category/list')
    } catch (error) {
      console.error(error)
    }
  }
}

const handleUploadSuccess = (response, uploadFile) => {
  if (response.code === 200) {
    imageUrls.value.push(response.data)
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

const handleRemove = (uploadFile) => {
  let url = ''
  if (uploadFile.response && uploadFile.response.data) {
    url = uploadFile.response.data
  } else {
    url = uploadFile.name
  }
  const index = imageUrls.value.indexOf(url)
  if (index !== -1) {
    imageUrls.value.splice(index, 1)
  }
}

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      if (imageUrls.value.length === 0) {
        ElMessage.warning('请至少上传一张物品图片')
        return
      }
      form.images = JSON.stringify(imageUrls.value)
      
      loading.value = true
      try {
        if (isEdit.value) {
          await request.post('/api/goods/update', form)
          ElMessage.success('保存成功')
        } else {
          await request.post('/api/goods/publish', form)
          ElMessage.success('发布成功')
        }
        visible.value = false
        
        emit('success')
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}

const emit = defineEmits(['success'])
defineExpose({ open })
</script>

<style scoped>
.publish-form {
  padding: 10px 20px 0;
}
:deep(.el-form-item__label) {
  font-weight: 600;
  color: #444;
}
.submit-btn {
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-primary));
  border: none;
  box-shadow: 0 4px 15px var(--color-primary-shadow);
}
.submit-btn:hover {
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-primary-dark));
  box-shadow: 0 6px 20px rgba(255, 107, 129, 0.4);
}
</style>