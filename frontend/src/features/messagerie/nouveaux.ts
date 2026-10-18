import { useQuery } from '@tanstack/react-query'
import { apiMessagerie, cleListesMessagerie } from './api'

const entier = new Intl.NumberFormat('fr-FR')

/** « 3 nouveaux messages » (RG-MSG-11). */
export function libelleNouveaux(nombre: number) {
  return nombre === 0
    ? 'Aucun nouveau message'
    : `${entier.format(nombre)} ${nombre === 1 ? 'nouveau message' : 'nouveaux messages'}`
}

/** Le nombre de nouveaux messages, partagé par la carte et le menu (RG-MSG-11). */
export function useNouveauxMessages(operateur?: number) {
  return useQuery({
    queryKey: [...cleListesMessagerie, 'nouveaux', operateur ?? 'bnia'],
    queryFn: () => apiMessagerie.nouveaux(operateur),
  })
}
