import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from '@tanstack/react-router'
import {
  ChevronRight,
  CloudUpload,
  Loader2,
  Search,
  Send,
  X,
} from 'lucide-react'
import { toast } from 'sonner'
import {
  emettreBrouillons,
  listerPieces,
  transmettreAZeendoc,
  type CompteRenduEmission,
  type PieceListee,
} from '@/api/generated'
import { cn } from '@/lib/utils'
import { useFiltresAdresse, useTexteAdresse } from '@/hooks/use-filtres-adresse'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { CarteListe, CartesListe } from '@/components/metier/cartes-liste'
import { PageMetier } from '@/components/metier/page-metier'
import { Pagination } from '@/components/metier/pagination'
import { TAILLE_PAGE, usePagination } from '@/components/metier/use-pagination'
import {
  formatDate,
  formatDateHeure,
  formatEuros,
} from '@/features/declarations/format'
import { useDroits, useMoi } from '@/features/session/session'
import { cleFacturation } from './api'
import { BadgeZeendoc } from './badge-zeendoc'
import { echueImpayee, SENS } from './libelles'

/** La transmission à Zeendoc est simulée : le loader dure au moins ce délai, pour la démonstration. */
const DELAI_ZEENDOC = 1500

type OngletPieces = 'brouillons' | 'emises' | 'payees'

/**
 * Factures du back-office (spec 08 §6.1, UC-FAC-4) : l'historique des pièces émises, filtré par numéro, acheteur,
 * période et date d'émission (RG-FAC-27), et l'onglet « Brouillons », où qui gère la facturation sélectionne des
 * brouillons à émettre ensemble, avec un compte rendu par brouillon (RG-FAC-19) ; dans l'onglet « Émises », il
 * sélectionne des pièces à transmettre à Zeendoc, une seule fois chacune (RG-FAC-32, simulé) ; les pièces payées
 * passent dans l'onglet « Payées » (RG-FAC-33). Filtrable sur un client par l'URL ; la fiche opérateur a son propre
 * onglet « Factures ». Une ligne ouvre la pièce. Le client, l'onglet et les filtres sont gardés dans l'adresse
 * (`useFiltresAdresse`) : le retour depuis une pièce les retrouve.
 */
