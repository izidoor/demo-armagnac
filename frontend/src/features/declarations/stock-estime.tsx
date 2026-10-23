import { type StockEstime } from '@/api/generated'
import {
  Table,
  TableBody,
  TableCell,
  TableFooter,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { formatDate, formatLitres } from './format'
import { millesimee } from './libelles'

/**
 * Le stock estimé d'un opérateur (spec 06 RG-CA-11) : la base (dernière DSM validée, sinon point de départ) vieillie à
 * aujourd'hui, plus les achats, moins les ventes de ses retiraisons validées. Les colonnes du calcul se masquent sur un
 * contenu étroit : restent la désignation, la clé, le compte et le stock estimé.
 */
export function TableStockEstime({ stock }: { stock: StockEstime }) {
  if (!stock.baseAu)
    return (
      <p className='text-muted-foreground text-sm'>
        Ni DSM validée ni point de départ : pas encore de stock estimé.
      </p>
    )
  const total = stock.lignes.reduce((s, l) => s + l.estime, 0)
  const calcul = '@max-[40rem]/content:hidden'
  return (
    <div className='space-y-2'>
      <p className='text-sm'>
        Aujourd'hui, depuis le stock du{' '}
        <strong>{formatDate(stock.baseAu)}</strong>, vieilli au 1er avril, et
        vos retiraisons validées depuis.
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
              <TableHead className={`text-right ${calcul}`}>Base</TableHead>
              <TableHead className={`text-right ${calcul}`}>Achats</TableHead>
              <TableHead className={`text-right ${calcul}`}>Ventes</TableHead>
              <TableHead className='text-right whitespace-normal'>
                Stock estimé <span className='whitespace-nowrap'>(L AP)</span>
              </TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {stock.lignes.map((l, i) => (
              <TableRow key={i}>
                <TableCell className='whitespace-normal'>
                  {millesimee(l.cle) ? l.millesime : l.denomination}
                </TableCell>
                <TableCell className='font-mono'>{l.cle}</TableCell>
                <TableCell>{l.compte}</TableCell>
                <TableCell className={`text-right tabular-nums ${calcul}`}>
                  {formatLitres(l.base)}
                </TableCell>
                <TableCell className={`text-right tabular-nums ${calcul}`}>
                  {l.achats ? `+ ${formatLitres(l.achats)}` : ''}
                </TableCell>
                <TableCell className={`text-right tabular-nums ${calcul}`}>
                  {l.ventes ? `− ${formatLitres(l.ventes)}` : ''}
                </TableCell>
                <TableCell className='text-right font-medium tabular-nums'>
                  {formatLitres(l.estime)}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
          <TableFooter>
            <TableRow>
              <TableCell colSpan={3}>Total</TableCell>
              <TableCell colSpan={3} className={calcul} />
              <TableCell className='text-right tabular-nums'>
                {formatLitres(total)}
              </TableCell>
            </TableRow>
          </TableFooter>
        </Table>
      </div>
    </div>
  )
}
