import { Check, ChevronsUpDown } from 'lucide-react'
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

/** Choix de l'opérateur actif, dans l'en-tête (RG-OPE-49, spec 02 §6) ; absent pour qui n'en a qu'un. */
export function SelecteurOperateur() {
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
          className='max-w-40 sm:max-w-64'
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
            onClick={() =>
              r.operateur !== actif?.operateur && choix.mutate(r.operateur)
            }
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
