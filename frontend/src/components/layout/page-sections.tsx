import { type ElementType, type ReactNode } from 'react'
import { Link, useNavigate } from '@tanstack/react-router'
import { cn } from '@/lib/utils'
import { buttonVariants } from '@/components/ui/button'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Separator } from '@/components/ui/separator'
import { ActionsEntete } from './actions-entete'
import { Header } from './header'
import { Main } from './main'

export type SectionPage = {
  cle: string
  titre: string
  icon?: ElementType
  description?: string
  /** Section portée par une route ; sinon, choisie par `onChoisir`. */
  url?: string
}

type PageSectionsProps = {
  titre: string
  description?: ReactNode
  sections: SectionPage[]
  active?: string
  onChoisir?: (cle: string) => void
  children: ReactNode
}

/**
 * Page à sections, mise en page comme /settings du gabarit : menu des sections à gauche (liste déroulante
 * sur mobile), en-tête de la section puis son contenu à droite, qui défile seul.
 */
export function PageSections({
  titre,
  description,
  sections,
  active,
  onChoisir,
  children,
}: PageSectionsProps) {
  const navigate = useNavigate()
  const section = sections.find((s) => s.cle === active)
  const choisir = (cle: string) => {
    const url = sections.find((s) => s.cle === cle)?.url
    if (url) navigate({ to: url })
    else onChoisir?.(cle)
  }
  const classeLien = (s: SectionPage) =>
    cn(
      buttonVariants({ variant: 'ghost' }),
      active === s.cle
        ? 'bg-muted hover:bg-accent'
        : 'hover:bg-accent hover:underline',
      'justify-start'
    )

  return (
    <>
      <Header fixed>
        <ActionsEntete />
      </Header>
      <Main fixed>
        <div className='space-y-0.5'>
          <h1 className='mb-2 text-[1.75rem] leading-tight font-bold tracking-tight'>
            {titre}
          </h1>
          {description && (
            <p className='text-muted-foreground'>{description}</p>
          )}
        </div>
        <Separator className='my-4 lg:my-6' />
        <div className='relative flex flex-1 flex-col space-y-2 overflow-hidden md:flex-row md:space-y-0 md:space-x-12'>
          <aside className='top-0 md:sticky md:w-56 md:shrink-0 md:overflow-y-auto'>
            <div className='p-1 md:hidden'>
              <Select value={active} onValueChange={choisir}>
                <SelectTrigger className='h-12 w-full sm:w-64'>
                  <SelectValue placeholder='Section' />
                </SelectTrigger>
                <SelectContent>
                  {sections.map((s) => (
                    <SelectItem key={s.cle} value={s.cle}>
                      <div className='flex items-center gap-2'>
                        {s.icon && <s.icon className='size-4' />}
                        {s.titre}
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <nav className='hidden w-full flex-col gap-1 md:flex'>
              {sections.map((s) =>
                s.url ? (
                  <Link key={s.cle} to={s.url} className={classeLien(s)}>
                    {s.icon && <s.icon className='me-2' />}
                    {s.titre}
                  </Link>
                ) : (
                  <button
                    key={s.cle}
                    type='button'
                    onClick={() => choisir(s.cle)}
                    className={classeLien(s)}
                  >
                    {s.icon && <s.icon className='me-2' />}
                    {s.titre}
                  </button>
                )
              )}
            </nav>
          </aside>
          {/* relative : les éléments en position absolue (sr-only) restent dans le défilement, sans
              allonger la page. */}
          <div className='relative min-w-0 flex-1 overflow-y-auto p-1 pe-6'>
            {section && (
              <>
                <h3 className='text-lg font-medium'>{section.titre}</h3>
                <p className='text-muted-foreground text-sm'>
                  {section.description}
                </p>
                <Separator className='mt-4 mb-8' />
              </>
            )}
            {children}
          </div>
        </div>
      </Main>
    </>
  )
}
