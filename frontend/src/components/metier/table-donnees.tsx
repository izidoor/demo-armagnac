import { type ReactNode, useState } from 'react'
import {
  type ColumnDef,
  type ColumnFiltersState,
  flexRender,
  getCoreRowModel,
  getFacetedRowModel,
  getFacetedUniqueValues,
  getFilteredRowModel,
  getSortedRowModel,
  type SortingState,
  useReactTable,
} from '@tanstack/react-table'
import { X } from 'lucide-react'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  Table,
  TableBody,
  TableCell,
  TableFooter,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'

/**
 * Table de données brutes : recherche dans toutes les colonnes, tri et filtre par valeurs sur chaque colonne
 * (en-têtes `EnteteFiltrable`, filtre `filtreValeurs`), nombre de lignes, total de la dernière colonne sur
 * les lignes affichées. Les colonnes chiffrées (`chiffrees`) s'alignent à droite.
 */
export function TableDonnees<T>({
  donnees,
  colonnes,
  chiffrees = [],
  chargement = false,
  vide,
  total,
  formatTotal = String,
  onLigne,
  actions,
}: {
  donnees: T[]
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  colonnes: ColumnDef<T, any>[]
  /** Identifiants des colonnes chiffrées. */
  chiffrees?: string[]
  chargement?: boolean
  vide: string
  /** La valeur à totaliser d'une ligne ; sans elle, pas de ligne de total. */
  total?: (ligne: T) => number
  formatTotal?: (valeur: number) => string
  onLigne?: (ligne: T) => void
  /** À droite de la barre de recherche (un export, par exemple). */
  actions?: ReactNode
}) {
  const [tri, setTri] = useState<SortingState>([])
  const [filtres, setFiltres] = useState<ColumnFiltersState>([])
  const [recherche, setRecherche] = useState('')
  // Le React Compiler laisse ce composant hors de sa mémoïsation : TanStack Table rend des fonctions.
  // eslint-disable-next-line react-hooks/incompatible-library
  const table = useReactTable({
    data: donnees,
    columns: colonnes,
    state: { sorting: tri, columnFilters: filtres, globalFilter: recherche },
    onSortingChange: setTri,
    onColumnFiltersChange: setFiltres,
    onGlobalFilterChange: setRecherche,
    globalFilterFn: 'includesString',
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getFacetedRowModel: getFacetedRowModel(),
    getFacetedUniqueValues: getFacetedUniqueValues(),
  })
  const lignes = table.getRowModel().rows
  const filtre = filtres.length > 0 || recherche !== ''
  const somme = total ? lignes.reduce((s, r) => s + total(r.original), 0) : null
  const chiffree = (id: string) => chiffrees.includes(id)

  return (
    <>
      <div className='mb-2 flex flex-wrap items-center gap-2'>
        <Input
          placeholder='Rechercher dans la table…'
          value={recherche}
          onChange={(e) => setRecherche(e.target.value)}
          className='h-8 w-64'
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
        <span className='text-muted-foreground ms-auto text-sm'>
          {filtre
            ? `${lignes.length} ligne(s) sur ${donnees.length}`
            : `${donnees.length} ligne(s)`}
        </span>
        {actions}
      </div>
      <div className='rounded-md border'>
        <Table>
          <TableHeader>
            {table.getHeaderGroups().map((groupe) => (
              <TableRow key={groupe.id}>
                {groupe.headers.map((h) => (
                  <TableHead
                    key={h.id}
                    className={cn(chiffree(h.column.id) && 'text-right')}
                  >
                    {flexRender(h.column.columnDef.header, h.getContext())}
                  </TableHead>
                ))}
              </TableRow>
            ))}
          </TableHeader>
          <TableBody>
            {lignes.length === 0 && (
              <TableRow>
                <TableCell
                  colSpan={colonnes.length}
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
            {lignes.map((row) => (
              <TableRow
                key={row.id}
                className={cn(onLigne && 'cursor-pointer')}
                onClick={onLigne ? () => onLigne(row.original) : undefined}
              >
                {row.getVisibleCells().map((cell) => (
                  <TableCell
                    key={cell.id}
                    className={cn(
                      chiffree(cell.column.id) && 'text-right tabular-nums'
                    )}
                  >
                    {flexRender(cell.column.columnDef.cell, cell.getContext())}
                  </TableCell>
                ))}
              </TableRow>
            ))}
          </TableBody>
          {somme !== null && lignes.length > 0 && (
            <TableFooter>
              <TableRow>
                <TableCell colSpan={colonnes.length - 1}>
                  Total{filtre ? ' des lignes affichées' : ''}
                </TableCell>
                <TableCell className='text-right tabular-nums'>
                  {formatTotal(somme)}
                </TableCell>
              </TableRow>
            </TableFooter>
          )}
        </Table>
      </div>
    </>
  )
}
