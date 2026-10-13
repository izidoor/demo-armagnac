import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { Plus } from 'lucide-react'
import {
  creerPays,
  desactiverPays,
  listerPays,
  listerZonesCommerciales,
  modifierPays,
  reactiverPays,
  supprimerPays,
  type Pays,
} from '@/api/generated'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { BadgeEtat } from '@/components/metier/badge-etat'
import { LignesEtat } from '@/components/metier/lignes-etat'
import { PageMetier } from '@/components/metier/page-metier'
import { Pagination } from '@/components/metier/pagination'
import { usePagination } from '@/components/metier/use-pagination'
import { ChangementEtat } from '../commun/changement-etat'
import { BarreFiltre } from '../commun/filtre-valeurs'
import { MenuActions } from '../commun/menu-actions'
import { Suppression } from '../commun/suppression'
import { useFiltreValeurs } from '../commun/use-filtre-valeurs'
import { useGestionParametrage } from '../commun/use-gestion-parametrage'
import { FormulairePays, type SaisiePays } from './formulaire-pays'
import { LANGUES_CERTIFICAT, NIVEAUX_DETAIL } from './libelles'

function versRequete({ zoneCommerciale, ageMinimum, ...saisie }: SaisiePays) {
  return {
    ...saisie,
    zoneCommerciale: Number(zoneCommerciale),
    ageMinimum: ageMinimum || undefined,
  }
}

export function PagePays() {
  const filtreValeurs = useFiltreValeurs()
  const { filtre } = filtreValeurs
  const queryClient = useQueryClient()
  const gestion = useGestionParametrage()
  const requete = useQuery({
    queryKey: ['pays', filtre],
    queryFn: () =>
      listerPays({ query: filtre, throwOnError: true }).then((r) => r.data),
  })
  const { lignes, pagination } = usePagination(requete.data)
  const zones = useQuery({
    queryKey: ['zones-commerciales', {}],
    queryFn: () =>
      listerZonesCommerciales({ throwOnError: true }).then((r) => r.data),
  })
  const libelleZone = (id: number) =>
    zones.data?.find((z) => z.id === id)?.libelle ?? String(id)
  const rafraichir = () => queryClient.invalidateQueries({ queryKey: ['pays'] })

  const [edition, setEdition] = useState<Pays | 'nouveau' | null>(null)
  const [changement, setChangement] = useState<Pays | null>(null)
  const [suppression, setSuppression] = useState<Pays | null>(null)
  const paysEdite = edition === 'nouveau' ? undefined : (edition ?? undefined)

  return (
    <PageMetier
      titre='Pays'
      description="Zone commerciale, appartenance à l'UE, certificat export et âge minimum (C.10.5)."
      actions={
        gestion && (
          <Button onClick={() => setEdition('nouveau')}>
            <Plus /> Nouveau pays
          </Button>
        )
      }
    >
      <BarreFiltre
        {...filtreValeurs}
        placeholder='Rechercher un code, un code ISO ou un libellé'
      />
      <div className='rounded-md border'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Code</TableHead>
              <TableHead>ISO</TableHead>
              <TableHead>Libellé</TableHead>
              <TableHead>Zone</TableHead>
              <TableHead>UE</TableHead>
              <TableHead>Certificat</TableHead>
              <TableHead>Âge min.</TableHead>
              <TableHead>État</TableHead>
              <TableHead className='w-12' />
            </TableRow>
          </TableHeader>
          <TableBody>
            <LignesEtat requete={requete} colonnes={9} />
            {lignes?.map((pays) => (
              <TableRow key={pays.id}>
                <TableCell className='font-mono'>{pays.code}</TableCell>
                <TableCell className='font-mono'>{pays.codeIso}</TableCell>
                <TableCell>{pays.libelle}</TableCell>
                <TableCell>{libelleZone(pays.zoneCommerciale)}</TableCell>
                <TableCell>
                  {pays.membreUe && <Badge variant='secondary'>UE</Badge>}
                </TableCell>
                <TableCell className='text-sm'>
                  {LANGUES_CERTIFICAT[pays.langueCertificat]}
                  <div className='text-muted-foreground'>
                    {NIVEAUX_DETAIL[pays.niveauDetailCertificat]}
                  </div>
                </TableCell>
                <TableCell className='font-mono'>{pays.ageMinimum}</TableCell>
                <TableCell>
                  <BadgeEtat actif={pays.actif} />
                </TableCell>
                <TableCell>
                  <MenuActions
                    actif={pays.actif}
                    onModifier={() => setEdition(pays)}
                    onChangerEtat={() => setChangement(pays)}
                    onSupprimer={() => setSuppression(pays)}
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
      <Pagination {...pagination} />

      <FormulairePays
        ouvert={edition !== null}
        onOuvertChange={(ouvert) => !ouvert && setEdition(null)}
        pays={paysEdite}
        zones={zones.data ?? []}
        enregistrer={(saisie) =>
          paysEdite
            ? modifierPays({
                path: { id: paysEdite.id },
                body: versRequete(saisie),
                throwOnError: true,
              })
            : creerPays({
                body: versRequete(saisie),
                throwOnError: true,
              })
        }
        onSucces={rafraichir}
      />
      <ChangementEtat
        valeur={changement}
        onFermer={() => setChangement(null)}
        changer={() =>
          changement!.actif
            ? desactiverPays({
                path: { id: changement!.id },
                throwOnError: true,
              })
            : reactiverPays({
                path: { id: changement!.id },
                throwOnError: true,
              })
        }
        onSucces={rafraichir}
      />
      <Suppression
        valeur={suppression}
        onFermer={() => setSuppression(null)}
        supprimer={() =>
          supprimerPays({ path: { id: suppression!.id }, throwOnError: true })
        }
        onSucces={rafraichir}
      />
    </PageMetier>
  )
}
