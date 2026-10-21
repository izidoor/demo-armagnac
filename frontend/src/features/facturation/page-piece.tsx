import { type ReactNode, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Link,
  useCanGoBack,
  useNavigate,
  useRouter,
} from '@tanstack/react-router'
import {
  ArrowLeft,
  Building2,
  FileDown,
  FileSearch,
  History,
  Lock,
  Pencil,
  Send,
  Trash2,
  TriangleAlert,
} from 'lucide-react'
import { toast } from 'sonner'
import {
  abandonnerBrouillon,
  consulterJournalPiece,
  consulterPiece,
  ecrireNoteInternePiece,
  emettrePiece,
  listerNotesInternesPiece,
  modifierNoteInternePiece,
  supprimerNoteInternePiece,
  type AdresseFacturation,
  type LignePiece,
  type Piece,
} from '@/api/generated'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardAction,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/ui/tooltip'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { CarnetNotesInternes } from '@/components/metier/notes-internes'
import { PageMetier } from '@/components/metier/page-metier'
import { ActionDanger, ZoneDanger } from '@/components/metier/zone-danger'
import {
  formatDate,
  formatDateHeure,
  formatEuros,
  formatHectolitres,
} from '@/features/declarations/format'
import { SectionMessages } from '@/features/messagerie/section-messages'
import { cleFacturation, urlApercuPiece, urlPdfPiece } from './api'
import { BadgeZeendoc } from './badge-zeendoc'
import {
  EditionContenu,
  EditionFacture,
  EditionNotePied,
  TEINTE,
} from './edition-brouillon'
import { ETATS, NATURES_OPERATION, SENS, VARIANTE_ETAT } from './libelles'
import { Recapitulatif } from './recapitulatif'

/**
 * Une pièce au back-office (spec 08 §6.1) : l'en-tête (origine, acheteur, dates, période, condition, échéance,
 * notes), les lignes groupées, le récapitulatif de TVA et les totaux ; le journal dans le tiroir « Historique » (RG-FAC-20). Un
 * brouillon affiche ce qui l'empêche d'être émis (RG-FAC-09) ; il se modifie (RG-FAC-17), s'émet ou s'abandonne
 * (RG-FAC-18). Une pièce émise a son PDF (RG-FAC-15). Le carnet de notes internes en bas de page, pour toute pièce
 * (RG-FAC-21).
 */
export function PagePiece({ id }: { id: number }) {
  const requete = useQuery({
    queryKey: [...cleFacturation, 'piece', id],
    queryFn: () =>
      consulterPiece({ path: { id }, throwOnError: true }).then((r) => r.data),
    // L'ouverture d'une pièce émise est journalisée (RG-FAC-20) : pas de relecture au retour sur l'onglet.
    refetchOnWindowFocus: false,
  })
  if (!requete.data) {
    return (
      <PageMetier titre='Pièce' description='Chargement…'>
        {null}
      </PageMetier>
    )
  }
  return <VuePiece piece={requete.data} />
}

