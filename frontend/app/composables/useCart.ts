import { ref, computed } from 'vue'
import type { Ref } from 'vue'

export interface CartItem {
  id: number
  productVariantId: number
  productName: string
  variantName: string
  imageUrl: string
  price: number
  quantity: number
  inStock: number
}

export interface Cart {
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
const GUEST_CART_KEY = 'ecshop_guest_cart'
const cart: Ref<Cart | null> = ref(null)
const loading = ref(false)
const error = ref<string | null>(null)

const getGuestCartFromStorage = (): Cart => {
  if (typeof window === 'undefined') {
    return { id: 0, userId: 0, cartItems: [], totalItems: 0, totalPrice: 0 }
  }
  try {
    const raw = localStorage.getItem(GUEST_CART_KEY)
    if (!raw) {
      return { id: 0, userId: 0, cartItems: [], totalItems: 0, totalPrice: 0 }
    }
    const parsed = JSON.parse(raw)
    const cartItems: CartItem[] = Array.isArray(parsed.cartItems) ? parsed.cartItems : []
    const totalItems = cartItems.reduce((sum, item) => sum + (item.quantity || 0), 0)
    const totalPrice = cartItems.reduce((sum, item) => sum + ((item.price || 0) * (item.quantity || 0)), 0)
    return {
      id: 0,
      userId: 0,
      cartItems,
      totalItems,
      totalPrice
    }
  } catch (e) {
    console.error('Error reading guest cart from storage:', e)
    return { id: 0, userId: 0, cartItems: [], totalItems: 0, totalPrice: 0 }
  }
}

const saveGuestCartToStorage = (cartData: Cart) => {
  if (typeof window === 'undefined') return
  try {
    localStorage.setItem(GUEST_CART_KEY, JSON.stringify(cartData))
  } catch (e) {
    console.error('Error saving guest cart to storage:', e)
  }
}

export const useCart = () => {
  const { success, error: showError } = useNotification()
  const { token: authToken, isAuthenticated, logout } = useAuth()
  const router = useRouter()

  // Fetch cart from backend or localStorage for guest
  const fetchCart = async () => {
    if (!isAuthenticated.value) {
      cart.value = getGuestCartFromStorage()
      return
    }

    loading.value = true
    error.value = null

    try {
      const token = authToken.value
      if (!token) {
        cart.value = getGuestCartFromStorage()
        return
      }

      const response = await fetch(`${API_BASE_URL}/api/cart`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })

      if (response.status === 401) {
        console.warn('401 Unauthorized - token invalid or expired')
        cart.value = null
        logout()
        return
      }

      if (!response.ok) {
        cart.value = null
        throw new Error('Không thể tải giỏ hàng')
      }

      cart.value = await response.json()
    } catch (err: any) {
      error.value = err.message
      console.error('Cart fetch error:', err)
    } finally {
      loading.value = false
    }
  }

  // Add item to cart (supports both guests and authenticated users)
  const addToCart = async (
    productVariantId: number,
    quantity: number = 1,
    price: number,
    extra?: {
      productName?: string
      variantName?: string
      imageUrl?: string
      inStock?: number
    }
  ) => {
    loading.value = true
    error.value = null

    if (!isAuthenticated.value) {
      try {
        const guestCart = getGuestCartFromStorage()
        const existingItemIndex = guestCart.cartItems.findIndex(
          item => item.productVariantId === productVariantId
        )

        if (existingItemIndex > -1) {
          const item = guestCart.cartItems[existingItemIndex]
          const maxStock = extra?.inStock || item.inStock || 99
          const newQty = item.quantity + quantity
          if (newQty > maxStock) {
            showError(`Số lượng vượt quá tồn kho (tối đa ${maxStock})`)
            return false
          }
          item.quantity = newQty
          if (price) item.price = price
          if (extra?.imageUrl) item.imageUrl = extra.imageUrl
          if (extra?.productName) item.productName = extra.productName
          if (extra?.variantName) item.variantName = extra.variantName
        } else {
          const newItem: CartItem = {
            id: Date.now(),
            productVariantId,
            productName: extra?.productName || 'Sản phẩm',
            variantName: extra?.variantName || '',
            imageUrl: extra?.imageUrl || '/images/placeholder.png',
            price: Number(price) || 0,
            quantity: Math.max(1, quantity),
            inStock: extra?.inStock || 99
          }
          guestCart.cartItems.push(newItem)
        }

        guestCart.totalItems = guestCart.cartItems.reduce((sum, it) => sum + it.quantity, 0)
        guestCart.totalPrice = guestCart.cartItems.reduce((sum, it) => sum + (it.price * it.quantity), 0)
        saveGuestCartToStorage(guestCart)
        cart.value = guestCart
        success('Đã thêm sản phẩm vào giỏ hàng')
        return true
      } catch (err: any) {
        error.value = err.message
        showError('Không thể thêm vào giỏ hàng')
        return false
      } finally {
        loading.value = false
      }
    }

    // Authenticated user
    try {
      const token = authToken.value
      if (!token) {
        showError('Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng')
        return false
      }

      const requestBody: AddToCartRequest = {
        productVariantId,
        quantity,
        price
      }

      const response = await fetch(`${API_BASE_URL}/api/cart/items`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestBody)
      })

      if (response.status === 401) {
        console.warn('401 Unauthorized - redirecting to login')
        showError('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
        router.push('/auth/auth')
        return false
      }

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Không thể thêm vào giỏ hàng' }))
        throw new Error(errorData.message || 'Không thể thêm vào giỏ hàng')
      }

      cart.value = await response.json()
      success('Đã thêm sản phẩm vào giỏ hàng')
      return true
    } catch (err: any) {
      error.value = err.message
      showError(err.message)
      return false
    } finally {
      loading.value = false
    }
  }

  // Update cart item quantity
  const updateCartItem = async (cartItemId: number, quantity: number) => {
    loading.value = true
    error.value = null

    if (!isAuthenticated.value) {
      try {
        const guestCart = getGuestCartFromStorage()
        const item = guestCart.cartItems.find(i => i.id === cartItemId)
        if (item) {
          item.quantity = quantity
          guestCart.totalItems = guestCart.cartItems.reduce((sum, it) => sum + it.quantity, 0)
          guestCart.totalPrice = guestCart.cartItems.reduce((sum, it) => sum + (it.price * it.quantity), 0)
          saveGuestCartToStorage(guestCart)
          cart.value = guestCart
          success('Đã cập nhật giỏ hàng')
          return true
        }
        return false
      } catch (err: any) {
        error.value = err.message
        showError(err.message)
        return false
      } finally {
        loading.value = false
      }
    }

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
    loading.value = true
    error.value = null

    if (!isAuthenticated.value) {
      try {
        const guestCart = getGuestCartFromStorage()
        guestCart.cartItems = guestCart.cartItems.filter(i => i.id !== cartItemId)
        guestCart.totalItems = guestCart.cartItems.reduce((sum, it) => sum + it.quantity, 0)
        guestCart.totalPrice = guestCart.cartItems.reduce((sum, it) => sum + (it.price * it.quantity), 0)
        saveGuestCartToStorage(guestCart)
        cart.value = guestCart
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
    loading.value = true
    error.value = null

    if (!isAuthenticated.value) {
      try {
        if (typeof window !== 'undefined') {
          localStorage.removeItem(GUEST_CART_KEY)
        }
        cart.value = { id: 0, userId: 0, cartItems: [], totalItems: 0, totalPrice: 0 }
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
