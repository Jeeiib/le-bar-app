import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import * as api from '@/api/client'
import type { TableInfo } from '@/types'

// La table et le prenom survivent a un rafraichissement (session a table)
const TABLE_KEY = 'barapp_table'
const NAME_KEY = 'barapp_customer'

export const useTableStore = defineStore('table', () => {
  const storedTable = sessionStorage.getItem(TABLE_KEY)
  const current = ref<TableInfo | null>(storedTable ? JSON.parse(storedTable) : null)
  const customerName = ref<string>(sessionStorage.getItem(NAME_KEY) ?? '')

  watch(current, (value) => {
    if (value) sessionStorage.setItem(TABLE_KEY, JSON.stringify(value))
  })
  watch(customerName, (value) => sessionStorage.setItem(NAME_KEY, value))

  const load = async (qrSlug: string) => {
    try {
      const tableInfo = await api.getTable(qrSlug)
      current.value = tableInfo
      return tableInfo
    } catch (error) {
      console.error('Erreur lors du chargement de la table:', error)
      throw error
    }
  }

  return {
    current,
    customerName,
    load,
  }
})
