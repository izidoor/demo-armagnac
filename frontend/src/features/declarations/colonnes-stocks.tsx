import { type ColumnDef } from '@tanstack/react-table'
import { EnteteFiltrable } from '@/components/metier/entete-filtrable'
import { filtreValeurs } from '@/components/metier/filtre-valeurs'
import { formatLitres } from './format'
import { type CleConditionnement, millesimee, rangCompte } from './libelles'

type LigneDeStock = {
  cle: CleConditionnement
  millesime?: number
  denomination?: string
  compte: string
  stock: number
}

const COLLATION = new Intl.Collator('fr', { numeric: true })

const comparerComptes = (a: string, b: string) =>
  rangCompte(a) - rangCompte(b) || a.localeCompare(b)

/**
 * Les colonnes d'une ligne de stock, pour `TableDonnees` : millésime ou dénomination, clé, compte d'âge
 * (dans l'ordre de la grille), stock en L AP ; triables et filtrables, le stock trié seulement.
 */
export function colonnesLigneStock<T extends LigneDeStock>(): ColumnDef<
  T,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  any
>[] {
  return [
    {
      id: 'article',
      accessorFn: (l) =>
        millesimee(l.cle) ? String(l.millesime ?? '') : (l.denomination ?? ''),
      header: ({ column }) => (
        <EnteteFiltrable column={column} titre='Millésime ou dénomination' />
      ),
      sortingFn: (a, b, id) =>
        COLLATION.compare(a.getValue<string>(id), b.getValue<string>(id)),
      filterFn: filtreValeurs,
    },
    {
      id: 'cle',
      accessorFn: (l) => l.cle,
      header: ({ column }) => <EnteteFiltrable column={column} titre='Clé' />,
      cell: ({ getValue }) => (
        <span className='font-mono'>{getValue<string>()}</span>
      ),
      filterFn: filtreValeurs,
    },
    {
      id: 'compte',
      accessorFn: (l) => l.compte,
      header: ({ column }) => (
        <EnteteFiltrable
          column={column}
          titre="Compte d'âge"
          ordre={comparerComptes}
        />
      ),
      sortingFn: (a, b, id) =>
        comparerComptes(a.getValue<string>(id), b.getValue<string>(id)),
      filterFn: filtreValeurs,
    },
    {
      id: 'stock',
      accessorFn: (l) => l.stock,
      header: ({ column }) => (
        <EnteteFiltrable column={column} titre='Stock (L AP)' aDroite />
      ),
      cell: ({ getValue }) => formatLitres(getValue<number>()),
      enableColumnFilter: false,
    },
  ]
}
