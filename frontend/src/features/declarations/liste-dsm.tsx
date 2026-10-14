import { useNavigate } from '@tanstack/react-router'
import { ChevronRight } from 'lucide-react'
import { type DsmResume } from '@/api/generated'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { type Espace } from './api'
import { formatDate, formatDateHeure, formatEuros } from './format'
import { VARIANTE_ETAT } from './libelles'

/**
 * Les DSM d'un opérateur, de la plus récente à la plus ancienne, avec leur échéance (RG-DEC-39) et leur total
 * HT, calculé ou facturé (RG-DEC-34) ; une ligne ouvre la DSM.
 */
export function ListeDsm({
  espace,
  operateur,
  dsms,
}: {
  espace: Espace
  operateur: number
  dsms: DsmResume[]
}) {
  const navigate = useNavigate()
  const ouvrir = (id: number) =>
    espace === 'bo'
      ? navigate({
          to: '/bo/operateurs/$id/dsm/$dsm',
          params: { id: String(operateur), dsm: String(id) },
        })
      : navigate({
          to: '/portail/declarations/$dsm',
          params: { dsm: String(id) },
        })
  return (
    <div className='rounded-md border'>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Période</TableHead>
            <TableHead>État</TableHead>
            <TableHead>Échéance</TableHead>
            <TableHead>Envoyée le</TableHead>
            <TableHead>Validée le</TableHead>
            <TableHead className='text-right'>Total HT</TableHead>
            <TableHead className='w-0' />
          </TableRow>
        </TableHeader>
        <TableBody>
          {dsms.length === 0 && (
            <TableRow>
              <TableCell
                colSpan={7}
                className='text-muted-foreground h-16 text-center'
              >
                Aucune DSM.
              </TableCell>
            </TableRow>
          )}
          {dsms.map((d) => (
            <TableRow
              key={d.id}
              className='cursor-pointer'
              onClick={() => ouvrir(d.id)}
            >
              <TableCell className='font-medium'>{d.periode.libelle}</TableCell>
              <TableCell>
                <Badge variant={VARIANTE_ETAT[d.etat]}>{d.libelleEtat}</Badge>
              </TableCell>
              <TableCell>{formatDate(d.echeance)}</TableCell>
              <TableCell>{formatDateHeure(d.envoyeeLe)}</TableCell>
              <TableCell>{formatDateHeure(d.valideeLe)}</TableCell>
              <TableCell className='text-right tabular-nums'>
                {formatEuros(d.totalHt)}
                {d.facturee && (
                  <Badge variant='secondary' className='ms-2'>
                    Facturée
                  </Badge>
                )}
              </TableCell>
              <TableCell>
                <Button
                  variant='ghost'
                  size='sm'
                  onClick={(e) => {
                    e.stopPropagation()
                    ouvrir(d.id)
                  }}
                >
                  Ouvrir <ChevronRight className='size-4' />
                </Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )
}
