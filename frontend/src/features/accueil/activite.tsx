import { createContext, type ReactNode, useContext } from 'react'
import { Link, type LinkProps } from '@tanstack/react-router'
import {
  ArrowRight,
  ChevronRight,
  type LucideIcon,
  TriangleAlert,
} from 'lucide-react'
import { cn } from '@/lib/utils'
import { Card } from '@/components/ui/card'
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

/**
 * La présentation des activités d'un thème (`Theme`) : en cellules de la carte du thème, sur sa ligne (contenu large),
 * ou en lignes d'une liste, dans la carte du thème (contenu étroit), avec la couleur du thème. Hors thème : une carte
 * à part.
 */
const Presentation = createContext<{
  mode: 'carte' | 'ligne'
  couleur?: string
  cellule?: boolean
}>({ mode: 'carte' })

const teinte = (couleur: string, part: number, fond = 'transparent') =>
  `color-mix(in oklch, ${couleur} ${part}%, ${fond})`

/**
 * Les activités rangées par thème (`Theme`). Contenu large (56rem) : une carte par thème sur une grille de quatre
 * colonnes, son nom dans la première, ses activités dans les suivantes, alignées d'un thème à l'autre ; plus étroit :
 * une carte par thème, ses activités en liste, deux cartes par ligne dès 36rem. Les thèmes sont rendus une fois par
 * présentation, chacune masquée hors de son seuil (les lectures, partagées par TanStack Query, ne partent qu'une fois).
 */
export function ThemesActivites({ children }: { children: ReactNode }) {
  return (
    <>
      <Presentation.Provider value={{ mode: 'ligne' }}>
        <div className='grid items-start gap-4 @min-[36rem]/content:grid-cols-2 @min-[56rem]/content:hidden'>
          {children}
        </div>
      </Presentation.Provider>
      <Presentation.Provider value={{ mode: 'carte' }}>
        <div className='grid grid-cols-4 gap-5 @max-[56rem]/content:hidden'>
          {children}
        </div>
      </Presentation.Provider>
    </>
  )
}

type LienTheme = {
  to: LinkProps['to']
  search?: LinkProps['search']
  libelle: string
}

const LARGEUR = { 1: 'col-span-2', 2: 'col-span-3', 3: 'col-span-4' }

/**
 * Un thème d'activités (DSM, export, facturation…) : sur contenu large, une carte sobre, son nom, son icône à la
 * couleur du thème et le lien vers son module dans la première cellule, ses activités dans les suivantes, séparées
 * d'un filet ; sur contenu étroit, une carte dont l'en-tête teinté porte le nom et le lien, et les activités une
 * liste. Sur contenu large, la carte prend autant de colonnes que ses `colonnes` d'activités (trois au plus, par
 * défaut), plus la sienne : deux thèmes d'une activité partagent une ligne.
 */
