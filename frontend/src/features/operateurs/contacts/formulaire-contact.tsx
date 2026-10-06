import { useEffect } from 'react'
import { z } from 'zod'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { type Contact, type SaisieContact } from '@/api/generated'
import { Button } from '@/components/ui/button'
import { Form } from '@/components/ui/form'
import { afficherErreur } from '@/components/metier/erreur-formulaire'
import { ErreurRacine } from '@/components/metier/erreur-racine'
import { ChampCases, ChampChoix, ChampTexte } from '../commun/champs'
import { useListe } from '../commun/referentiels'
import {
  identifiant,
  optionsReference,
  texte,
  versChoix,
  versTexte,
} from '../commun/saisie'

const schema = z.object({
  titre: z.string(),
  nom: z.string().trim().min(1, 'Le nom est obligatoire.'),
  prenom: z.string(),
  fonction: z.string(),
  centresInteret: z.array(z.number()),
  telephoneProfessionnel: z.string(),
  telephoneSecondaire: z.string(),
  email: z.string(),
  adresse: z.string(),
  codePostal: z.string(),
  ville: z.string(),
  pays: z.string(),
  observation: z.string(),
})

type Saisie = z.infer<typeof schema>

function depuis(c?: Contact): Saisie {
  return {
    titre: versChoix(c?.titre),
    nom: c?.nom ?? '',
    prenom: versTexte(c?.prenom),
    fonction: versTexte(c?.fonction),
    centresInteret: c?.centresInteret ?? [],
    telephoneProfessionnel: versTexte(c?.telephoneProfessionnel),
    telephoneSecondaire: versTexte(c?.telephoneSecondaire),
    email: versTexte(c?.email),
    adresse: versTexte(c?.adresse),
    codePostal: versTexte(c?.codePostal),
    ville: versTexte(c?.ville),
    pays: versChoix(c?.pays),
    observation: versTexte(c?.observation),
  }
}

function versRequete(s: Saisie): SaisieContact {
  return {
    titre: identifiant(s.titre),
    nom: s.nom.trim(),
    prenom: texte(s.prenom),
    fonction: texte(s.fonction),
    centresInteret: s.centresInteret,
    telephoneProfessionnel: texte(s.telephoneProfessionnel),
    telephoneSecondaire: texte(s.telephoneSecondaire),
    email: texte(s.email),
    adresse: texte(s.adresse),
    codePostal: texte(s.codePostal),
    ville: texte(s.ville),
    pays: identifiant(s.pays),
    observation: texte(s.observation),
  }
}

/**
 * Identité et coordonnées d'un contact (C.1.9.1). Le nom est obligatoire (RG-OPE-27) ; l'e-mail, unique
 * dans l'opérateur (RG-OPE-28), est grisé quand il est l'identifiant d'un compte (RG-OPE-32).
 */
export function FormulaireContact({
  contact,
  enregistrer,
  onSucces,
  libelle = 'Enregistrer',
}: {
  contact?: Contact
  enregistrer: (saisie: SaisieContact) => Promise<unknown>
  onSucces: (resultat: unknown) => void
  libelle?: string
}) {
  const titres = useListe('titres')
  const pays = useListe('pays')
  const centres = useListe('centresInteret')
  const form = useForm<Saisie>({
    resolver: zodResolver(schema),
    defaultValues: depuis(contact),
  })
  useEffect(() => form.reset(depuis(contact)), [contact, form])

  const mutation = useMutation({
    mutationFn: (s: Saisie) => enregistrer(versRequete(s)),
    meta: { erreurAffichee: true },
    onSuccess: onSucces,
    onError: (error) =>
      afficherErreur(form, error, {
        'RG-OPE-27': 'nom',
        'RG-OPE-28': 'email',
        'RG-OPE-29': 'email',
        'RG-OPE-32': 'email',
      }),
  })

  const c = form.control
  return (
    <Form {...form}>
      <form
        onSubmit={form.handleSubmit((s) => mutation.mutate(s))}
        className='grid gap-4 md:grid-cols-3'
      >
        <div className='md:col-span-3'>
          <ErreurRacine errors={form.formState.errors} />
        </div>
        <ChampChoix
          control={c}
          name='titre'
          label='Titre'
          facultatif
          options={optionsReference(titres.valeurs, contact?.titre)}
        />
        <ChampTexte control={c} name='nom' label='Nom' />
        <ChampTexte control={c} name='prenom' label='Prénom' />
        <ChampTexte control={c} name='fonction' label='Fonction' />
        <ChampTexte
          control={c}
          name='email'
          label='E-mail'
          disabled={contact?.emailVerrouille}
          description={
            contact?.emailVerrouille
              ? 'Identifiant de son compte portail : seule cette personne peut le changer, dans « Mon compte ».'
              : undefined
          }
        />
        <ChampTexte
          control={c}
          name='telephoneProfessionnel'
          label='Téléphone professionnel'
        />
        <ChampTexte
          control={c}
          name='telephoneSecondaire'
          label='Téléphone secondaire'
        />
        <ChampTexte control={c} name='adresse' label='Adresse (si distincte)' />
        <ChampTexte control={c} name='codePostal' label='Code postal' />
        <ChampTexte control={c} name='ville' label='Ville' />
        <ChampChoix
          control={c}
          name='pays'
          label='Pays'
          facultatif
          options={optionsReference(pays.valeurs, contact?.pays)}
        />
        <ChampCases
          control={c}
          name='centresInteret'
          label="Centres d'intérêt"
          className='md:col-span-3'
          options={centres
            .proposees(contact?.centresInteret ?? [])
            .map((v) => ({
              valeur: v.id,
              libelle: v.actif ? v.libelle : `${v.libelle} (inactif)`,
            }))}
        />
        <ChampTexte
          control={c}
          name='observation'
          label='Observation'
          multiligne
          className='md:col-span-3'
        />
        <div className='flex justify-end md:col-span-3'>
          <Button type='submit' disabled={mutation.isPending}>
            {libelle}
          </Button>
        </div>
      </form>
    </Form>
  )
}
