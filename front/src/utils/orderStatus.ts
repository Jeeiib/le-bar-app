// Libellés et classes CSS d'affichage des statuts de commande, réutilisés côté client et barmaker.
import type { OrderStatus } from '@/types'

export function statusLabel(status: OrderStatus | undefined): string {
  switch (status) {
    case 'COMMANDEE':
      return 'Commandée'
    case 'EN_PREPARATION':
      return 'En préparation'
    case 'TERMINEE':
      return 'Terminée'
    default:
      return 'Statut inconnu'
  }
}

export function statusClass(status: OrderStatus | undefined): string {
  switch (status) {
    case 'COMMANDEE':
      return 'sb-new'
    case 'EN_PREPARATION':
      return 'sb-prep'
    case 'TERMINEE':
      return 'sb-done'
    default:
      return ''
  }
}
