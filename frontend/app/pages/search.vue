<template>
  <div class="bg-gray-50 min-h-screen">
    <!-- Breadcrumb -->
    <section class="bg-white border-b border-gray-200">
      <div class="w-[90%] max-w-[1920px] mx-auto px-2 sm:px-4 lg:px-6 py-3">
        <div class="flex items-center gap-2 text-sm text-gray-600">
          <NuxtLink to="/" class="hover:text-[#09f] transition-colors duration-200">
            Trang chủ
          </NuxtLink>
          <span>/</span>
          <span class="text-gray-900 font-medium">Kết quả tìm kiếm</span>
        </div>
      </div>
    </section>

    <!-- Main Content -->
    <div class="w-[90%] max-w-[1920px] mx-auto px-2 sm:px-4 lg:px-6 py-8">
      <!-- Search Info -->
      <div class="mb-6">
        <h1 class="text-2xl font-bold text-gray-900 mb-2">
          <span v-if="searchQuery">
            Kết quả tìm kiếm cho: <span class="text-[#09f]">"{{ searchQuery }}"</span>
            <span v-if="selectedCategory" class="text-sm text-gray-600"> trong {{ getCategoryName(selectedCategory) }}</span>
          </span>
          <span v-else-if="selectedCategory">
            Danh mục: <span class="text-[#09f]">{{ getCategoryName(selectedCategory) }}</span>
          </span>
          <span v-else>Tất cả sản phẩm</span>
        </h1>
        <p class="text-gray-600">
          <span v-if="loading">Đang tải...</span>
          <span v-else>Tìm thấy <span class="font-semibold text-gray-900">{{ totalElements }}</span> sản phẩm</span>
        </p>
      </div>

      <div v-if="searchQuery" class="mb-6 space-y-3">
        <div v-if="relatedKeywords.length" class="flex flex-wrap items-center gap-2">
          <span class="text-sm font-semibold text-gray-600">Từ khóa liên quan:</span>
          <button
            v-for="keyword in relatedKeywords"
            :key="keyword"
            @click="searchForKeyword(keyword)"
            class="px-3 py-1.5 rounded-full bg-blue-50 text-[#0077cc] text-sm hover:bg-blue-100 transition-colors"
          >
            {{ keyword }}
          </button>
        </div>
        <div v-if="popularSearches.length" class="flex flex-wrap items-center gap-2">
          <span class="text-sm font-semibold text-gray-600">Từ khóa bạn hay tìm:</span>
          <button
            v-for="item in popularSearches"
            :key="item.query"
            @click="searchForKeyword(item.query)"
            class="px-3 py-1.5 rounded-full bg-gray-100 text-gray-700 text-sm hover:bg-gray-200 transition-colors"
          >
            {{ item.query }}
          </button>
        </div>
      </div>

      <div class="flex gap-6">
        <!-- Filters Sidebar -->
        <aside class="w-64 flex-shrink-0 hidden lg:block">
          <div class="bg-white rounded-2xl p-5 sticky top-24 space-y-6">
            <!-- Category Filter -->
            <div>
              <h3 class="font-semibold text-gray-900 mb-3 flex items-center justify-between">
                Danh mục
                <button
                  v-if="selectedCategory"
                  @click="selectedCategory = null"
                  class="text-xs text-[#09f] hover:text-[#0077cc] transition-colors duration-200"
                >
                  Xóa
                </button>
              </h3>
              <div class="space-y-2 max-h-64 overflow-y-auto">
                <label
                  v-for="category in categories"
                  :key="category.id"
                  class="flex items-center gap-2 cursor-pointer group"
                >
                  <input
                    type="radio"
                    :value="category.id"
                    v-model="selectedCategory"
                    class="w-4 h-4 text-[#09f] border-gray-300 focus:ring-[#09f] focus:ring-2 cursor-pointer"
                  />
                  <span class="text-sm text-gray-700 group-hover:text-[#09f] transition-colors duration-200">
                    {{ category.name }}
                  </span>
                </label>
              </div>
            </div>

            <!-- Price Filter: Thanh kéo 2 đầu (Dual Range Slider) -->
            <div class="bg-white border border-slate-200/90 rounded-2xl p-4 shadow-xs space-y-3.5">
              <!-- Tiêu đề & Nút đặt lại -->
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-2">
                  <span class="w-2 h-2 rounded-full bg-[#09f]"></span>
                  <h3 class="font-bold text-slate-900 text-sm">Khoảng giá</h3>
                </div>
                <button
                  v-if="isPriceFilterActive"
                  @click="resetPriceFilter"
                  class="text-xs font-semibold text-[#09f] hover:text-[#0077cc] hover:underline cursor-pointer transition-colors"
                >
                  Đặt lại
                </button>
              </div>

              <!-- 2 Ô nhập giá ở 2 đầu (Min - Max) -->
              <div class="grid grid-cols-2 gap-2">
                <div class="bg-slate-50 border border-slate-200 rounded-xl p-2.5 focus-within:border-[#09f] focus-within:bg-white focus-within:ring-2 focus-within:ring-[#09f]/15 transition-all">
                  <span class="text-[10px] font-semibold text-slate-400 block mb-0.5">Từ</span>
                  <div class="flex items-center gap-1">
                    <input
                      type="text"
                      inputmode="numeric"
                      v-model="minInputText"
                      @focus="onMinInputFocus"
                      @blur="onMinInputBlur"
                      @keydown.enter="onMinInputBlur"
                      class="w-full text-xs font-bold text-slate-800 font-mono bg-transparent focus:outline-none"
                      placeholder="0"
                    />
                    <span class="text-[11px] font-medium text-slate-400 select-none">₫</span>
                  </div>
                </div>

                <div class="bg-slate-50 border border-slate-200 rounded-xl p-2.5 focus-within:border-[#09f] focus-within:bg-white focus-within:ring-2 focus-within:ring-[#09f]/15 transition-all">
                  <span class="text-[10px] font-semibold text-slate-400 block mb-0.5">Đến</span>
                  <div class="flex items-center gap-1">
                    <input
                      type="text"
                      inputmode="numeric"
                      v-model="maxInputText"
                      @focus="onMaxInputFocus"
                      @blur="onMaxInputBlur"
                      @keydown.enter="onMaxInputBlur"
                      class="w-full text-xs font-bold text-slate-800 font-mono bg-transparent focus:outline-none"
                      placeholder="5.000.000"
                    />
                    <span class="text-[11px] font-medium text-slate-400 select-none">₫</span>
                  </div>
                </div>
              </div>

              <!-- Thanh trượt kéo 2 đầu (Dual Range Slider) -->
              <div class="pt-2 px-1">
                <div class="relative w-full h-7 flex items-center select-none">
                  <!-- Thanh ray nền xám -->
                  <div class="w-full h-2 bg-slate-200 rounded-full relative overflow-hidden">
                    <!-- Dải highlight xanh giữa 2 đầu kéo -->
                    <div
                      class="absolute top-0 bottom-0 bg-gradient-to-r from-[#0088ea] to-[#09f] rounded-full transition-all duration-75"
                      :style="{
                        left: progressLeft + '%',
                        width: Math.max(0, 100 - progressLeft - progressRight) + '%'
                      }"
                    ></div>
                  </div>

                  <!-- Nút kéo Min Thumb -->
                  <input
                    type="range"
                    :min="SLIDER_MIN"
                    :max="sliderMaxDynamic"
                    :step="STEP"
                    v-model.number="currentMin"
                    @input="onMinSliderInput"
                    :style="{ zIndex: currentMin > (sliderMaxDynamic - SLIDER_MIN) * 0.75 ? 30 : 20 }"
                    class="dual-range-slider pointer-events-none absolute inset-0 w-full h-full appearance-none bg-transparent m-0 cursor-pointer"
                    aria-label="Kéo giá tối thiểu"
                  />

                  <!-- Nút kéo Max Thumb -->
                  <input
                    type="range"
                    :min="SLIDER_MIN"
                    :max="sliderMaxDynamic"
                    :step="STEP"
                    v-model.number="currentMax"
                    @input="onMaxSliderInput"
                    :style="{ zIndex: currentMin > (sliderMaxDynamic - SLIDER_MIN) * 0.75 ? 20 : 30 }"
                    class="dual-range-slider pointer-events-none absolute inset-0 w-full h-full appearance-none bg-transparent m-0 cursor-pointer"
                    aria-label="Kéo giá tối đa"
                  />
                </div>

                <!-- Nhãn 2 đầu và bước nhảy -->
                <div class="flex items-center justify-between text-[11px] text-slate-400 font-mono mt-0.5 select-none">
                  <span>{{ formatCompactPrice(SLIDER_MIN) }}</span>
                  <span class="text-slate-400 font-sans text-[10px]"></span>
                  <span>{{ formatCompactPrice(sliderMaxDynamic) }}</span>
                </div>
              </div>

              <!-- Nút Áp dụng bộ lọc -->
              <button
                @click="triggerPriceFilterImmediate"
                class="w-full py-2 px-3 bg-gradient-to-r from-[#09f] to-[#0077cc] hover:from-[#33adff] hover:to-[#0088ea] text-white rounded-xl text-xs font-bold shadow-xs hover:shadow-sm hover:shadow-[#09f]/25 transition-all cursor-pointer flex items-center justify-center gap-1.5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#09f]"
              >
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
                </svg>
                <span>Áp dụng ({{ formatCompactPrice(currentMin) }} - {{ formatCompactPrice(currentMax) }})</span>
              </button>

              <!-- Gợi ý nhanh (Presets) -->
              <div class="pt-2 border-t border-slate-100">
                <p class="text-[11px] font-semibold text-slate-500 mb-2">Gợi ý nhanh:</p>
                <div class="flex flex-wrap gap-1.5">
                  <button
                    v-for="preset in pricePresets"
                    :key="preset.id"
                    @click="applyPreset(preset)"
                    class="px-2.5 py-1 rounded-lg text-[11px] font-medium transition-all cursor-pointer border"
                    :class="isPresetActive(preset)
                      ? 'bg-[#09f] text-white border-[#09f] font-bold shadow-xs'
                      : 'bg-slate-50 text-slate-600 border-slate-200/80 hover:border-slate-300 hover:text-slate-900'"
                  >
                    {{ preset.label }}
                  </button>
                </div>
              </div>
            </div>

            <!-- Sale Filter -->
            <div>
              <h3 class="font-semibold text-gray-900 mb-3">Khác</h3>
              <label class="flex items-center gap-2 cursor-pointer group">
                <input
                  type="checkbox"
                  v-model="onlyShowSale"
                  class="w-4 h-4 text-[#09f] border-gray-300 rounded focus:ring-[#09f] focus:ring-2 cursor-pointer"
                />
                <span class="text-sm text-gray-700 group-hover:text-[#09f] transition-colors duration-200">
                  Chỉ hiển thị sản phẩm giảm giá
                </span>
              </label>
            </div>

            <!-- Clear All Filters -->
            <button
              v-if="hasActiveFilters"
              @click="clearAllFilters"
              class="w-full px-4 py-2 border-2 border-gray-300 text-gray-700 rounded-xl text-sm font-medium transition-all duration-300 hover:border-[#09f] hover:text-[#09f] hover:bg-blue-50"
            >
              Xóa tất cả bộ lọc
            </button>
          </div>
        </aside>

        <!-- Products Grid -->
        <div class="flex-1">
          <!-- Sort Options -->
          <div class="bg-white rounded-2xl p-4 mb-6 flex items-center justify-between flex-wrap gap-3">
            <div class="flex items-center gap-2 text-sm text-gray-600">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4h13M3 8h9m-9 4h6m4 0l4-4m0 0l4 4m-4-4v12" />
              </svg>
              <span>Sắp xếp theo:</span>
            </div>
            <div class="flex gap-2 flex-wrap">
              <button
                v-for="option in sortOptions"
                :key="option.value"
                @click="sortBy = option.value"
                :class="[
                  'px-4 py-2 rounded-xl text-sm font-medium transition-all duration-300',
                  sortBy === option.value
                    ? 'bg-[#09f] text-white shadow-lg'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                ]"
              >
                {{ option.label }}
              </button>
            </div>
          </div>

          <!-- Products List -->
          <div v-if="loading" class="text-center py-12">
            <div class="inline-block animate-spin rounded-full h-12 w-12 border-4 border-gray-300 border-t-[#09f]"></div>
            <p class="mt-4 text-gray-600">Đang tải sản phẩm...</p>
          </div>

          <div v-else-if="sortedProducts.length > 0" class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-6">
            <NuxtLink
              v-for="product in paginatedProducts"
              :key="product.id"
              :to="`/products/${product.id}`"
              class="bg-white border-2 border-gray-100 rounded-2xl overflow-hidden transition-all duration-300 hover:shadow-xl hover:scale-105 hover:-translate-y-1 hover:border-[#09f] group cursor-pointer"
            >
              <!-- Product Image -->
              <div class="relative aspect-square bg-gradient-to-br from-blue-50 to-cyan-50 flex items-center justify-center overflow-hidden p-6">
                <img 
                  :src="product.img" 
                  :alt="product.name"
                  class="w-full h-full object-contain group-hover:scale-110 transition-transform duration-300"
                />
                <!-- Category Badge -->
                <div class="absolute top-3 left-3 bg-white/90 backdrop-blur-sm text-gray-700 px-3 py-1 rounded-lg text-xs font-medium">
                  {{ product.category }}
                </div>
                <!-- Variant Count Badge -->
                <div v-if="product.variantCount > 1" class="absolute bottom-3 right-3 bg-purple-100 text-purple-800 px-2 py-1 rounded-lg text-xs font-medium">
                  {{ product.variantCount }} phân loại
                </div>
              </div>

              <!-- Product Info -->
              <div class="p-4">
                <h3 class="text-sm font-semibold text-gray-800 mb-2 line-clamp-2 group-hover:text-[#09f] transition-colors duration-200">
                  {{ product.name }}
                </h3>
                
                <!-- Price Range & Discount: 10.000đ 20.000đ 50% -->
                <div class="flex items-center gap-2 mb-2 flex-wrap">
                  <span class="font-bold text-[#09f]">
                    {{ product.priceRange }}
                  </span>
                  <span v-if="product.originalPrice && product.originalPrice > product.minPrice" class="text-sm text-gray-400 line-through">
                    {{ formatPrice(product.originalPrice) }}
                  </span>
                  <span v-if="product.hasDiscount && product.discountPercent" class="text-xs font-bold text-white bg-rose-600 px-1.5 py-0.5 rounded-md shadow-xs">
                    {{ product.discountPercent }}%
                  </span>
                </div>

                <!-- Rating, Đã bán, Còn lại -->
                <div class="flex items-center justify-between text-xs text-gray-500 mb-3 pt-2 border-t border-gray-100">
                  <div class="flex items-center gap-1 font-bold text-amber-500">
                    <span>★</span>
                    <span>{{ Number(product.rating || 5.0).toFixed(1) }}</span>
                  </div>
                  <div class="flex items-center gap-2 text-[11px] text-gray-500">
                    <span>Đã bán {{ product.soldQuantity || 0 }}</span>
                    <span>•</span>
                    <span :class="product.totalStock > 0 ? 'text-emerald-700 font-semibold' : 'text-rose-600 font-semibold'">
                      {{ product.totalStock > 0 ? `Còn ${product.totalStock}` : 'Hết hàng' }}
                    </span>
                  </div>
                </div>

                <!-- Actions -->
                <div class="flex gap-2">
                  <button @click.prevent="buyNow(product.productId)" class="flex-1 bg-[#09f] text-white py-2 rounded-xl text-sm font-medium transition-all duration-300 hover:bg-[#0077cc] hover:shadow-lg active:scale-95">
                    Mua ngay
                  </button>
                </div>
              </div>
            </NuxtLink>
          </div>

          <!-- Empty State -->
          <div v-else class="bg-white rounded-2xl p-12 text-center">
            <div class="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg class="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
            <h3 class="text-xl font-semibold text-gray-900 mb-2">Không tìm thấy sản phẩm</h3>
            <p class="text-gray-600 mb-6">Thử điều chỉnh bộ lọc hoặc tìm kiếm từ khóa khác</p>
            <button
              @click="clearAllFilters"
              class="px-6 py-2.5 bg-[#09f] text-white rounded-xl font-medium transition-all duration-300 hover:bg-[#0077cc] hover:shadow-lg hover:scale-105"
            >
              Xóa bộ lọc
            </button>
          </div>

          <!-- Pagination -->
          <div v-if="totalPages > 1" class="mt-8 flex justify-center items-center gap-2">
            <button
              @click="currentPage--"
              :disabled="currentPage === 1"
              class="w-10 h-10 rounded-xl border-2 border-gray-200 flex items-center justify-center transition-all duration-300 hover:border-[#09f] hover:text-[#09f] disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:border-gray-200 disabled:hover:text-gray-400"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
              </svg>
            </button>

            <div class="flex gap-2">
              <button
                v-for="page in displayedPages"
                :key="page"
                @click="currentPage = page"
                :class="[
                  'w-10 h-10 rounded-xl font-medium transition-all duration-300',
                  currentPage === page
                    ? 'bg-[#09f] text-white shadow-lg scale-110'
                    : 'border-2 border-gray-200 text-gray-700 hover:border-[#09f] hover:text-[#09f]'
                ]"
              >
                {{ page }}
              </button>
            </div>

            <button
              @click="currentPage++"
              :disabled="currentPage === totalPages"
              class="w-10 h-10 rounded-xl border-2 border-gray-200 flex items-center justify-center transition-all duration-300 hover:border-[#09f] hover:text-[#09f] disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:border-gray-200 disabled:hover:text-gray-400"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </button>
          </div>

          <section v-if="searchQuery && (relatedLoading || relatedProducts.length)" class="mt-10">
            <div class="flex items-end justify-between mb-4">
              <div>
                <h2 class="text-xl font-bold text-gray-900">Sản phẩm liên quan</h2>
                <p class="text-sm text-gray-500 mt-1">Sản phẩm cùng nhóm hoặc thường được mua kèm</p>
              </div>
            </div>
            <div v-if="relatedLoading" class="text-sm text-gray-500 py-4">Đang tìm sản phẩm phù hợp...</div>
            <div v-else class="grid grid-cols-2 sm:grid-cols-3 xl:grid-cols-4 gap-4">
              <NuxtLink
                v-for="product in relatedProducts"
                :key="product.id"
                :to="`/products/${product.id}`"
                class="bg-white border border-gray-200 rounded-xl p-3 hover:border-[#09f] hover:shadow-md transition-all"
              >
                <img :src="product.img" :alt="product.name" class="w-full aspect-square object-contain bg-gray-50 rounded-lg mb-3" />
                <h3 class="text-sm font-semibold text-gray-800 line-clamp-2">{{ product.name }}</h3>
                <p class="text-sm font-bold text-[#0077cc] mt-2">{{ product.priceRange }}</p>
                <p class="text-xs text-gray-500 mt-1">Còn {{ product.totalStock }} sản phẩm</p>
              </NuxtLink>
            </div>
          </section>
        </div>
      </div>
    </div>
    
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const API_BASE_URL = 'http://localhost:8080/api'