export function PagePieces() {
  const queryClient = useQueryClient()
  const { actif } = useMoi()
  const gere = useDroits(actif?.operateur ?? 0)?.facturation === 'GESTION'
  const [selection, setSelection] = useState<Set<number>>(new Set())
  const [confirmation, setConfirmation] = useState(false)
  const [emis, setEmis] = useState<{
    comptesRendus: CompteRenduEmission[]
    selection: PieceListee[]
  }>()
  const [filtres, changer] = useFiltresAdresse({
    onglet: 'brouillons' as OngletPieces,
    client: '',
    numero: '',
    acheteur: '',
    periode: '',
    du: '',
    au: '',
  })
  const { onglet, du, au } = filtres
  const client = filtres.client || undefined
  // Les textes saisis filtrent une fois la frappe arrêtée.
  const [numero, setNumero, numeroStable] = useTexteAdresse(
    filtres.numero,
    (texte) => changer({ numero: texte })
  )
  const [acheteur, setAcheteur, acheteurStable] = useTexteAdresse(
    filtres.acheteur,
    (texte) => changer({ acheteur: texte })
  )
  const [periode, setPeriode, periodeStable] = useTexteAdresse(
    filtres.periode,
    (texte) => changer({ periode: texte })
  )
  const textes = {
    numero: numeroStable,
    acheteur: acheteurStable,
    periode: periodeStable,
  }
  const brouillons = onglet === 'brouillons'
  // Les filtres de chaque onglet : l'acheteur et la période valent pour les deux, le numéro et les dates
  // d'émission pour les pièces émises seulement (« Émises » et « Payées »).
  const filtreBrouillons = {
    client,
    etat: 'BROUILLON' as const,
    acheteur: textes.acheteur || undefined,
    periode: textes.periode || undefined,
  }
  const filtreEmises = {
    client,
    etat: 'EMISE' as const,
    numero: textes.numero || undefined,
    acheteur: textes.acheteur || undefined,
    periode: textes.periode || undefined,
    du: du || undefined,
    au: au || undefined,
  }
  // Le nombre de chaque onglet compte les lignes qu'il montre avec ces filtres, comme celles de la page DSM.
  const listingBrouillons = useQuery({
    queryKey: [...cleFacturation, 'pieces', filtreBrouillons],
    queryFn: () =>
      listerPieces({ query: filtreBrouillons, throwOnError: true }).then(
        (r) => r.data
      ),
  })
  const listingEmises = useQuery({
    queryKey: [...cleFacturation, 'pieces', filtreEmises],
    queryFn: () =>
      listerPieces({ query: filtreEmises, throwOnError: true }).then(
        (r) => r.data
      ),
  })
  const listing = brouillons ? listingBrouillons : listingEmises
  // « Émises » et « Payées » se partagent le listing des pièces émises, selon leur état Zeendoc.
  const pieces = (listing.data ?? []).filter(
    (p) => brouillons || (onglet === 'payees') === (p.zeendoc === 'PAYEE')
  )
  const enAttente = listingBrouillons.data?.length
  const nonPayees = listingEmises.data?.filter(
    (p) => p.zeendoc !== 'PAYEE'
  ).length
  // Seules les pièces affichées comptent : un filtre en masque, une émission en retire ; une pièce déjà transmise
  // ne se retransmet pas.
  const selectionnes = pieces.filter(
    (p) => selection.has(p.id) && transmissible(p, brouillons)
  )
  const emission = useMutation({
    mutationFn: () =>
      emettreBrouillons({
        body: { ids: selectionnes.map((p) => p.id) },
        throwOnError: true,
      }).then((r) => r.data),
    onSuccess: (resultat) => {
      setConfirmation(false)
      setSelection(new Set())
      setEmis({ comptesRendus: resultat, selection: selectionnes })
      queryClient.invalidateQueries({ queryKey: cleFacturation })
      queryClient.invalidateQueries({ queryKey: ['declarations'] })
    },
    onError: () => setConfirmation(false),
  })
  const transmission = useMutation({
    mutationFn: () =>
      Promise.all([
        transmettreAZeendoc({
          body: { ids: selectionnes.map((p) => p.id) },
          throwOnError: true,
        }).then((r) => r.data),
        new Promise((fini) => setTimeout(fini, DELAI_ZEENDOC)),
      ]).then(([comptesRendus]) => comptesRendus),
    onSuccess: (comptesRendus) => {
      setSelection(new Set())
      queryClient.invalidateQueries({ queryKey: cleFacturation })
      const transmises = comptesRendus.filter((c) => !c.motif).length
      if (transmises > 0)
        toast.success(
          transmises > 1
            ? `${transmises} factures transmises à Zeendoc.`
            : 'Facture transmise à Zeendoc.'
        )
      comptesRendus.forEach((c) => c.motif && toast.error(c.motif))
    },
  })
  const changerOnglet = (o: string) => {
    setSelection(new Set())
    changer({ onglet: o as OngletPieces })
  }
  const nomClient = pieces.find((p) => p.clientIdentifiant === client)

  return (
    <PageMetier
      titre='Factures'
      description='Les factures et avoirs du BNIA, quelle que soit leur origine ; les brouillons attendent leur émission.'
    >
      <Tabs value={onglet} onValueChange={changerOnglet} className='mb-4'>
        <TabsList className='max-sm:w-full'>
          <TabsTrigger value='brouillons'>
            Brouillons
            <Compteur nombre={enAttente} />
          </TabsTrigger>
          <TabsTrigger value='emises'>
            Émises
            <Compteur nombre={nonPayees} />
          </TabsTrigger>
          <TabsTrigger value='payees'>Payées</TabsTrigger>
        </TabsList>
      </Tabs>
      <div className='mb-4 flex flex-wrap items-center gap-2'>
        {client && (
          <div className='flex h-9 items-center gap-1 rounded-md border ps-3 pe-1 text-sm max-sm:w-full'>
            <span className='truncate'>
              {nomClient ? (
                <>
                  <span className='font-mono'>{nomClient.clientCode}</span> ·{' '}
                  {nomClient.acheteur}
                </>
              ) : (
                `Opérateur n° ${client}`
              )}
            </span>
            <Button
              variant='ghost'
              size='icon'
              className='ms-auto size-7 shrink-0'
              aria-label='Retirer le filtre sur le client'
              onClick={() => changer({ client: '' })}
            >
              <X className='size-4' />
            </Button>
          </div>
        )}
        <div className='relative w-full sm:w-auto'>
          <Search className='text-muted-foreground absolute start-2.5 top-2.5 size-4' />
          <Input
            placeholder='Rechercher un acheteur'
            title='Nom ou n° BNIA'
            aria-label='Acheteur, par nom ou n° BNIA'
            value={acheteur}
            onChange={(e) => setAcheteur(e.target.value)}
            className='h-9 w-full ps-8 sm:w-64'
          />
        </div>
        {!brouillons && (
          <Input
            aria-label='Numéro'
            placeholder='Numéro'
            title='Début du numéro : 2026…'
            className='h-9 w-36 max-sm:min-w-0 max-sm:flex-1'
            value={numero}
            onChange={(e) => setNumero(e.target.value)}
          />
        )}
        <Input
          aria-label='Période'
          placeholder='Période'
          title='Juillet 2026, 2026…'
          className='h-9 w-40 max-sm:min-w-0 max-sm:flex-1'
          value={periode}
          onChange={(e) => setPeriode(e.target.value)}
        />
        {!brouillons && (
          // Les dates d'émission, sur une ligne à part sur un téléphone.
          <div className='flex items-center gap-2 text-sm max-sm:w-full'>
            <span className='text-muted-foreground shrink-0'>Émise du</span>
            <Input
              type='date'
              aria-label='Émise du'
              className='h-9 w-40 max-sm:min-w-0 max-sm:flex-1'
              value={du}
              onChange={(e) => changer({ du: e.target.value })}
            />
            <span className='text-muted-foreground shrink-0'>au</span>
            <Input
              type='date'
              aria-label='Émise au'
              className='h-9 w-40 max-sm:min-w-0 max-sm:flex-1'
              value={au}
              onChange={(e) => changer({ au: e.target.value })}
            />
          </div>
        )}
      </div>
      {brouillons && gere && (
        <div className='mb-2 flex justify-end'>
          <Button
            className='max-sm:w-full'
            disabled={selectionnes.length === 0 || emission.isPending}
            onClick={() => setConfirmation(true)}
          >
            <Send className='size-4' /> Émettre la sélection
            {selectionnes.length > 0 && ` (${selectionnes.length})`}
          </Button>
        </div>
      )}
      {onglet === 'emises' && gere && (
        <div className='mb-2 flex justify-end'>
          <Button
            className='max-sm:w-full'
            disabled={selectionnes.length === 0 || transmission.isPending}
            onClick={() => transmission.mutate()}
          >
            {transmission.isPending ? (
              <>
                <Loader2 className='size-4 animate-spin' /> Transmission…
              </>
            ) : (
              <>
                <CloudUpload className='size-4' /> Transmettre à Zeendoc
                {selectionnes.length > 0 && ` (${selectionnes.length})`}
              </>
            )}
          </Button>
        </div>
      )}
      <TablePieces
        pieces={pieces}
        brouillons={brouillons}
        chargement={listing.isPending}
        vide={onglet === 'payees' ? 'Aucune pièce payée.' : undefined}
        selection={
          gere && onglet !== 'payees'
            ? {
                ids: selection,
                changer: (ids) => setSelection(new Set(ids)),
                possible: (p) => transmissible(p, brouillons),
              }
            : undefined
        }
      />
      <ConfirmDialog
        open={confirmation}
        onOpenChange={setConfirmation}
        title={`Émettre ${selectionnes.length} brouillon${selectionnes.length > 1 ? 's' : ''} ?`}
        desc="Chacun reçoit le numéro suivant, dans l'ordre du tableau, et ne se modifiera plus. Un brouillon qui ne peut pas être émis est écarté sans arrêter les autres ; le compte rendu dira pourquoi."
        confirmText='Émettre'
        cancelBtnText='Annuler'
        isLoading={emission.isPending}
        handleConfirm={() => emission.mutate()}
      />
      <CompteRendu
        comptesRendus={emis?.comptesRendus}
        pieces={emis?.selection ?? []}
        onFermer={() => setEmis(undefined)}
      />
    </PageMetier>
  )
}

