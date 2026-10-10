export default defineNuxtRouteMiddleware((to, from) => {
  const { user } = useAuth()
  
  // Check if user is authenticated and is admin
  if (!user.value || user.value.role !== 'ROLE_ADMIN') {
    // Redirect to home page if not admin
    return navigateTo('/')
  }
})
