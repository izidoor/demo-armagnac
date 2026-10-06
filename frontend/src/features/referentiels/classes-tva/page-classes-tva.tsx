import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from '@tanstack/react-router'
import { ChevronRight, Plus } from 'lucide-react'
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
import { BadgeEtat } from '@/components/metier/badge-etat'
import { LignesEtat } from '@/components/metier/lignes-etat'
import { PageMetier } from '@/components/metier/page-metier'
import { BarreFiltre } from '../commun/filtre-valeurs'
import { useFiltreValeurs } from '../commun/use-filtre-valeurs'
import { FormulaireValeurSimple } from '../listes-simples/formulaire-valeur-simple'
import { formatDate, formatTaux } from './format'

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
    <PageMetier
      titre='Classes de TVA'
      description='Classes de TVA et historique de leurs taux ; les taux ne se chevauchent pas (§4.11.1, C.10.1-2). Cliquer une classe pour ouvrir son historique.'
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
              <TableHead>En vigueur depuis</TableHead>
              <TableHead className='w-28'>État</TableHead>
              <TableHead className='w-24' />
            </TableRow>
          </TableHeader>
          <TableBody>
            <LignesEtat requete={requete} colonnes={6} />
            {requete.data?.map((classe) => (
              <TableRow
                key={classe.id}
                className='cursor-pointer'
                onClick={() =>
                  navigate({
                    to: '/bo/parametrage/classes-tva/$id',
                    params: { id: String(classe.id) },
                  })
                }
              >
                <TableCell className='font-mono'>{classe.code}</TableCell>
                <TableCell>{classe.libelle}</TableCell>
                <TableCell className='text-end'>
                  {classe.tauxEnVigueur != null ? (
                    formatTaux(classe.tauxEnVigueur)
                  ) : (
                    <span className='text-muted-foreground'>Aucun</span>
                  )}
                </TableCell>
                <TableCell>
                  {classe.debutTauxEnVigueur &&
                    formatDate(classe.debutTauxEnVigueur)}
                </TableCell>
                <TableCell>
                  <BadgeEtat actif={classe.actif} />
                </TableCell>
                <TableCell className='text-end'>
                  <Button variant='ghost' size='sm' asChild>
                    <Link
                      to='/bo/parametrage/classes-tva/$id'
                      params={{ id: String(classe.id) }}
                      onClick={(e) => e.stopPropagation()}
                    >
                      Détail <ChevronRight />
                    </Link>
                  </Button>
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
    </PageMetier>
  )
}