const searchQuery = ref(String(route.query.q || ''))
const selectedCategory = ref<number | null>(route.query.category ? Number(route.query.category) : null)
const onlyShowSale = ref(false)
const sortBy = ref('newest')
const currentPage = ref(1)
const itemsPerPage = 12

// ==================== BỘ LỌC KHOẢNG GIÁ KÉO 2 ĐẦU (DUAL RANGE SLIDER) ====================
const STEP = 50000 // 50.000đ mỗi bước nhảy
const MIN_GAP = 50000 // 50.000đ chênh lệch tối thiểu giữa 2 đầu kéo
const SLIDER_MIN = 0
const DEFAULT_SLIDER_MAX = 5000000

// Giá trị lọc đang được chọn từ 2 đầu kéo (Step 50k, lệch >= 50k)
const currentMin = ref(0)
const currentMax = ref(DEFAULT_SLIDER_MAX)
const isPriceFilterActive = ref(false)

// Thước đo tối đa tự động co giãn nếu người dùng nhập số lớn hơn 5.000.000
const sliderMaxDynamic = computed(() => {
  return Math.max(DEFAULT_SLIDER_MAX, currentMax.value)
})

// Format số tiền chuẩn tiếng Việt (vd: 500.000)
const formatNumberVn = (val: number) => {
  return new Intl.NumberFormat('vi-VN').format(val || 0)
}

