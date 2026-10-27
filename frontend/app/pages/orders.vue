<template>
  <div class="bg-gray-50 min-h-screen py-8">
    <div class="w-[90%] max-w-[1920px] mx-auto px-2 sm:px-4 lg:px-6">
      <nav class="flex items-center gap-2 text-sm text-gray-600 mb-6">
        <NuxtLink to="/" class="hover:text-[#09f] transition-colors">Trang chủ</NuxtLink>
        <span>›</span>
        <span class="text-gray-900 font-medium">Đơn hàng của tôi</span>
      </nav>

      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-6">
        <div>
          <h1 class="text-3xl font-bold text-gray-900">Đơn hàng của tôi</h1>
          <p class="text-sm text-gray-600 mt-1">Theo dõi lịch sử đơn và trạng thái giao hàng</p>
        </div>
        <div class="flex gap-2">
          <button
            @click="createOrderFromCart"
            :disabled="creatingOrder"
            class="px-4 py-2.5 bg-[#09f] text-white rounded-xl text-sm font-semibold transition-all duration-300 hover:bg-[#0077cc] disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {{ creatingOrder ? 'Đang tạo...' : 'Tạo đơn từ giỏ hàng' }}
          </button>
          <button
            @click="fetchOrders"
            :disabled="loading"
            class="px-4 py-2.5 border-2 border-gray-300 text-gray-700 rounded-xl text-sm font-semibold transition-all duration-300 hover:border-[#09f] hover:text-[#09f] disabled:opacity-60 disabled:cursor-not-allowed"
          >
            Làm mới
          </button>
        </div>
      </div>

      <!-- Status Tabs -->
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 mb-6 p-1.5 flex gap-1 overflow-x-auto">
        <button
          v-for="tab in statusTabs"
          :key="tab.key"
          @click="activeTab = tab.key"
          :class="[
            'px-4 py-2.5 rounded-xl text-sm font-semibold whitespace-nowrap transition-all duration-200',
            activeTab === tab.key
              ? 'bg-[#09f] text-white shadow-sm'
              : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
          ]"
        >
          {{ tab.label }}
          <span
            v-if="getTabCount(tab.key) > 0"
            :class="[
              'ml-1.5 px-2 py-0.5 rounded-full text-xs font-bold',
              activeTab === tab.key
                ? 'bg-white/25 text-white'
                : 'bg-gray-200 text-gray-600'
            ]"
          >
            {{ getTabCount(tab.key) }}
          </span>
        </button>
      </div>

      <div v-if="loading" class="bg-white rounded-2xl p-10 text-center">
        <div class="inline-block animate-spin rounded-full h-10 w-10 border-4 border-[#09f] border-t-transparent mb-3"></div>
        <p class="text-gray-600">Đang tải danh sách đơn hàng...</p>
      </div>

      <div v-else-if="errorMessage" class="bg-red-50 border border-red-200 rounded-2xl p-4 text-sm text-red-700 mb-6">
        {{ errorMessage }}
      </div>

      <div v-if="!loading && filteredOrders.length > 0" class="space-y-4">
        <div
          v-for="order in filteredOrders"
          :key="order.id"
          class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden transition-all duration-200 hover:shadow-md"
        >
          <!-- Card Header & First Product -->
          <div class="p-5">
            <!-- Top bar: Order ID, Date & Status -->
            <div class="flex items-center justify-between pb-3 border-b border-gray-100 mb-4">
              <div class="flex items-center gap-2">
                <span class="text-xs font-semibold px-2.5 py-1 bg-gray-100 text-gray-700 rounded-lg font-mono">
                  #{{ order.orderNumber || order.id }}
                </span>
                <span class="text-xs text-gray-500">
                  {{ formatDate(order.createdAt) }}
                </span>
              </div>
              <span
                :class="[
                  'px-3 py-1 rounded-full text-xs font-semibold',
                  statusClass(getDisplayStatus(order))
                ]"
              >
                {{ statusLabel(getDisplayStatus(order)) }}
              </span>
            </div>

            <!-- Unpaid / Awaiting Online Payment Notice Banner -->
            <div
              v-if="isAwaitingVnpay(order)"
              class="mb-4 bg-amber-50 border border-amber-200 rounded-xl p-3 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2"
            >
              <div class="flex items-center gap-2">
                <span class="text-lg">⏳</span>
                <p class="text-xs sm:text-sm text-amber-800 font-medium">
                  Đơn hàng chưa hoàn tất thanh toán ({{ order.paymentMethod?.toUpperCase() }}). Vui lòng thanh toán trong vòng
                  <span class="font-bold text-amber-900 underline">{{ getPaymentRemainingTime(order) }}</span> trước khi hệ thống tự động hủy.
                </p>
              </div>
              <button
                @click="retryPayment(order)"
                :disabled="retryingOrderIds.has(order.id)"
                class="px-3.5 py-1.5 bg-amber-600 hover:bg-amber-700 text-white font-semibold text-xs rounded-lg transition-colors shadow-2xs whitespace-nowrap self-end sm:self-auto disabled:opacity-50"
              >
                {{ retryingOrderIds.has(order.id) ? 'Đang kết nối...' : 'Thanh toán ngay ↗' }}
              </button>
            </div>

            <!-- Return Status Banner if RETURNED -->
            <div
              v-if="normalizeStatus(getDisplayStatus(order)) === 'RETURNED'"
              class="mb-4 bg-rose-50 border border-rose-200 rounded-xl p-3 flex items-center justify-between"
            >
              <div>
                <p class="text-xs font-bold text-rose-800 uppercase flex items-center gap-1">
                  <span>↩</span> Yêu cầu trả hàng / Hoàn tiền đang được xử lý
                </p>
                <p v-if="order.returnReason" class="text-xs text-rose-700 mt-0.5">
                  Lý do: {{ order.returnReason }}
                </p>
              </div>
              <span class="text-xs font-semibold px-2 py-1 bg-white border border-rose-200 text-rose-700 rounded-lg">
                CSKH đang duyệt
              </span>
            </div>

            <!-- First Representative Product -->
            <div v-if="order.orderItems?.length" class="flex gap-4 items-start">
              <!-- Thumbnail -->
              <div class="w-18 h-18 sm:w-20 sm:h-20 rounded-xl bg-gray-100 overflow-hidden flex-shrink-0 border border-gray-100 flex items-center justify-center">
                <img
                  v-if="order.orderItems[0].imageUrl"
                  :src="order.orderItems[0].imageUrl"
                  :alt="order.orderItems[0].productName || order.orderItems[0].variantName || 'Sản phẩm'"
                  class="w-full h-full object-cover"
                />
                <svg v-else class="w-8 h-8 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                </svg>
              </div>

              <!-- Product Details -->
              <div class="flex-1 min-w-0">
                <h3 class="text-sm sm:text-base font-semibold text-gray-900 line-clamp-2 leading-snug">
                  {{ order.orderItems[0].productName || order.orderItems[0].variantName || `Biến thể #${order.orderItems[0].productVariantId}` }}
                </h3>
                <p v-if="order.orderItems[0].variantName && order.orderItems[0].productName" class="text-xs text-gray-500 mt-1 line-clamp-1">
                  Phân loại: <span class="font-medium text-gray-700">{{ order.orderItems[0].variantName }}</span>
                </p>
                <div class="flex items-center gap-3 mt-2 text-xs sm:text-sm">
                  <span class="text-gray-500">SL: <span class="font-semibold text-gray-800">{{ order.orderItems[0].quantity }}</span></span>
                  <span class="text-gray-300">•</span>
                  <span class="font-semibold text-gray-900">{{ formatPrice(order.orderItems[0].price) }}</span>
                </div>
              </div>

              <!-- Item Total / Price -->
              <div class="text-right hidden sm:block flex-shrink-0">
                <p class="text-xs text-gray-500">Thành tiền</p>
                <p class="text-sm font-bold text-gray-900">{{ formatPrice(order.orderItems[0].totalPrice) }}</p>
              </div>
            </div>

            <!-- Empty order fallback -->
            <div v-else class="text-sm text-gray-500 py-2">
              Đơn hàng không có sản phẩm.
            </div>

            <!-- OTP Alert if present -->
            <div v-if="order.recipientOTP" class="mt-4 bg-gradient-to-r from-blue-50 to-blue-100 border border-blue-200 rounded-xl p-3 flex items-center justify-between">
              <div>
                <p class="text-[10px] text-blue-600 font-semibold uppercase">Mã OTP nhận tủ thông minh</p>
                <p class="text-xl font-bold text-blue-900 font-mono tracking-wider">{{ order.recipientOTP }}</p>
              </div>
              <span class="text-2xl">📦</span>
            </div>

            <!-- Accordion Toggle button (If 2+ items) -->
            <div
              v-if="order.orderItems && order.orderItems.length > 1"
              class="mt-3 pt-3 border-t border-gray-50"
            >
              <button
                @click="toggleExpanded(order.id)"
                class="w-full py-2 px-3 rounded-xl bg-gray-50 hover:bg-gray-100 text-xs sm:text-sm font-medium text-gray-700 flex items-center justify-between transition-colors group"
              >
                <span class="flex items-center gap-1.5 text-gray-600 group-hover:text-gray-900">
                  <span v-if="!expandedOrderIds.has(order.id)">
                    Và <strong>{{ order.orderItems.length - 1 }}</strong> sản phẩm khác...
                  </span>
                  <span v-else>Thu gọn danh sách</span>
                </span>
                <span class="flex items-center gap-1 text-[#09f] font-semibold text-xs">
                  <span>{{ expandedOrderIds.has(order.id) ? 'Thu gọn' : 'Xem thêm' }}</span>
                  <svg
                    class="w-4 h-4 transition-transform duration-200"
                    :class="{ 'rotate-180': expandedOrderIds.has(order.id) }"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                  </svg>
                </span>
              </button>
            </div>

            <!-- Order Footer: Total amount & Actions -->
            <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between pt-4 mt-3 border-t border-gray-100 gap-3">
              <div class="flex items-baseline gap-2">
                <span class="text-xs sm:text-sm text-gray-500">Tổng thanh toán ({{ order.orderItems?.length || 0 }} món):</span>
                <span class="text-lg sm:text-xl font-bold text-[#09f]">{{ formatPrice(order.totalPrice) }}</span>
              </div>

              <div class="flex items-center gap-2 flex-wrap justify-end">
                <!-- Action: Retry VNPAY Payment button -->
                <button
                  v-if="isAwaitingVnpay(order)"
                  @click="retryPayment(order)"
                  :disabled="retryingOrderIds.has(order.id)"
                  class="px-4 py-2 bg-[#09f] hover:bg-[#0077cc] text-white rounded-xl text-xs sm:text-sm font-semibold transition-colors shadow-xs disabled:opacity-60"
                >
                  {{ retryingOrderIds.has(order.id) ? 'Đang mở cổng...' : 'Thanh toán lại' }}
                </button>

                <!-- Action: 7-day Return Request button -->
                <button
                  v-if="isEligibleForReturn(order)"
                  @click="openReturnModal(order)"
                  :disabled="returningOrderIds.has(order.id)"
                  class="px-4 py-2 border border-amber-300 text-amber-700 bg-amber-50 hover:bg-amber-100 rounded-xl text-xs sm:text-sm font-semibold transition-colors flex items-center gap-1.5"
                >
                  <span>↩</span> Yêu cầu trả hàng (7 ngày)
                </button>

                <!-- Action: Cancel button -->
                <button
                  v-if="canCancel(getDisplayStatus(order))"
                  @click="cancelOrder(order.id)"
                  :disabled="cancellingOrderIds.has(order.id)"
                  class="px-4 py-2 border border-red-300 rounded-xl text-xs sm:text-sm font-semibold text-red-600 hover:bg-red-50 transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
                >
                  {{ cancellingOrderIds.has(order.id) ? 'Đang hủy...' : 'Hủy đơn' }}
                </button>
              </div>
            </div>
          </div>

          <!-- Expanded Accordion: Remaining Products (items[1..n]) -->
          <transition
            enter-active-class="transition-all duration-300 ease-out"
            enter-from-class="opacity-0 max-h-0"
            enter-to-class="opacity-100 max-h-[1000px]"
            leave-active-class="transition-all duration-200 ease-in"
            leave-from-class="opacity-100 max-h-[1000px]"
            leave-to-class="opacity-0 max-h-0"
          >
            <div
              v-if="expandedOrderIds.has(order.id) && order.orderItems && order.orderItems.length > 1"
              class="border-t border-gray-100 bg-gray-50/70 px-5 py-4 space-y-3"
            >
              <p class="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">Các sản phẩm khác trong đơn</p>
              <div
                v-for="item in order.orderItems.slice(1)"
                :key="`${order.id}-${item.id}`"
                class="bg-white border border-gray-200/80 rounded-xl p-3 flex gap-3 items-center justify-between shadow-2xs"
              >
                <div class="flex items-center gap-3 min-w-0">
                  <div class="w-12 h-12 rounded-lg bg-gray-100 overflow-hidden flex-shrink-0 border border-gray-100 flex items-center justify-center">
                    <img
                      v-if="item.imageUrl"
                      :src="item.imageUrl"
                      :alt="item.productName || item.variantName || 'Sản phẩm'"
                      class="w-full h-full object-cover"
                    />
                    <svg v-else class="w-6 h-6 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                    </svg>
                  </div>
                  <div class="min-w-0">
                    <p class="text-xs sm:text-sm font-semibold text-gray-900 line-clamp-1">
                      {{ item.productName || item.variantName || `Biến thể #${item.productVariantId}` }}
                    </p>
                    <p v-if="item.variantName && item.productName" class="text-[11px] text-gray-500 line-clamp-1">
                      Phân loại: {{ item.variantName }}
                    </p>
                    <p class="text-[11px] text-gray-600 mt-0.5">
                      SL: <span class="font-medium text-gray-800">{{ item.quantity }}</span> × {{ formatPrice(item.price) }}
                    </p>
                  </div>
                </div>
                <div class="text-right flex-shrink-0">
                  <p class="text-xs sm:text-sm font-bold text-gray-900">{{ formatPrice(item.totalPrice) }}</p>
                </div>
              </div>
            </div>
          </transition>
        </div>

        <div v-if="totalPages > 1" class="flex items-center justify-center gap-2 pt-2">
          <button
            @click="changePage(currentPage - 1)"
            :disabled="currentPage === 0"
            class="px-3 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Trước
          </button>

          <span class="text-sm text-gray-700">Trang {{ currentPage + 1 }} / {{ totalPages }}</span>

          <button
            @click="changePage(currentPage + 1)"
            :disabled="currentPage + 1 >= totalPages"
            class="px-3 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Sau
          </button>
        </div>
      </div>

      <div v-else-if="!loading && orders.length > 0 && filteredOrders.length === 0" class="bg-white rounded-2xl p-12 text-center">
        <div class="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <svg class="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
        </div>
        <h2 class="text-xl font-bold text-gray-900 mb-2">Không có đơn hàng nào ở trạng thái này</h2>
        <p class="text-gray-600 mb-6">Chọn tab khác để xem đơn hàng ở trạng thái khác.</p>
      </div>

      <div v-else-if="!loading" class="bg-white rounded-2xl p-12 text-center">
        <div class="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <svg class="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
        </div>
        <h2 class="text-xl font-bold text-gray-900 mb-2">Bạn chưa có đơn hàng nào</h2>
        <p class="text-gray-600 mb-6">Hãy thêm sản phẩm vào giỏ và tạo đơn hàng đầu tiên.</p>
        <NuxtLink
          to="/cart"
          class="inline-block px-6 py-3 bg-gradient-to-r from-[#09f] to-[#0077cc] text-white rounded-xl font-semibold transition-all duration-300 hover:shadow-xl hover:scale-105"
        >
          Đi tới giỏ hàng
        </NuxtLink>
      </div>
    </div>

    <!-- Return Request Modal -->
    <div
      v-if="showReturnModal && returnTargetOrder"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs"
      @click.self="showReturnModal = false"
    >
      <div class="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl relative border border-gray-100 max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between pb-3 border-b border-gray-100">
          <div>
            <h3 class="text-lg font-bold text-gray-900">Yêu cầu trả hàng & hoàn tiền</h3>
            <p class="text-xs text-gray-500 mt-0.5">Đơn hàng #{{ returnTargetOrder.id }} • Hỗ trợ trong 7 ngày</p>
          </div>
          <button
            @click="showReturnModal = false"
            class="text-gray-400 hover:text-gray-600 p-1.5 rounded-lg hover:bg-gray-100 transition-colors"
          >
            ✕
          </button>
        </div>

        <div class="mt-4 space-y-4">
          <!-- Products to return -->
          <div>
            <label class="block text-xs font-semibold text-gray-700 uppercase mb-2">Sản phẩm yêu cầu trả</label>
            <div class="space-y-2 max-h-40 overflow-y-auto pr-1">
              <div
                v-for="item in returnTargetOrder.orderItems"
                :key="item.id"
                class="flex items-center gap-3 p-2 bg-gray-50 rounded-xl border border-gray-100 text-xs"
              >
                <input
                  type="checkbox"
                  :value="item.id"
                  v-model="selectedReturnItemIds"
                  class="rounded text-[#09f] focus:ring-[#09f] h-4 w-4"
                />
                <img
                  v-if="item.imageUrl"
                  :src="item.imageUrl"
                  class="w-9 h-9 rounded object-cover border border-gray-200"
                />
                <div class="flex-1 min-w-0">
                  <p class="font-medium text-gray-900 truncate">{{ item.productName || item.variantName || 'Sản phẩm' }}</p>
                  <p class="text-gray-500">SL: {{ item.quantity }} • {{ formatPrice(item.price) }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Return Reason Selection -->
          <div>
            <label class="block text-xs font-semibold text-gray-700 uppercase mb-1">Lý do trả hàng *</label>
            <select
              v-model="returnReason"
              class="w-full text-sm border border-gray-300 rounded-xl px-3 py-2.5 focus:ring-2 focus:ring-[#09f]/30 focus:border-[#09f] outline-hidden bg-white"
            >
              <option value="Sản phẩm lỗi kỹ thuật / không hoạt động">Sản phẩm lỗi kỹ thuật / không hoạt động</option>
              <option value="Giao sai mẫu mã / sai phân loại">Giao sai mẫu mã / sai phân loại</option>
              <option value="Hàng bị hư hỏng / móp méo khi vận chuyển">Hàng bị hư hỏng / móp méo khi vận chuyển</option>
              <option value="Thiếu phụ kiện / không đủ số lượng">Thiếu phụ kiện / không đủ số lượng</option>
              <option value="Đổi ý, không còn nhu cầu sử dụng">Đổi ý, không còn nhu cầu sử dụng</option>
              <option value="Khác">Lý do khác</option>
            </select>
          </div>

          <!-- Notes / Description -->
          <div>
            <label class="block text-xs font-semibold text-gray-700 uppercase mb-1">Mô tả chi tiết lỗi / Ghi chú</label>
            <textarea
              v-model="returnProofNote"
              rows="3"
              placeholder="Vui lòng mô tả tình trạng sản phẩm và lý do bạn muốn trả lại..."
              class="w-full text-sm border border-gray-300 rounded-xl p-3 focus:ring-2 focus:ring-[#09f]/30 focus:border-[#09f] outline-hidden resize-none"
            ></textarea>
          </div>

          <!-- Refund Method -->
          <div>
            <label class="block text-xs font-semibold text-gray-700 uppercase mb-2">Phương thức nhận hoàn tiền</label>
            <div class="grid grid-cols-2 gap-2 text-xs">
              <label
                class="flex items-center gap-2 p-2.5 border rounded-xl cursor-pointer transition-colors"
                :class="returnRefundMethod === 'VNPAY' ? 'border-[#09f] bg-blue-50/50 text-[#09f] font-semibold' : 'border-gray-200 text-gray-700 hover:bg-gray-50'"
              >
                <input type="radio" value="VNPAY" v-model="returnRefundMethod" class="sr-only" />
                <span>💳 Ví / Cổng VNPAY</span>
              </label>
              <label
                class="flex items-center gap-2 p-2.5 border rounded-xl cursor-pointer transition-colors"
                :class="returnRefundMethod === 'BANK' ? 'border-[#09f] bg-blue-50/50 text-[#09f] font-semibold' : 'border-gray-200 text-gray-700 hover:bg-gray-50'"
              >
                <input type="radio" value="BANK" v-model="returnRefundMethod" class="sr-only" />
                <span>🏦 Tài khoản ngân hàng</span>
              </label>
            </div>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="flex items-center justify-end gap-2 mt-6 pt-4 border-t border-gray-100">
          <button
            @click="showReturnModal = false"
            class="px-4 py-2 border border-gray-300 rounded-xl text-xs sm:text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors"
          >
            Hủy bỏ
          </button>
          <button
            @click="submitReturnOrder"
            :disabled="returningOrderIds.has(returnTargetOrder.id) || selectedReturnItemIds.length === 0"
            class="px-5 py-2 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-600 hover:to-amber-700 text-white rounded-xl text-xs sm:text-sm font-semibold transition-all shadow-sm disabled:opacity-50"
          >
            {{ returningOrderIds.has(returnTargetOrder.id) ? 'Đang gửi yêu cầu...' : 'Gửi yêu cầu trả hàng' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

interface OrderItem {
  id: number
  productVariantId: number
  quantity: number
  price: number
  totalPrice: number
  variantName?: string
  productName?: string
  imageUrl?: string
}

interface Order {
  id: number
  userId: number
  status: string
  deliveryStatus?: string
  orderNumber?: string
  paymentMethod?: string
  paymentStatus?: string
  lockerOrderId?: string
  senderOTP?: string
  recipientOTP?: string
  totalPrice: number
  orderItems: OrderItem[]
  createdAt: string
  updatedAt: string
  deliveredAt?: string
  returnedAt?: string
  returnReason?: string
}

interface OrderPageResponse {
  content: Order[]
  totalPages: number
  totalElements: number
  size: number
  number: number
}

definePageMeta({
  middleware: ['auth']
})

const API_BASE_URL = 'http://localhost:8080'
const { getAuthHeader } = useAuth()
const { success: showSuccess, error: showError } = useNotification()
const { confirm: showConfirm } = useConfirm()

const orders = ref<Order[]>([])
const loading = ref(false)
const creatingOrder = ref(false)
const errorMessage = ref('')
const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = 10

const expandedOrderIds = ref<Set<number>>(new Set())
const cancellingOrderIds = ref<Set<number>>(new Set())
const retryingOrderIds = ref<Set<number>>(new Set())
const returningOrderIds = ref<Set<number>>(new Set())

// Return Modal State
const showReturnModal = ref(false)
const returnTargetOrder = ref<Order | null>(null)
const selectedReturnItemIds = ref<number[]>([])
const returnReason = ref('')
const returnProofNote = ref('')
const returnRefundMethod = ref('VNPAY')

type TabKey = 'ALL' | 'UNPAID' | 'PENDING' | 'SHIPPING' | 'DELIVERED' | 'RETURNED' | 'CANCELLED'

const activeTab = ref<TabKey>('ALL')

const statusTabs: { key: TabKey; label: string }[] = [
  { key: 'ALL', label: 'Tất cả' },
  { key: 'UNPAID', label: 'Chờ thanh toán' },
  { key: 'PENDING', label: 'Chờ xác nhận' },
  { key: 'SHIPPING', label: 'Đang giao' },
  { key: 'DELIVERED', label: 'Đã giao' },
  { key: 'RETURNED', label: 'Trả hàng / Hoàn tiền' },
  { key: 'CANCELLED', label: 'Đã hủy' },
]

const isAwaitingVnpay = (order: Order) => {
  const method = order.paymentMethod?.toUpperCase()
  return (
    (method === 'VNPAY' || method === 'PAYOS') &&
    (order.paymentStatus === 'UNPAID' || order.paymentStatus === 'AWAITING_PAYMENT') &&
    order.status === 'PENDING'
  )
}

const getOrderTabKey = (order: Order): TabKey => {
  const status = normalizeStatus(getDisplayStatus(order))
  if (status === 'CANCELLED') return 'CANCELLED'
  if (status === 'RETURNED') return 'RETURNED'

  // If VNPAY/PAYOS and unpaid/awaiting_payment
  const pm = order.paymentMethod?.toUpperCase()
  if ((pm === 'VNPAY' || pm === 'PAYOS') &&
      (order.paymentStatus === 'UNPAID' || order.paymentStatus === 'AWAITING_PAYMENT') &&
      status === 'PENDING') {
    return 'UNPAID'
  }

  switch (status) {
    case 'PENDING':
    case 'CONFIRMED':
      return 'PENDING'
    case 'SHIPPED':
    case 'IN_TRANSIT':
      return 'SHIPPING'
    case 'DELIVERED':
    case 'DELIVERED_TO_LOCKER':
    case 'PICKED_UP':
    case 'COMPLETED':
      return 'DELIVERED'
    case 'RETURNED':
      return 'RETURNED'
    case 'CANCELLED':
      return 'CANCELLED'
    default:
      return 'PENDING'
  }
}

const filteredOrders = computed(() => {
  if (activeTab.value === 'ALL') return orders.value
  return orders.value.filter(order => getOrderTabKey(order) === activeTab.value)
})

const getTabCount = (tab: TabKey): number => {
  if (tab === 'ALL') return orders.value.length
  return orders.value.filter(order => getOrderTabKey(order) === tab).length
}

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price || 0)
}

const formatDate = (dateString?: string) => {
  if (!dateString) return '--'
  const date = new Date(dateString)
  return date.toLocaleString('vi-VN')
}

// Check if order is eligible for 7-day return
const isEligibleForReturn = (order: Order): boolean => {
  const status = normalizeStatus(getDisplayStatus(order))
  const isDelivered = status === 'DELIVERED' || status === 'COMPLETED' || status === 'PICKED_UP'
  if (!isDelivered) return false

  const referenceDate = order.deliveredAt || order.updatedAt || order.createdAt
  if (!referenceDate) return false

  const deliveredTime = new Date(referenceDate).getTime()
  const now = Date.now()
  const sevenDaysMs = 7 * 24 * 60 * 60 * 1000
  return now - deliveredTime <= sevenDaysMs
}

// Calculate remaining hours for 24h payment countdown
const getPaymentRemainingTime = (order: Order): string => {
  if (!order.createdAt) return '24h'
  const createdTime = new Date(order.createdAt).getTime()
  const expireTime = createdTime + 24 * 60 * 60 * 1000
  const remainingMs = expireTime - Date.now()
  if (remainingMs <= 0) return 'Đã hết hạn'
  const hours = Math.floor(remainingMs / (1000 * 60 * 60))
  const minutes = Math.floor((remainingMs % (1000 * 60 * 60)) / (1000 * 60))
  return `${hours}h ${minutes}m`
}

const statusLabel = (status: string, order?: Order) => {
  const pm = order?.paymentMethod?.toUpperCase()
  if (order && (pm === 'VNPAY' || pm === 'PAYOS') &&
      (order.paymentStatus === 'UNPAID' || order.paymentStatus === 'AWAITING_PAYMENT') &&
      normalizeStatus(order.status) === 'PENDING') {
    return order.paymentStatus === 'AWAITING_PAYMENT' ? 'Chờ thanh toán (24h)' : 'Chưa thanh toán'
  }

  const normalized = normalizeStatus(status)
  switch (normalized) {
    case 'IN_TRANSIT':
      return 'Đang giao'
    case 'PICKED_UP':
      return 'Đã nhận hàng'
    case 'PENDING':
      return 'Chờ xử lý'
    case 'CONFIRMED':
      return 'Đã xác nhận'
    case 'SHIPPED':
      return 'Đang giao'
    case 'DELIVERED':
    case 'DELIVERED_TO_LOCKER':
    case 'COMPLETED':
      return 'Đã giao'
    case 'RETURNED':
      return 'Trả hàng / Hoàn tiền'
    case 'CANCELLED':
      return 'Đã hủy'
    default:
      return status
  }
}

const statusClass = (status: string, order?: Order) => {
  const pm = order?.paymentMethod?.toUpperCase()
  if (order && (pm === 'VNPAY' || pm === 'PAYOS') &&
      (order.paymentStatus === 'UNPAID' || order.paymentStatus === 'AWAITING_PAYMENT') &&
      normalizeStatus(order.status) === 'PENDING') {
    return 'bg-amber-100 text-amber-800 border border-amber-200'
  }

  const normalized = normalizeStatus(status)
  switch (normalized) {
    case 'IN_TRANSIT':
    case 'SHIPPED':
      return 'bg-purple-100 text-purple-700'
    case 'PICKED_UP':
    case 'DELIVERED':
    case 'DELIVERED_TO_LOCKER':
    case 'COMPLETED':
      return 'bg-green-100 text-green-700'
    case 'PENDING':
      return 'bg-yellow-100 text-yellow-700'
    case 'CONFIRMED':
      return 'bg-blue-100 text-blue-700'
    case 'RETURNED':
      return 'bg-rose-100 text-rose-700 border border-rose-200'
    case 'CANCELLED':
      return 'bg-red-100 text-red-700'
    default:
      return 'bg-gray-100 text-gray-700'
  }
}

const canCancel = (status: string) => {
  const normalized = normalizeStatus(status)
  return normalized === 'PENDING' || normalized === 'CONFIRMED'
}

const normalizeStatus = (status?: string) => {
  return (status || 'PENDING').toUpperCase().replace(/-/g, '_')
}

const getDisplayStatus = (order: Order) => {
  return order.deliveryStatus || order.status || 'PENDING'
}

const toggleExpanded = (orderId: number) => {
  if (expandedOrderIds.value.has(orderId)) {
    expandedOrderIds.value.delete(orderId)
    return
  }
  expandedOrderIds.value.add(orderId)
}

const fetchOrders = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    const response: OrderPageResponse = await $fetch(`${API_BASE_URL}/api/orders`, {
      params: {
        page: currentPage.value,
        size: pageSize
      },
      headers: {
        ...(getAuthHeader() as Record<string, string>)
      }
    })

    orders.value = response?.content || []
    totalPages.value = response?.totalPages || 0
  } catch (error: any) {
    console.error('Error fetching orders:', error)
    errorMessage.value = 'Không thể tải danh sách đơn hàng. Vui lòng thử lại.'
    showError(errorMessage.value)
  } finally {
    loading.value = false
  }
}

