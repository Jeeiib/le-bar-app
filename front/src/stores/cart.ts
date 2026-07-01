import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import type { Cocktail, Size } from '@/types'

interface CartItem {
  cocktail: Cocktail
  size: Size
  unitPrice: number
  quantity: number
}

// Cle de stockage : le panier survit a un rafraichissement, se vide a la fermeture de l'onglet
const STORAGE_KEY = 'barapp_cart'

export const useCartStore = defineStore('cart', () => {
  const stored = sessionStorage.getItem(STORAGE_KEY)
  const items = ref<CartItem[]>(stored ? JSON.parse(stored) : [])

  watch(
    items,
    (value) => sessionStorage.setItem(STORAGE_KEY, JSON.stringify(value)),
    { deep: true }
  )

  // Ajoute un verre : regroupe par cocktail + taille en incrementant la quantite
  const add = (cocktail: Cocktail, size: Size, unitPrice: number) => {
    const existing = items.value.find(
      (item) => item.cocktail.id === cocktail.id && item.size === size
    )
    if (existing) {
      existing.quantity++
    } else {
      items.value.push({ cocktail, size, unitPrice, quantity: 1 })
    }
  }

  const increment = (index: number) => {
    const item = items.value[index]
    if (item) {
      item.quantity++
    }
  }

  const decrement = (index: number) => {
    const item = items.value[index]
    if (!item) {
      return
    }
    item.quantity--
    if (item.quantity <= 0) {
      items.value.splice(index, 1)
    }
  }

  const remove = (index: number) => {
    items.value.splice(index, 1)
  }

  const clear = () => {
    items.value = []
  }

  const count = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))

  const total = computed(() =>
    items.value.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0)
  )

  return {
    items,
    count,
    total,
    add,
    increment,
    decrement,
    remove,
    clear,
  }
})
