<template>
  <div class="min-h-screen bg-gray-50 p-8">
    <div class="max-w-7xl mx-auto">
      <!-- Header -->
      <div class="flex items-center justify-between mb-8">
        <div class="flex items-center gap-4">
          <NuxtLink to="/admin" class="p-2 hover:bg-gray-200 rounded-lg transition-colors" title="Quay lại">
            <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
          </NuxtLink>
          <div>
            <h1 class="text-3xl font-bold text-gray-900">Quản lý sản phẩm</h1>
            <p class="text-gray-600 mt-1">
              <span v-if="!selectedProduct">Tổng số: {{ products.length }} sản phẩm</span>
              <span v-else>{{ selectedProduct.name }} - {{ variants.length }} phân loại</span>
            </p>
          </div>
        </div>
        <div class="flex gap-3">
          <button
            v-if="selectedProduct"
            @click="backToProducts"
            class="flex items-center gap-2 px-6 py-3 bg-gray-200 text-gray-700 rounded-xl hover:bg-gray-300 transition-all"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            Quay lại
          </button>
          <button
            v-if="selectedProduct"
            @click="openCreateVariantModal"
            class="flex items-center gap-2 px-6 py-3 bg-[#09f] text-white rounded-xl hover:bg-[#0077cc] transition-all shadow-lg hover:shadow-xl"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            Thêm phân loại
          </button>
        </div>
      </div>

      <!-- Search & Filter -->
      <div class="bg-white rounded-xl p-4 mb-6 shadow-sm">
        <div class="relative">
          <input
            v-model="searchQuery"
            type="text"
            :placeholder="selectedProduct ? 'Tìm kiếm phân loại...' : 'Tìm kiếm sản phẩm...'"
            class="w-full px-4 py-3 pl-12 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#09f]"
          />
          <svg class="w-5 h-5 text-gray-400 absolute left-4 top-1/2 -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </div>
      </div>

      <!-- Products Table (when no product selected) -->
      <div v-if="!selectedProduct" class="bg-white rounded-xl shadow-sm overflow-hidden">
        <table class="min-w-full">
          <thead class="bg-gray-50 border-b border-gray-200">
            <tr>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">ID</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Sản phẩm</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Danh mục</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Khoảng giá</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">phân loại</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Tồn kho</th>
              <th class="px-6 py-4 text-right text-xs font-semibold text-gray-600 uppercase">Thao tác</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            <tr v-for="product in filteredProducts" :key="product.id" class="hover:bg-gray-50 transition-colors">
              <td class="px-6 py-4 text-sm text-gray-900">#{{ product.id }}</td>
              <td class="px-6 py-4">
                <div class="flex items-center gap-3">
                  <div class="w-12 h-12 bg-gray-100 rounded-lg overflow-hidden">
                    <img 
                      :src="product.defaultImageUrl ? `http://localhost:8080${product.defaultImageUrl}` : (product.categories?.[0]?.imageUrl || '/images/placeholder.png')" 
                      :alt="product.name" 
                      class="w-full h-full object-cover" 
                    />
                  </div>
                  <div>
                    <div class="text-sm font-medium text-gray-900">{{ product.name }}</div>
                    <div class="text-xs text-gray-500">{{ product.slug }}</div>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 text-sm text-gray-600">
                <span v-if="product.categories?.length" class="inline-flex flex-wrap gap-1">
                  <span 
                    v-for="cat in product.categories" 
                    :key="cat.id"
                    class="px-2 py-0.5 bg-blue-50 text-blue-700 rounded text-xs"
                  >
                    {{ cat.name }}
                  </span>
                </span>
                <span v-else class="text-gray-400">-</span>
              </td>
              <td class="px-6 py-4 text-sm">
                <div v-if="product.minPrice !== undefined">
                  <span class="font-medium text-gray-900">{{ formatPriceRange(product) }}</span>
                  <div v-if="product.hasDiscount" class="text-xs text-green-600">Có giảm giá</div>
                </div>
                <span v-else class="text-gray-400">-</span>
              </td>
              <td class="px-6 py-4">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                  {{ product.variants?.length || 0 }} phân loại
                </span>
              </td>
              <td class="px-6 py-4">
                <span :class="[
                  'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
                  product.totalStock > 0 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                ]">
                  {{ product.totalStock || 0 }}
                </span>
              </td>
              <td class="px-6 py-4 text-right">
                <div class="flex items-center justify-end gap-2">
                  <button
                    @click="selectProduct(product)"
                    class="p-2 text-purple-600 hover:bg-purple-50 rounded-lg transition-colors"
                    title="Xem phân loại"
                  >
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 10h16M4 14h16M4 18h16" />
                    </svg>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredProducts.length === 0">
              <td colspan="7" class="px-6 py-12 text-center text-gray-500">
                Không tìm thấy sản phẩm nào
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Variants Table (when product selected) -->
      <div v-else class="bg-white rounded-xl shadow-sm overflow-hidden">
        <!-- Selected Product Info -->
        <div class="bg-gradient-to-r from-blue-50 to-cyan-50 p-4 border-b border-gray-200">
          <div class="flex items-center gap-4">
            <div class="w-16 h-16 bg-white rounded-lg overflow-hidden shadow">
              <img 
                :src="selectedProduct.defaultImageUrl ? `http://localhost:8080${selectedProduct.defaultImageUrl}` : '/images/placeholder.png'" 
                :alt="selectedProduct.name" 
                class="w-full h-full object-cover" 
              />
            </div>
            <div>
              <h2 class="text-lg font-bold text-gray-900">{{ selectedProduct.name }}</h2>
              <p class="text-sm text-gray-600">{{ selectedProduct.variants?.length || 0 }} phân loại</p>
              <div class="flex gap-2 mt-1">
                <span 
                  v-for="cat in selectedProduct.categories" 
                  :key="cat.id"
                  class="px-2 py-0.5 bg-blue-100 text-blue-700 rounded text-xs"
                >
                  {{ cat.name }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <table class="min-w-full">
          <thead class="bg-gray-50 border-b border-gray-200">
            <tr>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">ID</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Tên phân loại</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Giá</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Giá KM</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Tồn kho</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Trạng thái</th>
              <th class="px-6 py-4 text-right text-xs font-semibold text-gray-600 uppercase">Thao tác</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            <tr v-for="variant in filteredVariants" :key="variant.id" class="hover:bg-gray-50 transition-colors">
              <td class="px-6 py-4 text-sm text-gray-900">#{{ variant.id }}</td>
              <td class="px-6 py-4">
                <div class="flex items-center gap-3">
                  <div class="w-12 h-12 bg-gray-100 rounded-lg overflow-hidden">
                    <img 
                      :src="variant.imageUrl ? `http://localhost:8080${variant.imageUrl}` : '/images/placeholder.png'" 
                      :alt="variant.variantName" 
                      class="w-full h-full object-cover" 
                    />
                  </div>
                  <div>
                    <div class="text-sm font-medium text-gray-900">{{ variant.variantName }}</div>
                    <div v-if="variant.description" class="text-xs text-gray-500 line-clamp-1">{{ variant.description }}</div>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 text-sm font-medium text-gray-900">
                {{ formatPrice(variant.price) }}
              </td>
              <td class="px-6 py-4 text-sm text-green-600">
                {{ variant.discountPrice ? formatPrice(variant.discountPrice) : '-' }}
              </td>
              <td class="px-6 py-4">
                <span :class="[
                  'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
                  variant.inStock > 0 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                ]">
                  {{ variant.inStock }}
                </span>
              </td>
              <td class="px-6 py-4">
                <span :class="[
                  'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
                  variant.isAvailable ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-800'
                ]">
                  {{ variant.isAvailable ? 'Đang bán' : 'Tạm dừng' }}
                </span>
              </td>
              <td class="px-6 py-4 text-right">
                <div class="flex items-center justify-end gap-2">
                  <button
                    @click="openEditVariantModal(variant)"
                    class="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                    title="Sửa"
                  >
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                    </svg>
                  </button>
                  <button
                    @click="confirmDeleteVariant(variant)"
                    class="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                    title="Xóa"
                  >
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredVariants.length === 0">
              <td colspan="7" class="px-6 py-12 text-center text-gray-500">
                Chưa có phân loại nào. Nhấn "Thêm phân loại" để tạo mới.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Create/Edit Variant Modal -->
      <div
        v-if="showModal"
        class="fixed inset-0 bg-black/30 backdrop-blur-sm flex items-center justify-center z-50 p-4"
        @click.self="closeModal"
      >
        <div class="bg-white rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
          <div class="p-6 border-b border-gray-200">
            <h2 class="text-2xl font-bold text-gray-900">
              {{ isEditMode ? 'Sửa phân loại' : 'Thêm phân loại mới' }}
            </h2>
            <p class="text-sm text-gray-500 mt-1">Sản phẩm: {{ selectedProduct?.name }}</p>
          </div>
          <div class="p-6 space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Tên phân loại *</label>
              <input
                v-model="formData.variantName"
                type="text"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#09f]"
                placeholder="VD: 10uF/25V, Màu đỏ, Loại A..."
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Mô tả</label>
              <textarea
                v-model="formData.description"
                rows="3"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#09f]"
                placeholder="Mô tả chi tiết về phân loại..."
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Thông số kỹ thuật</label>
              <textarea
                v-model="formData.specifications"
                rows="3"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#09f]"
                placeholder="VD: Vi điều khiển: ATmega328P | Điện áp: 5V | Digital I/O: 14"
              />
              <p class="text-xs text-gray-500 mt-1">Dùng dấu | để phân cách các thông số. VD: Tên: Giá trị | Tên: Giá trị</p>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Giá *</label>
                <input
                  v-model.number="formData.price"
                  type="number"
                  min="0"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#09f]"
                  placeholder="180000"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Giá khuyến mãi</label>
                <input
                  v-model.number="formData.discountPrice"
                  type="number"
                  min="0"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#09f]"
                  placeholder="165000"
                />
              </div>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Tồn kho *</label>
                <input
                  v-model.number="formData.inStock"
                  type="number"
                  min="0"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#09f]"
                  placeholder="50"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Trạng thái</label>
                <select
                  v-model="formData.isAvailable"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#09f]"
                >
                  <option :value="true">Đang bán</option>
                  <option :value="false">Tạm dừng</option>
                </select>
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Ảnh phân loại</label>
              <div v-if="formData.imageUrl && !selectedFile" class="mb-2">
                <img :src="`http://localhost:8080${formData.imageUrl}`" alt="Preview" class="w-32 h-32 object-cover rounded-lg border">
                <p class="text-xs text-green-600 mt-1">Ảnh hiện tại</p>
              </div>
              <div v-if="selectedFile" class="mb-2">
                <p class="text-xs text-blue-600 mb-1">Ảnh mới: {{ selectedFile.name }}</p>
              </div>
              <input
                ref="fileInput"
                type="file"
                accept="image/*"
                @change="handleFileSelect"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#09f]"
              />
            </div>
          </div>
          <div class="p-6 border-t border-gray-200 flex gap-3">
            <button
              @click="saveVariant"
              :disabled="saving"
              class="flex-1 px-4 py-2 bg-[#09f] text-white rounded-lg hover:bg-[#0077cc] transition-colors font-medium disabled:opacity-50"
            >
              {{ saving ? 'Đang lưu...' : (isEditMode ? 'Cập nhật' : 'Thêm mới') }}
            </button>
            <button
              @click="closeModal"
              class="flex-1 px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors font-medium"
            >
              Hủy
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useConfirm } from '~/composables/useConfirm'
import { useNotification } from '~/composables/useNotification'
import { useAuth } from '~/composables/useAuth'

