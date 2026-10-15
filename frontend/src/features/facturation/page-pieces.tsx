import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link, useNavigate } from '@tanstack/react-router'
import { ChevronRight, X } from 'lucide-react'
import { listerPieces, type PieceListee } from '@/api/generated'
import { useDebounce } from '@/hooks/use-debounce'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
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
import { PageMetier } from '@/components/metier/page-metier'
import {
  formatDate,
  formatDateHeure,
  formatEuros,
} from '@/features/declarations/format'
import { cleFacturation } from './api'
import { SENS } from './libelles'

type OngletPieces = 'emises' | 'brouillons'

/**
 * Factures du back-office (spec 08 §6.1, UC-FAC-4) : l'historique des pièces émises, filtré par numéro, acheteur,
 * période et date d'émission (RG-FAC-27), et l'onglet « Brouillons ». Filtrable sur un client par l'URL ; la
 * fiche opérateur a son propre onglet « Factures ». Une ligne ouvre la pièce.
 */
export function PagePieces({
  client,
  onglet,
}: {
  client?: string
  onglet: OngletPieces
}) {
  const navigate = useNavigate()
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
  const pieces = listing.data ?? []
  const changerOnglet = (o: string) =>
    navigate({
      to: '/bo/facturation',
      search: { client, onglet: o as OngletPieces },
    })
  const nomClient = pieces.find((p) => p.clientIdentifiant === client)

  return (
    <PageMetier
      titre='Factures'
      description='Les factures et avoirs du BNIA, quelle que soit leur origine ; les brouillons attendent leur émission.'
    >
      <Tabs value={onglet} onValueChange={changerOnglet} className='mb-4'>
        <TabsList>
          <TabsTrigger value='emises'>Émises</TabsTrigger>
          <TabsTrigger value='brouillons'>Brouillons</TabsTrigger>
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
      <TablePieces
        pieces={pieces}
        brouillons={brouillons}
        chargement={listing.isPending}
      />
    </PageMetier>
  )
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
}: {
  pieces: PieceListee[]
  brouillons: boolean
  chargement: boolean
  avecAcheteur?: boolean
}) {
  const navigate = useNavigate()
  return (
    <div className='rounded-md border'>
      <Table>
        <TableHeader>
          <TableRow>
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
            <TableHead className='w-0' />
          </TableRow>
        </TableHeader>
        <TableBody>
          {pieces.length === 0 && (
            <TableRow>
              <TableCell
                colSpan={(brouillons ? 7 : 8) + (avecAcheteur ? 1 : 0)}
                className='text-muted-foreground h-16 text-center'
              >
                {chargement
                  ? 'Chargement…'
                  : brouillons
                    ? 'Aucun brouillon.'
                    : 'Aucune pièce émise.'}
              </TableCell>
            </TableRow>
          )}
          {pieces.map((p) => (
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
  )
}
