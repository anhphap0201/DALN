<template>
  <div class="py-12 md:py-20 min-h-[calc(100vh-140px)] flex items-center justify-center bg-gradient-to-br from-slate-100 via-blue-50/50 to-cyan-50/60 px-4">
    <!-- Main Dual Panel Container -->
    <div class="w-full max-w-4xl min-h-[560px] md:min-h-[640px] md:h-[640px] bg-white rounded-3xl shadow-2xl relative overflow-hidden border border-slate-100">
      
      <!-- Mobile Tab Switcher (Visible on mobile only) -->
      <div class="md:hidden p-5 pb-0">
        <div class="relative bg-slate-100 rounded-2xl p-1 mb-2">
          <!-- Sliding pill -->
          <div
            :class="[
              'absolute top-1 bottom-1 rounded-xl bg-[#09f] shadow-md transition-all duration-300 ease-out',
              activeTab === 'login' ? 'left-1 right-[calc(50%+0.125rem)]' : 'left-[calc(50%+0.125rem)] right-1'
            ]"
          ></div>
          <div class="relative flex gap-1">
            <button
              type="button"
              @click="activeTab = 'login'"
              :class="[
                'flex-1 py-2 rounded-xl font-bold text-sm transition-all duration-300 z-10',
                activeTab === 'login' ? 'text-white' : 'text-slate-600 hover:text-slate-900'
              ]"
            >
              Đăng Nhập
            </button>
            <button
              type="button"
              @click="activeTab = 'register'"
              :class="[
                'flex-1 py-2 rounded-xl font-bold text-sm transition-all duration-300 z-10',
                activeTab === 'register' ? 'text-white' : 'text-slate-600 hover:text-slate-900'
              ]"
            >
              Đăng Ký
            </button>
          </div>
        </div>
      </div>

      <!-- DESKTOP SLIDING OVERLAY PANEL (Visible on md and up) -->
      <div
        :class="[
          'hidden md:block absolute top-0 left-0 w-1/2 h-full z-30 transition-transform duration-700 ease-in-out overflow-hidden shadow-2xl bg-gradient-to-br from-[#0099ff] via-[#0077cc] to-[#00488f] text-white',
          activeTab === 'login' ? 'translate-x-full' : 'translate-x-0'
        ]"
      >
        <!-- Ambient Glow Shapes -->
        <div class="absolute -top-16 -left-16 w-56 h-56 rounded-full bg-white/10 blur-2xl pointer-events-none"></div>
        <div class="absolute -bottom-20 -right-20 w-72 h-72 rounded-full bg-cyan-400/20 blur-3xl pointer-events-none"></div>
        <div class="absolute top-1/3 left-10 w-40 h-40 rounded-full bg-blue-300/10 blur-xl pointer-events-none"></div>

        <!-- Panel 1: Shown when activeTab === 'login' (Overlay is on the right side) -> Invites user to Register -->
        <div
          :class="[
            'w-full h-full absolute inset-0 flex flex-col items-center justify-center px-10 text-center transition-all duration-500 ease-in-out',
            activeTab === 'login'
              ? 'opacity-100 translate-x-0 pointer-events-auto delay-150'
              : 'opacity-0 translate-x-8 pointer-events-none'
          ]"
        >
          <div class="w-16 h-16 rounded-2xl bg-white/15 backdrop-blur-md border border-white/25 flex items-center justify-center mb-6 shadow-inner">
            <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
            </svg>
          </div>
          <h2 class="text-3xl font-black mb-3 tracking-tight">Chào Bạn Mới!</h2>
          <p class="text-white/85 text-sm leading-relaxed mb-8 max-w-xs">
            Chưa có tài khoản? Đăng ký thành viên chỉ trong 30 giây để nhận ưu đãi lên tới 50% cho linh kiện điện tử!
          </p>
          <button
            type="button"
            @click="activeTab = 'register'"
            class="px-8 py-3 rounded-xl border-2 border-white/80 text-white font-bold text-sm tracking-wide uppercase hover:bg-white hover:text-[#0077cc] transition-all duration-300 shadow-lg hover:shadow-xl hover:scale-105 active:scale-95 cursor-pointer backdrop-blur-xs"
          >
            Đăng Ký Ngay
          </button>
        </div>

        <!-- Panel 2: Shown when activeTab === 'register' (Overlay is on the left side) -> Invites user to Login -->
        <div
          :class="[
            'w-full h-full absolute inset-0 flex flex-col items-center justify-center px-10 text-center transition-all duration-500 ease-in-out',
            activeTab === 'register'
              ? 'opacity-100 translate-x-0 pointer-events-auto delay-150'
              : 'opacity-0 -translate-x-8 pointer-events-none'
          ]"
        >
          <div class="w-16 h-16 rounded-2xl bg-white/15 backdrop-blur-md border border-white/25 flex items-center justify-center mb-6 shadow-inner">
            <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
            </svg>
          </div>
          <h2 class="text-3xl font-black mb-3 tracking-tight">Chào Mừng Trở Lại!</h2>
          <p class="text-white/85 text-sm leading-relaxed mb-8 max-w-xs">
            Đã có tài khoản tại cửa hàng? Đăng nhập ngay để tiếp tục mua sắm và theo dõi đơn hàng của bạn.
          </p>
          <button
            type="button"
            @click="activeTab = 'login'"
            class="px-8 py-3 rounded-xl border-2 border-white/80 text-white font-bold text-sm tracking-wide uppercase hover:bg-white hover:text-[#0077cc] transition-all duration-300 shadow-lg hover:shadow-xl hover:scale-105 active:scale-95 cursor-pointer backdrop-blur-xs"
          >
            Đăng Nhập Ngay
          </button>
        </div>
      </div>

      <!-- FORMS WRAPPER -->
      <div class="w-full h-full min-h-[560px] md:min-h-[640px] md:h-[640px] flex flex-col md:flex-row relative">
        
        <!-- LOGIN FORM (Left Half on desktop) -->
        <div
          :class="[
            'w-full md:w-1/2 h-full min-h-[560px] md:min-h-[640px] px-6 sm:px-10 md:px-12 py-8 flex flex-col justify-center transition-opacity duration-500 ease-in-out',
            activeTab === 'login'
              ? 'opacity-100 pointer-events-auto flex'
              : 'md:opacity-0 md:pointer-events-none hidden md:flex'
          ]"
        >
          <div class="max-w-sm mx-auto w-full">
            <div class="mb-6">
              <h1 class="text-2xl sm:text-3xl font-black text-slate-900 mb-1.5 tracking-tight">Đăng Nhập</h1>
              <p class="text-slate-500 text-xs sm:text-sm">Nhập thông tin tài khoản của bạn để tiếp tục</p>
            </div>

            <form @submit.prevent="handleLogin" class="space-y-4">
              <!-- Email -->
              <div>
                <label for="loginEmail" class="block text-xs font-bold text-slate-700 mb-1.5">
                  Email
                </label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207" />
                    </svg>
                  </div>
                  <input
                    id="loginEmail"
                    v-model="loginData.email"
                    type="email"
                    required
                    class="w-full pl-10 pr-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm transition-all duration-200 outline-none focus:bg-white focus:border-[#09f] focus:ring-2 focus:ring-[#09f]/20 hover:border-slate-300 placeholder:text-slate-400 font-medium text-slate-800"
                    placeholder="example@email.com"
                  />
                </div>
              </div>

              <!-- Password -->
              <div>
                <label for="loginPassword" class="block text-xs font-bold text-slate-700 mb-1.5">
                  Mật khẩu
                </label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                  </div>
                  <input
                    id="loginPassword"
                    v-model="loginData.password"
                    type="password"
                    required
                    class="w-full pl-10 pr-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm transition-all duration-200 outline-none focus:bg-white focus:border-[#09f] focus:ring-2 focus:ring-[#09f]/20 hover:border-slate-300 placeholder:text-slate-400 font-medium text-slate-800"
                    placeholder="••••••••"
                  />
                </div>
              </div>

              <!-- Remember & Forgot -->
              <div class="flex items-center justify-between pt-1">
                <label class="flex items-center gap-2 cursor-pointer group">
                  <input
                    v-model="loginData.rememberMe"
                    type="checkbox"
                    class="w-4 h-4 rounded border-slate-300 text-[#09f] focus:ring-[#09f] focus:ring-2 cursor-pointer"
                  />
                  <span class="text-xs text-slate-600 group-hover:text-slate-900 transition-colors">
                    Ghi nhớ
                  </span>
                </label>
                <NuxtLink
                  to="/auth/forgot-password"
                  class="text-xs text-[#09f] hover:text-[#0077cc] font-bold transition-colors"
                >
                  Quên mật khẩu?
                </NuxtLink>
              </div>

              <!-- Submit Button -->
              <button
                type="submit"
                class="w-full bg-[#09f] text-white py-3 rounded-xl font-bold text-sm transition-all duration-300 hover:bg-[#0077cc] hover:shadow-lg hover:shadow-[#09f]/25 hover:scale-[1.01] active:scale-[0.99] cursor-pointer"
              >
                Đăng Nhập
              </button>
            </form>

            <!-- Mobile switcher hint -->
            <div class="md:hidden mt-6 text-center text-xs text-slate-500">
              Chưa có tài khoản? 
              <button @click="activeTab = 'register'" class="text-[#09f] font-bold hover:underline">
                Đăng ký ngay
              </button>
            </div>
          </div>
        </div>

        <!-- REGISTER FORM (Right Half on desktop) -->
        <div
          :class="[
            'w-full md:w-1/2 h-full min-h-[560px] md:min-h-[640px] px-6 sm:px-10 md:px-12 py-8 flex flex-col justify-center transition-opacity duration-500 ease-in-out',
            activeTab === 'register'
              ? 'opacity-100 pointer-events-auto flex'
              : 'md:opacity-0 md:pointer-events-none hidden md:flex'
          ]"
        >
          <div class="max-w-sm mx-auto w-full">
            <div class="mb-5">
              <h1 class="text-2xl sm:text-3xl font-black text-slate-900 mb-1.5 tracking-tight">Tạo Tài Khoản</h1>
              <p class="text-slate-500 text-xs sm:text-sm">Đăng ký để nhận voucher giảm 10% đơn hàng đầu</p>
            </div>

            <form @submit.prevent="handleRegister" class="space-y-3.5">
              <!-- Full Name -->
              <div>
                <label for="fullName" class="block text-xs font-bold text-slate-700 mb-1">
                  Họ và tên
                </label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                  </div>
                  <input
                    id="fullName"
                    v-model="registerData.fullName"
                    type="text"
                    required
                    class="w-full pl-10 pr-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm transition-all duration-200 outline-none focus:bg-white focus:border-[#09f] focus:ring-2 focus:ring-[#09f]/20 hover:border-slate-300 placeholder:text-slate-400 font-medium text-slate-800"
                    placeholder="Nguyễn Văn A"
                  />
                </div>
              </div>

              <!-- Email -->
              <div>
                <label for="email" class="block text-xs font-bold text-slate-700 mb-1">
                  Email
                </label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207" />
                    </svg>
                  </div>
                  <input
                    id="email"
                    v-model="registerData.email"
                    type="email"
                    required
                    class="w-full pl-10 pr-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm transition-all duration-200 outline-none focus:bg-white focus:border-[#09f] focus:ring-2 focus:ring-[#09f]/20 hover:border-slate-300 placeholder:text-slate-400 font-medium text-slate-800"
                    placeholder="example@email.com"
                  />
                </div>
              </div>

              <!-- Password -->
              <div>
                <label for="password" class="block text-xs font-bold text-slate-700 mb-1">
                  Mật khẩu
                </label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                  </div>
                  <input
                    id="password"
                    v-model="registerData.password"
                    type="password"
                    required
                    class="w-full pl-10 pr-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm transition-all duration-200 outline-none focus:bg-white focus:border-[#09f] focus:ring-2 focus:ring-[#09f]/20 hover:border-slate-300 placeholder:text-slate-400 font-medium text-slate-800"
                    placeholder="Tối thiểu 6 ký tự"
                  />
                </div>
              </div>

              <!-- Confirm Password -->
              <div>
                <label for="confirmPassword" class="block text-xs font-bold text-slate-700 mb-1">
                  Xác nhận mật khẩu
                </label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                    </svg>
                  </div>
                  <input
                    id="confirmPassword"
                    v-model="registerData.confirmPassword"
                    type="password"
                    required
                    class="w-full pl-10 pr-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm transition-all duration-200 outline-none focus:bg-white focus:border-[#09f] focus:ring-2 focus:ring-[#09f]/20 hover:border-slate-300 placeholder:text-slate-400 font-medium text-slate-800"
                    placeholder="Nhập lại mật khẩu"
                  />
                </div>
              </div>

              <!-- Submit Button -->
              <button
                type="submit"
                class="w-full bg-[#09f] text-white py-3 rounded-xl font-bold text-sm transition-all duration-300 hover:bg-[#0077cc] hover:shadow-lg hover:shadow-[#09f]/25 hover:scale-[1.01] active:scale-[0.99] cursor-pointer mt-1"
              >
                Đăng Ký Tài Khoản
              </button>
            </form>

            <!-- Mobile switcher hint -->
            <div class="md:hidden mt-5 text-center text-xs text-slate-500">
              Đã có tài khoản? 
              <button @click="activeTab = 'login'" class="text-[#09f] font-bold hover:underline">
                Đăng nhập ngay
              </button>
            </div>
          </div>
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
    const data = await $fetch<any>('http://localhost:8080/auth/login', {
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
    const data = await $fetch<{ message: string }>('http://localhost:8080/auth/register', {
      method: 'POST',
      body: {
        fullName: registerData.value.fullName,
        email: registerData.value.email,
        password: registerData.value.password,
        role: 'ROLE_USER'
      }
    })

    success('Đăng ký thành công!', data.message)
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