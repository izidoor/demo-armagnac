import {
  createContext,
  type CSSProperties,
  type ReactNode,
  useContext,
} from 'react'
import { Link, type LinkProps } from '@tanstack/react-router'
import {
  ArrowRight,
  ChevronRight,
  type LucideIcon,
  TriangleAlert,
} from 'lucide-react'
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

/**
 * La présentation des activités d'un thème (`Theme`) : en cartes, sur la ligne du thème (contenu large), ou en lignes
 * d'une liste, dans la carte du thème (contenu étroit) ; avec la couleur du thème. Hors thème : en cartes, sans couleur.
 */
const Presentation = createContext<{
  mode: 'carte' | 'ligne'
  couleur?: string
}>({ mode: 'carte' })

const teinte = (couleur: string, part: number, fond = 'transparent') =>
  `color-mix(in oklch, ${couleur} ${part}%, ${fond})`

/** Au survol, une carte cliquable prend le fond `--survol`, comme une ligne de tableau : prête à être ouverte. */
const SURVOL = 'transition-colors hover:bg-(--survol)'

/**
 * Les activités rangées par thème (`Theme`). Contenu large (56rem) : une ligne de quatre colonnes par thème, la
 * première carte, teintée, portant son nom ; plus étroit : une carte par thème, ses activités en liste, deux cartes
 * par ligne dès 36rem. Les thèmes sont rendus une fois par présentation, chacune masquée hors de son seuil (les
 * lectures, partagées par TanStack Query, ne partent qu'une fois).
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
        <div className='grid grid-cols-4 gap-4 @max-[56rem]/content:hidden'>
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

/**
 * Un thème d'activités (DSM, export, facturation…), à sa couleur : en tête de sa ligne, une carte teintée à son nom,
 * avec le lien vers son module ; sur contenu étroit, une carte dont l'en-tête teinté porte le nom et le lien, et les
 * activités une liste. Un thème d'une seule activité peut suivre le précédent sur sa ligne (`suite`).
 */
