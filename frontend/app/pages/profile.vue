<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Hồ sơ của tôi</h1>
        <p class="mt-2 text-sm text-gray-600">Quản lý thông tin cá nhân, nhận hàng và mật khẩu</p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div class="lg:col-span-1">
          <div class="bg-white rounded-lg shadow-sm p-6">
            <div class="flex flex-col items-center">
              <div class="w-24 h-24 rounded-full bg-gradient-to-br from-[#09f] to-[#0077cc] flex items-center justify-center text-white text-3xl font-bold mb-4">
                {{ (user?.fullName || user?.email)?.charAt(0).toUpperCase() }}
              </div>
              <h2 class="text-xl font-semibold text-gray-900">{{ user?.fullName || user?.email }}</h2>
              <p v-if="user?.fullName" class="text-sm text-gray-500 mt-1">{{ user?.email }}</p>
              <p class="text-sm text-gray-500 mt-1">{{ user?.role === 'ROLE_ADMIN' ? 'Quản trị viên' : 'Khách hàng' }}</p>
            </div>

            <div class="mt-6 space-y-2">
              <button
                @click="activeTab = 'info'"
                :class="[
                  'w-full text-left px-4 py-3 rounded-lg transition-colors',
                  activeTab === 'info' ? 'bg-[#09f] text-white' : 'hover:bg-gray-100 text-gray-700'
                ]"
              >
                <div class="flex items-center gap-3">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                  <span>Thông tin tài khoản</span>
                </div>
              </button>

              <button
                @click="activeTab = 'password'"
                :class="[
                  'w-full text-left px-4 py-3 rounded-lg transition-colors',
                  activeTab === 'password' ? 'bg-[#09f] text-white' : 'hover:bg-gray-100 text-gray-700'
                ]"
              >
                <div class="flex items-center gap-3">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
                  </svg>
                  <span>Đổi mật khẩu</span>
                </div>
              </button>
            </div>
          </div>
        </div>

        <div class="lg:col-span-2">
          <div v-if="activeTab === 'info'" class="bg-white rounded-lg shadow-sm p-6">
            <h3 class="text-xl font-semibold text-gray-900 mb-6">Thông tin nhận hàng</h3>

            <form @submit.prevent="handleSaveProfile" class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Họ và tên *</label>
                <input
                  v-model="profileForm.fullName"
                  type="text"
                  required
                  class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#09f] focus:border-transparent outline-none"
                  placeholder="Nhập họ và tên"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Số điện thoại *</label>
                <input
                  v-model="profileForm.phone"
                  type="text"
                  required
                  class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#09f] focus:border-transparent outline-none"
                  placeholder="Nhập số điện thoại"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Email *</label>
                <input
                  v-model="profileForm.email"
                  type="email"
                  required
                  class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#09f] focus:border-transparent outline-none"
                  placeholder="email@example.com"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Tỉnh/Thành phố *</label>
                <select
                  v-model="selectedProvinceCode"
                  required
                  class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#09f] focus:border-transparent outline-none"
                  @change="handleProvinceChange"
                >
                  <option value="">Chọn tỉnh/thành</option>
                  <option v-for="province in provinces" :key="province.code" :value="String(province.code)">
                    {{ province.name }}
                  </option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Phường/Xã *</label>
                <select
                  v-model="selectedWardCode"
                  required
                  :disabled="!selectedProvinceCode || wardLoading"
                  class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#09f] focus:border-transparent outline-none disabled:bg-gray-100"
                >
                  <option value="">Chọn phường/xã</option>
                  <option v-for="ward in wards" :key="ward.code" :value="String(ward.code)">
                    {{ ward.name }}
                  </option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Địa chỉ cụ thể *</label>
                <textarea
                  v-model="profileForm.detailedAddress"
                  required
                  rows="3"
                  class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#09f] focus:border-transparent outline-none"
                  placeholder="Nhập số nhà, tên đường..."
                />
              </div>

              <div class="flex gap-3 pt-4">
                <button
                  type="submit"
                  :disabled="isSavingProfile || isLoadingProfile"
                  class="flex-1 bg-[#09f] text-white py-3 rounded-lg font-medium hover:bg-[#0077cc] transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
                >
                  {{ isSavingProfile ? 'Đang lưu...' : 'Lưu thông tin' }}
                </button>
                <button
                  type="button"
                  @click="handleClearProfile"
                  :disabled="isClearingProfile || isLoadingProfile"
                  class="px-6 py-3 border border-red-300 rounded-lg font-medium text-red-700 hover:bg-red-50 transition-colors disabled:opacity-50"
                >
                  {{ isClearingProfile ? 'Đang xoá...' : 'Xoá thông tin' }}
                </button>
              </div>
            </form>
          </div>

          <div v-if="activeTab === 'password'" class="bg-white rounded-lg shadow-sm p-6">
            <h3 class="text-xl font-semibold text-gray-900 mb-6">Đổi mật khẩu</h3>

            <form @submit.prevent="handleChangePassword" class="space-y-4">
              <div>
                <label for="currentPassword" class="block text-sm font-medium text-gray-700 mb-2">
                  Mật khẩu hiện tại *
                </label>
                <div class="relative">
                  <input
                    id="currentPassword"
                    v-model="passwordForm.currentPassword"
                    :type="showCurrentPassword ? 'text' : 'password'"
                    required
                    class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#09f] focus:border-transparent outline-none transition-all"
                    placeholder="Nhập mật khẩu hiện tại"
                  />
                  <button
                    type="button"
                    @click="showCurrentPassword = !showCurrentPassword"
                    class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  >
                    <svg v-if="!showCurrentPassword" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                    <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                    </svg>
                  </button>
                </div>
              </div>

              <div>
                <label for="newPassword" class="block text-sm font-medium text-gray-700 mb-2">
                  Mật khẩu mới *
                </label>
                <div class="relative">
                  <input
                    id="newPassword"
                    v-model="passwordForm.newPassword"
                    :type="showNewPassword ? 'text' : 'password'"
                    required
                    minlength="6"
                    class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#09f] focus:border-transparent outline-none transition-all"
                    placeholder="Nhập mật khẩu mới (tối thiểu 6 ký tự)"
                  />
                  <button
                    type="button"
                    @click="showNewPassword = !showNewPassword"
                    class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  >
                    <svg v-if="!showNewPassword" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                    <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                    </svg>
                  </button>
                </div>
                <p class="mt-1 text-xs text-gray-500">Mật khẩu phải có ít nhất 6 ký tự</p>
              </div>

              <div>
                <label for="confirmPassword" class="block text-sm font-medium text-gray-700 mb-2">
                  Xác nhận mật khẩu mới *
                </label>
                <div class="relative">
                  <input
                    id="confirmPassword"
                    v-model="passwordForm.confirmPassword"
                    :type="showConfirmPassword ? 'text' : 'password'"
                    required
                    class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#09f] focus:border-transparent outline-none transition-all"
                    placeholder="Nhập lại mật khẩu mới"
                  />
                  <button
                    type="button"
                    @click="showConfirmPassword = !showConfirmPassword"
                    class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  >
                    <svg v-if="!showConfirmPassword" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                    <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                    </svg>
                  </button>
                </div>
              </div>

              <div v-if="passwordForm.newPassword" class="space-y-2">
                <div class="flex items-center gap-2">
                  <div class="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden">
                    <div
                      :class="[
                        'h-full transition-all duration-300',
                        passwordStrength === 'weak' ? 'w-1/3 bg-red-500' :
                        passwordStrength === 'medium' ? 'w-2/3 bg-yellow-500' :
                        'w-full bg-green-500'
                      ]"
                    ></div>
                  </div>
                  <span :class="[
                    'text-xs font-medium',
                    passwordStrength === 'weak' ? 'text-red-600' :
                    passwordStrength === 'medium' ? 'text-yellow-600' :
                    'text-green-600'
                  ]">
                    {{
                      passwordStrength === 'weak' ? 'Yếu' :
                      passwordStrength === 'medium' ? 'Trung bình' :
                      'Mạnh'
                    }}
                  </span>
                </div>
              </div>

              <div class="flex gap-3 pt-4">
                <button
                  type="submit"
                  :disabled="isChangingPassword"
                  class="flex-1 bg-[#09f] text-white py-3 rounded-lg font-medium hover:bg-[#0077cc] transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
                >
                  <span v-if="!isChangingPassword">Đổi mật khẩu</span>
                  <span v-else class="flex items-center justify-center gap-2">Đang xử lý...</span>
                </button>
                <button
                  type="button"
                  @click="resetPasswordForm"
                  class="px-6 py-3 border border-gray-300 rounded-lg font-medium text-gray-700 hover:bg-gray-50 transition-colors"
                >
                  Hủy
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

