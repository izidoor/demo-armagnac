import { useQuery } from '@tanstack/react-query'
import {
  apercevoirDsm,
  consulterDsm,
  consulterJournalDeclarations,
  consulterJournalDsm,
  consulterPiecesDsm,
  consulterPointDeDepart,
  consulterSituationDsm,
  consulterStockDeclare,
  consulterStockEstime,
  demanderComplementsDsm,
  deposerDrm,
  deposerJustificatifDsm,
  ecrireNoteInterneDsm,
  enregistrerDsm,
  envoyerDsm,
  facturerDsm,
  fixerPointDeDepart,
  historiquePointsDeDepart,
  importerDsm,
  importerPointDeDepart,
  initierDsm,
  listerNotesInternesDsm,
  listerDenominations,
  listerDsm,
  modifierNoteInterneDsm,
  modifierPointDeDepart,
  prendreDsmEnInstruction,
  proposerPointDeDepart,
  supprimerNoteInterneDsm,
  supprimerDrm,
  supprimerDsm,
  supprimerJustificatifDsm,
  validerDsm,
  type SaisieNoteInterneDsm,
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
    /** RG-DEC-09 : tous les points de départ, du plus récent au plus ancien. */
    historique: () =>
      historiquePointsDeDepart({ path, throwOnError: true }).then(
        (r) => r.data
      ),
    /** RG-DEC-09 : le stock proposé pour un nouveau point de départ ; {@code null} sans DSM validée. */
    proposition: (debut?: string) =>
      proposerPointDeDepart({
        path,
        query: { debut },
        throwOnError: true,
      }).then((r) => (r.status === 204 ? null : r.data)),
    /** RG-DEC-42 : le stock déclaré ; {@code null} sans DSM validée. */
    stockDeclare: () =>
      consulterStockDeclare({ path, throwOnError: true }).then((r) =>
        r.status === 204 ? null : r.data
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
    /** RG-DEC-37, 38 : le stock initial importé du modèle Excel ; sans date, le lendemain de la période du fichier. */
    importerPointDeDepart: (fichier: File, debut?: string, version?: number) =>
      importerPointDeDepart({
        path,
        body: { fichier },
        query: { debut, version },
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
    /** RG-DEC-37, 38 : le modèle Excel importé dans la DSM, tout ou rien. */
    importer: (id: number, fichier: File, version: number) =>
      importerDsm({
        path: { operateur, id },
        body: { fichier },
        query: { version },
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
    /** RG-DEC-43 : le BNIA demande la facture d'une DSM validée ; la DSM revient avec son brouillon. */
    facturer: (id: number) =>
      facturerDsm({ path: { operateur, id }, throwOnError: true }).then(
        (r) => r.data
      ),
    valider: (id: number, version: number) =>
      validerDsm({
        path: { operateur, id },
        body: { version },
        throwOnError: true,
      }).then((r) => r.data),
    /** Carnet de notes internes (spec 15 §3.7) : personnel du BNIA seulement. */
    notes: (dsm: number) =>
      listerNotesInternesDsm({
        path: { operateur, dsm },
        throwOnError: true,
      }).then((r) => r.data),
    annoter: (dsm: number, body: SaisieNoteInterneDsm) =>
      ecrireNoteInterneDsm({
        path: { operateur, dsm },
        body,
        throwOnError: true,
      }).then((r) => r.data),
    modifierNote: (dsm: number, id: number, body: SaisieNoteInterneDsm) =>
      modifierNoteInterneDsm({
        path: { operateur, dsm, id },
        body,
        throwOnError: true,
      }).then((r) => r.data),
    supprimerNote: (dsm: number, id: number) =>
      supprimerNoteInterneDsm({
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
    /** RG-DEC-36 : le classeur Excel de la DSM, téléchargé ; l'édition est inscrite au journal. */
    urlExcel: (dsm: number) => `/api/operateurs/${operateur}/dsm/${dsm}/excel`,
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

/** Les points de départ de l'opérateur, du plus récent au plus ancien (RG-DEC-09). */
export function useHistoriquePointsDeDepart(operateur: number) {
  return useQuery({
    queryKey: [...cleDeclarations(operateur), 'point-de-depart', 'historique'],
    queryFn: apiDeclarations(operateur).historique,
  })
}

/** Le stock déclaré de l'opérateur, ou {@code null} (RG-DEC-42). */
export function useStockDeclare(operateur: number) {
  return useQuery({
    queryKey: [...cleDeclarations(operateur), 'stock-declare'],
    queryFn: apiDeclarations(operateur).stockDeclare,
  })
}

/** RG-CA-11 (spec 06) : le stock estimé de l'opérateur, base vieillie à aujourd'hui et retiraisons validées. */
export function useStockEstime(operateur: number) {
  return useQuery({
    queryKey: [...cleDeclarations(operateur), 'stock-estime'],
    queryFn: () =>
      consulterStockEstime({ path: { operateur }, throwOnError: true }).then(
        (r) => r.data
      ),
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
