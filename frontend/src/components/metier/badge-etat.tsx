import { BadgePoint } from './badge-statut'

/** Actif ou inactif, en point coloré (succès, danger). */
export function BadgeEtat({ actif }: { actif: boolean }) {
  return (
    <BadgePoint statut={actif ? 'success' : 'danger'}>
      {actif ? 'Actif' : 'Inactif'}
    </BadgePoint>
  )
}
