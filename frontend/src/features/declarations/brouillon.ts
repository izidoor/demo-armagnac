import { type ColonneDsm, type Dsm, type SaisieDsm } from '@/api/generated'
import { lireLitres, texteLitres } from './format'
import { type CleConditionnement } from './libelles'

/** Une ligne de la DSM en cours de saisie : volumes en texte, tels que tapés. */
export type LigneBrouillon = {
  /** Absent pour une ligne ajoutée et pas encore enregistrée. */
  id?: number
  cle: CleConditionnement
  millesime?: number
  denomination?: string
  compte: string
  origine: 'REPRISE' | 'AJOUT'
  /** 26 textes, indice 0 = colonne 1 ; les colonnes 1 et 26 ne se saisissent pas. */
  volumes: string[]
  /** Colonne 1, reprise, jamais saisie (RG-DEC-13). */
  stockDebut: number
}

export type Brouillon = { dateDia: string; lignes: LigneBrouillon[] }

export function brouillonDe(dsm: Dsm): Brouillon {
  return {
    dateDia: dsm.dateDia ?? '',
    lignes: dsm.lignes.map((l) => ({
      id: l.id,
      cle: l.cle,
      millesime: l.millesime,
      denomination: l.denomination,
      compte: l.compte,
      origine: l.origine,
      volumes: l.volumes.map((v, i) =>
        i === 0 || i === 25 ? '' : texteLitres(v)
      ),
      stockDebut: l.volumes[0],
    })),
  }
}

/** Chaque volume saisi se lit comme un nombre (RG-DEC-15). */
export function brouillonLisible(b: Brouillon) {
  return b.lignes.every((l) => l.volumes.every((v) => lireLitres(v) !== null))
}

/** La saisie envoyée à l'API, pour l'enregistrement ou la vue provisoire. */
export function saisieDe(b: Brouillon, version?: number): SaisieDsm {
  return {
    dateDia: b.dateDia || undefined,
    version,
    lignes: b.lignes.map((l) => ({
      id: l.id,
      cle: l.cle,
      millesime: l.millesime,
      denomination: l.denomination,
      compte: l.cle === 'MB' ? undefined : l.compte,
      volumes: l.volumes.map((v, i) =>
        i === 0 || i === 25 ? 0 : (lireLitres(v) ?? 0)
      ),
    })),
  }
}

/**
 * Sommes d'affichage (RG-DEC-17), recalculées à chaque frappe : par ligne, le volume de chaque colonne,
 * les entrées, les sorties et le stock de fin ; par colonne, le total. Les règles (anomalies, comptes)
 * restent au serveur, par la vue provisoire.
 */
export function sommes(b: Brouillon, colonnes: ColonneDsm[]) {
  const lignes = b.lignes.map((l) => {
    const v = l.volumes.map((t, i) =>
      i === 0 ? l.stockDebut : (lireLitres(t) ?? 0)
    )
    let entrees = 0
    let sorties = 0
    colonnes.forEach((c) => {
      if (c.sens === 'ENTREE') entrees += v[c.numero - 1]
      if (c.sens === 'SORTIE') sorties += v[c.numero - 1]
    })
    v[25] = arrondi(l.stockDebut + entrees - sorties)
    return { volumes: v, entrees: arrondi(entrees), sorties: arrondi(sorties) }
  })
  const totaux = colonnes.map((c) =>
    arrondi(lignes.reduce((s, l) => s + l.volumes[c.numero - 1], 0))
  )
  return { lignes, totaux }
}

/** Au centilitre, pour l'affichage. */
function arrondi(n: number) {
  return Math.round(n * 100) / 100
}