definePageMeta({
  middleware: 'auth'
})

const { user, getAuthHeader, logout } = useAuth()
const { success, error } = useNotification()

if (!user.value) {
  navigateTo('/auth/auth')
}

type ActiveTab = 'info' | 'password'

interface ProvinceItem {
  code: number
  name: string
}

interface WardItem {
  code: number
  name: string
}

interface UserProfileResponse {
  id: number
  fullName: string
  phone: string
  email: string
  province: string
  ward: string
  detailedAddress: string
  role: string
}

const activeTab = ref<ActiveTab>('info')
const API_BASE = 'http://localhost:8080'
const PROVINCE_API = 'https://provinces.open-api.vn/api/v2'

const profileForm = ref({
  fullName: '',
  phone: '',
  email: '',
  detailedAddress: ''
})

const selectedProvinceCode = ref('')
const selectedWardCode = ref('')

const provinces = ref<ProvinceItem[]>([])
const wards = ref<WardItem[]>([])

const isLoadingProfile = ref(false)
const isSavingProfile = ref(false)
const isClearingProfile = ref(false)
const wardLoading = ref(false)

const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const showCurrentPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)
const isChangingPassword = ref(false)

const passwordStrength = computed(() => {
  const password = passwordForm.value.newPassword
  if (password.length < 6) return 'weak'
  if (password.length < 10) return 'medium'

  const hasLower = /[a-z]/.test(password)
  const hasUpper = /[A-Z]/.test(password)
  const hasNumber = /[0-9]/.test(password)
  const hasSpecial = /[^a-zA-Z0-9]/.test(password)

  const strength = [hasLower, hasUpper, hasNumber, hasSpecial].filter(Boolean).length

  if (strength >= 3) return 'strong'
  if (strength >= 2) return 'medium'
  return 'weak'
})

