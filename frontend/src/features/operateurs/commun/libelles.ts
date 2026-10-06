import {
  type Alambic,
  type Contact,
  type Doublon,
  type FicheOperateur,
  type LigneJournalContact,
  type MedailleConcours,
  type PresentationOperateur,
} from '@/api/generated'

export type TypeOperateur = FicheOperateur['type']
export type Periodicite = NonNullable<FicheOperateur['periodicite']>
export type StatutDeclaration = FicheOperateur['statutDeclarationStock']
export type Appellation = FicheOperateur['appellations'][number]
export type TypeAlambic = Alambic['type']
export type NiveauDiffusion = FicheOperateur['niveauDiffusion']
export type ModulePortail = Contact['modules'][number]
type Carte = PresentationOperateur['cartes'][number]
export type ModePresentation = NonNullable<
  PresentationOperateur['modePresentation']
>
export type Distinction = NonNullable<MedailleConcours['distinction']>

/** Type d'opérateur (RG-OPE-01). */
export const TYPES: Record<TypeOperateur, string> = {
  PRODUCTEUR: 'Producteur',
  NEGOCIANT: 'Négociant',
  AUTRE: 'Autre',
}

/** Périodicité DSM (RG-OPE-10). */
export const PERIODICITES: Record<Periodicite, string> = {
  MENSUELLE: 'Mensuelle',
  TRIMESTRIELLE: 'Trimestrielle',
  SEMESTRIELLE: 'Semestrielle',
  ANNUELLE: 'Annuelle',
}

/** Statut de déclaration de stock (RG-OPE-11, H-OPE-2). */
export const STATUTS: Record<StatutDeclaration, string> = {
  OUI: 'Oui : déclare',
  NON: 'Non : pas concerné',
  SOMMEIL: 'Sommeil : suspendu',
  CLOTURE: 'Clôturé : a cessé',
}

/** Appellations (RG-OPE-12). */
export const APPELLATIONS: Record<Appellation, string> = {
  ARMAGNAC: 'Armagnac',
  BAS_ARMAGNAC: 'Bas Armagnac',
  ARMAGNAC_TENAREZE: 'Armagnac Ténarèze',
  HAUT_ARMAGNAC: 'Haut Armagnac',
  BLANCHE_ARMAGNAC: 'Blanche Armagnac',
}

/** Types d'alambic (RG-OPE-13, H-OPE-3). */
export const TYPES_ALAMBIC: Record<TypeAlambic, string> = {
  ARMAGNACAIS: 'Armagnacais (continu)',
  DOUBLE_CHAUFFE: 'Double chauffe',
}

/** Diffusion des millésimes (RG-OPE-14). */
export const NIVEAUX_DIFFUSION: Record<NiveauDiffusion, string> = {
  REFUS: 'Refus',
  PARTIEL: 'Accord partiel',
  TOTAL: 'Accord total',
}

/** Modules du portail (RG-OPE-31). */
export const MODULES: Record<ModulePortail, string> = {
  DSM: 'Déclarations de stocks et mouvements',
  CONTRATS_ARMAGNAC: 'Contrats Armagnac',
  PLURIANNUELS_ARMAGNAC: 'Contrats pluriannuels Armagnac',
  PROPOSITIONS_ARMAGNAC: 'Propositions Armagnac',
  CONTRATS_VIN: 'Contrats vin',
  PLURIANNUELS_VIN: 'Contrats pluriannuels vin',
  PROPOSITIONS_VIN: 'Propositions vin',
  FINS_DE_TRAVAUX: 'Fins de travaux',
  BOUTIQUE: 'Boutique',
  FACTURES: 'Factures',
  CERTIFICATS_EXPORT: 'Certificats et ventes export',
  STATISTIQUES: 'Statistiques',
}

/** Cartes acceptées (RG-OPE-18). */
export const CARTES: Record<Carte, string> = {
  VISA: 'Visa',
  MASTERCARD: 'Mastercard',
  AMERICAN_EXPRESS: 'American Express',
}

export const MODES_PRESENTATION: Record<ModePresentation, string> = {
  ARMAGNAC_EXCLUSIVEMENT: 'Armagnac exclusivement',
  ARMAGNAC_ET_AUTRES_PRODUITS: 'Armagnac et autres produits',
}

/** Distinctions d'une médaille (RG-OPE-19). */
export const DISTINCTIONS: Record<Distinction, string> = {
  OR: 'Or',
  ARGENT: 'Argent',
  BRONZE: 'Bronze',
  AUTRE: 'Autre',
}

/** Actions du journal d'un contact (RG-OPE-34). */
export const ACTIONS_JOURNAL: Record<LigneJournalContact['action'], string> = {
  CREATION: 'Création',
  MODIFICATION: 'Modification',
  DESACTIVATION: 'Désactivation',
  REACTIVATION: 'Réactivation',
  SUPPRESSION: 'Suppression',
  ACCES_ACCORDE: 'Accès au portail accordé',
  ACCES_RETIRE: 'Accès au portail retiré',
  DROITS_MODIFIES: 'Droits modifiés',
}

/** Champs signalés en doublon (RG-OPE-06, 08). */
export const CHAMPS_DOUBLON: Record<Doublon['champ'], string> = {
  RAISON_SOCIALE: 'Raison sociale',
  CVI: 'CVI',
  SIRET: 'SIRET',
  ACCISE: "N° d'accise",
}

/** Les clés d'un dictionnaire de libellés, dans l'ordre de déclaration. */
export function cles<K extends string>(libelles: Record<K, string>): K[] {
  return Object.keys(libelles) as K[]
}
