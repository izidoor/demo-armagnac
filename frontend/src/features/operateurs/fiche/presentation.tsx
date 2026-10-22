import { useState, type ReactNode } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Trash2 } from 'lucide-react'
import { toast } from 'sonner'
import {
  creerMarque,
  listerMarquesProches,
  type MedailleConcours,
  type PresentationOperateur,
} from '@/api/generated'
import { lireProbleme } from '@/lib/probleme'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { DialogFooter } from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Switch } from '@/components/ui/switch'
import { Textarea } from '@/components/ui/textarea'
import { CadreEditable } from '@/components/metier/cadre-editable'
import { DialogueFormulaire } from '@/components/metier/dialogue-formulaire'
import { ValeursProches } from '@/features/referentiels/listes-simples/valeurs-proches'
import { Lecture, LectureListe } from '../commun/champs'
import {
  CARTES,
  DISTINCTIONS,
  MODES_PRESENTATION,
  cles,
  type Distinction,
  type ModePresentation,
} from '../commun/libelles'
import { useListe, type Liste } from '../commun/referentiels'
import { AUCUN } from '../commun/saisie'
import { type usePresentation } from './use-presentation'

type Portee = 'FRANCE' | 'EXPORT'

type Presentation = ReturnType<typeof usePresentation>