const fetchProvinces = async () => {
  try {
    const data = await $fetch<any>(`${PROVINCE_API}/?depth=1`)
    provinces.value = Array.isArray(data) ? data : (data?.data || data?.value || [])
  } catch (err) {
    console.error('Error fetching provinces:', err)
    provinces.value = []
  }
}

const fetchWardsByProvince = async (provinceCode: string) => {
  if (!provinceCode) {
    wards.value = []
    return
  }

  try {
    wardLoading.value = true
    const data = await $fetch<any>(`${PROVINCE_API}/p/${provinceCode}?depth=2`)
    const province = data?.data || data?.value || data
    wards.value = province?.wards || []
  } catch (err) {
    console.error('Error fetching wards:', err)
    wards.value = []
  } finally {
    wardLoading.value = false
  }
}

const findProvinceName = (provinceCode: string) => {
  const province = provinces.value.find((item) => String(item.code) === provinceCode)
  return province?.name || ''
}

const findWardName = (wardCode: string) => {
  const ward = wards.value.find((item) => String(item.code) === wardCode)
  return ward?.name || ''
}

const handleProvinceChange = async () => {
  selectedWardCode.value = ''
  await fetchWardsByProvince(selectedProvinceCode.value)
}

const syncProvinceWardSelection = async (provinceName?: string, wardName?: string) => {
  if (!provinceName) {
    selectedProvinceCode.value = ''
    selectedWardCode.value = ''
    wards.value = []
    return
  }

  const matchedProvince = provinces.value.find((item) => item.name === provinceName)
  if (!matchedProvince) {
    selectedProvinceCode.value = ''
    selectedWardCode.value = ''
    wards.value = []
    return
  }

  selectedProvinceCode.value = String(matchedProvince.code)
  await fetchWardsByProvince(selectedProvinceCode.value)

  if (!wardName) {
    selectedWardCode.value = ''
    return
  }

  const matchedWard = wards.value.find((item) => item.name === wardName)
  selectedWardCode.value = matchedWard ? String(matchedWard.code) : ''
}

const updateStoredAuthUser = (payload: { fullName: string; email: string; role?: string }) => {
  if (!user.value) return

  const nextUser = {
    ...user.value,
    fullName: payload.fullName,
    email: payload.email,
    role: payload.role || user.value.role
  }

  user.value = nextUser

  if (import.meta.client) {
    localStorage.setItem('user', JSON.stringify(nextUser))
  }
}

const authHeaders = (): Record<string, string> => {
  return getAuthHeader() as Record<string, string>
}

