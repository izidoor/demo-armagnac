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
  /** Volume annuel de référence, en hl AP : les sorties export de 2018 communiquées par le BNIA. */
  annuel: number
  /** Croissance annuelle fictive, autour de l'année en cours. */
  croissance: number
  pointillee?: boolean
}

/**
 * Les zones d'exportation, aux volumes réels de 2018 (4 589 hl AP au total), pris pour niveau de l'année en
 * cours ; Afrique (11 hl AP) et autres destinations sont regroupées. La couleur suit la zone, quelles que
 * soient les autres affichées.
 */
const MARCHES: Marche[] = [
  {
    cle: 'ue',
    libelle: 'UE hors France',
    couleur: 'var(--chart-1)',
    annuel: 2002,
    croissance: -0.02,
  },
  {
    cle: 'europe',
    libelle: 'Europe hors UE',
    couleur: 'var(--chart-2)',
    annuel: 820,
    croissance: 0.01,
  },
  {
    cle: 'asie',
    libelle: 'Asie',
    couleur: 'var(--chart-3)',
    annuel: 812,
    croissance: 0.03,
  },
  {
    cle: 'amerique',
    libelle: 'Amérique',
    couleur: 'var(--chart-4)',
    annuel: 563,
    croissance: 0.02,
  },
  {
    cle: 'autres',
    libelle: 'Afrique et autres',
    couleur: 'var(--muted-foreground)',
    annuel: 392,
    croissance: 0.04,
    pointillee: true,
  },
]

const ANNEE_DE_REFERENCE = new Date().getFullYear()

/** Les sorties export culminent avant les fêtes de fin d'année. */
const SAISONS = [0.8, 0.8, 0.9, 0.9, 0.9, 0.9, 0.8, 0.7, 1, 1.2, 1.5, 1.6]

/** Volume fictif exporté vers une zone sur un mois (1 à 12), en hl AP : référence, croissance, saison, ±10 %. */
function volume(marche: Marche, annee: number, mois: number) {
  const graine = MARCHES.indexOf(marche) * 1013 + annee * 37 + mois * 7 + 5
  return (
    (marche.annuel / 12) *
    Math.pow(1 + marche.croissance, annee - ANNEE_DE_REFERENCE) *
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
 * Évolution des exportations par zone géographique, en courbes, sur l'une des trois vues ; chaque zone se
 * masque ou s'affiche.
 */
export function GraphiqueExportations() {
  const [vue, setVue] = useState<VueCivile>('annees')
  const [masquees, setMasquees] = useState<Set<string>>(new Set())
  const affiches = MARCHES.filter((m) => !masquees.has(m.cle))
  const points = periodesCiviles(vue).map((p) => {
    const point: Record<string, number | string> = { x: p.libelle }
    if (p.mois.length) affiches.forEach((m) => (point[m.cle] = somme(m, p)))
    return point
  })
  return (
    <Card>
      <EnteteDemo
        titre='Exportations par zone'
        description="Sorties export déclarées par zone géographique, en hl d'alcool pur."
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
              Choisissez au moins une zone.
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
