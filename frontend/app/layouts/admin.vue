<template>
  <div class="flex flex-col min-h-screen bg-gray-50/60 text-gray-800 antialiased">
    <!-- Popups / Modals toàn cục -->
    <NotificationContainer />
    <ConfirmModal />
    <AlertModal />
    <ChatWidget />

    <!-- ==================== TOP HEADER (ĐỒNG NHẤT VỚI TEMPLATE GỐC CỦA SHOP) ==================== -->
    <header class="border-b border-gray-200 sticky top-0 z-50 backdrop-blur-sm bg-white/95">
      <div class="w-[90%] max-w-[1920px] mx-auto px-2 sm:px-4 lg:px-6">
        <!-- Top Row: Logo, Search, Cart, Auth Menu -->
        <div class="flex items-center justify-between gap-4 py-4">
          <!-- Logo -->
          <NuxtLink to="/" class="flex items-center gap-2 group flex-shrink-0">
            <img
              src="/images/Sub_Edunera_Logo_512.svg"
              alt="Electronic Components Logo"
              class="w-10 h-10 transition-all duration-300 group-hover:scale-110"
            />
            <div class="flex items-center gap-1.5">
              <span class="font-bold text-xl text-gray-800 hidden sm:block">ECShop</span>
              <span class="text-[10px] font-extrabold uppercase tracking-wider px-2.5 py-0.5 rounded-full bg-[#09f] text-white shadow-xs">
                Admin
              </span>
            </div>
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
                class="absolute right-2 top-1/2 -translate-y-1/2 w-8 h-8 bg-[#09f] rounded-lg flex items-center justify-center text-white transition-all duration-300 hover:bg-[#0077cc] hover:scale-105 active:scale-95 cursor-pointer"
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

          <!-- Cart & Auth Menu -->
          <div class="flex items-center gap-3 flex-shrink-0">
            <!-- Cart Icon -->
            <NuxtLink to="/cart" class="relative group">
              <div class="w-10 h-10 rounded-xl bg-gray-100 flex items-center justify-center transition-all duration-300 group-hover:bg-[#09f] group-hover:scale-105">
                <svg class="w-5 h-5 text-gray-600 group-hover:text-white transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
              </div>
              <ClientOnly>
                <span
                  v-if="cartItemCount > 0"
                  class="absolute -top-1 -right-1 w-5 h-5 bg-red-500 text-white text-xs font-bold rounded-full flex items-center justify-center border-2 border-white"
                >
                  {{ cartItemCount > 9 ? '9+' : cartItemCount }}
                </span>
              </ClientOnly>
            </NuxtLink>

            <!-- User Menu -->
            <ClientOnly>
              <template #fallback>
                <div class="h-10 w-24"></div>
              </template>

              <div v-if="isLoggedIn" class="relative">
                <button
                  @click="showUserMenu = !showUserMenu"
                  class="flex items-center gap-2 px-3 py-2 rounded-xl bg-gray-100 hover:bg-gray-200 transition-all duration-200 cursor-pointer"
                >
                  <div class="w-8 h-8 rounded-full bg-gradient-to-tr from-[#09f] to-[#0077cc] flex items-center justify-center text-white font-semibold text-sm">
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
                      <p class="text-xs text-[#09f] font-semibold mt-1">
                        Quản trị viên
                      </p>
                    </div>

                    <NuxtLink
                      to="/"
                      class="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                      @click="showUserMenu = false"
                    >
                      <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
                      </svg>
                      Xem trang cửa hàng
                    </NuxtLink>

                    <NuxtLink
                      to="/profile"
                      class="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                      @click="showUserMenu = false"
                    >
                      <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                      Tài khoản của tôi
                    </NuxtLink>

                    <button
                      @click="handleLogout"
                      class="flex items-center gap-3 w-full px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors cursor-pointer"
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

        <!-- Popular Keywords row -->
        <div class="pb-3 border-t border-gray-100 pt-3">
          <div class="flex items-center gap-2 flex-wrap">
            <span class="text-xs text-gray-500 whitespace-nowrap">Từ khóa phổ biến:</span>
            <button
              v-for="keyword in popularKeywords"
              :key="keyword"
              @click="searchQuery = keyword; handleSearch()"
              class="px-3 py-1 bg-gray-100 text-gray-700 text-xs rounded-full transition-all duration-200 hover:bg-[#09f] hover:text-white hover:scale-105 cursor-pointer"
            >
              {{ keyword }}
            </button>
          </div>
        </div>
      </div>
    </header>

    <!-- ==================== MAIN CONTENT: KHUNG QUẢN TRỊ KÈM CỘT MENU TRÁI ==================== -->
    <main class="flex-grow py-6 sm:py-8">
      <div class="w-[90%] max-w-[1920px] mx-auto px-2 sm:px-4 lg:px-6 space-y-6">

        <!-- Banner thông tin định vị trang Admin (Light theme, hài hòa với website) -->
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 bg-white p-4 sm:p-5 rounded-2xl border border-gray-200/80 shadow-xs">
          <div class="flex items-center gap-3">
            <div class="w-11 h-11 rounded-xl bg-gradient-to-tr from-[#09f] to-[#0077cc] text-white flex items-center justify-center shadow-md shadow-[#09f]/20 shrink-0">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </div>
            <div>
              <div class="flex items-center gap-2">
                <h2 class="text-base sm:text-lg font-bold text-gray-900">Quản Trị Cửa Hàng</h2>
                <span class="text-[10px] font-extrabold uppercase tracking-wider px-2 py-0.5 rounded-full bg-blue-50 text-[#09f] border border-blue-200">
                  {{ activeTabTitle }}
                </span>
              </div>
              <p class="text-xs text-gray-500 mt-0.5">Hệ thống điều hành danh mục, sản phẩm, đơn hàng và phân tích kinh doanh</p>
            </div>
          </div>

          <div class="flex items-center gap-2.5 self-start sm:self-auto">
            <NuxtLink
              to="/admin/products?action=create"
              class="inline-flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-[#09f] to-[#0077cc] hover:from-[#33adff] hover:to-[#0088ea] text-white text-xs font-bold rounded-xl shadow-md shadow-[#09f]/20 hover:shadow-lg hover:shadow-[#09f]/30 active:scale-95 transition-all cursor-pointer"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M12 4v16m8-8H4" />
              </svg>
              <span>Thêm Sản Phẩm Mới</span>
            </NuxtLink>

            <NuxtLink
              to="/"
              class="inline-flex items-center gap-1.5 px-3 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 text-xs font-semibold rounded-xl transition-colors"
            >
              <svg class="w-3.5 h-3.5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
              </svg>
              <span>Về Trang Chủ</span>
            </NuxtLink>
          </div>
        </div>

        <!-- Layout 2 Cột: Cột Menu Nhỏ Nhỏ Bên Trái + Khu Vực Nội Dung Trang Bên Phải -->
        <div class="flex flex-col lg:flex-row gap-6 items-start">

          <!-- ==================== CỘT MENU NHỎ GỌN BÊN TRÁI (TABS CHUYỂN TRANG) ==================== -->
          <aside class="w-full lg:w-60 xl:w-64 shrink-0 bg-white rounded-2xl border border-gray-200/80 p-3 shadow-xs lg:sticky lg:top-28">
            <p class="px-3 pt-1 pb-2 text-[10px] font-bold text-gray-400 uppercase tracking-wider hidden lg:block">
              Danh Mục Quản Trị
            </p>

            <!-- Menu List (Dạng cột trên desktop, có thể cuộn ngang nhẹ nhàng trên mobile) -->
            <nav class="flex lg:flex-col overflow-x-auto lg:overflow-visible gap-1.5 p-1 lg:p-0 scrollbar-none">
              <!-- 1. Dashboard -->
              <NuxtLink
                to="/admin"
                class="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-semibold transition-all whitespace-nowrap cursor-pointer shrink-0 lg:shrink"
                :class="route.path === '/admin' ? 'bg-[#09f] text-white shadow-md shadow-[#09f]/25 font-bold' : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'"
              >
                <svg class="w-4 h-4 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
                <span>Dashboard Thống Kê</span>
              </NuxtLink>

              <!-- 2. Sản phẩm -->
              <NuxtLink
                to="/admin/products"
                class="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-semibold transition-all whitespace-nowrap cursor-pointer shrink-0 lg:shrink"
                :class="route.path.startsWith('/admin/products') ? 'bg-[#09f] text-white shadow-md shadow-[#09f]/25 font-bold' : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'"
              >
                <svg class="w-4 h-4 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                </svg>
                <span>Quản Lý Sản Phẩm</span>
              </NuxtLink>

              <!-- 3. Danh mục -->
              <NuxtLink
                to="/admin/categories"
                class="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-semibold transition-all whitespace-nowrap cursor-pointer shrink-0 lg:shrink"
                :class="route.path.startsWith('/admin/categories') ? 'bg-[#09f] text-white shadow-md shadow-[#09f]/25 font-bold' : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'"
              >
                <svg class="w-4 h-4 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                </svg>
                <span>Danh Mục Linh Kiện</span>
              </NuxtLink>

              <!-- 4. Đơn hàng -->
              <NuxtLink
                to="/admin/orders"
                class="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-semibold transition-all whitespace-nowrap cursor-pointer shrink-0 lg:shrink"
                :class="route.path.startsWith('/admin/orders') ? 'bg-[#09f] text-white shadow-md shadow-[#09f]/25 font-bold' : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'"
              >
                <svg class="w-4 h-4 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
                <span>Đơn Hàng & Vận Chuyển</span>
              </NuxtLink>

              <!-- 5. Người dùng -->
              <NuxtLink
                to="/admin/users"
                class="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-semibold transition-all whitespace-nowrap cursor-pointer shrink-0 lg:shrink"
                :class="route.path.startsWith('/admin/users') ? 'bg-[#09f] text-white shadow-md shadow-[#09f]/25 font-bold' : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'"
              >
                <svg class="w-4 h-4 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
                <span>Quản Lý Người Dùng</span>
              </NuxtLink>

              <!-- 6. Quản lý Bảo hành -->
              <NuxtLink
                to="/admin/warranties"
                class="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-semibold transition-all whitespace-nowrap cursor-pointer shrink-0 lg:shrink"
                :class="route.path.startsWith('/admin/warranties') ? 'bg-[#09f] text-white shadow-md shadow-[#09f]/25 font-bold' : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'"
              >
                <svg class="w-4 h-4 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                </svg>
                <span>Quản Lý Bảo Hành</span>
              </NuxtLink>
            </nav>
          </aside>

          <!-- ==================== KHU VỰC NỘI DUNG CHÍNH (NẰM BÊN PHẢI CỘT MENU) ==================== -->
          <div class="flex-1 min-w-0 w-full">
            <slot />
          </div>

        </div>

      </div>
    </main>

    <!-- ==================== FOOTER (ĐỒNG NHẤT VỚI TEMPLATE GỐC CỦA SHOP) ==================== -->
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
import { useRoute } from 'vue-router'
import { useAuth } from '~/composables/useAuth'
import { useNotification } from '~/composables/useNotification'
import { useCart } from '~/composables/useCart'

const route = useRoute()
const searchQuery = ref('')
const showUserMenu = ref(false)
const showSearchSuggestions = ref(false)
const searchHistory = ref<{ query: string; count: number }[]>([])

const { user, isAuthenticated, logout, initializeAuth } = useAuth()
const { success } = useNotification()
const { cartItemCount, fetchCart } = useCart()

onMounted(() => {
  initializeAuth()
  loadSearchHistory()
  if (isAuthenticated.value) {
    fetchCart()
  }
})

const isLoggedIn = computed(() => isAuthenticated.value)

const activeTabTitle = computed(() => {
  const p = route.path
  if (p === '/admin') return 'Dashboard'
  if (p.startsWith('/admin/products')) return 'Sản Phẩm'
  if (p.startsWith('/admin/categories')) return 'Danh Mục'
  if (p.startsWith('/admin/orders')) return 'Đơn Hàng'
  if (p.startsWith('/admin/users')) return 'Người Dùng'
  return 'Quản Trị'
})

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
