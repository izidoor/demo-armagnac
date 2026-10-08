import { useEffect } from 'react'
import { z } from 'zod'
import { useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery } from '@tanstack/react-query'
import { Link } from '@tanstack/react-router'
import {
  listerGroupesOperateurs,
  modifierIdentiteOperateur,
  modifierProfilOperateur,
  type FicheOperateur,
} from '@/api/generated'
import { useDebounce } from '@/hooks/use-debounce'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Form } from '@/components/ui/form'
import { afficherErreur } from '@/components/metier/erreur-formulaire'
import { ErreurRacine } from '@/components/metier/erreur-racine'
import { type Espace } from '../commun/api'
import {
  ChampChoix,
  ChampInterrupteur,
  ChampTexte,
  Lecture,
} from '../commun/champs'
import { AlerteDoublons } from '../commun/doublons'
import { TYPES, TYPES_CHOISIS } from '../commun/libelles'
import { useListe } from '../commun/referentiels'
import {
  identifiant,
  nombre,
  optionsListe,
  optionsReference,
  texte,
  versChoix,
  versTexte,
} from '../commun/saisie'
import { useDoublons } from '../commun/use-doublons'
import { Designations } from './designations'

const schema = z.object({
  type: z.enum(['BNIA', 'PRODUCTEUR', 'NEGOCIANT', 'AUTRE']),
  code: z.string().trim().min(1, 'Le code est obligatoire.'),
  groupe: z.string(),
  cvi: z.string(),
  siret: z.string(),
  accise: z.string(),
  bureauDouanes: z.string(),
  identifiantOdg: z.string(),
  titre: z.string(),
  raisonSociale: z.string().trim().min(1, 'La raison sociale est obligatoire.'),
  nomCommercial: z.string(),
  adresseLigne1: z.string(),
  adresseLigne2: z.string(),
  adresseLigne3: z.string(),
  codePostal: z.string(),
  ville: z.string(),
  pays: z.string(),
  telephone1: z.string(),
  telephone2: z.string(),
  portable: z.string(),
  emailGeneral: z.string(),
  siteWeb: z.string(),
  siteCommercial: z.string(),
  latitude: z.string(),
  longitude: z.string(),
  membreBnia: z.string(),
  accordDiffusionGeneral: z.boolean(),
  commentaireInterne: z.string(),
})

type Saisie = z.infer<typeof schema>

function depuis(f: FicheOperateur): Saisie {
  return {
    type: f.type,
    code: f.code,
    groupe: versTexte(f.groupe),
    cvi: versTexte(f.cvi),
    siret: versTexte(f.siret),
    accise: versTexte(f.accise),
    bureauDouanes: versTexte(f.bureauDouanes),
    identifiantOdg: versTexte(f.identifiantOdg),
    titre: versChoix(f.titre),
    raisonSociale: f.raisonSociale,
    nomCommercial: versTexte(f.nomCommercial),
    adresseLigne1: versTexte(f.adresseLigne1),
    adresseLigne2: versTexte(f.adresseLigne2),
    adresseLigne3: versTexte(f.adresseLigne3),
    codePostal: versTexte(f.codePostal),
    ville: versTexte(f.ville),
    pays: versChoix(f.pays),
    telephone1: versTexte(f.telephone1),
    telephone2: versTexte(f.telephone2),
    portable: versTexte(f.portable),
    emailGeneral: versTexte(f.emailGeneral),
    siteWeb: versTexte(f.siteWeb),
    siteCommercial: versTexte(f.siteCommercial),
    latitude: versTexte(f.latitude),
    longitude: versTexte(f.longitude),
    membreBnia: versChoix(f.membreBnia),
    accordDiffusionGeneral: f.accordDiffusionGeneral,
    commentaireInterne: versTexte(f.commentaireInterne),
  }
}

/** Ce qu'un opérateur modifie lui-même (RG-OPE-36). */
function versProfil(s: Saisie, version: number) {
  return {
    version,
    titre: identifiant(s.titre),
    nomCommercial: texte(s.nomCommercial),
    adresseLigne1: texte(s.adresseLigne1),
    adresseLigne2: texte(s.adresseLigne2),
    adresseLigne3: texte(s.adresseLigne3),
    codePostal: texte(s.codePostal),
    ville: texte(s.ville),
    pays: identifiant(s.pays),
    telephone1: texte(s.telephone1),
    telephone2: texte(s.telephone2),
    portable: texte(s.portable),
    emailGeneral: texte(s.emailGeneral),
    siteWeb: texte(s.siteWeb),
    siteCommercial: texte(s.siteCommercial),
    latitude: nombre(s.latitude),
    longitude: nombre(s.longitude),
    accordDiffusionGeneral: s.accordDiffusionGeneral,
  }
}

