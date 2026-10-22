import {
  creerCentreInteret,
  creerCircuitDistribution,
  creerLabel,
  creerLangue,
  creerMarque,
  creerMembreBnia,
  creerProduitCommercialise,
  creerTitre,
  creerTypeVisite,
  creerZoneCommerciale,
  desactiverCentreInteret,
  desactiverCircuitDistribution,
  desactiverLabel,
  desactiverLangue,
  desactiverMarque,
  desactiverMembreBnia,
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
  listerMembresBnia,
  listerProduitsCommercialises,
  listerTitres,
  listerTypesVisite,
  listerZonesCommerciales,
  modifierCentreInteret,
  modifierCircuitDistribution,
  modifierLabel,
  modifierLangue,
  modifierMarque,
  modifierMembreBnia,
  modifierProduitCommercialise,
  modifierTitre,
  modifierTypeVisite,
  modifierZoneCommerciale,
  reactiverCentreInteret,
  reactiverCircuitDistribution,
  reactiverLabel,
  reactiverLangue,
  reactiverMarque,
  reactiverMembreBnia,
  reactiverProduitCommercialise,
  reactiverTitre,
  reactiverTypeVisite,
  reactiverZoneCommerciale,
  supprimerCentreInteret,
  supprimerCircuitDistribution,
  supprimerLabel,
  supprimerLangue,
  supprimerMarque,
  supprimerMembreBnia,
  supprimerProduitCommercialise,
  supprimerTitre,
  supprimerTypeVisite,
  supprimerZoneCommerciale,
  type SaisieValeurSimple,
  type ValeurSimple,
} from '@/api/generated'
import { type FiltreValeurs } from '../commun/use-filtre-valeurs'

/**
 * Accès à une liste simple (code, libellé, actif), commun aux dix listes (A-REF-3). Les valeurs sont
 * désignées par leur identifiant ; le code se corrige (ADR-011).
 */
type ApiListeSimple = {
  /** Racine des clés de cache de la liste. */
  cle: string
  lister: (filtre: FiltreValeurs) => Promise<ValeurSimple[]>
  creer: (saisie: SaisieValeurSimple) => Promise<ValeurSimple>
  modifier: (id: number, saisie: SaisieValeurSimple) => Promise<ValeurSimple>
  desactiver: (id: number) => Promise<ValeurSimple>
  reactiver: (id: number) => Promise<ValeurSimple>
  /** Échoue en 409 si la valeur est utilisée ailleurs : la désactiver (RG-REF-04). */
  supprimer: (id: number) => Promise<unknown>
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
  description: 'Continents auxquels les pays sont rattachés.',
  libelleNouveau: 'Nouvelle zone commerciale',
  api: {
    cle: 'zones-commerciales',
    lister: (query) =>
      listerZonesCommerciales({ query, throwOnError: true }).then(
        (r) => r.data
      ),
    creer: (body) =>
      creerZoneCommerciale({ body, throwOnError: true }).then((r) => r.data),
    modifier: (id, body) =>
      modifierZoneCommerciale({ path: { id }, body, throwOnError: true }).then(
        (r) => r.data
      ),
    desactiver: (id) =>
      desactiverZoneCommerciale({ path: { id }, throwOnError: true }).then(
        (r) => r.data
      ),
    reactiver: (id) =>
      reactiverZoneCommerciale({ path: { id }, throwOnError: true }).then(
        (r) => r.data
      ),
    supprimer: (id) =>
      supprimerZoneCommerciale({ path: { id }, throwOnError: true }),
  },
}

