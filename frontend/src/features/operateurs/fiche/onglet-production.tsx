import { useEffect, useState } from 'react'
import { z } from 'zod'
import { useFieldArray, useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { Plus, Trash2 } from 'lucide-react'
import {
  modifierProductionOperateur,
  type FicheOperateur,
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
import {
  APPELLATIONS,
  PERIODICITES,
  STATUTS,
  TYPES_ALAMBIC,
  cles,
  type Appellation,
  type Periodicite,
  type StatutDeclaration,
  type TypeAlambic,
} from '../commun/libelles'
import { useListe } from '../commun/referentiels'
import {
  AUCUN,
  identifiant,
  nombre,
  optionsListe,
  optionsReference,
  texte,
  versChoix,
  versTexte,
} from '../commun/saisie'
import { CadreLabels, CadreMarques } from './presentation'
import { type usePresentation } from './use-presentation'

const schema = z.object({
  periodicite: z.string(),
  statutDeclarationStock: z.string(),
  region: z.string(),
  appellations: z.array(z.string()),
  alambics: z.array(
    z.object({
      numeroAgrement: z.string(),
      type: z.string(),
      capacite: z.string(),
    })
  ),
})

type Saisie = z.infer<typeof schema>

function depuis(f: FicheOperateur): Saisie {
  return {
    periodicite: f.periodicite ?? AUCUN,
    statutDeclarationStock: f.statutDeclarationStock,
    region: versChoix(f.region),
    appellations: f.appellations,
    alambics: f.alambics.map((a) => ({
      numeroAgrement: versTexte(a.numeroAgrement),
      type: a.type,
      capacite: versTexte(a.capacite),
    })),
  }
}

/** Le cadre en édition, un seul à la fois. */
type Cadre = 'declaratif' | 'alambics'

/** Un alambic, en une ligne. */
function alambic(a: FicheOperateur['alambics'][number]) {
  return `${TYPES_ALAMBIC[a.type]}${a.numeroAgrement ? ` n° ${a.numeroAgrement}` : ''}${a.capacite ? `, capacité ${a.capacite}` : ''}`
}

/**
 * Onglet Production : périodicité et statut de déclaration selon le type (RG-OPE-01, 10, 11), région,
 * appellations (RG-OPE-12), alambics (RG-OPE-13), modifiables par le personnel du BNIA qui gère le module
 * Contacts (RG-OPE-36) ; puis marques, circuits et labels de la présentation (RG-OPE-16, 17), selon le
 * module Contacts. Chaque cadre s'affiche en lecture ; « Éditer » le passe en saisie, un cadre à la fois.
 */
export function OngletProduction({
  fiche,
  presentation,
  onEnregistre,
}: {
  fiche: FicheOperateur
  presentation: ReturnType<typeof usePresentation>
  onEnregistre: () => void
}) {
  const [cadre, setCadre] = useState<Cadre | null>(null)
  const regions = useListe('regions')
  const form = useForm<Saisie>({
    resolver: zodResolver(schema),
    defaultValues: depuis(fiche),
  })
  useEffect(() => form.reset(depuis(fiche)), [fiche, form])
  const alambics = useFieldArray({ control: form.control, name: 'alambics' })
  const periodicite = useWatch({ control: form.control, name: 'periodicite' })

  const mutation = useMutation({
    mutationFn: (s: Saisie) =>
      modifierProductionOperateur({
        path: { id: fiche.id },
        body: {
          version: fiche.version,
          periodicite:
            s.periodicite === AUCUN
              ? undefined
              : (s.periodicite as Periodicite),
          statutDeclarationStock: s.statutDeclarationStock as StatutDeclaration,
          region: identifiant(s.region),
          appellations: s.appellations as Appellation[],
          alambics: s.alambics.map((a) => ({
            numeroAgrement: texte(a.numeroAgrement),
            type: a.type as TypeAlambic,
            capacite: nombre(a.capacite),
          })),
        },
        throwOnError: true,
      }),
    meta: { erreurAffichee: true },
    onSuccess: () => {
      setCadre(null)
      onEnregistre()
    },
    onError: (error) =>
      afficherErreur(form, error, {
        'RG-OPE-01': 'periodicite',
        'RG-OPE-10': 'periodicite',
        'RG-OPE-11': 'statutDeclarationStock',
        'RG-OPE-21': 'region',
      }),
  })

  const gestion = fiche.droits.contacts === 'GESTION'
  const cadreDe = (cle: Cadre) => ({
    edition: cadre === cle,
    bloque: cadre != null || presentation.cadre != null,
    onEditer: () => setCadre(cle),
  })
  const cadreProduction = (cle: Cadre) => ({
    ...cadreDe(cle),
    modifiable: gestion && fiche.droits.personnelBnia,
    onAnnuler: () => {
      form.reset(depuis(fiche))
      setCadre(null)
    },
    enregistrement: mutation.isPending,
  })
  // Les cadres de présentation : leur édition est tenue par la présentation, un seul à la fois.
  const cadrePresentation = {
    presentation,
    modifiable: gestion,
    bloque: cadre != null,
  }

  const suggestion =
    fiche.periodiciteParDefaut && periodicite !== fiche.periodiciteParDefaut
      ? `Valeur par défaut pour ce type : ${PERIODICITES[fiche.periodiciteParDefaut].toLowerCase()}.`
      : undefined
  const c = form.control
  const nombreAlambics =
    cadre === 'alambics' ? alambics.fields.length : fiche.alambics.length
  return (
    <div className='space-y-4'>
      <Form {...form}>
        <form
          onSubmit={form.handleSubmit((s) => mutation.mutate(s))}
          className='space-y-4'
        >
          <ErreurRacine errors={form.formState.errors} />
          <CadreEditable titre='Déclaratif' {...cadreProduction('declaratif')}>
            {cadre === 'declaratif' ? (
              <div className='grid gap-4 md:grid-cols-3'>
                {fiche.type === 'AUTRE' ? (
                  <Lecture
                    label='Périodicité DSM'
                    valeur='Aucune : un opérateur « Autre » ne déclare pas'
                  />
                ) : (
                  <ChampChoix
                    control={c}
                    name='periodicite'
                    label='Périodicité DSM'
                    options={optionsListe(PERIODICITES)}
                    description={suggestion}
                  />
                )}
                <ChampChoix
                  control={c}
                  name='statutDeclarationStock'
                  label='Statut de déclaration de stock'
                  options={optionsListe(STATUTS).filter(
                    (o) => fiche.type !== 'AUTRE' || o.valeur !== 'OUI'
                  )}
                  description='Seul « Oui » rend une DSM attendue.'
                />
                <ChampChoix
                  control={c}
                  name='region'
                  label='Région'
                  facultatif
                  options={optionsReference(regions.valeurs, fiche.region)}
                />
                <ChampCases
                  control={c}
                  name='appellations'
                  label='Appellations'
                  className='md:col-span-3'
                  options={cles(APPELLATIONS).map((a) => ({
                    valeur: a,
                    libelle: APPELLATIONS[a],
                  }))}
                />
              </div>
            ) : (
              <div className='grid gap-4 md:grid-cols-3'>
                <Lecture
                  label='Périodicité DSM'
                  valeur={
                    fiche.type === 'AUTRE'
                      ? 'Aucune : un opérateur « Autre » ne déclare pas'
                      : fiche.periodicite
                        ? PERIODICITES[fiche.periodicite]
                        : undefined
                  }
                />
                <Lecture
                  label='Statut de déclaration de stock'
                  valeur={STATUTS[fiche.statutDeclarationStock]}
                />
                <Lecture
                  label='Région'
                  valeur={regions.libelle(fiche.region)}
                />
                <LectureListe
                  label='Appellations'
                  vide='Aucune appellation'
                  className='md:col-span-3'
                  valeurs={fiche.appellations.map((a) => APPELLATIONS[a])}
                />
              </div>
            )}
          </CadreEditable>
          <CadreEditable
            titre={
              <>
                Alambics{' '}
                <span className='text-muted-foreground text-sm font-normal'>
                  {nombreAlambics > 0
                    ? '· détenteur d’alambic'
                    : '· pas détenteur'}
                </span>
              </>
            }
            {...cadreProduction('alambics')}
          >
            {cadre === 'alambics' ? (
              <div className='space-y-3'>
                {alambics.fields.map((champ, i) => (
                  <div
                    key={champ.id}
                    className='grid items-end gap-3 md:grid-cols-[1fr_1fr_1fr_auto]'
                  >
                    <ChampChoix
                      control={c}
                      name={`alambics.${i}.type`}
                      label='Type'
                      options={optionsListe(TYPES_ALAMBIC)}
                    />
                    <ChampTexte
                      control={c}
                      name={`alambics.${i}.numeroAgrement`}
                      label="N° d'agrément"
                    />
                    <ChampTexte
                      control={c}
                      name={`alambics.${i}.capacite`}
                      label='Capacité'
                      description='Unité à préciser par le BNIA (Q-OPE-8).'
                    />
                    <Button
                      type='button'
                      variant='ghost'
                      size='icon'
                      onClick={() => alambics.remove(i)}
                    >
                      <Trash2 />
                      <span className='sr-only'>Retirer</span>
                    </Button>
                  </div>
                ))}
                <Button
                  type='button'
                  variant='outline'
                  onClick={() =>
                    alambics.append({
                      type: 'ARMAGNACAIS',
                      numeroAgrement: '',
                      capacite: '',
                    })
                  }
                >
                  <Plus /> Ajouter un alambic
                </Button>
              </div>
            ) : (
              <LectureListe
                vide='Aucun alambic'
                valeurs={fiche.alambics.map(alambic)}
              />
            )}
          </CadreEditable>
        </form>
      </Form>
      <CadreMarques portee='FRANCE' {...cadrePresentation} />
      <CadreMarques portee='EXPORT' {...cadrePresentation} />
      <CadreLabels {...cadrePresentation} />
    </div>
  )
}
