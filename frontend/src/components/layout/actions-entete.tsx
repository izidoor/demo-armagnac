import { Link, useLocation } from '@tanstack/react-router'
import { Settings } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/ui/tooltip'
import { ProfileDropdown } from '@/components/profile-dropdown'
import { Search } from '@/components/search'
import { ThemeSwitch } from '@/components/theme-switch'
import { SelecteurOperateur } from './selecteur-operateur'

/**
 * Actions à droite de l'en-tête, sur toutes les pages : recherche et actions rapides (⌘K), opérateur actif, thème,
 * paramètres de l'application (back-office seulement), profil. Sur mobile, la loupe seule : l'opérateur actif, les
 * paramètres et « Mon compte » passent dans le tiroir de navigation (`AppSidebar`), le thème dans « Mon compte ».
 */
export function ActionsEntete() {
  const { pathname } = useLocation()
  return (
    <div className='ms-auto flex items-center gap-4'>
      <Search placeholder='Rechercher' />
      <SelecteurOperateur className='max-md:hidden' />
      {/* Masqué, pas retiré : il garde la couleur de la barre d'état (theme-color) au thème choisi. */}
      <span className='contents max-md:hidden'>
        <ThemeSwitch />
      </span>
      {pathname.startsWith('/bo') && (
        <Tooltip>
          <TooltipTrigger asChild>
            <Button
              asChild
              size='icon'
              variant='ghost'
              className='scale-95 rounded-full max-md:hidden'
              aria-label="Paramètres de l'application"
            >
              <Link to='/bo/parametrage'>
                <Settings className='size-[1.2rem]' aria-hidden='true' />
              </Link>
            </Button>
          </TooltipTrigger>
          <TooltipContent>Paramètres de l'application</TooltipContent>
        </Tooltip>
      )}
      <span className='contents max-md:hidden'>
        <ProfileDropdown />
      </span>
    </div>
  )
}