/** Le nombre de pièces d'un onglet, en pastille ; mis en avant s'il y en a. */
function Compteur({ nombre }: { nombre?: number }) {
  if (nombre == null) return null
  return (
    <Badge
      variant={nombre > 0 ? 'default' : 'secondary'}
      className='ms-1 h-5 min-w-5 rounded-full px-1.5 tabular-nums'
    >
      {nombre}
    </Badge>
  )
}

/** Un brouillon s'émet ; une pièce émise se transmet à Zeendoc tant qu'elle ne l'est pas (RG-FAC-32). */
function transmissible(p: PieceListee, brouillons: boolean) {
  return brouillons || !p.zeendoc
}

/**
 * Les seuils du tableau, selon la largeur du contenu (`CartesListe`).
 *
 * Dans l'onglet « Factures » de la fiche opérateur (sans acheteur), dans le cadre d'une carte (84 px de marges) : sous
 * 72rem (1050 px de large naturelle), le tableau perd le HT et la TVA ; sous 62rem (886 px sans eux), la période, que
 * la référence de l'origine rappelle ; l'origine prend ensuite la place qui reste et se tronque ; sous 40rem (544 px
 * avec l'origine à 8rem), une carte par pièce. Les brouillons, plus étroits, suivent les mêmes seuils : les deux
 * tableaux de l'onglet changent ensemble.
 */
