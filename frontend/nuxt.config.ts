export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',

  css: ['~/assets/css/main.css'],

  experimental: {
    oxcParser: false,
    appManifest: false
  },

  postcss: {
    plugins: {
      '@tailwindcss/postcss': {},
    },
  },
})


