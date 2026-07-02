import { vi } from 'vitest'

// Mock vue-router pour les tests
vi.mock('vue-router', () => ({
  useRouter: vi.fn(() => ({
    push: vi.fn(),
  })),
  useRoute: vi.fn(() => ({
    path: '/barmaker/commandes',
  })),
  createRouter: vi.fn(),
  createWebHistory: vi.fn(),
}))