function ChoixValeur({
  liste,
  valeur,
  onChange,
  placeholder = 'Choisir',
}: {
  liste: Liste
  valeur?: number
  onChange: (id: number) => void
  placeholder?: string
}) {
  return (
    <Select
      value={valeur ? String(valeur) : AUCUN}
      onValueChange={(v) => onChange(Number(v))}
    >
      <SelectTrigger className='w-full'>
        <SelectValue placeholder={placeholder} />
      </SelectTrigger>
      <SelectContent>
        {liste.proposees(valeur ? [valeur] : []).map((v) => (
          <SelectItem key={v.id} value={String(v.id)}>
            {v.libelle}
            {v.actif ? '' : ' (inactif)'}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  )
}

/**
 * Ce que reçoit un cadre de présentation. Le cadre en édition est tenu par la présentation (un seul à la
 * fois, d'un onglet à l'autre) ; `bloque` désactive en plus « Éditer » (un autre cadre de l'onglet).
 */
export type CadrePresentationProps = {
  presentation: Presentation
  modifiable: boolean
  bloque?: boolean
}

/** Un cadre de présentation : « Annuler » reprend ses champs, « Enregistrer » toute la présentation. */
function CadrePresentation({
  cle,
  titre,
  champs,
  presentation,
  modifiable,
  bloque = false,
  className,
  children,
}: CadrePresentationProps & {
  cle: string
  titre: ReactNode
  champs: (keyof PresentationOperateur)[]
  className?: string
  children: ReactNode
}) {
  const edition = presentation.cadre === cle
  const onFermer = () => presentation.editer(null)
  return (
    <CadreEditable
      titre={titre}
      edition={edition}
      modifiable={modifiable}
      bloque={bloque || presentation.cadre != null}
      onEditer={() => presentation.editer(cle)}
      className={className}
      onAnnuler={() => {
        presentation.retablir(champs)
        onFermer()
      }}
      onEnregistrer={() =>
        presentation.enregistrer(() => {
          toast.success('Enregistré.')
          onFermer()
        })
      }
      enregistrement={presentation.enCours}
    >
      <div className='space-y-4'>
        {edition && presentation.erreur && (
          <Alert variant='destructive'>
            <AlertDescription>{presentation.erreur}</AlertDescription>
          </Alert>
        )}
        {children}
      </div>
    </CadreEditable>
  )
}

/** Marques (diffusables web) et circuits d'une portée, France ou Export (RG-OPE-16). */
export function CadreMarques({
  portee,
  ...cadre
}: CadrePresentationProps & { portee: Portee }) {
  const { presentation } = cadre
  const cle = `marques-${portee}`
  const edition = presentation.cadre === cle
  const marques = useListe('marques')
  const circuits = useListe('circuits')
  const [nouvelle, setNouvelle] = useState(false)
  const p = presentation.valeur
  if (!p) return null
  const sesMarques = p.marques.filter((m) => m.portee === portee)
  const sesCircuits = p.circuits.filter((c) => c.portee === portee)
  return (
    <CadrePresentation
      {...cadre}
      cle={cle}
      titre={`Marques et circuits · ${portee === 'FRANCE' ? 'France' : 'Export'}`}
      champs={['marques', 'circuits']}
    >
      {!edition ? (
        <div className='grid gap-6 @min-[48rem]/content:grid-cols-2'>
          <LectureListe
            label='Marques'
            vide='Aucune marque'
            valeurs={sesMarques.map((m) => (
              <span className='flex items-center gap-2'>
                {marques.libelle(m.marque)}
                {m.diffusableWeb && (
                  <Badge variant='secondary'>Diffusable web</Badge>
                )}
              </span>
            ))}
          />
          <LectureListe
            label='Circuits de distribution'
            vide='Aucun circuit de distribution'
            valeurs={sesCircuits.map((c) => circuits.libelle(c.circuit))}
          />
        </div>
      ) : (
        <div className='grid gap-6 @min-[48rem]/content:grid-cols-2'>
          <div className='space-y-2'>
            <Label>Marques</Label>
            {p.marques.map((m, i) =>
              m.portee !== portee ? null : (
                <div key={i} className='flex items-center gap-2'>
                  <ChoixValeur
                    liste={marques}
                    valeur={m.marque}
                    onChange={(marque) =>
                      presentation.modifier({
                        marques: p.marques.map((x, j) =>
                          j === i ? { ...x, marque } : x
                        ),
                      })
                    }
                  />
                  <label className='flex shrink-0 items-center gap-1 text-sm'>
                    <Checkbox
                      checked={m.diffusableWeb}
                      onCheckedChange={(coche) =>
                        presentation.modifier({
                          marques: p.marques.map((x, j) =>
                            j === i
                              ? { ...x, diffusableWeb: coche === true }
                              : x
                          ),
                        })
                      }
                    />
                    Diffusable web
                  </label>
                  <Retirer
                    onClick={() =>
                      presentation.modifier({
                        marques: p.marques.filter((_, j) => j !== i),
                      })
                    }
                  />
                </div>
              )
            )}
            <div className='flex gap-2'>
              <Ajouter
                libelle='Ajouter une marque'
                onClick={() => {
                  const premiere = marques.proposees()[0]
                  if (premiere)
                    presentation.modifier({
                      marques: [
                        ...p.marques,
                        { portee, marque: premiere.id, diffusableWeb: false },
                      ],
                    })
                }}
              />
              <Button
                type='button'
                variant='ghost'
                onClick={() => setNouvelle(true)}
              >
                Créer une marque
              </Button>
            </div>
          </div>
          <div className='space-y-2'>
            <Label>Circuits de distribution</Label>
            {p.circuits.map((c, i) =>
              c.portee !== portee ? null : (
                <div key={i} className='flex items-center gap-2'>
                  <ChoixValeur
                    liste={circuits}
                    valeur={c.circuit}
                    onChange={(circuit) =>
                      presentation.modifier({
                        circuits: p.circuits.map((x, j) =>
                          j === i ? { ...x, circuit } : x
                        ),
                      })
                    }
                  />
                  <Retirer
                    onClick={() =>
                      presentation.modifier({
                        circuits: p.circuits.filter((_, j) => j !== i),
                      })
                    }
                  />
                </div>
              )
            )}
            <Ajouter
              libelle='Ajouter un circuit'
              onClick={() => {
                const premier = circuits.proposees()[0]
                if (premier)
                  presentation.modifier({
                    circuits: [...p.circuits, { portee, circuit: premier.id }],
                  })
              }}
            />
          </div>
        </div>
      )}
      <NouvelleMarque
        portee={nouvelle ? portee : null}
        onFermer={() => setNouvelle(false)}
        onCree={(marque) =>
          presentation.modifier({
            marques: [...p.marques, { portee, marque, diffusableWeb: false }],
          })
        }
      />
    </CadrePresentation>
  )
}

/** Création d'une marque depuis la fiche (RG-REF-14), avec les libellés proches (RG-REF-15). */
function NouvelleMarque({
  portee,
  onFermer,
  onCree,
}: {
  portee: Portee | null
  onFermer: () => void
  onCree: (id: number) => void
}) {
  return (
    <DialogueFormulaire
      ouvert={portee !== null}
      onOuvertChange={(o) => !o && onFermer()}
      titre='Nouvelle marque'
      className='sm:max-w-md'
    >
      {portee && <SaisieMarque onFermer={onFermer} onCree={onCree} />}
    </DialogueFormulaire>
  )
}

/** Monté à chaque ouverture : la saisie repart de zéro. */
function SaisieMarque({
  onFermer,
  onCree,
}: {
  onFermer: () => void
  onCree: (id: number) => void
}) {
  const queryClient = useQueryClient()
  const [code, setCode] = useState('')
  const [libelle, setLibelle] = useState('')
  const [erreur, setErreur] = useState<string | null>(null)
  const mutation = useMutation({
    mutationFn: () =>
      creerMarque({
        body: { code: code.trim(), libelle: libelle.trim() },
        throwOnError: true,
      }),
    meta: { erreurAffichee: true },
    onSuccess: (r) => {
      queryClient.invalidateQueries({ queryKey: ['marques'] })
      onCree(r.data.id)
      onFermer()
    },
    onError: (error) => {
      const p = lireProbleme(error)
      setErreur(p.regle ? `${p.detail} (${p.regle})` : (p.detail ?? p.titre))
    },
  })
  return (
    <>
      <div className='space-y-3'>
        {erreur && (
          <Alert variant='destructive'>
            <AlertDescription>{erreur}</AlertDescription>
          </Alert>
        )}
        <div className='space-y-1'>
          <Label>Code</Label>
          <Input value={code} onChange={(e) => setCode(e.target.value)} />
        </div>
        <div className='space-y-1'>
          <Label>Libellé</Label>
          <Input value={libelle} onChange={(e) => setLibelle(e.target.value)} />
          <p className='text-muted-foreground text-sm'>
            <ValeursProches
              libelle={libelle}
              proches={(texte) =>
                listerMarquesProches({
                  query: { libelle: texte },
                  throwOnError: true,
                }).then((r) => r.data)
              }
            />
          </p>
        </div>
      </div>
      <DialogFooter>
        <Button variant='outline' className='max-md:hidden' onClick={onFermer}>
          Annuler
        </Button>
        <Button
          className='max-md:w-full'
          onClick={() => mutation.mutate()}
          disabled={mutation.isPending}
        >
          Créer
        </Button>
      </DialogFooter>
    </>
  )
}

/** Visites, chacune au plus une fois, avec un commentaire (RG-OPE-17). */
function CadreVisites(cadre: CadrePresentationProps) {
  const { presentation } = cadre
  const edition = presentation.cadre === 'visites'
  const typesVisite = useListe('typesVisite')
  const p = presentation.valeur
  if (!p) return null
  return (
    <CadrePresentation
      {...cadre}
      cle='visites'
      titre='Visites'
      champs={['visites']}
    >
      {!edition ? (
        <LectureListe
          vide='Aucune visite'
          valeurs={p.visites.map(
            (v) =>
              `${typesVisite.libelle(v.typeVisite) ?? ''}${v.commentaire ? ` · ${v.commentaire}` : ''}`
          )}
        />
      ) : (
        <div className='space-y-2'>
          {p.visites.map((v, i) => (
            <div
              key={i}
              className='grid grid-cols-[1fr_1fr_auto] items-center gap-2'
            >
              <ChoixValeur
                liste={typesVisite}
                valeur={v.typeVisite}
                onChange={(typeVisite) =>
                  presentation.modifier({
                    visites: p.visites.map((x, j) =>
                      j === i ? { ...x, typeVisite } : x
                    ),
                  })
                }
              />
              <Input
                placeholder='Commentaire'
                value={v.commentaire ?? ''}
                onChange={(e) =>
                  presentation.modifier({
                    visites: p.visites.map((x, j) =>
                      j === i ? { ...x, commentaire: e.target.value } : x
                    ),
                  })
                }
              />
              <Retirer
                onClick={() =>
                  presentation.modifier({
                    visites: p.visites.filter((_, j) => j !== i),
                  })
                }
              />
            </div>
          ))}
          <Ajouter
            libelle='Ajouter une visite'
            onClick={() => {
              const premier = typesVisite.proposees()[0]
              if (premier)
                presentation.modifier({
                  visites: [...p.visites, { typeVisite: premier.id }],
                })
            }}
          />
        </div>
      )}
    </CadrePresentation>
  )
}

/** Labels, chacun au plus une fois, avec un commentaire (RG-OPE-17). */
export function CadreLabels(cadre: CadrePresentationProps) {
  const { presentation } = cadre
  const edition = presentation.cadre === 'labels'
  const labels = useListe('labels')
  const p = presentation.valeur
  if (!p) return null
  return (
    <CadrePresentation
      {...cadre}
      cle='labels'
      titre='Labels'
      champs={['labels']}
    >
      {!edition ? (
        <LectureListe
          vide='Aucun label'
          valeurs={p.labels.map(
            (l) =>
              `${labels.libelle(l.label) ?? ''}${l.commentaire ? ` · ${l.commentaire}` : ''}`
          )}
        />
      ) : (
        <div className='space-y-2'>
          {p.labels.map((l, i) => (
            <div
              key={i}
              className='grid grid-cols-[1fr_1fr_auto] items-center gap-2'
            >
              <ChoixValeur
                liste={labels}
                valeur={l.label}
                onChange={(label) =>
                  presentation.modifier({
                    labels: p.labels.map((x, j) =>
                      j === i ? { ...x, label } : x
                    ),
                  })
                }
              />
              <Input
                placeholder='Commentaire'
                value={l.commentaire ?? ''}
                onChange={(e) =>
                  presentation.modifier({
                    labels: p.labels.map((x, j) =>
                      j === i ? { ...x, commentaire: e.target.value } : x
                    ),
                  })
                }
              />
              <Retirer
                onClick={() =>
                  presentation.modifier({
                    labels: p.labels.filter((_, j) => j !== i),
                  })
                }
              />
            </div>
          ))}
          <Ajouter
            libelle='Ajouter un label'
            onClick={() => {
              const premier = labels.proposees()[0]
              if (premier)
                presentation.modifier({
                  labels: [...p.labels, { label: premier.id }],
                })
            }}
          />
        </div>
      )}
    </CadrePresentation>
  )
}

/**
 * Onglet Œnotourisme : accueil, visites, langues, produits, présentations, médailles (RG-OPE-17 à 19).
 * Chaque cadre s'affiche en lecture ; « Éditer » le passe en saisie.
 */
export function SectionOenotourisme({
  presentation,
  modifiable,
}: {
  presentation: Presentation
  modifiable: boolean
}) {
  const langues = useListe('langues')
  const produits = useListe('produits')
  const p = presentation.valeur
  if (!p) return <p className='text-muted-foreground'>Chargement…</p>
  const cadre = { presentation, modifiable }
  const enEdition = (cle: string) => presentation.cadre === cle
  const ouiNon = (v: boolean) => (v ? 'Oui' : 'Non')
  const bascule = (
    nom:
      | 'degustationGratuite'
      | 'lieuDeVente'
      | 'accueilGroupes'
      | 'adhesionOenotourisme',
    libelle: string
  ) => (
    <label className='flex items-center gap-2 text-sm'>
      <Switch
        checked={p[nom]}
        onCheckedChange={(v) => presentation.modifier({ [nom]: v })}
      />
      {libelle}
    </label>
  )
  const cases = (nom: 'langues' | 'produitsCommercialises', liste: Liste) => (
    <div className='grid gap-2 @min-[48rem]/content:grid-cols-2'>
      {liste.proposees(p[nom]).map((v) => (
        <label key={v.id} className='flex items-center gap-2 text-sm'>
          <Checkbox
            checked={p[nom].includes(v.id)}
            onCheckedChange={(coche) =>
              presentation.modifier({
                [nom]: coche
                  ? [...p[nom], v.id]
                  : p[nom].filter((x) => x !== v.id),
              })
            }
          />
          {v.libelle}
          {v.actif ? '' : ' (inactif)'}
        </label>
      ))}
    </div>
  )
  const medaille = (i: number, changement: Partial<MedailleConcours>) =>
    presentation.modifier({
      medailles: p.medailles.map((m, j) =>
        j === i ? { ...m, ...changement } : m
      ),
    })
  return (
    <div className='space-y-4'>
      <CadrePresentation
        {...cadre}
        cle='accueil'
        titre='Accueil'
        champs={[
          'paiementCarte',
          'cartes',
          'degustationGratuite',
          'lieuDeVente',
          'accueilGroupes',
          'adhesionOenotourisme',
          'modePresentation',
        ]}
      >
        {!enEdition('accueil') ? (
          <div className='grid gap-4 @min-[48rem]/content:grid-cols-3'>
            <Lecture
              label='Paiement par carte'
              valeur={
                p.paiementCarte
                  ? p.cartes.length > 0
                    ? `Oui : ${p.cartes.map((c) => CARTES[c]).join(', ')}`
                    : 'Oui'
                  : 'Non'
              }
            />
            <Lecture
              label='Dégustation gratuite'
              valeur={ouiNon(p.degustationGratuite)}
            />
            <Lecture
              label='Lieu de vente aménagé'
              valeur={ouiNon(p.lieuDeVente)}
            />
            <Lecture
              label='Accueil de groupes'
              valeur={ouiNon(p.accueilGroupes)}
            />
            <Lecture
              label='Adhésion œnotourisme BNIA'
              valeur={ouiNon(p.adhesionOenotourisme)}
            />
            <Lecture
              label='Mode de présentation des produits'
              valeur={
                p.modePresentation
                  ? MODES_PRESENTATION[p.modePresentation]
                  : undefined
              }
            />
          </div>
        ) : (
          <div className='grid gap-4 @min-[48rem]/content:grid-cols-2'>
            <div className='space-y-2'>
              <label className='flex items-center gap-2 text-sm'>
                <Switch
                  checked={p.paiementCarte}
                  onCheckedChange={(v) =>
                    presentation.modifier({
                      paiementCarte: v,
                      cartes: v ? p.cartes : [],
                    })
                  }
                />
                Paiement par carte
              </label>
              {p.paiementCarte && (
                <div className='flex flex-wrap gap-4 ps-2'>
                  {cles(CARTES).map((carte) => (
                    <label
                      key={carte}
                      className='flex items-center gap-2 text-sm'
                    >
                      <Checkbox
                        checked={p.cartes.includes(carte)}
                        onCheckedChange={(coche) =>
                          presentation.modifier({
                            cartes: coche
                              ? [...p.cartes, carte]
                              : p.cartes.filter((x) => x !== carte),
                          })
                        }
                      />
                      {CARTES[carte]}
                    </label>
                  ))}
                </div>
              )}
            </div>
            <div className='space-y-2'>
              {bascule('degustationGratuite', 'Dégustation gratuite')}
              {bascule('lieuDeVente', 'Lieu de vente aménagé')}
              {bascule('accueilGroupes', 'Accueil de groupes')}
              {bascule('adhesionOenotourisme', 'Adhésion œnotourisme BNIA')}
            </div>
            <div className='space-y-2'>
              <Label>Mode de présentation des produits</Label>
              <Select
                value={p.modePresentation ?? AUCUN}
                onValueChange={(v) =>
                  presentation.modifier({
                    modePresentation:
                      v === AUCUN ? undefined : (v as ModePresentation),
                  })
                }
              >
                <SelectTrigger className='w-full'>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={AUCUN}>—</SelectItem>
                  {cles(MODES_PRESENTATION).map((m) => (
                    <SelectItem key={m} value={m}>
                      {MODES_PRESENTATION[m]}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
        )}
      </CadrePresentation>
      <CadreVisites {...cadre} />
      <div className='grid gap-4 @min-[48rem]/content:grid-cols-2'>
        <CadrePresentation
          {...cadre}
          cle='langues'
          titre='Langues parlées'
          champs={['langues']}
        >
          {enEdition('langues') ? (
            cases('langues', langues)
          ) : (
            <LectureListe
              vide='Aucune langue'
              valeurs={p.langues.map((l) => langues.libelle(l))}
            />
          )}
        </CadrePresentation>
        <CadrePresentation
          {...cadre}
          cle='produits'
          titre='Produits commercialisés'
          champs={['produitsCommercialises']}
        >
          {enEdition('produits') ? (
            cases('produitsCommercialises', produits)
          ) : (
            <LectureListe
              vide='Aucun produit commercialisé'
              valeurs={p.produitsCommercialises.map((x) => produits.libelle(x))}
            />
          )}
        </CadrePresentation>
      </div>
      <CadrePresentation
        {...cadre}
        cle='presentation'
        titre='Présentation du domaine'
        champs={['presentationFr', 'presentationEn']}
      >
        {enEdition('presentation') ? (
          <div className='grid gap-4 @min-[48rem]/content:grid-cols-2'>
            <div className='space-y-1'>
              <Label>En français</Label>
              <Textarea
                rows={5}
                value={p.presentationFr ?? ''}
                onChange={(e) =>
                  presentation.modifier({ presentationFr: e.target.value })
                }
              />
            </div>
            <div className='space-y-1'>
              <Label>En anglais</Label>
              <Textarea
                rows={5}
                value={p.presentationEn ?? ''}
                onChange={(e) =>
                  presentation.modifier({ presentationEn: e.target.value })
                }
              />
            </div>
          </div>
        ) : (
          <div className='grid gap-4 @min-[48rem]/content:grid-cols-2'>
            <Lecture
              label='En français'
              valeur={
                p.presentationFr && (
                  <span className='whitespace-pre-line'>
                    {p.presentationFr}
                  </span>
                )
              }
            />
            <Lecture
              label='En anglais'
              valeur={
                p.presentationEn && (
                  <span className='whitespace-pre-line'>
                    {p.presentationEn}
                  </span>
                )
              }
            />
          </div>
        )}
      </CadrePresentation>
      <CadrePresentation
        {...cadre}
        cle='medailles'
        titre='Médailles et concours'
        champs={['medailles']}
      >
        {enEdition('medailles') ? (
          <div className='space-y-2'>
            {p.medailles.map((m, i) => (
              <div
                key={i}
                className='grid items-center gap-2 @min-[48rem]/content:grid-cols-[2fr_1fr_1fr_2fr_auto]'
              >
                <Input
                  placeholder='Concours'
                  value={m.concours}
                  onChange={(e) => medaille(i, { concours: e.target.value })}
                />
                <Input
                  placeholder='Année'
                  value={m.annee ?? ''}
                  onChange={(e) =>
                    medaille(i, {
                      annee:
                        e.target.value === ''
                          ? undefined
                          : Number(e.target.value),
                    })
                  }
                />
                <Select
                  value={m.distinction ?? AUCUN}
                  onValueChange={(v) =>
                    medaille(i, {
                      distinction: v === AUCUN ? undefined : (v as Distinction),
                    })
                  }
                >
                  <SelectTrigger className='w-full'>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value={AUCUN}>—</SelectItem>
                    {cles(DISTINCTIONS).map((d) => (
                      <SelectItem key={d} value={d}>
                        {DISTINCTIONS[d]}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <Input
                  placeholder='Produit'
                  value={m.produit ?? ''}
                  onChange={(e) => medaille(i, { produit: e.target.value })}
                />
                <Retirer
                  onClick={() =>
                    presentation.modifier({
                      medailles: p.medailles.filter((_, j) => j !== i),
                    })
                  }
                />
              </div>
            ))}
            <Ajouter
              libelle='Ajouter une médaille'
              onClick={() =>
                presentation.modifier({
                  medailles: [...p.medailles, { concours: '' }],
                })
              }
            />
          </div>
        ) : (
          <LectureListe
            vide='Aucune médaille'
            valeurs={p.medailles.map((m) =>
              [
                m.concours,
                m.annee,
                m.distinction && DISTINCTIONS[m.distinction],
                m.produit,
              ]
                .filter(Boolean)
                .join(' · ')
            )}
          />
        )}
      </CadrePresentation>
    </div>
  )
}

function Ajouter({
  libelle,
  onClick,
}: {
  libelle: string
  onClick: () => void
}) {
  return (
    <Button type='button' variant='outline' size='sm' onClick={onClick}>
      <Plus /> {libelle}
    </Button>
  )
}

function Retirer({ onClick }: { onClick: () => void }) {
  return (
    <Button type='button' variant='ghost' size='icon' onClick={onClick}>
      <Trash2 />
      <span className='sr-only'>Retirer</span>
    </Button>
  )
}
