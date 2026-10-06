import { format, parseISO } from 'date-fns'

const pourcentage = new Intl.NumberFormat('fr-FR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

/** 20 → « 20,00 % ». */
export function formatTaux(taux: number) {
  return `${pourcentage.format(taux)} %`
}

/** « 2014-01-01 » → « 01/01/2014 », sans décalage de fuseau. */
export function formatDate(date: string) {
  return format(parseISO(date), 'dd/MM/yyyy')
}
