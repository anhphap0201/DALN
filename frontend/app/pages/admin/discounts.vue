<template>
  <div class="space-y-6">
    <!-- Header Page Title & Subtitle -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 bg-white p-5 rounded-2xl border border-gray-200/80 shadow-xs">
      <div>
        <h1 class="text-xl sm:text-2xl font-bold text-gray-900 flex items-center gap-2">
          <span>Thiết Lập Giảm Giá Linh Kiện</span>
          <span class="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-rose-50 text-rose-600 border border-rose-200">
            Khuyến mãi trực tiếp
          </span>
        </h1>
        <p class="text-xs sm:text-sm text-gray-500 mt-1">
          Quản lý giá ưu đãi cho từng phân loại linh kiện điện tử, hỗ trợ tính toán % giảm và áp dụng tức thì.
        </p>
      </div>

      <div class="flex items-center gap-2">
        <button
          @click="fetchVariants"
          :disabled="loading"
          class="inline-flex items-center gap-2 px-3.5 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 text-xs font-semibold rounded-xl transition-all cursor-pointer disabled:opacity-50"
        >
          <svg class="w-4 h-4" :class="{ 'animate-spin': loading }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
          <span>Làm mới</span>
        </button>
      </div>
    </div>

    <!-- Summary KPI Cards -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-white p-4 rounded-2xl border border-gray-200/80 shadow-xs">
        <div class="flex items-center justify-between text-gray-500 mb-2">
          <span class="text-xs font-medium uppercase tracking-wider">Tổng linh kiện</span>
          <span class="p-2 bg-blue-50 text-[#09f] rounded-xl">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
            </svg>
          </span>
        </div>
        <div class="text-2xl font-bold text-gray-900">{{ variants.length }}</div>
        <p class="text-[11px] text-gray-400 mt-1">Phân loại biến thể sản phẩm</p>
      </div>

      <div class="bg-white p-4 rounded-2xl border border-gray-200/80 shadow-xs">
        <div class="flex items-center justify-between text-gray-500 mb-2">
          <span class="text-xs font-medium uppercase tracking-wider">Đang giảm giá</span>
          <span class="p-2 bg-emerald-50 text-emerald-600 rounded-xl">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
            </svg>
          </span>
        </div>
        <div class="text-2xl font-bold text-emerald-600">{{ discountedCount }}</div>
        <p class="text-[11px] text-gray-400 mt-1">Chiếm {{ variants.length ? Math.round((discountedCount / variants.length) * 100) : 0 }}% danh mục</p>
      </div>

      <div class="bg-white p-4 rounded-2xl border border-gray-200/80 shadow-xs">
        <div class="flex items-center justify-between text-gray-500 mb-2">
          <span class="text-xs font-medium uppercase tracking-wider">Nguyên giá</span>
          <span class="p-2 bg-gray-100 text-gray-600 rounded-xl">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </span>
        </div>
        <div class="text-2xl font-bold text-gray-900">{{ variants.length - discountedCount }}</div>
        <p class="text-[11px] text-gray-400 mt-1">Bán theo giá niêm yết gốc</p>
      </div>

      <div class="bg-white p-4 rounded-2xl border border-gray-200/80 shadow-xs">
        <div class="flex items-center justify-between text-gray-500 mb-2">
          <span class="text-xs font-medium uppercase tracking-wider">Giảm sâu nhất</span>
          <span class="p-2 bg-rose-50 text-rose-600 rounded-xl">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 17h8m0 0V9m0 8l-8-8-4 4-6-6" />
            </svg>
          </span>
        </div>
        <div class="text-2xl font-bold text-rose-600">-{{ maxDiscountPercent }}%</div>
        <p class="text-[11px] text-gray-400 mt-1">Ưu đãi giảm cao nhất hiện tại</p>
      </div>
    </div>

    <!-- Bulk Action Toolbar (khi chọn 1 hoặc nhiều linh kiện) -->
    <Transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="opacity-0 -translate-y-2"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 -translate-y-2"
    >
      <div v-if="selectedIds.size > 0" class="bg-gradient-to-r from-blue-900 to-indigo-900 text-white p-4 rounded-2xl shadow-lg flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div class="flex items-center gap-3">
          <span class="px-3 py-1 bg-white/20 rounded-lg text-xs font-bold">
            Đã chọn {{ selectedIds.size }} linh kiện
          </span>
          <p class="text-xs text-blue-200 hidden sm:block">Thiết lập giảm giá hàng loạt cho các phân loại đã chọn</p>
        </div>

        <div class="flex flex-wrap items-center gap-2">
          <div class="flex items-center gap-1.5 bg-white/10 px-3 py-1.5 rounded-xl border border-white/20">
            <span class="text-xs text-blue-100 font-medium">Giảm:</span>
            <input
              v-model.number="bulkPercent"
              type="number"
              min="1"
              max="99"
              placeholder="10"
              class="w-14 px-2 py-1 bg-white text-gray-900 rounded-lg text-xs font-bold text-center outline-none focus:ring-2 focus:ring-[#09f]"
            />
            <span class="text-xs font-bold">%</span>
            <button
              @click="applyBulkDiscount"
              :disabled="bulkLoading || !bulkPercent || bulkPercent <= 0 || bulkPercent >= 100"
              class="ml-2 px-3 py-1 bg-[#09f] hover:bg-blue-500 text-white text-xs font-bold rounded-lg transition-colors disabled:opacity-50 cursor-pointer"
            >
              {{ bulkLoading ? 'Đang lưu...' : 'Áp dụng' }}
            </button>
          </div>

          <button
            @click="clearBulkDiscount"
            :disabled="bulkLoading"
            class="px-3 py-2 bg-rose-600 hover:bg-rose-700 text-white text-xs font-semibold rounded-xl transition-colors disabled:opacity-50 cursor-pointer"
          >
            Hủy giảm giá hàng loạt
          </button>

          <button
            @click="selectedIds.clear()"
            class="px-2.5 py-2 text-xs text-blue-200 hover:text-white transition-colors"
          >
            Bỏ chọn
          </button>
        </div>
      </div>
    </Transition>

    <!-- Filter & Search Toolbar -->
    <div class="bg-white p-4 rounded-2xl border border-gray-200/80 shadow-xs space-y-4">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-3">
        <!-- Status Tabs -->
        <div class="flex items-center gap-1 p-1 bg-gray-100 rounded-xl self-start">
          <button
            @click="statusFilter = 'ALL'"
            :class="statusFilter === 'ALL' ? 'bg-white text-gray-900 font-bold shadow-xs' : 'text-gray-600 hover:text-gray-900'"
            class="px-3.5 py-1.5 rounded-lg text-xs transition-all cursor-pointer"
          >
            Tất cả ({{ variants.length }})
          </button>
          <button
            @click="statusFilter = 'DISCOUNTED'"
            :class="statusFilter === 'DISCOUNTED' ? 'bg-white text-emerald-600 font-bold shadow-xs' : 'text-gray-600 hover:text-gray-900'"
            class="px-3.5 py-1.5 rounded-lg text-xs transition-all cursor-pointer"
          >
            Đang giảm giá ({{ discountedCount }})
          </button>
          <button
            @click="statusFilter = 'REGULAR'"
            :class="statusFilter === 'REGULAR' ? 'bg-white text-gray-900 font-bold shadow-xs' : 'text-gray-600 hover:text-gray-900'"
            class="px-3.5 py-1.5 rounded-lg text-xs transition-all cursor-pointer"
          >
            Nguyên giá ({{ variants.length - discountedCount }})
          </button>
        </div>

        <!-- Category & Sort -->
        <div class="flex flex-wrap items-center gap-2">
          <select
            v-model="categoryFilter"
            class="px-3 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs text-gray-700 outline-none focus:border-[#09f]"
          >
            <option value="">Tất cả danh mục</option>
            <option v-for="cat in availableCategories" :key="cat" :value="cat">
              {{ cat }}
            </option>
          </select>

          <select
            v-model="sortBy"
            class="px-3 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs text-gray-700 outline-none focus:border-[#09f]"
          >
            <option value="discount_desc">Mức giảm cao nhất</option>
            <option value="price_asc">Giá gốc: Thấp đến Cao</option>
            <option value="price_desc">Giá gốc: Cao đến Thấp</option>
            <option value="name_asc">Tên linh kiện: A-Z</option>
          </select>
        </div>
      </div>

      <!-- Search Input -->
      <div class="relative">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Tìm theo tên linh kiện, tên phân loại, sản phẩm..."
          class="w-full px-4 py-2.5 pl-10 bg-gray-50 border border-gray-200 rounded-xl text-xs sm:text-sm outline-none focus:border-[#09f] focus:bg-white transition-all"
        />
        <svg class="w-4 h-4 text-gray-400 absolute left-3.5 top-1/2 -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
        <button
          v-if="searchQuery"
          @click="searchQuery = ''"
          class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 text-xs"
        >
          ✕
        </button>
      </div>
    </div>

    <!-- Data Table -->
    <div class="bg-white rounded-2xl border border-gray-200/80 shadow-xs overflow-hidden">
      <!-- Loading State -->
      <div v-if="loading" class="p-12 text-center text-gray-500">
        <div class="animate-spin w-8 h-8 border-3 border-[#09f] border-t-transparent rounded-full mx-auto mb-3"></div>
        <p class="text-xs">Đang tải danh sách linh kiện...</p>
      </div>

      <!-- Empty State -->
      <div v-else-if="filteredVariants.length === 0" class="p-12 text-center text-gray-500">
        <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-3 text-gray-400">
          <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <p class="text-sm font-semibold text-gray-700">Không tìm thấy linh kiện nào</p>
        <p class="text-xs text-gray-400 mt-1">Thử đổi từ khóa tìm kiếm hoặc bộ lọc danh mục</p>
      </div>

      <!-- Table Body -->
      <div v-else class="overflow-x-auto">
        <table class="w-full text-left text-xs">
          <thead class="bg-gray-50/80 text-gray-500 uppercase font-semibold border-b border-gray-200 text-[10px] tracking-wider">
            <tr>
              <th class="p-3.5 w-10 text-center">
                <input
                  type="checkbox"
                  :checked="isAllSelected"
                  @change="toggleSelectAll"
                  class="rounded text-[#09f] focus:ring-[#09f]"
                />
              </th>
              <th class="p-3.5">Linh kiện / Phân loại</th>
              <th class="p-3.5">Danh mục</th>
              <th class="p-3.5 text-right">Giá gốc</th>
              <th class="p-3.5 text-center">Trạng thái</th>
              <th class="p-3.5">Thiết lập giảm giá</th>
              <th class="p-3.5 text-right w-28">Thao tác</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr
              v-for="v in filteredVariants"
              :key="v.id"
              class="hover:bg-blue-50/30 transition-colors"
              :class="{ 'bg-blue-50/20': selectedIds.has(v.id) }"
            >
              <!-- Checkbox -->
              <td class="p-3.5 text-center">
                <input
                  type="checkbox"
                  :checked="selectedIds.has(v.id)"
                  @change="toggleSelect(v.id)"
                  class="rounded text-[#09f] focus:ring-[#09f]"
                />
              </td>

              <!-- Product Info -->
              <td class="p-3.5">
                <div class="flex items-center gap-3 max-w-sm">
                  <img
                    :src="v.imageUrl || '/images/placeholder.png'"
                    :alt="v.variantName"
                    class="w-12 h-12 object-contain bg-gray-50 rounded-xl border border-gray-200 p-1 shrink-0"
                  />
                  <div class="min-w-0">
                    <p class="font-bold text-gray-900 truncate" :title="v.productName">
                      {{ v.productName }}
                    </p>
                    <p class="text-gray-500 font-medium truncate mt-0.5" :title="v.variantName">
                      Phân loại: <span class="text-gray-700 font-semibold">{{ v.variantName }}</span>
                    </p>
                    <span v-if="v.inStock !== undefined" class="text-[10px] text-gray-400">
                      Kho: {{ v.inStock }} sp
                    </span>
                  </div>
                </div>
              </td>

              <!-- Categories -->
              <td class="p-3.5">
                <div class="flex flex-wrap gap-1 max-w-[150px]">
                  <span
                    v-for="cat in (v.categoryNames || [])"
                    :key="cat"
                    class="px-2 py-0.5 bg-gray-100 text-gray-600 rounded-md text-[10px] font-medium"
                  >
                    {{ cat }}
                  </span>
                  <span v-if="!v.categoryNames || v.categoryNames.length === 0" class="text-gray-400 text-[10px]">
                    --
                  </span>
                </div>
              </td>

              <!-- Original Price -->
              <td class="p-3.5 text-right font-bold text-gray-700 whitespace-nowrap">
                {{ formatPrice(v.price) }}
              </td>

              <!-- Current Discount Status -->
              <td class="p-3.5 text-center whitespace-nowrap">
                <div v-if="isDiscounted(v)" class="space-y-1">
                  <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-rose-50 text-rose-600 border border-rose-200">
                    -{{ getDiscountPercent(v) }}%
                  </span>
                  <p class="font-bold text-emerald-600 text-xs">
                    {{ formatPrice(v.discountPrice) }}
                  </p>
                </div>
                <div v-else>
                  <span class="inline-block px-2 py-0.5 rounded-full text-[10px] font-medium bg-gray-100 text-gray-500">
                    Nguyên giá
                  </span>
                </div>
              </td>

              <!-- Inline Discount Config Inputs -->
              <td class="p-3.5">
                <div class="flex items-center gap-2">
                  <!-- Quick percent input -->
                  <div class="flex items-center border border-gray-300 rounded-lg overflow-hidden bg-white w-20 shrink-0">
                    <input
                      type="number"
                      min="1"
                      max="99"
                      placeholder="%"
                      :value="editForms[v.id]?.percent ?? ''"
                      @input="onPercentInput(v, ($event.target as HTMLInputElement).value)"
                      class="w-full px-2 py-1.5 text-xs text-center font-bold text-gray-800 outline-none"
                    />
                    <span class="pr-2 text-[10px] font-bold text-gray-400">%</span>
                  </div>

                  <!-- Direct VND price input -->
                  <div class="relative w-32 shrink-0">
                    <input
                      type="number"
                      min="0"
                      :max="v.price ? v.price - 1 : undefined"
                      placeholder="Giá ưu đãi VND"
                      :value="editForms[v.id]?.discountPrice ?? ''"
                      @input="onPriceInput(v, ($event.target as HTMLInputElement).value)"
                      class="w-full px-2.5 py-1.5 border border-gray-300 rounded-lg text-xs font-bold text-emerald-700 outline-none focus:border-[#09f]"
                    />
                  </div>
                </div>
              </td>

              <!-- Actions -->
              <td class="p-3.5 text-right whitespace-nowrap">
                <div class="flex items-center justify-end gap-1.5">
                  <!-- Save Button -->
                  <button
                    @click="saveDiscount(v)"
                    :disabled="savingIds.has(v.id) || !isRowModified(v)"
                    class="px-2.5 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer"
                    :class="isRowModified(v) ? 'bg-[#09f] hover:bg-[#0077cc] text-white shadow-xs' : 'bg-gray-100 text-gray-400 cursor-not-allowed'"
                    title="Lưu giá khuyến mãi"
                  >
                    <span v-if="savingIds.has(v.id)">...</span>
                    <span v-else>Lưu</span>
                  </button>

                  <!-- Cancel/Remove Discount Button -->
                  <button
                    v-if="v.discountPrice"
                    @click="removeDiscount(v)"
                    :disabled="savingIds.has(v.id)"
                    class="p-1.5 rounded-lg text-gray-400 hover:text-rose-600 hover:bg-rose-50 transition-colors cursor-pointer"
                    title="Hủy giá khuyến mãi (về giá gốc)"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'

