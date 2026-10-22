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

/** Le premier segment d'un chemin : l'espace (`bo`, `portail`), hors duquel le retour ne remonte pas (connexion). */
const espace = (chemin: string) => chemin.split('/')[1]

/**
 * La flèche de retour d'une page de second niveau, dans l'en-tête : la page précédente, comme le bouton retour du
 * téléphone (de la fiche d'un opérateur ouverte depuis une DSM, on revient à la DSM). Sans page précédente dans
 * l'application et dans le même espace (page ouverte par une adresse, après la connexion), on ouvre la page parente.
 */
export function useRetour(parente: NavigateOptions) {
  const router = useRouter()
  return () => {
    const { pathname, state } = router.history.location
    const precedente = chemins.get(state.__TSR_index - 1)
    if (precedente && espace(precedente) === espace(pathname))
      router.history.back()
    else router.navigate(parente)
  }
}

/**
 * Le lien vers la page parente (« ← Opérateurs », sous le titre, sur ordinateur) : il mène à la parente. Si la page
 * précédente est la parente, on y revient en arrière, pour retrouver son historique ; sinon on l'ouvre.
 */
export function useVersParente(parente: NavigateOptions) {
  const router = useRouter()
  return () => {
    const rang = router.history.location.state.__TSR_index
    if (chemins.get(rang - 1) === router.buildLocation(parente).pathname)
      router.history.back()
    else router.navigate(parente)
  }
}
