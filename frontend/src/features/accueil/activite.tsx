import { type ReactNode } from 'react'
import { Link, type LinkProps } from '@tanstack/react-router'
import { ChevronRight, type LucideIcon } from 'lucide-react'
import { cn } from '@/lib/utils'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'

/** Les briques des tableaux de bord, communes au back-office et au portail. */

const entier = new Intl.NumberFormat('fr-FR', {
  maximumFractionDigits: 0,
})

export function Section({
  titre,
  children,
}: {
  titre: string
  children: ReactNode
}) {
  return (
    <section>
      <h2 className='mb-4 text-xl font-semibold'>{titre}</h2>
      {children}
    </section>
  )
}

/** La grille des activités : deux par ligne, quatre sur grand écran. */
export function GrilleActivites({ children }: { children: ReactNode }) {
  return (
    <div className='grid grid-cols-2 gap-3 sm:gap-4 xl:grid-cols-4'>
      {children}
    </div>
  )
}

/** Un nombre en attente de sa lecture. */
export function Valeur({ valeur }: { valeur: string | undefined }) {
  return valeur === undefined ? (
    <Skeleton className='h-9 w-24' />
  ) : (
    <span className='block text-2xl font-bold tabular-nums sm:text-3xl'>
      {valeur}
    </span>
  )
}

/**
 * Une activité : un nombre de choses à faire, et le listing préfiltré où les faire. Deux par ligne sur mobile
 * (`GrilleActivites`) : la carte s'y resserre. En `alerte`, un liseré rouge et l'icône teintée : ce qui presse.
 * Sous deux, le libellé passe au `singulier` (« 0 contrat à approuver », « 1 facture impayée »), s'il diffère.
 */
export function Activite({
  libelle,
  singulier,
  icone: Icone,
  valeur,
  lien,
  demo,
  alerte,
}: {
  libelle: string
  singulier?: string
  icone: LucideIcon
  valeur: number | undefined
  lien?: { to: LinkProps['to']; search?: LinkProps['search'] }
  demo?: boolean
  alerte?: boolean
}) {
  const contenu = (
    <Card
      className={cn(
        'h-full gap-2 py-4 sm:py-5',
        alerte && 'border-destructive'
      )}
    >
      <CardHeader className='flex flex-row items-center justify-between gap-2 px-4 sm:px-5'>
        <span
          className={cn(
            'flex size-9 items-center justify-center rounded-lg',
            alerte
              ? 'bg-danger text-danger-foreground'
              : 'bg-muted text-muted-foreground'
          )}
        >
          <Icone className='size-[18px]' />
        </span>
        {demo ? (
          <Badge
            variant='outline'
            className='font-normal max-sm:px-1.5 max-sm:text-[10px]'
          >
            Démonstration
          </Badge>
        ) : (
          lien && <ChevronRight className='text-muted-foreground size-4' />
        )}
      </CardHeader>
      <CardContent className='space-y-1 px-4 sm:px-5'>
        <Valeur
          valeur={valeur === undefined ? undefined : entier.format(valeur)}
        />
        <p className='text-muted-foreground text-sm'>
          {singulier && valeur !== undefined && valeur < 2
            ? singulier
            : libelle}
        </p>
      </CardContent>
    </Card>
  )
  return lien ? (
    <Link
      to={lien.to}
      search={lien.search}
      className={cn(
        'block rounded-xl transition-colors',
        alerte
          ? 'hover:[&>[data-slot=card]]:border-destructive/70'
          : 'hover:[&>[data-slot=card]]:border-primary/40'
      )}
    >
      {contenu}
    </Link>
  ) : (
    contenu
  )
}
