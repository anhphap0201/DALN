<template>
  <div class="py-20 min-h-[60vh] flex items-center justify-center bg-gradient-to-br from-blue-50 to-cyan-50 px-4">
    <div class="w-full max-w-sm">
      <!-- Card -->
      <div class="bg-white rounded-2xl shadow-lg p-6 transition-all duration-300 hover:shadow-xl">
        <!-- Header -->
        <div class="text-center mb-5">
          <h1 class="text-2xl font-bold text-[#09f] mb-1">Quên Mật Khẩu</h1>
        </div>

        <!-- Status Messages -->
        <div v-if="statusMessage" :class="[
          'mb-4 p-3 rounded-xl text-sm transition-all duration-300',
          statusType === 'success' ? 'bg-green-50 text-green-700 border border-green-200' : 'bg-red-50 text-red-700 border border-red-200'
        ]">
          {{ statusMessage }}
        </div>

        <!-- Form -->
        <form v-if="!emailSent" @submit.prevent="handleForgotPassword" class="space-y-4">
          <!-- Email -->
          <div>
            <label for="email" class="block text-xs font-medium text-gray-700 mb-1.5">
              Email
            </label>
            <input
              id="email"
              v-model="email"
              type="email"
              required
              class="w-full px-3 py-2 border-2 border-gray-200 rounded-xl text-sm transition-all duration-300 outline-none focus:border-[#09f] focus:shadow-[0_0_0_3px_rgba(0,153,255,0.1)] hover:border-[#33adff] placeholder:text-gray-400"
              placeholder="example@email.com"
            />
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            :disabled="isLoading"
            class="w-full bg-[#09f] text-white py-2.5 rounded-xl font-medium text-base transition-all duration-300 hover:bg-[#0077cc] hover:shadow-lg hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="!isLoading">Gửi Link Đặt Lại</span>
            <span v-else class="flex items-center justify-center gap-2">
              <svg class="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Đang gửi...
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
            <p class="text-gray-700 font-medium mb-2">Email đã được gửi!</p>
            <p class="text-sm text-gray-500">
              Vui lòng kiểm tra hộp thư của bạn và làm theo hướng dẫn để đặt lại mật khẩu.
            </p>
          </div>
          <button
            @click="resetForm"
            class="text-[#09f] hover:text-[#0077cc] font-medium text-sm transition-colors duration-200"
          >
            Gửi lại email
          </button>
        </div>

        <!-- Divider -->
        <div class="relative my-4">
          <div class="absolute inset-0 flex items-center">
            <div class="w-full border-t border-gray-200"></div>
          </div>
          <div class="relative flex justify-center text-sm">
            <span class="px-4 bg-white text-gray-500">hoặc</span>
          </div>
        </div>

        <!-- Back to Login -->
        <div class="text-center">
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
import { ref } from 'vue'

const email = ref('')
const emailSent = ref(false)
const isLoading = ref(false)
const statusMessage = ref('')
const statusType = ref<'success' | 'error'>('success')

const handleForgotPassword = async () => {
  if (!email.value) {
    statusMessage.value = 'Vui lòng nhập địa chỉ email'
    statusType.value = 'error'
    return
  }

  isLoading.value = true
  statusMessage.value = ''

  try {
    await $fetch('http://localhost:8080/auth/forgot-password', {
      method: 'POST',
      body: { email: email.value },
    })
    emailSent.value = true
    statusType.value = 'success'
  } catch (error) {
    statusMessage.value = 'Có lỗi xảy ra. Vui lòng thử lại sau.'
    statusType.value = 'error'
    console.error('Forgot password error:', error)
  } finally {
    isLoading.value = false
  }
}

const resetForm = () => {
  email.value = ''
  emailSent.value = false
  statusMessage.value = ''
  isLoading.value = false
}
</script>
