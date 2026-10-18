import { useQuery } from '@tanstack/react-query'
import { listerPieces } from '@/api/generated'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { cleFacturation } from './api'
import { TablePieces } from './page-pieces'

/**
 * Onglet « Factures » de la fiche opérateur, au back-office (spec 08 §6.1, RG-FAC-27) : les factures et avoirs
 * émis pour ce client, puis ses brouillons s'il en a.
 */
export function OngletFactures({ operateur }: { operateur: number }) {
  const client = String(operateur)
  const emises = useQuery({
    queryKey: [...cleFacturation, 'pieces', { client, etat: 'EMISE' }],
    queryFn: () =>
      listerPieces({
        query: { client, etat: 'EMISE' },
        throwOnError: true,
      }).then((r) => r.data),
  })
  const brouillons = useQuery({
    queryKey: [...cleFacturation, 'pieces', { client, etat: 'BROUILLON' }],
    queryFn: () =>
      listerPieces({
        query: { client, etat: 'BROUILLON' },
        throwOnError: true,
      }).then((r) => r.data),
  })
  return (
    <div className='space-y-4'>
      <Card>
        <CardHeader>
          <CardTitle>Factures et avoirs</CardTitle>
        </CardHeader>
        <CardContent>
          <TablePieces
            pieces={emises.data ?? []}
            brouillons={false}
            chargement={emises.isPending}
            avecAcheteur={false}
          />
        </CardContent>
      </Card>
      {(brouillons.data ?? []).length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Brouillons</CardTitle>
          </CardHeader>
          <CardContent>
            <TablePieces
              pieces={brouillons.data ?? []}
              brouillons
              chargement={brouillons.isPending}
              avecAcheteur={false}
            />
          </CardContent>
        </Card>
      )}
    </div>
  )
}
