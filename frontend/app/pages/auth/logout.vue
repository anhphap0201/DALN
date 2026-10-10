<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-cyan-50">
    <div class="text-center">
      <div class="w-16 h-16 mx-auto mb-4 rounded-full bg-[#09f] flex items-center justify-center">
        <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
        </svg>
      </div>
      <h2 class="text-2xl font-bold text-gray-800 mb-2">Đăng xuất thành công</h2>
      <p class="text-gray-600 mb-6">Hẹn gặp lại bạn!</p>
      <NuxtLink
        to="/"
        class="inline-block px-6 py-3 bg-[#09f] text-white rounded-xl font-medium transition-all duration-300 hover:bg-[#0077cc] hover:shadow-lg"
      >
        Về trang chủ
      </NuxtLink>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuth } from '~/composables/useAuth'
import { useCart } from '~/composables/useCart'
import { useNotification } from '~/composables/useNotification'

const { logout } = useAuth()
const { cart } = useCart()
const { success } = useNotification()

// Logout immediately when this page loads
logout()
cart.value = null
success('Đăng xuất thành công', 'Hẹn gặp lại bạn!')

// Redirect to home after 2 seconds
setTimeout(() => {
  navigateTo('/')
}, 2000)
</script>
