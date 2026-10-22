import { useQuery } from '@tanstack/react-query'
import { useRouter } from '@tanstack/react-router'
import { createColumnHelper } from '@tanstack/react-table'
import { type ArticleCatalogue, listerArticles } from '@/api/generated'
import { Badge } from '@/components/ui/badge'
import { CarteListe } from '@/components/metier/cartes-liste'
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

/**
 * Selon la largeur du contenu, comme la liste des opérateurs (34 px de marges) : la désignation prend la place qui reste
 * et se tronque (12rem au moins) ; le tableau fait alors 1123 px. Sous 73rem, il perd la source et l'unité (869 px) ;
 * sous 57rem, une carte par article.
 */
const SEUILS = {
  secondaire: '@max-[73rem]/content:hidden',
  tableau: '@max-[57rem]/content:hidden',
  cartes: '@min-[57rem]/content:hidden',
}

const COLONNES = [
  colonne.accessor('source', {
    meta: { titre: 'Source', className: SEUILS.secondaire },
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
    meta: { titre: 'Désignation', className: 'w-full max-w-0 min-w-48' },
    header: ({ column }) => <EnteteFiltrable column={column} />,
    cell: ({ getValue }) => (
      <span className='block truncate' title={getValue()}>
        {getValue()}
      </span>
    ),
    filterFn: filtreValeurs,
  }),
  colonne.accessor((a) => a.unite ?? '', {
    id: 'unite',
    meta: { titre: 'Unité', className: SEUILS.secondaire },
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
 * cotisation). Colonnes masquées par paliers et cartes sous un seuil (`SEUILS`).
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
        cartes={{
          tableau: SEUILS.tableau,
          masquee: SEUILS.cartes,
          carte: (a) => (
            <CarteListe
              onOuvrir={() => router.history.push(a.lienAdministration)}
              titre={a.designation}
            >
              <div className='flex flex-wrap items-center gap-x-2 gap-y-1 text-sm'>
                <span className='text-muted-foreground font-mono break-all'>
                  {a.code}
                </span>
                <Badge variant={a.actif ? 'default' : 'outline'}>
                  {a.actif ? 'Actif' : 'Inactif'}
                </Badge>
              </div>
              <div className='text-muted-foreground text-sm'>
                <span className='text-foreground font-medium tabular-nums'>
                  {prix.format(a.prixUnitaire)}
                </span>
                {a.unite && ` HT par ${a.unite}`} · TVA {a.tvaLibelle}
              </div>
            </CarteListe>
          ),
        }}
      />
    </PageMetier>
  )
}
