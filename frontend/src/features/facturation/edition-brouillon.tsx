import { type ReactNode, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  closestCenter,
  DndContext,
  type DragEndEvent,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core'
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import {
  ChevronDown,
  GripVertical,
  Folder,
  Package,
  Lock,
  Plus,
  Save,
  Text,
  Trash2,
  TriangleAlert,
} from 'lucide-react'
import { toast } from 'sonner'
import {
  listerArticlesProposes,
  modifierBrouillon,
  type ArticlePropose,
  type LignePiece,
  type LigneSaisie,
  type LigneTvaPiece,
  type ModificationBrouillon,
  type Piece,
} from '@/api/generated'
import { lireProbleme } from '@/lib/probleme'
import { cn } from '@/lib/utils'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/components/ui/command'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Switch } from '@/components/ui/switch'
import { Textarea } from '@/components/ui/textarea'
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/ui/tooltip'
import { formatDate } from '@/features/declarations/format'
import { cleFacturation } from './api'
import { Recapitulatif } from './recapitulatif'

/** Une ligne en cours d'édition ; nombres en texte, tels que saisis. */
type LigneEditee = {
  cle: string
  numero?: number
  recue: boolean
  sorte: LignePiece['sorte']
  texte: string
  source?: string
  code?: string
  unite: string
  quantite: string
  prixUnitaire: string
  tvaLibelle?: string
  tvaCode?: LignePiece['tvaCode']
  tvaTaux?: number
  tvaMotif?: string
  lignes: LigneEditee[]
}

type EnTete = {
  libellePeriode: string
  jours: string
  finDeMois: boolean
  bonDeCommande: string
  noteEntete: string
}

let compteur = 0
const nouvelleCle = () => `n${++compteur}`

/** Une ligne enregistrée, prête à l'édition ; le motif d'exonération se lit au récapitulatif de la pièce. */
function editee(
  l: LignePiece,
  recapitulatif: LigneTvaPiece[] = []
): LigneEditee {
  return {
    cle: `l${l.numero}`,
    numero: l.numero,
    recue: l.recue,
    sorte: l.sorte,
    texte: l.texte,
    source: l.source,
    code: l.code,
    unite: l.unite ?? '',
    quantite: l.quantite != null ? String(l.quantite).replace('.', ',') : '',
    prixUnitaire:
      l.prixUnitaire != null ? String(l.prixUnitaire).replace('.', ',') : '',
    tvaLibelle: l.tvaLibelle,
    tvaCode: l.tvaCode,
    tvaTaux: l.tvaTaux,
    tvaMotif: recapitulatif.find(
      (t) => t.code === l.tvaCode && t.taux === l.tvaTaux
    )?.motif,
    lignes: l.lignes.map((e) => editee(e, recapitulatif)),
  }
}

/** « 12,5 » → 12.5 ; vide ou illisible → undefined (le serveur nomme ce qui manque). */
function nombre(texte: string): number | undefined {
  const valeur = Number(texte.replace(/\s/g, '').replace(',', '.'))
  return texte.trim() === '' || Number.isNaN(valeur) ? undefined : valeur
}

function saisie(l: LigneEditee): LigneSaisie {
  return {
    numero: l.numero,
    sorte: l.sorte,
    texte: l.texte,
    source: l.numero == null ? l.source : undefined,
    code: l.numero == null ? l.code : undefined,
    unite: l.unite || undefined,
    quantite: nombre(l.quantite),
    prixUnitaire: nombre(l.prixUnitaire),
    lignes: l.lignes.map(saisie),
  }
}

/** Le brouillon tel qu'enregistré, à renvoyer entier : chaque carte n'y change que sa partie. */
function corpsActuel(piece: Piece): ModificationBrouillon {
  return {
    libellePeriode: piece.libellePeriode || undefined,
    condition: {
      jours: piece.condition.jours,
      finDeMois: piece.condition.finDeMois,
    },
    bonDeCommande: piece.bonDeCommande || undefined,
    noteEntete: piece.noteEntete || undefined,
    notePied: piece.notePied || undefined,
    lignes: piece.lignes.map((l) => saisie(editee(l))),
    version: piece.version,
  }
}

/** Enregistre le brouillon entier (un seul `MODIFIEE` au journal), avec la partie modifiée par la carte. */
function useEnregistrement(piece: Piece, onTerminer: () => void) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (partie: Partial<ModificationBrouillon>) =>
      modifierBrouillon({
        path: { id: piece.id },
        body: { ...corpsActuel(piece), ...partie },
        throwOnError: true,
      }).then((r) => r.data),
    meta: { erreurAffichee: true },
    onSuccess: (enregistree) => {
      toast.success('Brouillon enregistré.')
      queryClient.setQueryData(
        [...cleFacturation, 'piece', piece.id],
        enregistree
      )
      queryClient.invalidateQueries({ queryKey: cleFacturation })
      onTerminer()
    },
  })
}

