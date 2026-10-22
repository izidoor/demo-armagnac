import { type PointDeDepart } from '@/api/generated'
import { Badge } from '@/components/ui/badge'
import {
  Table,
  TableBody,
  TableCell,
  TableFooter,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { formatDate, formatDateHeure, formatLitres } from './format'
import { millesimee } from './libelles'

/**
 * Le point de départ en vigueur, en lecture : début et stock initial (spec 05 §3.3). Les en-têtes passent à la ligne :
 * le tableau tient dans un téléphone (300 px au plus serré).
 */
export function ResumePointDeDepart({ point }: { point: PointDeDepart }) {
  const total = point.lignes.reduce((s, l) => s + l.volume, 0)
  return (
    <div className='space-y-2'>
      <p className='flex flex-wrap items-center gap-2 text-sm'>
        <span>
          Début au <strong>{formatDate(point.debut)}</strong>, fixé par{' '}
          {point.fixePar} le {formatDateHeure(point.fixeLe)}.
        </span>
        {point.fige && (
          <Badge
            variant='secondary'
            title='La première DSM qui en est issue est validée (RG-DEC-08)'
          >
            Figé
          </Badge>
        )}
      </p>
      <div className='rounded-md border'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className='whitespace-normal'>
                Millésime ou dénomination
              </TableHead>
              <TableHead>Clé</TableHead>
              <TableHead className='whitespace-normal'>Compte d'âge</TableHead>
              <TableHead className='text-right whitespace-normal'>
                Stock initial <span className='whitespace-nowrap'>(L AP)</span>
              </TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {point.lignes.length === 0 && (
              <TableRow>
                <TableCell
                  colSpan={4}
                  className='text-muted-foreground h-12 text-center'
                >
                  Aucune ligne : stock initial nul.
                </TableCell>
              </TableRow>
            )}
            {point.lignes.map((l, i) => (
              <TableRow key={i}>
                <TableCell className='whitespace-normal'>
                  {millesimee(l.cle) ? l.millesime : l.denomination}
                </TableCell>
                <TableCell className='font-mono'>{l.cle}</TableCell>
                <TableCell>{l.compte}</TableCell>
                <TableCell className='text-right tabular-nums'>
                  {formatLitres(l.volume)}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
          {point.lignes.length > 0 && (
            <TableFooter>
              <TableRow>
                <TableCell colSpan={3}>Total</TableCell>
                <TableCell className='text-right tabular-nums'>
                  {formatLitres(total)}
                </TableCell>
              </TableRow>
            </TableFooter>
          )}
        </Table>
      </div>
    </div>
  )
}
