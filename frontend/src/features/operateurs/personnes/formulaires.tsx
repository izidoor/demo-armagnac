import { useEffect, useState } from 'react'
import { z } from 'zod'
import { useForm, type Control } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import {
  type Contact,
  type FicheProfessionnelle,
  type SaisieContact,
  type SaisieFiche,
  type SaisieIdentiteContact,
} from '@/api/generated'
import { Button } from '@/components/ui/button'
import { Form } from '@/components/ui/form'
import { CadreEditable } from '@/components/metier/cadre-editable'
import { afficherErreur } from '@/components/metier/erreur-formulaire'
import { ErreurRacine } from '@/components/metier/erreur-racine'
import {
  ChampCases,
  ChampChoix,
  ChampTexte,
  Lecture,
  LectureListe,
} from '../commun/champs'
import { useListe } from '../commun/referentiels'
import {
  identifiant,
  optionsReference,
  texte,
  versChoix,
  versTexte,
} from '../commun/saisie'

const schemaIdentite = z.object({
  titre: z.string(),
  nom: z.string().trim().min(1, 'Le nom est obligatoire.'),
  prenom: z.string(),
  email: z.string(),
})

const schemaFiche = z.object({
  fonction: z.string(),
  centresInteret: z.array(z.number()),
  telephoneProfessionnel: z.string(),
  telephoneSecondaire: z.string(),
  adresse: z.string(),
  codePostal: z.string(),
  ville: z.string(),
  pays: z.string(),
  observation: z.string(),
})

const schemaContact = schemaIdentite.extend(schemaFiche.shape)

type Identite = z.infer<typeof schemaIdentite>
type Fiche = z.infer<typeof schemaFiche>
type SaisieComplete = z.infer<typeof schemaContact>

function ficheDepuis(f?: FicheProfessionnelle): Fiche {
  return {
    fonction: versTexte(f?.fonction),
    centresInteret: f?.centresInteret ?? [],
    telephoneProfessionnel: versTexte(f?.telephoneProfessionnel),
    telephoneSecondaire: versTexte(f?.telephoneSecondaire),
    adresse: versTexte(f?.adresse),
    codePostal: versTexte(f?.codePostal),
    ville: versTexte(f?.ville),
    pays: versChoix(f?.pays),
    observation: versTexte(f?.observation),
  }
}

function versFiche(s: Fiche) {
  return {
    fonction: texte(s.fonction),
    centresInteret: s.centresInteret,
    telephoneProfessionnel: texte(s.telephoneProfessionnel),
    telephoneSecondaire: texte(s.telephoneSecondaire),
    adresse: texte(s.adresse),
    codePostal: texte(s.codePostal),
    ville: texte(s.ville),
    pays: identifiant(s.pays),
    observation: texte(s.observation),
  }
}

function versIdentite(s: Identite) {
  return {
    titre: identifiant(s.titre),
    nom: s.nom.trim(),
    prenom: texte(s.prenom),
    email: texte(s.email),
  }
}

const REGLES_IDENTITE = {
  'RG-OPE-27': 'nom',
  'RG-OPE-28': 'email',
  'RG-OPE-29': 'email',
} as const

