import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { Plus } from 'lucide-react'
import { type ValeurSimple } from '@/api/generated'
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
import { ChangementEtat } from '../commun/changement-etat'
import { BarreFiltre } from '../commun/filtre-valeurs'
import { MenuActions } from '../commun/menu-actions'
import { Suppression } from '../commun/suppression'
import { useFiltreValeurs } from '../commun/use-filtre-valeurs'
import { useGestionParametrage } from '../commun/use-gestion-parametrage'
import { FormulaireValeurSimple } from './formulaire-valeur-simple'
import { type ConfigListeSimple } from './listes'

/** Page d'une liste simple : tableau, recherche, création, modification, (dés)activation, suppression. */
export function PageListeSimple({ config }: { config: ConfigListeSimple }) {
  const { api } = config
  const filtreValeurs = useFiltreValeurs()
  const { filtre } = filtreValeurs
  const queryClient = useQueryClient()
  const gestion = useGestionParametrage()
  const requete = useQuery({
    queryKey: [api.cle, filtre],
    queryFn: () => api.lister(filtre),
  })
  const rafraichir = () =>
    queryClient.invalidateQueries({ queryKey: [api.cle] })

  const [edition, setEdition] = useState<ValeurSimple | 'nouvelle' | null>(null)
  const [changement, setChangement] = useState<ValeurSimple | null>(null)
  const [suppression, setSuppression] = useState<ValeurSimple | null>(null)
  const valeurEditee =
    edition === 'nouvelle' ? undefined : (edition ?? undefined)

  return (
    <PageMetier
      titre={config.titre}
      description={config.description}
      actions={
        gestion && (
          <Button onClick={() => setEdition('nouvelle')}>
            <Plus /> {config.libelleNouveau}
          </Button>
        )
      }
    >
      <BarreFiltre {...filtreValeurs} />
      <div className='rounded-md border'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className='w-40'>Code</TableHead>
              <TableHead>Libellé</TableHead>
              <TableHead className='w-28'>État</TableHead>
              <TableHead className='w-12' />
            </TableRow>
          </TableHeader>
          <TableBody>
            <LignesEtat requete={requete} colonnes={4} />
            {requete.data?.map((valeur) => (
              <TableRow key={valeur.id}>
                <TableCell className='font-mono'>{valeur.code}</TableCell>
                <TableCell>{valeur.libelle}</TableCell>
                <TableCell>
                  <BadgeEtat actif={valeur.actif} />
                </TableCell>
                <TableCell>
                  <MenuActions
                    actif={valeur.actif}
                    onModifier={() => setEdition(valeur)}
                    onChangerEtat={() => setChangement(valeur)}
                    onSupprimer={() => setSuppression(valeur)}
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      <FormulaireValeurSimple
        ouvert={edition !== null}
        onOuvertChange={(ouvert) => !ouvert && setEdition(null)}
        titre={
          valeurEditee
            ? `Modifier « ${valeurEditee.libelle} »`
            : config.libelleNouveau
        }
        valeur={valeurEditee}
        enregistrer={(saisie) =>
          valeurEditee
            ? api.modifier(valeurEditee.id, saisie)
            : api.creer(saisie)
        }
        onSucces={rafraichir}
        proches={api.proches}
      />
      <ChangementEtat
        valeur={changement}
        onFermer={() => setChangement(null)}
        changer={() =>
          changement!.actif
            ? api.desactiver(changement!.id)
            : api.reactiver(changement!.id)
        }
        onSucces={rafraichir}
      />
      <Suppression
        valeur={suppression}
        onFermer={() => setSuppression(null)}
        supprimer={() => api.supprimer(suppression!.id)}
        onSucces={rafraichir}
      />
    </PageMetier>
  )
}
