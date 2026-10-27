<template>
  <div class="fixed bottom-6 right-6 z-50 font-sans">
    <!-- Floating Action Button (FAB) -->
    <button
      v-if="!isOpen"
      @click="isOpen = true"
      class="group relative flex items-center justify-center w-14 h-14 bg-gradient-to-tr from-[#0077cc] to-[#09f] text-white rounded-full shadow-lg hover:shadow-cyan-500/50 hover:scale-105 active:scale-95 transition-all duration-300"
      title="Hỏi trợ lý AI"
    >
      <!-- Chat Icon -->
      <svg class="w-7 h-7 transition-transform group-hover:scale-110" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
      </svg>
      <!-- Online Pulse Dot -->
      <span class="absolute top-0 right-0 flex h-3.5 w-3.5">
        <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
        <span class="relative inline-flex rounded-full h-3.5 w-3.5 bg-emerald-500 border-2 border-white"></span>
      </span>
    </button>

    <!-- Chat Box Window -->
    <div
      v-else
      class="flex flex-col w-[380px] sm:w-[450px] h-[600px] max-h-[88vh] bg-white rounded-2xl shadow-2xl border border-gray-100 overflow-hidden transition-all duration-300 animate-in fade-in slide-in-from-bottom-5"
    >
      <!-- Header -->
      <div class="flex items-center justify-between px-4 py-3.5 bg-gradient-to-r from-[#0077cc] to-[#09f] text-white shadow-sm">
        <div class="flex items-center gap-2.5">
          <div class="w-9 h-9 rounded-full bg-white/20 backdrop-blur-md flex items-center justify-center border border-white/30 text-white font-bold text-sm">
            AI
          </div>
          <div>
            <h3 class="font-bold text-sm leading-tight">Trợ Lý ECSupport</h3>
            <div class="flex items-center gap-1.5 text-xs text-white/80">
              <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
              <span>Trò chuyện &amp; mua sắm</span>
            </div>
          </div>
        </div>

        <div class="flex items-center gap-1">
          <!-- Reset / New Session Button -->
          <button
            @click="resetSession"
            class="p-1.5 rounded-lg text-white/80 hover:text-white hover:bg-white/15 transition-colors"
            title="Cuộc trò chuyện mới"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
          </button>
          <!-- Close Button -->
          <button
            @click="isOpen = false"
            class="p-1.5 rounded-lg text-white/80 hover:text-white hover:bg-white/15 transition-colors"
            title="Đóng chat"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>

      <!-- Messages Body -->
      <div ref="messagesContainer" class="flex-1 p-4 overflow-y-auto space-y-3.5 bg-gray-50/50">
        <!-- Welcome Message -->
        <div v-if="messages.length === 0" class="text-center py-6 px-3">
          <div class="w-12 h-12 mx-auto mb-3 rounded-2xl bg-blue-50 text-[#09f] flex items-center justify-center">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
          <h4 class="font-bold text-gray-800 text-sm mb-1">Xin chào! Tôi có thể giúp gì cho bạn?</h4>
          <p class="text-xs text-gray-500 mb-4 leading-relaxed">
            Bạn có thể trò chuyện, hỏi kiến thức hoặc nhờ tư vấn và tìm linh kiện.
          </p>

          <!-- Suggestion Chips -->
          <div class="flex flex-col gap-1.5 text-left">
            <button
              v-for="(suggestion, idx) in suggestions"
              :key="idx"
              @click="sendQuickMessage(suggestion)"
              class="text-xs px-3 py-2 bg-white border border-gray-200 rounded-xl text-gray-700 hover:border-[#09f] hover:text-[#09f] hover:bg-blue-50/50 transition-all text-left shadow-2xs"
            >
              💡 {{ suggestion }}
            </button>
          </div>
        </div>

        <!-- Message Item -->
        <template v-for="(msg, index) in messages" :key="index">
          <!-- User Message -->
          <div v-if="msg.role === 'user'" class="flex justify-end">
            <div class="max-w-[85%] px-3.5 py-2.5 bg-[#09f] text-white rounded-2xl rounded-tr-xs text-xs sm:text-sm leading-relaxed shadow-xs break-words">
              {{ msg.content }}
            </div>
          </div>

          <!-- Assistant Message -->
          <div v-else class="flex gap-2 items-start justify-start">
            <div class="w-6 h-6 rounded-full bg-[#09f] text-white flex-shrink-0 flex items-center justify-center text-[10px] font-bold mt-1 shadow-xs">
              AI
            </div>
            <div class="max-w-[88%] sm:max-w-[85%] flex flex-col gap-1.5 w-full">
              <!-- Rendered Markdown Message -->
              <div
                class="px-3.5 py-2.5 bg-white border border-gray-200 text-gray-800 rounded-2xl rounded-tl-xs text-xs sm:text-sm leading-relaxed shadow-xs break-words ai-rendered-content"
                v-html="renderMarkdown(msg.content)"
              ></div>

              <!-- Interactive Structured Cards from Referenced Data -->
              <template v-if="msg.referencedData && msg.referencedData.length">
                <div v-for="(ref, rIdx) in msg.referencedData" :key="rIdx" class="flex flex-col gap-2">
                  <!-- Case 1: Product Grid / Cards (from search_products or get_top_selling_products) -->
                  <div v-if="hasProducts(ref)" class="bg-white border border-blue-100 rounded-xl p-2.5 shadow-2xs">
                    <div class="text-[11px] font-bold text-blue-900 mb-2 flex items-center justify-between">
                      <span class="flex items-center gap-1">⚡ Linh kiện gợi ý</span>
                      <span class="text-[10px] text-gray-400 font-normal">{{ getProductsFromRef(ref).length }} kết quả</span>
                    </div>
                    <div class="grid grid-cols-1 gap-1.5">
                      <div
                        v-for="p in getProductsFromRef(ref).slice(0, 3)"
                        :key="p.id"
                        class="flex items-center justify-between p-2 rounded-lg bg-gray-50/80 hover:bg-blue-50/60 border border-gray-100 transition-colors group"
                      >
                        <div class="min-w-0 flex-1 pr-2">
                          <NuxtLink :to="'/products/' + p.id" class="text-xs font-semibold text-gray-800 group-hover:text-[#09f] line-clamp-1 block">
                            {{ p.name }}
                          </NuxtLink>
                          <div class="flex items-center gap-2 mt-0.5">
                            <span class="text-[11px] font-bold text-[#09f]">{{ formatPriceDisplay(p) }}</span>
                            <span v-if="p.totalStock !== undefined" :class="p.totalStock > 0 ? 'text-emerald-700 bg-emerald-50' : 'text-rose-700 bg-rose-50'" class="text-[9px] px-1.5 py-0.2 rounded-full font-medium">
                              {{ p.totalStock > 0 ? 'Còn hàng' : 'Hết hàng' }}
                            </span>
                            <span v-else-if="p.soldQuantity !== undefined" class="text-[10px] text-gray-500">
                              Đã bán: {{ p.soldQuantity }}
                            </span>
                          </div>
                        </div>
                        <NuxtLink
                          :to="'/products/' + p.id"
                          class="px-2.5 py-1 bg-white border border-blue-200 text-[#09f] group-hover:bg-[#09f] group-hover:text-white rounded-lg text-[10px] font-medium transition-all shadow-2xs flex-shrink-0 flex items-center gap-0.5"
                        >
                          Xem chi tiết ↗
                        </NuxtLink>
                      </div>
                    </div>
                  </div>

                  <!-- Case 2: Order Tracking Card (from track_order) -->
                  <div v-else-if="ref.orderNumber && !ref.error" class="bg-gradient-to-br from-blue-50/60 to-indigo-50/40 border border-blue-200/80 rounded-xl p-3 shadow-2xs">
                    <div class="flex items-center justify-between pb-2 border-b border-blue-100 mb-2">
                      <div class="flex items-center gap-1.5">
                        <span class="text-sm">🚚</span>
                        <span class="font-mono text-xs font-bold text-gray-900">#{{ ref.orderNumber }}</span>
                      </div>
                      <span :class="getOrderStatusBadgeClass(ref.status)" class="text-[10px] font-semibold px-2 py-0.5 rounded-full shadow-2xs">
                        {{ ref.statusDescription || ref.status }}
                      </span>
                    </div>
                    <div class="grid grid-cols-2 gap-1.5 text-[11px] text-gray-600 mb-2">
                      <div><span class="text-gray-400">Khách:</span> <span class="font-medium text-gray-800">{{ ref.customerName || 'N/A' }}</span></div>
                      <div><span class="text-gray-400">Tổng tiền:</span> <span class="font-bold text-[#09f]">{{ ref.formattedTotal || '0 ₫' }}</span></div>
                      <div><span class="text-gray-400">PT thanh toán:</span> <span class="font-medium text-gray-800">{{ ref.paymentMethod || 'COD' }}</span></div>
                      <div><span class="text-gray-400">Trạng thái:</span> <span class="font-medium text-gray-800">{{ ref.paymentStatus || 'Chưa thanh toán' }}</span></div>
                    </div>
                    <div v-if="ref.items && ref.items.length" class="pt-2 border-t border-blue-100 text-[11px]">
                      <div class="text-[10px] text-gray-400 mb-1 font-medium">Sản phẩm trong đơn ({{ ref.items.length }}):</div>
                      <div v-for="(it, itIdx) in ref.items.slice(0, 3)" :key="itIdx" class="flex justify-between py-0.5 text-gray-700">
                        <span class="line-clamp-1 flex-1">{{ it.productName }}</span>
                        <span class="text-gray-500 font-semibold ml-2">x{{ it.quantity }}</span>
                      </div>
                      <div v-if="ref.items.length > 3" class="text-[10px] text-blue-600 mt-1">
                        + {{ ref.items.length - 3 }} linh kiện khác
                      </div>
                    </div>
                    <div class="mt-2.5 pt-2 border-t border-blue-100 flex justify-end">
                      <NuxtLink to="/orders" class="text-[11px] font-semibold text-[#09f] hover:underline flex items-center gap-1">
                        Xem chi tiết đơn hàng →
                      </NuxtLink>
                    </div>
                  </div>

                  <!-- Case 3: Revenue Report Card (from get_revenue_report) -->
                  <div v-else-if="ref.totalRevenue !== undefined && ref.deliveredOrders !== undefined && !ref.error" class="bg-gradient-to-br from-emerald-50 to-teal-50/50 border border-emerald-200 rounded-xl p-3 shadow-2xs">
                    <div class="flex items-center justify-between mb-2">
                      <div class="flex items-center gap-1.5 text-emerald-900 font-bold text-xs">
                        <span>📈 Báo Cáo Doanh Thu</span>
                      </div>
                      <span class="text-[10px] text-emerald-800 bg-emerald-100/80 px-2 py-0.5 rounded-full font-medium">
                        {{ ref.startDate }} → {{ ref.endDate }}
                      </span>
                    </div>
                    <div class="flex items-baseline gap-2 mb-2">
                      <span class="text-base sm:text-lg font-black text-emerald-700">{{ ref.formattedRevenue }}</span>
                      <span class="text-[10px] text-emerald-600 uppercase font-bold tracking-wider ml-1">Doanh thu hoàn tất</span>
                    </div>
                    <div class="grid grid-cols-2 gap-2 text-[11px] bg-white/80 p-2 rounded-lg border border-emerald-100">
                      <div>
                        <span class="text-gray-500">Đơn hoàn thành:</span>
                        <span class="font-bold text-gray-800 ml-1">{{ ref.deliveredOrders }}</span>
                      </div>
                      <div>
                        <span class="text-gray-500">Tổng đơn phát sinh:</span>
                        <span class="font-bold text-gray-800 ml-1">{{ ref.totalOrders }}</span>
                      </div>
                    </div>
                  </div>

                  <!-- Case 4: Low Stock Alert Card (from get_low_stock_products) -->
                  <div v-else-if="ref.lowStockItems && ref.lowStockItems.length && !ref.error" class="bg-gradient-to-br from-amber-50 to-orange-50/40 border border-amber-200 rounded-xl p-3 shadow-2xs">
                    <div class="flex items-center justify-between mb-2">
                      <div class="flex items-center gap-1 text-amber-900 font-bold text-xs">
                        <span>⚠️ Cảnh Báo Tồn Kho Thấp</span>
                      </div>
                      <span class="text-[10px] text-amber-800 bg-amber-100 px-2 py-0.5 rounded-full font-semibold">
                        Ngưỡng ≤ {{ ref.threshold }}
                      </span>
                    </div>
                    <div class="space-y-1.5 max-h-36 overflow-y-auto pr-1">
                      <div
                        v-for="(item, itemIdx) in ref.lowStockItems"
                        :key="itemIdx"
                        class="flex items-center justify-between bg-white/90 p-1.5 rounded-lg border border-amber-100 text-xs"
                      >
                        <div class="min-w-0 flex-1 pr-2">
                          <div class="font-medium text-gray-800 line-clamp-1">{{ item.productName }}</div>
                          <div class="text-[10px] text-gray-400">{{ item.variantName }}</div>
                        </div>
                        <span class="text-xs font-bold text-rose-600 bg-rose-50 px-2 py-0.5 rounded-full border border-rose-100 flex-shrink-0">
                          Còn {{ item.inStock }} cái
                        </span>
                      </div>
                    </div>
                    <div class="mt-2 pt-2 border-t border-amber-200/60 flex justify-end">
                      <NuxtLink to="/admin/products" class="text-[11px] font-semibold text-amber-900 hover:underline">
                        Đến trang Quản lý kho hàng →
                      </NuxtLink>
                    </div>
                  </div>

                  <!-- Case 6: Bill of Materials (BOM) Card (from build_bill_of_materials) -->
                  <div v-else-if="ref.items && ref.grandTotal !== undefined && !ref.error" class="bg-gradient-to-br from-indigo-50/70 to-blue-50/50 border border-indigo-200 rounded-xl p-3 shadow-2xs">
                    <div class="flex items-center justify-between mb-2">
                      <div class="flex items-center gap-1.5 text-indigo-900 font-bold text-xs">
                        <span>📋 Dự Toán Linh Kiện (BOM)</span>
                      </div>
                      <span :class="ref.stockStatus === 'FULL_IN_STOCK' ? 'text-emerald-800 bg-emerald-100' : 'text-amber-800 bg-amber-100'" class="text-[10px] px-2 py-0.5 rounded-full font-semibold">
                        {{ ref.stockStatus === 'FULL_IN_STOCK' ? 'Đủ 100% tại Shop' : 'Có linh kiện ngoài' }}
                      </span>
                    </div>

                    <!-- Shop items list -->
                    <div v-if="ref.items && ref.items.length" class="space-y-1.5 mb-2">
                      <div class="text-[10px] text-gray-500 font-medium">Linh kiện có sẵn tại ECShop ({{ ref.items.length }}):</div>
                      <div v-for="(it, itIdx) in ref.items.slice(0, 4)" :key="itIdx" class="flex items-center justify-between bg-white/90 p-1.5 rounded-lg border border-indigo-100 text-[11px]">
                        <NuxtLink :to="'/products/' + it.productId" class="line-clamp-1 flex-1 font-medium text-gray-800 hover:text-[#09f]">
                          {{ it.productName }} <span class="text-gray-400 text-[10px]">(x{{ it.quantity }})</span>
                        </NuxtLink>
                        <span class="font-bold text-indigo-700 ml-2 whitespace-nowrap">{{ it.formattedSubtotal }}</span>
                      </div>
                      <div v-if="ref.items.length > 4" class="text-[10px] text-indigo-600 text-right">
                        + {{ ref.items.length - 4 }} linh kiện khác trong bảng BOM
                      </div>
                    </div>                    <!-- Total cost breakdown -->
                    <div class="mt-2.5 pt-2 border-t border-indigo-200/80 flex items-center justify-between">
                      <span class="text-[11px] text-gray-600 font-medium">Tổng chi phí dự tính:</span>
                      <span class="text-sm font-extrabold text-indigo-900">{{ ref.formattedGrandTotal }}</span>
                    </div>

                    <!-- Add All BOM to Cart Action -->
                    <button
                      v-if="ref.items && ref.items.length"
                      type="button"
                      @click="addAllBomToCart(ref.items)"
                      :disabled="isAddingBomToCart"
                      class="mt-2.5 w-full py-1.5 px-3 bg-gradient-to-r from-indigo-600 to-blue-600 hover:from-indigo-700 hover:to-blue-700 active:scale-[0.99] text-white rounded-lg text-xs font-semibold flex items-center justify-center gap-1.5 shadow-2xs transition-all disabled:opacity-50 cursor-pointer"
                    >
                      <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                      </svg>
                      <span>{{ isAddingBomToCart ? 'Đang thêm vào giỏ...' : 'Thêm ' + ref.items.length + ' linh kiện BOM vào giỏ' }}</span>
                    </button>
                  </div>

                  <!-- Case 7: Component Compatibility Card (from check_component_compatibility) -->
                  <div v-else-if="(ref.compatibilityScore !== undefined || ref.isCompatible !== undefined) && !ref.error" class="bg-gradient-to-br from-slate-50 to-blue-50/50 border border-slate-200 rounded-xl p-3 shadow-2xs">
                    <div class="flex items-center justify-between mb-2">
                      <div class="flex items-center gap-1.5 font-bold text-xs" :class="ref.isCompatible ? 'text-emerald-900' : 'text-rose-900'">
                        <span>⚡ Kiểm Tra Tương Thích Kỹ Thuật</span>
                      </div>
                      <span :class="ref.isCompatible ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800'" class="text-[10px] px-2 py-0.5 rounded-full font-bold">
                        {{ ref.isCompatible ? 'Tương thích tốt' : 'Có xung đột' }} ({{ ref.compatibilityScore }}/100)
                      </span>
                    </div>

                    <p v-if="ref.summary" class="text-[11px] text-gray-700 mb-2 leading-relaxed bg-white/70 p-2 rounded-lg border border-slate-100">
                      {{ ref.summary }}
                    </p>

                    <!-- Errors -->
                    <div v-if="ref.errors && ref.errors.length" class="space-y-1 mb-2">
                      <div v-for="(err, eIdx) in ref.errors" :key="'e-'+eIdx" class="text-[10px] text-rose-700 bg-rose-50 px-2 py-1 rounded-md border border-rose-100 flex items-start gap-1">
                        <span class="font-bold flex-shrink-0">✕</span>
                        <span>{{ err }}</span>
                      </div>
                    </div>

                    <!-- Warnings -->
                    <div v-if="ref.warnings && ref.warnings.length" class="space-y-1 mb-2">
                      <div v-for="(warn, wIdx) in ref.warnings" :key="'w-'+wIdx" class="text-[10px] text-amber-800 bg-amber-50 px-2 py-1 rounded-md border border-amber-100 flex items-start gap-1">
                        <span class="font-bold flex-shrink-0">⚠</span>
                        <span>{{ warn }}</span>
                      </div>
                    </div>

                    <!-- Recommendations -->
                    <div v-if="ref.recommendations && ref.recommendations.length" class="space-y-1">
                      <div class="text-[10px] text-gray-500 font-semibold">Khuyến nghị kỹ thuật:</div>
                      <div v-for="(rec, rIdx) in ref.recommendations" :key="'r-'+rIdx" class="text-[10px] text-indigo-700 bg-indigo-50/60 px-2 py-1 rounded-md border border-indigo-100 flex items-start gap-1">
                        <span class="font-bold flex-shrink-0">💡</span>
                        <span>{{ rec }}</span>
                      </div>
                    </div>
                  </div>

                  <!-- Case 8: Order Analytics Card (from get_order_analytics) -->
                  <div v-else-if="ref.aov !== undefined && !ref.error" class="bg-gradient-to-br from-cyan-50 to-blue-50/60 border border-cyan-200 rounded-xl p-3 shadow-2xs">
                    <div class="flex items-center justify-between mb-2">
                      <div class="flex items-center gap-1.5 text-cyan-900 font-bold text-xs">
                        <span>📊 Phân Tích Chỉ Số Đơn Hàng</span>
                      </div>
                      <span class="text-[10px] text-cyan-800 bg-cyan-100 px-2 py-0.5 rounded-full font-semibold">
                        Quản trị Admin
                      </span>
                    </div>

                    <div class="grid grid-cols-2 gap-2 text-[11px] mb-2">
                      <div class="bg-white/90 p-2 rounded-lg border border-cyan-100">
                        <div class="text-[10px] text-gray-500 font-medium">Giá trị đơn TB (AOV)</div>
                        <div class="text-xs sm:text-sm font-bold text-cyan-800">{{ ref.formattedAov || ref.aov }}</div>
                      </div>
                      <div class="bg-white/90 p-2 rounded-lg border border-cyan-100">
                        <div class="text-[10px] text-gray-500 font-medium">Tỷ lệ hủy đơn</div>
                        <div class="text-xs sm:text-sm font-bold" :class="ref.cancellationRatePercent > 15 ? 'text-rose-600' : 'text-emerald-700'">
                          {{ ref.cancellationRatePercent }}%
                        </div>
                      </div>
                    </div>

                    <div v-if="ref.revenueGrowthPercent !== undefined" class="flex items-center justify-between text-[11px] bg-white/80 p-2 rounded-lg border border-cyan-100">
                      <span class="text-gray-600">Tăng trưởng kỳ trước (PoP):</span>
                      <span class="font-bold" :class="ref.revenueGrowthPercent >= 0 ? 'text-emerald-600' : 'text-rose-600'">
                        {{ ref.revenueGrowthPercent >= 0 ? '+' : '' }}{{ ref.revenueGrowthPercent }}%
                      </span>
                    </div>
                  </div>

                  <!-- Case 9: Inventory Report Card (from get_inventory_report) -->
                  <div v-else-if="ref.totalInventoryValue !== undefined && !ref.error" class="bg-gradient-to-br from-amber-50 to-orange-50/60 border border-amber-200 rounded-xl p-3 shadow-2xs">
                    <div class="flex items-center justify-between mb-2">
                      <div class="flex items-center gap-1.5 text-amber-900 font-bold text-xs">
                        <span>📦 Báo Cáo Giá Trị Vốn Lưu Kho</span>
                      </div>
                      <span class="text-[10px] text-amber-800 bg-amber-100 px-2 py-0.5 rounded-full font-semibold">
                        {{ ref.totalVariantsCount }} SKU
                      </span>
                    </div>

                    <div class="bg-white/90 p-2 rounded-lg border border-amber-100 mb-2">
                      <div class="text-[10px] text-gray-500">Tổng vốn lưu kho ước tính:</div>
                      <div class="text-base font-black text-amber-800">{{ ref.formattedTotalInventoryValue }}</div>
                    </div>

                    <div class="grid grid-cols-2 gap-2 text-[11px] mb-2">
                      <div class="bg-white/80 p-1.5 rounded-lg border border-amber-100">
                        <span class="text-gray-500 text-[10px]">Mã cạn hàng:</span>
                        <span class="font-bold text-rose-600 ml-1">{{ ref.outOfStockCount || 0 }}</span>
                      </div>
                      <div class="bg-white/80 p-1.5 rounded-lg border border-amber-100">
                        <span class="text-gray-500 text-[10px]">Tồn chậm:</span>
                        <span class="font-bold text-amber-700 ml-1">{{ ref.slowMovingCount || 0 }}</span>
                      </div>
                    </div>

                    <div class="pt-2 border-t border-amber-200/60 flex justify-end">
                      <NuxtLink to="/admin/products" class="text-[11px] font-semibold text-amber-900 hover:underline">
                        Đến trang quản lý kho hàng →
                      </NuxtLink>
                    </div>
                  </div>

                  <!-- Case 10: Customer Analytics Card (from get_customer_analytics) -->
                  <div v-else-if="ref.retentionRatePercent !== undefined && ref.totalCustomers !== undefined && !ref.error" class="bg-gradient-to-br from-purple-50 to-pink-50/50 border border-purple-200 rounded-xl p-3 shadow-2xs">
                    <div class="flex items-center justify-between mb-2">
                      <div class="flex items-center gap-1.5 text-purple-900 font-bold text-xs">
                        <span>👥 Báo Cáo Giữ Chân Khách Hàng</span>
                      </div>
                      <span class="text-[10px] text-purple-800 bg-purple-100 px-2 py-0.5 rounded-full font-semibold">
                        Retention: {{ ref.retentionRatePercent }}%
                      </span>
                    </div>

                    <div class="grid grid-cols-3 gap-1.5 text-center text-[11px]">
                      <div class="bg-white/90 p-1.5 rounded-lg border border-purple-100">
                        <div class="text-[9px] text-gray-400">Tổng khách</div>
                        <div class="font-bold text-purple-900">{{ ref.totalCustomers }}</div>
                      </div>
                      <div class="bg-white/90 p-1.5 rounded-lg border border-purple-100">
                        <div class="text-[9px] text-gray-400">Khách mới</div>
                        <div class="font-bold text-emerald-700">+{{ ref.newCustomers }}</div>
                      </div>
                      <div class="bg-white/90 p-1.5 rounded-lg border border-purple-100">
                        <div class="text-[9px] text-gray-400">Mua lại</div>
                        <div class="font-bold text-indigo-700">{{ ref.repeatCustomers }}</div>
                      </div>
                    </div>
                  </div>

                  <!-- Case 11: Product Performance Card (from get_product_performance) -->
                  <div v-else-if="ref.categoryBreakdown && ref.categoryBreakdown.length && !ref.error" class="bg-gradient-to-br from-violet-50 to-indigo-50/50 border border-violet-200 rounded-xl p-3 shadow-2xs">
                    <div class="flex items-center justify-between mb-2">
                      <div class="flex items-center gap-1.5 text-violet-900 font-bold text-xs">
                        <span>🏆 Hiệu Suất Bán Theo Danh Mục</span>
                      </div>
                      <span class="text-[10px] text-violet-800 bg-violet-100 px-2 py-0.5 rounded-full font-semibold">
                        {{ ref.categoryBreakdown.length }} ngành hàng
                      </span>
                    </div>

                    <div class="space-y-1.5 max-h-36 overflow-y-auto pr-1">
                      <div v-for="(cat, cIdx) in ref.categoryBreakdown" :key="cIdx" class="flex items-center justify-between bg-white/90 p-1.5 rounded-lg border border-violet-100 text-[11px]">
                        <span class="font-medium text-gray-800 line-clamp-1">{{ cat.categoryName }}</span>
                        <div class="text-right flex-shrink-0 ml-2">
                          <span class="font-bold text-violet-800">{{ cat.formattedRevenue || cat.revenue }}</span>
                          <span class="text-[9px] text-gray-400 ml-1">({{ cat.productCount }} SP)</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- Case 12: Customer Inquiry Analytics Card (from get_customer_inquiry_analytics) -->
                  <div v-else-if="ref.topProjectTopics && ref.popularSearches && !ref.error" class="bg-gradient-to-br from-teal-50 to-emerald-50/50 border border-teal-200 rounded-xl p-3 shadow-2xs space-y-2.5">
                    <div class="flex items-center justify-between">
                      <div class="flex items-center gap-1.5 text-teal-900 font-bold text-xs">
                        <span>📊 Xu Hướng Nhu Cầu & Chủ Đề Chat</span>
                      </div>
                      <span class="text-[10px] text-teal-800 bg-teal-100 px-2 py-0.5 rounded-full font-semibold">
                        {{ ref.totalLogsAnalyzed || 0 }} tương tác
                      </span>
                    </div>

                    <!-- Top Projects -->
                    <div>
                      <div class="text-[10px] font-bold text-teal-800 uppercase tracking-wider mb-1">Top Dự Án Khách Quan Tâm:</div>
                      <div class="space-y-1">
                        <div v-for="(p, pIdx) in ref.topProjectTopics.slice(0, 3)" :key="'proj-'+pIdx" class="flex items-center justify-between bg-white/90 px-2 py-1 rounded-lg border border-teal-100 text-[11px]">
                          <span class="font-medium text-gray-800 line-clamp-1">#{{ pIdx + 1 }} {{ p.topic }}</span>
                          <span class="font-bold text-teal-700 ml-2 shrink-0">{{ p.inquiryCount }} lượt</span>
                        </div>
                      </div>
                    </div>

                    <!-- Top Web Searches -->
                    <div>
                      <div class="text-[10px] font-bold text-teal-800 uppercase tracking-wider mb-1">Top Tìm Kiếm Trên Web:</div>
                      <div class="flex flex-wrap gap-1">
                        <span v-for="(s, sIdx) in ref.popularSearches.slice(0, 4)" :key="'s-'+sIdx" class="text-[10px] bg-white px-2 py-0.5 rounded-md border border-teal-200 text-teal-900 font-medium">
                          {{ s.keyword }} ({{ s.searchCount }})
                        </span>
                      </div>
                    </div>

                    <!-- Unmet Demand -->
                    <div v-if="ref.unmetDemand && ref.unmetDemand.length" class="pt-1.5 border-t border-teal-200/60">
                      <div class="text-[10px] font-bold text-rose-700 uppercase tracking-wider mb-1">⚠️ Nhu Cầu Cần Bổ Sung:</div>
                      <p class="text-[11px] text-rose-800 leading-snug font-medium">
                        {{ ref.unmetDemand[0] }}
                      </p>
                    </div>
                  </div>

                  <!-- Case 5: Single Product Stock Details (from check_stock) -->
                  <div v-else-if="ref.productId && ref.variants && !ref.error" class="bg-white border border-gray-200 rounded-xl p-2.5 shadow-2xs">
                    <div class="flex items-center justify-between mb-1.5">
                      <span class="text-xs font-bold text-gray-800 line-clamp-1">{{ ref.productName }}</span>
                      <span class="text-[10px] font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-full">
                        Tổng tồn: {{ ref.totalStock }}
                      </span>
                    </div>
                    <div class="space-y-1 text-[11px]">
                      <div v-for="v in ref.variants" :key="v.variantId" class="flex justify-between items-center py-1 border-t border-gray-100 first:border-0 text-gray-600">
                        <span>{{ v.variantName || 'Biến thể' }}</span>
                        <span class="font-medium" :class="v.inStock > 0 ? 'text-gray-800' : 'text-rose-500'">{{ v.inStock }} còn lại</span>
                      </div>
                    </div>
                  </div>
                
                  <!-- Case 13: Warranty Lookup Card (from check_warranty) -->
                  <div v-else-if="ref.type === 'WARRANTY_LOOKUP' || (ref.serialNumber && ref.status && !ref.error)" class="bg-gradient-to-br from-emerald-50/70 to-teal-50/50 border border-emerald-200 rounded-xl p-3 shadow-2xs space-y-2">
                    <div class="flex items-center justify-between pb-2 border-b border-emerald-100">
                      <div class="flex items-center gap-1.5">
                        <span class="text-sm">🛡️</span>
                        <span class="font-mono text-xs font-bold text-gray-900">{{ ref.serialNumber }}</span>
                      </div>
                      <span
                        class="text-[10px] font-bold px-2 py-0.5 rounded-full"
                        :class="ref.isExpired ? 'bg-red-100 text-red-700' : 'bg-emerald-100 text-emerald-800'"
                      >
                        {{ ref.isExpired ? 'Hết hạn bảo hành' : `Còn ${ref.daysRemaining} ngày` }}
                      </span>
                    </div>

                    <div class="text-[11px] text-gray-700 space-y-0.5">
                      <div class="font-semibold text-gray-900 line-clamp-1">{{ ref.productName }}</div>
                      <div v-if="ref.variantName" class="text-gray-500">Phân loại: {{ ref.variantName }}</div>
                      <div class="flex justify-between text-gray-500 text-[10px] pt-1">
                        <span>Hạn bảo hành: {{ ref.warrantyMonths || 12 }} tháng</span>
                        <span class="font-medium text-emerald-800" v-if="!ref.isExpired">Đang có hiệu lực</span>
                      </div>
                    </div>

                    <div class="pt-1 flex items-center justify-between">
                      <NuxtLink
                        to="/warranty"
                        class="text-[11px] text-[#09f] hover:underline font-bold inline-flex items-center gap-1"
                      >
                        Mở trang bảo hành & tạo phiếu →
                      </NuxtLink>
                    </div>
                  </div>
                </div>
              </template>

              <!-- Tool Used Badges -->
              <div v-if="msg.toolsUsed && msg.toolsUsed.length" class="flex flex-wrap gap-1 px-1">
                <span
                  v-for="tool in msg.toolsUsed"
                  :key="tool"
                  class="inline-flex items-center gap-1 text-[10px] font-medium px-2 py-0.5 rounded-full bg-blue-50 text-blue-600 border border-blue-100"
                >
                  ⚙️ {{ formatToolName(tool) }}
                </span>
              </div>
            </div>
          </div>
        </template>

        <!-- Loading Indicator -->
        <div v-if="isLoading" class="flex gap-2 items-center text-gray-400 text-xs">
          <div class="w-6 h-6 rounded-full bg-blue-100 text-[#09f] flex-shrink-0 flex items-center justify-center text-[10px] font-bold">
            AI
          </div>
          <div class="px-3 py-2 bg-white border border-gray-200 rounded-2xl rounded-tl-xs flex items-center gap-1.5 shadow-2xs">
            <span class="w-1.5 h-1.5 rounded-full bg-[#09f] animate-bounce"></span>
            <span class="w-1.5 h-1.5 rounded-full bg-[#09f] animate-bounce [animation-delay:0.2s]"></span>
            <span class="w-1.5 h-1.5 rounded-full bg-[#09f] animate-bounce [animation-delay:0.4s]"></span>
            <span class="text-[11px] text-gray-400 ml-1">Đang trả lời...</span>
          </div>
        </div>
      </div>

      <!-- Footer / Input Form -->
      <div class="p-3 bg-white border-t border-gray-100">
        <form @submit.prevent="sendMessage" class="flex items-center gap-2">
          <input
            v-model="inputMessage"
            type="text"
            placeholder="Nhắn tin hoặc hỏi về sản phẩm..."
            :disabled="isLoading"
            class="flex-1 px-3.5 py-2 text-xs sm:text-sm bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:border-[#09f] focus:bg-white transition-all disabled:opacity-50"
          />
          <button
            type="submit"
            :disabled="isLoading || !inputMessage.trim()"
            class="w-9 h-9 flex items-center justify-center bg-[#09f] text-white rounded-xl hover:bg-[#0077cc] disabled:opacity-40 disabled:cursor-not-allowed transition-all flex-shrink-0 shadow-xs"
          >
            <svg class="w-4 h-4 transform rotate-90" fill="currentColor" viewBox="0 0 20 20">
              <path d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z" />
            </svg>
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue'
import { useAuth } from '~/composables/useAuth'
import { useCart } from '~/composables/useCart'
import { useNotification } from '~/composables/useNotification'

