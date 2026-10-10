<template>
  <NuxtLink
    :to="`/products/${product.id}`"
    class="bg-white border-2 border-gray-100 rounded-2xl overflow-hidden transition-all duration-300 hover:shadow-xl hover:scale-105 hover:-translate-y-1 hover:border-[#09f] group cursor-pointer"
  >
    <!-- Product Image -->
    <div class="relative aspect-square bg-gradient-to-br from-blue-50 to-cyan-50 flex items-center justify-center overflow-hidden p-6">
      <img 
        :src="product.img || product.imageUrl" 
        :alt="product.name"
        class="w-full h-full object-contain group-hover:scale-110 transition-transform duration-300"
      />
      <!-- Sale Badge -->
      <div v-if="product.discount" class="absolute top-3 right-3 bg-red-500 text-white px-3 py-1 rounded-lg text-xs font-bold">
        -{{ product.discount }}%
      </div>
      <!-- Category Badge -->
      <div v-if="showCategory && product.category" class="absolute top-3 left-3 bg-white/90 backdrop-blur-sm text-gray-700 px-3 py-1 rounded-lg text-xs font-medium">
        {{ product.category }}
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
          {{ formatPrice(product.salePrice || product.price) }}
        </span>
        <span v-if="product.originalPrice && product.originalPrice > (product.salePrice || product.price)" class="text-sm text-gray-400 line-through">
          {{ formatPrice(product.originalPrice) }}
        </span>
      </div>

      <!-- Actions -->
      <div class="flex gap-2">
        <button 
          @click.prevent="$emit('buy', product.id)" 
          class="flex-1 bg-[#09f] text-white py-2 rounded-xl text-sm font-medium transition-all duration-300 hover:bg-[#0077cc] hover:shadow-lg active:scale-95"
        >
          Mua ngay
        </button>
        <button 
          @click.prevent="$emit('addToCart', product.id)" 
          class="w-10 h-10 border-2 border-gray-200 rounded-xl flex items-center justify-center transition-all duration-300 hover:border-[#09f] hover:text-[#09f] hover:scale-105 active:scale-95"
        >
          +
        </button>
      </div>
    </div>
  </NuxtLink>
</template>

<script setup lang="ts">
interface Product {
  id: string | number
  name: string
  img?: string
  imageUrl?: string
  originalPrice?: number
  salePrice?: number
  price?: number
  discount?: number
  category?: string
}

interface Props {
  product: Product
  showCategory?: boolean
}

defineProps<Props>()
defineEmits<{
  buy: [id: string | number]
  addToCart: [id: string | number]
}>()

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}
</script>
