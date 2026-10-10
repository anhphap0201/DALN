<template>
  <div class="bg-gray-50 min-h-screen py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Breadcrumb -->
      <nav class="flex items-center gap-2 text-sm text-gray-600 mb-6">
        <NuxtLink to="/" class="hover:text-[#09f] transition-colors">Trang chủ</NuxtLink>
        <span>›</span>
        <NuxtLink to="/search" class="hover:text-[#09f] transition-colors">Sản phẩm</NuxtLink>
        <span>›</span>
        <span class="text-gray-900 font-medium">{{ product?.name }}</span>
      </nav>

      <!-- Product Details -->
      <div v-if="product" class="bg-white rounded-2xl shadow-lg overflow-hidden">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 p-8">
          <!-- Product Images -->
          <div class="space-y-4">
            <!-- Main Image -->
            <div class="aspect-video bg-gradient-to-br from-blue-50 to-cyan-50 rounded-2xl overflow-hidden flex items-center justify-center p-6">
              <img 
                :src="selectedImage" 
                :alt="product.name"
                class="w-full h-full object-contain hover:scale-110 transition-transform duration-300"
              />
            </div>
            
            <!-- Thumbnail Images -->
            <div class="grid grid-cols-4 gap-4">
              <div
                v-for="(image, idx) in product.images"
                :key="idx"
                @click="selectedImage = image"
                :class="[
                  'aspect-square bg-gray-100 rounded-lg overflow-hidden cursor-pointer border-2 transition-all duration-200',
                  selectedImage === image ? 'border-[#09f] scale-105' : 'border-transparent hover:border-gray-300'
                ]"
              >
                <img 
                  :src="image" 
                  :alt="`${product.name} - ${Number(idx) + 1}`"
                  class="w-full h-full object-contain p-2"
                />
              </div>
            </div>
          </div>

          <!-- Product Info -->
          <div class="space-y-6">
            <!-- Title & Rating -->
            <div>
              <h1 class="text-3xl font-bold text-gray-900 mb-3">{{ product.name }}</h1>
              <div class="flex items-center gap-4">
                <div class="flex items-center gap-1">
                  <span v-for="i in 5" :key="i" class="text-yellow-400 text-lg">
                    {{ i <= (reviewSummary?.averageRating || 0) ? '★' : '☆' }}
                  </span>
                  <span class="text-sm text-gray-600 ml-2">({{ reviewSummary?.totalReviews || 0 }} đánh giá)</span>
                </div>
                <span class="text-sm text-gray-600">|</span>
                <span class="text-sm text-gray-600">Đã bán: {{ product.sold }}</span>
              </div>
            </div>

            <!-- Price -->
            <div class="bg-gray-50 rounded-xl p-6">
              <div class="flex items-baseline gap-4 mb-2">
                <span class="text-4xl font-bold text-[#09f]">{{ formatPrice(currentPrice) }}</span>
                <span v-if="currentOriginalPrice > currentPrice" class="text-xl text-gray-400 line-through">
                  {{ formatPrice(currentOriginalPrice) }}
                </span>
                <span v-if="currentDiscount" class="bg-red-500 text-white px-3 py-1 rounded-lg text-sm font-bold">
                  -{{ currentDiscount }}%
                </span>
              </div>
              <p class="text-sm text-gray-600">Giá đã bao gồm VAT</p>
            </div>

            <!-- Stock Status -->
            <div class="flex items-center gap-2">
              <span class="text-gray-700 font-medium">Tình trạng:</span>
              <span :class="currentStock > 0 ? 'text-green-600' : 'text-red-600'" class="font-semibold">
                {{ currentStock > 0 ? `Còn hàng (${currentStock} sản phẩm)` : 'Hết hàng' }}
              </span>
            </div>

            <!-- Variant Selector -->
            <div v-if="product.variants && product.variants.length > 0" class="space-y-3">
              <label class="text-gray-700 font-medium block">
                Chọn phân loại: 
                <span v-if="product.variants.length > 1" class="text-gray-500 text-sm">({{ product.variants.length }} lựa chọn)</span>
              </label>
              <div class="flex flex-wrap gap-3">
                <button
                  v-for="variant in product.variants"
                  :key="variant.id"
                  @click="selectVariant(variant)"
                  :class="[
                    'relative px-4 py-3 rounded-xl border-2 font-medium transition-all duration-200 text-left',
                    selectedVariant?.id === variant.id
                      ? 'border-[#09f] bg-blue-50 text-[#09f] shadow-md'
                      : 'border-gray-300 hover:border-[#09f] text-gray-700 hover:bg-gray-50',
                    (!variant.isAvailable || variant.inStock <= 0) ? 'opacity-50 cursor-not-allowed' : ''
                  ]"
                  :disabled="!variant.isAvailable || variant.inStock <= 0"
                  :title="variant.description || variant.variantName"
                >
                  <div class="flex items-center gap-3">
                    <!-- Variant thumbnail -->
                    <div v-if="variant.imageUrl" class="w-10 h-10 rounded-lg overflow-hidden bg-gray-100 flex-shrink-0">
                      <img 
                        :src="variant.imageUrl.startsWith('http') ? variant.imageUrl : `${API_BASE_URL}${variant.imageUrl}`" 
                        :alt="variant.variantName"
                        class="w-full h-full object-cover"
                      />
                    </div>
                    <div>
                      <span class="block text-sm font-medium">{{ variant.variantName }}</span>
                      <div class="flex items-center gap-2 mt-0.5">
                        <span class="text-xs font-semibold text-[#09f]">
                          {{ formatPrice(variant.discountPrice || variant.price) }}
                        </span>
                        <span v-if="variant.discountPrice && variant.discountPrice < variant.price" class="text-xs text-gray-400 line-through">
                          {{ formatPrice(variant.price) }}
                        </span>
                      </div>
                      <span v-if="variant.inStock <= 0" class="text-xs text-red-500">Hết hàng</span>
                      <span v-else-if="variant.inStock < 10" class="text-xs text-orange-500">Còn {{ variant.inStock }}</span>
                    </div>
                  </div>
                  <!-- Check mark for selected -->
                  <div v-if="selectedVariant?.id === variant.id" class="absolute top-1 right-1 w-5 h-5 bg-[#09f] rounded-full flex items-center justify-center">
                    <svg class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
                    </svg>
                  </div>
                </button>
              </div>
            </div>

            <!-- Quantity Selector -->
            <div v-if="currentStock > 0">
              <label class="text-gray-700 font-small block mb-2">Số lượng:</label>
              <div class="flex items-center gap-2">
                <div class="flex items-center border-2 border-gray-300 rounded-xl overflow-hidden">
                  <button 
                    @click="decreaseQuantity"
                    class="w-8 h-8 flex items-center justify-center bg-gray-100 hover:bg-gray-200 transition-colors"
                    :disabled="quantity <= 1"
                  >
                    <span class="text-xl">−</span>
                  </button>
                  <input 
                    v-model.number="quantity" 
                    type="number" 
                    min="1" 
                    :max="currentStock"
                    class="w-8 h-8 text-center font-semibold focus:outline-none [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none" 
                  />
                  <button   
                    @click="increaseQuantity"
                    class="w-8 h-8 flex items-center justify-center bg-gray-100 hover:bg-gray-200 transition-colors"
                    :disabled="quantity >= currentStock"
                  >
                    <span class="text-xl">+</span>
                  </button>
                </div>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex gap-4">
              <button 
                v-if="currentStock > 0"
                @click="addToCart"
                :disabled="cartLoading"
                class="flex-1  text-black py-4 rounded-xl font-semibold text-lg transition-all duration-300 hover:bg-gray-200 hover:shadow-xl hover:scale-105 active:scale-95 bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <span v-if="cartLoading">Đang thêm...</span>
                <span v-else>Thêm vào giỏ hàng</span>
              </button>
              <button 
                v-if="currentStock > 0"
                @click="buyNow"
                :disabled="cartLoading"
                class="flex-1 bg-gradient-to-r from-[#09f] to-[#0077cc] text-white py-4 rounded-xl font-semibold text-lg transition-all duration-300 hover:shadow-xl hover:scale-105 active:scale-95 flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <span v-if="cartLoading">Đang xử lý...</span>
                <span v-else>Mua ngay</span>
              </button>
              <button 
                v-if="currentStock > 0"
                @click="toggleWishlist"
                :class="[
                  'w-14 h-14 rounded-xl transition-all duration-300 hover:scale-105 active:scale-95 border-2 flex items-center justify-center',
                  isInWishlist ? 'bg-red-50 border-red-500' : 'bg-gray-100 border-gray-300 hover:border-red-500'
                ]"
              >
                <svg 
                  :class="[
                    'w-6 h-6 transition-colors duration-300',
                    isInWishlist ? 'fill-red-500' : 'fill-none stroke-gray-400 hover:stroke-red-500'
                  ]"
                  viewBox="0 0 24 24" 
                  stroke-width="2" 
                  stroke-linecap="round" 
                  stroke-linejoin="round"
                >
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
                </svg>
              </button>
              <button 
                v-else
                disabled
                class="flex-1 bg-gray-300 text-gray-600 py-4 rounded-xl font-semibold text-lg cursor-not-allowed"
              >
                Hết hàng
              </button>
            </div>
          </div>
        </div>

        <!-- Product Details Tabs -->
        <div class="border-t border-gray-200">
          <div class="flex border-b border-gray-200">
            <button
              v-for="tab in tabs"
              :key="tab.id"
              @click="activeTab = tab.id"
              :class="[
                'px-8 py-4 font-semibold transition-all duration-200 border-b-2',
                activeTab === tab.id 
                  ? 'border-[#09f] text-[#09f]' 
                  : 'border-transparent text-gray-600 hover:text-[#09f]'
              ]"
            >
              {{ tab.label }}
            </button>
          </div>

          <div class="p-8">
            <!-- Description Tab -->
            <div v-if="activeTab === 'description'" class="prose max-w-none">
              <h3 class="text-xl font-bold text-gray-900 mb-4">Mô tả sản phẩm</h3>
              <!-- Multiple variants: show selected or all descriptions -->
              <div>
                
                <!-- All Variant Descriptions -->
                <div class="space-y-4">
                  <div
                    v-for="variant in product.variants"
                    :key="variant.id"
                    v-show="!selectedVariant || selectedVariant.id === variant.id"
                    class="bg-gray-50 rounded-xl p-4"
                  >
                    <h4 class="font-semibold text-gray-900 mb-2">{{ variant.variantName }}</h4>
                    <p class="text-gray-600">{{ variant.description || 'Chưa có mô tả.' }}</p>
                  </div>
                </div>
                
                <!-- Show all variants descriptions when none selected -->
                <div v-if="!selectedVariant" class="space-y-4">
                  <div
                    v-for="variant in product.variants"
                    :key="'desc-' + variant.id"
                    class="bg-gray-50 rounded-xl p-4"
                  >
                    <h4 class="font-semibold text-gray-900 mb-2">{{ variant.variantName }}</h4>
                    <p class="text-gray-600">{{ variant.description || 'Chưa có mô tả.' }}</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Specifications Tab -->
            <div v-if="activeTab === 'specifications'" class="space-y-6">
              <h3 class="text-xl font-bold text-gray-900 mb-4">Thông số kỹ thuật</h3>
              
              <!-- Single variant or selected variant specs -->
              <div v-if="currentSpecs.length > 0">
                <h4 v-if="selectedVariant && product.variants?.length > 1" class="text-lg font-semibold text-gray-800 mb-3">
                  {{ selectedVariant.variantName }}
                </h4>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div
                    v-for="(spec, index) in currentSpecs"
                    :key="index"
                    class="bg-gray-50 rounded-lg p-4 flex justify-between"
                  >
                    <span class="font-medium text-gray-700">{{ spec.label }}:</span>
                    <span class="text-gray-900">{{ spec.value }}</span>
                  </div>
                </div>
              </div>
              
              <!-- All variants specs when none selected -->
              <div v-else-if="!selectedVariant && product.variants?.length > 1" class="space-y-6">
                <div v-for="variant in product.variants" :key="'spec-' + variant.id">
                  <h4 class="text-lg font-semibold text-gray-800 mb-3">{{ variant.variantName }}</h4>
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div
                      v-for="(spec, index) in parseSpecifications(variant.specifications)"
                      :key="index"
                      class="bg-gray-50 rounded-lg p-4 flex justify-between"
                    >
                      <span class="font-medium text-gray-700">{{ spec.label }}:</span>
                      <span class="text-gray-900">{{ spec.value }}</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <div v-else class="text-gray-500 text-center py-8">
                Chưa có thông số kỹ thuật cho sản phẩm này.
              </div>
            </div>

            <!-- Reviews Tab -->
            <div v-if="activeTab === 'reviews'" class="space-y-6">
              <div class="flex items-center justify-between mb-6">
                <h3 class="text-xl font-bold text-gray-900">Đánh giá sản phẩm</h3>
                <button
                  @click="openReviewForm"
                  class="bg-[#09f] text-white px-6 py-2 rounded-lg font-medium hover:bg-[#0077cc] transition-colors"
                >
                  Viết đánh giá
                </button>
              </div>

              <!-- Review Form -->
              <div v-if="showReviewForm" class="bg-white border-2 border-[#09f] rounded-xl p-6 space-y-4">
                <h4 class="text-lg font-bold text-gray-900">{{ editingReview ? 'Chỉnh sửa đánh giá' : 'Viết đánh giá' }}</h4>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">Đánh giá của bạn</label>
                  <div class="flex items-center gap-1">
                    <button
                      v-for="i in 5"
                      :key="i"
                      @click="reviewForm.rating = i"
                      class="text-3xl transition-colors cursor-pointer"
                      :class="i <= reviewForm.rating ? 'text-yellow-400' : 'text-gray-300 hover:text-yellow-300'"
                    >
                      ★
                    </button>
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">Nhận xét</label>
                  <textarea
                    v-model="reviewForm.comment"
                    rows="4"
                    class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-xl text-sm transition-all duration-300 outline-none focus:border-[#09f] focus:shadow-[0_0_0_3px_rgba(0,153,255,0.1)] resize-none"
                    placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm..."
                  ></textarea>
                </div>
                <div class="flex gap-3">
                  <button
                    @click="submitReview"
                    :disabled="reviewSubmitting || reviewForm.rating === 0"
                    class="px-6 py-2 bg-[#09f] text-white rounded-lg font-medium hover:bg-[#0077cc] transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {{ reviewSubmitting ? 'Đang gửi...' : (editingReview ? 'Cập nhật' : 'Gửi đánh giá') }}
                  </button>
                  <button
                    @click="cancelReviewForm"
                    class="px-6 py-2 bg-gray-200 text-gray-700 rounded-lg font-medium hover:bg-gray-300 transition-colors"
                  >
                    Hủy
                  </button>
                </div>
              </div>

              <!-- Rating Summary -->
              <div class="bg-gradient-to-br from-blue-50 to-cyan-50 rounded-xl p-6 flex items-center gap-8">
                <div class="text-center">
                  <div class="text-5xl font-bold text-[#09f]">{{ reviewSummary?.averageRating || 0 }}<span class="text-3xl">/5</span></div>
                  <div class="flex items-center justify-center gap-1 mt-2">
                    <span v-for="i in 5" :key="i" class="text-yellow-400 text-xl">
                      {{ i <= (reviewSummary?.averageRating || 0) ? '★' : '☆' }}
                    </span>
                  </div>
                  <div class="text-sm text-gray-600 mt-1">{{ reviewSummary?.totalReviews || 0 }} đánh giá</div>
                </div>
                <div class="flex-1 space-y-2">
                  <div v-for="star in [5, 4, 3, 2, 1]" :key="star" class="flex items-center gap-3">
                    <span class="text-sm text-gray-600 w-8">{{ star }}</span>
                    <div class="flex-1 bg-gray-200 rounded-full h-2">
                      <div 
                        :style="{ width: `${ratingPercent(star)}%` }"
                        class="bg-yellow-400 h-2 rounded-full transition-all duration-500"
                      ></div>
                    </div>
                    <span class="text-sm text-gray-600 w-12">{{ ratingPercent(star) }}%</span>
                  </div>
                </div>
              </div>

              <!-- Individual Reviews -->
              <div v-if="reviewsLoading" class="text-center py-8">
                <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-[#09f] border-t-transparent mb-2"></div>
                <p class="text-gray-600">Đang tải đánh giá...</p>
              </div>

              <div v-else-if="reviews.length === 0" class="text-center py-8">
                <p class="text-gray-500">Chưa có đánh giá nào cho sản phẩm này.</p>
              </div>

              <div v-else class="space-y-4">
                <div
                  v-for="review in reviews"
                  :key="review.id"
                  class="border border-gray-200 rounded-xl p-6 hover:shadow-md transition-shadow"
                >
                  <div class="flex items-start justify-between mb-3">
                    <div class="flex items-center gap-3">
                      <div class="w-12 h-12 bg-gradient-to-br from-blue-500 to-cyan-500 rounded-full flex items-center justify-center text-white font-bold">
                        {{ (review.userName || 'U').charAt(0).toUpperCase() }}
                      </div>
                      <div>
                        <p class="font-semibold text-gray-900">{{ review.userName }}</p>
                        <div class="flex items-center gap-2">
                          <div class="flex items-center">
                            <span v-for="i in 5" :key="i" class="text-yellow-400">
                              {{ i <= review.rating ? '★' : '☆' }}
                            </span>
                          </div>
                          <span class="text-xs text-gray-500">{{ formatDate(new Date(review.createdAt)) }}</span>
                        </div>
                      </div>
                    </div>
                    <!-- Edit/Delete for own reviews -->
                    <div v-if="currentUserId && review.userId == currentUserId" class="flex gap-2">
                      <button @click="editReview(review)" class="text-sm text-[#09f] hover:underline">Sửa</button>
                      <button @click="deleteReview(review.id)" class="text-sm text-red-500 hover:underline">Xóa</button>
                    </div>
                  </div>
                  <p class="text-gray-700 leading-relaxed">{{ review.comment }}</p>
                </div>

                <!-- Load More -->
                <div v-if="reviewsHasMore" class="text-center pt-4">
                  <button
                    @click="loadMoreReviews"
                    class="px-6 py-2 border-2 border-[#09f] text-[#09f] rounded-lg font-medium hover:bg-[#09f] hover:text-white transition-colors"
                  >
                    Xem thêm đánh giá
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Related Products -->
      <div v-if="relatedProducts.length > 0" class="mt-12">
        <h2 class="text-2xl font-bold text-gray-900 mb-6">Sản phẩm liên quan</h2>
        <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6">
          <NuxtLink
            v-for="relatedProduct in relatedProducts"
            :key="relatedProduct.id"
            :to="`/products/${relatedProduct.id}`"
            class="bg-white border-2 border-gray-100 rounded-2xl overflow-hidden transition-all duration-300 hover:shadow-xl hover:scale-105 hover:-translate-y-1 hover:border-[#09f] group"
          >
            <div class="aspect-square bg-gradient-to-br from-blue-50 to-cyan-50 flex items-center justify-center p-4">
              <img 
                :src="relatedProduct.img" 
                :alt="relatedProduct.name"
                class="w-full h-full object-contain group-hover:scale-110 transition-transform duration-300"
              />
            </div>
            <div class="p-4">
              <h3 class="text-sm font-semibold text-gray-800 mb-2 line-clamp-2 group-hover:text-[#09f] transition-colors">
                {{ relatedProduct.name }}
              </h3>
              <div class="flex items-center gap-2">
                <span class="text-lg font-bold text-[#09f]">{{ formatPrice(relatedProduct.salePrice) }}</span>
                <span v-if="relatedProduct.originalPrice > relatedProduct.salePrice" class="text-xs text-gray-400 line-through">
                  {{ formatPrice(relatedProduct.originalPrice) }}
                </span>
              </div>
            </div>
          </NuxtLink>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="!product" class="flex items-center justify-center py-20">
        <div class="text-center">
          <div class="inline-block animate-spin rounded-full h-12 w-12 border-4 border-[#09f] border-t-transparent mb-4"></div>
          <p class="text-gray-600">Đang tải thông tin sản phẩm...</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useCart } from '~/composables/useCart'
import { useConfirm } from '~/composables/useConfirm'

const route = useRoute()
const productId = computed(() => route.params.id as string)

const API_BASE_URL = 'http://localhost:8080'

// Composables
const { addToCart: addToCartComposable, loading: cartLoading } = useCart()
const { getAuthHeader, isAuthenticated } = useAuth()
const { success: showSuccess, error: showError } = useNotification()

// Product data
const product = ref<any>(null)
const selectedVariant = ref<any>(null)
const selectedImage = ref('')
const quantity = ref(1)
const activeTab = ref('description')
const isInWishlist = ref(false)

// Reviews data
const reviews = ref<any[]>([])
const reviewSummary = ref<any>(null)
const reviewsLoading = ref(false)
const reviewsPage = ref(0)
const reviewsHasMore = ref(false)
const showReviewForm = ref(false)
const reviewSubmitting = ref(false)
const editingReview = ref<any>(null)
const reviewForm = ref({ rating: 0, comment: '' })
const currentUserId = ref<number | null>(null)

const tabs = [
  { id: 'description', label: 'Mô tả' },
  { id: 'specifications', label: 'Thông số kỹ thuật' },
  { id: 'reviews', label: 'Đánh giá' }
]

// Computed properties for variant-based data
const currentPrice = computed(() => {
  if (selectedVariant.value) {
    return selectedVariant.value.discountPrice || selectedVariant.value.price
  }
  // Fallback to first variant or product minPrice
  const firstVariant = product.value?.variants?.[0]
  return firstVariant?.discountPrice || firstVariant?.price || product.value?.minPrice || 0
})

const currentOriginalPrice = computed(() => {
  if (selectedVariant.value) {
    return selectedVariant.value.price
  }
  const firstVariant = product.value?.variants?.[0]
  return firstVariant?.price || product.value?.maxPrice || 0
})

const currentDiscount = computed(() => {
  const original = currentOriginalPrice.value
  const sale = currentPrice.value
  if (original > sale && sale > 0) {
    return Math.round(((original - sale) / original) * 100)
  }
  return 0
})

const currentStock = computed(() => {
  if (selectedVariant.value) {
    return selectedVariant.value.inStock || 0
  }
  return product.value?.totalStock || product.value?.variants?.reduce((sum: number, v: any) => sum + (v.inStock || 0), 0) || 0
})

// Parse specifications string "Key1: Value1 | Key2: Value2" into array of {label, value}
const parseSpecifications = (specs: string | null | undefined) => {
  if (!specs) return []
  return specs.split('|').map((item: string) => {
    const parts = item.split(':')
    return {
      label: (parts[0] || '').trim(),
      value: (parts.slice(1).join(':') || '').trim()
    }
  }).filter((s: any) => s.label && s.value)
}

// Current specifications based on selected variant or first variant
const currentSpecs = computed(() => {
  const variant = selectedVariant.value || (product.value?.variants?.length === 1 ? product.value.variants[0] : null)
  if (variant?.specifications) {
    return parseSpecifications(variant.specifications)
  }
  return []
})

const selectVariant = (variant: any) => {
  selectedVariant.value = variant
  // Update image if variant has image
  if (variant.imageUrl) {
    selectedImage.value = `${API_BASE_URL}${variant.imageUrl}`
  }
  // Reset quantity
  quantity.value = 1
}

// Mock data - replace with API call
const mockProducts = [
  {
    id: '1',
    name: 'Arduino Uno R3 - Bo mạch phát triển',
    description: `Arduino Uno R3 là bo mạch vi điều khiển phổ biến nhất trong dòng sản phẩm Arduino. 
    
Với chip xử lý ATmega328P, bo mạch có 14 chân digital I/O (trong đó 6 chân có thể xuất PWM), 6 chân analog input, thạch anh 16MHz, cổng USB, jack nguồn, header ICSP và nút reset.

Bo mạch hoàn hảo cho người mới bắt đầu và các dự án IoT, automation, robot. Hỗ trợ lập trình với Arduino IDE thông qua cổng USB, rất dễ sử dụng với thư viện phong phú.

Đây là phiên bản chính hãng từ Arduino Italy với chất lượng cao, PCB màu xanh đặc trưng và logo Arduino chính thức.`,
    images: [
      '/images/ArduinoUnoR3.jpg',
      '/images/Sub_Edunera_Logo_512.png',
      '/images/Sub_Edunera_Logo_512.png',
      '/images/Sub_Edunera_Logo_512.png'
    ],
    originalPrice: 200000,
    salePrice: 140000,
    discount: 30,
    stock: 50,
    category: 'Arduino',
    rating: 4.5,
    reviewCount: 128,
    sold: 1542,
    warranty: 12,
    specifications: [
      { label: 'Vi điều khiển', value: 'ATmega328P' },
      { label: 'Điện áp hoạt động', value: '5V' },
      { label: 'Điện áp đầu vào', value: '7-12V' },
      { label: 'Chân Digital I/O', value: '14 (6 PWM)' },
      { label: 'Chân Analog Input', value: '6' },
      { label: 'Dòng DC mỗi chân I/O', value: '20 mA' },
      { label: 'Flash Memory', value: '32 KB' },
      { label: 'SRAM', value: '2 KB' },
      { label: 'EEPROM', value: '1 KB' },
      { label: 'Tốc độ xung nhịp', value: '16 MHz' }
    ],
    ratingDistribution: {
      5: 70,
      4: 20,
      3: 7,
      2: 2,
      1: 1
    },
    reviews: [
      {
        id: 1,
        userName: 'Nguyễn Văn A',
        rating: 5,
        comment: 'Sản phẩm chất lượng, đóng gói cẩn thận. Hoạt động tốt, phù hợp cho người mới học Arduino.',
        date: new Date('2025-12-15'),
        verified: true
      },
      {
        id: 2,
        userName: 'Trần Thị B',
        rating: 4,
        comment: 'Board chạy ổn định, giá cả hợp lý. Giao hàng nhanh.',
        date: new Date('2025-12-10'),
        verified: true
      },
      {
        id: 3,
        userName: 'Lê Văn C',
        rating: 5,
        comment: 'Hàng chính hãng, chất lượng tốt. Shop tư vấn nhiệt tình. Sẽ ủng hộ thêm.',
        date: new Date('2025-12-05'),
        verified: false
      }
    ]
  },
  {
    id: '2',
    name: 'ESP32 DevKit V1 - WiFi Bluetooth Module',
    description: `ESP32 DevKit V1 là bo mạch phát triển dựa trên chip ESP32-WROOM-32 của Espressif. 

Bo mạch tích hợp WiFi 802.11 b/g/n và Bluetooth 4.2 BR/EDR và BLE, lõi kép Tensilica LX6 chạy lên đến 240MHz.

ESP32 có 36 GPIO pins, hỗ trợ giao tiếp I2C, SPI, UART, CAN và nhiều giao thức khác. Thích hợp cho các dự án IoT cần kết nối không dây.

Hỗ trợ lập trình với Arduino IDE, ESP-IDF, MicroPython. Tiêu thụ điện năng thấp với các chế độ deep sleep.`,
    images: [
      '/images/nodemcu-esp32-01.webp',
      '/images/Sub_Edunera_Logo_512.png',
      '/images/Sub_Edunera_Logo_512.png',
      '/images/Sub_Edunera_Logo_512.png'
    ],
    originalPrice: 150000,
    salePrice: 105000,
    discount: 30,
    stock: 35,
    category: 'ESP32',
    rating: 4.7,
    reviewCount: 95,
    sold: 876,
    warranty: 6,
    specifications: [
      { label: 'Chip', value: 'ESP32-WROOM-32' },
      { label: 'CPU', value: 'Dual-core Tensilica LX6' },
      { label: 'Tốc độ xung nhịp', value: 'Up to 240MHz' },
      { label: 'SRAM', value: '520 KB' },
      { label: 'Flash', value: '4 MB' },
      { label: 'WiFi', value: '802.11 b/g/n' },
      { label: 'Bluetooth', value: 'v4.2 BR/EDR và BLE' },
      { label: 'GPIO', value: '36 pins' },
      { label: 'ADC', value: '18 channels, 12-bit' },
      { label: 'Điện áp hoạt động', value: '3.3V' }
    ],
    ratingDistribution: {
      5: 75,
      4: 18,
      3: 5,
      2: 1,
      1: 1
    },
    reviews: [
      {
        id: 1,
        userName: 'Phạm Minh D',
        rating: 5,
        comment: 'ESP32 rất mạnh, WiFi ổn định. Dùng cho dự án smart home rất ok.',
        date: new Date('2025-12-18'),
        verified: true
      },
      {
        id: 2,
        userName: 'Hoàng Thị E',
        rating: 5,
        comment: 'Giá tốt, hàng đẹp. Bluetooth và WiFi hoạt động tốt.',
        date: new Date('2025-12-12'),
        verified: true
      }
    ]
  }
]

const relatedProducts = ref<any[]>([])

onMounted(() => {
  // Load product data
  loadProduct()
  // Load current user id
  loadCurrentUser()
})

const fetchRelatedProducts = async (currentProduct: any) => {
  try {
    if (!currentProduct.categories || currentProduct.categories.length === 0) return
    
    const categoryId = currentProduct.categories[0].id
    const data = await $fetch<any[]>(`${API_BASE_URL}/api/products/search/by-category-id/${categoryId}`)
    
    if (data) {
      relatedProducts.value = data
        .filter((p: any) => p.id !== currentProduct.id)
        .slice(0, 10)
        .map((p: any) => {
          // Get first variant image or default image
          let img = '/images/placeholder.png'
          if (p.defaultImageUrl) {
            img = p.defaultImageUrl.startsWith('http') ? p.defaultImageUrl : `${API_BASE_URL}${p.defaultImageUrl}`
          } else if (p.variants?.[0]?.imageUrl) {
            const vImg = p.variants[0].imageUrl
            img = vImg.startsWith('http') ? vImg : `${API_BASE_URL}${vImg}`
          }
          
          return {
            id: p.id,
            name: p.name,
            img,
            salePrice: p.minDiscountPrice ?? p.minPrice ?? 0,
            originalPrice: p.minPrice ?? 0
          }
        })
    }
  } catch (error) {
    console.error('Error fetching related products:', error)
  }
}

const loadProduct = async () => {
  try {
    // Fetch product from API
    const data = await $fetch(`${API_BASE_URL}/api/products/search/by-product-id/${productId.value}`)
    
    if (data) {
      const p = data as any
      
      // Map API response to component format
      // Description và specifications từ Variants
      product.value = {
        id: p.id,
        name: p.name,
        slug: p.slug,
        categories: p.categories || [],
        variants: p.variants || [],
        images: buildProductImages(p),
        rating: p.avgRating || 0,
        sold: p.soldQuantity || 0,
        warranty: 12,
        // Price range info từ variants
        minPrice: p.minPrice,
        maxPrice: p.maxPrice,
        minDiscountPrice: p.minDiscountPrice,
        maxDiscountPrice: p.maxDiscountPrice,
        totalStock: p.totalStock,
        hasDiscount: p.hasDiscount,
        defaultImageUrl: p.defaultImageUrl
      }
      
      // Fetch reviews and summary for this product
      fetchReviewSummary(p.id)
      fetchReviews(p.id)
      
      // Fetch related products by category
      fetchRelatedProducts(p)
      
      // Set selected image
      selectedImage.value = product.value.images[0] || '/images/placeholder.png'
      
      // Auto-select first variant if only one
      if (p.variants && p.variants.length === 1) {
        selectVariant(p.variants[0])
      } else if (p.variants && p.variants.length > 0) {
        // Pre-select first available variant
        const firstAvailable = p.variants.find((v: any) => v.isAvailable && v.inStock > 0)
        if (firstAvailable) {
          selectVariant(firstAvailable)
        }
      }
    }
  } catch (error) {
    console.error('Error loading product:', error)
    // Fallback to mock data for demo
    loadMockProduct()
  }
}

const buildProductImages = (p: any) => {
  const images: string[] = []
  
  // Ưu tiên lấy ảnh từ variants trước
  if (p.variants && p.variants.length > 0) {
    p.variants.forEach((v: any) => {
      if (v.imageUrl) {
        const imgUrl = v.imageUrl.startsWith('http') ? v.imageUrl : `${API_BASE_URL}${v.imageUrl}`
        if (!images.includes(imgUrl)) {
          images.push(imgUrl)
        }
      }
    })
  }
  
  // Thêm default image nếu có và chưa có trong list
  if (p.defaultImageUrl) {
    const defaultImg = p.defaultImageUrl.startsWith('http') ? p.defaultImageUrl : `${API_BASE_URL}${p.defaultImageUrl}`
    if (!images.includes(defaultImg)) {
      images.unshift(defaultImg) // Đặt lên đầu
    }
  }
  
  // Add category image as fallback
  if (images.length === 0 && p.categories?.[0]?.imageUrl) {
    images.push(p.categories[0].imageUrl)
  }
  
  // Ensure at least one placeholder
  if (images.length === 0) {
    images.push('/images/placeholder.png')
  }
  
  return images
}

const loadMockProduct = () => {
  // Mock data fallback
  const foundProduct = mockProducts.find(p => p.id === productId.value)
  if (foundProduct) {
    product.value = foundProduct
    selectedImage.value = foundProduct.images[0] || '/images/placeholder.png'
  }
}

const { confirm } = useConfirm()
const toggleWishlist = () => {
  isInWishlist.value = !isInWishlist.value
  if (isInWishlist.value) {
    showSuccess('Đã thêm vào danh sách yêu thích')
  } else {
    showSuccess('Đã xóa khỏi danh sách yêu thích')
  }
}

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const formatDate = (date: Date) => {
  return new Intl.DateTimeFormat('vi-VN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  }).format(date)
}

const increaseQuantity = () => {
  if (quantity.value < currentStock.value) {
    quantity.value++
  }
}

const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

const addToCart = async () => {
  // Ensure variant is selected if there are multiple
  if (product.value.variants?.length > 1 && !selectedVariant.value) {
    showError('Vui lòng chọn phân loại sản phẩm')
    return
  }
  
  // Check if stock is available
  if (currentStock.value < quantity.value) {
    showError('Số lượng trong kho không đủ')
    return
  }
  
  const variant = selectedVariant.value || product.value.variants?.[0]
  const variantId = variant?.id
  
  if (!variantId) {
    showError('Sản phẩm không hợp lệ')
    return
  }
  
  const price = variant.discountPrice || variant.price
  
  // Call API to add to cart (requires login)
  const success = await addToCartComposable(variantId, quantity.value, price)
  
  if (success) {
    // Reset quantity after successful add
    quantity.value = 1
  }
}

const buyNow = async () => {
  // Ensure variant is selected if there are multiple
  if (product.value.variants?.length > 1 && !selectedVariant.value) {
    showError('Vui lòng chọn phân loại sản phẩm')
    return
  }
  
  // Check if stock is available
  if (currentStock.value < quantity.value) {
    showError('Số lượng trong kho không đủ')
    return
  }
  
  const variant = selectedVariant.value || product.value.variants?.[0]
  const variantId = variant?.id
  
  if (!variantId) {
    showError('Sản phẩm không hợp lệ')
    return
  }
  
  const price = variant.discountPrice || variant.price
  
  // Call API to add to cart (requires login)
  const success = await addToCartComposable(variantId, quantity.value, price)
  
  if (success) {
    // Navigate to checkout
    navigateTo('/checkout')
  }
}

// ===== Review functions =====

const loadCurrentUser = async () => {
  if (!isAuthenticated.value) return
  try {
    const data = await $fetch<any>(`${API_BASE_URL}/api/auth/me`, {
      headers: getAuthHeader()
    })
    currentUserId.value = data?.id || null
  } catch {
    currentUserId.value = null
  }
}

const fetchReviews = async (prodId: number | string, page = 0) => {
  reviewsLoading.value = true
  try {
    const data = await $fetch<any>(`${API_BASE_URL}/api/reviews/product/${prodId}`, {
      params: { page, size: 10 }
    })
    if (page === 0) {
      reviews.value = data.content || []
    } else {
      reviews.value.push(...(data.content || []))
    }
    reviewsPage.value = page
    reviewsHasMore.value = !data.last
  } catch (err) {
    console.error('Error fetching reviews:', err)
  } finally {
    reviewsLoading.value = false
  }
}

const fetchReviewSummary = async (prodId: number | string) => {
  try {
    const data = await $fetch<any>(`${API_BASE_URL}/api/reviews/product/${prodId}/summary`)
    reviewSummary.value = data
  } catch (err) {
    console.error('Error fetching review summary:', err)
  }
}

const ratingPercent = (star: number): number => {
  if (!reviewSummary.value || !reviewSummary.value.totalReviews) return 0
  const count = reviewSummary.value.ratingDistribution?.[star] || 0
  return Math.round((count / reviewSummary.value.totalReviews) * 100)
}

const openReviewForm = () => {
  if (!isAuthenticated.value) {
    showError('Vui lòng đăng nhập để viết đánh giá')
    return navigateTo('/auth/auth')
  }
  editingReview.value = null
  reviewForm.value = { rating: 0, comment: '' }
  showReviewForm.value = true
}

const cancelReviewForm = () => {
  showReviewForm.value = false
  editingReview.value = null
  reviewForm.value = { rating: 0, comment: '' }
}

const submitReview = async () => {
  if (reviewForm.value.rating === 0) return
  reviewSubmitting.value = true
  try {
    if (editingReview.value) {
      await $fetch(`${API_BASE_URL}/api/reviews/${editingReview.value.id}`, {
        method: 'PUT',
        headers: { ...getAuthHeader(), 'Content-Type': 'application/json' },
        body: { rating: reviewForm.value.rating, comment: reviewForm.value.comment }
      })
      showSuccess('Đã cập nhật đánh giá')
    } else {
      await $fetch(`${API_BASE_URL}/api/reviews`, {
        method: 'POST',
        headers: { ...getAuthHeader(), 'Content-Type': 'application/json' },
        body: { productId: product.value.id, rating: reviewForm.value.rating, comment: reviewForm.value.comment }
      })
      showSuccess('Đã gửi đánh giá')
    }
    cancelReviewForm()
    // Reload reviews
    fetchReviews(product.value.id)
    fetchReviewSummary(product.value.id)
  } catch (err: any) {
    const msg = err?.data?.message || err?.message || 'Không thể gửi đánh giá'
    showError(msg)
  } finally {
    reviewSubmitting.value = false
  }
}

const editReview = (review: any) => {
  editingReview.value = review
  reviewForm.value = { rating: review.rating, comment: review.comment || '' }
  showReviewForm.value = true
}

const deleteReview = async (reviewId: number) => {
  const ok = await confirm('Bạn có chắc muốn xóa đánh giá này?', 'Xác nhận xóa')
  if (!ok) return
  try {
    await $fetch(`${API_BASE_URL}/api/reviews/${reviewId}`, {
      method: 'DELETE',
      headers: getAuthHeader()
    })
    showSuccess('Đã xóa đánh giá')
    fetchReviews(product.value.id)
    fetchReviewSummary(product.value.id)
  } catch (err: any) {
    showError(err?.data?.message || 'Không thể xóa đánh giá')
  }
}

const loadMoreReviews = () => {
  if (product.value && reviewsHasMore.value) {
    fetchReviews(product.value.id, reviewsPage.value + 1)
  }
}

// Watch for route changes to reload product
watch(() => route.params.id, () => {
  if (route.params.id) {
    loadProduct()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
})
</script>
