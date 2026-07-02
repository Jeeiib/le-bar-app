import { describe, it, expect } from 'vitest'
import { sizeCl, SIZE_CL } from '@/utils/sizes'
import type { Size } from '@/types'

describe('sizes.ts', () => {
  describe('SIZE_CL', () => {
    it('doit contenir les équivalents en centilitres pour chaque taille', () => {
      expect(SIZE_CL).toEqual({
        S: '15 cl',
        M: '25 cl',
        L: '35 cl',
      })
    })

    it('doit avoir une clé pour S', () => {
      expect(SIZE_CL.S).toBe('15 cl')
    })

    it('doit avoir une clé pour M', () => {
      expect(SIZE_CL.M).toBe('25 cl')
    })

    it('doit avoir une clé pour L', () => {
      expect(SIZE_CL.L).toBe('35 cl')
    })
  })

  describe('sizeCl', () => {
    it('doit retourner la taille en centilitres pour S', () => {
      expect(sizeCl('S')).toBe('15 cl')
    })

    it('doit retourner la taille en centilitres pour M', () => {
      expect(sizeCl('M')).toBe('25 cl')
    })

    it('doit retourner la taille en centilitres pour L', () => {
      expect(sizeCl('L')).toBe('35 cl')
    })

    it('doit retourner une chaîne vide pour une taille invalide', () => {
      expect(sizeCl('XL' as Size)).toBe('')
    })
  })
})
