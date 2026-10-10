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
      class="flex flex-col w-[380px] sm:w-[420px] h-[580px] max-h-[85vh] bg-white rounded-2xl shadow-2xl border border-gray-100 overflow-hidden transition-all duration-300 animate-in fade-in slide-in-from-bottom-5"
    >
      <!-- Header -->
      <div class="flex items-center justify-between px-4 py-3.5 bg-gradient-to-r from-[#0077cc] to-[#09f] text-white shadow-sm">
        <div class="flex items-center gap-2.5">
          <div class="w-9 h-9 rounded-full bg-white/20 backdrop-blur-md flex items-center justify-center border border-white/30 text-white font-bold text-sm">
            AI
          </div>
          <div>
            <h3 class="font-bold text-sm leading-tight">Trợ Lý Linh Kiện DALN</h3>
            <div class="flex items-center gap-1.5 text-xs text-white/80">
              <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
              <span>Qwen 2.5 (Online)</span>
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
            Hỏi tôi về thông số linh kiện, tra cứu giá bán, hoặc kiểm tra số lượng tồn kho theo thời gian thực.
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
            <div class="w-6 h-6 rounded-full bg-[#09f] text-white flex-shrink-0 flex items-center justify-center text-[10px] font-bold mt-1">
              AI
            </div>
            <div class="max-w-[85%] flex flex-col gap-1">
              <div class="px-3.5 py-2.5 bg-white border border-gray-200 text-gray-800 rounded-2xl rounded-tl-xs text-xs sm:text-sm leading-relaxed shadow-xs break-words whitespace-pre-wrap">
                {{ msg.content }}
              </div>
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
            <span class="text-[11px] text-gray-400 ml-1">Đang tra cứu dữ liệu...</span>
          </div>
        </div>
      </div>

      <!-- Footer / Input Form -->
      <div class="p-3 bg-white border-t border-gray-100">
        <form @submit.prevent="sendMessage" class="flex items-center gap-2">
          <input
            v-model="inputMessage"
            type="text"
            placeholder="Hỏi về linh kiện, tồn kho..."
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
import { ref, nextTick, onMounted } from 'vue'
import { useAuth } from '~/composables/useAuth'

interface Message {
  role: 'user' | 'assistant'
  content: string
  toolsUsed?: string[]
}

const isOpen = ref(false)
const inputMessage = ref('')
const isLoading = ref(false)
const sessionId = ref<string | null>(null)
const messages = ref<Message[]>([])
const messagesContainer = ref<HTMLElement | null>(null)

const { user, token } = useAuth()

const suggestions = [
  'Tư vấn bo mạch Arduino Uno R3 và giá bán',
  'ESP32 DevKit V1 còn hàng không?',
  'Top mặt hàng nào bán chạy nhất?',
  'Kiểm tra danh sách linh kiện sắp hết hàng'
]

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
    case 'check_stock': return 'Kiểm tra tồn kho'
    case 'get_low_stock_products': return 'Cảnh báo tồn kho'
    case 'get_top_selling_products': return 'Top bán chạy'
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
      toolsUsed: response?.toolsUsed || []
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

</script>
