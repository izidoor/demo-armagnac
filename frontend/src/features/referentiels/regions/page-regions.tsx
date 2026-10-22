import { useEffect, useState } from 'react'
import { z } from 'zod'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Plus } from 'lucide-react'
import {
  creerRegion,
  desactiverRegion,
  listerRegions,
  modifierRegion,
  reactiverRegion,
  supprimerRegion,
  type Region,
} from '@/api/generated'
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
import { Switch } from '@/components/ui/switch'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { BadgeEtat } from '@/components/metier/badge-etat'
import { DialogueFormulaire } from '@/components/metier/dialogue-formulaire'
import { afficherErreur } from '@/components/metier/erreur-formulaire'
import { ErreurRacine } from '@/components/metier/erreur-racine'
import { LignesEtat } from '@/components/metier/lignes-etat'
import { PageMetier } from '@/components/metier/page-metier'
import { ChangementEtat } from '../commun/changement-etat'
import { COLONNE_CODE, COLONNE_ETAT, COLONNE_LIBELLE } from '../commun/colonnes'
import { BarreFiltre } from '../commun/filtre-valeurs'
import { MenuActions } from '../commun/menu-actions'
import { Suppression } from '../commun/suppression'
import { useFiltreValeurs } from '../commun/use-filtre-valeurs'
import { useGestionParametrage } from '../commun/use-gestion-parametrage'

const schema = z.object({
  code: z.string().trim().min(1, 'Le code est obligatoire.'),
  libelle: z.string().trim().min(1, 'Le libellé est obligatoire.'),
  enRegionDelimitee: z.boolean(),
})

type SaisieRegion = z.infer<typeof schema>

/** Sous 40rem de contenu, la colonne de la région délimitée passe sous le libellé. */
const SOUS_40REM = '@max-[40rem]/content:hidden'

