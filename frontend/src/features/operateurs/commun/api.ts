import { useQuery } from '@tanstack/react-query'
import {
  consulterAnnuaire,
  consulterOperateur,
  consulterPresentationOperateur,
  designerContactsOperateur,
  modifierDiffusionOperateur,
  modifierPresentationOperateur,
  type PresentationOperateur,
  type SaisieDesignations,
} from '@/api/generated'
import { type NiveauDiffusion } from './libelles'

/**
 * Où s'affiche la fiche : au back-office (opérateur actif BNIA) ou au portail (« Mon entreprise »). Les
 * écrans sont les mêmes ; ce que l'on peut y faire vient des droits renvoyés par l'API (ADR-016),
 * l'espace ne décide que des liens.
 */
export type Espace = 'bo' | 'portail'

/** Clés de cache d'un opérateur, invalidées ensemble après une écriture. */
export const cleOperateur = (id: number) => ['operateur', id] as const

/** Les appels de la fiche d'un opérateur. */
export function apiOperateur(id: number) {
  const path = { id }
  return {
    fiche: () =>
      consulterOperateur({ path, throwOnError: true }).then((r) => r.data),
    designer: (body: SaisieDesignations) =>
      designerContactsOperateur({ path, body, throwOnError: true }),
    diffusion: (
      niveau: NiveauDiffusion,
      millesimes: number[],
      accesStatistiques: boolean,
      version: number
    ) =>
      modifierDiffusionOperateur({
        path,
        body: { niveau, millesimes, accesStatistiques, version },
        throwOnError: true,
      }),
    presentation: () =>
      consulterPresentationOperateur({
        path: { operateur: id },
        throwOnError: true,
      }).then((r) => r.data),
    modifierPresentation: (body: PresentationOperateur) =>
      modifierPresentationOperateur({
        path: { operateur: id },
        body,
        throwOnError: true,
      }),
  }
}

/** L'annuaire d'un opérateur : contacts, membres et personnes nouvelles invitées (RG-OPE-52). */
export function useAnnuaire(id: number) {
  return useQuery({
    queryKey: [...cleOperateur(id), 'annuaire'],
    queryFn: () =>
      consulterAnnuaire({ path: { id }, throwOnError: true }).then(
        (r) => r.data
      ),
  })
}
