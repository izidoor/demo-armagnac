import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  consulterDoubleAuthentification,
  definirDoubleAuthentification,
} from '@/api/generated'
import { Switch } from '@/components/ui/switch'
import { PageMetier } from '@/components/metier/page-metier'

const CLE = ['double-authentification']

/**
 * Section « Sécurité » des paramètres de l'application (spec 03 §6.2) : la double authentification pour
 * toute l'application (RG-AUT-16), son état pour le personnel du BNIA, l'interrupteur pour un ADMIN du
 * BNIA. Chaque changement va au journal des connexions.
 */
export function SectionSecurite() {
  const queryClient = useQueryClient()
  const reglage = useQuery({
    queryKey: CLE,
    queryFn: () =>
      consulterDoubleAuthentification({ throwOnError: true }).then(
        (r) => r.data
      ),
  })
  const changer = useMutation({
    mutationFn: (active: boolean) =>
      definirDoubleAuthentification({
        body: { active },
        throwOnError: true,
      }).then((r) => r.data),
    onSuccess: (r) => {
      queryClient.setQueryData(CLE, r)
      void queryClient.invalidateQueries({ queryKey: ['connexions'] })
      toast.success(
        r.active
          ? 'Double authentification activée.'
          : 'Double authentification désactivée.'
      )
    },
  })
  if (!reglage.data) return null
  const { active, modifiable } = reglage.data
  return (
    <PageMetier
      titre='Double authentification'
      description='Un code reçu par e-mail, après le mot de passe, pour tous les utilisateurs.'
    >
      <div className='flex items-start gap-3 rounded-md border p-4'>
        <Switch
          id='double-authentification'
          checked={active}
          disabled={!modifiable || changer.isPending}
          onCheckedChange={(v) => changer.mutate(v)}
        />
        <div className='space-y-1'>
          <label
            htmlFor='double-authentification'
            className='text-sm leading-none font-medium'
          >
            Double authentification {active ? 'activée' : 'désactivée'}
          </label>
          <p className='text-muted-foreground text-sm'>
            {active
              ? 'Après le mot de passe, chaque utilisateur saisit un code reçu par e-mail, sauf sur un navigateur de confiance.'
              : 'La session s’ouvre après le mot de passe, sans code.'}{' '}
            {modifiable
              ? 'Vaut pour toute l’application.'
              : 'Réglée par un administrateur du BNIA.'}
          </p>
        </div>
      </div>
    </PageMetier>
  )
}
