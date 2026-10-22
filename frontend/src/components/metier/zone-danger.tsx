import { type ReactNode } from 'react'
import { cn } from '@/lib/utils'

/**
 * Le cadre « Zone à risque », tout en bas d'une page de détail : les actions qui retirent ou suppriment (désactiver,
 * suspendre, bloquer, supprimer…), chacune avec ce qu'elle entraîne ; la confirmation reste celle de l'action.
 */
export function ZoneDanger({
  titre = 'Zone à risque',
  className,
  children,
}: {
  /** Un autre titre, pour une action sensible qui ne supprime rien (« Session », pour se déconnecter). */
  titre?: string
  className?: string
  children: ReactNode
}) {
  return (
    <section
      className={cn('border-destructive/50 mt-8 rounded-xl border', className)}
    >
      <h2 className='text-destructive border-destructive/30 border-b px-6 py-4 text-base font-semibold'>
        {titre}
      </h2>
      <div className='divide-destructive/20 divide-y'>{children}</div>
    </section>
  )
}

/** Une action de la zone à risque : son nom, ce qu'elle entraîne, son bouton à droite. */
export function ActionDanger({
  titre,
  description,
  children,
}: {
  titre: string
  description: ReactNode
  children: ReactNode
}) {
  return (
    <div className='flex flex-wrap items-center justify-between gap-4 px-6 py-4'>
      <div className='min-w-0 flex-1 basis-80 space-y-1'>
        <p className='font-medium'>{titre}</p>
        <p className='text-muted-foreground text-sm'>{description}</p>
      </div>
      <div className='shrink-0'>{children}</div>
    </div>
  )
}
