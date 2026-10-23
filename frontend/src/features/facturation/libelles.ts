import { type Piece, type PieceListee } from '@/api/generated'

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

type EtatZeendoc = NonNullable<Piece['zeendoc']>

/** RG-FAC-32, 33 : l'état d'une pièce émise dans Zeendoc (simulé). */
export const ETATS_ZEENDOC: Record<EtatZeendoc, string> = {
  TRANSMISE: 'Transmis',
  PAYEE: 'Payé',
}

/** Badge plein : « Transmis » de la couleur principale, « Payé » en vert, celui des états aboutis. */
export const COULEUR_ZEENDOC: Record<EtatZeendoc, string> = {
  TRANSMISE: 'bg-primary text-primary-foreground',
  PAYEE: 'bg-success-foreground text-background',
}

/** Le jour, au format ISO des dates de l'API (`2026-10-23`), dans le fuseau du navigateur. */
function jourIso(date = new Date()) {
  const deux = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${deux(date.getMonth() + 1)}-${deux(date.getDate())}`
}

/**
 * RG-FAC-13, 33 : une facture échue et impayée, transmise à Zeendoc, non payée, son échéance passée. Le tableau de
 * bord du back-office les compte ; le tableau des pièces les teinte.
 */
export function echueImpayee(
  p: Pick<PieceListee, 'sens' | 'zeendoc' | 'echeance'>
) {
  return (
    p.sens === 'FACTURE' &&
    p.zeendoc === 'TRANSMISE' &&
    !!p.echeance &&
    p.echeance < jourIso()
  )
}