definePageMeta({
  layout: 'admin',
  middleware: ['auth']
})

interface VariantItem {
  id: number
  productId: number
  productName: string
  productSlug: string
  variantName: string
  description?: string
  specifications?: string
  imageUrl?: string
  price: number
  discountPrice?: number | null
  inStock?: number
  isAvailable?: boolean
  categoryNames?: string[]
}

interface RowEditState {
  discountPrice: number | null
  percent: number | null
}

const API_BASE_URL = 'http://localhost:8080'
const { getAuthHeader } = useAuth()
const { success: showSuccess, error: showError } = useNotification()
const { confirm } = useConfirm()

// State
const variants = ref<VariantItem[]>([])
const loading = ref(false)
const savingIds = ref<Set<number>>(new Set())
const selectedIds = ref<Set<number>>(new Set())
const editForms = reactive<Record<number, RowEditState>>({})

// Filter states
const searchQuery = ref('')
const categoryFilter = ref('')
const statusFilter = ref<'ALL' | 'DISCOUNTED' | 'REGULAR'>('ALL')
const sortBy = ref<'discount_desc' | 'price_asc' | 'price_desc' | 'name_asc'>('discount_desc')

// Bulk action states
const bulkPercent = ref<number | null>(null)
const bulkLoading = ref(false)

