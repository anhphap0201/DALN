export const useProvinces = () => {
  const provinces = ref<any[]>([])
  const districts = ref<any[]>([])
  const wards = ref<any[]>([])
  const loading = ref(false)

  const API_URL = 'https://provinces.open-api.vn/api'

  // Lấy danh sách tỉnh/thành phố
  const fetchProvinces = async () => {
    try {
      loading.value = true
      const response = await fetch(`${API_URL}/p/`)
      const data = await response.json()
      provinces.value = data.data || []
    } catch (error) {
      console.error('Error fetching provinces:', error)
    } finally {
      loading.value = false
    }
  }

  // Lấy danh sách quận/huyện theo tỉnh
  const fetchDistricts = async (provinceCode: string | number) => {
    try {
      loading.value = true
      districts.value = []
      wards.value = []
      
      const response = await fetch(`${API_URL}/p/${provinceCode}?depth=2`)
      const data = await response.json()
      districts.value = data.data?.districts || []
    } catch (error) {
      console.error('Error fetching districts:', error)
    } finally {
      loading.value = false
    }
  }

  // Lấy danh sách phường/xã theo quận
  const fetchWards = async (districtCode: string | number) => {
    try {
      loading.value = true
      wards.value = []
      
      const response = await fetch(`${API_URL}/d/${districtCode}?depth=2`)
      const data = await response.json()
      wards.value = data.data?.wards || []
    } catch (error) {
      console.error('Error fetching wards:', error)
    } finally {
      loading.value = false
    }
  }

  // Lấy tên tỉnh/thành từ code
  const getProvinceName = (code: string | number) => {
    const province = provinces.value.find(p => p.code === code)
    return province?.name || ''
  }

  // Lấy tên quận/huyện từ code
  const getDistrictName = (code: string | number) => {
    const district = districts.value.find(d => d.code === code)
    return district?.name || ''
  }

  // Lấy tên phường/xã từ code
  const getWardName = (code: string | number) => {
    const ward = wards.value.find(w => w.code === code)
    return ward?.name || ''
  }

  onMounted(() => {
    fetchProvinces()
  })

  return {
    provinces,
    districts,
    wards,
    loading,
    fetchProvinces,
    fetchDistricts,
    fetchWards,
    getProvinceName,
    getDistrictName,
    getWardName
  }
}
