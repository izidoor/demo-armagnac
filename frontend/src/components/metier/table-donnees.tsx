import {
  type CSSProperties,
  Fragment,
  type ReactNode,
  useEffect,
  useState,
} from 'react'
import {
  type Cell,
  type Column,
  type ColumnDef,
  type ColumnFiltersState,
  type ColumnPinningState,
  type ExpandedState,
  flexRender,
  getCoreRowModel,
  getExpandedRowModel,
  getFacetedRowModel,
  getFacetedUniqueValues,
  getFilteredRowModel,
  getGroupedRowModel,
  getPaginationRowModel,
  getSortedRowModel,
  type PaginationState,
  type GroupingState,
  type SortingState,
  useReactTable,
  type VisibilityState,
} from '@tanstack/react-table'
import { ChevronRight, Columns3, RotateCcw, X } from 'lucide-react'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Input } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  Table,
  TableBody,
  TableCell,
  TableFooter,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { CartesListe } from './cartes-liste'
import { CelluleOuvrir, EnteteOuvrir } from './cellule-ouvrir'
import { Pagination } from './pagination'

/** Les tailles de page proposées. */
const TAILLES = [10, 25, 50, 100]

const PREFIXE_MEMOIRE = 'bnia.table.'
/** Le fond de l'en-tête (`TableHeader`), opaque, pour une cellule épinglée sous laquelle la table défile. */
export const FOND_EN_TETE =
  'bg-[color-mix(in_oklab,var(--muted)_50%,var(--background))]'

/** La disposition retenue d'une table : colonnes affichées, épinglées, regroupées. */
type Disposition = {
  visibilite: VisibilityState
  epinglage: ColumnPinningState
  regroupement: GroupingState
  taillePage?: number
}

function lire(cle: string | undefined): Disposition | null {
  if (!cle) return null
  try {
    const texte = window.localStorage.getItem(PREFIXE_MEMOIRE + cle)
    return texte ? (JSON.parse(texte) as Disposition) : null
  } catch {
    return null
  }
}

function retenir(cle: string | undefined, disposition: Disposition) {
  if (!cle) return
  try {
    window.localStorage.setItem(
      PREFIXE_MEMOIRE + cle,
      JSON.stringify(disposition)
    )
  } catch {
    // Mémoire du navigateur indisponible : la disposition vaut pour la visite.
  }
}

/** Une colonne épinglée colle au bord au défilement horizontal ; sa largeur est fixée pour placer les suivantes. */
function styleEpingle<T>(colonne: Column<T, unknown>): CSSProperties {
  const cote = colonne.getIsPinned()
  if (!cote) return {}
  const largeur = colonne.getSize()
  return {
    position: 'sticky',
    left: cote === 'left' ? colonne.getStart('left') : undefined,
    right: cote === 'right' ? colonne.getAfter('right') : undefined,
    width: largeur,
    minWidth: largeur,
    maxWidth: largeur,
    zIndex: 1,
  }
}

const bordEpingle = <T,>(colonne: Column<T, unknown>) =>
  cn(
    colonne.getIsPinned() && 'bg-background',
    colonne.getIsPinned() === 'left' &&
      colonne.getIsLastColumn('left') &&
      'shadow-[inset_-1px_0_0_var(--border)]',
    colonne.getIsPinned() === 'right' &&
      colonne.getIsFirstColumn('right') &&
      'shadow-[inset_1px_0_0_var(--border)]'
  )

/**
 * Table de données brutes : recherche dans toutes les colonnes ; tri, filtre par valeurs, épinglage et
 * regroupement sur chaque colonne (en-têtes `EnteteFiltrable`, filtre `filtreValeurs`) ; nombre de lignes ; total
 * d'une colonne (`colonneTotal`, la dernière par défaut) sur les lignes affichées. Les colonnes chiffrées (`chiffrees`) s'alignent à droite.
 *
 * <ul>
 * <li>Visibilité : le menu « Colonnes » affiche ou masque chaque colonne, sous son titre `meta.titre` ; celles de
 * `masquees` le sont au départ ; la dernière colonne affichée ne se masque pas.</li>
 * <li>Épinglage : une colonne épinglée à gauche ou à droite reste visible au défilement horizontal.</li>
 * <li>Regroupement : les lignes se replient sous chaque valeur des colonnes regroupées, dans l'ordre choisi, avec
 * leur nombre et les sous-totaux des colonnes qui ont une agrégation (`aggregationFn`), mis en forme par
 * `meta.format` ou `aggregatedCell`. Les autres colonnes ne s'agrègent pas.</li>
 * </ul>
 * Sur un contenu étroit (facultatif, comme la liste des opérateurs) : une colonne prend les classes de son
 * `meta.className` (colonne masquée sous un seuil, colonne qui se tronque) ; avec `cartes`, sous un seuil de la largeur
 * du contenu, une carte par ligne (`CarteListe`) remplace la table et le menu « Colonnes », dans l'ordre et sous les
 * filtres de la table.
 *
 * Pagination dans le navigateur, 25 lignes par page au départ (10, 25, 50 ou 100) ; un groupe déplié compte ses
 * lignes dans la page. La disposition (visibilité, épinglage, regroupement, taille de page) est retenue dans le
 * navigateur, par `cle`.
 */
