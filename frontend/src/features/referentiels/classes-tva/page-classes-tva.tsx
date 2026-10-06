import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from '@tanstack/react-router'
import { Plus } from 'lucide-react'
import { creerClasseTva, listerClassesTva } from '@/api/generated'
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
import { BarreFiltre } from '../commun/filtre-valeurs'
import { LignesEtat } from '../commun/lignes-etat'
import { PageParametrage } from '../commun/page-parametrage'
import { useFiltreValeurs } from '../commun/use-filtre-valeurs'
import { FormulaireValeurSimple } from '../listes-simples/formulaire-valeur-simple'
import { formatTaux } from './format'

export function PageClassesTva() {
  const filtreValeurs = useFiltreValeurs()
  const { filtre } = filtreValeurs
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const requete = useQuery({
    queryKey: ['classes-tva', filtre],
    queryFn: () =>
      listerClassesTva({ query: filtre, throwOnError: true }).then(
        (r) => r.data
      ),
  })
  const [creation, setCreation] = useState(false)

  return (
    <PageParametrage
      titre='Classes de TVA'
      description='Classes de TVA et historique de leurs taux ; les taux ne se chevauchent pas (§4.11.1, C.10.1-2).'
      actions={
        <Button onClick={() => setCreation(true)}>
          <Plus /> Nouvelle classe
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
              <TableHead className='text-end'>Taux en vigueur</TableHead>
              <TableHead className='w-28'>État</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <LignesEtat requete={requete} colonnes={4} />
            {requete.data?.map((classe) => (
              <TableRow key={classe.id}>
                <TableCell className='font-mono'>
                  <Link
                    to='/bo/parametrage/classes-tva/$id'
                    params={{ id: String(classe.id) }}
                    className='underline-offset-4 hover:underline'
                  >
                    {classe.code}
                  </Link>
                </TableCell>
                <TableCell>{classe.libelle}</TableCell>
                <TableCell className='text-end'>
                  {classe.tauxEnVigueur != null ? (
                    formatTaux(classe.tauxEnVigueur)
                  ) : (
                    <span className='text-muted-foreground'>Aucun</span>
                  )}
                </TableCell>
                <TableCell>
                  <BadgeEtat actif={classe.actif} />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      <FormulaireValeurSimple
        ouvert={creation}
        onOuvertChange={setCreation}
        titre='Nouvelle classe de TVA'
        enregistrer={(saisie) =>
          creerClasseTva({ body: saisie, throwOnError: true }).then(
            (r) => r.data
          )
        }
        onSucces={(classe) => {
          queryClient.invalidateQueries({ queryKey: ['classes-tva'] })
          // Une classe naît sans taux : on l'ouvre pour saisir le premier.
          navigate({
            to: '/bo/parametrage/classes-tva/$id',
            params: { id: String(classe.id) },
          })
        }}
      />
    </PageParametrage>
  )
}
