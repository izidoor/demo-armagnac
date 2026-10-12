import { Badge } from '@/components/ui/badge'

/** Fond clair en mode clair, foncé en mode sombre ; texte contrasté dans les deux. */
export function BadgeEtat({ actif }: { actif: boolean }) {
  return actif ? (
    <Badge className='bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300'>
      Actif
    </Badge>
  ) : (
    <Badge className='bg-rose-100 text-rose-800 dark:bg-rose-950 dark:text-rose-300'>
      Inactif
    </Badge>
  )
}
