import { Link } from '@tanstack/react-router'
import { CircleUserRound, Settings } from 'lucide-react'
import { useLayout } from '@/context/layout-provider'
import { Separator } from '@/components/ui/separator'
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarRail,
  useSidebar,
} from '@/components/ui/sidebar'
import { BoutonOffre, LienOffrePdf } from '@/features/offre/offre'
import { useMoi } from '@/features/session/session'
import { AppTitle, ToggleSidebar } from './app-title'
import { NavGroup } from './nav-group'
import { SelecteurOperateur } from './selecteur-operateur'
import { type Espace } from './types'

type AppSidebarProps = {
  espace: Espace
}

/**
 * Barre latérale ; sur mobile, tiroir de navigation, sans le titre de l'espace, qui reçoit ce que l'en-tête n'a pas la
 * place de montrer : « Mon compte » en tête, à côté de la croix, et sous un trait l'opérateur actif (pour qui en a
 * plusieurs) ; les paramètres (back-office), au pied. L'offre : « Voir l'offre » dès `lg`, son PDF
 * en dessous.
 */
export function AppSidebar({ espace }: AppSidebarProps) {
  const { collapsible, variant } = useLayout()
  const { isMobile, setOpenMobile } = useSidebar()
  const { moi } = useMoi()
  const plusieursOperateurs =
    moi.rattachements.filter((r) => r.utilisable).length > 1
  return (
    <Sidebar collapsible={collapsible} variant={variant}>
      <SidebarHeader className='bg-sidebar-bandeau border-sidebar-trait border-b'>
        {isMobile ? (
          <>
            <div className='flex items-center gap-2'>
              <SidebarMenu className='min-w-0 flex-1'>
                <SidebarMenuItem>
                  <SidebarMenuButton asChild size='lg'>
                    <Link
                      to={
                        espace.url === '/bo' ? '/bo/compte' : '/portail/compte'
                      }
                      onClick={() => setOpenMobile(false)}
                    >
                      <CircleUserRound />
                      <span className='grid min-w-0 leading-tight'>
                        <span className='truncate font-medium'>Mon compte</span>
                        <span className='text-muted-foreground truncate text-sm'>
                          {moi.nomComplet} · {moi.email}
                        </span>
                      </span>
                    </Link>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              </SidebarMenu>
              <ToggleSidebar />
            </div>
            {plusieursOperateurs && (
              <>
                <Separator className='bg-sidebar-trait' />
                <SelecteurOperateur
                  className='h-9 w-full max-w-none justify-between sm:max-w-none [&>span]:text-sm'
                  apresChoix={() => setOpenMobile(false)}
                />
              </>
            )}
          </>
        ) : (
          <AppTitle espace={espace} />
        )}
      </SidebarHeader>
      <SidebarContent className='max-md:py-4' ref={montrerPageActive}>
        {espace.navGroups.map((props) => (
          <NavGroup key={props.title} {...props} />
        ))}
      </SidebarContent>
      <SidebarFooter className='bg-sidebar-bandeau border-sidebar-trait border-t pb-16 max-lg:hidden'>
        <BoutonOffre />
      </SidebarFooter>
      <SidebarFooter className='bg-sidebar-bandeau border-sidebar-trait border-t pt-4 lg:hidden'>
        {isMobile && espace.url === '/bo' && (
          <SidebarMenu>
            <SidebarMenuItem>
              <SidebarMenuButton
                asChild
                className='[font-variant-caps:all-small-caps]'
              >
                <Link to='/bo/parametrage' onClick={() => setOpenMobile(false)}>
                  <Settings />
                  Paramètres
                </Link>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        )}
        <LienOffrePdf />
      </SidebarFooter>
      <SidebarRail />
    </Sidebar>
  )
}

/**
 * Sur mobile, à chaque ouverture du tiroir, qui recrée la liste défilée tout en haut : si l'entrée de la page courante
 * est hors de vue, la liste défile pour la centrer.
 */
function montrerPageActive(liste: HTMLDivElement | null) {
  if (!liste?.closest('[data-mobile="true"]')) return
  requestAnimationFrame(() => {
    const entree = liste.querySelector<HTMLElement>('[data-active="true"]')
    if (!entree) return
    const cadre = liste.getBoundingClientRect()
    const position = entree.getBoundingClientRect()
    if (position.top >= cadre.top && position.bottom <= cadre.bottom) return
    liste.scrollTop +=
      position.top - cadre.top - (cadre.height - position.height) / 2
  })
}
