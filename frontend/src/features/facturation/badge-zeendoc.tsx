import { CircleCheck, Send } from 'lucide-react'
import { type Piece } from '@/api/generated'
import { Badge } from '@/components/ui/badge'
import { formatDate, formatDateHeure } from '@/features/declarations/format'
import { ETATS_ZEENDOC, VARIANTE_ZEENDOC } from './libelles'

/**
 * RG-FAC-32, 33 : l'état d'une pièce émise dans Zeendoc, « Transmis » ou « Payé » ; rien tant qu'elle n'est pas
 * transmise. Avec les dates en infobulle, ou à côté (`avecDates`).
 */
export function BadgeZeendoc({
  piece,
  avecDates = false,
}: {
  piece: Pick<Piece, 'zeendoc' | 'transmiseLe' | 'payeeLe'>
  avecDates?: boolean
}) {
  if (!piece.zeendoc) return null
  const dates = [
    piece.transmiseLe && `transmis le ${formatDateHeure(piece.transmiseLe)}`,
    piece.payeeLe && `payé le ${formatDate(piece.payeeLe)}`,
  ]
    .filter(Boolean)
    .join(', ')
  return (
    <span className='inline-flex items-center gap-2'>
      <Badge
        variant={VARIANTE_ZEENDOC[piece.zeendoc]}
        title={`Zeendoc : ${dates}`}
      >
        {piece.zeendoc === 'PAYEE' ? <CircleCheck /> : <Send />}
        {ETATS_ZEENDOC[piece.zeendoc]}
      </Badge>
      {avecDates && (
        <span className='text-muted-foreground text-xs'>Zeendoc : {dates}</span>
      )}
    </span>
  )
}
