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
import { type Croisement } from './calcul'

const MAX_SERIES = 12
const PALETTE = [
  'var(--chart-1)',
  'var(--chart-2)',
  'var(--secondary)',
  'var(--chart-3)',
  'var(--primary)',
  'var(--chart-4)',
]

const couleur = (i: number) =>
  i < PALETTE.length ? PALETTE[i] : `oklch(0.65 0.12 ${(i * 47) % 360})`

type Serie = { cle: string; libelle: string }
type Point = { x: string } & Record<string, number | string | undefined>

/**
 * Le graphique du tableau croisé, tiré du premier niveau de lignes et des colonnes. Une courbe quand le champ
 * non cumulable (une date) est en colonnes ou au premier niveau des lignes : l'évolution ; sinon un
 * histogramme empilé selon les colonnes.
 */
export function GraphiqueCroise({
  croisement,
  evolutionEnColonnes,
  evolutionEnLignes,
  format,
}: {
  croisement: Croisement
  /** Le champ non cumulable est en colonnes : une courbe par ligne de premier niveau. */
  evolutionEnColonnes: boolean
  /** Le champ non cumulable est au premier niveau des lignes : une courbe par colonne. */
  evolutionEnLignes: boolean
  format: (valeur: number) => string
}) {
  const racines = croisement.lignes.length
    ? croisement.lignes.filter((n) => Object.keys(n.valeurs).length > 0)
    : croisement.totaux
      ? [{ id: 'total', libelle: 'Total', valeurs: croisement.totaux.valeurs }]
      : []

  let series: Serie[]
  let points: Point[]
  if (evolutionEnColonnes) {
    series = racines.map((n, i) => ({ cle: `s${i}`, libelle: n.libelle }))
    points = croisement.colonnes.map((c) => {
      const p: Point = { x: c.libelle }
      racines.forEach((n, i) => (p[`s${i}`] = n.valeurs[c.cle]))
      return p
    })
  } else {
    series = croisement.colonnes.map((c, i) => ({
      cle: `s${i}`,
      libelle: c.libelle,
    }))
    points = racines.map((n) => {
      const p: Point = { x: n.libelle }
      croisement.colonnes.forEach((c, i) => (p[`s${i}`] = n.valeurs[c.cle]))
      return p
    })
  }
  const coupees = series.length > MAX_SERIES
  series = series.slice(0, MAX_SERIES)

  if (points.length === 0 || series.length === 0) {
    return (
      <p className='text-muted-foreground py-8 text-center text-sm'>
        Rien à représenter : un sous-total qui cumulerait un champ non cumulable
        (une date, un article pour une quantité) n&apos;est pas calculé.
        Placez-le en colonnes ou au premier niveau des lignes.
      </p>
    )
  }

  const courbe = evolutionEnColonnes || evolutionEnLignes
  const axes = (
    <>
      <CartesianGrid strokeDasharray='3 3' className='stroke-border' />
      <XAxis dataKey='x' tick={{ fontSize: 12 }} />
      <YAxis
        tick={{ fontSize: 12 }}
        width={90}
        tickFormatter={(v: number) => format(v)}
      />
      <Tooltip
        formatter={(v) => (typeof v === 'number' ? format(v) : String(v))}
        contentStyle={{
          background: 'var(--popover)',
          border: '1px solid var(--border)',
          borderRadius: 6,
          fontSize: 12,
        }}
      />
      {series.length > 1 && <Legend wrapperStyle={{ fontSize: 12 }} />}
    </>
  )
  return (
    <div className='space-y-1'>
      <div className='h-80 w-full'>
        <ResponsiveContainer width='100%' height='100%'>
          {courbe ? (
            <LineChart data={points}>
              {axes}
              {series.map((s, i) => (
                <Line
                  key={s.cle}
                  dataKey={s.cle}
                  name={s.libelle}
                  stroke={couleur(i)}
                  strokeWidth={2}
                  dot={{ r: 3 }}
                  connectNulls
                />
              ))}
            </LineChart>
          ) : (
            <BarChart data={points}>
              {axes}
              {series.map((s, i) => (
                <Bar
                  key={s.cle}
                  dataKey={s.cle}
                  name={s.libelle}
                  stackId='pile'
                  fill={couleur(i)}
                />
              ))}
            </BarChart>
          )}
        </ResponsiveContainer>
      </div>
      {coupees && (
        <p className='text-muted-foreground text-xs'>
          Les {MAX_SERIES} premières séries seulement.
        </p>
      )}
    </div>
  )
}
