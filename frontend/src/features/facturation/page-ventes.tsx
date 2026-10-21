import { useQuery } from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'
import { FileDown } from 'lucide-react'
import { consulterVentes, type LigneVendue } from '@/api/generated'
import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'
import { PageMetier } from '@/components/metier/page-metier'
import { TableDonnees } from '@/components/metier/table-donnees'
import { formatEuros } from '@/features/declarations/format'
import { cleFacturation } from './api'
import { ChoixPeriode } from './choix-periode'
import { colonnesLignesVendues } from './colonnes-ventes'
import { GraphiquesVentes } from './graphiques-ventes'
import { usePeriode } from './periode'

/**
 * Ventes du back-office (spec 08 §6.1, UC-FAC-8, RG-FAC-30) : les lignes article des factures et avoirs émis dans
 * une période (date d'émission), avoirs déduits. La période, l'année civile en cours par défaut, vaut pour les
 * lignes vendues ; au-dessus, deux graphiques de démonstration. Export : la synthèse de la période en PDF.
 */
export function PageVentes() {
  const { periode, setPeriode, valide } = usePeriode()
  const ventes = useQuery({
    queryKey: [...cleFacturation, 'ventes', periode],
    queryFn: () =>
      consulterVentes({ query: periode, throwOnError: true }).then(
        (r) => r.data
      ),
    enabled: valide,
  })
  return (
    <PageMetier titre='Ventes' description='Historique des ventes et analyse'>
      <GraphiquesVentes />
      <Separator className='my-10' />
      <h2 className='mb-4 text-xl font-semibold'>Liste des ventes</h2>
      <ChoixPeriode
        id='ventes'
        periode={periode}
        valide={valide}
        onChange={setPeriode}
        actions={
          valide ? (
            <Button variant='outline' asChild>
              <a
                href={`/api/facturation/ventes/pdf?du=${periode.du}&au=${periode.au}`}
                download
              >
                <FileDown className='size-4' /> Exporter en PDF
              </a>
            </Button>
          ) : (
            <Button variant='outline' disabled>
              <FileDown className='size-4' /> Exporter en PDF
            </Button>
          )
        }
      />
      <div className='mb-8'>
        <DonneesVentes
          lignes={ventes.data?.lignes ?? []}
          chargement={valide && ventes.isPending}
        />
      </div>
    </PageMetier>
  )
}

const COLONNES = colonnesLignesVendues({ acheteur: true })

/**
 * *Données* : les lignes vendues de la période, triables et filtrables par colonne, total HT des lignes
 * affichées ; une ligne ouvre sa pièce.
 */
function DonneesVentes({
  lignes,
  chargement,
}: {
  lignes: LigneVendue[]
  chargement: boolean
}) {
  const navigate = useNavigate()
  return (
    <TableDonnees
      cle='ventes'
      donnees={lignes}
      colonnes={COLONNES}
      chiffrees={['quantite', 'prixUnitaire', 'montantHt']}
      masquees={['code']}
      chargement={chargement}
      vide='Aucune facture ni aucun avoir émis dans la période.'
      total={(l) => l.montantHt}
      formatTotal={formatEuros}
      libelleLigne={(l) => `Ouvrir la pièce ${l.numero}`}
      onLigne={(l) =>
        navigate({ to: '/bo/facturation/$id', params: { id: String(l.piece) } })
      }
    />
  )
}
