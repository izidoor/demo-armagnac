/** Clés de cache de la facturation, invalidées ensemble après une écriture (spec 08). */
export const cleFacturation = ['facturation'] as const

/** RG-FAC-15 : le PDF d'une pièce émise, ouvert dans un nouvel onglet ; le téléchargement est journalisé. */
export const urlPdfPiece = (id: number) => `/api/facturation/pieces/${id}/pdf`
