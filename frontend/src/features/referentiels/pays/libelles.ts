import { type Pays } from '@/api/generated'

/** Langue du certificat export : le français associé à une langue étrangère (C.7.2.1). */
export const LANGUES_CERTIFICAT: Record<Pays['langueCertificat'], string> = {
  FR_EN: 'Français / anglais',
  FR_ES: 'Français / espagnol',
  FR_IT: 'Français / italien',
}

/** Niveau de détail du certificat export (§4.8.5, C.7.2.1). */
export const NIVEAUX_DETAIL: Record<Pays['niveauDetailCertificat'], string> = {
  CUMUL_DEGRE: 'Simplifié (cumul par degré)',
  DETAIL_COMPTE_AGE: "Détaillé par compte d'âge",
  DETAIL_MILLESIME: 'Détaillé par millésime',
}
