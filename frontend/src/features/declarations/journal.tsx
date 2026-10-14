import { type EntreeJournalDeclarations } from '@/api/generated'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { formatDateHeure } from './format'

/**
 * Journal des déclarations (RG-DEC-23), du plus récent au plus ancien : l'historique d'une DSM, ou toutes
 * les entrées d'un opérateur, brouillons supprimés compris (avec la colonne DSM).
 */
export function TableJournalDeclarations({
  entrees,
  avecDsm = false,
}: {
  entrees: EntreeJournalDeclarations[]
  avecDsm?: boolean
}) {
  return (
    <div className='rounded-md border'>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Date</TableHead>
            <TableHead>Action</TableHead>
            {avecDsm && <TableHead>DSM</TableHead>}
            <TableHead>Détail</TableHead>
            <TableHead>Par</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {entrees.length === 0 && (
            <TableRow>
              <TableCell
                colSpan={avecDsm ? 5 : 4}
                className='text-muted-foreground h-16 text-center'
              >
                Aucune action.
              </TableCell>
            </TableRow>
          )}
          {entrees.map((e, i) => (
            <TableRow key={i}>
              <TableCell className='whitespace-nowrap'>
                {formatDateHeure(e.date)}
              </TableCell>
              <TableCell>{e.libelle}</TableCell>
              {avecDsm && <TableCell>{e.periode ?? '—'}</TableCell>}
              <TableCell className='text-sm'>{e.detail ?? ''}</TableCell>
              <TableCell>{e.acteur}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )
}
