import { Badge } from '@/components/ui/badge'

export function BadgeEtat({ actif }: { actif: boolean }) {
  return actif ? (
    <Badge variant='secondary'>Actif</Badge>
  ) : (
    <Badge variant='outline' className='text-muted-foreground'>
      Inactif
    </Badge>
  )
}
