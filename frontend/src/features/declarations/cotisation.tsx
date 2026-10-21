import { Link } from '@tanstack/react-router'
import { Receipt } from 'lucide-react'
import { type CotisationDsm, type Dsm } from '@/api/generated'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import {
  Table,
  TableBody,
  TableCell,
  TableFooter,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  formatDate,
  formatEuros,
  formatHectolitres,
  formatLitres,
  formatPourcentage,
} from './format'

/**
 * Onglet « Cotisation » d'une DSM (spec 05 §6.3) : calculée à la volée tant qu'elle n'est pas facturée, au
 * taux CVO en vigueur à la fin de la période (RG-DEC-31 à 33), avec la TVA et le TTC indicatifs (RG-DEC-34) ;
 * facturée, ses factures et avoirs.
 */
export function OngletCotisation({
  dsm,
  liens = false,
}: {
  dsm: Dsm
  /** Au back-office : les factures ouvrent leur pièce (spec 08 §6.1). */
  liens?: boolean
}) {
  const cotisation = dsm.cotisation
  switch (cotisation.statut) {
    case 'NON_CALCULABLE':
      return (
        <Alert variant='warning'>
          <Receipt />
          <AlertTitle>Cotisation non calculable</AlertTitle>
          <AlertDescription>
            Aucun taux CVO n'est en vigueur au{' '}
            {formatDate(cotisation.finPeriode)}, fin de la période. Le BNIA
            saisit les taux dans le Paramétrage, rubrique Cotisation.
          </AlertDescription>
        </Alert>
      )
    case 'FACTUREE':
      return <Factures cotisation={cotisation} liens={liens} />
    case 'CALCULEE':
      return (
        <Detail
          cotisation={cotisation}
          validee={dsm.etat === 'VALIDEE'}
          demandee={dsm.brouillonFacture != null}
        />
      )
  }
}

