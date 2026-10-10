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
            <h1 class="text-3xl font-bold text-gray-900">Quản lý người dùng</h1>
            <p class="text-gray-500 mt-1">Quản lý tất cả tài khoản người dùng</p>
          </div>
        </div>
      </div>

      <!-- Search -->
      <div class="bg-white rounded-xl shadow-sm p-4 mb-6">
        <div class="relative max-w-md">
          <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tìm theo email, tên, SĐT..."
            class="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#09f] focus:border-transparent"
            @input="debouncedSearch"
          />
        </div>
      </div>

      <!-- Users Table -->
      <div class="bg-white rounded-xl shadow-sm overflow-hidden">
        <div v-if="loading" class="flex items-center justify-center py-20">
          <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-[#09f]"></div>
        </div>

        <table v-else class="min-w-full">
          <thead class="bg-gray-50 border-b border-gray-200">
            <tr>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">ID</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Người dùng</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">SĐT</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Vai trò</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Đơn hàng</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Tổng chi</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Trạng thái</th>
              <th class="px-6 py-4 text-right text-xs font-semibold text-gray-600 uppercase">Thao tác</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            <tr v-for="u in users" :key="u.id" class="hover:bg-gray-50 transition-colors">
              <td class="px-6 py-4 text-sm text-gray-900">#{{ u.id }}</td>
              <td class="px-6 py-4">
                <div>
                  <div class="text-sm font-medium text-gray-900">{{ u.fullName || 'Chưa cập nhật' }}</div>
                  <div class="text-xs text-gray-500">{{ u.email }}</div>
                </div>
              </td>
              <td class="px-6 py-4 text-sm text-gray-600">{{ u.phone || '-' }}</td>
              <td class="px-6 py-4">
                <span :class="[
                  'inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium',
                  u.role === 'ROLE_ADMIN' ? 'bg-purple-100 text-purple-800' : 'bg-blue-100 text-blue-800'
                ]">
                  {{ u.role === 'ROLE_ADMIN' ? 'Admin' : 'User' }}
                </span>
              </td>
              <td class="px-6 py-4 text-sm text-gray-900 font-medium">{{ u.orderCount }}</td>
              <td class="px-6 py-4 text-sm text-gray-900">{{ formatPrice(u.totalSpent) }}</td>
              <td class="px-6 py-4">
                <span :class="[
                  'inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium',
                  u.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                ]">
                  {{ u.isActive ? 'Hoạt động' : 'Bị khóa' }}
                </span>
              </td>
              <td class="px-6 py-4 text-right">
                <div class="flex items-center justify-end gap-2">
                  <button @click="viewUser(u)" class="p-2 text-blue-600 hover:bg-blue-50 rounded-lg" title="Xem chi tiết">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                  </button>
                  <button
                    @click="toggleActive(u)"
                    :class="[
                      'p-2 rounded-lg',
                      u.isActive ? 'text-red-600 hover:bg-red-50' : 'text-green-600 hover:bg-green-50'
                    ]"
                    :title="u.isActive ? 'Khóa tài khoản' : 'Mở khóa tài khoản'"
                  >
                    <svg v-if="u.isActive" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                    <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 11V7a4 4 0 118 0m-4 8v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2z" />
                    </svg>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="users.length === 0 && !loading">
              <td colspan="8" class="px-6 py-16 text-center text-gray-500">
                Không tìm thấy người dùng nào
              </td>
            </tr>
          </tbody>
        </table>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="flex items-center justify-between px-6 py-4 border-t border-gray-200">
          <p class="text-sm text-gray-600">
            Hiển thị {{ users.length }} / {{ totalElements }} người dùng
          </p>
          <div class="flex gap-2">
            <button
              @click="currentPage--; loadUsers()"
              :disabled="currentPage === 0"
              class="px-3 py-1.5 text-sm border rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
            >
              Trước
            </button>
            <span class="px-3 py-1.5 text-sm text-gray-600">
              {{ currentPage + 1 }} / {{ totalPages }}
            </span>
            <button
              @click="currentPage++; loadUsers()"
              :disabled="currentPage >= totalPages - 1"
              class="px-3 py-1.5 text-sm border rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
            >
              Sau
            </button>
          </div>
        </div>
      </div>

      <!-- User Detail Modal -->
      <div
        v-if="showDetailModal"
        class="fixed inset-0 bg-black/30 backdrop-blur-sm flex items-center justify-center z-50 p-4"
        @click.self="showDetailModal = false"
      >
        <div class="bg-white rounded-2xl max-w-lg w-full">
          <div class="p-6 border-b border-gray-200 flex items-center justify-between">
            <h2 class="text-xl font-bold text-gray-900">Chi tiết người dùng</h2>
            <button @click="showDetailModal = false" class="p-2 hover:bg-gray-100 rounded-lg">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          <div v-if="selectedUser" class="p-6 space-y-4">
            <div class="flex items-center gap-4">
              <div class="w-16 h-16 bg-gradient-to-br from-[#09f] to-blue-600 rounded-full flex items-center justify-center text-white text-2xl font-bold">
                {{ (selectedUser.fullName || selectedUser.email || '?').charAt(0).toUpperCase() }}
              </div>
              <div>
                <h3 class="text-lg font-bold text-gray-900">{{ selectedUser.fullName || 'Chưa cập nhật' }}</h3>
                <p class="text-sm text-gray-500">{{ selectedUser.email }}</p>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4 pt-4 border-t">
              <div>
                <p class="text-xs text-gray-500">ID</p>
                <p class="text-sm font-medium">#{{ selectedUser.id }}</p>
              </div>
              <div>
                <p class="text-xs text-gray-500">Vai trò</p>
                <p class="text-sm font-medium">{{ selectedUser.role === 'ROLE_ADMIN' ? 'Admin' : 'User' }}</p>
              </div>
              <div>
                <p class="text-xs text-gray-500">Số điện thoại</p>
                <p class="text-sm font-medium">{{ selectedUser.phone || 'Chưa cập nhật' }}</p>
              </div>
              <div>
                <p class="text-xs text-gray-500">Trạng thái</p>
                <p :class="selectedUser.isActive ? 'text-green-600' : 'text-red-600'" class="text-sm font-medium">
                  {{ selectedUser.isActive ? 'Hoạt động' : 'Bị khóa' }}
                </p>
              </div>
              <div>
                <p class="text-xs text-gray-500">Tỉnh/Thành</p>
                <p class="text-sm font-medium">{{ selectedUser.province || 'Chưa cập nhật' }}</p>
              </div>
              <div>
                <p class="text-xs text-gray-500">Phường/Xã</p>
                <p class="text-sm font-medium">{{ selectedUser.ward || 'Chưa cập nhật' }}</p>
              </div>
              <div class="col-span-2">
                <p class="text-xs text-gray-500">Địa chỉ chi tiết</p>
                <p class="text-sm font-medium">{{ selectedUser.detailedAddress || 'Chưa cập nhật' }}</p>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4 pt-4 border-t">
              <div class="bg-blue-50 rounded-lg p-3 text-center">
                <p class="text-2xl font-bold text-blue-600">{{ selectedUser.orderCount }}</p>
                <p class="text-xs text-gray-500">Đơn hàng</p>
              </div>
              <div class="bg-green-50 rounded-lg p-3 text-center">
                <p class="text-xl font-bold text-green-600">{{ formatPrice(selectedUser.totalSpent) }}</p>
                <p class="text-xs text-gray-500">Tổng chi tiêu</p>
              </div>
            </div>

            <div v-if="selectedUser.createdAt" class="text-xs text-gray-400 pt-2 border-t">
              Ngày tạo: {{ formatDate(selectedUser.createdAt) }}
            </div>
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

