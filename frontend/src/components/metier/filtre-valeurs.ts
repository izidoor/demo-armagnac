import { type Row } from '@tanstack/react-table'

/**
 * Filtre de colonne de TanStack Table, pour `EnteteFiltrable` : la valeur de la ligne est l'une des valeurs
 * cochées ; rien de coché, tout passe.
 */
export function filtreValeurs<T>(row: Row<T>, id: string, retenues: string[]) {
  return !retenues?.length || retenues.includes(String(row.getValue(id) ?? ''))
}
