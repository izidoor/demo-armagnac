import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from '@tanstack/react-router'
import { ChevronRight, Receipt, Search, TriangleAlert } from 'lucide-react'
import { toast } from 'sonner'
import {
  type Dsm,
  type DsmListee,
  facturerSelectionDsm,
  listerToutesDsm,
} from '@/api/generated'
import { cn } from '@/lib/utils'
import { useFiltresAdresse, useTexteAdresse } from '@/hooks/use-filtres-adresse'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/ui/tooltip'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { BadgePlein } from '@/components/metier/badge-statut'
import { CarteListe, CartesListe } from '@/components/metier/cartes-liste'
import { PageMetier } from '@/components/metier/page-metier'
import { Pagination } from '@/components/metier/pagination'
import { TAILLE_PAGE, usePagination } from '@/components/metier/use-pagination'
import { useDroits, useMoi } from '@/features/session/session'
import { Echeance } from './echeance'
import { formatDateHeure, formatEuros } from './format'
import { GROUPES_ETATS, STATUT_ETAT, TYPES_DECLARANT } from './libelles'

const TOUS = 'tous'

type EtatDsm = Dsm['etat']
type TypeDeclarant = Dsm['type']

/** Une période attendue sans DSM (RG-DEC-40), filtrée comme un état. */
const ABSENTE = 'ABSENTE'

/** Les états d'une DSM en cours, du point de vue du BNIA, que l'onglet « En cours » filtre un à un. */
const EN_COURS: Partial<Record<EtatDsm | typeof ABSENTE, string>> = {
  ABSENTE: 'Absente',
  BROUILLON: 'Brouillon',
  ENVOYEE: 'À prendre en charge',
  EN_INSTRUCTION: 'En instruction',
  COMPLEMENTS_DEMANDES: 'Compléments demandés',
  COMPLEMENTS_RECUS: 'Compléments reçus',
}

type Vue = 'en-cours' | 'validees' | 'facturees'

/**
 * Les DSM de tous les opérateurs, pour le BNIA (spec 05 §6.1, UC-DEC-7), en trois onglets : en cours
 * (brouillon, à prendre en charge, en instruction, compléments demandés, compléments reçus ; filtre d'état,
 * « tous » par défaut), validées (sans facture, donc à facturer), facturées (projection des factures,
 * RG-DEC-34). Total HT, calculé ou facturé. Recherche d'un opérateur, filtre de type. Une ligne ouvre la DSM.
 * Lot 7 : échéance et retard de chaque ligne ; la période attendue d'un opérateur sans DSM est une ligne
 * « absente » de l'onglet « En cours », grisée, faute de DSM à ouvrir ; seul son opérateur mène à sa fiche (RG-DEC-39, 40).
 * Spec 08 : dans « Validées », une sélection se facture d'un coup (RG-DEC-43) ; une DSM dont la facture est
 * demandée passe dans « Facturées », badge « Brouillon » (décision du 2026-10-14).
 * L'onglet et les filtres sont gardés dans l'adresse (`useFiltresAdresse`) : le retour depuis une DSM les retrouve ; le
 * tableau de bord (CDC §4.1) y passe l'onglet et l'état, ou un regroupement d'états.
 *
 * Selon la largeur du contenu (`CartesListe`), comme la liste des opérateurs : l'opérateur prend la place qui reste et
 * se tronque ; sous 70rem, le tableau perd ses dates (modification, ou envoi et validation) et le type ; sous 55rem,
 * une carte par DSM (`SEUILS`).
 */
