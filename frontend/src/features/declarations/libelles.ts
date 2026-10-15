import {
  ArrowDownToLine,
  ArrowUpFromLine,
  ClipboardList,
  Globe,
  type LucideIcon,
  Store,
} from 'lucide-react'
import { type Dsm, type LigneDsm } from '@/api/generated'

type EtatDsm = Dsm['etat']
export type CleConditionnement = LigneDsm['cle']
export type Onglet = Dsm['colonnes'][number]['onglet']

/** Glossaire : clés de conditionnement ; la première lettre décide de ce que porte la ligne (RG-DEC-10). */
export const CLES: Record<CleConditionnement, string> = {
  MB: 'Millésime sous bois',
  MV: 'Millésime sous verre',
  NB: 'Assemblage sous bois',
  NV: 'Assemblage sous verre',
}

export const millesimee = (cle: CleConditionnement) =>
  cle === 'MB' || cle === 'MV'

/** Ordre de la grille des comptes d'âge : 00, 0, 1… 20+ ; un compte inattendu va à la fin. */
export function rangCompte(compte: string) {
  if (compte === '00') return -1
  if (compte.endsWith('+')) return Number(compte.slice(0, -1)) + 0.5
  const n = Number(compte)
  return Number.isNaN(n) ? 1000 : n
}

export const TYPES_DECLARANT: Record<Dsm['type'], string> = {
  PRODUCTEUR: 'Producteur',
  NEGOCIANT: 'Négociant',
}

/** Les cinq onglets du format unique, dans l'ordre (§4.4.3.1), avec la phrase qui situe chacun. */
export const ONGLETS: {
  onglet: Onglet
  libelle: string
  icone: LucideIcon
  contexte: string
}[] = [
  {
    onglet: 'RECAPITULATIF',
    libelle: 'Récapitulatif',
    icone: ClipboardList,
    contexte:
      'Le stock de chaque ligne du début à la fin de la période : stock de début repris, achats, total des entrées et des sorties des autres onglets, stock de fin calculé.',
  },
  {
    onglet: 'MOUVEMENTS_ENTREE',
    libelle: 'Mouvements en entrée',
    icone: ArrowDownToLine,
    contexte:
      'Ce qui entre dans le stock de la ligne sans achat : distillation, passage sous bois ou sous verre, assemblages et coupes, transferts entre chais, réintégrations.',
  },
  {
    onglet: 'MOUVEMENTS_SORTIE',
    libelle: 'Mouvements en sortie',
    icone: ArrowUpFromLine,
    contexte:
      'Ce qui sort du stock de la ligne sans être vendu : pertes, passage sous bois ou sous verre, assemblages et coupes, transferts entre chais, Floc de Gascogne et autres fabrications.',
  },
  {
    onglet: 'VENTES_FRANCE',
    libelle: 'Ventes France',
    icone: Store,
    contexte:
      'Les ventes sur le marché intérieur, hors chai ou à un négociant, en bouteilles, en vrac ou en brandies.',
  },
  {
    onglet: 'VENTES_EXPORT',
    libelle: "Ventes à l'exportation",
    icone: Globe,
    contexte:
      "Les ventes à l'exportation, en bouteilles, en vrac ou en brandies, ainsi que les vins vinés et autres fabrications.",
  },
]

/** Variante du badge d'état d'une DSM. */
export const VARIANTE_ETAT: Record<
  EtatDsm,
  'default' | 'secondary' | 'outline' | 'destructive'
> = {
  BROUILLON: 'outline',
  ENVOYEE: 'secondary',
  EN_INSTRUCTION: 'secondary',
  COMPLEMENTS_DEMANDES: 'destructive',
  COMPLEMENTS_RECUS: 'secondary',
  VALIDEE: 'default',
}

/** Le millésime ou la dénomination d'une ligne, selon sa clé (RG-DEC-10) : « 2010 », « XO ». */
export function designation(l: {
  cle: CleConditionnement
  millesime?: number | null
  denomination?: string | null
}) {
  return String((millesimee(l.cle) ? l.millesime : l.denomination) ?? '')
}

/** L'identité d'une ligne, pour l'écran : « MB 2010 », « NB XO ». */
export function identite(l: {
  cle: CleConditionnement
  millesime?: number | null
  denomination?: string | null
}) {
  return `${l.cle} ${designation(l)}`
}
