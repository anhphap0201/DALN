import { ref } from 'vue'

export type NotificationType = 'success' | 'error' | 'warning' | 'info'

export interface Notification {
  id: number
  type: NotificationType
  title: string
  message?: string
  duration?: number
}

const notifications = ref<Notification[]>([])
let notificationId = 0

export const useNotification = () => {
  const addNotification = (
    type: NotificationType,
    title: string,
    message?: string,
    duration: number = 3000
  ) => {
    const id = ++notificationId
    const notification: Notification = {
      id,
      type,
      title,
      message,
      duration
    }

    notifications.value.push(notification)

    if (duration > 0) {
      setTimeout(() => {
        removeNotification(id)
      }, duration)
    }
  }

  const removeNotification = (id: number) => {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      notifications.value.splice(index, 1)
    }
  }

  const success = (title: string, message?: string, duration?: number) => {
    addNotification('success', title, message, duration)
  }

  const error = (title: string, message?: string, duration?: number) => {
    addNotification('error', title, message, duration)
  }

  const warning = (title: string, message?: string, duration?: number) => {
    addNotification('warning', title, message, duration)
  }

  const info = (title: string, message?: string, duration?: number) => {
    addNotification('info', title, message, duration)
  }

  return {
    notifications,
    addNotification,
    removeNotification,
    success,
    error,
    warning,
    info
  }
}