function VuePiece({ piece }: { piece: Piece }) {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const router = useRouter()
  const peutRevenir = useCanGoBack()
  const [confirmation, setConfirmation] = useState(false)
  const [abandon, setAbandon] = useState(false)
  const [edition, setEdition] = useState<Section | null>(null)
  const [historique, setHistorique] = useState(false)
  const brouillon = piece.etat === 'BROUILLON'
  const retour = () =>
    peutRevenir
      ? router.history.back()
      : navigate({
          to: '/bo/facturation',
          search: { onglet: brouillon ? 'brouillons' : 'emises' },
        })
  const emission = useMutation({
    mutationFn: () =>
      emettrePiece({ path: { id: piece.id }, throwOnError: true }).then(
        (r) => r.data
      ),
    meta: { erreurAffichee: true },
    onSuccess: (emise) => {
      setConfirmation(false)
      toast.success(`Facture ${emise.numero} émise.`)
      queryClient.invalidateQueries({ queryKey: cleFacturation })
      queryClient.invalidateQueries({ queryKey: ['declarations'] })
    },
    onError: () => {
      setConfirmation(false)
      queryClient.invalidateQueries({ queryKey: cleFacturation })
    },
  })
  const abandonner = useMutation({
    mutationFn: () =>
      abandonnerBrouillon({ path: { id: piece.id }, throwOnError: true }),
    onSuccess: () => {
      setAbandon(false)
      toast.success(
        piece.origine
          ? `Brouillon abandonné : ${piece.origine.reference} redevient à facturer.`
          : 'Brouillon abandonné.'
      )
      queryClient.removeQueries({
        queryKey: [...cleFacturation, 'piece', piece.id],
      })
      queryClient.invalidateQueries({ queryKey: cleFacturation })
      queryClient.invalidateQueries({ queryKey: ['declarations'] })
      navigate({ to: '/bo/facturation', search: { onglet: 'brouillons' } })
    },
    onError: () => setAbandon(false),
  })
  const pdf = () => {
    window.open(urlPdfPiece(piece.id), '_blank', 'noopener')
    setTimeout(
      () =>
        queryClient.invalidateQueries({
          queryKey: [...cleFacturation, 'piece', piece.id, 'journal'],
        }),
      1500
    )
  }
  const titre = piece.numero
    ? `${SENS[piece.sens]} ${piece.numero}`
    : `Brouillon de ${SENS[piece.sens].toLowerCase()}`
  const empeche = piece.empechements.length > 0

  return (
    <PageMetier
      titre={titre}
      complement={piece.numero ? piece.acheteur.nom : undefined}
      description={
        <span className='flex flex-wrap items-center gap-2'>
          <Button variant='link' className='h-auto p-0' onClick={retour}>
            <ArrowLeft className='size-4' />
            Retour
          </Button>
          {piece.acheteur.clientIdentifiant && (
            <Button variant='link' className='h-auto p-0' asChild>
              <Link
                to='/bo/operateurs/$id'
                params={{ id: piece.acheteur.clientIdentifiant }}
              >
                <Building2 className='size-4' />
                Voir la fiche opérateur
              </Link>
            </Button>
          )}
          <Badge variant={VARIANTE_ETAT[piece.etat]}>{ETATS[piece.etat]}</Badge>
          <BadgeZeendoc piece={piece} avecDates />
          {piece.origine && <span>{piece.origine.reference}</span>}
        </span>
      }
      actions={
        <div className='flex gap-2'>
          <Button variant='outline' onClick={() => setHistorique(true)}>
            <History className='size-4' /> Historique
          </Button>
          {piece.pdf && (
            <Button variant='outline' onClick={pdf}>
              <FileDown className='size-4' /> PDF
            </Button>
          )}
          {brouillon && !edition && (
            <Button
              variant='outline'
              onClick={() =>
                window.open(urlApercuPiece(piece.id), '_blank', 'noopener')
              }
            >
              <FileSearch className='size-4' /> Aperçu PDF
            </Button>
          )}
          {piece.emettre && !edition && (
            <Tooltip>
              <TooltipTrigger asChild>
                <span tabIndex={empeche ? 0 : undefined}>
                  <Button
                    disabled={empeche}
                    onClick={() => setConfirmation(true)}
                  >
                    <Send className='size-4' /> Émettre
                  </Button>
                </span>
              </TooltipTrigger>
              {empeche && (
                <TooltipContent>
                  Le brouillon ne peut pas être émis : voir ce qui manque.
                </TooltipContent>
              )}
            </Tooltip>
          )}
        </div>
      }
    >
      <ContenuPiece piece={piece} edition={edition} onEditer={setEdition} />
      <NotesPiece piece={piece} />
      {piece.emettre && !edition && (
        <ZoneDanger>
          <ActionDanger
            titre='Abandonner le brouillon'
            description={`Il est supprimé avec son journal et ses notes internes${piece.origine ? ` ; ${piece.origine.reference} redevient à facturer` : ''}. Aucun numéro n'est consommé.`}
          >
            <Button variant='destructive' onClick={() => setAbandon(true)}>
              <Trash2 className='size-4' /> Abandonner
            </Button>
          </ActionDanger>
        </ZoneDanger>
      )}
      <Sheet open={historique} onOpenChange={setHistorique}>
        <SheetContent className='w-full overflow-y-auto sm:max-w-2xl'>
          <SheetHeader>
            <SheetTitle>Historique</SheetTitle>
            <SheetDescription>
              Le journal de la pièce, du plus récent au plus ancien.
            </SheetDescription>
          </SheetHeader>
          <div className='px-4 pb-4'>
            <JournalPiece id={piece.id} />
          </div>
        </SheetContent>
      </Sheet>
      <ConfirmDialog
        open={abandon}
        onOpenChange={setAbandon}
        title='Abandonner ce brouillon ?'
        desc={`Il sera supprimé avec son journal et ses notes internes${piece.origine ? ` ; ${piece.origine.reference} redeviendra à facturer` : ''}.`}
        confirmText='Abandonner'
        cancelBtnText='Garder'
        destructive
        isLoading={abandonner.isPending}
        handleConfirm={() => abandonner.mutate()}
      />
      <ConfirmDialog
        open={confirmation}
        onOpenChange={setConfirmation}
        title='Émettre cette facture ?'
        desc={`Elle recevra le numéro suivant, sera datée d'aujourd'hui et ne se modifiera plus : une erreur se corrigera par avoir. Total TTC : ${formatEuros(piece.totalTtc)}.`}
        confirmText='Émettre'
        cancelBtnText='Annuler'
        isLoading={emission.isPending}
        handleConfirm={() => emission.mutate()}
      />
    </PageMetier>
  )
}

