import { format, parseISO } from 'date-fns'

const litres = new Intl.NumberFormat('fr-FR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

/** Un volume en litres d'alcool pur, au centilitre : « 1 500,00 » (H-DEC-4). */
export function formatLitres(valeur: number | null | undefined) {
  return valeur == null ? '' : litres.format(valeur)
}

/**
 * Un volume saisi en litres décimaux (RG-DEC-15) : virgule ou point, espaces ignorés. Vide : 0. Ce qui
 * n'est pas un nombre donne {@code null}, signalé à l'écran.
 */
export function lireLitres(texte: string): number | null {
  const nettoye = texte.replace(/\s/g, '').replace(',', '.')
  if (nettoye === '') return 0
  if (!/^-?\d*\.?\d*$/.test(nettoye) || nettoye === '.' || nettoye === '-')
    return null
  return Number(nettoye)
}

/** Un volume pour un champ de saisie : vide pour 0, sinon « 1500,5 ». */
export function texteLitres(valeur: number) {
  return valeur === 0 ? '' : String(valeur).replace('.', ',')
}

/** Une date ISO {@code YYYY-MM-DD} : « 01/06/2026 ». */
export function formatDate(date: string | null | undefined) {
  return date ? format(parseISO(date), 'dd/MM/yyyy') : ''
}

const dateHeure = new Intl.DateTimeFormat('fr-FR', {
  dateStyle: 'short',
  timeStyle: 'short',
})

export function formatDateHeure(instant: string | null | undefined) {
  return instant ? dateHeure.format(new Date(instant)) : ''
}

const euros = new Intl.NumberFormat('fr-FR', {
  style: 'currency',
  currency: 'EUR',
})

/** Un montant en euros, au centime : « 1 855,95 € » (RG-DEC-33). */
export function formatEuros(valeur: number | null | undefined) {
  return valeur == null ? '' : euros.format(valeur)
}

const pourcentage = new Intl.NumberFormat('fr-FR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

/** Un pourcentage à deux décimales : « 20,00 % ». */
export function formatPourcentage(valeur: number) {
  return `${pourcentage.format(valeur)} %`
}

const hectolitres = new Intl.NumberFormat('fr-FR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 4,
})

/** Un volume en hectolitres d'alcool pur, sans arrondi : « 8,3911 » (RG-DEC-33). */
export function formatHectolitres(valeur: number) {
  return hectolitres.format(valeur)
}
