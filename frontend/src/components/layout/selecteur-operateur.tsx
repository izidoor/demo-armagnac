import { Check, ChevronsUpDown } from 'lucide-react'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useMoi } from '@/features/session/session'
import { useChoisirOperateur } from '@/features/session/use-choisir-operateur'

/**
 * Choix de l'opérateur actif (RG-OPE-49, spec 02 §6) : dans l'en-tête, et sur mobile en tête du tiroir de navigation ;
 * absent pour qui n'en a qu'un.
 */
export function SelecteurOperateur({
  className,
  apresChoix,
}: {
  className?: string
  /** Appelé une fois l'autre opérateur ouvert : le tiroir de navigation se ferme (plus tôt, il démonterait le sélecteur avant la fin du changement). */
  apresChoix?: () => void
}) {
  const { moi, actif } = useMoi()
  const choix = useChoisirOperateur()
  const utilisables = moi.rattachements.filter((r) => r.utilisable)
  if (utilisables.length <= 1) return null

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant='outline'
          size='sm'
          className={cn('max-w-40 sm:max-w-64', className)}
          disabled={choix.isPending}
        >
          <span className='truncate text-xs'>
            {actif?.raisonSociale ?? 'Choisir un opérateur'}
          </span>
          <ChevronsUpDown className='size-4' />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className='min-w-64' align='end'>
        <DropdownMenuLabel className='text-muted-foreground text-xs'>
          Opérateur actif
        </DropdownMenuLabel>
        {utilisables.map((r) => (
          <DropdownMenuItem
            key={r.id}
            onClick={() => {
              if (r.operateur === actif?.operateur) return
              choix.mutate(r.operateur, { onSuccess: apresChoix })
            }}
          >
            <span className='grid flex-1 leading-tight'>
              <span>{r.raisonSociale}</span>
              <span className='text-muted-foreground text-xs'>
                {r.type === 'BNIA' ? 'Back-office' : 'Portail'}
              </span>
            </span>
            {r.operateur === actif?.operateur && <Check />}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
