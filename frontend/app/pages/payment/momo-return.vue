<template>
  <div class="bg-gray-50 min-h-screen py-8">
    <div class="max-w-lg mx-auto px-4">
      <!-- Loading -->
      <div v-if="loading" class="bg-white rounded-2xl p-8 shadow-sm text-center">
        <div class="animate-spin w-12 h-12 border-4 border-[#09f] border-t-transparent rounded-full mx-auto mb-4"></div>
        <p class="text-gray-600">Đang xác nhận thanh toán MoMo...</p>
      </div>

      <!-- Success -->
      <div v-else-if="paymentSuccess" class="bg-white rounded-2xl p-8 shadow-sm text-center space-y-4">
        <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto">
          <svg class="w-8 h-8 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-gray-900">Thanh toán thành công!</h1>
        <p class="text-gray-600">Đơn hàng <span class="font-semibold text-[#09f]">{{ orderNumber }}</span> đã được thanh toán qua MoMo.</p>
        <NuxtLink
          to="/orders"
          class="inline-block mt-4 px-6 py-3 bg-gradient-to-r from-[#09f] to-[#0077cc] text-white rounded-xl font-semibold hover:shadow-lg transition-all duration-300"
        >
          Xem đơn hàng
        </NuxtLink>
      </div>

      <!-- Failed -->
      <div v-else class="bg-white rounded-2xl p-8 shadow-sm text-center space-y-4">
        <div class="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto">
          <svg class="w-8 h-8 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-gray-900">Thanh toán thất bại</h1>
        <p class="text-gray-600">{{ errorMessage }}</p>
        <div class="flex gap-3 justify-center mt-4">
          <NuxtLink
            to="/orders"
            class="px-6 py-3 bg-gray-200 text-gray-700 rounded-xl font-semibold hover:bg-gray-300 transition-all duration-300"
          >
            Xem đơn hàng
          </NuxtLink>
          <NuxtLink
            to="/"
            class="px-6 py-3 bg-gradient-to-r from-[#09f] to-[#0077cc] text-white rounded-xl font-semibold hover:shadow-lg transition-all duration-300"
          >
            Trang chủ
          </NuxtLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

definePageMeta({
  layout: 'default'
})

const API_BASE_URL = 'http://localhost:8080'
const route = useRoute()
const { success: showSuccess, error: showError } = useNotification()

const loading = ref(true)
const paymentSuccess = ref(false)
const orderNumber = ref('')
const errorMessage = ref('Thanh toán MoMo không thành công. Vui lòng thử lại hoặc chọn phương thức khác.')

onMounted(async () => {
  try {
    // Forward all MoMo query params to backend for verification
    const queryString = new URLSearchParams(route.query as Record<string, string>).toString()
    const result: any = await $fetch(`${API_BASE_URL}/api/momo/return?${queryString}`)

    if (result.success) {
      paymentSuccess.value = true
      orderNumber.value = result.orderNumber || ''
      showSuccess('Thanh toán MoMo thành công!')
    } else {
      paymentSuccess.value = false
      errorMessage.value = result.message || 'Thanh toán MoMo không thành công.'
    }
  } catch (error: any) {
    console.error('Error verifying MoMo payment:', error)
    paymentSuccess.value = false
    errorMessage.value = 'Không thể xác nhận thanh toán. Vui lòng kiểm tra đơn hàng.'
  } finally {
    loading.value = false
  }
})
</script>