// Format giá trị thu gọn (50k, 100k, 1.5Tr, v.v.)
const formatCompactPrice = (val: number) => {
  if (val >= 1000000) {
    const m = (val / 1000000)
    return `${m % 1 === 0 ? m.toFixed(0) : m.toFixed(1)}Tr`
  }
  if (val >= 1000) {
    return `${(val / 1000).toFixed(0)}k`
  }
  return `${val}đ`
}

// Giá trị hiển thị trên 2 ô input ở 2 đầu
const minInputText = ref('0')
const maxInputText = ref(formatNumberVn(DEFAULT_SLIDER_MAX))

// Tỷ lệ % vị trí để vẽ dải highlight xanh giữa 2 đầu kéo
const progressLeft = computed(() => {
  const range = sliderMaxDynamic.value - SLIDER_MIN
  if (range <= 0) return 0
  const pct = ((currentMin.value - SLIDER_MIN) / range) * 100
  return Math.min(100, Math.max(0, pct))
})

const progressRight = computed(() => {
  const range = sliderMaxDynamic.value - SLIDER_MIN
  if (range <= 0) return 0
  const pct = 100 - (((currentMax.value - SLIDER_MIN) / range) * 100)
  return Math.min(100, Math.max(0, pct))
})

// Xử lý kéo thanh Min: Không vượt quá currentMax - 50k
const onMinSliderInput = () => {
  if (currentMin.value > currentMax.value - MIN_GAP) {
    currentMin.value = currentMax.value - MIN_GAP
  }
  currentMin.value = Math.round(currentMin.value / STEP) * STEP
  minInputText.value = formatNumberVn(currentMin.value)
  triggerPriceFilterDebounced()
}