const API_BASE_URL = 'http://localhost:8080'

// State
const products = ref<any[]>([])
const selectedProduct = ref<any>(null)
const variants = ref<any[]>([])
const searchQuery = ref('')
const showModal = ref(false)
const isEditMode = ref(false)
const currentVariant = ref<any>(null)
const saving = ref(false)

const formData = ref({
  variantName: '',
  description: '',
  specifications: '',
  price: 0,
  discountPrice: 0,
  inStock: 0,
  isAvailable: true,
  imageUrl: ''
})

const fileInput = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)

// Computed
const filteredProducts = computed(() => {
  if (!searchQuery.value) return products.value
  const query = searchQuery.value.toLowerCase()
  return products.value.filter(p =>
    p.name?.toLowerCase().includes(query) ||
    p.slug?.toLowerCase().includes(query) ||
    p.categories?.some((c: any) => c.name?.toLowerCase().includes(query))
  )
})

const filteredVariants = computed(() => {
  if (!searchQuery.value) return variants.value
  const query = searchQuery.value.toLowerCase()
  return variants.value.filter(v =>
    v.variantName?.toLowerCase().includes(query) ||
    v.description?.toLowerCase().includes(query)
  )
})

// Methods
const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price)
}

const formatPriceRange = (product: any) => {
  const min = product.minDiscountPrice || product.minPrice
  const max = product.maxDiscountPrice || product.maxPrice
  
  if (min === max || !max) {
    return formatPrice(min || 0)
  }
  return `${formatPrice(min)} - ${formatPrice(max)}`
}

