import { useEffect } from 'react'
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
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Form } from '@/components/ui/form'
import { afficherErreur } from '@/components/metier/erreur-formulaire'
import { ErreurRacine } from '@/components/metier/erreur-racine'
import { type Espace } from '../commun/api'
import { ChampCases, ChampChoix, ChampTexte, Lecture } from '../commun/champs'
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

/**
 * Onglet Production : périodicité et statut de déclaration selon le type (RG-OPE-01, 10, 11), région,
 * appellations (RG-OPE-12), alambics (RG-OPE-13). En lecture au portail (RG-OPE-36).
 */
export function OngletProduction({
  espace,
  fiche,
  onEnregistre,
}: {
  espace: Espace
  fiche: FicheOperateur
  onEnregistre: () => void
}) {
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
    onSuccess: onEnregistre,
    onError: (error) =>
      afficherErreur(form, error, {
        'RG-OPE-01': 'periodicite',
        'RG-OPE-10': 'periodicite',
        'RG-OPE-11': 'statutDeclarationStock',
        'RG-OPE-21': 'region',
      }),
  })

  if (espace === 'portail') {
    return (
      <Card>
        <CardContent className='grid gap-4 pt-6 md:grid-cols-3'>
          <Lecture
            label='Périodicité DSM'
            valeur={
              fiche.periodicite ? PERIODICITES[fiche.periodicite] : undefined
            }
          />
          <Lecture
            label='Statut de déclaration de stock'
            valeur={STATUTS[fiche.statutDeclarationStock]}
          />
          <Lecture label='Région' valeur={regions.libelle(fiche.region)} />
          <Lecture
            label='Appellations'
            valeur={fiche.appellations.map((a) => APPELLATIONS[a]).join(', ')}
          />
          <Lecture
            label='Alambics'
            className='md:col-span-2'
            valeur={fiche.alambics
              .map(
                (a) =>
                  `${TYPES_ALAMBIC[a.type]}${a.numeroAgrement ? ` n° ${a.numeroAgrement}` : ''}${a.capacite ? `, ${a.capacite}` : ''}`
              )
              .join(' ; ')}
          />
        </CardContent>
      </Card>
    )
  }

  const suggestion =
    fiche.periodiciteParDefaut && periodicite !== fiche.periodiciteParDefaut
      ? `Valeur par défaut pour ce type : ${PERIODICITES[fiche.periodiciteParDefaut].toLowerCase()}.`
      : undefined
  const c = form.control
  return (
    <Form {...form}>
      <form
        onSubmit={form.handleSubmit((s) => mutation.mutate(s))}
        className='space-y-4'
      >
        <ErreurRacine errors={form.formState.errors} />
        <Card>
          <CardHeader>
            <CardTitle>Déclaratif</CardTitle>
          </CardHeader>
          <CardContent className='grid gap-4 md:grid-cols-3'>
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
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>
              Alambics{' '}
              <span className='text-muted-foreground text-sm font-normal'>
                {alambics.fields.length > 0
                  ? '· détenteur d’alambic'
                  : '· pas détenteur'}
              </span>
            </CardTitle>
          </CardHeader>
          <CardContent className='space-y-3'>
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
          </CardContent>
        </Card>
        <div className='flex justify-end'>
          <Button type='submit' disabled={mutation.isPending}>
            Enregistrer
          </Button>
        </div>
      </form>
    </Form>
  )
}
