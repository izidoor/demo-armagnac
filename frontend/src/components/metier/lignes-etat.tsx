import { type UseQueryResult } from '@tanstack/react-query'
import { TableCell, TableRow } from '@/components/ui/table'

type LignesEtatProps = {
  requete: UseQueryResult<unknown[]>
  colonnes: number
}

/** Ligne de chargement, d'erreur ou de liste vide d'un tableau. */
export function LignesEtat({ requete, colonnes }: LignesEtatProps) {
  let message: string | null = null
  if (requete.isPending) message = 'Chargement…'
  else if (requete.isError) message = 'Impossible de charger la liste.'
  else if (requete.data?.length === 0) message = 'Aucun élément.'
  if (!message) return null
  return (
    <TableRow>
      <TableCell
        colSpan={colonnes}
        className='text-muted-foreground h-16 text-center'
      >
        {message}
      </TableCell>
    </TableRow>
  )
}
