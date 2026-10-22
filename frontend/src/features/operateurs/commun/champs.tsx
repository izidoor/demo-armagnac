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
            <div className='grid gap-2 @min-[48rem]/content:grid-cols-2'>
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

/**
 * Sur un contenu étroit (moins de 48rem : téléphone, tablette, petite fenêtre barre latérale ouverte), un champ en
 * lecture tient sur une ligne, libellé à gauche, valeur à droite ; au-delà, libellé au-dessus de la valeur. Les
 * grilles de champs suivent le même seuil (`@min-[48rem]/content:grid-cols-…`) : en dessous, une colonne, où la ligne a
 * sa place.
 */
const LIGNE_ETROITE =
  '@max-[48rem]/content:flex @max-[48rem]/content:items-baseline @max-[48rem]/content:justify-between @max-[48rem]/content:gap-4 @max-[48rem]/content:space-y-0'
/** Le libellé garde sa largeur, la moitié au plus : un libellé long passe à la ligne plutôt que de couper la valeur. */
const LIBELLE_ETROIT =
  '@max-[48rem]/content:max-w-[50%] @max-[48rem]/content:shrink-0'
const VALEUR_ETROITE =
  '@max-[48rem]/content:min-w-0 @max-[48rem]/content:text-end @max-[48rem]/content:[overflow-wrap:anywhere]'

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
    <div className={cn('space-y-1', LIGNE_ETROITE, className)}>
      <div className={cn('text-sm font-medium', LIBELLE_ETROIT)}>{label}</div>
      <div className={cn('text-muted-foreground text-sm', VALEUR_ETROITE)}>
        {valeur || '—'}
      </div>
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
    <div className={cn('space-y-1', label && LIGNE_ETROITE, className)}>
      {label && (
        <div className={cn('text-sm font-medium', LIBELLE_ETROIT)}>{label}</div>
      )}
      {valeurs.length === 0 ? (
        <div
          className={cn(
            'text-muted-foreground text-sm italic',
            label && VALEUR_ETROITE
          )}
        >
          {vide}
        </div>
      ) : (
        <ul
          className={cn(
            'text-muted-foreground space-y-1 text-sm',
            label && VALEUR_ETROITE
          )}
        >
          {valeurs.map((v, i) => (
            <li key={i}>{v}</li>
          ))}
        </ul>
      )}
    </div>
  )
}