const users = ref<any[]>([])
const loading = ref(false)
const searchQuery = ref('')
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)

const showDetailModal = ref(false)
const selectedUser = ref<any>(null)

let searchTimeout: ReturnType<typeof setTimeout>

const loadUsers = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams({
      page: String(currentPage.value),
      size: '10',
    })
    if (searchQuery.value.trim()) params.set('search', searchQuery.value.trim())

    const data = await $fetch<any>(`${API}/api/admin/users?${params}`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    users.value = data.content || []
    totalPages.value = data.totalPages || 0
    totalElements.value = data.totalElements || 0
  } catch (e: any) {
    showError('Lỗi', e.data?.message || 'Không thể tải danh sách người dùng')
  } finally {
    loading.value = false
  }
}

const debouncedSearch = () => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    currentPage.value = 0
    loadUsers()
  }, 400)
}

const viewUser = (u: any) => {
  selectedUser.value = u
  showDetailModal.value = true
}

const toggleActive = async (u: any) => {
  const action = u.isActive ? 'khóa' : 'mở khóa'
  const { confirm } = useConfirm()
  const ok = await confirm(`Bạn có chắc muốn ${action} tài khoản "${u.email}"?`, 'Xác nhận')
  if (!ok) return

  try {
    await $fetch(`${API}/api/admin/users/${u.id}/toggle-active`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    success('Thành công', `Đã ${action} tài khoản ${u.email}`)
    loadUsers()
  } catch (e: any) {
    showError('Lỗi', e.data?.message || `Không thể ${action} tài khoản`)
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

onMounted(() => {
  loadUsers()
})
</script>
