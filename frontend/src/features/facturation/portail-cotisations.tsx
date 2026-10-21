import { useQuery } from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'
import { listerMesLignesFacturees } from '@/api/generated'
import { Separator } from '@/components/ui/separator'
import { partMoyenne, typeDeclarant } from '@/components/metier/filiere'
import { PageMetier } from '@/components/metier/page-metier'
import { TableDonnees } from '@/components/metier/table-donnees'
import { formatEuros } from '@/features/declarations/format'
import { useMoi } from '@/features/session/session'
import { cleFacturation } from './api'
import { ChoixPeriode } from './choix-periode'
import { colonnesLignesVendues } from './colonnes-ventes'
import { GraphiquesCotisations } from './graphiques-ventes'
import { usePeriode } from './periode'

/** La nature d'origine des factures de cotisation : celles des DSM (spec 05). */
const NATURE_COTISATION = 'DSM'

const COLONNES = colonnesLignesVendues({ acheteur: false })

/**
 * Portail, menu « Cotisation » (spec 08 §6.2, RG-FAC-34) : le suivi de la facturation des cotisations de
 * l'opérateur actif, pendant de la page *Ventes* du back-office. Deux graphiques de démonstration à l'échelle de
 * l'opérateur, puis les lignes de ses factures de cotisation émises dans la période (date d'émission, l'année
 * civile en cours par défaut), avec le total HT des lignes affichées ; une ligne ouvre sa facture.
 */
export function PageMesCotisations() {
  const { actif } = useMoi()
  if (!actif) return null
  return (
    <MesCotisations
      key={actif.operateur}
      operateur={actif.operateur}
      type={actif.type}
    />
  )
}

function MesCotisations({
  operateur,
  type,
}: {
  operateur: number
  type: string
}) {
  const navigate = useNavigate()
  const { periode, setPeriode, valide } = usePeriode()
  const lignes = useQuery({
    queryKey: [...cleFacturation, 'portail', operateur, 'lignes', periode],
    queryFn: () =>
      listerMesLignesFacturees({
        path: { operateur },
        query: { ...periode, nature: NATURE_COTISATION },
        throwOnError: true,
      }).then((r) => r.data),
    enabled: valide,
  })
  return (
    <PageMetier
      titre='Cotisation'
      description='Suivi de la facturation de vos cotisations'
    >
      <GraphiquesCotisations echelle={partMoyenne(typeDeclarant(type))} />
      <Separator className='my-10' />
      <h2 className='mb-4 text-xl font-semibold'>Liste des cotisations</h2>
      <ChoixPeriode
        id='cotisations'
        periode={periode}
        valide={valide}
        onChange={setPeriode}
      />
      <div className='mb-8'>
        <TableDonnees
          cle='cotisations-portail'
          donnees={lignes.data ?? []}
          colonnes={COLONNES}
          chiffrees={['quantite', 'prixUnitaire', 'montantHt']}
          masquees={['code']}
          chargement={valide && lignes.isPending}
          vide='Aucune facture de cotisation émise dans la période.'
          total={(l) => l.montantHt}
          formatTotal={formatEuros}
          libelleLigne={(l) => `Ouvrir la facture ${l.numero}`}
          onLigne={(l) =>
            navigate({
              to: '/portail/factures/$id',
              params: { id: String(l.piece) },
            })
          }
        />
      </div>
    </PageMetier>
  )
}
