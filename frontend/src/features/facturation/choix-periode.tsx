import { type ReactNode } from 'react'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { type Periode, PERSONNALISEE, raccourcis } from './periode'

/**
 * Le choix de la période : raccourcis (trimestre en cours, trimestre précédent, année en cours, année
 * précédente) ou dates d'émission du… au… ; les actions de la page à droite.
 */
export function ChoixPeriode({
  id,
  periode,
  valide,
  onChange,
  actions,
}: {
  /** Préfixe des identifiants des champs. */
  id: string
  periode: Periode
  valide: boolean
  onChange: (periode: Periode) => void
  actions?: ReactNode
}) {
  const choix = raccourcis()
  const raccourci = choix.find(
    (c) => c.periode.du === periode.du && c.periode.au === periode.au
  )
  return (
    <>
      <div className='mb-4 flex flex-wrap items-end gap-3'>
        <div className='space-y-1'>
          <Label htmlFor={`${id}-raccourci`}>Période</Label>
          <Select
            value={raccourci?.id ?? PERSONNALISEE}
            onValueChange={(v) => {
              const c = choix.find((r) => r.id === v)
              if (c) onChange(c.periode)
            }}
          >
            <SelectTrigger id={`${id}-raccourci`} className='w-48'>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {choix.map((c) => (
                <SelectItem key={c.id} value={c.id}>
                  {c.libelle}
                </SelectItem>
              ))}
              {!raccourci && (
                <SelectItem value={PERSONNALISEE} disabled>
                  Personnalisée
                </SelectItem>
              )}
            </SelectContent>
          </Select>
        </div>
        <div className='space-y-1'>
          <Label htmlFor={`${id}-du`}>Émises du</Label>
          <Input
            id={`${id}-du`}
            type='date'
            className='w-40'
            value={periode.du}
            onChange={(e) => onChange({ ...periode, du: e.target.value })}
          />
        </div>
        <div className='space-y-1'>
          <Label htmlFor={`${id}-au`}>au</Label>
          <Input
            id={`${id}-au`}
            type='date'
            className='w-40'
            value={periode.au}
            onChange={(e) => onChange({ ...periode, au: e.target.value })}
          />
        </div>
        {actions && <div className='ms-auto'>{actions}</div>}
      </div>
      {!valide && (
        <p className='text-destructive mb-4 text-sm'>
          La période va d&apos;une date à une date postérieure ou égale.
        </p>
      )}
    </>
  )
}
