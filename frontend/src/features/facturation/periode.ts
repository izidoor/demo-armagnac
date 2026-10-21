import { useState } from 'react'

/** Une période d'émission, du… au…, bornes comprises (dates ISO). */
export type Periode = { du: string; au: string }

const deux = (n: number) => String(n).padStart(2, '0')
const iso = (annee: number, mois: number, jour: number) =>
  `${annee}-${deux(mois)}-${deux(jour)}`
/** Dernier jour d'un mois (1 à 12). */
const finDeMois = (annee: number, mois: number) =>
  new Date(annee, mois, 0).getDate()

function trimestre(annee: number, numero: number): Periode {
  const debut = (numero - 1) * 3 + 1
  return {
    du: iso(annee, debut, 1),
    au: iso(annee, debut + 2, finDeMois(annee, debut + 2)),
  }
}

/** Les raccourcis de période, calculés au jour du navigateur. */
export function raccourcis(): {
  id: string
  libelle: string
  periode: Periode
}[] {
  const jour = new Date()
  const annee = jour.getFullYear()
  const t = Math.floor(jour.getMonth() / 3) + 1
  const precedent = t === 1 ? trimestre(annee - 1, 4) : trimestre(annee, t - 1)
  return [
    {
      id: 'trimestre',
      libelle: 'Trimestre en cours',
      periode: trimestre(annee, t),
    },
    {
      id: 'trimestre-precedent',
      libelle: 'Trimestre précédent',
      periode: precedent,
    },
    {
      id: 'annee',
      libelle: 'Année en cours',
      periode: { du: iso(annee, 1, 1), au: iso(annee, 12, 31) },
    },
    {
      id: 'annee-precedente',
      libelle: 'Année précédente',
      periode: { du: iso(annee - 1, 1, 1), au: iso(annee - 1, 12, 31) },
    },
  ]
}

export const PERSONNALISEE = 'personnalisee'

/**
 * La période d'émission d'une page de lignes facturées (RG-FAC-30), l'année civile en cours par défaut ; valide
 * si elle va d'une date à une date postérieure ou égale.
 */
export function usePeriode() {
  const [periode, setPeriode] = useState<Periode>(
    raccourcis().find((c) => c.id === 'annee')!.periode
  )
  const valide = !!periode.du && !!periode.au && periode.du <= periode.au
  return { periode, setPeriode, valide }
}