/** Les parties d'un brouillon qui se modifient, une à la fois (RG-FAC-17). */
type Section = 'facture' | 'contenu' | 'note'

/**
 * Pourquoi une partie ne se modifie pas, ou rien si elle se modifie. L'acheteur ne se modifie jamais : il se
 * corrige sur la fiche de l'opérateur (RG-FAC-11 révisée).
 */
function verrou(piece: Piece, section: Section | 'acheteur') {
  if (piece.etat !== 'BROUILLON')
    return 'Pièce émise : elle ne se modifie plus. Une erreur se corrige par avoir.'
  if (section === 'acheteur')
    return "L'acheteur ne se modifie pas : il se corrige sur la fiche de l'opérateur et se relit à l'émission."
  if (!piece.gerer)
    return 'Vous consultez la facturation sans pouvoir la gérer : la pièce ne se modifie pas.'
  return undefined
}

/** En haut à droite d'une carte : un cadenas qui dit pourquoi elle ne se modifie pas, ou le bouton « Modifier ». */
function ActionCarte({
  piece,
  section,
  edition,
  onEditer,
}: {
  piece: Piece
  section: Section | 'acheteur'
  edition: Section | null
  onEditer: (section: Section) => void
}) {
  const raison = verrou(piece, section)
  if (raison || section === 'acheteur') {
    return (
      <Tooltip>
        <TooltipTrigger asChild>
          <span
            tabIndex={0}
            aria-label={raison}
            className='text-muted-foreground inline-flex size-8 items-center justify-center'
          >
            <Lock className='size-4' />
          </span>
        </TooltipTrigger>
        <TooltipContent className='max-w-xs'>{raison}</TooltipContent>
      </Tooltip>
    )
  }
  return (
    <Button
      variant='outline'
      size='sm'
      className={TEINTE}
      disabled={edition != null}
      onClick={() => onEditer(section)}
    >
      <Pencil className='size-4' /> Modifier
    </Button>
  )
}

/**
 * La pièce : ce qui empêche son émission, puis ses cartes. Les cartes Facture et Contenu
 * d'un brouillon se modifient une à une, en place.
 */
