import { useState } from 'react'
import { Navigate } from '@tanstack/react-router'
import { Plus } from 'lucide-react'
import { type Contrat } from '@/api/generated'
import { useRetour } from '@/hooks/use-retour'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { PageMetier } from '@/components/metier/page-metier'
import {
  aDesRetiraisons,
  droitsRetiraisons,
  versContrat,
} from './acces-retiraisons'
import { type Espace, useContrat } from './api'
import {
  CloturerRetiraisons,
  FormulaireRetiraison,
  ListeRetiraisons,
  RestesARetirer,
  type SaisieOuverte,
} from './retiraisons'

/**
 * Les retiraisons d'un contrat, sur mobile (spec 06 §6.2) : une page à part, ouverte depuis le contrat (« Gérer les
 * retiraisons ») ou par l'appui sur une notification de retiraison, entrée par la droite. Le reste à retirer, puis
 * chaque retiraison et ses actions ; « Déclarer une retiraison » pour l'acheteur, et en bas, « Fin des
 * retiraisons », « Clôturer les retiraisons » pour l'une ou l'autre partie (RG-CA-17). La flèche ramène au contrat.
 */
export function PageRetiraisons({
  espace,
  operateur,
  id,
}: {
  espace: Espace
  operateur: number
  id: number
}) {
  const contrat = useContrat(espace, operateur, id)
  const retour = useRetour(versContrat(espace, id))
  if (contrat.isPending) return <Skeleton className='m-4 h-96' />
  if (contrat.isError || !contrat.data)
    return (
      <p className='text-muted-foreground p-6'>Ce contrat est introuvable.</p>
    )
  if (!aDesRetiraisons(contrat.data))
    return <Navigate {...versContrat(espace, id)} replace />
  return (
    <DetailRetiraisons
      espace={espace}
      operateur={operateur}
      contrat={contrat.data}
      retour={retour}
    />
  )
}

function DetailRetiraisons({
  espace,
  operateur,
  contrat: c,
  retour,
}: {
  espace: Espace
  operateur: number
  contrat: Contrat
  retour: () => void
}) {
  const [saisie, setSaisie] = useState<SaisieOuverte>()
  const { agit, peutDeclarer } = droitsRetiraisons(espace, c)
  const autre =
    c.maPartie === 'VENDEUR'
      ? c.acheteur.raisonSociale
      : c.maPartie === 'ACHETEUR'
        ? c.vendeur.raisonSociale
        : `${c.vendeur.raisonSociale} et ${c.acheteur.raisonSociale}`
  return (
    <PageMetier
      titre='Retiraisons'
      description={`Contrat ${c.numero}, ${c.maPartie ? 'avec' : 'entre'} ${autre}`}
      retour={{ libelle: 'Retour au contrat', onRetour: retour }}
      entreeDroite
      actions={
        peutDeclarer && (
          <Button onClick={() => setSaisie({})} className='max-md:w-full'>
            <Plus /> Déclarer une retiraison
          </Button>
        )
      }
    >
      <div className='space-y-4'>
        <Card>
          <CardHeader>
            <CardTitle>Reste à retirer</CardTitle>
          </CardHeader>
          <CardContent>
            <RestesARetirer contrat={c} />
          </CardContent>
        </Card>
        <ListeRetiraisons
          operateur={operateur}
          contrat={c}
          agit={agit}
          onModifier={(r) => setSaisie({ retiraison: r })}
        />
      </div>
      {agit && <CloturerRetiraisons operateur={operateur} contrat={c} />}
      {saisie && (
        <FormulaireRetiraison
          operateur={operateur}
          contrat={c}
          retiraison={saisie.retiraison}
          onFermer={() => setSaisie(undefined)}
        />
      )}
    </PageMetier>
  )
}