const SEUILS_ONGLET = {
  montants: '@max-[72rem]/content:hidden',
  periode: '@max-[62rem]/content:hidden',
  tableau: '@max-[40rem]/content:hidden',
  cartes: '@min-[40rem]/content:hidden',
  acheteur: '',
  origine: 'w-full max-w-0 min-w-32',
}
/**
 * Dans la page Factures, avec l'acheteur et la sélection (34 px de marges) : l'acheteur (12rem au moins) et l'origine
 * (8rem) se partagent la place qui reste et se tronquent ; le tableau des pièces émises fait alors 1113 px. Sous 72rem,
 * il perd d'abord la période, que la référence de l'origine rappelle (931 px) ; sous 61rem, le HT et la TVA (764 px) ;
 * sous 51rem, une carte par pièce. Les brouillons et les pièces payées, plus étroits, suivent les mêmes seuils.
 */
const SEUILS_PAGE = {
  montants: '@max-[61rem]/content:hidden',
  periode: '@max-[72rem]/content:hidden',
  tableau: '@max-[51rem]/content:hidden',
  cartes: '@min-[51rem]/content:hidden',
  acheteur: 'w-1/2 max-w-0 min-w-48',
  origine: 'w-1/2 max-w-0 min-w-32',
}

/**
 * Les pièces, émises ou brouillons : une ligne ouvre la pièce. Sans acheteur dans l'onglet « Factures » de la
 * fiche opérateur, où il est toujours le même.
 */
