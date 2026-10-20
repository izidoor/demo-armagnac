import { AlarmClock } from 'lucide-react'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { formatDate } from './format'

/**
 * L'échéance d'une période (RG-DEC-39) et, passée, le retard (RG-DEC-40) : DSM absente, en brouillon ou en
 * compléments demandés le lendemain de l'échéance.
 */
export function Echeance({
  echeance,
  enRetard,
}: {
  echeance: string
  enRetard: boolean
}) {
  return (
    <span className='inline-flex items-center gap-2 whitespace-nowrap'>
      {formatDate(echeance)}
      {enRetard && (
        <Badge variant='danger'>
          <AlarmClock /> En retard
        </Badge>
      )}
    </span>
  )
}

/**
 * Bandeau du portail (spec 05 §6.2) : l'échéance de la période attendue, ou son retard.
 *
 * @param attendBnia la DSM est envoyée : l'opérateur n'a plus rien à faire avant l'échéance
 */
export function BandeauEcheance({
  periode,
  echeance,
  enRetard,
  attendBnia,
}: {
  periode: string
  echeance: string
  enRetard: boolean
  attendBnia: boolean
}) {
  if (attendBnia) return null
  return (
    <Alert variant={enRetard ? 'destructive' : 'default'}>
      <AlarmClock className='size-4' />
      <AlertTitle>
        {enRetard
          ? `DSM de ${periode} en retard`
          : `DSM de ${periode} à envoyer au plus tard le ${formatDate(echeance)}`}
      </AlertTitle>
      <AlertDescription>
        {enRetard
          ? `L'échéance du ${formatDate(echeance)} est passée : envoyez votre déclaration au BNIA dès que possible.`
          : "L'échéance est le 5 du mois qui suit la période pour un négociant, le 30 avril pour un producteur."}
      </AlertDescription>
    </Alert>
  )
}
