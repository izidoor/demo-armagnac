import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { type PresentationOperateur } from '@/api/generated'
import { lireProbleme } from '@/lib/probleme'
import { apiOperateur } from '../commun/api'

/**
 * État partagé des trois onglets de présentation (Marques, Visites et labels, Œnotourisme) : un seul
 * agrégat, enregistré d'un bloc (ADR-012). Le brouillon n'existe qu'entre la première modification et
 * l'enregistrement.
 */
export function usePresentation(id: number) {
  const queryClient = useQueryClient()
  const api = apiOperateur(id)
  const cle = ['presentation', id]
  const requete = useQuery({ queryKey: cle, queryFn: api.presentation })
  const [brouillon, setBrouillon] = useState<PresentationOperateur | null>(null)
  const [erreur, setErreur] = useState<string | null>(null)
  const valeur = brouillon ?? requete.data ?? null
  const mutation = useMutation({
    mutationFn: (p: PresentationOperateur) => api.modifierPresentation(p),
    meta: { erreurAffichee: true },
    onSuccess: (reponse) => {
      setErreur(null)
      queryClient.setQueryData(cle, reponse.data)
      setBrouillon(null)
    },
    onError: (error) => {
      const p = lireProbleme(error)
      setErreur(p.regle ? `${p.detail} (${p.regle})` : (p.detail ?? p.titre))
    },
  })
  return {
    valeur,
    modifier: (changement: Partial<PresentationOperateur>) =>
      valeur && setBrouillon({ ...valeur, ...changement }),
    enregistrer: () => valeur && mutation.mutate(valeur),
    enCours: mutation.isPending,
    erreur,
  }
}
