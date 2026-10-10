<template>
  <div class="py-20 min-h-[50vh] flex items-center justify-center bg-gradient-to-br from-blue-50 to-cyan-50 px-4">
    <div class="w-full max-w-sm">
      <!-- Card -->
      <div class="bg-white rounded-2xl shadow-lg p-6 transition-all duration-300 hover:shadow-xl">
        <!-- Tab Switcher -->
        <div class="relative bg-gray-100 rounded-xl p-1 mb-5">
          <!-- Sliding Background -->
          <div
            :class="[
              'absolute top-1 bottom-1 rounded-xl bg-[#09f] shadow-md transition-all duration-300 ease-out',
              activeTab === 'login' ? 'left-1 right-[calc(50%+0.125rem)]' : 'left-[calc(50%+0.125rem)] right-1'
            ]"
          ></div>
          
          <!-- Buttons -->
          <div class="relative flex gap-1">
            <button
              @click="activeTab = 'login'"
              :class="[
                'flex-1 py-2 rounded-xl font-medium text-sm transition-all duration-300 z-10',
                activeTab === 'login'
                  ? 'text-white'
                  : 'text-gray-600 hover:text-gray-800'
              ]"
            >
              Đăng Nhập
            </button>
            <button
              @click="activeTab = 'register'"
              :class="[
                'flex-1 py-2 rounded-xl font-medium text-sm transition-all duration-300 z-10',
                activeTab === 'register'
                  ? 'text-white'
                  : 'text-gray-600 hover:text-gray-800'
              ]"
            >
              Đăng Ký
            </button>
          </div>
        </div>

        <!-- Forms Container -->
        <div class="relative min-h-[280px] overflow-hidden">
          <!-- Login Form -->
          <Transition
            enter-active-class="transition-all duration-500 ease-out z-20"
            leave-active-class="transition-all duration-500 ease-in absolute top-0 left-0 right-0 z-10"
            enter-from-class="opacity-0 translate-x-[-100%]"
            enter-to-class="opacity-100 translate-x-0"
            leave-from-class="opacity-100 translate-x-0"
            leave-to-class="opacity-0 translate-x-[-100%]"
          >
            <form v-if="activeTab === 'login'" @submit.prevent="handleLogin" class="space-y-3.5">
              <!-- Email -->
              <div>
                <label for="loginEmail" class="block text-xs font-medium text-gray-700 mb-1.5">
                  Email
                </label>
                <input
                  id="loginEmail"
                  v-model="loginData.email"
                  type="email"
                  required
                  class="w-full px-3 py-2 border-2 border-gray-200 rounded-xl text-sm transition-all duration-300 outline-none focus:border-[#09f] focus:shadow-[0_0_0_3px_rgba(0,153,255,0.1)] hover:border-[#33adff] placeholder:text-gray-400"
                  placeholder="example@email.com"
                />
              </div>

              <!-- Password -->
              <div>
                <label for="loginPassword" class="block text-xs font-medium text-gray-700 mb-1.5">
                  Mật khẩu
                </label>
                <input
                  id="loginPassword"
                  v-model="loginData.password"
                  type="password"
                  required
                  class="w-full px-3 py-2 border-2 border-gray-200 rounded-xl text-sm transition-all duration-300 outline-none focus:border-[#09f] focus:shadow-[0_0_0_3px_rgba(0,153,255,0.1)] hover:border-[#33adff] placeholder:text-gray-400"
                  placeholder="Nhập mật khẩu"
                />
              </div>

              <!-- Remember & Forgot -->
              <div class="flex items-center justify-between">
                <label class="flex items-center gap-2 cursor-pointer group">
                  <input
                    v-model="loginData.rememberMe"
                    type="checkbox"
                    class="w-4 h-4 rounded border-2 border-gray-300 text-[#09f] focus:ring-2 focus:ring-[#09f] focus:ring-offset-0 transition-all duration-200 cursor-pointer"
                  />
                  <span class="text-xs text-gray-600 group-hover:text-gray-800 transition-colors duration-200">
                    Ghi nhớ mật khẩu
                  </span>
                </label>
                <NuxtLink
                  to="/auth/forgot-password"
                  class="text-xs text-[#09f] hover:text-[#0077cc] font-medium"
                >
                Quên mật khẩu?
                </NuxtLink>
              </div>

              <!-- Submit Button -->
              <button
                type="submit"
                class="w-full bg-[#09f] text-white py-2.5 rounded-xl font-medium text-base transition-all duration-300 hover:bg-[#0077cc] hover:shadow-lg hover:scale-[1.02] active:scale-[0.98]"
              >
                Đăng Nhập
              </button>
            </form>
          </Transition>

          <!-- Register Form -->
          <Transition
            enter-active-class="transition-all duration-500 ease-out z-20"
            leave-active-class="transition-all duration-500 ease-in absolute top-0 left-0 right-0 z-10"
            enter-from-class="opacity-0 translate-x-[100%]"
            enter-to-class="opacity-100 translate-x-0"
            leave-from-class="opacity-100 translate-x-0"
            leave-to-class="opacity-0 translate-x-[100%]"
          >
            <form v-if="activeTab === 'register'" @submit.prevent="handleRegister" class="space-y-3.5">
              <!-- Full Name -->
              <div>
                <label for="fullName" class="block text-xs font-medium text-gray-700 mb-1.5">
                  Họ và tên
                </label>
                <input
                  id="fullName"
                  v-model="registerData.fullName"
                  type="text"
                  required
                  class="w-full px-3 py-2 border-2 border-gray-200 rounded-xl text-sm transition-all duration-300 outline-none focus:border-[#09f] focus:shadow-[0_0_0_3px_rgba(0,153,255,0.1)] hover:border-[#33adff] placeholder:text-gray-400"
                  placeholder="Nguyễn Văn A"
                />
              </div>

              <!-- Email -->
              <div>
                <label for="email" class="block text-xs font-medium text-gray-700 mb-1.5">
                  Email
                </label>
                <input
                  id="email"
                  v-model="registerData.email"
                  type="email"
                  required
                  class="w-full px-3 py-2 border-2 border-gray-200 rounded-xl text-sm transition-all duration-300 outline-none focus:border-[#09f] focus:shadow-[0_0_0_3px_rgba(0,153,255,0.1)] hover:border-[#33adff] placeholder:text-gray-400"
                  placeholder="example@email.com"
                />
              </div>

              <!-- Password -->
              <div>
                <label for="password" class="block text-xs font-medium text-gray-700 mb-1.5">
                  Mật khẩu
                </label>
                <input
                  id="password"
                  v-model="registerData.password"
                  type="password"
                  required
                  class="w-full px-3 py-2 border-2 border-gray-200 rounded-xl text-sm transition-all duration-300 outline-none focus:border-[#09f] focus:shadow-[0_0_0_3px_rgba(0,153,255,0.1)] hover:border-[#33adff] placeholder:text-gray-400"
                  placeholder="Nhập mật khẩu"
                />
              </div>

              <!-- Confirm Password -->
              <div>
                <label for="confirmPassword" class="block text-xs font-medium text-gray-700 mb-1.5">
                  Xác nhận mật khẩu
                </label>
                <input
                  id="confirmPassword"
                  v-model="registerData.confirmPassword"
                  type="password"
                  required
                  class="w-full px-3 py-2 border-2 border-gray-200 rounded-xl text-sm transition-all duration-300 outline-none focus:border-[#09f] focus:shadow-[0_0_0_3px_rgba(0,153,255,0.1)] hover:border-[#33adff] placeholder:text-gray-400"
                  placeholder="Nhập lại mật khẩu"
                />
              </div>

              <!-- Submit Button -->
              <button
                type="submit"
                class="w-full bg-[#09f] text-white py-2.5 rounded-xl font-medium text-base transition-all duration-300 hover:bg-[#0077cc] hover:shadow-lg hover:scale-[1.02] active:scale-[0.98]"
              >
                Đăng Ký
              </button>
            </form>
          </Transition>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { useNotification } from '~/composables/useNotification'
