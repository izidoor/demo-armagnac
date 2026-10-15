/**
 * Calcul d'un tableau croisé dynamique, sans dépendance à l'écran : filtrage, regroupement des lignes en
 * arbre, colonnes croisées, sommes. Un champ « non cumulable » (le stock à une date, par exemple) ne se somme
 * jamais à travers ses valeurs : un sous-total qui le traverserait n'est pas calculé (`null`).
 */

/** Une dimension du tableau croisé. */
export type Champ<T> = {
  id: string
  libelle: string
  /** La valeur de regroupement d'une ligne de données. */
  valeur: (ligne: T) => string
  /** Le libellé affiché d'une valeur ; la valeur elle-même par défaut. */
  affichage?: (valeur: string) => string
  /** L'ordre des valeurs ; alphabétique, nombres compris, par défaut. */
  ordre?: (a: string, b: string) => number
  /** Ne se somme pas à travers ses valeurs : en lignes, en colonnes, ou réduit à une seule valeur. */
  nonCumulable?: boolean
}

/** Ce qui se somme dans les cases. */
export type Mesure<T> = {
  id: string
  libelle: string
  valeur: (ligne: T) => number
  format: (valeur: number) => string
}

export type Zone = 'lignes' | 'colonnes' | 'filtres'

/** Ce que l'utilisateur a composé : les champs de chaque zone, dans l'ordre, et les valeurs retenues des filtres. */
export type Composition = {
  lignes: string[]
  colonnes: string[]
  filtres: string[]
  /** Valeurs retenues d'un champ en filtre ; absent : toutes. */
  selection: Record<string, string[]>
  mesure?: string
}

export type Vue = {
  id: string
  libelle: string
  composition: Composition
}

export type Noeud = {
  id: string
  libelle: string
  /** Rang de la valeur dans l'ordre de son champ, pour le tri. */
  rang: number
  /** Somme par clé de colonne ; vide si le nœud n'est pas cumulable. */
  valeurs: Record<string, number>
  total: number | null
  subRows?: Noeud[]
}

export type Croisement = {
  colonnes: { cle: string; libelle: string }[]
  /** Une colonne « Total » à droite : il y a des colonnes et aucune n'est non cumulable. */
  totalColonnes: boolean
  lignes: Noeud[]
  /** Ligne de total ; `null` si une dimension en lignes n'est pas cumulable. */
  totaux: { valeurs: Record<string, number>; total: number | null } | null
}

const SEPARATEUR = '\u001f'
const COLLATION = new Intl.Collator('fr', { numeric: true })

const ordreDe =
  <T>(champ: Champ<T>) =>
  (a: string, b: string) =>
    champ.ordre ? champ.ordre(a, b) : COLLATION.compare(a, b)

export const affichageDe = <T>(champ: Champ<T>, valeur: string) =>
  champ.affichage ? champ.affichage(valeur) : valeur

/** Les valeurs distinctes d'un champ dans les données, dans son ordre. */
export function valeursDe<T>(donnees: T[], champ: Champ<T>): string[] {
  return [...new Set(donnees.map(champ.valeur))].sort(ordreDe(champ))
}

/**
 * Remet une composition en état : champs connus, chacun dans une seule zone ; un champ non cumulable hors des
 * lignes et des colonnes va en filtre, réduit à une valeur (la dernière par défaut).
 */
export function normaliser<T>(
  composition: Composition,
  champs: Champ<T>[],
  donnees: T[]
): Composition {
  const connus = new Set(champs.map((c) => c.id))
  const vus = new Set<string>()
  const garder = (ids: string[]) =>
    ids.filter((id) => {
      if (!connus.has(id) || vus.has(id)) return false
      vus.add(id)
      return true
    })
  const lignes = garder(composition.lignes)
  const colonnes = garder(composition.colonnes)
  const filtres = garder(composition.filtres)
  const selection: Record<string, string[]> = {}
  for (const champ of champs) {
    const place = lignes.includes(champ.id) || colonnes.includes(champ.id)
    if (champ.nonCumulable && !place) {
      if (!filtres.includes(champ.id)) filtres.push(champ.id)
      const valeurs = valeursDe(donnees, champ)
      const choisie = composition.selection[champ.id]?.find((v) =>
        valeurs.includes(v)
      )
      const une = choisie ?? valeurs[valeurs.length - 1]
      selection[champ.id] = une === undefined ? [] : [une]
    } else if (filtres.includes(champ.id) && composition.selection[champ.id]) {
      selection[champ.id] = composition.selection[champ.id]
    }
  }
  return { lignes, colonnes, filtres, selection, mesure: composition.mesure }
}

