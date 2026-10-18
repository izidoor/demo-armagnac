import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { ChevronDown } from 'lucide-react'
import { toast } from 'sonner'
import { type Droits, type PropositionPointDeDepart } from '@/api/generated'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardAction,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from '@/components/ui/collapsible'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group'
import {
  apiDeclarations,
  cleDeclarations,
  useDsms,
  useHistoriquePointsDeDepart,
  useJournalDeclarations,
  usePointDeDepart,
  useStockDeclare,
} from './api'
import { DialoguePointDeDepart } from './dialogue-point-de-depart'
import { formatDate, formatDateHeure } from './format'
import { DialogueImportExcel } from './import-excel'
import { TableJournalDeclarations } from './journal'
import { ListeDsm } from './liste-dsm'
import { ResumePointDeDepart } from './point-de-depart'
import { ResumeStockDeclare } from './stock-declare'

/**
 * Onglet « Stocks & Mouvements » de la fiche opérateur, au back-office (spec 05 §6.1) : le point de départ,
 * fixé ou modifié par le BNIA qui gère le module (RG-DEC-01, 06 à 08), saisi ou importé du modèle Excel
 * (RG-DEC-37, 38), les DSM de l'opérateur et le journal de ses déclarations. Lot 7 : un nouveau point de
 * départ quand le précédent est figé et qu'aucune DSM n'est en cours, et les points précédents en lecture
 * (RG-DEC-09) ; lot 8 : le stock déclaré (RG-DEC-42). Chaque partie dans un cadre : DSM, stocks déclarés,
 * stock initial (le point de départ), stocks initiaux précédents, puis le journal, dépliable.
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
  const historique = useHistoriquePointsDeDepart(operateur)
  const stock = useStockDeclare(operateur)
  const [proposition, setProposition] =
    useState<PropositionPointDeDepart | null>(null)
  // RG-DEC-09 : le stock proposé est chargé avant d'ouvrir le dialogue.
  const proposer = useMutation({
    mutationFn: () => apiDeclarations(operateur).proposition(),
    meta: { erreurAffichee: true },
    onSuccess: (p) => p && setProposition(p),
  })
  const [edition, setEdition] = useState(false)
  const [import_, setImport] = useState(false)
  const [dateChoisie, setDateChoisie] = useState(false)
  const [debut, setDebut] = useState('')
  const gereBnia = droits.personnelBnia && droits.declarations === 'GESTION'
  const actuel = point.data ?? null
  const precedents = (historique.data ?? []).filter((p) => p.id !== actuel?.id)

  return (
    <div className='space-y-4'>
      <Card>
        <CardHeader>
          <CardTitle>DSM</CardTitle>
        </CardHeader>
        <CardContent>
          <ListeDsm espace='bo' operateur={operateur} dsms={dsms.data ?? []} />
        </CardContent>
      </Card>
      <Card>
        <CardHeader>
          <CardTitle>Stocks déclarés</CardTitle>
        </CardHeader>
        <CardContent>
          {stock.isSuccess && <ResumeStockDeclare stock={stock.data} />}
        </CardContent>
      </Card>
      <Card>
        <CardHeader>
          <CardTitle>Stock initial</CardTitle>
          {gereBnia && actuel?.renouvelable && (
            <CardAction className='flex flex-wrap justify-end gap-2'>
              <Button variant='outline' onClick={() => setImport(true)}>
                Importer depuis Excel
              </Button>
              <Button
                variant='outline'
                disabled={proposer.isPending}
                onClick={() => proposer.mutate()}
              >
                Fixer un nouveau stock initial
              </Button>
            </CardAction>
          )}
          {gereBnia && point.isSuccess && !actuel?.fige && (
            <CardAction className='flex flex-wrap justify-end gap-2'>
              <Button variant='outline' onClick={() => setImport(true)}>
                Importer depuis Excel
              </Button>
              <Button variant='outline' onClick={() => setEdition(true)}>
                {actuel
                  ? 'Modifier le stock initial'
                  : 'Fixer le stock initial'}
              </Button>
            </CardAction>
          )}
        </CardHeader>
        <CardContent>
          {point.isSuccess &&
            (actuel ? (
              <ResumePointDeDepart point={actuel} />
            ) : (
              <p className='text-muted-foreground text-sm'>
                Aucun point de départ : l'opérateur ne peut pas encore initier
                de DSM.
              </p>
            ))}
        </CardContent>
      </Card>
      {precedents.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Stocks initiaux précédents</CardTitle>
          </CardHeader>
          <CardContent>
            <ul className='space-y-1 text-sm'>
              {precedents.map((p) => (
                <li key={p.id} className='flex flex-wrap items-center gap-2'>
                  Début au <strong>{formatDate(p.debut)}</strong>,{' '}
                  {p.lignes.length} ligne{p.lignes.length > 1 ? 's' : ''} de
                  stock initial, fixé par {p.fixePar} le{' '}
                  {formatDateHeure(p.fixeLe)}
                  {p.fige && <Badge variant='secondary'>Figé</Badge>}
                </li>
              ))}
            </ul>
          </CardContent>
        </Card>
      )}
      <Collapsible asChild className='group'>
        <Card>
          <CardHeader>
            <CollapsibleTrigger className='hover:text-primary flex w-full items-center justify-between text-left'>
              <CardTitle>
                Journal des déclarations de stocks et mouvements
              </CardTitle>
              <ChevronDown className='text-muted-foreground size-4 shrink-0 transition-transform group-data-[state=open]:rotate-180' />
            </CollapsibleTrigger>
          </CardHeader>
          <CollapsibleContent>
            <CardContent>
              <TableJournalDeclarations entrees={journal.data ?? []} avecDsm />
            </CardContent>
          </CollapsibleContent>
        </Card>
      </Collapsible>
      <DialogueImportExcel
        titre='Importer le stock initial depuis le modèle Excel'
        description={
          <>
            Le stock de fin de chaque ligne du fichier (colonnes 1 à 25,
            calculé) devient le stock initial ; les lignes finies à 0 ne sont
            pas reprises.{' '}
            {actuel?.renouvelable
              ? 'Un nouveau point de départ est fixé ; le précédent reste consultable.'
              : actuel
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
      {proposition && (
        <DialoguePointDeDepart
          operateur={operateur}
          producteur={producteur}
          point={null}
          proposition={proposition}
          onOpenChange={(ouvert) => !ouvert && setProposition(null)}
          onEnregistre={() => {
            toast.success('Nouveau point de départ fixé.')
            queryClient.invalidateQueries({
              queryKey: cleDeclarations(operateur),
            })
          }}
        />
      )}
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
