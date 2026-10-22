import { useEffect, useState } from 'react'
import { z } from 'zod'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Plus } from 'lucide-react'
import {
  ajouterTauxCvo,
  listerTauxCvo,
  modifierTauxCvo,
  supprimerTauxCvo,
  type TauxCvo,
} from '@/api/generated'
import { cn } from '@/lib/utils'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { DialogFooter } from '@/components/ui/dialog'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { DialogueFormulaire } from '@/components/metier/dialogue-formulaire'
import { afficherErreur } from '@/components/metier/erreur-formulaire'
import { ErreurRacine } from '@/components/metier/erreur-racine'
import { PageMetier } from '@/components/metier/page-metier'
import {
  ActionsHistorique,
  COLONNE_ACTIONS,
  SECONDAIRE,
  SOUS_LE_TAUX,
} from '@/features/referentiels/commun/actions-historique'
import { formatDate, formatEuros, formatPourcentage } from './format'

const CLE = ['declarations', 'taux-cvo'] as const

/** Le taux dont la période contient aujourd'hui. */
function enVigueur(taux: TauxCvo) {
  const aujourdhui = new Date().toISOString().slice(0, 10)
  return taux.debut <= aujourdhui && (!taux.fin || aujourdhui <= taux.fin)
}

/**
 * La grille des taux CVO, menu Paramétrage, rubrique Cotisation (spec 05 §3.5, §6.1) : une chaîne continue,
 * chaque taux courant de son début à la veille du suivant, le dernier sans fin (RG-DEC-30). Ajouter, corriger
 * ou supprimer un taux recompose la chaîne ; seules les DSM non facturées en dépendent (H-DEC-20).
 */
