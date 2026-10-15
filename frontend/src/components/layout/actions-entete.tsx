import { Link, useLocation } from '@tanstack/react-router'
import { Settings } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/ui/tooltip'
import { ProfileDropdown } from '@/components/profile-dropdown'
import { ThemeSwitch } from '@/components/theme-switch'
import { SelecteurOperateur } from './selecteur-operateur'

/** Actions à droite de l'en-tête : opérateur actif, thème, paramètres de l'application (back-office seulement), profil. */
export function ActionsEntete() {
  const { pathname } = useLocation()
  return (
    <div className='ms-auto flex items-center gap-4'>
      <SelecteurOperateur />
      <ThemeSwitch />
      {pathname.startsWith('/bo') && (
        <Tooltip>
          <TooltipTrigger asChild>
            <Button
              asChild
              size='icon'
              variant='ghost'
              className='scale-95 rounded-full'
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
      <ProfileDropdown />
    </div>
  )
}
