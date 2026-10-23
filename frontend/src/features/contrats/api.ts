import { useQuery } from '@tanstack/react-query'
import {
  annulerContrat,
  approuverContrat,
  cloturerContrat,
  consulterContrat,
  consulterMercuriales,
  consulterMonContrat,
  consulterStockPourContrat,
  declarerRetiraison,
  listerContrats,
  listerMesContrats,
  modifierContrat,
  modifierRetiraison,
  ouvrirContrat,
  refuserContrat,
  retirerRetiraison,
  validerRetiraison,
  type SaisieContenuContrat,
  type SaisieContrat,
  type SaisieRetiraison,
} from '@/api/generated'

/**
 * Où s'affiche un contrat : au back-office (le BNIA, en lecture) ou au portail (une partie, par son opérateur actif).
 */
export type Espace = 'bo' | 'portail'

/** Clé de cache des contrats, invalidée après chaque écriture (listes, détail, cartes des tableaux de bord). */
export const cleContrats = ['contrats'] as const

/** Les contrats de l'opérateur actif, au portail, chacun rangé dans son onglet (RG-CA-25). */
export function useMesContrats(operateur: number) {
  return useQuery({
    queryKey: [...cleContrats, 'portail', operateur],
    queryFn: () =>
      listerMesContrats({ path: { operateur }, throwOnError: true }).then(
        (r) => r.data
      ),
  })
}

/** Tous les contrats, au back-office ; filtrés par opérateur partie (fiche opérateur) et par numéro. */
export function useContratsBnia(filtre: {
  operateur?: number
  numero?: string
}) {
  return useQuery({
    queryKey: [...cleContrats, 'bo', filtre],
    queryFn: () =>
      listerContrats({ query: filtre, throwOnError: true }).then((r) => r.data),
  })
}

/** Un contrat : au portail par l'opérateur actif, au back-office en lecture. */
export function useContrat(espace: Espace, operateur: number, id: number) {
  return useQuery({
    queryKey: [...cleContrats, espace, operateur, id],
    queryFn: () =>
      (espace === 'bo'
        ? consulterContrat({ path: { id }, throwOnError: true })
        : consulterMonContrat({ path: { operateur, id }, throwOnError: true })
      ).then((r) => r.data),
  })
}

/** RG-CA-26 : les mercuriales d'une période, au back-office ; par défaut, les 12 derniers mois. */
export function useMercuriales(du?: string, au?: string) {
  return useQuery({
    queryKey: [...cleContrats, 'mercuriales', du, au],
    queryFn: () =>
      consulterMercuriales({ query: { du, au }, throwOnError: true }).then(
        (r) => r.data
      ),
  })
}

/** RG-CA-20 : l'adresse du PDF d'un contrat, au portail (l'édition entre au journal) ou au back-office. */
export const urlPdf = (espace: Espace, operateur: number, id: number) =>
  espace === 'bo'
    ? `/api/contrats/${id}/pdf`
    : `/api/operateurs/${operateur}/contrats/${id}/pdf`

/** RG-CA-11, 12 : le stock estimé sous bois de l'opérateur actif, où le vendeur choisit ses lignes. */
export function useStockPourContrat(operateur: number, actif: boolean) {
  return useQuery({
    queryKey: [...cleContrats, 'stock', operateur],
    queryFn: () =>
      consulterStockPourContrat({
        path: { operateur },
        throwOnError: true,
      }).then((r) => r.data),
    enabled: actif,
  })
}

/** Les écritures d'un contrat au portail (UC-CA-1 à 5). */
export function apiContrats(operateur: number) {
  return {
    ouvrir: (body: SaisieContrat) =>
      ouvrirContrat({ path: { operateur }, body, throwOnError: true }).then(
        (r) => r.data
      ),
    modifier: (id: number, versionVue: number, contenu: SaisieContenuContrat) =>
      modifierContrat({
        path: { operateur, id },
        body: { versionVue, contenu },
        throwOnError: true,
      }).then((r) => r.data),
    approuver: (id: number, versionVue: number) =>
      approuverContrat({
        path: { operateur, id },
        body: { versionVue },
        throwOnError: true,
      }).then((r) => r.data),
    refuser: (id: number, versionVue: number) =>
      refuserContrat({
        path: { operateur, id },
        body: { versionVue },
        throwOnError: true,
      }).then((r) => r.data),
    declarerRetiraison: (id: number, body: SaisieRetiraison) =>
      declarerRetiraison({
        path: { operateur, id },
        body,
        throwOnError: true,
      }).then((r) => r.data),
    modifierRetiraison: (
      id: number,
      numero: number,
      versionVue: number,
      contenu: SaisieRetiraison
    ) =>
      modifierRetiraison({
        path: { operateur, id, numero },
        body: { versionVue, contenu },
        throwOnError: true,
      }).then((r) => r.data),
    validerRetiraison: (id: number, numero: number, versionVue: number) =>
      validerRetiraison({
        path: { operateur, id, numero },
        body: { versionVue },
        throwOnError: true,
      }).then((r) => r.data),
    retirerRetiraison: (id: number, numero: number) =>
      retirerRetiraison({
        path: { operateur, id, numero },
        throwOnError: true,
      }).then((r) => r.data),
    annuler: (id: number) =>
      annulerContrat({ path: { operateur, id }, throwOnError: true }).then(
        (r) => r.data
      ),
    cloturer: (id: number) =>
      cloturerContrat({ path: { operateur, id }, throwOnError: true }).then(
        (r) => r.data
      ),
  }
}
