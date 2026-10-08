import { useQueryClient } from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'
import { seDeconnecter } from '@/api/generated'
import { ConfirmDialog } from '@/components/confirm-dialog'

interface SignOutDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

/** Ferme la session côté serveur (RG-AUT-09), oublie les données en cache, revient à la connexion. */
export function SignOutDialog({ open, onOpenChange }: SignOutDialogProps) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const handleSignOut = async () => {
    try {
      await seDeconnecter({ throwOnError: true })
    } finally {
      queryClient.clear()
      await navigate({ to: '/connexion', replace: true })
    }
  }

  return (
    <ConfirmDialog
      open={open}
      onOpenChange={onOpenChange}
      title='Se déconnecter'
      desc='Voulez-vous vraiment vous déconnecter ? Il faudra vous reconnecter pour accéder à votre compte.'
      confirmText='Se déconnecter'
      destructive
      handleConfirm={handleSignOut}
      className='sm:max-w-sm'
    />
  )
}
