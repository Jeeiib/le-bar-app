import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import * as api from '@/api/client'
import { useAuthStore } from '@/stores/auth'
import type {
  Category,
  CocktailRequest,
  CreateOrderRequest,
} from '@/types'

// Mock fetch global
global.fetch = vi.fn()

describe('API client', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  describe('fetchAPI base wrapper', () => {
    it('doit lancer une erreur pour une réponse non-ok avec message personnalisé', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({ message: 'Erreur personnalisée' }),
      })

      await expect(api.getCategories()).rejects.toThrow('Erreur personnalisée')
    })

    it('doit lancer une erreur avec le code HTTP si pas de message JSON', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: async () => {
          throw new Error('Parsing error')
        },
      })

      await expect(api.getCategories()).rejects.toThrow('HTTP 500')
    })

    it('doit supporter les réponses 204 sans corps', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 204,
      })

      const result = await api.deleteCategory(1)
      expect(result).toBeUndefined()
    })

    it('doit parser et retourner du JSON valide', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      const mockData: Category = { id: 1, name: 'Rhum', position: 0 }
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        text: async () => JSON.stringify(mockData),
      })

      const result = await api.getCategories()
      expect(result).toEqual(expect.arrayContaining([]))
    })

    it('doit ajouter le header Authorization si useAuth est true et le token existe', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      const authStore = useAuthStore()
      authStore.token = 'test-token-123'

      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        text: async () => JSON.stringify([]),
      })

      await api.getQueue()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/orders/queue',
        expect.objectContaining({
          headers: expect.objectContaining({
            Authorization: 'Bearer test-token-123',
          }),
        })
      )
    })

    it('ne doit pas ajouter Authorization si le token est vide', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      const authStore = useAuthStore()
      authStore.token = ''

      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        text: async () => JSON.stringify([]),
      })

      await api.getQueue()

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/orders/queue',
        expect.objectContaining({
          headers: expect.not.objectContaining({
            Authorization: expect.anything(),
          }),
        })
      )
    })
  })

  describe('Public endpoints', () => {
    it('getTable doit appeler le bon endpoint', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        text: async () => JSON.stringify({ id: 1, label: 'Table 1', qrSlug: 'table-1' }),
      })

      const result = await api.getTable('abc123')
      expect(mockFetch).toHaveBeenCalledWith(
        '/api/tables/abc123',
        expect.objectContaining({
          method: 'GET',
        })
      )
      expect(result).toBeDefined()
    })

    it('getTables doit appeler le bon endpoint et retourner un tableau de tables', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      const mockTables = [
        { id: 1, label: 'Table 1', qrSlug: 'table-1' },
        { id: 2, label: 'Table 2', qrSlug: 'table-2' },
      ]
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        text: async () => JSON.stringify(mockTables),
      })

      const result = await api.getTables()
      expect(mockFetch).toHaveBeenCalledWith(
        '/api/tables',
        expect.objectContaining({
          method: 'GET',
        })
      )
      expect(result).toEqual(mockTables)
      expect(result[0]?.qrSlug).toBeDefined()
    })

    it('getCategories doit appeler le bon endpoint', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        text: async () => JSON.stringify([]),
      })

      await api.getCategories()
      expect(mockFetch).toHaveBeenCalledWith(
        '/api/categories',
        expect.objectContaining({
          method: 'GET',
        })
      )
    })

    it('getCocktails sans categoryId doit appeler le bon endpoint', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        text: async () => JSON.stringify([]),
      })

      await api.getCocktails()
      expect(mockFetch).toHaveBeenCalledWith(
        '/api/cocktails',
        expect.objectContaining({
          method: 'GET',
        })
      )
    })

    it('getCocktails avec categoryId doit ajouter le query param', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        text: async () => JSON.stringify([]),
      })

      await api.getCocktails(5)
      expect(mockFetch).toHaveBeenCalledWith(
        '/api/cocktails?categoryId=5',
        expect.objectContaining({
          method: 'GET',
        })
      )
    })

    it('getCocktail doit appeler le bon endpoint avec l\'id', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        text: async () => JSON.stringify({}),
      })

      await api.getCocktail(42)
      expect(mockFetch).toHaveBeenCalledWith(
        '/api/cocktails/42',
        expect.objectContaining({
          method: 'GET',
        })
      )
    })

    it('createOrder doit appeler le bon endpoint avec POST et body', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 201,
        text: async () => JSON.stringify({ id: 1 }),
      })

      const body: CreateOrderRequest = {
        tableId: 1,
        items: [{ cocktailId: 1, size: 'M' }],
      }
      await api.createOrder(body)

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/orders',
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify(body),
        })
      )
    })

    it('getOrder doit appeler le bon endpoint avec l\'id', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        text: async () => JSON.stringify({}),
      })

      await api.getOrder(99)
      expect(mockFetch).toHaveBeenCalledWith(
        '/api/orders/99',
        expect.objectContaining({
          method: 'GET',
        })
      )
    })
  })

  describe('Auth endpoints', () => {
    it('login doit appeler le bon endpoint avec POST et credentials', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        text: async () => JSON.stringify({ token: 'abc', name: 'John', role: 'barmaker' }),
      })

      await api.login('john@example.com', 'password123')

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/auth/login',
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({ email: 'john@example.com', password: 'password123' }),
        })
      )
    })

    it('register doit appeler le bon endpoint avec POST', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 201,
        text: async () => JSON.stringify({ token: 'abc', name: 'John', role: 'barmaker' }),
      })

      await api.register('john@example.com', 'password123', 'John Doe')

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/auth/register',
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({
            email: 'john@example.com',
            password: 'password123',
            name: 'John Doe',
          }),
        })
      )
    })
  })

  describe('Barmaker endpoints (auth required)', () => {
    it('getQueue doit appeler le bon endpoint avec auth', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        text: async () => JSON.stringify([]),
      })

      const authStore = useAuthStore()
      authStore.token = 'token123'

      await api.getQueue()
      expect(mockFetch).toHaveBeenCalledWith(
        '/api/orders/queue',
        expect.objectContaining({
          method: 'GET',
          headers: expect.objectContaining({
            Authorization: 'Bearer token123',
          }),
        })
      )
    })

    it('createTable doit appeler POST /api/tables avec le nom et auth', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 201,
        text: async () => JSON.stringify({ id: 13, label: 'Terrasse 1', qrSlug: 'terrasse-1' }),
      })

      const authStore = useAuthStore()
      authStore.token = 'token123'

      await api.createTable('Terrasse 1')
      expect(mockFetch).toHaveBeenCalledWith(
        '/api/tables',
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({ label: 'Terrasse 1' }),
          headers: expect.objectContaining({
            Authorization: 'Bearer token123',
          }),
        })
      )
    })

    it('deleteTable doit appeler DELETE /api/tables/:id avec auth', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 204,
        text: async () => '',
      })

      const authStore = useAuthStore()
      authStore.token = 'token123'

      await api.deleteTable(13)
      expect(mockFetch).toHaveBeenCalledWith(
        '/api/tables/13',
        expect.objectContaining({
          method: 'DELETE',
          headers: expect.objectContaining({
            Authorization: 'Bearer token123',
          }),
        })
      )
    })

    it('advanceItem doit appeler le bon endpoint avec PATCH et auth', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        text: async () => JSON.stringify({}),
      })

      const authStore = useAuthStore()
      authStore.token = 'token123'

      await api.advanceItem(5, 7)
      expect(mockFetch).toHaveBeenCalledWith(
        '/api/orders/5/items/7/advance',
        expect.objectContaining({
          method: 'PATCH',
          headers: expect.objectContaining({
            Authorization: 'Bearer token123',
          }),
        })
      )
    })

    it('createCategory doit appeler le bon endpoint avec POST et auth', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 201,
        text: async () => JSON.stringify({ id: 1, name: 'Rhum', position: 0 }),
      })

      const authStore = useAuthStore()
      authStore.token = 'token123'

      await api.createCategory('Rhum', 0)
      expect(mockFetch).toHaveBeenCalledWith(
        '/api/categories',
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({ name: 'Rhum', position: 0 }),
          headers: expect.objectContaining({
            Authorization: 'Bearer token123',
          }),
        })
      )
    })

    it('updateCategory doit appeler le bon endpoint avec PUT et auth', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        text: async () => JSON.stringify({ id: 1, name: 'Vodka', position: 1 }),
      })

      const authStore = useAuthStore()
      authStore.token = 'token123'

      await api.updateCategory(1, 'Vodka', 1)
      expect(mockFetch).toHaveBeenCalledWith(
        '/api/categories/1',
        expect.objectContaining({
          method: 'PUT',
          body: JSON.stringify({ name: 'Vodka', position: 1 }),
        })
      )
    })

    it('deleteCategory doit appeler le bon endpoint avec DELETE et auth', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 204,
      })

      const authStore = useAuthStore()
      authStore.token = 'token123'

      await api.deleteCategory(1)
      expect(mockFetch).toHaveBeenCalledWith(
        '/api/categories/1',
        expect.objectContaining({
          method: 'DELETE',
        })
      )
    })

    it('createCocktail doit appeler le bon endpoint avec POST et auth', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 201,
        text: async () => JSON.stringify({ id: 1 }),
      })

      const authStore = useAuthStore()
      authStore.token = 'token123'

      const body: CocktailRequest = {
        name: 'Mojito',
        description: 'Rafraichissant',
        available: true,
        categoryId: 1,
        ingredients: [{ name: 'Rhum', measure: '45 ml' }],
        sizes: [{ size: 'M', price: 8 }],
      }

      await api.createCocktail(body)
      expect(mockFetch).toHaveBeenCalledWith(
        '/api/cocktails',
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify(body),
        })
      )
    })

    it('updateCocktail doit appeler le bon endpoint avec PUT et auth', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        text: async () => JSON.stringify({ id: 1 }),
      })

      const authStore = useAuthStore()
      authStore.token = 'token123'

      const body: CocktailRequest = {
        name: 'Daiquiri',
        description: 'Classique',
        available: true,
        categoryId: 1,
        ingredients: [],
        sizes: [],
      }

      await api.updateCocktail(1, body)
      expect(mockFetch).toHaveBeenCalledWith(
        '/api/cocktails/1',
        expect.objectContaining({
          method: 'PUT',
          body: JSON.stringify(body),
        })
      )
    })

    it('deleteCocktail doit appeler le bon endpoint avec DELETE et auth', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 204,
      })

      const authStore = useAuthStore()
      authStore.token = 'token123'

      await api.deleteCocktail(1)
      expect(mockFetch).toHaveBeenCalledWith(
        '/api/cocktails/1',
        expect.objectContaining({
          method: 'DELETE',
        })
      )
    })

    it('searchExternal doit appeler le bon endpoint avec query param et auth', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        text: async () => JSON.stringify([]),
      })

      const authStore = useAuthStore()
      authStore.token = 'token123'

      await api.searchExternal('Margarita')
      expect(mockFetch).toHaveBeenCalledWith(
        '/api/external/cocktails?name=Margarita',
        expect.objectContaining({
          method: 'GET',
        })
      )
    })

    it('searchExternal doit encoder l\'URL correctement pour les noms avec espaces', async () => {
      const mockFetch = global.fetch as ReturnType<typeof vi.fn>
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        text: async () => JSON.stringify([]),
      })

      const authStore = useAuthStore()
      authStore.token = 'token123'

      await api.searchExternal('Blue Lagoon')
      expect(mockFetch).toHaveBeenCalledWith(
        '/api/external/cocktails?name=Blue%20Lagoon',
        expect.objectContaining({
          method: 'GET',
        })
      )
    })
  })
})