interface Message {
  role: 'user' | 'assistant'
  content: string
  toolsUsed?: string[]
  referencedData?: any[]
}

const isOpen = ref(false)
const inputMessage = ref('')
const isLoading = ref(false)
const sessionId = ref<string | null>(null)
const messages = ref<Message[]>([])
const messagesContainer = ref<HTMLElement | null>(null)

const { user, token } = useAuth()
const { addToCart } = useCart()
const { success: showNotificationSuccess, error: showNotificationError } = useNotification()
const isAddingBomToCart = ref(false)

const addAllBomToCart = async (items: any[]) => {
  if (!items || !items.length) return
  isAddingBomToCart.value = true
  let successCount = 0
  let failedCount = 0
  try {
    for (const item of items) {
      if (item.variantId) {
        const price = item.unitPrice || 0
        const qty = item.quantity || 1
        const res = await addToCart(item.variantId, qty, price)
        if (res !== false) {
          successCount++
        } else {
          failedCount++
        }
      }
    }
    if (successCount > 0) {
      showNotificationSuccess('Giỏ hàng', `Đã thêm ${successCount} linh kiện từ BOM vào giỏ hàng!`)
    }
    if (failedCount > 0) {
      showNotificationError('Giỏ hàng', `${failedCount} linh kiện chưa thêm được (yêu cầu đăng nhập hoặc hết hàng)`)
    }
  } catch (err: any) {
    showNotificationError('Lỗi', err?.message || 'Có lỗi xảy ra khi thêm vào giỏ')
  } finally {
    isAddingBomToCart.value = false
  }
}

