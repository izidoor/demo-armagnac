import { useState } from 'react'

/**
 * Pagination côté front d'une liste déjà chargée. La page courante est ramenée dans les bornes quand la
 * liste raccourcit (filtre, suppression).
 */
export function usePagination<T>(lignes: T[] | undefined, taille = 10) {
  const [demandee, setPage] = useState(1)
  const total = lignes?.length ?? 0
  const pages = Math.max(1, Math.ceil(total / taille))
  const page = Math.min(demandee, pages)
  return {
    lignes: lignes?.slice((page - 1) * taille, page * taille),
    pagination: { page, pages, total, setPage },
  }
}
