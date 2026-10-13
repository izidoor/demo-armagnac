import { useQuery } from '@tanstack/react-query'
import {
  apercevoirDsm,
  consulterDsm,
  consulterJournalDeclarations,
  consulterJournalDsm,
  consulterPiecesDsm,
  consulterPointDeDepart,
  consulterSituationDsm,
  demanderComplementsDsm,
  deposerDrm,
  deposerJustificatifDsm,
  ecrireCommentaireDsm,
  enregistrerDsm,
  envoyerDsm,
  fixerPointDeDepart,
  initierDsm,
  listerCommentairesDsm,
  listerDenominations,
  listerDsm,
  modifierCommentaireDsm,
  modifierPointDeDepart,
  prendreDsmEnInstruction,
  supprimerCommentaireDsm,
  supprimerDrm,
  supprimerDsm,
  supprimerJustificatifDsm,
  validerDsm,
  type SaisieCommentaireDsm,
  type DeposerJustificatifDsmData,
  type SaisieDsm,
  type SaisiePointDeDepart,
} from '@/api/generated'

/**
 * Où s'affiche une DSM : au back-office (opérateur actif BNIA) ou au portail. Les écrans sont les mêmes ;
 * ce que l'on peut y faire vient des droits (RG-DEC-01), l'espace ne décide que des liens.
 */
export type Espace = 'bo' | 'portail'

/** Clés de cache des déclarations d'un opérateur, invalidées ensemble après une écriture. */
export const cleDeclarations = (operateur: number) =>
  ['declarations', operateur] as const