// Lifecycle
onMounted(() => {
  fetchVariants()
})

// Fetch all variants
const fetchVariants = async () => {
  loading.value = true
  try {
    const data = await $fetch<VariantItem[]>(`${API_BASE_URL}/product-variants`, {
      headers: getAuthHeader() as Record<string, string>
    })
    variants.value = data || []

    // Initialize editForms
    for (const v of variants.value) {
      const dp = v.discountPrice != null && v.discountPrice > 0 ? Number(v.discountPrice) : null
      const pct = dp && v.price > dp ? Math.round(((v.price - dp) / v.price) * 100) : null
      editForms[v.id] = {
        discountPrice: dp,
        percent: pct
      }
    }
  } catch (err: any) {
    console.error('Error fetching variants for discount management:', err)
    showError(err?.data?.message || err?.message || 'Không thể tải danh sách linh kiện')
  } finally {
    loading.value = false
  }
}

// Helper computations
const isDiscounted = (v: VariantItem) => {
  return v.discountPrice != null && v.discountPrice > 0 && v.discountPrice < v.price
}

const getDiscountPercent = (v: VariantItem) => {
  if (!isDiscounted(v)) return 0
  return Math.round(((v.price - Number(v.discountPrice)) / v.price) * 100)
}

const discountedCount = computed(() => {
  return variants.value.filter(isDiscounted).length
})