export function Theme({
  titre,
  icone: Icone,
  couleur,
  lien,
  colonnes = 3,
  children,
}: {
  titre: string
  icone: LucideIcon
  couleur: string
  lien?: LienTheme
  colonnes?: 1 | 2 | 3
  children: ReactNode
}) {
  const { mode } = useContext(Presentation)
  if (mode === 'ligne')
    return (
      <Card className='gap-0 overflow-hidden py-0'>
        <div
          className='flex items-center gap-3 border-b px-4 py-3'
          style={{ background: teinte(couleur, 10, 'var(--card)') }}
        >
          <span
            className='flex size-9 shrink-0 items-center justify-center rounded-lg text-white'
            style={{ background: couleur }}
          >
            <Icone className='size-[18px]' />
          </span>
          <h3 className='min-w-0 flex-1 leading-snug font-semibold'>{titre}</h3>
          {lien && (
            <Link
              to={lien.to}
              search={lien.search}
              className='text-muted-foreground hover:text-foreground -my-2 shrink-0 py-2 text-sm'
            >
              Tout voir
            </Link>
          )}
        </div>
        <div className='divide-y'>
          <Presentation.Provider value={{ mode, couleur }}>
            {children}
          </Presentation.Provider>
        </div>
      </Card>
    )
  return (
    <Card
      className={cn(
        'grid grid-cols-subgrid gap-0 overflow-hidden py-0',
        LARGEUR[colonnes]
      )}
    >
      <div className='bg-muted/40 flex flex-col gap-4 p-6'>
        <Icone className='size-5' style={{ color: couleur }} />
        <div className='mt-auto space-y-1.5'>
          <h3 className='leading-snug font-semibold'>{titre}</h3>
          {lien && (
            <Link
              to={lien.to}
              search={lien.search}
              className='text-muted-foreground hover:text-foreground inline-flex items-center gap-1 text-sm'
            >
              {lien.libelle} <ArrowRight className='size-3.5' />
            </Link>
          )}
        </div>
      </div>
      <Presentation.Provider value={{ mode, couleur, cellule: true }}>
        {children}
      </Presentation.Provider>
    </Card>
  )
}

/** Un nombre en attente de sa lecture, suivi de son `unite`, plus discrète, d'un bloc. */
export function Valeur({
  valeur,
  unite,
}: {
  valeur: string | undefined
  unite?: string
}) {
  return valeur === undefined ? (
    <Skeleton className='h-9 w-24' />
  ) : (
    <span className='block text-2xl font-semibold tabular-nums sm:text-3xl'>
      {valeur}
      {unite && (
        <span className='text-muted-foreground ml-1.5 text-base font-normal whitespace-nowrap'>
          {unite}
        </span>
      )}
    </span>
  )
}

/**
 * Une activité : un nombre de choses à faire, et le listing préfiltré où les faire. Dans un thème (`Theme`), cellule
 * de sa carte ou ligne de sa liste, l'icône de la ligne à la couleur du thème ; hors thème, une carte à part (deux par
 * ligne sur mobile, la carte s'y resserre). En cellule ou en carte, sobre : icône discrète, nombre, libellé en gris.
 * En `alerte`, le nombre en rouge et un triangle « Attention » à côté : ce qui presse.
 * Sous deux, le libellé passe au `singulier` (« 0 contrat à approuver », « 1 facture impayée »), s'il diffère. Une
 * `valeur` textuelle nomme ce dont le libellé dit l'état (« Otentik », « Service opérationnel »), précédée
 * d'un point vert si `pastille` ; en liste, elle passe en titre de la ligne, le point à droite ; sans valeur (`null`), le
 * libellé seul, suivi de l'`action` du lien (« Voir tous les contrats »). Hors thème, un `titre` (« Messagerie »)
 * coiffe la carte, à côté de son icône, à sa `couleur`, comme le nom d'un thème.
 */