// Xử lý kéo thanh Max: Không nhỏ hơn currentMin + 50k
const onMaxSliderInput = () => {
  if (currentMax.value < currentMin.value + MIN_GAP) {
    currentMax.value = currentMin.value + MIN_GAP
  }
  currentMax.value = Math.round(currentMax.value / STEP) * STEP
  maxInputText.value = formatNumberVn(currentMax.value)
  triggerPriceFilterDebounced()
}

// Xử lý khi người dùng nhập số trực tiếp vào ô Min ở đầu trái
const onMinInputFocus = () => {
  minInputText.value = String(currentMin.value)
}

const onMinInputBlur = (val?: string) => {
  const text = typeof val === 'string' ? val : minInputText.value
  const digits = String(text).replace(/\D/g, '')
  let num = digits ? parseInt(digits, 10) : 0
  num = Math.max(0, Math.round(num / STEP) * STEP)
  if (num > currentMax.value - MIN_GAP) {
    num = currentMax.value - MIN_GAP
  }
  currentMin.value = num
  minInputText.value = formatNumberVn(num)
  triggerPriceFilterImmediate()
}

// Xử lý khi người dùng nhập số trực tiếp vào ô Max ở đầu phải
const onMaxInputFocus = () => {
  maxInputText.value = String(currentMax.value)
}

