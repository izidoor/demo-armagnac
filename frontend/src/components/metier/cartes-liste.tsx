import { type ReactNode } from 'react'
import { Link, type LinkProps } from '@tanstack/react-router'
import { ChevronRight } from 'lucide-react'
import { cn } from '@/lib/utils'

/**
 * Un listing en cartes pleine largeur, à la place de son tableau quand celui-ci ne tient pas.
 *
 * Le seuil porte sur la largeur de la zone de contenu (`@container/content`, posé par `AuthenticatedLayout`), pas sur
 * celle de la fenêtre : il suit la barre latérale, ouverte, réduite en icônes ou en tiroir. Il se mesure par tableau :
 * largeur naturelle du tableau (`width: max-content`) + 34 px de marges de `Main`, arrondie au-dessus avec un peu de
 * marge pour des données plus longues. Le tableau prend la variante `@max-` de ce seuil (`hidden` sous le seuil), les
 * cartes la variante `@min-` (`masquee`), écrites en entier pour que Tailwind les trouve. Un tableau peut aussi perdre
 * des colonnes avant de passer en cartes, sur le même principe : voir la liste des opérateurs.
 *
 * Chargement, erreur et liste vide s'annoncent comme dans le tableau (`LignesEtat`).
 */
export function CartesListe({
  requete,
  vide = 'Aucun élément.',
  masquee,
  children,
}: {
  /** La requête de la liste, ou, pour une liste déjà en main, `{ isPending: false, isError: false, data }`. */
  requete: { isPending: boolean; isError: boolean; data?: unknown[] }
  vide?: string
  /** Les classes qui masquent les cartes quand le tableau tient : `@min-[58rem]/content:hidden`. */
  masquee: string
  children: ReactNode
}) {
  let message: string | null = null
  if (requete.isPending) message = 'Chargement…'
  else if (requete.isError) message = 'Impossible de charger la liste.'
  else if (requete.data?.length === 0) message = vide
  if (message)
    return (
      <p
        className={cn(
          'text-muted-foreground rounded-md border p-4 text-center text-sm',
          masquee
        )}
      >
        {message}
      </p>
    )
  return <ul className={cn('space-y-2', masquee)}>{children}</ul>
}

/** Le titre d'une carte qui s'ouvre : son lien (ou bouton) s'étend à toute la carte. */
const TITRE_OUVRANT =
  'block text-start font-medium after:absolute after:inset-0 after:rounded-lg'

/**
 * Une ligne du listing en carte. Avec `lien` (une route) ou `onOuvrir` (une navigation calculée), le titre ouvre la
 * fiche, et toute la carte avec lui (lien étendu) ; les liens du contenu (téléphone, e-mail) passent devant
 * (`relative z-10`) et restent cliquables. Sans l'un ni l'autre, une carte à lire (une ligne de journal).
 */
export function CarteListe({
  lien,
  onOuvrir,
  titre,
  className,
  children,
}: {
  lien?: { to: LinkProps['to']; params?: LinkProps['params'] }
  onOuvrir?: () => void
  titre: ReactNode
  /** Une teinte de fond, pour une carte qui se distingue (par exemple, une facture échue). */
  className?: string
  children?: ReactNode
}) {
  const ouvrante = lien !== undefined || onOuvrir !== undefined
  return (
    <li
      className={cn(
        'bg-card relative flex items-start gap-2 rounded-lg border p-3 shadow-xs',
        ouvrante && 'hover:border-primary/40 transition-colors',
        className
      )}
    >
      <div className='min-w-0 flex-1 space-y-1'>
        {lien ? (
          <Link to={lien.to} params={lien.params} className={TITRE_OUVRANT}>
            {titre}
          </Link>
        ) : onOuvrir ? (
          <button type='button' onClick={onOuvrir} className={TITRE_OUVRANT}>
            {titre}
          </button>
        ) : (
          <div className='font-medium'>{titre}</div>
        )}
        {children}
      </div>
      {ouvrante && (
        <ChevronRight className='text-muted-foreground mt-0.5 size-4 shrink-0' />
      )}
    </li>
  )
}
