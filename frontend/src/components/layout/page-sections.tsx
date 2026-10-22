import { type ElementType, type ReactNode } from 'react'
import { Link, useNavigate } from '@tanstack/react-router'
import { cn } from '@/lib/utils'
import { useIsMobile } from '@/hooks/use-mobile'
import { useTitreSorti } from '@/hooks/use-titre-sorti'
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
import { Header, type HeaderProps } from './header'
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
  /** À droite du titre (« Se déconnecter » de Mon compte). */
  actions?: ReactNode
  /** Sur mobile, les sections en onglets plutôt qu'en liste déroulante : pour une page qui en a peu. */
  onglets?: boolean
  /** Une page de second niveau dans une section (une classe de TVA) : la flèche de retour de l'en-tête (`Header`). */
  retour?: HeaderProps['retour']
  children: ReactNode
}

/**
 * Page à sections, mise en page comme /settings du gabarit : menu des sections à gauche (liste déroulante sur mobile,
 * ou onglets en pastilles, comme ceux de la fiche opérateur, pour une page qui en a peu), en-tête de la section puis son
 * contenu à droite, qui défile seul (sur mobile, toute la page défile).
 */
export function PageSections({
  titre,
  description,
  sections,
  active,
  onChoisir,
  actions,
  onglets = false,
  retour,
  children,
}: PageSectionsProps) {
  const navigate = useNavigate()
  // Sur écran large, la page tient dans la fenêtre : le menu reste en place, la section défile seule. Sur mobile, la
  // page entière défile, comme les autres.
  const mobile = useIsMobile()
  const [refTitre, titreSorti] = useTitreSorti()
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
      <Header fixed titre={titre} titreVisible={titreSorti} retour={retour}>
        <ActionsEntete />
      </Header>
      <Main fixed={!mobile}>
        <div className='flex flex-wrap items-end justify-between gap-x-4 gap-y-2'>
          <div className='space-y-0.5'>
            <h1
              ref={refTitre}
              className='mb-2 text-[1.75rem] leading-tight font-bold tracking-tight'
            >
              {titre}
            </h1>
            {description && (
              <p className='text-muted-foreground'>{description}</p>
            )}
          </div>
          {actions}
        </div>
        <Separator className='my-4 lg:my-6' />
        <div className='relative flex flex-1 flex-col space-y-2 overflow-hidden md:flex-row md:space-y-0 md:space-x-12'>
          <aside className='top-0 md:sticky md:w-56 md:shrink-0 md:overflow-y-auto'>
            {onglets ? (
              <nav
                aria-label='Sections'
                className='flex flex-wrap gap-2 p-1 md:hidden'
              >
                {sections.map((s) => (
                  <button
                    key={s.cle}
                    type='button'
                    aria-pressed={active === s.cle}
                    onClick={() => choisir(s.cle)}
                    className={cn(
                      'rounded-full border px-3 py-1 text-sm transition-colors',
                      active === s.cle
                        ? 'bg-primary text-primary-foreground border-primary'
                        : 'bg-card hover:bg-muted'
                    )}
                  >
                    {s.titre}
                  </button>
                ))}
              </nav>
            ) : (
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
            )}
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
          <div className='relative min-w-0 flex-1 overflow-y-auto p-1 md:pe-6'>
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
