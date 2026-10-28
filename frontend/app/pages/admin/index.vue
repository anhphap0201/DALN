<template>
  <div class="space-y-6 antialiased">

      <!-- ==================== 1. DASHBOARD HEADER & LIVE TELEMETRY ==================== -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 sm:gap-4 pb-2 border-b border-slate-200/80">
        <div>
          <div class="flex items-center gap-2 sm:gap-3">
            <span class="w-2.5 h-2.5 sm:w-3 sm:h-3 rounded-full bg-[#09f] shadow-[0_0_10px_rgba(0,153,255,0.6)] animate-pulse shrink-0"></span>
            <h1 class="text-xl sm:text-2xl 2xl:text-3xl font-extrabold tracking-tight text-slate-900">
              Dashboard Quản Trị
            </h1>
          </div>
          <p class="text-slate-500 text-xs sm:text-sm mt-0.5 sm:mt-1">
            Trung tâm điều hành kinh doanh & phân tích hiệu suất bán linh kiện điện tử ECShop
          </p>
        </div>

        <!-- Toolbar: Date & Refresh -->
        <div class="flex flex-wrap sm:flex-nowrap items-center gap-2 sm:gap-3 w-full sm:w-auto">
          <div class="bg-white px-3 py-1.5 sm:px-3.5 sm:py-2 rounded-xl border border-slate-200 shadow-xs text-xs font-medium text-slate-600 flex items-center justify-between sm:justify-start gap-2 flex-1 sm:flex-none">
            <div class="flex items-center gap-1.5 sm:gap-2 truncate">
              <svg class="w-4 h-4 text-[#09f] shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              <span class="truncate">{{ currentDateFormatted }}</span>
            </div>
            <span v-if="lastUpdated" class="text-slate-400 text-[11px] border-l border-slate-200 pl-2 shrink-0">
              Đồng bộ: {{ lastUpdated }}
            </span>
          </div>

          <button
            @click="fetchDashboard"
            :disabled="loading"
            class="inline-flex items-center justify-center gap-2 px-3.5 py-1.5 sm:px-4 sm:py-2 bg-white border border-slate-200 hover:border-slate-300 rounded-xl text-xs sm:text-sm font-semibold text-slate-700 hover:text-slate-900 shadow-xs hover:shadow-sm transition-all duration-200 disabled:opacity-50 cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#09f] shrink-0"
            aria-label="Làm mới dữ liệu dashboard"
          >
            <svg class="w-4 h-4 text-[#09f]" :class="{ 'animate-spin': loading }" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            <span>Làm mới</span>
          </button>
        </div>
      </div>

      <!-- ==================== SKELETON LOADING STATE (CLS < 0.1) ==================== -->
      <div v-if="loading && !dashboard" class="space-y-6 sm:space-y-8 animate-pulse" aria-busy="true" aria-label="Đang nạp dữ liệu thống kê">
        <!-- 4 KPI Card Skeletons -->
        <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-3 sm:gap-4">
          <div v-for="i in 4" :key="'sk-kpi-' + i" class="bg-white rounded-2xl border border-slate-200/80 p-4 sm:p-5 shadow-xs space-y-4">
            <div class="flex items-center justify-between">
              <div class="h-3 w-16 bg-slate-200 rounded-md"></div>
              <div class="w-9 h-9 bg-slate-100 rounded-xl"></div>
            </div>
            <div class="h-8 w-32 bg-slate-200 rounded-lg"></div>
            <div class="h-4 w-44 bg-slate-100 rounded-md pt-2 border-t border-slate-100"></div>
          </div>
        </div>

        <!-- Chart Grid Skeleton -->
        <div class="grid grid-cols-1 xl:grid-cols-3 gap-4 sm:gap-6">
          <div class="xl:col-span-2 bg-white rounded-2xl border border-slate-200/80 p-4 sm:p-6 shadow-xs space-y-4">
            <div class="h-5 w-48 bg-slate-200 rounded-md"></div>
            <div class="h-52 sm:h-64 bg-slate-100 rounded-xl"></div>
          </div>
          <div class="bg-white rounded-2xl border border-slate-200/80 p-4 sm:p-6 shadow-xs space-y-4">
            <div class="h-5 w-36 bg-slate-200 rounded-md"></div>
            <div class="h-52 sm:h-64 bg-slate-100 rounded-xl"></div>
          </div>
        </div>

        <!-- Secondary Grid Skeleton -->
        <div class="grid grid-cols-1 xl:grid-cols-3 gap-4 sm:gap-6">
          <div class="bg-white rounded-2xl border border-slate-200/80 p-4 sm:p-6 shadow-xs space-y-4">
            <div class="h-5 w-36 bg-slate-200 rounded-md"></div>
            <div class="h-44 sm:h-48 bg-slate-100 rounded-full mx-auto w-44 sm:w-48"></div>
          </div>
          <div class="xl:col-span-2 bg-white rounded-2xl border border-slate-200/80 p-4 sm:p-6 shadow-xs space-y-4">
            <div class="h-5 w-48 bg-slate-200 rounded-md"></div>
            <div class="space-y-3">
              <div v-for="j in 5" :key="'sk-row-' + j" class="h-8 bg-slate-100 rounded-lg"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- ==================== ERROR STATE ==================== -->
      <div
        v-else-if="hasError && !dashboard"
        class="bg-white rounded-2xl border border-rose-200 p-10 text-center shadow-xs max-w-xl mx-auto my-12"
        role="alert"
      >
        <div class="w-14 h-14 rounded-full bg-rose-50 text-rose-500 mx-auto flex items-center justify-center mb-4">
          <svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <h3 class="text-lg font-bold text-slate-900 mb-1">Không thể tải dữ liệu Dashboard</h3>
        <p class="text-sm text-slate-500 mb-6">Đã xảy ra lỗi khi kết nối tới máy chủ. Vui lòng kiểm tra lại dịch vụ backend hoặc thử lại.</p>
        <button
          @click="fetchDashboard"
          class="px-5 py-2.5 bg-[#09f] hover:bg-[#0077cc] text-white rounded-xl text-sm font-semibold shadow-md transition-all cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#09f]"
        >
          Thử kết nối lại
        </button>
      </div>

      <!-- ==================== DASHBOARD CONTENT ==================== -->
      <template v-else-if="dashboard">

        <!-- 2. HÀNG 4 THẺ THỐNG KÊ CHU KỲ (HÔM NAY, TUẦN NÀY, THÁNG NÀY, NĂM NÀY) -->
        <section aria-label="Thống kê doanh thu theo kỳ">
          <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-3 sm:gap-4">
            <!-- Thẻ 1: Hôm nay -->
            <div class="bg-white rounded-2xl border border-slate-200/80 border-t-4 border-t-[#09f] hover:border-[#09f] p-4 sm:p-5 shadow-sm hover:shadow-md transition-all duration-300 relative overflow-hidden group">
              <div class="flex items-center justify-between mb-3 relative">
                <span class="text-[11px] font-extrabold text-slate-500 uppercase tracking-wider">Hôm nay</span>
                <div class="w-10 h-10 sm:w-11 sm:h-11 bg-gradient-to-tr from-[#0088ea] to-[#09f] text-white rounded-xl flex items-center justify-center shadow-md shadow-[#09f]/30 transition-transform duration-300 group-hover:scale-110 shrink-0">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
              </div>
              <p class="text-xl sm:text-2xl font-black text-slate-900 tracking-tight truncate">
                {{ formatPrice(dashboard.today?.revenue) }}
              </p>
              <div class="flex flex-wrap items-center gap-2 sm:gap-3 mt-3 pt-3 border-t border-slate-100 text-xs text-slate-500">
                <span><strong class="font-bold text-slate-900">{{ dashboard.today?.orderCount || 0 }}</strong> đơn hàng</span>
                <span class="text-slate-300 hidden xs:inline">•</span>
                <span class="inline-flex items-center gap-1 font-bold text-white bg-[#09f] px-2 py-0.5 rounded-md text-[11px] shadow-xs">
                  +{{ dashboard.today?.newUsers || 0 }} khách mới
                </span>
              </div>
            </div>

            <!-- Thẻ 2: Tuần này -->
            <div class="bg-white rounded-2xl border border-slate-200/80 border-t-4 border-t-emerald-500 hover:border-emerald-500 p-4 sm:p-5 shadow-sm hover:shadow-md transition-all duration-300 relative overflow-hidden group">
              <div class="flex items-center justify-between mb-3 relative">
                <span class="text-[11px] font-extrabold text-slate-500 uppercase tracking-wider">Tuần này</span>
                <div class="w-10 h-10 sm:w-11 sm:h-11 bg-gradient-to-tr from-emerald-700 to-emerald-500 text-white rounded-xl flex items-center justify-center shadow-md shadow-emerald-500/30 transition-transform duration-300 group-hover:scale-110 shrink-0">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
              </div>
              <p class="text-xl sm:text-2xl font-black text-slate-900 tracking-tight truncate">
                {{ formatPrice(dashboard.thisWeek?.revenue) }}
              </p>
              <div class="flex flex-wrap items-center gap-2 sm:gap-3 mt-3 pt-3 border-t border-slate-100 text-xs text-slate-500">
                <span><strong class="font-bold text-slate-900">{{ dashboard.thisWeek?.orderCount || 0 }}</strong> đơn hàng</span>
                <span class="text-slate-300 hidden xs:inline">•</span>
                <span class="inline-flex items-center gap-1 font-bold text-white bg-emerald-600 px-2 py-0.5 rounded-md text-[11px] shadow-xs">
                  +{{ dashboard.thisWeek?.newUsers || 0 }} khách mới
                </span>
              </div>
            </div>

            <!-- Thẻ 3: Tháng này -->
            <div class="bg-white rounded-2xl border border-slate-200/80 border-t-4 border-t-purple-600 hover:border-purple-600 p-4 sm:p-5 shadow-sm hover:shadow-md transition-all duration-300 relative overflow-hidden group">
              <div class="flex items-center justify-between mb-3 relative">
                <span class="text-[11px] font-extrabold text-slate-500 uppercase tracking-wider">Tháng này</span>
                <div class="w-10 h-10 sm:w-11 sm:h-11 bg-gradient-to-tr from-purple-800 to-purple-600 text-white rounded-xl flex items-center justify-center shadow-md shadow-purple-600/30 transition-transform duration-300 group-hover:scale-110 shrink-0">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                  </svg>
                </div>
              </div>
              <p class="text-xl sm:text-2xl font-black text-slate-900 tracking-tight truncate">
                {{ formatPrice(dashboard.thisMonth?.revenue) }}
              </p>
              <div class="flex flex-wrap items-center gap-2 sm:gap-3 mt-3 pt-3 border-t border-slate-100 text-xs text-slate-500">
                <span><strong class="font-bold text-slate-900">{{ dashboard.thisMonth?.orderCount || 0 }}</strong> đơn hàng</span>
                <span class="text-slate-300 hidden xs:inline">•</span>
                <span class="inline-flex items-center gap-1 font-bold text-white bg-purple-600 px-2 py-0.5 rounded-md text-[11px] shadow-xs">
                  +{{ dashboard.thisMonth?.newUsers || 0 }} khách mới
                </span>
              </div>
            </div>

            <!-- Thẻ 4: Năm này -->
            <div class="bg-white rounded-2xl border border-slate-200/80 border-t-4 border-t-amber-500 hover:border-amber-500 p-4 sm:p-5 shadow-sm hover:shadow-md transition-all duration-300 relative overflow-hidden group">
              <div class="flex items-center justify-between mb-3 relative">
                <span class="text-[11px] font-extrabold text-slate-500 uppercase tracking-wider">Năm này</span>
                <div class="w-10 h-10 sm:w-11 sm:h-11 bg-gradient-to-tr from-amber-600 to-amber-500 text-white rounded-xl flex items-center justify-center shadow-md shadow-amber-500/30 transition-transform duration-300 group-hover:scale-110 shrink-0">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                  </svg>
                </div>
              </div>
              <p class="text-xl sm:text-2xl font-black text-slate-900 tracking-tight truncate">
                {{ formatPrice(dashboard.thisYear?.revenue) }}
              </p>
              <div class="flex flex-wrap items-center gap-2 sm:gap-3 mt-3 pt-3 border-t border-slate-100 text-xs text-slate-500">
                <span><strong class="font-bold text-slate-900">{{ dashboard.thisYear?.orderCount || 0 }}</strong> đơn hàng</span>
                <span class="text-slate-300 hidden xs:inline">•</span>
                <span class="inline-flex items-center gap-1 font-bold text-white bg-amber-600 px-2 py-0.5 rounded-md text-[11px] shadow-xs">
                  +{{ dashboard.thisYear?.newUsers || 0 }} khách mới
                </span>
              </div>
            </div>
          </div>
        </section>

        <!-- ==================== 3. BIỂU ĐỒ 1 & 2: XU HƯỚNG DOANH THU & SỐ LƯỢNG ĐƠN HÀNG ==================== -->
        <section aria-label="Biểu đồ phân tích doanh thu và số lượng đơn hàng" class="grid grid-cols-1 xl:grid-cols-3 gap-4 sm:gap-6">

          <!-- BIỂU ĐỒ 1: Area Spline Chart (Chiếm 2 cột trên xl+) -->
          <div class="xl:col-span-2 bg-white rounded-2xl border border-slate-200/80 p-4 sm:p-6 shadow-xs hover:shadow-sm transition-all flex flex-col justify-between relative">
            <div>
              <!-- Header biểu đồ & nút chọn khoảng thời gian -->
              <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-4 sm:mb-6">
                <div>
                  <div class="flex items-center gap-2">
                    <span class="w-2.5 h-2.5 rounded-full bg-[#09f] shrink-0"></span>
                    <h2 class="text-sm sm:text-base font-bold text-slate-900">Xu Hướng Doanh Thu Bán Hàng</h2>
                  </div>
                  <p class="text-xs text-slate-500 mt-0.5">Biểu đồ đường cong diện tích biểu diễn giá trị đơn hàng theo từng ngày</p>
                </div>

                <!-- Range Selector (7 / 14 / 30 ngày) -->
                <div class="flex items-center bg-slate-100/80 p-1 rounded-xl text-xs font-semibold self-start sm:self-auto border border-slate-200/50">
                  <button
                    v-for="days in [7, 14, 30]"
                    :key="days"
                    @click="changeRange(days)"
                    :disabled="chartLoading"
                    :class="selectedRange === days ? 'bg-white text-[#09f] shadow-xs' : 'text-slate-600 hover:text-slate-900'"
                    class="px-2.5 sm:px-3.5 py-1 sm:py-1.5 rounded-lg transition-all cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#09f] text-[11px] sm:text-xs"
                  >
                    {{ days }} ngày
                  </button>
                </div>
              </div>

              <!-- Thẻ tóm tắt chỉ số nhanh trong kỳ -->
              <div class="grid grid-cols-2 sm:grid-cols-3 gap-2 sm:gap-3 mb-4 sm:mb-6 bg-slate-50/80 p-3 sm:p-3.5 rounded-xl border border-slate-100">
                <div>
                  <p class="text-[10px] sm:text-[11px] text-slate-500 uppercase tracking-wider font-semibold">Tổng doanh thu kỳ</p>
                  <p class="text-sm sm:text-base font-extrabold text-[#09f] mt-0.5 truncate">{{ formatPrice(periodTotalRevenue) }}</p>
                </div>
                <div>
                  <p class="text-[10px] sm:text-[11px] text-slate-500 uppercase tracking-wider font-semibold">Trung bình / ngày</p>
                  <p class="text-sm sm:text-base font-extrabold text-slate-800 mt-0.5 truncate">{{ formatPrice(dailyAverageRevenue) }}</p>
                </div>
                <div class="hidden sm:block">
                  <p class="text-[10px] sm:text-[11px] text-slate-500 uppercase tracking-wider font-semibold">Ngày cao nhất</p>
                  <p class="text-sm sm:text-base font-extrabold text-emerald-600 mt-0.5 truncate">{{ formatPrice(peakRevenue) }}</p>
                </div>
              </div>

              <!-- Interactive Area Chart SVG -->
              <div v-if="chartData.length > 0" class="relative w-full overflow-hidden" @mouseleave="hoveredPoint = null">
                <!-- Overlay loading spinner when switching range -->
                <div v-if="chartLoading" class="absolute inset-0 bg-white/60 backdrop-blur-xs flex items-center justify-center z-10">
                  <div class="w-8 h-8 rounded-full border-2 border-slate-200 border-t-[#09f] animate-spin"></div>
                </div>

                <svg
                  :viewBox="`0 0 ${svgWidth} ${svgHeight}`"
                  class="w-full h-52 sm:h-64 2xl:h-72 overflow-visible select-none"
                  @mousemove="handleSvgMouseMove"
                  role="img"
                  aria-label="Biểu đồ diện tích biểu diễn doanh thu"
                >
                  <defs>
                    <linearGradient id="areaGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stop-color="#0099ff" stop-opacity="0.35" />
                      <stop offset="60%" stop-color="#38bdf8" stop-opacity="0.10" />
                      <stop offset="100%" stop-color="#0099ff" stop-opacity="0.00" />
                    </linearGradient>
                    <filter id="splineGlow" x="-20%" y="-20%" width="140%" height="140%">
                      <feDropShadow dx="0" dy="4" stdDeviation="4" flood-color="#0099ff" flood-opacity="0.25" />
                    </filter>
                  </defs>

                  <!-- Horizontal Grid lines & labels -->
                  <g class="text-[10px] text-slate-400">
                    <line
                      v-for="(tick, idx) in yTicks"
                      :key="'grid-' + idx"
                      :x1="padding.left"
                      :y1="tick.y"
                      :x2="svgWidth - padding.right"
                      :y2="tick.y"
                      stroke="#f1f5f9"
                      stroke-width="1"
                      stroke-dasharray="4 4"
                    />
                    <text
                      v-for="(tick, idx) in yTicks"
                      :key="'text-' + idx"
                      :x="padding.left - 8"
                      :y="tick.y + 3"
                      text-anchor="end"
                      fill="#94a3b8"
                      class="text-[9px] font-mono"
                    >
                      {{ formatCompactPrice(tick.value) }}
                    </text>
                  </g>

                  <!-- Average Revenue dashed guideline -->
                  <line
                    v-if="dailyAverageRevenueY"
                    :x1="padding.left"
                    :y1="dailyAverageRevenueY"
                    :x2="svgWidth - padding.right"
                    :y2="dailyAverageRevenueY"
                    stroke="#cbd5e1"
                    stroke-width="1.5"
                    stroke-dasharray="3 3"
                  />

                  <!-- The Area Fill -->
                  <path :d="areaPath" fill="url(#areaGradient)" />

                  <!-- The Spline Stroke Line -->
                  <path
                    :d="splinePath"
                    fill="none"
                    stroke="#0099ff"
                    stroke-width="3"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    filter="url(#splineGlow)"
                  />

                  <!-- Hover Crosshair & indicator circle -->
                  <g v-if="hoveredPoint">
                    <line
                      :x1="hoveredPoint.x"
                      :y1="padding.top"
                      :x2="hoveredPoint.x"
                      :y2="svgHeight - padding.bottom"
                      stroke="#0099ff"
                      stroke-width="1.5"
                      stroke-dasharray="3 3"
                    />
                    <circle
                      :cx="hoveredPoint.x"
                      :cy="hoveredPoint.y"
                      r="6"
                      fill="#ffffff"
                      stroke="#0099ff"
                      stroke-width="3"
                      class="filter drop-shadow"
                    />
                  </g>

                  <!-- X-Axis Dates -->
                  <g class="text-[9px] select-none font-mono">
                    <text
                      v-for="(pt, idx) in visibleDateLabels"
                      :key="'label-' + idx"
                      :x="pt.x"
                      :y="svgHeight - 10"
                      text-anchor="middle"
                      fill="#64748b"
                    >
                      {{ pt.label }}
                    </text>
                  </g>
                </svg>

                <!-- Floating Glassmorphic Tooltip -->
                <div
                  v-if="hoveredPoint"
                  class="absolute z-20 pointer-events-none transition-all duration-75 bg-slate-900/95 backdrop-blur-md text-white text-xs rounded-xl py-2 px-3 shadow-xl border border-slate-700/60"
                  :style="tooltipStyle"
                >
                  <p class="font-semibold text-slate-300 text-[11px] mb-1">{{ formatFullDate(hoveredPoint.date) }}</p>
                  <div class="flex items-center gap-2">
                    <span class="w-2 h-2 rounded-full bg-[#38bdf8]"></span>
                    <span class="text-slate-300">Doanh thu:</span>
                    <span class="font-bold text-white">{{ formatPrice(hoveredPoint.revenue) }}</span>
                  </div>
                  <div class="flex items-center gap-2 mt-0.5">
                    <span class="w-2 h-2 rounded-full bg-emerald-400"></span>
                    <span class="text-slate-300">Đơn hàng:</span>
                    <span class="font-bold text-white">{{ hoveredPoint.orderCount }} đơn</span>
                  </div>
                </div>
              </div>
              <div v-else class="h-52 sm:h-64 2xl:h-72 flex items-center justify-center text-slate-400 text-sm">
                Chưa có dữ liệu biểu đồ cho kỳ đã chọn
              </div>
            </div>

            <!-- Chart Footer Legend -->
            <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 text-xs text-slate-500 pt-3 border-t border-slate-100 mt-2">
              <div class="flex items-center gap-4">
                <span class="flex items-center gap-1.5">
                  <span class="w-3 h-1 bg-[#09f] rounded-full"></span> Doanh thu
                </span>
                <span class="flex items-center gap-1.5">
                  <span class="w-3 h-0.5 border-t border-dashed border-slate-400"></span> Mức trung bình
                </span>
              </div>
              <span class="text-[11px] text-slate-400">Rê chuột trên biểu đồ để xem chi tiết từng ngày</span>
            </div>
          </div>

          <!-- BIỂU ĐỒ 2: Bar Chart Số lượng đơn hàng (Chiếm 1 cột trên xl+) -->
          <div class="bg-white rounded-2xl border border-slate-200/80 p-4 sm:p-6 shadow-xs hover:shadow-sm transition-all flex flex-col justify-between">
            <div>
              <div class="flex items-center justify-between mb-4">
                <div>
                  <div class="flex items-center gap-2">
                    <span class="w-2.5 h-2.5 rounded-full bg-emerald-500 shrink-0"></span>
                    <h2 class="text-sm sm:text-base font-bold text-slate-900">Số Lượng Đơn Hàng</h2>
                  </div>
                  <p class="text-xs text-slate-500 mt-0.5">Tần suất đặt hàng phát sinh trong chu kỳ</p>
                </div>
                <span class="text-xs font-bold text-white bg-emerald-600 shadow-xs px-2.5 py-0.5 rounded-full shrink-0">
                  {{ periodTotalOrders }} đơn
                </span>
              </div>

              <!-- Bar Chart Container -->
              <div class="h-52 sm:h-64 2xl:h-72 w-full flex items-end gap-0.5 sm:gap-1 relative pt-6 pb-6 select-none" v-if="chartData.length > 0">
                <div
                  v-for="(day, idx) in chartData"
                  :key="'bar-' + idx"
                  class="flex-1 h-full flex flex-col justify-end items-center group relative cursor-pointer"
                >
                  <!-- Bar tooltip -->
                  <div class="absolute bottom-full mb-2 left-1/2 -translate-x-1/2 bg-slate-900 text-white text-[11px] rounded-lg py-1 px-2.5 whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-30 shadow-lg border border-slate-700">
                    <p class="font-bold">{{ day.orderCount }} đơn</p>
                    <p class="text-[10px] text-slate-300 font-mono">{{ day.date.slice(5) }}</p>
                  </div>

                  <!-- Bar Representation -->
                  <div class="w-full flex items-end justify-center h-full">
                    <div
                      class="w-full max-w-[8px] sm:max-w-[14px] 2xl:max-w-[18px] rounded-t-md transition-all duration-300"
                      :class="day.orderCount === peakOrders && day.orderCount > 0 ? 'bg-gradient-to-t from-emerald-500 to-emerald-400 shadow-xs' : 'bg-emerald-200/80 group-hover:bg-emerald-400'"
                      :style="{ height: getOrderBarPercent(day.orderCount) + '%' }"
                    ></div>
                  </div>
                </div>
              </div>
              <div v-else class="h-52 sm:h-64 2xl:h-72 flex items-center justify-center text-slate-400 text-sm">
                Không có dữ liệu đơn hàng
              </div>
            </div>

            <!-- Bar Chart Summary Footer -->
            <div class="pt-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
              <span>Đỉnh điểm: <strong class="text-emerald-700 font-bold">{{ peakOrders }} đơn/ngày</strong></span>
              <span>TB: <strong class="text-slate-700 font-bold">{{ averageOrdersPerDay }} đơn/ngày</strong></span>
            </div>
          </div>
        </section>

        <!-- ==================== 4. BIỂU ĐỒ 3 & 4: VÀNH KHĂN TRẠNG THÁI & TOP BÁN CHẠY ==================== -->
        <section aria-label="Cơ cấu trạng thái đơn hàng và sản phẩm bán chạy" class="grid grid-cols-1 xl:grid-cols-3 gap-4 sm:gap-6">

          <!-- BIỂU ĐỒ 3: Donut Chart Cơ cấu Trạng thái Đơn (Chiếm 1 cột trên xl+) -->
          <div class="bg-white rounded-2xl border border-slate-200/80 p-4 sm:p-6 shadow-xs hover:shadow-sm transition-all flex flex-col justify-between">
            <div>
              <div class="flex items-center justify-between mb-4">
                <div class="flex items-center gap-2">
                  <span class="w-2.5 h-2.5 rounded-full bg-indigo-600 shrink-0"></span>
                  <h2 class="text-sm sm:text-base font-bold text-slate-900">Trạng Thái Đơn Hàng</h2>
                </div>
                <span class="text-xs font-bold text-slate-700 bg-slate-100 px-2.5 py-0.5 rounded-full border border-slate-200/60 font-mono shrink-0">
                  {{ dashboard.totalOrders || 0 }} đơn
                </span>
              </div>
              <p class="text-xs text-slate-500 mb-5">Phân bổ tỷ trọng trạng thái xử lý đơn hàng trên toàn hệ thống</p>

              <!-- Donut Container -->
              <div class="flex flex-col sm:flex-row xl:flex-col 2xl:flex-row items-center justify-center gap-4 sm:gap-6 my-2">
                <!-- SVG Donut -->
                <div class="relative w-36 h-36 sm:w-44 sm:h-44 shrink-0 flex items-center justify-center">
                  <svg viewBox="0 0 100 100" class="w-full h-full -rotate-90 transform select-none" role="img" aria-label="Biểu đồ hình vành khăn">
                    <!-- Background Circle -->
                    <circle
                      cx="50"
                      cy="50"
                      :r="DONUT_R"
                      fill="transparent"
                      stroke="#f1f5f9"
                      stroke-width="11"
                    />

                    <!-- Segments -->
                    <circle
                      v-for="seg in donutSegments"
                      :key="'donut-' + seg.status"
                      cx="50"
                      cy="50"
                      :r="DONUT_R"
                      fill="transparent"
                      :stroke="seg.color"
                      :stroke-width="hoveredSegment === seg.status ? 14 : 11"
                      :stroke-dasharray="`${seg.length} ${DONUT_CIRCUMFERENCE.toFixed(2)}`"
                      :stroke-dashoffset="`-${seg.offset}`"
                      stroke-linecap="butt"
                      class="transition-all duration-300 cursor-pointer origin-center"
                      :class="hoveredSegment && hoveredSegment !== seg.status ? 'opacity-35' : 'opacity-100'"
                      @mouseenter="hoveredSegment = seg.status"
                      @mouseleave="hoveredSegment = null"
                    />
                  </svg>

                  <!-- Donut Center Dynamic Readout -->
                  <div class="absolute inset-0 flex flex-col items-center justify-center text-center pointer-events-none px-2 select-none">
                    <template v-if="hoveredSegmentData">
                      <span class="text-[10px] sm:text-[11px] font-bold uppercase tracking-wider text-slate-400 truncate max-w-[100px] sm:max-w-[110px]">
                        {{ hoveredSegmentData.label }}
                      </span>
                      <span class="text-xl sm:text-2xl font-black text-slate-900 my-0.5">
                        {{ hoveredSegmentData.count }}
                      </span>
                      <span
                        class="text-[10px] sm:text-[11px] font-bold text-white px-2 py-0.5 rounded-full shadow-xs"
                        :style="{ backgroundColor: hoveredSegmentData.color }"
                      >
                        {{ hoveredSegmentData.percent }}%
                      </span>
                    </template>
                    <template v-else>
                      <span class="text-[10px] sm:text-[11px] font-bold uppercase tracking-wider text-slate-400">Tổng cộng</span>
                      <span class="text-xl sm:text-2xl font-black text-slate-900 my-0.5">
                        {{ dashboard.totalOrders || 0 }}
                      </span>
                      <span class="text-[10px] text-white font-bold bg-emerald-600 shadow-xs px-2.5 py-0.5 rounded-full mt-0.5">
                        {{ deliverySuccessRate }}% hoàn tất
                      </span>
                    </template>
                  </div>
                </div>

                <!-- Donut Legend List with Mini Progress Bars -->
                <div class="flex-1 w-full space-y-2">
                  <div
                    v-for="item in statusBreakdownList"
                    :key="item.status"
                    class="p-2 rounded-xl transition-all duration-200 cursor-pointer border"
                    :class="hoveredSegment === item.status 
                      ? 'bg-slate-100 border-slate-300 shadow-xs scale-[1.02]' 
                      : 'border-transparent hover:bg-slate-50'"
                    @mouseenter="hoveredSegment = item.status"
                    @mouseleave="hoveredSegment = null"
                  >
                    <div class="flex items-center justify-between text-xs mb-1">
                      <div class="flex items-center gap-2 min-w-0">
                        <span
                          class="w-2.5 h-2.5 rounded-sm shrink-0 transition-transform duration-200"
                          :class="hoveredSegment === item.status ? 'scale-125' : ''"
                          :style="{ backgroundColor: item.color }"
                        ></span>
                        <span class="font-bold text-slate-800 truncate">{{ item.label }}</span>
                      </div>
                      <div class="flex items-center gap-2 shrink-0 font-mono">
                        <span class="font-black text-slate-900">{{ item.count }}</span>
                        <span class="text-slate-500 text-[11px] font-semibold w-11 text-right">{{ item.percent }}%</span>
                      </div>
                    </div>
                    <!-- Mini progress track -->
                    <div class="h-1.5 w-full bg-slate-100 rounded-full overflow-hidden flex">
                      <div
                        class="h-full rounded-full transition-all duration-500"
                        :style="{ width: item.percent + '%', backgroundColor: item.color }"
                      ></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Mini Summary Footer -->
            <div class="pt-3 border-t border-slate-100 text-xs text-slate-500 flex items-center justify-between">
              <span>Đã giao thành công:</span>
              <span class="font-bold text-emerald-700 bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded-md">
                {{ dashboard.deliveredOrders || 0 }} đơn ({{ deliverySuccessRate }}%)
              </span>
            </div>
          </div>

          <!-- BIỂU ĐỒ 4: Top Sản Phẩm Bán Chạy (Chiếm 2 cột trên xl+) -->
          <div class="xl:col-span-2 bg-white rounded-2xl border border-slate-200/80 p-4 sm:p-6 shadow-xs hover:shadow-sm transition-all flex flex-col justify-between">
            <div>
              <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-4">
                <div class="flex items-center gap-2">
                  <span class="w-2.5 h-2.5 rounded-full bg-amber-500 shrink-0"></span>
                  <h2 class="text-sm sm:text-base font-bold text-slate-900">Bảng Xếp Hạng Linh Kiện Bán Chạy</h2>
                </div>

                <!-- Tab: Doanh thu vs Số lượng -->
                <div class="flex items-center bg-slate-100 p-1 rounded-xl text-xs font-semibold self-start sm:self-auto border border-slate-200/50">
                  <button
                    @click="rankingTab = 'revenue'"
                    :class="rankingTab === 'revenue' ? 'bg-white text-[#09f] shadow-xs' : 'text-slate-600 hover:text-slate-900'"
                    class="px-2.5 sm:px-3 py-1 rounded-lg transition-all cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#09f] text-[11px] sm:text-xs"
                  >
                    Theo Doanh Thu
                  </button>
                  <button
                    @click="rankingTab = 'rating'"
                    :class="rankingTab === 'rating' ? 'bg-white text-[#09f] shadow-xs' : 'text-slate-600 hover:text-slate-900'"
                    class="px-2.5 sm:px-3 py-1 rounded-lg transition-all cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#09f] text-[11px] sm:text-xs"
                  >
                    Đánh Giá Cao
                  </button>
                </div>
              </div>
              <p class="text-xs text-slate-500 mb-5">
                {{ rankingTab === 'revenue' ? 'Đóng góp số lượng bán & tỷ trọng doanh thu của các sản phẩm dẫn đầu' : 'Các mặt hàng nhận được đánh giá hài lòng cao nhất từ khách hàng' }}
              </p>

              <!-- Ranking List: Theo Doanh thu -->
              <div v-if="rankingTab === 'revenue' && dashboard.topProducts && dashboard.topProducts.length > 0" class="space-y-4">
                <div
                  v-for="(prod, idx) in dashboard.topProducts.slice(0, 5)"
                  :key="idx"
                  class="group p-2 -mx-2 rounded-xl hover:bg-slate-50/80 transition-colors"
                >
                  <div class="flex items-center justify-between text-xs mb-1.5 gap-2">
                    <div class="flex items-center gap-2 min-w-0 pr-1 flex-1">
                      <span
                        class="w-6 h-6 rounded-lg flex items-center justify-center font-black text-[11px] shrink-0 border"
                        :class="getRankBadgeClass(idx)"
                      >
                        {{ idx + 1 }}
                      </span>
                      <span class="font-bold text-slate-800 truncate" :title="prod.productName">
                        {{ prod.productName }}
                      </span>
                      <span v-if="prod.variantName" class="text-slate-400 text-[11px] truncate hidden md:inline shrink-0">
                        ({{ prod.variantName }})
                      </span>
                    </div>
                    <div class="flex items-center gap-2 sm:gap-3 shrink-0 text-right">
                      <span class="font-semibold text-slate-600 bg-slate-100 px-1.5 sm:px-2 py-0.5 rounded-md text-[11px] sm:text-xs shrink-0">{{ prod.totalQuantity }} sp</span>
                      <span class="text-slate-300 hidden xs:inline">|</span>
                      <span class="font-extrabold text-[#09f] text-xs sm:text-sm shrink-0">{{ formatPrice(prod.totalRevenue) }}</span>
                    </div>
                  </div>

                  <!-- Horizontal Progress Bar -->
                  <div class="h-2 w-full bg-slate-100 rounded-full overflow-hidden flex">
                    <div
                      class="h-full rounded-full transition-all duration-500"
                      :class="getRankBarGradient(idx)"
                      :style="{ width: getProductProgress(prod.totalRevenue) + '%' }"
                    ></div>
                  </div>
                </div>
              </div>

              <!-- Ranking List: Theo Đánh giá cao -->
              <div v-else-if="rankingTab === 'rating' && topRatedList.length > 0" class="space-y-4">
                <div
                  v-for="(prod, idx) in topRatedList.slice(0, 5)"
                  :key="idx"
                  class="group p-2 -mx-2 rounded-xl hover:bg-slate-50/80 transition-colors"
                >
                  <div class="flex items-center justify-between text-xs mb-1.5 gap-2">
                    <div class="flex items-center gap-2 min-w-0 pr-1 flex-1">
                      <span
                        class="w-6 h-6 rounded-lg flex items-center justify-center font-black text-[11px] shrink-0 border"
                        :class="getRankBadgeClass(idx)"
                      >
                        {{ idx + 1 }}
                      </span>
                      <span class="font-bold text-slate-800 truncate" :title="prod.productName">
                        {{ prod.productName }}
                      </span>
                    </div>
                    <div class="flex items-center gap-2 sm:gap-3 shrink-0 text-right">
                      <span class="flex items-center gap-1 font-bold text-white bg-amber-500 shadow-xs px-2 py-0.5 rounded-md text-[11px] sm:text-xs shrink-0">
                        ★ {{ (prod.avgRating || 5.0).toFixed(1) }}
                      </span>
                      <span class="text-slate-400 text-xs shrink-0 hidden xs:inline">Đã bán {{ prod.soldQuantity || 0 }} sp</span>
                    </div>
                  </div>
                  <div class="h-2 w-full bg-slate-100 rounded-full overflow-hidden flex">
                    <div
                      class="h-full rounded-full bg-gradient-to-r from-amber-400 to-amber-500 transition-all duration-500"
                      :style="{ width: ((prod.avgRating || 5) / 5) * 100 + '%' }"
                    ></div>
                  </div>
                </div>
              </div>

              <div v-else class="py-12 text-center text-slate-400 text-sm">
                Chưa có dữ liệu sản phẩm trong kỳ
              </div>
            </div>

            <!-- Ranking Footer -->
            <div class="pt-4 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
              <span>Tổng lượng đã bán toàn shop:</span>
              <strong class="text-slate-800 font-extrabold">{{ totalProductsSold }} sản phẩm</strong>
            </div>
          </div>
        </section>

        <!-- ==================== 5. BIỂU ĐỒ 5 & 6: ĐÁNH GIÁ CHẤT LƯỢNG & CHỈ SỐ HIỆU SUẤT VẬN HÀNH ==================== -->
        <section aria-label="Đánh giá chất lượng và chỉ số KPI vận hành" class="grid grid-cols-1 xl:grid-cols-3 gap-4 sm:gap-6">

          <!-- BIỂU ĐỒ 5: Rating Breakdown (Chiếm 1 cột trên xl+) -->
          <div class="bg-white rounded-2xl border border-slate-200/80 p-4 sm:p-6 shadow-xs hover:shadow-sm transition-all flex flex-col justify-between">
            <div>
              <div class="flex items-center justify-between mb-4">
                <div class="flex items-center gap-2">
                  <span class="w-2.5 h-2.5 rounded-full bg-amber-400 shrink-0"></span>
                  <h2 class="text-sm sm:text-base font-bold text-slate-900">Đánh Giá Sản Phẩm</h2>
                </div>
                <span class="text-xs text-slate-400 shrink-0">Phản hồi</span>
              </div>
              <p class="text-xs text-slate-500 mb-5 sm:mb-6">Mức độ thỏa mãn và điểm chất lượng sản phẩm</p>

              <!-- Star Score Highlight -->
              <div class="flex items-center gap-3 sm:gap-4 bg-gradient-to-r from-amber-500 to-amber-600 text-white p-3.5 sm:p-4 rounded-xl shadow-md mb-5">
                <div class="text-center shrink-0">
                  <p class="text-2xl sm:text-3xl font-black text-white tracking-tight">
                    {{ (dashboard.productStats?.averageRating || 5.0).toFixed(1) }}
                  </p>
                  <div class="flex items-center justify-center gap-0.5 text-amber-200 text-xs mt-1">
                    <span v-for="s in 5" :key="s">★</span>
                  </div>
                </div>
                <div class="h-10 w-px bg-white/30 shrink-0"></div>
                <div class="text-xs">
                  <p class="font-bold text-white">Chỉ số uy tín chất lượng</p>
                  <p class="text-amber-100 text-[11px] sm:text-xs mt-0.5">Dựa trên hàng trăm phản hồi của khách hàng đã mua sắm linh kiện</p>
                </div>
              </div>

              <!-- Rating Bars breakdown -->
              <div class="space-y-2.5">
                <div
                  v-for="(star, sIdx) in ratingRows"
                  :key="sIdx"
                  class="flex items-center gap-2 text-xs"
                >
                  <span class="w-12 text-slate-600 font-medium flex items-center gap-1 shrink-0">
                    {{ star.stars }} <span class="text-amber-400">★</span>
                  </span>
                  <div class="flex-1 h-2 bg-slate-100 rounded-full overflow-hidden">
                    <div
                      class="h-full bg-amber-400 rounded-full transition-all duration-300"
                      :style="{ width: star.percent + '%' }"
                    ></div>
                  </div>
                  <span class="w-10 text-right text-slate-400 font-mono text-[11px] shrink-0">{{ star.count }}</span>
                </div>
              </div>
            </div>

            <!-- Footer: Catalog stats -->
            <div class="pt-4 border-t border-slate-100 text-xs text-slate-500 flex items-center justify-between">
              <span>Tổng linh kiện trong kho:</span>
              <strong class="text-slate-800 font-bold">{{ dashboard.totalProducts }} sản phẩm</strong>
            </div>
          </div>

          <!-- BIỂU ĐỒ 6: Gauges Vận Hành & Chuyển Đổi (Chiếm 2 cột trên xl+) -->
          <div class="xl:col-span-2 bg-white rounded-2xl border border-slate-200/80 p-4 sm:p-6 shadow-xs hover:shadow-sm transition-all flex flex-col justify-between">
            <div>
              <div class="flex items-center justify-between mb-4">
                <div class="flex items-center gap-2">
                  <span class="w-2.5 h-2.5 rounded-full bg-cyan-500 shrink-0"></span>
                  <h2 class="text-sm sm:text-base font-bold text-slate-900">Chỉ Số Vận Hành & Chuyển Đổi (KPIs)</h2>
                </div>
                <span class="text-xs text-slate-400 shrink-0">Đo lường tự động</span>
              </div>
              <p class="text-xs text-slate-500 mb-5 sm:mb-6">Thước đo trọng yếu phản ánh tốc độ hoàn tất đơn hàng và hiệu quả doanh thu</p>

              <!-- 3 Gauges Grid -->
              <div class="grid grid-cols-1 sm:grid-cols-3 gap-3 sm:gap-4">
                <!-- Gauge 1: Tỷ lệ Giao Thành Công -->
                <div class="bg-slate-50/80 rounded-xl p-4 border border-slate-100 flex flex-col items-center text-center">
                  <div class="relative w-20 h-20 sm:w-24 sm:h-24 mb-3 flex items-center justify-center">
                    <svg viewBox="0 0 36 36" class="w-full h-full -rotate-90 transform">
                      <path
                        class="text-slate-200"
                        stroke-width="3.5"
                        stroke="currentColor"
                        fill="none"
                        d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                      />
                      <path
                        class="text-emerald-500 transition-all duration-500"
                        stroke-dasharray="100, 100"
                        :stroke-dashoffset="100 - deliverySuccessRate"
                        stroke-width="3.5"
                        stroke-linecap="round"
                        stroke="currentColor"
                        fill="none"
                        d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                      />
                    </svg>
                    <div class="absolute inset-0 flex flex-col items-center justify-center">
                      <span class="text-base sm:text-lg font-black text-slate-900">{{ deliverySuccessRate }}%</span>
                    </div>
                  </div>
                  <h4 class="text-xs font-bold text-slate-800">Tỷ Lệ Giao Thành Công</h4>
                  <p class="text-[11px] text-slate-400 mt-1">{{ dashboard.deliveredOrders }} / {{ dashboard.totalOrders }} đơn</p>
                </div>

                <!-- Gauge 2: AOV (Giá trị trung bình đơn hàng) -->
                <div class="bg-slate-50/80 rounded-xl p-4 border border-slate-100 flex flex-col items-center text-center justify-between">
                  <div class="w-11 h-11 sm:w-12 sm:h-12 rounded-2xl bg-gradient-to-tr from-[#0088ea] to-[#09f] text-white flex items-center justify-center my-2 shadow-md shadow-[#09f]/25 shrink-0">
                    <svg class="w-5 h-5 sm:w-6 sm:h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <div class="w-full">
                    <p class="text-xs font-bold text-slate-800">Giá Trị TB / Đơn (AOV)</p>
                    <p class="text-base sm:text-lg font-black text-[#09f] mt-1 truncate">{{ formatPrice(averageOrderValue) }}</p>
                    <p class="text-[11px] text-slate-400 mt-0.5">Doanh thu / đơn hoàn tất</p>
                  </div>
                </div>

                <!-- Gauge 3: Tỷ lệ hủy đơn -->
                <div class="bg-slate-50/80 rounded-xl p-4 border border-slate-100 flex flex-col items-center text-center">
                  <div class="relative w-20 h-20 sm:w-24 sm:h-24 mb-3 flex items-center justify-center">
                    <svg viewBox="0 0 36 36" class="w-full h-full -rotate-90 transform">
                      <path
                        class="text-slate-200"
                        stroke-width="3.5"
                        stroke="currentColor"
                        fill="none"
                        d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                      />
                      <path
                        class="text-rose-500 transition-all duration-500"
                        stroke-dasharray="100, 100"
                        :stroke-dashoffset="100 - cancellationRate"
                        stroke-width="3.5"
                        stroke-linecap="round"
                        stroke="currentColor"
                        fill="none"
                        d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                      />
                    </svg>
                    <div class="absolute inset-0 flex flex-col items-center justify-center">
                      <span class="text-base sm:text-lg font-black text-rose-600">{{ cancellationRate }}%</span>
                    </div>
                  </div>
                  <h4 class="text-xs font-bold text-slate-800">Tỷ Lệ Đơn Hủy</h4>
                  <p class="text-[11px] text-slate-400 mt-1">{{ dashboard.cancelledOrders }} đơn đã hủy</p>
                </div>
              </div>
            </div>

            <!-- Pipeline status line -->
            <div class="pt-4 border-t border-slate-100 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 text-xs text-slate-500">
              <span>Đang lưu chuyển trong quy trình:</span>
              <strong class="text-white font-bold bg-indigo-600 shadow-xs px-2.5 py-0.5 rounded-md self-start sm:self-auto">
                {{ activeTransitOrders }} đơn hàng cần xử lý
              </strong>
            </div>
          </div>
        </section>

        <!-- ==================== 6. BẢNG GIAO DỊCH ĐƠN HÀNG GẦN ĐÂY ==================== -->
        <section aria-label="Danh sách đơn hàng mới nhất" class="bg-white rounded-2xl border border-slate-200/80 p-4 sm:p-6 shadow-xs hover:shadow-sm transition-all space-y-4 sm:space-y-5">
          <!-- Header Bảng & Quick Action -->
          <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 sm:gap-4">
            <div>
              <div class="flex items-center gap-2">
                <span class="w-2.5 h-2.5 rounded-full bg-[#09f] shrink-0"></span>
                <h3 class="text-sm sm:text-base font-bold text-slate-900">Giao Dịch Đơn Hàng Mới Nhất</h3>
              </div>
              <p class="text-xs text-slate-500 mt-0.5">Các đơn đặt hàng phát sinh gần đây trên hệ thống cửa hàng</p>
            </div>

            <NuxtLink
              to="/admin/orders"
              class="text-xs font-bold text-white bg-gradient-to-r from-[#09f] to-[#0077cc] hover:from-[#33adff] hover:to-[#0088ea] flex items-center justify-center gap-1.5 px-3.5 py-2 rounded-xl shadow-md shadow-[#09f]/25 transition-all self-start sm:self-auto focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#09f]"
            >
              <span>Xem toàn bộ {{ dashboard.totalOrders }} đơn</span>
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </NuxtLink>
          </div>

          <!-- Controls: Bộ lọc trạng thái & Ô tìm kiếm nhanh -->
          <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-3 pt-1 sm:pt-2">
            <!-- Tabs lọc trạng thái (Hỗ trợ vuốt ngang trên mobile) -->
            <div class="flex items-center gap-1.5 overflow-x-auto pb-1.5 md:pb-0 scrollbar-none -mx-1 px-1">
              <button
                v-for="tab in filterTabs"
                :key="tab.value"
                @click="orderFilterStatus = tab.value"
                class="px-2.5 sm:px-3 py-1.5 rounded-xl text-xs font-semibold transition-all cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#09f] shrink-0"
                :class="orderFilterStatus === tab.value ? 'bg-[#09f] text-white shadow-md shadow-[#09f]/25 font-bold' : 'bg-slate-100 text-slate-700 font-bold hover:bg-slate-200'"
              >
                {{ tab.label }}
                <span
                  class="ml-1 px-1.5 py-0.2 rounded-full text-[10px]"
                  :class="orderFilterStatus === tab.value ? 'bg-white/25 text-white' : 'bg-slate-200 text-slate-800 font-bold'"
                >
                  {{ tab.count }}
                </span>
              </button>
            </div>

            <!-- Search Input -->
            <div class="relative w-full md:w-64">
              <input
                v-model="orderSearch"
                type="text"
                placeholder="Tìm khách, mã đơn, SĐT..."
                class="w-full pl-9 pr-8 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-[#09f] focus:bg-white focus:ring-2 focus:ring-[#09f]/10 transition-all placeholder:text-slate-400"
              />
              <svg class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <button
                v-if="orderSearch"
                @click="orderSearch = ''"
                class="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 p-0.5"
                title="Xóa tìm kiếm"
              >
                ✕
              </button>
            </div>
          </div>

          <!-- Orders Data List -->
          <div v-if="filteredOrders.length > 0" class="divide-y divide-slate-100">
            <div
              v-for="order in filteredOrders"
              :key="order.id"
              class="py-3 sm:py-3.5 px-2 sm:px-3 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 hover:bg-slate-50/80 rounded-xl transition-colors group"
            >
              <!-- Cột trái: Mã đơn, Khách hàng, Trạng thái -->
              <div class="flex items-start gap-2.5 sm:gap-3 min-w-0 flex-1">
                <!-- Avatar viết tắt -->
                <div class="w-9 h-9 sm:w-10 sm:h-10 rounded-full bg-gradient-to-tr from-slate-200 to-slate-100 flex items-center justify-center font-bold text-slate-700 text-xs shrink-0 border border-slate-200/60 shadow-xs">
                  {{ (order.fullName || 'K').charAt(0).toUpperCase() }}
                </div>

                <div class="min-w-0 flex-1">
                  <div class="flex flex-wrap items-center gap-1.5 sm:gap-2">
                    <!-- Mã đơn + Copy button -->
                    <button
                      @click="copyOrderNumber(order.orderNumber || `#${order.id}`)"
                      class="font-mono text-xs font-bold text-[#09f] hover:text-[#0077cc] hover:underline flex items-center gap-1 cursor-pointer focus-visible:outline-none"
                      title="Bấm để sao chép mã đơn"
                    >
                      <span>{{ order.orderNumber || `#${order.id}` }}</span>
                      <svg v-if="copiedOrderId !== (order.orderNumber || `#${order.id}`)" class="w-3 h-3 text-slate-400 opacity-0 group-hover:opacity-100 transition-opacity" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                      </svg>
                      <span v-else class="text-[10px] text-white font-bold bg-emerald-600 px-1.5 py-0.5 rounded shadow-xs">Đã chép!</span>
                    </button>

                    <!-- Status Pill with Pulsing Dot -->
                    <span :class="getStatusClass(order.status)" class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold shadow-xs">
                      <span class="w-1.5 h-1.5 rounded-full bg-white"></span>
                      {{ getStatusLabel(order.status) }}
                    </span>

                    <!-- Payment Badge -->
                    <span class="text-[10px] px-1.5 py-0.5 rounded-md bg-slate-900 text-white font-bold uppercase shadow-xs">
                      {{ order.paymentMethod || 'COD' }}
                    </span>
                  </div>

                  <p class="text-xs text-slate-500 mt-1 truncate">
                    Khách: <strong class="text-slate-800 font-medium">{{ order.fullName }}</strong>
                    <span class="text-slate-300 mx-1.5">•</span>
                    SĐT: <span class="font-mono">{{ order.phone || 'N/A' }}</span>
                    <span class="text-slate-300 mx-1.5">•</span>
                    {{ formatDate(order.createdAt) }}
                  </p>
                </div>
              </div>

              <!-- Cột phải: Giá tiền & Link thao tác -->
              <div class="flex items-center justify-between sm:justify-end gap-3 shrink-0 sm:text-right pl-0 sm:pl-0 pt-2 sm:pt-0 border-t sm:border-t-0 border-slate-100">
                <div>
                  <p class="text-sm font-black text-slate-900">{{ formatPrice(order.totalCost) }}</p>
                  <p class="text-[11px] text-slate-400">
                    {{ order.paymentStatus === 'PAID' ? 'Đã thanh toán' : 'Chưa thanh toán' }}
                  </p>
                </div>

                <NuxtLink
                  :to="`/admin/orders`"
                  class="p-2 text-blue-700 bg-blue-100 hover:bg-blue-600 hover:text-white border border-blue-200 rounded-lg transition-colors cursor-pointer shadow-xs shrink-0"
                  title="Xem đơn hàng chi tiết"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                  </svg>
                </NuxtLink>
              </div>
            </div>
          </div>

          <!-- Empty search/filter state -->
          <div v-else class="py-12 text-center text-slate-400 text-sm space-y-2">
            <svg class="w-10 h-10 text-slate-300 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
            <p class="font-semibold text-slate-600">Không tìm thấy đơn hàng phù hợp</p>
            <p class="text-xs text-slate-400">Thử thay đổi từ khóa hoặc bộ lọc trạng thái</p>
            <button
              v-if="orderFilterStatus !== 'ALL' || orderSearch"
              @click="orderFilterStatus = 'ALL'; orderSearch = ''"
              class="mt-2 text-xs text-[#09f] font-bold hover:underline cursor-pointer"
            >
              Đặt lại bộ lọc
            </button>
          </div>
        </section>

        <!-- ==================== 7. BẢNG THỐNG KÊ TỪ KHÓA TÌM KIẾM CỦA KHÁCH HÀNG ==================== -->
        <section aria-label="Thống kê từ khóa tìm kiếm" class="bg-white rounded-2xl border border-slate-200/80 p-4 sm:p-6 shadow-xs hover:shadow-sm transition-all space-y-4 sm:space-y-5">
          <!-- Header Bảng & Nút Refresh -->
          <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 sm:gap-4">
            <div>
              <div class="flex items-center gap-2">
                <span class="w-2.5 h-2.5 rounded-full bg-emerald-500 shrink-0"></span>
                <h3 class="text-sm sm:text-base font-bold text-slate-900">Thống Kê Từ Khóa Tìm Kiếm (Khách Hàng & Nhu Cầu)</h3>
              </div>
              <p class="text-xs text-slate-500 mt-0.5">Top các từ khóa khách hàng tìm kiếm nhiều nhất trên website để phát hiện xu hướng và mặt hàng quan tâm</p>
            </div>

            <!-- Nút làm mới dữ liệu từ khóa -->
            <button
              @click="fetchSearchStats"
              :disabled="searchStatsLoading"
              class="inline-flex items-center gap-1.5 px-3.5 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl text-xs font-semibold shadow-xs transition-all cursor-pointer disabled:opacity-50 self-start sm:self-auto shrink-0"
            >
              <svg class="w-3.5 h-3.5 text-[#09f]" :class="{ 'animate-spin': searchStatsLoading }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
              <span>Làm mới dữ liệu</span>
            </button>
          </div>

          <!-- Table Container (Horizontal scroll on mobile) -->
          <div class="overflow-x-auto rounded-xl border border-slate-100 -mx-1 sm:mx-0 scrollbar-none">
            <table class="min-w-[560px] w-full text-left text-xs text-slate-600">
              <thead class="bg-slate-50 text-[11px] font-bold text-slate-700 uppercase tracking-wider border-b border-slate-200">
                <tr>
                  <th scope="col" class="px-3.5 sm:px-4 py-3 w-16 text-center">Hạng</th>
                  <th scope="col" class="px-3.5 sm:px-4 py-3">Từ khóa tìm kiếm</th>
                  <th scope="col" class="px-3.5 sm:px-4 py-3 text-center">Số lượt tìm</th>
                  <th scope="col" class="px-3.5 sm:px-4 py-3 text-center">Tình trạng kết quả</th>
                  <th scope="col" class="px-3.5 sm:px-4 py-3 text-right">Lần tìm gần nhất</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                <tr v-if="searchStatsLoading" class="animate-pulse">
                  <td colspan="5" class="px-4 py-8 text-center text-slate-400">Đang tải dữ liệu từ khóa tìm kiếm...</td>
                </tr>
                <tr v-else-if="!searchStats.length">
                  <td colspan="5" class="px-4 py-8 text-center text-slate-400">Chưa có lịch sử tìm kiếm nào được ghi nhận.</td>
                </tr>
                <tr
                  v-else
                  v-for="(item, idx) in searchStats"
                  :key="item.keyword"
                  class="hover:bg-slate-50/80 transition-colors"
                >
                  <!-- Hạng -->
                  <td class="px-3.5 sm:px-4 py-3 text-center font-bold">
                    <span
                      class="inline-flex items-center justify-center w-6 h-6 rounded-full text-xs"
                      :class="[
                        idx === 0 ? 'bg-amber-100 text-amber-700 font-extrabold ring-1 ring-amber-300' :
                        idx === 1 ? 'bg-slate-200 text-slate-700 font-extrabold' :
                        idx === 2 ? 'bg-amber-700/10 text-amber-900 font-extrabold' :
                        'text-slate-400'
                      ]"
                    >
                      #{{ idx + 1 }}
                    </span>
                  </td>

                  <!-- Từ khóa -->
                  <td class="px-3.5 sm:px-4 py-3 font-semibold text-slate-900">
                    <div class="flex items-center gap-2">
                      <span class="text-sm font-medium">{{ item.keyword }}</span>
                      <NuxtLink
                        :to="`/search?q=${encodeURIComponent(item.keyword)}`"
                        target="_blank"
                        class="text-[#09f] hover:text-[#0077cc] transition-colors"
                        title="Xem kết quả tìm kiếm trên shop"
                      >
                        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                        </svg>
                      </NuxtLink>
                    </div>
                  </td>

                  <!-- Số lượt tìm -->
                  <td class="px-3.5 sm:px-4 py-3 text-center font-bold text-slate-900">
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-blue-50 text-blue-700 border border-blue-200">
                      {{ item.searchCount }} lượt
                    </span>
                  </td>

                  <!-- Tình trạng kết quả -->
                  <td class="px-3.5 sm:px-4 py-3 text-center">
                    <span
                      v-if="item.resultsCount > 0"
                      class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-md text-[11px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200"
                    >
                      <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
                      Có hàng ({{ item.resultsCount }} sp)
                    </span>
                    <span
                      v-else
                      class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-md text-[11px] font-semibold bg-rose-50 text-rose-700 border border-rose-200"
                    >
                      <span class="w-1.5 h-1.5 rounded-full bg-rose-500"></span>
                      0 kết quả (Cần nhập)
                    </span>
                  </td>

                  <!-- Lần tìm gần nhất -->
                  <td class="px-3.5 sm:px-4 py-3 text-right text-slate-400 text-[11px]">
                    {{ formatSearchDate(item.lastSearchedAt) }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

      </template>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuth } from '~/composables/useAuth'
import { useNotification } from '~/composables/useNotification'

definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

const { user, token } = useAuth()
const { error } = useNotification()

// State
const loading = ref(true)
const chartLoading = ref(false)
const hasError = ref(false)
const dashboard = ref<any>(null)
const selectedRange = ref<number>(30)
const lastUpdated = ref<string>('')
const rankingTab = ref<'revenue' | 'rating'>('revenue')
const orderFilterStatus = ref<string>('ALL')
const orderSearch = ref<string>('')
const copiedOrderId = ref<string | null>(null)

// Interactive states
const hoveredPoint = ref<any>(null)
const hoveredSegment = ref<string | null>(null)

// Current formatted date
const currentDateFormatted = computed(() => {
  return new Date().toLocaleDateString('vi-VN', {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
  })
})

onMounted(async () => {
  if (user.value?.role !== 'ROLE_ADMIN') {
    error('Truy cập bị từ chối', 'Bạn không có quyền truy cập trang quản trị này')
    navigateTo('/')
    return
  }
  await Promise.all([
    fetchDashboard(),
    fetchSearchStats()
  ])
})

// Search statistics state
const searchStats = ref<any[]>([])
const searchStatsLoading = ref(false)

const fetchSearchStats = async () => {
  searchStatsLoading.value = true
  try {
    const data = await $fetch<any[]>('http://localhost:8080/api/admin/search-stats?limit=20', {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    searchStats.value = Array.isArray(data) ? data : []
  } catch (e: any) {
    console.error('Failed to fetch search stats:', e)
  } finally {
    searchStatsLoading.value = false
  }
}

const formatSearchDate = (dateStr: string) => {
  if (!dateStr) return 'Mới đây'
  try {
    return new Date(dateStr).toLocaleString('vi-VN', {
      day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit'
    })
  } catch {
    return dateStr
  }
}

const fetchDashboard = async () => {
  loading.value = true
  hasError.value = false
  try {
    const data = await $fetch<any>('http://localhost:8080/api/admin/dashboard', {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    dashboard.value = data
    lastUpdated.value = new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
  } catch (e: any) {
    console.error('Failed to fetch dashboard:', e)
    hasError.value = true
    error('Lỗi', 'Không thể tải dữ liệu dashboard')
  } finally {
    loading.value = false
  }
}

// Chuyển đổi khung ngày (7, 14, 30 ngày)
const changeRange = async (days: number) => {
  if (selectedRange.value === days) return
  selectedRange.value = days
  chartLoading.value = true
  try {
    const data = await $fetch<any[]>(`http://localhost:8080/api/admin/chart?days=${days}`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    if (dashboard.value) {
      dashboard.value.revenueChart = data
    }
  } catch (e) {
    console.error('Failed to update chart range:', e)
  } finally {
    chartLoading.value = false
  }
}

// Copy order code helper
const copyOrderNumber = async (orderNum: string) => {
  try {
    await navigator.clipboard.writeText(orderNum)
    copiedOrderId.value = orderNum
    setTimeout(() => {
      if (copiedOrderId.value === orderNum) {
        copiedOrderId.value = null
      }
    }, 1800)
  } catch (e) {
    console.error('Clipboard copy failed:', e)
  }
}

// Dữ liệu chart hiện tại
const chartData = computed(() => {
  return dashboard.value?.revenueChart || []
})

// ==================== 1. TOÁN HỌC & TÍNH TOÁN BIỂU ĐỒ DIỆN TÍCH (AREA SPLINE CHART) ====================
const svgWidth = 800
const svgHeight = 260
const padding = { top: 25, right: 25, bottom: 35, left: 75 }

const maxRevenue = computed(() => {
  if (!chartData.value.length) return 1000000
  const max = Math.max(...chartData.value.map((d: any) => Number(d.revenue) || 0))
  return max > 0 ? max : 1000000
})

const yTicks = computed(() => {
  const max = maxRevenue.value
  const steps = 4
  const ticks = []
  const plotH = svgHeight - padding.top - padding.bottom
  for (let i = 0; i <= steps; i++) {
    const val = (max / steps) * i
    const y = svgHeight - padding.bottom - (i / steps) * plotH
    ticks.push({ value: val, y })
  }
  return ticks
})

const periodTotalRevenue = computed(() => {
  return chartData.value.reduce((sum: number, d: any) => sum + (Number(d.revenue) || 0), 0)
})

const dailyAverageRevenue = computed(() => {
  if (!chartData.value.length) return 0
  return periodTotalRevenue.value / chartData.value.length
})

const dailyAverageRevenueY = computed(() => {
  if (!dailyAverageRevenue.value) return null
  const plotH = svgHeight - padding.top - padding.bottom
  return svgHeight - padding.bottom - (dailyAverageRevenue.value / maxRevenue.value) * plotH
})

const peakRevenue = computed(() => {
  return maxRevenue.value
})

// Tính toán tọa độ (x, y) cho từng ngày
const splinePoints = computed(() => {
  const data = chartData.value
  if (!data.length) return []
  const n = data.length
  const plotW = svgWidth - padding.left - padding.right
  const plotH = svgHeight - padding.top - padding.bottom

  return data.map((d: any, idx: number) => {
    const x = padding.left + (idx / Math.max(1, n - 1)) * plotW
    const rev = Number(d.revenue) || 0
    const y = svgHeight - padding.bottom - (rev / maxRevenue.value) * plotH
    return {
      x,
      y,
      date: d.date,
      revenue: rev,
      orderCount: d.orderCount || 0
    }
  })
})

// Đường cong Monotone Cubic Bezier Spline
const splinePath = computed(() => {
  const pts = splinePoints.value
  if (!pts.length) return ''
  if (pts.length === 1) return `M ${pts[0].x} ${pts[0].y}`

  let path = `M ${pts[0].x.toFixed(1)} ${pts[0].y.toFixed(1)}`
  for (let i = 0; i < pts.length - 1; i++) {
    const p0 = pts[i === 0 ? i : i - 1]
    const p1 = pts[i]
    const p2 = pts[i + 1]
    const p3 = pts[i + 2 < pts.length ? i + 2 : i + 1]

    const cp1x = p1.x + (p2.x - p0.x) / 6
    const cp1y = p1.y + (p2.y - p0.y) / 6
    const cp2x = p2.x - (p3.x - p1.x) / 6
    const cp2y = p2.y - (p3.y - p1.y) / 6

    path += ` C ${cp1x.toFixed(1)} ${cp1y.toFixed(1)}, ${cp2x.toFixed(1)} ${cp2y.toFixed(1)}, ${p2.x.toFixed(1)} ${p2.y.toFixed(1)}`
  }
  return path
})

// Vùng phủ diện tích dưới đường cong
const areaPath = computed(() => {
  const pts = splinePoints.value
  if (!pts.length) return ''
  const baseLineY = svgHeight - padding.bottom
  return `${splinePath.value} L ${pts[pts.length - 1].x.toFixed(1)} ${baseLineY} L ${pts[0].x.toFixed(1)} ${baseLineY} Z`
})

// Nhãn ngày hiển thị cách đều
const visibleDateLabels = computed(() => {
  const pts = splinePoints.value
  if (!pts.length) return []
  const step = Math.ceil(pts.length / 7)
  return pts.filter((_, idx) => idx % step === 0 || idx === pts.length - 1).map(p => ({
    x: p.x,
    label: p.date.slice(5) // MM-DD
  }))
})

// Xử lý di chuột tương tác trên biểu đồ đường
const handleSvgMouseMove = (event: MouseEvent) => {
  const svg = (event.currentTarget as SVGElement).getBoundingClientRect()
  const mouseX = ((event.clientX - svg.left) / svg.width) * svgWidth
  const pts = splinePoints.value
  if (!pts.length) return

  let closest = pts[0]
  let minDist = Math.abs(mouseX - closest.x)
  for (let i = 1; i < pts.length; i++) {
    const dist = Math.abs(mouseX - pts[i].x)
    if (dist < minDist) {
      minDist = dist
      closest = pts[i]
    }
  }
  hoveredPoint.value = closest
}

const tooltipStyle = computed(() => {
  if (!hoveredPoint.value) return {}
  const leftPercent = (hoveredPoint.value.x / svgWidth) * 100
  const topPercent = (hoveredPoint.value.y / svgHeight) * 100
  return {
    left: `clamp(12px, calc(${leftPercent}% - 65px), calc(100% - 150px))`,
    top: `clamp(8px, calc(${topPercent}% - 75px), calc(100% - 75px))`
  }
})

// ==================== 2. TÍNH TOÁN BIỂU ĐỒ CỘT SỐ LƯỢNG ĐƠN ====================
const peakOrders = computed(() => {
  if (!chartData.value.length) return 1
  const max = Math.max(...chartData.value.map((d: any) => d.orderCount || 0))
  return max > 0 ? max : 1
})

const periodTotalOrders = computed(() => {
  return chartData.value.reduce((sum: number, d: any) => sum + (d.orderCount || 0), 0)
})

const averageOrdersPerDay = computed(() => {
  if (!chartData.value.length) return 0
  return (periodTotalOrders.value / chartData.value.length).toFixed(1)
})

const getOrderBarPercent = (count: number) => {
  if (!count) return 3
  return Math.max(6, (count / peakOrders.value) * 100)
}

// ==================== 3. TÍNH TOÁN BIỂU ĐỒ VÀNH KHĂN (DONUT CHART) ====================
const DONUT_R = 38
const DONUT_CIRCUMFERENCE = 2 * Math.PI * DONUT_R // ~238.761

const statusConfig = [
  { status: 'DELIVERED', key: 'deliveredOrders', label: 'Đã giao', color: '#059669' },
  { status: 'SHIPPED', key: 'shippedOrders', label: 'Đang giao', color: '#4f46e5' },
  { status: 'CONFIRMED', key: 'confirmedOrders', label: 'Đã xác nhận', color: '#0284c7' },
  { status: 'PENDING', key: 'pendingOrders', label: 'Chờ xử lý', color: '#f59e0b' },
  { status: 'CANCELLED', key: 'cancelledOrders', label: 'Đã hủy', color: '#e11d48' }
]

const statusBreakdownList = computed(() => {
  if (!dashboard.value) return []
  const total = Number(dashboard.value.totalOrders) || 0
  return statusConfig.map(cfg => {
    const count = Number(dashboard.value[cfg.key]) || 0
    const percent = total > 0 ? ((count / total) * 100).toFixed(1) : '0.0'
    return {
      status: cfg.status,
      label: cfg.label,
      color: cfg.color,
      count,
      percent
    }
  })
})

const donutSegments = computed(() => {
  if (!dashboard.value || !dashboard.value.totalOrders) return []
  const total = Number(dashboard.value.totalOrders) || 0
  if (total <= 0) return []

  const activeItems = statusBreakdownList.value.filter(s => s.count > 0)
  if (activeItems.length === 0) return []

  // Nếu chỉ có 1 trạng thái duy nhất chiếm 100%
  if (activeItems.length === 1) {
    const single = activeItems[0]
    return [{
      status: single.status,
      label: single.label,
      color: single.color,
      length: DONUT_CIRCUMFERENCE.toFixed(2),
      offset: '0.00'
    }]
  }

  // Nếu có nhiều hơn 1 trạng thái, chừa gap 2.5px để tạo rãnh phân cách rõ nét
  const gap = 2.5
  let accumulated = 0

  return activeItems.map(item => {
    const pct = item.count / total
    const rawLen = pct * DONUT_CIRCUMFERENCE
    const segLen = Math.max(1, rawLen - gap)
    const segOffset = accumulated + (gap / 2)
    accumulated += rawLen
    return {
      status: item.status,
      label: item.label,
      color: item.color,
      length: segLen.toFixed(2),
      offset: segOffset.toFixed(2)
    }
  })
})

const hoveredSegmentData = computed(() => {
  if (!hoveredSegment.value) return null
  return statusBreakdownList.value.find(s => s.status === hoveredSegment.value) || null
})

// ==================== 4. TOP SẢN PHẨM & XẾP HẠNG ====================
const topProductMaxRevenue = computed(() => {
  if (!dashboard.value?.topProducts?.length) return 1
  return Math.max(...dashboard.value.topProducts.map((p: any) => Number(p.totalRevenue) || 0)) || 1
})

const getProductProgress = (revenue: number) => {
  return Math.min(100, Math.max(5, (Number(revenue) / topProductMaxRevenue.value) * 100))
}

const topRatedList = computed(() => {
  return dashboard.value?.productStats?.topRatedProducts || []
})

const getRankBadgeClass = (idx: number) => {
  if (idx === 0) return 'bg-amber-500 text-white font-black border-amber-600 shadow-xs'
  if (idx === 1) return 'bg-slate-700 text-white font-black border-slate-800 shadow-xs'
  if (idx === 2) return 'bg-amber-700 text-white font-black border-amber-800 shadow-xs'
  return 'bg-slate-200 text-slate-800 font-bold border-slate-300'
}

const getRankBarGradient = (idx: number) => {
  if (idx === 0) return 'bg-gradient-to-r from-amber-400 to-amber-500'
  if (idx === 1) return 'bg-gradient-to-r from-slate-400 to-slate-500'
  if (idx === 2) return 'bg-gradient-to-r from-orange-400 to-amber-500'
  return 'bg-gradient-to-r from-[#09f] to-blue-600'
}

const totalProductsSold = computed(() => {
  return dashboard.value?.productStats?.totalSoldQuantity || 0
})

// ==================== 5. ĐÁNH GIÁ (RATING BREAKDOWN) ====================
const ratingRows = computed(() => {
  const dist = dashboard.value?.productStats?.ratingDistribution
  const total = dist ? (dist.fiveStar + dist.fourStar + dist.threeStar + dist.twoStar + dist.oneStar) : 0
  const calcPct = (cnt: number) => (total > 0 ? ((cnt / total) * 100).toFixed(0) : '0')

  return [
    { stars: 5, count: dist?.fiveStar || 24, percent: dist ? calcPct(dist.fiveStar) : 75 },
    { stars: 4, count: dist?.fourStar || 6, percent: dist ? calcPct(dist.fourStar) : 18 },
    { stars: 3, count: dist?.threeStar || 2, percent: dist ? calcPct(dist.threeStar) : 5 },
    { stars: 2, count: dist?.twoStar || 1, percent: dist ? calcPct(dist.twoStar) : 2 },
    { stars: 1, count: dist?.oneStar || 0, percent: dist ? calcPct(dist.oneStar) : 0 },
  ]
})

// ==================== 6. CHỈ SỐ KPI VẬN HÀNH ====================
const deliverySuccessRate = computed(() => {
  if (!dashboard.value || !dashboard.value.totalOrders) return 0
  return Math.round((dashboard.value.deliveredOrders / dashboard.value.totalOrders) * 100)
})

const cancellationRate = computed(() => {
  if (!dashboard.value || !dashboard.value.totalOrders) return 0
  return Math.round((dashboard.value.cancelledOrders / dashboard.value.totalOrders) * 100)
})

const averageOrderValue = computed(() => {
  if (!dashboard.value || !dashboard.value.deliveredOrders) return 0
  return Math.round(Number(dashboard.value.totalRevenue) / dashboard.value.deliveredOrders)
})

const activeTransitOrders = computed(() => {
  if (!dashboard.value) return 0
  return (dashboard.value.pendingOrders || 0) + (dashboard.value.confirmedOrders || 0) + (dashboard.value.shippedOrders || 0)
})

// ==================== 7. BỘ LỌC ĐƠN HÀNG GẦN ĐÂY ====================
const recentOrdersList = computed(() => {
  return dashboard.value?.recentOrders || []
})

const filterTabs = computed(() => {
  const all = recentOrdersList.value
  return [
    { label: 'Tất cả', value: 'ALL', count: all.length },
    { label: 'Chờ xử lý', value: 'PENDING', count: all.filter((o: any) => o.status === 'PENDING').length },
    { label: 'Đang giao', value: 'SHIPPED', count: all.filter((o: any) => o.status === 'SHIPPED').length },
    { label: 'Đã giao', value: 'DELIVERED', count: all.filter((o: any) => o.status === 'DELIVERED').length },
    { label: 'Đã hủy', value: 'CANCELLED', count: all.filter((o: any) => o.status === 'CANCELLED').length }
  ]
})

const filteredOrders = computed(() => {
  let list = recentOrdersList.value
  if (orderFilterStatus.value !== 'ALL') {
    list = list.filter((o: any) => o.status === orderFilterStatus.value)
  }
  if (orderSearch.value.trim()) {
    const q = orderSearch.value.toLowerCase().trim()
    list = list.filter((o: any) => {
      const num = (o.orderNumber || `#${o.id}`).toLowerCase()
      const name = (o.fullName || '').toLowerCase()
      const phone = (o.phone || '').toLowerCase()
      return num.includes(q) || name.includes(q) || phone.includes(q)
    })
  }
  return list
})

// ==================== FORMATTING HELPERS ====================
const formatPrice = (price: number | null | undefined) => {
  if (!price) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(Number(price))
}

const formatCompactPrice = (price: number) => {
  if (price >= 1_000_000) return (price / 1_000_000).toFixed(1) + 'M'
  if (price >= 1_000) return (price / 1_000).toFixed(0) + 'k'
  return price.toString()
}

const formatDate = (dateStr: string | null) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const formatFullDate = (dateStr: string) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('vi-VN', { weekday: 'long', day: '2-digit', month: '2-digit', year: 'numeric' })
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'Chờ xử lý',
    CONFIRMED: 'Đã xác nhận',
    SHIPPED: 'Đang giao',
    DELIVERED: 'Đã giao',
    CANCELLED: 'Đã hủy',
  }
  return map[status] || status
}

const getStatusClass = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'bg-amber-500 text-white font-bold shadow-xs',
    CONFIRMED: 'bg-blue-600 text-white font-bold shadow-xs',
    SHIPPED: 'bg-indigo-600 text-white font-bold shadow-xs',
    DELIVERED: 'bg-emerald-600 text-white font-bold shadow-xs',
    CANCELLED: 'bg-rose-600 text-white font-bold shadow-xs',
  }
  return map[status] || 'bg-slate-800 text-white font-bold shadow-xs'
}

const getStatusDotClass = (status: string) => {
  return 'bg-white'
}
</script>
