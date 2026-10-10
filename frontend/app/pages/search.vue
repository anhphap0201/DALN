<template>
  <div class="bg-gray-50 min-h-screen">
    <!-- Breadcrumb -->
    <section class="bg-white border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-3">
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
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
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

            <!-- Price Filter -->
            <div>
              <h3 class="font-semibold text-gray-900 mb-3 flex items-center justify-between">
                Khoảng giá
                <button
                  v-if="selectedPriceRange"
                  @click="selectedPriceRange = null"
                  class="text-xs text-[#09f] hover:text-[#0077cc] transition-colors duration-200"
                >
                  Xóa
                </button>
              </h3>
              <div class="space-y-2">
                <label
                  v-for="range in priceRanges"
                  :key="range.id"
                  class="flex items-center gap-2 cursor-pointer group"
                >
                  <input
                    type="radio"
                    :value="range.id"
                    v-model="selectedPriceRange"
                    class="w-4 h-4 text-[#09f] border-gray-300 focus:ring-[#09f] focus:ring-2 cursor-pointer"
                  />
                  <span class="text-sm text-gray-700 group-hover:text-[#09f] transition-colors duration-200">
                    {{ range.label }}
                  </span>
                </label>
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
                <!-- Sale Badge -->
                <div v-if="product.hasDiscount" class="z-10 absolute bottom-3 left-3 bg-red-500 text-white px-3 py-1 rounded-lg text-xs font-bold">
                  Giảm giá
                </div>
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
                
                <!-- Price Range -->
                <div class="flex flex-col mb-3">
                  <span class=" font-bold text-[#09f]">
                    {{ product.priceRange }}
                  </span>
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
const selectedPriceRange = ref<string | null>(null)
const onlyShowSale = ref(false)
const sortBy = ref('newest')
const currentPage = ref(1)
const itemsPerPage = 12

// State
const categories = ref<any[]>([])
const products = ref<any[]>([])
const loading = ref(false)
const totalElements = ref(0)

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
      selectedPriceRange: selectedPriceRange.value,
      onlyShowSale: onlyShowSale.value,
      sortBy: sortBy.value
    })

    // Map sortBy value to backend format
    const backendSortBy = sortBy.value === 'price-asc' ? 'name_asc' : 
                          sortBy.value === 'price-desc' ? 'name_desc' : 
                          sortBy.value

    // 1. Nếu chỉ lọc sản phẩm giảm giá (có thể kết hợp sort)
    if (onlyShowSale.value && !selectedCategory.value && !selectedPriceRange.value && !searchQuery.value) {
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
    else if (selectedPriceRange.value && !selectedCategory.value && !onlyShowSale.value && !searchQuery.value) {
      const range = priceRanges.find(r => r.id === selectedPriceRange.value)
      if (range && range.max !== Infinity) {
        // GET /api/products/search/filter/by-price
        apiUrl = `${API_BASE_URL}/products/search/filter/by-price`
        result = await $fetch(apiUrl, {
          params: {
            minPrice: range.min,
            maxPrice: range.max,
            page: currentPage.value - 1,
            size: itemsPerPage,
            sortBy: backendSortBy
          }
        })
      }
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
    else if (selectedCategory.value && !selectedPriceRange.value && !onlyShowSale.value && !searchQuery.value) {
      // GET /api/products/search/by-category-id/{categoryId}
      apiUrl = `${API_BASE_URL}/products/search/by-category-id/${selectedCategory.value}`
      result = await $fetch(apiUrl)
      console.log('📂 Category ID:', selectedCategory.value)
    }
    // 5. Nếu có nhiều điều kiện lọc kết hợp (KHÔNG có search), dùng POST filter
    else {
      const range = selectedPriceRange.value 
        ? priceRanges.find(r => r.id === selectedPriceRange.value)
        : null

      const filterRequest: any = {
        page: currentPage.value - 1,
        size: itemsPerPage
      }

      // Thêm điều kiện lọc giá
      if (range) {
        filterRequest.minPrice = range.min
        if (range.max !== Infinity) {
          filterRequest.maxPrice = range.max
        }
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
        
        // Filter by price range (sử dụng minPrice của product)
        if (selectedPriceRange.value && searchQuery.value) {
          const range = priceRanges.find(r => r.id === selectedPriceRange.value)
          if (range) {
            finalProducts = finalProducts.filter((p: any) => {
              const minPrice = p.minDiscountPrice || p.minPrice || 0
              return minPrice >= range.min && minPrice <= range.max
            })
          }
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
  } catch (error: any) {
    console.error('Error searching products:', error)
    products.value = []
    totalElements.value = 0
  } finally {
    loading.value = false
  }
}

// Map product data với price range (không còn flatten variants)
const mapProduct = (p: any) => {
  // Tính khoảng giá từ minPrice/maxPrice
  const minPrice = p.minDiscountPrice || p.minPrice || 0
  const maxPrice = p.maxDiscountPrice || p.maxPrice || 0
  
  // Format price range
  let priceRange = ''
  if (minPrice === maxPrice || maxPrice === 0) {
    priceRange = formatPrice(minPrice)
  } else {
    priceRange = `${formatPrice(minPrice)} - ${formatPrice(maxPrice)}`
  }
  
  return {
    id: p.id,
    productId: p.id,
    name: p.name,
    category: p.categories?.[0]?.name || 'N/A',
    img: p.defaultImageUrl ? `http://localhost:8080${p.defaultImageUrl}` : (p.categories?.[0]?.imageUrl || '/images/Sub_Edunera_Logo_512.png'),
    minPrice: minPrice,
    maxPrice: maxPrice,
    priceRange: priceRange,
    hasDiscount: p.hasDiscount || false,
    variantCount: p.variants?.length || 0,
    totalStock: p.totalStock || 0,
    isAvailable: (p.totalStock || 0) > 0
  }
}

const filteredProducts = computed(() => {
  return products.value.map(mapProduct)
})

const sortedProducts = computed(() => {
  // Nếu dùng API có sort (on-sale, by-price), không cần sort lại
  // Chỉ sort client-side cho API không có sort param (by-product-name, by-category-id)
  if (searchQuery.value || (selectedCategory.value && !selectedPriceRange.value && !onlyShowSale.value)) {
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
         selectedPriceRange.value !== null || 
         onlyShowSale.value
})

const handleSearch = () => {
  if (searchQuery.value !== route.query.q) {
    router.push({ path: '/search', query: { q: searchQuery.value } })
  }
}

const clearAllFilters = () => {
  selectedCategory.value = null
  selectedPriceRange.value = null
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
watch([selectedCategory, selectedPriceRange, onlyShowSale, sortBy], () => {
  currentPage.value = 1
  searchProducts()
})

watch(() => route.query.q, (newQuery) => {
  searchQuery.value = String(newQuery || '')
  currentPage.value = 1
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
