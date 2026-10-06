import { useQuery } from '@tanstack/react-query'
import { consulterOperateurDemoPortail } from '@/api/generated'

/**
 * L'opérateur ouvert au portail. Tant que le contexte acces n'existe pas, c'est l'opérateur fixe de
 * démonstration (décision du 2026-10-06) ; absent hors profil demo.
 */
export function useOperateurPortail() {
  const requete = useQuery({
    queryKey: ['operateur-demo'],
    queryFn: () =>
      consulterOperateurDemoPortail({ throwOnError: true }).then((r) => r.data),
    retry: false,
    staleTime: Infinity,
  })
  return {
    operateur: requete.data,
    chargement: requete.isPending,
    absent: requete.isError,
  }
}
