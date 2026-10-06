import { useMutation } from '@tanstack/react-query'
import { toast } from 'sonner'
import { ConfirmDialog } from '@/components/confirm-dialog'

type SuppressionProps = {
  /** La valeur à supprimer ; null : fenêtre fermée. */
  valeur: { libelle: string } | null
  onFermer: () => void
  supprimer: () => Promise<unknown>
  onSucces: () => void
}

/**
 * Confirmation de la suppression d'une valeur. Le serveur la refuse (409) si d'autres données y font
 * référence : il faut alors la désactiver (RG-REF-04).
 */
export function Suppression({
  valeur,
  onFermer,
  supprimer,
  onSucces,
}: SuppressionProps) {
  const mutation = useMutation({
    mutationFn: supprimer,
    onSuccess: () => {
      toast.success(`« ${valeur?.libelle} » supprimé.`)
      onSucces()
      onFermer()
    },
    onError: onFermer,
  })
  return (
    <ConfirmDialog
      open={valeur !== null}
      onOpenChange={(ouvert) => !ouvert && onFermer()}
      title='Supprimer'
      desc={`« ${valeur?.libelle} » sera supprimé définitivement. Si d'autres données l'utilisent, la suppression sera refusée : désactivez-le plutôt.`}
      confirmText='Supprimer'
      cancelBtnText='Annuler'
      destructive
      isLoading={mutation.isPending}
      handleConfirm={() => mutation.mutate()}
    />
  )
}
