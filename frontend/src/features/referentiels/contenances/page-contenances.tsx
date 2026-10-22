import { useEffect, useState } from 'react'
import { z } from 'zod'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Plus } from 'lucide-react'
import {
  creerContenance,
  desactiverContenance,
  listerContenances,
  modifierContenance,
  reactiverContenance,
  supprimerContenance,
  type Contenance,
} from '@/api/generated'
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
import { BadgeEtat } from '@/components/metier/badge-etat'
import { afficherErreur } from '@/components/metier/erreur-formulaire'
import { ErreurRacine } from '@/components/metier/erreur-racine'
import { LignesEtat } from '@/components/metier/lignes-etat'
import { PageMetier } from '@/components/metier/page-metier'
import { ChangementEtat } from '../commun/changement-etat'
import { BarreFiltre } from '../commun/filtre-valeurs'
import { MenuActions } from '../commun/menu-actions'
import { Suppression } from '../commun/suppression'
import { useFiltreValeurs } from '../commun/use-filtre-valeurs'
import { useGestionParametrage } from '../commun/use-gestion-parametrage'

const schema = z.object({
  volume: z
    .string()
    .trim()
    .regex(
      /^\d+([.,]\d{1,3})?$/,
      'Un volume en litres, trois décimales au plus (0,70).'
    )
    .refine((v) => Number(v.replace(',', '.')) > 0, {
      message: 'Le volume est strictement positif.',
    }),
  libelle: z.string(),
})

type SaisieContenance = z.infer<typeof schema>

const litres = new Intl.NumberFormat('fr-FR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 3,
})

export function PageContenances() {
  const filtreValeurs = useFiltreValeurs()
  const { filtre } = filtreValeurs
  const queryClient = useQueryClient()
  const gestion = useGestionParametrage()
  const requete = useQuery({
    queryKey: ['contenances', filtre],
    queryFn: () =>
      listerContenances({ query: filtre, throwOnError: true }).then(
        (r) => r.data
      ),
  })
  const rafraichir = () =>
    queryClient.invalidateQueries({ queryKey: ['contenances'] })

  const [edition, setEdition] = useState<Contenance | 'nouvelle' | null>(null)
  const [changement, setChangement] = useState<Contenance | null>(null)
  const [suppression, setSuppression] = useState<Contenance | null>(null)
  const contenanceEditee =
    edition === 'nouvelle' ? undefined : (edition ?? undefined)

  return (
    <PageMetier
      titre='Contenances'
      description="Volumes en litres, utilisés pour le calcul du volume d'alcool pur des ventes export."
      actions={
        gestion && (
          <Button onClick={() => setEdition('nouvelle')}>
            <Plus /> Nouvelle contenance
          </Button>
        )
      }
    >
      <BarreFiltre
        {...filtreValeurs}
        placeholder='Rechercher un libellé ou un volume'
      />
      <div className='rounded-md border'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className='w-40 text-end'>Volume (L)</TableHead>
              <TableHead>Libellé</TableHead>
              <TableHead className='w-28'>État</TableHead>
              <TableHead className='w-12' />
            </TableRow>
          </TableHeader>
          <TableBody>
            <LignesEtat requete={requete} colonnes={4} />
            {requete.data?.map((contenance) => (
              <TableRow key={contenance.id}>
                <TableCell className='text-end font-mono'>
                  {litres.format(contenance.volume)}
                </TableCell>
                <TableCell>
                  {contenance.libelle}
                  {!contenance.libelleSaisi && (
                    <span className='text-muted-foreground'> (dérivé)</span>
                  )}
                </TableCell>
                <TableCell>
                  <BadgeEtat actif={contenance.actif} />
                </TableCell>
                <TableCell>
                  <MenuActions
                    actif={contenance.actif}
                    onModifier={() => setEdition(contenance)}
                    onChangerEtat={() => setChangement(contenance)}
                    onSupprimer={() => setSuppression(contenance)}
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      <FormulaireContenance
        ouvert={edition !== null}
        onOuvertChange={(ouvert) => !ouvert && setEdition(null)}
        contenance={contenanceEditee}
        onSucces={rafraichir}
      />
      <ChangementEtat
        valeur={changement}
        onFermer={() => setChangement(null)}
        changer={() => {
          const path = { id: changement!.id }
          return changement!.actif
            ? desactiverContenance({ path, throwOnError: true })
            : reactiverContenance({ path, throwOnError: true })
        }}
        onSucces={rafraichir}
      />
      <Suppression
        valeur={suppression}
        onFermer={() => setSuppression(null)}
        supprimer={() =>
          supprimerContenance({
            path: { id: suppression!.id },
            throwOnError: true,
          })
        }
        onSucces={rafraichir}
      />
    </PageMetier>
  )
}

type FormulaireContenanceProps = {
  ouvert: boolean
  onOuvertChange: (ouvert: boolean) => void
  contenance?: Contenance
  onSucces: () => void
}

function FormulaireContenance({
  ouvert,
  onOuvertChange,
  contenance,
  onSucces,
}: FormulaireContenanceProps) {
  const form = useForm<SaisieContenance>({
    resolver: zodResolver(schema),
    defaultValues: { volume: '', libelle: '' },
  })

  useEffect(() => {
    if (ouvert)
      form.reset(
        contenance
          ? {
              volume: litres.format(contenance.volume),
              libelle: contenance.libelleSaisi ?? '',
            }
          : { volume: '', libelle: '' }
      )
  }, [ouvert, contenance, form])

  const mutation = useMutation({
    mutationFn: ({ volume, libelle }: SaisieContenance) => {
      const body = {
        volume: Number(volume.replace(',', '.')),
        libelle: libelle.trim() || undefined,
      }
      return contenance
        ? modifierContenance({
            path: { id: contenance.id },
            body,
            throwOnError: true,
          })
        : creerContenance({ body, throwOnError: true })
    },
    meta: { erreurAffichee: true },
    onSuccess: () => {
      onSucces()
      onOuvertChange(false)
    },
    onError: (error) => afficherErreur(form, error, { 'RG-REF-10': 'volume' }),
  })

  return (
    <Dialog open={ouvert} onOpenChange={onOuvertChange}>
      <DialogContent className='sm:max-w-md'>
        <DialogHeader>
          <DialogTitle>
            {contenance
              ? `Modifier « ${contenance.libelle} »`
              : 'Nouvelle contenance'}
          </DialogTitle>
          <DialogDescription>
            Le volume est unique dans le référentiel.
          </DialogDescription>
        </DialogHeader>
        <Form {...form}>
          <form
            id='formulaire-contenance'
            onSubmit={form.handleSubmit((saisie) => mutation.mutate(saisie))}
            className='space-y-4'
          >
            <ErreurRacine errors={form.formState.errors} />
            <FormField
              control={form.control}
              name='volume'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Volume en litres</FormLabel>
                  <FormControl>
                    <Input
                      {...field}
                      inputMode='decimal'
                      placeholder='0,70'
                      autoComplete='off'
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name='libelle'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Libellé</FormLabel>
                  <FormControl>
                    <Input {...field} autoComplete='off' />
                  </FormControl>
                  <FormDescription>
                    Facultatif : à défaut, dérivé du volume (« 0,70 L »).
                  </FormDescription>
                  <FormMessage />
                </FormItem>
              )}
            />
          </form>
        </Form>
        <DialogFooter>
          <Button variant='outline' onClick={() => onOuvertChange(false)}>
            Annuler
          </Button>
          <Button
            type='submit'
            form='formulaire-contenance'
            disabled={mutation.isPending}
          >
            Enregistrer
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
