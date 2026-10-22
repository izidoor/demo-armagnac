import {
  type AnyRouter,
  type NavigateOptions,
  useRouter,
} from '@tanstack/react-router'

/** Le chemin de chaque entrée de l'historique, par son rang : le retour sait ce qui précède la page affichée. */
const chemins = new Map<number, string>()

/** À chaque écran affiché (`main.tsx`), retenir le chemin de son entrée d'historique. */
export function retenirEntree(router: AnyRouter) {
  const { pathname, state } = router.history.location
  chemins.set(state.__TSR_index, pathname)
}

/**
 * Le retour d'une page de second niveau vers sa page parente (de la fiche d'un opérateur à la liste) : si la page
 * précédente dans l'historique est la parente, on y revient en arrière, comme le bouton retour du téléphone ; sinon
 * (page ouverte par un lien, une recherche, une adresse), on ouvre la parente.
 */
export function useRetour(parente: NavigateOptions) {
  const router = useRouter()
  return () => {
    const rang = router.history.location.state.__TSR_index
    if (chemins.get(rang - 1) === router.buildLocation(parente).pathname)
      router.history.back()
    else router.navigate(parente)
  }
}
