<template>
  <div class="bg-gray-50 min-h-screen py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <nav class="flex items-center gap-2 text-sm text-gray-600 mb-6">
        <NuxtLink to="/" class="hover:text-[#09f] transition-colors">Trang chủ</NuxtLink>
        <span>›</span>
        <span class="text-gray-900 font-medium">Đơn hàng của tôi</span>
      </nav>

      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-6">
        <div>
          <h1 class="text-3xl font-bold text-gray-900">Đơn hàng của tôi</h1>
          <p class="text-sm text-gray-600 mt-1">Theo dõi lịch sử đơn và trạng thái giao hàng</p>
        </div>
        <div class="flex gap-2">
          <button
            @click="createOrderFromCart"
            :disabled="creatingOrder"
            class="px-4 py-2.5 bg-[#09f] text-white rounded-xl text-sm font-semibold transition-all duration-300 hover:bg-[#0077cc] disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {{ creatingOrder ? 'Đang tạo...' : 'Tạo đơn từ giỏ hàng' }}
          </button>
          <button
            @click="fetchOrders"
            :disabled="loading"
            class="px-4 py-2.5 border-2 border-gray-300 text-gray-700 rounded-xl text-sm font-semibold transition-all duration-300 hover:border-[#09f] hover:text-[#09f] disabled:opacity-60 disabled:cursor-not-allowed"
          >
            Làm mới
          </button>
        </div>
      </div>

      <!-- Status Tabs -->
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 mb-6 p-1.5 flex gap-1 overflow-x-auto">
        <button
          v-for="tab in statusTabs"
          :key="tab.key"
          @click="activeTab = tab.key"
          :class="[
            'px-4 py-2.5 rounded-xl text-sm font-semibold whitespace-nowrap transition-all duration-200',
            activeTab === tab.key
              ? 'bg-[#09f] text-white shadow-sm'
              : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
          ]"
        >
          {{ tab.label }}
          <span
            v-if="getTabCount(tab.key) > 0"
            :class="[
              'ml-1.5 px-2 py-0.5 rounded-full text-xs font-bold',
              activeTab === tab.key
                ? 'bg-white/25 text-white'
                : 'bg-gray-200 text-gray-600'
            ]"
          >
            {{ getTabCount(tab.key) }}
          </span>
        </button>
      </div>

      <div v-if="loading" class="bg-white rounded-2xl p-10 text-center">
        <div class="inline-block animate-spin rounded-full h-10 w-10 border-4 border-[#09f] border-t-transparent mb-3"></div>
        <p class="text-gray-600">Đang tải danh sách đơn hàng...</p>
      </div>

      <div v-else-if="errorMessage" class="bg-red-50 border border-red-200 rounded-2xl p-4 text-sm text-red-700 mb-6">
        {{ errorMessage }}
      </div>

      <div v-if="!loading && filteredOrders.length > 0" class="space-y-4">
        <div
          v-for="order in filteredOrders"
          :key="order.id"
          class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden"
        >
          <div class="p-5">
            <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
              <div>
                <p class="text-sm text-gray-500">Mã đơn hàng</p>
                <p class="text-lg font-bold text-gray-900">#{{ order.id }}</p>
                <p class="text-sm text-gray-500 mt-1">Đặt lúc: {{ formatDate(order.createdAt) }}</p>
              </div>

              <div class="flex items-center gap-3">
                <span
                  :class="[
                    'px-3 py-1 rounded-full text-xs font-semibold',
                    statusClass(getDisplayStatus(order))
                  ]"
                >
                  {{ statusLabel(getDisplayStatus(order)) }}
                </span>
                <span class="text-lg font-bold text-[#09f]">{{ formatPrice(order.totalPrice) }}</span>
              </div>
            </div>

            <div v-if="order.recipientOTP" class="mt-4 bg-gradient-to-r from-blue-50 to-blue-100 border border-blue-200 rounded-xl p-4">
              <p class="text-xs text-blue-600 font-semibold uppercase mb-1">Mã OTP người nhận</p>
              <p class="text-2xl font-bold text-blue-900 tracking-wider">{{ order.recipientOTP }}</p>
            </div>

            <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mt-4 gap-3">
              <p class="text-sm text-gray-600">
                {{ order.orderItems?.length || 0 }} sản phẩm
              </p>

              <div class="flex gap-2">
                <button
                  @click="toggleExpanded(order.id)"
                  class="px-3 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:border-[#09f] hover:text-[#09f] transition-colors"
                >
                  {{ expandedOrderIds.has(order.id) ? 'Ẩn chi tiết' : 'Xem chi tiết' }}
                </button>

                <button
                  v-if="canCancel(getDisplayStatus(order))"
                  @click="cancelOrder(order.id)"
                  :disabled="cancellingOrderIds.has(order.id)"
                  class="px-3 py-2 border border-red-300 rounded-lg text-sm text-red-600 hover:bg-red-50 transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
                >
                  {{ cancellingOrderIds.has(order.id) ? 'Đang hủy...' : 'Hủy đơn' }}
                </button>
              </div>
            </div>
          </div>

          <div v-if="expandedOrderIds.has(order.id)" class="border-t border-gray-100 bg-gray-50 px-5 py-4">

            <div v-if="order.orderItems?.length" class="space-y-3">
              <div
                v-for="item in order.orderItems"
                :key="`${order.id}-${item.id}`"
                class="bg-white border border-gray-200 rounded-xl p-3"
              >
                <div class="flex items-start justify-between gap-4">
                  <div>
                    <p class="font-semibold text-gray-900">{{ item.variantName || `Biến thể #${item.productVariantId}` }}</p>
                    <p class="text-sm text-gray-600 mt-1">Mã biến thể: {{ item.productVariantId }}</p>
                  </div>
                  <div class="text-right">
                    <p class="text-sm text-gray-600">SL: {{ item.quantity }}</p>
                    <p class="text-sm text-gray-600">Đơn giá: {{ formatPrice(item.price) }}</p>
                    <p class="font-semibold text-gray-900">Thành tiền: {{ formatPrice(item.totalPrice) }}</p>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="text-sm text-gray-500">Đơn hàng không có sản phẩm.</div>
          </div>
        </div>

        <div v-if="totalPages > 1" class="flex items-center justify-center gap-2 pt-2">
          <button
            @click="changePage(currentPage - 1)"
            :disabled="currentPage === 0"
            class="px-3 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Trước
          </button>

          <span class="text-sm text-gray-700">Trang {{ currentPage + 1 }} / {{ totalPages }}</span>

          <button
            @click="changePage(currentPage + 1)"
            :disabled="currentPage + 1 >= totalPages"
            class="px-3 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Sau
          </button>
        </div>
      </div>

      <div v-else-if="!loading && orders.length > 0 && filteredOrders.length === 0" class="bg-white rounded-2xl p-12 text-center">
        <div class="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <svg class="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
        </div>
        <h2 class="text-xl font-bold text-gray-900 mb-2">Không có đơn hàng nào ở trạng thái này</h2>
        <p class="text-gray-600 mb-6">Chọn tab khác để xem đơn hàng ở trạng thái khác.</p>
      </div>

      <div v-else-if="!loading" class="bg-white rounded-2xl p-12 text-center">
        <div class="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <svg class="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
        </div>
        <h2 class="text-xl font-bold text-gray-900 mb-2">Bạn chưa có đơn hàng nào</h2>
        <p class="text-gray-600 mb-6">Hãy thêm sản phẩm vào giỏ và tạo đơn hàng đầu tiên.</p>
        <NuxtLink
          to="/cart"
          class="inline-block px-6 py-3 bg-gradient-to-r from-[#09f] to-[#0077cc] text-white rounded-xl font-semibold transition-all duration-300 hover:shadow-xl hover:scale-105"
        >
          Đi tới giỏ hàng
        </NuxtLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