function comparerCles<T>(champs: Champ<T>[]) {
  return (a: string, b: string) => {
    const va = a.split(SEPARATEUR)
    const vb = b.split(SEPARATEUR)
    for (let i = 0; i < champs.length; i++) {
      const c = ordreDe(champs[i])(va[i], vb[i])
      if (c !== 0) return c
    }
    return 0
  }
}

function sommer<T>(
  lignes: T[],
  cleColonne: (l: T) => string,
  mesure: Mesure<T>
): { valeurs: Record<string, number>; total: number } {
  const valeurs: Record<string, number> = {}
  let total = 0
  for (const l of lignes) {
    const v = mesure.valeur(l)
    const cle = cleColonne(l)
    valeurs[cle] = (valeurs[cle] ?? 0) + v
    total += v
  }
  return { valeurs, total }
}

/** Le tableau croisé des données selon la composition (supposée normalisée). */
export function croiser<T>(
  donnees: T[],
  champs: Champ<T>[],
  mesure: Mesure<T>,
  composition: Composition
): Croisement {
  const parId = new Map(champs.map((c) => [c.id, c]))
  const de = (ids: string[]) =>
    ids.map((id) => parId.get(id)).filter((c): c is Champ<T> => !!c)
  const enLignes = de(composition.lignes)
  const enColonnes = de(composition.colonnes)

  const retenues = donnees.filter((d) =>
    composition.filtres.every((id) => {
      const choix = composition.selection[id]
      const champ = parId.get(id)
      return !choix || !champ || choix.includes(champ.valeur(d))
    })
  )

  const cleColonne = (d: T) =>
    enColonnes.map((c) => c.valeur(d)).join(SEPARATEUR)
  const colonnes =
    enColonnes.length === 0
      ? [{ cle: '', libelle: mesure.libelle }]
      : [...new Set(retenues.map(cleColonne))]
          .sort(comparerCles(enColonnes))
          .map((cle) => ({
            cle,
            libelle: cle
              .split(SEPARATEUR)
              .map((v, i) => affichageDe(enColonnes[i], v))
              .join(' · '),
          }))
  const totalColonnes =
    enColonnes.length > 0 && !enColonnes.some((c) => c.nonCumulable)

  const grouper = (lignes: T[], niveau: number, prefixe: string): Noeud[] => {
    const champ = enLignes[niveau]
    const groupes = new Map<string, T[]>()
    for (const l of lignes) {
      const v = champ.valeur(l)
      const groupe = groupes.get(v)
      if (groupe) groupe.push(l)
      else groupes.set(v, [l])
    }
    const cumulable = enLignes.slice(niveau + 1).every((c) => !c.nonCumulable)
    return [...groupes.keys()].sort(ordreDe(champ)).map((v, rang) => {
      const groupe = groupes.get(v) ?? []
      const id = prefixe + SEPARATEUR + v
      const sommes = sommer(groupe, cleColonne, mesure)
      return {
        id,
        libelle: affichageDe(champ, v),
        rang,
        valeurs: cumulable ? sommes.valeurs : {},
        total: cumulable && totalColonnes ? sommes.total : null,
        subRows:
          niveau + 1 < enLignes.length
            ? grouper(groupe, niveau + 1, id)
            : undefined,
      }
    })
  }

  const lignes = enLignes.length > 0 ? grouper(retenues, 0, '') : []
  const sommes = sommer(retenues, cleColonne, mesure)
  const totaux = enLignes.some((c) => c.nonCumulable)
    ? null
    : {
        valeurs: sommes.valeurs,
        total: totalColonnes ? sommes.total : null,
      }
  return { colonnes, totalColonnes, lignes, totaux }
}
