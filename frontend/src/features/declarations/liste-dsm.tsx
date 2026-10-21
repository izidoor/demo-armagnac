import { useNavigate } from '@tanstack/react-router'
import { type DsmResume } from '@/api/generated'
import { Badge } from '@/components/ui/badge'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { BadgePlein } from '@/components/metier/badge-statut'
import { CelluleOuvrir, EnteteOuvrir } from '@/components/metier/cellule-ouvrir'
import { Pagination } from '@/components/metier/pagination'
import { TAILLE_PAGE, usePagination } from '@/components/metier/use-pagination'
import { type Espace } from './api'
import { formatDate, formatDateHeure, formatEuros } from './format'
import { STATUT_ETAT } from './libelles'

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
  const { lignes, pagination } = usePagination(dsms, TAILLE_PAGE)
  return (
    <>
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
              <EnteteOuvrir />
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
            {(lignes ?? []).map((d) => (
              <TableRow
                key={d.id}
                className='cursor-pointer'
                onClick={() => ouvrir(d.id)}
              >
                <TableCell className='font-medium'>
                  {d.periode.libelle}
                </TableCell>
                <TableCell>
                  <BadgePlein statut={STATUT_ETAT[d.etat]}>
                    {d.libelleEtat}
                  </BadgePlein>
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
                <CelluleOuvrir
                  libelle={`Ouvrir la DSM de ${d.periode.libelle}`}
                  onOuvrir={() => ouvrir(d.id)}
                />
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
      <Pagination {...pagination} />
    </>
  )
}
