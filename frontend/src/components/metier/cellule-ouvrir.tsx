import { ChevronRight } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { TableCell, TableHead } from '@/components/ui/table'

/** L'en-tête, vide, de la colonne du chevron. */
export function EnteteOuvrir() {
  return <TableHead className='w-0' />
}

/**
 * La dernière cellule d'une ligne qui ouvre un détail : un chevron seul, comme sur toutes les tables (la ligne
 * entière reste cliquable) ; son libellé, pour les lecteurs d'écran, dit ce qu'il ouvre.
 */
export function CelluleOuvrir({
  libelle,
  onOuvrir,
}: {
  libelle: string
  onOuvrir: () => void
}) {
  return (
    <TableCell className='w-0'>
      <Button
        variant='ghost'
        size='sm'
        aria-label={libelle}
        onClick={(e) => {
          e.stopPropagation()
          onOuvrir()
        }}
      >
        <ChevronRight className='size-4' />
      </Button>
    </TableCell>
  )
}
