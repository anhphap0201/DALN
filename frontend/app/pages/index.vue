<template>
  <div class="bg-gray-50 min-h-screen">
    <!-- Banner Carousel -->
    <section class="bg-white border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div class="relative overflow-hidden rounded-2xl bg-gradient-to-r from-[#09f] to-[#0077cc] h-64">
          <Transition
            enter-active-class="transition-all duration-700 ease-out"
            leave-active-class="transition-all duration-700 ease-in absolute inset-0"
            enter-from-class="opacity-0 translate-x-full"
            enter-to-class="opacity-100 translate-x-0"
            leave-from-class="opacity-100 translate-x-0"
            leave-to-class="opacity-0 -translate-x-full"
          >
            <div :key="currentBanner" class="absolute inset-0 flex items-center justify-center p-8">
              <div class="text-center text-white">
                <h2 class="text-4xl font-bold mb-3">{{ banners[currentBanner]?.title }}</h2>
                <p class="text-xl mb-6 text-blue-100">{{ banners[currentBanner]?.description }}</p>
                <button class="px-8 py-3 bg-white text-[#09f] rounded-xl font-semibold text-lg transition-all duration-300 hover:bg-blue-50 hover:scale-105 hover:shadow-xl">
                  {{ banners[currentBanner]?.action }}
                </button>
              </div>
            </div>
          </Transition>
          
          <!-- Banner Indicators -->
          <div class="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2">
            <button
              v-for="(_, index) in banners"
              :key="index"
              @click="currentBanner = index"
              :class="[
                'w-2 h-2 rounded-full transition-all duration-300',
                currentBanner === index ? 'w-8 bg-white' : 'bg-white/50 hover:bg-white/80'
              ]"
            ></button>
          </div>
        </div>
      </div>
    </section>

    <!-- Categories -->
    <section class="py-12">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-2xl font-bold text-gray-800">Danh Mục Sản Phẩm</h2>
        </div>

        <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-10 gap-4">
          <div
            v-for="category in categories"
            :key="category.id"
            class="bg-white rounded-2xl p-4 flex flex-col items-center justify-center gap-3 transition-all duration-300 hover:shadow-lg hover:scale-105 hover:-translate-y-1 cursor-pointer border-2 border-transparent hover:border-[#09f] group"
            @click="handleCategoryClick(category.id)"
          >
            <div class="bg-none w-16 h-16 bg-gradient-to-br from-blue-50 to-cyan-50 rounded-xl flex items-center justify-center overflow-hidden transition-all duration-300 group-hover:scale-110">
              <img 
                :src="getCategoryImage(category)" 
                :alt="category.name"
                class="w-16 h-16 object-contain "
              />
            </div>
            <span class="text-xs text-center text-gray-700 font-medium group-hover:text-[#09f] transition-colors duration-200">
              {{ category.name }}
            </span>
          </div>
        </div>
      </div>
    </section>

    <!-- Sale Products -->
    <section class="py-12 bg-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between mb-6">
          <div>
            <h2 class="text-2xl font-bold text-gray-800">Sản Phẩm Đang Sale</h2>
            <p class="text-sm text-gray-500 mt-1">Giảm giá lên đến 50%</p>
          </div>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
          <NuxtLink
            v-for="product in saleProducts"
            :key="product.id"
            :to="`/products/${product.id}`"
            class="bg-white border-2 border-gray-100 rounded-2xl overflow-hidden transition-all duration-300 hover:shadow-xl hover:scale-105 hover:-translate-y-1 hover:border-[#09f] group cursor-pointer"
          >
            <!-- Product Image -->
            <div class=" relative aspect-square bg-gradient-to-br from-blue-50 to-cyan-50 flex items-center justify-center overflow-hidden p-6">
              <img 
                :src="product.img" 
                :alt="product.name"
                class="w-full h-full object-contain group-hover:scale-110 transition-transform duration-300"
              />
              <!-- Sale Badge -->
              <div class="absolute top-3 right-3 bg-red-500 text-white px-3 py-1 rounded-lg text-xs font-bold">
                -{{ product.discount }}%
              </div>
            </div>

            <!-- Product Info -->
            <div class="p-4">
              <h3 class="text-sm font-semibold text-gray-800 mb-2 line-clamp-2 group-hover:text-[#09f] transition-colors duration-200">
                {{ product.name }}
              </h3>
              
              <!-- Price -->
              <div class="flex items-center gap-2 mb-3">
                <span class="text-lg font-bold text-[#09f]">
                  {{ formatPrice(product.salePrice) }}
                </span>
                <span class="text-sm text-gray-400 line-through">
                  {{ formatPrice(product.originalPrice) }}
                </span>
              </div>

              <!-- Actions -->
              <div class="flex gap-2">
                <button @click.prevent="buyNow($event, product)" class="flex-1 bg-[#09f] text-white py-2 rounded-xl text-sm font-medium transition-all duration-300 hover:bg-[#0077cc] hover:shadow-lg active:scale-95">
                  Mua ngay
                </button>
                <button @click.prevent="addToCart($event, product)" class="w-10 h-10 border-2 border-gray-200 rounded-xl flex items-center justify-center transition-all duration-300 hover:border-[#09f] hover:text-[#09f] hover:scale-105 active:scale-95">
                  +
                </button>
              </div>
            </div>
          </NuxtLink>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useAuth } from '../composables/useAuth'
import { useNotification } from '../composables/useNotification'

const banners = [
  {
    title: 'Giảm Giá 30% Tất Cả Sản Phẩm',
    description: 'Chương trình khuyến mãi lớn - Áp dụng đến hết tháng',
    action: 'Mua Ngay'
  },
  {
    title: 'Arduino Nano Mới Về',
    description: 'Hàng chính hãng - Giá tốt nhất thị trường',
    action: 'Xem Chi Tiết'
  },
  {
    title: 'Miễn Phí Vận Chuyển',
    description: 'Cho đơn hàng từ 500.000đ',
    action: 'Khám Phá'
  }
]

