import { defineStore } from 'pinia'
import { ref } from 'vue'

interface Toast {
  id: number
  message: string
  type: 'success' | 'error' | 'info'
}

// Dialogues et notifications stylés (remplacent les alert()/confirm() natifs).
export const useUiStore = defineStore('ui', () => {
  // --- Confirmation (promisifiée) ---
  const confirmOpen = ref(false)
  const confirmMessage = ref('')
  const confirmLabel = ref('Confirmer')
  let resolver: ((ok: boolean) => void) | null = null

  const confirm = (message: string, label = 'Confirmer'): Promise<boolean> => {
    confirmMessage.value = message
    confirmLabel.value = label
    confirmOpen.value = true
    return new Promise((resolve) => {
      resolver = resolve
    })
  }

  const resolveConfirm = (ok: boolean) => {
    confirmOpen.value = false
    if (resolver) {
      resolver(ok)
      resolver = null
    }
  }

  // --- Toasts ---
  const toasts = ref<Toast[]>([])
  let nextId = 1

  const toast = (message: string, type: Toast['type'] = 'info') => {
    const id = nextId++
    toasts.value.push({ id, message, type })
    setTimeout(() => {
      toasts.value = toasts.value.filter((t) => t.id !== id)
    }, 3500)
  }

  return {
    confirmOpen,
    confirmMessage,
    confirmLabel,
    confirm,
    resolveConfirm,
    toasts,
    toast,
  }
})
