<template>
  <label class="inline-flex items-center cursor-pointer group">
    <input
      type="checkbox"
      :checked="modelValue"
      @change="$emit('update:modelValue', ($event.target as HTMLInputElement).checked)"
      class="sr-only peer"
    />
    <div
      :class="[
        'relative w-5 h-5 rounded border-2 transition-all duration-200 flex items-center justify-center',
        modelValue
          ? 'bg-[#09f] border-[#09f]'
          : 'bg-white border-gray-300 group-hover:border-[#09f]'
      ]"
    >
      <svg
        v-if="modelValue"
        class="w-3.5 h-3.5 text-white"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="3"
          d="M5 13l4 4L19 7"
        />
      </svg>
    </div>
    <span v-if="$slots.default" class="ml-2 select-none">
      <slot />
    </span>
  </label>
</template>

<script setup lang="ts">
interface Props {
  modelValue?: boolean
}

defineProps<Props>()

defineEmits<{
  'update:modelValue': [value: boolean]
}>()
</script>