/** L'erreur du serveur, au-dessus de la carte, et les boutons Annuler et Enregistrer, en dessous. */
function CarteEdition({
  titre,
  erreur,
  enCours,
  onAnnuler,
  onEnregistrer,
  children,
}: {
  titre: string
  erreur: unknown
  enCours: boolean
  onAnnuler: () => void
  onEnregistrer: () => void
  children: ReactNode
}) {
  const probleme = erreur ? lireProbleme(erreur) : undefined
  return (
    <Card className='border-primary'>
      <CardHeader>
        <CardTitle className='text-base'>{titre}</CardTitle>
      </CardHeader>
      <CardContent className='space-y-3'>
        {probleme && (
          <Alert variant='destructive'>
            <TriangleAlert />
            <AlertTitle>Le brouillon n'a pas été enregistré</AlertTitle>
            <AlertDescription>
              {probleme.detail}
              {probleme.regle && ` (${probleme.regle})`}
            </AlertDescription>
          </Alert>
        )}
        {children}
        <div className='flex justify-end gap-2 pt-2'>
          <Button variant='outline' onClick={onAnnuler} disabled={enCours}>
            Annuler
          </Button>
          <Button onClick={onEnregistrer} disabled={enCours}>
            <Save className='size-4' /> Enregistrer
          </Button>
        </div>
      </CardContent>
    </Card>
  )
}

/**
 * Carte « Facture » d'un brouillon en édition (spec 08 §6.1, RG-FAC-17) : période, condition de paiement, bon de
 * commande, note d'en-tête. La date de l'opération reste en lecture : pour la corriger, on abandonne le brouillon
 * et l'on refacture.
 */
export function EditionFacture({
  piece,
  onTerminer,
}: {
  piece: Piece
  onTerminer: () => void
}) {
  const [enTete, setEnTete] = useState<EnTete>({
    libellePeriode: piece.libellePeriode ?? '',
    jours: String(piece.condition.jours),
    finDeMois: piece.condition.finDeMois,
    bonDeCommande: piece.bonDeCommande ?? '',
    noteEntete: piece.noteEntete ?? '',
  })
  const enregistrement = useEnregistrement(piece, onTerminer)
  return (
    <CarteEdition
      titre='Facture'
      erreur={enregistrement.error}
      enCours={enregistrement.isPending}
      onAnnuler={onTerminer}
      onEnregistrer={() =>
        enregistrement.mutate({
          libellePeriode: enTete.libellePeriode || undefined,
          condition: {
            jours: nombre(enTete.jours) ?? 0,
            finDeMois: enTete.finDeMois,
          },
          bonDeCommande: enTete.bonDeCommande || undefined,
          noteEntete: enTete.noteEntete || undefined,
        })
      }
    >
      <p className='text-muted-foreground flex items-center gap-1 text-sm'>
        <Lock className='size-3' /> Titre : {piece.titre}, fourni par l'origine.
      </p>
      <p className='text-muted-foreground flex items-center gap-1 text-sm'>
        <Lock className='size-3' /> Date de l'opération :{' '}
        {formatDate(piece.dateOperation)}. Pour la corriger, abandonnez le
        brouillon et refacturez.
      </p>
      <div className='space-y-1'>
        <Label htmlFor='periode'>Période</Label>
        <Input
          id='periode'
          value={enTete.libellePeriode}
          onChange={(e) =>
            setEnTete({ ...enTete, libellePeriode: e.target.value })
          }
        />
      </div>
      <div className='flex flex-wrap items-end gap-4'>
        <div className='space-y-1'>
          <Label htmlFor='jours'>Paiement à (jours)</Label>
          <Input
            id='jours'
            className='w-24'
            inputMode='numeric'
            value={enTete.jours}
            onChange={(e) => setEnTete({ ...enTete, jours: e.target.value })}
          />
        </div>
        <div className='flex h-9 items-center gap-2'>
          <Switch
            id='fin-de-mois'
            checked={enTete.finDeMois}
            onCheckedChange={(v) => setEnTete({ ...enTete, finDeMois: v })}
          />
          <Label htmlFor='fin-de-mois'>Fin de mois</Label>
        </div>
        <div className='space-y-1'>
          <Label htmlFor='bon'>Bon de commande</Label>
          <Input
            id='bon'
            className='w-40'
            value={enTete.bonDeCommande}
            onChange={(e) =>
              setEnTete({ ...enTete, bonDeCommande: e.target.value })
            }
          />
        </div>
      </div>
      <div className='space-y-1'>
        <Label htmlFor='note-entete'>Note d'en-tête</Label>
        <Textarea
          id='note-entete'
          rows={2}
          value={enTete.noteEntete}
          onChange={(e) => setEnTete({ ...enTete, noteEntete: e.target.value })}
        />
      </div>
    </CarteEdition>
  )
}

