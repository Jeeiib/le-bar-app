import { describe, it, expect } from 'vitest'
import { statusLabel, statusClass } from '@/utils/orderStatus'
import type { OrderStatus } from '@/types'

describe('orderStatus utils', () => {
  describe('statusLabel', () => {
    it('doit retourner "Commandée" pour ORDERED', () => {
      expect(statusLabel('ORDERED')).toBe('Commandée')
    })

    it('doit retourner "En préparation" pour IN_PREPARATION', () => {
      expect(statusLabel('IN_PREPARATION')).toBe('En préparation')
    })

    it('doit retourner "Terminée" pour COMPLETED', () => {
      expect(statusLabel('COMPLETED')).toBe('Terminée')
    })

    it('doit retourner "Statut inconnu" pour undefined', () => {
      expect(statusLabel(undefined)).toBe('Statut inconnu')
    })

    it('doit retourner "Statut inconnu" pour un statut invalide', () => {
      expect(statusLabel('INVALIDE' as OrderStatus)).toBe('Statut inconnu')
    })
  })

  describe('statusClass', () => {
    it('doit retourner "sb-new" pour ORDERED', () => {
      expect(statusClass('ORDERED')).toBe('sb-new')
    })

    it('doit retourner "sb-prep" pour IN_PREPARATION', () => {
      expect(statusClass('IN_PREPARATION')).toBe('sb-prep')
    })

    it('doit retourner "sb-done" pour COMPLETED', () => {
      expect(statusClass('COMPLETED')).toBe('sb-done')
    })

    it('doit retourner une chaîne vide pour undefined', () => {
      expect(statusClass(undefined)).toBe('')
    })

    it('doit retourner une chaîne vide pour un statut invalide', () => {
      expect(statusClass('INVALIDE' as OrderStatus)).toBe('')
    })
  })
})
