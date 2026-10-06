import { useMutation } from '@tanstack/react-query'
import { toast } from 'sonner'
import { ConfirmDialog } from '@/components/confirm-dialog'

type ChangementEtatProps = {
  /** La valeur à désactiver ou réactiver ; null : fenêtre fermée. */
  valeur: { libelle: string; actif: boolean } | null
  onFermer: () => void
  changer: () => Promise<unknown>
  onSucces: () => void
}

/**
 * Confirmation de la désactivation ou de la réactivation d'une valeur. Une valeur désactivée n'est
 * plus proposée à la saisie, mais reste affichée là où elle est déjà utilisée (RG-REF-04).
 */
export function ChangementEtat({
  valeur,
  onFermer,
  changer,
  onSucces,
}: ChangementEtatProps) {
  const mutation = useMutation({
    mutationFn: changer,
    onSuccess: () => {
      toast.success(
        valeur?.actif
          ? `« ${valeur.libelle} » désactivé.`
          : `« ${valeur?.libelle} » réactivé.`
      )
      onSucces()
      onFermer()
    },
  })
  return (
    <ConfirmDialog
      open={valeur !== null}
      onOpenChange={(ouvert) => !ouvert && onFermer()}
      title={valeur?.actif ? 'Désactiver' : 'Réactiver'}
      desc={
        valeur?.actif
          ? `« ${valeur.libelle} » ne sera plus proposé à la saisie, mais restera affiché là où il est déjà utilisé.`
          : `« ${valeur?.libelle} » sera de nouveau proposé à la saisie.`
      }
      confirmText={valeur?.actif ? 'Désactiver' : 'Réactiver'}
      cancelBtnText='Annuler'
      destructive={valeur?.actif}
      isLoading={mutation.isPending}
      handleConfirm={() => mutation.mutate()}
    />
  )
}
