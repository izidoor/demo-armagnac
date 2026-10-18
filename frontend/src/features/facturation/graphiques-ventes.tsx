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
  periodesCiviles as periodes,
  type Periode,
  STYLE_INFOBULLE,
  type VueCivile as Vue,
  VUES_CIVILES as VUES,
} from '@/components/metier/graphique-demo-outils'
import { formatEuros } from '@/features/declarations/format'

/*
 * Graphiques de démonstration : des montants fictifs, calculés dans le navigateur, pour montrer au client ce
 * que la page *Ventes* présentera en production. Aucune donnée réelle n'y est lue.
 */

type Secteur =
  | 'cotisationsFrance'
  | 'cotisationsExport'
  | 'cotisationsEdvFrance'
  | 'cotisationsEdvExport'
  | 'boutique'
  | 'autre'

const COTISATIONS: Secteur[] = [
  'cotisationsFrance',
  'cotisationsExport',
  'cotisationsEdvFrance',
  'cotisationsEdvExport',
]
const SECTEURS: Secteur[] = [...COTISATIONS, 'boutique', 'autre']

/** Chiffre d'affaires mensuel moyen fictif, en euros HT, et saisonnalité (indice par mois, de janvier à décembre). */
const MODELE: Record<Secteur, { base: number; saisons: number[] }> = {
  cotisationsFrance: {
    base: 38000,
    saisons: [1.2, 0.8, 0.9, 0.9, 0.9, 0.9, 1, 0.7, 0.9, 1.1, 1.2, 1.5],
  },
  cotisationsExport: {
    base: 52000,
    saisons: [0.9, 0.8, 1, 1, 1, 0.9, 1, 0.8, 1, 1.1, 1.3, 1.2],
  },
  cotisationsEdvFrance: {
    base: 6000,
    saisons: [1, 1, 1, 1, 1, 1, 1, 0.8, 1, 1, 1.1, 1.1],
  },
  cotisationsEdvExport: {
    base: 9000,
    saisons: [1, 0.9, 1, 1, 1.1, 1, 1, 0.8, 1, 1, 1.1, 1.1],
  },
  boutique: {
    base: 4500,
    saisons: [0.6, 0.6, 0.7, 0.8, 0.9, 1.1, 1.5, 1.6, 0.9, 0.9, 1.2, 2.2],
  },
  autre: {
    base: 2000,
    saisons: [0.8, 1, 1.2, 0.9, 1, 1.1, 0.7, 0.6, 1, 1.3, 1.1, 1.2],
  },
}

/** Chiffre d'affaires fictif d'un secteur pour un mois (1 à 12) : base, croissance de 4 % l'an, saison, ±12 %. */
function montant(secteur: Secteur, annee: number, mois: number) {
  const { base, saisons } = MODELE[secteur]
  const croissance = Math.pow(1.04, annee - 2020)
  const graine = SECTEURS.indexOf(secteur) * 1013 + annee * 37 + mois * 7
  return Math.round(
    base * croissance * saisons[mois - 1] * (1 + 0.12 * bruit(graine))
  )
}

const somme = (secteurs: Secteur[], periode: Periode) =>
  periode.mois.reduce(
    (total, { annee, mois }) =>
      total + secteurs.reduce((s, sec) => s + montant(sec, annee, mois), 0),
    0
  )

const formatEurosAxe = formatAxe('€')
const formatInfobulle = (v: unknown) =>
  typeof v === 'number' ? formatEuros(v) : String(v)

/**
 * Chiffre d'affaires total HT, en histogramme, sur l'une des trois vues ; sur l'année en cours (la vue par
 * défaut), ses mois sont placés à côté de ceux de l'année précédente, sauf si l'on ôte la comparaison.
 */