/** Carte « Note de pied » d'un brouillon en édition (spec 08 §6.1, RG-FAC-17) : imprimée sous les totaux. */
export function EditionNotePied({
  piece,
  onTerminer,
}: {
  piece: Piece
  onTerminer: () => void
}) {
  const [notePied, setNotePied] = useState(piece.notePied ?? '')
  const enregistrement = useEnregistrement(piece, onTerminer)
  return (
    <CarteEdition
      titre='Note de pied'
      erreur={enregistrement.error}
      enCours={enregistrement.isPending}
      onAnnuler={onTerminer}
      onEnregistrer={() =>
        enregistrement.mutate({ notePied: notePied || undefined })
      }
    >
      <Textarea
        aria-label='Note de pied'
        rows={4}
        value={notePied}
        onChange={(e) => setNotePied(e.target.value)}
      />
    </CarteEdition>
  )
}

/** Où ajouter une ligne : au premier niveau (`groupe` vide) ou dans un groupe ; en tête ou à la fin. */
type Place = { groupe: string | null; debut?: boolean }
const EN_TETE: Place = { groupe: null, debut: true }
const EN_FIN: Place = { groupe: null }

/**
 * Carte « Contenu » d'un brouillon en édition (spec 08 §6.1, RG-FAC-17 révisée) : les lignes.
 * Une ligne reçue de la source garde son article, sa quantité, son prix et son groupe : seules sa désignation et
 * sa place changent. Les lignes ajoutées sont libres.
 */