export function PageDsmBnia() {
  const navigate = useNavigate()
  const [filtres, changer] = useFiltresAdresse({
    vue: 'en-cours' as Vue,
    type: TOUS,
    etat: TOUS,
    retard: false as boolean,
    recherche: '',
  })
  const { vue, type, etat, retard: enRetard } = filtres
  const [saisie, setSaisie, recherche] = useTexteAdresse(
    filtres.recherche,
    (texte) => changer({ recherche: texte })
  )
  const [selection, setSelection] = useState<Set<number>>(new Set())
  const { actif } = useMoi()
  // RG-DEC-43 : facturer est réservé à qui gère la facturation au BNIA.
  const facturer = useDroits(actif?.operateur ?? 0)?.facturation === 'GESTION'
  // Une seule lecture, filtrée par type : les onglets et leurs nombres se calculent dessus, et suivent donc
  // ce filtre, comme la recherche d'un opérateur, faite dans le navigateur ; « En cours » suit aussi l'état et
  // le retard, ses filtres propres.
  const listing = useQuery({
    queryKey: ['declarations', 'toutes', type],
    queryFn: () =>
      listerToutesDsm({
        query: {
          type: type === TOUS ? undefined : (type as TypeDeclarant),
        },
        throwOnError: true,
      }).then((r) => r.data),
  })
  const cherche = sansAccents(recherche.trim())
  const toutes = (listing.data ?? []).filter(
    (d) =>
      !cherche ||
      sansAccents(`${d.codeOperateur} ${d.raisonSociale}`).includes(cherche)
  )
  const etatDe = (d: DsmListee) => (d.absente ? ABSENTE : d.etat)
  // L'onglet « En cours » et son nombre suivent aussi l'état et la case « En retard ».
  const enCours = toutes
    .filter((d) => d.etat !== 'VALIDEE')
    .filter((d) => {
      const e = etatDe(d)
      return (
        etat === TOUS ||
        e === etat ||
        (!!e && !!GROUPES_ETATS[etat]?.etats.includes(e))
      )
    })
    .filter((d) => !enRetard || d.enRetard)
  // RG-DEC-34, 43 : une DSM validée sans facture vivante est à facturer ; facturée, ou sa facture demandée,
  // elle passe dans le dernier onglet.
  const validees = toutes.filter(
    (d) => d.etat === 'VALIDEE' && !d.facturee && !d.facturationDemandee
  )
  const facturees = toutes.filter((d) => d.facturee || d.facturationDemandee)
  const selectionnees = validees.filter(
    (d) => d.id != null && selection.has(d.id)
  )
  const dsms =
    vue === 'validees' ? validees : vue === 'facturees' ? facturees : enCours
  // Pagination dans le navigateur, par 15 (au-delà seulement, la barre apparaît).
  const { lignes: page, pagination } = usePagination(dsms, TAILLE_PAGE)
  const enCoursVue = vue === 'en-cours'
  const queryClient = useQueryClient()
  const ouvrir = (operateurId: number, id: number) =>
    navigate({
      to: '/bo/operateurs/$id/dsm/$dsm',
      params: { id: String(operateurId), dsm: String(id) },
    })
  const facturation = useMutation({
    mutationFn: (ids: number[]) =>
      facturerSelectionDsm({ body: { dsms: ids }, throwOnError: true }).then(
        (r) => r.data
      ),
    onSuccess: (brouillons) => {
      setSelection(new Set())
      toast.success(
        brouillons.length === 1
          ? '1 brouillon de facture créé.'
          : `${brouillons.length} brouillons de facture créés.`,
        {
          action: {
            label: 'Voir les brouillons',
            onClick: () =>
              navigate({
                to: '/bo/facturation',
                search: { onglet: 'brouillons' },
              }),
          },
        }
      )
      queryClient.invalidateQueries({ queryKey: ['declarations'] })
      queryClient.invalidateQueries({ queryKey: ['facturation'] })
    },
  })
  const selectionnable = vue === 'validees' && facturer
  // RG-DEC-43 : la DSM d'un opérateur non facturable (RG-OPE-02) serait refusée, et la sélection avec elle ;
  // si la sélection en compte, un dialogue les nomme et seules les autres sont facturées.
  const [exclusion, setExclusion] = useState(false)
  const exclues = selectionnees.filter((d) => !d.operateurFacturable)
  const facturables = selectionnees.filter((d) => d.operateurFacturable)
  const facturerSelection = () =>
    facturation.mutate(
      facturables.map((d) => d.id!),
      {
        onSettled: () => setExclusion(false),
      }
    )
  const basculer = (id: number, coche: boolean) =>
    setSelection((s) => {
      const nouvelle = new Set(s)
      if (coche) nouvelle.add(id)
      else nouvelle.delete(id)
      return nouvelle
    })

  return (
    <PageMetier
      titre='DSM'
      description='Les déclarations de stocks et mouvements de tous les opérateurs.'
    >
      <Tabs
        value={vue}
        onValueChange={(v) => changer({ vue: v as Vue })}
        className='mb-4'
      >
        <TabsList className='max-sm:w-full'>
          <TabsTrigger value='en-cours'>
            En cours <Nombre valeur={enCours.length} pret={listing.isSuccess} />
          </TabsTrigger>
          <TabsTrigger value='validees'>
            Validées{' '}
            <Nombre valeur={validees.length} pret={listing.isSuccess} />
          </TabsTrigger>
          <TabsTrigger value='facturees'>
            {/* Sans nombre : les DSM facturées s'accumulent. */}
            Facturées
          </TabsTrigger>
        </TabsList>
      </Tabs>
      <div className='mb-4 flex flex-wrap items-center gap-2'>
        <div className='relative w-full sm:w-auto'>
          <Search className='text-muted-foreground absolute start-2.5 top-2.5 size-4' />
          <Input
            placeholder='Rechercher un opérateur'
            title='Code ou raison sociale'
            aria-label='Recherche par code ou raison sociale'
            value={saisie}
            onChange={(e) => setSaisie(e.target.value)}
            className='h-9 w-full ps-8 sm:w-72'
          />
        </div>
        <Select value={type} onValueChange={(v) => changer({ type: v })}>
          <SelectTrigger
            aria-label='Type de déclarant'
            className='h-9 w-40 max-sm:min-w-0 max-sm:flex-1'
          >
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={TOUS}>Tous les types</SelectItem>
            {Object.entries(TYPES_DECLARANT).map(([valeur, libelle]) => (
              <SelectItem key={valeur} value={valeur}>
                {libelle}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        {vue === 'en-cours' && (
          <Select value={etat} onValueChange={(v) => changer({ etat: v })}>
            <SelectTrigger
              aria-label='État'
              className='h-9 w-52 max-sm:min-w-0 max-sm:flex-1'
            >
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={TOUS}>Tous les états</SelectItem>
              {Object.entries(GROUPES_ETATS).map(([valeur, g]) => (
                <SelectItem key={valeur} value={valeur}>
                  {g.libelle}
                </SelectItem>
              ))}
              {Object.entries(EN_COURS).map(([valeur, libelle]) => (
                <SelectItem key={valeur} value={valeur}>
                  {libelle}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        )}
        {vue === 'en-cours' && (
          <div className='flex h-9 items-center gap-2 px-1 max-sm:w-full'>
            <Checkbox
              id='filtre-retard'
              checked={enRetard}
              onCheckedChange={(v) => changer({ retard: v === true })}
            />
            <Label htmlFor='filtre-retard' className='font-normal'>
              En retard
            </Label>
          </div>
        )}
      </div>
      {vue === 'validees' && (
        <div className='mb-4 flex flex-wrap items-center justify-between gap-2'>
          <p className='text-muted-foreground flex items-center gap-2 text-sm'>
            <Receipt className='size-4' />
            Les DSM validées, à facturer ; total HT calculé à la grille CVO.
          </p>
          {facturer && (
            <Button
              disabled={selectionnees.length === 0 || facturation.isPending}
              onClick={() =>
                exclues.length > 0 ? setExclusion(true) : facturerSelection()
              }
            >
              <Receipt className='size-4' /> Facturer la sélection
              {selectionnees.length > 0 && ` (${selectionnees.length})`}
            </Button>
          )}
        </div>
      )}
      {vue === 'facturees' && (
        <p className='text-muted-foreground mb-4 flex items-center gap-2 text-sm'>
          <Receipt className='size-4' />
          Les DSM dont la facture est émise, ou demandée (brouillon) ; total HT
          facturé, factures moins avoirs, ou calculé pour un brouillon.
        </p>
      )}
      <div className={cn('rounded-md border', SEUILS.tableau)}>
        <Table>
          <TableHeader>
            <TableRow>
              {selectionnable && (
                <TableHead className='w-0'>
                  <Checkbox
                    aria-label='Sélectionner toutes les DSM à facturer'
                    checked={
                      validees.length > 0 &&
                      selectionnees.length === validees.length
                    }
                    onCheckedChange={(v) =>
                      setSelection(
                        v === true
                          ? new Set(validees.map((d) => d.id!))
                          : new Set()
                      )
                    }
                  />
                </TableHead>
              )}
              <TableHead>Opérateur</TableHead>
              <TableHead className={SEUILS.type}>Type</TableHead>
              <TableHead>Période</TableHead>
              <TableHead>État</TableHead>
              <TableHead>Échéance</TableHead>
              {enCoursVue ? (
                <TableHead className={SEUILS.dates}>Modifiée le</TableHead>
              ) : (
                <>
                  <TableHead className={SEUILS.dates}>Envoyée le</TableHead>
                  <TableHead className={SEUILS.dates}>Validée le</TableHead>
                </>
              )}
              <TableHead className='text-right'>Total HT</TableHead>
              <TableHead className='w-0' />
            </TableRow>
          </TableHeader>
          <TableBody>
            {dsms.length === 0 && (
              <TableRow>
                <TableCell
                  colSpan={(selectionnable ? 9 : 8) + (enCoursVue ? 0 : 1)}
                  className='text-muted-foreground h-16 text-center'
                >
                  {listing.isPending ? 'Chargement…' : 'Aucune DSM.'}
                </TableCell>
              </TableRow>
            )}
            {(page ?? []).map((d) => (
              <TableRow
                key={d.id ?? `absente-${d.operateur}-${d.periode.debut}`}
                // Une période absente n'a pas de DSM à ouvrir : ligne grisée, sans lien, sauf l'opérateur, qui
                // mène à sa fiche et ne reprend sa couleur qu'au survol.
                className={
                  d.id == null
                    ? 'text-muted-foreground hover:bg-transparent [&>td:not([data-operateur])]:opacity-60'
                    : 'cursor-pointer'
                }
                onClick={
                  d.id == null ? undefined : () => ouvrir(d.operateur, d.id!)
                }
              >
                {selectionnable && (
                  <TableCell onClick={(e) => e.stopPropagation()}>
                    <Checkbox
                      aria-label={`Sélectionner la DSM de ${d.periode.libelle} de ${d.raisonSociale}`}
                      checked={d.id != null && selection.has(d.id)}
                      onCheckedChange={(v) => basculer(d.id!, v === true)}
                    />
                  </TableCell>
                )}
                {d.id == null ? (
                  <TableCell
                    data-operateur
                    className={OPERATEUR}
                    title={`${d.codeOperateur} · ${d.raisonSociale}`}
                  >
                    <div className='flex items-center'>
                      <Link
                        to='/bo/operateurs/$id'
                        params={{ id: String(d.operateur) }}
                        className='hover:text-foreground truncate opacity-60 transition-opacity hover:underline hover:opacity-100'
                      >
                        <span className='font-mono'>{d.codeOperateur}</span> ·{' '}
                        {d.raisonSociale}
                      </Link>
                      {!d.operateurFacturable && <NonFacturable />}
                    </div>
                  </TableCell>
                ) : (
                  <TableCell
                    className={OPERATEUR}
                    title={`${d.codeOperateur} · ${d.raisonSociale}`}
                  >
                    <div className='flex items-center'>
                      <span className='truncate'>
                        <span className='font-mono'>{d.codeOperateur}</span> ·{' '}
                        {d.raisonSociale}
                      </span>
                      {!d.operateurFacturable && <NonFacturable />}
                    </div>
                  </TableCell>
                )}
                <TableCell className={SEUILS.type}>
                  {TYPES_DECLARANT[d.type]}
                </TableCell>
                <TableCell className='font-medium'>
                  {d.periode.libelle}
                </TableCell>
                <TableCell>
                  <EtatDsm dsm={d} />
                </TableCell>
                <TableCell>
                  <Echeance echeance={d.echeance} enRetard={d.enRetard} />
                </TableCell>
                {enCoursVue ? (
                  <TableCell className={SEUILS.dates}>
                    {formatDateHeure(d.modifieeLe)}
                  </TableCell>
                ) : (
                  <>
                    <TableCell className={SEUILS.dates}>
                      {formatDateHeure(d.envoyeeLe)}
                    </TableCell>
                    <TableCell className={SEUILS.dates}>
                      {formatDateHeure(d.valideeLe)}
                    </TableCell>
                  </>
                )}
                <TableCell className='text-right tabular-nums'>
                  {formatEuros(d.totalHt)}
                </TableCell>
                <TableCell>
                  {d.id != null && (
                    <Button
                      variant='ghost'
                      size='sm'
                      aria-label={`Ouvrir la DSM de ${d.periode.libelle} de ${d.raisonSociale}`}
                      onClick={(e) => {
                        e.stopPropagation()
                        ouvrir(d.operateur, d.id!)
                      }}
                    >
                      <ChevronRight className='size-4' />
                    </Button>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
      <CartesListe
        requete={{
          isPending: listing.isPending,
          isError: listing.isError,
          data: dsms,
        }}
        vide='Aucune DSM.'
        masquee={SEUILS.cartes}
      >
        {(page ?? []).map((d) => (
          <CarteDsm
            key={d.id ?? `absente-${d.operateur}-${d.periode.debut}`}
            dsm={d}
            onOuvrir={
              d.id == null ? undefined : () => ouvrir(d.operateur, d.id!)
            }
            selection={
              selectionnable && d.id != null
                ? {
                    coche: selection.has(d.id),
                    basculer: (coche) => basculer(d.id!, coche),
                  }
                : undefined
            }
          />
        ))}
      </CartesListe>
      <Pagination {...pagination} />
      <ConfirmDialog
        open={exclusion}
        onOpenChange={setExclusion}
        title='Opérateurs non facturables'
        desc={
          <div className='space-y-2'>
            <p>
              {exclues.length === 1
                ? 'Cette DSM ne sera pas facturée'
                : `Ces ${exclues.length} DSM ne seront pas facturées`}
              , leur opérateur n'étant pas facturable (fiche incomplète,
              RG-OPE-02) :
            </p>
            <ul className='list-disc ps-5'>
              {exclues.map((d) => (
                <li key={d.id}>
                  {d.raisonSociale}, DSM {d.periode.libelle}
                </li>
              ))}
            </ul>
            <p>
              {facturables.length === 0
                ? 'Aucune autre DSM sélectionnée : rien à facturer.'
                : facturables.length === 1
                  ? "L'autre DSM sélectionnée sera facturée."
                  : `Les ${facturables.length} autres DSM sélectionnées seront facturées.`}
            </p>
          </div>
        }
        confirmText={
          facturables.length > 0
            ? `Facturer ${facturables.length} DSM`
            : 'Facturer'
        }
        cancelBtnText='Annuler'
        disabled={facturables.length === 0}
        isLoading={facturation.isPending}
        handleConfirm={facturerSelection}
      />
    </PageMetier>
  )
}

/**
 * Les seuils, en largeur du contenu, mesurés avec l'opérateur à 12rem (plus 34 px de marges) : le tableau entier fait
 * au plus 1047 px (« En cours », « Facturées ») ; sous 70rem, il perd ses dates et le type (857 px au plus, « En
 * cours », dont les états et les retards sont les plus larges) ; sous 55rem, une carte par DSM.
 */
const SEUILS = {
  type: '@max-[70rem]/content:hidden',
  dates: '@max-[70rem]/content:hidden',
  tableau: '@max-[55rem]/content:hidden',
  cartes: '@min-[55rem]/content:hidden',
}
/** La colonne de l'opérateur prend la place qui reste et se tronque, son nom complet en infobulle. */
const OPERATEUR = 'w-full max-w-0 min-w-48'

/** L'état d'une DSM, en pointillés pour une période absente, et « Brouillon » si sa facture est demandée. */
function EtatDsm({ dsm: d }: { dsm: DsmListee }) {
  return (
    <span className='inline-flex flex-wrap items-center gap-1'>
      {d.etat ? (
        <BadgePlein
          statut={STATUT_ETAT[d.etat]}
          className={d.absente ? 'border-dashed' : undefined}
        >
          {d.libelleEtat}
        </BadgePlein>
      ) : (
        <Badge
          variant='outline'
          className={d.absente ? 'border-dashed' : undefined}
        >
          {d.libelleEtat}
        </Badge>
      )}
      {d.facturationDemandee && <Badge variant='outline'>Brouillon</Badge>}
    </span>
  )
}

/**
 * Une DSM en carte : l'opérateur, la période et l'état, puis l'échéance et le total HT. Une période absente n'a pas de
 * DSM à ouvrir : sa carte, grisée, mène à la fiche de l'opérateur. Dans « Validées », une case la sélectionne pour la
 * facturation.
 */
function CarteDsm({
  dsm: d,
  onOuvrir,
  selection,
}: {
  dsm: DsmListee
  onOuvrir?: () => void
  selection?: { coche: boolean; basculer: (coche: boolean) => void }
}) {
  return (
    <CarteListe
      onOuvrir={onOuvrir}
      lien={
        onOuvrir
          ? undefined
          : { to: '/bo/operateurs/$id', params: { id: String(d.operateur) } }
      }
      titre={
        <span className={cn(d.id == null && 'opacity-60')}>
          {d.raisonSociale}
        </span>
      }
    >
      <div
        className={cn(
          'flex flex-wrap items-center gap-x-2 gap-y-1 text-sm',
          d.id == null && 'opacity-60'
        )}
      >
        <span className='text-muted-foreground font-mono'>
          {d.codeOperateur}
        </span>
        <span className='font-medium'>{d.periode.libelle}</span>
        <EtatDsm dsm={d} />
        {!d.operateurFacturable && <NonFacturable />}
      </div>
      <div className='text-muted-foreground flex flex-wrap items-center gap-x-2 gap-y-1 text-sm'>
        <span className='inline-flex items-center gap-1'>
          Échéance <Echeance echeance={d.echeance} enRetard={d.enRetard} />
        </span>
        {d.totalHt != null && (
          <span className='text-foreground tabular-nums'>
            · {formatEuros(d.totalHt)} HT
          </span>
        )}
      </div>
      {selection && (
        <label className='relative z-10 flex w-fit items-center gap-2 pt-1 text-sm'>
          <Checkbox
            checked={selection.coche}
            onCheckedChange={(v) => selection.basculer(v === true)}
          />
          Sélectionner pour la facturation
        </label>
      )}
    </CarteListe>
  )
}

/** RG-OPE-02 : l'opérateur n'est pas facturable ; sa fiche dit ce qui manque. */
function NonFacturable() {
  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <span
          tabIndex={0}
          aria-label='Opérateur non facturable'
          className='text-warning-foreground ms-1 inline-flex shrink-0 align-middle'
          onClick={(e) => e.stopPropagation()}
        >
          <TriangleAlert className='size-4' />
        </span>
      </TooltipTrigger>
      <TooltipContent>
        Non facturable : la fiche de l'opérateur est incomplète (RG-OPE-02).
      </TooltipContent>
    </Tooltip>
  )
}

/** Le nombre de DSM d'un onglet, une fois le listing chargé. */
function Nombre({ valeur, pret }: { valeur: number; pret: boolean }) {
  if (!pret) return null
  return (
    <Badge variant='secondary' className='ms-1 px-1.5 tabular-nums'>
      {valeur}
    </Badge>
  )
}

/** Pour chercher sans tenir compte de la casse ni des accents. */
function sansAccents(texte: string) {
  return texte
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase()
}
