import { Link } from '@tanstack/react-router'
import { type LigneStock } from '@/api/generated'
import { Separator } from '@/components/ui/separator'
import { Skeleton } from '@/components/ui/skeleton'
import { FILIERE, typeDeclarant } from '@/components/metier/filiere'
import { PageMetier } from '@/components/metier/page-metier'
import { TableDonnees } from '@/components/metier/table-donnees'
import { useMoi } from '@/features/session/session'
import { useStockDeclare, useStockEstime } from './api'
import { colonnesLigneStock } from './colonnes-stocks'
import { formatDate, formatDateHeure, formatLitres } from './format'
import { type EchelleOperateur, GraphiquesStocks } from './graphiques-stocks'
import { TableStockEstime } from './stock-estime'

/**
 * Portail, « Stocks déclarés » (spec 05 §6.2), lu par tout lecteur des déclarations de l'opérateur actif
 * (RG-DEC-01), sans onglets, comme au back-office : deux graphiques de démonstration à l'échelle de l'opérateur, son
 * stock estimé d'aujourd'hui (spec 06 RG-CA-11), puis le stock de fin de sa dernière DSM validée, ligne par ligne
 * (RG-DEC-42).
 */
export function PageEtatStocks() {
  const { actif } = useMoi()
  if (!actif) return null
  return (
    <EtatStocks
      key={actif.operateur}
      operateur={actif.operateur}
      type={actif.type}
    />
  )
}

function EtatStocks({ operateur, type }: { operateur: number; type: string }) {
  const stock = useStockDeclare(operateur)
  const estime = useStockEstime(operateur)
  return (
    <PageMetier
      titre='Stocks déclarés'
      description='Historique de vos stocks déclarés et analyse'
    >
      <div className='mb-8'>
        {stock.isPending ? (
          <Skeleton className='h-96 w-full' />
        ) : (
          <GraphiquesStocks
            operateur={echelle(type, stock.data?.lignes ?? [])}
          />
        )}
        <Separator className='my-10' />
        <h2 className='mb-4 text-xl font-semibold'>Stock estimé</h2>
        {estime.isPending ? (
          <Skeleton className='h-48 w-full' />
        ) : (
          estime.data && <TableStockEstime stock={estime.data} />
        )}
        <Separator className='my-10' />
        <h2 className='mb-4 text-xl font-semibold'>
          Liste des stocks déclarés
        </h2>
        <DonneesStock stock={stock} />
      </div>
    </PageMetier>
  )
}

/**
 * L'échelle des graphiques : le stock déclaré de l'opérateur, en hl AP ; sans stock, celui d'un opérateur moyen
 * de son type dans la filière.
 */
function echelle(type: string, lignes: LigneStock[]): EchelleOperateur {
  const t = typeDeclarant(type)
  const litres = lignes.reduce((s, l) => s + l.stock, 0)
  return {
    type: t,
    stockHl:
      litres > 0 ? litres / 100 : FILIERE[t].stockHl / FILIERE[t].operateurs,
  }
}

const COLONNES = colonnesLigneStock<LigneStock>()

/**
 * Le stock de fin de la dernière DSM validée, ligne par ligne, lignes nulles omises (RG-DEC-42) ; recherche, tri
 * et filtre par valeurs sur chaque colonne, total des lignes affichées.
 */
function DonneesStock({
  stock,
}: {
  stock: ReturnType<typeof useStockDeclare>
}) {
  if (stock.isSuccess && !stock.data) {
    return (
      <p className='text-muted-foreground text-sm'>
        Aucune DSM validée : pas encore de stock déclaré.
      </p>
    )
  }
  return (
    <>
      {stock.data && (
        <p className='mb-3 text-sm'>
          Au <strong>{formatDate(stock.data.periode.fin)}</strong>, fin de la{' '}
          <Link
            to='/portail/declarations/$dsm'
            params={{ dsm: String(stock.data.dsm) }}
            className='underline underline-offset-4'
          >
            DSM de {stock.data.periode.libelle}
          </Link>
          , validée le {formatDateHeure(stock.data.valideeLe)}.
        </p>
      )}
      <TableDonnees
        cle='stocks-declares-portail'
        donnees={stock.data?.lignes ?? []}
        colonnes={COLONNES}
        chiffrees={['stock']}
        chargement={stock.isPending}
        vide='Toutes les lignes sont finies à 0 : stock nul.'
        total={(l) => l.stock}
        formatTotal={formatLitres}
      />
    </>
  )
}