export function EditionContenu({
  piece,
  onTerminer,
}: {
  piece: Piece
  onTerminer: () => void
}) {
  const [lignes, setLignes] = useState<LigneEditee[]>(() =>
    piece.lignes.map((l) => editee(l, piece.recapitulatifTva))
  )
  // Ajout d'un article : sa place, en attendant le choix dans la liste des articles.
  const [ajoutArticle, setAjoutArticle] = useState<Place | false>(false)
  const enregistrement = useEnregistrement(piece, onTerminer)

  /** Applique une transformation à la liste qui contient la ligne : la racine, ou les lignes d'un groupe. */
  const changerDans = (
    groupe: string | null,
    f: (liste: LigneEditee[]) => LigneEditee[]
  ) =>
    setLignes((racine) =>
      groupe == null
        ? f(racine)
        : racine.map((l) =>
            l.cle === groupe ? { ...l, lignes: f(l.lignes) } : l
          )
    )

  const ajouter = ({ groupe, debut }: Place, ligne: LigneEditee) =>
    changerDans(groupe, (liste) =>
      debut ? [ligne, ...liste] : [...liste, ligne]
    )

  const ajouterArticle = (a: ArticlePropose) => {
    if (ajoutArticle === false) return
    ajouter(ajoutArticle, {
      cle: nouvelleCle(),
      recue: false,
      sorte: 'ARTICLE',
      texte: a.designation,
      source: a.source,
      code: a.code,
      unite: a.unite ?? '',
      quantite: '1',
      prixUnitaire: String(a.prixUnitaire).replace('.', ','),
      tvaLibelle: a.tvaLibelle,
      tvaCode: a.tvaCode,
      tvaTaux: a.tvaTaux,
      tvaMotif: a.tvaMotif,
      lignes: [],
    })
    setAjoutArticle(false)
  }

  const totaux = recalculer(lignes)
  return (
    <div className='space-y-6'>
      <CarteEdition
        titre='Contenu'
        erreur={enregistrement.error}
        enCours={enregistrement.isPending}
        onAnnuler={onTerminer}
        onEnregistrer={() =>
          enregistrement.mutate({
            lignes: lignes.map(saisie),
          })
        }
      >
        <div className='divide-foreground/20 -mx-6 divide-y-2'>
          {lignes.length > 0 && (
            <BoutonsAjout
              onArticle={() => setAjoutArticle(EN_TETE)}
              onTexte={() =>
                ajouter(EN_TETE, ligneVide('TEXTE', 'Nouvelle ligne de texte'))
              }
              onGroupe={() =>
                ajouter(EN_TETE, ligneVide('GROUPE', 'Nouveau groupe'))
              }
            />
          )}
          <ListeTriable
            lignes={lignes}
            onReordonner={(de, vers) =>
              changerDans(null, (liste) => arrayMove(liste, de, vers))
            }
          >
            {lignes.map((l, i) => (
              <Triable
                key={l.cle}
                id={l.cle}
                className={cn(
                  'px-6 py-3',
                  l.sorte === 'GROUPE' && 'space-y-3',
                  // Effet « striped » : une bande sur deux légèrement grisée.
                  i % 2 === 1 && 'bg-muted/60'
                )}
              >
                {(poignee) => (
                  <>
                    <LigneEdition
                      ligne={l}
                      poignee={poignee}
                      onChanger={(n) =>
                        changerDans(null, (liste) =>
                          liste.map((x) => (x.cle === l.cle ? n : x))
                        )
                      }
                      onRetirer={() =>
                        changerDans(null, (liste) =>
                          liste.filter((x) => x.cle !== l.cle)
                        )
                      }
                    />
                    {l.sorte === 'GROUPE' && (
                      <div className='ms-6 divide-y'>
                        <ListeTriable
                          lignes={l.lignes}
                          onReordonner={(de, vers) =>
                            changerDans(l.cle, (liste) =>
                              arrayMove(liste, de, vers)
                            )
                          }
                        >
                          {l.lignes.map((e) => (
                            <Triable
                              key={e.cle}
                              id={e.cle}
                              className='px-2 py-2'
                            >
                              {(poigneeEnfant) => (
                                <LigneEdition
                                  ligne={e}
                                  poignee={poigneeEnfant}
                                  onChanger={(n) =>
                                    changerDans(l.cle, (liste) =>
                                      liste.map((x) =>
                                        x.cle === e.cle ? n : x
                                      )
                                    )
                                  }
                                  onRetirer={() =>
                                    changerDans(l.cle, (liste) =>
                                      liste.filter((x) => x.cle !== e.cle)
                                    )
                                  }
                                />
                              )}
                            </Triable>
                          ))}
                        </ListeTriable>
                        <BoutonsAjout
                          dansGroupe
                          onArticle={() => setAjoutArticle({ groupe: l.cle })}
                          onTexte={() =>
                            ajouter(
                              { groupe: l.cle },
                              ligneVide('TEXTE', 'Nouvelle ligne de texte')
                            )
                          }
                        />
                      </div>
                    )}
                  </>
                )}
              </Triable>
            ))}
          </ListeTriable>
          <BoutonsAjout
            onArticle={() => setAjoutArticle(EN_FIN)}
            onTexte={() =>
              ajouter(EN_FIN, ligneVide('TEXTE', 'Nouvelle ligne de texte'))
            }
            onGroupe={() =>
              ajouter(EN_FIN, ligneVide('GROUPE', 'Nouveau groupe'))
            }
          />
        </div>
        <ChoixArticle
          piece={piece.id}
          ouvert={ajoutArticle !== false}
          onFermer={() => setAjoutArticle(false)}
          onChoisir={ajouterArticle}
        />
      </CarteEdition>
      <Recapitulatif {...totaux} />
    </div>
  )
}

