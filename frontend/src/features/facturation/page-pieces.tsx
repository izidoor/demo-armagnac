import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from '@tanstack/react-router'
import { ChevronRight, CloudUpload, Loader2, Send, X } from 'lucide-react'
import { toast } from 'sonner'
import {
  emettreBrouillons,
  listerPieces,
  transmettreAZeendoc,
  type CompteRenduEmission,
  type PieceListee,
} from '@/api/generated'
import { useDebounce } from '@/hooks/use-debounce'
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
import { Label } from '@/components/ui/label'
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
import { SENS } from './libelles'

/** La transmission à Zeendoc est simulée : le loader dure au moins ce délai, pour la démonstration. */
const DELAI_ZEENDOC = 1500

type OngletPieces = 'brouillons' | 'emises' | 'payees'

/**
 * Factures du back-office (spec 08 §6.1, UC-FAC-4) : l'historique des pièces émises, filtré par numéro, acheteur,
 * période et date d'émission (RG-FAC-27), et l'onglet « Brouillons », où qui gère la facturation sélectionne des
 * brouillons à émettre ensemble, avec un compte rendu par brouillon (RG-FAC-19) ; dans l'onglet « Émises », il
 * sélectionne des pièces à transmettre à Zeendoc, une seule fois chacune (RG-FAC-32, simulé) ; les pièces payées
 * passent dans l'onglet « Payées » (RG-FAC-33). Filtrable sur un client par
 * l'URL ; la fiche opérateur a son propre onglet « Factures ». Une ligne ouvre la pièce.
 */
