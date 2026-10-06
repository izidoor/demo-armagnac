import { useEffect } from 'react'
import { z } from 'zod'
import { useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { type ValeurSimple } from '@/api/generated'
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
import { afficherErreur } from '../commun/erreur-formulaire'
import { ErreurRacine } from '../commun/erreur-racine'
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
    <Dialog open={ouvert} onOpenChange={onOuvertChange}>
      <DialogContent className='sm:max-w-md'>
        <DialogHeader>
          <DialogTitle>{titre}</DialogTitle>
          <DialogDescription>
            Le code est unique dans la liste.
          </DialogDescription>
        </DialogHeader>
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
          <Button variant='outline' onClick={() => onOuvertChange(false)}>
            Annuler
          </Button>
          <Button
            type='submit'
            form='formulaire-valeur-simple'
            disabled={mutation.isPending}
          >
            Enregistrer
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
