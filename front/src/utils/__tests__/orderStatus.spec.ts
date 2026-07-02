import { describe, it, expect } from 'vitest'
import { statusLabel, statusClass } from '@/utils/orderStatus'
import type { OrderStatus } from '@/types'

describe('orderStatus utils', () => {
  describe('statusLabel', () => {
    it('doit retourner "Commandée" pour COMMANDEE', () => {
      expect(statusLabel('COMMANDEE')).toBe('Commandée')
    })

    it('doit retourner "En préparation" pour EN_PREPARATION', () => {
      expect(statusLabel('EN_PREPARATION')).toBe('En préparation')
    })

    it('doit retourner "Terminée" pour TERMINEE', () => {
      expect(statusLabel('TERMINEE')).toBe('Terminée')
    })

    it('doit retourner "Statut inconnu" pour undefined', () => {
      expect(statusLabel(undefined)).toBe('Statut inconnu')
    })

    it('doit retourner "Statut inconnu" pour un statut invalide', () => {
      expect(statusLabel('INVALIDE' as OrderStatus)).toBe('Statut inconnu')
    })
  })

  describe('statusClass', () => {
    it('doit retourner "sb-new" pour COMMANDEE', () => {
      expect(statusClass('COMMANDEE')).toBe('sb-new')
    })

    it('doit retourner "sb-prep" pour EN_PREPARATION', () => {
      expect(statusClass('EN_PREPARATION')).toBe('sb-prep')
    })

    it('doit retourner "sb-done" pour TERMINEE', () => {
      expect(statusClass('TERMINEE')).toBe('sb-done')
    })

    it('doit retourner une chaîne vide pour undefined', () => {
      expect(statusClass(undefined)).toBe('')
    })

    it('doit retourner une chaîne vide pour un statut invalide', () => {
      expect(statusClass('INVALIDE' as OrderStatus)).toBe('')
    })
  })
})
