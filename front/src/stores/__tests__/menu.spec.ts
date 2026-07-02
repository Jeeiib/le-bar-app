import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useMenuStore } from '@/stores/menu'
import type { Category, Cocktail } from '@/types'

// Mock du module API
vi.mock('@/api/client', () => ({
  getCategories: vi.fn(),
  getCocktails: vi.fn(),
}))

describe('Menu store', () => {
  let mockCategories: Category[]
  let mockCocktails: Cocktail[]

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()

    mockCategories = [
      { id: 1, name: 'Rhum', position: 0 },
      { id: 2, name: 'Vodka', position: 1 },
    ]

    mockCocktails = [
      {
        id: 1,
        name: 'Mojito',
        description: 'Rafraichissant',
        available: true,
        categoryId: 1,
        categoryName: 'Rhum',
        ingredients: [],
        sizes: [{ size: 'M', price: 8 }],
        imageUrl: 'https://example.com/mojito.jpg',
      },
      {
        id: 2,
        name: 'Cosmopolitan',
        description: 'Elegante',
        available: true,
        categoryId: 2,
        categoryName: 'Vodka',
        ingredients: [],
        sizes: [{ size: 'M', price: 9 }],
        imageUrl: 'https://example.com/cosmo.jpg',
      },
    ]
  })

  describe('Initialisation', () => {
    it('doit initialiser avec des valeurs par défaut', () => {
      const menuStore = useMenuStore()
      expect(menuStore.categories).toEqual([])
      expect(menuStore.cocktails).toEqual([])
      expect(menuStore.loaded).toBe(false)
      expect(menuStore.loading).toBe(false)
    })
  })

  describe('load action', () => {
    it('doit charger les catégories et cocktails', async () => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const apiClient = (await import('@/api/client')) as any
      apiClient.getCategories.mockResolvedValueOnce(mockCategories)
      apiClient.getCocktails.mockResolvedValueOnce(mockCocktails)

      const menuStore = useMenuStore()
      await menuStore.load()

      expect(menuStore.categories).toEqual(mockCategories)
      expect(menuStore.cocktails).toEqual(mockCocktails)
      expect(menuStore.loaded).toBe(true)
      expect(menuStore.loading).toBe(false)
    })

    it('doit appeler l\'API avec Promise.all', async () => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const apiClient = (await import('@/api/client')) as any
      apiClient.getCategories.mockResolvedValueOnce(mockCategories)
      apiClient.getCocktails.mockResolvedValueOnce(mockCocktails)

      const menuStore = useMenuStore()
      await menuStore.load()

      expect(apiClient.getCategories).toHaveBeenCalled()
      expect(apiClient.getCocktails).toHaveBeenCalled()
    })

    it('ne doit pas recharger si déjà chargé', async () => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const apiClient = (await import('@/api/client')) as any
      apiClient.getCategories.mockResolvedValueOnce(mockCategories)
      apiClient.getCocktails.mockResolvedValueOnce(mockCocktails)

      const menuStore = useMenuStore()
      await menuStore.load()
      vi.clearAllMocks()

      await menuStore.load()

      expect(apiClient.getCategories).not.toHaveBeenCalled()
      expect(apiClient.getCocktails).not.toHaveBeenCalled()
    })

    it('ne doit pas recharger si actuellement en cours de chargement', async () => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const apiClient = (await import('@/api/client')) as any
      let resolveLoad: () => void
      const loadPromise = new Promise<void>((resolve) => {
        resolveLoad = resolve
      })
      apiClient.getCategories.mockReturnValueOnce(loadPromise)
      apiClient.getCocktails.mockReturnValueOnce(loadPromise)

      const menuStore = useMenuStore()
      const loadPromise1 = menuStore.load()
      const loadPromise2 = menuStore.load()

      expect(apiClient.getCategories).toHaveBeenCalledTimes(1)
      expect(apiClient.getCocktails).toHaveBeenCalledTimes(1)

      resolveLoad!()
      await Promise.all([loadPromise1, loadPromise2])
    })

    it('doit logger les erreurs sans lancer d\'exception', async () => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const apiClient = (await import('@/api/client')) as any
      apiClient.getCategories.mockRejectedValueOnce(new Error('API Error'))
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      const menuStore = useMenuStore()
      await expect(menuStore.load()).resolves.not.toThrow()

      expect(menuStore.loading).toBe(false)
      consoleErrorSpy.mockRestore()
    })
  })

  describe('reload action', () => {
    it('doit forcer le rechargement même si déjà chargé', async () => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const apiClient = (await import('@/api/client')) as any
      apiClient.getCategories.mockResolvedValueOnce(mockCategories)
      apiClient.getCocktails.mockResolvedValueOnce(mockCocktails)

      const menuStore = useMenuStore()
      await menuStore.load()

      apiClient.getCategories.mockClear()
      apiClient.getCocktails.mockClear()
      apiClient.getCategories.mockResolvedValueOnce(mockCategories)
      apiClient.getCocktails.mockResolvedValueOnce(mockCocktails)

      await menuStore.reload()

      expect(apiClient.getCategories).toHaveBeenCalled()
      expect(apiClient.getCocktails).toHaveBeenCalled()
    })

    it('doit mettre à jour les données lors du rechargement', async () => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const apiClient = (await import('@/api/client')) as any
      const updatedCategories = [...mockCategories, { id: 3, name: 'Gin', position: 2 }]
      apiClient.getCategories.mockResolvedValueOnce(mockCategories)
      apiClient.getCocktails.mockResolvedValueOnce(mockCocktails)

      const menuStore = useMenuStore()
      await menuStore.load()

      apiClient.getCategories.mockResolvedValueOnce(updatedCategories)
      apiClient.getCocktails.mockResolvedValueOnce(mockCocktails)
      await menuStore.reload()

      expect(menuStore.categories).toEqual(updatedCategories)
    })

    it('doit définir loaded à true après rechargement', async () => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const apiClient = (await import('@/api/client')) as any
      apiClient.getCategories.mockResolvedValueOnce(mockCategories)
      apiClient.getCocktails.mockResolvedValueOnce(mockCocktails)

      const menuStore = useMenuStore()
      await menuStore.reload()

      expect(menuStore.loaded).toBe(true)
    })

    it('doit logger les erreurs de rechargement sans lancer d\'exception', async () => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const apiClient = (await import('@/api/client')) as any
      apiClient.getCategories.mockRejectedValueOnce(new Error('API Error'))
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      const menuStore = useMenuStore()
      await expect(menuStore.reload()).resolves.not.toThrow()

      consoleErrorSpy.mockRestore()
    })
  })

  describe('invalidate action', () => {
    it('doit définir loaded à false', async () => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const apiClient = (await import('@/api/client')) as any
      apiClient.getCategories.mockResolvedValueOnce(mockCategories)
      apiClient.getCocktails.mockResolvedValueOnce(mockCocktails)

      const menuStore = useMenuStore()
      await menuStore.load()
      expect(menuStore.loaded).toBe(true)

      menuStore.invalidate()
      expect(menuStore.loaded).toBe(false)
    })

    it('doit permettre un rechargement après invalidation', async () => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const apiClient = (await import('@/api/client')) as any
      apiClient.getCategories.mockResolvedValueOnce(mockCategories)
      apiClient.getCocktails.mockResolvedValueOnce(mockCocktails)

      const menuStore = useMenuStore()
      await menuStore.load()

      menuStore.invalidate()

      apiClient.getCategories.mockClear()
      apiClient.getCocktails.mockClear()
      apiClient.getCategories.mockResolvedValueOnce(mockCategories)
      apiClient.getCocktails.mockResolvedValueOnce(mockCocktails)

      await menuStore.load()

      expect(apiClient.getCategories).toHaveBeenCalled()
      expect(apiClient.getCocktails).toHaveBeenCalled()
    })
  })

  describe('removeLocal action', () => {
    it('doit retirer un cocktail du cache par id', () => {
      const menuStore = useMenuStore()
      menuStore.cocktails = mockCocktails

      const snapshot = menuStore.removeLocal(1)

      expect(menuStore.cocktails).toHaveLength(1)
      const cocktail = menuStore.cocktails[0]
      if (cocktail) {
        expect(cocktail.id).toBe(2)
      }
      expect(snapshot).not.toBeNull()
      if (snapshot) {
        expect(snapshot.item.id).toBe(1)
      }
    })

    it('doit retourner null si le cocktail n\'existe pas', () => {
      const menuStore = useMenuStore()
      menuStore.cocktails = mockCocktails

      const snapshot = menuStore.removeLocal(999)

      expect(snapshot).toBeNull()
      expect(menuStore.cocktails).toHaveLength(2)
    })

    it('doit retourner le snapshot avec l\'index correct', () => {
      const menuStore = useMenuStore()
      menuStore.cocktails = mockCocktails

      const snapshot = menuStore.removeLocal(2)

      expect(snapshot?.index).toBe(1)
      expect(snapshot?.item.id).toBe(2)
    })
  })

  describe('restoreLocal action', () => {
    it('doit restaurer un cocktail à partir du snapshot', () => {
      const menuStore = useMenuStore()
      menuStore.cocktails = [...mockCocktails]

      const snapshot = menuStore.removeLocal(1)
      expect(menuStore.cocktails).toHaveLength(1)

      if (snapshot) {
        menuStore.restoreLocal(snapshot)
      }

      expect(menuStore.cocktails).toHaveLength(2)
      expect(menuStore.cocktails[0]!.id).toBe(1)
    })

    it('ne doit rien faire si le snapshot est null', () => {
      const menuStore = useMenuStore()
      menuStore.cocktails = mockCocktails

      menuStore.restoreLocal(null)

      expect(menuStore.cocktails).toEqual(mockCocktails)
    })

    it('doit restaurer à l\'index correct', () => {
      const menuStore = useMenuStore()
      const cocktail1 = mockCocktails[1]
      if (cocktail1) {
        menuStore.cocktails = [cocktail1]
      }

      const cocktail0 = mockCocktails[0]
      if (cocktail0) {
        const mockSnapshot = {
          item: cocktail0,
          index: 0,
        }

        menuStore.restoreLocal(mockSnapshot)

        expect(menuStore.cocktails).toHaveLength(2)
        const c0 = menuStore.cocktails[0]
        const c1 = menuStore.cocktails[1]
        if (c0 && c1) {
          expect(c0.id).toBe(1)
          expect(c1.id).toBe(2)
        }
      }
    })
  })

  describe('upsertLocal action', () => {
    it('doit ajouter un nouveau cocktail s\'il n\'existe pas', () => {
      const menuStore = useMenuStore()
      const cocktail0 = mockCocktails[0]
      if (cocktail0) {
        menuStore.cocktails = [cocktail0]
      }

      const cocktail1 = mockCocktails[1]
      if (cocktail1) {
        menuStore.upsertLocal(cocktail1)
      }

      expect(menuStore.cocktails).toHaveLength(2)
      const addedCocktail = menuStore.cocktails[1]
      if (addedCocktail) {
        expect(addedCocktail.id).toBe(2)
      }
    })

    it('doit remplacer un cocktail existant par son id', () => {
      const menuStore = useMenuStore()
      menuStore.cocktails = [...mockCocktails]

      const cocktail0 = mockCocktails[0]
      if (cocktail0) {
        const updatedCocktail = { ...cocktail0, name: 'Mojito Updated' }
        menuStore.upsertLocal(updatedCocktail)
      }

      expect(menuStore.cocktails).toHaveLength(2)
      const updatedCocktail = menuStore.cocktails[0]
      if (updatedCocktail) {
        expect(updatedCocktail.name).toBe('Mojito Updated')
      }
    })

    it('doit conserver l\'ordre lors du remplacement', () => {
      const menuStore = useMenuStore()
      menuStore.cocktails = [...mockCocktails]

      const cocktail1 = mockCocktails[1]
      if (cocktail1) {
        const updatedCocktail = { ...cocktail1, name: 'Cosmo Updated' }
        menuStore.upsertLocal(updatedCocktail)
      }

      const c0 = menuStore.cocktails[0]
      const c1 = menuStore.cocktails[1]
      if (c0 && c1) {
        expect(c0.id).toBe(1)
        expect(c1.id).toBe(2)
        expect(c1.name).toBe('Cosmo Updated')
      }
    })
  })

  describe('getById action', () => {
    it('doit retourner le cocktail avec l\'id donné', () => {
      const menuStore = useMenuStore()
      menuStore.cocktails = mockCocktails

      const cocktail = menuStore.getById(1)

      expect(cocktail).toEqual(mockCocktails[0])
    })

    it('doit retourner undefined si le cocktail n\'existe pas', () => {
      const menuStore = useMenuStore()
      menuStore.cocktails = mockCocktails

      const cocktail = menuStore.getById(999)

      expect(cocktail).toBeUndefined()
    })

    it('doit retourner undefined pour un panier vide', () => {
      const menuStore = useMenuStore()
      menuStore.cocktails = []

      const cocktail = menuStore.getById(1)

      expect(cocktail).toBeUndefined()
    })
  })
})