const onMaxInputBlur = (val?: string) => {
  const text = typeof val === 'string' ? val : maxInputText.value
  const digits = String(text).replace(/\D/g, '')
  let num = digits ? parseInt(digits, 10) : (currentMin.value + MIN_GAP)
  num = Math.round(num / STEP) * STEP
  if (num < currentMin.value + MIN_GAP) {
    num = currentMin.value + MIN_GAP
  }
  currentMax.value = num
  maxInputText.value = formatNumberVn(num)
  triggerPriceFilterImmediate()
}

// Presets gợi ý nhanh
const pricePresets = [
  { id: 'all', label: 'Tất cả', min: 0, max: DEFAULT_SLIDER_MAX },
  { id: 'u100k', label: '< 100k', min: 0, max: 100000 },
  { id: '100-500k', label: '100k - 500k', min: 100000, max: 500000 },
  { id: '500k-1m', label: '500k - 1Tr', min: 500000, max: 1000000 },
  { id: '1m-5m', label: '1Tr - 5Tr', min: 1000000, max: 5000000 }
]

const isPresetActive = (preset: any) => {
  if (!isPriceFilterActive.value && preset.id === 'all') return true
  return isPriceFilterActive.value && currentMin.value === preset.min && currentMax.value === preset.max
}

const applyPreset = (preset: any) => {
  if (preset.id === 'all') {
    resetPriceFilter()
    return
  }
  currentMin.value = preset.min
  currentMax.value = preset.max
  minInputText.value = formatNumberVn(preset.min)
  maxInputText.value = formatNumberVn(preset.max)
  triggerPriceFilterImmediate()
}

// Debounce trigger khi kéo thanh trượt
let priceDebounceTimer: any = null
const triggerPriceFilterDebounced = () => {
  isPriceFilterActive.value = true
  if (priceDebounceTimer) clearTimeout(priceDebounceTimer)
  priceDebounceTimer = setTimeout(() => {
    currentPage.value = 1
    searchProducts()
  }, 350)
}

const triggerPriceFilterImmediate = () => {
  isPriceFilterActive.value = true
  if (priceDebounceTimer) clearTimeout(priceDebounceTimer)
  currentPage.value = 1
  searchProducts()
}

const resetPriceFilter = () => {
  isPriceFilterActive.value = false
  if (priceDebounceTimer) clearTimeout(priceDebounceTimer)
  currentMin.value = 0
  currentMax.value = DEFAULT_SLIDER_MAX
  minInputText.value = '0'
  maxInputText.value = formatNumberVn(DEFAULT_SLIDER_MAX)
  currentPage.value = 1
  searchProducts()
}

// State
const categories = ref<any[]>([])
const products = ref<any[]>([])
const loading = ref(false)
const totalElements = ref(0)
const relatedProducts = ref<any[]>([])
const relatedLoading = ref(false)
const popularSearches = ref<{ query: string; count: number }[]>([])
const lastRecordedSearch = ref('')

const keywordSuggestions: Record<string, string[]> = {
  esp32: ['ESP32 DevKit', 'ESP32-WROOM', 'NodeMCU', 'cảm biến', 'module nguồn'],
  arduino: ['Arduino Uno', 'Arduino Nano', 'ESP32', 'cảm biến', 'module relay'],
  raspberry: ['Raspberry Pi 5', 'thẻ nhớ microSD', 'nguồn USB-C', 'cảm biến'],
  cam: ['cảm biến khoảng cách', 'cảm biến nhiệt độ', 'Arduino', 'ESP32'],
  sensor: ['cảm biến khoảng cách', 'cảm biến nhiệt độ', 'Arduino', 'ESP32'],
  dien: ['điện trở 10k', 'tụ điện', 'IC Logic', 'breadboard']
}

const relatedKeywords = computed(() => {
  const query = searchQuery.value.toLowerCase()
  const mapped = Object.entries(keywordSuggestions).find(([key]) => query.includes(key))?.[1] || []
  const fromProducts = products.value
    .flatMap((product: any) => [
      ...(product.categories || []).map((category: any) => category.name),
      ...(product.name || '').split(/[\s-]+/)
    ])
    .filter((term: string) => term && term.length > 2 && !query.includes(term.toLowerCase()))
  const unique = new Map<string, string>()
  for (const keyword of [...mapped, ...fromProducts]) {
    unique.set(keyword.toLowerCase(), keyword)
  }
  return [...unique.values()].slice(0, 6)
})

