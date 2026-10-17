import { useQuery } from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'
import { createColumnHelper } from '@tanstack/react-table'
import { FileDown } from 'lucide-react'
import { listerStocksDeclares, type LigneStockDeclare } from '@/api/generated'
import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'
import { EnteteFiltrable } from '@/components/metier/entete-filtrable'
import { filtreValeurs } from '@/components/metier/filtre-valeurs'
import { PageMetier } from '@/components/metier/page-metier'
import { TableDonnees } from '@/components/metier/table-donnees'
import { colonnesLigneStock } from './colonnes-stocks'
import { formatDate, formatLitres } from './format'
import { GraphiquesStocks } from './graphiques-stocks'
import { TYPES_DECLARANT } from './libelles'

/**
 * Stocks déclarés de tous les opérateurs, pour le BNIA (analyse UC-DEC-1, spec 05 §6.1) : deux graphiques de
 * démonstration, puis le stock de fin de la dernière DSM validée de chacun, ligne par ligne (RG-DEC-42), avec
 * l'export Excel pour les douanes.
 */
export function PageStocksDeclares() {
  return (
    <PageMetier
      titre='Stocks déclarés'
      description='Historique des stocks déclarés et analyse'
    >
      <div className='mb-8'>
        <GraphiquesStocks />
        <Separator className='my-10' />
        <h2 className='mb-4 text-xl font-semibold'>
          Liste des stocks déclarés
        </h2>
        <DonneesStocks />
      </div>
    </PageMetier>
  )
}

const colonne = createColumnHelper<LigneStockDeclare>()

const COLONNES = [
  colonne.accessor((l) => `${l.codeOperateur} · ${l.raisonSociale}`, {
    id: 'operateur',
    meta: { titre: 'Opérateur' },
    header: ({ column }) => <EnteteFiltrable column={column} />,
    cell: ({ row }) => (
      <>
        <span className='font-mono'>{row.original.codeOperateur}</span> ·{' '}
        {row.original.raisonSociale}
      </>
    ),
    filterFn: filtreValeurs,
  }),
  colonne.accessor((l) => TYPES_DECLARANT[l.type], {
    id: 'type',
    meta: { titre: 'Type' },
    header: ({ column }) => <EnteteFiltrable column={column} />,
    filterFn: filtreValeurs,
  }),
  colonne.accessor('au', {
    meta: { titre: 'Au' },
    header: ({ column }) => (
      <EnteteFiltrable column={column} affichage={formatDate} />
    ),
    cell: ({ getValue }) => formatDate(getValue()),
    filterFn: filtreValeurs,
  }),
  ...colonnesLigneStock<LigneStockDeclare>(),
]

/**
 * *Données* : le stock de fin de la dernière DSM validée de chaque opérateur, ligne par ligne, lignes nulles
 * omises (RG-DEC-42) ; dans la table, recherche, tri et filtre par valeurs sur chaque colonne, total des
 * lignes affichées. Une ligne ouvre la DSM dont elle est tirée. L'export Excel pour les douanes rend toutes
 * les lignes.
 */
function DonneesStocks() {
  const navigate = useNavigate()
  const stocks = useQuery({
    queryKey: ['declarations', 'stocks'],
    queryFn: () =>
      listerStocksDeclares({ throwOnError: true }).then((r) => r.data),
  })
  return (
    <TableDonnees
      cle='stocks-declares-bo'
      donnees={stocks.data ?? []}
      colonnes={COLONNES}
      chiffrees={['stock']}
      chargement={stocks.isPending}
      vide='Aucun stock déclaré.'
      total={(l) => l.stock}
      formatTotal={formatLitres}
      onLigne={(l) =>
        navigate({
          to: '/bo/operateurs/$id/dsm/$dsm',
          params: { id: String(l.operateur), dsm: String(l.dsm) },
        })
      }
      actions={
        <Button variant='outline' size='sm' asChild>
          <a href='/api/declarations/stocks/excel' download>
            <FileDown className='size-4' /> Exporter en Excel
          </a>
        </Button>
      }
    />
  )
}
