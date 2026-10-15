import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { type PresentationOperateur } from '@/api/generated'
import { lireProbleme } from '@/lib/probleme'
import { apiOperateur } from '../commun/api'

/**
 * État partagé des cadres de présentation (marques, circuits et labels dans Production ; Œnotourisme) : un
 * seul agrégat, enregistré d'un bloc (ADR-012). Le brouillon n'existe qu'entre la première modification et
 * l'enregistrement ; un cadre annulé reprend ses champs enregistrés, sans toucher aux autres. Un seul cadre
 * de présentation en édition à la fois, d'un onglet à l'autre : son enregistrement porte tout l'agrégat.
 */
export function usePresentation(id: number) {
  const queryClient = useQueryClient()
  const api = apiOperateur(id)
  const cle = ['presentation', id]
  const requete = useQuery({ queryKey: cle, queryFn: api.presentation })
  const [brouillon, setBrouillon] = useState<PresentationOperateur | null>(null)
  const [erreur, setErreur] = useState<string | null>(null)
  const [cadre, setCadre] = useState<string | null>(null)
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
    enregistrer: (apres?: () => void) =>
      valeur && mutation.mutate(valeur, { onSuccess: () => apres?.() }),
    /** Les champs d'un cadre reprennent leur valeur enregistrée. */
    retablir: (champs: (keyof PresentationOperateur)[]) => {
      setErreur(null)
      const enregistree = requete.data
      if (!brouillon || !enregistree) return
      setBrouillon({
        ...brouillon,
        ...Object.fromEntries(champs.map((c) => [c, enregistree[c]])),
      })
    },
    enCours: mutation.isPending,
    erreur,
    /** Le cadre de présentation en édition. */
    cadre,
    editer: (c: string | null) => setCadre(c),
  }
}
