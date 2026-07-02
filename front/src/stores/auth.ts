import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as api from '@/api/client'

const STORAGE_KEY = 'barmaker_auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>('')
  const name = ref<string>('')
  const role = ref<string>('')

  // Récupérer depuis localStorage au démarrage
  const loadFromStorage = () => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY)
      if (stored) {
        const data = JSON.parse(stored)
        token.value = data.token || ''
        name.value = data.name || ''
        role.value = data.role || ''
      }
    } catch (e) {
      console.error('Erreur lors du chargement de l\'auth:', e)
    }
  }

  // Sauvegarder en localStorage
  const saveToStorage = () => {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        token: token.value,
        name: name.value,
        role: role.value,
      })
    )
  }

  // Actions
  const login = async (email: string, password: string) => {
    try {
      const response = await api.login(email, password)
      token.value = response.token
      name.value = response.name
      role.value = response.role
      saveToStorage()
      return response
    } catch (error) {
      console.error('Erreur de connexion:', error)
      throw error
    }
  }

  const logout = () => {
    token.value = ''
    name.value = ''
    role.value = ''
    localStorage.removeItem(STORAGE_KEY)
  }

  // Getters
  const isAuthenticated = computed(() => !!token.value)

  // Charger au démarrage du store
  loadFromStorage()

  return {
    token,
    name,
    role,
    isAuthenticated,
    login,
    logout,
  }
})