/** Un décimal exact : entier et nombre de décimales (pas de flottant dans les montants). */
type Decimal = { valeur: bigint; echelle: number }

function decimal(texte: string): Decimal | undefined {
  const m = /^(-?)(\d*)(?:\.(\d*))?$/.exec(
    texte.replace(/\s/g, '').replace(',', '.')
  )
  if (!m || (m[2] === '' && !m[3])) return undefined
  const decimales = m[3] ?? ''
  return {
    valeur: BigInt(m[1] + (m[2] || '0') + decimales),
    echelle: decimales.length,
  }
}

/** Au centime, demi supérieur (loin de zéro, comme `HALF_UP` au serveur) ; rend des centimes. */
function centimes({ valeur, echelle }: Decimal): bigint {
  if (echelle <= 2) return valeur * 10n ** BigInt(2 - echelle)
  const diviseur = 10n ** BigInt(echelle - 2)
  const quotient = valeur / diviseur
  const reste = valeur % diviseur
  const absolu = reste < 0n ? -reste : reste
  if (absolu * 2n < diviseur) return quotient
  return quotient + (valeur < 0n ? -1n : 1n)
}

const euros = (c: bigint) => Number(c) / 100

/**
 * RG-FAC-05, recalculé à chaque saisie comme le fait le serveur : HT d'une ligne = quantité × prix au centime ;
 * récapitulatif par catégorie et taux, dans l'ordre de première apparition, TVA = base × taux au centime ;
 * TTC = HT + TVA. Une quantité ou un prix illisible compte pour zéro.
 */
function recalculer(lignes: LigneEditee[]) {
  const articles = lignes
    .flatMap((l) => (l.sorte === 'GROUPE' ? l.lignes : [l]))
    .filter((l) => l.sorte === 'ARTICLE')
  const categories = new Map<string, { ligne: LigneEditee; base: bigint }>()
  let ht = 0n
  for (const a of articles) {
    const quantite = decimal(a.quantite)
    const prix = decimal(a.prixUnitaire)
    const montant =
      quantite && prix
        ? centimes({
            valeur: quantite.valeur * prix.valeur,
            echelle: quantite.echelle + prix.echelle,
          })
        : 0n
    ht += montant
    if (!a.tvaCode) continue
    const cle = `${a.tvaCode}/${a.tvaTaux ?? ''}`
    const categorie = categories.get(cle)
    if (categorie) categorie.base += montant
    else categories.set(cle, { ligne: a, base: montant })
  }
  let totalTva = 0n
  const tva: LigneTvaPiece[] = [...categories.values()].map(
    ({ ligne, base }) => {
      const taux = decimal(String(ligne.tvaTaux ?? 0)) ?? {
        valeur: 0n,
        echelle: 0,
      }
      // base en centimes × taux en pourcentage : 2 + échelle du taux + 2 décimales.
      const montant = centimes({
        valeur: base * taux.valeur,
        echelle: 4 + taux.echelle,
      })
      totalTva += montant
      return {
        code: ligne.tvaCode!,
        taux: ligne.tvaTaux,
        libelle: ligne.tvaLibelle ?? '',
        motif: ligne.tvaMotif,
        base: euros(base),
        tva: euros(montant),
      }
    }
  )
  return {
    tva,
    totalHt: euros(ht),
    totalTva: euros(totalTva),
    totalTtc: euros(ht + totalTva),
  }
}

function ligneVide(sorte: 'TEXTE' | 'GROUPE', texte: string): LigneEditee {
  return {
    cle: nouvelleCle(),
    recue: false,
    sorte,
    texte,
    unite: '',
    quantite: '',
    prixUnitaire: '',
    lignes: [],
  }
}

/** Le bouton « Modifier » des cartes : teinté de la couleur principale, sans concurrencer « Émettre ». */
export const TEINTE =
  'border-primary/40 bg-primary/10 text-primary hover:bg-primary/20 hover:text-primary'

