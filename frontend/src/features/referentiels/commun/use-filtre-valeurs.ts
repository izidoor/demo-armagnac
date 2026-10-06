import { useState } from 'react'
import { useDebounce } from '@/hooks/use-debounce'

export type FiltreValeurs = { actif?: boolean; recherche?: string }

export type Etat = 'tous' | 'actifs' | 'inactifs'

/** Filtre d'un listing : recherche (envoyée après une pause de frappe) et état actif. */
export function useFiltreValeurs() {
  const [recherche, setRecherche] = useState('')
  const [etat, setEtat] = useState<Etat>('actifs')
  const rechercheStable = useDebounce(recherche.trim())
  const filtre: FiltreValeurs = {
    actif: etat === 'tous' ? undefined : etat === 'actifs',
    recherche: rechercheStable || undefined,
  }
  return { filtre, recherche, setRecherche, etat, setEtat }
}
