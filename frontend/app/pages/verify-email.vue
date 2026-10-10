<template>
  <div class="py-20 min-h-[60vh] flex items-center justify-center bg-gradient-to-br from-blue-50 to-cyan-50 px-4">
    <div class="w-full max-w-sm">
      <div class="bg-white rounded-2xl shadow-lg p-6 transition-all duration-300 hover:shadow-xl">
        <!-- Loading State -->
        <div v-if="status === 'loading'" class="text-center space-y-4">
          <div class="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto">
            <svg class="animate-spin h-8 w-8 text-[#09f]" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
          </div>
          <div>
            <h2 class="text-xl font-bold text-gray-800 mb-1">Đang xác thực email...</h2>
            <p class="text-sm text-gray-500">Vui lòng chờ trong giây lát</p>
          </div>
        </div>

        <!-- Success State -->
        <div v-else-if="status === 'success'" class="text-center space-y-4">
          <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto">
            <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <div>
            <h2 class="text-xl font-bold text-green-700 mb-1">Xác thực thành công!</h2>
            <p class="text-sm text-gray-600">{{ message }}</p>
          </div>
          <NuxtLink
            to="/auth/auth"
            class="inline-block w-full bg-[#09f] text-white py-2.5 rounded-xl font-medium text-base transition-all duration-300 hover:bg-[#0077cc] hover:shadow-lg hover:scale-[1.02] active:scale-[0.98] text-center"
          >
            Đăng nhập ngay
          </NuxtLink>
        </div>

        <!-- Error State -->
        <div v-else-if="status === 'error'" class="text-center space-y-4">
          <div class="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto">
            <svg class="w-8 h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <div>
            <h2 class="text-xl font-bold text-red-700 mb-1">Xác thực thất bại</h2>
            <p class="text-sm text-gray-600">{{ message }}</p>
          </div>
          <div class="space-y-2">
            <NuxtLink
              to="/auth/auth"
              class="inline-block w-full bg-[#09f] text-white py-2.5 rounded-xl font-medium text-base transition-all duration-300 hover:bg-[#0077cc] hover:shadow-lg hover:scale-[1.02] active:scale-[0.98] text-center"
            >
              Quay lại đăng nhập
            </NuxtLink>
          </div>
        </div>

        <!-- No Token -->
        <div v-else class="text-center space-y-4">
          <div class="w-16 h-16 bg-yellow-100 rounded-full flex items-center justify-center mx-auto">
            <svg class="w-8 h-8 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4.5c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
          </div>
          <div>
            <h2 class="text-xl font-bold text-yellow-700 mb-1">Thiếu token</h2>
            <p class="text-sm text-gray-600">Link xác thực không hợp lệ. Vui lòng kiểm tra lại email của bạn.</p>
          </div>
          <NuxtLink
            to="/auth/auth"
            class="inline-block w-full bg-[#09f] text-white py-2.5 rounded-xl font-medium text-base transition-all duration-300 hover:bg-[#0077cc] hover:shadow-lg hover:scale-[1.02] active:scale-[0.98] text-center"
          >
            Quay lại đăng nhập
          </NuxtLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const route = useRoute()

const API_BASE_URL = 'http://localhost:8080'

const status = ref<'loading' | 'success' | 'error' | 'no-token'>('no-token')
const message = ref('')

onMounted(async () => {
  const token = route.query.token as string

  if (!token) {
    status.value = 'no-token'
    return
  }

  status.value = 'loading'

  try {
    const data = await $fetch<{ message: string; email: string; verified: boolean }>(
      `${API_BASE_URL}/auth/verify-email`,
      {
        method: 'POST',
        body: { token },
      }
    )
    status.value = 'success'
    message.value = data.message || 'Email của bạn đã được xác thực thành công!'
  } catch (err: any) {
    status.value = 'error'
    const errorData = err?.data || err?.response?._data
    message.value = errorData?.message || 'Token không hợp lệ hoặc đã hết hạn. Vui lòng thử lại.'
  }
})
</script>