interface OrderItem {
  id: number
  productVariantId: number
  quantity: number
  price: number
  totalPrice: number
  variantName?: string
}

interface Order {
  id: number
  userId: number
  status: string
  deliveryStatus?: string
  orderNumber?: string
  lockerOrderId?: string
  senderOTP?: string
  recipientOTP?: string
  totalPrice: number
  orderItems: OrderItem[]
  createdAt: string
  updatedAt: string
}

interface OrderPageResponse {
  content: Order[]
  totalPages: number
  totalElements: number
  size: number
  number: number
}

definePageMeta({
  middleware: ['auth']
})

const API_BASE_URL = 'http://localhost:8080'
const { getAuthHeader } = useAuth()
const { success: showSuccess, error: showError } = useNotification()

const orders = ref<Order[]>([])
const loading = ref(false)
const creatingOrder = ref(false)
const errorMessage = ref('')
const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = 10

const expandedOrderIds = ref<Set<number>>(new Set())
const cancellingOrderIds = ref<Set<number>>(new Set())

type TabKey = 'ALL' | 'PENDING' | 'SHIPPING' | 'DELIVERED' | 'COMPLETED' | 'CANCELLED'

const activeTab = ref<TabKey>('ALL')

const statusTabs: { key: TabKey; label: string }[] = [
  { key: 'ALL', label: 'Tất cả' },
  { key: 'PENDING', label: 'Chờ xác nhận' },
  { key: 'SHIPPING', label: 'Đang giao' },
  { key: 'DELIVERED', label: 'Đã giao' },
  { key: 'COMPLETED', label: 'Hoàn thành' },
  { key: 'CANCELLED', label: 'Đã hủy' },
]

const getOrderTabKey = (order: Order): TabKey => {
  const status = normalizeStatus(getDisplayStatus(order))
  switch (status) {
    case 'PENDING':
    case 'CONFIRMED':
      return 'PENDING'
    case 'SHIPPED':
    case 'IN_TRANSIT':
      return 'SHIPPING'
    case 'DELIVERED':
    case 'DELIVERED_TO_LOCKER':
    case 'PICKED_UP':
      return 'DELIVERED'
    case 'COMPLETED':
      return 'COMPLETED'
    case 'CANCELLED':
      return 'CANCELLED'
    default:
      return 'PENDING'
  }
}

