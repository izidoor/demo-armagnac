import { type Column } from '@tanstack/react-table'
import {
  ArrowDown,
  ArrowUp,
  ArrowUpDown,
  Check,
  EllipsisVertical,
  Filter,
  Group,
  Pin,
  PinOff,
  Ungroup,
} from 'lucide-react'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
  CommandSeparator,
} from '@/components/ui/command'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'

const COLLATION = new Intl.Collator('fr', { numeric: true })

/**
 * En-tête d'une colonne de TanStack Table, en français : un clic trie (croissant, décroissant, sans tri) ;
 * l'entonnoir filtre par valeurs, à cocher parmi celles des lignes (avec leur nombre), avec une recherche ; le
 * menu « ⋮ » épingle la colonne à gauche ou à droite, ou regroupe les lignes par ses valeurs, quand la table le
 * permet. La colonne doit avoir `filterFn: filtreValeurs` et la table les modèles à facettes.
 */
export function EnteteFiltrable<T>({
  column,
  titre,
  affichage = (v) => v,
  ordre = COLLATION.compare,
  aDroite = false,
}: {
  column: Column<T, unknown>
  /** Le titre ; par défaut celui des métadonnées de la colonne (`meta.titre`). */
  titre?: string
  /** Libellé d'une valeur dans la liste du filtre. */
  affichage?: (valeur: string) => string
  /** Ordre des valeurs dans la liste du filtre ; alphabétique par défaut. */
  ordre?: (a: string, b: string) => number
  aDroite?: boolean
}) {
  const libelle = titre ?? column.columnDef.meta?.titre ?? column.id
  const sens = column.getIsSorted()
  const filtrable = column.getCanFilter()
  const retenues = new Set((column.getFilterValue() as string[]) ?? [])
  const facettes = filtrable ? column.getFacetedUniqueValues() : new Map()
  const valeurs = [...facettes.keys()]
    .map((v) => String(v ?? ''))
    .filter((v, i, t) => t.indexOf(v) === i)
    .sort(ordre)
  const epinglable = column.getCanPin()
  const groupable = column.getCanGroup()
  const epinglee = column.getIsPinned()
  const changer = (suivantes: Set<string>) =>
    column.setFilterValue(suivantes.size ? [...suivantes] : undefined)

  return (
    <div
      className={cn('flex items-center gap-0.5', aDroite && 'flex-row-reverse')}
    >
      <button
        type='button'
        onClick={column.getToggleSortingHandler()}
        className={cn(
          'hover:text-foreground inline-flex items-center gap-1 whitespace-nowrap',
          aDroite && 'flex-row-reverse'
        )}
        title='Trier'
      >
        {libelle}
        {sens === 'asc' ? (
          <ArrowUp className='size-3.5' />
        ) : sens === 'desc' ? (
          <ArrowDown className='size-3.5' />
        ) : (
          <ArrowUpDown className='size-3.5 opacity-30' />
        )}
      </button>
      {filtrable && (
        <Popover>
          <PopoverTrigger asChild>
            <Button
              variant='ghost'
              size='icon'
              className={cn(
                'size-6',
                retenues.size > 0
                  ? 'text-primary'
                  : 'text-muted-foreground opacity-50 hover:opacity-100'
              )}
              aria-label={`Filtrer « ${libelle} »`}
            >
              <Filter className='size-3.5' />
            </Button>
          </PopoverTrigger>
          <PopoverContent className='w-64 p-0' align='start'>
            <Command>
              <CommandInput placeholder='Rechercher…' />
              <CommandList>
                <CommandEmpty>Aucune valeur.</CommandEmpty>
                <CommandGroup>
                  {valeurs.map((v) => {
                    const coche = retenues.has(v)
                    return (
                      <CommandItem
                        key={v}
                        value={affichage(v) || '(vide)'}
                        onSelect={() => {
                          const suivantes = new Set(retenues)
                          if (coche) suivantes.delete(v)
                          else suivantes.add(v)
                          changer(suivantes)
                        }}
                      >
                        <div
                          className={cn(
                            'border-primary flex size-4 items-center justify-center rounded-sm border',
                            coche
                              ? 'bg-primary text-primary-foreground'
                              : 'opacity-50 [&_svg]:invisible'
                          )}
                        >
                          <Check className='size-3.5' />
                        </div>
                        <span className='truncate'>
                          {affichage(v) || '(vide)'}
                        </span>
                        <span className='text-muted-foreground ms-auto font-mono text-xs'>
                          {facettes.get(v)}
                        </span>
                      </CommandItem>
                    )
                  })}
                </CommandGroup>
                {retenues.size > 0 && (
                  <>
                    <CommandSeparator />
                    <CommandGroup>
                      <CommandItem
                        onSelect={() => changer(new Set())}
                        className='justify-center'
                      >
                        Effacer le filtre
                      </CommandItem>
                    </CommandGroup>
                  </>
                )}
              </CommandList>
            </Command>
          </PopoverContent>
        </Popover>
      )}
      {(epinglable || groupable) && (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant='ghost'
              size='icon'
              className={cn(
                'size-6',
                epinglee || column.getIsGrouped()
                  ? 'text-primary'
                  : 'text-muted-foreground opacity-50 hover:opacity-100'
              )}
              aria-label={`Options de la colonne « ${libelle} »`}
            >
              <EllipsisVertical className='size-3.5' />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align='start'>
            {epinglable && (
              <>
                {epinglee !== 'left' && (
                  <DropdownMenuItem onSelect={() => column.pin('left')}>
                    <Pin className='size-4' /> Épingler à gauche
                  </DropdownMenuItem>
                )}
                {epinglee !== 'right' && (
                  <DropdownMenuItem onSelect={() => column.pin('right')}>
                    <Pin className='size-4' /> Épingler à droite
                  </DropdownMenuItem>
                )}
                {epinglee && (
                  <DropdownMenuItem onSelect={() => column.pin(false)}>
                    <PinOff className='size-4' /> Désépingler
                  </DropdownMenuItem>
                )}
              </>
            )}
            {epinglable && groupable && <DropdownMenuSeparator />}
            {groupable && (
              <DropdownMenuItem onSelect={column.getToggleGroupingHandler()}>
                {column.getIsGrouped() ? (
                  <>
                    <Ungroup className='size-4' /> Ne plus regrouper
                  </>
                ) : (
                  <>
                    <Group className='size-4' /> Regrouper par cette colonne
                  </>
                )}
              </DropdownMenuItem>
            )}
          </DropdownMenuContent>
        </DropdownMenu>
      )}
    </div>
  )
}
