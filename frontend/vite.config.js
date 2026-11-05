import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const backend = 'http://localhost:8000'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
  },
  server: {
    // En desarrollo el navegador habla solo con Vite y este reenvía la API al backend:
    // así no hay que escribir "http://localhost:8080" en cada vista ni pelearse con CORS.
    proxy: {
      '/api': { target: backend, ws: true },
      '/uploads': backend,
    },
  },
})
