import { createHash } from 'node:crypto'
import { readdirSync, readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

const backend = 'http://localhost:8000'

/**
 * Genera dist/sw.js a partir de sw.js con la lista de ficheros de la compilación, para que la app
 * se pueda abrir sin conexión. De las fuentes solo se incluye el alfabeto latino (el resto se
 * descarga si alguna vez hace falta).
 */
function serviceWorker() {
  const raiz = fileURLToPath(new URL('.', import.meta.url))
  const publicos = (carpeta) =>
    readdirSync(`${raiz}public/${carpeta}`, { withFileTypes: true }).flatMap((f) =>
      f.isDirectory() ? publicos(`${carpeta}${f.name}/`) : [`/${carpeta}${f.name}`]
    )
  return {
    name: 'service-worker',
    apply: 'build',
    generateBundle(_, bundle) {
      const archivos = [
        '/',
        ...publicos(''),
        ...Object.keys(bundle)
          .filter((f) => !f.endsWith('.map'))
          .filter((f) => !f.endsWith('.woff2') || /-latin-wght/.test(f))
          .map((f) => `/${f}`),
      ]
      const version = createHash('sha256').update(archivos.join()).digest('hex').slice(0, 12)
      const plantilla = readFileSync(`${raiz}sw.js`, 'utf8')
      this.emitFile({
        type: 'asset',
        fileName: 'sw.js',
        source: plantilla.replace('__VERSION__', version).replace('__ARCHIVOS__', JSON.stringify(archivos)),
      })
    },
  }
}

/**
 * App nativa (Capacitor): el index.html va dentro del paquete y no lo sirve el backend, así que la
 * Content Security Policy se pone aquí, con el servidor de la API como único destino permitido.
 */
function politicaMovil(servidor) {
  const origen = new URL(servidor).origin
  const socket = origen.replace(/^http/, 'ws')
  const csp = [
    "default-src 'self'",
    "script-src 'self'",
    "style-src 'self' 'unsafe-inline'",
    `img-src 'self' data: blob: ${origen} https://*.tile.openstreetmap.org`,
    `connect-src 'self' ${origen} ${socket}`,
    "font-src 'self'",
    "object-src 'none'",
    "base-uri 'self'",
  ].join('; ')
  return {
    name: 'csp-movil',
    transformIndexHtml: (html) =>
      html.replace(
        '<meta charset="utf-8" />',
        `<meta charset="utf-8" />
    <meta http-equiv="Content-Security-Policy" content="${csp}" />`
      ),
  }
}

export default defineConfig(({ mode }) => {
  const movil = mode === 'movil' // npm run build:movil → dist-movil, que Capacitor mete en la app
  const servidor = loadEnv(mode, process.cwd(), '').VITE_SERVIDOR
  if (movil && !servidor)
    throw new Error('Falta VITE_SERVIDOR: la dirección del servidor de la API (consulta .env.movil)')
  return {
    plugins: [vue(), movil ? politicaMovil(servidor) : serviceWorker()],
    build: movil ? { outDir: 'dist-movil' } : {},
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
  }
})
