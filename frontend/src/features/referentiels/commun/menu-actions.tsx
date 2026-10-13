import { DotsHorizontalIcon } from '@radix-ui/react-icons'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useGestionParametrage } from './use-gestion-parametrage'

type MenuActionsProps = {
  actif: boolean
  onModifier: () => void
  onChangerEtat: () => void
  onSupprimer: () => void
}

/**
 * Actions d'une ligne : modifier, désactiver ou réactiver, supprimer si rien ne l'utilise (RG-REF-04) ;
 * absentes sans la gestion du module Paramétrage (RG-OPE-31).
 */
export function MenuActions({
  actif,
  onModifier,
  onChangerEtat,
  onSupprimer,
}: MenuActionsProps) {
  const gestion = useGestionParametrage()
  if (!gestion) return null
  return (
    <DropdownMenu modal={false}>
      <DropdownMenuTrigger asChild>
        <Button variant='ghost' size='icon' className='size-8'>
          <DotsHorizontalIcon />
          <span className='sr-only'>Actions</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align='end'>
        <DropdownMenuItem onClick={onModifier}>Modifier</DropdownMenuItem>
        <DropdownMenuItem onClick={onChangerEtat}>
          {actif ? 'Désactiver' : 'Réactiver'}
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuItem variant='destructive' onClick={onSupprimer}>
          Supprimer
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
