import { Link } from '@tanstack/react-router'
import { CircleUserRound, LogOut, UserRound } from 'lucide-react'
import useDialogState from '@/hooks/use-dialog-state'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { SignOutDialog } from '@/components/sign-out-dialog'
import { espaceDe, useMoi } from '@/features/session/session'

/** Menu du profil, en haut à droite : l'utilisateur connecté, « Mon compte », la déconnexion (spec 03 §6.1). */
export function ProfileDropdown() {
  const [open, setOpen] = useDialogState()
  const { moi, actif } = useMoi()
  const compte =
    actif && espaceDe(actif) === 'bo' ? '/bo/compte' : '/portail/compte'

  return (
    <>
      <DropdownMenu modal={false}>
        <DropdownMenuTrigger asChild>
          <Button
            variant='ghost'
            size='icon'
            className='rounded-full'
            aria-label='Mon profil'
          >
            <CircleUserRound className='size-6' />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent className='w-64' align='end' forceMount>
          <DropdownMenuLabel className='font-normal'>
            <div className='flex flex-col gap-1.5'>
              <p className='text-sm leading-none font-medium'>
                {moi.nomComplet}
              </p>
              <p className='text-muted-foreground text-xs leading-none'>
                {moi.email}
              </p>
              {actif && (
                <p className='text-muted-foreground text-xs leading-none'>
                  {actif.raisonSociale}
                </p>
              )}
            </div>
          </DropdownMenuLabel>
          <DropdownMenuSeparator />
          <DropdownMenuGroup>
            <DropdownMenuItem asChild>
              <Link to={compte}>
                <UserRound />
                Mon compte
              </Link>
            </DropdownMenuItem>
          </DropdownMenuGroup>
          <DropdownMenuSeparator />
          <DropdownMenuItem variant='destructive' onClick={() => setOpen(true)}>
            <LogOut />
            Se déconnecter
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>

      <SignOutDialog open={!!open} onOpenChange={setOpen} />
    </>
  )
}
