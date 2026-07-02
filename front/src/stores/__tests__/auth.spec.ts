import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'

// Mock du module API
vi.mock('@/api/client', () => ({
  login: vi.fn(),
}))

describe('Auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  describe('Initialisation et localStorage', () => {
    it('doit initialiser avec des valeurs vides par défaut', () => {
      const authStore = useAuthStore()
      expect(authStore.token).toBe('')
      expect(authStore.name).toBe('')
      expect(authStore.role).toBe('')
    })

    it('doit charger depuis localStorage au démarrage', () => {
      const mockData = {
        token: 'stored-token',
        name: 'John Doe',
        role: 'barmaker',
      }
      localStorage.setItem('barmaker_auth', JSON.stringify(mockData))

      const authStore = useAuthStore()
      expect(authStore.token).toBe('stored-token')
      expect(authStore.name).toBe('John Doe')
      expect(authStore.role).toBe('barmaker')
    })

    it('doit ignorer les erreurs de parsing JSON du localStorage', () => {
      localStorage.setItem('barmaker_auth', 'invalid json{')
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      const authStore = useAuthStore()
      expect(authStore.token).toBe('')
      expect(authStore.name).toBe('')

      consoleErrorSpy.mockRestore()
    })

    it('doit gérer les données partielles du localStorage', () => {
      localStorage.setItem('barmaker_auth', JSON.stringify({ token: 'partial-token' }))

      const authStore = useAuthStore()
      expect(authStore.token).toBe('partial-token')
      expect(authStore.name).toBe('')
      expect(authStore.role).toBe('')
    })
  })

  describe('Computed isAuthenticated', () => {
    it('doit retourner false quand token est vide', () => {
      const authStore = useAuthStore()
      expect(authStore.isAuthenticated).toBe(false)
    })

    it('doit retourner true quand token existe', () => {
      const authStore = useAuthStore()
      authStore.token = 'some-token'
      expect(authStore.isAuthenticated).toBe(true)
    })
  })

  describe('login action', () => {
    it('doit appeler l\'API login avec les credentials', async () => {
      const apiClient = await import('@/api/client')
      vi.mocked(apiClient.login).mockResolvedValueOnce({
        token: 'new-token',
        name: 'Alice',
        role: 'barmaker',
      })

      const authStore = useAuthStore()
      const result = await authStore.login('alice@example.com', 'password123')

      expect(vi.mocked(apiClient.login)).toHaveBeenCalledWith('alice@example.com', 'password123')
      expect(result).toEqual({
        token: 'new-token',
        name: 'Alice',
        role: 'barmaker',
      })
    })

    it('doit mettre à jour l\'état après une login réussie', async () => {
      const apiClient = await import('@/api/client')
      vi.mocked(apiClient.login).mockResolvedValueOnce({
        token: 'new-token',
        name: 'Alice',
        role: 'barmaker',
      })

      const authStore = useAuthStore()
      await authStore.login('alice@example.com', 'password123')

      expect(authStore.token).toBe('new-token')
      expect(authStore.name).toBe('Alice')
      expect(authStore.role).toBe('barmaker')
    })

    it('doit sauvegarder en localStorage après une login réussie', async () => {
      const apiClient = await import('@/api/client')
      vi.mocked(apiClient.login).mockResolvedValueOnce({
        token: 'new-token',
        name: 'Alice',
        role: 'barmaker',
      })

      const authStore = useAuthStore()
      await authStore.login('alice@example.com', 'password123')

      const stored = JSON.parse(localStorage.getItem('barmaker_auth') || '{}')
      expect(stored.token).toBe('new-token')
      expect(stored.name).toBe('Alice')
      expect(stored.role).toBe('barmaker')
    })

    it('doit lancer une erreur si l\'API échoue', async () => {
      const apiClient = await import('@/api/client')
      vi.mocked(apiClient.login).mockRejectedValueOnce(new Error('API Error'))

      const authStore = useAuthStore()
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      await expect(authStore.login('alice@example.com', 'password123')).rejects.toThrow(
        'API Error'
      )

      consoleErrorSpy.mockRestore()
    })

    it('ne doit pas modifier l\'état si l\'API échoue', async () => {
      const apiClient = await import('@/api/client')
      vi.mocked(apiClient.login).mockRejectedValueOnce(new Error('API Error'))

      const authStore = useAuthStore()
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      try {
        await authStore.login('alice@example.com', 'password123')
      } catch {
        // Ignorer l'erreur
      }

      expect(authStore.token).toBe('')
      expect(authStore.name).toBe('')

      consoleErrorSpy.mockRestore()
    })
  })

  describe('logout action', () => {
    it('doit vider token, name et role', () => {
      const authStore = useAuthStore()
      authStore.token = 'test-token'
      authStore.name = 'Test User'
      authStore.role = 'barmaker'

      authStore.logout()

      expect(authStore.token).toBe('')
      expect(authStore.name).toBe('')
      expect(authStore.role).toBe('')
    })

    it('doit supprimer les données du localStorage', () => {
      const authStore = useAuthStore()
      authStore.token = 'test-token'
      authStore.name = 'Test User'

      authStore.logout()

      expect(localStorage.getItem('barmaker_auth')).toBeNull()
    })

    it('doit définir isAuthenticated à false après logout', () => {
      const authStore = useAuthStore()
      authStore.token = 'test-token'

      authStore.logout()

      expect(authStore.isAuthenticated).toBe(false)
    })
  })
})
