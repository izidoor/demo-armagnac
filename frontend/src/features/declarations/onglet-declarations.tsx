import { useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { type Droits } from '@/api/generated'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group'
import {
  apiDeclarations,
  cleDeclarations,
  useDsms,
  useJournalDeclarations,
  usePointDeDepart,
} from './api'
import { DialoguePointDeDepart } from './dialogue-point-de-depart'
import { DialogueImportExcel } from './import-excel'
import { TableJournalDeclarations } from './journal'
import { ListeDsm } from './liste-dsm'
import { ResumePointDeDepart } from './point-de-depart'

/**
 * Onglet « Déclarations » de la fiche opérateur, au back-office (spec 05 §6.1) : le point de départ,
 * fixé ou modifié par le BNIA qui gère le module (RG-DEC-01, 06 à 08), saisi ou importé du modèle Excel
 * (RG-DEC-37, 38), les DSM de l'opérateur et le journal de ses déclarations.
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
  const [import_, setImport] = useState(false)
  const [dateChoisie, setDateChoisie] = useState(false)
  const [debut, setDebut] = useState('')
  const gereBnia = droits.personnelBnia && droits.declarations === 'GESTION'
  const actuel = point.data ?? null

  return (
    <div className='space-y-6'>
      <section className='space-y-2'>
        <div className='flex flex-wrap items-center justify-between gap-2'>
          <h3 className='text-lg font-semibold'>Point de départ</h3>
          {gereBnia && point.isSuccess && !actuel?.fige && (
            <div className='flex flex-wrap gap-2'>
              <Button variant='outline' onClick={() => setImport(true)}>
                Importer depuis Excel
              </Button>
              <Button variant='outline' onClick={() => setEdition(true)}>
                {actuel
                  ? 'Modifier le point de départ'
                  : 'Fixer le point de départ'}
              </Button>
            </div>
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
      <DialogueImportExcel
        titre='Importer le stock initial depuis le modèle Excel'
        description={
          <>
            Le stock de fin de chaque ligne du fichier (colonnes 1 à 25,
            calculé) devient le stock initial ; les lignes finies à 0 ne sont
            pas reprises.{' '}
            {actuel
              ? 'Le point de départ en vigueur est remplacé ; la DSM en cours suit.'
              : 'Le point de départ est fixé.'}{' '}
            Un fichier en erreur est refusé en entier.
          </>
        }
        champs={
          <div className='space-y-2'>
            <Label>Date de début du point de départ</Label>
            <RadioGroup
              value={dateChoisie ? 'choisie' : 'fichier'}
              onValueChange={(v) => setDateChoisie(v === 'choisie')}
            >
              <div className='flex items-center gap-2'>
                <RadioGroupItem value='fichier' id='debut-fichier' />
                <Label htmlFor='debut-fichier' className='font-normal'>
                  Le lendemain de la période du fichier
                </Label>
              </div>
              <div className='flex items-center gap-2'>
                <RadioGroupItem value='choisie' id='debut-choisi' />
                <Label htmlFor='debut-choisi' className='font-normal'>
                  Une autre date
                </Label>
                {dateChoisie && (
                  <Input
                    type='date'
                    className='w-44'
                    value={debut}
                    onChange={(e) => setDebut(e.target.value)}
                  />
                )}
              </div>
            </RadioGroup>
            <p className='text-muted-foreground text-xs'>
              {producteur
                ? 'Un 1er avril pour un producteur.'
                : 'Un premier du mois pour un négociant.'}
            </p>
          </div>
        }
        ouvert={import_}
        onOuvertChange={(ouvert) => {
          setImport(ouvert)
          if (!ouvert) {
            setDateChoisie(false)
            setDebut('')
          }
        }}
        importer={(fichier) =>
          apiDeclarations(operateur).importerPointDeDepart(
            fichier,
            dateChoisie && debut ? debut : undefined,
            actuel?.version
          )
        }
        onImporte={() =>
          queryClient.invalidateQueries({
            queryKey: cleDeclarations(operateur),
          })
        }
      />
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
