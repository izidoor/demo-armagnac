import { type ReactNode } from 'react'
import { cn } from '@/lib/utils'
import { Badge } from '@/components/ui/badge'

/** Couleur de statut du thème (index.css) ; « neutre » pour un état sans enjeu. */
export type Statut = 'neutre' | 'success' | 'warning' | 'danger' | 'info'

/** La couleur franche de chaque statut, en fond (point, badge plein). */
const FOND: Record<Statut, string> = {
  neutre: 'bg-muted-foreground',
  success: 'bg-success-foreground',
  warning: 'bg-warning-foreground',
  danger: 'bg-danger-foreground',
  info: 'bg-info-foreground',
}

/**
 * Un état : badge sobre, texte neutre, point de la couleur du statut. Les mises en garde, elles, sont en
 * contour coloré (variantes de statut de {@link Badge}).
 */
export function BadgePoint({
  statut,
  className,
  children,
}: {
  statut: Statut
  className?: string
  children: ReactNode
}) {
  return (
    <Badge
      variant='outline'
      className={cn('text-foreground gap-1.5 font-normal', className)}
    >
      <span className={cn('size-1.5 rounded-full', FOND[statut])} />
      {children}
    </Badge>
  )
}

/**
 * Un état qui doit se voir (cycle de vie d'une DSM) : badge plein de la couleur du statut, texte de la
 * couleur du fond de page (clair en thème clair, foncé en thème sombre).
 */
export function BadgePlein({
  statut,
  className,
  children,
}: {
  statut: Statut
  className?: string
  children: ReactNode
}) {
  return (
    <Badge
      className={cn(
        'text-background border-transparent',
        FOND[statut],
        className
      )}
    >
      {children}
    </Badge>
  )
}
