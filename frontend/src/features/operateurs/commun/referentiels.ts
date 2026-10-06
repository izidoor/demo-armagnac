import { useQuery } from '@tanstack/react-query'
import {
  listerCentresInteret,
  listerCircuitsDistribution,
  listerLabels,
  listerLangues,
  listerMarques,
  listerMembresBnia,
  listerPays,
  listerProduitsCommercialises,
  listerRegions,
  listerTitres,
  listerTypesVisite,
} from '@/api/generated'

/** Valeur de référentiel telle que la fiche opérateur l'affiche et la propose. */
export type Valeur = {
  id: number
  code: string
  libelle: string
  actif: boolean
}

const LISTES = {
  titres: listerTitres,
  pays: listerPays,
  regions: listerRegions,
  membresBnia: listerMembresBnia,
  marques: listerMarques,
  circuits: listerCircuitsDistribution,
  typesVisite: listerTypesVisite,
  labels: listerLabels,
  langues: listerLangues,
  produits: listerProduitsCommercialises,
  centresInteret: listerCentresInteret,
} as const

type NomListe = keyof typeof LISTES

/** Clés de cache de referentiels : invalidées par leurs propres écrans (`['pays']`…). */
const CLES: Record<NomListe, string> = {
  titres: 'titres',
  pays: 'pays',
  regions: 'regions',
  membresBnia: 'membres-bnia',
  marques: 'marques',
  circuits: 'circuits-distribution',
  typesVisite: 'types-visite',
  labels: 'labels',
  langues: 'langues',
  produits: 'produits-commercialises',
  centresInteret: 'centres-interet',
}

/**
 * Une liste de referentiels, actives et inactives : une valeur inactive reste affichée là où elle est
 * déjà utilisée, mais n'est plus proposée (RG-OPE-21).
 */
export function useListe(nom: NomListe) {
  const requete = useQuery({
    queryKey: [CLES[nom], {}],
    queryFn: () =>
      LISTES[nom]({ throwOnError: true }).then((r) => r.data as Valeur[]),
    staleTime: 60 * 1000,
  })
  const valeurs = requete.data ?? []
  return {
    valeurs,
    libelle: (id?: number | null) =>
      id == null
        ? undefined
        : (valeurs.find((v) => v.id === id)?.libelle ?? `n° ${id}`),
    /** Les valeurs actives, plus celles déjà portées (RG-OPE-21). */
    proposees: (portees: (number | null | undefined)[] = []) =>
      valeurs.filter((v) => v.actif || portees.includes(v.id)),
  }
}

export type Liste = ReturnType<typeof useListe>
