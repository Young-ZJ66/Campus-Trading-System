import { createLogger, defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const baseLogger = createLogger()
const customLogger = {
  ...baseLogger,
  info(msg, options) {
    if (typeof msg === 'string') {
      msg = msg.replace(/\n\s*➜\s*Local:\s*[^\n]+\n?/g, '\n')
    }
    baseLogger.info(msg, options)
  }
}

export default defineConfig({
  customLogger,
  plugins: [
    vue(),
    {
      name: 'show-entry-urls',
      configureServer(server) {
        const print = () => {
          const local = server.resolvedUrls?.local?.[0]
          const network = server.resolvedUrls?.network?.[0]
          const base = local || network
          if (!base) return
          const userUrl = base
          const adminUrl = new URL('/admin/login', base).toString()
          console.log(`  ➜  User:   ${userUrl}`)
          console.log(`  ➜  Admin:  ${adminUrl}`)
        }
        server.httpServer?.once('listening', () => {
          setTimeout(print, 0)
        })
      }
    }
  ],
})
