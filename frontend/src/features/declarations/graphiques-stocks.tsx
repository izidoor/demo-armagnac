import { useState } from 'react'
import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { Card, CardContent } from '@/components/ui/card'
import {
  ChoixCourbes,
  EnteteDemo,
  Option,
  SelecteurVue,
} from '@/components/metier/graphique-demo'
import {
  basculer,
  bruit,
  formatAxe,
  libelleMois,
  STYLE_INFOBULLE,
} from '@/components/metier/graphique-demo-outils'

/*
 * Graphiques de démonstration : des stocks fictifs, calculés dans le navigateur, pour montrer au client ce que
 * la page *Stocks déclarés* présentera en production. Aucune donnée réelle n'y est lue. Un stock est un
 * instantané : celui d'une période est le stock à la fin de son dernier mois.
 */

type Vue = 'annees' | 'glissants' | 'annee-en-cours'

const VUES: { id: Vue; libelle: string }[] = [
  { id: 'annees', libelle: '5 dernières années' },
  { id: 'glissants', libelle: '12 mois glissants' },
  { id: 'annee-en-cours', libelle: 'Année en cours' },
]

type Type = 'NEGOCIANT' | 'PRODUCTEUR'
type Tranche = 'jeunes' | '1-3' | '4-9' | '10-19' | '20+'

const TYPES: Type[] = ['NEGOCIANT', 'PRODUCTEUR']
const TRANCHES: Tranche[] = ['jeunes', '1-3', '4-9', '10-19', '20+']

/** Stock moyen fictif par type de déclarant, en hl AP, et part de chaque tranche de compte d'âge. */
const BASE: Record<Type, number> = { NEGOCIANT: 30000, PRODUCTEUR: 12000 }
const PART: Record<Tranche, number> = {
  jeunes: 0.28,
  '1-3': 0.27,
  '4-9': 0.22,
  '10-19': 0.14,
  '20+': 0.09,
}
/** Niveau du stock à chaque fin de mois (janvier à décembre) : il monte avec la distillation, puis s'écoule. */
const SAISONS = [
  1.04, 1.07, 1.08, 1.06, 1.03, 1, 0.98, 0.97, 0.96, 0.95, 0.98, 1.01,
]

/** Stock fictif, en hl AP, à la fin d'un mois (1 à 12) : croissance de 1,5 % l'an, saison (accentuée pour les jeunes comptes), ±4 %. */
function stock(type: Type, tranche: Tranche, annee: number, mois: number) {
  const saison = SAISONS[mois - 1]
  const s = tranche === 'jeunes' ? 1 + (saison - 1) * 3 : saison
  const graine =
    TYPES.indexOf(type) * 2027 +
    TRANCHES.indexOf(tranche) * 1013 +
    annee * 37 +
    mois * 7
  return Math.round(
    BASE[type] *
      PART[tranche] *
      Math.pow(1.015, annee - 2020) *
      s *
      (1 + 0.04 * bruit(graine))
  )
}

const total = (
  types: Type[],
  tranches: Tranche[],
  { annee, mois }: FinDeMois
) =>
  types.reduce(
    (t, ty) =>
      t + tranches.reduce((s, tr) => s + stock(ty, tr, annee, mois), 0),
    0
  )

type FinDeMois = { annee: number; mois: number }
/** Un instant de l'abscisse ; sans fin de mois s'il n'est pas encore passé (pas de valeur). */
type Instant = { libelle: string; fin?: FinDeMois }

const moisApres = (annee: number, mois: number, decalage: number) => {
  const d = new Date(annee, mois - 1 + decalage, 1)
  return { annee: d.getFullYear(), mois: d.getMonth() + 1 }
}

/** Première année de l'année déclarative en cours (du 1er avril au 31 mars). */
const anneeDeclarative = (jour: Date) =>
  jour.getMonth() + 1 >= 4 ? jour.getFullYear() : jour.getFullYear() - 1

/**
 * Les instants d'une vue, au jour du navigateur, chacun une fin de mois terminé : le 31 mars des cinq
 * dernières années déclaratives closes, les douze dernières fins de mois, ou les douze de l'année déclarative
 * en cours, celles à venir sans valeur.
 */
