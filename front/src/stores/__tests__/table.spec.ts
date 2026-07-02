import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useTableStore } from '@/stores/table'
import type { TableInfo } from '@/types'

// Mock du module API
vi.mock('@/api/client', () => ({
  getTable: vi.fn(),
}))

describe('Table store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    sessionStorage.clear()
    vi.clearAllMocks()
  })

  describe('Initialisation', () => {
    it('doit initialiser avec current = null par défaut', () => {
      const tableStore = useTableStore()
      expect(tableStore.current).toBeNull()
      expect(tableStore.customerName).toBe('')
    })

    it('doit charger la table depuis sessionStorage si présente', () => {
      const mockTable: TableInfo = { id: 1, label: 'Table 1' }
      sessionStorage.setItem('barapp_table', JSON.stringify(mockTable))

      const tableStore = useTableStore()
      expect(tableStore.current).toEqual(mockTable)
    })

    it('doit charger le nom du client depuis sessionStorage si présent', () => {
      sessionStorage.setItem('barapp_customer', 'Alice')

      const tableStore = useTableStore()
      expect(tableStore.customerName).toBe('Alice')
    })

    it('doit charger à la fois table et customer name du sessionStorage', () => {
      const mockTable: TableInfo = { id: 1, label: 'Table 1' }
      sessionStorage.setItem('barapp_table', JSON.stringify(mockTable))
      sessionStorage.setItem('barapp_customer', 'Bob')

      const tableStore = useTableStore()
      expect(tableStore.current).toEqual(mockTable)
      expect(tableStore.customerName).toBe('Bob')
    })
  })

  describe('load action', () => {
    it('doit appeler l\'API getTable avec le qrSlug', async () => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const apiClient = (await import('@/api/client')) as any
      const mockTable: TableInfo = { id: 1, label: 'Table 1' }
      apiClient.getTable.mockResolvedValueOnce(mockTable)

      const tableStore = useTableStore()
      await tableStore.load('abc123')

      expect(apiClient.getTable).toHaveBeenCalledWith('abc123')
    })

    it('doit mettre à jour current avec la réponse de l\'API', async () => {
      const apiClient = await import('@/api/client')
      const mockTable: TableInfo = { id: 5, label: 'Table 5' }
      vi.mocked(apiClient.getTable).mockResolvedValueOnce(mockTable)

      const tableStore = useTableStore()
      const result = await tableStore.load('xyz789')

      expect(tableStore.current).toEqual(mockTable)
      expect(result).toEqual(mockTable)
    })

    it('doit retourner les infos de la table chargée', async () => {
      const apiClient = await import('@/api/client')
      const mockTable: TableInfo = { id: 3, label: 'Terrasse' }
      vi.mocked(apiClient.getTable).mockResolvedValueOnce(mockTable)

      const tableStore = useTableStore()
      const result = await tableStore.load('outdoor')

      expect(result).toEqual(mockTable)
    })

    it('doit lancer une erreur si l\'API échoue', async () => {
      const apiClient = await import('@/api/client')
      vi.mocked(apiClient.getTable).mockRejectedValueOnce(new Error('Invalid QR Code'))
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      const tableStore = useTableStore()

      await expect(tableStore.load('invalid')).rejects.toThrow('Invalid QR Code')

      consoleErrorSpy.mockRestore()
    })

    it('ne doit pas modifier current si l\'API échoue', async () => {
      const apiClient = await import('@/api/client')
      vi.mocked(apiClient.getTable).mockRejectedValueOnce(new Error('API Error'))
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      const tableStore = useTableStore()
      const consoleErrorSpyLocal = vi.spyOn(console, 'error').mockImplementation(() => {})

      try {
        await tableStore.load('bad-slug')
      } catch {
        // Ignorer l'erreur
      }

      expect(tableStore.current).toBeNull()

      consoleErrorSpy.mockRestore()
      consoleErrorSpyLocal.mockRestore()
    })
  })

  describe('sessionStorage persistence', () => {
    it('doit sauvegarder current dans sessionStorage quand il change', async () => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const apiClient = (await import('@/api/client')) as any
      const mockTable: TableInfo = { id: 1, label: 'Table 1' }
      apiClient.getTable.mockResolvedValueOnce(mockTable)

      const tableStore = useTableStore()
      await tableStore.load('abc')
      await new Promise(resolve => setTimeout(resolve, 10))

      const stored = JSON.parse(sessionStorage.getItem('barapp_table') || 'null')
      expect(stored).toEqual(mockTable)
    })

    it('doit sauvegarder customerName dans sessionStorage quand il change', async () => {
      const tableStore = useTableStore()
      tableStore.customerName = 'Charlie'
      await new Promise(resolve => setTimeout(resolve, 10))

      const stored = sessionStorage.getItem('barapp_customer')
      expect(stored).toBe('Charlie')
    })

    it('doit sauvegarder une chaîne vide pour customerName si vide', async () => {
      const tableStore = useTableStore()
      tableStore.customerName = 'Initial'
      await new Promise(resolve => setTimeout(resolve, 10))
      tableStore.customerName = ''
      await new Promise(resolve => setTimeout(resolve, 10))

      const stored = sessionStorage.getItem('barapp_customer')
      expect(stored).toBe('')
    })
  })

  describe('Changements de state', () => {
    it('doit permettre de modifier customerName manuellement', () => {
      const tableStore = useTableStore()
      tableStore.customerName = 'Diana'

      expect(tableStore.customerName).toBe('Diana')
    })

    it('doit permettre de modifier current manuellement', () => {
      const tableStore = useTableStore()
      const newTable: TableInfo = { id: 2, label: 'Table 2' }
      tableStore.current = newTable

      expect(tableStore.current).toEqual(newTable)
    })

    it('doit sauvegarder les changements manuels en sessionStorage', async () => {
      const tableStore = useTableStore()
      const newTable: TableInfo = { id: 10, label: 'Bar' }
      tableStore.current = newTable
      await new Promise(resolve => setTimeout(resolve, 10))

      const stored = JSON.parse(sessionStorage.getItem('barapp_table') || 'null')
      expect(stored).toEqual(newTable)
    })
  })
})
