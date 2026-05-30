import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import viteCompression from 'vite-plugin-compression'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia'],
      dts: resolve(__dirname, 'src/auto-imports.d.ts'),
      vueTemplate: true,
    }),
    Components({
      resolvers: [ElementPlusResolver({ importStyle: 'css' })],
      dts: resolve(__dirname, 'src/components.d.ts'),
    }),
    viteCompression({
      algorithm: 'gzip',
      threshold: 1024,
      deleteOriginFile: false,
    }),
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    }
  },
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'axios',
      'element-plus/es',
    ],
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    chunkSizeWarningLimit: 1000,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules')) {
            if (id.includes('vditor')) {
              return 'vditor'
            }
            if (id.includes('element-plus')) {
              return 'element-plus'
            }
            if (id.includes('@element-plus/icons-vue')) {
              return 'element-icons'
            }
            if (id.includes('highlight.js')) {
              return 'highlightjs'
            }
            if (id.includes('marked')) {
              return 'markdown'
            }
            if (id.includes('dompurify')) {
              return 'dompurify'
            }
            if (id.includes('lucide-vue-next')) {
              return 'lucide'
            }
            if (id.includes('vue-i18n')) {
              return 'vue-i18n'
            }
            if (id.includes('axios')) {
              return 'axios'
            }
            if (id.includes('vue') || id.includes('pinia') || id.includes('@vue/shared') || id.includes('@vue/reactivity') || id.includes('@vue/runtime')) {
              return 'vue-vendor'
            }
          }
        }
      }
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        api: 'modern-compiler',
        silenceDeprecations: ['legacy-js-api']
      }
    }
  }
})
