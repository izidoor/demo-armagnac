import { useEffect, useState } from 'react'
import { z } from 'zod'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link } from '@tanstack/react-router'
import { ArrowLeft, Plus } from 'lucide-react'
import {
  ajouterTauxTva,
  consulterClasseTva,
  desactiverClasseTva,
  modifierClasseTva,
  modifierTauxTva,
  reactiverClasseTva,
  retirerTauxTva,
  type ClasseTva,
  type TauxTva,
} from '@/api/generated'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {
  Form,
  FormControl,
  FormDescription,
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
import { BadgeEtat } from '../commun/badge-etat'
import { ChangementEtat } from '../commun/changement-etat'
import { afficherErreur } from '../commun/erreur-formulaire'
import { ErreurRacine } from '../commun/erreur-racine'
import { PageParametrage } from '../commun/page-parametrage'
import { FormulaireValeurSimple } from '../listes-simples/formulaire-valeur-simple'
import { formatDate, formatTaux } from './format'

/**
 * Détail d'une classe de TVA : l'historique de ses taux, du plus récent au plus ancien, avec le taux
 * en vigueur mis en évidence. Les taux restent consultables si la classe est inactive (RG-REF-13).
 */
export function PageClasseTva({ code }: { code: string }) {
  const queryClient = useQueryClient()
  const requete = useQuery({
    queryKey: ['classes-tva', 'detail', code],
    queryFn: () =>
      consulterClasseTva({ path: { code }, throwOnError: true }).then(
        (r) => r.data
      ),
  })
  const rafraichir = () =>
    queryClient.invalidateQueries({ queryKey: ['classes-tva'] })

  const [renommage, setRenommage] = useState(false)
  const [changementEtat, setChangementEtat] = useState(false)
  const [editionTaux, setEditionTaux] = useState<TauxTva | 'nouveau' | null>(
    null
  )
  const [retrait, setRetrait] = useState<TauxTva | null>(null)
  const classe = requete.data

  const retirer = useMutation({
    mutationFn: (taux: TauxTva) =>
      retirerTauxTva({
        path: { code, dateDebut: taux.dateDebut },
        throwOnError: true,
      }),
    onSuccess: () => {
      rafraichir()
      setRetrait(null)
    },
  })

  const retour = (
    <Button variant='ghost' size='sm' asChild className='-ms-2 mb-2'>
      <Link to='/bo/parametrage/classes-tva'>
        <ArrowLeft /> Classes de TVA
      </Link>
    </Button>
  )

  if (!classe) {
    return (
      <PageParametrage titre='Classe de TVA' description={code}>
        {retour}
        <p className='text-muted-foreground'>
          {requete.isError ? 'Classe introuvable.' : 'Chargement…'}
        </p>
      </PageParametrage>
    )
  }

  const enVigueur = classe.tauxEnVigueur?.dateDebut

  return (
    <PageParametrage
      titre={classe.libelle}
      description={
        <span className='flex items-center gap-2'>
          <span className='font-mono'>{classe.code}</span>
          <BadgeEtat actif={classe.actif} />
        </span>
      }
      actions={
        <div className='flex gap-2'>
          <Button variant='outline' onClick={() => setRenommage(true)}>
            Modifier le libellé
          </Button>
          <Button variant='outline' onClick={() => setChangementEtat(true)}>
            {classe.actif ? 'Désactiver' : 'Réactiver'}
          </Button>
          <Button onClick={() => setEditionTaux('nouveau')}>
            <Plus /> Ajouter un taux
          </Button>
        </div>
      }
    >
      {retour}
      <div className='rounded-md border'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className='text-end'>Taux</TableHead>
              <TableHead>Début</TableHead>
              <TableHead>Fin</TableHead>
              <TableHead />
              <TableHead className='w-48' />
            </TableRow>
          </TableHeader>
          <TableBody>
            {classe.taux.length === 0 && (
              <TableRow>
                <TableCell
                  colSpan={5}
                  className='text-muted-foreground h-16 text-center'
                >
                  Aucun taux : la classe n'a de taux applicable à aucune date.
                </TableCell>
              </TableRow>
            )}
            {classe.taux.map((taux) => (
              <TableRow
                key={taux.dateDebut}
                className={taux.dateDebut === enVigueur ? 'bg-muted/50' : ''}
              >
                <TableCell className='text-end font-medium'>
                  {formatTaux(taux.taux)}
                </TableCell>
                <TableCell>{formatDate(taux.dateDebut)}</TableCell>
                <TableCell>
                  {taux.dateFin ? (
                    formatDate(taux.dateFin)
                  ) : (
                    <span className='text-muted-foreground'>Sans terme</span>
                  )}
                </TableCell>
                <TableCell>
                  {taux.dateDebut === enVigueur && <Badge>En vigueur</Badge>}
                </TableCell>
                <TableCell className='text-end'>
                  <Button
                    variant='ghost'
                    size='sm'
                    onClick={() => setEditionTaux(taux)}
                  >
                    Modifier
                  </Button>
                  <Button
                    variant='ghost'
                    size='sm'
                    onClick={() => setRetrait(taux)}
                  >
                    Retirer
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      <FormulaireValeurSimple
        ouvert={renommage}
        onOuvertChange={setRenommage}
        titre={`Modifier « ${classe.libelle} »`}
        valeur={classe}
        enregistrer={(saisie) =>
          modifierClasseTva({
            path: { code },
            body: { libelle: saisie.libelle },
            throwOnError: true,
          })
        }
        onSucces={rafraichir}
      />
      <ChangementEtat
        valeur={changementEtat ? classe : null}
        onFermer={() => setChangementEtat(false)}
        changer={() =>
          classe.actif
            ? desactiverClasseTva({ path: { code }, throwOnError: true })
            : reactiverClasseTva({ path: { code }, throwOnError: true })
        }
        onSucces={rafraichir}
      />
      <FormulaireTaux
        ouvert={editionTaux !== null}
        onOuvertChange={(ouvert) => !ouvert && setEditionTaux(null)}
        classe={classe}
        taux={
          editionTaux === 'nouveau' ? undefined : (editionTaux ?? undefined)
        }
        onSucces={rafraichir}
      />
      <ConfirmDialog
        open={retrait !== null}
        onOpenChange={(ouvert) => !ouvert && setRetrait(null)}
        title='Retirer ce taux ?'
        desc={
          retrait
            ? `Le taux de ${formatTaux(retrait.taux)} du ${formatDate(retrait.dateDebut)} sera retiré de l'historique. À réserver aux erreurs de saisie : les factures déjà émises ont figé leur taux.`
            : ''
        }
        confirmText='Retirer'
        cancelBtnText='Annuler'
        destructive
        isLoading={retirer.isPending}
        handleConfirm={() => retrait && retirer.mutate(retrait)}
      />
    </PageParametrage>
  )
}

const schemaTaux = z
  .object({
    taux: z
      .string()
      .trim()
      .regex(
        /^\d{1,3}([.,]\d{1,2})?$/,
        'Un pourcentage, deux décimales au plus.'
      )
      .refine((t) => Number(t.replace(',', '.')) <= 100, {
        message: 'Le taux est compris entre 0 et 100.',
      }),
    dateDebut: z.string().min(1, 'La date de début est obligatoire.'),
    dateFin: z.string(),
  })
  .refine((s) => !s.dateFin || s.dateFin >= s.dateDebut, {
    message: 'La date de fin précède la date de début.',
    path: ['dateFin'],
  })

type SaisieTaux = z.infer<typeof schemaTaux>

type FormulaireTauxProps = {
  ouvert: boolean
  onOuvertChange: (ouvert: boolean) => void
  classe: ClasseTva
  /** Taux corrigé ; absent pour un ajout. */
  taux?: TauxTva
  onSucces: () => void
}

/** Ajout ou correction d'un taux ; un chevauchement est refusé par le serveur (RG-REF-11). */
function FormulaireTaux({
  ouvert,
  onOuvertChange,
  classe,
  taux,
  onSucces,
}: FormulaireTauxProps) {
  const form = useForm<SaisieTaux>({
    resolver: zodResolver(schemaTaux),
    defaultValues: { taux: '', dateDebut: '', dateFin: '' },
  })

  useEffect(() => {
    if (ouvert)
      form.reset(
        taux
          ? {
              taux: String(taux.taux).replace('.', ','),
              dateDebut: taux.dateDebut,
              dateFin: taux.dateFin ?? '',
            }
          : { taux: '', dateDebut: '', dateFin: '' }
      )
  }, [ouvert, taux, form])

  const mutation = useMutation({
    mutationFn: (saisie: SaisieTaux) => {
      const body = {
        taux: Number(saisie.taux.replace(',', '.')),
        dateDebut: saisie.dateDebut,
        dateFin: saisie.dateFin || undefined,
      }
      return taux
        ? modifierTauxTva({
            path: { code: classe.code, dateDebut: taux.dateDebut },
            body,
            throwOnError: true,
          })
        : ajouterTauxTva({
            path: { code: classe.code },
            body,
            throwOnError: true,
          })
    },
    meta: { erreurAffichee: true },
    onSuccess: () => {
      onSucces()
      onOuvertChange(false)
    },
    onError: (error) => afficherErreur(form, error),
  })

  return (
    <Dialog open={ouvert} onOpenChange={onOuvertChange}>
      <DialogContent className='sm:max-w-md'>
        <DialogHeader>
          <DialogTitle>
            {taux ? 'Corriger le taux' : 'Ajouter un taux'}
          </DialogTitle>
          <DialogDescription>
            Les périodes des taux d'une classe ne doivent pas se chevaucher ;
            des trous sont permis.
          </DialogDescription>
        </DialogHeader>
        <Form {...form}>
          <form
            id='formulaire-taux'
            onSubmit={form.handleSubmit((saisie) => mutation.mutate(saisie))}
            className='space-y-4'
          >
            <ErreurRacine errors={form.formState.errors} />
            <FormField
              control={form.control}
              name='taux'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Taux (%)</FormLabel>
                  <FormControl>
                    <Input {...field} inputMode='decimal' placeholder='20,00' />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <div className='grid grid-cols-2 gap-4'>
              <FormField
                control={form.control}
                name='dateDebut'
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
              <FormField
                control={form.control}
                name='dateFin'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Fin</FormLabel>
                    <FormControl>
                      <Input type='date' {...field} />
                    </FormControl>
                    <FormDescription>Facultative.</FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
          </form>
        </Form>
        <DialogFooter>
          <Button variant='outline' onClick={() => onOuvertChange(false)}>
            Annuler
          </Button>
          <Button
            type='submit'
            form='formulaire-taux'
            disabled={mutation.isPending}
          >
            Enregistrer
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
