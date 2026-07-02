// Libellés et classes CSS d'affichage des statuts de commande, réutilisés côté client et barmaker.
import type { OrderStatus } from '@/types'

export function statusLabel(status: OrderStatus | undefined): string {
  switch (status) {
    case 'ORDERED':
      return 'Commandée'
    case 'IN_PREPARATION':
      return 'En préparation'
    case 'COMPLETED':
      return 'Terminée'
    default:
      return 'Statut inconnu'
  }
}

export function statusClass(status: OrderStatus | undefined): string {
  switch (status) {
    case 'ORDERED':
      return 'sb-new'
    case 'IN_PREPARATION':
      return 'sb-prep'
    case 'COMPLETED':
      return 'sb-done'
    default:
      return ''
  }
}