export function PageRegions() {
  const filtreValeurs = useFiltreValeurs()
  const { filtre } = filtreValeurs
  const queryClient = useQueryClient()
  const gestion = useGestionParametrage()
  const requete = useQuery({
    queryKey: ['regions', filtre],
    queryFn: () =>
      listerRegions({ query: filtre, throwOnError: true }).then((r) => r.data),
  })
  const rafraichir = () =>
    queryClient.invalidateQueries({ queryKey: ['regions'] })

  const [edition, setEdition] = useState<Region | 'nouvelle' | null>(null)
  const [changement, setChangement] = useState<Region | null>(null)
  const [suppression, setSuppression] = useState<Region | null>(null)
  const regionEditee =
    edition === 'nouvelle' ? undefined : (edition ?? undefined)

  return (
    <PageMetier
      titre='Régions'
      description="Régions de rattachement des opérateurs ; l'indicateur distingue la région délimitée de l'Armagnac."
      actions={
        gestion && (
          <Button onClick={() => setEdition('nouvelle')}>
            <Plus /> Nouvelle région
          </Button>
        )
      }
    >
      <BarreFiltre {...filtreValeurs} />
      <div className='rounded-md border'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className={COLONNE_CODE}>Code</TableHead>
              <TableHead className={COLONNE_LIBELLE}>Libellé</TableHead>
              <TableHead className={SOUS_40REM}>Région délimitée</TableHead>
              <TableHead className={COLONNE_ETAT}>État</TableHead>
              <TableHead className='w-12' />
            </TableRow>
          </TableHeader>
          <TableBody>
            <LignesEtat requete={requete} colonnes={5} />
            {requete.data?.map((region) => (
              <TableRow key={region.id}>
                <TableCell className='font-mono'>{region.code}</TableCell>
                <TableCell className={COLONNE_LIBELLE} title={region.libelle}>
                  <span className='block truncate'>{region.libelle}</span>
                  {/* Sur un contenu étroit, la région délimitée passe sous le libellé. */}
                  <span className='text-muted-foreground block truncate text-xs @min-[40rem]/content:hidden'>
                    {region.enRegionDelimitee
                      ? 'En région délimitée'
                      : 'Hors région'}
                  </span>
                </TableCell>
                <TableCell className={SOUS_40REM}>
                  {region.enRegionDelimitee ? (
                    <Badge variant='secondary'>En région délimitée</Badge>
                  ) : (
                    <span className='text-muted-foreground'>Hors région</span>
                  )}
                </TableCell>
                <TableCell>
                  <BadgeEtat actif={region.actif} />
                </TableCell>
                <TableCell>
                  <MenuActions
                    actif={region.actif}
                    onModifier={() => setEdition(region)}
                    onChangerEtat={() => setChangement(region)}
                    onSupprimer={() => setSuppression(region)}
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      <FormulaireRegion
        ouvert={edition !== null}
        onOuvertChange={(ouvert) => !ouvert && setEdition(null)}
        region={regionEditee}
        onSucces={rafraichir}
      />
      <ChangementEtat
        valeur={changement}
        onFermer={() => setChangement(null)}
        changer={() =>
          changement!.actif
            ? desactiverRegion({
                path: { id: changement!.id },
                throwOnError: true,
              })
            : reactiverRegion({
                path: { id: changement!.id },
                throwOnError: true,
              })
        }
        onSucces={rafraichir}
      />
      <Suppression
        valeur={suppression}
        onFermer={() => setSuppression(null)}
        supprimer={() =>
          supprimerRegion({ path: { id: suppression!.id }, throwOnError: true })
        }
        onSucces={rafraichir}
      />
    </PageMetier>
  )
}

type FormulaireRegionProps = {
  ouvert: boolean
  onOuvertChange: (ouvert: boolean) => void
  region?: Region
  onSucces: () => void
}

function FormulaireRegion({
  ouvert,
  onOuvertChange,
  region,
  onSucces,
}: FormulaireRegionProps) {
  const form = useForm<SaisieRegion>({
    resolver: zodResolver(schema),
    defaultValues: { code: '', libelle: '', enRegionDelimitee: false },
  })

  useEffect(() => {
    if (ouvert)
      form.reset(region ?? { code: '', libelle: '', enRegionDelimitee: false })
  }, [ouvert, region, form])

  const mutation = useMutation({
    mutationFn: (saisie: SaisieRegion) =>
      region
        ? modifierRegion({
            path: { id: region.id },
            body: saisie,
            throwOnError: true,
          })
        : creerRegion({ body: saisie, throwOnError: true }),
    meta: { erreurAffichee: true },
    onSuccess: () => {
      onSucces()
      onOuvertChange(false)
    },
    onError: (error) =>
      afficherErreur(form, error, {
        'RG-REF-01': 'code',
        'RG-REF-03': 'libelle',
      }),
  })

  return (
    <DialogueFormulaire
      ouvert={ouvert}
      onOuvertChange={onOuvertChange}
      className='sm:max-w-md'
      titre={region ? `Modifier « ${region.libelle} »` : 'Nouvelle région'}
    >
      <Form {...form}>
        <form
          id='formulaire-region'
          onSubmit={form.handleSubmit((saisie) => mutation.mutate(saisie))}
          className='space-y-4'
        >
          <ErreurRacine errors={form.formState.errors} />
          <FormField
            control={form.control}
            name='code'
            render={({ field }) => (
              <FormItem>
                <FormLabel>Code</FormLabel>
                <FormControl>
                  <Input {...field} autoComplete='off' />
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
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name='enRegionDelimitee'
            render={({ field }) => (
              <FormItem className='flex items-center gap-2'>
                <FormControl>
                  <Switch
                    checked={field.value}
                    onCheckedChange={field.onChange}
                  />
                </FormControl>
                <FormLabel>En région délimitée</FormLabel>
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
          form='formulaire-region'
          disabled={mutation.isPending}
        >
          Enregistrer
        </Button>
      </DialogFooter>
    </DialogueFormulaire>
  )
}
