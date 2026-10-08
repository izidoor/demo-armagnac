import {
  type Alambic,
  type Doublon,
  type FicheOperateur,
  type LigneJournal,
  type Membre,
  type Invitation,
  type MedailleConcours,
  type PresentationOperateur,
} from '@/api/generated'

export type TypeOperateur = FicheOperateur['type']
export type Periodicite = NonNullable<FicheOperateur['periodicite']>
export type StatutDeclaration = FicheOperateur['statutDeclarationStock']
export type Appellation = FicheOperateur['appellations'][number]
export type TypeAlambic = Alambic['type']
export type NiveauDiffusion = FicheOperateur['niveauDiffusion']
export type Role = Membre['role']
type StatutRattachement = Membre['statut']
type StatutInvitation = Invitation['statut']
type Carte = PresentationOperateur['cartes'][number]
export type ModePresentation = NonNullable<
  PresentationOperateur['modePresentation']
>
export type Distinction = NonNullable<MedailleConcours['distinction']>

/** Type d'opérateur (RG-OPE-01). */
export const TYPES: Record<TypeOperateur, string> = {
  BNIA: 'BNIA',
  PRODUCTEUR: 'Producteur',
  NEGOCIANT: 'Négociant',
  AUTRE: 'Autre',
}

/** Types proposés à la saisie : le type BNIA ne se choisit pas (RG-OPE-39). */
export const TYPES_CHOISIS = {
  PRODUCTEUR: TYPES.PRODUCTEUR,
  NEGOCIANT: TYPES.NEGOCIANT,
  AUTRE: TYPES.AUTRE,
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

/** Rôles d'un rattachement (spec 02 §3.2.7, ADR-016). */
export const ROLES: Record<Role, string> = {
  OBSERVATEUR: 'Observateur',
  AGENT: 'Agent',
  ADMIN: 'Admin',
}

/** Ce que permet chaque rôle, pour le choisir en connaissance de cause (§3.2.7). */
export const ROLES_DESCRIPTION: Record<Role, string> = {
  OBSERVATEUR: 'Lit tout ce qui concerne l’opérateur.',
  AGENT:
    'Lit tout, et gère la fiche, les contacts et les fiches professionnelles ; au BNIA, aussi le Paramétrage.',
  ADMIN: 'Gère tout, dont les accès : invitations, rôles, suspensions.',
}

/** Statut d'un rattachement (RG-OPE-43). */
export const STATUTS_RATTACHEMENT: Record<StatutRattachement, string> = {
  ACTIF: 'Actif',
  SUSPENDU: 'Accès suspendu',
}

/** Statut d'une invitation (RG-OPE-41). */
export const STATUTS_INVITATION: Record<StatutInvitation, string> = {
  EN_ATTENTE: 'En attente',
  ACCEPTEE: 'Acceptée',
  ANNULEE: 'Annulée',
  EXPIREE: 'Expirée',
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

/** Cible d'une ligne du journal (RG-OPE-34). */
export const CIBLES_JOURNAL: Record<LigneJournal['cible'], string> = {
  CONTACT: 'Contact',
  MEMBRE: 'Membre',
  INVITATION: 'Invitation',
  UTILISATEUR: 'Utilisateur',
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
