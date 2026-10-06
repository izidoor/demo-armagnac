import { type Valeur } from './referentiels'

/** Valeur d'un choix vide : un Select ne prend pas de chaîne vide. */
export const AUCUN = 'aucun'

/** Options d'un Select de référentiel : actives, plus la valeur actuelle si elle est inactive (RG-OPE-21). */
export function optionsReference(valeurs: Valeur[], actuelle?: number | null) {
  return valeurs
    .filter((v) => v.actif || v.id === actuelle)
    .map((v) => ({
      valeur: String(v.id),
      libelle: v.actif ? v.libelle : `${v.libelle} (inactif)`,
    }))
}

export function optionsListe(libelles: Record<string, string>) {
  return Object.entries(libelles).map(([valeur, libelle]) => ({
    valeur,
    libelle,
  }))
}

/** Texte saisi → valeur envoyée : vide devient absent. */
export function texte(valeur: string): string | undefined {
  const t = valeur.trim()
  return t === '' ? undefined : t
}

/** Identifiant choisi dans un Select → valeur envoyée. */
export function identifiant(valeur: string): number | undefined {
  return valeur === AUCUN || valeur === '' ? undefined : Number(valeur)
}

/** Nombre saisi → valeur envoyée ; un texte non numérique est envoyé tel quel pour que le serveur le refuse. */
export function nombre(valeur: string): number | undefined {
  const t = valeur.trim().replace(',', '.')
  return t === '' ? undefined : Number(t)
}

export function versTexte(valeur?: string | number | null): string {
  return valeur == null ? '' : String(valeur)
}

export function versChoix(valeur?: number | null): string {
  return valeur == null ? AUCUN : String(valeur)
}
