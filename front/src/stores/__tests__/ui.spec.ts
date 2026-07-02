import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useUiStore } from '@/stores/ui'

describe('UI store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.runOnlyPendingTimers()
    vi.useRealTimers()
  })

  describe('Confirmation dialog', () => {
    it('doit initialiser avec confirmOpen = false', () => {
      const uiStore = useUiStore()
      expect(uiStore.confirmOpen).toBe(false)
      expect(uiStore.confirmMessage).toBe('')
      expect(uiStore.confirmLabel).toBe('Confirmer')
    })

    it('doit ouvrir la modale de confirmation avec confirm()', async () => {
      const uiStore = useUiStore()
      const promise = uiStore.confirm('Êtes-vous sûr ?')

      expect(uiStore.confirmOpen).toBe(true)
      expect(uiStore.confirmMessage).toBe('Êtes-vous sûr ?')
      expect(uiStore.confirmLabel).toBe('Confirmer')
      expect(promise).toBeInstanceOf(Promise)
    })

    it('doit accepter un label personnalisé pour le bouton', async () => {
      const uiStore = useUiStore()
      uiStore.confirm('Supprimer ?', 'Oui, supprimer')

      expect(uiStore.confirmLabel).toBe('Oui, supprimer')
    })

    it('doit résoudre la promise avec true si ok', async () => {
      const uiStore = useUiStore()
      const promise = uiStore.confirm('Êtes-vous sûr ?')

      uiStore.resolveConfirm(true)

      const result = await promise
      expect(result).toBe(true)
    })

    it('doit résoudre la promise avec false si annuler', async () => {
      const uiStore = useUiStore()
      const promise = uiStore.confirm('Êtes-vous sûr ?')

      uiStore.resolveConfirm(false)

      const result = await promise
      expect(result).toBe(false)
    })

    it('doit fermer la modale après resolveConfirm', async () => {
      const uiStore = useUiStore()
      const _promise = uiStore.confirm('Êtes-vous sûr ?')
      expect(uiStore.confirmOpen).toBe(true)

      uiStore.resolveConfirm(true)
      await _promise

      expect(uiStore.confirmOpen).toBe(false)
    })

    it('doit gérer plusieurs confirmations de suite', async () => {
      const uiStore = useUiStore()
      const promise1 = uiStore.confirm('Premier ?')
      uiStore.resolveConfirm(true)
      await promise1

      const promise2 = uiStore.confirm('Deuxième ?')
      uiStore.resolveConfirm(false)
      const result = await promise2

      expect(result).toBe(false)
    })
  })

  describe('Toasts', () => {
    it('doit initialiser avec un tableau de toasts vide', () => {
      const uiStore = useUiStore()
      expect(uiStore.toasts).toEqual([])
    })

    it('doit ajouter un toast success', () => {
      const uiStore = useUiStore()
      uiStore.toast('Opération réussie', 'success')

      expect(uiStore.toasts).toHaveLength(1)
      const toast = uiStore.toasts[0]
      if (toast) {
        expect(toast.message).toBe('Opération réussie')
        expect(toast.type).toBe('success')
      }
    })

    it('doit ajouter un toast error', () => {
      const uiStore = useUiStore()
      uiStore.toast('Erreur !', 'error')

      expect(uiStore.toasts).toHaveLength(1)
      const toast = uiStore.toasts[0]
      if (toast) {
        expect(toast.type).toBe('error')
      }
    })

    it('doit ajouter un toast info par défaut', () => {
      const uiStore = useUiStore()
      uiStore.toast('Information')

      expect(uiStore.toasts).toHaveLength(1)
      const toast = uiStore.toasts[0]
      if (toast) {
        expect(toast.type).toBe('info')
      }
    })

    it('doit assigner des ids uniques aux toasts', () => {
      const uiStore = useUiStore()
      uiStore.toast('Toast 1')
      uiStore.toast('Toast 2')
      uiStore.toast('Toast 3')

      const toast0 = uiStore.toasts[0]
      const toast1 = uiStore.toasts[1]
      const toast2 = uiStore.toasts[2]
      if (toast0 && toast1 && toast2) {
        expect(toast0.id).not.toBe(toast1.id)
        expect(toast1.id).not.toBe(toast2.id)
      }
    })

    it('doit supprimer automatiquement un toast après 3500ms', async () => {
      const uiStore = useUiStore()
      uiStore.toast('Temporaire', 'info')

      expect(uiStore.toasts).toHaveLength(1)

      vi.advanceTimersByTime(3500)

      expect(uiStore.toasts).toHaveLength(0)
    })

    it('doit supprimer le bon toast parmi plusieurs', async () => {
      const uiStore = useUiStore()
      uiStore.toast('Toast 1', 'success')

      vi.advanceTimersByTime(1000)

      uiStore.toast('Toast 2', 'error')
      const toast1 = uiStore.toasts[1]
      const id2 = toast1 ? toast1.id : 0

      vi.advanceTimersByTime(2500)
      // Toast 1 disparaît à 3500ms total
      expect(uiStore.toasts).toHaveLength(1)
      const currentToast = uiStore.toasts[0]
      if (currentToast) {
        expect(currentToast.id).toBe(id2)
      }

      vi.advanceTimersByTime(1000)
      // Toast 2 disparaît à 4500ms total
      expect(uiStore.toasts).toHaveLength(0)
    })

    it('doit garder les toasts avant leur timeout', async () => {
      const uiStore = useUiStore()
      uiStore.toast('Message', 'info')

      vi.advanceTimersByTime(3000)

      expect(uiStore.toasts).toHaveLength(1)

      vi.advanceTimersByTime(500)

      expect(uiStore.toasts).toHaveLength(0)
    })

    it('doit permettre plusieurs toasts simultanés', () => {
      const uiStore = useUiStore()
      uiStore.toast('Toast 1', 'success')
      uiStore.toast('Toast 2', 'error')
      uiStore.toast('Toast 3', 'info')

      expect(uiStore.toasts).toHaveLength(3)
      const toast0 = uiStore.toasts[0]
      const toast1 = uiStore.toasts[1]
      const toast2 = uiStore.toasts[2]
      if (toast0 && toast1 && toast2) {
        expect(toast0.type).toBe('success')
        expect(toast1.type).toBe('error')
        expect(toast2.type).toBe('info')
      }
    })

    it('doit ajouter plusieurs toasts et les supprimer correctement', async () => {
      const uiStore = useUiStore()
      uiStore.toast('Toast 1')
      uiStore.toast('Toast 2')

      expect(uiStore.toasts).toHaveLength(2)

      vi.advanceTimersByTime(3500)

      expect(uiStore.toasts).toHaveLength(0)
    })
  })
})
