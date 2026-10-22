import { Link } from '@tanstack/react-router'
import { Settings } from 'lucide-react'
import { useLayout } from '@/context/layout-provider'
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
import { AppTitle } from './app-title'
import { NavGroup } from './nav-group'
import { SelecteurOperateur } from './selecteur-operateur'
import { type Espace } from './types'

type AppSidebarProps = {
  espace: Espace
}

/**
 * Barre latérale ; sur mobile, tiroir de navigation, qui reçoit aussi ce que l'en-tête n'a pas la place de montrer :
 * l'opérateur actif, en tête, et les paramètres de l'application (back-office), au pied. L'offre : « Voir l'offre »
 * dès `lg`, son PDF en dessous.
 */
export function AppSidebar({ espace }: AppSidebarProps) {
  const { collapsible, variant } = useLayout()
  const { isMobile, setOpenMobile } = useSidebar()
  return (
    <Sidebar collapsible={collapsible} variant={variant}>
      <SidebarHeader>
        <AppTitle espace={espace} />
        {isMobile && (
          <SelecteurOperateur
            className='w-full max-w-none justify-between sm:max-w-none'
            apresChoix={() => setOpenMobile(false)}
          />
        )}
      </SidebarHeader>
      <SidebarContent>
        {espace.navGroups.map((props) => (
          <NavGroup key={props.title} {...props} />
        ))}
      </SidebarContent>
      <SidebarFooter className='pb-16 max-lg:hidden'>
        <BoutonOffre />
      </SidebarFooter>
      <SidebarFooter className='lg:hidden'>
        {isMobile && espace.url === '/bo' && (
          <SidebarMenu>
            <SidebarMenuItem>
              <SidebarMenuButton asChild>
                <Link to='/bo/parametrage' onClick={() => setOpenMobile(false)}>
                  <Settings />
                  Paramètres de l'application
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
