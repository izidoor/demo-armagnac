import { useState } from 'react'
import {
  CartesianGrid,
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
  SelecteurVue,
} from '@/components/metier/graphique-demo'
import {
  basculer,
  bruit,
  formatAxe,
  type Periode,
  periodesCiviles,
  STYLE_INFOBULLE,
  type VueCivile,
  VUES_CIVILES,
} from '@/components/metier/graphique-demo-outils'

/*
 * Graphique de démonstration : des exportations fictives, calculées dans le navigateur, pour montrer au client
 * ce que le tableau de bord présentera en production. Aucune donnée réelle n'y est lue.
 */

type Marche = {
  cle: string
  libelle: string
  couleur: string
  /** Volume mensuel moyen fictif en 2020, en hl AP. */
  base: number
  /** Croissance annuelle fictive. */
  croissance: number
  pointillee?: boolean
}

/** Les principaux pays d'exportation ; la couleur suit le pays, quels que soient les autres affichés. */
const MARCHES: Marche[] = [
  {
    cle: 'us',
    libelle: 'États-Unis',
    couleur: 'var(--chart-1)',
    base: 210,
    croissance: 0.03,
  },
  {
    cle: 'de',
    libelle: 'Allemagne',
    couleur: 'var(--chart-2)',
    base: 120,
    croissance: 0.01,
  },
  {
    cle: 'cn',
    libelle: 'Chine',
    couleur: 'var(--chart-3)',
    base: 70,
    croissance: 0.09,
  },
  {
    cle: 'gb',
    libelle: 'Royaume-Uni',
    couleur: 'var(--chart-4)',
    base: 95,
    croissance: -0.01,
  },
  {
    cle: 'be',
    libelle: 'Belgique',
    couleur: 'var(--primary)',
    base: 65,
    croissance: 0.02,
  },
  {
    cle: 'jp',
    libelle: 'Japon',
    couleur: 'var(--muted-foreground)',
    base: 50,
    croissance: 0.04,
    pointillee: true,
  },
]

/** Les sorties export culminent avant les fêtes de fin d'année. */
const SAISONS = [0.8, 0.8, 0.9, 0.9, 0.9, 0.9, 0.8, 0.7, 1, 1.2, 1.5, 1.6]

/** Volume fictif exporté vers un pays sur un mois (1 à 12), en hl AP : base, croissance, saison, ±10 %. */
function volume(marche: Marche, annee: number, mois: number) {
  const graine = MARCHES.indexOf(marche) * 1013 + annee * 37 + mois * 7 + 5
  return (
    marche.base *
    Math.pow(1 + marche.croissance, annee - 2020) *
    SAISONS[mois - 1] *
    (1 + 0.1 * bruit(graine))
  )
}

const somme = (marche: Marche, periode: Periode) =>
  Math.round(
    periode.mois.reduce((t, m) => t + volume(marche, m.annee, m.mois), 0)
  )

const hlAp = new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 })
const formatAxeHl = formatAxe('hl')
const formatInfobulle = (v: unknown) =>
  typeof v === 'number' ? `${hlAp.format(v)} hl AP` : String(v)

/**
 * Évolution des exportations vers les principaux pays, en courbes, sur l'une des trois vues ; chaque pays se
 * masque ou s'affiche.
 */
export function GraphiqueExportations() {
  const [vue, setVue] = useState<VueCivile>('annees')
  const [masquees, setMasquees] = useState<Set<string>>(new Set())
  const affiches = MARCHES.filter((m) => !masquees.has(m.cle))
  const points = periodesCiviles(vue).map((p) => {
    const point: Record<string, number | string> = { x: p.libelle }
    affiches.forEach((m) => (point[m.cle] = somme(m, p)))
    return point
  })
  return (
    <Card>
      <EnteteDemo
        titre='Exportations par pays'
        description="Sorties export déclarées vers les principaux pays, en hl d'alcool pur."
      />
      <CardContent className='space-y-3'>
        <SelecteurVue vues={VUES_CIVILES} vue={vue} onChange={setVue} />
        <ChoixCourbes
          courbes={MARCHES}
          masquees={masquees}
          onBasculer={(cle) => setMasquees((m) => basculer(m, cle))}
        />
        <div className='h-72 w-full'>
          {affiches.length === 0 ? (
            <p className='text-muted-foreground flex h-full items-center justify-center text-sm'>
              Choisissez au moins un pays.
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
                  tickFormatter={formatAxeHl}
                />
                <Tooltip
                  formatter={formatInfobulle}
                  contentStyle={STYLE_INFOBULLE}
                />
                {affiches.map((m) => (
                  <Line
                    key={m.cle}
                    dataKey={m.cle}
                    name={m.libelle}
                    stroke={m.couleur}
                    strokeWidth={2}
                    strokeDasharray={m.pointillee ? '5 4' : undefined}
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
