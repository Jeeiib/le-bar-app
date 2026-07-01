import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as api from '@/api/client'
import type { Category, Cocktail } from '@/types'

// Cache du menu : chargé une seule fois, les visites suivantes (retour depuis une fiche)
// sont instantanées et ne relancent pas de requête.
export const useMenuStore = defineStore('menu', () => {
  const categories = ref<Category[]>([])
  const cocktails = ref<Cocktail[]>([])
  const loaded = ref(false)
  const loading = ref(false)

  const load = async () => {
    if (loaded.value || loading.value) {
      return
    }
    loading.value = true
    try {
      const [cats, cocks] = await Promise.all([api.getCategories(), api.getCocktails()])
      categories.value = cats
      cocktails.value = cocks
      loaded.value = true
    } catch (error) {
      console.error('Erreur lors du chargement du menu:', error)
    } finally {
      loading.value = false
    }
  }

  // Force un rechargement (après création/édition/suppression côté barmaker)
  const reload = async () => {
    loading.value = true
    try {
      const [cats, cocks] = await Promise.all([api.getCategories(), api.getCocktails()])
      categories.value = cats
      cocktails.value = cocks
      loaded.value = true
    } catch (error) {
      console.error('Erreur lors du rechargement du menu:', error)
    } finally {
      loading.value = false
    }
  }

  // Invalide le cache : le prochain load() refera la requête
  const invalidate = () => {
    loaded.value = false
  }

  // --- Mutations locales (optimistic UI côté barmaker) ---

  // Retire un cocktail du cache et renvoie de quoi le restaurer en cas d'échec API
  const removeLocal = (id: number): { item: Cocktail; index: number } | null => {
    const index = cocktails.value.findIndex((c) => c.id === id)
    const item = cocktails.value[index]
    if (index === -1 || !item) {
      return null
    }
    cocktails.value.splice(index, 1)
    return { item, index }
  }

  const restoreLocal = (snapshot: { item: Cocktail; index: number } | null) => {
    if (snapshot) {
      cocktails.value.splice(snapshot.index, 0, snapshot.item)
    }
  }

  // Ajoute ou remplace un cocktail dans le cache (après création/édition)
  const upsertLocal = (cocktail: Cocktail) => {
    const index = cocktails.value.findIndex((c) => c.id === cocktail.id)
    if (index === -1) {
      cocktails.value.push(cocktail)
    } else {
      cocktails.value[index] = cocktail
    }
  }

  const getById = (id: number) => cocktails.value.find((c) => c.id === id)

  return {
    categories,
    cocktails,
    loaded,
    loading,
    load,
    reload,
    invalidate,
    removeLocal,
    restoreLocal,
    upsertLocal,
    getById,
  }
})
