import { type Statut } from '@/components/metier/badge-statut'

/** Les états d'un contrat (spec 06 §3.1), leur libellé et leur couleur. */
export const ETATS: Record<string, { libelle: string; statut: Statut }> = {
  PROJET: { libelle: 'Projet', statut: 'warning' },
  CONCLU: { libelle: 'Conclu', statut: 'success' },
  EXECUTE: { libelle: 'Exécuté', statut: 'neutre' },
  ANNULE: { libelle: 'Annulé', statut: 'danger' },
  REFUSE: { libelle: 'Refusé', statut: 'danger' },
}

export const PARTIES: Record<string, string> = {
  ACHETEUR: 'Acheteur',
  VENDEUR: 'Vendeur',
}

/** Les appellations (ADR-009), dans l'ordre du formulaire CA-05. */
export const APPELLATIONS: Record<string, string> = {
  BAS_ARMAGNAC: 'Bas Armagnac',
  ARMAGNAC_TENAREZE: 'Armagnac Ténarèze',
  HAUT_ARMAGNAC: 'Haut Armagnac',
  ARMAGNAC: 'Armagnac',
  BLANCHE_ARMAGNAC: 'Blanche Armagnac',
}

export const SOLDES: Record<string, string> = {
  COMPTANT: 'Comptant',
  TRENTE_JOURS_FIN_DE_MOIS: '30 jours fin de mois',
}

/** RG-CA-25 : les onglets de la liste du portail, dans l'ordre. */
export const ONGLETS = [
  { valeur: 'A_APPROUVER', libelle: 'À approuver', court: 'À approuver' },
  { valeur: 'EN_NEGOCIATION', libelle: 'En négociation', court: 'En négo.' },
  { valeur: 'CONCLUS', libelle: 'Conclus', court: 'Conclus' },
  { valeur: 'EXECUTES', libelle: 'Exécutés', court: 'Exécutés' },
  {
    valeur: 'ANNULES_REFUSES',
    libelle: 'Annulés ou refusés',
    court: 'Annulés',
  },
] as const

export type Onglet = (typeof ONGLETS)[number]['valeur']

/** Le libellé d'un champ de l'écart (RG-CA-09), tel que l'API le nomme. */
export const CHAMPS: Record<string, string> = {
  caracteristiques: 'Caractéristiques complémentaires',
  conditionsRetiraison: 'Conditions de retiraison',
  aRetirerAvantLe: 'À retirer avant le',
  solde: 'Paiement du solde',
  acompte: 'Acompte',
  appellation: 'Appellation',
  millesime: 'Millésime',
  denomination: 'Dénomination',
  compte: "Compte d'âge",
  volume: 'Volume',
  degre: 'Degré',
  prix: 'Prix',
  dateEnlevement: "Date d'enlèvement",
  derniere: 'Dernière retiraison',
}

/** Les états d'une retiraison (spec 06 §3.4), leur couleur. */
export const ETATS_RETIRAISON: Record<string, Statut> = {
  A_VALIDER_VENDEUR: 'warning',
  A_VALIDER_ACHETEUR: 'warning',
  VALIDEE: 'success',
}