export function TablePieces({
  pieces,
  brouillons,
  chargement,
  avecAcheteur = true,
  vide,
  selection,
}: {
  pieces: PieceListee[]
  brouillons: boolean
  chargement: boolean
  avecAcheteur?: boolean
  /** Le message d'une liste vide, si ce n'est « Aucune pièce émise. ». */
  vide?: string
  /**
   * Des cases à cocher, pour émettre une sélection de brouillons (RG-FAC-19) ou transmettre des pièces émises à
   * Zeendoc (RG-FAC-32) ; `possible` dit quelles lignes se cochent.
   */
  selection?: {
    ids: Set<number>
    changer: (ids: number[]) => void
    possible: (p: PieceListee) => boolean
  }
}) {
  const navigate = useNavigate()
  const cochables = selection ? pieces.filter(selection.possible) : []
  const coches = cochables.filter((p) => selection?.ids.has(p.id)).length
  const { lignes, pagination } = usePagination(pieces, TAILLE_PAGE)
  const seuils = avecAcheteur ? SEUILS_PAGE : SEUILS_ONGLET
  return (
    <>
      <div className={cn('rounded-md border', seuils.tableau)}>
        <Table>
          <TableHeader>
            <TableRow>
              {selection && (
                <TableHead className='w-0'>
                  <Checkbox
                    aria-label='Tout sélectionner'
                    disabled={cochables.length === 0}
                    checked={
                      coches > 0 && coches === cochables.length
                        ? true
                        : coches > 0
                          ? 'indeterminate'
                          : false
                    }
                    onCheckedChange={(v) =>
                      selection.changer(
                        v === true ? cochables.map((p) => p.id) : []
                      )
                    }
                  />
                </TableHead>
              )}
              {brouillons ? (
                <TableHead>Demandée le</TableHead>
              ) : (
                <>
                  <TableHead>Numéro</TableHead>
                  <TableHead>Date</TableHead>
                </>
              )}
              {avecAcheteur && <TableHead>Acheteur</TableHead>}
              <TableHead>Origine</TableHead>
              <TableHead className={seuils.periode}>Période</TableHead>
              <TableHead className={cn('text-right', seuils.montants)}>
                HT
              </TableHead>
              <TableHead className={cn('text-right', seuils.montants)}>
                TVA
              </TableHead>
              <TableHead className='text-right'>TTC</TableHead>
              {!brouillons && <TableHead>Zeendoc</TableHead>}
              <TableHead className='w-0' />
            </TableRow>
          </TableHeader>
          <TableBody>
            {pieces.length === 0 && (
              <TableRow>
                <TableCell
                  colSpan={
                    (brouillons ? 7 : 9) +
                    (avecAcheteur ? 1 : 0) +
                    (selection ? 1 : 0)
                  }
                  className='text-muted-foreground h-16 text-center'
                >
                  {chargement
                    ? 'Chargement…'
                    : brouillons
                      ? 'Aucun brouillon.'
                      : (vide ?? 'Aucune pièce émise.')}
                </TableCell>
              </TableRow>
            )}
            {(lignes ?? []).map((p) => (
              <TableRow
                key={p.id}
                className={cn(
                  'cursor-pointer',
                  // Échue et impayée (RG-FAC-33) : la ligne se teinte.
                  echueImpayee(p) && 'bg-danger/50 hover:bg-danger/80'
                )}
                title={echueImpayee(p) ? 'Échue et impayée' : undefined}
                onClick={() =>
                  navigate({
                    to: '/bo/facturation/$id',
                    params: { id: String(p.id) },
                  })
                }
              >
                {selection && (
                  <TableCell onClick={(e) => e.stopPropagation()}>
                    <Checkbox
                      aria-label={`Sélectionner ${p.numero ?? p.reference ?? p.id}`}
                      disabled={!selection.possible(p)}
                      checked={selection.possible(p) && selection.ids.has(p.id)}
                      onCheckedChange={(v) =>
                        selection.changer(
                          v === true
                            ? [...selection.ids, p.id]
                            : [...selection.ids].filter((id) => id !== p.id)
                        )
                      }
                    />
                  </TableCell>
                )}
                {brouillons ? (
                  <TableCell>{formatDateHeure(p.creeeLe)}</TableCell>
                ) : (
                  <>
                    <TableCell className='font-mono font-medium'>
                      {p.numero}
                      {p.sens === 'AVOIR' && (
                        <Badge variant='secondary' className='ms-2'>
                          {SENS[p.sens]}
                        </Badge>
                      )}
                    </TableCell>
                    <TableCell>{formatDate(p.dateEmission)}</TableCell>
                  </>
                )}
                {avecAcheteur && (
                  <TableCell
                    className={seuils.acheteur}
                    title={[p.clientCode, p.acheteur]
                      .filter(Boolean)
                      .join(' · ')}
                  >
                    <span className='block truncate'>
                      {p.clientCode && (
                        <span className='font-mono'>{p.clientCode} · </span>
                      )}
                      {p.acheteur}
                    </span>
                  </TableCell>
                )}
                <TableCell
                  className={seuils.origine}
                  title={[p.libelleNature, p.reference]
                    .filter(Boolean)
                    .join(' · ')}
                >
                  <span className='block truncate'>
                    {p.libelleNature ?? '—'}
                  </span>
                  <span className='text-muted-foreground block truncate text-xs'>
                    {p.reference}
                  </span>
                </TableCell>
                <TableCell className={seuils.periode}>
                  {p.libellePeriode}
                </TableCell>
                <TableCell
                  className={cn('text-right tabular-nums', seuils.montants)}
                >
                  {formatEuros(p.ht)}
                </TableCell>
                <TableCell
                  className={cn('text-right tabular-nums', seuils.montants)}
                >
                  {formatEuros(p.tva)}
                </TableCell>
                <TableCell className='text-right font-medium tabular-nums'>
                  {formatEuros(p.ttc)}
                </TableCell>
                {!brouillons && (
                  <TableCell>
                    <BadgeZeendoc piece={p} />
                  </TableCell>
                )}
                <TableCell>
                  <Button
                    variant='ghost'
                    size='sm'
                    asChild
                    onClick={(e) => e.stopPropagation()}
                  >
                    <Link
                      to='/bo/facturation/$id'
                      params={{ id: String(p.id) }}
                      aria-label={`Ouvrir la pièce ${p.numero ?? p.reference ?? p.id}`}
                    >
                      <ChevronRight className='size-4' />
                    </Link>
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
      <CartesListe
        requete={{ isPending: chargement, isError: false, data: pieces }}
        vide={brouillons ? 'Aucun brouillon.' : (vide ?? 'Aucune pièce émise.')}
        masquee={seuils.cartes}
      >
        {(lignes ?? []).map((p) => (
          <CartePiece
            key={p.id}
            piece={p}
            brouillon={brouillons}
            avecAcheteur={avecAcheteur}
            selection={
              selection?.possible(p)
                ? {
                    coche: selection.ids.has(p.id),
                    basculer: (coche) =>
                      selection.changer(
                        coche
                          ? [...selection.ids, p.id]
                          : [...selection.ids].filter((id) => id !== p.id)
                      ),
                  }
                : undefined
            }
          />
        ))}
      </CartesListe>
      <Pagination {...pagination} />
    </>
  )
}

/**
 * Une pièce en carte : son numéro et son état Zeendoc (le libellé de son origine pour un brouillon), l'acheteur (page
 * Factures), la référence de l'origine, qui distingue les pièces, et son libellé, puis la date et le TTC. Une case la
 * sélectionne pour l'émission (brouillon) ou la transmission à Zeendoc.
 */
function CartePiece({
  piece: p,
  brouillon,
  avecAcheteur,
  selection,
}: {
  piece: PieceListee
  brouillon: boolean
  avecAcheteur: boolean
  selection?: { coche: boolean; basculer: (coche: boolean) => void }
}) {
  return (
    <CarteListe
      lien={{ to: '/bo/facturation/$id', params: { id: String(p.id) } }}
      className={echueImpayee(p) ? 'bg-danger/50' : undefined}
      titre={
        brouillon ? (
          (p.libelleNature ?? 'Brouillon')
        ) : (
          <span className='flex flex-wrap items-center gap-2'>
            <span className='font-mono'>{p.numero}</span>
            {p.sens === 'AVOIR' && (
              <Badge variant='secondary'>{SENS[p.sens]}</Badge>
            )}
            <BadgeZeendoc piece={p} />
          </span>
        )
      }
    >
      {avecAcheteur && (
        <div className='truncate text-sm'>
          {p.clientCode && (
            <span className='text-muted-foreground font-mono'>
              {p.clientCode} ·{' '}
            </span>
          )}
          {p.acheteur}
        </div>
      )}
      <div
        className={cn(
          'truncate text-sm',
          avecAcheteur && 'text-muted-foreground'
        )}
      >
        {[p.reference, brouillon ? null : p.libelleNature]
          .filter(Boolean)
          .join(' · ')}
      </div>
      <div className='flex justify-between gap-2 text-sm'>
        <span className='text-muted-foreground'>
          {formatDate(brouillon ? p.creeeLe : p.dateEmission)}
        </span>
        <span className='font-medium whitespace-nowrap tabular-nums'>
          {formatEuros(p.ttc)} TTC
        </span>
      </div>
      {selection && (
        <label className='relative z-10 flex w-fit items-center gap-2 pt-1 text-sm'>
          <Checkbox
            checked={selection.coche}
            onCheckedChange={(v) => selection.basculer(v === true)}
          />
          {brouillon
            ? "Sélectionner pour l'émission"
            : 'Sélectionner pour Zeendoc'}
        </label>
      )}
    </CarteListe>
  )
}

/**
 * RG-FAC-19 : le sort de chaque brouillon de la sélection, dans l'ordre : son numéro, ou le motif du refus. Les
 * brouillons émis quittent le listing : on les nomme d'après la sélection, prise au moment de l'émission.
 */
function CompteRendu({
  comptesRendus,
  pieces,
  onFermer,
}: {
  comptesRendus?: CompteRenduEmission[]
  pieces: PieceListee[]
  onFermer: () => void
}) {
  const emis = comptesRendus?.filter((c) => c.numero).length ?? 0
  const ecartes = (comptesRendus?.length ?? 0) - emis
  return (
    <Dialog open={!!comptesRendus} onOpenChange={(o) => !o && onFermer()}>
      <DialogContent className='sm:max-w-2xl'>
        <DialogHeader>
          <DialogTitle>Compte rendu de l'émission</DialogTitle>
          <DialogDescription>
            {emis} émis
            {ecartes > 0 && `, ${ecartes} écarté${ecartes > 1 ? 's' : ''}`}.
          </DialogDescription>
        </DialogHeader>
        <div className='max-h-96 overflow-y-auto rounded-md border'>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Brouillon</TableHead>
                <TableHead>Résultat</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {comptesRendus?.map((c) => {
                const piece = pieces.find((p) => p.id === c.id)
                return (
                  <TableRow key={c.id}>
                    <TableCell>
                      {piece ? (
                        <>
                          <span className='block'>{piece.acheteur}</span>
                          <span className='text-muted-foreground text-xs'>
                            {piece.reference}
                          </span>
                        </>
                      ) : (
                        `n° ${c.id}`
                      )}
                    </TableCell>
                    <TableCell>
                      {c.numero ? (
                        <Link
                          to='/bo/facturation/$id'
                          params={{ id: String(c.id) }}
                          className='font-mono font-medium underline-offset-4 hover:underline'
                        >
                          {c.numero}
                        </Link>
                      ) : (
                        <span className='text-destructive text-sm'>
                          {c.motif}
                        </span>
                      )}
                    </TableCell>
                  </TableRow>
                )
              })}
            </TableBody>
          </Table>
        </div>
        <DialogFooter>
          <Button onClick={onFermer}>Fermer</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