const changePage = async (page: number) => {
  if (page < 0 || page >= totalPages.value || page === currentPage.value) return
  currentPage.value = page
  expandedOrderIds.value.clear()
  await fetchOrders()
}

const cancelOrder = async (orderId: number) => {
  const ok = await showConfirm('Bạn có chắc muốn hủy đơn hàng này?', 'Xác nhận hủy đơn')
  if (!ok) {
    return
  }

  cancellingOrderIds.value.add(orderId)
  try {
    await $fetch(`${API_BASE_URL}/api/orders/${orderId}/cancel`, {
      method: 'DELETE',
      headers: {
        ...(getAuthHeader() as Record<string, string>)
      }
    })

    showSuccess('Đã hủy đơn hàng thành công')
    await fetchOrders()
  } catch (error: any) {
    console.error('Error cancelling order:', error)
    const message = error?.data?.message || error?.data?.error || 'Không thể hủy đơn hàng này'
    showError(message)
  } finally {
    cancellingOrderIds.value.delete(orderId)
  }
}

const createOrderFromCart = async () => {
  creatingOrder.value = true
  try {
    await $fetch(`${API_BASE_URL}/api/orders`, {
      method: 'POST',
      headers: {
        ...(getAuthHeader() as Record<string, string>)
      }
    })

    showSuccess('Tạo đơn hàng từ giỏ thành công')
    currentPage.value = 0
    expandedOrderIds.value.clear()
    await fetchOrders()
  } catch (error: any) {
    console.error('Error creating order:', error)
    const message = error?.data?.message || 'Không thể tạo đơn từ giỏ hàng (có thể giỏ đang trống)'
    showError(message)
  } finally {
    creatingOrder.value = false
  }
}

