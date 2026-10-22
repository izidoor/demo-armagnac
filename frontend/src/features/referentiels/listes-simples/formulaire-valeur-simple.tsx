import { useEffect } from 'react'
import { z } from 'zod'
import { useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { type ValeurSimple } from '@/api/generated'
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
import { DialogueFormulaire } from '@/components/metier/dialogue-formulaire'
import { afficherErreur } from '@/components/metier/erreur-formulaire'
import { ErreurRacine } from '@/components/metier/erreur-racine'
import { ValeursProches } from './valeurs-proches'

const schema = z.object({
  code: z.string().trim().min(1, 'Le code est obligatoire.'),
  libelle: z.string().trim().min(1, 'Le libellé est obligatoire.'),
})

type SaisieValeurSimple = z.infer<typeof schema>

type FormulaireValeurSimpleProps<R> = {
  ouvert: boolean
  onOuvertChange: (ouvert: boolean) => void
  titre: string
  /** Valeur modifiée ; absente à la création. */
  valeur?: { id: number; code: string; libelle: string }
  enregistrer: (saisie: SaisieValeurSimple) => Promise<R>
  /** Reçoit le résultat de l'enregistrement. */
  onSucces: (resultat: R) => void
  /** Valeurs au libellé proche, pour éviter un doublon (marques, RG-REF-15). */
  proches?: (libelle: string) => Promise<ValeurSimple[]>
}

/**
 * Création ou modification d'une valeur : code et libellé, tous deux corrigeables ; le code reste
 * unique dans la liste (RG-REF-01, RG-REF-02).
 */
export function FormulaireValeurSimple<R>({
  ouvert,
  onOuvertChange,
  titre,
  valeur,
  enregistrer,
  onSucces,
  proches,
}: FormulaireValeurSimpleProps<R>) {
  const form = useForm<SaisieValeurSimple>({
    resolver: zodResolver(schema),
    defaultValues: { code: '', libelle: '' },
  })

  useEffect(() => {
    if (ouvert) form.reset(valeur ?? { code: '', libelle: '' })
  }, [ouvert, valeur, form])

  const mutation = useMutation({
    mutationFn: enregistrer,
    meta: { erreurAffichee: true },
    onSuccess: (resultat) => {
      onSucces(resultat)
      onOuvertChange(false)
    },
    onError: (error) =>
      afficherErreur(form, error, {
        'RG-REF-01': 'code',
        'RG-REF-03': 'libelle',
        'RG-REF-15': 'libelle',
      }),
  })

  const libelle = useWatch({ control: form.control, name: 'libelle' })

  return (
    <DialogueFormulaire
      ouvert={ouvert}
      onOuvertChange={onOuvertChange}
      className='sm:max-w-md'
      titre={titre}
      description='Le code est unique dans la liste.'
    >
      <Form {...form}>
        <form
          id='formulaire-valeur-simple'
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
                {proches && (
                  <FormDescription>
                    <ValeursProches
                      libelle={libelle}
                      proches={proches}
                      sauf={valeur?.id}
                    />
                  </FormDescription>
                )}
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
          form='formulaire-valeur-simple'
          disabled={mutation.isPending}
        >
          Enregistrer
        </Button>
      </DialogFooter>
    </DialogueFormulaire>
  )
}
