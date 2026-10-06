import { useEffect, useRef } from 'react'
import { z } from 'zod'
import { useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { creerOperateur, proposerCodeOperateur } from '@/api/generated'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Form } from '@/components/ui/form'
import { afficherErreur } from '@/components/metier/erreur-formulaire'
import { ErreurRacine } from '@/components/metier/erreur-racine'
import { ChampChoix, ChampTexte } from './commun/champs'
import { AlerteDoublons } from './commun/doublons'
import { TYPES, type TypeOperateur } from './commun/libelles'
import { useListe } from './commun/referentiels'
import {
  AUCUN,
  identifiant,
  optionsListe,
  optionsReference,
  texte,
} from './commun/saisie'
import { useDoublons } from './commun/use-doublons'

const schema = z.object({
  type: z.enum(['PRODUCTEUR', 'NEGOCIANT', 'AUTRE']),
  codePostal: z.string(),
  ville: z.string(),
  pays: z.string(),
  raisonSociale: z.string().trim().min(1, 'La raison sociale est obligatoire.'),
  code: z.string(),
})

type Saisie = z.infer<typeof schema>

const VIDE: Saisie = {
  type: 'PRODUCTEUR',
  codePostal: '',
  ville: '',
  pays: AUCUN,
  raisonSociale: '',
  code: '',
}

type CreationOperateurProps = {
  ouvert: boolean
  onOuvertChange: (ouvert: boolean) => void
  onCree: (id: number) => void
}

/**
 * Création (spec 02, §6.1) : le type d'abord, puis le code postal ; le code proposé se remplit
 * (RG-OPE-03) et reste modifiable. Les doublons de raison sociale sont signalés sans bloquer (RG-OPE-08).
 * Le reste de la fiche se complète ensuite.
 */
export function CreationOperateur({
  ouvert,
  onOuvertChange,
  onCree,
}: CreationOperateurProps) {
  const queryClient = useQueryClient()
  const pays = useListe('pays')
  const form = useForm<Saisie>({
    resolver: zodResolver(schema),
    defaultValues: VIDE,
  })
  const [type, codePostal, paysChoisi, raisonSociale] = useWatch({
    control: form.control,
    name: ['type', 'codePostal', 'pays', 'raisonSociale'],
  })
  const codeTouche = useRef(false)

  useEffect(() => {
    if (ouvert) {
      form.reset(VIDE)
      codeTouche.current = false
    }
  }, [ouvert, form])

  const propose = useQuery({
    queryKey: ['code-propose', type, codePostal, paysChoisi],
    queryFn: () =>
      proposerCodeOperateur({
        query: {
          type: type as TypeOperateur,
          codePostal: texte(codePostal),
          pays: identifiant(paysChoisi),
        },
        throwOnError: true,
      }).then((r) => r.data.code),
    enabled: ouvert,
  })
  useEffect(() => {
    if (!codeTouche.current) form.setValue('code', propose.data ?? '')
  }, [propose.data, form])

  const doublons = useDoublons({ raisonSociale })

  const mutation = useMutation({
    mutationFn: (s: Saisie) =>
      creerOperateur({
        body: {
          type: s.type,
          code: texte(s.code),
          raisonSociale: s.raisonSociale.trim(),
          codePostal: texte(s.codePostal),
          ville: texte(s.ville),
          pays: identifiant(s.pays),
          accordDiffusionGeneral: false,
        },
        throwOnError: true,
      }),
    meta: { erreurAffichee: true },
    onSuccess: (reponse) => {
      queryClient.invalidateQueries({ queryKey: ['operateurs'] })
      onOuvertChange(false)
      onCree(reponse.data.id)
    },
    onError: (error) =>
      afficherErreur(form, error, {
        'RG-OPE-02': 'raisonSociale',
        'RG-OPE-04': 'code',
      }),
  })

  return (
    <Dialog open={ouvert} onOpenChange={onOuvertChange}>
      <DialogContent className='sm:max-w-lg'>
        <DialogHeader>
          <DialogTitle>Nouvel opérateur</DialogTitle>
          <DialogDescription>
            Le code est proposé selon le type et le code postal ; il reste
            modifiable. La fiche se complète ensuite.
          </DialogDescription>
        </DialogHeader>
        <Form {...form}>
          <form
            id='creation-operateur'
            onSubmit={form.handleSubmit((s) => mutation.mutate(s))}
            className='grid grid-cols-2 gap-4'
          >
            <div className='col-span-2'>
              <ErreurRacine errors={form.formState.errors} />
            </div>
            <ChampChoix
              control={form.control}
              name='type'
              label='Type'
              options={optionsListe(TYPES)}
            />
            <ChampTexte
              control={form.control}
              name='codePostal'
              label='Code postal'
            />
            <ChampTexte control={form.control} name='ville' label='Ville' />
            <ChampChoix
              control={form.control}
              name='pays'
              label='Pays'
              facultatif
              description='Vide : France.'
              options={optionsReference(pays.valeurs)}
            />
            <div className='col-span-2 space-y-1'>
              <ChampTexte
                control={form.control}
                name='raisonSociale'
                label='Raison sociale'
              />
              <AlerteDoublons doublons={doublons} champ='RAISON_SOCIALE' />
            </div>
            <div onInput={() => (codeTouche.current = true)}>
              <ChampTexte
                control={form.control}
                name='code'
                label='Code'
                description={
                  propose.data
                    ? `Proposé : ${propose.data}`
                    : 'Aucun code proposé : à saisir (négociant sans code postal français).'
                }
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
            form='creation-operateur'
            disabled={mutation.isPending}
          >
            Créer
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
