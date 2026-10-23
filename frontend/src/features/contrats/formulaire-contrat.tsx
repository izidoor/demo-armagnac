import { type ReactNode, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'
import { AlertTriangle, Plus, Search, Trash2 } from 'lucide-react'
import { toast } from 'sonner'
import {
  type Contrat,
  type LigneStockContrat,
  type ResultatRechercheOperateur,
  type SaisieContenuContrat,
} from '@/api/generated'
import { lireProbleme } from '@/lib/probleme'
import { cn } from '@/lib/utils'
import { useRetour } from '@/hooks/use-retour'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Dialog,
  DialogDescription,
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
import { Textarea } from '@/components/ui/textarea'
import { ContenuPleinePage } from '@/components/metier/dialogue-formulaire'
import { PageMetier } from '@/components/metier/page-metier'
import { formatEuros } from '@/features/declarations/format'
import { RechercheOperateur } from '@/features/operateurs/portail/recherche-operateur'
import { apiContrats, cleContrats, useStockPourContrat } from './api'
import {
  formatDegre,
  formatHl,
  formatHlAp,
  formatPrixLitre,
  lireNombre,
  montantHt,
  prixLitre,
  volumeAp,
} from './format'
import { APPELLATIONS, SOLDES } from './libelles'

type Partie = 'ACHETEUR' | 'VENDEUR'

/** Une ligne en cours de saisie : les nombres restent du texte tant qu'on les tape. */
type LigneSaisie = {
  assemblage: boolean
  appellation: string
  millesime: string
  denomination: string
  compte: string
  volume: string
  degre: string
  prix: string
}

const COMPTES = [
  '00',
  ...Array.from({ length: 20 }, (_, i) => String(i)),
  '20+',
]

const LIGNE_VIDE: LigneSaisie = {
  assemblage: false,
  appellation: 'BAS_ARMAGNAC',
  millesime: '',
  denomination: '',
  compte: '',
  volume: '',
  degre: '',
  prix: '',
}

const texte = (n: number | undefined | null) =>
  n == null ? '' : String(n).replace('.', ',')

/** RG-DEC-11 : la forme de comparaison d'une dénomination (casse, accents, espaces). */
const normalise = (t: string) =>
  t
    .normalize('NFD')
    .replace(/\p{M}/gu, '')
    .toUpperCase()
    .trim()
    .replace(/\s+/g, ' ')

/** RG-CA-05 : le grain d'une ligne, `MB 2015` ou `NB HORS D'AGE 20+` ; `null` tant qu'il est incomplet. */
function grain(l: {
  assemblage: boolean
  millesime: string
  denomination: string
  compte: string
}) {
  if (!l.assemblage) return l.millesime ? `MB ${l.millesime}` : null
  return l.denomination.trim() && l.compte
    ? `NB ${normalise(l.denomination)} ${l.compte === '20+' || Number(l.compte) >= 20 ? '20+' : l.compte}`
    : null
}

const grainStock = (s: LigneStockContrat) =>
  s.cle === 'MB'
    ? `MB ${s.millesime}`
    : `NB ${normalise(s.denomination ?? '')} ${s.compte}`

/**
 * Nouveau contrat ou nouvelle version (spec 06 §6.2, UC-CA-1, 2), en pleine page : la partie et l'autre opérateur
 * (recherche restreinte, sans l'opérateur actif, RG-CA-02 ; en fenêtre, en pleine page sur un téléphone), puis les lignes et leurs calculs en direct (RG-CA-04), puis les conditions et le
 * paiement. Le vendeur choisit chaque produit dans son stock estimé sous bois : le reste s'affiche, un dépassement est
 * signalé en direct (RG-CA-12) et refusé par le serveur. L'acheteur saisit millésime, ou dénomination et compte d'âge.
 */
export function FormulaireContrat({
  operateur,
  contrat,
}: {
  operateur: number
  /** Le contrat à modifier : son contenu préremplit le formulaire. */
  contrat?: Contrat
}) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const retour = useRetour(
    contrat
      ? { to: '/portail/contrats/$id', params: { id: String(contrat.id) } }
      : { to: '/portail/contrats' }
  )
  const [partie, setPartie] = useState<Partie | null>(
    (contrat?.maPartie as Partie | undefined) ?? null
  )
  const [autre, setAutre] = useState<ResultatRechercheOperateur | null>(null)
  const [choixAutre, setChoixAutre] = useState(false)
  const v = contrat?.version
  const [lignes, setLignes] = useState<LigneSaisie[]>(
    v
      ? v.lignes.map((l) => ({
          assemblage: l.millesime == null,
          appellation: l.appellation,
          millesime: l.millesime == null ? '' : String(l.millesime),
          denomination: l.denomination ?? '',
          compte: l.millesime == null ? l.compte : '',
          volume: texte(l.volume),
          degre: texte(l.degre),
          prix: texte(l.prix),
        }))
      : [LIGNE_VIDE]
  )
  const [conditions, setConditions] = useState({
    caracteristiques: v?.caracteristiques ?? '',
    conditionsRetiraison: v?.conditionsRetiraison ?? '',
    aRetirerAvantLe: v?.aRetirerAvantLe ?? '',
    solde: v?.solde ?? 'COMPTANT',
    acompte: texte(v?.acompte),
  })
  const [erreur, setErreur] = useState<string | null>(null)
  const vendeur = partie === 'VENDEUR'
  const stock = useStockPourContrat(operateur, vendeur)

  const calculs = lignes.map((l) => {
    const volume = lireNombre(l.volume)
    const degre = lireNombre(l.degre)
    const prix = lireNombre(l.prix)
    return {
      ap: volume != null && degre != null ? volumeAp(volume, degre) : null,
      montant:
        volume != null && degre != null && prix != null
          ? montantHt(volume, degre, prix)
          : null,
      prixLitre: prix != null && degre != null ? prixLitre(prix, degre) : null,
    }
  })
  /** RG-CA-12 : par grain, le stock estimé et le volume AP demandé par toutes les lignes de ce grain. */
  const disponible = new Map<string, number>()
  for (const s of stock.data ?? [])
    disponible.set(
      grainStock(s),
      (disponible.get(grainStock(s)) ?? 0) + s.estime
    )
  const demande = new Map<string, number>()
  lignes.forEach((l, i) => {
    const g = grain(l)
    if (g) demande.set(g, (demande.get(g) ?? 0) + (calculs[i].ap ?? 0))
  })
  /** RG-CA-12 : pour le vendeur, le stock du grain de la ligne et ce que le contrat en demande. */
  const couverture = (l: LigneSaisie) => {
    const g = grain(l)
    if (!vendeur || !g) return null
    return { dispo: disponible.get(g) ?? 0, dem: demande.get(g) ?? 0 }
  }
  const bloque =
    vendeur &&
    lignes.some((l) => {
      const c = couverture(l)
      return c !== null && c.dem > c.dispo
    })
  const total = calculs.reduce((s, c) => s + (c.montant ?? 0), 0)
  const totalAp = calculs.reduce((s, c) => s + (c.ap ?? 0), 0)

  const changer = (i: number, changement: Partial<LigneSaisie>) =>
    setLignes((avant) =>
      avant.map((l, j) => (j === i ? { ...l, ...changement } : l))
    )

  const contenu = (): SaisieContenuContrat => ({
    lignes: lignes.map((l) => ({
      appellation:
        l.appellation as SaisieContenuContrat['lignes'][number]['appellation'],
      millesime: l.assemblage || !l.millesime ? undefined : Number(l.millesime),
      denomination: l.assemblage ? l.denomination : undefined,
      compte: l.assemblage ? l.compte : undefined,
      volume: lireNombre(l.volume) ?? 0,
      degre: lireNombre(l.degre) ?? 0,
      prix: lireNombre(l.prix) ?? 0,
    })),
    caracteristiques: conditions.caracteristiques || undefined,
    conditionsRetiraison: conditions.conditionsRetiraison || undefined,
    aRetirerAvantLe: conditions.aRetirerAvantLe || undefined,
    solde: conditions.solde as SaisieContenuContrat['solde'],
    acompte: lireNombre(conditions.acompte) ?? undefined,
  })

  const api = apiContrats(operateur)
  const envoi = useMutation({
    mutationFn: () =>
      contrat
        ? api.modifier(contrat.id, contrat.version.numero, contenu())
        : api.ouvrir({
            partie: partie!,
            autrePartie: autre!.id,
            contenu: contenu(),
          }),
    onSuccess: (c) => {
      queryClient.invalidateQueries({ queryKey: cleContrats })
      toast.success(
        contrat
          ? `Version ${c.version.numero} envoyée à l'autre partie.`
          : `Projet ${c.numero} envoyé à ${partie === 'VENDEUR' ? "l'acheteur" : 'au vendeur'}.`
      )
      navigate({
        to: '/portail/contrats/$id',
        params: { id: String(c.id) },
        replace: true,
      })
    },
    onError: (e) => {
      const probleme = lireProbleme(e)
      setErreur(probleme.detail ?? probleme.titre)
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
    meta: { erreurAffichee: true },
  })

  const pret = partie !== null && (contrat !== undefined || autre !== null)

  return (
    <PageMetier
      titre={
        contrat ? `Modifier le contrat ${contrat.numero}` : 'Nouveau contrat'
      }
      description={
        contrat
          ? `Version ${contrat.version.numero + 1} : approuvée par vous, elle sera envoyée à l'autre partie.`
          : "Un projet de contrat d'achat d'Armagnac en fût, envoyé à l'autre partie pour approbation."
      }
      retour={{ libelle: 'Annuler', onRetour: retour }}
    >
      <form
        className='mx-auto max-w-3xl space-y-4'
        onSubmit={(e) => {
          e.preventDefault()
          setErreur(null)
          envoi.mutate()
        }}
      >
        {erreur && (
          <Alert variant='destructive'>
            <AlertTriangle />
            <AlertTitle>Le projet n'a pas été enregistré</AlertTitle>
            <AlertDescription>{erreur}</AlertDescription>
          </Alert>
        )}
        {!contrat && (
          <Card>
            <CardHeader>
              <CardTitle>Parties</CardTitle>
            </CardHeader>
            <CardContent className='space-y-4'>
              <div className='grid grid-cols-2 gap-2'>
                {(['VENDEUR', 'ACHETEUR'] as const).map((p) => (
                  <button
                    key={p}
                    type='button'
                    aria-pressed={partie === p}
                    onClick={() => setPartie(p)}
                    className={cn(
                      'rounded-md border px-3 py-3 text-sm font-medium transition-colors',
                      partie === p
                        ? 'bg-primary text-primary-foreground border-primary'
                        : 'bg-card hover:bg-muted'
                    )}
                  >
                    Je suis {p === 'VENDEUR' ? 'le vendeur' : "l'acheteur"}
                  </button>
                ))}
              </div>
              {partie && (
                <div className='space-y-2'>
                  <Label>{partie === 'VENDEUR' ? 'Acheteur' : 'Vendeur'}</Label>
                  {autre ? (
                    <div className='flex items-center justify-between gap-2 rounded-md border px-3 py-2 text-sm'>
                      <span>
                        <span className='font-mono'>{autre.code}</span> ·{' '}
                        {autre.raisonSociale}
                        {autre.ville && (
                          <span className='text-muted-foreground'>
                            {' '}
                            · {autre.ville}
                          </span>
                        )}
                      </span>
                      <Button
                        type='button'
                        variant='ghost'
                        size='sm'
                        onClick={() => setAutre(null)}
                      >
                        Changer
                      </Button>
                    </div>
                  ) : (
                    <Button
                      type='button'
                      variant='outline'
                      className='text-muted-foreground w-full justify-start font-normal'
                      onClick={() => setChoixAutre(true)}
                    >
                      <Search />
                      Rechercher{' '}
                      {partie === 'VENDEUR' ? "l'acheteur" : 'le vendeur'}
                    </Button>
                  )}
                  <Dialog open={choixAutre} onOpenChange={setChoixAutre}>
                    <ContenuPleinePage className='sm:max-w-lg'>
                      <div className='grid content-start gap-4 max-md:p-4'>
                        <DialogHeader className='text-start'>
                          <DialogTitle>
                            {partie === 'VENDEUR' ? "L'acheteur" : 'Le vendeur'}
                          </DialogTitle>
                          <DialogDescription>
                            Un autre opérateur actif que le vôtre.
                          </DialogDescription>
                        </DialogHeader>
                        <RechercheOperateur
                          exclure={operateur}
                          autoFocus
                          onChoisir={(o) => {
                            setAutre(o)
                            setChoixAutre(false)
                          }}
                        />
                      </div>
                    </ContenuPleinePage>
                  </Dialog>
                </div>
              )}
            </CardContent>
          </Card>
        )}
        {partie && (
          <>
            <Card>
              <CardHeader>
                <CardTitle>Lignes</CardTitle>
              </CardHeader>
              <CardContent className='space-y-3'>
                {lignes.map((l, i) => (
                  <BlocLigne
                    key={i}
                    rang={i}
                    ligne={l}
                    vendeur={vendeur}
                    stock={stock.data ?? []}
                    calcul={calculs[i]}
                    couverture={couverture(l)}
                    onChanger={(c) => changer(i, c)}
                    onRetirer={
                      lignes.length > 1
                        ? () =>
                            setLignes((avant) =>
                              avant.filter((_, j) => j !== i)
                            )
                        : undefined
                    }
                  />
                ))}
                <Button
                  type='button'
                  variant='outline'
                  onClick={() => setLignes((avant) => [...avant, LIGNE_VIDE])}
                >
                  <Plus /> Ajouter une ligne
                </Button>
                <div className='flex flex-wrap justify-between gap-2 border-t pt-3 font-medium'>
                  <span>Total : {formatHlAp(totalAp)}</span>
                  <span className='tabular-nums'>{formatEuros(total)} HT</span>
                </div>
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle>Conditions</CardTitle>
              </CardHeader>
              <CardContent className='grid gap-4 @min-[40rem]/content:grid-cols-2'>
                <Champ
                  label='Caractéristiques complémentaires'
                  id='caracteristiques'
                  large
                >
                  <Textarea
                    id='caracteristiques'
                    value={conditions.caracteristiques}
                    onChange={(e) =>
                      setConditions({
                        ...conditions,
                        caracteristiques: e.target.value,
                      })
                    }
                  />
                </Champ>
                <Champ
                  label='Conditions et calendrier de retiraison'
                  id='conditions'
                  large
                >
                  <Textarea
                    id='conditions'
                    value={conditions.conditionsRetiraison}
                    onChange={(e) =>
                      setConditions({
                        ...conditions,
                        conditionsRetiraison: e.target.value,
                      })
                    }
                  />
                </Champ>
                <Champ label='À retirer avant le' id='avant'>
                  <Input
                    id='avant'
                    type='date'
                    value={conditions.aRetirerAvantLe}
                    onChange={(e) =>
                      setConditions({
                        ...conditions,
                        aRetirerAvantLe: e.target.value,
                      })
                    }
                  />
                </Champ>
                <Champ label='Paiement du solde' id='solde'>
                  <Select
                    value={conditions.solde}
                    onValueChange={(s) =>
                      setConditions({
                        ...conditions,
                        solde: s as typeof conditions.solde,
                      })
                    }
                  >
                    <SelectTrigger id='solde' className='w-full'>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {Object.entries(SOLDES).map(([valeur, libelle]) => (
                        <SelectItem key={valeur} value={valeur}>
                          {libelle}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </Champ>
                <Champ label='Acompte (%)' id='acompte'>
                  <Input
                    id='acompte'
                    inputMode='decimal'
                    placeholder='Aucun'
                    value={conditions.acompte}
                    onChange={(e) =>
                      setConditions({ ...conditions, acompte: e.target.value })
                    }
                  />
                </Champ>
              </CardContent>
            </Card>
          </>
        )}
        <div className='flex flex-wrap justify-end gap-2'>
          <Button
            type='button'
            variant='outline'
            onClick={retour}
            className='max-md:hidden'
          >
            Annuler
          </Button>
          <Button
            type='submit'
            disabled={!pret || bloque || envoi.isPending}
            className='max-md:w-full'
          >
            {contrat
              ? `Envoyer la version ${contrat.version.numero + 1}`
              : 'Envoyer le projet'}
          </Button>
        </div>
      </form>
    </PageMetier>
  )
}

function Champ({
  label,
  id,
  large,
  children,
}: {
  label: string
  id: string
  large?: boolean
  children: ReactNode
}) {
  return (
    <div
      className={cn('space-y-1', large && '@min-[40rem]/content:col-span-2')}
    >
      <Label htmlFor={id}>{label}</Label>
      {children}
    </div>
  )
}

/** Un nombre et son unité en suffixe dans le champ. */
function Nombre({
  id,
  label,
  unite,
  value,
  onChange,
}: {
  id: string
  label: string
  unite: string
  value: string
  onChange: (v: string) => void
}) {
  const invalide = value !== '' && lireNombre(value) === null
  return (
    <div className='space-y-1'>
      <Label htmlFor={id}>{label}</Label>
      <div className='relative'>
        <Input
          id={id}
          inputMode='decimal'
          value={value}
          aria-invalid={invalide}
          onChange={(e) => onChange(e.target.value)}
          className='pe-16 text-right tabular-nums'
        />
        <span className='text-muted-foreground pointer-events-none absolute inset-y-0 end-3 flex items-center text-xs'>
          {unite}
        </span>
      </div>
    </div>
  )
}

/** Une ligne : produit, appellation, volume, degré, prix, et ses calculs en direct. */
function BlocLigne({
  rang,
  ligne: l,
  vendeur,
  stock,
  calcul,
  couverture,
  onChanger,
  onRetirer,
}: {
  rang: number
  ligne: LigneSaisie
  vendeur: boolean
  stock: LigneStockContrat[]
  calcul: {
    ap: number | null
    montant: number | null
    prixLitre: number | null
  }
  couverture: { dispo: number; dem: number } | null
  onChanger: (c: Partial<LigneSaisie>) => void
  onRetirer?: () => void
}) {
  const id = (nom: string) => `ligne-${rang}-${nom}`
  const choisi = stock.findIndex((s) => grainStock(s) === grain(l))
  const depassement = couverture !== null && couverture.dem > couverture.dispo
  /** Le degré saisi : le stock, en hl AP, se lit aussi en hl à ce degré. */
  const degre = lireNombre(l.degre) || null
  return (
    <div
      className={cn(
        'space-y-3 rounded-md border p-3',
        depassement && 'border-destructive'
      )}
    >
      <div className='flex items-center justify-between gap-2'>
        <span className='text-sm font-medium'>Ligne {rang + 1}</span>
        {onRetirer && (
          <Button type='button' variant='ghost' size='sm' onClick={onRetirer}>
            <Trash2 /> Retirer
          </Button>
        )}
      </div>
      <div className='grid grid-cols-1 gap-3 @min-[40rem]/content:grid-cols-2'>
        {vendeur ? (
          <div className='space-y-1 @min-[40rem]/content:col-span-2'>
            <Label htmlFor={id('produit')}>Produit de votre stock</Label>
            <Select
              value={choisi >= 0 ? String(choisi) : ''}
              onValueChange={(v) => {
                const s = stock[Number(v)]
                onChanger(
                  s.cle === 'MB'
                    ? {
                        assemblage: false,
                        millesime: String(s.millesime),
                        denomination: '',
                        compte: '',
                      }
                    : {
                        assemblage: true,
                        millesime: '',
                        denomination: s.denomination ?? '',
                        compte: s.compte,
                      }
                )
              }}
            >
              <SelectTrigger id={id('produit')} className='w-full'>
                <SelectValue placeholder='Choisir dans votre stock estimé sous bois' />
              </SelectTrigger>
              <SelectContent>
                {stock.map((s, i) => (
                  <SelectItem key={i} value={String(i)}>
                    {s.cle === 'MB'
                      ? `Millésime ${s.millesime} (compte d'âge ${s.compte})`
                      : `${s.denomination} (compte d'âge ${s.compte})`}{' '}
                    : {formatHlAp(s.estime)} en stock
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {stock.length === 0 && (
              <p className='text-muted-foreground text-sm'>
                Aucun stock estimé sous bois : sans DSM validée ni point de
                départ, vous ne pouvez pas vendre.
              </p>
            )}
          </div>
        ) : (
          <>
            <div className='grid grid-cols-2 gap-2 @min-[40rem]/content:col-span-2'>
              {[false, true].map((assemblage) => (
                <button
                  key={String(assemblage)}
                  type='button'
                  aria-pressed={l.assemblage === assemblage}
                  onClick={() => onChanger({ assemblage })}
                  className={cn(
                    'rounded-md border px-3 py-2 text-sm transition-colors',
                    l.assemblage === assemblage
                      ? 'bg-primary text-primary-foreground border-primary'
                      : 'bg-card hover:bg-muted'
                  )}
                >
                  {assemblage ? 'Assemblage' : 'Millésime'}
                </button>
              ))}
            </div>
            {l.assemblage ? (
              <>
                <div className='space-y-1'>
                  <Label htmlFor={id('denomination')}>Dénomination</Label>
                  <Input
                    id={id('denomination')}
                    placeholder="Hors d'âge, XO, VSOP…"
                    value={l.denomination}
                    onChange={(e) =>
                      onChanger({ denomination: e.target.value })
                    }
                  />
                </div>
                <div className='space-y-1'>
                  <Label htmlFor={id('compte')}>Compte d'âge</Label>
                  <Select
                    value={l.compte}
                    onValueChange={(compte) => onChanger({ compte })}
                  >
                    <SelectTrigger id={id('compte')} className='w-full'>
                      <SelectValue placeholder='Choisir' />
                    </SelectTrigger>
                    <SelectContent>
                      {COMPTES.map((c) => (
                        <SelectItem key={c} value={c}>
                          {c}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </>
            ) : (
              <div className='space-y-1'>
                <Label htmlFor={id('millesime')}>Millésime</Label>
                <Input
                  id={id('millesime')}
                  inputMode='numeric'
                  placeholder='2015'
                  value={l.millesime}
                  onChange={(e) =>
                    onChanger({
                      millesime: e.target.value.replace(/\D/g, '').slice(0, 4),
                    })
                  }
                />
              </div>
            )}
          </>
        )}
        <div className='space-y-1'>
          <Label htmlFor={id('appellation')}>Appellation</Label>
          <Select
            value={l.appellation}
            onValueChange={(appellation) => onChanger({ appellation })}
          >
            <SelectTrigger id={id('appellation')} className='w-full'>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {Object.entries(APPELLATIONS).map(([valeur, libelle]) => (
                <SelectItem key={valeur} value={valeur}>
                  {libelle}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>
      <div className='grid grid-cols-2 gap-3 @min-[40rem]/content:grid-cols-3'>
        <Nombre
          id={id('volume')}
          label='Volume'
          unite='hl'
          value={l.volume}
          onChange={(volume) => onChanger({ volume })}
        />
        <Nombre
          id={id('degre')}
          label='Degré'
          unite='% vol.'
          value={l.degre}
          onChange={(degre) => onChanger({ degre })}
        />
        <div className='col-span-2 @min-[40rem]/content:col-span-1'>
          <Nombre
            id={id('prix')}
            label='Prix HT'
            unite='€ / hl AP'
            value={l.prix}
            onChange={(prix) => onChanger({ prix })}
          />
        </div>
      </div>
      <div className='bg-muted/50 grid grid-cols-3 gap-2 rounded-md px-3 py-2 text-sm'>
        <Calcul titre='Volume AP'>
          {calcul.ap == null ? '—' : formatHlAp(calcul.ap)}
        </Calcul>
        <Calcul titre='Prix au litre'>
          {calcul.prixLitre == null ? '—' : formatPrixLitre(calcul.prixLitre)}
        </Calcul>
        <Calcul titre='Montant HT'>
          {calcul.montant == null ? '—' : formatEuros(calcul.montant)}
        </Calcul>
      </div>
      {couverture && depassement && (
        <p className='text-destructive flex items-start gap-2 text-sm font-medium'>
          <AlertTriangle className='mt-0.5 size-4 shrink-0' />
          <span>
            Votre stock estimé ne couvre pas cette ligne :{' '}
            {formatHlAp(couverture.dem)} au contrat pour{' '}
            {formatHlAp(couverture.dispo)} en stock
            {degre != null
              ? `, soit ${formatHl(couverture.dispo / degre)} à ${formatDegre(degre)}`
              : '.'}
          </span>
        </p>
      )}
      {couverture && !depassement && calcul.ap != null && degre != null && (
        <p className='text-muted-foreground text-sm'>
          {formatHlAp(couverture.dem)} au contrat sur{' '}
          {formatHlAp(couverture.dispo)} en stock, soit{' '}
          {formatHl(couverture.dispo / degre)} à {formatDegre(degre)} : il en
          restera {formatHlAp(couverture.dispo - couverture.dem)}, soit{' '}
          {formatHl((couverture.dispo - couverture.dem) / degre)}.
        </p>
      )}
    </div>
  )
}

function Calcul({ titre, children }: { titre: string; children: ReactNode }) {
  return (
    <div>
      <div className='text-muted-foreground text-xs'>{titre}</div>
      <div className='font-medium tabular-nums'>{children}</div>
    </div>
  )
}