const fetchProducts = async () => {
  try {
    // Fetch products with variants using the search API
    const data = await $fetch(`${API_BASE_URL}/api/products/search/filter`, {
      method: 'POST',
      body: { page: 0, size: 1000 }
    })
    products.value = (data as any).content || []
  } catch (error) {
    console.error('Error fetching products:', error)
    const { error: showError } = useNotification()
    showError('Lỗi khi tải danh sách sản phẩm')
  }
}

const selectProduct = (product: any) => {
  selectedProduct.value = product
  variants.value = product.variants || []
  searchQuery.value = ''
}

const backToProducts = () => {
  selectedProduct.value = null
  variants.value = []
  searchQuery.value = ''
  fetchProducts() // Refresh to get updated data
}

const openCreateVariantModal = () => {
  isEditMode.value = false
  currentVariant.value = null
  selectedFile.value = null
  formData.value = {
    variantName: '',
    description: '',
    specifications: '',
    price: 0,
    discountPrice: 0,
    inStock: 0,
    isAvailable: true,
    imageUrl: ''
  }
  showModal.value = true
}

const openEditVariantModal = (variant: any) => {
  isEditMode.value = true
  currentVariant.value = variant
  selectedFile.value = null
  formData.value = {
    variantName: variant.variantName || '',
    description: variant.description || '',
    specifications: variant.specifications || '',
    price: variant.price || 0,
    discountPrice: variant.discountPrice || 0,
    inStock: variant.inStock || 0,
    isAvailable: variant.isAvailable ?? true,
    imageUrl: variant.imageUrl || ''
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const saveVariant = async () => {
  try {
    if (!formData.value.variantName || formData.value.price <= 0) {
      const { error: showError } = useNotification()
      showError('Vui lòng điền đầy đủ thông tin bắt buộc')
      return
    }

    saving.value = true

    // Upload image first if selected
    if (selectedFile.value) {
      try {
        const imageFormData = new FormData()
        imageFormData.append('file', selectedFile.value)

        const response = await fetch(`${API_BASE_URL}/api/upload/image`, {
          method: 'POST',
          body: imageFormData
        })

        if (!response.ok) {
          const errorData = await response.json()
          throw new Error(errorData.error || 'Upload failed')
        }

        const uploadResponse = await response.json()
        formData.value.imageUrl = uploadResponse.url
      } catch (uploadError: any) {
        console.error('Error uploading image:', uploadError)
        const { error: showError } = useNotification()
        showError('Lỗi khi tải ảnh lên: ' + (uploadError.message || 'Unknown error'))
        saving.value = false
        return
      }
    }

    const variantData = {
      ...formData.value,
      productId: selectedProduct.value.id
    }

    const auth = useAuth()
    const { success: showSuccess, error: showError } = useNotification()

    if (isEditMode.value && currentVariant.value) {
      // Update
      await $fetch(`${API_BASE_URL}/product-variants/${currentVariant.value.id}`, {
        method: 'PUT',
        body: variantData,
        headers: auth.getAuthHeader()
      })
      showSuccess('Cập nhật phân loại thành công!')
    } else {
      // Create - need to call API that creates variant for a product
      await $fetch(`${API_BASE_URL}/product-variants`, {
        method: 'POST',
        body: variantData,
        headers: auth.getAuthHeader()
      })
      showSuccess('Thêm phân loại thành công!')
    }

    closeModal()
    // Refresh product data
    await fetchProducts()
    // Re-select the product to get updated variants
    const updatedProduct = products.value.find(p => p.id === selectedProduct.value.id)
    if (updatedProduct) {
      selectProduct(updatedProduct)
    }
  } catch (error: any) {
    console.error('Error saving variant:', error)
    const { error: showError } = useNotification()
    showError('Lỗi: ' + (error.message || 'Không thể lưu phân loại'))
  } finally {
    saving.value = false
  }
}

const confirmDeleteVariant = async (variant: any) => {
  const { confirm } = useConfirm()
  const auth = useAuth()
  const { success: showSuccess, error: showError } = useNotification()

  const ok = await confirm(`Bạn có chắc muốn xóa phân loại "${variant.variantName}"?`, 'Xác nhận xóa')
  if (!ok) return

  try {
    await $fetch(`${API_BASE_URL}/product-variants/${variant.id}`, {
      method: 'DELETE',
      headers: auth.getAuthHeader()
    })
    showSuccess('Xóa phân loại thành công!')
    
    // Refresh
    await fetchProducts()
    const updatedProduct = products.value.find(p => p.id === selectedProduct.value.id)
    if (updatedProduct) {
      selectProduct(updatedProduct)
    }
  } catch (error) {
    console.error('Error deleting variant:', error)
    showError('Lỗi khi xóa phân loại')
  }
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    selectedFile.value = target.files[0]
  }
}

// Lifecycle
onMounted(() => {
  fetchProducts()
})
</script>