function Detail({
  cotisation,
  validee,
  demandee,
}: {
  cotisation: CotisationDsm
  validee: boolean
  /** RG-DEC-43 : la facture est demandée, pas encore émise. */
  demandee: boolean
}) {
  const taux = cotisation.taux
  return (
    <div className='space-y-4'>
      <div className='flex flex-wrap items-center gap-2 text-sm'>
        {demandee ? (
          <Badge variant='outline'>Facturation demandée</Badge>
        ) : validee ? (
          <Badge>À facturer</Badge>
        ) : (
          <Badge variant='outline'>Provisoire</Badge>
        )}
        {taux && (
          <span className='text-muted-foreground'>
            Taux appliqué : {formatEuros(taux.montant)}/hlAP, dont{' '}
            {formatPourcentage(taux.pourcentageExonere)} exonérés de TVA, en
            vigueur{' '}
            {taux.fin
              ? `du ${formatDate(taux.debut)} au ${formatDate(taux.fin)}`
              : `depuis le ${formatDate(taux.debut)}`}
            .
          </span>
        )}
      </div>
      {cotisation.lignes.length === 0 ? (
        <p className='text-muted-foreground text-sm'>
          Aucun volume dans l'assiette : la cotisation est nulle.
        </p>
      ) : (
        <div className='rounded-md border'>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Ligne de cotisation</TableHead>
                <TableHead className='text-right'>Volume (L AP)</TableHead>
                <TableHead className='text-right'>Volume (hlAP)</TableHead>
                <TableHead className='text-right'>
                  Part non assujettie
                </TableHead>
                <TableHead className='text-right'>Part assujettie</TableHead>
                <TableHead className='text-right'>Total HT</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {cotisation.lignes.map((l) => (
                <TableRow key={l.ligne}>
                  <TableCell>{l.libelle}</TableCell>
                  <TableCell className='text-right tabular-nums'>
                    {formatLitres(l.volume)}
                  </TableCell>
                  <TableCell className='text-right tabular-nums'>
                    {formatHectolitres(l.hectolitres)}
                  </TableCell>
                  <TableCell className='text-right tabular-nums'>
                    {formatEuros(l.nonAssujettie)}
                  </TableCell>
                  <TableCell className='text-right tabular-nums'>
                    {formatEuros(l.assujettie)}
                  </TableCell>
                  <TableCell className='text-right tabular-nums'>
                    {formatEuros(l.total)}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
            <TableFooter>
              <TableRow>
                <TableCell colSpan={3}>Total</TableCell>
                <TableCell className='text-right tabular-nums'>
                  {formatEuros(cotisation.totalNonAssujettie)}
                </TableCell>
                <TableCell className='text-right tabular-nums'>
                  {formatEuros(cotisation.totalAssujettie)}
                </TableCell>
                <TableCell className='text-right tabular-nums'>
                  {formatEuros(cotisation.totalHt)}
                </TableCell>
              </TableRow>
            </TableFooter>
          </Table>
        </div>
      )}
      <dl className='grid max-w-md grid-cols-[1fr_auto] gap-x-6 gap-y-1 text-sm'>
        <dt>Total HT</dt>
        <dd className='text-right font-medium tabular-nums'>
          {formatEuros(cotisation.totalHt)}
        </dd>
        {cotisation.tva ? (
          <>
            <dt>
              TVA indicative ({formatPourcentage(cotisation.tva.taux)} de la
              part assujettie)
            </dt>
            <dd className='text-right tabular-nums'>
              {formatEuros(cotisation.tva.montant)}
            </dd>
            <dt>TTC indicatif</dt>
            <dd className='text-right font-medium tabular-nums'>
              {formatEuros(cotisation.ttc)}
            </dd>
          </>
        ) : (
          <dt className='text-muted-foreground col-span-2'>
            TVA indicative non disponible : la classe de TVA de la CVO n'a pas
            de taux à la date du jour.
          </dt>
        )}
      </dl>
      <p className='text-muted-foreground text-sm'>
        Calculée à la grille en vigueur ; la facture émise par le BNIA la
        fixera.
      </p>
    </div>
  )
}

function Factures({
  cotisation,
  liens,
}: {
  cotisation: CotisationDsm
  liens: boolean
}) {
  return (
    <div className='space-y-4'>
      <Badge variant='secondary'>Facturée</Badge>
      <div className='rounded-md border'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Document</TableHead>
              <TableHead>Numéro</TableHead>
              <TableHead>Date</TableHead>
              <TableHead className='text-right'>HT</TableHead>
              <TableHead className='text-right'>TVA</TableHead>
              <TableHead className='text-right'>TTC</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {cotisation.factures.map((f) => (
              <TableRow key={`${f.type}-${f.numero}`}>
                <TableCell>
                  {f.type === 'FACTURE' ? 'Facture' : 'Avoir'}
                </TableCell>
                <TableCell className='font-mono'>
                  {liens ? (
                    <Link
                      to='/bo/facturation/$id'
                      params={{ id: String(f.piece) }}
                      className='underline-offset-4 hover:underline'
                    >
                      {f.numero}
                    </Link>
                  ) : (
                    f.numero
                  )}
                </TableCell>
                <TableCell>{formatDate(f.date)}</TableCell>
                <TableCell className='text-right tabular-nums'>
                  {f.type === 'AVOIR' && '− '}
                  {formatEuros(f.ht)}
                </TableCell>
                <TableCell className='text-right tabular-nums'>
                  {formatEuros(f.tva)}
                </TableCell>
                <TableCell className='text-right tabular-nums'>
                  {formatEuros(f.ttc)}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
          <TableFooter>
            <TableRow>
              <TableCell colSpan={3}>
                Total HT facturé (factures moins avoirs)
              </TableCell>
              <TableCell className='text-right tabular-nums'>
                {formatEuros(cotisation.totalHt)}
              </TableCell>
              <TableCell colSpan={2} />
            </TableRow>
          </TableFooter>
        </Table>
      </div>
    </div>
  )
}