/** Déclarer un contact et sa fiche professionnelle (vision UC-10, RG-OPE-27, 28, 30). */
export function FormulaireContact({
  enregistrer,
  onSucces,
}: {
  enregistrer: (saisie: SaisieContact) => Promise<unknown>
  onSucces: (resultat: unknown) => void
}) {
  const titres = useListe('titres')
  const form = useForm<SaisieComplete>({
    resolver: zodResolver(schemaContact),
    defaultValues: {
      titre: versChoix(undefined),
      nom: '',
      prenom: '',
      email: '',
      ...ficheDepuis(),
    },
  })
  const mutation = useMutation({
    mutationFn: (s: SaisieComplete) =>
      enregistrer({ ...versIdentite(s), ...versFiche(s) }),
    meta: { erreurAffichee: true },
    onSuccess: onSucces,
    onError: (error) => afficherErreur(form, error, REGLES_IDENTITE),
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
          options={optionsReference(titres.valeurs, undefined)}
        />
        <ChampTexte control={c} name='nom' label='Nom' />
        <ChampTexte control={c} name='prenom' label='Prénom' />
        <ChampTexte control={c} name='email' label='E-mail' />
        {/* Les champs de la fiche portent les mêmes noms dans la saisie complète. */}
        <ChampsFiche control={c as unknown as Control<Fiche>} />
        <div className='flex justify-end md:col-span-3'>
          <Button
            type='submit'
            className='max-md:w-full'
            disabled={mutation.isPending}
          >
            Déclarer
          </Button>
        </div>
      </form>
    </Form>
  )
}

/** Identité et e-mail d'un contact (vision UC-11) ; sa fiche se modifie à part. */
export function FormulaireIdentiteContact({
  contact,
  modifiable,
  enregistrer,
  onSucces,
}: {
  contact: Contact
  modifiable: boolean
  enregistrer: (saisie: SaisieIdentiteContact) => Promise<unknown>
  onSucces: () => void
}) {
  const titres = useListe('titres')
  const depuis = (x: Contact): Identite => ({
    titre: versChoix(x.titre),
    nom: x.nom,
    prenom: versTexte(x.prenom),
    email: versTexte(x.email),
  })
  const form = useForm<Identite>({
    resolver: zodResolver(schemaIdentite),
    defaultValues: depuis(contact),
  })
  useEffect(() => form.reset(depuis(contact)), [contact, form])
  const mutation = useMutation({
    mutationFn: (s: Identite) =>
      enregistrer({ ...versIdentite(s), version: contact.version }),
    meta: { erreurAffichee: true },
    onSuccess: onSucces,
    onError: (error) => afficherErreur(form, error, REGLES_IDENTITE),
  })
  const c = form.control
  return (
    <Form {...form}>
      <form
        onSubmit={form.handleSubmit((s) => mutation.mutate(s))}
        className='grid gap-4 md:grid-cols-4'
      >
        <div className='md:col-span-4'>
          <ErreurRacine errors={form.formState.errors} />
        </div>
        <fieldset disabled={!modifiable} className='contents'>
          <ChampChoix
            control={c}
            name='titre'
            label='Titre'
            facultatif
            options={optionsReference(titres.valeurs, contact.titre)}
          />
          <ChampTexte control={c} name='nom' label='Nom' />
          <ChampTexte control={c} name='prenom' label='Prénom' />
          <ChampTexte control={c} name='email' label='E-mail' />
        </fieldset>
        {modifiable && (
          <div className='flex justify-end md:col-span-4'>
            <Button type='submit' disabled={mutation.isPending}>
              Enregistrer l'identité
            </Button>
          </div>
        )}
      </form>
    </Form>
  )
}

/**
 * La fiche professionnelle d'un contact ou d'un membre, dans sa carte (RG-OPE-46) : en lecture, et « Éditer » pour qui
 * peut la modifier, son titulaire ou un rôle qui gère le module Contacts.
 */
export function CarteFiche({
  titre = 'Fiche professionnelle',
  fiche,
  modifiable,
  enregistrer,
  onSucces,
}: {
  /** « Ma fiche chez … », dans Mon compte. */
  titre?: string
  fiche: FicheProfessionnelle
  modifiable: boolean
  enregistrer: (saisie: SaisieFiche) => Promise<unknown>
  onSucces: () => void
}) {
  const [edition, setEdition] = useState(false)
  const pays = useListe('pays')
  const centres = useListe('centresInteret')
  const form = useForm<Fiche>({
    resolver: zodResolver(schemaFiche),
    defaultValues: ficheDepuis(fiche),
  })
  useEffect(() => form.reset(ficheDepuis(fiche)), [fiche, form])
  const mutation = useMutation({
    mutationFn: (s: Fiche) =>
      enregistrer({ ...versFiche(s), version: fiche.version }),
    meta: { erreurAffichee: true },
    onSuccess: () => {
      setEdition(false)
      onSucces()
    },
    onError: (error) => afficherErreur(form, error),
  })
  const telephone = (numero?: string | null) =>
    numero && (
      <a href={`tel:${numero}`} className='hover:underline'>
        {numero}
      </a>
    )
  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit((s) => mutation.mutate(s))}>
        <CadreEditable
          titre={titre}
          edition={edition}
          modifiable={modifiable}
          onEditer={() => setEdition(true)}
          onAnnuler={() => {
            form.reset(ficheDepuis(fiche))
            setEdition(false)
          }}
          enregistrement={mutation.isPending}
        >
          {edition ? (
            <div className='grid gap-4 @min-[48rem]/content:grid-cols-3'>
              <div className='@min-[48rem]/content:col-span-3'>
                <ErreurRacine errors={form.formState.errors} />
              </div>
              <ChampsFiche
                control={form.control}
                pays={fiche.pays}
                centresInteret={fiche.centresInteret}
                pleineLargeur='@min-[48rem]/content:col-span-3'
              />
            </div>
          ) : (
            <div className='space-y-4'>
              <div className='grid gap-4 @min-[48rem]/content:grid-cols-3'>
                <Lecture label='Fonction' valeur={fiche.fonction} />
                <Lecture
                  label='Téléphone professionnel'
                  valeur={telephone(fiche.telephoneProfessionnel)}
                />
                <Lecture
                  label='Téléphone secondaire'
                  valeur={telephone(fiche.telephoneSecondaire)}
                />
                <Lecture
                  label='Adresse (si distincte)'
                  valeur={fiche.adresse}
                />
                <Lecture label='Code postal' valeur={fiche.codePostal} />
                <Lecture label='Ville' valeur={fiche.ville} />
                <Lecture label='Pays' valeur={pays.libelle(fiche.pays)} />
                <LectureListe
                  label="Centres d'intérêt"
                  valeurs={(fiche.centresInteret ?? []).map((id) =>
                    centres.libelle(id)
                  )}
                  vide='Aucun'
                  className='@min-[48rem]/content:col-span-2'
                />
                <Lecture
                  label='Observation'
                  valeur={
                    fiche.observation && (
                      <span className='whitespace-pre-line'>
                        {fiche.observation}
                      </span>
                    )
                  }
                  className='@min-[48rem]/content:col-span-3'
                />
              </div>
              {fiche.modifieePar && (
                <p className='text-muted-foreground text-sm'>
                  Modifiée par {fiche.modifieePar}
                  {fiche.modifieeLe &&
                    `, le ${new Date(fiche.modifieeLe).toLocaleDateString('fr-FR')}`}
                  .
                </p>
              )}
            </div>
          )}
        </CadreEditable>
      </form>
    </Form>
  )
}

/** Les champs d'une fiche professionnelle ; pays et centres d'intérêt portés restent proposés (RG-OPE-21). */
function ChampsFiche({
  control: c,
  pays: paysPorte,
  centresInteret = [],
  pleineLargeur = 'md:col-span-3',
}: {
  control: Control<Fiche>
  pays?: number | null
  centresInteret?: number[]
  /** Les champs sur toute la largeur de la grille ; dans la zone de contenu, sa variante de conteneur. */
  pleineLargeur?: string
}) {
  const pays = useListe('pays')
  const centres = useListe('centresInteret')
  return (
    <>
      <ChampTexte control={c} name='fonction' label='Fonction' />
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
        options={optionsReference(pays.valeurs, paysPorte)}
      />
      <ChampCases
        control={c}
        name='centresInteret'
        label="Centres d'intérêt"
        className={pleineLargeur}
        options={centres.proposees(centresInteret).map((v) => ({
          valeur: v.id,
          libelle: v.actif ? v.libelle : `${v.libelle} (inactif)`,
        }))}
      />
      <ChampTexte
        control={c}
        name='observation'
        label='Observation'
        multiligne
        className={pleineLargeur}
      />
    </>
  )
}
