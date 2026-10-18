import { useQuery } from '@tanstack/react-query'
import { useRouter } from '@tanstack/react-router'
import { createColumnHelper } from '@tanstack/react-table'
import { type ArticleCatalogue, listerArticles } from '@/api/generated'
import { Badge } from '@/components/ui/badge'
import { EnteteFiltrable } from '@/components/metier/entete-filtrable'
import { filtreValeurs } from '@/components/metier/filtre-valeurs'
import { PageMetier } from '@/components/metier/page-metier'
import { TableDonnees } from '@/components/metier/table-donnees'
import { cleFacturation } from './api'

const prix = new Intl.NumberFormat('fr-FR', {
  style: 'currency',
  currency: 'EUR',
  minimumFractionDigits: 2,
  maximumFractionDigits: 4,
})

const colonne = createColumnHelper<ArticleCatalogue>()

const COLONNES = [
  colonne.accessor('source', {
    meta: { titre: 'Source' },
    header: ({ column }) => <EnteteFiltrable column={column} />,
    filterFn: filtreValeurs,
  }),
  colonne.accessor('code', {
    meta: { titre: 'Code' },
    header: ({ column }) => <EnteteFiltrable column={column} />,
    cell: ({ getValue }) => <span className='font-mono'>{getValue()}</span>,
    filterFn: filtreValeurs,
  }),
  colonne.accessor('designation', {
    meta: { titre: 'Désignation' },
    header: ({ column }) => <EnteteFiltrable column={column} />,
    filterFn: filtreValeurs,
  }),
  colonne.accessor((a) => a.unite ?? '', {
    id: 'unite',
    meta: { titre: 'Unité' },
    header: ({ column }) => <EnteteFiltrable column={column} />,
    filterFn: filtreValeurs,
  }),
  colonne.accessor('tvaLibelle', {
    meta: { titre: 'TVA' },
    header: ({ column }) => <EnteteFiltrable column={column} />,
    filterFn: filtreValeurs,
  }),
  colonne.accessor((a) => (a.actif ? 'Actif' : 'Inactif'), {
    id: 'actif',
    meta: { titre: 'État' },
    header: ({ column }) => <EnteteFiltrable column={column} />,
    cell: ({ row }) => (
      <Badge variant={row.original.actif ? 'default' : 'outline'}>
        {row.original.actif ? 'Actif' : 'Inactif'}
      </Badge>
    ),
    filterFn: filtreValeurs,
  }),
  colonne.accessor('prixUnitaire', {
    meta: { titre: 'Prix unitaire HT' },
    header: ({ column }) => <EnteteFiltrable column={column} />,
    cell: ({ getValue }) => prix.format(getValue()),
    enableColumnFilter: false,
    enableGrouping: false,
  }),
]

/**
 * Articles du back-office (spec 08 §6.1, UC-FAC-6, RG-FAC-29) : tous les articles de tous les catalogues, à la
 * date du jour, actifs ou non ; une ligne ouvre l'écran où sa source l'administre (la grille CVO pour la
 * cotisation).
 */
export function PageArticles() {
  const router = useRouter()
  const articles = useQuery({
    queryKey: [...cleFacturation, 'articles'],
    queryFn: () => listerArticles({ throwOnError: true }).then((r) => r.data),
  })
  return (
    <PageMetier
      titre='Articles'
      description="Les articles de tous les catalogues, au prix et à la TVA du jour. Chacun s'administre chez sa source : une ligne l'ouvre."
    >
      <TableDonnees
        cle='articles'
        donnees={articles.data ?? []}
        colonnes={COLONNES}
        chiffrees={['prixUnitaire']}
        chargement={articles.isPending}
        vide='Aucun article.'
        onLigne={(a) => router.history.push(a.lienAdministration)}
      />
    </PageMetier>
  )
}
