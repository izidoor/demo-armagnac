import { useQuery } from '@tanstack/react-query'
import { listerDoublonsOperateur } from '@/api/generated'
import { useDebounce } from '@/hooks/use-debounce'

type Criteres = {
  raisonSociale?: string
  cvi?: string
  siret?: string
  accise?: string
}

/**
 * RG-OPE-06, 08 : fiches qui portent déjà les valeurs saisies, cherchées pendant la frappe. Un
 * avertissement, jamais un blocage.
 *
 * @param sauf l'opérateur modifié, qui n'est pas son propre doublon
 */
export function useDoublons(criteres: Criteres, sauf?: number) {
  const stables = useDebounce(criteres, 400)
  const query = Object.fromEntries(
    Object.entries(stables).filter(([, v]) => v && v.trim().length >= 3)
  ) as Criteres
  const { data } = useQuery({
    queryKey: ['doublons', query, sauf],
    queryFn: () =>
      listerDoublonsOperateur({
        query: { ...query, sauf },
        throwOnError: true,
      }).then((r) => r.data),
    enabled: Object.keys(query).length > 0,
  })
  return data ?? []
}
