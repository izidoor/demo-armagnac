import { useState, type ReactNode } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Trash2 } from 'lucide-react'
import {
  creerMarque,
  listerMarquesProches,
  type MedailleConcours,
} from '@/api/generated'
import { lireProbleme } from '@/lib/probleme'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Checkbox } from '@/components/ui/checkbox'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
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
import { ValeursProches } from '@/features/referentiels/listes-simples/valeurs-proches'
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

function Gabarit({
  presentation,
  children,
}: {
  presentation: Presentation
  children: ReactNode
}) {
  if (!presentation.valeur)
    return <p className='text-muted-foreground'>Chargement…</p>
  return (
    <div className='space-y-4'>
      {presentation.erreur && (
        <Alert variant='destructive'>
          <AlertDescription>{presentation.erreur}</AlertDescription>
        </Alert>
      )}
      {children}
      <div className='flex justify-end'>
        <Button
          onClick={presentation.enregistrer}
          disabled={presentation.enCours}
        >
          Enregistrer
        </Button>
      </div>
    </div>
  )
}

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

/** Onglet Marques : marques (diffusables web) et circuits, par portée France et Export (RG-OPE-16). */
export function SectionMarques({
  presentation,
}: {
  presentation: Presentation
}) {
  const marques = useListe('marques')
  const circuits = useListe('circuits')
  const [nouvelle, setNouvelle] = useState<Portee | null>(null)
  const p = presentation.valeur
  return (
    <Gabarit presentation={presentation}>
      {p &&
        (['FRANCE', 'EXPORT'] as Portee[]).map((portee) => (
          <Card key={portee}>
            <CardHeader>
              <CardTitle>{portee === 'FRANCE' ? 'France' : 'Export'}</CardTitle>
            </CardHeader>
            <CardContent className='grid gap-6 md:grid-cols-2'>
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
                            {
                              portee,
                              marque: premiere.id,
                              diffusableWeb: false,
                            },
                          ],
                        })
                    }}
                  />
                  <Button
                    type='button'
                    variant='ghost'
                    onClick={() => setNouvelle(portee)}
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
                        circuits: [
                          ...p.circuits,
                          { portee, circuit: premier.id },
                        ],
                      })
                  }}
                />
              </div>
            </CardContent>
          </Card>
        ))}
      <NouvelleMarque
        portee={nouvelle}
        onFermer={() => setNouvelle(null)}
        onCree={(marque) =>
          p &&
          nouvelle &&
          presentation.modifier({
            marques: [
              ...p.marques,
              { portee: nouvelle, marque, diffusableWeb: false },
            ],
          })
        }
      />
    </Gabarit>
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
    <Dialog open={portee !== null} onOpenChange={(o) => !o && onFermer()}>
      <DialogContent className='sm:max-w-md'>
        <DialogHeader>
          <DialogTitle>Nouvelle marque</DialogTitle>
        </DialogHeader>
        {portee && <SaisieMarque onFermer={onFermer} onCree={onCree} />}
      </DialogContent>
    </Dialog>
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
        <Button variant='outline' onClick={onFermer}>
          Annuler
        </Button>
        <Button onClick={() => mutation.mutate()} disabled={mutation.isPending}>
          Créer
        </Button>
      </DialogFooter>
    </>
  )
}

/** Onglet Visites et labels : chacun au plus une fois, avec un commentaire (RG-OPE-17). */
export function SectionVisitesLabels({
  presentation,
}: {
  presentation: Presentation
}) {
  const typesVisite = useListe('typesVisite')
  const labels = useListe('labels')
  const p = presentation.valeur
  return (
    <Gabarit presentation={presentation}>
      {p && (
        <div className='grid gap-4 md:grid-cols-2'>
          <Card>
            <CardHeader>
              <CardTitle>Visites</CardTitle>
            </CardHeader>
            <CardContent className='space-y-2'>
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
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardTitle>Labels</CardTitle>
            </CardHeader>
            <CardContent className='space-y-2'>
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
            </CardContent>
          </Card>
        </div>
      )}
    </Gabarit>
  )
}

/** Onglet Œnotourisme : accueil, langues, produits, présentations, médailles (RG-OPE-18, 19). */
export function SectionOenotourisme({
  presentation,
}: {
  presentation: Presentation
}) {
  const langues = useListe('langues')
  const produits = useListe('produits')
  const p = presentation.valeur
  const bascule = (
    nom:
      | 'degustationGratuite'
      | 'lieuDeVente'
      | 'accueilGroupes'
      | 'adhesionOenotourisme',
    libelle: string
  ) =>
    p && (
      <label className='flex items-center gap-2 text-sm'>
        <Switch
          checked={p[nom]}
          onCheckedChange={(v) => presentation.modifier({ [nom]: v })}
        />
        {libelle}
      </label>
    )
  const cases = (nom: 'langues' | 'produitsCommercialises', liste: Liste) =>
    p && (
      <div className='grid gap-2 sm:grid-cols-2'>
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
    p &&
    presentation.modifier({
      medailles: p.medailles.map((m, j) =>
        j === i ? { ...m, ...changement } : m
      ),
    })
  return (
    <Gabarit presentation={presentation}>
      {p && (
        <>
          <Card>
            <CardHeader>
              <CardTitle>Accueil</CardTitle>
            </CardHeader>
            <CardContent className='grid gap-4 md:grid-cols-2'>
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
            </CardContent>
          </Card>
          <div className='grid gap-4 md:grid-cols-2'>
            <Card>
              <CardHeader>
                <CardTitle>Langues parlées</CardTitle>
              </CardHeader>
              <CardContent>{cases('langues', langues)}</CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle>Produits commercialisés</CardTitle>
              </CardHeader>
              <CardContent>
                {cases('produitsCommercialises', produits)}
              </CardContent>
            </Card>
          </div>
          <Card>
            <CardHeader>
              <CardTitle>Présentation du domaine</CardTitle>
            </CardHeader>
            <CardContent className='grid gap-4 md:grid-cols-2'>
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
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardTitle>Médailles et concours</CardTitle>
            </CardHeader>
            <CardContent className='space-y-2'>
              {p.medailles.map((m, i) => (
                <div
                  key={i}
                  className='grid items-center gap-2 md:grid-cols-[2fr_1fr_1fr_2fr_auto]'
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
                        distinction:
                          v === AUCUN ? undefined : (v as Distinction),
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
            </CardContent>
          </Card>
        </>
      )}
    </Gabarit>
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