export function Theme({
  titre,
  icone: Icone,
  couleur,
  lien,
  suite,
  children,
}: {
  titre: string
  icone: LucideIcon
  couleur: string
  lien?: LienTheme
  suite?: boolean
  children: ReactNode
}) {
  const { mode } = useContext(Presentation)
  const activites = (
    <Presentation.Provider value={{ mode, couleur }}>
      {children}
    </Presentation.Provider>
  )
  const pastille = (
    <span
      className='flex size-9 shrink-0 items-center justify-center rounded-lg text-white'
      style={{ background: couleur }}
    >
      <Icone className='size-[18px]' />
    </span>
  )
  if (mode === 'ligne')
    return (
      <Card className='gap-0 overflow-hidden py-0'>
        <div
          className='flex items-center gap-3 border-b px-4 py-3'
          style={{ background: teinte(couleur, 10, 'var(--card)') }}
        >
          {pastille}
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
        <div className='divide-y'>{activites}</div>
      </Card>
    )
  return (
    <>
      <Card
        className={cn(
          'relative gap-3 overflow-hidden py-5',
          !suite && 'col-start-1'
        )}
        style={{
          background: teinte(couleur, 9, 'var(--card)'),
          borderColor: teinte(couleur, 30, 'var(--border)'),
        }}
      >
        <CardHeader className='px-5'>{pastille}</CardHeader>
        <CardContent className='mt-auto space-y-2 px-5'>
          <h3 className='text-lg leading-snug font-semibold'>{titre}</h3>
          {lien && (
            <Link
              to={lien.to}
              search={lien.search}
              className='text-muted-foreground hover:text-foreground relative z-10 inline-flex items-center gap-1 text-sm'
            >
              {lien.libelle} <ArrowRight className='size-3.5' />
            </Link>
          )}
        </CardContent>
        <Icone
          aria-hidden
          className='pointer-events-none absolute -right-5 -bottom-5 size-28 opacity-[0.08]'
          style={{ color: couleur }}
        />
      </Card>
      {activites}
    </>
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
 * Une activité : un nombre de choses à faire, et le listing préfiltré où les faire. Deux par ligne sur mobile, la
 * carte s'y resserre ; dans un thème (`Theme`), carte de sa ligne ou ligne de sa liste, l'icône
 * à la couleur du thème. En `alerte`, un liseré rouge (un nombre rouge en liste) et un triangle « Attention » à côté
 * du nombre : ce qui presse ; hors thème, l'icône aussi passe au rouge.
 * Sous deux, le libellé passe au `singulier` (« 0 contrat à approuver », « 1 facture impayée »), s'il diffère. Une
 * `valeur` textuelle nomme ce dont le libellé dit l'état (« Otentik », « Service opérationnel »), précédée
 * d'un point vert si `pastille` ; en liste, elle passe en titre de la ligne, le point à droite ; sans valeur (`null`), le
 * libellé seul, suivi de l'`action` du lien (« Voir tous les contrats »). Hors thème, une `couleur` propre teinte
 * l'icône comme dans un thème.
 */
export function Activite({
  libelle,
  singulier,
  icone: Icone,
  valeur,
  lien,
  demo,
  alerte,
  pastille,
  action,
  couleur: couleurPropre,
}: {
  libelle: string
  singulier?: string
  icone: LucideIcon
  valeur: number | string | null | undefined
  lien?: { to: LinkProps['to']; search?: LinkProps['search'] }
  demo?: boolean
  alerte?: boolean
  pastille?: boolean
  action?: string
  couleur?: string
}) {
  const presentation = useContext(Presentation)
  const mode = presentation.mode
  const couleur = couleurPropre ?? presentation.couleur
  const texte =
    singulier && typeof valeur === 'number' && valeur < 2 ? singulier : libelle
  const affichee = typeof valeur === 'number' ? entier.format(valeur) : valeur
  const point = pastille && (
    <span className='bg-success-point mr-2 inline-block size-2.5 rounded-full align-middle' />
  )
  const icone = (
    <span
      className={cn(
        'flex size-9 shrink-0 items-center justify-center rounded-lg',
        !couleur &&
          (alerte
            ? 'bg-danger text-danger-foreground'
            : 'bg-muted text-muted-foreground')
      )}
      style={
        couleur
          ? { background: teinte(couleur, 15), color: couleur }
          : undefined
      }
    >
      <Icone className='size-[18px]' />
    </span>
  )
  if (mode === 'ligne') {
    const etat = typeof valeur === 'string'
    const precision = [
      etat && texte,
      action ?? (demo && 'Démonstration'),
    ].filter(Boolean)
    const ligne = (
      <div className='flex items-center gap-3 px-4 py-3'>
        {icone}
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
  const contenu = (
    <Card
      className={cn(
        'h-full gap-2 py-4 sm:py-5',
        lien && SURVOL,
        alerte && 'border-destructive'
      )}
      style={
        lien
          ? ({
              '--survol': teinte(
                alerte ? 'var(--destructive)' : (couleur ?? 'var(--primary)'),
                alerte ? 5 : 7,
                'var(--card)'
              ),
            } as CSSProperties)
          : undefined
      }
    >
      <CardHeader className='flex flex-row items-center justify-between gap-2 px-4 sm:px-5'>
        {icone}
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
        {affichee === null ? (
          <>
            <p className='text-lg leading-snug font-semibold'>{texte}</p>
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
                  'flex items-center font-bold tabular-nums',
                  typeof valeur === 'string'
                    ? 'h-9 text-xl font-semibold sm:text-2xl'
                    : 'text-2xl sm:text-3xl'
                )}
              >
                {point}
                {affichee}
                {alerte && (
                  <TriangleAlert
                    aria-label='Attention'
                    className='text-destructive ml-auto size-7 sm:size-8'
                  />
                )}
              </span>
            )}
            <p className='text-sm leading-snug font-medium sm:text-base'>
              {texte}
            </p>
          </>
        )}
      </CardContent>
    </Card>
  )
  return lien ? (
    <Link to={lien.to} search={lien.search} className='block rounded-xl'>
      {contenu}
    </Link>
  ) : (
    contenu
  )
}
