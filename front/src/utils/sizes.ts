import type { Size } from '@/types'

// Équivalent en centilitres de chaque taille (logique de bar).
export const SIZE_CL: Record<Size, string> = {
  S: '15 cl',
  M: '25 cl',
  L: '35 cl',
}

export const sizeCl = (size: Size): string => SIZE_CL[size] ?? ''
