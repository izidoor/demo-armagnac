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
import { BadgeEtat } from '../commun/badge-etat'
import { ChangementEtat } from '../commun/changement-etat'
import { BarreFiltre } from '../commun/filtre-valeurs'
import { LignesEtat } from '../commun/lignes-etat'
import { MenuActions } from '../commun/menu-actions'
import { PageParametrage } from '../commun/page-parametrage'
import { useFiltreValeurs } from '../commun/use-filtre-valeurs'
import { FormulaireValeurSimple } from './formulaire-valeur-simple'
import { type ConfigListeSimple } from './listes'

/** Page d'une liste simple : tableau, recherche, création, modification, (dés)activation. */
export function PageListeSimple({ config }: { config: ConfigListeSimple }) {
  const { api } = config
  const filtreValeurs = useFiltreValeurs()
  const { filtre } = filtreValeurs
  const queryClient = useQueryClient()
  const requete = useQuery({
    queryKey: [api.cle, filtre],
    queryFn: () => api.lister(filtre),
  })
  const rafraichir = () =>
    queryClient.invalidateQueries({ queryKey: [api.cle] })

  const [edition, setEdition] = useState<ValeurSimple | 'nouvelle' | null>(null)
  const [changement, setChangement] = useState<ValeurSimple | null>(null)
  const valeurEditee =
    edition === 'nouvelle' ? undefined : (edition ?? undefined)

  return (
    <PageParametrage
      titre={config.titre}
      description={config.description}
      actions={
        <Button onClick={() => setEdition('nouvelle')}>
          <Plus /> {config.libelleNouveau}
        </Button>
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
              <TableRow key={valeur.code}>
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
            ? api.modifier(valeurEditee.code, saisie.libelle)
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
            ? api.desactiver(changement!.code)
            : api.reactiver(changement!.code)
        }
        onSucces={rafraichir}
      />
    </PageParametrage>
  )
}
