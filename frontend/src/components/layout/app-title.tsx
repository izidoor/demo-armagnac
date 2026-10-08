import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from '@tanstack/react-router'
import { Check, ChevronsUpDown, Menu, X } from 'lucide-react'
import { choisirOperateurActif } from '@/api/generated'
import { cn } from '@/lib/utils'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from '@/components/ui/sidebar'
import { espaceDe, requeteMoi, useMoi } from '@/features/session/session'
import { Button } from '../ui/button'
import { type Espace } from './types'

type AppTitleProps = {
  espace: Espace
}

/**
 * Titre de l'espace et sélecteur de l'opérateur actif (RG-OPE-49, spec 02 §6) : changer d'opérateur
 * ouvre l'espace qui lui correspond, back-office pour le BNIA, portail sinon.
 */
export function AppTitle({ espace }: AppTitleProps) {
  const { setOpenMobile } = useSidebar()
  const { moi, actif } = useMoi()
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const choix = useMutation({
    mutationFn: (operateur: number) =>
      choisirOperateurActif({ body: { operateur }, throwOnError: true }),
    onSuccess: async (r) => {
      queryClient.clear()
      queryClient.setQueryData(requeteMoi.queryKey, r.data)
      const nouveau = r.data.rattachements.find(
        (x) => x.operateur === r.data.operateurOuvert
      )
      await navigate({
        to: nouveau && espaceDe(nouveau) === 'bo' ? '/bo' : '/portail',
      })
    },
  })
  const utilisables = moi.rattachements.filter((r) => r.utilisable)

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
              <span className='truncate font-bold'>{espace.nom}</span>
              <span className='truncate text-xs'>{espace.description}</span>
            </Link>
            <ToggleSidebar />
          </div>
        </SidebarMenuButton>
      </SidebarMenuItem>
      <SidebarMenuItem>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <SidebarMenuButton className='border' disabled={choix.isPending}>
              <span className='truncate text-xs'>
                {actif?.raisonSociale ?? 'Choisir un opérateur'}
              </span>
              {utilisables.length > 1 && (
                <ChevronsUpDown className='ms-auto size-4' />
              )}
            </SidebarMenuButton>
          </DropdownMenuTrigger>
          {utilisables.length > 1 && (
            <DropdownMenuContent className='min-w-64' align='start'>
              <DropdownMenuLabel className='text-muted-foreground text-xs'>
                Opérateur actif
              </DropdownMenuLabel>
              {utilisables.map((r) => (
                <DropdownMenuItem
                  key={r.id}
                  onClick={() =>
                    r.operateur !== actif?.operateur &&
                    choix.mutate(r.operateur)
                  }
                >
                  <span className='grid flex-1 leading-tight'>
                    <span>{r.raisonSociale}</span>
                    <span className='text-muted-foreground text-xs'>
                      {r.type === 'BNIA' ? 'Back-office' : 'Portail'}
                    </span>
                  </span>
                  {r.operateur === actif?.operateur && <Check />}
                </DropdownMenuItem>
              ))}
            </DropdownMenuContent>
          )}
        </DropdownMenu>
      </SidebarMenuItem>
    </SidebarMenu>
  )
}

function ToggleSidebar({
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
