import { Link } from '@tanstack/react-router'
import { Menu, X } from 'lucide-react'
import { cn } from '@/lib/utils'
import {
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from '@/components/ui/sidebar'
import { Button } from '../ui/button'
import { type Espace } from './types'

type AppTitleProps = {
  espace: Espace
}

/**
 * Titre de l'espace et bouton du menu ; l'opérateur actif se choisit dans l'en-tête (`SelecteurOperateur`).
 * Repliée en icônes, la sidebar ne montre que le bouton : c'est le seul, l'en-tête n'en a que sur mobile.
 */
export function AppTitle({ espace }: AppTitleProps) {
  const { setOpenMobile, state, isMobile } = useSidebar()

  if (state === 'collapsed' && !isMobile)
    return (
      <SidebarMenu>
        <SidebarMenuItem>
          <ToggleSidebar />
        </SidebarMenuItem>
      </SidebarMenu>
    )
  return (
    <SidebarMenu>
      <SidebarMenuItem>
        <SidebarMenuButton
          size='lg'
          className='gap-0 py-0 hover:bg-transparent active:bg-transparent'
          asChild
        >
          <div>
            <Link
              to={espace.url}
              onClick={() => setOpenMobile(false)}
              className='grid flex-1 text-start text-sm leading-tight'
            >
              <span className='truncate font-bold max-md:text-base'>
                {espace.nom}
              </span>
              <span className='truncate text-xs max-md:text-sm'>
                {espace.description}
              </span>
            </Link>
            <ToggleSidebar />
          </div>
        </SidebarMenuButton>
      </SidebarMenuItem>
    </SidebarMenu>
  )
}

/** Ouvre ou ferme la sidebar ; sur mobile, une croix, qui ferme le tiroir. */
export function ToggleSidebar({
  className,
  onClick,
  ...props
}: React.ComponentProps<typeof Button>) {
  const { toggleSidebar } = useSidebar()
  return (
    <Button
      data-sidebar='trigger'
      data-slot='sidebar-trigger'
      variant='ghost'
      size='icon'
      className={cn('aspect-square size-8 max-md:scale-125', className)}
      onClick={(event) => {
        onClick?.(event)
        toggleSidebar()
      }}
      {...props}
    >
      <X className='md:hidden' />
      <Menu className='max-md:hidden' />
      <span className='sr-only'>Afficher ou masquer le menu</span>
    </Button>
  )
}
