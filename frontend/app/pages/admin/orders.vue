<template>
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-7xl mx-auto px-4 py-8">
      <!-- Header -->
      <div class="flex items-center justify-between mb-8">
        <div class="flex items-center gap-4">
          <NuxtLink to="/admin" class="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-lg transition-colors">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
          </NuxtLink>
          <div>
            <h1 class="text-3xl font-bold text-gray-900">Quản lý đơn hàng</h1>
            <p class="text-gray-500 mt-1">Quản lý tất cả đơn hàng trong hệ thống</p>
          </div>
        </div>
      </div>

      <!-- Filters -->
      <div class="bg-white rounded-xl shadow-sm p-4 mb-6">
        <div class="flex flex-wrap items-center gap-4">
          <!-- Search -->
          <div class="flex-1 min-w-[250px]">
            <div class="relative">
              <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <input
                v-model="searchQuery"
                type="text"
                placeholder="Tìm theo mã đơn, tên, email, SĐT..."
                class="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#09f] focus:border-transparent"
                @input="debouncedSearch"
              />
            </div>
          </div>
          <!-- Status filter -->
          <div class="flex gap-2 flex-wrap">
            <button
              v-for="s in statusFilters"
              :key="s.value"
              @click="filterStatus = s.value; loadOrders()"
              :class="[
                'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
                filterStatus === s.value
                  ? 'bg-[#09f] text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              ]"
            >
              {{ s.label }}
              <span v-if="s.value === ''" class="ml-1 opacity-75">({{ totalElements }})</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Orders Table -->
      <div class="bg-white rounded-xl shadow-sm overflow-hidden">
        <div v-if="loading" class="flex items-center justify-center py-20">
          <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-[#09f]"></div>
        </div>

        <table v-else class="min-w-full">
          <thead class="bg-gray-50 border-b border-gray-200">
            <tr>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Mã đơn</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Khách hàng</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Tổng tiền</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Thanh toán</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Trạng thái</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Ngày tạo</th>
              <th class="px-6 py-4 text-right text-xs font-semibold text-gray-600 uppercase">Thao tác</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            <tr v-for="order in orders" :key="order.id" class="hover:bg-gray-50 transition-colors">
              <td class="px-6 py-4">
                <span class="text-sm font-medium text-[#09f]">{{ order.orderNumber || `#${order.id}` }}</span>
              </td>
              <td class="px-6 py-4">
                <div>
                  <div class="text-sm font-medium text-gray-900">{{ order.fullName || 'N/A' }}</div>
                  <div class="text-xs text-gray-500">{{ order.email }}</div>
                  <div class="text-xs text-gray-500">{{ order.phone }}</div>
                </div>
              </td>
              <td class="px-6 py-4">
                <span class="text-sm font-bold text-gray-900">{{ formatPrice(order.totalCost) }}</span>
              </td>
              <td class="px-6 py-4">
                <span class="inline-flex items-center px-2 py-1 rounded text-xs font-medium"
                  :class="getPaymentClass(order.paymentMethod)">
                  {{ getPaymentLabel(order.paymentMethod) }}
                </span>
              </td>
              <td class="px-6 py-4">
                <span :class="getStatusClass(order.status)" class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium">
                  {{ getStatusLabel(order.status) }}
                </span>
              </td>
              <td class="px-6 py-4 text-sm text-gray-500">
                {{ formatDate(order.createdAt) }}
              </td>
              <td class="px-6 py-4 text-right">
                <div class="flex items-center justify-end gap-2">
                  <button @click="viewOrder(order)" class="p-2 text-blue-600 hover:bg-blue-50 rounded-lg" title="Xem chi tiết">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                  </button>
                  <button @click="openStatusModal(order)" class="p-2 text-green-600 hover:bg-green-50 rounded-lg" title="Cập nhật trạng thái">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                    </svg>
                  </button>
                  <button @click="confirmDeleteOrder(order)" class="p-2 text-red-600 hover:bg-red-50 rounded-lg" title="Xóa">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="orders.length === 0 && !loading">
              <td colspan="7" class="px-6 py-16 text-center text-gray-500">
                <svg class="w-12 h-12 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
                <p>Không tìm thấy đơn hàng nào</p>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="flex items-center justify-between px-6 py-4 border-t border-gray-200">
          <p class="text-sm text-gray-600">
            Hiển thị {{ orders.length }} / {{ totalElements }} đơn hàng
          </p>
          <div class="flex gap-2">
            <button
              @click="currentPage--; loadOrders()"
              :disabled="currentPage === 0"
              class="px-3 py-1.5 text-sm border rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
            >
              Trước
            </button>
            <span class="px-3 py-1.5 text-sm text-gray-600">
              {{ currentPage + 1 }} / {{ totalPages }}
            </span>
            <button
              @click="currentPage++; loadOrders()"
              :disabled="currentPage >= totalPages - 1"
              class="px-3 py-1.5 text-sm border rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
            >
              Sau
            </button>
          </div>
        </div>
      </div>

      <!-- Order Detail Modal -->
      <div
        v-if="showDetailModal"
        class="fixed inset-0 bg-black/30 backdrop-blur-sm flex items-center justify-center z-50 p-4"
        @click.self="showDetailModal = false"
      >
        <div class="bg-white rounded-2xl max-w-3xl w-full max-h-[90vh] overflow-y-auto">
          <div class="p-6 border-b border-gray-200 flex items-center justify-between">
            <h2 class="text-xl font-bold text-gray-900">
              Chi tiết đơn hàng {{ selectedOrder?.orderNumber || `#${selectedOrder?.id}` }}
            </h2>
            <button @click="showDetailModal = false" class="p-2 hover:bg-gray-100 rounded-lg">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          <div v-if="selectedOrder" class="p-6 space-y-6">
            <!-- Status Badge -->
            <div class="flex items-center gap-3">
              <span :class="getStatusClass(selectedOrder.status)" class="inline-flex items-center px-3 py-1.5 rounded-full text-sm font-medium">
                {{ getStatusLabel(selectedOrder.status) }}
              </span>
              <span class="text-sm text-gray-500">{{ formatDate(selectedOrder.createdAt) }}</span>
            </div>

            <!-- Customer Info -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div class="space-y-3">
                <h3 class="font-semibold text-gray-900">Thông tin khách hàng</h3>
                <div class="text-sm space-y-1">
                  <p><span class="text-gray-500">Họ tên:</span> {{ selectedOrder.fullName }}</p>
                  <p><span class="text-gray-500">Email:</span> {{ selectedOrder.email }}</p>
                  <p><span class="text-gray-500">SĐT:</span> {{ selectedOrder.phone }}</p>
                </div>
              </div>
              <div class="space-y-3">
                <h3 class="font-semibold text-gray-900">Địa chỉ giao hàng</h3>
                <div class="text-sm space-y-1">
                  <p>{{ selectedOrder.address }}</p>
                  <p v-if="selectedOrder.ward">{{ selectedOrder.ward }}, {{ selectedOrder.province }}</p>
                </div>
              </div>
            </div>

            <!-- Order Details -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div class="space-y-3">
                <h3 class="font-semibold text-gray-900">Thông tin đơn hàng</h3>
                <div class="text-sm space-y-1">
                  <p><span class="text-gray-500">Thanh toán:</span> {{ getPaymentLabel(selectedOrder.paymentMethod) }}</p>
                  <p><span class="text-gray-500">TT Thanh toán:</span> {{ selectedOrder.paymentStatus || 'N/A' }}</p>
                  <p><span class="text-gray-500">Vận chuyển:</span> {{ selectedOrder.shippingMethod || 'N/A' }}</p>
                  <p v-if="selectedOrder.voucherCode"><span class="text-gray-500">Voucher:</span> {{ selectedOrder.voucherCode }}</p>
                  <p v-if="selectedOrder.note"><span class="text-gray-500">Ghi chú:</span> {{ selectedOrder.note }}</p>
                </div>
              </div>
              <div class="space-y-3">
                <h3 class="font-semibold text-gray-900">Mốc thời gian</h3>
                <div class="text-sm space-y-1">
                  <p><span class="text-gray-500">Tạo:</span> {{ formatDate(selectedOrder.createdAt) }}</p>
                  <p v-if="selectedOrder.confirmedAt"><span class="text-gray-500">Xác nhận:</span> {{ formatDate(selectedOrder.confirmedAt) }}</p>
                  <p v-if="selectedOrder.shippedAt"><span class="text-gray-500">Giao hàng:</span> {{ formatDate(selectedOrder.shippedAt) }}</p>
                  <p v-if="selectedOrder.deliveredAt"><span class="text-gray-500">Hoàn thành:</span> {{ formatDate(selectedOrder.deliveredAt) }}</p>
                  <p v-if="selectedOrder.cancelledAt"><span class="text-gray-500">Hủy:</span> {{ formatDate(selectedOrder.cancelledAt) }}</p>
                </div>
              </div>
            </div>

            <!-- Order Items -->
            <div>
              <h3 class="font-semibold text-gray-900 mb-3">Sản phẩm ({{ selectedOrder.orderItems?.length || 0 }})</h3>
              <div class="border rounded-lg overflow-hidden">
                <table class="min-w-full">
                  <thead class="bg-gray-50">
                    <tr>
                      <th class="px-4 py-3 text-left text-xs font-medium text-gray-500">Sản phẩm</th>
                      <th class="px-4 py-3 text-center text-xs font-medium text-gray-500">SL</th>
                      <th class="px-4 py-3 text-right text-xs font-medium text-gray-500">Đơn giá</th>
                      <th class="px-4 py-3 text-right text-xs font-medium text-gray-500">Thành tiền</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-gray-200">
                    <tr v-for="item in selectedOrder.orderItems" :key="item.id">
                      <td class="px-4 py-3 text-sm">{{ item.variantName || `Variant #${item.productVariantId}` }}</td>
                      <td class="px-4 py-3 text-sm text-center">{{ item.quantity }}</td>
                      <td class="px-4 py-3 text-sm text-right">{{ formatPrice(item.price) }}</td>
                      <td class="px-4 py-3 text-sm text-right font-medium">{{ formatPrice(item.totalPrice) }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- Totals -->
            <div class="bg-gray-50 rounded-lg p-4 space-y-2">
              <div class="flex justify-between text-sm">
                <span class="text-gray-500">Tạm tính:</span>
                <span>{{ formatPrice(selectedOrder.subtotal) }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-gray-500">Phí vận chuyển:</span>
                <span>{{ formatPrice(selectedOrder.shippingFee) }}</span>
              </div>
              <div v-if="selectedOrder.discount && selectedOrder.discount > 0" class="flex justify-between text-sm text-green-600">
                <span>Giảm giá:</span>
                <span>-{{ formatPrice(selectedOrder.discount) }}</span>
              </div>
              <hr class="border-gray-300">
              <div class="flex justify-between text-lg font-bold">
                <span>Tổng cộng:</span>
                <span class="text-[#09f]">{{ formatPrice(selectedOrder.totalCost) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Status Update Modal -->
      <div
        v-if="showStatusModal"
        class="fixed inset-0 bg-black/30 backdrop-blur-sm flex items-center justify-center z-50 p-4"
        @click.self="showStatusModal = false"
      >
        <div class="bg-white rounded-2xl max-w-md w-full">
          <div class="p-6 border-b border-gray-200">
            <h2 class="text-xl font-bold text-gray-900">Cập nhật trạng thái</h2>
            <p class="text-sm text-gray-500 mt-1">Đơn hàng: {{ statusOrder?.orderNumber || `#${statusOrder?.id}` }}</p>
          </div>
          <div class="p-6 space-y-3">
            <button
              v-for="s in orderStatuses"
              :key="s.value"
              @click="updateStatus(s.value)"
              :disabled="updatingStatus"
              :class="[
                'w-full flex items-center gap-3 px-4 py-3 rounded-lg border-2 transition-all',
                statusOrder?.status === s.value
                  ? 'border-[#09f] bg-blue-50'
                  : 'border-gray-200 hover:border-gray-300 hover:bg-gray-50'
              ]"
            >
              <span :class="getStatusClass(s.value)" class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium">
                {{ s.label }}
              </span>
              <svg v-if="statusOrder?.status === s.value" class="w-5 h-5 text-[#09f] ml-auto" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
            </button>
          </div>
          <div class="p-6 border-t border-gray-200">
            <button @click="showStatusModal = false" class="w-full px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 font-medium">
              Đóng
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuth } from '~/composables/useAuth'
import { useNotification } from '~/composables/useNotification'
import { useConfirm } from '~/composables/useConfirm'

definePageMeta({
  middleware: 'admin'
})

const { token } = useAuth()
const { success, error: showError } = useNotification()
const API = 'http://localhost:8080'

const orders = ref<any[]>([])
const loading = ref(false)
const searchQuery = ref('')
const filterStatus = ref('')
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)

const showDetailModal = ref(false)
const selectedOrder = ref<any>(null)
const showStatusModal = ref(false)
const statusOrder = ref<any>(null)
const updatingStatus = ref(false)

let searchTimeout: ReturnType<typeof setTimeout>

const statusFilters = [
  { value: '', label: 'Tất cả' },
  { value: 'PENDING', label: 'Chờ xử lý' },
  { value: 'CONFIRMED', label: 'Đã xác nhận' },
  { value: 'SHIPPED', label: 'Đang giao' },
  { value: 'DELIVERED', label: 'Đã giao' },
  { value: 'CANCELLED', label: 'Đã hủy' },
]

const orderStatuses = [
  { value: 'PENDING', label: 'Chờ xử lý' },
  { value: 'CONFIRMED', label: 'Đã xác nhận' },
  { value: 'SHIPPED', label: 'Đang giao' },
  { value: 'DELIVERED', label: 'Đã giao' },
  { value: 'CANCELLED', label: 'Đã hủy' },
]

const loadOrders = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams({
      page: String(currentPage.value),
      size: '10',
    })
    if (filterStatus.value) params.set('status', filterStatus.value)
    if (searchQuery.value.trim()) params.set('search', searchQuery.value.trim())

    const data = await $fetch<any>(`${API}/api/admin/orders?${params}`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    orders.value = data.content || []
    totalPages.value = data.totalPages || 0
    totalElements.value = data.totalElements || 0
  } catch (e: any) {
    showError('Lỗi', e.data?.message || 'Không thể tải danh sách đơn hàng')
  } finally {
    loading.value = false
  }
}

const debouncedSearch = () => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    currentPage.value = 0
    loadOrders()
  }, 400)
}

