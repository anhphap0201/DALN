import { ref, computed } from 'vue'
import type { Ref } from 'vue'

interface CartItem {
  id: number
  productVariantId: number
  productName: string
  variantName: string
  imageUrl: string
  price: number
  quantity: number
  inStock: number
}

interface Cart {
  id: number
  userId: number
  cartItems: CartItem[]
  totalItems: number
  totalPrice: number
}

interface AddToCartRequest {
  productVariantId: number
  quantity: number
  price: number
}

// Global state (shared across all useCart() calls)
const API_BASE_URL = 'http://localhost:8080'
const cart: Ref<Cart | null> = ref(null)
const loading = ref(false)
const error = ref<string | null>(null)

export const useCart = () => {
  const { success, error: showError } = useNotification()
  const { token: authToken, isAuthenticated } = useAuth()
  const router = useRouter()

  // Fetch cart from backend
  const fetchCart = async () => {
    if (!isAuthenticated.value) {
      console.warn('User not authenticated, cannot fetch cart')
      return
    }

    loading.value = true
    error.value = null

    try {
      const token = authToken.value
      console.log('Token for fetchCart:', token ? `${token.substring(0, 20)}...` : 'NO TOKEN')
      
      if (!token) {
        throw new Error('Bạn cần đăng nhập để xem giỏ hàng')
      }

      const response = await fetch(`${API_BASE_URL}/api/cart`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })

      console.log('fetchCart response status:', response.status)

      if (response.status === 401) {
        // Don't auto-logout, just show empty cart
        console.warn('401 Unauthorized - token invalid or expired')
        cart.value = null
        return
      }

      if (!response.ok) {
        throw new Error('Không thể tải giỏ hàng')
      }

      cart.value = await response.json()
      console.log('Cart loaded successfully:', cart.value)
    } catch (err: any) {
      error.value = err.message
      showError(err.message)
      console.error('Cart fetch error:', err)
    } finally {
      loading.value = false
    }
  }

  // Add item to cart (authenticated users only)
  const addToCart = async (productVariantId: number, quantity: number = 1, price: number) => {
    console.log('addToCart called:', { productVariantId, quantity, price, isAuthenticated: isAuthenticated.value })
    
    if (!isAuthenticated.value) {
      showError('Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng')
      return false
    }

    loading.value = true
    error.value = null

    try {
      const token = authToken.value
      console.log('Token for addToCart:', token ? `${token.substring(0, 20)}...` : 'NO TOKEN')
      
      if (!token) {
        showError('Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng')
        return false
      }

      const requestBody: AddToCartRequest = {
        productVariantId,
        quantity,
        price
      }

      console.log('Request body:', requestBody)

      const response = await fetch(`${API_BASE_URL}/api/cart/items`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestBody)
      })

      console.log('addToCart response status:', response.status)

      if (response.status === 401) {
        console.warn('401 Unauthorized - redirecting to login')
        showError('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
        // Redirect to login without full page reload
        router.push('/auth/auth')
        return false
      }

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Không thể thêm vào giỏ hàng' }))
        throw new Error(errorData.message || 'Không thể thêm vào giỏ hàng')
      }

      cart.value = await response.json()
      console.log('Added to cart successfully:', cart.value)
      success('Đã thêm sản phẩm vào giỏ hàng')
      return true
    } catch (err: any) {
      error.value = err.message
      showError(err.message)
      console.error('addToCart error:', err)
      return false
    } finally {
      loading.value = false
    }
  }

  // Update cart item quantity
  const updateCartItem = async (cartItemId: number, quantity: number) => {
    if (!isAuthenticated.value) {
      showError('Vui lòng đăng nhập để cập nhật giỏ hàng')
      return false
    }

    loading.value = true
    error.value = null

    try {
      const token = authToken.value
      if (!token) {
        throw new Error('Bạn cần đăng nhập')
      }

      const response = await fetch(`${API_BASE_URL}/api/cart/items/${cartItemId}?quantity=${quantity}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })

      if (response.status === 401) {
        showError('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
        router.push('/auth/auth')
        return false
      }

      cart.value = await response.json()
      success('Đã cập nhật giỏ hàng')
      return true
    } catch (err: any) {
      error.value = err.message
      showError(err.message)
      return false
    } finally {
      loading.value = false
    }
  }

  // Remove item from cart
  const removeFromCart = async (cartItemId: number) => {
    if (!isAuthenticated.value) {
      showError('Vui lòng đăng nhập để xóa sản phẩm')
      return false
    }

    loading.value = true
    error.value = null

    try {
      const token = authToken.value
      if (!token) {
        throw new Error('Bạn cần đăng nhập')
      }

      const response = await fetch(`${API_BASE_URL}/api/cart/items/${cartItemId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })

      if (response.status === 401) {
        showError('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
        router.push('/auth/auth')
        return false
      }

      if (!response.ok) {
        throw new Error('Không thể xóa sản phẩm')
      }

      cart.value = await response.json()
      success('Đã xóa sản phẩm khỏi giỏ hàng')
      return true
    } catch (err: any) {
      error.value = err.message
      showError(err.message)
      return false
    } finally {
      loading.value = false
    }
  }

  // Clear entire cart
  const clearCart = async () => {
    if (!isAuthenticated.value) {
      showError('Vui lòng đăng nhập để xóa giỏ hàng')
      return false
    }

    loading.value = true
    error.value = null

    try {
      const token = authToken.value
      if (!token) {
        throw new Error('Bạn cần đăng nhập')
      }

      const response = await fetch(`${API_BASE_URL}/api/cart`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })

      if (response.status === 401) {
        showError('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
        router.push('/auth/auth')
        return false
      }

      if (!response.ok) {
        throw new Error('Không thể xóa giỏ hàng')
      }

      cart.value = null
      success('Đã xóa toàn bộ giỏ hàng')
      return true
    } catch (err: any) {
      error.value = err.message
      showError(err.message)
      return false
    } finally {
      loading.value = false
    }
  }

  // Get cart item count
  const cartItemCount = computed(() => {
    return cart.value?.cartItems?.reduce((sum, item) => sum + item.quantity, 0) || 0
  })

  // Get cart total price
  const cartTotalPrice = computed(() => {
    return cart.value?.totalPrice || 0
  })

  return {
    cart,
    loading,
    error,
    cartItemCount,
    cartTotalPrice,
    fetchCart,
    addToCart,
    updateCartItem,
    removeFromCart,
    clearCart
  }
}
