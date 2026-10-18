import { isThisYear, isToday, isYesterday } from 'date-fns'

/** Les dates d'un fil de messages : séparateurs de jour, heure des en-têtes, date des lignes de la boîte de réception. */

const jour = new Intl.DateTimeFormat('fr-FR', {
  weekday: 'long',
  day: 'numeric',
  month: 'long',
  year: 'numeric',
})

const heure = new Intl.DateTimeFormat('fr-FR', { timeStyle: 'short' })

/** Le jour d'un instant, pour séparer un fil : « Aujourd'hui », « Hier », « lundi 12 octobre 2026 ». */
export function libelleJour(instant: string) {
  const date = new Date(instant)
  if (isToday(date)) return 'Aujourd’hui'
  if (isYesterday(date)) return 'Hier'
  return jour.format(date)
}

/** La clé du jour d'un instant, dans le fuseau du navigateur : « 2026-10-12 ». */
export function cleJour(instant: string) {
  const d = new Date(instant)
  return `${d.getFullYear()}-${d.getMonth() + 1}-${d.getDate()}`
}

/** L'heure d'un instant : « 14:05 ». */
export function formatHeure(instant: string) {
  return heure.format(new Date(instant))
}

const jourMois = new Intl.DateTimeFormat('fr-FR', {
  day: 'numeric',
  month: 'short',
})

const dateCourte = new Intl.DateTimeFormat('fr-FR', { dateStyle: 'short' })

/** La date d'une ligne de boîte de réception : « 14:05 » aujourd'hui, « 12 oct. » cette année, sinon « 12/10/2025 ». */
export function formatDateCourte(instant: string) {
  const date = new Date(instant)
  if (isToday(date)) return heure.format(date)
  if (isThisYear(date)) return jourMois.format(date)
  return dateCourte.format(date)
}