const retryPayment = async (order: Order) => {
  retryingOrderIds.value.add(order.id)
  try {
    const isPayOs = order.paymentMethod?.toUpperCase() === 'PAYOS'
    const endpoint = isPayOs
      ? `${API_BASE_URL}/api/payos/retry-payment/${order.id}`
      : `${API_BASE_URL}/api/vnpay/retry-payment/${order.id}`
    const res: any = await $fetch(endpoint, {
      method: 'POST',
      headers: {
        ...(getAuthHeader() as Record<string, string>)
      }
    })
    const redirectUrl = res.checkoutUrl || res.payUrl
    if (res.resultCode === 0 && redirectUrl) {
      window.location.href = redirectUrl
    } else {
      showError(res.message || 'Không thể tạo phiên thanh toán lại. Vui lòng thử lại.')
    }
  } catch (error: any) {
    console.error('Error retrying payment:', error)
    showError(error?.data?.message || 'Không thể kết nối đến cổng thanh toán')
  } finally {
    retryingOrderIds.value.delete(order.id)
  }
}

const openReturnModal = (order: Order) => {
  returnTargetOrder.value = order
  selectedReturnItemIds.value = order.orderItems?.map(i => i.id) || []
  returnReason.value = 'Sản phẩm lỗi kỹ thuật / không hoạt động'
  returnProofNote.value = ''
  returnRefundMethod.value = 'VNPAY'
  showReturnModal.value = true
}

const submitReturnOrder = async () => {
  if (!returnTargetOrder.value) return
  if (!returnReason.value.trim()) {
    showError('Vui lòng chọn hoặc nhập lý do trả hàng')
    return
  }

  const orderId = returnTargetOrder.value.id
  returningOrderIds.value.add(orderId)
  try {
    const fullReason = `${returnReason.value}${returnProofNote.value ? ' - Ghi chú: ' + returnProofNote.value : ''} [Hoàn tiền qua: ${returnRefundMethod.value}]`
    await $fetch(`${API_BASE_URL}/api/orders/${orderId}/return`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(getAuthHeader() as Record<string, string>)
      },
      body: JSON.stringify({ reason: fullReason })
    })

    showSuccess('Đã gửi yêu cầu trả hàng / hoàn tiền thành công!')
    showReturnModal.value = false
    await fetchOrders()
  } catch (error: any) {
    console.error('Error submitting return request:', error)
    showError(error?.data?.message || 'Không thể gửi yêu cầu trả hàng')
  } finally {
    returningOrderIds.value.delete(orderId)
  }
}

onMounted(() => {
  fetchOrders()
})
</script>
