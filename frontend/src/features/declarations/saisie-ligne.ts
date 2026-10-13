import { type CleConditionnement, millesimee } from './libelles'

/**
 * L'identité d'une ligne telle que saisie (RG-DEC-10, 12) : la clé, puis un millésime (clé M) ou une
 * dénomination (clé N) dans un même champ, et le compte d'âge, calculé pour une ligne MB.
 */
export type IdentiteSaisie = {
  cle: CleConditionnement
  /** Millésime pour une clé M, dénomination pour une clé N. */
  designation: string
  compte: string
}

/** Les champs de l'API : millésime ou dénomination, compte ignoré pour MB. */
export function versApi(i: IdentiteSaisie) {
  const texte = i.designation.trim()
  const m = millesimee(i.cle)
  return {
    cle: i.cle,
    millesime: m && texte !== '' ? Number(texte) : undefined,
    denomination: !m && texte !== '' ? texte : undefined,
    compte: i.cle === 'MB' ? undefined : i.compte.trim() || undefined,
  }
}

/** L'identité d'une ligne de l'API, pour la saisie. */
export function depuisApi(l: {
  cle: CleConditionnement
  millesime?: number
  denomination?: string
  compte: string
}): IdentiteSaisie {
  return {
    cle: l.cle,
    designation: millesimee(l.cle)
      ? String(l.millesime ?? '')
      : (l.denomination ?? ''),
    compte: l.compte,
  }
}