export function PagePieces({
  client,
  onglet,
}: {
  client?: string
  onglet: OngletPieces
}) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { actif } = useMoi()
  const gere = useDroits(actif?.operateur ?? 0)?.facturation === 'GESTION'
  const [selection, setSelection] = useState<Set<number>>(new Set())
  const [confirmation, setConfirmation] = useState(false)
  const [emis, setEmis] = useState<{
    comptesRendus: CompteRenduEmission[]
    selection: PieceListee[]
  }>()
  const [numero, setNumero] = useState('')
  const [acheteur, setAcheteur] = useState('')
  const [periode, setPeriode] = useState('')
  const [du, setDu] = useState('')
  const [au, setAu] = useState('')
  const brouillons = onglet === 'brouillons'
  // Les textes saisis filtrent une fois la frappe arrêtée.
  const textes = useDebounce({ numero, acheteur, periode })
  const filtre = {
    client,
    etat: brouillons ? ('BROUILLON' as const) : ('EMISE' as const),
    numero: brouillons ? undefined : textes.numero || undefined,
    acheteur: textes.acheteur || undefined,
    periode: textes.periode || undefined,
    du: brouillons ? undefined : du || undefined,
    au: brouillons ? undefined : au || undefined,
  }
  const listing = useQuery({
    queryKey: [...cleFacturation, 'pieces', filtre],
    queryFn: () =>
      listerPieces({ query: filtre, throwOnError: true }).then((r) => r.data),
  })
  // « Émises » et « Payées » se partagent le listing des pièces émises, selon leur état Zeendoc.
  const pieces = (listing.data ?? []).filter(
    (p) => brouillons || (onglet === 'payees') === (p.zeendoc === 'PAYEE')
  )
  // Le nombre de brouillons en attente, sans les filtres de saisie (seul le client de l'URL compte).
  const filtreBrouillons = { client, etat: 'BROUILLON' as const }
  const enAttente = useQuery({
    queryKey: [...cleFacturation, 'pieces', filtreBrouillons],
    queryFn: () =>
      listerPieces({ query: filtreBrouillons, throwOnError: true }).then(
        (r) => r.data
      ),
  }).data?.length
  // De même, le nombre de pièces émises non payées (onglet « Émises »).
  const filtreEmises = { client, etat: 'EMISE' as const }
  const nonPayees = useQuery({
    queryKey: [...cleFacturation, 'pieces', filtreEmises],
    queryFn: () =>
      listerPieces({ query: filtreEmises, throwOnError: true }).then(
        (r) => r.data
      ),
  }).data?.filter((p) => p.zeendoc !== 'PAYEE').length
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
    navigate({
      to: '/bo/facturation',
      search: { client, onglet: o as OngletPieces },
    })
  }
  const nomClient = pieces.find((p) => p.clientIdentifiant === client)

  return (
    <PageMetier
      titre='Factures'
      description='Les factures et avoirs du BNIA, quelle que soit leur origine ; les brouillons attendent leur émission.'
    >
      <Tabs value={onglet} onValueChange={changerOnglet} className='mb-4'>
        <TabsList>
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
      <div className='mb-4 flex flex-wrap items-end gap-4'>
        {client && (
          <div className='space-y-1'>
            <Label>Client</Label>
            <div className='flex h-9 items-center gap-1 rounded-md border px-3 text-sm'>
              {nomClient ? (
                <>
                  <span className='font-mono'>{nomClient.clientCode}</span> ·{' '}
                  {nomClient.acheteur}
                </>
              ) : (
                `Opérateur n° ${client}`
              )}
              <Button
                variant='ghost'
                size='icon'
                className='size-6'
                aria-label='Retirer le filtre sur le client'
                onClick={() =>
                  navigate({ to: '/bo/facturation', search: { onglet } })
                }
              >
                <X className='size-4' />
              </Button>
            </div>
          </div>
        )}
        {!brouillons && (
          <div className='space-y-1'>
            <Label htmlFor='filtre-numero'>Numéro</Label>
            <Input
              id='filtre-numero'
              className='w-36'
              placeholder='2026…'
              value={numero}
              onChange={(e) => setNumero(e.target.value)}
            />
          </div>
        )}
        <div className='space-y-1'>
          <Label htmlFor='filtre-acheteur'>Acheteur</Label>
          <Input
            id='filtre-acheteur'
            className='w-56'
            placeholder='Nom ou n° BNIA'
            value={acheteur}
            onChange={(e) => setAcheteur(e.target.value)}
          />
        </div>
        <div className='space-y-1'>
          <Label htmlFor='filtre-periode'>Période</Label>
          <Input
            id='filtre-periode'
            className='w-44'
            placeholder='juillet 2026'
            value={periode}
            onChange={(e) => setPeriode(e.target.value)}
          />
        </div>
        {!brouillons && (
          <>
            <div className='space-y-1'>
              <Label htmlFor='filtre-du'>Émise du</Label>
              <Input
                id='filtre-du'
                type='date'
                className='w-40'
                value={du}
                onChange={(e) => setDu(e.target.value)}
              />
            </div>
            <div className='space-y-1'>
              <Label htmlFor='filtre-au'>au</Label>
              <Input
                id='filtre-au'
                type='date'
                className='w-40'
                value={au}
                onChange={(e) => setAu(e.target.value)}
              />
            </div>
          </>
        )}
      </div>
      {brouillons && gere && (
        <div className='mb-2 flex justify-end'>
          <Button
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
  return (
    <>
      <div className='rounded-md border'>
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
              <TableHead>Période</TableHead>
              <TableHead className='text-right'>HT</TableHead>
              <TableHead className='text-right'>TVA</TableHead>
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
                className='cursor-pointer'
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
                  <TableCell>
                    {p.clientCode && (
                      <span className='font-mono'>{p.clientCode} · </span>
                    )}
                    {p.acheteur}
                  </TableCell>
                )}
                <TableCell>
                  <span className='block'>{p.libelleNature ?? '—'}</span>
                  <span className='text-muted-foreground text-xs'>
                    {p.reference}
                  </span>
                </TableCell>
                <TableCell>{p.libellePeriode}</TableCell>
                <TableCell className='text-right tabular-nums'>
                  {formatEuros(p.ht)}
                </TableCell>
                <TableCell className='text-right tabular-nums'>
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
      <Pagination {...pagination} />
    </>
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