const maxDiscountPercent = computed(() => {
  let max = 0
  for (const v of variants.value) {
    const pct = getDiscountPercent(v)
    if (pct > max) max = pct
  }
  return max
})

const availableCategories = computed(() => {
  const set = new Set<string>()
  for (const v of variants.value) {
    if (Array.isArray(v.categoryNames)) {
      v.categoryNames.forEach(c => set.add(c))
    }
  }
  return Array.from(set).sort()
})

// Filtered and sorted variants
const filteredVariants = computed(() => {
  let list = variants.value

  // Status Filter
  if (statusFilter.value === 'DISCOUNTED') {
    list = list.filter(isDiscounted)
  } else if (statusFilter.value === 'REGULAR') {
    list = list.filter(v => !isDiscounted(v))
  }

  // Category Filter
  if (categoryFilter.value) {
    list = list.filter(v => v.categoryNames && v.categoryNames.includes(categoryFilter.value))
  }

  // Search Query
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    list = list.filter(v =>
      (v.productName && v.productName.toLowerCase().includes(q)) ||
      (v.variantName && v.variantName.toLowerCase().includes(q)) ||
      (v.productSlug && v.productSlug.toLowerCase().includes(q))
    )
  }

  // Sort
  return [...list].sort((a, b) => {
    if (sortBy.value === 'discount_desc') {
      return getDiscountPercent(b) - getDiscountPercent(a)
    }
    if (sortBy.value === 'price_asc') {
      return a.price - b.price
    }
    if (sortBy.value === 'price_desc') {
      return b.price - a.price
    }
    if (sortBy.value === 'name_asc') {
      return (a.productName || '').localeCompare(b.productName || '')
    }
    return 0
  })
})

