import { useLayout } from '@/context/layout-provider'
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarRail,
} from '@/components/ui/sidebar'
import { AppTitle } from './app-title'
import { NavGroup } from './nav-group'
import { NavUser } from './nav-user'
import { type Espace } from './types'

type AppSidebarProps = {
  espace: Espace
}

export function AppSidebar({ espace }: AppSidebarProps) {
  const { collapsible, variant } = useLayout()
  return (
    <Sidebar collapsible={collapsible} variant={variant}>
      <SidebarHeader>
        <AppTitle espace={espace} />
      </SidebarHeader>
      <SidebarContent>
        {espace.navGroups.map((props) => (
          <NavGroup key={props.title} {...props} />
        ))}
      </SidebarContent>
      <SidebarFooter>
        <NavUser espace={espace} />
      </SidebarFooter>
      <SidebarRail />
    </Sidebar>
  )
}