/**
 * L'ajout d'une ligne : un bouton « Ajouter » et son menu (article, texte, et groupe au premier niveau). Couleur
 * principale au premier niveau, `secondary` (vert-bleu du thème) dans un groupe.
 */
function BoutonsAjout({
  dansGroupe,
  onArticle,
  onTexte,
  onGroupe,
}: {
  dansGroupe?: boolean
  onArticle: () => void
  onTexte: () => void
  onGroupe?: () => void
}) {
  return (
    <div className={dansGroupe ? 'px-2 py-2' : 'px-6 py-3'}>
      {/* Non modal : le menu se referme sans bloquer la page, avant l'ouverture de la liste des articles. */}
      <DropdownMenu modal={false}>
        <DropdownMenuTrigger asChild>
          <Button
            variant='outline'
            size='sm'
            className={
              dansGroupe
                ? 'border-info-border bg-info text-info-foreground hover:bg-info-border/60 hover:text-info-foreground'
                : TEINTE
            }
          >
            <Plus className='size-4' /> Ajouter{dansGroupe && ' au groupe'}
            <ChevronDown className='size-4' />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align='start'>
          <DropdownMenuItem onSelect={onArticle}>
            <Package className='size-4' /> Nouvel article
          </DropdownMenuItem>
          <DropdownMenuItem onSelect={onTexte}>
            <Text className='size-4' /> Ligne de texte libre
          </DropdownMenuItem>
          {onGroupe && (
            <DropdownMenuItem onSelect={onGroupe}>
              <Folder className='size-4' /> Nouveau groupe avec sous-total
            </DropdownMenuItem>
          )}
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  )
}

const LIBELLES_SORTE: Record<LignePiece['sorte'], string> = {
  ARTICLE: 'Désignation',
  TEXTE: 'Texte',
  GROUPE: 'Titre du groupe',
}

/**
 * Une liste de lignes réordonnée par glisser-déposer, au pointeur ou au clavier (espace, flèches). Une ligne ne
 * quitte pas sa liste : la racine, ou son groupe (une ligne reçue reste dans son groupe reçu).
 */
function ListeTriable({
  lignes,
  onReordonner,
  children,
}: {
  lignes: LigneEditee[]
  onReordonner: (de: number, vers: number) => void
  children: ReactNode
}) {
  const capteurs = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 4 } }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  )
  const surFin = ({ active, over }: DragEndEvent) => {
    if (!over || active.id === over.id) return
    const de = lignes.findIndex((l) => l.cle === active.id)
    const vers = lignes.findIndex((l) => l.cle === over.id)
    if (de >= 0 && vers >= 0) onReordonner(de, vers)
  }
  return (
    <DndContext
      sensors={capteurs}
      collisionDetection={closestCenter}
      onDragEnd={surFin}
    >
      <SortableContext
        items={lignes.map((l) => l.cle)}
        strategy={verticalListSortingStrategy}
      >
        {children}
      </SortableContext>
    </DndContext>
  )
}

/** Un élément déplaçable (une ligne, ou un groupe avec ses lignes) ; la poignée est placée par l'élément. */
function Triable({
  id,
  className,
  children,
}: {
  id: string
  className?: string
  children: (poignee: ReactNode) => ReactNode
}) {
  const {
    attributes,
    listeners,
    setNodeRef,
    setActivatorNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id })
  const poignee = (
    <button
      type='button'
      ref={setActivatorNodeRef}
      className='text-muted-foreground hover:text-foreground flex h-9 cursor-grab touch-none items-center rounded-sm active:cursor-grabbing'
      aria-label='Déplacer'
      {...attributes}
      {...listeners}
    >
      <GripVertical className='size-4' />
    </button>
  )
  return (
    <div
      ref={setNodeRef}
      style={{
        transform: CSS.Translate.toString(transform),
        transition,
      }}
      className={cn(
        className,
        isDragging && 'bg-background relative z-10 opacity-80 shadow-md'
      )}
    >
      {children(poignee)}
    </div>
  )
}

