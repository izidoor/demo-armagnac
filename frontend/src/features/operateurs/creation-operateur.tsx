import { useEffect, useRef } from 'react'
import { z } from 'zod'
import { useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { creerOperateur, proposerCodeOperateur } from '@/api/generated'
import { Button } from '@/components/ui/button'
import { DialogFooter } from '@/components/ui/dialog'
import { Form } from '@/components/ui/form'
import { DialogueFormulaire } from '@/components/metier/dialogue-formulaire'
import { afficherErreur } from '@/components/metier/erreur-formulaire'
import { ErreurRacine } from '@/components/metier/erreur-racine'
import { useDroits, useMoi } from '@/features/session/session'
import { ChampChoix, ChampTexte } from './commun/champs'
import { AlerteDoublons } from './commun/doublons'
import { TYPES_CHOISIS, type TypeOperateur } from './commun/libelles'
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
  adminEmail: z.string(),
  adminNom: z.string(),
  adminPrenom: z.string(),
})

type Saisie = z.infer<typeof schema>

const VIDE: Saisie = {
  type: 'PRODUCTEUR',
  codePostal: '',
  ville: '',
  pays: AUCUN,
  raisonSociale: '',
  code: '',
  adminEmail: '',
  adminNom: '',
  adminPrenom: '',
}

type CreationOperateurProps = {
  ouvert: boolean
  onOuvertChange: (ouvert: boolean) => void
  onCree: (id: number) => void
}

/**
 * Création (spec 02, §6.1) : le type d'abord, puis le code postal ; le code proposé se remplit
 * (RG-OPE-03) et reste modifiable. Les doublons de raison sociale sont signalés sans bloquer (RG-OPE-08).
 * Le reste de la fiche se complète ensuite. Un ADMIN du BNIA peut inviter le premier ADMIN de
 * l'opérateur (RG-OPE-40).
 */
export function CreationOperateur({
  ouvert,
  onOuvertChange,
  onCree,
}: CreationOperateurProps) {
  const queryClient = useQueryClient()
  const pays = useListe('pays')
  const { actif } = useMoi()
  const adminBnia = useDroits(actif?.operateur ?? 0)?.adminBnia ?? false
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
          premierAdmin: s.adminEmail.trim()
            ? {
                email: s.adminEmail.trim(),
                nom: s.adminNom.trim(),
                prenom: s.adminPrenom.trim(),
              }
            : undefined,
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
        'RG-OPE-40': 'adminEmail',
      }),
  })

  return (
    <DialogueFormulaire
      ouvert={ouvert}
      onOuvertChange={onOuvertChange}
      titre='Nouvel opérateur'
      description='Le code est proposé selon le type et le code postal ; il reste modifiable. La fiche se complète ensuite.'
    >
      <Form {...form}>
        <form
          id='creation-operateur'
          onSubmit={form.handleSubmit((s) => mutation.mutate(s))}
          className='grid gap-4 sm:grid-cols-2'
        >
          <div className='sm:col-span-2'>
            <ErreurRacine errors={form.formState.errors} />
          </div>
          <ChampChoix
            control={form.control}
            name='type'
            label='Type'
            options={optionsListe(TYPES_CHOISIS)}
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
          <div className='space-y-1 sm:col-span-2'>
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
          {adminBnia && (
            <fieldset className='grid gap-4 rounded-md border p-3 sm:col-span-2 sm:grid-cols-2'>
              <legend className='px-1 text-sm font-medium'>
                Inviter un premier ADMIN (facultatif)
              </legend>
              <ChampTexte
                control={form.control}
                name='adminPrenom'
                label='Prénom'
              />
              <ChampTexte control={form.control} name='adminNom' label='Nom' />
              <ChampTexte
                control={form.control}
                name='adminEmail'
                label='E-mail'
                className='sm:col-span-2'
                description='Un lien valable 7 jours lui sera envoyé.'
              />
            </fieldset>
          )}
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
          type='submit'
          form='creation-operateur'
          disabled={mutation.isPending}
        >
          Créer
        </Button>
      </DialogFooter>
    </DialogueFormulaire>
  )
}