export function PageTauxCvo() {
  const queryClient = useQueryClient()
  const requete = useQuery({
    queryKey: CLE,
    queryFn: () => listerTauxCvo({ throwOnError: true }).then((r) => r.data),
  })
  const rafraichir = () =>
    queryClient.invalidateQueries({ queryKey: ['declarations'] })
  const [edition, setEdition] = useState<TauxCvo | 'nouveau' | null>(null)
  const [suppression, setSuppression] = useState<TauxCvo | null>(null)
  // Du plus récent au plus ancien, comme les classes de TVA.
  const taux = [...(requete.data ?? [])].reverse()

  const supprimer = useMutation({
    mutationFn: (t: TauxCvo) =>
      supprimerTauxCvo({ path: { debut: t.debut }, throwOnError: true }),
    meta: { erreurAffichee: true },
    onSuccess: () => {
      rafraichir()
      setSuppression(null)
    },
    onError: () => setSuppression(null),
  })

  return (
    <PageMetier
      titre='Taux CVO'
      description='La cotisation des DSM, en euros par hectolitre d’alcool pur ; chaque taux vaut jusqu’à la veille du suivant.'
      actions={
        <Button onClick={() => setEdition('nouveau')}>
          <Plus /> Ajouter un taux
        </Button>
      }
    >
      <div className='rounded-md border'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className='text-end'>Montant</TableHead>
              <TableHead className={cn('text-end', SECONDAIRE)}>
                Exonéré de TVA
              </TableHead>
              <TableHead>Début</TableHead>
              <TableHead>Fin</TableHead>
              <TableHead className={SECONDAIRE} />
              <TableHead className={COLONNE_ACTIONS} />
            </TableRow>
          </TableHeader>
          <TableBody>
            {taux.length === 0 && (
              <TableRow>
                <TableCell
                  colSpan={6}
                  className='text-muted-foreground h-16 text-center'
                >
                  {requete.isPending
                    ? 'Chargement…'
                    : 'Aucun taux : la cotisation des DSM est non calculable.'}
                </TableCell>
              </TableRow>
            )}
            {taux.map((t) => (
              <TableRow
                key={t.debut}
                className={enVigueur(t) ? 'bg-muted/50' : ''}
              >
                <TableCell className='text-end font-medium whitespace-normal tabular-nums'>
                  {formatEuros(t.montant)}/hlAP
                  {/* Sur un contenu étroit, la part exonérée et « En vigueur » passent sous le montant. */}
                  <span
                    className={`text-muted-foreground block text-xs font-normal ${SOUS_LE_TAUX}`}
                  >
                    dont {formatPourcentage(t.pourcentageExonere)} exonéré
                  </span>
                  {enVigueur(t) && (
                    <Badge className={`mt-1 ${SOUS_LE_TAUX}`}>En vigueur</Badge>
                  )}
                </TableCell>
                <TableCell className={cn('text-end tabular-nums', SECONDAIRE)}>
                  {formatPourcentage(t.pourcentageExonere)}
                </TableCell>
                <TableCell>{formatDate(t.debut)}</TableCell>
                <TableCell>
                  {t.fin ? (
                    formatDate(t.fin)
                  ) : (
                    <span className='text-muted-foreground'>Sans terme</span>
                  )}
                </TableCell>
                <TableCell className={SECONDAIRE}>
                  {enVigueur(t) && <Badge>En vigueur</Badge>}
                </TableCell>
                <TableCell className='text-end'>
                  <ActionsHistorique
                    onModifier={() => setEdition(t)}
                    onRetirer={() => setSuppression(t)}
                    retirer='Supprimer'
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
      <FormulaireTauxCvo
        ouvert={edition !== null}
        onOuvertChange={(ouvert) => !ouvert && setEdition(null)}
        taux={edition === 'nouveau' ? undefined : (edition ?? undefined)}
        onSucces={rafraichir}
      />
      <ConfirmDialog
        open={suppression !== null}
        onOpenChange={(ouvert) => !ouvert && setSuppression(null)}
        title='Supprimer ce taux ?'
        desc={
          suppression
            ? `Le taux de ${formatEuros(suppression.montant)}/hlAP du ${formatDate(suppression.debut)} sera supprimé ; le taux précédent courra jusqu'à la veille du suivant. La cotisation des DSM non facturées sera recalculée.`
            : ''
        }
        confirmText='Supprimer'
        cancelBtnText='Annuler'
        destructive
        isLoading={supprimer.isPending}
        handleConfirm={() => suppression && supprimer.mutate(suppression)}
      />
    </PageMetier>
  )
}

const decimal = (message: string) =>
  z
    .string()
    .trim()
    .regex(/^\d{1,6}([.,]\d{1,2})?$/, message)

const schema = z.object({
  montant: decimal('Un montant en euros, deux décimales au plus.').refine(
    (m) => Number(m.replace(',', '.')) > 0,
    { message: 'Le montant est strictement positif.' }
  ),
  pourcentageExonere: decimal('Un pourcentage, deux décimales au plus.').refine(
    (p) => Number(p.replace(',', '.')) <= 100,
    {
      message: 'Le pourcentage est compris entre 0 et 100.',
    }
  ),
  debut: z.string().min(1, 'La date de début est obligatoire.'),
})

type Saisie = z.infer<typeof schema>

/** Ajout ou correction d'un taux ; un début déjà pris est refusé par le serveur (RG-DEC-30). */
function FormulaireTauxCvo({
  ouvert,
  onOuvertChange,
  taux,
  onSucces,
}: {
  ouvert: boolean
  onOuvertChange: (ouvert: boolean) => void
  /** Taux corrigé ; absent pour un ajout. */
  taux?: TauxCvo
  onSucces: () => void
}) {
  const form = useForm<Saisie>({
    resolver: zodResolver(schema),
    defaultValues: { montant: '', pourcentageExonere: '', debut: '' },
  })

  useEffect(() => {
    if (ouvert)
      form.reset(
        taux
          ? {
              montant: String(taux.montant).replace('.', ','),
              pourcentageExonere: String(taux.pourcentageExonere).replace(
                '.',
                ','
              ),
              debut: taux.debut,
            }
          : { montant: '', pourcentageExonere: '', debut: '' }
      )
  }, [ouvert, taux, form])

  const mutation = useMutation({
    mutationFn: (saisie: Saisie) => {
      const body = {
        montant: Number(saisie.montant.replace(',', '.')),
        pourcentageExonere: Number(saisie.pourcentageExonere.replace(',', '.')),
        debut: saisie.debut,
      }
      return taux
        ? modifierTauxCvo({
            path: { debut: taux.debut },
            body,
            throwOnError: true,
          })
        : ajouterTauxCvo({ body, throwOnError: true })
    },
    meta: { erreurAffichee: true },
    onSuccess: () => {
      onSucces()
      onOuvertChange(false)
    },
    onError: (error) => afficherErreur(form, error),
  })

  return (
    <DialogueFormulaire
      ouvert={ouvert}
      onOuvertChange={onOuvertChange}
      className='sm:max-w-md'
      titre={taux ? 'Corriger le taux CVO' : 'Ajouter un taux CVO'}
      description="Le taux vaut à partir de son début, jusqu'à la veille du taux suivant ; il clôt le précédent."
    >
      <Form {...form}>
        <form
          id='formulaire-taux-cvo'
          onSubmit={form.handleSubmit((saisie) => mutation.mutate(saisie))}
          className='space-y-4'
        >
          <ErreurRacine errors={form.formState.errors} />
          <div className='grid grid-cols-2 gap-4'>
            <FormField
              control={form.control}
              name='montant'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Montant (€/hlAP)</FormLabel>
                  <FormControl>
                    <Input {...field} inputMode='decimal' placeholder='60,00' />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name='pourcentageExonere'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Exonéré de TVA (%)</FormLabel>
                  <FormControl>
                    <Input {...field} inputMode='decimal' placeholder='20,00' />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
          </div>
          <FormField
            control={form.control}
            name='debut'
            render={({ field }) => (
              <FormItem>
                <FormLabel>Début</FormLabel>
                <FormControl>
                  <Input type='date' {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
        </form>
      </Form>
      <DialogFooter>
        <Button
          variant='outline'
          className='max-md:hidden'
          onClick={() => onOuvertChange(false)}
        >
          Annuler
        </Button>
        <Button
          className='max-md:w-full'
          type='submit'
          form='formulaire-taux-cvo'
          disabled={mutation.isPending}
        >
          Enregistrer
        </Button>
      </DialogFooter>
    </DialogueFormulaire>
  )
}