// Checkbox selection
const isAllSelected = computed(() => {
  return filteredVariants.value.length > 0 &&
    filteredVariants.value.every(v => selectedIds.value.has(v.id))
})

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    filteredVariants.value.forEach(v => selectedIds.value.delete(v.id))
  } else {
    filteredVariants.value.forEach(v => selectedIds.value.add(v.id))
  }
}

const toggleSelect = (id: number) => {
  if (selectedIds.value.has(id)) {
    selectedIds.value.delete(id)
  } else {
    selectedIds.value.add(id)
  }
}

// Input Handlers
const onPercentInput = (v: VariantItem, valStr: string) => {
  const p = editForms[v.id]
  if (!p) return
  if (!valStr || valStr.trim() === '') {
    p.percent = null
    p.discountPrice = null
    return
  }
  const pct = parseInt(valStr, 10)
  if (isNaN(pct) || pct <= 0) {
    p.percent = null
    p.discountPrice = null
    return
  }
  const validPct = Math.min(99, pct)
  p.percent = validPct
  // Calculate price = price * (100 - pct) / 100 rounded to 1,000 VND
  const rawPrice = v.price * ((100 - validPct) / 100)
  p.discountPrice = Math.round(rawPrice / 1000) * 1000
}

const onPriceInput = (v: VariantItem, valStr: string) => {
  const p = editForms[v.id]
  if (!p) return
  if (!valStr || valStr.trim() === '') {
    p.discountPrice = null
    p.percent = null
    return
  }
  const priceVal = parseFloat(valStr)
  if (isNaN(priceVal) || priceVal <= 0) {
    p.discountPrice = null
    p.percent = null
    return
  }
  p.discountPrice = priceVal
  if (priceVal < v.price) {
    p.percent = Math.round(((v.price - priceVal) / v.price) * 100)
  } else {
    p.percent = null
  }
}

