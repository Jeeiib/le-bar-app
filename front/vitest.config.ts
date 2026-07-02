import { fileURLToPath } from 'node:url'
import { mergeConfig, defineConfig, configDefaults } from 'vitest/config'
import viteConfig from './vite.config'

export default mergeConfig(
  viteConfig,
  defineConfig({
    test: {
      environment: 'jsdom',
      exclude: [...configDefaults.exclude, 'e2e/**'],
      root: fileURLToPath(new URL('./', import.meta.url)),
      setupFiles: [fileURLToPath(new URL('./vitest.setup.ts', import.meta.url))],
      coverage: {
        provider: 'v8',
        reporter: ['text', 'html'],
        include: ['src/stores/**', 'src/utils/**', 'src/api/**', 'src/components/**'],
        thresholds: {
          lines: 90,
          functions: 90,
          statements: 90,
          branches: 80,
        },
      },
    },
  }),
)