export function TableDonnees<T>({
  cle,
  donnees,
  colonnes,
  chiffrees = [],
  masquees = [],
  chargement = false,
  vide,
  total,
  colonneTotal: colonneTotalDemandee,
  formatTotal = String,
  onLigne,
  libelleLigne = () => 'Ouvrir le détail',
  actions,
  cartes,
}: {
  /** Clé de la mémoire de la disposition, propre à la table ; sans elle, rien n'est retenu. */
  cle?: string
  donnees: T[]
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  colonnes: ColumnDef<T, any>[]
  /** Identifiants des colonnes chiffrées. */
  chiffrees?: string[]
  /** Identifiants des colonnes masquées au départ, que l'utilisateur affiche à son gré. */
  masquees?: string[]
  chargement?: boolean
  vide: string
  /** La valeur à totaliser d'une ligne ; sans elle, pas de ligne de total. */
  total?: (ligne: T) => number
  /** La colonne sous laquelle s'affiche le total ; la dernière par défaut. */
  colonneTotal?: string
  formatTotal?: (valeur: number) => string
  /** Ouvre le détail d'une ligne : la ligne est cliquable et finit par un chevron. */
  onLigne?: (ligne: T) => void
  /** Ce qu'ouvre le chevron d'une ligne, pour les lecteurs d'écran. */
  libelleLigne?: (ligne: T) => string
  /** À droite de la barre de recherche (un export, par exemple). */
  actions?: ReactNode
  /**
   * Une carte par ligne sous un seuil : `carte` rend la ligne (`CarteListe`) ; `tableau` masque la table sous le seuil
   * et `masquee` les cartes au-dessus, écrites en entier pour Tailwind (voir `CartesListe`).
   */
  cartes?: {
    carte: (ligne: T) => ReactNode
    tableau: string
    masquee: string
  }
}) {
  const initiale = (): Disposition => ({
    visibilite: Object.fromEntries(masquees.map((id) => [id, false])),
    epinglage: {},
    regroupement: [],
  })
  const [retenue] = useState(() => lire(cle))
  const [tri, setTri] = useState<SortingState>([])
  const [filtres, setFiltres] = useState<ColumnFiltersState>([])
  const [recherche, setRecherche] = useState('')
  const [deplies, setDeplies] = useState<ExpandedState>({})
  const [pagination, setPagination] = useState<PaginationState>(() => ({
    pageIndex: 0,
    pageSize: retenue?.taillePage ?? 25,
  }))
  const [visibles, setVisibles] = useState<VisibilityState>(
    () => retenue?.visibilite ?? initiale().visibilite
  )
  const [epinglage, setEpinglage] = useState<ColumnPinningState>(
    () => retenue?.epinglage ?? {}
  )
  const [regroupement, setRegroupement] = useState<GroupingState>(
    () => retenue?.regroupement ?? []
  )
  useEffect(() => {
    retenir(cle, {
      visibilite: visibles,
      epinglage,
      regroupement,
      taillePage: pagination.pageSize,
    })
  }, [cle, visibles, epinglage, regroupement, pagination.pageSize])

  // Le React Compiler laisse ce composant hors de sa mémoïsation : TanStack Table rend des fonctions.
  // eslint-disable-next-line react-hooks/incompatible-library
  const table = useReactTable({
    data: donnees,
    columns: colonnes,
    // Seules s'agrègent les colonnes qui le déclarent (pas de somme d'un prix unitaire, par exemple).
    defaultColumn: { aggregationFn: undefined },
    state: {
      sorting: tri,
      columnFilters: filtres,
      globalFilter: recherche,
      columnVisibility: visibles,
      columnPinning: epinglage,
      grouping: regroupement,
      expanded: deplies,
      pagination,
    },
    onPaginationChange: setPagination,
    onSortingChange: setTri,
    onColumnVisibilityChange: setVisibles,
    onColumnPinningChange: setEpinglage,
    onGroupingChange: setRegroupement,
    onExpandedChange: setDeplies,
    onColumnFiltersChange: setFiltres,
    onGlobalFilterChange: setRecherche,
    globalFilterFn: 'includesString',
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getGroupedRowModel: getGroupedRowModel(),
    getExpandedRowModel: getExpandedRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    getFacetedRowModel: getFacetedRowModel(),
    getFacetedUniqueValues: getFacetedUniqueValues(),
  })
  // Les lignes de données retenues par les filtres, hors lignes de groupe.
  const retenues = table.getFilteredRowModel().rows
  const lignes = table.getRowModel().rows
  const filtre = filtres.length > 0 || recherche !== ''
  const somme = total
    ? retenues.reduce((s, r) => s + total(r.original), 0)
    : null
  const chiffree = (id: string) => chiffrees.includes(id)
  const nombreColonnes = table.getVisibleLeafColumns().length
  const regroupee = regroupement.length > 0
  const colonneTotal =
    colonneTotalDemandee ?? table.getAllLeafColumns().slice(-1)[0]?.id

  const contenu = (cell: Cell<T, unknown>) => {
    const row = cell.row
    if (cell.getIsGrouped()) {
      return (
        <button
          type='button'
          onClick={(e) => {
            e.stopPropagation()
            row.toggleExpanded()
          }}
          className='inline-flex items-center gap-1 font-medium'
          style={{ paddingInlineStart: `${row.depth * 1.25}rem` }}
        >
          <ChevronRight
            className={cn(
              'size-4 transition-transform',
              row.getIsExpanded() && 'rotate-90'
            )}
          />
          {flexRender(cell.column.columnDef.cell, cell.getContext())}
          <span className='text-muted-foreground font-normal'>
            ({row.getLeafRows().length})
          </span>
        </button>
      )
    }
    if (cell.getIsAggregated()) {
      const def = cell.column.columnDef
      if (def.aggregatedCell)
        return flexRender(def.aggregatedCell, cell.getContext())
      const v = cell.getValue()
      if (typeof v !== 'number') return null
      return def.meta?.format ? def.meta.format(v) : v
    }
    if (cell.getIsPlaceholder()) return null
    return flexRender(cell.column.columnDef.cell, cell.getContext())
  }

  return (
    <>
      <div className='mb-2 flex flex-wrap items-center gap-2'>
        <Input
          placeholder='Rechercher dans la table…'
          value={recherche}
          onChange={(e) => setRecherche(e.target.value)}
          className='h-8 w-64 max-sm:w-full'
        />
        {filtre && (
          <Button
            variant='ghost'
            size='sm'
            onClick={() => {
              setFiltres([])
              setRecherche('')
            }}
          >
            <X className='size-4' /> Effacer les filtres
          </Button>
        )}
        {regroupee && (
          <>
            <Button
              variant='ghost'
              size='sm'
              onClick={() => table.toggleAllRowsExpanded(true)}
            >
              Tout déplier
            </Button>
            <Button
              variant='ghost'
              size='sm'
              onClick={() => table.toggleAllRowsExpanded(false)}
            >
              Tout replier
            </Button>
          </>
        )}
        <span className='text-muted-foreground ms-auto text-sm'>
          {filtre
            ? `${retenues.length} ligne(s) sur ${donnees.length}`
            : `${donnees.length} ligne(s)`}
        </span>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant='outline' size='sm' className={cartes?.tableau}>
              <Columns3 className='size-4' /> Colonnes
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align='end'>
            <DropdownMenuLabel>Afficher</DropdownMenuLabel>
            <DropdownMenuCheckboxItem
              checked={table.getIsAllColumnsVisible()}
              onCheckedChange={() => table.toggleAllColumnsVisible(true)}
              disabled={table.getIsAllColumnsVisible()}
              onSelect={(e) => e.preventDefault()}
            >
              Tout afficher
            </DropdownMenuCheckboxItem>
            <DropdownMenuSeparator />
            {table
              .getAllLeafColumns()
              .filter((c) => c.getCanHide())
              .map((c) => (
                <DropdownMenuCheckboxItem
                  key={c.id}
                  checked={c.getIsVisible()}
                  disabled={c.getIsVisible() && nombreColonnes === 1}
                  onCheckedChange={(v) => c.toggleVisibility(!!v)}
                  onSelect={(e) => e.preventDefault()}
                >
                  {c.columnDef.meta?.titre ?? c.id}
                </DropdownMenuCheckboxItem>
              ))}
            <DropdownMenuSeparator />
            <DropdownMenuItem
              onSelect={() => {
                const depart = initiale()
                setVisibles(depart.visibilite)
                setEpinglage(depart.epinglage)
                setRegroupement(depart.regroupement)
              }}
            >
              <RotateCcw className='size-4' /> Disposition de départ
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
        {actions}
      </div>
      <div className={cn('overflow-x-auto rounded-md border', cartes?.tableau)}>
        <Table>
          <TableHeader>
            {table.getHeaderGroups().map((groupe) => (
              <TableRow key={groupe.id}>
                {groupe.headers.map((h) => (
                  <TableHead
                    key={h.id}
                    style={styleEpingle(h.column)}
                    className={cn(
                      chiffree(h.column.id) && 'text-right',
                      h.column.columnDef.meta?.className,
                      bordEpingle(h.column),
                      h.column.getIsPinned() && FOND_EN_TETE
                    )}
                  >
                    {flexRender(h.column.columnDef.header, h.getContext())}
                  </TableHead>
                ))}
                {onLigne && <EnteteOuvrir />}
              </TableRow>
            ))}
          </TableHeader>
          <TableBody>
            {lignes.length === 0 && (
              <TableRow>
                <TableCell
                  colSpan={nombreColonnes + (onLigne ? 1 : 0)}
                  className='text-muted-foreground h-16 text-center'
                >
                  {chargement
                    ? 'Chargement…'
                    : filtre
                      ? 'Aucune ligne ne répond aux filtres.'
                      : vide}
                </TableCell>
              </TableRow>
            )}
            {lignes.map((row) => {
              const groupe = row.getIsGrouped()
              return (
                <TableRow
                  key={row.id}
                  className={cn(
                    groupe && 'bg-muted/30',
                    onLigne && !groupe && 'cursor-pointer'
                  )}
                  onClick={
                    groupe
                      ? () => row.toggleExpanded()
                      : onLigne
                        ? () => onLigne(row.original)
                        : undefined
                  }
                >
                  {row.getVisibleCells().map((cell) => (
                    <TableCell
                      key={cell.id}
                      style={styleEpingle(cell.column)}
                      className={cn(
                        chiffree(cell.column.id) && 'text-right tabular-nums',
                        cell.column.columnDef.meta?.className,
                        groupe && cell.getIsAggregated() && 'font-medium',
                        bordEpingle(cell.column),
                        groupe && cell.column.getIsPinned() && 'bg-muted'
                      )}
                    >
                      {contenu(cell)}
                    </TableCell>
                  ))}
                  {onLigne &&
                    (groupe ? (
                      <TableCell />
                    ) : (
                      <CelluleOuvrir
                        libelle={libelleLigne(row.original)}
                        onOuvrir={() => onLigne(row.original)}
                      />
                    ))}
                </TableRow>
              )
            })}
          </TableBody>
          {somme !== null && retenues.length > 0 && (
            <TableFooter>
              <TableRow>
                {table.getVisibleLeafColumns().map((c, i) => (
                  <TableCell
                    key={c.id}
                    style={styleEpingle(c)}
                    className={cn(
                      c.id === colonneTotal && 'text-right tabular-nums',
                      c.columnDef.meta?.className,
                      c.getIsPinned() && 'bg-muted'
                    )}
                  >
                    {c.id === colonneTotal
                      ? formatTotal(somme)
                      : i === 0
                        ? `Total${filtre ? ' des lignes affichées' : ''}`
                        : null}
                  </TableCell>
                ))}
                {onLigne && <TableCell />}
              </TableRow>
            </TableFooter>
          )}
        </Table>
      </div>
      {cartes && (
        <CartesListe
          requete={{
            isPending: chargement,
            isError: false,
            data: lignes,
          }}
          vide={filtre ? 'Aucune ligne ne répond aux filtres.' : vide}
          masquee={cartes.masquee}
        >
          {/* Les lignes de groupe ne se montrent pas en cartes : seules leurs lignes dépliées. */}
          {lignes
            .filter((row) => !row.getIsGrouped())
            .map((row) => (
              <Fragment key={row.id}>{cartes.carte(row.original)}</Fragment>
            ))}
        </CartesListe>
      )}
      <div className='flex flex-wrap items-center justify-end gap-2'>
        {table.getPrePaginationRowModel().rows.length > TAILLES[0] && (
          <div className='flex items-center gap-2 pt-4 text-sm'>
            <span className='text-muted-foreground'>Lignes par page</span>
            <Select
              value={String(pagination.pageSize)}
              onValueChange={(v) => table.setPageSize(Number(v))}
            >
              <SelectTrigger className='h-8 w-20'>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {TAILLES.map((t) => (
                  <SelectItem key={t} value={String(t)}>
                    {t}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        )}
        <div className='grow'>
          <Pagination
            page={pagination.pageIndex + 1}
            pages={table.getPageCount()}
            total={table.getPrePaginationRowModel().rows.length}
            setPage={(page) => table.setPageIndex(page - 1)}
          />
        </div>
      </div>
    </>
  )
}
