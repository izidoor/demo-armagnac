import { redirect } from '@tanstack/react-router'
import { type Contrat } from '@/api/generated'
import { type Espace } from './api'

/** Un contrat a des retiraisons dès qu'il est conclu, et les garde exécuté ou annulé. */
export const aDesRetiraisons = (c: Contrat) =>
  c.etat !== 'PROJET' && c.etat !== 'REFUSE'

/**
 * Ce que ma partie peut faire des retiraisons : agir au portail sur un contrat conclu ; l'acheteur déclare tant qu'il
 * reste du volume ; la partie attendue valide ou modifie.
 */
export function droitsRetiraisons(espace: Espace, c: Contrat) {
  const agit = espace === 'portail' && c.peutAgir && c.etat === 'CONCLU'
  const resteTotal = c.restes.reduce((t, r) => t + r.reste, 0)
  return {
    agit,
    peutDeclarer: agit && c.maPartie === 'ACHETEUR' && resteTotal > 0,
    aValider: agit
      ? c.retiraisons.filter(
          (r) => r.attend !== undefined && r.attend === c.maPartie
        ).length
      : 0,
  }
}

/** La page du contrat, parente de celle de ses retiraisons. */
export const versContrat = (espace: Espace, id: number) =>
  espace === 'bo'
    ? ({ to: '/bo/contrats/$id', params: { id: String(id) } } as const)
    : ({ to: '/portail/contrats/$id', params: { id: String(id) } } as const)

/**
 * Avant d'ouvrir la page des retiraisons : sur ordinateur, elles sont dans la page du contrat ; un lien vers elles (une
 * notification push) y mène, à leur carte.
 */
export function retiraisonsSurOrdinateur(espace: Espace, id: string) {
  if (window.matchMedia('(min-width: 768px)').matches)
    throw redirect({
      ...versContrat(espace, Number(id)),
      hash: 'retiraisons',
      replace: true,
    })
}