export const titres: ConfigListeSimple = {
  titre: 'Titres',
  description:
    "Civilités et formes d'appel des fiches, contacts et destinataires.",
  libelleNouveau: 'Nouveau titre',
  api: {
    cle: 'titres',
    lister: (query) =>
      listerTitres({ query, throwOnError: true }).then((r) => r.data),
    creer: (body) =>
      creerTitre({ body, throwOnError: true }).then((r) => r.data),
    modifier: (id, body) =>
      modifierTitre({ path: { id }, body, throwOnError: true }).then(
        (r) => r.data
      ),
    desactiver: (id) =>
      desactiverTitre({ path: { id }, throwOnError: true }).then((r) => r.data),
    reactiver: (id) =>
      reactiverTitre({ path: { id }, throwOnError: true }).then((r) => r.data),
    supprimer: (id) => supprimerTitre({ path: { id }, throwOnError: true }),
  },
}

export const labels: ConfigListeSimple = {
  titre: 'Labels',
  description: 'Labels et distinctions.',
  libelleNouveau: 'Nouveau label',
  api: {
    cle: 'labels',
    lister: (query) =>
      listerLabels({ query, throwOnError: true }).then((r) => r.data),
    creer: (body) =>
      creerLabel({ body, throwOnError: true }).then((r) => r.data),
    modifier: (id, body) =>
      modifierLabel({ path: { id }, body, throwOnError: true }).then(
        (r) => r.data
      ),
    desactiver: (id) =>
      desactiverLabel({ path: { id }, throwOnError: true }).then((r) => r.data),
    reactiver: (id) =>
      reactiverLabel({ path: { id }, throwOnError: true }).then((r) => r.data),
    supprimer: (id) => supprimerLabel({ path: { id }, throwOnError: true }),
  },
}

export const typesVisite: ConfigListeSimple = {
  titre: 'Types de visite',
  description: "Visites et prestations d'accueil proposées par les opérateurs.",
  libelleNouveau: 'Nouveau type de visite',
  api: {
    cle: 'types-visite',
    lister: (query) =>
      listerTypesVisite({ query, throwOnError: true }).then((r) => r.data),
    creer: (body) =>
      creerTypeVisite({ body, throwOnError: true }).then((r) => r.data),
    modifier: (id, body) =>
      modifierTypeVisite({ path: { id }, body, throwOnError: true }).then(
        (r) => r.data
      ),
    desactiver: (id) =>
      desactiverTypeVisite({ path: { id }, throwOnError: true }).then(
        (r) => r.data
      ),
    reactiver: (id) =>
      reactiverTypeVisite({ path: { id }, throwOnError: true }).then(
        (r) => r.data
      ),
    supprimer: (id) =>
      supprimerTypeVisite({ path: { id }, throwOnError: true }),
  },
}

export const langues: ConfigListeSimple = {
  titre: 'Langues',
  description: 'Langues utilisées dans les fiches opérateurs.',
  libelleNouveau: 'Nouvelle langue',
  api: {
    cle: 'langues',
    lister: (query) =>
      listerLangues({ query, throwOnError: true }).then((r) => r.data),
    creer: (body) =>
      creerLangue({ body, throwOnError: true }).then((r) => r.data),
    modifier: (id, body) =>
      modifierLangue({ path: { id }, body, throwOnError: true }).then(
        (r) => r.data
      ),
    desactiver: (id) =>
      desactiverLangue({ path: { id }, throwOnError: true }).then(
        (r) => r.data
      ),
    reactiver: (id) =>
      reactiverLangue({ path: { id }, throwOnError: true }).then((r) => r.data),
    supprimer: (id) => supprimerLangue({ path: { id }, throwOnError: true }),
  },
}

export const centresInteret: ConfigListeSimple = {
  titre: "Centres d'intérêt",
  description:
    "Domaines d'activité des contacts : comptabilité, marketing, production….",
  libelleNouveau: "Nouveau centre d'intérêt",
  api: {
    cle: 'centres-interet',
    lister: (query) =>
      listerCentresInteret({ query, throwOnError: true }).then((r) => r.data),
    creer: (body) =>
      creerCentreInteret({ body, throwOnError: true }).then((r) => r.data),
    modifier: (id, body) =>
      modifierCentreInteret({ path: { id }, body, throwOnError: true }).then(
        (r) => r.data
      ),
    desactiver: (id) =>
      desactiverCentreInteret({ path: { id }, throwOnError: true }).then(
        (r) => r.data
      ),
    reactiver: (id) =>
      reactiverCentreInteret({ path: { id }, throwOnError: true }).then(
        (r) => r.data
      ),
    supprimer: (id) =>
      supprimerCentreInteret({ path: { id }, throwOnError: true }),
  },
}