const isRowModified = (v: VariantItem) => {
  const p = editForms[v.id]
  if (!p) return false
  const orig = v.discountPrice != null && v.discountPrice > 0 ? Number(v.discountPrice) : null
  const current = p.discountPrice != null && p.discountPrice > 0 ? Number(p.discountPrice) : null
  return orig !== current
}

// Save Single Discount
const saveDiscount = async (v: VariantItem) => {
  const p = editForms[v.id]
  if (!p) return

  if (p.discountPrice != null && p.discountPrice >= v.price) {
    showError(`Giá khuyến mãi (${formatPrice(p.discountPrice)}) phải nhỏ hơn giá gốc (${formatPrice(v.price)})!`)
    return
  }

  savingIds.value.add(v.id)
  try {
    const updated = await $fetch<VariantItem>(`${API_BASE_URL}/product-variants/${v.id}/discount`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        ...(getAuthHeader() as Record<string, string>)
      },
      body: JSON.stringify({
        discountPrice: p.discountPrice
      })
    })

    // Update local state
    v.discountPrice = updated.discountPrice
    const newDp = v.discountPrice != null && v.discountPrice > 0 ? Number(v.discountPrice) : null
    p.discountPrice = newDp
    p.percent = newDp && v.price > newDp ? Math.round(((v.price - newDp) / v.price) * 100) : null

    showSuccess(`Đã cập nhật giá khuyến mãi cho ${v.variantName || v.productName}`)
  } catch (err: any) {
    console.error('Error updating discount:', err)
    showError(err?.data?.message || err?.message || 'Không thể lưu giá khuyến mãi')
  } finally {
    savingIds.value.delete(v.id)
  }
}

