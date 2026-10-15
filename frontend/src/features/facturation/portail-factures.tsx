import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import {
  Link,
  useCanGoBack,
  useNavigate,
  useRouter,
} from '@tanstack/react-router'
import { ArrowLeft, ChevronRight, FileDown } from 'lucide-react'
import { consulterMaFacture, listerMesFactures } from '@/api/generated'
import { useDebounce } from '@/hooks/use-debounce'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
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
import { PageMetier } from '@/components/metier/page-metier'
import { formatDate, formatEuros } from '@/features/declarations/format'
import { cleFacturation, urlPdfMaFacture } from './api'
import { SENS } from './libelles'
import { CarteNotePied, EntetePiece, Lignes } from './page-piece'

type FiltreSens = 'TOUS' | 'FACTURE' | 'AVOIR'

/**
 * Portail, menu « Factures » (spec 08 §6.2, RG-FAC-28) : les factures et avoirs émis dont l'opérateur actif est le
 * client, retrouvés par numéro, période et date d'émission, distingués par sens et par nature d'origine. Jamais de
 * brouillon. Une ligne ouvre la pièce.
 */
export function PageMesFactures({ operateur }: { operateur: number }) {
  const navigate = useNavigate()
  const [numero, setNumero] = useState('')
  const [periode, setPeriode] = useState('')
  const [sens, setSens] = useState<FiltreSens>('TOUS')
  const [du, setDu] = useState('')
  const [au, setAu] = useState('')
  const textes = useDebounce({ numero, periode })
  const filtre = {
    numero: textes.numero || undefined,
    periode: textes.periode || undefined,
    sens: sens === 'TOUS' ? undefined : sens,
    du: du || undefined,
    au: au || undefined,
  }
  const listing = useQuery({
    queryKey: [...cleFacturation, 'portail', operateur, filtre],
    queryFn: () =>
      listerMesFactures({
        path: { operateur },
        query: filtre,
        throwOnError: true,
      }).then((r) => r.data),
  })
  const pieces = listing.data ?? []
  const ouvrir = (id: number) =>
    navigate({ to: '/portail/factures/$id', params: { id: String(id) } })

  return (
    <PageMetier
      titre='Factures'
      description='Les factures et avoirs que le BNIA vous a adressés.'
    >
      <div className='mb-4 flex flex-wrap items-end gap-4'>
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
        <div className='space-y-1'>
          <Label htmlFor='filtre-sens'>Pièces</Label>
          <Select value={sens} onValueChange={(v) => setSens(v as FiltreSens)}>
            <SelectTrigger id='filtre-sens' className='w-36'>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='TOUS'>Toutes</SelectItem>
              <SelectItem value='FACTURE'>Factures</SelectItem>
              <SelectItem value='AVOIR'>Avoirs</SelectItem>
            </SelectContent>
          </Select>
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
      </div>
      <div className='rounded-md border'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Numéro</TableHead>
              <TableHead>Date</TableHead>
              <TableHead>Objet</TableHead>
              <TableHead>Période</TableHead>
              <TableHead className='text-right'>HT</TableHead>
              <TableHead className='text-right'>TVA</TableHead>
              <TableHead className='text-right'>TTC</TableHead>
              <TableHead className='w-0' />
            </TableRow>
          </TableHeader>
          <TableBody>
            {pieces.length === 0 && (
              <TableRow>
                <TableCell
                  colSpan={8}
                  className='text-muted-foreground h-16 text-center'
                >
                  {listing.isPending ? 'Chargement…' : 'Aucune facture.'}
                </TableCell>
              </TableRow>
            )}
            {pieces.map((p) => (
              <TableRow
                key={p.id}
                className='cursor-pointer'
                onClick={() => ouvrir(p.id)}
              >
                <TableCell className='font-mono font-medium'>
                  {p.numero}
                  {p.sens === 'AVOIR' && (
                    <Badge variant='secondary' className='ms-2'>
                      {SENS[p.sens]}
                    </Badge>
                  )}
                </TableCell>
                <TableCell>{formatDate(p.dateEmission)}</TableCell>
                <TableCell>
                  <span className='block'>
                    {p.libelleNature ?? SENS[p.sens]}
                  </span>
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
                <TableCell>
                  <Button
                    variant='ghost'
                    size='sm'
                    asChild
                    onClick={(e) => e.stopPropagation()}
                  >
                    <Link
                      to='/portail/factures/$id'
                      params={{ id: String(p.id) }}
                      aria-label={`Ouvrir la pièce ${p.numero}`}
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
    </PageMetier>
  )
}

/**
 * Portail : une facture ou un avoir émis (spec 08 §6.2, RG-FAC-28) : en-tête, référence de l'origine, lignes,
 * récapitulatif de TVA, totaux, PDF. L'ouverture et le téléchargement sont journalisés côté client ; le journal
 * reste au BNIA.
 */
export function PageMaFacture({
  operateur,
  id,
}: {
  operateur: number
  id: number
}) {
  const navigate = useNavigate()
  const router = useRouter()
  const peutRevenir = useCanGoBack()
  const requete = useQuery({
    queryKey: [...cleFacturation, 'portail', operateur, 'piece', id],
    queryFn: () =>
      consulterMaFacture({
        path: { operateur, id },
        throwOnError: true,
      }).then((r) => r.data),
    // Chaque ouverture est journalisée (RG-FAC-28) : pas de relecture au retour sur l'onglet.
    refetchOnWindowFocus: false,
  })
  const piece = requete.data
  if (!piece) {
    return (
      <PageMetier titre='Facture' description='Chargement…'>
        {null}
      </PageMetier>
    )
  }
  const retour = () =>
    peutRevenir ? router.history.back() : navigate({ to: '/portail/factures' })
  return (
    <PageMetier
      titre={`${SENS[piece.sens]} ${piece.numero}`}
      description={
        <span className='flex flex-wrap items-center gap-2'>
          <Button variant='link' className='h-auto p-0' onClick={retour}>
            <ArrowLeft className='size-4' />
            Retour
          </Button>
          {piece.origine && <span>{piece.origine.reference}</span>}
        </span>
      }
      actions={
        <Button
          variant='outline'
          onClick={() =>
            window.open(
              urlPdfMaFacture(operateur, piece.id),
              '_blank',
              'noopener'
            )
          }
        >
          <FileDown className='size-4' /> PDF
        </Button>
      }
    >
      <div className='mb-8 space-y-8'>
        <EntetePiece piece={piece} portail />
        <Lignes piece={piece} />
        {piece.notePied && <CarteNotePied piece={piece} />}
      </div>
    </PageMetier>
  )
}