function ContenuPiece({
  piece,
  edition,
  onEditer,
}: {
  piece: Piece
  edition: Section | null
  onEditer: (section: Section | null) => void
}) {
  const action = (section: Section | 'acheteur') => (
    <ActionCarte
      piece={piece}
      section={section}
      edition={edition}
      onEditer={onEditer}
    />
  )
  const terminer = () => onEditer(null)
  const cartes = (
    <div className='mb-8 space-y-8'>
      <div className='grid gap-6 md:grid-cols-2'>
        <CarteAcheteur piece={piece} action={action('acheteur')} />
        {edition === 'facture' ? (
          <EditionFacture piece={piece} onTerminer={terminer} />
        ) : (
          <CarteFacture piece={piece} action={action('facture')} />
        )}
      </div>
      {edition === 'contenu' ? (
        <EditionContenu piece={piece} onTerminer={terminer} />
      ) : (
        <Lignes piece={piece} action={action('contenu')} />
      )}
      {edition === 'note' ? (
        <EditionNotePied piece={piece} onTerminer={terminer} />
      ) : (
        (piece.notePied || !verrou(piece, 'note')) && (
          <CarteNotePied piece={piece} action={action('note')} />
        )
      )}
      {/* Spec 15, RG-MSG-17 : une pièce émise d'un client opérateur a sa conversation. */}
      {piece.etat === 'EMISE' && piece.acheteur.clientIdentifiant && (
        <SectionMessages
          type='FACTURE'
          identifiant={String(piece.id)}
          description={`Les échanges avec ${piece.acheteur.nom} sur cette pièce ; aussi dans la messagerie.`}
        />
      )}
    </div>
  )
  return (
    <>
      {piece.etat === 'BROUILLON' && piece.empechements.length > 0 && (
        <Alert variant='destructive' className='mt-2 mb-6'>
          <TriangleAlert />
          <AlertTitle>Ne peut pas être émis</AlertTitle>
          <AlertDescription>
            <ul className='list-disc ps-4'>
              {piece.empechements.map((e) => (
                <li key={e}>{e}</li>
              ))}
            </ul>
          </AlertDescription>
        </Alert>
      )}
      {cartes}
    </>
  )
}

/**
 * RG-FAC-21 : le carnet de notes internes de la pièce, comme celui d'une DSM, quel que soit son état ; jamais imprimé
 * ni transmis. Lu par le personnel du BNIA, écrit par qui gère la facturation.
 */
function NotesPiece({ piece }: { piece: Piece }) {
  const queryClient = useQueryClient()
  const cle = [...cleFacturation, 'piece', piece.id, 'notes']
  const notes = useQuery({
    queryKey: cle,
    queryFn: () =>
      listerNotesInternesPiece({
        path: { piece: piece.id },
        throwOnError: true,
      }).then((r) => r.data),
  })
  const path = { piece: piece.id }
  return (
    <CarnetNotesInternes
      description='Le carnet du BNIA sur cette pièce, brouillon ou émise : relances, échanges avec le comptable, points à vérifier. Jamais imprimé ni transmis : le client ne le voit pas.'
      notes={notes.data ?? []}
      peutAnnoter={piece.gerer}
      ecrire={(texte) =>
        ecrireNoteInternePiece({ path, body: { texte }, throwOnError: true })
      }
      modifier={(n, texte) =>
        modifierNoteInternePiece({
          path: { ...path, id: n.id },
          body: { texte },
          throwOnError: true,
        })
      }
      supprimer={(n) =>
        supprimerNoteInternePiece({
          path: { ...path, id: n.id },
          throwOnError: true,
        })
      }
      onChange={() => queryClient.invalidateQueries({ queryKey: cle })}
    />
  )
}

function lignesAdresse(a: AdresseFacturation | undefined) {
  if (!a) return []
  return [
    a.ligne1,
    a.ligne2,
    [a.codePostal, a.ville].filter(Boolean).join(' '),
    a.pays,
  ].filter((l): l is string => !!l)
}

/** L'en-tête de la pièce ; au portail, l'acheteur ne renvoie pas à la fiche opérateur du back-office. */
export function EntetePiece({
  piece,
  portail,
}: {
  piece: Piece
  portail?: boolean
}) {
  return (
    <div className='grid gap-6 md:grid-cols-2'>
      <CarteAcheteur piece={piece} portail={portail} />
      <CarteFacture piece={piece} />
    </div>
  )
}

