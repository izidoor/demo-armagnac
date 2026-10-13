import { useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { type Droits } from '@/api/generated'
import { Button } from '@/components/ui/button'
import {
  cleDeclarations,
  useDsms,
  useJournalDeclarations,
  usePointDeDepart,
} from './api'
import { DialoguePointDeDepart } from './dialogue-point-de-depart'
import { TableJournalDeclarations } from './journal'
import { ListeDsm } from './liste-dsm'
import { ResumePointDeDepart } from './point-de-depart'

/**
 * Onglet « Déclarations » de la fiche opérateur, au back-office (spec 05 §6.1) : le point de départ,
 * fixé ou modifié par le BNIA qui gère le module (RG-DEC-01, 06 à 08), les DSM de l'opérateur et le
 * journal de ses déclarations.
 */
export function OngletDeclarations({
  operateur,
  producteur,
  droits,
}: {
  operateur: number
  producteur: boolean
  droits: Droits
}) {
  const queryClient = useQueryClient()
  const point = usePointDeDepart(operateur)
  const dsms = useDsms(operateur)
  const journal = useJournalDeclarations(operateur)
  const [edition, setEdition] = useState(false)
  const gereBnia = droits.personnelBnia && droits.declarations === 'GESTION'
  const actuel = point.data ?? null

  return (
    <div className='space-y-6'>
      <section className='space-y-2'>
        <div className='flex flex-wrap items-center justify-between gap-2'>
          <h3 className='text-lg font-semibold'>Point de départ</h3>
          {gereBnia && point.isSuccess && !actuel?.fige && (
            <Button variant='outline' onClick={() => setEdition(true)}>
              {actuel
                ? 'Modifier le point de départ'
                : 'Fixer le point de départ'}
            </Button>
          )}
        </div>
        {point.isSuccess &&
          (actuel ? (
            <ResumePointDeDepart point={actuel} />
          ) : (
            <p className='text-muted-foreground text-sm'>
              Aucun point de départ : l'opérateur ne peut pas encore initier de
              DSM.
            </p>
          ))}
      </section>
      <section className='space-y-2'>
        <h3 className='text-lg font-semibold'>DSM</h3>
        <ListeDsm espace='bo' operateur={operateur} dsms={dsms.data ?? []} />
      </section>
      <section className='space-y-2'>
        <h3 className='text-lg font-semibold'>Journal</h3>
        <TableJournalDeclarations entrees={journal.data ?? []} avecDsm />
      </section>
      {edition && (
        <DialoguePointDeDepart
          operateur={operateur}
          producteur={producteur}
          point={actuel}
          onOpenChange={setEdition}
          onEnregistre={() => {
            toast.success('Point de départ enregistré.')
            queryClient.invalidateQueries({
              queryKey: cleDeclarations(operateur),
            })
          }}
        />
      )}
    </div>
  )
}