function GraphiqueChiffreAffaires() {
  const [vue, setVue] = useState<Vue>('annee-en-cours')
  const [comparer, setComparer] = useState(true)
  const jour = new Date()
  const annee = jour.getFullYear()
  const comparaison = vue === 'annee-en-cours' && comparer
  const points = periodes(vue, jour).map((p, i) => ({
    x: p.libelle,
    // Les mois terminés seulement.
    total: p.mois.length ? somme(SECTEURS, p) : undefined,
    precedente: comparaison
      ? somme(SECTEURS, {
          libelle: '',
          mois: [{ annee: annee - 1, mois: i + 1 }],
        })
      : undefined,
  }))
  return (
    <Card>
      <EnteteDemo
        titre="Chiffre d'affaires total"
        description='Factures émises, avoirs déduits, en euros HT.'
      />
      <CardContent className='space-y-3'>
        <div className='flex flex-wrap items-center gap-3'>
          <SelecteurVue vues={VUES} vue={vue} onChange={setVue} />
          {vue === 'annee-en-cours' && (
            <Option
              id='ca-comparer'
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
                tickFormatter={formatEurosAxe}
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
                  name={String(annee - 1)}
                  fill='var(--chart-3)'
                  radius={[4, 4, 0, 0]}
                />
              )}
              <Bar
                dataKey='total'
                name={comparaison ? String(annee) : "Chiffre d'affaires"}
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

type Courbe = {
  cle: string
  libelle: string
  couleur: string
  secteurs: Secteur[]
  pointillee?: boolean
}

/** Les courbes possibles ; la couleur suit la courbe, quelles que soient les autres affichées. */
const COURBES: Courbe[] = [
  {
    cle: 'cotisations',
    libelle: 'Cotisations',
    couleur: 'var(--chart-1)',
    secteurs: COTISATIONS,
  },
  {
    cle: 'cotisationsFrance',
    libelle: 'Cotisations · sorties France',
    couleur: 'var(--chart-1)',
    secteurs: ['cotisationsFrance'],
  },
  {
    cle: 'cotisationsExport',
    libelle: 'Cotisations · sorties export',
    couleur: 'var(--chart-2)',
    secteurs: ['cotisationsExport'],
  },
  {
    cle: 'cotisationsEdvFrance',
    libelle: 'Cotisations · EDV France',
    couleur: 'var(--chart-3)',
    secteurs: ['cotisationsEdvFrance'],
  },
  {
    cle: 'cotisationsEdvExport',
    libelle: 'Cotisations · EDV export',
    couleur: 'var(--chart-4)',
    secteurs: ['cotisationsEdvExport'],
  },
  {
    cle: 'boutique',
    libelle: 'Boutique',
    couleur: 'var(--primary)',
    secteurs: ['boutique'],
  },
  {
    cle: 'autre',
    libelle: 'Autre',
    couleur: 'var(--muted-foreground)',
    secteurs: ['autre'],
    pointillee: true,
  },
]

const DETAIL = COTISATIONS as string[]

/**
 * Évolution du chiffre d'affaires HT par secteur de vente (cotisations, boutique, autre), en courbes, sur l'une
 * des trois vues. Les cotisations s'affichent en une courbe ou par ligne de cotisation ; chaque courbe se
 * masque ou s'affiche.
 */
function GraphiqueSecteurs() {
  const [vue, setVue] = useState<Vue>('annee-en-cours')
  const [detailler, setDetailler] = useState(false)
  const [masquees, setMasquees] = useState<Set<string>>(new Set())
  const proposees = COURBES.filter((c) =>
    detailler ? c.cle !== 'cotisations' : !DETAIL.includes(c.cle)
  )
  const affichees = proposees.filter((c) => !masquees.has(c.cle))
  const points = periodes(vue).map((p) => {
    const point: Record<string, number | string> = { x: p.libelle }
    if (p.mois.length)
      affichees.forEach((c) => (point[c.cle] = somme(c.secteurs, p)))
    return point
  })
  return (
    <Card>
      <EnteteDemo
        titre="Chiffre d'affaires par secteur"
        description='Évolution par secteur de vente, en euros HT.'
      />
      <CardContent className='space-y-3'>
        <div className='flex flex-wrap items-center gap-3'>
          <SelecteurVue vues={VUES} vue={vue} onChange={setVue} />
          <Option
            id='secteurs-detailler'
            libelle='Détailler les cotisations'
            valeur={detailler}
            onChange={setDetailler}
          />
        </div>
        <ChoixCourbes
          courbes={proposees}
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
                  tickFormatter={formatEurosAxe}
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
                    strokeDasharray={c.pointillee ? '5 4' : undefined}
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

/** Les deux graphiques de démonstration, côte à côte, au-dessus des données de la page *Ventes*. */
export function GraphiquesVentes() {
  return (
    <div className='grid gap-6 xl:grid-cols-2'>
      <GraphiqueChiffreAffaires />
      <GraphiqueSecteurs />
    </div>
  )
}