/** L'acheteur, copié de la fiche de l'opérateur (RG-FAC-11) ; au back-office, lien vers cette fiche. */
function CarteAcheteur({
  piece,
  portail,
  action,
}: {
  piece: Piece
  portail?: boolean
  action?: ReactNode
}) {
  const a = piece.acheteur
  return (
    <Card>
      <CardHeader>
        <CardTitle className='text-base'>Acheteur</CardTitle>
        {action && <CardAction>{action}</CardAction>}
      </CardHeader>
      <CardContent className='space-y-1 text-sm'>
        <p className='font-medium'>
          {a.clientIdentifiant && !portail ? (
            <Link
              to='/bo/operateurs/$id'
              params={{ id: a.clientIdentifiant }}
              className='underline-offset-4 hover:underline'
            >
              <span className='font-mono'>{a.clientCode}</span> · {a.nom}
            </Link>
          ) : (
            a.nom
          )}
        </p>
        {lignesAdresse(a.adresseFacturation).map((l) => (
          <p key={l}>{l}</p>
        ))}
        {a.siren && (
          <p>
            SIREN {a.siren}
            {a.numeroTva && ` · TVA ${a.numeroTva}`}
          </p>
        )}
        {a.email && <p>{a.email}</p>}
        {piece.etat === 'BROUILLON' && a.manques.length > 0 && (
          <p className='text-destructive'>
            Il manque : {a.manques.join(', ')}. À corriger sur la fiche de
            l'opérateur ; l'acheteur est relu à l'émission.
          </p>
        )}
      </CardContent>
    </Card>
  )
}

/**
 * Titre (libellé de la nature d'origine, ou « Avoir », RG-FAC-15), dates, période, échéance, bon de commande,
 * nature de l'opération, note d'en-tête.
 */
function CarteFacture({ piece, action }: { piece: Piece; action?: ReactNode }) {
  const infos: [string, string | undefined][] = [
    ['Titre', piece.titre],
    ["Date d'émission", formatDate(piece.dateEmission) || '—'],
    ["Date de l'opération", formatDate(piece.dateOperation)],
    ['Période', piece.libellePeriode],
    [
      'Échéance',
      piece.echeance
        ? `${formatDate(piece.echeance)} (${piece.condition.libelle})`
        : `${piece.condition.libelle}, calculée à l'émission`,
    ],
    ['Bon de commande', piece.bonDeCommande],
    [
      "Nature de l'opération",
      piece.natureOperation
        ? NATURES_OPERATION[piece.natureOperation]
        : undefined,
    ],
  ]
  return (
    <Card>
      <CardHeader>
        <CardTitle className='text-base'>Facture</CardTitle>
        {action && <CardAction>{action}</CardAction>}
      </CardHeader>
      <CardContent>
        <dl className='grid grid-cols-[auto_1fr] gap-x-4 gap-y-1 text-sm'>
          {infos
            .filter(([, valeur]) => valeur)
            .map(([cle, valeur]) => (
              <div key={cle} className='contents'>
                <dt className='text-muted-foreground'>{cle}</dt>
                <dd>{valeur}</dd>
              </div>
            ))}
        </dl>
        {piece.noteEntete && (
          <p className='mt-2 text-sm whitespace-pre-line'>{piece.noteEntete}</p>
        )}
      </CardContent>
    </Card>
  )
}

/** La note de pied, imprimée sous les totaux ; vide, elle ne s'affiche que si elle peut être saisie. */
export function CarteNotePied({
  piece,
  action,
}: {
  piece: Piece
  action?: ReactNode
}) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className='text-base'>Note de pied</CardTitle>
        {action && <CardAction>{action}</CardAction>}
      </CardHeader>
      <CardContent>
        {piece.notePied ? (
          <p className='text-sm whitespace-pre-line'>{piece.notePied}</p>
        ) : (
          <p className='text-muted-foreground text-sm'>Aucune note.</p>
        )}
      </CardContent>
    </Card>
  )
}

/** Un prix unitaire HT, jusqu'à 4 décimales (spec 08 §3.2). */
const prix = new Intl.NumberFormat('fr-FR', {
  style: 'currency',
  currency: 'EUR',
  minimumFractionDigits: 2,
  maximumFractionDigits: 4,
})

