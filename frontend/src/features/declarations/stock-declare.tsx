import { type StockDeclare } from '@/api/generated'
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
 * Le stock déclaré d'un opérateur (RG-DEC-42) : le stock de fin de sa dernière DSM validée, ligne par ligne,
 * daté de la fin de sa période ; les lignes finies à 0 sont omises.
 */
export function ResumeStockDeclare({ stock }: { stock: StockDeclare | null }) {
  if (!stock) {
    return (
      <p className='text-muted-foreground text-sm'>
        Aucune DSM validée : pas encore de stock déclaré.
      </p>
    )
  }
  const total = stock.lignes.reduce((s, l) => s + l.stock, 0)
  return (
    <div className='space-y-2'>
      <p className='text-sm'>
        Au <strong>{formatDate(stock.periode.fin)}</strong>, fin de la DSM de{' '}
        {stock.periode.libelle}, validée le {formatDateHeure(stock.valideeLe)}.
      </p>
      <div className='rounded-md border'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Millésime ou dénomination</TableHead>
              <TableHead>Clé</TableHead>
              <TableHead>Compte d'âge</TableHead>
              <TableHead className='text-right'>Stock (L AP)</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {stock.lignes.length === 0 && (
              <TableRow>
                <TableCell
                  colSpan={4}
                  className='text-muted-foreground h-12 text-center'
                >
                  Toutes les lignes sont finies à 0 : stock nul.
                </TableCell>
              </TableRow>
            )}
            {stock.lignes.map((l, i) => (
              <TableRow key={i}>
                <TableCell>
                  {millesimee(l.cle) ? l.millesime : l.denomination}
                </TableCell>
                <TableCell className='font-mono'>{l.cle}</TableCell>
                <TableCell>{l.compte}</TableCell>
                <TableCell className='text-right tabular-nums'>
                  {formatLitres(l.stock)}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
          {stock.lignes.length > 0 && (
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
