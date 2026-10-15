import { useMemo, useState } from 'react'
import {
  type ColumnDef,
  type ExpandedState,
  flexRender,
  getCoreRowModel,
  getExpandedRowModel,
  getSortedRowModel,
  type SortingState,
  useReactTable,
} from '@tanstack/react-table'
import { ArrowDown, ArrowUp, ChevronRight } from 'lucide-react'
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
import { type Croisement, type Noeud } from './calcul'

const TOTAL = '__total'

const afficherAvec = (
  format: (valeur: number) => string,
  v: number | null | undefined
) => (v === null || v === undefined ? '' : format(v))

/**
 * Le tableau croisé : niveaux de lignes dépliables avec leurs sous-totaux, une colonne par valeur croisée,
 * total à droite et en bas quand ils se cumulent ; tri par en-tête, à chaque niveau.
 */
export function TableauCroiseVue({
  croisement,
  titreLignes,
  format,
}: {
  croisement: Croisement
  titreLignes: string
  format: (valeur: number) => string
}) {
  const [tri, setTri] = useState<SortingState>([])
  const [deplies, setDeplies] = useState<ExpandedState>(true)
  const afficher = (v: number | null | undefined) => afficherAvec(format, v)

  const colonnes = useMemo<ColumnDef<Noeud>[]>(
    () => [
      {
        id: 'libelle',
        header: titreLignes,
        accessorFn: (n) => n.rang,
        cell: ({ row }) => (
          <div
            className='flex items-center gap-1'
            style={{ paddingInlineStart: `${row.depth * 1.25}rem` }}
          >
            {row.getCanExpand() ? (
              <button
                type='button'
                onClick={row.getToggleExpandedHandler()}
                className='text-muted-foreground hover:text-foreground rounded-sm'
                aria-label={row.getIsExpanded() ? 'Replier' : 'Déplier'}
              >
                <ChevronRight
                  className={cn(
                    'size-4 transition-transform',
                    row.getIsExpanded() && 'rotate-90'
                  )}
                />
              </button>
            ) : (
              <span className='inline-block w-4' />
            )}
            <span className={cn(row.getCanExpand() && 'font-medium')}>
              {row.original.libelle}
            </span>
          </div>
        ),
      },
      ...croisement.colonnes.map<ColumnDef<Noeud>>((c) => ({
        id: `c:${c.cle}`,
        header: c.libelle,
        accessorFn: (n) => n.valeurs[c.cle] ?? null,
        sortUndefined: 'last',
        cell: ({ getValue }) => afficherAvec(format, getValue<number | null>()),
      })),
      ...(croisement.totalColonnes
        ? [
            {
              id: TOTAL,
              header: 'Total',
              accessorFn: (n: Noeud) => n.total,
              cell: ({ getValue }) =>
                afficherAvec(format, getValue<number | null>()),
            } as ColumnDef<Noeud>,
          ]
        : []),
    ],
    [croisement, titreLignes, format]
  )

  // Le React Compiler laisse ce composant hors de sa mémoïsation : TanStack Table rend des fonctions.
  // eslint-disable-next-line react-hooks/incompatible-library
  const table = useReactTable({
    data: croisement.lignes,
    columns: colonnes,
    state: { sorting: tri, expanded: deplies },
    onSortingChange: setTri,
    onExpandedChange: setDeplies,
    getSubRows: (n) => n.subRows,
    getCoreRowModel: getCoreRowModel(),
    getExpandedRowModel: getExpandedRowModel(),
    getSortedRowModel: getSortedRowModel(),
  })
  const extensible = croisement.lignes.some((n) => n.subRows)
  const meta = (id: string) => ({
    nombre: id !== 'libelle',
    total: id === TOTAL,
  })

  return (
    <div className='space-y-2'>
      {extensible && (
        <div className='flex gap-1'>
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
        </div>
      )}
      <div className='overflow-x-auto rounded-md border'>
        <Table>
          <TableHeader>
            {table.getHeaderGroups().map((groupe) => (
              <TableRow key={groupe.id}>
                {groupe.headers.map((h) => {
                  const sens = h.column.getIsSorted()
                  const { nombre, total } = meta(h.column.id)
                  return (
                    <TableHead
                      key={h.id}
                      className={cn(
                        'whitespace-nowrap',
                        nombre && 'text-right',
                        total && 'bg-muted/50'
                      )}
                      aria-sort={
                        sens === 'asc'
                          ? 'ascending'
                          : sens === 'desc'
                            ? 'descending'
                            : undefined
                      }
                    >
                      <button
                        type='button'
                        onClick={h.column.getToggleSortingHandler()}
                        className={cn(
                          'hover:text-foreground inline-flex items-center gap-1',
                          nombre && 'flex-row-reverse'
                        )}
                      >
                        {flexRender(h.column.columnDef.header, h.getContext())}
                        {sens === 'asc' && <ArrowUp className='size-3.5' />}
                        {sens === 'desc' && <ArrowDown className='size-3.5' />}
                      </button>
                    </TableHead>
                  )
                })}
              </TableRow>
            ))}
          </TableHeader>
          <TableBody>
            {table.getRowModel().rows.length === 0 && (
              <TableRow>
                <TableCell
                  colSpan={colonnes.length}
                  className='text-muted-foreground h-14 text-center'
                >
                  {croisement.totaux
                    ? 'Placez un champ en lignes pour détailler.'
                    : 'Aucune donnée.'}
                </TableCell>
              </TableRow>
            )}
            {table.getRowModel().rows.map((row) => (
              <TableRow
                key={row.id}
                className={cn(
                  row.depth === 0 && row.getCanExpand() && 'bg-muted/30'
                )}
              >
                {row.getVisibleCells().map((cell) => {
                  const { nombre, total } = meta(cell.column.id)
                  return (
                    <TableCell
                      key={cell.id}
                      className={cn(
                        nombre && 'text-right tabular-nums',
                        total && 'bg-muted/50 font-medium'
                      )}
                    >
                      {flexRender(
                        cell.column.columnDef.cell,
                        cell.getContext()
                      )}
                    </TableCell>
                  )
                })}
              </TableRow>
            ))}
          </TableBody>
          {croisement.totaux && (
            <TableFooter>
              <TableRow>
                <TableCell>Total</TableCell>
                {croisement.colonnes.map((c) => (
                  <TableCell key={c.cle} className='text-right tabular-nums'>
                    {afficher(croisement.totaux?.valeurs[c.cle] ?? null)}
                  </TableCell>
                ))}
                {croisement.totalColonnes && (
                  <TableCell className='text-right tabular-nums'>
                    {afficher(croisement.totaux.total)}
                  </TableCell>
                )}
              </TableRow>
            </TableFooter>
          )}
        </Table>
      </div>
    </div>
  )
}
