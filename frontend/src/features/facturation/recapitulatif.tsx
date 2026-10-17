import { type LigneTvaPiece } from '@/api/generated'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Table,
  TableBody,
  TableCell,
  TableFooter,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { formatEuros } from '@/features/declarations/format'

/**
 * Le récapitulatif de TVA et les totaux d'une pièce (RG-FAC-05), enregistrés ou recalculés pendant l'édition ; ou
 * ceux des ventes d'une période, somme des récapitulatifs des pièces (RG-FAC-30).
 */
export function Recapitulatif({
  titre = 'TVA et TOTAL',
  tva,
  totalHt,
  totalTva,
  totalTtc,
}: {
  titre?: string
  tva: LigneTvaPiece[]
  totalHt: number
  totalTva: number
  totalTtc: number
}) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className='text-base'>{titre}</CardTitle>
      </CardHeader>
      <CardContent>
        <div className='flex flex-wrap justify-between gap-6'>
          <div className='rounded-md border'>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>TVA</TableHead>
                  <TableHead className='text-right'>Base HT</TableHead>
                  <TableHead className='text-right'>Montant</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {tva.map((t) => (
                  <TableRow key={`${t.code}-${t.taux}`}>
                    <TableCell>
                      {t.libelle}
                      {t.motif && (
                        <span className='text-muted-foreground block text-xs'>
                          {t.motif}
                        </span>
                      )}
                    </TableCell>
                    <TableCell className='text-right tabular-nums'>
                      {formatEuros(t.base)}
                    </TableCell>
                    <TableCell className='text-right tabular-nums'>
                      {formatEuros(t.tva)}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
          <div className='rounded-md border'>
            <Table>
              <TableBody>
                <TableRow>
                  <TableCell>Total HT</TableCell>
                  <TableCell className='text-right tabular-nums'>
                    {formatEuros(totalHt)}
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell>Total TVA</TableCell>
                  <TableCell className='text-right tabular-nums'>
                    {formatEuros(totalTva)}
                  </TableCell>
                </TableRow>
              </TableBody>
              <TableFooter>
                <TableRow>
                  <TableCell>Total TTC</TableCell>
                  <TableCell className='text-right font-semibold tabular-nums'>
                    {formatEuros(totalTtc)}
                  </TableCell>
                </TableRow>
              </TableFooter>
            </Table>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