const loadPopularSearches = async () => {
  try {
    const res = await $fetch<any[]>(`${API_BASE_URL}/search/popular?limit=8`)
    if (res && Array.isArray(res) && res.length > 0) {
      popularSearches.value = res.map((item: any) => ({
        query: item.keyword,
        count: item.searchCount
      }))
      return
    }
  } catch (error) {
    console.warn('Could not load popular searches from API:', error)
  }
  try {
    const stored = localStorage.getItem('daln_search_terms')
    popularSearches.value = stored ? JSON.parse(stored) : []
  } catch (error) {
    popularSearches.value = []
  }
}

const recordSearchTerm = async (query: string, resultsCount: number = 0) => {
  const term = query.trim()
  if (!term || term.toLowerCase() === lastRecordedSearch.value.toLowerCase()) return
  lastRecordedSearch.value = term
  try {
    await $fetch(`${API_BASE_URL}/search/record`, {
      method: 'POST',
      body: { keyword: term, resultsCount }
    })
  } catch (e) {
    // Non-blocking fallback
  }
  try {
    const stored = localStorage.getItem('daln_search_terms')
    const counts: { query: string; count: number }[] = stored ? JSON.parse(stored) : []
    const existing = counts.find(item => item.query.toLowerCase() === term.toLowerCase())
    if (existing) existing.count += 1
    else counts.push({ query: term, count: 1 })
    counts.sort((a, b) => b.count - a.count)
    localStorage.setItem('daln_search_terms', JSON.stringify(counts.slice(0, 20)))
    if (!popularSearches.value.length) {
      popularSearches.value = counts.slice(0, 5)
    }
  } catch (error) {
    console.error('Could not save search history:', error)
  }
}

const searchForKeyword = (keyword: string) => {
  router.push({ path: '/search', query: { q: keyword } })
}

const loadRelatedProducts = async (sourceProducts: any[]) => {
  if (!searchQuery.value || sourceProducts.length === 0) {
    relatedProducts.value = []
    return
  }
  relatedLoading.value = true
  try {
    const sourceIds = new Set(sourceProducts.map(product => String(product.id)))
    const results = await Promise.all(
      sourceProducts.slice(0, 3).map(product =>
        $fetch<any[]>(`${API_BASE_URL}/products/search/related/${product.id}`, {
          params: { limit: 6 }
        })
      )
    )
    const unique = new Map<string, any>()
    results.flat().forEach(product => {
      if (!sourceIds.has(String(product.id)) && !unique.has(String(product.id))) {
        unique.set(String(product.id), mapProduct(product))
      }
    })
    relatedProducts.value = [...unique.values()].slice(0, 8)
  } catch (error) {
    console.error('Could not load related products:', error)
    relatedProducts.value = []
  } finally {
    relatedLoading.value = false
  }
}

const priceRanges = [
  { id: '0-100k', label: 'Dưới 100.000đ', min: 0, max: 100000 },
  { id: '100k-500k', label: '100.000đ - 500.000đ', min: 100000, max: 500000 },
  { id: '500k-1m', label: '500.000đ - 1.000.000đ', min: 500000, max: 1000000 },
  { id: '1m-5m', label: '1.000.000đ - 5.000.000đ', min: 1000000, max: 5000000 },
  { id: '5m+', label: 'Trên 5.000.000đ', min: 5000000, max: Infinity }
]

const sortOptions = [
  { value: 'newest', label: 'Mới nhất' },
  { value: 'bestseller', label: 'Bán chạy' },
  { value: 'rating_desc', label: 'Đánh giá cao' },
  { value: 'price-asc', label: 'Giá thấp đến cao' },
  { value: 'price-desc', label: 'Giá cao đến thấp' },
  { value: 'name_asc', label: 'Tên A-Z' },
  { value: 'name_desc', label: 'Tên Z-A' }
]

