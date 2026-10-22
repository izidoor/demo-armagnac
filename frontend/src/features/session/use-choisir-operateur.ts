import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'
import { choisirOperateurActif } from '@/api/generated'
import { espaceDe, requeteMoi } from './session'

/**
 * Changer d'opérateur actif (RG-OPE-49) : les données en cache sont oubliées, puis l'espace qui lui
 * correspond s'ouvre, back-office pour le BNIA, portail sinon.
 */
export function useChoisirOperateur() {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  return useMutation({
    mutationFn: (operateur: number) =>
      choisirOperateurActif({ body: { operateur }, throwOnError: true }),
    onSuccess: async (r) => {
      // « moi » est mis à jour sur place, pas vidé avec le reste : ses abonnés (en-tête, tableau de bord) restent
      // attachés au cache et passent au nouvel opérateur. Après un clear(), ils gardaient l'ancien jusqu'au
      // rechargement de la page.
      queryClient.removeQueries({
        predicate: (q) => q.queryKey[0] !== requeteMoi.queryKey[0],
      })
      queryClient.setQueryData(requeteMoi.queryKey, r.data)
      const nouveau = r.data.rattachements.find(
        (x) => x.operateur === r.data.operateurOuvert
      )
      await navigate({
        to: nouveau && espaceDe(nouveau) === 'bo' ? '/bo' : '/portail',
      })
    },
  })
}
