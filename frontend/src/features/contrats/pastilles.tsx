import { cn } from '@/lib/utils'

/** Des onglets en pastilles, avec leur compte, qui passent à la ligne sur un contenu étroit. */
export function Pastilles<T extends string>({
  valeur,
  onChanger,
  onglets,
}: {
  valeur: T
  onChanger: (valeur: T) => void
  onglets: { valeur: T; libelle: string; nombre?: number }[]
}) {
  return (
    <nav aria-label='Onglets' className='mb-4 flex flex-wrap gap-2'>
      {onglets.map((o) => (
        <button
          key={o.valeur}
          type='button'
          aria-pressed={o.valeur === valeur}
          onClick={() => onChanger(o.valeur)}
          className={cn(
            'inline-flex items-center gap-1.5 rounded-full border px-3 py-1 text-sm transition-colors',
            o.valeur === valeur
              ? 'bg-primary text-primary-foreground border-primary'
              : 'bg-card hover:bg-muted'
          )}
        >
          {o.libelle}
          {o.nombre !== undefined && (
            <span
              className={cn(
                'rounded-full px-1.5 text-xs tabular-nums',
                o.valeur === valeur ? 'bg-primary-foreground/20' : 'bg-muted'
              )}
            >
              {o.nombre}
            </span>
          )}
        </button>
      ))}
    </nav>
  )
}
