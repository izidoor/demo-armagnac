import { Link, useLocation } from '@tanstack/react-router'
import { Settings } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { ProfileDropdown } from '@/components/profile-dropdown'
import { ThemeSwitch } from '@/components/theme-switch'

/** Actions à droite de l'en-tête : thème, Paramétrage (back-office seulement), profil. */
export function ActionsEntete() {
  const { pathname } = useLocation()
  return (
    <div className='ms-auto flex items-center gap-4'>
      <ThemeSwitch />
      {pathname.startsWith('/bo') && (
        <Button
          asChild
          size='icon'
          variant='ghost'
          className='rounded-full'
          aria-label='Paramétrage'
          title='Paramétrage'
        >
          <Link to='/bo/parametrage'>
            <Settings aria-hidden='true' />
          </Link>
        </Button>
      )}
      <ProfileDropdown />
    </div>
  )
}