const viewOrder = (order: any) => {
  selectedOrder.value = order
  showDetailModal.value = true
}

const openStatusModal = (order: any) => {
  statusOrder.value = order
  showStatusModal.value = true
}

const updateStatus = async (newStatus: string) => {
  if (!statusOrder.value || statusOrder.value.status === newStatus) return
  updatingStatus.value = true
  try {
    await $fetch(`${API}/api/admin/orders/${statusOrder.value.id}/status?status=${newStatus}`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    success('Thành công', `Đã cập nhật trạng thái đơn hàng thành "${getStatusLabel(newStatus)}"`)
    showStatusModal.value = false
    loadOrders()
  } catch (e: any) {
    showError('Lỗi', e.data?.message || 'Không thể cập nhật trạng thái')
  } finally {
    updatingStatus.value = false
  }
}

const confirmDeleteOrder = async (order: any) => {
  const { confirm } = useConfirm()
  const ok = await confirm(`Bạn có chắc muốn xóa đơn hàng ${order.orderNumber || '#' + order.id}?`, 'Xác nhận xóa')
  if (!ok) return
  try {
    await $fetch(`${API}/api/admin/orders/${order.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    success('Thành công', 'Đã xóa đơn hàng')
    loadOrders()
  } catch (e: any) {
    showError('Lỗi', e.data?.message || 'Không thể xóa đơn hàng')
  }
}

const formatPrice = (price: number | null) => {
  if (!price) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price)
}

const formatDate = (dateStr: string | null) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('vi-VN')
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'Chờ xử lý',
    CONFIRMED: 'Đã xác nhận',
    SHIPPED: 'Đang giao',
    DELIVERED: 'Đã giao',
    CANCELLED: 'Đã hủy',
  }
  return map[status] || status
}

const getStatusClass = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'bg-yellow-100 text-yellow-800',
    CONFIRMED: 'bg-blue-100 text-blue-800',
    SHIPPED: 'bg-purple-100 text-purple-800',
    DELIVERED: 'bg-green-100 text-green-800',
    CANCELLED: 'bg-red-100 text-red-800',
  }
  return map[status] || 'bg-gray-100 text-gray-800'
}

const getPaymentLabel = (method: string) => {
  const map: Record<string, string> = {
    COD: 'COD',
    cod: 'COD',
    bank: 'Chuyển khoản',
    BANK: 'Chuyển khoản',
    momo: 'MoMo',
    MOMO: 'MoMo',
    card: 'Thẻ',
    CARD: 'Thẻ',
  }
  return map[method] || method || 'N/A'
}

const getPaymentClass = (method: string) => {
  const m = (method || '').toLowerCase()
  if (m === 'cod') return 'bg-orange-100 text-orange-700'
  if (m === 'bank') return 'bg-blue-100 text-blue-700'
  if (m === 'momo') return 'bg-pink-100 text-pink-700'
  return 'bg-gray-100 text-gray-700'
}

onMounted(() => {
  loadOrders()
})
</script>
