import {
  creerCentreInteret,
  creerCircuitDistribution,
  creerLabel,
  creerLangue,
  creerMarque,
  creerProduitCommercialise,
  creerTitre,
  creerTypeVisite,
  creerZoneCommerciale,
  desactiverCentreInteret,
  desactiverCircuitDistribution,
  desactiverLabel,
  desactiverLangue,
  desactiverMarque,
  desactiverProduitCommercialise,
  desactiverTitre,
  desactiverTypeVisite,
  desactiverZoneCommerciale,
  listerCentresInteret,
  listerCircuitsDistribution,
  listerLabels,
  listerLangues,
  listerMarques,
  listerMarquesProches,
  listerProduitsCommercialises,
  listerTitres,
  listerTypesVisite,
  listerZonesCommerciales,
  modifierCentreInteret,
  modifierCircuitDistribution,
  modifierLabel,
  modifierLangue,
  modifierMarque,
  modifierProduitCommercialise,
  modifierTitre,
  modifierTypeVisite,
  modifierZoneCommerciale,
  reactiverCentreInteret,
  reactiverCircuitDistribution,
  reactiverLabel,
  reactiverLangue,
  reactiverMarque,
  reactiverProduitCommercialise,
  reactiverTitre,
  reactiverTypeVisite,
  reactiverZoneCommerciale,
  type ValeurSimple,
} from '@/api/generated'
import { type FiltreValeurs } from '../commun/use-filtre-valeurs'

/** Accès à une liste simple (code, libellé, actif), commun aux neuf listes (A-REF-3). */
type ApiListeSimple = {
  /** Racine des clés de cache de la liste. */
  cle: string
  lister: (filtre: FiltreValeurs) => Promise<ValeurSimple[]>
  creer: (saisie: { code: string; libelle: string }) => Promise<ValeurSimple>
  modifier: (code: string, libelle: string) => Promise<ValeurSimple>
  desactiver: (code: string) => Promise<ValeurSimple>
  reactiver: (code: string) => Promise<ValeurSimple>
  /** Valeurs au libellé proche, proposées pendant la saisie (marques, RG-REF-15). */
  proches?: (libelle: string) => Promise<ValeurSimple[]>
}

export type ConfigListeSimple = {
  titre: string
  description: string
  libelleNouveau: string
  api: ApiListeSimple
}

export const zonesCommerciales: ConfigListeSimple = {
  titre: 'Zones commerciales',
  description: 'Continents auxquels les pays sont rattachés (C.10.4).',
  libelleNouveau: 'Nouvelle zone commerciale',
  api: {
    cle: 'zones-commerciales',
    lister: (query) =>
      listerZonesCommerciales({ query, throwOnError: true }).then(
        (r) => r.data
      ),
    creer: (body) =>
      creerZoneCommerciale({ body, throwOnError: true }).then((r) => r.data),
    modifier: (code, libelle) =>
      modifierZoneCommerciale({
        path: { code },
        body: { libelle },
        throwOnError: true,
      }).then((r) => r.data),
    desactiver: (code) =>
      desactiverZoneCommerciale({ path: { code }, throwOnError: true }).then(
        (r) => r.data
      ),
    reactiver: (code) =>
      reactiverZoneCommerciale({ path: { code }, throwOnError: true }).then(
        (r) => r.data
      ),
  },
}

export const titres: ConfigListeSimple = {
  titre: 'Titres',
  description:
    "Civilités et formes d'appel des fiches, contacts et destinataires (C.10.8.1).",
  libelleNouveau: 'Nouveau titre',
  api: {
    cle: 'titres',
    lister: (query) =>
      listerTitres({ query, throwOnError: true }).then((r) => r.data),
    creer: (body) =>
      creerTitre({ body, throwOnError: true }).then((r) => r.data),
    modifier: (code, libelle) =>
      modifierTitre({
        path: { code },
        body: { libelle },
        throwOnError: true,
      }).then((r) => r.data),
    desactiver: (code) =>
      desactiverTitre({ path: { code }, throwOnError: true }).then(
        (r) => r.data
      ),
    reactiver: (code) =>
      reactiverTitre({ path: { code }, throwOnError: true }).then(
        (r) => r.data
      ),
  },
}

export const labels: ConfigListeSimple = {
  titre: 'Labels',
  description: 'Labels et distinctions (C.10.8.3).',
  libelleNouveau: 'Nouveau label',
  api: {
    cle: 'labels',
    lister: (query) =>
      listerLabels({ query, throwOnError: true }).then((r) => r.data),
    creer: (body) =>
      creerLabel({ body, throwOnError: true }).then((r) => r.data),
    modifier: (code, libelle) =>
      modifierLabel({
        path: { code },
        body: { libelle },
        throwOnError: true,
      }).then((r) => r.data),
    desactiver: (code) =>
      desactiverLabel({ path: { code }, throwOnError: true }).then(
        (r) => r.data
      ),
    reactiver: (code) =>
      reactiverLabel({ path: { code }, throwOnError: true }).then(
        (r) => r.data
      ),
  },
}

