import { ref } from 'vue'

interface Locker {
  lockerId: string
  locationName: string
  address: string
  status: string
  totalCompartments: number
  availableCompartments: number
}

const API_BASE_URL = 'http://localhost:8080'

export const useSmartLocker = () => {
  const lockers = ref<Locker[]>([])
  const loadingLockers = ref(false)
  const selectedLocker = ref<string | null>(null)

  const { getAuthHeader } = useAuth()

  const fetchLockers = async () => {
    loadingLockers.value = true
    try {
      const data = await $fetch<Locker[]>(`${API_BASE_URL}/api/smart-locker/lockers`, {
        headers: {
          ...(getAuthHeader() as Record<string, string>)
        }
      })
      lockers.value = data || []
    } catch (error) {
      console.error('Error fetching lockers:', error)
      lockers.value = []
    } finally {
      loadingLockers.value = false
    }
  }

  const selectLocker = (lockerId: string) => {
    selectedLocker.value = lockerId
  }

  const reset = () => {
    selectedLocker.value = null
  }

  return {
    lockers,
    loadingLockers,
    selectedLocker,
    fetchLockers,
    selectLocker,
    reset
  }
}
