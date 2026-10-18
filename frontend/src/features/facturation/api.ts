/** Clés de cache de la facturation, invalidées ensemble après une écriture (spec 08). */
export const cleFacturation = ['facturation'] as const

/** RG-FAC-15 : le PDF d'une pièce émise, ouvert dans un nouvel onglet ; le téléchargement est journalisé. */
export const urlPdfPiece = (id: number) => `/api/facturation/pieces/${id}/pdf`

/** RG-FAC-28 : au portail, le PDF d'une pièce émise de l'opérateur ; le téléchargement est journalisé côté client. */
export const urlPdfMaFacture = (operateur: number, id: number) =>
  `/api/operateurs/${operateur}/factures/${id}/pdf`

/** RG-FAC-15 : l'aperçu PDF d'un brouillon, marqué « projet », recalculé à chaque ouverture ; ni conservé ni journalisé. */
export const urlApercuPiece = (id: number) =>
  `/api/facturation/pieces/${id}/apercu`
