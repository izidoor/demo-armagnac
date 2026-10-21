import { createColumnHelper } from '@tanstack/react-table'
import { type LigneVendue } from '@/api/generated'
import { Badge } from '@/components/ui/badge'
import { EnteteFiltrable } from '@/components/metier/entete-filtrable'
import { filtreValeurs } from '@/components/metier/filtre-valeurs'
import { formatDate, formatEuros } from '@/features/declarations/format'

const nombre = new Intl.NumberFormat('fr-FR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 4,
})

const colonne = createColumnHelper<LigneVendue>()

/**
 * Les colonnes des lignes vendues (RG-FAC-30) : émise le, n° (avec un badge pour un avoir), acheteur (au
 * back-office seulement), objet, code article, article, TVA, quantité (sommée par article seulement), prix
 * unitaire et montant HT.
 */
export function colonnesLignesVendues({ acheteur }: { acheteur: boolean }) {
  return [
    colonne.accessor('dateEmission', {
      meta: { titre: 'Émise le' },
      header: ({ column }) => (
        <EnteteFiltrable column={column} affichage={formatDate} />
      ),
      cell: ({ getValue }) => formatDate(getValue()),
      filterFn: filtreValeurs,
    }),
    colonne.accessor('numero', {
      meta: { titre: 'N°' },
      header: ({ column }) => <EnteteFiltrable column={column} />,
      cell: ({ row }) => (
        <span className='flex items-center gap-1'>
          <span className='font-mono'>{row.original.numero}</span>
          {row.original.avoir && <Badge variant='outline'>Avoir</Badge>}
        </span>
      ),
      filterFn: filtreValeurs,
    }),
    ...(acheteur
      ? [
          colonne.accessor(
            (l) =>
              l.clientCode ? `${l.clientCode} · ${l.acheteur}` : l.acheteur,
            {
              id: 'acheteur',
              meta: { titre: 'Acheteur' },
              header: ({ column }) => <EnteteFiltrable column={column} />,
              filterFn: filtreValeurs,
            }
          ),
        ]
      : []),
    colonne.accessor((l) => (l.origine ? l.origine.reference : 'Avoir'), {
      id: 'origine',
      meta: { titre: 'Objet' },
      header: ({ column }) => <EnteteFiltrable column={column} />,
      filterFn: filtreValeurs,
    }),
    colonne.accessor('code', {
      meta: { titre: 'Code article' },
      header: ({ column }) => <EnteteFiltrable column={column} />,
      cell: ({ getValue }) => <span className='font-mono'>{getValue()}</span>,
      filterFn: filtreValeurs,
    }),
    colonne.accessor((l) => l.designation, {
      id: 'article',
      meta: { titre: 'Article' },
      header: ({ column }) => <EnteteFiltrable column={column} />,
      filterFn: filtreValeurs,
    }),
    colonne.accessor('tvaLibelle', {
      meta: { titre: 'TVA' },
      header: ({ column }) => <EnteteFiltrable column={column} />,
      filterFn: filtreValeurs,
    }),
    colonne.accessor('quantite', {
      meta: { titre: 'Quantité' },
      header: ({ column }) => <EnteteFiltrable column={column} />,
      cell: ({ row }) =>
        `${nombre.format(row.original.quantite)}${row.original.unite ? ` ${row.original.unite}` : ''}`,
      enableColumnFilter: false,
      enableGrouping: false,
      // RG-FAC-30 : une quantité ne se somme que dans un même article ; un groupe qui en mêle plusieurs n'en a pas.
      aggregationFn: (_colonne, feuilles) =>
        new Set(feuilles.map((r) => r.original.code)).size === 1
          ? feuilles.reduce((s, r) => s + r.original.quantite, 0)
          : undefined,
      aggregatedCell: ({ row, getValue }) => {
        const v = getValue<number | undefined>()
        const unite = row.getLeafRows()[0]?.original.unite
        return v === undefined
          ? null
          : `${nombre.format(v)}${unite ? ` ${unite}` : ''}`
      },
    }),
    colonne.accessor('prixUnitaire', {
      meta: { titre: 'Prix unitaire HT' },
      header: ({ column }) => <EnteteFiltrable column={column} />,
      cell: ({ getValue }) => `${nombre.format(getValue())} €`,
      enableColumnFilter: false,
      enableGrouping: false,
    }),
    colonne.accessor('montantHt', {
      meta: { titre: 'Montant HT', format: formatEuros },
      header: ({ column }) => <EnteteFiltrable column={column} />,
      cell: ({ getValue }) => formatEuros(getValue()),
      enableColumnFilter: false,
      enableGrouping: false,
      aggregationFn: 'sum',
    }),
  ]
}
