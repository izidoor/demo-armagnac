import {
  consulterContact,
  consulterContactPortail,
  consulterJournalContact,
  consulterJournalContactPortail,
  consulterOperateur,
  consulterPresentationOperateur,
  consulterPresentationPortail,
  consulterProfilPortail,
  creerContact,
  creerContactPortail,
  desactiverContact,
  desactiverContactPortail,
  designerContactsOperateur,
  designerContactsPortail,
  modifierContact,
  modifierContactPortail,
  modifierDiffusionOperateur,
  modifierDiffusionPortail,
  modifierDroitsContact,
  modifierDroitsContactPortail,
  modifierPresentationOperateur,
  modifierPresentationPortail,
  reactiverContact,
  reactiverContactPortail,
  supprimerContact,
  supprimerContactPortail,
  type PresentationOperateur,
  type SaisieContact,
  type SaisieDesignations,
  type SaisieDroits,
} from '@/api/generated'
import { type NiveauDiffusion } from './libelles'

/**
 * Où s'affiche la fiche : au back-office, le BNIA peut tout ; au portail, l'administrateur de
 * l'opérateur ne modifie que ce que RG-OPE-36 ouvre, et RG-OPE-30 protège le dernier administrateur.
 */
export type Espace = 'bo' | 'portail'

/** Clés de cache d'un opérateur, invalidées ensemble après une écriture. */
export const cleOperateur = (id: number) => ['operateur', id] as const

/** Les appels de la fiche, selon l'espace : mêmes écrans, endpoints du back-office ou du portail. */
export function apiOperateur(espace: Espace, id: number) {
  const bo = espace === 'bo'
  const path = { id }
  const contact = (c: number) => ({ operateur: id, contact: c })
  return {
    fiche: () =>
      (bo
        ? consulterOperateur({ path, throwOnError: true })
        : consulterProfilPortail({ path, throwOnError: true })
      ).then((r) => r.data),
    designer: (body: SaisieDesignations) =>
      bo
        ? designerContactsOperateur({ path, body, throwOnError: true })
        : designerContactsPortail({ path, body, throwOnError: true }),
    diffusion: (
      niveau: NiveauDiffusion,
      millesimes: number[],
      accesStatistiques: boolean
    ) =>
      bo
        ? modifierDiffusionOperateur({
            path,
            body: { niveau, millesimes, accesStatistiques },
            throwOnError: true,
          })
        : modifierDiffusionPortail({
            path,
            body: { niveau, millesimes },
            throwOnError: true,
          }),
    presentation: () =>
      (bo
        ? consulterPresentationOperateur({
            path: { operateur: id },
            throwOnError: true,
          })
        : consulterPresentationPortail({ path, throwOnError: true })
      ).then((r) => r.data),
    modifierPresentation: (body: PresentationOperateur) =>
      bo
        ? modifierPresentationOperateur({
            path: { operateur: id },
            body,
            throwOnError: true,
          })
        : modifierPresentationPortail({ path, body, throwOnError: true }),
    contact: (c: number) =>
      (bo
        ? consulterContact({ path: contact(c), throwOnError: true })
        : consulterContactPortail({ path: contact(c), throwOnError: true })
      ).then((r) => r.data),
    journal: (c: number) =>
      (bo
        ? consulterJournalContact({ path: contact(c), throwOnError: true })
        : consulterJournalContactPortail({
            path: contact(c),
            throwOnError: true,
          })
      ).then((r) => r.data),
    creerContact: (body: SaisieContact) =>
      (bo
        ? creerContact({ path: { operateur: id }, body, throwOnError: true })
        : creerContactPortail({
            path: { operateur: id },
            body,
            throwOnError: true,
          })
      ).then((r) => r.data),
    modifierContact: (c: number, body: SaisieContact) =>
      bo
        ? modifierContact({ path: contact(c), body, throwOnError: true })
        : modifierContactPortail({
            path: contact(c),
            body,
            throwOnError: true,
          }),
    modifierDroits: (c: number, body: SaisieDroits) =>
      bo
        ? modifierDroitsContact({ path: contact(c), body, throwOnError: true })
        : modifierDroitsContactPortail({
            path: contact(c),
            body,
            throwOnError: true,
          }),
    desactiverContact: (c: number) =>
      bo
        ? desactiverContact({ path: contact(c), throwOnError: true })
        : desactiverContactPortail({ path: contact(c), throwOnError: true }),
    reactiverContact: (c: number) =>
      bo
        ? reactiverContact({ path: contact(c), throwOnError: true })
        : reactiverContactPortail({ path: contact(c), throwOnError: true }),
    supprimerContact: (c: number) =>
      bo
        ? supprimerContact({ path: contact(c), throwOnError: true })
        : supprimerContactPortail({ path: contact(c), throwOnError: true }),
  }
}