/** Une valeur reçue de la source, en lecture : pas de champ à saisir. */
function ValeurFigee({
  libelle,
  valeur,
  className,
}: {
  libelle: string
  valeur: string
  className?: string
}) {
  return (
    <div className='space-y-1'>
      <Label className='text-muted-foreground justify-end text-xs'>
        {libelle}
      </Label>
      <p className={cn('flex h-9 items-center text-sm', className)}>
        {valeur || '—'}
      </p>
    </div>
  )
}

/** Une ligne : désignation toujours modifiable ; quantité, unité, prix et retrait pour les seules lignes ajoutées. */
function LigneEdition({
  ligne,
  poignee,
  onChanger,
  onRetirer,
}: {
  ligne: LigneEditee
  poignee: ReactNode
  onChanger: (ligne: LigneEditee) => void
  onRetirer: () => void
}) {
  const libre = !ligne.recue
  return (
    <div className='flex flex-wrap items-end gap-2'>
      {poignee}
      <div className='min-w-64 flex-1 space-y-1'>
        <Label className='text-muted-foreground text-xs'>
          {LIBELLES_SORTE[ligne.sorte]}
          {ligne.code && (
            <span className='ms-2 font-mono'>
              {ligne.code}
              {ligne.tvaLibelle && ` · TVA ${ligne.tvaLibelle}`}
            </span>
          )}
        </Label>
        <Input
          aria-label={LIBELLES_SORTE[ligne.sorte]}
          className={ligne.sorte === 'GROUPE' ? 'font-medium' : undefined}
          value={ligne.texte}
          onChange={(e) => onChanger({ ...ligne, texte: e.target.value })}
        />
      </div>
      {ligne.sorte === 'ARTICLE' && !libre && (
        <>
          <ValeurFigee
            libelle='Quantité'
            className='w-28 justify-end tabular-nums'
            valeur={`${ligne.quantite}${ligne.unite ? ` ${ligne.unite}` : ''}`}
          />
          <ValeurFigee
            libelle='Prix unitaire HT'
            className='w-28 justify-end tabular-nums'
            valeur={ligne.prixUnitaire && `${ligne.prixUnitaire} €`}
          />
        </>
      )}
      {ligne.sorte === 'ARTICLE' && libre && (
        <>
          <div className='space-y-1'>
            <Label className='text-muted-foreground justify-end text-xs'>
              Quantité
            </Label>
            <Input
              aria-label='Quantité'
              className='w-28 text-right'
              value={ligne.quantite}
              onChange={(e) =>
                onChanger({ ...ligne, quantite: e.target.value })
              }
            />
          </div>
          <div className='space-y-1'>
            <Label className='text-muted-foreground justify-end text-xs'>
              Unité
            </Label>
            <Input
              aria-label='Unité'
              className='w-20 text-right'
              value={ligne.unite}
              onChange={(e) => onChanger({ ...ligne, unite: e.target.value })}
            />
          </div>
          <div className='space-y-1'>
            <Label className='text-muted-foreground justify-end text-xs'>
              Prix unitaire HT
            </Label>
            <Input
              aria-label='Prix unitaire HT'
              className='w-28 text-right'
              value={ligne.prixUnitaire}
              onChange={(e) =>
                onChanger({ ...ligne, prixUnitaire: e.target.value })
              }
            />
          </div>
        </>
      )}
      {libre ? (
        <Button
          variant='ghost'
          size='icon'
          aria-label='Retirer la ligne'
          onClick={onRetirer}
        >
          <Trash2 className='size-4' />
        </Button>
      ) : (
        <Tooltip>
          <TooltipTrigger asChild>
            <span
              tabIndex={0}
              className='text-muted-foreground flex size-9 items-center justify-center'
            >
              <Lock className='size-4' aria-label='Reçue de la source' />
            </span>
          </TooltipTrigger>
          <TooltipContent>
            Reçue de la source : seules sa désignation et sa place changent.
          </TooltipContent>
        </Tooltip>
      )}
    </div>
  )
}

/** Colonnes de la liste des articles, communes à la ligne de titre et aux articles. */
const COLONNES_ARTICLE =
  'grid grid-cols-[14rem_1fr_9rem_6rem] items-center gap-4'

