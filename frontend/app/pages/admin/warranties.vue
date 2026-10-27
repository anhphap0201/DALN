<template>
  <div class="space-y-6">
    <!-- Page Header -->
    <div class="flex flex-wrap items-center justify-between gap-4">
      <div>
        <h1 class="text-xl font-extrabold text-gray-900 tracking-tight flex items-center gap-2">
          <span>🛡️</span> Quản Lý Bảo Hành & Phiếu Sửa Chữa
        </h1>
        <p class="text-xs text-gray-500 mt-1">
          Theo dõi vòng đời phiếu yêu cầu bảo hành, ghi chú kỹ thuật và gán tem serial xuất kho
        </p>
      </div>

      <div class="flex items-center gap-3">
        <button
          type="button"
          @click="openAssignModal"
          class="inline-flex items-center gap-2 px-4 py-2 bg-white border border-gray-300 hover:bg-gray-50 text-gray-700 text-xs font-semibold rounded-xl shadow-xs transition-all cursor-pointer"
        >
          <svg class="w-4 h-4 text-[#09f]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          Gán Serial Đóng Gói
        </button>

        <button
          type="button"
          @click="fetchClaims"
          class="inline-flex items-center gap-2 px-3 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 text-xs font-semibold rounded-xl transition-all cursor-pointer"
        >
          <svg class="w-4 h-4" :class="loading ? 'animate-spin' : ''" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
          Làm mới
        </button>
      </div>
    </div>

    <!-- Stats Grid -->
    <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
      <div class="bg-white p-4 rounded-2xl border border-gray-200/80 shadow-xs">
        <span class="text-[11px] font-bold uppercase tracking-wider text-gray-500">Tổng Phiếu</span>
        <div class="text-2xl font-black text-gray-900 mt-1">{{ stats.totalClaims || 0 }}</div>
        <div class="text-[11px] text-gray-400 mt-0.5">Tổng số tem: {{ stats.totalSerials || 0 }}</div>
      </div>

      <div class="bg-white p-4 rounded-2xl border border-amber-200 shadow-xs bg-amber-50/20">
        <span class="text-[11px] font-bold uppercase tracking-wider text-amber-700">Chờ Tiếp Nhận</span>
        <div class="text-2xl font-black text-amber-600 mt-1">{{ stats.pendingClaims || 0 }}</div>
        <div class="text-[11px] text-amber-600 mt-0.5">Cần kỹ thuật viên duyệt</div>
      </div>

      <div class="bg-white p-4 rounded-2xl border border-blue-200 shadow-xs bg-blue-50/20">
        <span class="text-[11px] font-bold uppercase tracking-wider text-blue-700">Đang Xử Lý</span>
        <div class="text-2xl font-black text-[#09f] mt-1">{{ stats.processingClaims || 0 }}</div>
        <div class="text-[11px] text-blue-600 mt-0.5">Đang test/sửa chữa</div>
      </div>

      <div class="bg-white p-4 rounded-2xl border border-emerald-200 shadow-xs bg-emerald-50/20">
        <span class="text-[11px] font-bold uppercase tracking-wider text-emerald-700">Đã Hoàn Tất</span>
        <div class="text-2xl font-black text-emerald-600 mt-1">{{ stats.completedClaims || 0 }}</div>
        <div class="text-[11px] text-emerald-600 mt-0.5">Từ chối: {{ stats.rejectedClaims || 0 }}</div>
      </div>
    </div>

    <!-- Filter & Table Card -->
    <div class="bg-white rounded-2xl border border-gray-200/80 shadow-xs overflow-hidden">
      <!-- Filter Tabs -->
      <div class="px-6 py-3 border-b border-gray-100 flex flex-wrap items-center justify-between gap-3 bg-gray-50/50">
        <div class="flex items-center gap-1 overflow-x-auto py-1">
          <button
            v-for="f in filterOptions"
            :key="f.value"
            @click="setFilter(f.value)"
            class="px-3.5 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer whitespace-nowrap"
            :class="selectedStatus === f.value ? 'bg-[#09f] text-white shadow-xs' : 'text-gray-600 hover:bg-gray-100'"
          >
            {{ f.label }}
          </button>
        </div>
      </div>

      <!-- Claims Table -->
      <div class="overflow-x-auto">
        <table class="w-full text-left text-xs text-gray-700">
          <thead class="bg-gray-50/80 text-[11px] font-bold uppercase tracking-wider text-gray-500 border-b border-gray-200/80">
            <tr>
              <th class="py-3 px-4">Mã Phiếu</th>
              <th class="py-3 px-4">Mã Serial / Linh Kiện</th>
              <th class="py-3 px-4">Khách Hàng</th>
              <th class="py-3 px-4">Mô Tả Lỗi</th>
              <th class="py-3 px-4">Ngày Tạo</th>
              <th class="py-3 px-4">Trạng Thái</th>
              <th class="py-3 px-4 text-right">Thao Tác</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr v-if="loading">
              <td colspan="7" class="py-8 text-center text-gray-500">
                <span class="inline-block w-5 h-5 border-2 border-[#09f] border-t-transparent rounded-full animate-spin mr-2"></span>
                Đang tải danh sách phiếu bảo hành...
              </td>
            </tr>
            <tr v-else-if="!claims.length">
              <td colspan="7" class="py-8 text-center text-gray-500">
                Không có phiếu bảo hành nào ở trạng thái này.
              </td>
            </tr>
            <tr
              v-for="c in claims"
              :key="c.id"
              class="hover:bg-gray-50/80 transition-colors"
            >
              <td class="py-3 px-4 font-mono font-bold text-[#09f]">
                #{{ c.claimCode }}
              </td>
              <td class="py-3 px-4">
                <div class="font-mono text-gray-900 font-semibold">{{ c.serialNumber }}</div>
                <div class="text-[11px] text-gray-500 line-clamp-1">{{ c.productName }}</div>
              </td>
              <td class="py-3 px-4">
                <div class="font-bold text-gray-900">{{ c.customerName }}</div>
                <div class="text-[11px] text-gray-500">{{ c.customerPhone }}</div>
              </td>
              <td class="py-3 px-4 max-w-xs">
                <p class="line-clamp-2 text-gray-700">{{ c.issueDescription }}</p>
                <span v-if="c.technicianNotes" class="block text-[11px] text-blue-600 font-medium mt-0.5">
                  KTV: {{ c.technicianNotes }}
                </span>
              </td>
              <td class="py-3 px-4 text-gray-500 whitespace-nowrap">
                {{ formatDate(c.createdAt) }}
              </td>
              <td class="py-3 px-4">
                <span
                  class="px-2.5 py-1 rounded-md text-[10px] font-bold uppercase tracking-wider"
                  :class="getStatusBadgeClass(c.status)"
                >
                  {{ getStatusLabel(c.status) }}
                </span>
              </td>
              <td class="py-3 px-4 text-right whitespace-nowrap">
                <button
                  type="button"
                  @click="openProcessModal(c)"
                  class="px-3 py-1.5 bg-[#09f]/10 hover:bg-[#09f] text-[#09f] hover:text-white rounded-lg text-xs font-bold transition-all cursor-pointer"
                >
                  Xử Lý Phiếu
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="px-6 py-3 border-t border-gray-100 flex items-center justify-between text-xs">
        <span class="text-gray-500">
          Trang {{ currentPage + 1 }} / {{ totalPages }} (Tổng {{ totalElements }} phiếu)
        </span>
        <div class="flex items-center gap-2">
          <button
            :disabled="currentPage === 0"
            @click="changePage(currentPage - 1)"
            class="px-3 py-1 bg-gray-100 hover:bg-gray-200 disabled:opacity-40 rounded-lg font-medium cursor-pointer"
          >
            Trước
          </button>
          <button
            :disabled="currentPage >= totalPages - 1"
            @click="changePage(currentPage + 1)"
            class="px-3 py-1 bg-gray-100 hover:bg-gray-200 disabled:opacity-40 rounded-lg font-medium cursor-pointer"
          >
            Sau
          </button>
        </div>
      </div>
    </div>

    <!-- MODAL XỬ LÝ PHIẾU BẢO HÀNH -->
    <div v-if="showProcessModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs">
      <div class="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl border border-gray-200">
        <div class="flex items-center justify-between pb-4 border-b border-gray-100">
          <h3 class="text-sm font-bold text-gray-900 flex items-center gap-2">
            <span>🔧</span> Xử Lý Phiếu #{{ targetClaim?.claimCode }}
          </h3>
          <button @click="showProcessModal = false" class="text-gray-400 hover:text-gray-600 text-lg cursor-pointer">✕</button>
        </div>

        <form @submit.prevent="submitProcessForm" class="space-y-4 mt-4 text-xs">
          <div class="p-3 bg-gray-50 rounded-xl space-y-1">
            <div class="font-bold text-gray-900">{{ targetClaim?.productName }}</div>
            <div class="text-gray-600">Mã Serial: <span class="font-mono font-bold">{{ targetClaim?.serialNumber }}</span></div>
            <div class="text-gray-600">Khách: {{ targetClaim?.customerName }} ({{ targetClaim?.customerPhone }})</div>
            <div class="text-gray-700 mt-1 font-medium bg-white p-2 rounded border border-gray-200">
              Lỗi phản ánh: {{ targetClaim?.issueDescription }}
            </div>
          </div>

          <div>
            <label class="block font-bold text-gray-700 mb-1">Cập Nhật Trạng Thái <span class="text-red-500">*</span></label>
            <select
              v-model="processForm.status"
              required
              class="w-full px-3 py-2 rounded-lg border border-gray-300 focus:border-[#09f] outline-none font-medium"
            >
              <option value="PENDING">Chờ Tiếp Nhận (PENDING)</option>
              <option value="PROCESSING">Đang Xử Lý / Kiểm Tra (PROCESSING)</option>
              <option value="COMPLETED">Đã Xong / Hoàn Tất (COMPLETED)</option>
              <option value="REJECTED">Từ Chối Bảo Hành (REJECTED)</option>
            </select>
          </div>

          <div>
            <label class="block font-bold text-gray-700 mb-1">Hình Thức Giải Quyết</label>
            <select
              v-model="processForm.resolutionType"
              class="w-full px-3 py-2 rounded-lg border border-gray-300 focus:border-[#09f] outline-none font-medium"
            >
              <option value="">-- Chưa quyết định --</option>
              <option value="REPAIRED">Đã sửa chữa và kiểm tra đạt tiêu chuẩn</option>
              <option value="REPLACED_NEW">Đổi mới linh kiện 100%</option>
              <option value="REJECTED">Từ chối (Cắm quá áp, chập cháy, rơi vỡ)</option>
            </select>
          </div>

          <div>
            <label class="block font-bold text-gray-700 mb-1">Ghi Chú Kỹ Thuật Viên / Hướng Dẫn Khách</label>
            <textarea
              v-model="processForm.technicianNotes"
              rows="3"
              placeholder="Ví dụ: Đã thay IC nguồn AMS1117-3.3V, đo điện áp ra ổn định; đã gửi bưu điện theo mã vận đơn VNPost 1234..."
              class="w-full px-3 py-2 rounded-lg border border-gray-300 focus:border-[#09f] outline-none"
            ></textarea>
          </div>

          <div class="flex items-center justify-end gap-3 pt-3 border-t border-gray-100">
            <button
              type="button"
              @click="showProcessModal = false"
              class="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-xl font-semibold cursor-pointer"
            >
              Đóng
            </button>
            <button
              type="submit"
              :disabled="savingProcess"
              class="px-5 py-2 bg-[#09f] hover:bg-[#0088ea] text-white rounded-xl font-bold cursor-pointer disabled:opacity-50"
            >
              {{ savingProcess ? 'Đang lưu...' : 'Lưu Cập Nhật' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- MODAL GÁN MÃ SERIAL XUẤT KHO -->
    <div v-if="showAssignModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs">
      <div class="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-gray-200">
        <div class="flex items-center justify-between pb-4 border-b border-gray-100">
          <h3 class="text-sm font-bold text-gray-900 flex items-center gap-2">
            <span>🏷️</span> Gán Tem Serial Cho OrderItem
          </h3>
          <button @click="showAssignModal = false" class="text-gray-400 hover:text-gray-600 text-lg cursor-pointer">✕</button>
        </div>

        <form @submit.prevent="submitAssignSerials" class="space-y-4 mt-4 text-xs">
          <div>
            <label class="block font-bold text-gray-700 mb-1">Mã OrderItem ID <span class="text-red-500">*</span></label>
            <input
              v-model.number="assignForm.orderItemId"
              type="number"
              required
              placeholder="Ví dụ: 12"
              class="w-full px-3 py-2 rounded-lg border border-gray-300 focus:border-[#09f] outline-none"
            />
          </div>

          <div class="p-3 bg-blue-50/60 rounded-xl border border-blue-100 text-gray-600">
            Hệ thống sẽ tự động liên kết với sản phẩm, đơn hàng và kích hoạt ngày hết hạn tính theo số tháng bảo hành của linh kiện đó.
          </div>

          <div class="flex items-center gap-2">
            <input
              id="autoGen"
              v-model="assignForm.autoGenerate"
              type="checkbox"
              class="rounded text-[#09f] focus:ring-[#09f]"
            />
            <label for="autoGen" class="font-bold text-gray-700 cursor-pointer">
              Tự động sinh mã tem Serial (SN-YYMM-XXXXXXXX)
            </label>
          </div>

          <div v-if="!assignForm.autoGenerate">
            <label class="block font-bold text-gray-700 mb-1">Mã Tem Serial (cách nhau bởi dấu phẩy)</label>
            <input
              v-model="assignForm.manualSerials"
              type="text"
              placeholder="SN-2610-A01, SN-2610-A02"
              class="w-full px-3 py-2 rounded-lg border border-gray-300 focus:border-[#09f] outline-none font-mono uppercase"
            />
          </div>

          <div class="flex items-center justify-end gap-3 pt-3 border-t border-gray-100">
            <button
              type="button"
              @click="showAssignModal = false"
              class="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-xl font-semibold cursor-pointer"
            >
              Đóng
            </button>
            <button
              type="submit"
              :disabled="savingAssign"
              class="px-5 py-2 bg-[#09f] hover:bg-[#0088ea] text-white rounded-xl font-bold cursor-pointer disabled:opacity-50"
            >
              {{ savingAssign ? 'Đang gán...' : 'Gán Serial' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuth } from '~/composables/useAuth'
import { useNotification } from '~/composables/useNotification'

definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

const { token } = useAuth()
const { success: showSuccess, error: showError } = useNotification()
const API = 'http://localhost:8080'

const filterOptions = [
  { label: 'Tất Cả', value: 'ALL' },
  { label: 'Chờ Tiếp Nhận', value: 'PENDING' },
  { label: 'Đang Xử Lý', value: 'PROCESSING' },
  { label: 'Đã Xong', value: 'COMPLETED' },
  { label: 'Từ Chối', value: 'REJECTED' }
]

const selectedStatus = ref('ALL')
const claims = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const stats = ref<any>({})

// Modal Process State
const showProcessModal = ref(false)
const savingProcess = ref(false)
const targetClaim = ref<any>(null)
const processForm = ref({
  status: 'PROCESSING',
  resolutionType: '',
  technicianNotes: ''
})

// Modal Assign State
const showAssignModal = ref(false)
const savingAssign = ref(false)
const assignForm = ref({
  orderItemId: null as number | null,
  autoGenerate: true,
  manualSerials: ''
})

const fetchStats = async () => {
  try {
    const res: any = await $fetch(`${API}/api/admin/warranties/stats`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    stats.value = res || {}
  } catch (err: any) {
    console.warn('Lỗi lấy stats bảo hành:', err?.message)
  }
}

const fetchClaims = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    if (selectedStatus.value !== 'ALL') {
      params.append('status', selectedStatus.value)
    }
    params.append('page', currentPage.value.toString())
    params.append('size', '10')

    const res: any = await $fetch(`${API}/api/admin/warranties?${params.toString()}`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })

    claims.value = res?.content || []
    totalPages.value = res?.totalPages || 0
    totalElements.value = res?.totalElements || 0
  } catch (err: any) {
    showError(err?.data?.message || err?.message || 'Lỗi khi tải danh sách phiếu bảo hành.')
  } finally {
    loading.value = false
  }
}

const setFilter = (val: string) => {
  selectedStatus.value = val
  currentPage.value = 0
  fetchClaims()
}

const changePage = (page: number) => {
  currentPage.value = page
  fetchClaims()
}

const openProcessModal = (claim: any) => {
  targetClaim.value = claim
  processForm.value = {
    status: claim.status || 'PROCESSING',
    resolutionType: claim.resolutionType || '',
    technicianNotes: claim.technicianNotes || ''
  }
  showProcessModal.value = true
}

const submitProcessForm = async () => {
  if (!targetClaim.value) return
  savingProcess.value = true

  try {
    const res: any = await $fetch(`${API}/api/admin/warranties/${targetClaim.value.id}`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${token.value}` },
      body: processForm.value
    })

    showSuccess(`Đã cập nhật phiếu #${res.claimCode} thành công!`)
    showProcessModal.value = false
    await fetchClaims()
    await fetchStats()
  } catch (err: any) {
    showError(err?.data?.message || err?.message || 'Lỗi cập nhật phiếu.')
  } finally {
    savingProcess.value = false
  }
}

const openAssignModal = () => {
  assignForm.value = {
    orderItemId: null,
    autoGenerate: true,
    manualSerials: ''
  }
  showAssignModal.value = true
}

const submitAssignSerials = async () => {
  if (!assignForm.value.orderItemId) {
    showError('Vui lòng nhập OrderItem ID')
    return
  }
  savingAssign.value = true

  try {
    let serialList: string[] = []
    if (!assignForm.value.autoGenerate && assignForm.value.manualSerials) {
      serialList = assignForm.value.manualSerials.split(',').map(s => s.trim()).filter(Boolean)
    }

    const res: any = await $fetch(`${API}/api/admin/warranties/orders/assign-serials`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` },
      body: {
        orderItemId: assignForm.value.orderItemId,
        serialNumbers: serialList,
        autoGenerate: assignForm.value.autoGenerate
      }
    })

    showSuccess(`Đã gán thành công ${res?.length || 0} mã Serial cho linh kiện!`)
    showAssignModal.value = false
    await fetchStats()
  } catch (err: any) {
    showError(err?.data?.message || err?.message || 'Lỗi khi gán mã serial.')
  } finally {
    savingAssign.value = false
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  try {
    const d = new Date(dateStr)
    return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
  } catch {
    return dateStr
  }
}

const getStatusBadgeClass = (status: string) => {
  switch (status) {
    case 'PENDING':
      return 'bg-amber-100 text-amber-800'
    case 'PROCESSING':
      return 'bg-blue-100 text-blue-800'
    case 'COMPLETED':
      return 'bg-emerald-100 text-emerald-800'
    case 'REJECTED':
      return 'bg-red-100 text-red-800'
    default:
      return 'bg-gray-100 text-gray-700'
  }
}

const getStatusLabel = (status: string) => {
  switch (status) {
    case 'PENDING':
      return 'Chờ Tiếp Nhận'
    case 'PROCESSING':
      return 'Đang Xử Lý'
    case 'COMPLETED':
      return 'Hoàn Tất'
    case 'REJECTED':
      return 'Từ Chối'
    default:
      return status
  }
}

onMounted(() => {
  fetchStats()
  fetchClaims()
})
</script>
