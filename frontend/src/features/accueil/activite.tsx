import { type ReactNode } from 'react'
import { Link, type LinkProps } from '@tanstack/react-router'
import { ChevronRight, type LucideIcon } from 'lucide-react'
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

/** Un nombre en attente de sa lecture. */
export function Valeur({ valeur }: { valeur: string | undefined }) {
  return valeur === undefined ? (
    <Skeleton className='h-9 w-24' />
  ) : (
    <span className='block text-3xl font-bold tabular-nums'>{valeur}</span>
  )
}

/** Une activité : un nombre de choses à faire, et le listing préfiltré où les faire. */
export function Activite({
  libelle,
  icone: Icone,
  valeur,
  lien,
  demo,
}: {
  libelle: string
  icone: LucideIcon
  valeur: number | undefined
  lien?: { to: LinkProps['to']; search?: LinkProps['search'] }
  demo?: boolean
}) {
  const contenu = (
    <Card className='h-full gap-2 py-5'>
      <CardHeader className='flex flex-row items-center justify-between gap-2 px-5'>
        <span className='bg-muted text-muted-foreground flex size-9 items-center justify-center rounded-lg'>
          <Icone className='size-[18px]' />
        </span>
        {demo ? (
          <Badge variant='outline' className='font-normal'>
            Démonstration
          </Badge>
        ) : (
          lien && <ChevronRight className='text-muted-foreground size-4' />
        )}
      </CardHeader>
      <CardContent className='space-y-1 px-5'>
        <Valeur
          valeur={valeur === undefined ? undefined : entier.format(valeur)}
        />
        <p className='text-muted-foreground text-sm'>{libelle}</p>
      </CardContent>
    </Card>
  )
  return lien ? (
    <Link
      to={lien.to}
      search={lien.search}
      className='hover:[&>[data-slot=card]]:border-primary/40 block rounded-xl transition-colors'
    >
      {contenu}
    </Link>
  ) : (
    contenu
  )
}
