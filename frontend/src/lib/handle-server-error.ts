import { toast } from 'sonner'
import { lireProbleme } from '@/lib/probleme'

export function handleServerError(error: unknown) {
  // eslint-disable-next-line no-console
  console.log(error)

  const probleme = lireProbleme(error)
  toast.error(probleme.titre, {
    description: probleme.regle
      ? `${probleme.detail} (${probleme.regle})`
      : probleme.detail,
  })
}
