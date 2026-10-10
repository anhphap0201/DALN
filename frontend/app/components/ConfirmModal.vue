<template>
  <teleport to="body">
    <div v-if="state.visible" class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="absolute inset-0 bg-black/50" @click="cancel"></div>
      <div class="relative bg-white rounded-xl p-6 w-full max-w-md z-10 shadow-xl">
        <h3 v-if="state.title" class="text-lg font-semibold mb-2">{{ state.title }}</h3>
        <p class="text-sm text-gray-700 mb-4">{{ state.message }}</p>
        <div class="flex justify-end gap-3">
          <button @click="cancel" class="px-4 py-2 rounded-lg bg-gray-100">{{ state.cancelText }}</button>
          <button @click="confirm" class="px-4 py-2 rounded-lg bg-[#09f] text-white">{{ state.confirmText }}</button>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { useConfirm } from '~/composables/useConfirm'

const { state, resolveConfirm } = useConfirm()

const confirm = () => {
  resolveConfirm(true)
}

const cancel = () => {
  resolveConfirm(false)
}
</script>

<style scoped>
/* minimal styles, tailwind already used */
</style>
