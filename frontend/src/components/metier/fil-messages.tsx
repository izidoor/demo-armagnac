import { type ReactNode } from 'react'
import { EllipsisVertical } from 'lucide-react'
import { cn } from '@/lib/utils'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

/**
 * Briques communes aux fils de messages (messagerie) et aux carnets de notes (notes internes) : avatar de l'auteur,
 * menu d'actions révélé au survol, séparateurs ; les dates du fil sont dans `dates-fil`.
 */

/** Les initiales d'un nom : « Marie Dupont » donne « MD », « Agent » donne « AG ». */
function initiales(nom: string) {
  const mots = nom
    .trim()
    .split(/[\s-]+/)
    .filter(Boolean)
  if (mots.length === 0) return '?'
  if (mots.length === 1) return mots[0].slice(0, 2).toUpperCase()
  return (mots[0][0] + mots[mots.length - 1][0]).toUpperCase()
}

export function AvatarAuteur({
  nom,
  className,
}: {
  nom: string
  /** Couleurs de l'avatar (fond, texte). */
  className?: string
}) {
  return (
    <Avatar className='size-8' title={nom}>
      <AvatarFallback className={cn('text-xs font-semibold', className)}>
        {initiales(nom)}
      </AvatarFallback>
    </Avatar>
  )
}

export type ActionElement = {
  libelle: string
  icone: ReactNode
  onClick: () => void
  destructive?: boolean
}

/**
 * Le menu « ⋮ » d'un message ou d'une note : discret, il apparaît au survol ou au focus de l'élément (classe
 * `group`) ; toujours visible sur écran tactile. Rien n'est affiché sans action.
 */
export function MenuActions({
  actions,
  libelle,
}: {
  actions: ActionElement[]
  libelle: string
}) {
  if (actions.length === 0) return null
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant='ghost'
          size='icon'
          className='text-muted-foreground size-7 shrink-0 transition-opacity group-focus-within:opacity-100 group-hover:opacity-100 data-[state=open]:opacity-100 [@media(hover:hover)]:opacity-0'
          aria-label={libelle}
        >
          <EllipsisVertical className='size-4' />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align='end'>
        {actions.map((a) => (
          <DropdownMenuItem
            key={a.libelle}
            onClick={a.onClick}
            className={cn(a.destructive && 'text-destructive')}
          >
            {a.icone} {a.libelle}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}

/** Un séparateur horizontal titré : jour d'un fil, début des nouveaux messages. */
export function Separateur({
  children,
  className,
}: {
  children: ReactNode
  className?: string
}) {
  return (
    <li
      role='separator'
      className={cn(
        'text-muted-foreground flex items-center gap-3 py-1 text-xs font-medium',
        className
      )}
    >
      <span className='h-px flex-1 bg-current opacity-25' />
      <span className='first-letter:uppercase'>{children}</span>
      <span className='h-px flex-1 bg-current opacity-25' />
    </li>
  )
}
