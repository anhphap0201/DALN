export default defineNuxtRouteMiddleware((to, from) => {
  // Skip middleware on server side - only run on client
  if (process.server) {
    return
  }
  
  // On client side, check localStorage first
  if (process.client) {
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')
    
    // If we have valid auth data in localStorage, allow access
    if (storedToken && storedUser) {
      try {
        // Parse to validate
        JSON.parse(storedUser)
        
        // Initialize auth state if not already done
        const { initializeAuth } = useAuth()
        initializeAuth()
        
        return // Allow access
      } catch (error) {
        // Invalid JSON, clear and redirect
        localStorage.removeItem('token')
        localStorage.removeItem('user')
      }
    }
    
    // No valid auth, redirect to login
    // Don't redirect if already going to auth page (prevent loop)
    if (to.path !== '/auth/auth') {
      return navigateTo('/auth/auth', { replace: true })
    }
  }
})
