import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useSearch } from '@tanstack/react-router'
import { useDebounce } from './use-debounce'

type Filtres = Record<string, string | boolean>

/**
 * L'onglet et les filtres d'une liste, gardés dans l'adresse : revenu du détail d'un élément, on retrouve la liste
 * comme on l'a laissée (onglet, filtres, et défilement, que le routeur restaure). L'adresse est remplacée, sans
 * nouvelle entrée d'historique ni retour en haut de page ; une valeur par défaut n'y figure pas. La route déclare
 * chaque clé dans son `validateSearch`, sans quoi elle est écartée.
 */
export function useFiltresAdresse<F extends Filtres>(
  defauts: F
): [F, (changement: Partial<F>) => void] {
  const adresse = useSearch({ strict: false }) as Record<string, unknown>
  const navigate = useNavigate()
  const filtres = Object.fromEntries(
    Object.entries(defauts).map(([cle, defaut]) => [
      cle,
      adresse[cle] ?? defaut,
    ])
  ) as F
  const cles = JSON.stringify(defauts)
  const changer = useCallback(
    (changement: Partial<F>) => {
      const parDefaut = JSON.parse(cles) as F
      navigate({
        to: '.',
        search: (avant: Record<string, unknown>) => {
          const apres: Record<string, unknown> = { ...avant, ...changement }
          for (const [cle, valeur] of Object.entries(changement))
            if (valeur === parDefaut[cle] || valeur === '') delete apres[cle]
          return apres
        },
        replace: true,
        resetScroll: false,
      } as never)
    },
    [navigate, cles]
  )
  return [filtres, changer]
}

/**
 * Un champ de recherche dont la valeur est gardée dans l'adresse (`useFiltresAdresse`) : la saisie reste locale, et
 * n'est écrite dans l'adresse qu'une fois la frappe arrêtée. Rend le texte saisi, son modificateur, et le texte stable,
 * celui qui filtre.
 */
export function useTexteAdresse(
  valeur: string,
  enregistrer: (texte: string) => void
): [string, (texte: string) => void, string] {
  const [texte, setTexte] = useState(valeur)
  const stable = useDebounce(texte)
  useEffect(() => {
    if (stable !== valeur) enregistrer(stable)
  }, [stable, valeur, enregistrer])
  return [texte, setTexte, stable]
}
