<template>
  <div class="flex flex-col min-h-screen">
    <!-- Notification Container -->
    <NotificationContainer />
    <ConfirmModal />
    <AlertModal />
    <ChatWidget />
    
    <!-- Header -->
    <header class="border-b border-gray-200 sticky top-0 z-50 backdrop-blur-sm bg-white/95">
      <div class="w-[90%] max-w-[1920px] mx-auto px-2 sm:px-4 lg:px-6">
        <!-- Top Row: Logo, Search, Auth -->
        <div class="flex items-center justify-between gap-4 py-4">
          <!-- Logo -->
          <NuxtLink to="/" class="flex items-center gap-2 group flex-shrink-0">
            <img 
              src="/images/Sub_Edunera_Logo_512.svg" 
              alt="Electronic Components Logo" 
              class="w-10 h-10 transition-all duration-300 group-hover:scale-110"
            />
            <span class="font-bold text-xl text-gray-800 hidden sm:block">ECShop</span>
          </NuxtLink>

          <!-- Search Bar -->
          <div class="flex-1 max-w-3xl">
            <div class="relative">
              <input
                v-model="searchQuery"
                type="text"
                placeholder="Tìm kiếm linh kiện điện tử..."
                class="w-full px-4 py-2.5 pr-12 border-2 border-gray-200 rounded-xl text-sm transition-all duration-300 outline-none focus:border-[#09f] focus:shadow-[0_0_0_3px_rgba(0,153,255,0.1)] hover:border-[#33adff] placeholder:text-gray-400"
                role="combobox"
                aria-autocomplete="list"
                :aria-expanded="showSearchSuggestions && filteredSearchSuggestions.length > 0"
                @focus="openSearchSuggestions"
                @input="openSearchSuggestions"
                @keydown.esc="showSearchSuggestions = false"
                @keyup.enter="handleSearch"
              />
              <button
                @click="handleSearch"
                class="absolute right-2 top-1/2 -translate-y-1/2 w-8 h-8 bg-[#09f] rounded-lg flex items-center justify-center text-white transition-all duration-300 hover:bg-[#0077cc] hover:scale-105 active:scale-95"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              </button>
              <div
                v-if="showSearchSuggestions && filteredSearchSuggestions.length"
                role="listbox"
                class="absolute left-0 right-0 top-full z-50 mt-2 overflow-hidden rounded-xl border border-gray-200 bg-white py-2 shadow-xl"
              >
                <p class="px-4 pb-1 text-xs font-semibold uppercase tracking-wide text-gray-400">
                  Gợi ý tìm kiếm
                </p>
                <button
                  v-for="suggestion in filteredSearchSuggestions"
                  :key="suggestion.query.toLowerCase()"
                  type="button"
                  role="option"
                  class="flex w-full items-center justify-between px-4 py-2 text-left text-sm text-gray-700 hover:bg-blue-50 hover:text-[#0077cc]"
                  @mousedown.prevent
                  @click="selectSearchSuggestion(suggestion.query)"
                >
                  <span class="flex items-center gap-3">
                    <svg class="h-4 w-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                      <circle cx="11" cy="11" r="7" stroke-width="2" />
                      <path stroke-linecap="round" stroke-width="2" d="m20 20-4-4" />
                    </svg>
                    {{ suggestion.query }}
                  </span>
                  <span v-if="suggestion.count" class="text-xs text-gray-400">
                    {{ suggestion.count }} lượt tìm
                  </span>
                </button>
              </div>
            </div>
          </div>

          <!-- Cart & Auth -->
          <div class="flex items-center gap-3 flex-shrink-0">
            <!-- Cart Icon -->
            <NuxtLink 
              to="/cart" 
              class="relative group"
            >
              <div class="w-10 h-10 rounded-xl bg-gray-100 flex items-center justify-center transition-all duration-300 group-hover:bg-[#09f] group-hover:scale-105">
                <svg class="w-5 h-5 text-gray-600 group-hover:text-white transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
              </div>
              <!-- Cart Badge -->
              <ClientOnly>
                <span 
                  v-if="cartItemCount > 0"
                  class="absolute -top-1 -right-1 w-5 h-5 bg-red-500 text-white text-xs font-bold rounded-full flex items-center justify-center border-2 border-white"
                >
                  {{ cartItemCount > 9 ? '9+' : cartItemCount }}
                </span>
              </ClientOnly>
            </NuxtLink>

            <!-- User Menu or Login Button -->
            <ClientOnly>
              <template #fallback>
                <!-- Empty placeholder div with same height to prevent layout shift -->
                <div class="h-10 w-24"></div>
              </template>
              
              <div v-if="isLoggedIn" class="relative">
                <button
                  @click="showUserMenu = !showUserMenu"
                  class="flex items-center gap-2 px-3 py-2 rounded-xl bg-gray-100 hover:bg-gray-200 transition-all duration-200"
                >
                  <div class="w-8 h-8 rounded-full bg-[#09f] flex items-center justify-center text-white font-semibold text-sm">
                    {{ (user?.fullName || user?.email)?.charAt(0).toUpperCase() }}
                  </div>
                  <span class="text-sm font-medium text-gray-700 hidden md:block max-w-[150px] truncate">
                    {{ user?.fullName || user?.email }}
                  </span>
                  <svg class="w-4 h-4 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                  </svg>
                </button>

                <!-- Dropdown Menu -->
                <Transition
                  enter-active-class="transition-all duration-200 ease-out"
                  leave-active-class="transition-all duration-150 ease-in"
                  enter-from-class="opacity-0 scale-95 translate-y-[-10px]"
                  enter-to-class="opacity-100 scale-100 translate-y-0"
                  leave-from-class="opacity-100 scale-100 translate-y-0"
                  leave-to-class="opacity-0 scale-95 translate-y-[-10px]"
                >
                  <div
                    v-if="showUserMenu"
                    class="absolute right-0 mt-2 w-56 bg-white rounded-xl shadow-lg border border-gray-200 py-2 z-50"
                  >
                    <div class="px-4 py-2 border-b border-gray-100">
                      <p class="text-xs text-gray-500">Xin chào</p>
                      <p class="text-sm font-semibold text-gray-800 truncate">{{ user?.fullName || user?.email }}</p>
                      <p v-if="user?.fullName" class="text-xs text-gray-500 truncate">{{ user?.email }}</p>
                      <p v-if="user?.role" class="text-xs text-[#09f] font-medium mt-1">
                        {{ user.role === 'ROLE_ADMIN' ? 'Quản trị viên' : 'Khách hàng' }}
                      </p>
                    </div>
                    
                    <!-- Admin Management Link -->
                    <NuxtLink
                      v-if="user?.role === 'ROLE_ADMIN'"
                      to="/admin"
                      class="flex items-center gap-3 px-4 py-2 text-sm text-[#09f] font-medium hover:bg-blue-50 transition-colors border-b border-gray-100"
                      @click="showUserMenu = false"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      </svg>
                      Quản lý cửa hàng
                    </NuxtLink>
                    
                    <NuxtLink
                      to="/profile"
                      class="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                      @click="showUserMenu = false"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                      Tài khoản của tôi
                    </NuxtLink>
                    <NuxtLink
                      to="/orders"
                      class="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                      @click="showUserMenu = false"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
                      </svg>
                      Đơn hàng của tôi
                    </NuxtLink>
                    <NuxtLink
                      to="/warranty"
                      class="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                      @click="showUserMenu = false"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                      </svg>
                      Bảo hành linh kiện
                    </NuxtLink>
                    <button
                      @click="handleLogout"
                      class="flex items-center gap-3 w-full px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                      </svg>
                      Đăng xuất
                    </button>
                  </div>
                </Transition>
              </div>

              <NuxtLink
                v-else
                to="/auth/auth" 
                class="px-4 py-2 text-sm font-medium text-[#09f] hover:text-[#0077cc] transition-colors duration-200 whitespace-nowrap"
              >
                Đăng nhập
              </NuxtLink>
            </ClientOnly>
          </div>
        </div>

        <!-- Popular Keywords -->
        <div class="pb-3 border-t border-gray-100 pt-3">
          <div class="flex items-center gap-2 flex-wrap">
            <span class="text-xs text-gray-500 whitespace-nowrap">Từ khóa phổ biến:</span>
            <button
              v-for="keyword in popularKeywords"
              :key="keyword"
              @click="searchQuery = keyword; handleSearch()"
              class="px-3 py-1 bg-gray-100 text-gray-700 text-xs rounded-full transition-all duration-200 hover:bg-[#09f] hover:text-white hover:scale-105"
            >
              {{ keyword }}
            </button>
          </div>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="flex-grow">
      <NuxtPage />
    </main>

    <!-- Footer -->
    <footer class="bg-gray-50 border-t border-gray-200 mt-auto">
      <div class="w-[90%] max-w-[1920px] mx-auto px-2 sm:px-4 lg:px-6 py-12">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
          <!-- Company Info -->
          <div class="col-span-1 md:col-span-2">
            <div class="flex items-center gap-2 mb-4">
              <img 
                src="/images/Sub_Edunera_Logo_512.svg" 
                alt="Electronic Components Logo" 
                class="w-10 h-10"
              />
              <span class="font-bold text-lg text-gray-800">ECShop</span>
            </div>
            <p class="text-sm text-gray-600 mb-4">
              Cung cấp các linh kiện điện tử chất lượng cao với giá cả cạnh tranh. Đội ngũ chuyên nghiệp, giao hàng nhanh chóng trên toàn quốc.
            </p>
            <div class="flex gap-3">
              <a href="#" class="w-9 h-9 flex items-center justify-center rounded-lg bg-gray-200 text-gray-600 hover:bg-[#09f] hover:text-white transition-all duration-300">
                <span class="text-lg">f</span>
              </a>
              <a href="#" class="w-9 h-9 flex items-center justify-center rounded-lg bg-gray-200 text-gray-600 hover:bg-[#09f] hover:text-white transition-all duration-300">
                <span class="text-lg">in</span>
              </a>
              <a href="#" class="w-9 h-9 flex items-center justify-center rounded-lg bg-gray-200 text-gray-600 hover:bg-[#09f] hover:text-white transition-all duration-300">
                <span class="text-lg">@</span>
              </a>
            </div>
          </div>

          <!-- Quick Links -->
          <div>
            <h3 class="font-semibold text-gray-800 mb-4">Liên kết nhanh</h3>
            <ul class="space-y-2">
              <li>
                <NuxtLink to="/" class="text-sm text-gray-600 hover:text-[#09f] transition-colors duration-200">Trang chủ</NuxtLink>
              </li>
              <li>
                <NuxtLink to="/search" class="text-sm text-gray-600 hover:text-[#09f] transition-colors duration-200">Tìm kiếm</NuxtLink>
              </li>
              <li>
                <NuxtLink to="/cart" class="text-sm text-gray-600 hover:text-[#09f] transition-colors duration-200">Giỏ hàng</NuxtLink>
              </li>
              <li>
                <NuxtLink to="/profile" class="text-sm text-gray-600 hover:text-[#09f] transition-colors duration-200">Tài khoản</NuxtLink>
              </li>
            </ul>
          </div>

          <!-- Support -->
          <div>
            <h3 class="font-semibold text-gray-800 mb-4">Hỗ trợ</h3>
            <ul class="space-y-2">
              <li>
                <NuxtLink to="/warranty" class="text-sm text-gray-600 hover:text-[#09f] transition-colors duration-200">Chính sách & Tra cứu bảo hành</NuxtLink>
              </li>
              <li>
                <a href="#" class="text-sm text-gray-600 hover:text-[#09f] transition-colors duration-200">Chính sách đổi trả</a>
              </li>
              <li>
                <a href="#" class="text-sm text-gray-600 hover:text-[#09f] transition-colors duration-200">Phương thức thanh toán</a>
              </li>
              <li>
                <a href="#" class="text-sm text-gray-600 hover:text-[#09f] transition-colors duration-200">Câu hỏi thường gặp</a>
              </li>
            </ul>
          </div>
        </div>

        <!-- Copyright -->
        <div class="border-t border-gray-200 mt-8 pt-8 text-center">
          <p class="text-sm text-gray-600">
            © {{ new Date().getFullYear() }} Electronic Components. All rights reserved.
          </p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuth } from '~/composables/useAuth'