/** Les lignes groupées (RG-FAC-04), le récapitulatif de TVA et les totaux (RG-FAC-05). */
export function Lignes({
  piece,
  action,
}: {
  piece: Piece
  action?: ReactNode
}) {
  return (
    <div className='space-y-6'>
      <Card>
        <CardHeader>
          <CardTitle className='text-base'>Contenu</CardTitle>
          {action && <CardAction>{action}</CardAction>}
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Désignation</TableHead>
                <TableHead className='text-right'>Quantité</TableHead>
                <TableHead className='text-right'>Prix unitaire HT</TableHead>
                <TableHead className='text-right'>Montant HT</TableHead>
                <TableHead className='text-right'>TVA</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {piece.lignes.map((l, i) =>
                l.sorte === 'GROUPE' ? (
                  <Groupe key={i} groupe={l} />
                ) : (
                  <Ligne key={i} ligne={l} />
                )
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
      <Recapitulatif
        tva={piece.recapitulatifTva}
        totalHt={piece.totalHt}
        totalTva={piece.totalTva}
        totalTtc={piece.totalTtc}
      />
    </div>
  )
}

function Groupe({ groupe }: { groupe: LignePiece }) {
  return (
    <>
      <TableRow className='bg-muted/50'>
        <TableCell colSpan={5} className='font-medium'>
          {groupe.texte}
        </TableCell>
      </TableRow>
      {groupe.lignes.map((l, i) => (
        <Ligne key={i} ligne={l} retrait />
      ))}
      <TableRow>
        <TableCell colSpan={3} className='text-muted-foreground italic'>
          Sous-total {groupe.texte}
        </TableCell>
        <TableCell className='text-right italic tabular-nums'>
          {formatEuros(groupe.montantHt)}
        </TableCell>
        <TableCell />
      </TableRow>
    </>
  )
}

function Ligne({ ligne, retrait }: { ligne: LignePiece; retrait?: boolean }) {
  if (ligne.sorte === 'TEXTE') {
    return (
      <TableRow>
        <TableCell colSpan={5} className={retrait ? 'ps-6' : undefined}>
          {ligne.texte}
        </TableCell>
      </TableRow>
    )
  }
  return (
    <TableRow>
      <TableCell className={retrait ? 'ps-6' : undefined}>
        {ligne.texte}
        <span className='text-muted-foreground block font-mono text-xs'>
          {ligne.code}
        </span>
      </TableCell>
      <TableCell className='text-right whitespace-nowrap tabular-nums'>
        {ligne.quantite != null && formatHectolitres(ligne.quantite)}{' '}
        {ligne.unite}
      </TableCell>
      <TableCell className='text-right tabular-nums'>
        {ligne.prixUnitaire != null && prix.format(ligne.prixUnitaire)}
      </TableCell>
      <TableCell className='text-right tabular-nums'>
        {formatEuros(ligne.montantHt)}
      </TableCell>
      <TableCell className='text-right'>{ligne.tvaLibelle}</TableCell>
    </TableRow>
  )
}

/** RG-FAC-20 : le journal de la pièce, du plus récent au plus ancien, lu par le BNIA. */
function JournalPiece({ id }: { id: number }) {
  const journal = useQuery({
    queryKey: [...cleFacturation, 'piece', id, 'journal'],
    queryFn: () =>
      consulterJournalPiece({ path: { id }, throwOnError: true }).then(
        (r) => r.data
      ),
  })
  const entrees = journal.data ?? []
  return (
    <div className='rounded-md border'>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Date</TableHead>
            <TableHead>Événement</TableHead>
            <TableHead>Détail</TableHead>
            <TableHead>Par</TableHead>
            <TableHead>Côté</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {entrees.length === 0 && (
            <TableRow>
              <TableCell
                colSpan={5}
                className='text-muted-foreground h-16 text-center'
              >
                {journal.isPending ? 'Chargement…' : 'Aucune entrée.'}
              </TableCell>
            </TableRow>
          )}
          {entrees.map((e, i) => (
            <TableRow key={i}>
              <TableCell className='whitespace-nowrap'>
                {formatDateHeure(e.date)}
              </TableCell>
              <TableCell>{e.libelle}</TableCell>
              <TableCell className='text-sm'>{e.detail ?? ''}</TableCell>
              <TableCell>{e.acteur}</TableCell>
              <TableCell>{e.cote === 'VENDEUR' ? 'BNIA' : 'Client'}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )
}
