<template>
  <teleport to="body">
    <transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="opacity-0 scale-95"
      enter-to-class="opacity-100 scale-100"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="opacity-100 scale-100"
      leave-to-class="opacity-0 scale-95"
    >
      <div v-if="state.visible" class="fixed inset-0 z-[9999] flex items-center justify-center p-4">
        <!-- Backdrop -->
        <div class="fixed inset-0 bg-black/50 backdrop-blur-xs transition-opacity" @click="closeAlert"></div>

        <!-- Popup Box -->
        <div class="relative bg-white rounded-2xl p-6 w-full max-w-sm sm:max-w-md z-10 shadow-2xl border border-gray-100 transform text-center">
          <!-- Icon -->
          <div
            class="w-14 h-14 rounded-full flex items-center justify-center mx-auto mb-4"
            :class="iconBgClass"
          >
            <!-- Success Icon -->
            <svg v-if="state.type === 'success'" class="w-7 h-7 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
            <!-- Error Icon -->
            <svg v-else-if="state.type === 'error'" class="w-7 h-7 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
            <!-- Warning Icon -->
            <svg v-else-if="state.type === 'warning'" class="w-7 h-7 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <!-- Info Icon -->
            <svg v-else class="w-7 h-7 text-[#09f]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>

          <!-- Title -->
          <h3 v-if="state.title" class="text-lg font-bold text-gray-900 mb-2">
            {{ state.title }}
          </h3>

          <!-- Message -->
          <p class="text-sm text-gray-600 mb-6 leading-relaxed whitespace-pre-line">
            {{ state.message }}
          </p>

          <!-- Button -->
          <button
            @click="closeAlert"
            class="w-full py-2.5 px-4 rounded-xl text-white font-medium text-sm transition-all duration-200 shadow-sm hover:shadow active:scale-98"
            :class="buttonClass"
          >
            {{ state.buttonText || 'Đồng ý' }}
          </button>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAlert } from '~/composables/useAlert'

const { state, closeAlert } = useAlert()

const iconBgClass = computed(() => {
  switch (state.type) {
    case 'success':
      return 'bg-green-100'
    case 'error':
      return 'bg-red-100'
    case 'warning':
      return 'bg-amber-100'
    default:
      return 'bg-blue-100'
  }
})

const buttonClass = computed(() => {
  switch (state.type) {
    case 'success':
      return 'bg-green-600 hover:bg-green-700'
    case 'error':
      return 'bg-red-600 hover:bg-red-700'
    case 'warning':
      return 'bg-amber-600 hover:bg-amber-700'
    default:
      return 'bg-[#09f] hover:bg-[#0077cc]'
  }
})
</script>
