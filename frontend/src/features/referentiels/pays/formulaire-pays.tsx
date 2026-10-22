import { useEffect } from 'react'
import { z } from 'zod'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { type Pays, type ValeurSimple } from '@/api/generated'
import { Button } from '@/components/ui/button'
import { DialogFooter } from '@/components/ui/dialog'
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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Switch } from '@/components/ui/switch'
import { DialogueFormulaire } from '@/components/metier/dialogue-formulaire'
import { afficherErreur } from '@/components/metier/erreur-formulaire'
import { ErreurRacine } from '@/components/metier/erreur-racine'
import { LANGUES_CERTIFICAT, NIVEAUX_DETAIL } from './libelles'

const schema = z.object({
  code: z.string().trim().min(1, 'Le code est obligatoire.'),
  libelle: z.string().trim().min(1, 'Le libellé est obligatoire.'),
  codeIso: z
    .string()
    .trim()
    .regex(/^[A-Za-z]{2}$/, 'Deux lettres (ISO 3166-1 alpha-2).'),
  zoneCommerciale: z.string().min(1, 'La zone commerciale est obligatoire.'),
  membreUe: z.boolean(),
  langueCertificat: z.enum(['FR_EN', 'FR_ES', 'FR_IT']),
  niveauDetailCertificat: z.enum([
    'CUMUL_DEGRE',
    'DETAIL_COMPTE_AGE',
    'DETAIL_MILLESIME',
  ]),
  ageMinimum: z
    .string()
    .trim()
    .regex(/^(00|0|[1-9]\d{0,2})?$/, "Un compte d'âge : 00, 0, 1, 2…"),
})

export type SaisiePays = z.infer<typeof schema>

const VIDE: SaisiePays = {
  code: '',
  libelle: '',
  codeIso: '',
  zoneCommerciale: '',
  membreUe: false,
  langueCertificat: 'FR_EN',
  niveauDetailCertificat: 'CUMUL_DEGRE',
  ageMinimum: '',
}

type FormulairePaysProps = {
  ouvert: boolean
  onOuvertChange: (ouvert: boolean) => void
  pays?: Pays
  /**
   * Toutes les zones : seules les actives sont proposées, plus la zone actuelle du pays (RG-REF-05).
   * Le formulaire porte l'identifiant de la zone, sous forme de texte.
   */
  zones: ValeurSimple[]
  enregistrer: (saisie: SaisiePays) => Promise<unknown>
  onSucces: () => void
}

export function FormulairePays({
  ouvert,
  onOuvertChange,
  pays,
  zones,
  enregistrer,
  onSucces,
}: FormulairePaysProps) {
  const form = useForm<SaisiePays>({
    resolver: zodResolver(schema),
    defaultValues: VIDE,
  })

  useEffect(() => {
    if (ouvert)
      form.reset(
        pays
          ? {
              ...pays,
              zoneCommerciale: String(pays.zoneCommerciale),
              ageMinimum: pays.ageMinimum ?? '',
            }
          : VIDE
      )
  }, [ouvert, pays, form])

  const mutation = useMutation({
    mutationFn: enregistrer,
    meta: { erreurAffichee: true },
    onSuccess: () => {
      onSucces()
      onOuvertChange(false)
    },
    onError: (error) =>
      afficherErreur(form, error, {
        'RG-REF-01': 'code',
        'RG-REF-03': 'libelle',
        'RG-REF-05': 'zoneCommerciale',
        'RG-REF-06': 'codeIso',
        'RG-REF-09': 'ageMinimum',
      }),
  })

  const zonesProposees = zones.filter(
    (z) => z.actif || z.id === pays?.zoneCommerciale
  )

  return (
    <DialogueFormulaire
      ouvert={ouvert}
      onOuvertChange={onOuvertChange}
      className='sm:max-w-lg'
      titre={pays ? `Modifier « ${pays.libelle} »` : 'Nouveau pays'}
      description='Le code et le code ISO sont uniques.'
    >
      <Form {...form}>
        <form
          id='formulaire-pays'
          onSubmit={form.handleSubmit((saisie) => mutation.mutate(saisie))}
          className='grid grid-cols-2 gap-4'
        >
          <div className='col-span-2'>
            <ErreurRacine errors={form.formState.errors} />
          </div>
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
            name='codeIso'
            render={({ field }) => (
              <FormItem>
                <FormLabel>Code ISO</FormLabel>
                <FormControl>
                  <Input {...field} maxLength={2} autoComplete='off' />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name='libelle'
            render={({ field }) => (
              <FormItem className='col-span-2'>
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
            name='zoneCommerciale'
            render={({ field }) => (
              <FormItem>
                <FormLabel>Zone commerciale</FormLabel>
                <Select value={field.value} onValueChange={field.onChange}>
                  <FormControl>
                    <SelectTrigger className='w-full'>
                      <SelectValue placeholder='Choisir' />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    {zonesProposees.map((z) => (
                      <SelectItem key={z.id} value={String(z.id)}>
                        {z.libelle}
                        {z.actif ? '' : ' (inactive)'}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name='membreUe'
            render={({ field }) => (
              <FormItem className='flex flex-col justify-end'>
                <div className='flex h-9 items-center gap-2'>
                  <FormControl>
                    <Switch
                      checked={field.value}
                      onCheckedChange={field.onChange}
                    />
                  </FormControl>
                  <FormLabel>Membre de l'UE</FormLabel>
                </div>
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name='langueCertificat'
            render={({ field }) => (
              <FormItem>
                <FormLabel>Langue du certificat</FormLabel>
                <Select value={field.value} onValueChange={field.onChange}>
                  <FormControl>
                    <SelectTrigger className='w-full'>
                      <SelectValue />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    {Object.entries(LANGUES_CERTIFICAT).map(([v, l]) => (
                      <SelectItem key={v} value={v}>
                        {l}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name='niveauDetailCertificat'
            render={({ field }) => (
              <FormItem>
                <FormLabel>Niveau de détail</FormLabel>
                <Select value={field.value} onValueChange={field.onChange}>
                  <FormControl>
                    <SelectTrigger className='w-full'>
                      <SelectValue />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    {Object.entries(NIVEAUX_DETAIL).map(([v, l]) => (
                      <SelectItem key={v} value={v}>
                        {l}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name='ageMinimum'
            render={({ field }) => (
              <FormItem>
                <FormLabel>Âge minimum à l'export</FormLabel>
                <FormControl>
                  <Input {...field} placeholder='Aucun' autoComplete='off' />
                </FormControl>
                <FormDescription>Compte d'âge : 00, 0, 1, 2…</FormDescription>
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
          form='formulaire-pays'
          disabled={mutation.isPending}
        >
          Enregistrer
        </Button>
      </DialogFooter>
    </DialogueFormulaire>
  )
}
