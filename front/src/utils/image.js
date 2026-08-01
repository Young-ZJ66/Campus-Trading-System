/**
 * 统一处理图片 URL：将后端相对路径拼接为完整可访问的地址
 * @param {string} imagesStr - 图片路径字符串，可以是 JSON 数组、相对路径或完整 URL
 * @returns {string} 第一张图片的完整 URL，无图返回空字符串
 */
export const getCoverImage = (imagesStr) => {
  if (!imagesStr) return ''
  if (typeof imagesStr !== 'string') return ''
  const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

  // 完整 URL 直接返回
  if (imagesStr.startsWith('http')) return imagesStr

  // 尝试解析 JSON 数组
  try {
    const images = JSON.parse(imagesStr)
    if (images && images.length > 0) {
      if (images[0].startsWith('http')) return images[0]
      return baseURL + images[0]
    }
  } catch (e) {
    // 非JSON，作为纯路径处理
    if (imagesStr.startsWith('/')) return baseURL + imagesStr
  }
  return ''
}

/**
 * 获取上传接口完整地址（el-upload action 使用）
 */
export const getUploadUrl = () => {
  const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  return baseURL + '/api/file/upload'
}
