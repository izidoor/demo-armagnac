export type TypeDeclarant = 'PRODUCTEUR' | 'NEGOCIANT'

/**
 * Chiffres réels de la filière, communiqués par le BNIA : nombre d'opérateurs et stock total (159 236 hl AP,
 * 35 % chez les producteurs, 65 % chez les négociants). Les données de démonstration n'en sont qu'un
 * échantillon : le tableau de bord et les graphiques de démonstration partent de ces chiffres plutôt que de
 * ceux de la base.
 */
export const FILIERE: Record<
  TypeDeclarant,
  { operateurs: number; stockHl: number }
> = {
  PRODUCTEUR: { operateurs: 460, stockHl: 55_733 },
  NEGOCIANT: { operateurs: 180, stockHl: 103_503 },
}

/** Le type de déclarant d'un opérateur du portail ; un opérateur d'un autre type compte comme un producteur. */
export const typeDeclarant = (type: string): TypeDeclarant =>
  type === 'NEGOCIANT' ? 'NEGOCIANT' : 'PRODUCTEUR'

/** La part d'un opérateur moyen de son type dans la filière : son stock rapporté au stock total. */
export function partMoyenne(type: TypeDeclarant) {
  const total = FILIERE.PRODUCTEUR.stockHl + FILIERE.NEGOCIANT.stockHl
  return FILIERE[type].stockHl / FILIERE[type].operateurs / total
}