function versIdentite(s: Saisie, version: number) {
  return {
    ...versProfil(s, version),
    type: s.type,
    code: s.code.trim(),
    groupe: texte(s.groupe),
    cvi: texte(s.cvi),
    siret: texte(s.siret),
    accise: texte(s.accise),
    bureauDouanes: texte(s.bureauDouanes),
    identifiantOdg: texte(s.identifiantOdg),
    raisonSociale: s.raisonSociale.trim(),
    membreBnia: identifiant(s.membreBnia),
    commentaireInterne: texte(s.commentaireInterne),
  }
}

type OngletGeneralProps = {
  espace: Espace
  fiche: FicheOperateur
  onEnregistre: () => void
}

/**
 * Onglet Général : identité, coordonnées, membre BNIA, accord de diffusion, commentaire interne ; puis
 * contact principal et destinataire des factures. Hors du personnel du BNIA, seuls les champs de
 * RG-OPE-36 se modifient, et le commentaire interne n'apparaît pas ; sans la gestion du module
 * Contacts, tout est en lecture.
 */
export function OngletGeneral({
  espace,
  fiche,
  onEnregistre,
}: OngletGeneralProps) {
  const bo = fiche.droits.personnelBnia
  const gestion = fiche.droits.contacts === 'GESTION'
  const titres = useListe('titres')
  const pays = useListe('pays')
  const membres = useListe('membresBnia')
  const form = useForm<Saisie>({
    resolver: zodResolver(schema),
    defaultValues: depuis(fiche),
  })
  useEffect(() => form.reset(depuis(fiche)), [fiche, form])

  const [raisonSociale, cvi, siret, accise, groupe] = useWatch({
    control: form.control,
    name: ['raisonSociale', 'cvi', 'siret', 'accise', 'groupe'],
  })
  const doublons = useDoublons(
    bo ? { raisonSociale, cvi, siret, accise } : {},
    fiche.id
  )
  const groupeStable = useDebounce(groupe.trim())
  const groupes = useQuery({
    queryKey: ['groupes', groupeStable],
    queryFn: () =>
      listerGroupesOperateurs({
        query: { recherche: groupeStable || undefined },
        throwOnError: true,
      }).then((r) => r.data),
    enabled: bo,
  })

  const mutation = useMutation({
    mutationFn: (s: Saisie) =>
      bo
        ? modifierIdentiteOperateur({
            path: { id: fiche.id },
            body: versIdentite(s, fiche.version),
            throwOnError: true,
          })
        : modifierProfilOperateur({
            path: { id: fiche.id },
            body: versProfil(s, fiche.version),
            throwOnError: true,
          }),
    meta: { erreurAffichee: true },
    onSuccess: onEnregistre,
    onError: (error) =>
      afficherErreur(form, error, {
        'RG-OPE-01': 'type',
        'RG-OPE-04': 'code',
        'RG-OPE-06': 'siret',
        'RG-OPE-07': 'identifiantOdg',
        'RG-OPE-24': 'emailGeneral',
      }),
  })

  const c = form.control
  const ouvert = (label: string, nom: keyof Saisie, valeur?: string) =>
    bo ? (
      <ChampTexte control={c} name={nom} label={label} />
    ) : (
      <Lecture label={label} valeur={valeur} />
    )

  return (
    <div className='space-y-4'>
      <Form {...form}>
        <form
          onSubmit={form.handleSubmit((s) => mutation.mutate(s))}
          className='space-y-4'
        >
          <ErreurRacine errors={form.formState.errors} />
          <fieldset disabled={!gestion} className='space-y-4'>
            <Card>
              <CardHeader>
                <CardTitle>Identité</CardTitle>
              </CardHeader>
              <CardContent className='grid gap-4 md:grid-cols-3'>
                {bo && fiche.type !== 'BNIA' ? (
                  <ChampChoix
                    control={c}
                    name='type'
                    label='Type'
                    options={optionsListe(TYPES_CHOISIS)}
                    description='Changer le type ne change pas le code.'
                  />
                ) : (
                  <Lecture label='Type' valeur={TYPES[fiche.type]} />
                )}
                {ouvert('Code', 'code', fiche.code)}
                {bo ? (
                  <div className='space-y-1'>
                    <ChampTexte
                      control={c}
                      name='groupe'
                      label='Groupe'
                      list='groupes-operateurs'
                    />
                    <datalist id='groupes-operateurs'>
                      {groupes.data?.map((g) => (
                        <option key={g} value={g} />
                      ))}
                    </datalist>
                  </div>
                ) : (
                  <Lecture label='Groupe' valeur={fiche.groupe} />
                )}
                <ChampChoix
                  control={c}
                  name='titre'
                  label='Titre'
                  facultatif
                  options={optionsReference(titres.valeurs, fiche.titre)}
                />
                <div className='space-y-1 md:col-span-2'>
                  {bo ? (
                    <>
                      <ChampTexte
                        control={c}
                        name='raisonSociale'
                        label='Raison sociale'
                      />
                      <AlerteDoublons
                        doublons={doublons}
                        champ='RAISON_SOCIALE'
                      />
                    </>
                  ) : (
                    <Lecture
                      label='Raison sociale'
                      valeur={fiche.raisonSociale}
                    />
                  )}
                </div>
                <ChampTexte
                  control={c}
                  name='nomCommercial'
                  label='Nom commercial'
                />
                <div className='space-y-1'>
                  {ouvert('CVI', 'cvi', fiche.cvi)}
                  <AlerteDoublons doublons={doublons} champ='CVI' />
                </div>
                <div className='space-y-1'>
                  {ouvert('SIRET', 'siret', fiche.siret)}
                  <AlerteDoublons doublons={doublons} champ='SIRET' />
                </div>
                <div className='space-y-1'>
                  {ouvert("N° d'accise", 'accise', fiche.accise)}
                  <AlerteDoublons doublons={doublons} champ='ACCISE' />
                </div>
                {ouvert(
                  'Bureau des douanes',
                  'bureauDouanes',
                  fiche.bureauDouanes
                )}
                {ouvert(
                  'Identifiant tiers ODG',
                  'identifiantOdg',
                  fiche.identifiantOdg
                )}
                {bo ? (
                  <ChampChoix
                    control={c}
                    name='membreBnia'
                    label='Membre BNIA'
                    facultatif
                    options={optionsReference(
                      membres.valeurs,
                      fiche.membreBnia
                    )}
                  />
                ) : (
                  <Lecture
                    label='Membre BNIA'
                    valeur={membres.libelle(fiche.membreBnia)}
                  />
                )}
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle>Coordonnées</CardTitle>
              </CardHeader>
              <CardContent className='grid gap-4 md:grid-cols-3'>
                <ChampTexte control={c} name='adresseLigne1' label='Adresse' />
                <ChampTexte
                  control={c}
                  name='adresseLigne2'
                  label='Complément'
                />
                <ChampTexte
                  control={c}
                  name='adresseLigne3'
                  label='Complément (suite)'
                />
                <ChampTexte control={c} name='codePostal' label='Code postal' />
                <ChampTexte control={c} name='ville' label='Ville' />
                <ChampChoix
                  control={c}
                  name='pays'
                  label='Pays'
                  facultatif
                  options={optionsReference(pays.valeurs, fiche.pays)}
                />
                <ChampTexte control={c} name='telephone1' label='Téléphone 1' />
                <ChampTexte control={c} name='telephone2' label='Téléphone 2' />
                <ChampTexte control={c} name='portable' label='Portable' />
                <ChampTexte
                  control={c}
                  name='emailGeneral'
                  label='E-mail général'
                />
                <ChampTexte
                  control={c}
                  name='siteWeb'
                  label='Site web'
                  placeholder='https://…'
                />
                <ChampTexte
                  control={c}
                  name='siteCommercial'
                  label='Site commercial'
                  placeholder='https://…'
                />
                <ChampTexte
                  control={c}
                  name='latitude'
                  label='Latitude'
                  placeholder='43.958'
                />
                <ChampTexte
                  control={c}
                  name='longitude'
                  label='Longitude'
                  placeholder='0.372'
                />
                <ChampInterrupteur
                  control={c}
                  name='accordDiffusionGeneral'
                  label='Accord de diffusion général'
                />
                {bo && (
                  <ChampTexte
                    control={c}
                    name='commentaireInterne'
                    label='Commentaire interne'
                    description='Jamais montré au portail.'
                    multiligne
                    className='md:col-span-3'
                  />
                )}
              </CardContent>
            </Card>
          </fieldset>
          {gestion && (
            <div className='flex justify-end'>
              <Button type='submit' disabled={mutation.isPending}>
                Enregistrer
              </Button>
            </div>
          )}
        </form>
      </Form>

      {espace === 'bo' && fiche.autresDuGroupe.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Autres fiches du groupe « {fiche.groupe} »</CardTitle>
          </CardHeader>
          <CardContent className='flex flex-wrap gap-3 text-sm'>
            {fiche.autresDuGroupe.map((o) => (
              <Link
                key={o.id}
                to='/bo/operateurs/$id'
                params={{ id: String(o.id) }}
                className='underline'
              >
                {o.code} · {o.raisonSociale}
                {o.ville ? ` (${o.ville})` : ''}
                {o.actif ? '' : ', inactive'}
              </Link>
            ))}
          </CardContent>
        </Card>
      )}

      <Designations
        key={fiche.version}
        fiche={fiche}
        onEnregistre={onEnregistre}
      />
    </div>
  )
}
