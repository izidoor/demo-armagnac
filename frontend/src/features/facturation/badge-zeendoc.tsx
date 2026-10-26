import { CircleCheck, Clock, Send } from 'lucide-react'
import { type Piece } from '@/api/generated'
import { cn } from '@/lib/utils'
import { Badge } from '@/components/ui/badge'
import { formatDate, formatDateHeure } from '@/features/declarations/format'
import { COULEUR_ZEENDOC, ETATS_PORTAIL, ETATS_ZEENDOC } from './libelles'

/**
 * RG-FAC-32, 33 : l'état d'une pièce émise dans Zeendoc, « Transmis » ou « Payé » ; rien tant qu'elle n'est pas
 * transmise. Avec les dates en infobulle, ou à côté (`avecDates`). Au portail (`portail`), sans le nom de Zeendoc :
 * « À payer » (une horloge) ou « Payé ».
 */
export function BadgeZeendoc({
  piece,
  avecDates = false,
  portail = false,
}: {
  piece: Pick<Piece, 'zeendoc' | 'transmiseLe' | 'payeeLe'>
  avecDates?: boolean
  portail?: boolean
}) {
  if (!piece.zeendoc) return null
  const dates = [
    piece.transmiseLe && `transmis le ${formatDateHeure(piece.transmiseLe)}`,
    piece.payeeLe && `payé le ${formatDate(piece.payeeLe)}`,
  ]
    .filter(Boolean)
    .join(', ')
  const infobulle = portail ? dates : `Zeendoc : ${dates}`
  return (
    <span className='inline-flex items-center gap-2'>
      <Badge
        className={cn('border-transparent', COULEUR_ZEENDOC[piece.zeendoc])}
        title={infobulle}
      >
        {piece.zeendoc === 'PAYEE' ? (
          <CircleCheck />
        ) : portail ? (
          <Clock />
        ) : (
          <Send />
        )}
        {(portail ? ETATS_PORTAIL : ETATS_ZEENDOC)[piece.zeendoc]}
      </Badge>
      {avecDates && (
        <span className='text-muted-foreground text-xs'>{infobulle}</span>
      )}
    </span>
  )
}