/** Les appels des déclarations d'un opérateur (spec 05). */
export function apiDeclarations(operateur: number) {
  const path = { operateur }
  return {
    pointDeDepart: () =>
      consulterPointDeDepart({ path, throwOnError: true }).then(
        // 204 : pas encore de point de départ.
        (r) => (r.status === 204 ? null : r.data)
      ),
    fixer: (body: SaisiePointDeDepart) =>
      fixerPointDeDepart({ path, body, throwOnError: true }).then(
        (r) => r.data
      ),
    modifier: (id: number, body: SaisiePointDeDepart) =>
      modifierPointDeDepart({
        path: { operateur, id },
        body,
        throwOnError: true,
      }).then((r) => r.data),
    situation: () =>
      consulterSituationDsm({ path, throwOnError: true }).then((r) => r.data),
    lister: () => listerDsm({ path, throwOnError: true }).then((r) => r.data),
    initier: () => initierDsm({ path, throwOnError: true }).then((r) => r.data),
    consulter: (id: number) =>
      consulterDsm({ path: { operateur, id }, throwOnError: true }).then(
        (r) => r.data
      ),
    apercevoir: (id: number, body: SaisieDsm) =>
      apercevoirDsm({ path: { operateur, id }, body, throwOnError: true }).then(
        (r) => r.data
      ),
    enregistrer: (id: number, body: SaisieDsm) =>
      enregistrerDsm({
        path: { operateur, id },
        body,
        throwOnError: true,
      }).then((r) => r.data),
    supprimer: (id: number) =>
      supprimerDsm({ path: { operateur, id }, throwOnError: true }),
    envoyer: (id: number, version: number) =>
      envoyerDsm({
        path: { operateur, id },
        body: { version },
        throwOnError: true,
      }).then((r) => r.data),
    prendreEnInstruction: (id: number, version: number) =>
      prendreDsmEnInstruction({
        path: { operateur, id },
        body: { version },
        throwOnError: true,
      }).then((r) => r.data),
    demanderComplements: (id: number, motif: string, version: number) =>
      demanderComplementsDsm({
        path: { operateur, id },
        body: { motif, version },
        throwOnError: true,
      }).then((r) => r.data),
    valider: (id: number, version: number) =>
      validerDsm({
        path: { operateur, id },
        body: { version },
        throwOnError: true,
      }).then((r) => r.data),
    commentaires: (dsm: number) =>
      listerCommentairesDsm({
        path: { operateur, dsm },
        throwOnError: true,
      }).then((r) => r.data),
    commenter: (dsm: number, body: SaisieCommentaireDsm) =>
      ecrireCommentaireDsm({
        path: { operateur, dsm },
        body,
        throwOnError: true,
      }).then((r) => r.data),
    modifierCommentaire: (
      dsm: number,
      id: number,
      body: SaisieCommentaireDsm
    ) =>
      modifierCommentaireDsm({
        path: { operateur, dsm, id },
        body,
        throwOnError: true,
      }).then((r) => r.data),
    supprimerCommentaire: (dsm: number, id: number) =>
      supprimerCommentaireDsm({
        path: { operateur, dsm, id },
        throwOnError: true,
      }),
    pieces: (dsm: number) =>
      consulterPiecesDsm({ path: { operateur, dsm }, throwOnError: true }).then(
        (r) => r.data
      ),
    deposerJustificatif: (
      dsm: number,
      fichier: File,
      query: DeposerJustificatifDsmData['query']
    ) =>
      deposerJustificatifDsm({
        path: { operateur, dsm },
        body: { fichier },
        query,
        throwOnError: true,
      }).then((r) => r.data),
    supprimerJustificatif: (dsm: number, id: number) =>
      supprimerJustificatifDsm({
        path: { operateur, dsm, id },
        throwOnError: true,
      }),
    deposerDrm: (dsm: number, mois: string, fichier: File) =>
      deposerDrm({
        path: { operateur, dsm, mois },
        body: { fichier },
        throwOnError: true,
      }),
    supprimerDrm: (dsm: number, mois: string) =>
      supprimerDrm({ path: { operateur, dsm, mois }, throwOnError: true }),
    /** Le PDF s'ouvre dans un nouvel onglet du navigateur, avec la session en cours. */
    urlJustificatif: (dsm: number, id: number) =>
      `/api/operateurs/${operateur}/dsm/${dsm}/justificatifs/${id}/document`,
    urlDrm: (dsm: number, mois: string) =>
      `/api/operateurs/${operateur}/dsm/${dsm}/drm/${mois}/document`,
    /** RG-DEC-35 : le PDF de la DSM, ouvert dans un nouvel onglet ; l'édition est inscrite au journal. */
    urlPdf: (dsm: number) => `/api/operateurs/${operateur}/dsm/${dsm}/pdf`,
    journalDsm: (id: number) =>
      consulterJournalDsm({ path: { operateur, id }, throwOnError: true }).then(
        (r) => r.data
      ),
    journal: () =>
      consulterJournalDeclarations({ path, throwOnError: true }).then(
        (r) => r.data
      ),
    denominations: () =>
      listerDenominations({ path, throwOnError: true }).then((r) => r.data),
  }
}

/** Le point de départ en vigueur de l'opérateur, ou {@code null}. */
export function usePointDeDepart(operateur: number) {
  return useQuery({
    queryKey: [...cleDeclarations(operateur), 'point-de-depart'],
    queryFn: apiDeclarations(operateur).pointDeDepart,
  })
}

/** Les DSM de l'opérateur, de la plus récente à la plus ancienne. */
export function useDsms(operateur: number) {
  return useQuery({
    queryKey: [...cleDeclarations(operateur), 'dsm'],
    queryFn: apiDeclarations(operateur).lister,
  })
}

/** Le journal des déclarations de l'opérateur (RG-DEC-23). */
export function useJournalDeclarations(operateur: number) {
  return useQuery({
    queryKey: [...cleDeclarations(operateur), 'journal'],
    queryFn: apiDeclarations(operateur).journal,
  })
}

/** Les pièces d'une DSM : justificatifs, mois de DRM, manques (spec 05 §6.3). */
export function usePiecesDsm(operateur: number, dsm: number) {
  return useQuery({
    queryKey: [...cleDeclarations(operateur), 'dsm', dsm, 'pieces'],
    queryFn: () => apiDeclarations(operateur).pieces(dsm),
  })
}
