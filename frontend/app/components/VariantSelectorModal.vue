<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="show" class="fixed inset-0 z-50 overflow-y-auto">
        <!-- Backdrop -->
        <div 
          class="fixed inset-0 bg-black/50 backdrop-blur-sm transition-opacity"
          @click="closeModal"
        ></div>
        
        <!-- Modal -->
        <div class="flex min-h-full items-center justify-center p-4">
          <div 
            class="relative bg-white rounded-2xl shadow-2xl max-w-2xl w-full max-h-[90vh] overflow-hidden transform transition-all"
            @click.stop
          >
            <!-- Header -->
            <div class="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between z-10">
              <h3 class="text-xl font-bold text-gray-900">Chọn phân loại sản phẩm</h3>
              <button 
                @click="closeModal"
                class="text-gray-400 hover:text-gray-600 transition-colors p-1 rounded-lg hover:bg-gray-100"
              >
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <!-- Body -->
            <div class="px-6 py-6 overflow-y-auto max-h-[calc(90vh-200px)]">
              <!-- Product Info -->
              <div v-if="product" class="mb-6 flex gap-4 pb-6 border-b border-gray-200">
                <div class="w-20 h-20 rounded-xl overflow-hidden bg-gray-100 flex-shrink-0">
                  <img 
                    :src="product.images?.[0] || '/placeholder.png'" 
                    :alt="product.name"
                    class="w-full h-full object-contain p-2"
                  />
                </div>
                <div class="flex-1">
                  <h4 class="font-semibold text-gray-900 mb-1 line-clamp-2">{{ product.name }}</h4>
                  <p v-if="selectedVariant" class="text-lg font-bold text-[#09f]">
                    {{ formatPrice(selectedVariant.discountPrice || selectedVariant.price) }}
                  </p>
                </div>
              </div>

              <!-- Variant Selection -->
              <div class="mb-6">
                <label class="text-gray-700 font-semibold block mb-3">
                  Phân loại: 
                  <span class="text-gray-500 text-sm font-normal">({{ variants.length }} lựa chọn)</span>
                </label>
                <div class="grid grid-cols-1 gap-3">
                  <button
                    v-for="variant in variants"
                    :key="variant.id"
                    @click="selectVariant(variant)"
                    :class="[
                      'relative px-4 py-3 rounded-xl border-2 font-medium transition-all duration-200 text-left',
                      selectedVariant?.id === variant.id
                        ? 'border-[#09f] bg-blue-50 shadow-md'
                        : 'border-gray-300 hover:border-[#09f] hover:bg-gray-50',
                      (!variant.isAvailable || variant.inStock <= 0) ? 'opacity-50 cursor-not-allowed' : ''
                    ]"
                    :disabled="!variant.isAvailable || variant.inStock <= 0"
                  >
                    <div class="flex items-center gap-3">
                      <!-- Variant thumbnail -->
                      <div v-if="variant.imageUrl" class="w-12 h-12 rounded-lg overflow-hidden bg-gray-100 flex-shrink-0">
                        <img 
                          :src="variant.imageUrl.startsWith('http') ? variant.imageUrl : `${API_BASE_URL}${variant.imageUrl}`" 
                          :alt="variant.variantName"
                          class="w-full h-full object-cover"
                        />
                      </div>
                      <div class="flex-1">
                        <div class="flex items-center justify-between mb-1">
                          <span class="font-semibold text-gray-900">{{ variant.variantName }}</span>
                          <div class="flex items-center gap-2">
                            <span class="text-base font-bold text-[#09f]">
                              {{ formatPrice(variant.discountPrice || variant.price) }}
                            </span>
                            <span v-if="variant.discountPrice && variant.discountPrice < variant.price" class="text-sm text-gray-400 line-through">
                              {{ formatPrice(variant.price) }}
                            </span>
                          </div>
                        </div>
                        <div class="flex items-center justify-between">
                          <span v-if="variant.description" class="text-sm text-gray-600">{{ variant.description }}</span>
                          <span v-if="variant.inStock <= 0" class="text-sm text-red-500 font-medium">Hết hàng</span>
                          <span v-else-if="variant.inStock < 10" class="text-sm text-orange-500">Còn {{ variant.inStock }}</span>
                          <span v-else class="text-sm text-green-600">Còn hàng</span>
                        </div>
                      </div>
                      <!-- Check mark -->
                      <div v-if="selectedVariant?.id === variant.id" class="w-6 h-6 bg-[#09f] rounded-full flex items-center justify-center flex-shrink-0">
                        <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
                        </svg>
                      </div>
                    </div>
                  </button>
                </div>
              </div>

              <!-- Quantity Selector -->
              <div v-if="selectedVariant && selectedVariant.inStock > 0">
                <label class="text-gray-700 font-semibold block mb-3">Số lượng:</label>
                <div class="flex items-center gap-4">
                  <div class="flex items-center border-2 border-gray-300 rounded-xl overflow-hidden">
                    <button 
                      @click="decreaseQuantity"
                      :disabled="quantity <= 1"
                      class="w-12 h-12 flex items-center justify-center bg-gray-100 hover:bg-gray-200 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4" />
                      </svg>
                    </button>
                    <input
                      v-model.number="quantity"
                      type="number"
                      min="1"
                      :max="selectedVariant.inStock"
                      class="w-20 h-12 text-center border-0 focus:outline-none text-lg font-semibold"
                      @input="validateQuantity"
                    />
                    <button 
                      @click="increaseQuantity"
                      :disabled="quantity >= selectedVariant.inStock"
                      class="w-12 h-12 flex items-center justify-center bg-gray-100 hover:bg-gray-200 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                      </svg>
                    </button>
                  </div>
                  <span class="text-sm text-gray-600">
                    Tối đa: {{ selectedVariant.inStock }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Footer -->
            <div class="sticky bottom-0 bg-white border-t border-gray-200 px-6 py-4 flex items-center justify-between">
              <div v-if="selectedVariant">
                <p class="text-sm text-gray-600">Tổng cộng</p>
                <p class="text-2xl font-bold text-[#09f]">
                  {{ formatPrice((selectedVariant.discountPrice || selectedVariant.price) * quantity) }}
                </p>
              </div>
              <div v-else class="text-sm text-gray-500">
                Vui lòng chọn phân loại
              </div>
              <button
                @click="handleAddToCart"
                :disabled="!selectedVariant || selectedVariant.inStock <= 0 || loading"
                class="px-8 py-3 bg-[#09f] text-white font-semibold rounded-xl hover:bg-[#0088dd] transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed shadow-lg hover:shadow-xl transform hover:scale-105"
              >
                <span v-if="loading" class="flex items-center gap-2">
                  <svg class="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Đang thêm...
                </span>
                <span v-else>Thêm vào giỏ hàng</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

const API_BASE_URL = 'http://localhost:8080/api'

interface Variant {
  id: number
  variantName: string
  price: number
  discountPrice?: number
  inStock: number
  isAvailable: boolean
  imageUrl?: string
  description?: string
}

interface Product {
  id: number
  name: string
  images?: string[]
  variants: Variant[]
}

interface Props {
  show: boolean
  product: Product | null
}

interface Emits {
  (e: 'close'): void
  (e: 'confirm', variantId: number, quantity: number, price: number): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const selectedVariant = ref<Variant | null>(null)
const quantity = ref(1)
const loading = ref(false)

const variants = computed(() => props.product?.variants || [])

// Auto-select first available variant when modal opens
watch(() => props.show, (newShow) => {
  if (newShow && props.product) {
    // Reset state
    selectedVariant.value = null
    quantity.value = 1
    
    // Auto-select first available variant
    const firstAvailable = variants.value.find(v => v.isAvailable && v.inStock > 0)
    if (firstAvailable) {
      selectedVariant.value = firstAvailable
    }
  }
})

const selectVariant = (variant: Variant) => {
  if (!variant.isAvailable || variant.inStock <= 0) return
  selectedVariant.value = variant
  quantity.value = 1
}

const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

const increaseQuantity = () => {
  if (selectedVariant.value && quantity.value < selectedVariant.value.inStock) {
    quantity.value++
  }
}

const validateQuantity = () => {
  if (!selectedVariant.value) return
  
  if (quantity.value < 1) {
    quantity.value = 1
  } else if (quantity.value > selectedVariant.value.inStock) {
    quantity.value = selectedVariant.value.inStock
  }
}

const handleAddToCart = () => {
  if (!selectedVariant.value || selectedVariant.value.inStock <= 0) return
  
  loading.value = true
  const price = selectedVariant.value.discountPrice || selectedVariant.value.price
  emit('confirm', selectedVariant.value.id, quantity.value, price)
  
  // Reset loading state after a delay (parent will close modal)
  setTimeout(() => {
    loading.value = false
  }, 500)
}

const closeModal = () => {
  if (!loading.value) {
    emit('close')
  }
}

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .bg-white,
.modal-leave-active .bg-white {
  transition: transform 0.3s ease;
}

.modal-enter-from .bg-white {
  transform: scale(0.9) translateY(20px);
}

.modal-leave-to .bg-white {
  transform: scale(0.9) translateY(20px);
}

/* Hide number input arrows */
input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

input[type="number"] {
  -moz-appearance: textfield;
}

/* Line clamp utility */
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
