<template>
  <el-popover placement="bottom-end" :width="360" trigger="click" @show="handleShow">
    <template #reference>
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notification-badge">
        <el-icon class="notification-bell" :size="20"><Bell /></el-icon>
      </el-badge>
    </template>

    <div class="notification-panel">
      <div class="panel-header">
        <span class="panel-title">通知中心</span>
        <el-button v-if="notifications.length > 0" type="text" size="small" @click="clearAll">
          全部已读
        </el-button>
      </div>

      <el-scrollbar max-height="320px">
        <div v-if="notifications.length === 0" class="empty-state">
          <el-icon :size="32" color="#c0c4cc"><Bell /></el-icon>
          <p>暂无新通知</p>
        </div>
        <div v-else class="notification-list">
          <div
            v-for="(item, index) in notifications"
            :key="index"
            class="notification-item"
            @click="handleClick(item)"
          >
            <el-icon class="item-icon" :class="getTypeClass(item.type)">
              <component :is="getTypeIcon(item.type)" />
            </el-icon>
            <div class="item-content">
              <div class="item-title">{{ item.title }}</div>
              <div class="item-text">{{ item.content }}</div>
              <div class="item-time">{{ formatTime(item.time) }}</div>
            </div>
          </div>
        </div>
      </el-scrollbar>
    </div>
  </el-popover>
</template>

<script setup>
import { Bell, ShoppingCart, CircleCheck, ChatDotRound, WarningFilled } from '@element-plus/icons-vue'
import { formatTime } from '../utils/time'

const props = defineProps({
  notifications: { type: Array, default: () => [] },
  unreadCount: { type: Number, default: 0 }
})

const emit = defineEmits(['clear', 'click'])

const handleShow = () => {
  // 展开时不做额外操作
}

const clearAll = () => {
  emit('clear')
}

const handleClick = (item) => {
  emit('click', item)
}

const getTypeIcon = (type) => {
  switch (type) {
    case 'ORDER_CREATED': return ShoppingCart
    case 'ORDER_APPROVED':
    case 'ORDER_COMPLETED': return CircleCheck
    case 'COMMENT_ADDED': return ChatDotRound
    default: return Bell
  }
}

const getTypeClass = (type) => {
  switch (type) {
    case 'ORDER_CREATED': return 'type-info'
    case 'ORDER_APPROVED':
    case 'ORDER_COMPLETED': return 'type-success'
    case 'ORDER_CANCELLED': return 'type-warning'
    case 'COMMENT_ADDED': return 'type-info'
    default: return ''
  }
}
</script>

<style scoped>
.notification-panel {
  padding: 0;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border-light, #f1f5f9);
}
.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-body, #2c3e50);
}
.empty-state {
  text-align: center;
  padding: 32px 16px;
  color: var(--color-text-muted, #909399);
}
.empty-state p {
  margin: 8px 0 0;
  font-size: 13px;
}
.notification-list {
  padding: 0;
}
.notification-item {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid var(--color-border-light, #f1f5f9);
}
.notification-item:hover {
  background: var(--color-bg-hover, #f1f5f9);
}
.notification-item:last-child {
  border-bottom: none;
}
.item-icon {
  font-size: 20px;
  flex-shrink: 0;
  margin-top: 2px;
}
.item-icon.type-info { color: var(--color-primary, #ff6b81); }
.item-icon.type-success { color: var(--color-success, #67c23a); }
.item-icon.type-warning { color: var(--color-warning, #f59e0b); }
.item-content {
  flex: 1;
  min-width: 0;
}
.item-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-body, #2c3e50);
  margin-bottom: 4px;
}
.item-text {
  font-size: 13px;
  color: var(--color-text-secondary, #64748b);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item-time {
  font-size: 12px;
  color: var(--color-text-muted, #909399);
  margin-top: 4px;
}
.notification-badge {
  cursor: pointer;
}
.notification-bell {
  color: var(--color-text-secondary, #64748b);
  transition: color 0.2s;
}
.notification-bell:hover {
  color: var(--color-primary, #ff6b81);
}
</style>