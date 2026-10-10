<template>
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Header -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8 gap-4">
        <div>
          <h1 class="text-3xl font-bold text-gray-900 mb-1">Dashboard</h1>
          <p class="text-gray-500 text-sm">Tổng quan hoạt động cửa hàng</p>
        </div>
        <div class="flex items-center gap-3">
          <span class="text-sm text-gray-500">{{ currentDateFormatted }}</span>
          <button @click="fetchDashboard" :disabled="loading"
            class="inline-flex items-center gap-2 px-4 py-2 bg-white border border-gray-200 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors disabled:opacity-50">
            <svg class="w-4 h-4" :class="{ 'animate-spin': loading }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            Làm mới
          </button>
        </div>
      </div>

      <!-- Loading -->
      <div v-if="loading && !dashboard" class="flex items-center justify-center py-32">
        <div class="text-center">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-[#09f] mx-auto mb-4"></div>
          <p class="text-gray-500">Đang tải dữ liệu...</p>
        </div>
      </div>

      <template v-else-if="dashboard">
        <!-- Period Stats Cards -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
          <!-- Hôm nay -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 hover:shadow-md transition-shadow">
            <div class="flex items-center justify-between mb-3">
              <span class="text-xs font-semibold text-gray-500 uppercase tracking-wider">Hôm nay</span>
              <div class="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center">
                <svg class="w-4 h-4 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
            </div>
            <p class="text-2xl font-bold text-gray-900">{{ formatPrice(dashboard.today?.revenue) }}</p>
            <div class="flex items-center gap-3 mt-2">
              <span class="text-xs text-gray-500">{{ dashboard.today?.orderCount || 0 }} đơn</span>
              <span class="text-xs text-gray-400">|</span>
              <span class="text-xs text-gray-500">{{ dashboard.today?.newUsers || 0 }} user mới</span>
            </div>
          </div>

          <!-- Tuần này -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 hover:shadow-md transition-shadow">
            <div class="flex items-center justify-between mb-3">
              <span class="text-xs font-semibold text-gray-500 uppercase tracking-wider">Tuần này</span>
              <div class="w-8 h-8 bg-green-100 rounded-lg flex items-center justify-center">
                <svg class="w-4 h-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
            </div>
            <p class="text-2xl font-bold text-gray-900">{{ formatPrice(dashboard.thisWeek?.revenue) }}</p>
            <div class="flex items-center gap-3 mt-2">
              <span class="text-xs text-gray-500">{{ dashboard.thisWeek?.orderCount || 0 }} đơn</span>
              <span class="text-xs text-gray-400">|</span>
              <span class="text-xs text-gray-500">{{ dashboard.thisWeek?.newUsers || 0 }} user mới</span>
            </div>
          </div>

          <!-- Tháng này -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 hover:shadow-md transition-shadow">
            <div class="flex items-center justify-between mb-3">
              <span class="text-xs font-semibold text-gray-500 uppercase tracking-wider">Tháng này</span>
              <div class="w-8 h-8 bg-purple-100 rounded-lg flex items-center justify-center">
                <svg class="w-4 h-4 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
              </div>
            </div>
            <p class="text-2xl font-bold text-gray-900">{{ formatPrice(dashboard.thisMonth?.revenue) }}</p>
            <div class="flex items-center gap-3 mt-2">
              <span class="text-xs text-gray-500">{{ dashboard.thisMonth?.orderCount || 0 }} đơn</span>
              <span class="text-xs text-gray-400">|</span>
              <span class="text-xs text-gray-500">{{ dashboard.thisMonth?.newUsers || 0 }} user mới</span>
            </div>
          </div>

          <!-- Năm này -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 hover:shadow-md transition-shadow">
            <div class="flex items-center justify-between mb-3">
              <span class="text-xs font-semibold text-gray-500 uppercase tracking-wider">Năm này</span>
              <div class="w-8 h-8 bg-orange-100 rounded-lg flex items-center justify-center">
                <svg class="w-4 h-4 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                </svg>
              </div>
            </div>
            <p class="text-2xl font-bold text-gray-900">{{ formatPrice(dashboard.thisYear?.revenue) }}</p>
            <div class="flex items-center gap-3 mt-2">
              <span class="text-xs text-gray-500">{{ dashboard.thisYear?.orderCount || 0 }} đơn</span>
              <span class="text-xs text-gray-400">|</span>
              <span class="text-xs text-gray-500">{{ dashboard.thisYear?.newUsers || 0 }} user mới</span>
            </div>
          </div>
        </div>

        <!-- Tổng quan & Trạng thái đơn hàng -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
          <!-- Revenue Total Card -->
          <div class="bg-gradient-to-br from-[#09f] to-blue-700 rounded-xl shadow-lg p-6 text-white">
            <p class="text-blue-100 text-sm mb-1">Tổng doanh thu</p>
            <p class="text-3xl font-bold mb-4">{{ formatPrice(dashboard.totalRevenue) }}</p>
            <div class="grid grid-cols-2 gap-3 text-sm">
              <div class="bg-white/10 rounded-lg p-3">
                <p class="text-blue-200 text-xs">Tổng đơn</p>
                <p class="font-bold text-lg">{{ dashboard.totalOrders }}</p>
              </div>
              <div class="bg-white/10 rounded-lg p-3">
                <p class="text-blue-200 text-xs">Sản phẩm</p>
                <p class="font-bold text-lg">{{ dashboard.totalProducts }}</p>
              </div>
              <div class="bg-white/10 rounded-lg p-3">
                <p class="text-blue-200 text-xs">Danh mục</p>
                <p class="font-bold text-lg">{{ dashboard.totalCategories }}</p>
              </div>
              <div class="bg-white/10 rounded-lg p-3">
                <p class="text-blue-200 text-xs">Người dùng</p>
                <p class="font-bold text-lg">{{ dashboard.totalUsers }}</p>
              </div>
            </div>
          </div>

          <!-- Trạng thái đơn hàng -->
          <div class="lg:col-span-2 bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h3 class="text-lg font-semibold text-gray-900 mb-4">Trạng thái đơn hàng</h3>
            <div class="grid grid-cols-5 gap-3 mb-4">
              <div class="text-center p-3 bg-yellow-50 rounded-xl">
                <p class="text-2xl font-bold text-yellow-600">{{ dashboard.pendingOrders }}</p>
                <p class="text-xs text-gray-500 mt-1">Chờ xử lý</p>
              </div>
              <div class="text-center p-3 bg-blue-50 rounded-xl">
                <p class="text-2xl font-bold text-blue-600">{{ dashboard.confirmedOrders }}</p>
                <p class="text-xs text-gray-500 mt-1">Đã xác nhận</p>
              </div>
              <div class="text-center p-3 bg-purple-50 rounded-xl">
                <p class="text-2xl font-bold text-purple-600">{{ dashboard.shippedOrders }}</p>
                <p class="text-xs text-gray-500 mt-1">Đang giao</p>
              </div>
              <div class="text-center p-3 bg-green-50 rounded-xl">
                <p class="text-2xl font-bold text-green-600">{{ dashboard.deliveredOrders }}</p>
                <p class="text-xs text-gray-500 mt-1">Đã giao</p>
              </div>
              <div class="text-center p-3 bg-red-50 rounded-xl">
                <p class="text-2xl font-bold text-red-600">{{ dashboard.cancelledOrders }}</p>
                <p class="text-xs text-gray-500 mt-1">Đã hủy</p>
              </div>
            </div>
            <!-- Status bar -->
            <div class="h-3 rounded-full overflow-hidden flex bg-gray-100" v-if="dashboard.totalOrders > 0">
              <div class="bg-yellow-400 transition-all" :style="{ width: statusPercent('pending') + '%' }" :title="`Chờ xử lý: ${dashboard.pendingOrders}`"></div>
              <div class="bg-blue-400 transition-all" :style="{ width: statusPercent('confirmed') + '%' }" :title="`Đã xác nhận: ${dashboard.confirmedOrders}`"></div>
              <div class="bg-purple-400 transition-all" :style="{ width: statusPercent('shipped') + '%' }" :title="`Đang giao: ${dashboard.shippedOrders}`"></div>
              <div class="bg-green-500 transition-all" :style="{ width: statusPercent('delivered') + '%' }" :title="`Đã giao: ${dashboard.deliveredOrders}`"></div>
              <div class="bg-red-400 transition-all" :style="{ width: statusPercent('cancelled') + '%' }" :title="`Đã hủy: ${dashboard.cancelledOrders}`"></div>
            </div>
          </div>
        </div>

        <!-- Biểu đồ doanh thu 30 ngày -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold text-gray-900">Doanh thu & Đơn hàng (30 ngày gần đây)</h3>
            <div class="flex items-center gap-4 text-xs">
              <span class="flex items-center gap-1">
                <span class="w-3 h-3 rounded-full bg-[#09f]"></span> Doanh thu
              </span>
              <span class="flex items-center gap-1">
                <span class="w-3 h-3 rounded-full bg-emerald-400"></span> Đơn hàng
              </span>
            </div>
          </div>

          <div v-if="dashboard.revenueChart && dashboard.revenueChart.length > 0" class="relative">
            <!-- Revenue bars -->
            <div class="flex items-end gap-[2px] h-48 mb-2">
              <div
                v-for="(day, idx) in dashboard.revenueChart"
                :key="idx"
                class="flex-1 flex flex-col items-center justify-end gap-[1px] group relative"
              >
                <!-- Tooltip -->
                <div class="absolute bottom-full mb-2 left-1/2 -translate-x-1/2 bg-gray-800 text-white text-xs rounded-lg px-3 py-2 whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-10 shadow-lg">
                  <p class="font-medium">{{ formatChartDate(day.date) }}</p>
                  <p>Doanh thu: {{ formatPrice(day.revenue) }}</p>
                  <p>Đơn hàng: {{ day.orderCount }}</p>
                </div>
                <!-- Revenue bar -->
                <div
                  class="w-full rounded-t bg-[#09f]/80 hover:bg-[#09f] transition-all cursor-pointer min-h-[2px]"
                  :style="{ height: revenueBarHeight(day.revenue) + '%' }"
                ></div>
                <!-- Order count dot -->
                <div
                  class="w-full rounded-t bg-emerald-400/80 hover:bg-emerald-400 transition-all cursor-pointer min-h-[1px]"
                  :style="{ height: orderBarHeight(day.orderCount) + '%' }"
                ></div>
              </div>
            </div>
            <!-- X-axis labels -->
            <div class="flex gap-[2px]">
              <div
                v-for="(day, idx) in dashboard.revenueChart"
                :key="'label-' + idx"
                class="flex-1 text-center"
              >
                <span v-if="idx % 5 === 0 || idx === dashboard.revenueChart.length - 1" class="text-[10px] text-gray-400">
                  {{ day.date.slice(5) }}
                </span>
              </div>
            </div>
          </div>
          <div v-else class="flex items-center justify-center h-48 text-gray-400 text-sm">
            Chưa có dữ liệu
          </div>
        </div>

        <!-- Top sản phẩm & Đơn hàng gần đây -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
          <!-- Top sản phẩm bán chạy -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h3 class="text-lg font-semibold text-gray-900 mb-4">Top sản phẩm bán chạy (30 ngày)</h3>
            <div v-if="dashboard.topProducts && dashboard.topProducts.length > 0" class="space-y-3">
              <div
                v-for="(product, idx) in dashboard.topProducts"
                :key="idx"
                class="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-50 transition-colors"
              >
                <div class="w-8 h-8 rounded-lg flex items-center justify-center font-bold text-sm"
                  :class="idx < 3 ? 'bg-[#09f] text-white' : 'bg-gray-100 text-gray-600'">
                  {{ idx + 1 }}
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium text-gray-900 truncate">{{ product.productName }}</p>
                  <p class="text-xs text-gray-500 truncate">{{ product.variantName }}</p>
                </div>
                <div class="text-right flex-shrink-0">
                  <p class="text-sm font-bold text-gray-900">{{ product.totalQuantity }} sp</p>
                  <p class="text-xs text-gray-500">{{ formatPrice(product.totalRevenue) }}</p>
                </div>
              </div>
            </div>
            <div v-else class="flex flex-col items-center justify-center py-8 text-gray-400">
              <svg class="w-10 h-10 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
              </svg>
              <p class="text-sm">Chưa có dữ liệu</p>
            </div>
          </div>

          <!-- Đơn hàng gần đây -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-lg font-semibold text-gray-900">Đơn hàng gần đây</h3>
              <NuxtLink to="/admin/orders" class="text-sm text-[#09f] hover:underline">Xem tất cả</NuxtLink>
            </div>
            <div v-if="dashboard.recentOrders && dashboard.recentOrders.length > 0" class="space-y-2">
              <div
                v-for="order in dashboard.recentOrders"
                :key="order.id"
                class="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-50 transition-colors"
              >
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2">
                    <span class="text-sm font-medium text-[#09f]">{{ order.orderNumber || `#${order.id}` }}</span>
                    <span :class="getStatusClass(order.status)" class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-medium">
                      {{ getStatusLabel(order.status) }}
                    </span>
                  </div>
                  <p class="text-xs text-gray-500 mt-0.5">{{ order.fullName }} &middot; {{ formatDate(order.createdAt) }}</p>
                </div>
                <p class="text-sm font-bold text-gray-900 flex-shrink-0">{{ formatPrice(order.totalCost) }}</p>
              </div>
            </div>
            <div v-else class="flex flex-col items-center justify-center py-8 text-gray-400">
              <svg class="w-10 h-10 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
              <p class="text-sm">Chưa có đơn hàng nào</p>
            </div>
          </div>
        </div>

        <!-- Quick Access -->
        <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
          <NuxtLink to="/admin/products" class="group">
            <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4 hover:shadow-md hover:border-[#09f] transition-all text-center">
              <div class="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center mx-auto mb-3 group-hover:bg-[#09f] transition-colors">
                <svg class="w-6 h-6 text-blue-600 group-hover:text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                </svg>
              </div>
              <h4 class="font-semibold text-gray-900 text-sm">Sản phẩm</h4>
              <p class="text-xs text-gray-500 mt-1">{{ dashboard.totalProducts }} sản phẩm</p>
            </div>
          </NuxtLink>

          <NuxtLink to="/admin/categories" class="group">
            <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4 hover:shadow-md hover:border-[#09f] transition-all text-center">
              <div class="w-12 h-12 bg-purple-100 rounded-xl flex items-center justify-center mx-auto mb-3 group-hover:bg-[#09f] transition-colors">
                <svg class="w-6 h-6 text-purple-600 group-hover:text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                </svg>
              </div>
              <h4 class="font-semibold text-gray-900 text-sm">Danh mục</h4>
              <p class="text-xs text-gray-500 mt-1">{{ dashboard.totalCategories }} danh mục</p>
            </div>
          </NuxtLink>

          <NuxtLink to="/admin/orders" class="group">
            <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4 hover:shadow-md hover:border-[#09f] transition-all text-center">
              <div class="w-12 h-12 bg-green-100 rounded-xl flex items-center justify-center mx-auto mb-3 group-hover:bg-[#09f] transition-colors">
                <svg class="w-6 h-6 text-green-600 group-hover:text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
              </div>
              <h4 class="font-semibold text-gray-900 text-sm">Đơn hàng</h4>
              <p class="text-xs text-gray-500 mt-1">{{ dashboard.totalOrders }} đơn hàng</p>
            </div>
          </NuxtLink>

          <NuxtLink to="/admin/users" class="group">
            <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4 hover:shadow-md hover:border-[#09f] transition-all text-center">
              <div class="w-12 h-12 bg-orange-100 rounded-xl flex items-center justify-center mx-auto mb-3 group-hover:bg-[#09f] transition-colors">
                <svg class="w-6 h-6 text-orange-600 group-hover:text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
              </div>
              <h4 class="font-semibold text-gray-900 text-sm">Người dùng</h4>
              <p class="text-xs text-gray-500 mt-1">{{ dashboard.totalUsers }} tài khoản</p>
            </div>
          </NuxtLink>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuth } from '~/composables/useAuth'
import { useNotification } from '~/composables/useNotification'

definePageMeta({
  middleware: 'admin'
})

const { user, token } = useAuth()
const { error } = useNotification()

const loading = ref(false)
const dashboard = ref<any>(null)

const currentDateFormatted = computed(() => {
  return new Date().toLocaleDateString('vi-VN', {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
  })
})

onMounted(async () => {
  if (user.value?.role !== 'ROLE_ADMIN') {
    error('Truy cập bị từ chối', 'Bạn không có quyền truy cập trang này')
    navigateTo('/')
    return
  }
  await fetchDashboard()
})

const fetchDashboard = async () => {
  loading.value = true
  try {
    const data = await $fetch<any>('http://localhost:8080/api/admin/dashboard', {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    dashboard.value = data
  } catch (e: any) {
    console.error('Failed to fetch dashboard:', e)
    try {
      const data = await $fetch<any>('http://localhost:8080/api/admin/stats', {
        headers: { Authorization: `Bearer ${token.value}` }
      })
      dashboard.value = {
        ...data,
        today: { orderCount: 0, revenue: 0, newUsers: 0 },
        thisWeek: { orderCount: 0, revenue: 0, newUsers: 0 },
        thisMonth: { orderCount: 0, revenue: 0, newUsers: 0 },
        thisYear: { orderCount: 0, revenue: 0, newUsers: 0 },
        revenueChart: [],
        topProducts: [],
      }
    } catch (e2) {
      console.error('Fallback also failed:', e2)
      error('Lỗi', 'Không thể tải dữ liệu dashboard')
    }
  } finally {
    loading.value = false
  }
}

// --- Chart helpers ---
const maxRevenue = computed(() => {
  if (!dashboard.value?.revenueChart) return 1
  const max = Math.max(...dashboard.value.revenueChart.map((d: any) => Number(d.revenue) || 0))
  return max || 1
})

const maxOrders = computed(() => {
  if (!dashboard.value?.revenueChart) return 1
  const max = Math.max(...dashboard.value.revenueChart.map((d: any) => d.orderCount || 0))
  return max || 1
})

const revenueBarHeight = (revenue: number | null) => {
  if (!revenue) return 1
  return Math.max(1, (Number(revenue) / maxRevenue.value) * 80)
}

const orderBarHeight = (count: number) => {
  if (!count) return 1
  return Math.max(1, (count / maxOrders.value) * 20)
}

const statusPercent = (status: string) => {
  if (!dashboard.value || !dashboard.value.totalOrders) return 0
  const map: Record<string, string> = {
    pending: 'pendingOrders',
    confirmed: 'confirmedOrders',
    shipped: 'shippedOrders',
    delivered: 'deliveredOrders',
    cancelled: 'cancelledOrders',
  }
  return ((dashboard.value[map[status]] || 0) / dashboard.value.totalOrders * 100).toFixed(1)
}

// --- Formatting ---
const formatPrice = (price: number | null) => {
  if (!price) return '0 '
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(Number(price))
}

const formatDate = (dateStr: string | null) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const formatChartDate = (dateStr: string) => {
  const d = new Date(dateStr)
  return d.toLocaleDateString('vi-VN', { weekday: 'short', day: '2-digit', month: '2-digit' })
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
</script>
