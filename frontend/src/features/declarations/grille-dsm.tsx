import { type KeyboardEvent, type ReactNode } from 'react'
import {
  ArrowDown,
  ArrowUp,
  ArrowUpDown,
  Ban,
  Calculator,
  NotebookPen,
  Plus,
  Trash2,
} from 'lucide-react'
import { type ColonneDsm } from '@/api/generated'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import {
  Table,
  TableBody,
  TableCell,
  TableFooter,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { type LigneBrouillon, type sommes } from './brouillon'
import { formatLitres, lireLitres } from './format'
import { designation, identite, type Onglet } from './libelles'

type Sommes = ReturnType<typeof sommes>

/**
 * Fond des cellules calculées (colonnes 1 et 26, entrées et sorties du récapitulatif). En thème sombre, le fond
 * d'information se confond avec celui de la page : le bleu de sa bordure, plus clair et plus coloré.
 */
const CALCULEE = 'bg-info/60 dark:bg-info-border/50'
/**
 * Les colonnes « Clé » et « Compte d'âge », sous 40rem de grille (`@container/grille`) : masquées, rappelées sous la
 * désignation dans la colonne fixe, pour laisser voir les volumes à côté d'elle sur un téléphone.
 */
const IDENTITE_LARGE = '@max-[40rem]/grille:hidden'
const ENTETE_CALCULEE = 'bg-info dark:bg-info-border/80'
/** Fond hachuré des colonnes sans objet pour un producteur (RG-DEC-16). */
const SANS_OBJET =
  'bg-muted bg-[image:repeating-linear-gradient(135deg,transparent_0_6px,var(--border)_6px_7px)]'

/** Tri des lignes sur une colonne d'identité, commun aux cinq onglets. */
export type TriLignes = {
  colonne: 'designation' | 'cle' | 'compte'
  sens: 'croissant' | 'decroissant'
} | null

const comparaison = new Intl.Collator('fr', {
  numeric: true,
  sensitivity: 'base',
})

/** Les rangs des lignes dans l'ordre d'affichage ; à égalité, l'ordre de saisie. */
function ordonner(
  lignes: LigneBrouillon[],
  comptes: string[],
  tri: TriLignes
): number[] {
  const rangs = lignes.map((_, i) => i)
  if (!tri) return rangs
  const valeur = (i: number) => {
    const l = lignes[i]
    if (tri.colonne === 'cle') return l.cle
    if (tri.colonne === 'compte') return comptes[i] ?? l.compte
    return designation(l)
  }
  const signe = tri.sens === 'croissant' ? 1 : -1
  return rangs.sort(
    (a, b) => signe * comparaison.compare(valeur(a), valeur(b)) || a - b
  )
}

const calculee = (c: ColonneDsm) =>
  c.sens === 'STOCK_DEBUT' || c.sens === 'STOCK_FIN'

/**
 * Déplacement au clavier entre les cellules saisissables, comme dans un tableur : flèches haut et bas,
 * Entrée (vers le bas) et Maj+Entrée (vers le haut) ; flèches gauche et droite quand le curseur est au
 * bord du texte, pour laisser corriger un nombre. La cellule atteinte a son contenu sélectionné.
 */
function naviguer(e: KeyboardEvent<HTMLInputElement>) {
  const champ = e.currentTarget
  const grille = champ.closest('table')
  if (!grille) return
  const champs = (ligne: number) =>
    Array.from(
      grille.querySelectorAll<HTMLInputElement>(`input[data-ligne="${ligne}"]`)
    )
  const ligne = Number(champ.dataset.ligne)
  const colonne = champ.dataset.colonne
  const debut = champ.selectionStart === 0 && champ.selectionEnd === 0
  const fin =
    champ.selectionStart === champ.value.length &&
    champ.selectionEnd === champ.value.length
  let cible: HTMLInputElement | undefined
  if (e.key === 'ArrowDown' || (e.key === 'Enter' && !e.shiftKey)) {
    cible = champs(ligne + 1).find((c) => c.dataset.colonne === colonne)
  } else if (e.key === 'ArrowUp' || (e.key === 'Enter' && e.shiftKey)) {
    cible = champs(ligne - 1).find((c) => c.dataset.colonne === colonne)
  } else if (e.key === 'ArrowRight' && fin) {
    const rangee = champs(ligne)
    cible = rangee[rangee.indexOf(champ) + 1]
  } else if (e.key === 'ArrowLeft' && debut) {
    const rangee = champs(ligne)
    cible = rangee[rangee.indexOf(champ) - 1]
  } else {
    return
  }
  e.preventDefault()
  if (cible) {
    cible.focus()
    cible.select()
  }
}

/**
 * Un onglet du format unique (spec 05 §6.3) : une rangée par ligne, une colonne par colonne de l'onglet,
 * numérotée ; une rangée de totaux. Colonnes 1 et 26 en lecture seule ; colonnes grisées visibles,
 * grisées et vides pour un producteur (RG-DEC-16). Le récapitulatif montre aussi les entrées et les
 * sorties de chaque ligne. Quadrillée comme un tableur : chaque champ remplit sa cellule, sans bordure
 * propre, la cellule active s'encadre, et le clavier mène d'une cellule à l'autre.
 */
export function GrilleDsm({
  onglet,
  colonnes,
  lignes,
  calculs,
  comptes,
  saisissable,
  ligneEnEvidence,
  tri,
  onTri,
  onVolume,
  onAjouter,
  onSupprimer,
  notes: notesParLigne,
  onNotes,
}: {
  onglet: Onglet
  colonnes: ColonneDsm[]
  lignes: LigneBrouillon[]
  calculs: Sommes
  /** Le compte d'âge de chaque ligne, calculé par le serveur pour MB. */
  comptes: string[]
  saisissable: boolean
  ligneEnEvidence: number | null
  tri: TriLignes
  onTri: (tri: TriLignes) => void
  onVolume: (ligne: number, colonne: number, texte: string) => void
  /** Ajout d'une ligne, depuis n'importe quel onglet : les lignes sont communes aux cinq. */
  onAjouter?: () => void
  onSupprimer: (ligne: number) => void
  /** Nombre de notes internes d'une ligne enregistrée, par son identifiant (spec 15 §3.7), au back-office. */
  notes?: Map<number, number>
  /** La pastille d'une ligne annotée mène à ses notes. */
  onNotes?: (ligne: number) => void
}) {
  const recap = onglet === 'RECAPITULATIF'
  const visibles = colonnes.filter((c) => c.onglet === onglet)
  const avecCalculees = recap || visibles.some(calculee)
  const avecSansObjet = visibles.some((c) => c.grisee)
  const ordre = ordonner(lignes, comptes, tri)
  const largeur =
    3 + visibles.length + (recap ? 2 : 0) + (recap && saisissable ? 1 : 0)
  const entete = (
    colonne: NonNullable<TriLignes>['colonne'],
    libelle: ReactNode,
    centre = false
  ) => (
    <EnteteTriable
      libelle={libelle}
      sens={tri?.colonne === colonne ? tri.sens : null}
      centre={centre}
      // Croissant, décroissant, puis l'ordre de saisie.
      onClick={() =>
        onTri(
          tri?.colonne !== colonne
            ? { colonne, sens: 'croissant' }
            : tri.sens === 'croissant'
              ? { colonne, sens: 'decroissant' }
              : null
        )
      }
    />
  )
  return (
    <div className='@container/grille space-y-2'>
      <Legende calculees={avecCalculees} sansObjet={avecSansObjet} />
      <div className='overflow-x-auto rounded-md border'>
        <Table className='[&_td]:border-r [&_th]:border-r [&_tr>*:last-child]:border-r-0'>
          <TableHeader>
            <TableRow className='bg-[color-mix(in_oklab,var(--muted)_50%,var(--background))] hover:bg-[color-mix(in_oklab,var(--muted)_50%,var(--background))] [&>th]:py-2 [&>th]:align-top'>
              <TableHead className='sticky left-0 min-w-40 bg-[color-mix(in_oklab,var(--muted)_50%,var(--background))] whitespace-normal @max-[40rem]/grille:min-w-28'>
                {entete('designation', 'Millésime ou dénomination')}
              </TableHead>
              <TableHead className={IDENTITE_LARGE}>
                {entete('cle', 'Clé')}
              </TableHead>
              <TableHead className={cn('text-center', IDENTITE_LARGE)}>
                {entete(
                  'compte',
                  <>
                    Compte
                    <br />
                    d'âge
                  </>,
                  true
                )}
              </TableHead>
              {visibles.map((c) => (
                <TableHead
                  key={c.numero}
                  className={cn(
                    'h-auto min-w-36 py-2 text-right whitespace-normal @max-[40rem]/grille:min-w-28',
                    calculee(c) && ENTETE_CALCULEE,
                    c.grisee && 'bg-muted-foreground/10 text-muted-foreground'
                  )}
                  title={
                    c.grisee
                      ? 'Sans objet pour un producteur : non modifiable'
                      : calculee(c)
                        ? 'Calculée : non modifiable'
                        : undefined
                  }
                >
                  <div className='flex flex-col items-end gap-1.5'>
                    <span className='text-sm leading-snug font-medium first-letter:uppercase'>
                      {c.libelle}
                    </span>
                    <span
                      className={cn(
                        'rounded-sm px-2 py-0.5 text-sm font-semibold tabular-nums',
                        c.grisee
                          ? 'bg-muted-foreground/15 text-muted-foreground'
                          : 'bg-primary/10 text-primary'
                      )}
                    >
                      {c.numero}
                    </span>
                    {calculee(c) && <Marque nature='calculee' />}
                    {c.grisee && <Marque nature='sans-objet' />}
                  </div>
                </TableHead>
              ))}
              {recap && (
                <>
                  {['Entrées', 'Sorties'].map((libelle) => (
                    <TableHead
                      key={libelle}
                      className={cn('min-w-28 text-right', ENTETE_CALCULEE)}
                      title='Calculée : non modifiable'
                    >
                      <div className='flex flex-col items-end gap-1.5'>
                        <span className='text-sm font-medium'>{libelle}</span>
                        <Marque nature='calculee' />
                      </div>
                    </TableHead>
                  ))}
                </>
              )}
              {recap && saisissable && <TableHead className='w-0' />}
            </TableRow>
          </TableHeader>
          <TableBody>
            {lignes.length === 0 && (
              <TableRow>
                <TableCell
                  colSpan={largeur}
                  className='text-muted-foreground h-16 text-center'
                >
                  Aucune ligne.
                </TableCell>
              </TableRow>
            )}
            {ordre.map((i, rang) => {
              const l = lignes[i]
              return (
                <TableRow
                  key={l.id ?? `nouvelle-${i}`}
                  id={`ligne-${onglet}-${i}`}
                  className={cn(ligneEnEvidence === i && 'bg-warning')}
                >
                  <TableCell className='bg-background sticky left-0 font-medium whitespace-nowrap @max-[40rem]/grille:whitespace-normal'>
                    {designation(l)}
                    <span className='text-muted-foreground block text-xs font-normal @min-[40rem]/grille:hidden'>
                      {l.cle} · compte {comptes[i] ?? l.compte}
                    </span>
                    {l.origine === 'AJOUT' && (
                      <span className='text-muted-foreground ml-1 text-xs'>
                        ajoutée
                      </span>
                    )}
                    {l.contrats?.map((c) => (
                      <span
                        key={c}
                        className='text-muted-foreground block text-xs font-normal'
                      >
                        {c}
                      </span>
                    ))}
                    {l.id != null && (notesParLigne?.get(l.id) ?? 0) > 0 && (
                      <button
                        type='button'
                        className='bg-warning text-warning-foreground hover:bg-warning-border ml-2 inline-flex items-center gap-1 rounded-full px-1.5 py-0.5 align-middle text-xs font-semibold'
                        aria-label={`${notesParLigne?.get(l.id)} note(s) interne(s) sur la ligne ${identite(l)}`}
                        onClick={() => onNotes?.(l.id!)}
                      >
                        <NotebookPen className='size-3' />
                        {notesParLigne?.get(l.id)}
                      </button>
                    )}
                  </TableCell>
                  <TableCell className={cn('font-medium', IDENTITE_LARGE)}>
                    {l.cle}
                  </TableCell>
                  <TableCell className={cn('text-center', IDENTITE_LARGE)}>
                    {comptes[i] ?? l.compte}
                  </TableCell>
                  {visibles.map((c) => {
                    const calcule = calculee(c)
                    const valeur = calculs.lignes[i]?.volumes[c.numero - 1] ?? 0
                    if (calcule || !saisissable || c.grisee) {
                      return (
                        <TableCell
                          key={c.numero}
                          className={cn(
                            'text-right tabular-nums',
                            calcule && CALCULEE,
                            c.grisee && SANS_OBJET,
                            c.sens === 'STOCK_FIN' &&
                              valeur < 0 &&
                              'text-destructive font-semibold'
                          )}
                        >
                          {c.grisee ? '' : formatLitres(valeur)}
                        </TableCell>
                      )
                    }
                    const texte = l.volumes[c.numero - 1]
                    return (
                      <TableCell key={c.numero} className='p-0'>
                        <input
                          className={cn(
                            'focus-visible:ring-primary focus-visible:bg-background h-10 w-full bg-transparent px-2 text-right tabular-nums focus-visible:ring-2 focus-visible:outline-none focus-visible:ring-inset',
                            lireLitres(texte) === null &&
                              'bg-destructive/10 ring-destructive ring-2 ring-inset'
                          )}
                          inputMode='decimal'
                          aria-label={`${identite(l)}, colonne ${c.numero}`}
                          data-ligne={rang}
                          data-colonne={c.numero}
                          value={texte}
                          onChange={(e) =>
                            onVolume(i, c.numero, e.target.value)
                          }
                          onKeyDown={naviguer}
                        />
                      </TableCell>
                    )
                  })}
                  {recap && (
                    <>
                      <TableCell
                        className={cn('text-right tabular-nums', CALCULEE)}
                      >
                        {formatLitres(calculs.lignes[i]?.entrees)}
                      </TableCell>
                      <TableCell
                        className={cn('text-right tabular-nums', CALCULEE)}
                      >
                        {formatLitres(calculs.lignes[i]?.sorties)}
                      </TableCell>
                    </>
                  )}
                  {recap && saisissable && (
                    <TableCell>
                      {l.origine === 'AJOUT' && (
                        <Button
                          variant='ghost'
                          size='icon'
                          aria-label={`Supprimer la ligne ${identite(l)}`}
                          onClick={() => onSupprimer(i)}
                        >
                          <Trash2 className='size-4' />
                        </Button>
                      )}
                    </TableCell>
                  )}
                </TableRow>
              )
            })}
            {onAjouter && (
              <TableRow className='hover:bg-transparent'>
                <TableCell className='bg-background sticky left-0 p-0'>
                  <Button
                    variant='ghost'
                    className='text-primary h-10 w-full justify-start rounded-none px-3 text-base'
                    onClick={onAjouter}
                  >
                    <Plus className='size-4' /> Ajouter une ligne
                  </Button>
                </TableCell>
                {/* Cellules vides : le quadrillage se poursuit sous chaque colonne. */}
                <TableCell className={IDENTITE_LARGE} />
                <TableCell className={IDENTITE_LARGE} />
                {visibles.map((c) => (
                  <TableCell
                    key={c.numero}
                    className={cn(
                      calculee(c) && CALCULEE,
                      c.grisee && SANS_OBJET
                    )}
                  />
                ))}
                {recap && (
                  <>
                    <TableCell className={CALCULEE} />
                    <TableCell className={CALCULEE} />
                  </>
                )}
                {recap && saisissable && <TableCell />}
              </TableRow>
            )}
          </TableBody>
          <TableFooter className='bg-muted'>
            <TableRow className='hover:bg-muted'>
              <TableCell className='bg-muted sticky left-0'>Total</TableCell>
              <TableCell className={IDENTITE_LARGE} />
              <TableCell className={IDENTITE_LARGE} />
              {visibles.map((c) => (
                <TableCell
                  key={c.numero}
                  className={cn(
                    'text-right tabular-nums',
                    c.grisee && SANS_OBJET
                  )}
                >
                  {c.grisee ? '' : formatLitres(calculs.totaux[c.numero - 1])}
                </TableCell>
              ))}
              {recap && (
                <>
                  <TableCell className='text-right tabular-nums'>
                    {formatLitres(
                      calculs.lignes.reduce((s, l) => s + l.entrees, 0)
                    )}
                  </TableCell>
                  <TableCell className='text-right tabular-nums'>
                    {formatLitres(
                      calculs.lignes.reduce((s, l) => s + l.sorties, 0)
                    )}
                  </TableCell>
                </>
              )}
              {recap && saisissable && <TableCell />}
            </TableRow>
          </TableFooter>
        </Table>
      </div>
      <Raccourcis saisissable={saisissable} />
    </div>
  )
}

/** Les raccourcis de la grille, sous elle ; le tri vaut aussi en lecture seule. */
function Raccourcis({ saisissable }: { saisissable: boolean }) {
  return (
    <p className='text-muted-foreground flex flex-wrap gap-x-5 gap-y-1 text-xs'>
      {saisissable && (
        <>
          <span className='pointer-coarse:hidden'>
            <Touche>↑</Touche> <Touche>↓</Touche> <Touche>Entrée</Touche> :
            cellule du dessus ou du dessous
          </span>
          <span className='pointer-coarse:hidden'>
            <Touche>←</Touche> <Touche>→</Touche> : cellule voisine, depuis le
            bord du texte
          </span>
          <span className='pointer-coarse:hidden'>
            <Touche>Tab</Touche> : cellule suivante
          </span>
        </>
      )}
      <span className='@max-[40rem]/grille:hidden'>
        Clic sur « Millésime ou dénomination », « Clé » ou « Compte d'âge » :
        trier les lignes
      </span>
      <span className='@min-[40rem]/grille:hidden'>
        « Millésime ou dénomination » : trier les lignes
      </span>
    </p>
  )
}

function Touche({ children }: { children: ReactNode }) {
  return (
    <kbd className='bg-muted rounded-sm border px-1 font-mono text-[0.7rem]'>
      {children}
    </kbd>
  )
}

/** Un en-tête de colonne d'identité, qui trie les lignes au clic. */
function EnteteTriable({
  libelle,
  sens,
  centre,
  onClick,
}: {
  libelle: ReactNode
  sens: 'croissant' | 'decroissant' | null
  centre: boolean
  onClick: () => void
}) {
  const Icone =
    sens === 'croissant'
      ? ArrowUp
      : sens === 'decroissant'
        ? ArrowDown
        : ArrowUpDown
  return (
    <button
      type='button'
      className={cn(
        'hover:text-foreground inline-flex items-start gap-1 text-left font-medium',
        centre && 'text-center',
        sens && 'text-foreground'
      )}
      title={
        sens === 'croissant'
          ? 'Trié par ordre croissant ; cliquer pour l’ordre décroissant'
          : sens === 'decroissant'
            ? 'Trié par ordre décroissant ; cliquer pour revenir à l’ordre de saisie'
            : 'Trier par ordre croissant'
      }
      onClick={onClick}
    >
      <span>{libelle}</span>
      <Icone
        className={cn('mt-0.5 size-3.5 shrink-0', !sens && 'opacity-40')}
      />
    </button>
  )
}

/** La mention d'une colonne non modifiable, sous son numéro. */
function Marque({ nature }: { nature: 'calculee' | 'sans-objet' }) {
  return nature === 'calculee' ? (
    <span className='text-info-foreground inline-flex items-center gap-1 text-xs font-normal'>
      <Calculator className='size-3' /> calculée
    </span>
  ) : (
    <span className='text-muted-foreground inline-flex items-center gap-1 text-xs font-normal'>
      <Ban className='size-3' /> sans objet
    </span>
  )
}

/** Ce que signifient les fonds de la grille. */
function Legende({
  calculees,
  sansObjet,
}: {
  calculees: boolean
  sansObjet: boolean
}) {
  // Sans colonne calculée ni sans objet, toutes se saisissent : rien à distinguer.
  if (!calculees && !sansObjet) return null
  const pastille = 'inline-block size-3.5 rounded-xs border'
  return (
    <div className='text-muted-foreground flex flex-wrap gap-x-5 gap-y-1 text-sm'>
      <span className='inline-flex items-center gap-1.5'>
        <span className={cn(pastille, 'bg-background')} /> À saisir
      </span>
      {calculees && (
        <span className='inline-flex items-center gap-1.5'>
          <span className={cn(pastille, CALCULEE)} /> Calculée, non modifiable
        </span>
      )}
      {sansObjet && (
        <span className='inline-flex items-center gap-1.5'>
          <span className={cn(pastille, SANS_OBJET)} /> Sans objet pour un
          producteur, non modifiable
        </span>
      )}
    </div>
  )
}
