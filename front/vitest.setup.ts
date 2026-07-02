import { vi } from 'vitest'

// Mock de vue-router : les tests montent des composants isolés, sans routeur réel.
vi.mock('vue-router', () => ({
  useRouter: vi.fn(() => ({
    push: vi.fn(),
  })),
  useRoute: vi.fn(() => ({
    path: '/barmaker/orders',
  })),
  createRouter: vi.fn(),
  createWebHistory: vi.fn(),
}))

// localStorage / sessionStorage en mémoire : selon l'ordre d'exécution des fichiers, jsdom
// ne les expose pas de façon fiable, ce qui provoquait une erreur non gérée (removeItem sur
// un storage undefined) lors d'une déconnexion. On garantit un stockage stable et isolé.
function createStorageMock(): Storage {
  let store: Record<string, string> = {}
  return {
    getItem: (key: string) => (key in store ? store[key]! : null),
    setItem: (key: string, value: string) => {
      store[key] = String(value)
    },
    removeItem: (key: string) => {
      delete store[key]
    },
    clear: () => {
      store = {}
    },
    key: (index: number) => Object.keys(store)[index] ?? null,
    get length() {
      return Object.keys(store).length
    },
  } as Storage
}

vi.stubGlobal('localStorage', createStorageMock())
vi.stubGlobal('sessionStorage', createStorageMock())