// API Functions
const searchProducts = async () => {
  loading.value = true
  try {
    let result: any = null
    let apiUrl = ''

    console.log('🎯 Search Params:', {
      searchQuery: searchQuery.value,
      selectedCategory: selectedCategory.value,
      priceFilterActive: isPriceFilterActive.value,
      priceRange: isPriceFilterActive.value ? `${currentMin.value} - ${currentMax.value}` : 'all',
      onlyShowSale: onlyShowSale.value,
      sortBy: sortBy.value
    })

    // Map sortBy value to backend format
    const backendSortBy = sortBy.value === 'price-asc' ? 'name_asc' : 
                          sortBy.value === 'price-desc' ? 'name_desc' : 
                          sortBy.value

    // 1. Nếu chỉ lọc sản phẩm giảm giá (có thể kết hợp sort)
    if (onlyShowSale.value && !selectedCategory.value && !isPriceFilterActive.value && !searchQuery.value) {
      // GET /api/products/search/filter/on-sale
      apiUrl = `${API_BASE_URL}/products/search/filter/on-sale`
      result = await $fetch(apiUrl, {
        params: {
          page: currentPage.value - 1,
          size: itemsPerPage,
          sortBy: backendSortBy
        }
      })
    }
    // 2. Nếu chỉ có khoảng giá (không có category, sale, search)
    else if (isPriceFilterActive.value && !selectedCategory.value && !onlyShowSale.value && !searchQuery.value) {
      // GET /api/products/search/filter/by-price
      apiUrl = `${API_BASE_URL}/products/search/filter/by-price`
      result = await $fetch(apiUrl, {
        params: {
          minPrice: currentMin.value,
          maxPrice: currentMax.value,
          page: currentPage.value - 1,
          size: itemsPerPage,
          sortBy: backendSortBy
        }
      })
    }
    // 3. Nếu có search query (có thể kết hợp với category, price, sale)
    else if (searchQuery.value) {
      // GET /api/products/search/by-product-name (lấy tất cả kết quả)
      apiUrl = `${API_BASE_URL}/products/search/by-product-name`
      result = await $fetch(apiUrl, {
        params: {
          productName: searchQuery.value
        }
      })
      // Sẽ filter theo category/price/sale ở client-side sau
    }
    // 4. Nếu chỉ lọc theo category (không có filter khác)
    else if (selectedCategory.value && !isPriceFilterActive.value && !onlyShowSale.value && !searchQuery.value) {
      // GET /api/products/search/by-category-id/{categoryId}
      apiUrl = `${API_BASE_URL}/products/search/by-category-id/${selectedCategory.value}`
      result = await $fetch(apiUrl)
      console.log('📂 Category ID:', selectedCategory.value)
    }
    // 5. Nếu có nhiều điều kiện lọc kết hợp (KHÔNG có search), dùng POST filter
    else {
      const filterRequest: any = {
        page: currentPage.value - 1,
        size: itemsPerPage
      }

      // Thêm điều kiện lọc giá từ thanh trượt 2 đầu
      if (isPriceFilterActive.value) {
        filterRequest.minPrice = currentMin.value
        filterRequest.maxPrice = currentMax.value
      }

      // Thêm điều kiện lọc category
      if (selectedCategory.value) {
        filterRequest.categoryId = selectedCategory.value
      }

      console.log('Filter Request:', filterRequest)

      // POST /api/products/search/filter
      apiUrl = `${API_BASE_URL}/products/search/filter`
      result = await $fetch(apiUrl, {
        method: 'POST',
        body: filterRequest
      })
    }

    console.log('API Called:', apiUrl)
    console.log('Response:', result)
    console.log('Response Type:', typeof result)
    console.log('Is Array:', Array.isArray(result))

    if (result) {
      // Nếu result là array trực tiếp (không có pagination)
      if (Array.isArray(result)) {
        // Không flatten variants nữa - hiển thị sản phẩm với khoảng giá
        let finalProducts = result
        
        // Filter by category
        if (selectedCategory.value && searchQuery.value) {
          finalProducts = finalProducts.filter((p: any) => {
            return p.categories && p.categories.some((cat: any) => cat.id === selectedCategory.value)
          })
        }
        
        // Filter by price range từ thanh trượt 2 đầu (sử dụng minPrice của product)
        if (isPriceFilterActive.value && searchQuery.value) {
          finalProducts = finalProducts.filter((p: any) => {
            const minPrice = p.minDiscountPrice || p.minPrice || 0
            return minPrice >= currentMin.value && minPrice <= currentMax.value
          })
        }
        
        // Filter by sale (hasDiscount từ backend)
        if (onlyShowSale.value && searchQuery.value) {
          finalProducts = finalProducts.filter((p: any) => p.hasDiscount)
        }
        
        products.value = finalProducts
        totalElements.value = finalProducts.length
      } 
      // Nếu result có cấu trúc pagination (Page object)
      else if (result.content) {
        // Không flatten variants - sử dụng products với khoảng giá
        products.value = result.content
        totalElements.value = result.totalElements || result.content.length
      }
      // Fallback
      else {
        products.value = []
        totalElements.value = 0
      }
    }
    if (searchQuery.value) {
      void loadRelatedProducts(products.value.slice(0, 3))
      recordSearchTerm(searchQuery.value, products.value.length)
    } else {
      relatedProducts.value = []
    }
  } catch (error: any) {
    console.error('Error searching products:', error)
    products.value = []
    totalElements.value = 0
    relatedProducts.value = []
  } finally {
    loading.value = false
  }
}

// Map product data với price range (không còn flatten variants)
const mapProduct = (p: any) => {
  // Tính khoảng giá từ minPrice/maxPrice
  const minPrice = p.minDiscountPrice || p.minPrice || 0
  const maxPrice = p.maxDiscountPrice || p.maxPrice || 0
  const originalPrice = p.minPrice || 0
  
  // Format price range
  let priceRange = ''
  if (minPrice === maxPrice || maxPrice === 0) {
    priceRange = formatPrice(minPrice)
  } else {
    priceRange = `${formatPrice(minPrice)} - ${formatPrice(maxPrice)}`
  }

  const discountPercent = (originalPrice > minPrice && minPrice > 0)
    ? Math.round(((originalPrice - minPrice) / originalPrice) * 100)
    : (p.hasDiscount ? 30 : 0)
  
  return {
    id: p.id,
    productId: p.id,
    name: p.name,
    category: p.categories?.[0]?.name || 'N/A',
    img: p.defaultImageUrl ? `http://localhost:8080${p.defaultImageUrl}` : (p.categories?.[0]?.imageUrl || '/images/Sub_Edunera_Logo_512.png'),
    minPrice: minPrice,
    maxPrice: maxPrice,
    originalPrice: originalPrice,
    priceRange: priceRange,
    hasDiscount: p.hasDiscount || false,
    discountPercent: discountPercent,
    variantCount: p.variants?.length || 0,
    totalStock: p.totalStock != null ? p.totalStock : (p.variants?.reduce((sum: number, v: any) => sum + (v.inStock || 0), 0) || 0),
    soldQuantity: p.soldQuantity || 0,
    rating: p.avgRating != null ? p.avgRating : 5.0,
    isAvailable: (p.totalStock || 0) > 0
  }
}