import { useNotification } from '~/composables/useNotification'
import { useCart } from '~/composables/useCart'

const searchQuery = ref('')
const showUserMenu = ref(false)
const showSearchSuggestions = ref(false)
const searchHistory = ref<{ query: string; count: number }[]>([])

const { user, isAuthenticated, logout, initializeAuth } = useAuth()
const { success } = useNotification()
const { cartItemCount, fetchCart } = useCart()

// Force initialize on mount
onMounted(() => {
  initializeAuth()
  loadSearchHistory()
  // Fetch cart (both authenticated and guest)
  fetchCart()
})

const isLoggedIn = computed(() => isAuthenticated.value)

const handleLogout = () => {
  logout()
  showUserMenu.value = false
  success('Đăng xuất thành công', 'Hẹn gặp lại bạn!')
  navigateTo('/')
}

const popularKeywords = [
  'Arduino',
  'ESP32',
  'Raspberry Pi',
  'IC 74LS',
  'LED',
  'Cảm biến',
  'Motor',
  'Relay'
]

const filteredSearchSuggestions = computed(() => {
  const query = searchQuery.value.trim().toLocaleLowerCase()
  if (!query) return []

  const suggestions = new Map<string, { query: string; count: number }>()
  for (const item of searchHistory.value) {
    if (item.query.toLocaleLowerCase().includes(query)) {
      suggestions.set(item.query.toLocaleLowerCase(), item)
    }
  }
  for (const keyword of popularKeywords) {
    const normalized = keyword.toLocaleLowerCase()
    if (normalized.includes(query) && !suggestions.has(normalized)) {
      suggestions.set(normalized, { query: keyword, count: 0 })
    }
  }

  return [...suggestions.values()]
    .sort((a, b) => b.count - a.count)
    .slice(0, 6)
})

const loadSearchHistory = () => {
  try {
    const stored = localStorage.getItem('daln_search_terms')
    const parsed: unknown = stored ? JSON.parse(stored) : []
    if (Array.isArray(parsed)) {
      searchHistory.value = parsed.filter((item): item is { query: string; count: number } =>
        typeof item?.query === 'string' &&
        item.query.trim().length > 0 &&
        typeof item.count === 'number'
      )
    }
  } catch (error) {
    console.error('Could not load search history:', error)
    searchHistory.value = []
  }
}

const openSearchSuggestions = () => {
  loadSearchHistory()
  showSearchSuggestions.value = true
}

const selectSearchSuggestion = (query: string) => {
  searchQuery.value = query
  showSearchSuggestions.value = false
  handleSearch()
}

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    showSearchSuggestions.value = false
    navigateTo(`/search?q=${encodeURIComponent(searchQuery.value.trim())}`)
  }
}
</script>