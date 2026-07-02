import { describe, it, expect, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useCartStore } from '@/stores/cart'
import type { Cocktail } from '@/types'

describe('Cart store', () => {
  let mockCocktail: Cocktail

  beforeEach(() => {
    setActivePinia(createPinia())
    sessionStorage.clear()

    mockCocktail = {
      id: 1,
      name: 'Mojito',
      description: 'Rafraichissant',
      available: true,
      categoryId: 1,
      categoryName: 'Rhum',
      ingredients: [],
      sizes: [{ size: 'M', price: 8 }],
      imageUrl: 'https://example.com/mojito.jpg',
    }
  })

  describe('Initialisation', () => {
    it('doit initialiser avec un panier vide par défaut', () => {
      const cartStore = useCartStore()
      expect(cartStore.items).toEqual([])
      expect(cartStore.count).toBe(0)
      expect(cartStore.total).toBe(0)
    })

    it('doit charger le panier depuis sessionStorage si présent', () => {
      const mockCart = [
        {
          cocktail: mockCocktail,
          size: 'M' as const,
          unitPrice: 8,
          quantity: 2,
        },
      ]
      sessionStorage.setItem('barapp_cart', JSON.stringify(mockCart))

      const cartStore = useCartStore()
      expect(cartStore.items).toHaveLength(1)
      expect(cartStore.items[0]!.quantity).toBe(2)
    })
  })

  describe('add action', () => {
    it('doit ajouter un nouvel article au panier', () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)

      expect(cartStore.items).toHaveLength(1)
      expect(cartStore.items[0]!.cocktail.id).toBe(1)
      expect(cartStore.items[0]!.size).toBe('M')
      expect(cartStore.items[0]!.quantity).toBe(1)
    })

    it('doit incrémenter la quantité si le cocktail et la taille existent déjà', () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)
      cartStore.add(mockCocktail, 'M', 8)

      expect(cartStore.items).toHaveLength(1)
      expect(cartStore.items[0]!.quantity).toBe(2)
    })

    it('doit créer un nouvel article si c\'est le même cocktail mais une taille différente', () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)
      cartStore.add(mockCocktail, 'L', 10)

      expect(cartStore.items).toHaveLength(2)
      expect(cartStore.items[0]!.size).toBe('M')
      expect(cartStore.items[1]!.size).toBe('L')
    })

    it('doit créer un nouvel article si c\'est un cocktail différent', () => {
      const cartStore = useCartStore()
      const anotherCocktail = { ...mockCocktail, id: 2, name: 'Daiquiri' }

      cartStore.add(mockCocktail, 'M', 8)
      cartStore.add(anotherCocktail, 'M', 9)

      expect(cartStore.items).toHaveLength(2)
      expect(cartStore.items[0]!.cocktail.id).toBe(1)
      expect(cartStore.items[1]!.cocktail.id).toBe(2)
    })
  })

  describe('increment action', () => {
    it('doit augmenter la quantité de l\'article à l\'index donné', () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)
      cartStore.increment(0)

      expect(cartStore.items[0]!.quantity).toBe(2)
    })

    it('ne doit rien faire si l\'index n\'existe pas', () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)
      cartStore.increment(99)

      expect(cartStore.items[0]!.quantity).toBe(1)
    })
  })

  describe('decrement action', () => {
    it('doit diminuer la quantité de l\'article à l\'index donné', () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)
      cartStore.increment(0)
      cartStore.decrement(0)

      expect(cartStore.items[0]!.quantity).toBe(1)
    })

    it('doit supprimer l\'article si la quantité devient 0', () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)
      cartStore.decrement(0)

      expect(cartStore.items).toHaveLength(0)
    })

    it('doit supprimer l\'article si la quantité devient négative', () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)
      cartStore.decrement(0)
      cartStore.decrement(0)

      expect(cartStore.items).toHaveLength(0)
    })

    it('ne doit rien faire si l\'index n\'existe pas', () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)
      cartStore.decrement(99)

      expect(cartStore.items).toHaveLength(1)
      expect(cartStore.items[0]!.quantity).toBe(1)
    })
  })

  describe('remove action', () => {
    it('doit supprimer l\'article à l\'index donné', () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)
      const anotherCocktail = { ...mockCocktail, id: 2 }
      cartStore.add(anotherCocktail, 'M', 8)

      cartStore.remove(0)

      expect(cartStore.items).toHaveLength(1)
      expect(cartStore.items[0]!.cocktail.id).toBe(2)
    })

    it('doit ignorer les tentatives de suppression sur un index invalide', () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)
      cartStore.remove(99)

      expect(cartStore.items).toHaveLength(1)
    })
  })

  describe('clear action', () => {
    it('doit vider complètement le panier', () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)
      const anotherCocktail = { ...mockCocktail, id: 2 }
      cartStore.add(anotherCocktail, 'L', 10)

      cartStore.clear()

      expect(cartStore.items).toHaveLength(0)
      expect(cartStore.count).toBe(0)
      expect(cartStore.total).toBe(0)
    })
  })

  describe('count computed', () => {
    it('doit retourner 0 pour un panier vide', () => {
      const cartStore = useCartStore()
      expect(cartStore.count).toBe(0)
    })

    it('doit compter le nombre total d\'articles', () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)
      cartStore.add(mockCocktail, 'M', 8)

      expect(cartStore.count).toBe(2)
    })

    it('doit sommer les quantités de tous les articles', () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)
      const anotherCocktail = { ...mockCocktail, id: 2 }
      cartStore.add(anotherCocktail, 'L', 10)
      cartStore.add(anotherCocktail, 'L', 10)

      expect(cartStore.count).toBe(3)
    })
  })

  describe('total computed', () => {
    it('doit retourner 0 pour un panier vide', () => {
      const cartStore = useCartStore()
      expect(cartStore.total).toBe(0)
    })

    it('doit calculer le total correctement avec une quantité', () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)

      expect(cartStore.total).toBe(8)
    })

    it('doit calculer le total correctement avec plusieurs articles et quantités', () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)
      cartStore.add(mockCocktail, 'M', 8)
      const anotherCocktail = { ...mockCocktail, id: 2 }
      cartStore.add(anotherCocktail, 'L', 10)

      expect(cartStore.total).toBe(8 + 8 + 10)
    })

    it('doit mettre à jour le total après incrément', () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)
      cartStore.increment(0)

      expect(cartStore.total).toBe(16)
    })

    it('doit mettre à jour le total après décrement', () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)
      cartStore.add(mockCocktail, 'M', 8)
      cartStore.decrement(0)

      expect(cartStore.total).toBe(8)
    })
  })

  describe('sessionStorage persistence', () => {
    it('doit sauvegarder le panier dans sessionStorage lors de modifications', async () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)
      await new Promise(resolve => setTimeout(resolve, 10))

      const stored = JSON.parse(sessionStorage.getItem('barapp_cart') || '[]')
      expect(stored).toHaveLength(1)
      expect(stored[0].cocktail.id).toBe(1)
    })

    it('doit sauvegarder le panier après un increment', async () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)
      cartStore.increment(0)
      await new Promise(resolve => setTimeout(resolve, 10))

      const stored = JSON.parse(sessionStorage.getItem('barapp_cart') || '[]')
      expect(stored[0].quantity).toBe(2)
    })

    it('doit sauvegarder le panier après un decrement', async () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)
      cartStore.add(mockCocktail, 'M', 8)
      cartStore.decrement(0)
      await new Promise(resolve => setTimeout(resolve, 10))

      const stored = JSON.parse(sessionStorage.getItem('barapp_cart') || '[]')
      expect(stored).toHaveLength(1)
    })

    it('doit sauvegarder le panier après un clear', async () => {
      const cartStore = useCartStore()
      cartStore.add(mockCocktail, 'M', 8)
      cartStore.clear()
      await new Promise(resolve => setTimeout(resolve, 10))

      const stored = JSON.parse(sessionStorage.getItem('barapp_cart') || '[]')
      expect(stored).toHaveLength(0)
    })
  })
})
