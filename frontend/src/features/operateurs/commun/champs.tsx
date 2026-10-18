import { type ReactNode } from 'react'
import { type Control, type FieldPath, type FieldValues } from 'react-hook-form'
import { cn } from '@/lib/utils'
import { Checkbox } from '@/components/ui/checkbox'
import {
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Switch } from '@/components/ui/switch'
import { Textarea } from '@/components/ui/textarea'
import { AUCUN } from './saisie'

type Commun<T extends FieldValues> = {
  control: Control<T>
  name: FieldPath<T>
  label: string
  description?: ReactNode
  className?: string
  disabled?: boolean
}

export function ChampTexte<T extends FieldValues>({
  control,
  name,
  label,
  description,
  className,
  disabled,
  multiligne,
  ...props
}: Commun<T> & { multiligne?: boolean; placeholder?: string; list?: string }) {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem className={className}>
          <FormLabel>{label}</FormLabel>
          <FormControl>
            {multiligne ? (
              <Textarea {...field} disabled={disabled} rows={4} />
            ) : (
              <Input
                {...field}
                {...props}
                disabled={disabled}
                autoComplete='off'
              />
            )}
          </FormControl>
          {description && <FormDescription>{description}</FormDescription>}
          <FormMessage />
        </FormItem>
      )}
    />
  )
}

/** Choix dans une liste fermée, ou d'une valeur de référentiel (identifiant sous forme de texte). */
export function ChampChoix<T extends FieldValues>({
  control,
  name,
  label,
  description,
  className,
  disabled,
  options,
  facultatif,
}: Commun<T> & {
  options: { valeur: string; libelle: string }[]
  /** Ajoute le choix « — » ({@link AUCUN}). */
  facultatif?: boolean
}) {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem className={className}>
          <FormLabel>{label}</FormLabel>
          <Select
            value={field.value}
            onValueChange={field.onChange}
            disabled={disabled}
          >
            <FormControl>
              <SelectTrigger className='w-full'>
                <SelectValue placeholder='Choisir' />
              </SelectTrigger>
            </FormControl>
            <SelectContent>
              {facultatif && <SelectItem value={AUCUN}>—</SelectItem>}
              {options.map((o) => (
                <SelectItem key={o.valeur} value={o.valeur}>
                  {o.libelle}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {description && <FormDescription>{description}</FormDescription>}
          <FormMessage />
        </FormItem>
      )}
    />
  )
}

export function ChampInterrupteur<T extends FieldValues>({
  control,
  name,
  label,
  description,
  className,
  disabled,
}: Commun<T>) {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem className={cn('flex flex-col justify-end', className)}>
          <div className='flex min-h-9 items-center gap-2'>
            <FormControl>
              <Switch
                checked={field.value}
                onCheckedChange={field.onChange}
                disabled={disabled}
              />
            </FormControl>
            <FormLabel className='font-normal'>{label}</FormLabel>
          </div>
          {description && <FormDescription>{description}</FormDescription>}
          <FormMessage />
        </FormItem>
      )}
    />
  )
}

/** Choix multiple par cases à cocher ; la valeur du champ est un tableau. */
export function ChampCases<T extends FieldValues, V extends string | number>({
  control,
  name,
  label,
  description,
  className,
  disabled,
  options,
}: Commun<T> & { options: { valeur: V; libelle: string }[] }) {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => {
        const valeurs = (field.value ?? []) as V[]
        return (
          <FormItem className={className}>
            <FormLabel>{label}</FormLabel>
            <div className='grid gap-2 sm:grid-cols-2'>
              {options.map((o) => (
                <label
                  key={String(o.valeur)}
                  className='flex items-center gap-2 text-sm'
                >
                  <Checkbox
                    checked={valeurs.includes(o.valeur)}
                    disabled={disabled}
                    onCheckedChange={(coche) =>
                      field.onChange(
                        coche
                          ? [...valeurs, o.valeur]
                          : valeurs.filter((v) => v !== o.valeur)
                      )
                    }
                  />
                  {o.libelle}
                </label>
              ))}
              {options.length === 0 && (
                <span className='text-muted-foreground text-sm'>
                  Aucune valeur active.
                </span>
              )}
            </div>
            {description && <FormDescription>{description}</FormDescription>}
            <FormMessage />
          </FormItem>
        )
      }}
    />
  )
}

/** Champ en lecture seule (portail, RG-OPE-36). */
export function Lecture({
  label,
  valeur,
  className,
}: {
  label: string
  valeur?: ReactNode
  className?: string
}) {
  return (
    <div className={cn('space-y-1', className)}>
      <div className='text-sm font-medium'>{label}</div>
      <div className='text-muted-foreground text-sm'>{valeur || '—'}</div>
    </div>
  )
}

/** Une liste en lecture, une valeur par ligne ; vide, un texte qui le dit (« Aucune marque »). */
export function LectureListe({
  label,
  valeurs,
  vide,
  className,
}: {
  label?: string
  valeurs: ReactNode[]
  vide: string
  className?: string
}) {
  return (
    <div className={cn('space-y-1', className)}>
      {label && <div className='text-sm font-medium'>{label}</div>}
      {valeurs.length === 0 ? (
        <div className='text-muted-foreground text-sm italic'>{vide}</div>
      ) : (
        <ul className='text-muted-foreground space-y-1 text-sm'>
          {valeurs.map((v, i) => (
            <li key={i}>{v}</li>
          ))}
        </ul>
      )}
    </div>
  )
}