const suggestions = computed(() => {
  const isAdmin = user.value?.role === 'ROLE_ADMIN' || user.value?.role === 'ADMIN'
  if (isAdmin) {
    return [
      'Người dùng thường hỏi bạn về vấn đề gì?',
      'Báo cáo doanh thu tháng này',
      'Phân tích tỷ lệ hủy đơn và giá trị đơn trung bình',
      'Báo cáo tổng giá trị tồn kho và dự báo nhập hàng',
      'Xếp hạng hiệu suất và danh mục bán chạy nhất',
      'Phân tích khách hàng mới và tỷ lệ mua lại'
    ]
  }
  return [
    'Chào bạn, hôm nay thế nào?',
    'Tư vấn bo mạch Arduino Uno R3 và giá bán',
    'ESP32 DevKit V1 còn hàng không?',
    'So sánh Arduino Uno R3 với Raspberry Pi 5',
    'Gợi ý linh kiện Arduino dưới 200k'
  ]
})

onMounted(() => {
  if (import.meta.client) {
    const savedSession = sessionStorage.getItem('ai_chat_session_id')
    if (savedSession) {
      sessionId.value = savedSession
    }
  }
})

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const formatToolName = (tool: string) => {
  switch (tool) {
    case 'search_products': return 'Tìm kiếm sản phẩm'
    case 'recommend_products': return 'Gợi ý sản phẩm'
    case 'compare_products': return 'So sánh sản phẩm'
    case 'check_stock': return 'Kiểm tra tồn kho'
    case 'get_low_stock_products': return 'Cảnh báo tồn kho'
    case 'get_top_selling_products': return 'Top bán chạy'
    case 'get_revenue_report': return 'Báo cáo doanh thu'
    case 'track_order': return 'Tra cứu đơn hàng'
    case 'find_project_components': return 'Tìm linh kiện dự án'
    case 'check_component_compatibility': return 'Kiểm tra tương thích'
    case 'build_bill_of_materials': return 'Lập bảng BOM'
    case 'get_order_analytics': return 'Phân tích đơn hàng'
    case 'get_inventory_report': return 'Báo cáo tồn kho'
    case 'get_product_performance': return 'Hiệu suất sản phẩm'
    case 'get_customer_analytics': return 'Phân tích khách hàng'
    case 'get_customer_inquiry_analytics': return 'Phân tích nhu cầu & chủ đề chat'
    case 'check_warranty': return 'Tra cứu bảo hành'
    default: return tool
  }
}