const filteredOrders = computed(() => {
  if (activeTab.value === 'ALL') return orders.value
  return orders.value.filter(order => getOrderTabKey(order) === activeTab.value)
})

const getTabCount = (tab: TabKey): number => {
  if (tab === 'ALL') return orders.value.length
  return orders.value.filter(order => getOrderTabKey(order) === tab).length
}

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price || 0)
}

const formatDate = (dateString?: string) => {
  if (!dateString) return '--'
  const date = new Date(dateString)
  return date.toLocaleString('vi-VN')
}

const statusLabel = (status: string) => {
  const normalized = normalizeStatus(status)
  switch (normalized) {
    case 'IN_TRANSIT':
      return 'Đang giao'
    case 'PICKED_UP':
      return 'Đã nhận hàng'
    case 'PENDING':
      return 'Chờ xử lý'
    case 'CONFIRMED':
      return 'Đã xác nhận'
    case 'SHIPPED':
      return 'Đang giao'
    case 'DELIVERED':
      return 'Đã giao'
    case 'DELIVERED_TO_LOCKER':
      return 'Đã giao tại tủ'
    case 'COMPLETED':
      return 'Hoàn thành'
    case 'CANCELLED':
      return 'Đã hủy'
    default:
      return status
  }
}

const statusClass = (status: string) => {
  const normalized = normalizeStatus(status)
  switch (normalized) {
    case 'IN_TRANSIT':
      return 'bg-purple-100 text-purple-700'
    case 'PICKED_UP':
      return 'bg-emerald-100 text-emerald-700'
    case 'PENDING':
      return 'bg-yellow-100 text-yellow-700'
    case 'CONFIRMED':
      return 'bg-blue-100 text-blue-700'
    case 'SHIPPED':
      return 'bg-purple-100 text-purple-700'
    case 'DELIVERED':
      return 'bg-green-100 text-green-700'
    case 'DELIVERED_TO_LOCKER':
      return 'bg-blue-100 text-blue-700'
    case 'COMPLETED':
      return 'bg-green-100 text-green-700'
    case 'CANCELLED':
      return 'bg-red-100 text-red-700'
    default:
      return 'bg-gray-100 text-gray-700'
  }
}

const canCancel = (status: string) => {
  const normalized = normalizeStatus(status)
  return normalized === 'PENDING' || normalized === 'CONFIRMED'
}

const normalizeStatus = (status?: string) => {
  return (status || 'PENDING').toUpperCase().replace(/-/g, '_')
}

const getDisplayStatus = (order: Order) => {
  return order.deliveryStatus || order.status || 'PENDING'
}

const toggleExpanded = (orderId: number) => {
  if (expandedOrderIds.value.has(orderId)) {
    expandedOrderIds.value.delete(orderId)
    return
  }
  expandedOrderIds.value.add(orderId)
}

const fetchOrders = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    const response: OrderPageResponse = await $fetch(`${API_BASE_URL}/api/orders`, {
      params: {
        page: currentPage.value,
        size: pageSize
      },
      headers: {
        ...(getAuthHeader() as Record<string, string>)
      }
    })

    orders.value = response?.content || []
    totalPages.value = response?.totalPages || 0
  } catch (error: any) {
    console.error('Error fetching orders:', error)
    errorMessage.value = 'Không thể tải danh sách đơn hàng. Vui lòng thử lại.'
    showError(errorMessage.value)
  } finally {
    loading.value = false
  }
}

const changePage = async (page: number) => {
  if (page < 0 || page >= totalPages.value || page === currentPage.value) return
  currentPage.value = page
  expandedOrderIds.value.clear()
  await fetchOrders()
}

const cancelOrder = async (orderId: number) => {
  if (!confirm('Bạn có chắc muốn hủy đơn hàng này?')) {
    return
  }

  cancellingOrderIds.value.add(orderId)
  try {
    await $fetch(`${API_BASE_URL}/api/orders/${orderId}/cancel`, {
      method: 'DELETE',
      headers: {
        ...(getAuthHeader() as Record<string, string>)
      }
    })

    showSuccess('Đã hủy đơn hàng thành công')
    await fetchOrders()
  } catch (error: any) {
    console.error('Error cancelling order:', error)
    const message = error?.data?.message || error?.data?.error || 'Không thể hủy đơn hàng này'
    showError(message)
  } finally {
    cancellingOrderIds.value.delete(orderId)
  }
}

const createOrderFromCart = async () => {
  creatingOrder.value = true
  try {
    await $fetch(`${API_BASE_URL}/api/orders`, {
      method: 'POST',
      headers: {
        ...(getAuthHeader() as Record<string, string>)
      }
    })

    showSuccess('Tạo đơn hàng từ giỏ thành công')
    currentPage.value = 0
    expandedOrderIds.value.clear()
    await fetchOrders()
  } catch (error: any) {
    console.error('Error creating order:', error)
    const message = error?.data?.message || 'Không thể tạo đơn từ giỏ hàng (có thể giỏ đang trống)'
    showError(message)
  } finally {
    creatingOrder.value = false
  }
}

onMounted(() => {
  fetchOrders()
})
</script>
