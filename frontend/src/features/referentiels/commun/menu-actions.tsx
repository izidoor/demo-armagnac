import { DotsHorizontalIcon } from '@radix-ui/react-icons'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

type MenuActionsProps = {
  actif: boolean
  onModifier: () => void
  onChangerEtat: () => void
}

/** Actions d'une ligne : modifier, désactiver ou réactiver (RG-REF-04 : jamais supprimer). */
export function MenuActions({
  actif,
  onModifier,
  onChangerEtat,
}: MenuActionsProps) {
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
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
