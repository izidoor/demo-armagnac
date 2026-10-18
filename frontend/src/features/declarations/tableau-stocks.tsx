import { useMemo } from 'react'
import { type HistoriqueStocks, type StockALaDate } from '@/api/generated'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  type Champ,
  type Mesure,
  TableauCroise,
  type Vue,
} from '@/components/metier/tableau-croise'
import { formatDate, formatLitres } from './format'
import { CLES, millesimee, rangCompte, TYPES_DECLARANT } from './libelles'

const STOCK: Mesure<StockALaDate> = {
  id: 'stock',
  libelle: 'Stock (L AP)',
  valeur: (l) => l.stock,
  format: formatLitres,
}

function champsStocks(
  lignes: StockALaDate[],
  avecOperateur: boolean
): Champ<StockALaDate>[] {
  const periodes = new Map(lignes.map((l) => [l.finDsm, l.periodeDsm]))
  return [
    {
      id: 'au',
      libelle: 'Date du stock',
      valeur: (l) => l.au,
      affichage: formatDate,
      nonCumulable: true,
    },
    ...(avecOperateur
      ? [
          {
            id: 'operateur',
            libelle: 'Opérateur',
            valeur: (l: StockALaDate) =>
              `${l.codeOperateur} · ${l.raisonSociale}`,
          },
          {
            id: 'type',
            libelle: 'Type',
            valeur: (l: StockALaDate) => l.type,
            affichage: (v: string) =>
              TYPES_DECLARANT[v as StockALaDate['type']] ?? v,
          },
        ]
      : []),
    {
      id: 'cle',
      libelle: 'Clé',
      valeur: (l) => l.cle,
      affichage: (v) => `${v} · ${CLES[v as StockALaDate['cle']] ?? ''}`,
    },
    {
      id: 'article',
      libelle: 'Millésime ou dénomination',
      valeur: (l) =>
        millesimee(l.cle) ? String(l.millesime ?? '') : (l.denomination ?? ''),
    },
    {
      id: 'compte',
      libelle: "Compte d'âge",
      valeur: (l) => l.compte,
      ordre: (a, b) => rangCompte(a) - rangCompte(b) || a.localeCompare(b),
    },
    {
      id: 'dsm',
      libelle: "DSM d'origine",
      valeur: (l) => l.finDsm,
      affichage: (v) => periodes.get(v) ?? v,
    },
  ]
}

const vue = (
  id: string,
  libelle: string,
  lignes: string[],
  colonnes: string[] = []
): Vue => ({
  id,
  libelle,
  composition: { lignes, colonnes, filtres: [], selection: {} },
})

const VUES: Vue[] = [
  vue('compte', "Par compte d'âge", ['compte']),
  vue('evolution', 'Évolution mois par mois', ['cle'], ['au']),
  vue('article', 'Par millésime ou dénomination', ['article'], ['cle']),
]
const VUES_BO: Vue[] = [
  ...VUES,
  vue('operateur', 'Par opérateur', ['operateur', 'cle']),
  vue('type', 'Par type de déclarant', ['type'], ['compte']),
]

/**
 * Le tableau croisé des stocks déclarés d'une année déclarative (spec 05 §6.1, §6.2, RG-DEC-44) : la date du
 * stock (une fin de mois) ne se cumule pas ; elle est en lignes, en colonnes, ou réduite à une date, la
 * dernière par défaut.
 */
export function TableauStocks({
  historique,
  avecOperateur,
  cle,
}: {
  historique: HistoriqueStocks
  /** Back-office : les dimensions opérateur et type. */
  avecOperateur: boolean
  /** Clé de la mémoire de la composition. */
  cle: string
}) {
  const champs = useMemo(
    () => champsStocks(historique.lignes, avecOperateur),
    [historique.lignes, avecOperateur]
  )
  if (historique.lignes.length === 0) {
    return (
      <p className='text-muted-foreground py-6 text-center text-sm'>
        Aucun stock déclaré sur l&apos;année {historique.annee.libelle} : pas de
        DSM validée finie à une fin de mois écoulée.
      </p>
    )
  }
  return (
    <TableauCroise
      cle={cle}
      donnees={historique.lignes}
      champs={champs}
      mesures={[STOCK]}
      vues={avecOperateur ? VUES_BO : VUES}
      nomFichier={`stocks-declares-${historique.annee.libelle}.xlsx`}
    />
  )
}

/** Le choix de l'année déclarative (du 1er avril au 31 mars) : on n'en interroge qu'une à la fois. */
export function ChoixAnnee({
  historique,
  valeur,
  onChange,
}: {
  historique: HistoriqueStocks | undefined
  valeur: number | undefined
  onChange: (annee: number) => void
}) {
  const annees = historique?.anneesDisponibles ?? []
  const choisie = valeur ?? historique?.annee.debutAnnee
  return (
    <div className='space-y-1'>
      <Label htmlFor='choix-annee-declarative'>Année déclarative</Label>
      <Select
        value={choisie === undefined ? undefined : String(choisie)}
        onValueChange={(v) => onChange(Number(v))}
      >
        <SelectTrigger id='choix-annee-declarative' className='w-48'>
          <SelectValue placeholder='…' />
        </SelectTrigger>
        <SelectContent>
          {annees.map((a) => (
            <SelectItem key={a.debutAnnee} value={String(a.debutAnnee)}>
              {a.libelle}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  )
}