/** RG-FAC-03 : les articles actifs de tous les catalogues, à la date de l'opération du brouillon. */
function ChoixArticle({
  piece,
  ouvert,
  onFermer,
  onChoisir,
}: {
  piece: number
  ouvert: boolean
  onFermer: () => void
  onChoisir: (article: ArticlePropose) => void
}) {
  const articles = useQuery({
    queryKey: [...cleFacturation, 'piece', piece, 'articles'],
    queryFn: () =>
      listerArticlesProposes({ path: { id: piece }, throwOnError: true }).then(
        (r) => r.data
      ),
    enabled: ouvert,
  })
  const parSource: Record<string, ArticlePropose[]> = {}
  for (const a of articles.data ?? []) {
    ;(parSource[a.source] ??= []).push(a)
  }
  return (
    <Dialog open={ouvert} onOpenChange={(o) => !o && onFermer()}>
      <DialogContent className='gap-0 overflow-hidden p-0 sm:max-w-5xl'>
        <DialogHeader className='bg-primary/5 border-primary/20 flex-row items-center gap-4 border-b px-6 py-4 text-start'>
          <span className='bg-primary text-primary-foreground flex size-10 shrink-0 items-center justify-center rounded-full'>
            <Package className='size-5' />
          </span>
          <div className='space-y-1'>
            <DialogTitle>Ajouter un article</DialogTitle>
            <DialogDescription>
              Les articles actifs de tous les catalogues, à la date de
              l'opération du brouillon.
            </DialogDescription>
          </div>
        </DialogHeader>
        <Command className='[&_[cmdk-group-heading]]:text-info-foreground [&_[cmdk-item][data-selected=true]]:bg-primary/10 **:data-[slot=command-input-wrapper]:h-12 [&_[cmdk-group-heading]]:px-3 [&_[cmdk-group-heading]]:pt-3 [&_[cmdk-group-heading]]:text-xs [&_[cmdk-group-heading]]:font-semibold [&_[cmdk-group-heading]]:tracking-wide [&_[cmdk-group-heading]]:uppercase [&_[cmdk-group]]:px-3 [&_[cmdk-item]]:px-3 [&_[cmdk-item]]:py-2.5'>
          <CommandInput placeholder='Code ou désignation…' />
          <div
            className={cn(
              COLONNES_ARTICLE,
              'bg-muted text-primary border-b px-6 py-2 text-xs font-semibold'
            )}
          >
            <span>Code article</span>
            <span>Désignation</span>
            <span className='text-right'>Prix</span>
            <span className='text-right'>TVA</span>
          </div>
          <CommandList className='max-h-[70vh] min-h-[50vh]'>
            <CommandEmpty>
              {articles.isPending ? 'Chargement…' : 'Aucun article.'}
            </CommandEmpty>
            {Object.entries(parSource).map(([source, liste]) => (
              <CommandGroup key={source} heading={source}>
                {liste.map((a) => (
                  <CommandItem
                    key={a.code}
                    value={`${a.code} ${a.designation}`}
                    onSelect={() => onChoisir(a)}
                    className={cn(
                      COLONNES_ARTICLE,
                      'border-border/60 rounded-none border-b last:border-b-0'
                    )}
                  >
                    <span className='truncate font-mono text-xs'>{a.code}</span>
                    <span>{a.designation}</span>
                    <span className='text-right font-medium tabular-nums'>
                      {String(a.prixUnitaire).replace('.', ',')} €
                      <span className='text-muted-foreground block text-xs'>
                        par {a.unite ?? 'unité'}
                      </span>
                    </span>
                    <span className='text-right'>
                      <span
                        className={cn(
                          'inline-block rounded-full px-3 py-1 text-sm font-medium tabular-nums',
                          // Un taux (normal, réduit ou zéro) en gris ; exonérée ou hors champ en couleur.
                          a.tvaCode === 'S' || a.tvaCode === 'Z'
                            ? 'bg-muted text-foreground'
                            : 'bg-info text-info-foreground'
                        )}
                      >
                        {a.tvaLibelle}
                      </span>
                    </span>
                  </CommandItem>
                ))}
              </CommandGroup>
            ))}
          </CommandList>
        </Command>
      </DialogContent>
    </Dialog>
  )
}
