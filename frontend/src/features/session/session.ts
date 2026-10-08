import { AxiosError } from 'axios'
import {
  queryOptions,
  useQuery,
  useSuspenseQuery,
  type QueryClient,
} from '@tanstack/react-query'
import { redirect } from '@tanstack/react-router'
import { consulterMesDroits, consulterMoi, type Moi } from '@/api/generated'

/** Un rattachement de l'utilisateur connecté (RG-OPE-49). */
export type Rattachement = Moi['rattachements'][number]

/** Espace de l'application : back-office si l'opérateur actif est le BNIA, portail sinon (RG-OPE-49). */
type EspaceActif = 'bo' | 'portail'

/** L'utilisateur connecté, ses rattachements et son opérateur actif ; 401 sans session. */
export const requeteMoi = queryOptions({
  queryKey: ['moi'],
  queryFn: () => consulterMoi({ throwOnError: true }).then((r) => r.data),
  staleTime: 60 * 1000,
  retry: false,
})

function rattachementActif(moi: Moi): Rattachement | undefined {
  return moi.rattachements.find(
    (r) => r.operateur === moi.operateurOuvert && r.utilisable
  )
}

export function espaceDe(rattachement: Rattachement): EspaceActif {
  return rattachement.type === 'BNIA' ? 'bo' : 'portail'
}

/**
 * Garde d'une route : sans session, la connexion (RG-AUT-01) ; sans opérateur actif, son choix
 * (RG-OPE-49) ; dans le mauvais espace, l'espace de l'opérateur actif.
 */
export async function exigerSession(
  queryClient: QueryClient,
  href: string,
  espace?: EspaceActif
) {
  let moi: Moi
  try {
    moi = await queryClient.ensureQueryData(requeteMoi)
  } catch (erreur) {
    if (erreur instanceof AxiosError && erreur.response?.status === 401) {
      throw redirect({ to: '/connexion', search: { redirect: href } })
    }
    throw erreur
  }
  const actif = rattachementActif(moi)
  if (!actif) throw redirect({ to: '/choix-operateur' })
  if (espace && espaceDe(actif) !== espace) {
    throw redirect({ to: espaceDe(actif) === 'bo' ? '/bo' : '/portail' })
  }
  return { moi, actif }
}

/** L'utilisateur connecté, dans une route gardée par {@link exigerSession}. */
export function useMoi() {
  const { data } = useSuspenseQuery(requeteMoi)
  return { moi: data, actif: rattachementActif(data) }
}

/** Les droits de l'utilisateur connecté sur un opérateur, évalués par le serveur (RG-OPE-31, 32). */
export function useDroits(operateur: number) {
  return useQuery({
    queryKey: ['droits', operateur],
    queryFn: () =>
      consulterMesDroits({ path: { id: operateur }, throwOnError: true }).then(
        (r) => r.data
      ),
  }).data
}
