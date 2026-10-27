<template>
  <div class="min-h-screen bg-gray-50/60 py-8 lg:py-12">
    <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
      
      <!-- Breadcrumb & Header -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-[#09f]/10 text-[#09f] text-xs font-semibold mb-3">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
          </svg>
          Hệ Thống Bảo Hành Điện Tử ECShop
        </div>
        <h1 class="text-2xl sm:text-3xl font-extrabold text-gray-900 tracking-tight">
          Tra Cứu Bảo Hành & Hỗ Trợ Kỹ Thuật
        </h1>
        <p class="mt-2 text-sm text-gray-600 max-w-2xl mx-auto">
          Kiểm tra thời hạn bảo hành linh kiện theo Mã tem Serial hoặc tra cứu tiến độ xử lý phiếu bảo hành trực tuyến.
        </p>
      </div>

      <!-- Navigation Tabs -->
      <div class="flex items-center justify-center mb-8">
        <div class="inline-flex p-1 bg-white border border-gray-200 rounded-2xl shadow-sm">
          <button
            type="button"
            @click="activeTab = 'lookup'"
            class="flex items-center gap-2 px-5 py-2.5 rounded-xl text-xs sm:text-sm font-semibold transition-all cursor-pointer"
            :class="activeTab === 'lookup' ? 'bg-[#09f] text-white shadow-md shadow-[#09f]/25' : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            Tra Cứu Mã Serial
          </button>

          <button
            type="button"
            @click="activeTab = 'claim-status'"
            class="flex items-center gap-2 px-5 py-2.5 rounded-xl text-xs sm:text-sm font-semibold transition-all cursor-pointer"
            :class="activeTab === 'claim-status' ? 'bg-[#09f] text-white shadow-md shadow-[#09f]/25' : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
            Tiến Độ Phiếu Bảo Hành
          </button>

          <button
            type="button"
            @click="activeTab = 'policy'"
            class="flex items-center gap-2 px-5 py-2.5 rounded-xl text-xs sm:text-sm font-semibold transition-all cursor-pointer"
            :class="activeTab === 'policy' ? 'bg-[#09f] text-white shadow-md shadow-[#09f]/25' : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            Chính Sách & Quy Định
          </button>
        </div>
      </div>

      <!-- TAB 1: TRA CỨU SERIAL & GỬI YÊU CẦU -->
      <div v-if="activeTab === 'lookup'" class="space-y-6">
        <!-- Search Card -->
        <div class="bg-white rounded-2xl border border-gray-200/80 shadow-sm p-6 sm:p-8">
          <form @submit.prevent="handleLookupSerial" class="space-y-4">
            <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div class="sm:col-span-2">
                <label class="block text-xs font-bold text-gray-700 uppercase tracking-wider mb-2">
                  Mã Serial / Tem Bảo Hành <span class="text-red-500">*</span>
                </label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-gray-400">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 11h.01M7 15h.01M11 7h.01M11 11h.01M11 15h.01M15 7h.01M15 11h.01M15 15h.01" />
                    </svg>
                  </div>
                  <input
                    v-model="lookupInput.serialNumber"
                    type="text"
                    required
                    placeholder="Ví dụ: SN-2610-8910 hoặc SN-202610-001"
                    class="w-full pl-10 pr-4 py-3 rounded-xl border border-gray-300 focus:border-[#09f] focus:ring-2 focus:ring-[#09f]/20 outline-none text-sm font-mono uppercase tracking-wide transition-all"
                  />
                </div>
              </div>

              <div>
                <label class="block text-xs font-bold text-gray-700 uppercase tracking-wider mb-2">
                  Số Điện Thoại (Tùy chọn)
                </label>
                <input
                  v-model="lookupInput.phone"
                  type="text"
                  placeholder="0912xxx..."
                  class="w-full px-4 py-3 rounded-xl border border-gray-300 focus:border-[#09f] focus:ring-2 focus:ring-[#09f]/20 outline-none text-sm transition-all"
                />
              </div>
            </div>

            <div class="flex items-center justify-between pt-2">
              <div class="text-xs text-gray-500">
                Tem bảo hành được dán trực tiếp trên bao bì hoặc lưng bo mạch linh kiện.
              </div>
              <button
                type="submit"
                :disabled="searchingSerial"
                class="inline-flex items-center gap-2 px-6 py-3 bg-[#09f] hover:bg-[#0088ea] text-white text-sm font-bold rounded-xl shadow-md shadow-[#09f]/20 active:scale-95 transition-all cursor-pointer disabled:opacity-50"
              >
                <span v-if="searchingSerial" class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
                <span>{{ searchingSerial ? 'Đang tra cứu...' : 'Tra Cứu Ngay' }}</span>
              </button>
            </div>
          </form>

          <!-- Error Alert -->
          <div v-if="lookupError" class="mt-4 p-4 rounded-xl bg-red-50 border border-red-200 flex items-start gap-3">
            <svg class="w-5 h-5 text-red-500 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <div class="text-xs sm:text-sm text-red-700">
              {{ lookupError }}
            </div>
          </div>
        </div>

        <!-- Serial Detail Card (when found) -->
        <div v-if="serialData" class="bg-white rounded-2xl border border-gray-200/80 shadow-sm overflow-hidden">
          <!-- Status Banner -->
          <div
            class="px-6 py-4 flex flex-wrap items-center justify-between gap-4"
            :class="serialData.isExpired ? 'bg-red-50/80 border-b border-red-100' : 'bg-emerald-50/80 border-b border-emerald-100'"
          >
            <div class="flex items-center gap-3">
              <div
                class="w-10 h-10 rounded-xl flex items-center justify-center font-bold text-lg"
                :class="serialData.isExpired ? 'bg-red-100 text-red-600' : 'bg-emerald-100 text-emerald-600'"
              >
                {{ serialData.isExpired ? '✕' : '✓' }}
              </div>
              <div>
                <div class="text-xs font-bold uppercase tracking-wider" :class="serialData.isExpired ? 'text-red-700' : 'text-emerald-700'">
                  {{ serialData.isExpired ? 'Đã Hết Hạn Bảo Hành' : 'Đang Trong Hạn Bảo Hành' }}
                </div>
                <div class="text-sm font-semibold text-gray-900">
                  Mã Tem: <span class="font-mono">{{ serialData.serialNumber }}</span>
                </div>
              </div>
            </div>

            <div class="text-right">
              <span
                class="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider"
                :class="serialData.isExpired ? 'bg-red-100 text-red-700' : 'bg-emerald-100 text-emerald-700'"
              >
                {{ serialData.isExpired ? 'Hết Hạn' : `Còn lại ${serialData.daysRemaining} ngày` }}
              </span>
            </div>
          </div>

          <!-- Product & Warranty Info -->
          <div class="p-6 sm:p-8 grid grid-cols-1 md:grid-cols-3 gap-6">
            <!-- Col 1: Product Thumbnail & Name -->
            <div class="flex items-start gap-4 md:col-span-1">
              <img
                :src="serialData.productImageUrl || '/images/default-component.svg'"
                alt="Product"
                class="w-20 h-20 rounded-xl object-cover bg-gray-100 border border-gray-200 shrink-0"
                @error="$event.target.src = '/images/default-component.svg'"
              />
              <div>
                <h3 class="text-sm font-bold text-gray-900 leading-snug">
                  {{ serialData.productName }}
                </h3>
                <p v-if="serialData.variantName" class="text-xs text-gray-500 mt-1">
                  Phân loại: <span class="font-medium text-gray-700">{{ serialData.variantName }}</span>
                </p>
                <p v-if="serialData.orderCode" class="text-xs text-gray-500 mt-0.5">
                  Đơn hàng: <span class="font-mono text-[#09f] font-semibold">#{{ serialData.orderCode }}</span>
                </p>
              </div>
            </div>

            <!-- Col 2: Key Milestones -->
            <div class="md:col-span-2 grid grid-cols-2 sm:grid-cols-3 gap-4 border-t md:border-t-0 md:border-l border-gray-100 pt-4 md:pt-0 md:pl-6">
              <div class="p-3 bg-gray-50 rounded-xl">
                <span class="block text-[11px] font-medium text-gray-500 uppercase">Thời Hạn</span>
                <span class="text-sm font-bold text-gray-900">{{ serialData.warrantyMonths || 12 }} Tháng</span>
              </div>
              <div class="p-3 bg-gray-50 rounded-xl">
                <span class="block text-[11px] font-medium text-gray-500 uppercase">Ngày Kích Hoạt</span>
                <span class="text-sm font-semibold text-gray-900">{{ formatDate(serialData.activatedAt) }}</span>
              </div>
              <div class="p-3 bg-gray-50 rounded-xl">
                <span class="block text-[11px] font-medium text-gray-500 uppercase">Ngày Hết Hạn</span>
                <span class="text-sm font-semibold" :class="serialData.isExpired ? 'text-red-600 font-bold' : 'text-emerald-600 font-bold'">
                  {{ formatDate(serialData.expiresAt) }}
                </span>
              </div>
            </div>
          </div>

          <!-- Action & Claim CTA -->
          <div class="px-6 py-4 bg-gray-50 border-t border-gray-100 flex flex-wrap items-center justify-between gap-4">
            <div class="text-xs text-gray-600">
              Linh kiện gặp sự cố chập chờn hoặc không nhận diện? Bạn có thể gửi yêu cầu bảo hành ngay.
            </div>

            <button
              v-if="!serialData.isExpired && serialData.status !== 'WARRANTY_SERVICING'"
              type="button"
              @click="openClaimModal"
              class="inline-flex items-center gap-2 px-5 py-2.5 bg-gradient-to-r from-[#09f] to-[#0077cc] hover:from-[#33adff] hover:to-[#0088ea] text-white text-xs font-bold rounded-xl shadow-md shadow-[#09f]/20 active:scale-95 transition-all cursor-pointer"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              Gửi Yêu Cầu Bảo Hành
            </button>

            <span
              v-else-if="serialData.status === 'WARRANTY_SERVICING'"
              class="text-xs font-bold text-amber-600 bg-amber-50 px-3 py-1.5 rounded-lg border border-amber-200"
            >
              ⏳ Linh kiện đang trong quy trình xử lý bảo hành
            </span>

            <span
              v-else
              class="text-xs font-medium text-gray-500"
            >
              Linh kiện đã hết hạn bảo hành tiêu chuẩn
            </span>
          </div>

          <!-- Claim History for this Serial -->
          <div v-if="serialData.claimHistory && serialData.claimHistory.length" class="p-6 border-t border-gray-100">
            <h4 class="text-xs font-bold uppercase tracking-wider text-gray-600 mb-3">Lịch Sử Phiếu Bảo Hành</h4>
            <div class="space-y-2">
              <div
                v-for="c in serialData.claimHistory"
                :key="c.id"
                class="p-3.5 rounded-xl border border-gray-200/80 bg-white flex flex-wrap items-center justify-between gap-3 text-xs"
              >
                <div>
                  <span class="font-mono font-bold text-[#09f]">#{{ c.claimCode }}</span>
                  <span class="text-gray-500 ml-2">({{ formatDate(c.createdAt) }})</span>
                  <p class="text-gray-700 mt-1 line-clamp-1">Mô tả: {{ c.issueDescription }}</p>
                </div>
                <div class="flex items-center gap-2">
                  <span
                    class="px-2.5 py-1 rounded-md text-[11px] font-bold uppercase"
                    :class="getStatusBadgeClass(c.status)"
                  >
                    {{ getStatusLabel(c.status) }}
                  </span>
                  <button
                    type="button"
                    @click="viewClaimDetail(c.claimCode)"
                    class="text-[#09f] hover:underline font-semibold"
                  >
                    Xem chi tiết →
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- TAB 2: TIẾN ĐỘ PHIẾU BẢO HÀNH -->
      <div v-if="activeTab === 'claim-status'" class="space-y-6">
        <div class="bg-white rounded-2xl border border-gray-200/80 shadow-sm p-6 sm:p-8">
          <form @submit.prevent="handleLookupClaim" class="space-y-4">
            <div>
              <label class="block text-xs font-bold text-gray-700 uppercase tracking-wider mb-2">
                Mã Phiếu Bảo Hành <span class="text-red-500">*</span>
              </label>
              <div class="flex gap-3">
                <input
                  v-model="claimSearchCode"
                  type="text"
                  required
                  placeholder="Ví dụ: WR-2610-8472"
                  class="flex-1 px-4 py-3 rounded-xl border border-gray-300 focus:border-[#09f] focus:ring-2 focus:ring-[#09f]/20 outline-none text-sm font-mono uppercase tracking-wide transition-all"
                />
                <button
                  type="submit"
                  :disabled="searchingClaim"
                  class="px-6 py-3 bg-[#09f] hover:bg-[#0088ea] text-white text-sm font-bold rounded-xl shadow-md shadow-[#09f]/20 active:scale-95 transition-all cursor-pointer disabled:opacity-50"
                >
                  {{ searchingClaim ? 'Đang tìm...' : 'Kiểm Tra' }}
                </button>
              </div>
            </div>
          </form>

          <!-- Claim Error -->
          <div v-if="claimError" class="mt-4 p-4 rounded-xl bg-red-50 border border-red-200 text-xs text-red-700">
            {{ claimError }}
          </div>
        </div>

        <!-- Claim Result Detail -->
        <div v-if="claimData" class="bg-white rounded-2xl border border-gray-200/80 shadow-sm p-6 sm:p-8 space-y-6">
          <div class="flex flex-wrap items-center justify-between gap-4 border-b border-gray-100 pb-4">
            <div>
              <span class="text-xs text-gray-500 font-medium">Phiếu Bảo Hành</span>
              <h3 class="text-xl font-mono font-bold text-gray-900">#{{ claimData.claimCode }}</h3>
            </div>
            <span
              class="px-3.5 py-1.5 rounded-full text-xs font-bold uppercase tracking-wider"
              :class="getStatusBadgeClass(claimData.status)"
            >
              {{ getStatusLabel(claimData.status) }}
            </span>
          </div>

          <!-- 3-Step Lifecycle Stepper -->
          <div class="py-4">
            <div class="grid grid-cols-3 gap-2 text-center relative">
              <!-- Step 1 -->
              <div class="flex flex-col items-center">
                <div
                  class="w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm mb-2"
                  :class="['PENDING', 'PROCESSING', 'COMPLETED', 'REJECTED'].includes(claimData.status) ? 'bg-[#09f] text-white' : 'bg-gray-100 text-gray-400'"
                >
                  1
                </div>
                <span class="text-xs font-bold text-gray-900">Tiếp Nhận</span>
                <span class="text-[11px] text-gray-500">Đã gửi yêu cầu</span>
              </div>

              <!-- Step 2 -->
              <div class="flex flex-col items-center">
                <div
                  class="w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm mb-2"
                  :class="['PROCESSING', 'COMPLETED', 'REJECTED'].includes(claimData.status) ? 'bg-[#09f] text-white' : 'bg-gray-100 text-gray-400'"
                >
                  2
                </div>
                <span class="text-xs font-bold text-gray-900">Đang Xử Lý</span>
                <span class="text-[11px] text-gray-500">Kiểm tra & Sửa/Đổi</span>
              </div>

              <!-- Step 3 -->
              <div class="flex flex-col items-center">
                <div
                  class="w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm mb-2"
                  :class="claimData.status === 'COMPLETED' ? 'bg-emerald-600 text-white' : (claimData.status === 'REJECTED' ? 'bg-red-600 text-white' : 'bg-gray-100 text-gray-400')"
                >
                  3
                </div>
                <span class="text-xs font-bold text-gray-900">
                  {{ claimData.status === 'REJECTED' ? 'Từ Chối' : 'Hoàn Tất' }}
                </span>
                <span class="text-[11px] text-gray-500">Trả hàng/Đóng phiếu</span>
              </div>
            </div>
          </div>

          <!-- Claim Details Table -->
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs bg-gray-50 p-4 rounded-xl">
            <div>
              <span class="text-gray-500 block">Linh kiện bảo hành:</span>
              <span class="font-bold text-gray-900">{{ claimData.productName }} ({{ claimData.serialNumber }})</span>
            </div>
            <div>
              <span class="text-gray-500 block">Khách hàng:</span>
              <span class="font-bold text-gray-900">{{ claimData.customerName }} - {{ claimData.customerPhone }}</span>
            </div>
            <div class="sm:col-span-2">
              <span class="text-gray-500 block">Mô tả sự cố của khách:</span>
              <p class="text-gray-800 font-medium mt-0.5 bg-white p-2.5 rounded-lg border border-gray-200">
                {{ claimData.issueDescription }}
              </p>
            </div>
            <div v-if="claimData.technicianNotes" class="sm:col-span-2">
              <span class="text-[#09f] font-bold block">Ghi chú từ Kỹ thuật viên / Giải pháp:</span>
              <p class="text-gray-900 mt-0.5 bg-blue-50/50 p-2.5 rounded-lg border border-blue-100 font-medium">
                {{ claimData.technicianNotes }}
              </p>
            </div>
            <div v-if="claimData.resolutionType" class="sm:col-span-2">
              <span class="text-gray-500">Hình thức xử lý:</span>
              <span class="font-bold ml-2 text-emerald-700">
                {{ claimData.resolutionType === 'REPLACED_NEW' ? '🔄 Đổi mới linh kiện 100%' : (claimData.resolutionType === 'REPAIRED' ? '🔧 Đã sửa chữa hoàn thiện' : 'Từ chối bảo hành') }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- TAB 3: CHÍNH SÁCH BẢO HÀNH -->
      <div v-if="activeTab === 'policy'" class="bg-white rounded-2xl border border-gray-200/80 shadow-sm p-6 sm:p-8 space-y-6">
        <h2 class="text-lg font-bold text-gray-900 flex items-center gap-2">
          <span>📜</span> Chính Sách & Điều Kiện Bảo Hành ECShop
        </h2>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 text-xs sm:text-sm">
          <!-- Điều kiện được bảo hành -->
          <div class="p-5 rounded-2xl bg-emerald-50/50 border border-emerald-100 space-y-3">
            <h3 class="font-bold text-emerald-800 flex items-center gap-2">
              <span class="text-base">✓</span> Các trường hợp được bảo hành miễn phí
            </h3>
            <ul class="list-disc list-inside space-y-1.5 text-emerald-900/90 leading-relaxed">
              <li>Sản phẩm còn nguyên tem Serial Number của ECShop, không bị rách rời, chắp vá hay tẩy xóa.</li>
              <li>Linh kiện còn trong thời hạn bảo hành tính từ ngày đơn hàng hoàn tất.</li>
              <li>Lỗi kỹ thuật xuất phát từ nhà sản xuất (hỏng IC, đứt đường mạch nội bộ, lỗi nạp nạp bootloader, cảm biến không trả dữ liệu dù cấp đúng thông số theo Datasheet).</li>
              <li>Hỗ trợ <strong>1 đổi 1 trong 30 ngày đầu</strong> nếu phát sinh lỗi phần cứng từ nhà sản xuất.</li>
            </ul>
          </div>

          <!-- Điều kiện từ chối -->
          <div class="p-5 rounded-2xl bg-red-50/50 border border-red-100 space-y-3">
            <h3 class="font-bold text-red-800 flex items-center gap-2">
              <span class="text-base">✕</span> Các trường hợp từ chối bảo hành
            </h3>
            <ul class="list-disc list-inside space-y-1.5 text-red-900/90 leading-relaxed">
              <li>Cấp ngược cực nguồn âm/dương (+/-) gây nổ tụ, cháy chip hoặc bốc khói.</li>
              <li>Cấp quá áp vượt ngưỡng cho phép (Ví dụ: Chân logic 3.3V của ESP32 cắm thẳng vào 5V hoặc 12V).</li>
              <li>Linh kiện bị rơi vỡ, nứt mẻ bo mạch, dính nước, ẩm mốc hoặc tự ý câu dây can thiệp phần cứng.</li>
              <li>Các linh kiện tiêu hao, phụ kiện không có bảo hành (điện trở, tụ điện, dây cắm testboard).</li>
            </ul>
          </div>
        </div>

        <div class="p-4 bg-gray-50 rounded-xl border border-gray-200 text-xs text-gray-600 leading-relaxed">
          <strong>Địa chỉ gửi linh kiện bảo hành:</strong> Phòng Kỹ thuật ECShop - Số 123 Đường Công Nghệ, Q. Cầu Giấy, Hà Nội. Hotline/Zalo Kỹ thuật: 0912.345.678 (8:00 - 17:30 Thứ 2 đến Thứ 7).
        </div>
      </div>
    </div>

    <!-- MODAL GỬI YÊU CẦU BẢO HÀNH -->
    <div v-if="showClaimModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs">
      <div class="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl border border-gray-200">
        <div class="flex items-center justify-between pb-4 border-b border-gray-100">
          <h3 class="text-base font-bold text-gray-900 flex items-center gap-2">
            <span>🔧</span> Tạo Phiếu Yêu Cầu Bảo Hành
          </h3>
          <button @click="showClaimModal = false" class="text-gray-400 hover:text-gray-600 text-lg cursor-pointer">✕</button>
        </div>

        <form @submit.prevent="submitClaimForm" class="space-y-4 mt-4 text-xs">
          <div>
            <label class="block font-bold text-gray-700 mb-1">Mã Serial Linh Kiện</label>
            <input
              type="text"
              disabled
              :value="serialData?.serialNumber"
              class="w-full px-3 py-2 bg-gray-100 rounded-lg border border-gray-300 font-mono font-bold text-gray-700"
            />
          </div>

          <div>
            <label class="block font-bold text-gray-700 mb-1">Họ và Tên Người Yêu Cầu <span class="text-red-500">*</span></label>
            <input
              v-model="claimForm.customerName"
              type="text"
              required
              placeholder="Nguyễn Văn A"
              class="w-full px-3 py-2 rounded-lg border border-gray-300 focus:border-[#09f] outline-none"
            />
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block font-bold text-gray-700 mb-1">Số Điện Thoại <span class="text-red-500">*</span></label>
              <input
                v-model="claimForm.customerPhone"
                type="text"
                required
                placeholder="0912345678"
                class="w-full px-3 py-2 rounded-lg border border-gray-300 focus:border-[#09f] outline-none"
              />
            </div>
            <div>
              <label class="block font-bold text-gray-700 mb-1">Email Nhận Tin</label>
              <input
                v-model="claimForm.customerEmail"
                type="email"
                placeholder="email@example.com"
                class="w-full px-3 py-2 rounded-lg border border-gray-300 focus:border-[#09f] outline-none"
              />
            </div>
          </div>

          <div>
            <label class="block font-bold text-gray-700 mb-1">Mô Tả Chi Tiết Sự Cố / Lỗi Kỹ Thuật <span class="text-red-500">*</span></label>
            <textarea
              v-model="claimForm.issueDescription"
              required
              rows="3"
              placeholder="Ví dụ: Bo mạch không sáng đèn LED nguồn khi cắm cáp Micro-USB; máy tính báo Unknown USB Device..."
              class="w-full px-3 py-2 rounded-lg border border-gray-300 focus:border-[#09f] outline-none"
            ></textarea>
          </div>

          <div class="flex items-center justify-end gap-3 pt-3 border-t border-gray-100">
            <button
              type="button"
              @click="showClaimModal = false"
              class="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-xl font-semibold cursor-pointer"
            >
              Hủy
            </button>
            <button
              type="submit"
              :disabled="submittingClaim"
              class="px-5 py-2 bg-[#09f] hover:bg-[#0088ea] text-white rounded-xl font-bold cursor-pointer disabled:opacity-50"
            >
              {{ submittingClaim ? 'Đang gửi...' : 'Gửi Phiếu Yêu Cầu' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const API_BASE_URL = 'http://localhost:8080'
const { success: showSuccess, error: showError } = useNotification()

const activeTab = ref<'lookup' | 'claim-status' | 'policy'>('lookup')

// Search Serial State
const lookupInput = ref({
  serialNumber: '',
  phone: ''
})
const searchingSerial = ref(false)
const lookupError = ref('')
const serialData = ref<any>(null)

// Claim Status State
const claimSearchCode = ref('')
const searchingClaim = ref(false)
const claimError = ref('')
const claimData = ref<any>(null)

// Create Claim Modal State
const showClaimModal = ref(false)
const submittingClaim = ref(false)
const claimForm = ref({
  customerName: '',
  customerPhone: '',
  customerEmail: '',
  issueDescription: ''
})

const handleLookupSerial = async () => {
  if (!lookupInput.value.serialNumber) return
  searchingSerial.value = true
  lookupError.value = ''
  serialData.value = null

  try {
    const params = new URLSearchParams()
    params.append('serialNumber', lookupInput.value.serialNumber.trim())
    if (lookupInput.value.phone) {
      params.append('phone', lookupInput.value.phone.trim())
    }

    const res: any = await $fetch(`${API_BASE_URL}/api/warranty/lookup?${params.toString()}`)
    serialData.value = res
  } catch (err: any) {
    lookupError.value = err?.data?.message || err?.message || 'Không tìm thấy thông tin bảo hành cho mã tem này.'
  } finally {
    searchingSerial.value = false
  }
}

const handleLookupClaim = async () => {
  if (!claimSearchCode.value) return
  searchingClaim.value = true
  claimError.value = ''
  claimData.value = null

  try {
    const res: any = await $fetch(`${API_BASE_URL}/api/warranty/claims/${claimSearchCode.value.trim().toUpperCase()}`)
    claimData.value = res
  } catch (err: any) {
    claimError.value = err?.data?.message || err?.message || 'Không tìm thấy phiếu bảo hành này trong hệ thống.'
  } finally {
    searchingClaim.value = false
  }
}

const openClaimModal = () => {
  if (!serialData.value) return
  claimForm.value = {
    customerName: serialData.value.customerName || '',
    customerPhone: '',
    customerEmail: '',
    issueDescription: ''
  }
  showClaimModal.value = true
}

const submitClaimForm = async () => {
  if (!serialData.value) return
  submittingClaim.value = true

  try {
    const payload = {
      serialNumber: serialData.value.serialNumber,
      customerName: claimForm.value.customerName,
      customerPhone: claimForm.value.customerPhone,
      customerEmail: claimForm.value.customerEmail,
      issueDescription: claimForm.value.issueDescription
    }

    const res: any = await $fetch(`${API_BASE_URL}/api/warranty/claims`, {
      method: 'POST',
      body: payload
    })

    showSuccess('Gửi yêu cầu bảo hành thành công! Mã phiếu của bạn: ' + res.claimCode)
    showClaimModal.value = false
    // Refresh serial data
    await handleLookupSerial()
    // Open Claim view
    claimSearchCode.value = res.claimCode
    claimData.value = res
    activeTab.value = 'claim-status'
  } catch (err: any) {
    showError(err?.data?.message || err?.message || 'Lỗi khi tạo phiếu bảo hành.')
  } finally {
    submittingClaim.value = false
  }
}

const viewClaimDetail = (code: string) => {
  claimSearchCode.value = code
  activeTab.value = 'claim-status'
  handleLookupClaim()
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  try {
    const d = new Date(dateStr)
    return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
  } catch {
    return dateStr
  }
}

const getStatusBadgeClass = (status: string) => {
  switch (status) {
    case 'PENDING':
      return 'bg-amber-100 text-amber-800'
    case 'PROCESSING':
      return 'bg-blue-100 text-blue-800'
    case 'COMPLETED':
      return 'bg-emerald-100 text-emerald-800'
    case 'REJECTED':
      return 'bg-red-100 text-red-800'
    default:
      return 'bg-gray-100 text-gray-700'
  }
}

const getStatusLabel = (status: string) => {
  switch (status) {
    case 'PENDING':
      return 'Chờ Tiếp Nhận'
    case 'PROCESSING':
      return 'Đang Xử Lý'
    case 'COMPLETED':
      return 'Hoàn Tất'
    case 'REJECTED':
      return 'Từ Chối'
    default:
      return status
  }
}
</script>
