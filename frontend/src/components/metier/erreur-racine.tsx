import { type FieldErrors } from 'react-hook-form'
import { AlertCircle } from 'lucide-react'
import { Alert, AlertDescription } from '@/components/ui/alert'

/** Erreur générale d'un formulaire, celle qui ne vise aucun champ. */
export function ErreurRacine({ errors }: { errors: FieldErrors }) {
  if (!errors.root?.message) return null
  return (
    <Alert variant='destructive'>
      <AlertCircle />
      <AlertDescription>{errors.root.message}</AlertDescription>
    </Alert>
  )
}