// Fetch categories from API
const categories = ref<any[]>([])

// Default images for categories
const categoryImages: Record<string, string> = {
  'Arduino & Modules': '/images/ArduinoUnoR3.jpg',
  'Raspberry Pi': '/images/raspberry-pi-5-6.jpg',
  'Linh kiện điện tử': '/images/resistores.jpg',
  'Cảm biến': '/images/cam-bien-vat-can-hong-ngoai-fm52-5.jpg',
  'Module nguồn': '/images/61GYgJyVv5L.jpg',
  'Module truyền thông': '/images/nodemcu-esp32-01.webp',
  'Màn hình hiển thị': '/images/man-hinh-lcd-oled-0-96-inch-giao-tiep-i2c-white-9w56-1.jpg',
  'Motor & Driver': '/images/dong-co-servo-sg90-180-do-rk7a-1.jpg',
  'default': '/images/pngtree-ellipsis-black-glyph-ui-icon-flat-negative-space-ellipsis-vector-png-image_48303394.jpg'
}

const getCategoryImage = (category: any) => {
  return category.imageUrl || categoryImages[category.name] || categoryImages['default']
}

const saleProducts = ref<any[]>([])

const currentBanner = ref(0)
let bannerInterval: ReturnType<typeof setInterval>

onMounted(async () => {
  // Auto slide banner every 8 seconds
  bannerInterval = setInterval(() => {
    currentBanner.value = (currentBanner.value + 1) % banners.length
  }, 8000)

  // Fetch categories from API
  try {
    const data = await $fetch('http://localhost:8080/api/categories')
    categories.value = data as any[]
  } catch (error) {
    console.error('Error fetching categories:', error)
  }

  // Fetch sale products from API
  try {
    const data = await $fetch<any>('http://localhost:8080/api/products/search/filter/on-sale?page=0&size=10')
    const items = data.content || data
    saleProducts.value = (items as any[]).map((p: any) => {
      const minPrice = p.minDiscountPrice || p.minPrice || 0
      const originalPrice = p.minPrice || 0
      const discount = originalPrice > 0 && minPrice < originalPrice
        ? Math.round((1 - minPrice / originalPrice) * 100)
        : 0
      return {
        id: p.id,
        name: p.name,
        img: p.defaultImageUrl ? `http://localhost:8080${p.defaultImageUrl}` : '/images/Sub_Edunera_Logo_512.png',
        originalPrice: Number(originalPrice),
        salePrice: Number(minPrice),
        discount,
      }
    })
  } catch (error) {
    console.error('Error fetching sale products:', error)
  }
})

onUnmounted(() => {
  if (bannerInterval) {
    clearInterval(bannerInterval)
  }
})

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const handleCategoryClick = (categoryId: number) => {
  navigateTo(`/search?category=${categoryId}`)
}

const auth = useAuth()
const notify = useNotification()

const addToCart = async (event: Event, productOrVariant: any) => {
  event.stopPropagation()
  if (!auth.isAuthenticated.value) {
    notify.info('Vui lòng đăng nhập', 'Bạn cần đăng nhập để thêm sản phẩm vào giỏ hàng')
    return navigateTo('/auth/auth')
  }

  try {
    // If this is a product with variants (from search API), pick a variant
    let variant: any = null
    if (productOrVariant && Array.isArray(productOrVariant.variants)) {
      variant = productOrVariant.variants.find((v: any) => v.inStock && v.inStock > 0) || productOrVariant.variants[0]
    } else {
      // assume argument is already a variant-like object
      variant = productOrVariant
    }

    if (!variant || !variant.id) {
      notify.error('Lỗi', 'Không tìm thấy biến thể sản phẩm')
      return
    }

    const payload = {
      productVariantId: variant.id,
      quantity: 1,
      price: variant.discountPrice ?? variant.price ?? productOrVariant.salePrice ?? 0,
      productName: variant.productName ?? productOrVariant.name,
      imageUrl: variant.imageUrl ?? productOrVariant.img ?? productOrVariant.defaultImageUrl
    }

    await $fetch('http://localhost:8080/api/cart/items', {
      method: 'POST',
      body: payload,
      headers: auth.getAuthHeader()
    })

    notify.success('Thành công', 'Đã thêm sản phẩm vào giỏ hàng')
  } catch (err) {
    console.error('Add to cart error:', err)
    notify.error('Lỗi', 'Không thể thêm sản phẩm vào giỏ hàng')
  }
}

const buyNow = async (event: Event, product: any) => {
  event.stopPropagation()
  if (!auth.isAuthenticated.value) {
    notify.info('Vui lòng đăng nhập', 'Bạn cần đăng nhập để mua hàng')
    return navigateTo('/auth/auth')
  }

  try {
    // Fetch real product + variants from backend
    const productDetail = await $fetch<any>(`http://localhost:8080/api/products/search/by-product-id/${product.id}`)

    if (!productDetail) {
      notify.error('Lỗi', 'Không tìm thấy sản phẩm')
      return
    }

    // Add selected variant to cart (without creating order)
    await addToCart(event, productDetail)

    notify.success('Đã thêm vào giỏ', 'Sản phẩm đã được thêm vào giỏ hàng. Vui lòng xác nhận tại giỏ hàng để thanh toán.')
    // Redirect user to cart so they can confirm and checkout explicitly
    return navigateTo('/cart')
  } catch (err) {
    console.error('Buy now error:', err)
    notify.error('Lỗi', 'Không thể thực hiện mua ngay bây giờ')
  }
}
</script>
