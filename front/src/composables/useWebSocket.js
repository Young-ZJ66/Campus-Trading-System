import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '../store/user'
import { ElNotification } from 'element-plus'

/**
 * WebSocket 连接 composable：接收实时通知
 */
export function useWebSocket() {
  const notifications = ref([])
  const unreadCount = ref(0)
  const connected = ref(false)
  let ws = null
  let reconnectTimer = null
  const userStore = useUserStore()

  const connect = () => {
    if (!userStore.token) return

    const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
    const wsUrl = baseURL.replace(/^http/, 'ws') + '/ws'

    try {
      // 原生 WebSocket 连接 STOMP 端点
      ws = new WebSocket(wsUrl)

      ws.onopen = () => {
        connected.value = true
        // 发送 STOMP CONNECT 帧
        ws.send('CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n\0')
        // 订阅用户专属通知队列
        setTimeout(() => {
          if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send('SUBSCRIBE\nid:sub-0\ndestination:/user/' + userStore.userInfo.userId + '/queue/notifications\n\n\0')
          }
        }, 500)
      }

      ws.onmessage = (event) => {
        const data = event.data
        if (data && data.startsWith('MESSAGE')) {
          try {
            const body = data.split('\n\n')[1].replace('\0', '')
            const notification = JSON.parse(body)
            notifications.value.unshift(notification)
            unreadCount.value++

            ElNotification({
              title: notification.title || '新通知',
              message: notification.content,
              type: getNotificationType(notification.type),
              duration: 5000
            })
          } catch (e) {
            // 解析失败忽略
          }
        }
      }

      ws.onclose = () => {
        connected.value = false
        // 自动重连
        if (userStore.token) {
          reconnectTimer = setTimeout(connect, 5000)
        }
      }

      ws.onerror = () => {
        connected.value = false
      }
    } catch (e) {
      // WebSocket 不支持时静默失败
    }
  }

  const disconnect = () => {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    if (ws) {
      ws.close()
      ws = null
    }
    connected.value = false
  }

  const clearUnread = () => {
    unreadCount.value = 0
  }

  return {
    notifications,
    unreadCount,
    connected,
    connect,
    disconnect,
    clearUnread
  }
}

function getNotificationType(type) {
  switch (type) {
    case 'ORDER_CREATED': return 'info'
    case 'ORDER_APPROVED': return 'success'
    case 'ORDER_COMPLETED': return 'success'
    case 'ORDER_CANCELLED': return 'warning'
    case 'COMMENT_ADDED': return 'info'
    default: return 'info'
  }
}
