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
      queryClient.clear()
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
