import { formatDate, formatEuros } from '@/features/declarations/format'
import { APPELLATIONS, SOLDES } from './libelles'

const deux = new Intl.NumberFormat('fr-FR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

/** Des hectolitres : « 15,00 hl ». */
export const formatHl = (hl: number) => `${deux.format(hl)} hl`

/** Un volume d'alcool pur donné en litres, en hl AP : « 7,05 hl AP ». */
export const formatHlAp = (litres: number) =>
  `${deux.format(litres / 100)} hl AP`

const heure = new Intl.DateTimeFormat('fr-FR', {
  hour: '2-digit',
  minute: '2-digit',
})

/** Un instant : « le 23/10/2026 à 14:32 ». */
export function formatLeA(instant: string) {
  const d = new Date(instant)
  return `le ${d.toLocaleDateString('fr-FR')} à ${heure.format(d)}`
}

/** Un degré : « 47,00 % vol. ». */
export const formatDegre = (degre: number) => `${deux.format(degre)} % vol.`

/** Un prix au hl AP : « 1 750,00 € / hl AP ». */
export const formatPrix = (prix: number) => `${formatEuros(prix)} / hl AP`

/** Un prix au litre de volume : « 8,23 € / l vol ». */
export const formatPrixLitre = (prix: number) => `${formatEuros(prix)} / l vol`

/** RG-CA-04, comme le domaine : volume AP (L) = volume (hl) × degré. */
export const volumeAp = (volume: number, degre: number) =>
  Math.round(volume * degre * 100) / 100

/** RG-CA-04 : montant HT = volume AP (hl) × prix, au centime. */
export const montantHt = (volume: number, degre: number, prix: number) =>
  Math.round(volume * degre * prix) / 100

/** RG-CA-04 : prix au litre de volume = prix × degré / 10 000. */
export const prixLitre = (prix: number, degre: number) =>
  Math.round((prix * degre) / 100) / 100

/** Un nombre saisi : virgule ou point, espaces ignorés ; ce qui n'en est pas un donne `null`. */
export function lireNombre(texte: string): number | null {
  const nettoye = texte.replace(/\s/g, '').replace(',', '.')
  if (nettoye === '' || !/^\d*\.?\d*$/.test(nettoye) || nettoye === '.')
    return null
  return Number(nettoye)
}

/** La désignation d'une ligne : « 2015 » ou « Hors d'âge, compte 20+ ». */
export function designation(ligne: {
  millesime?: number | null
  denomination?: string | null
  compte: string
}) {
  return ligne.millesime != null
    ? `Millésime ${ligne.millesime}`
    : `${ligne.denomination}, compte ${ligne.compte}`
}

/** Une valeur brute de l'écart (RG-CA-09), mise en forme selon son champ. */
export function valeurChamp(champ: string, valeur?: string | null) {
  if (valeur == null || valeur === '') return '—'
  switch (champ) {
    case 'volume':
      return formatHl(Number(valeur))
    case 'degre':
      return formatDegre(Number(valeur))
    case 'prix':
      return formatPrix(Number(valeur))
    case 'acompte':
      return `${deux.format(Number(valeur))} %`
    case 'aRetirerAvantLe':
    case 'dateEnlevement':
      return formatDate(valeur)
    case 'derniere':
      return valeur === 'true' ? 'oui' : 'non'
    case 'appellation':
      return APPELLATIONS[valeur] ?? valeur
    case 'solde':
      return SOLDES[valeur] ?? valeur
    default:
      return valeur
  }
}