function instants(vue: Vue, jour = new Date()): Instant[] {
  const courante = anneeDeclarative(jour)
  if (vue === 'annees') {
    return Array.from({ length: 5 }, (_, i) => {
      const debut = courante - 5 + i
      return {
        libelle: `${debut}-${String(debut + 1).slice(2)}`,
        fin: { annee: debut + 1, mois: 3 },
      }
    })
  }
  if (vue === 'glissants') {
    return Array.from({ length: 12 }, (_, i) => {
      const fin = moisApres(jour.getFullYear(), jour.getMonth() + 1, i - 12)
      return { libelle: libelleMois(fin.annee, fin.mois), fin }
    })
  }
  const aujourdhui = jour.getFullYear() * 12 + jour.getMonth() + 1
  return Array.from({ length: 12 }, (_, i) => {
    const fin = moisApres(courante, 4, i)
    return {
      libelle: libelleMois(fin.annee, fin.mois, false),
      fin: fin.annee * 12 + fin.mois < aujourdhui ? fin : undefined,
    }
  })
}

const hlAp = new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 })
const formatHlAp = (v: number) => `${hlAp.format(v)} hl AP`
const formatHlApAxe = formatAxe('hl')
const formatInfobulle = (v: unknown) =>
  typeof v === 'number' ? formatHlAp(v) : String(v)

const libelleAnnee = (debut: number) => `${debut}-${String(debut + 1).slice(2)}`

/**
 * Stock total déclaré, en histogramme, sur l'une des trois vues ; sur l'année déclarative en cours (la vue par
 * défaut), ses fins de mois sont placées à côté de celles de l'année précédente, sauf si l'on ôte la comparaison.
 */
function GraphiqueStockTotal() {
  const [vue, setVue] = useState<Vue>('annee-en-cours')
  const [comparer, setComparer] = useState(true)
  const jour = new Date()
  const courante = anneeDeclarative(jour)
  const comparaison = vue === 'annee-en-cours' && comparer
  const points = instants(vue, jour).map((p, i) => ({
    x: p.libelle,
    stock: p.fin && total(TYPES, TRANCHES, p.fin),
    precedente: comparaison
      ? total(TYPES, TRANCHES, moisApres(courante - 1, 4, i))
      : undefined,
  }))
  return (
    <Card>
      <EnteteDemo
        titre='Stock total déclaré'
        description="Stock de fin des DSM validées, en hl d'alcool pur."
      />
      <CardContent className='space-y-3'>
        <div className='flex flex-wrap items-center gap-3'>
          <SelecteurVue vues={VUES} vue={vue} onChange={setVue} />
          {vue === 'annee-en-cours' && (
            <Option
              id='stock-comparer'
              libelle='Comparer avec année précédente'
              valeur={comparer}
              onChange={setComparer}
            />
          )}
        </div>
        <div className='h-72 w-full'>
          <ResponsiveContainer width='100%' height='100%'>
            <BarChart data={points} barGap={2}>
              <CartesianGrid
                strokeDasharray='3 3'
                vertical={false}
                className='stroke-border'
              />
              <XAxis dataKey='x' tick={{ fontSize: 12 }} />
              <YAxis
                tick={{ fontSize: 12 }}
                width={70}
                tickFormatter={formatHlApAxe}
              />
              <Tooltip
                formatter={formatInfobulle}
                cursor={{ fill: 'var(--muted)', opacity: 0.5 }}
                contentStyle={STYLE_INFOBULLE}
              />
              {comparaison && <Legend wrapperStyle={{ fontSize: 12 }} />}
              {comparaison && (
                <Bar
                  dataKey='precedente'
                  name={libelleAnnee(courante - 1)}
                  fill='var(--chart-3)'
                  radius={[4, 4, 0, 0]}
                />
              )}
              <Bar
                dataKey='stock'
                name={comparaison ? libelleAnnee(courante) : 'Stock'}
                fill='var(--chart-1)'
                radius={[4, 4, 0, 0]}
              />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  )
}

