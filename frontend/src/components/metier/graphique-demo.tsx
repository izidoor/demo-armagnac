import { cn } from '@/lib/utils'
import { Badge } from '@/components/ui/badge'
import { CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Label } from '@/components/ui/label'
import { Switch } from '@/components/ui/switch'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'

/*
 * Briques des graphiques de démonstration (pages *Ventes* et *Stocks déclarés*) : des données fictives, pour
 * montrer au client ce que ces pages présenteront en production.
 */

/** Titre du graphique, marqué « Démonstration », et sa description. */
export function EnteteDemo({
  titre,
  description,
}: {
  titre: string
  description: string
}) {
  return (
    <CardHeader>
      <CardTitle className='flex items-center gap-2 text-base'>
        {titre}
        <Badge variant='outline' className='font-normal'>
          Démonstration
        </Badge>
      </CardTitle>
      <CardDescription>{description}</CardDescription>
    </CardHeader>
  )
}

/** Le choix de la vue (la période représentée), en onglets compacts. */
export function SelecteurVue<V extends string>({
  vues,
  vue,
  onChange,
}: {
  vues: { id: V; libelle: string }[]
  vue: V
  onChange: (vue: V) => void
}) {
  return (
    <Tabs
      value={vue}
      onValueChange={(v) => onChange(v as V)}
      className='mt-0 gap-0'
    >
      <TabsList className='h-9'>
        {vues.map((v) => (
          <TabsTrigger key={v.id} value={v.id} className='px-2 text-sm'>
            {v.libelle}
          </TabsTrigger>
        ))}
      </TabsList>
    </Tabs>
  )
}

/** Un interrupteur et son libellé. */
export function Option({
  id,
  libelle,
  valeur,
  onChange,
}: {
  id: string
  libelle: string
  valeur: boolean
  onChange: (valeur: boolean) => void
}) {
  return (
    <div className='flex items-center gap-2'>
      <Switch id={id} checked={valeur} onCheckedChange={onChange} />
      <Label htmlFor={id} className='font-normal'>
        {libelle}
      </Label>
    </div>
  )
}

/** Les courbes proposées, en pastilles qui les affichent ou les masquent ; elles servent aussi de légende. */
export function ChoixCourbes({
  courbes,
  masquees,
  onBasculer,
}: {
  courbes: { cle: string; libelle: string; couleur: string }[]
  masquees: Set<string>
  onBasculer: (cle: string) => void
}) {
  return (
    <div className='flex flex-wrap gap-1.5' aria-label='Courbes affichées'>
      {courbes.map((c) => {
        const visible = !masquees.has(c.cle)
        return (
          <button
            key={c.cle}
            type='button'
            aria-pressed={visible}
            onClick={() => onBasculer(c.cle)}
            className={cn(
              'hover:bg-muted inline-flex items-center gap-1.5 rounded-md border px-2 py-0.5 text-xs transition-colors',
              !visible && 'text-muted-foreground opacity-60'
            )}
          >
            <span
              aria-hidden
              className='size-2.5 rounded-full'
              style={{
                background: visible ? c.couleur : 'transparent',
                border: `1.5px solid ${c.couleur}`,
              }}
            />
            {c.libelle}
          </button>
        )
      })}
    </div>
  )
}
