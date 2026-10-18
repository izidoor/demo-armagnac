/** Outils des graphiques de démonstration (`graphique-demo.tsx`). */

export const STYLE_INFOBULLE = {
  background: 'var(--popover)',
  border: '1px solid var(--border)',
  borderRadius: 6,
  fontSize: 12,
}

const compact = new Intl.NumberFormat('fr-FR', {
  notation: 'compact',
  maximumFractionDigits: 1,
})

/** Les graduations d'un axe, abrégées : « 1,2 k hl AP ». */
export const formatAxe = (unite: string) => (v: number) =>
  `${compact.format(v)} ${unite}`

/** Un nombre fictif mais stable entre -1 et 1, tiré d'une graine. */
export function bruit(graine: number) {
  const x = Math.sin(graine) * 10000
  return (x - Math.floor(x)) * 2 - 1
}

const nomMois = new Intl.DateTimeFormat('fr-FR', { month: 'short' })

/** « oct. 25 », ou « oct. » sans l'année. */
export const libelleMois = (annee: number, mois: number, avecAnnee = true) => {
  const m = nomMois.format(new Date(annee, mois - 1, 1))
  return avecAnnee ? `${m} ${String(annee).slice(2)}` : m
}

/** Bascule une clé dans un ensemble, sans le modifier. */
export function basculer(ensemble: Set<string>, cle: string) {
  const suivant = new Set(ensemble)
  if (suivant.has(cle)) suivant.delete(cle)
  else suivant.add(cle)
  return suivant
}

/** Les vues d'un graphique sur l'année civile. */
export type VueCivile = 'annees' | 'glissants' | 'annee-en-cours'

export const VUES_CIVILES: { id: VueCivile; libelle: string }[] = [
  { id: 'annees', libelle: '5 dernières années' },
  { id: 'glissants', libelle: '12 mois glissants' },
  { id: 'annee-en-cours', libelle: 'Année en cours' },
]

/** Une période : son libellé et ses mois. */
export type Periode = {
  libelle: string
  mois: { annee: number; mois: number }[]
}

/**
 * Les périodes d'une vue, au jour du navigateur ; un mois n'y entre qu'une fois terminé. Sur l'année en cours,
 * ses douze mois sont en abscisse, et ceux qui ne sont pas terminés n'ont pas de mois (pas de valeur).
 */
export function periodesCiviles(vue: VueCivile, jour = new Date()): Periode[] {
  const annee = jour.getFullYear()
  const moisCourant = jour.getMonth() + 1
  if (vue === 'annees') {
    return Array.from({ length: 5 }, (_, i) => {
      const a = annee - 5 + i
      return {
        libelle: String(a),
        mois: Array.from({ length: 12 }, (_, m) => ({ annee: a, mois: m + 1 })),
      }
    })
  }
  if (vue === 'glissants') {
    return Array.from({ length: 12 }, (_, i) => {
      const d = new Date(annee, moisCourant - 13 + i, 1)
      const a = d.getFullYear()
      const m = d.getMonth() + 1
      return { libelle: libelleMois(a, m), mois: [{ annee: a, mois: m }] }
    })
  }
  return Array.from({ length: 12 }, (_, i) => ({
    libelle: libelleMois(annee, i + 1, false),
    mois: i + 1 < moisCourant ? [{ annee, mois: i + 1 }] : [],
  }))
}