export const typesVisite: ConfigListeSimple = {
  titre: 'Types de visite',
  description:
    "Visites et prestations d'accueil proposées par les opérateurs (C.10.8.4).",
  libelleNouveau: 'Nouveau type de visite',
  api: {
    cle: 'types-visite',
    lister: (query) =>
      listerTypesVisite({ query, throwOnError: true }).then((r) => r.data),
    creer: (body) =>
      creerTypeVisite({ body, throwOnError: true }).then((r) => r.data),
    modifier: (code, libelle) =>
      modifierTypeVisite({
        path: { code },
        body: { libelle },
        throwOnError: true,
      }).then((r) => r.data),
    desactiver: (code) =>
      desactiverTypeVisite({ path: { code }, throwOnError: true }).then(
        (r) => r.data
      ),
    reactiver: (code) =>
      reactiverTypeVisite({ path: { code }, throwOnError: true }).then(
        (r) => r.data
      ),
  },
}

export const langues: ConfigListeSimple = {
  titre: 'Langues',
  description: 'Langues utilisées dans les fiches opérateurs (C.10.8.5).',
  libelleNouveau: 'Nouvelle langue',
  api: {
    cle: 'langues',
    lister: (query) =>
      listerLangues({ query, throwOnError: true }).then((r) => r.data),
    creer: (body) =>
      creerLangue({ body, throwOnError: true }).then((r) => r.data),
    modifier: (code, libelle) =>
      modifierLangue({
        path: { code },
        body: { libelle },
        throwOnError: true,
      }).then((r) => r.data),
    desactiver: (code) =>
      desactiverLangue({ path: { code }, throwOnError: true }).then(
        (r) => r.data
      ),
    reactiver: (code) =>
      reactiverLangue({ path: { code }, throwOnError: true }).then(
        (r) => r.data
      ),
  },
}

export const centresInteret: ConfigListeSimple = {
  titre: "Centres d'intérêt",
  description:
    "Domaines d'activité des contacts : comptabilité, marketing, production… (C.10.8.6).",
  libelleNouveau: "Nouveau centre d'intérêt",
  api: {
    cle: 'centres-interet',
    lister: (query) =>
      listerCentresInteret({ query, throwOnError: true }).then((r) => r.data),
    creer: (body) =>
      creerCentreInteret({ body, throwOnError: true }).then((r) => r.data),
    modifier: (code, libelle) =>
      modifierCentreInteret({
        path: { code },
        body: { libelle },
        throwOnError: true,
      }).then((r) => r.data),
    desactiver: (code) =>
      desactiverCentreInteret({ path: { code }, throwOnError: true }).then(
        (r) => r.data
      ),
    reactiver: (code) =>
      reactiverCentreInteret({ path: { code }, throwOnError: true }).then(
        (r) => r.data
      ),
  },
}

export const marques: ConfigListeSimple = {
  titre: 'Marques',
  description:
    'Marques des opérateurs, libellé unique à la casse et aux accents près (C.10.9.1, RG-REF-15).',
  libelleNouveau: 'Nouvelle marque',
  api: {
    cle: 'marques',
    lister: (query) =>
      listerMarques({ query, throwOnError: true }).then((r) => r.data),
    creer: (body) =>
      creerMarque({ body, throwOnError: true }).then((r) => r.data),
    modifier: (code, libelle) =>
      modifierMarque({
        path: { code },
        body: { libelle },
        throwOnError: true,
      }).then((r) => r.data),
    desactiver: (code) =>
      desactiverMarque({ path: { code }, throwOnError: true }).then(
        (r) => r.data
      ),
    reactiver: (code) =>
      reactiverMarque({ path: { code }, throwOnError: true }).then(
        (r) => r.data
      ),
    proches: (libelle) =>
      listerMarquesProches({ query: { libelle }, throwOnError: true }).then(
        (r) => r.data
      ),
  },
}

export const circuitsDistribution: ConfigListeSimple = {
  titre: 'Circuits de distribution',
  description: 'Circuits de distribution des opérateurs (C.10.9.2).',
  libelleNouveau: 'Nouveau circuit',
  api: {
    cle: 'circuits-distribution',
    lister: (query) =>
      listerCircuitsDistribution({ query, throwOnError: true }).then(
        (r) => r.data
      ),
    creer: (body) =>
      creerCircuitDistribution({ body, throwOnError: true }).then(
        (r) => r.data
      ),
    modifier: (code, libelle) =>
      modifierCircuitDistribution({
        path: { code },
        body: { libelle },
        throwOnError: true,
      }).then((r) => r.data),
    desactiver: (code) =>
      desactiverCircuitDistribution({
        path: { code },
        throwOnError: true,
      }).then((r) => r.data),
    reactiver: (code) =>
      reactiverCircuitDistribution({ path: { code }, throwOnError: true }).then(
        (r) => r.data
      ),
  },
}

export const produitsCommercialises: ConfigListeSimple = {
  titre: 'Produits commercialisés',
  description:
    'Types de produits commercialisés : Armagnac, Blanche, Floc… (C.10.9.3).',
  libelleNouveau: 'Nouveau produit commercialisé',
  api: {
    cle: 'produits-commercialises',
    lister: (query) =>
      listerProduitsCommercialises({ query, throwOnError: true }).then(
        (r) => r.data
      ),
    creer: (body) =>
      creerProduitCommercialise({ body, throwOnError: true }).then(
        (r) => r.data
      ),
    modifier: (code, libelle) =>
      modifierProduitCommercialise({
        path: { code },
        body: { libelle },
        throwOnError: true,
      }).then((r) => r.data),
    desactiver: (code) =>
      desactiverProduitCommercialise({
        path: { code },
        throwOnError: true,
      }).then((r) => r.data),
    reactiver: (code) =>
      reactiverProduitCommercialise({
        path: { code },
        throwOnError: true,
      }).then((r) => r.data),
  },
}