// Remove Single Discount
const removeDiscount = async (v: VariantItem) => {
  const ok = await confirm(`Bạn có muốn hủy giá khuyến mãi cho "${v.variantName}" và đưa về giá gốc?`, 'Hủy khuyến mãi')
  if (!ok) return

  savingIds.value.add(v.id)
  try {
    await $fetch(`${API_BASE_URL}/product-variants/${v.id}/discount`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        ...(getAuthHeader() as Record<string, string>)
      },
      body: JSON.stringify({
        discountPrice: null
      })
    })

    v.discountPrice = null
    editForms[v.id] = {
      discountPrice: null,
      percent: null
    }

    showSuccess(`Đã đưa ${v.variantName} về giá gốc`)
  } catch (err: any) {
    console.error('Error removing discount:', err)
    showError(err?.data?.message || err?.message || 'Không thể hủy giá khuyến mãi')
  } finally {
    savingIds.value.delete(v.id)
  }
}

// Bulk Discount
const applyBulkDiscount = async () => {
  if (!bulkPercent.value || bulkPercent.value <= 0 || bulkPercent.value >= 100) {
    showError('Vui lòng nhập phần trăm giảm giá hợp lệ từ 1% đến 99%')
    return
  }

  const ids = Array.from(selectedIds.value)
  if (ids.length === 0) return

  const ok = await confirm(`Áp dụng giảm ${bulkPercent.value}% cho ${ids.length} linh kiện đã chọn?`, 'Xác nhận giảm giá hàng loạt')
  if (!ok) return

  bulkLoading.value = true
  try {
    const updatedList = await $fetch<VariantItem[]>(`${API_BASE_URL}/product-variants/batch-discount`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(getAuthHeader() as Record<string, string>)
      },
      body: JSON.stringify({
        variantIds: ids,
        discountPercent: bulkPercent.value
      })
    })

    // Update local variants
    const map = new Map(updatedList.map(u => [u.id, u]))
    for (const v of variants.value) {
      if (map.has(v.id)) {
        const u = map.get(v.id)!
        v.discountPrice = u.discountPrice
        const newDp = v.discountPrice != null && v.discountPrice > 0 ? Number(v.discountPrice) : null
        editForms[v.id] = {
          discountPrice: newDp,
          percent: newDp && v.price > newDp ? Math.round(((v.price - newDp) / v.price) * 100) : null
        }
      }
    }

    showSuccess(`Đã áp dụng giảm giá ${bulkPercent.value}% cho ${ids.length} linh kiện!`)
    selectedIds.value.clear()
    bulkPercent.value = null
  } catch (err: any) {
    console.error('Error batch updating discounts:', err)
    showError(err?.data?.message || err?.message || 'Không thể áp dụng giảm giá hàng loạt')
  } finally {
    bulkLoading.value = false
  }
}

// Clear Bulk Discount
const clearBulkDiscount = async () => {
  const ids = Array.from(selectedIds.value)
  if (ids.length === 0) return

  const ok = await confirm(`Hủy toàn bộ khuyến mãi cho ${ids.length} linh kiện đã chọn (về giá gốc)?`, 'Xác nhận hủy khuyến mãi')
  if (!ok) return

  bulkLoading.value = true
  try {
    const updatedList = await $fetch<VariantItem[]>(`${API_BASE_URL}/product-variants/batch-discount`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(getAuthHeader() as Record<string, string>)
      },
      body: JSON.stringify({
        variantIds: ids,
        discountPercent: 0
      })
    })

    const map = new Map(updatedList.map(u => [u.id, u]))
    for (const v of variants.value) {
      if (map.has(v.id)) {
        v.discountPrice = null
        editForms[v.id] = {
          discountPrice: null,
          percent: null
        }
      }
    }

    showSuccess(`Đã hủy khuyến mãi cho ${ids.length} linh kiện!`)
    selectedIds.value.clear()
  } catch (err: any) {
    console.error('Error clearing bulk discounts:', err)
    showError(err?.data?.message || err?.message || 'Không thể hủy khuyến mãi hàng loạt')
  } finally {
    bulkLoading.value = false
  }
}

// Price Formatter
const formatPrice = (price?: number | null) => {
  if (price == null) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}
</script>
