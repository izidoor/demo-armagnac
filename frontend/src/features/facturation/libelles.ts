import { type Piece } from '@/api/generated'

type EtatPiece = Piece['etat']
type SensPiece = Piece['sens']

export const ETATS: Record<EtatPiece, string> = {
  BROUILLON: 'Brouillon',
  EMISE: 'Émise',
}

export const VARIANTE_ETAT: Record<EtatPiece, 'outline' | 'default'> = {
  BROUILLON: 'outline',
  EMISE: 'default',
}

export const SENS: Record<SensPiece, string> = {
  FACTURE: 'Facture',
  AVOIR: 'Avoir',
}

/** RG-FAC-08 : la nature de l'opération, mention obligatoire. */
export const NATURES_OPERATION: Record<
  NonNullable<Piece['natureOperation']>,
  string
> = {
  BIENS: 'Livraison de biens',
  SERVICES: 'Prestation de services',
  LES_DEUX: 'Livraison de biens et prestation de services',
}
