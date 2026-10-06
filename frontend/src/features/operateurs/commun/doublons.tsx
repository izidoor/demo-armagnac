import { Link } from '@tanstack/react-router'
import { TriangleAlert } from 'lucide-react'
import { type Doublon } from '@/api/generated'
import { CHAMPS_DOUBLON } from './libelles'

/** Avertissement sous un champ : la valeur est déjà portée par une autre fiche, nommée. */
export function AlerteDoublons({
  doublons,
  champ,
}: {
  doublons: Doublon[]
  champ: Doublon['champ']
}) {
  const concernes = doublons.filter((d) => d.champ === champ)
  if (concernes.length === 0) return null
  return (
    <p className='flex items-start gap-1 text-sm text-amber-700 dark:text-amber-400'>
      <TriangleAlert className='mt-0.5 size-4 shrink-0' />
      <span>
        {CHAMPS_DOUBLON[champ]} déjà porté par{' '}
        {concernes.map((d, i) => (
          <span key={d.operateur.id}>
            {i > 0 && ', '}
            <Link
              to='/bo/operateurs/$id'
              params={{ id: String(d.operateur.id) }}
              className='underline'
            >
              {d.operateur.code} · {d.operateur.raisonSociale}
            </Link>
            {d.operateur.actif ? '' : ' (inactive)'}
          </span>
        ))}{' '}
        ({concernes[0].regle}). Vous pouvez enregistrer.
      </span>
    </p>
  )
}