export const membresBnia: ConfigListeSimple = {
  titre: 'Membres BNIA',
  description: 'Valeurs du champ « membre BNIA » de la fiche opérateur.',
  libelleNouveau: 'Nouveau membre BNIA',
  api: {
    cle: 'membres-bnia',
    lister: (query) =>
      listerMembresBnia({ query, throwOnError: true }).then((r) => r.data),
    creer: (body) =>
      creerMembreBnia({ body, throwOnError: true }).then((r) => r.data),
    modifier: (id, body) =>
      modifierMembreBnia({ path: { id }, body, throwOnError: true }).then(
        (r) => r.data
      ),
    desactiver: (id) =>
      desactiverMembreBnia({ path: { id }, throwOnError: true }).then(
        (r) => r.data
      ),
    reactiver: (id) =>
      reactiverMembreBnia({ path: { id }, throwOnError: true }).then(
        (r) => r.data
      ),
    supprimer: (id) =>
      supprimerMembreBnia({ path: { id }, throwOnError: true }),
  },
}

export const marques: ConfigListeSimple = {
  titre: 'Marques',
  description:
    'Marques des opérateurs, libellé unique à la casse et aux accents près.',
  libelleNouveau: 'Nouvelle marque',
  api: {
    cle: 'marques',
    lister: (query) =>
      listerMarques({ query, throwOnError: true }).then((r) => r.data),
    creer: (body) =>
      creerMarque({ body, throwOnError: true }).then((r) => r.data),
    modifier: (id, body) =>
      modifierMarque({ path: { id }, body, throwOnError: true }).then(
        (r) => r.data
      ),
    desactiver: (id) =>
      desactiverMarque({ path: { id }, throwOnError: true }).then(
        (r) => r.data
      ),
    reactiver: (id) =>
      reactiverMarque({ path: { id }, throwOnError: true }).then((r) => r.data),
    supprimer: (id) => supprimerMarque({ path: { id }, throwOnError: true }),
    proches: (libelle) =>
      listerMarquesProches({ query: { libelle }, throwOnError: true }).then(
        (r) => r.data
      ),
  },
}

export const circuitsDistribution: ConfigListeSimple = {
  titre: 'Circuits de distribution',
  description: 'Circuits de distribution des opérateurs.',
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
    modifier: (id, body) =>
      modifierCircuitDistribution({
        path: { id },
        body,
        throwOnError: true,
      }).then((r) => r.data),
    desactiver: (id) =>
      desactiverCircuitDistribution({ path: { id }, throwOnError: true }).then(
        (r) => r.data
      ),
    reactiver: (id) =>
      reactiverCircuitDistribution({ path: { id }, throwOnError: true }).then(
        (r) => r.data
      ),
    supprimer: (id) =>
      supprimerCircuitDistribution({ path: { id }, throwOnError: true }),
  },
}

export const produitsCommercialises: ConfigListeSimple = {
  titre: 'Produits commercialisés',
  description: 'Types de produits commercialisés : Armagnac, Blanche, Floc….',
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
    modifier: (id, body) =>
      modifierProduitCommercialise({
        path: { id },
        body,
        throwOnError: true,
      }).then((r) => r.data),
    desactiver: (id) =>
      desactiverProduitCommercialise({ path: { id }, throwOnError: true }).then(
        (r) => r.data
      ),
    reactiver: (id) =>
      reactiverProduitCommercialise({ path: { id }, throwOnError: true }).then(
        (r) => r.data
      ),
    supprimer: (id) =>
      supprimerProduitCommercialise({ path: { id }, throwOnError: true }),
  },
}
