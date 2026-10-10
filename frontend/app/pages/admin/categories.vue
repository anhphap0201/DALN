<template>
  <div class="min-h-screen bg-gray-50 p-8">
    <div class="max-w-6xl mx-auto">
      <!-- Header -->
      <div class="flex items-center justify-between mb-8">
        <div class="flex items-center gap-4">
          <NuxtLink to="/admin" class="p-2 hover:bg-gray-200 rounded-lg transition-colors" title="Quay lại">
            <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
          </NuxtLink>
          <div>
            <h1 class="text-3xl font-bold text-gray-900">Quản lý Danh mục</h1>
            <p class="text-gray-600 mt-1">Tổng số: {{ categories.length }} danh mục</p>
          </div>
        </div>
        <button
          @click="openCreateModal"
          class="flex items-center gap-2 px-6 py-3 bg-[#09f] text-white rounded-xl hover:bg-[#0077cc] transition-all shadow-lg hover:shadow-xl"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          Thêm danh mục
        </button>
      </div>

      <!-- Search -->
      <div class="bg-white rounded-xl p-4 mb-6 shadow-sm">
        <div class="relative">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tìm kiếm danh mục..."
            class="w-full px-4 py-3 pl-12 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#09f]"
          />
          <svg class="w-5 h-5 text-gray-400 absolute left-4 top-1/2 -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </div>
      </div>

      <!-- Categories Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="category in filteredCategories"
          :key="category.id"
          class="bg-white rounded-xl p-6 shadow-sm border border-gray-200 hover:shadow-md transition-all"
        >
          <div class="flex items-start gap-4">
            <div class="w-16 h-16 bg-gradient-to-br from-blue-50 to-cyan-50 rounded-xl flex items-center justify-center overflow-hidden">
              <img
                v-if="category.imageUrl"
                :src="category.imageUrl.startsWith('/uploads') ? `http://localhost:8080${category.imageUrl}` : category.imageUrl"
                :alt="category.name"
                class="w-full h-full object-cover"
              />
              <svg v-else class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
              </svg>
            </div>
            <div class="flex-1 min-w-0">
              <h3 class="text-lg font-semibold text-gray-900 mb-1">{{ category.name }}</h3>
              <p class="text-sm text-gray-500 mb-2">{{ category.slug }}</p>
              <p class="text-xs text-gray-600 line-clamp-2">{{ category.description || 'Không có mô tả' }}</p>
            </div>
          </div>
          <div class="flex items-center gap-2 mt-4 pt-4 border-t border-gray-200">
            <button
              @click="openEditModal(category)"
              class="flex-1 flex items-center justify-center gap-2 px-4 py-2 text-blue-600 bg-blue-50 rounded-lg hover:bg-blue-100 transition-colors"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              Sửa
            </button>
            <button
              @click="confirmDelete(category)"
              class="flex-1 flex items-center justify-center gap-2 px-4 py-2 text-red-600 bg-red-50 rounded-lg hover:bg-red-100 transition-colors"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
              Xóa
            </button>
          </div>
        </div>

        <div v-if="filteredCategories.length === 0" class="col-span-full text-center py-12 text-gray-500">
          Không tìm thấy danh mục nào
        </div>
      </div>

      <!-- Create/Edit Modal -->
      <div
        v-if="showModal"
        class="fixed inset-0 bg-white/20 backdrop-blur-sm flex items-center justify-center z-50 p-4"
        @click.self="closeModal"
      >
        <div class="bg-white rounded-2xl max-w-lg w-full">
          <div class="p-6 border-b border-gray-200">
            <h2 class="text-2xl font-bold text-gray-900">
              {{ isEditMode ? 'Sửa danh mục' : 'Thêm danh mục mới' }}
            </h2>
          </div>
          <div class="p-6 space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Tên danh mục *</label>
              <input
                v-model="formData.name"
                type="text"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#09f]"
                placeholder="Nhập tên danh mục"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Slug *</label>
              <input
                v-model="formData.slug"
                type="text"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#09f]"
                placeholder="ten-danh-muc"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Hình ảnh danh mục</label>
              <div v-if="formData.imageUrl && !selectedFile" class="mb-2">
                <img :src="`http://localhost:8080${formData.imageUrl}`" alt="Preview" class="w-32 h-32 object-cover rounded-lg border">
                <p class="text-xs text-green-600 mt-1">Ảnh hiện tại (sẽ giữ nguyên nếu không chọn ảnh mới)</p>
              </div>
              <div v-if="selectedFile" class="mb-2">
                <p class="text-xs text-blue-600 mb-1">Ảnh mới sẽ được tải lên: {{ selectedFile.name }}</p>
              </div>
              <input
                ref="fileInput"
                type="file"
                accept="image/*"
                @change="handleFileSelect"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#09f]"
              />
              <p class="text-xs text-gray-500 mt-1">{{ isEditMode ? 'Chọn ảnh mới để thay đổi (bỏ qua nếu giữ nguyên)' : 'Chọn ảnh danh mục' }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Mô tả</label>
              <textarea
                v-model="formData.description"
                rows="3"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#09f]"
                placeholder="Mô tả về danh mục"
              ></textarea>
            </div>
          </div>
          <div class="p-6 border-t border-gray-200 flex gap-3 justify-end">
            <button
              @click="closeModal"
              class="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
            >
              Hủy
            </button>
            <button
              @click="saveCategory"
              class="px-6 py-2 bg-[#09f] text-white rounded-lg hover:bg-[#0077cc] transition-colors"
            >
              {{ isEditMode ? 'Cập nhật' : 'Tạo mới' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useConfirm } from '~/composables/useConfirm'
import { useNotification } from '~/composables/useNotification'
import { useAuth } from '~/composables/useAuth'

const API_BASE_URL = 'http://localhost:8080/api'

// State
const categories = ref<any[]>([])
const searchQuery = ref('')
const showModal = ref(false)
const isEditMode = ref(false)
const currentCategory = ref<any>(null)

const formData = ref({
  name: '',
  slug: '',
  imageUrl: '',
  description: ''
})

const fileInput = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)
const uploadingImage = ref(false)

// Computed
const filteredCategories = computed(() => {
  if (!searchQuery.value) return categories.value
  const query = searchQuery.value.toLowerCase()
  return categories.value.filter(c =>
    c.name.toLowerCase().includes(query) ||
    c.slug.toLowerCase().includes(query)
  )
})

// Methods
const fetchCategories = async () => {
  try {
    const data = await $fetch(`${API_BASE_URL}/categories`)
    categories.value = data as any[]
  } catch (error) {
    console.error('Error fetching categories:', error)
    const { error: showError } = useNotification()
    showError('Lỗi khi tải danh sách danh mục')
  }
}

const openCreateModal = () => {
  isEditMode.value = false
  currentCategory.value = null
  selectedFile.value = null
  formData.value = {
    name: '',
    slug: '',
    imageUrl: '',
    description: ''
  }
  showModal.value = true
}

const openEditModal = (category: any) => {
  isEditMode.value = true
  currentCategory.value = category
  selectedFile.value = null
  formData.value = {
    name: category.name || '',
    slug: category.slug || '',
    imageUrl: category.imageUrl || '',
    description: category.description || ''
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const saveCategory = async () => {
  try {
    if (!formData.value.name || !formData.value.slug) {
      const { error: showError } = useNotification()
      showError('Vui lòng điền đầy đủ thông tin bắt buộc')
      return
    }

    // Upload image first if selected
    if (selectedFile.value) {
      uploadingImage.value = true
      try {
        const imageFormData = new FormData()
        imageFormData.append('file', selectedFile.value)

        const response = await fetch(`http://localhost:8080/api/upload/image`, {
          method: 'POST',
          body: imageFormData
        })

        if (!response.ok) {
          const errorData = await response.json()
          throw new Error(errorData.error || 'Upload failed')
        }

        const uploadResponse = await response.json()
        formData.value.imageUrl = uploadResponse.url
      } catch (uploadError: any) {
        console.error('Error uploading image:', uploadError)
        const { error: showError } = useNotification()
        showError('Lỗi khi tải ảnh lên: ' + (uploadError.message || 'Unknown error'))
        uploadingImage.value = false
        return
      }
      uploadingImage.value = false
    }

    const auth = useAuth()
    const { success: showSuccess, error: showError } = useNotification()

    if (isEditMode.value && currentCategory.value) {
      // Update
      await $fetch(`${API_BASE_URL}/categories/${currentCategory.value.id}`, {
        method: 'PUT',
        body: formData.value,
        headers: auth.getAuthHeader()
      })
      showSuccess('Cập nhật danh mục thành công!')
    } else {
      // Create
      await $fetch(`${API_BASE_URL}/categories`, {
        method: 'POST',
        body: formData.value,
        headers: auth.getAuthHeader()
      })
      showSuccess('Thêm danh mục thành công!')
    }

    closeModal()
    await fetchCategories()
  } catch (error: any) {
    console.error('Error saving category:', error)
    const { error: showError } = useNotification()
    showError('Lỗi: ' + (error.data || error.message || 'Không thể lưu danh mục'))
  }
}

const confirmDelete = async (category: any) => {
  const { confirm } = useConfirm()
  const auth = useAuth()
  const { success: showSuccess, error: showError } = useNotification()

  const ok = await confirm(`Bạn có chắc muốn xóa danh mục "${category.name}"?`, 'Xác nhận xóa')
  if (!ok) return

  try {
    await $fetch(`${API_BASE_URL}/categories/${category.id}`, {
      method: 'DELETE',
      headers: auth.getAuthHeader()
    })
    showSuccess('Xóa danh mục thành công!')
    await fetchCategories()
  } catch (error: any) {
    console.error('Error deleting category:', error)
    showError('Lỗi: ' + (error.data || error.message || 'Không thể xóa danh mục'))
  }
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    selectedFile.value = target.files[0]
  }
}

// Lifecycle
onMounted(() => {
  fetchCategories()
})
</script>
