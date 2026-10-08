import { useQuery } from '@tanstack/react-query'
import { consulterSession } from '@/api/generated'

/**
 * État de la session ; l'appel pose aussi le cookie du jeton CSRF, nécessaire avant toute écriture
 * (ADR-015) : chaque écran public l'appelle à son ouverture.
 */
export function useEtatSession() {
  return useQuery({
    queryKey: ['session'],
    queryFn: () => consulterSession({ throwOnError: true }).then((r) => r.data),
    staleTime: 0,
  })
}
