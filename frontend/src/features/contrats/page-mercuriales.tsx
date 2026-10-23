import { useFiltresAdresse } from '@/hooks/use-filtres-adresse'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { PageMetier } from '@/components/metier/page-metier'
import { formatDate } from '@/features/declarations/format'
import { useMercuriales } from './api'
import { formatHlAp, formatPrix } from './format'

/**
 * Back-office, « Mercuriales » (spec 06 §6.1, RG-CA-26) : sur les contrats conclus ou exécutés de la période (par
 * défaut les 12 derniers mois), par millésime ou par dénomination et compte d'âge, le nombre de contrats, le volume
 * d'alcool pur et les prix minimum, moyen pondéré par le volume et maximum. Une table sur un contenu large, une carte
 * par produit en dessous.
 */
export function PageMercuriales() {
  const [filtres, changer] = useFiltresAdresse({ du: '', au: '' })
  const mercuriales = useMercuriales(
    filtres.du || undefined,
    filtres.au || undefined
  )
  const m = mercuriales.data
  return (
    <PageMetier
      titre='Mercuriales'
      description={
        m
          ? `Les prix des contrats conclus ou exécutés du ${formatDate(m.du)} au ${formatDate(m.au)}, en € HT par hl d'alcool pur.`
          : "Les prix des contrats conclus ou exécutés, en € HT par hl d'alcool pur."
      }
    >
      <div className='mb-4 flex flex-wrap gap-3'>
        <div className='space-y-1'>
          <Label htmlFor='du'>Du</Label>
          <Input
            id='du'
            type='date'
            value={filtres.du || m?.du || ''}
            onChange={(e) => changer({ du: e.target.value })}
          />
        </div>
        <div className='space-y-1'>
          <Label htmlFor='au'>Au</Label>
          <Input
            id='au'
            type='date'
            value={filtres.au || m?.au || ''}
            onChange={(e) => changer({ au: e.target.value })}
          />
        </div>
      </div>
      {mercuriales.isPending ? (
        <Skeleton className='h-64' />
      ) : mercuriales.isError || !m ? (
        <p className='text-destructive text-sm'>
          Les mercuriales de cette période ne se calculent pas : vérifiez les
          dates.
        </p>
      ) : m.produits.length === 0 ? (
        <p className='text-muted-foreground text-sm'>
          Aucun contrat conclu ou exécuté sur la période.
        </p>
      ) : (
        <>
          <div className='hidden rounded-md border @min-[40rem]/content:block'>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Produit</TableHead>
                  <TableHead className='text-right'>Contrats</TableHead>
                  <TableHead className='text-right'>Volume</TableHead>
                  <TableHead className='text-right'>Minimum</TableHead>
                  <TableHead className='text-right'>Moyen pondéré</TableHead>
                  <TableHead className='text-right'>Maximum</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {m.produits.map((p) => (
                  <TableRow key={p.libelle}>
                    <TableCell className='font-medium'>{p.libelle}</TableCell>
                    <TableCell className='text-right tabular-nums'>
                      {p.contrats}
                    </TableCell>
                    <TableCell className='text-right tabular-nums'>
                      {formatHlAp(p.volumeAp)}
                    </TableCell>
                    <TableCell className='text-right tabular-nums'>
                      {formatPrix(p.prixMinimum)}
                    </TableCell>
                    <TableCell className='text-right font-semibold tabular-nums'>
                      {formatPrix(p.prixMoyen)}
                    </TableCell>
                    <TableCell className='text-right tabular-nums'>
                      {formatPrix(p.prixMaximum)}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
          <ul className='space-y-2 @min-[40rem]/content:hidden'>
            {m.produits.map((p) => (
              <li key={p.libelle} className='bg-card rounded-md border p-3'>
                <div className='flex flex-wrap items-baseline justify-between gap-x-2'>
                  <span className='font-medium'>{p.libelle}</span>
                  <span className='text-muted-foreground text-sm'>
                    {p.contrats} contrat{p.contrats > 1 ? 's' : ''} ·{' '}
                    {formatHlAp(p.volumeAp)}
                  </span>
                </div>
                <div className='mt-1 text-lg font-semibold tabular-nums'>
                  {formatPrix(p.prixMoyen)}
                </div>
                {p.prixMinimum !== p.prixMaximum && (
                  <div className='text-muted-foreground text-sm tabular-nums'>
                    de {formatPrix(p.prixMinimum)} à {formatPrix(p.prixMaximum)}
                  </div>
                )}
              </li>
            ))}
          </ul>
        </>
      )}
    </PageMetier>
  )
}