export function Activite({
  titre,
  couleur: couleurTitre,
  libelle,
  singulier,
  icone: Icone,
  valeur,
  lien,
  demo,
  alerte,
  pastille,
  action,
}: {
  titre?: string
  couleur?: string
  libelle: string
  singulier?: string
  icone: LucideIcon
  valeur: number | string | null | undefined
  lien?: { to: LinkProps['to']; search?: LinkProps['search'] }
  demo?: boolean
  alerte?: boolean
  pastille?: boolean
  action?: string
}) {
  const { mode, couleur, cellule } = useContext(Presentation)
  const texte =
    singulier && typeof valeur === 'number' && valeur < 2 ? singulier : libelle
  const affichee = typeof valeur === 'number' ? entier.format(valeur) : valeur
  const point = pastille && (
    <span className='bg-success-point size-2.5 shrink-0 rounded-full' />
  )
  if (mode === 'ligne') {
    const etat = typeof valeur === 'string'
    const precision = [
      etat && texte,
      action ?? (demo && 'Démonstration'),
    ].filter(Boolean)
    const ligne = (
      <div className='flex items-center gap-3 px-4 py-3'>
        <span
          className={cn(
            'flex size-9 shrink-0 items-center justify-center rounded-lg',
            !couleur && 'bg-muted text-muted-foreground'
          )}
          style={
            couleur
              ? { background: teinte(couleur, 15), color: couleur }
              : undefined
          }
        >
          <Icone className='size-[18px]' />
        </span>
        <div className='min-w-0 flex-1'>
          <p className='text-[0.9375rem] leading-snug font-medium'>
            {etat ? valeur : texte}
          </p>
          {precision.length > 0 && (
            <p className='text-muted-foreground text-xs'>
              {precision.join(' · ')}
            </p>
          )}
        </div>
        {etat ? (
          pastille && (
            <span className='bg-success-point size-2.5 shrink-0 rounded-full' />
          )
        ) : affichee === null ? null : affichee === undefined ? (
          <Skeleton className='h-6 w-10' />
        ) : (
          <span
            className={cn(
              'flex items-center gap-1.5 text-xl font-bold whitespace-nowrap tabular-nums',
              alerte && 'text-danger-foreground'
            )}
          >
            {alerte && (
              <TriangleAlert
                aria-label='Attention'
                className='text-destructive size-5'
              />
            )}
            {affichee}
          </span>
        )}
        <ChevronRight
          className={cn(
            'text-muted-foreground size-4 shrink-0',
            !lien && 'invisible'
          )}
        />
      </div>
    )
    return lien ? (
      <Link
        to={lien.to}
        search={lien.search}
        className='hover:bg-muted/60 block transition-colors'
      >
        {ligne}
      </Link>
    ) : (
      ligne
    )
  }
  const corps = (
    <div
      className={cn(
        'flex h-full flex-col gap-4',
        cellule ? 'border-l p-6' : 'p-4 sm:p-5',
        lien && 'hover:bg-muted/50 transition-colors'
      )}
    >
      <div className='text-muted-foreground flex items-center justify-between gap-2'>
        {titre ? (
          <h3 className='text-foreground flex min-w-0 items-center gap-2 leading-snug font-semibold'>
            <Icone
              className='size-5 shrink-0'
              style={{ color: couleurTitre }}
            />
            {titre}
          </h3>
        ) : (
          <Icone className='size-[18px]' />
        )}
        {demo ? (
          <span className='text-xs max-sm:text-[10px]'>Démonstration</span>
        ) : (
          lien && <ChevronRight className='size-4' />
        )}
      </div>
      <div className='mt-auto space-y-1'>
        {affichee === null ? (
          <>
            <p className='leading-snug font-semibold sm:text-lg'>{texte}</p>
            {action && (
              <p className='text-muted-foreground inline-flex items-center gap-1 text-sm'>
                {action} <ArrowRight className='size-3.5' />
              </p>
            )}
          </>
        ) : (
          <>
            {affichee === undefined ? (
              <Valeur valeur={undefined} />
            ) : (
              <span
                className={cn(
                  'flex items-center gap-2 font-semibold tabular-nums',
                  typeof valeur === 'string'
                    ? 'h-9 text-xl'
                    : 'text-2xl sm:text-3xl',
                  alerte && 'text-danger-foreground'
                )}
              >
                {point}
                {affichee}
                {alerte && (
                  <TriangleAlert
                    aria-label='Attention'
                    className='text-destructive size-5'
                  />
                )}
              </span>
            )}
            <p className='text-muted-foreground text-sm leading-snug'>
              {texte}
            </p>
          </>
        )}
      </div>
    </div>
  )
  const contenu = cellule ? (
    corps
  ) : (
    <Card className='h-full gap-0 overflow-hidden py-0'>{corps}</Card>
  )
  return lien ? (
    <Link to={lien.to} search={lien.search} className='block rounded-xl'>
      {contenu}
    </Link>
  ) : (
    contenu
  )
}