import { useAuth } from '~/composables/useAuth'

definePageMeta({
  middleware: 'guest'
})

const activeTab = ref<'login' | 'register'>('login')
const { success, error } = useNotification()
const { login } = useAuth()

const loginData = ref({
  email: '',
  password: '',
  rememberMe: false
})

const registerData = ref({
  fullName: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const handleLogin = async () => {
  try {
    const data = await $fetch('http://localhost:8080/auth/login', {
      method: 'POST',
      body: {
        email: loginData.value.email,
        password: loginData.value.password
      }
    })

    // API returns accessToken, refreshToken, email, fullName, role
    login({ 
      email: data.email, 
      fullName: data.fullName,
      role: data.role 
    }, data.accessToken)
    success('Đăng nhập thành công!', 'Chào mừng bạn quay trở lại')
    
    // Wait for UI to update before navigating
    await nextTick()
    setTimeout(() => navigateTo('/'), 300)
  } catch (err: any) {
    // Read detailed error from backend response
    const errorMessage = err.data?.error 
      || err.data?.message 
      || (err.data && typeof err.data === 'object' ? Object.values(err.data).join(', ') : null)
      || 'Đã có lỗi xảy ra, vui lòng thử lại'
    error('Đăng nhập thất bại', errorMessage)
  }
}

const handleRegister = async () => {
  if (registerData.value.password !== registerData.value.confirmPassword) {
    return error('Mật khẩu không khớp!', 'Vui lòng nhập lại mật khẩu xác nhận')
  }

  try {
    await $fetch('http://localhost:8080/auth/register', {
      method: 'POST',
      body: {
        fullName: registerData.value.fullName,
        email: registerData.value.email,
        password: registerData.value.password,
        role: 'ROLE_USER'
      }
    })

    success('Đăng ký thành công!', 'Bạn có thể đăng nhập ngay bây giờ')
    activeTab.value = 'login'
    registerData.value = { fullName: '', email: '', password: '', confirmPassword: '' }
  } catch (err: any) {
    const errorMessage = err.data?.error 
      || err.data?.message 
      || (err.data && typeof err.data === 'object' ? Object.values(err.data).join(', ') : null)
      || 'Đã có lỗi xảy ra, vui lòng thử lại'
    error('Đăng ký thất bại', errorMessage)
  }
}
</script>