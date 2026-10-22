import { DotsHorizontalIcon } from '@radix-ui/react-icons'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

/**
 * Les colonnes d'un historique de taux (classes de TVA, taux CVO), selon la largeur du contenu : sous 44rem, les
 * colonnes secondaires (`SECONDAIRE`) passent sous le taux (`SOUS_LE_TAUX`), et les actions dans un menu.
 */
export const SECONDAIRE = '@max-[44rem]/content:hidden'
export const SOUS_LE_TAUX = '@min-[44rem]/content:hidden'
export const COLONNE_ACTIONS = 'w-48 @max-[44rem]/content:w-auto'

/** Modifier ou retirer un taux : deux boutons sur un contenu large, un menu « ⋯ » sur un contenu étroit. */
export function ActionsHistorique({
  onModifier,
  onRetirer,
  retirer,
}: {
  onModifier: () => void
  onRetirer: () => void
  /** Le libellé du retrait : « Retirer », « Supprimer ». */
  retirer: string
}) {
  return (
    <>
      <span className={SECONDAIRE}>
        <Button variant='ghost' size='sm' onClick={onModifier}>
          Modifier
        </Button>
        <Button variant='ghost' size='sm' onClick={onRetirer}>
          {retirer}
        </Button>
      </span>
      <DropdownMenu modal={false}>
        <DropdownMenuTrigger asChild>
          <Button
            variant='ghost'
            size='icon'
            className={`size-8 ${SOUS_LE_TAUX}`}
          >
            <DotsHorizontalIcon />
            <span className='sr-only'>Actions</span>
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align='end'>
          <DropdownMenuItem onClick={onModifier}>Modifier</DropdownMenuItem>
          <DropdownMenuItem variant='destructive' onClick={onRetirer}>
            {retirer}
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </>
  )
}
