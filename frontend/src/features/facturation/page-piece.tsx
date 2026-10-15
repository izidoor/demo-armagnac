import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Link,
  useCanGoBack,
  useNavigate,
  useRouter,
} from '@tanstack/react-router'
import { ArrowLeft, FileDown, Send, TriangleAlert } from 'lucide-react'
import { toast } from 'sonner'
import {
  consulterJournalPiece,
  consulterPiece,
  emettrePiece,
  type AdresseFacturation,
  type LignePiece,
  type Piece,
} from '@/api/generated'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Table,
  TableBody,
  TableCell,
  TableFooter,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/ui/tooltip'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { PageMetier } from '@/components/metier/page-metier'
import {
  formatDate,
  formatDateHeure,
  formatEuros,
  formatHectolitres,
} from '@/features/declarations/format'
import { cleFacturation, urlPdfPiece } from './api'
import { ETATS, NATURES_OPERATION, SENS, VARIANTE_ETAT } from './libelles'

/**
 * Une pièce au back-office (spec 08 §6.1) : l'en-tête (origine, acheteur, dates, période, condition, échéance,
 * notes), les lignes groupées, le récapitulatif de TVA et les totaux ; l'onglet « Journal » (RG-FAC-20). Un
 * brouillon affiche ce qui l'empêche d'être émis (RG-FAC-09) et s'émet ; une pièce émise a son PDF (RG-FAC-15).
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
      description={
        <span className='flex flex-wrap items-center gap-2'>
          <Button variant='link' className='h-auto p-0' onClick={retour}>
            <ArrowLeft className='size-4' />
            Retour
          </Button>
          <span>{piece.titre}</span>
          <Badge variant={VARIANTE_ETAT[piece.etat]}>{ETATS[piece.etat]}</Badge>
          {piece.origine && <span>{piece.origine.reference}</span>}
          <span>
            Demandée le {formatDateHeure(piece.creeeLe)} par {piece.creeePar}
          </span>
        </span>
      }
      actions={
        <div className='flex gap-2'>
          {piece.pdf && (
            <Button variant='outline' onClick={pdf}>
              <FileDown className='size-4' /> PDF
            </Button>
          )}
          {piece.emettre && (
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
      {brouillon && empeche && (
        <Alert variant='destructive' className='mb-4'>
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
      <Tabs defaultValue='piece'>
        <TabsList className='mb-4'>
          <TabsTrigger value='piece'>Pièce</TabsTrigger>
          <TabsTrigger value='journal'>Journal</TabsTrigger>
        </TabsList>
        <TabsContent value='piece' className='space-y-4'>
          <EntetePiece piece={piece} />
          <Lignes piece={piece} />
          {piece.notePied && (
            <p className='text-muted-foreground text-sm whitespace-pre-line'>
              {piece.notePied}
            </p>
          )}
        </TabsContent>
        <TabsContent value='journal'>
          <JournalPiece id={piece.id} />
        </TabsContent>
      </Tabs>
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

function lignesAdresse(a: AdresseFacturation | undefined) {
  if (!a) return []
  return [
    a.ligne1,
    a.ligne2,
    [a.codePostal, a.ville].filter(Boolean).join(' '),
    a.pays,
  ].filter((l): l is string => !!l)
}

function EntetePiece({ piece }: { piece: Piece }) {
  const a = piece.acheteur
  const infos: [string, string | undefined][] = [
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
    <div className='grid gap-4 md:grid-cols-2'>
      <Card>
        <CardHeader>
          <CardTitle className='text-base'>Acheteur</CardTitle>
        </CardHeader>
        <CardContent className='space-y-1 text-sm'>
          <p className='font-medium'>
            {a.clientIdentifiant ? (
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
      <Card>
        <CardHeader>
          <CardTitle className='text-base'>Facture</CardTitle>
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
            <p className='mt-2 text-sm whitespace-pre-line'>
              {piece.noteEntete}
            </p>
          )}
        </CardContent>
      </Card>
    </div>
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
function Lignes({ piece }: { piece: Piece }) {
  return (
    <div className='space-y-4'>
      <div className='rounded-md border'>
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
      </div>
      <div className='flex flex-wrap justify-between gap-4'>
        <div className='rounded-md border'>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>TVA</TableHead>
                <TableHead className='text-right'>Base HT</TableHead>
                <TableHead className='text-right'>Montant</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {piece.recapitulatifTva.map((t) => (
                <TableRow key={`${t.code}-${t.taux}`}>
                  <TableCell>
                    {t.libelle}
                    {t.motif && (
                      <span className='text-muted-foreground block text-xs'>
                        {t.motif}
                      </span>
                    )}
                  </TableCell>
                  <TableCell className='text-right tabular-nums'>
                    {formatEuros(t.base)}
                  </TableCell>
                  <TableCell className='text-right tabular-nums'>
                    {formatEuros(t.tva)}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
        <div className='rounded-md border'>
          <Table>
            <TableBody>
              <TableRow>
                <TableCell>Total HT</TableCell>
                <TableCell className='text-right tabular-nums'>
                  {formatEuros(piece.totalHt)}
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell>Total TVA</TableCell>
                <TableCell className='text-right tabular-nums'>
                  {formatEuros(piece.totalTva)}
                </TableCell>
              </TableRow>
            </TableBody>
            <TableFooter>
              <TableRow>
                <TableCell>Total TTC</TableCell>
                <TableCell className='text-right font-semibold tabular-nums'>
                  {formatEuros(piece.totalTtc)}
                </TableCell>
              </TableRow>
            </TableFooter>
          </Table>
        </div>
      </div>
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