const resetSession = () => {
  sessionId.value = null
  messages.value = []
  if (import.meta.client) {
    sessionStorage.removeItem('ai_chat_session_id')
  }
}

const sendQuickMessage = (text: string) => {
  inputMessage.value = text
  sendMessage()
}

const sendMessage = async () => {
  const query = inputMessage.value.trim()
  if (!query || isLoading.value) return

  // Push user message to UI
  messages.value.push({ role: 'user', content: query })
  inputMessage.value = ''
  isLoading.value = true
  scrollToBottom()

  try {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json'
    }
    if (token.value) {
      headers['Authorization'] = `Bearer ${token.value}`
    }

    let response: any = null
    try {
      response = await $fetch('http://localhost:8080/api/chat', {
        method: 'POST',
        headers,
        body: {
          sessionId: sessionId.value,
          message: query
        }
      })
    } catch (apiErr: any) {
      // Nếu lỗi 401 hoặc 500 liên quan tới token hết hạn/không hợp lệ, tự động thử lại ẩn danh
      if (headers['Authorization']) {
        console.warn('Token không hợp lệ hoặc đã hết hạn, tự động gửi lại với tư cách Khách:', apiErr)
        delete headers['Authorization']
        response = await $fetch('http://localhost:8080/api/chat', {
          method: 'POST',
          headers,
          body: {
            sessionId: sessionId.value,
            message: query
          }
        })
      } else {
        throw apiErr
      }
    }

    if (response?.sessionId) {
      sessionId.value = response.sessionId
      if (import.meta.client) {
        sessionStorage.setItem('ai_chat_session_id', response.sessionId)
      }
    }

    messages.value.push({
      role: 'assistant',
      content: response?.message || 'Không có phản hồi từ máy chủ.',
      toolsUsed: response?.toolsUsed || [],
      referencedData: response?.referencedData || []
    })
  } catch (error: any) {
    console.error('Chat error details:', error)
    const errText = error?.data?.message || error?.data?.error || error?.message || ''
    messages.value.push({
      role: 'assistant',
      content: `Xin lỗi, hiện tại không thể kết nối tới dịch vụ AI (${errText || 'Lỗi mạng hoặc Backend chưa phản hồi'}). Vui lòng thử lại sau.`
    })
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

const hasProducts = (ref: any) => {
  if (!ref || ref.error) return false
  return !!((ref.products && ref.products.length) || (ref.topSellingProducts && ref.topSellingProducts.length))
}

const getProductsFromRef = (ref: any) => {
  if (!ref || ref.error) return []
  if (Array.isArray(ref.products)) return ref.products
  if (Array.isArray(ref.topSellingProducts)) return ref.topSellingProducts
  return []
}

const formatPriceDisplay = (p: any) => {
  const val = p.minPrice || p.price || p.salePrice || 0
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

const getOrderStatusBadgeClass = (status?: string) => {
  switch (status?.toUpperCase()) {
    case 'PENDING': return 'bg-amber-100 text-amber-800'
    case 'CONFIRMED': return 'bg-blue-100 text-blue-800'
    case 'SHIPPED': return 'bg-indigo-100 text-indigo-800'
    case 'DELIVERED': return 'bg-emerald-100 text-emerald-800'
    case 'CANCELLED': return 'bg-rose-100 text-rose-800'
    default: return 'bg-gray-100 text-gray-700'
  }
}

const renderMarkdown = (text: string): string => {
  if (!text) return ''

  // 1. Sanitize HTML tags to prevent XSS
  let html = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  // 2. Fenced code blocks
  html = html.replace(/```([a-zA-Z0-9_]*)\n([\s\S]*?)```/g, (_, _lang, code) => {
    return `<pre class="bg-gray-800 text-gray-100 rounded-xl p-2.5 my-2 text-xs font-mono overflow-x-auto shadow-inner"><code>${code.trim()}</code></pre>`
  })

  // 3. Inline code
  html = html.replace(/`([^`]+)`/g, '<code class="bg-blue-50 text-blue-700 font-mono text-[11px] px-1.5 py-0.5 rounded border border-blue-100">$1</code>')

  // 4. Headings
  html = html.replace(/^### (.*$)/gim, '<h4 class="font-bold text-gray-900 text-xs mt-2 mb-1">$1</h4>')
  html = html.replace(/^## (.*$)/gim, '<h3 class="font-bold text-gray-900 text-sm mt-2.5 mb-1 text-[#0077cc]">$1</h3>')
  html = html.replace(/^# (.*$)/gim, '<h2 class="font-bold text-gray-900 text-sm mt-3 mb-1.5">$1</h2>')

  // 5. Bold & Italic
  html = html.replace(/\*\*([^*]+)\*\*/g, '<strong class="font-bold text-gray-900">$1</strong>')
  html = html.replace(/\*([^*]+)\*/g, '<em class="italic text-gray-700">$1</em>')

  // 6. Markdown Tables
  const tableRegex = /((?:\|[^\n]+\|\r?\n)+)/g
  html = html.replace(tableRegex, (match) => {
    const lines = match.trim().split(/\r?\n/).filter(line => line.trim().startsWith('|'))
    if (lines.length < 2) return match
    
    const isTable = lines.some(l => /^\|[\s-:]+\|/.test(l))
    if (!isTable) return match

    let tableHtml = '<div class="overflow-x-auto my-2 rounded-lg border border-gray-200"><table class="w-full text-[11px] text-left border-collapse">'
    let isHeader = true

    for (const line of lines) {
      if (/^\|[\s-:]+\|/.test(line)) {
        isHeader = false
        continue
      }
      const cells = line.split('|').slice(1, -1).map(c => c.trim())
      tableHtml += `<tr class="${isHeader ? 'bg-gray-100 font-semibold text-gray-800' : 'border-t border-gray-100 hover:bg-gray-50/80'}">`
      for (const cell of cells) {
        tableHtml += isHeader 
          ? `<th class="p-1.5 border-r border-gray-200 last:border-r-0">${cell}</th>`
          : `<td class="p-1.5 border-r border-gray-200 last:border-r-0">${cell}</td>`
      }
      tableHtml += '</tr>'
    }
    tableHtml += '</table></div>'
    return tableHtml
  })

  // 7. Markdown Links
  html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" class="text-[#09f] hover:underline font-medium inline-flex items-center gap-0.5">$1 ↗</a>')

  // 8. Lists (bullet & numbered)
  html = html.replace(/^\s*[-*]\s+(.*$)/gim, '<li class="ml-4 list-disc text-gray-800 leading-relaxed">$1</li>')
  html = html.replace(/^\s*(\d+)\.\s+(.*$)/gim, '<li class="ml-4 list-decimal text-gray-800 leading-relaxed">$2</li>')

  // 9. Line breaks
  html = html.replace(/\n/g, '<br>')

  return html
}

</script>

<style scoped>
:deep(.ai-rendered-content ul),
:deep(.ai-rendered-content ol) {
  margin: 0.35rem 0;
  padding-left: 1.25rem;
}
:deep(.ai-rendered-content li) {
  margin-bottom: 0.2rem;
}
:deep(.ai-rendered-content strong) {
  color: #111827;
}
:deep(.ai-rendered-content table) {
  width: 100%;
  margin: 0.5rem 0;
}
</style>
