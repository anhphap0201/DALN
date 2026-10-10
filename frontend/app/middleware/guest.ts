export default defineNuxtRouteMiddleware((to, from) => {
  // Skip middleware on server side - only run on client
  if (process.server) {
    return
  }
  
  // Check localStorage directly on client side
  if (process.client) {
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')
    
    // If we have token and user in localStorage, user is already logged in
    // Redirect to home, but don't redirect if already going home (prevent loop)
    if (storedToken && storedUser && to.path !== '/') {
      return navigateTo('/', { replace: true })
    }
  }
})
