import { type ReactNode } from 'react'
import { type ChangementContrat } from '@/api/generated'
import { valeurChamp } from './format'

/** Une valeur, surlignée si elle a changé : l'avant barré, puis l'après (RG-CA-09, 14). */
export function Surligne({
  changement,
  children,
}: {
  changement?: ChangementContrat
  children: ReactNode
}) {
  if (!changement) return <>{children}</>
  return (
    <span className='bg-warning rounded px-1'>
      {changement.avant != null && (
        <>
          <del className='text-muted-foreground'>
            {valeurChamp(changement.champ, changement.avant)}
          </del>{' '}
        </>
      )}
      <strong className='text-warning-foreground'>{children}</strong>
    </span>
  )
}