/** Les courbes, une par tranche de compte d'âge ; la couleur suit la tranche. */
const COURBES: { cle: Tranche; libelle: string; couleur: string }[] = [
  { cle: 'jeunes', libelle: 'Comptes 00 et 0', couleur: 'var(--chart-1)' },
  { cle: '1-3', libelle: 'Comptes 1 à 3', couleur: 'var(--chart-2)' },
  { cle: '4-9', libelle: 'Comptes 4 à 9', couleur: 'var(--chart-3)' },
  { cle: '10-19', libelle: 'Comptes 10 à 19', couleur: 'var(--chart-4)' },
  { cle: '20+', libelle: 'Compte 20+', couleur: 'var(--primary)' },
]

type ChoixType = 'tous' | Type

const CHOIX_TYPES: { id: ChoixType; libelle: string }[] = [
  { id: 'tous', libelle: 'Tous' },
  { id: 'NEGOCIANT', libelle: 'Négociants' },
  { id: 'PRODUCTEUR', libelle: 'Producteurs' },
]

/**
 * Évolution du stock déclaré par tranche de compte d'âge, en courbes, sur l'une des trois vues, pour tous les
 * déclarants ou un type ; chaque courbe se masque ou s'affiche.
 */
export function GraphiqueComptes() {
  const [vue, setVue] = useState<Vue>('annee-en-cours')
  const [type, setType] = useState<ChoixType>('tous')
  const [masquees, setMasquees] = useState<Set<string>>(new Set())
  const types = type === 'tous' ? TYPES : [type]
  const affichees = COURBES.filter((c) => !masquees.has(c.cle))
  const points = instants(vue).map((p) => {
    const point: Record<string, number | string> = { x: p.libelle }
    const fin = p.fin
    if (fin)
      affichees.forEach((c) => (point[c.cle] = total(types, [c.cle], fin)))
    return point
  })
  return (
    <Card>
      <EnteteDemo
        titre="Stock par compte d'âge"
        description="Évolution par tranche de compte d'âge, en hl d'alcool pur."
      />
      <CardContent className='space-y-3'>
        <div className='flex flex-wrap items-center gap-3'>
          <SelecteurVue vues={VUES} vue={vue} onChange={setVue} />
          <SelecteurVue vues={CHOIX_TYPES} vue={type} onChange={setType} />
        </div>
        <ChoixCourbes
          courbes={COURBES}
          masquees={masquees}
          onBasculer={(cle) => setMasquees((m) => basculer(m, cle))}
        />
        <div className='h-72 w-full'>
          {affichees.length === 0 ? (
            <p className='text-muted-foreground flex h-full items-center justify-center text-sm'>
              Choisissez au moins une courbe.
            </p>
          ) : (
            <ResponsiveContainer width='100%' height='100%'>
              <LineChart data={points}>
                <CartesianGrid
                  strokeDasharray='3 3'
                  vertical={false}
                  className='stroke-border'
                />
                <XAxis dataKey='x' tick={{ fontSize: 12 }} />
                <YAxis
                  tick={{ fontSize: 12 }}
                  width={70}
                  tickFormatter={formatHlApAxe}
                />
                <Tooltip
                  formatter={formatInfobulle}
                  contentStyle={STYLE_INFOBULLE}
                />
                {affichees.map((c) => (
                  <Line
                    key={c.cle}
                    dataKey={c.cle}
                    name={c.libelle}
                    stroke={c.couleur}
                    strokeWidth={2}
                    dot={{ r: 3 }}
                    activeDot={{ r: 5 }}
                  />
                ))}
              </LineChart>
            </ResponsiveContainer>
          )}
        </div>
      </CardContent>
    </Card>
  )
}

/** Les deux graphiques de démonstration, côte à côte, au-dessus des données de la page *Stocks déclarés*. */
export function GraphiquesStocks() {
  return (
    <div className='grid gap-6 xl:grid-cols-2'>
      <GraphiqueStockTotal />
      <GraphiqueComptes />
    </div>
  )
}
