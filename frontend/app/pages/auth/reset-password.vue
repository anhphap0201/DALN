<template>
  <div class="py-20 min-h-[60vh] flex items-center justify-center bg-gradient-to-br from-blue-50 to-cyan-50 px-4">
    <div class="w-full max-w-sm">
      <div class="bg-white rounded-2xl shadow-lg p-6 transition-all duration-300 hover:shadow-xl">
        <!-- Header -->
        <div class="text-center mb-5">
          <h1 class="text-2xl font-bold text-[#09f] mb-1">Đặt Lại Mật Khẩu</h1>
        </div>

        <!-- Status Messages -->
        <div v-if="statusMessage" :class="[
          'mb-4 p-3 rounded-xl text-sm transition-all duration-300',
          statusType === 'success' ? 'bg-green-50 text-green-700 border border-green-200' : 'bg-red-50 text-red-700 border border-red-200'
        ]">
          {{ statusMessage }}
        </div>

        <!-- No Token -->
        <div v-if="!token" class="text-center space-y-4">
          <div class="w-16 h-16 bg-yellow-100 rounded-full flex items-center justify-center mx-auto">
            <svg class="w-8 h-8 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4.5c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
          </div>
          <p class="text-sm text-gray-600">Link đặt lại mật khẩu không hợp lệ. Vui lòng kiểm tra lại email của bạn.</p>
          <NuxtLink
            to="/auth/forgot-password"
            class="inline-block w-full bg-[#09f] text-white py-2.5 rounded-xl font-medium text-base transition-all duration-300 hover:bg-[#0077cc] hover:shadow-lg hover:scale-[1.02] active:scale-[0.98] text-center"
          >
            Gửi lại link đặt lại
          </NuxtLink>
        </div>

        <!-- Form -->
        <form v-else-if="!resetSuccess" @submit.prevent="handleReset" class="space-y-4">
          <!-- New Password -->
          <div>
            <label for="newPassword" class="block text-xs font-medium text-gray-700 mb-1.5">
              Mật khẩu mới
            </label>
            <input
              id="newPassword"
              v-model="newPassword"
              type="password"
              required
              minlength="6"
              class="w-full px-3 py-2 border-2 border-gray-200 rounded-xl text-sm transition-all duration-300 outline-none focus:border-[#09f] focus:shadow-[0_0_0_3px_rgba(0,153,255,0.1)] hover:border-[#33adff] placeholder:text-gray-400"
              placeholder="Nhập mật khẩu mới (tối thiểu 6 ký tự)"
            />
          </div>

          <!-- Confirm Password -->
          <div>
            <label for="confirmPassword" class="block text-xs font-medium text-gray-700 mb-1.5">
              Xác nhận mật khẩu
            </label>
            <input
              id="confirmPassword"
              v-model="confirmPassword"
              type="password"
              required
              minlength="6"
              class="w-full px-3 py-2 border-2 border-gray-200 rounded-xl text-sm transition-all duration-300 outline-none focus:border-[#09f] focus:shadow-[0_0_0_3px_rgba(0,153,255,0.1)] hover:border-[#33adff] placeholder:text-gray-400"
              placeholder="Nhập lại mật khẩu mới"
            />
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            :disabled="isLoading"
            class="w-full bg-[#09f] text-white py-2.5 rounded-xl font-medium text-base transition-all duration-300 hover:bg-[#0077cc] hover:shadow-lg hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="!isLoading">Đặt lại mật khẩu</span>
            <span v-else class="flex items-center justify-center gap-2">
              <svg class="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Đang xử lý...
            </span>
          </button>
        </form>

        <!-- Success State -->
        <div v-else class="text-center space-y-4">
          <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto">
            <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <div>
            <p class="text-gray-700 font-medium mb-2">Đặt lại mật khẩu thành công!</p>
            <p class="text-sm text-gray-500">Bạn có thể đăng nhập bằng mật khẩu mới.</p>
          </div>
          <NuxtLink
            to="/auth/auth"
            class="inline-block w-full bg-[#09f] text-white py-2.5 rounded-xl font-medium text-base transition-all duration-300 hover:bg-[#0077cc] hover:shadow-lg hover:scale-[1.02] active:scale-[0.98] text-center"
          >
            Đăng nhập ngay
          </NuxtLink>
        </div>

        <!-- Divider -->
        <div v-if="!resetSuccess" class="relative my-4">
          <div class="absolute inset-0 flex items-center">
            <div class="w-full border-t border-gray-200"></div>
          </div>
          <div class="relative flex justify-center text-sm">
            <span class="px-4 bg-white text-gray-500">hoặc</span>
          </div>
        </div>

        <!-- Back to Login -->
        <div v-if="!resetSuccess" class="text-center">
          <NuxtLink
            to="/auth/auth"
            class="text-sm text-[#09f] hover:text-[#0077cc] font-medium transition-colors duration-200 hover:underline inline-flex items-center gap-1"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
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

const token = route.query.token as string
const newPassword = ref('')
const confirmPassword = ref('')
const isLoading = ref(false)
const resetSuccess = ref(false)
const statusMessage = ref('')
const statusType = ref<'success' | 'error'>('success')

const handleReset = async () => {
  if (newPassword.value.length < 6) {
    statusMessage.value = 'Mật khẩu phải có ít nhất 6 ký tự'
    statusType.value = 'error'
    return
  }

  if (newPassword.value !== confirmPassword.value) {
    statusMessage.value = 'Mật khẩu xác nhận không khớp'
    statusType.value = 'error'
    return
  }

  isLoading.value = true
  statusMessage.value = ''

  try {
    await $fetch(`${API_BASE_URL}/auth/reset-password`, {
      method: 'POST',
      body: {
        token,
        newPassword: newPassword.value,
        confirmPassword: confirmPassword.value,
      },
    })
    resetSuccess.value = true
    statusMessage.value = ''
  } catch (err: any) {
    const errorData = err?.data || err?.response?._data
    statusMessage.value = errorData?.message || 'Token không hợp lệ hoặc đã hết hạn. Vui lòng thử lại.'
    statusType.value = 'error'
  } finally {
    isLoading.value = false
  }
}
</script>