const fetchProfile = async () => {
  isLoadingProfile.value = true

  try {
    const data = await $fetch<UserProfileResponse>(`${API_BASE}/api/user/profile`, {
      headers: authHeaders()
    })

    profileForm.value.fullName = data.fullName || ''
    profileForm.value.phone = data.phone || ''
    profileForm.value.email = data.email || user.value?.email || ''
    profileForm.value.detailedAddress = data.detailedAddress || ''

    updateStoredAuthUser({
      fullName: data.fullName || '',
      email: data.email || user.value?.email || '',
      role: data.role
    })

    await syncProvinceWardSelection(data.province, data.ward)
  } catch (err: any) {
    console.error('Fetch profile error:', err)

    profileForm.value.fullName = user.value?.fullName || ''
    profileForm.value.email = user.value?.email || ''

    if (err?.status === 401) {
      error('Phiên đăng nhập hết hạn', 'Vui lòng đăng nhập lại')
      logout()
      navigateTo('/auth/auth')
    }
  } finally {
    isLoadingProfile.value = false
  }
}

const handleSaveProfile = async () => {
  const province = findProvinceName(selectedProvinceCode.value)
  const ward = findWardName(selectedWardCode.value)

  if (!province || !ward) {
    error('Thiếu thông tin', 'Vui lòng chọn tỉnh/thành và phường/xã')
    return
  }

  isSavingProfile.value = true

  try {
    const payload = {
      fullName: profileForm.value.fullName.trim(),
      phone: profileForm.value.phone.trim(),
      email: profileForm.value.email.trim(),
      province,
      ward,
      detailedAddress: profileForm.value.detailedAddress.trim()
    }

    const data = await $fetch<UserProfileResponse>(`${API_BASE}/api/user/profile`, {
      method: 'PUT',
      headers: authHeaders(),
      body: payload
    })

    updateStoredAuthUser({
      fullName: data.fullName || payload.fullName,
      email: data.email || payload.email,
      role: data.role
    })

    success('Lưu thành công', 'Đã cập nhật thông tin nhận hàng')
  } catch (err: any) {
    console.error('Save profile error:', err)
    error('Lưu thất bại', err?.data?.message || 'Không thể cập nhật thông tin nhận hàng')
  } finally {
    isSavingProfile.value = false
  }
}

const handleClearProfile = async () => {
  isClearingProfile.value = true

  try {
    const data = await $fetch<UserProfileResponse>(`${API_BASE}/api/user/profile`, {
      method: 'DELETE',
      headers: authHeaders()
    })

    profileForm.value.fullName = data.fullName || ''
    profileForm.value.phone = data.phone || ''
    profileForm.value.email = data.email || user.value?.email || ''
    profileForm.value.detailedAddress = data.detailedAddress || ''
    selectedProvinceCode.value = ''
    selectedWardCode.value = ''
    wards.value = []

    updateStoredAuthUser({
      fullName: data.fullName || '',
      email: data.email || user.value?.email || '',
      role: data.role
    })

    success('Đã xoá', 'Đã xoá thông tin nhận hàng')
  } catch (err: any) {
    console.error('Clear profile error:', err)
    error('Xoá thất bại', err?.data?.message || 'Không thể xoá thông tin nhận hàng')
  } finally {
    isClearingProfile.value = false
  }
}

const handleChangePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    error('Mật khẩu không khớp', 'Mật khẩu mới và xác nhận mật khẩu phải giống nhau')
    return
  }

  if (passwordForm.value.newPassword.length < 6) {
    error('Mật khẩu quá ngắn', 'Mật khẩu phải có ít nhất 6 ký tự')
    return
  }

  if (passwordForm.value.currentPassword === passwordForm.value.newPassword) {
    error('Mật khẩu trùng lặp', 'Mật khẩu mới phải khác mật khẩu hiện tại')
    return
  }

  isChangingPassword.value = true

  try {
    await $fetch(`${API_BASE}/auth/change-password`, {
      method: 'POST',
      headers: authHeaders(),
      body: {
        currentPassword: passwordForm.value.currentPassword,
        newPassword: passwordForm.value.newPassword,
        confirmPassword: passwordForm.value.confirmPassword
      }
    })

    success('Đổi mật khẩu thành công!', 'Vui lòng đăng nhập lại với mật khẩu mới')

    resetPasswordForm()

    setTimeout(() => {
      logout()
      navigateTo('/auth/auth')
    }, 2000)
  } catch (err: any) {
    console.error('Change password error:', err)
    error(
      'Đổi mật khẩu thất bại',
      err.data?.message || err.message || 'Mật khẩu hiện tại không đúng'
    )
  } finally {
    isChangingPassword.value = false
  }
}

const resetPasswordForm = () => {
  passwordForm.value = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  showCurrentPassword.value = false
  showNewPassword.value = false
  showConfirmPassword.value = false
}

onMounted(async () => {
  await fetchProvinces()
  await fetchProfile()
})
</script>