const filteredProducts = computed(() => {
  return products.value.map(mapProduct)
})

const sortedProducts = computed(() => {
  // Nếu dùng API có sort (on-sale, by-price), không cần sort lại
  // Chỉ sort client-side cho API không có sort param (by-product-name, by-category-id)
  if (searchQuery.value || (selectedCategory.value && !isPriceFilterActive.value && !onlyShowSale.value)) {
    const prods = [...filteredProducts.value]
    
    switch (sortBy.value) {
      case 'price-asc':
        return prods.sort((a, b) => a.minPrice - b.minPrice)
      case 'price-desc':
        return prods.sort((a, b) => b.minPrice - a.minPrice)
      case 'name_asc':
        return prods.sort((a, b) => a.name.localeCompare(b.name))
      case 'name_desc':
        return prods.sort((a, b) => b.name.localeCompare(a.name))
      default:
        return prods
    }
  }
  
  // Đã sort bởi backend
  return filteredProducts.value
})

const totalPages = computed(() => Math.ceil(totalElements.value / itemsPerPage))

const paginatedProducts = computed(() => {
  return sortedProducts.value
})

const displayedPages = computed(() => {
  const pages = []
  const maxDisplay = 5
  let start = Math.max(1, currentPage.value - Math.floor(maxDisplay / 2))
  let end = Math.min(totalPages.value, start + maxDisplay - 1)

  if (end - start < maxDisplay - 1) {
    start = Math.max(1, end - maxDisplay + 1)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})

const hasActiveFilters = computed(() => {
  return selectedCategory.value !== null || 
         isPriceFilterActive.value || 
         onlyShowSale.value
})

const handleSearch = () => {
  if (searchQuery.value !== route.query.q) {
    router.push({ path: '/search', query: { q: searchQuery.value } })
  }
}

const clearAllFilters = () => {
  selectedCategory.value = null
  isPriceFilterActive.value = false
  currentMin.value = 0
  currentMax.value = DEFAULT_SLIDER_MAX
  minInputText.value = '0'
  maxInputText.value = formatNumberVn(DEFAULT_SLIDER_MAX)
  onlyShowSale.value = false
  searchQuery.value = ''
  currentPage.value = 1
  router.replace('/search')
  searchProducts()
}

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const buyNow = async (productId: number) => {
  // Redirect to product detail page for better UX (let user choose variant)
  router.push(`/products/${productId}`)
}

const getCategoryName = (categoryId: number | null) => {
  if (!categoryId) return ''
  const category = categories.value.find(c => c.id === categoryId)
  return category?.name || ''
}

// Watchers
watch([selectedCategory, onlyShowSale, sortBy], () => {
  currentPage.value = 1
  searchProducts()
})

watch(() => route.query.q, (newQuery) => {
  searchQuery.value = String(newQuery || '')
  currentPage.value = 1
  recordSearchTerm(searchQuery.value)
  searchProducts()
})

watch(() => route.query.category, (newCategory) => {
  selectedCategory.value = newCategory ? Number(newCategory) : null
  currentPage.value = 1
  searchProducts()
})

watch(currentPage, () => {
  searchProducts()
})

// Load initial data
onMounted(async () => {
  loadPopularSearches()
  recordSearchTerm(searchQuery.value)
  try {
    // Fetch categories
    const data = await $fetch(`${API_BASE_URL}/categories`)
    categories.value = data as any[]
    
    // Fetch products
    await searchProducts()
  } catch (error) {
    console.error('Error loading data:', error)
  }
})
</script>

<style scoped>
.dual-range-slider {
  pointer-events: none;
}
.dual-range-slider::-webkit-slider-runnable-track {
  background: transparent;
  border: none;
  height: 100%;
}
.dual-range-slider::-moz-range-track {
  background: transparent;
  border: none;
  height: 100%;
}
.dual-range-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  pointer-events: auto;
  width: 20px;
  height: 20px;
  border-radius: 9999px;
  background-color: #ffffff;
  border: 3px solid #0099ff;
  box-shadow: 0 2px 6px rgba(0, 153, 255, 0.45);
  cursor: grab;
  margin-top: -6px;
  transition: transform 0.15s ease, border-color 0.15s ease, box-shadow 0.15s ease;
}
.dual-range-slider::-webkit-slider-thumb:hover {
  transform: scale(1.15);
  box-shadow: 0 0 0 4px rgba(0, 153, 255, 0.15);
}
.dual-range-slider::-webkit-slider-thumb:active {
  cursor: grabbing;
  transform: scale(1.25);
  border-color: #0077cc;
  box-shadow: 0 0 0 6px rgba(0, 153, 255, 0.25);
}
.dual-range-slider::-moz-range-thumb {
  pointer-events: auto;
  width: 20px;
  height: 20px;
  border-radius: 9999px;
  background-color: #ffffff;
  border: 3px solid #0099ff;
  box-shadow: 0 2px 6px rgba(0, 153, 255, 0.45);
  cursor: grab;
  transition: transform 0.15s ease, border-color 0.15s ease, box-shadow 0.15s ease;
}
.dual-range-slider::-moz-range-thumb:hover {
  transform: scale(1.15);
  box-shadow: 0 0 0 4px rgba(0, 153, 255, 0.15);
}
.dual-range-slider::-moz-range-thumb:active {
  cursor: grabbing;
  transform: scale(1.25);
  border-color: #0077cc;
  box-shadow: 0 0 0 6px rgba(0, 153, 255, 0.25);
}
</style>
